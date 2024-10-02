package differencing.classification;

import differencing.models.Partition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class IterationClassifier implements Classifier {
    private final Classification classification;

    public IterationClassifier(
        boolean isMissing,
        boolean isBaseToolMissing,
        boolean isError,
        boolean isTimeout,
        Set<Partition> partitions
    ) {
        this(isMissing, isBaseToolMissing, isError, isTimeout, partitions.stream().map(p -> p.result).collect(Collectors.toList()));
    }

    public IterationClassifier(
        boolean isMissing,
        boolean isBaseToolMissing,
        boolean isError,
        boolean isTimeout,
        Collection<Classification> partitionClassifications
    ) {
        Map<Classification, Integer> classifications = this.getClassifications(partitionClassifications);

        // Partitions should never have a MISSING or BASE_TOOL_MISSING status.
        // This is because we don't know (and can't know) which partitions
        // should or shouldn't exist for any given iteration.
        assert classifications.get(Classification.MISSING) == 0;
        assert classifications.get(Classification.BASE_TOOL_MISSING) == 0;

        int errorCount = classifications.get(Classification.ERROR);
        int unreachableCount = classifications.get(Classification.UNREACHABLE);
        int timeoutCount = classifications.get(Classification.TIMEOUT);
        int depthLimitedCount = classifications.get(Classification.DEPTH_LIMITED);
        int unknownCount = classifications.get(Classification.UNKNOWN);
        int maybeNeqCount = classifications.get(Classification.MAYBE_NEQ);
        int maybeEqCount = classifications.get(Classification.MAYBE_EQ);
        int neqCount = classifications.get(Classification.NEQ);
        int eqCount = classifications.get(Classification.EQ);

        int partitionCount = partitionClassifications.size();

        if (isMissing) {
            // An iteration should be classified as MISSING if we've never even
            // tried to run the differencing for the corresponding benchmark.
            this.classification = Classification.MISSING;
        } else if (isBaseToolMissing) {
            // An iteration should be classified as BASE_TOOL_MISSING if we
            // don't have any differencing results for the corresponding base
            // tool (SE, DSE or ARDiff). This can either be because the base
            // tool was never executed or because its execution ended with
            // an error.
            this.classification = Classification.BASE_TOOL_MISSING;
        } else if (isError || errorCount > 0) {
            // Some error occurred during (symbolic) execution / differencing
            // of the two program versions.
            this.classification = Classification.ERROR;
        } else {
            if (neqCount > 0) {
                // An iteration should be classified as NEQ if we've identified
                // even just a single partition as definitely NEQ.
                this.classification = Classification.NEQ;
            } else if (!isTimeout && partitionCount > 0 && (eqCount + unreachableCount) == partitionCount) {
                // An iteration should only be classified as EQ if it has run to
                // completion without reaching the timeout and ALL reachable
                // partitions are definitely EQ.
                
                this.classification = Classification.EQ;
            } else if (maybeNeqCount > 0) {
                this.classification = Classification.MAYBE_NEQ;
            } else if (maybeEqCount > 0 || eqCount > 0) {
            // } else if (maybeEqCount > 0) {
                // System.out.println("partitionCount: " + partitionCount + " unreachableCount: " + unreachableCount);
                // System.out.println("maybeEqCount: " + maybeEqCount + " eqCount: " + eqCount);
                this.classification = Classification.MAYBE_EQ;
            } else if (partitionCount == 0 || unknownCount > 0) {
                // If we have not identified *any* (MAYBE_)EQ or (MAYBE_)NEQ
                // partitions, classify the iteration as either:
                // (i)  UNKNOWN to signify that we *can't* provide a more
                //      specific result due to solver limitations or as
                // (ii) DEPTH_LIMITED to signify that we *might* be able to
                //      provide a more specific result if a larger depth limit
                //      was used.
                //System.out.println("partitionCount: " + partitionCount + " unknownCount: " + unknownCount + " depthLimitedCount: " + depthLimitedCount + " timeoutCount: " + timeoutCount + " unreachableCount: " + unreachableCount);
                assert unknownCount + depthLimitedCount + timeoutCount + unreachableCount == partitionCount;
                this.classification = Classification.UNKNOWN;
            } else if (depthLimitedCount > 0) {
                assert depthLimitedCount + timeoutCount + unreachableCount == partitionCount;
                this.classification = Classification.DEPTH_LIMITED;
            } else if (isTimeout || timeoutCount > 0) {
                assert timeoutCount + unreachableCount == partitionCount;
                this.classification = Classification.TIMEOUT;
            } else if (unreachableCount > 0) {
                // ALL partitions in the current iteration are unreachable. Such
                // a result should not be possible, because it would imply that
                // there can be methods that have no possible program paths
                // through them. Therefore, the more likely explanation in such
                // a case is that our implementation is buggy in some way.
                assert unreachableCount == partitionCount;
                throw new RuntimeException("Iteration contains only unreachable partitions.");
            } else {
                throw new RuntimeException("Unable to classify iteration.");
            }
        }
    }

    private Map<Classification, Integer> getClassifications(Iterable<Classification> partitionClassifications) {
        Map<Classification, Integer> classifications = Arrays.stream(Classification.values())
            .collect(Collectors.toMap(v -> v, v -> 0));

        for (Classification partitionClassification : partitionClassifications) {
            int count = classifications.get(partitionClassification);
            classifications.put(partitionClassification, count + 1);
        }

        return classifications;
    }

    @Override
    public Classification getClassification() {
        return this.classification;
    }

    @Override
    public boolean isMissing() {
        return this.classification == Classification.MISSING;
    }

    @Override
    public boolean isBaseToolMissing() {
        return this.classification == Classification.BASE_TOOL_MISSING;
    }

    @Override
    public boolean isUnreachable() {
        return this.classification == Classification.UNREACHABLE;
    }

    @Override
    public boolean isError() {
        return this.classification == Classification.ERROR;
    }

    @Override
    public boolean isTimeout() {
        return this.classification == Classification.TIMEOUT;
    }

    @Override
    public boolean isDepthLimited() {
        return this.classification == Classification.DEPTH_LIMITED;
    }

    @Override
    public boolean isUnknown() {
        return this.classification == Classification.UNKNOWN;
    }

    @Override
    public boolean isMaybeNeq() {
        return this.classification == Classification.MAYBE_NEQ;
    }

    @Override
    public boolean isMaybeEq() {
        return this.classification == Classification.MAYBE_EQ;
    }

    @Override
    public boolean isNeq() {
        return this.classification == Classification.NEQ;
    }

    @Override
    public boolean isEq() {
        return this.classification == Classification.EQ;
    }
}
