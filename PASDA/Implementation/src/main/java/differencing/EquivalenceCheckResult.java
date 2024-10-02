package differencing;

import com.microsoft.z3.Status;

public class EquivalenceCheckResult {
    public final Status status;
    public final String model;
    public final String v1Result;
    public final String v2Result;
    public final String reasonUnknown;
    public final String statistics;

    public EquivalenceCheckResult(
        Status status,
        String model,
        String v1Result,
        String v2Result,
        String reasonUnknown,
        String statistics
    ) {
        this.status = status;
        this.model = model;
        this.v1Result = v1Result;
        this.v2Result = v2Result;
        this.reasonUnknown = reasonUnknown;
        this.statistics = statistics;
    }
}
