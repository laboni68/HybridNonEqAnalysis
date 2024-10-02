package differencing;

import differencing.models.Instruction;
import differencing.models.Iteration;
import differencing.models.Partition;
import differencing.models.PartitionInstruction;
import differencing.repositories.InstructionRepository;
import differencing.repositories.PartitionInstructionRepository;
import differencing.repositories.PartitionRepository;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.JVMReturnInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.util.MethodSpec;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

import java.util.*;
import java.util.stream.Collectors;

public class ExecutionListener extends PropertyListenerAdapter {
    private final DifferencingParameters parameters;

    private final MethodSpec methodToCoverSpec;
    private final MethodSpec runSpec;

    // Note: all code that involves ExecutionNodes is only there to make
    // debugging easier by providing a tree view of all instructions executed
    // during the symbolic execution. Look at `roots` in `searchFinished` to
    // see the full execution tree.

    private final List<ExecutionNode> roots = new ArrayList<>();
    private final Map<Integer, ExecutionNode> nodeMap = new HashMap<>();
    private final Map<Integer, Integer> indexMap = new HashMap<>();

    private final Iteration iteration;
    private final Set<Partition> partitions = new HashSet<>();

    private Partition currentPartition;

    private ExecutionNode prevNode = null;
    private int prevIndex = -1;

    protected boolean isInMethodToCover = false;

    protected int partitionNr =  1;
    protected Boolean propertyViolated = false; //

    public ExecutionListener(Iteration iteration, DifferencingParameters parameters) {
        // System.out.println("Starting symbolic execution for iteration " + iteration.iteration);
        this.iteration = iteration;
        this.parameters = parameters;
        this.methodToCoverSpec = MethodSpec.createMethodSpec("*.I*V" + parameters.getToolName() + iteration.iteration + ".snippet");
        this.runSpec = MethodSpec.createMethodSpec("*.IDiff" + parameters.getToolName() + iteration.iteration + ".run");
        this.iteration.id=iteration.iteration;
        //System.out.println("Partion id: " + this.partitionNr);
        this.currentPartition = new Partition(
            this.iteration.id,
            this.partitionNr
        );
    }

    @Override
    public void methodEntered(VM vm, ThreadInfo currentThread, MethodInfo enteredMethod) {
        if (this.methodToCoverSpec.matches(enteredMethod)) {
            this.isInMethodToCover = true;
        }
    }

    @Override
    public void methodExited(VM vm, ThreadInfo currentThread, MethodInfo exitedMethod) {
        if (this.methodToCoverSpec.matches(exitedMethod)) {
            this.isInMethodToCover = false;
        }
    }

    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {
        if (this.isInMethodToCover && !vm.getSystemState().isIgnored()) {
            if (vm.getSearch().isNewState()) {
                this.nodeMap.put(vm.getStateId(), this.prevNode);
                this.indexMap.put(vm.getStateId(), this.prevIndex);
            }
        }
    }

    @Override
    public void stateBacktracked(Search search) {
        this.prevNode = this.nodeMap.get(search.getStateId());
        this.prevIndex = this.indexMap.getOrDefault(search.getStateId(), -1);
        this.isInMethodToCover = this.prevNode != null;
    }

    @Override
    public void searchConstraintHit(Search search) {
        if (search.getVM().getCurrentThread().isFirstStepInsn()) {
            return;
        }
        //System.out.println("Search constraint hit.....starting next partition"); //Does not reach this while I checked
        this.startNextPartition();
    }

    @Override
    public void propertyViolated(Search search) {
        if (search.getVM().getCurrentThread().isFirstStepInsn()) {
            return;
        }
        System.out.println("Property violated in execution listener");
        //this.propertyViolated = true; //
        this.startNextPartition();    
    }

    @Override
    public void instructionExecuted(VM vm, ThreadInfo currentThread, gov.nasa.jpf.vm.Instruction nextInstruction, gov.nasa.jpf.vm.Instruction executedInstruction) {
        MethodInfo mi = executedInstruction.getMethodInfo();
        if (executedInstruction instanceof JVMReturnInstruction && this.runSpec.matches(mi)){ // && this.propertyViolated==false) {
            //System.out.println("Instruction executed.....starting next partition");
            this.startNextPartition();
        }

        if (this.isInMethodToCover & !vm.getSystemState().isIgnored() && !currentThread.isFirstStepInsn()) {
            if (this.methodToCoverSpec.matchesClass(executedInstruction.getMethodInfo().getClassInfo().getName())) {
                assert vm.getChoiceGenerator() instanceof PCChoiceGenerator;
                PCChoiceGenerator cg = vm.getLastChoiceGeneratorOfType(PCChoiceGenerator.class);

                // -------------------------------------------------------------

                Instruction instruction = new Instruction(
                    this.iteration.id,
                    mi.getFullName(),
                    executedInstruction.getInstructionIndex(),
                    executedInstruction.toString(),
                    executedInstruction.getPosition(),
                    mi.getSourceFileName(),
                    executedInstruction.getLineNumber()
                );

                // -------------------------------------------------------------

                ExecutionNode node = new ExecutionNode(vm.getStateId(), cg.getNextChoice(), this.prevIndex + 1, instruction, this.prevNode);
                node.pathCondition = PathCondition.getPC(vm);

                ExecutionNode n = node;
                while (n != null && !n.partitionNrs.contains(this.partitionNr)) {
                    n.partitionNrs.add(this.partitionNr);
                    n = n.prev;
                }

                if (this.prevNode == null) {
                    this.roots.add(node);
                } else {
                    this.prevNode.addNext(node);
                }

                this.prevNode = node;
                this.prevIndex++;
            }
        }
    }

    private void startNextPartition() {
        //this.currentPartition.id = PartitionRepository.getId(this.currentPartition);
        this.currentPartition.id = this.partitionNr;
        //PartitionRepository.insertOrUpdate(this.currentPartition);

        ExecutionNode node = this.prevNode;

        Set<Instruction> newInstructions = new HashSet<>();
        while (node != null) {
            if (node.instruction.id == null) {
                newInstructions.add(node.instruction);
            }
            node = node.prev;
        }

        //InstructionRepository.insertOrUpdate(newInstructions);
        

        node = this.prevNode;
        //node.instruction.id=0;

        Set<PartitionInstruction> newPartitionInstructions = new HashSet<>();
        //System.out.println("Partition id: " + this.currentPartition.id);
        //System.out.println("Node instruction id: " +node.instruction.id);
        while (node != null) {
            newPartitionInstructions.add(new PartitionInstruction(
                this.currentPartition.id,
                //node.instruction.id,
                0,
                node.version,
                node.instruction.method,
                node.instruction.instructionIndex,
                node.executionIndex,
                node.stateId,
                node.choiceId
            ));
            node = node.prev;
        }

        //PartitionInstructionRepository.insertOrUpdate(newPartitionInstructions);

        this.partitions.add(this.currentPartition);
        this.partitionNr++;

        this.currentPartition = new Partition(
            this.iteration.id,
            this.partitionNr
        );
    }

    private static class ExecutionNode {
        public final int stateId;
        public final int choiceId;
        public final int version;
        public final int executionIndex;
        public final Instruction instruction;
        public final Set<Integer> partitionNrs = new HashSet<>();

        public final ExecutionNode prev;
        public List<ExecutionNode> next = new ArrayList<>();

        public PathCondition pathCondition;

        public ExecutionNode(int stateId, int choiceId, int executionIndex, Instruction instruction, ExecutionNode prev) {
            this.stateId = stateId;
            this.choiceId = choiceId;
            this.executionIndex = executionIndex;
            this.version = instruction.method.contains("IoldV") ? 1 : 2;
            this.instruction = instruction;
            this.prev = prev;
        }

        public List<ExecutionNode> getNext() {
            return this.next;
        }

        public void addNext(ExecutionNode next) {
            this.next.add(next);
        }

        @Override
        public String toString() {
            return this.toString(0);
        }

        private String toString(int level) {
            String outerIndent = String.join("", Collections.nCopies(level * 2, " "));

            StringBuilder sb = new StringBuilder();
            sb.append(outerIndent);
            sb.append("stateId=" + this.stateId);
            sb.append(", choiceId=" + this.choiceId);
            sb.append(", executionIndex=" + this.executionIndex);
            sb.append(", instruction=" + this.instruction.instruction);

            String partitions = this.partitionNrs.stream().map(Object::toString).collect(Collectors.joining(","));
            sb.append(", partitionIds=[" + partitions + "]");

            if (this.pathCondition != null && this.pathCondition.header != null) {
                sb.append(", pc=" + this.pathCondition.header.stringPC());
            }

            sb.append("\n");

            if (this.next.size() != 0) {
                sb.append(this.next.stream().map(n -> n.toString(level + 1)).collect(Collectors.joining("")));
            }

            return sb.toString();
        }
    }
}
