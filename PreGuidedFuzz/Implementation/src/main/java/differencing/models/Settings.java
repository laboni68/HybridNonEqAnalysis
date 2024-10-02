package differencing.models;

public class Settings {
    // Index
    public int runId;

    // Non-Index
    public String tool;
    public int runTimeout;
    public int iterationTimeout;
    public int solverTimeout;
    public int depthLimit;

    public Settings(
        int runId,
        String tool,
        int runTimeout,
        int iterationTimeout,
        int solverTimeout,
        int depthLimit
    ) {
        this.runId = runId;

        this.tool = tool;
        this.runTimeout = runTimeout;
        this.iterationTimeout = iterationTimeout;
        this.solverTimeout = solverTimeout;
        this.depthLimit = depthLimit;
    }
}
