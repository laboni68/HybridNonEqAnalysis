package differencing.models;

import differencing.classification.Classification;

public class Iteration {
    // Index
    public Integer id;
    public int runId;

    // Non-Index
    public int iteration;
    public Classification result;
    public Boolean hasTimedOut;
    public Boolean isDepthLimited;
    public Boolean hasUif;
    public Integer partitionCount;
    public Float runtime;
    public String errors;

    public Iteration(int runId, int iteration) {
        this(runId, iteration, null, null, null, null, null, null, null);
    }

    public Iteration(
        int runId,
        int iteration,
        Classification result,
        Boolean hasTimedOut,
        Boolean isDepthLimited,
        Boolean hasUif,
        Integer partitionCount,
        Float runtime,
        String errors
    ) {
        assert result != Classification.ERROR || !errors.isEmpty();

        this.runId = runId;

        this.iteration = iteration;
        this.result = result;
        this.hasTimedOut = hasTimedOut;
        this.isDepthLimited = isDepthLimited;
        this.hasUif = hasUif;
        this.partitionCount = partitionCount;
        this.runtime = runtime;
        this.errors = errors;
    }
}
