package differencing.models;

public class Time {
    // Index
    public Integer id;
    public int runId;

    // Non-Index
    public String topic;
    public String task;
    public Float runtime;
    public Integer step;
    public boolean isMissing;

    public Time(
        int runId,
        String topic,
        String task,
        float runtime,
        int step,
        boolean isMissing
    ) {
        this.runId = runId;

        this.topic = topic;
        this.task = task;
        this.runtime = runtime;
        this.step = step;
        this.isMissing = isMissing;
    }
}
