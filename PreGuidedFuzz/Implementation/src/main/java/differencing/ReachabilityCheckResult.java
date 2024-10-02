package differencing;

import com.microsoft.z3.Status;

public class ReachabilityCheckResult {
    public final Status status;
    public final String model;
    public final String reasonUnknown;
    public final String statistics;

    public ReachabilityCheckResult(Status status, String model, String reasonUnknown, String statistics) {
        this.status = status;
        this.model = model;
        this.reasonUnknown = reasonUnknown;
        this.statistics = statistics;
    }
}
