package differencing.models;

import differencing.EquivalenceCheckResult;
import differencing.ReachabilityCheckResult;
import differencing.classification.Classification;

public class Partition {
    // Index
    public Integer id;
    public int iterationId;

    // Non-Index
    public int partition;
    public Classification result;
    public ReachabilityCheckResult pcResult;
    public EquivalenceCheckResult neqResult;
    public EquivalenceCheckResult eqResult;
    public Boolean hasUif;
    public Boolean hasUifPc;
    public Boolean hasUifV1;
    public Boolean hasUifV2;
    public Integer constraintCount;
    public Float runtime;
    public String errors;

    public Partition(int iterationId, int partition) {
        this(
            iterationId, partition,
            null, null, null, null,
            null, null, null,
            null, null, null
        );
    }

    public Partition(
        int iterationId,
        int partition,
        Classification result,
        ReachabilityCheckResult pcResult,
        EquivalenceCheckResult neqResult,
        EquivalenceCheckResult eqResult,
        Boolean hasUifPc,
        Boolean hasUifV1,
        Boolean hasUifV2,
        Integer constraintCount,
        Float runtime,
        String errors
    ) {
        assert result != Classification.ERROR || !errors.isEmpty();

        this.iterationId = iterationId;

        this.partition = partition;
        this.result = result;
        this.pcResult = pcResult;
        this.neqResult = neqResult;
        this.eqResult = eqResult;
        this.hasUifPc = hasUifPc;
        this.hasUifV1 = hasUifV1;
        this.hasUifV2 = hasUifV2;
        this.constraintCount = constraintCount;
        this.runtime = runtime;
        this.errors = errors;

        if (hasUifPc == null && hasUifV1 == null && hasUifV2 == null) {
            this.hasUif = null;
        } else {
            this.hasUif = (hasUifPc != null && hasUifPc)
                || (hasUifV1 != null && hasUifV1)
                || (hasUifV2 != null && hasUifV2);
        }
    }
}
