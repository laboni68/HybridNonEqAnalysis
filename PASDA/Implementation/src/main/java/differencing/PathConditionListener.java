package differencing;

import differencing.domain.Model;
import differencing.models.Iteration;
import differencing.transformer.ModelToJsonTransformer;
import differencing.transformer.SpfToModelTransformer;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.JVMReturnInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.util.MethodSpec;
import gov.nasa.jpf.vm.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PathConditionListener extends PropertyListenerAdapter {
    private final DifferencingParameters parameters;
    private final MethodSpec runSpec;

    private final SpfToModelTransformer spfToModelTransformer;
    private final ModelToJsonTransformer modelToJsonTransformer;

    private final Map<Integer, Map<Integer, PathCondition>> statePcMap = new HashMap<>();
    private final Map<Integer, PathCondition> partitionPcMap = new HashMap<>();

    private int partitionId = 1;

    public PathConditionListener(Iteration iteration, DifferencingParameters parameters) {
        this.parameters = parameters;
        this.runSpec = MethodSpec.createMethodSpec("*.IDiff" + parameters.getToolName() + iteration.iteration + ".run");

        this.spfToModelTransformer = new SpfToModelTransformer();
        this.modelToJsonTransformer = new ModelToJsonTransformer();
    }

    @Override
    public void searchConstraintHit(Search search) {
        if (search.getVM().getCurrentThread().isFirstStepInsn()) {
            return;
        }
        //System.out.println("searchConstraintHit---"); //not found to be reached
        this.startNextPartition();
    }

    @Override
    public void propertyViolated(Search search) {
        if (search.getVM().getCurrentThread().isFirstStepInsn()) {
            return;
        }
        //System.out.println("propertyViolated---"); //Reached when property is violated
        this.startNextPartition();
    }

    @Override
    public void executeInstruction(VM vm, ThreadInfo currentThread, Instruction instructionToExecute) {
        MethodInfo mi = instructionToExecute.getMethodInfo();
        if (instructionToExecute instanceof JVMReturnInstruction && this.runSpec.matches(mi)) {
            this.startNextPartition();
            //System.out.println("executeInstruction---"); //Reached when executed instruction
        }
    }

    @Override
    public void choiceGeneratorProcessed(VM vm, ChoiceGenerator<?> processedCG) {
        if (!(vm.getChoiceGenerator() instanceof PCChoiceGenerator)) {
            return;
        }

        PCChoiceGenerator cg = (PCChoiceGenerator) vm.getChoiceGenerator();

        for (int choice : cg.getChoices()) {
            PathCondition pc = cg.getPC(choice);
            if (pc == null || pc.header == null) {
                continue;
            }

            Map<Integer, PathCondition> choicePcMap = this.statePcMap.getOrDefault(vm.getStateId(), new HashMap<>());
            this.statePcMap.putIfAbsent(vm.getStateId(), choicePcMap);
            assert !choicePcMap.containsKey(choice);

            //this.writePathCondition(vm.getStateId(), choice, pc);

            choicePcMap.put(choice, pc);
        }
    }

    private void startNextPartition() {
        PathCondition pc = PathCondition.getPC(VM.getVM());
        this.writePathCondition(this.partitionId, pc);
        //System.out.println("----------------------1");
        this.partitionPcMap.put(this.partitionId, pc);
        //System.out.println("----------------------2");
        this.partitionId++;
    }

    private void writePathCondition(int partition, PathCondition pc) {
        //System.out.println("Path Condition in writePathCondition "+pc);
        Constraint pcConstraint = pc == null ? null : pc.header;
        Model pcModel = this.spfToModelTransformer.transform(pcConstraint);
        String pcJson = this.modelToJsonTransformer.transform(pcModel);

        String filename = this.parameters.getTargetClassName() + "-P" + partition + "-JSON-PC.json";
        Path path = Paths.get(this.parameters.getTargetDirectory(), filename).toAbsolutePath();

        try {
            Files.write(path, pcJson.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writePathCondition(int state, int choice, PathCondition pc) {
        Constraint pcConstraint = pc == null ? null : pc.header;
        Model pcModel = this.spfToModelTransformer.transform(pcConstraint);
        String pcJson = this.modelToJsonTransformer.transform(pcModel);

        String filename = this.parameters.getTargetClassName() + "-S" + state + "-C" + choice + "-JSON-PC.json";
        Path path = Paths.get(this.parameters.getTargetDirectory(), filename).toAbsolutePath();

        try {
            Files.write(path, pcJson.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
