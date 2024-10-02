package differencing;

import GradDiff.GradDiffInstrumentation;
import SE.SEInstrumentation;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import equiv.checking.SourceInstrumentation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class PASDAInstrumentation implements SourceInstrumentation {
    private final SEInstrumentation seInstrumentation;
    private final GradDiffInstrumentation gradDiffInstrumentation;

    private boolean isFirstIteration = true;

    public PASDAInstrumentation(
        SEInstrumentation seInstrumentation,
        GradDiffInstrumentation gradDiffInstrumentation
    ) {
        this.seInstrumentation = seInstrumentation;
        this.gradDiffInstrumentation = gradDiffInstrumentation;
    }

    @Override
    public void runInstrumentation(int iteration, ArrayList<Integer> changes) throws Exception {
        if (this.isFirstIteration) {
            this.seInstrumentation.runInstrumentation(iteration, changes);
        } else {
            this.gradDiffInstrumentation.runInstrumentation(iteration, changes);
        }
    }

    @Override
    public String getNextToRefine(Context context, BoolExpr summaryOld, BoolExpr summaryNew, Map<String, Expr<?>> variables) throws IOException {
        if (this.isFirstIteration) {
            return "non-empty";
        } else {
            return this.gradDiffInstrumentation.getNextToRefine(context, summaryOld, summaryNew, variables);
        }
    }

    @Override
    public void expandFunction(String statement, ArrayList<Integer> changes) {
        if (this.isFirstIteration) {
            this.isFirstIteration = false;
        } else {
            this.gradDiffInstrumentation.expandFunction(statement, changes);
        }
    }
}
