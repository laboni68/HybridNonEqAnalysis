package differencing.models;

public class Instruction {
    // Index
    public Integer id;
    public int iterationId;

    // Non-Index
    public String method;
    public int instructionIndex;
    public String instruction;
    public Integer position;
    public String sourceFile;
    public Integer sourceLine;

    public Instruction(
        int iterationId,
        String method,
        int instructionIndex,
        String instruction,
        Integer position,
        String sourceFile,
        Integer sourceLine
    ) {
        this.iterationId = iterationId;
        this.method = method;
        this.instructionIndex = instructionIndex;
        this.instruction = instruction;
        this.position = position;
        this.sourceFile = sourceFile;
        this.sourceLine = sourceLine;
    }
}
