package differencing.classification;

import com.microsoft.z3.Status;

public class PartitionClassifier implements Classifier {
    private final boolean isMissing;
    private final boolean isBaseToolMissing;
    private final boolean isError;
    private final boolean isTimeout;
    private final boolean isDepthLimited;
    private final Status pcStatus;
    private final Status neqStatus;
    private final Status eqStatus;
    private final boolean hasUifPc;
    private final boolean hasUif;

    private final Classification classification;

    public PartitionClassifier(
        boolean isMissing,
        boolean isBaseToolMissing,
        boolean isError,
        boolean isTimeout,
        boolean isDepthLimited,
        Status pcStatus,
        Status neqStatus,
        Status eqStatus,
        boolean hasUifPc,
        boolean hasUif
    ) {
        // Partitions should never have a MISSING or BASE_TOOL_MISSING status.
        // This is because we don't know (and can't know) which partitions
        // should or shouldn't exist for any given run.
        assert !isMissing;
        assert !isBaseToolMissing;

        this.isMissing = false;
        this.isBaseToolMissing = false;
        this.isError = isError;
        this.isTimeout = isTimeout;
        this.isDepthLimited = isDepthLimited;
        this.pcStatus = pcStatus;
        this.neqStatus = neqStatus;
        this.eqStatus = eqStatus;

        this.hasUifPc = hasUifPc;
        this.hasUif = hasUif;

        this.classification = this.classify();
    }

    @Override
    public Classification getClassification() {
        return this.classification;
    }

    private enum ReachabilityClassification {
        REACHABLE,
        UNREACHABLE,
        MAYBE_REACHABLE,
    }

    private enum EquivalenceClassification {
        EQ,
        NEQ,
        MAYBE_EQ,
        MAYBE_NEQ,
        UNKNOWN,
    }

    private Classification classify() {
        // pcStatus  MUST be provided.
        assert this.pcStatus != null;

        ReachabilityClassification reachabilityClassification = this.classifyReachability();

        if (reachabilityClassification == ReachabilityClassification.UNREACHABLE) {
            // If we know that the partition is unreachable,
            // we can just ignore the partition irrespective
            // of any other information we might have about it.
            return Classification.UNREACHABLE;
        } else if (this.isMissing) {
            return Classification.MISSING;
        } else if (this.isBaseToolMissing) {
            return Classification.BASE_TOOL_MISSING;
        } else if (this.isError) {
            return Classification.ERROR;
        } else if (this.isTimeout) {
            return Classification.TIMEOUT;
        } else if (this.isDepthLimited) {
            return Classification.DEPTH_LIMITED;
        } else {
            assert this.neqStatus != null;
            assert this.eqStatus != null;

            EquivalenceClassification equivalenceClassification = this.classifyEquivalence();

            return classifyOverall(reachabilityClassification, equivalenceClassification);
        }
    }

    private ReachabilityClassification classifyReachability() {
        return this.hasUifPc
            ? this.classifyReachabilityUif()
            : this.classifyReachabilityNoUif();
    }

    private ReachabilityClassification classifyReachabilityNoUif() {
        if (this.pcStatus == Status.SATISFIABLE) {
            return ReachabilityClassification.REACHABLE;
        } else if (this.pcStatus == Status.UNSATISFIABLE) {
            return ReachabilityClassification.UNREACHABLE;
        } else {
            assert this.pcStatus == Status.UNKNOWN;
            return ReachabilityClassification.MAYBE_REACHABLE;
        }
    }

    private ReachabilityClassification classifyReachabilityUif() {
        if (this.pcStatus == Status.SATISFIABLE) {
            return ReachabilityClassification.MAYBE_REACHABLE;
        } else if (this.pcStatus == Status.UNSATISFIABLE) {
            return ReachabilityClassification.UNREACHABLE;
        } else {
            assert this.pcStatus == Status.UNKNOWN;
            return ReachabilityClassification.MAYBE_REACHABLE;
        }
    }

    private EquivalenceClassification classifyEquivalence() {
        return this.hasUif
            ? this.classifyEquivalenceUif()
            : this.classifyEquivalenceNoUif();
    }

    private EquivalenceClassification classifyEquivalenceNoUif() {
        if (this.neqStatus == Status.SATISFIABLE) {
            if (this.eqStatus == Status.SATISFIABLE) {
                return EquivalenceClassification.NEQ;
            } else if (this.eqStatus == Status.UNSATISFIABLE) {
                return EquivalenceClassification.NEQ;
            } else {
                assert this.eqStatus == Status.UNKNOWN;
                return EquivalenceClassification.NEQ;
            }
        } else if (this.neqStatus == Status.UNSATISFIABLE) {
            if (this.eqStatus == Status.SATISFIABLE) {
                return EquivalenceClassification.EQ;
            } else if (this.eqStatus == Status.UNSATISFIABLE) {
                return EquivalenceClassification.EQ;
            } else {
                assert this.eqStatus == Status.UNKNOWN;
                return EquivalenceClassification.EQ;
            }
        } else {
            assert this.neqStatus == Status.UNKNOWN;
            if (this.eqStatus == Status.SATISFIABLE) {
                return EquivalenceClassification.MAYBE_EQ;
            } else if (this.eqStatus == Status.UNSATISFIABLE) {
                return EquivalenceClassification.NEQ;
            } else {
                assert this.eqStatus == Status.UNKNOWN;
                return EquivalenceClassification.UNKNOWN;
            }
        }
    }

    private EquivalenceClassification classifyEquivalenceUif() {
        if (this.neqStatus == Status.SATISFIABLE) {
            if (this.eqStatus == Status.SATISFIABLE) {
                return EquivalenceClassification.MAYBE_NEQ;
            } else if (this.eqStatus == Status.UNSATISFIABLE) {
                return EquivalenceClassification.NEQ;
            } else {
                assert this.eqStatus == Status.UNKNOWN;
                return EquivalenceClassification.MAYBE_NEQ;
            }
        } else if (this.neqStatus == Status.UNSATISFIABLE) {
            if (this.eqStatus == Status.SATISFIABLE) {
                return EquivalenceClassification.EQ;
            } else if (this.eqStatus == Status.UNSATISFIABLE) {
                return EquivalenceClassification.EQ;
            } else {
                assert this.eqStatus == Status.UNKNOWN;
                return EquivalenceClassification.EQ;
            }
        } else {
            assert this.neqStatus == Status.UNKNOWN;
            if (this.eqStatus == Status.SATISFIABLE) {
                return EquivalenceClassification.MAYBE_EQ;
            } else if (this.eqStatus == Status.UNSATISFIABLE) {
                return EquivalenceClassification.NEQ;
            } else {
                assert this.eqStatus == Status.UNKNOWN;
                return EquivalenceClassification.UNKNOWN;
            }
        }
    }

    private Classification classifyOverall(ReachabilityClassification rc, EquivalenceClassification ec) {
        if (rc == ReachabilityClassification.UNREACHABLE) {
            return Classification.UNREACHABLE;
        } else if (rc == ReachabilityClassification.REACHABLE) {
            switch (ec) {
                case EQ:
                    return Classification.EQ;
                case NEQ:
                    return Classification.NEQ;
                case MAYBE_EQ:
                    return Classification.MAYBE_EQ;
                case MAYBE_NEQ:
                    return Classification.MAYBE_NEQ;
                case UNKNOWN:
                    return Classification.UNKNOWN;
                default:
                    assert false;
            }
        } else {
            assert rc == ReachabilityClassification.MAYBE_REACHABLE;
            switch (ec) {
                case EQ:
                    return Classification.EQ;
                case MAYBE_EQ:
                    return Classification.MAYBE_EQ;
                case NEQ:
                case MAYBE_NEQ:
                    return Classification.MAYBE_NEQ;
                case UNKNOWN:
                    return Classification.UNKNOWN;
                default:
                    assert false;
            }
        }
        throw new RuntimeException("Unable to classify partition.");
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
    public boolean isError() {
        return this.classification == Classification.ERROR;
    }

    @Override
    public boolean isUnreachable() {
        return this.classification == Classification.UNREACHABLE;
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
