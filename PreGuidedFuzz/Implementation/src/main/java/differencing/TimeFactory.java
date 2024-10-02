package differencing;

import differencing.models.Run;
import differencing.models.Time;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeFactory {
    private static final Map<String, Map<String, Integer>> steps = new HashMap<>();
    private static final Map<String, Integer> runSteps = new HashMap<>();
    private static final Map<String, Integer> iterationSteps = new HashMap<>();

    static {
        runSteps.put("", 0);
        runSteps.put("initialization", 1);
        runSteps.put("finalization", 2);

        iterationSteps.put("", 0);
        iterationSteps.put("initialization", 1);
        iterationSteps.put("instrumentation", 2);
        iterationSteps.put("symbolic-execution", 3);
        iterationSteps.put("partition-classification", 4);
        iterationSteps.put("program-classification", 5);
        iterationSteps.put("refinement", 6);
        iterationSteps.put("finalization", 7);

        steps.put("run", runSteps);
        steps.put("iteration", iterationSteps);
    }

    public static List<Time> create(Run run, Map<String, Float> stopwatchTimes) {
        List<Time> times = new ArrayList<>();

        List<String> names = getNames(run);
        for (String name : names) {
            times.add(create(run, name, stopwatchTimes.getOrDefault(name, null)));
        }

        return times;
    }

    private static List<String> getNames(Run run) {
        List<String> names = new ArrayList<>();
        for (String task : runSteps.keySet()) {
            names.add(task.equals("") ? "run" : ("run:" + task));
        }
        for (int i = 1; i <= run.iterationCount; i++) {
            for (String task : iterationSteps.keySet()) {
                names.add(task.equals("")
                    ? "iteration-" + i
                    : ("iteration-" + i + ":" + task));
            }
        }
        return names;
    }

    public static Time create(Run run, String name, Float runtime) {
        String topic = StringUtils.substringBefore(name, ":");;
        String task = StringUtils.substringAfter(name, ":");
        int step = getStep(topic, task);

        return new Time(
            run.id,
            topic,
            task,
            runtime == null ? 0 : runtime,
            step,
            runtime == null
        );
    }

    private static int getStep(String topic, String task) {
        String topicTitle = StringUtils.substringBefore(topic, "-");
        //String topicNumber = StringUtils.substringAfter(topic, "-");

        if (!steps.containsKey(topicTitle)) {
            return -1;
        }

        return steps.get(topicTitle).getOrDefault(task, -1);
    }
}
