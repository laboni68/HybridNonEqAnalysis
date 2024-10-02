package differencing;

import com.microsoft.z3.Status;
import differencing.domain.Model;
import differencing.transformer.SpfToModelTransformer;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class IgnoreUnreachablePathsListener extends PropertyListenerAdapter {
    private final SpfToModelTransformer spfToModel = new SpfToModelTransformer();
    private final SatisfiabilityChecker satChecker;

    private PathCondition previousPathCondition = null;

    public IgnoreUnreachablePathsListener(int solverTimeout) {
        this.satChecker = new SatisfiabilityChecker(solverTimeout);
    }

    @Override
    public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {
        if (vm.getSystemState().isIgnored()) {
            return;
        }

        PathCondition currentPathCondition = PathCondition.getPC(vm);

        if (this.previousPathCondition == null) {
            this.previousPathCondition = currentPathCondition;
        }

        if (this.previousPathCondition != currentPathCondition && !this.previousPathCondition.equals(currentPathCondition)) {
            Constraint pcConstraint = currentPathCondition.header;
            Model pcModel = this.spfToModel.transform(pcConstraint);
            Status pcStatus = this.satChecker.checkPc(pcModel).status;

            if (pcStatus == Status.UNSATISFIABLE) {
                currentThread.getVM().getSystemState().setIgnored(true);
            }

            this.previousPathCondition = currentPathCondition;
        }
    }
}
