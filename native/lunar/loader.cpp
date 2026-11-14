/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar native loader by opZywl
 * Reference: https://lucas-lima.xyz
 */
#include <windows.h>
#include <jni.h>
#include <jvmti.h>
#include <filesystem>
#include <fstream>
#include <stdexcept>
#include <string>
#include <vector>

namespace {
constexpr const char* AGENT = "net/ccbluex/liquidbounce/injection/bootstrap/LunarAgent";
using GetVMs = jint(JNICALL*)(JavaVM**, jsize, jsize*);
using AttachAgent = jint(JNICALL*)(JavaVM*, char*, void*);

void checked(JNIEnv* env, const char* operation) {
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear();
        throw std::runtime_error(operation);
    }
}

std::string string(JNIEnv* env, jstring value) {
    if (!value) return {};
    const char* bytes = env->GetStringUTFChars(value, nullptr);
    checked(env, "Could not read a Java string");
    if (!bytes) throw std::runtime_error("Could not read a Java string");
    std::string result(bytes);
    env->ReleaseStringUTFChars(value, bytes);
    env->DeleteLocalRef(value);
    return result;
}

std::wstring wide(JNIEnv* env, jstring value) {
    if (!value) return {};
    const jchar* chars = env->GetStringChars(value, nullptr);
    checked(env, "Could not read a Java path");
    if (!chars) throw std::runtime_error("Could not read a Java path");
    std::wstring result(reinterpret_cast<const wchar_t*>(chars), env->GetStringLength(value));
    env->ReleaseStringChars(value, chars);
    env->DeleteLocalRef(value);
    return result;
}

jstring property(JNIEnv* env, const char* key) {
    jclass system = env->FindClass("java/lang/System");
    checked(env, "Java System is unavailable");
    jmethodID getter = env->GetStaticMethodID(system, "getProperty", "(Ljava/lang/String;)Ljava/lang/String;");
    checked(env, "Java properties are unavailable");
    jstring name = env->NewStringUTF(key);
    auto result = static_cast<jstring>(env->CallStaticObjectMethod(system, getter, name));
    env->DeleteLocalRef(name);
    env->DeleteLocalRef(system);
    checked(env, "Could not read a Java property");
    return result;
}

std::wstring ini(const std::filesystem::path& file, const wchar_t* section, const wchar_t* key) {
    std::vector<wchar_t> value(32768);
    DWORD length = GetPrivateProfileStringW(section, key, L"", value.data(), static_cast<DWORD>(value.size()), file.c_str());
    if (length >= value.size() - 1) throw std::runtime_error("The selected path is too long");
    return std::wstring(value.data(), length);
}

void result(const std::filesystem::path& path, const std::string& message) {
    if (path.empty()) return;
    std::ofstream output(path, std::ios::binary | std::ios::trunc);
    output << message;
}

std::string agentArgument(const std::filesystem::path& jar) {
    // The Windows libinstrument entry takes a platform path, unlike JNI's UTF-16 APIs.
    // Prefer an ASCII short name where available, preserving the caller's chosen file.
    std::wstring path = jar.native();
    std::vector<wchar_t> shortName(32768);
    DWORD length = GetShortPathNameW(jar.c_str(), shortName.data(), static_cast<DWORD>(shortName.size()));
    if (length && length < shortName.size()) {
        std::wstring candidate(shortName.data(), length);
        bool ascii = true;
        for (wchar_t character : candidate) ascii &= character < 128;
        if (ascii) path = candidate;
    }
    int size = WideCharToMultiByte(CP_ACP, 0, path.data(), static_cast<int>(path.size()), nullptr, 0, nullptr, nullptr);
    if (!size) throw std::runtime_error("Java could not encode the selected agent path");
    std::string encoded(static_cast<size_t>(size), '\0');
    WideCharToMultiByte(CP_ACP, 0, path.data(), static_cast<int>(path.size()), encoded.data(), size, nullptr, nullptr);
    int wideSize = MultiByteToWideChar(CP_ACP, 0, encoded.data(), size, nullptr, 0);
    std::wstring decoded(static_cast<size_t>(wideSize), L'\0');
    MultiByteToWideChar(CP_ACP, 0, encoded.data(), size, decoded.data(), wideSize);
    if (decoded != path) throw std::runtime_error("This Java runtime cannot encode the selected path. Move the JAR to a supported path and select it again");
    return encoded;
}

bool requireLunar(JavaVM* vm, JNIEnv* env) {
    if (string(env, property(env, "java.specification.version")) != "17")
        throw std::runtime_error("The native FDPClient Inject loader requires Lunar's Java 17 runtime");
    if (string(env, property(env, "java.class.path")).find("lunar-platform-mappings-v1_8.jar") == std::string::npos)
        throw std::runtime_error("Select Lunar Client 1.8.9; this DLL does not support Forge or other game versions");
    if (!string(env, property(env, "fdpclient.lunar.agentRegistered")).empty())
        throw std::runtime_error("FDPClient is already loaded in this game process");
    jvmtiEnv* ti = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&ti), JVMTI_VERSION_1_2) != JNI_OK || !ti)
        throw std::runtime_error("The game JVM has no JVMTI interface");
    jint count = 0;
    jclass* classes = nullptr;
    if (ti->GetLoadedClasses(&count, &classes) != JVMTI_ERROR_NONE) {
        ti->DisposeEnvironment();
        throw std::runtime_error("Could not inspect the Lunar game JVM");
    }
    bool lunar = false;
    for (jint index = 0; index < count; ++index) {
        char* signature = nullptr;
        if (ti->GetClassSignature(classes[index], &signature, nullptr) == JVMTI_ERROR_NONE && signature) {
            std::string name(signature);
            lunar |= name.rfind("Lcom/moonsworth/lunar/genesis/", 0) == 0;
            ti->Deallocate(reinterpret_cast<unsigned char*>(signature));
        }
        env->DeleteLocalRef(classes[index]);
    }
    ti->Deallocate(reinterpret_cast<unsigned char*>(classes));
    ti->DisposeEnvironment();
    return lunar;
}

void requireBundle(JNIEnv* env, const std::filesystem::path& jar) {
    jclass type = env->FindClass("java/util/jar/JarFile");
    jmethodID constructor = env->GetMethodID(type, "<init>", "(Ljava/lang/String;)V");
    jstring path = env->NewString(reinterpret_cast<const jchar*>(jar.c_str()), static_cast<jsize>(jar.native().size()));
    jobject archive = env->NewObject(type, constructor, path);
    checked(env, "The selected Lunar JAR cannot be opened");
    jmethodID getManifest = env->GetMethodID(type, "getManifest", "()Ljava/util/jar/Manifest;");
    jobject manifest = env->CallObjectMethod(archive, getManifest);
    checked(env, "The Lunar JAR has no readable manifest");
    bool valid = false;
    if (manifest) {
        jclass manifestType = env->FindClass("java/util/jar/Manifest");
        jobject attributes = env->CallObjectMethod(manifest, env->GetMethodID(manifestType, "getMainAttributes", "()Ljava/util/jar/Attributes;"));
        jclass attributeType = env->FindClass("java/util/jar/Attributes");
        jmethodID getter = env->GetMethodID(attributeType, "getValue", "(Ljava/lang/String;)Ljava/lang/String;");
        auto value = [&](const char* key) {
            jstring name = env->NewStringUTF(key);
            auto text = static_cast<jstring>(env->CallObjectMethod(attributes, getter, name));
            env->DeleteLocalRef(name);
            checked(env, "Could not read the Lunar manifest");
            return string(env, text);
        };
        valid = value("FDP-Lunar-Bundle") == "1.8.9" &&
            value("Agent-Class") == "net.ccbluex.liquidbounce.injection.bootstrap.LunarAgent" &&
            value("FDP-Payload-Resource") == "FDPClient.jar";
        env->DeleteLocalRef(attributeType);
        env->DeleteLocalRef(attributes);
        env->DeleteLocalRef(manifestType);
        env->DeleteLocalRef(manifest);
    }
    env->CallVoidMethod(archive, env->GetMethodID(type, "close", "()V"));
    env->DeleteLocalRef(path);
    env->DeleteLocalRef(archive);
    env->DeleteLocalRef(type);
    checked(env, "Could not close the selected JAR");
    if (!valid) throw std::runtime_error("Choose a self-contained FDPClient Inject bundle. The JAR filename can be changed");
}

std::string status(JNIEnv* env) {
    jclass agent = env->FindClass(AGENT);
    checked(env, "The FDPClient Inject agent entry could not be resolved");
    jmethodID getter = env->GetStaticMethodID(agent, "nativeStatus", "()Ljava/lang/String;");
    checked(env, "The selected JAR has no native startup status API");
    auto value = static_cast<jstring>(env->CallStaticObjectMethod(agent, getter));
    env->DeleteLocalRef(agent);
    checked(env, "The FDPClient Inject status callback failed");
    return string(env, value);
}

DWORD WINAPI load(LPVOID) {
    JavaVM* vm = nullptr;
    JNIEnv* env = nullptr;
    std::filesystem::path resultPath;
    try {
        wchar_t profile[32768];
        DWORD profileLength = GetEnvironmentVariableW(L"USERPROFILE", profile, 32768);
        if (profileLength && profileLength < 32768) {
            resultPath = std::filesystem::path(profile) / L".zywl" / L"injector" / L"plans" /
                (std::to_wstring(GetCurrentProcessId()) + L".ini.result");
        }
        jsize count = 0;
        ULONGLONG vmDeadline = GetTickCount64() + 15000;
        while (GetTickCount64() < vmDeadline) {
            HMODULE jvm = GetModuleHandleW(L"jvm.dll");
            auto getVMs = jvm ? reinterpret_cast<GetVMs>(GetProcAddress(jvm, "JNI_GetCreatedJavaVMs")) : nullptr;
            if (getVMs && getVMs(&vm, 1, &count) == JNI_OK && count == 1 && vm) break;
            Sleep(10);
        }
        if (!vm) throw std::runtime_error("No Java VM was created in the selected process");
        if (vm->AttachCurrentThreadAsDaemon(reinterpret_cast<void**>(&env), nullptr) != JNI_OK)
            throw std::runtime_error("Could not join the Lunar Java VM");
        std::filesystem::path plans = std::filesystem::path(wide(env, property(env, "user.home"))) / L".zywl" / L"injector" / L"plans";
        auto plan = plans / (std::to_wstring(GetCurrentProcessId()) + L".ini");
        resultPath = std::filesystem::path(plan.native() + L".result");
        if (ini(plan, L"selection", L"version") != L"1" ||
            ini(plan, L"selection", L"pid") != std::to_wstring(GetCurrentProcessId()) ||
            ini(plan, L"selection", L"count") != L"1" || ini(plan, L"payload0", L"id") != L"fdplunar")
            throw std::runtime_error("Select only FDPClient Inject in ZAVZ and choose its JAR before starting the watcher");
        std::filesystem::path jar(ini(plan, L"payload0", L"jar"));
        if (!jar.is_absolute() || !std::filesystem::is_regular_file(jar) || jar.native().find(L'=') != std::wstring::npos)
            throw std::runtime_error("The selected Lunar JAR path is invalid or contains '='");
        ULONGLONG genesisDeadline = GetTickCount64() + 15000;
        while (!requireLunar(vm, env)) {
            if (GetTickCount64() >= genesisDeadline) throw std::runtime_error("Lunar's Genesis loader did not start");
            Sleep(10);
        }
        requireBundle(env, jar);
        auto library = std::filesystem::path(wide(env, property(env, "java.home"))) / L"bin" / L"instrument.dll";
        HMODULE instrument = LoadLibraryExW(library.c_str(), nullptr, LOAD_WITH_ALTERED_SEARCH_PATH);
        auto attach = instrument ? reinterpret_cast<AttachAgent>(GetProcAddress(instrument, "Agent_OnAttach")) : nullptr;
        if (!attach) throw std::runtime_error("The selected Java runtime has no instrumentation agent entry");
        std::string argument = agentArgument(jar);
        jint attached = attach(vm, argument.data(), nullptr);
        checked(env, "The current FDP agent could not be attached");
        if (attached != JNI_OK) {
            if (attached == 100) throw std::runtime_error("Java could not open the agent JAR or its Agent-Class manifest entry");
            if (attached == 101) throw std::runtime_error("Java could not add the selected agent JAR to its classpath");
            auto detail = status(env);
            throw std::runtime_error(detail.rfind("error: ", 0) == 0 ? detail.substr(7) : "The Java agent rejected native startup");
        }
        ULONGLONG deadline = GetTickCount64() + 300000;
        while (GetTickCount64() < deadline) {
            auto value = status(env);
            if (value == "ok") { result(resultPath, "ok"); vm->DetachCurrentThread(); return 0; }
            if (value.rfind("error: ", 0) == 0) throw std::runtime_error(value.substr(7));
            Sleep(100);
        }
        throw std::runtime_error("FDP did not confirm startup within five minutes. Check the Lunar game log");
    } catch (const std::exception& failure) {
        if (env && env->ExceptionCheck()) env->ExceptionClear();
        result(resultPath, std::string("error: ") + failure.what());
    }
    if (env && vm) vm->DetachCurrentThread();
    return 1;
}
}

BOOL WINAPI DllMain(HINSTANCE instance, DWORD reason, LPVOID) {
    if (reason == DLL_PROCESS_ATTACH) {
        DisableThreadLibraryCalls(instance);
        HANDLE worker = CreateThread(nullptr, 0, load, nullptr, 0, nullptr);
        if (!worker) return FALSE;
        CloseHandle(worker);
    }
    return TRUE;
}
