package differencing.classification;

import differencing.models.Iteration;

import java.util.Map;

public class RunClassifier implements Classifier {
    private final Classification classification;
    private final Iteration classificationIteration;

    public RunClassifier(Map<Integer, Iteration> iterations) {
        int iterationCount = iterations.size();
        assert iterationCount > 0;

        // For runs that did NOT run into the timeout, the run classification
        // should be the same as the classification of the last iteration.
        //
        // For runs that DID run into the timeout, the run classification
        // should fall back to the result of the "last fully analyzed iteration"
        // (if one exists), unless the (partially analyzed) last iteration has a
        // "final" result (EQ, NEQ, ERROR, ...).
        //
        // Note that the "last fully analyzed iteration" is always the
        // second-to-last iteration if more than one iteration exists. This
        // is because a new iteration is only started once the previous
        // iteration is fully analyzed.

        Iteration lastIteration = iterations.get(iterationCount);
        // System.out.println("Last iteration: " + lastIteration.result);
        // System.out.println("Iteration count: " + iterationCount);

        if (!lastIteration.hasTimedOut || iterationCount == 1) { 
        // if (!lastIteration.hasTimedOut || iterationCount == 2) { //change made by Laboni: if the first iteration is blocked, then the classification should be based on the second iteration
            //System.out.println("Run did not time out or only has one iteration.");
            this.classification = lastIteration.result;
            this.classificationIteration = lastIteration;
        } else {
            assert iterationCount > 1;
            switch (lastIteration.result) {
                case EQ:
                case NEQ:
                case ERROR:
                case MISSING:
                case BASE_TOOL_MISSING:
                    this.classification = lastIteration.result;
                    this.classificationIteration = lastIteration;
                    break;
                case MAYBE_EQ:
                case MAYBE_NEQ:
                case UNKNOWN:
                case DEPTH_LIMITED:
                case TIMEOUT:
                case UNREACHABLE:
                    this.classification = iterations.get(iterationCount - 1).result;
                    this.classificationIteration = iterations.get(iterationCount - 1);
                    break;
                default:
                    throw new RuntimeException("Unable to classify run.");
            }
        }
    }

    @Override
    public Classification getClassification() {
        return this.classification;
    }

    public Iteration getClassificationIteration() {
        return this.classificationIteration;
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
