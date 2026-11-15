import java.nio.file.*;
import java.time.*;
import java.util.*;
import jdk.jfr.consumer.*;

/** Streams a real-game JFR without expanding class-loader metadata into gigabytes of JSON. */
public final class LunarRecordingAnalysis {
    public static void main(String[] args) throws Exception {
        Instant since = args.length > 1 ? Instant.parse(args[1]) : Instant.MIN;
        Map<String, Long> samples = new HashMap<>(), exceptions = new HashMap<>(), waits = new HashMap<>();
        long sampled = 0, thrown = 0, clientCpuSamples = 0, workerCpuSamples = 0;
        try (RecordingFile recording = new RecordingFile(Paths.get(args[0]))) {
            while (recording.hasMoreEvents()) {
                RecordedEvent event = recording.readEvent();
                if (event.getStartTime().isBefore(since)) continue;
                String type = event.getEventType().getName();
                if (type.equals("jdk.JavaExceptionThrow")) {
                    RecordedThread thread = event.getThread();
                    if (!client(thread)) continue;
                    thrown++;
                    String key = event.getClass("thrownClass").getName() + ": " + event.getString("message");
                    exceptions.merge(key, 1L, Long::sum);
                } else if (type.equals("jdk.ExecutionSample") || type.equals("jdk.NativeMethodSample")) {
                    RecordedThread thread = event.getThread("sampledThread");
                    String trace = stack(event);
                    if (trace.contains("OperatingSystemImpl.getCpuLoad") || trace.contains("OperatingSystemImpl.getProcessCpuLoad")) {
                        if (client(thread)) clientCpuSamples++;
                        if (thread != null && thread.getJavaName().equals("FDP-CPU-Monitor")) workerCpuSamples++;
                    }
                    if (!client(thread)) continue;
                    sampled++;
                    samples.merge(trace, 1L, Long::sum);
                } else if (type.equals("jdk.JavaMonitorEnter") || type.equals("jdk.ThreadPark")) {
                    RecordedThread thread = event.getThread();
                    if (client(thread))
                        waits.merge(stack(event), event.getDuration().toMillis(), Long::sum);
                }
            }
        }
        System.out.println("Client samples: " + sampled + "; thrown exceptions: " + thrown + "; since: " + since);
        System.out.println("OS CPU counter samples: render thread=" + clientCpuSamples + "; CPU worker=" + workerCpuSamples);
        print("Samples", samples); print("Exceptions", exceptions); print("Wait milliseconds", waits);
    }

    private static boolean client(RecordedThread thread) {
        return thread != null && (thread.getJavaThreadId() == 1 || thread.getJavaName().equals("Client thread"));
    }

    private static String stack(RecordedEvent event) {
        RecordedStackTrace stack = event.getStackTrace();
        if (stack == null) return "(no stack)";
        List<String> frames = new ArrayList<>();
        for (RecordedFrame frame : stack.getFrames()) {
            RecordedMethod method = frame.getMethod();
            String name = method.getType().getName() + "." + method.getName();
            if (frames.size() < 3 || name.startsWith("net.ccbluex.liquidbounce.")) frames.add(name);
            if (frames.size() == 6) break;
        }
        return String.join(" <- ", frames);
    }

    private static void print(String title, Map<String, Long> values) {
        System.out.println(title + ":");
        values.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(8).forEach(entry -> System.out.println(entry.getValue() + " " + entry.getKey()));
    }
}
