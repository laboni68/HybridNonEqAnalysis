package equiv.checking;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public interface SourceInstrumentation {
    void runInstrumentation(int iteration, ArrayList<Integer> changes) throws Exception;

    String getNextToRefine(Context context, BoolExpr summaryOld, BoolExpr summaryNew, Map<String, Expr<?>> variables) throws IOException;

    void expandFunction(String statement, ArrayList<Integer> changes);
}
