package differencing.models;

import differencing.classification.Classification;

public class Run {
    // Index
    public Integer id;
    public String benchmark;

    // Non-Index
    public Classification result;
    public Boolean hasTimedOut;
    public Boolean isDepthLimited;
    public Boolean hasUif;
    public Integer iterationCount;
    public Integer resultIteration;
    public Float runtime;
    public String errors;

    public Run(String benchmark) {
        this(benchmark, null, null, null, null, null, null, null, null);
    }

    public Run(
        String benchmark,
        Classification result,
        Boolean hasTimedOut,
        Boolean isDepthLimited,
        Boolean hasUif,
        Integer iterationCount,
        Integer resultIteration,
        Float runtime,
        String errors
    ) {
        assert result != Classification.ERROR || !errors.isEmpty();

        this.benchmark = benchmark;

        this.result = result;
        this.hasTimedOut = hasTimedOut;
        this.isDepthLimited = isDepthLimited;
        this.hasUif = hasUif;
        this.iterationCount = iterationCount;
        this.resultIteration = resultIteration;
        this.runtime = runtime;
        this.errors = errors;
    }
}
