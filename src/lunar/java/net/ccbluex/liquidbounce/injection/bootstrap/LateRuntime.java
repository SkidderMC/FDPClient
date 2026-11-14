/*
 * FDPClient Hacked Client
 * Lunar runtime attachment by opZywl | https://lucas-lima.xyz
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.bootstrap;

import java.lang.invoke.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** State shared by the agent and Genesis copies without changing any Minecraft class schema. */
public final class LateRuntime {
    private static final String KEY = "fdpclient.lunar.late.runtime";
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Map<String, Object> SHARED = sharedRuntime();
    private LateRuntime() { }

    private static Map<String, Object> runtime() { return SHARED; }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> sharedRuntime() {
        Properties properties = System.getProperties();
        synchronized (properties) {
            Object existing = properties.get(KEY);
            if (existing != null) return (Map<String, Object>) existing;
            Map<String, Object> created = new ConcurrentHashMap<>();
            created.put("active", new AtomicBoolean());
            created.put("plans", new ConcurrentHashMap<String, Map<String, Object>>());
            created.put("dispatch", new ConcurrentHashMap<String, Map<Class<?>, Optional<MethodHandle>>>());
            created.put("proxies", new HashMap<Object, Map<String, WeakReference<Object>>>());
            created.put("proxyQueue", new ReferenceQueue<Object>());
            created.put("proxyTargets", new HashMap<Object, WeakReference<Object>>());
            created.put("targetQueue", new ReferenceQueue<Object>());
            properties.put(KEY, created);
            return created;
        }
    }

    public static boolean active() { return ((AtomicBoolean) runtime().get("active")).get(); }
    public static void activate(boolean active) { ((AtomicBoolean) runtime().get("active")).set(active); }

    public static CallSite castBootstrap(MethodHandles.Lookup caller, String ignored, MethodType signature, String owner) throws Exception {
        MethodHandle cast = MethodHandles.identity(type(owner)).asType(signature);
        MethodHandle unwrap = LOOKUP.findStatic(LateRuntime.class, "unwrap", MethodType.methodType(Object.class, Object.class));
        return new ConstantCallSite(MethodHandles.filterArguments(cast, 0, unwrap));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, Object>> plans() {
        return (Map<String, Map<String, Object>>) runtime().get("plans");
    }

    /** Plan values use only bootstrap JDK types so isolated class loaders share their identity. */
    public static void install(Map<String, Map<String, Object>> plans, ClassLoader game) {
        activate(false);
        runtime().put("loader", game);
        ((Map<?, ?>) runtime().get("dispatch")).clear();
        for (String cache : new String[]{"virtual", "types", "instances", "adapters"}) runtime().remove(cache);
        plans().clear();
        for (Map.Entry<String, Map<String, Object>> entry : plans.entrySet()) {
            Map<String, Object> plan = new HashMap<>(entry.getValue());
            plan.put("states", new HashMap<Object, Object[]>());
            plan.put("queue", new ReferenceQueue<Object>());
            plans().put(entry.getKey(), plan);
        }
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> cache(String name) {
        return (Map<K, V>) runtime().computeIfAbsent(name, unused -> new ConcurrentHashMap<K, V>());
    }

    private static Class<?> type(String owner) throws ClassNotFoundException {
        Map<String, Class<?>> types = cache("types");
        Class<?> found = types.get(owner);
        if (found == null) {
            found = Class.forName(owner.replace('/', '.'), false, (ClassLoader) runtime().get("loader"));
            types.put(owner, found);
        }
        return found;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String[]> members(Map<String, Object> plan, String kind) {
        return (Map<String, String[]>) plan.get(kind);
    }

    private static Object[] resolve(Class<?> start, String name, String descriptor, String kind) {
        String key = name + descriptor;
        boolean method = kind.equals("methods");
        for (Class<?> current = start; current != null; current = current.getSuperclass()) {
            Map<String, Object> plan = plans().get(current.getName().replace('.', '/'));
            if (plan != null && members(plan, kind).containsKey(key)) return new Object[]{plan, null};
            if (method) {
                for (Method declared : current.getDeclaredMethods())
                    if (declared.getName().equals(name) && descriptor(declared).equals(descriptor)) return new Object[]{null, declared};
            } else {
                for (Field declared : current.getDeclaredFields())
                    if (declared.getName().equals(name) && MethodType.methodType(declared.getType()).toMethodDescriptorString().substring(2).equals(descriptor))
                        return new Object[]{null, declared};
            }
        }
        return null;
    }

    private static String descriptor(Method method) {
        return MethodType.methodType(method.getReturnType(), method.getParameterTypes()).toMethodDescriptorString();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> plan(Object[] resolved) {
        return resolved == null ? null : (Map<String, Object>) resolved[0];
    }

    private static Class<?> helper(Map<String, Object> plan) throws ClassNotFoundException {
        return type((String) plan.get("helper"));
    }

    private static MethodHandle body(Map<String, Object> plan, String key) throws Exception {
        String[] method = members(plan, "methods").get(key);
        MethodType descriptor = MethodType.fromMethodDescriptorString(method[1], (ClassLoader) runtime().get("loader"));
        return MethodHandles.publicLookup().findStatic(helper(plan), method[0], descriptor);
    }

    private static MethodHandles.Lookup privateLookup(Class<?> target, MethodHandles.Lookup caller) throws Exception {
        return (MethodHandles.Lookup) MethodHandles.class
            .getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class).invoke(null, target, caller);
    }

    /** Links primitive/reference accesses once; render and movement hooks do not reflect per call. */
    public static CallSite bootstrap(MethodHandles.Lookup caller, String ignored, MethodType callType,
                                     String owner, String name, String descriptor, int opcode, String origin) throws Exception {
        MethodHandle handle;
        if (opcode == 187) {
            Constructor<?> constructor = type(owner).getDeclaredConstructor(
                MethodType.fromMethodDescriptorString(descriptor, (ClassLoader) runtime().get("loader")).parameterArray());
            constructor.setAccessible(true);
            handle = caller.unreflectConstructor(constructor);
        } else if (opcode >= 178 && opcode <= 181) {
            Object[] resolved = resolve(type(owner), name, descriptor, "fields");
            Map<String, Object> plan = plan(resolved);
            if (plan != null) {
                String[] metadata = members(plan, "fields").get(name + descriptor);
                Field field = helper(plan).getField(metadata[0]);
                handle = opcode == 178 || opcode == 180 ? LOOKUP.unreflectGetter(field) : LOOKUP.unreflectSetter(field);
                if (opcode == 180 || opcode == 181) {
                    MethodHandle state = LOOKUP.findStatic(LateRuntime.class, "state",
                        MethodType.methodType(Object.class, String.class, Object.class));
                    state = MethodHandles.insertArguments(state, 0, plan.get("owner"))
                        .asType(MethodType.methodType(helper(plan), callType.parameterType(0)));
                    handle = MethodHandles.filterArguments(handle, 0, state);
                }
            } else {
                Field field = resolved != null ? (Field) resolved[1] : MemberAccess.field(type(owner), name);
                field.setAccessible(true);
                handle = opcode == 178 || opcode == 180 ? caller.unreflectGetter(field) : caller.unreflectSetter(field);
            }
        } else {
            Class<?> start = type(owner);
            if (opcode == 183 && !start.isInterface()) {
                Class<?> current = type(origin);
                if (start != current && start.isAssignableFrom(current) && current.getSuperclass() != null) start = current.getSuperclass();
            }
            Object[] resolved = resolve(start, name, descriptor, "methods");
            Map<String, Object> plan = plan(resolved);
            String[] metadata = plan == null ? null : members(plan, "methods").get(name + descriptor);
            if (plan != null && (opcode == 183 || "added".equals(metadata[2]))) {
                handle = opcode != 183 && overridden(plan, name + descriptor)
                    ? virtual(name, descriptor, callType) : body(plan, name + descriptor);
            } else {
                Method method = resolved != null && resolved[1] != null ? (Method) resolved[1] : MemberAccess.method(start, name, descriptor);
                method.setAccessible(true);
                handle = opcode == 183
                    ? privateLookup(type(origin), caller).unreflectSpecial(method, type(origin))
                    : caller.unreflect(method);
            }
        }
        return new ConstantCallSite(handle.asType(callType));
    }

    private static boolean overridden(Map<String, Object> plan, String key) throws ClassNotFoundException {
        Class<?> declaring = type((String) plan.get("owner"));
        for (Map<String, Object> other : plans().values()) {
            String[] method = other == plan ? null : members(other, "methods").get(key);
            if (method != null && "added".equals(method[2]) && declaring.isAssignableFrom(type((String) other.get("owner")))) return true;
        }
        return false;
    }

    private static MethodHandle virtual(String name, String descriptor, MethodType callType) throws Exception {
        MethodHandle select = LOOKUP.findStatic(LateRuntime.class, "select",
            MethodType.methodType(MethodHandle.class, String.class, String.class, MethodType.class, Object.class));
        select = MethodHandles.insertArguments(select, 0, name, descriptor, callType)
            .asType(MethodType.methodType(MethodHandle.class, callType.parameterType(0)));
        return MethodHandles.foldArguments(MethodHandles.exactInvoker(callType), select);
    }

    @SuppressWarnings("unchecked")
    public static MethodHandle select(String name, String descriptor, MethodType callType, Object receiver) throws Exception {
        Class<?> type = Objects.requireNonNull(receiver).getClass();
        Map<String, Map<Class<?>, MethodHandle>> virtuals = cache("virtual");
        Map<Class<?>, MethodHandle> cache = virtuals.computeIfAbsent(name + descriptor + callType, unused -> new ConcurrentHashMap<>());
        MethodHandle selected = cache.get(type);
        if (selected != null) return selected;
        Object[] resolved = resolve(type, name, descriptor, "methods");
        Map<String, Object> plan = plan(resolved);
        if (plan != null && "added".equals(members(plan, "methods").get(name + descriptor)[2])) {
            selected = body(plan, name + descriptor);
        } else {
            Method method = plan == null && resolved != null ? (Method) resolved[1]
                : MemberAccess.method(plan == null ? type : type((String) plan.get("owner")), name, descriptor);
            method.setAccessible(true);
            selected = LOOKUP.unreflect(method);
        }
        selected = selected.asType(callType);
        cache.put(type, selected);
        return selected;
    }

    @SuppressWarnings("unchecked")
    public static MethodHandle override(Object receiver, String owner, String name, String descriptor) throws Exception {
        String key = owner + "." + name + descriptor;
        Map<String, Map<Class<?>, Optional<MethodHandle>>> dispatch =
            (Map<String, Map<Class<?>, Optional<MethodHandle>>>) runtime().get("dispatch");
        Map<Class<?>, Optional<MethodHandle>> cache = dispatch.computeIfAbsent(key, unused -> new ConcurrentHashMap<>());
        Optional<MethodHandle> cached = cache.get(receiver.getClass());
        if (cached != null) return cached.orElse(null);
        MethodHandle selected = null;
        for (Class<?> current = receiver.getClass(); current != null && !current.getName().replace('.', '/').equals(owner);
             current = current.getSuperclass()) {
            Map<String, Object> plan = plans().get(current.getName().replace('.', '/'));
            String[] method = plan == null ? null : members(plan, "methods").get(name + descriptor);
            if (method != null && "added".equals(method[2]) && (Integer.parseInt(method[3]) & 10) == 0) {
                MethodType signature = MethodType.fromMethodDescriptorString(descriptor, receiver.getClass().getClassLoader());
                selected = body(plan, name + descriptor).asType(signature.insertParameterTypes(0, Object.class));
                break;
            }
            for (Method declared : current.getDeclaredMethods()) {
                if (declared.getName().equals(name) && MethodType.methodType(declared.getReturnType(), declared.getParameterTypes())
                    .toMethodDescriptorString().equals(descriptor)) {
                    cache.put(receiver.getClass(), Optional.empty());
                    return null;
                }
            }
        }
        cache.put(receiver.getClass(), Optional.ofNullable(selected));
        return selected;
    }

    private static final class IdentityReference extends WeakReference<Object> {
        private final int hash;
        IdentityReference(Object value, ReferenceQueue<Object> queue) { super(value, queue); hash = System.identityHashCode(value); }
        public int hashCode() { return hash; }
        public boolean equals(Object other) {
            if (this == other) return true;
            Object value = get();
            return value != null && other instanceof Reference && value == ((Reference<?>) other).get();
        }
    }

    @SuppressWarnings("unchecked")
    private static Object[] stateRecord(Map<String, Object> plan, Object receiver) throws Exception {
        Map<Object, Object[]> states = (Map<Object, Object[]>) plan.get("states");
        ReferenceQueue<Object> queue = (ReferenceQueue<Object>) plan.get("queue");
        Reference<?> dead;
        while ((dead = queue.poll()) != null) states.remove(dead);
        IdentityReference key = new IdentityReference(Objects.requireNonNull(receiver), queue);
        Object[] state = states.get(key);
        if (state == null) {
            state = new Object[]{helper(plan).getConstructor().newInstance(), null, false};
            states.put(key, state);
        }
        return state;
    }

    public static Object state(String owner, Object receiver) throws Throwable {
        Map<String, Object> plan = plans().get(owner);
        synchronized (plan.get("states")) {
            Object[] record = stateRecord(plan, receiver);
            if (!Boolean.TRUE.equals(record[2]) && record[1] != Thread.currentThread()) {
                record[1] = Thread.currentThread();
                try {
                    helper(plan).getMethod("initialize", type(owner)).invoke(null, receiver);
                    record[2] = true;
                } catch (InvocationTargetException failure) { throw failure.getCause(); }
                finally { record[1] = null; }
            }
            return record[0];
        }
    }

    public static void beginConstruction(Object receiver, String owner) throws Exception {
        Map<String, Object> plan = plans().get(owner);
        synchronized (plan.get("states")) {
            Object[] record = stateRecord(plan, receiver);
            if (!Boolean.TRUE.equals(record[2])) record[1] = Thread.currentThread();
        }
    }

    public static void endConstruction(Object receiver, String owner) throws Exception {
        Map<String, Object> plan = plans().get(owner);
        synchronized (plan.get("states")) {
            Object[] record = stateRecord(plan, receiver);
            record[2] = true;
            record[1] = null;
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean isInstance(Object value, String name) throws ClassNotFoundException {
        value = unwrap(value);
        if (value == null) return false;
        Map<Class<?>, Map<String, Boolean>> instances = cache("instances");
        Map<String, Boolean> results = instances.computeIfAbsent(value.getClass(), unused -> new ConcurrentHashMap<>());
        Boolean known = results.get(name);
        if (known != null) return known;
        boolean result = type(name).isInstance(value);
        for (Class<?> current = value.getClass(); !result && current != null; current = current.getSuperclass()) {
            Map<String, Object> plan = plans().get(current.getName().replace('.', '/'));
            result = plan != null && ((List<String>) plan.get("interfaces")).contains(name);
        }
        results.put(name, result);
        return result;
    }

    private static final Object[] NO_ARGUMENTS = new Object[0];

    @SuppressWarnings("unchecked")
    private static MethodHandle adapter(Class<?> type, Method method) throws Exception {
        Map<Class<?>, Map<Method, MethodHandle>> adapters = cache("adapters");
        Map<Method, MethodHandle> methods = adapters.computeIfAbsent(type, unused -> new ConcurrentHashMap<>());
        MethodHandle adapted = methods.get(method);
        if (adapted != null) return adapted;
        String descriptor = descriptor(method);
        Object[] resolved = resolve(type, method.getName(), descriptor, "methods");
        Map<String, Object> plan = plan(resolved);
        MethodHandle target;
        if (plan != null) {
            target = body(plan, method.getName() + descriptor);
        } else if (resolved != null && !Modifier.isAbstract(((Method) resolved[1]).getModifiers())) {
            Method physical = (Method) resolved[1];
            physical.setAccessible(true);
            target = LOOKUP.unreflect(physical);
        } else {
            throw new AbstractMethodError(method.toString());
        }
        adapted = target.asType(target.type().generic()).asSpreader(Object[].class, method.getParameterCount());
        methods.put(method, adapted);
        return adapted;
    }

    @SuppressWarnings("unchecked")
    public static Object cast(Object value, String name) throws Exception {
        final Object receiver = unwrap(value);
        if (receiver == null) return null;
        Class<?> itf = type(name);
        if (itf.isInstance(receiver)) return receiver;
        if (!isInstance(receiver, name)) throw new ClassCastException(receiver.getClass().getName() + " cannot be cast to " + itf.getName());
        Map<Object, Map<String, WeakReference<Object>>> proxies = (Map<Object, Map<String, WeakReference<Object>>>) runtime().get("proxies");
        synchronized (proxies) {
            ReferenceQueue<Object> queue = (ReferenceQueue<Object>) runtime().get("proxyQueue");
            Reference<?> dead;
            while ((dead = queue.poll()) != null) proxies.remove(dead);
            IdentityReference key = new IdentityReference(receiver, queue);
            Map<String, WeakReference<Object>> cached = proxies.computeIfAbsent(key, unused -> new HashMap<>());
            Object previous = cached.containsKey(name) ? cached.get(name).get() : null;
            if (previous != null) return previous;
            Set<Class<?>> interfaces = new LinkedHashSet<>();
            interfaces.add(itf);
            for (Class<?> current = receiver.getClass(); current != null; current = current.getSuperclass()) {
                Map<String, Object> plan = plans().get(current.getName().replace('.', '/'));
                if (plan != null) for (String added : (List<String>) plan.get("interfaces")) interfaces.add(type(added));
            }
            Object adapted = Proxy.newProxyInstance((ClassLoader) runtime().get("loader"), interfaces.toArray(new Class<?>[0]), (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                if (method.getName().equals("hashCode")) return receiver.hashCode();
                if (method.getName().equals("equals")) return receiver.equals(unwrap(args[0]));
                return receiver.toString();
            }
            Object[] values = args == null ? NO_ARGUMENTS : args;
            return (Object) adapter(receiver.getClass(), method).invokeExact(receiver, values);
            });
            for (Class<?> implemented : interfaces) cached.put(implemented.getName().replace('.', '/'), new WeakReference<>(adapted));
            Map<Object, WeakReference<Object>> targets = (Map<Object, WeakReference<Object>>) runtime().get("proxyTargets");
            synchronized (targets) {
                ReferenceQueue<Object> targetQueue = (ReferenceQueue<Object>) runtime().get("targetQueue");
                while ((dead = targetQueue.poll()) != null) targets.remove(dead);
                targets.put(new IdentityReference(adapted, targetQueue), new WeakReference<>(receiver));
            }
            return adapted;
        }
    }

    @SuppressWarnings("unchecked")
    public static Object unwrap(Object value) {
        if (!(value instanceof Proxy)) return value;
        Map<Object, WeakReference<Object>> targets = (Map<Object, WeakReference<Object>>) runtime().get("proxyTargets");
        synchronized (targets) {
            WeakReference<Object> target = targets.get(new IdentityReference(value, null));
            return target == null ? value : target.get();
        }
    }

    public static boolean same(Object first, Object second) { return unwrap(first) == unwrap(second); }

    public static void releaseUiState(Object screen) {
        synchronized (runtime()) {
            Object previous = runtime().get("screen");
            if (previous instanceof WeakReference && ((WeakReference<?>) previous).get() == screen &&
                (screen != null || Boolean.TRUE.equals(runtime().get("screenNull")))) return;
            runtime().put("screen", new WeakReference<>(screen));
            runtime().put("screenNull", screen == null);
            Map<String, Object> buttons = plans().get("net/minecraft/client/gui/GuiButton");
            if (buttons != null) synchronized (buttons.get("states")) { ((Map<?, ?>) buttons.get("states")).clear(); }
            releaseClosedScreens(screen);
        }
    }

    @SuppressWarnings("unchecked")
    private static void releaseClosedScreens(Object current) {
        Class<?> screens;
        try { screens = type("net/minecraft/client/gui/GuiScreen"); }
        catch (ClassNotFoundException missing) { return; }
        for (Map<String, Object> plan : plans().values()) {
            try { if (!screens.isAssignableFrom(type((String) plan.get("owner")))) continue; }
            catch (ClassNotFoundException missing) { continue; }
            Map<Object, Object[]> states = (Map<Object, Object[]>) plan.get("states");
            synchronized (states) {
                states.entrySet().removeIf(entry -> {
                    Object receiver = ((Reference<?>) entry.getKey()).get();
                    return receiver == null || (receiver != current && entry.getValue()[1] == null);
                });
            }
        }
    }
}
