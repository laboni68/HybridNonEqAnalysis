package differencing;

import org.apache.commons.lang.time.StopWatch;

import java.util.HashMap;
import java.util.Map;

public class StopWatches {
    private final static Map<String, StopWatch> stopWatches = new HashMap<>();
    private final static Map<String, Long> splits = new HashMap<>();

    public static void start(String name) {
        assert !stopWatches.containsKey(name) && !splits.containsKey(name);
        stopWatches.put(name, new StopWatch());
        stopWatches.get(name).start();
    }

    public static void suspend(String name) {
        stopWatches.get(name).suspend();
    }

    public static void resume(String name) {
        stopWatches.get(name).resume();
    }

    public static void split(String name, String splitName) {
        assert !stopWatches.containsKey(splitName) && !splits.containsKey(splitName);
        splits.put(splitName, stopWatches.get(name).getTime());
    }

    public static float splitAndGetTime(String name, String splitName) {
        split(name, splitName);
        return getTime(splitName);
    }

    public static void stop(String name) {
        stopWatches.get(name).stop();
    }

    public static float stopAndGetTime(String name) {
        stop(name);
        return getTime(name);
    }

    public static float getTime(String name) {
        assert stopWatches.containsKey(name) || splits.containsKey(name);
        long time = stopWatches.containsKey(name)
            ? stopWatches.get(name).getTime()
            : splits.get(name);
        return time / 1000f;
    }

    public static Float getTimeOrDefault(String name, Float value) {
        if (stopWatches.containsKey(name) || splits.containsKey(name)) {
            return getTime(name);
        }
        return value;
    }

    public static Map<String, Float> getTimes() {
        Map<String, Float> times = new HashMap<>();
        for (String name : stopWatches.keySet()) {
            times.put(name, getTime(name));
        }
        for (String name : splits.keySet()) {
            times.put(name, getTime(name));
        }
        return times;
    }
}
