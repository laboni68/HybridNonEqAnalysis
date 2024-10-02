package differencing.models;

public class PartitionInstruction {
    // Index
    public Integer id;
    public int partitionId;
    public int instructionId;

    // Non-Index
    public int version;
    public String method;
    public int instructionIndex;
    public int executionIndex;
    public Integer state;
    public Integer choice;

    public PartitionInstruction(
        int partitionId,
        int instructionId,
        int version,
        String method,
        int instructionIndex,
        int executionIndex,
        Integer state,
        Integer choice
    ) {
        this.partitionId = partitionId;
        this.instructionId = instructionId;
        this.version = version;
        this.method = method;
        this.instructionIndex = instructionIndex;
        this.executionIndex = executionIndex;
        this.state = state;
        this.choice = choice;
    }
}
