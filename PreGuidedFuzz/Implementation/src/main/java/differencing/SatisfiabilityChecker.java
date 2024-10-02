package differencing;

import com.microsoft.z3.*;
import differencing.domain.Model;
import differencing.transformer.ModelToZ3Transformer;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.HashMap;
import java.util.Map;

public class SatisfiabilityChecker implements AutoCloseable {
    private final Context context;

    public SatisfiabilityChecker(int timeout) {
        Map<String, String> settings = new HashMap<>();
        settings.put("timeout", Integer.toString(timeout));
        this.context = new Context(settings);
    }

    @Override
    public void close() throws Exception {
        this.context.close();
    }

    public ReachabilityCheckResult checkPc(Model pcModel) {
        ModelToZ3Transformer modelToZ3 = new ModelToZ3Transformer(this.context);
        Expr<BoolSort> pcExpr = (Expr<BoolSort>) modelToZ3.transform(pcModel);

        Solver solver = this.context.mkSolver();
        solver.add(pcExpr);
        solver = this.removeFuncDeclsForBuiltIns(solver);

        return this.createReachabilityResult(solver, solver.check());
    }

    public EquivalenceCheckResult checkNeq(Model pcModel, Model v1Model, Model v2Model) {
        ModelToZ3Transformer modelToZ3 = new ModelToZ3Transformer(this.context);
        Expr<BoolSort> pcExpr = (Expr<BoolSort>) modelToZ3.transform(pcModel);
        Expr<?> v1Expr = modelToZ3.transform(v1Model);
        Expr<?> v2Expr = modelToZ3.transform(v2Model);

        Solver solver = this.context.mkSolver();
        solver.add(pcExpr);
        solver.add(this.context.mkNot(this.context.mkEq(v1Expr, v2Expr)));
        solver = this.removeFuncDeclsForBuiltIns(solver);
        //System.out.println("NEq check ");
        return this.createEqualityResult(solver, solver.check(), v1Expr, v2Expr);
    }

    public EquivalenceCheckResult checkEq(Model pcModel, Model v1Model, Model v2Model) {
        ModelToZ3Transformer modelToZ3 = new ModelToZ3Transformer(this.context);
        Expr<BoolSort> pcExpr = (Expr<BoolSort>) modelToZ3.transform(pcModel);
        Expr<?> v1Expr = modelToZ3.transform(v1Model);
        Expr<?> v2Expr = modelToZ3.transform(v2Model);

        Solver solver = this.context.mkSolver();
        solver.add(pcExpr);
        solver.add(this.context.mkEq(v1Expr, v2Expr));
        solver = this.removeFuncDeclsForBuiltIns(solver);
        //System.out.println("Eq check ");
        return this.createEqualityResult(solver, solver.check(), v1Expr, v2Expr);
    }


    private ReachabilityCheckResult createReachabilityResult(Solver solver, Status status) {
        String statistics = solver.getStatistics().toString();

        if (status == Status.SATISFIABLE) {
            return new ReachabilityCheckResult(
                status,
                solver.getModel().toString(),
                null,
                statistics
            );
        } else if (status == Status.UNSATISFIABLE) {
            return new ReachabilityCheckResult(
                status,
                null,
                null,
                statistics
            );
        } else if (status == Status.UNKNOWN) {
            return new ReachabilityCheckResult(
                status,
                null,
                solver.getReasonUnknown(),
                statistics
            );
        }

        throw new RuntimeException("Unknown solver status '" + status + "'.");
    }

    private EquivalenceCheckResult createEqualityResult(Solver solver, Status status, Expr<?> v1Expr, Expr<?> v2Expr) {
        //System.out.println("status: " + status+ " v1Expr: " + v1Expr + " v2Expr: " + v2Expr);
        String statistics = solver.getStatistics().toString();
        //System.out.println("statistics: " + statistics);    

        if (status == Status.SATISFIABLE) {
            com.microsoft.z3.Model model = solver.getModel();
           // System.out.println("+++++Status satisfiable+++++");
            //System.out.println("model: " + model.toString()+" Eval1 "+model.eval(v1Expr, true).toString()+" Eval2 "+model.eval(v2Expr, true).toString());
            return new EquivalenceCheckResult(
                status,
                model.toString(),
                model.eval(v1Expr, true).toString(),
                model.eval(v2Expr, true).toString(),
                null,
                statistics
            );
        } else if (status == Status.UNSATISFIABLE) {
            return new EquivalenceCheckResult(
                status,
                null,
                null,
                null,
                null,
                statistics
            );
        } else if (status == Status.UNKNOWN) {
            return new EquivalenceCheckResult(
                status,
                null,
                null,
                null,
                solver.getReasonUnknown(),
                statistics
            );
        }

        throw new RuntimeException("Unknown solver status '" + status + "'.");
    }

    private Solver removeFuncDeclsForBuiltIns(Solver solver) {
        // @TODO: This function only exists to remove function declarations
        //    that overwrite built-in z3 functions such as:
        //    (declare-fun sin (Real) Real)
        //    from the z3 queries.
        //
        // We do this because sin, cos, etc. already exist as built-in functions
        // of z3, so if the query includes new declarations for those functions,
        // it treats them as uninterpreted functions instead, thus ignoring the
        // built-in definitions of the functions.
        //
        // Ideally, the function declarations shouldn't be added to the query
        // in the first place. However, the Java API for z3 doesn't appear to
        // offer a way to create function applications (e.g., sin(...)) without
        // also providing declarations for the functions
        //
        // If there is a way to accomplish this (i.e., to create function
        // applications without declarations), change ModelToZ3Transformer
        // accordingly (i.e., replace this.context.mkApp in the 'case SIN'
        // switch branch) and call solver.check() directly, rather than
        // recreating the solver without the unwanted declarations here.
        //
        // @TODO: We should also remove the log declaration from the query.
        //    However, ARDiff doesn't do this (i.e., it also defines the
        //    log function as an uninterpreted function) so to keep our
        //    results comparable with ARDiff, we're using the "wrong"
        //    definition as well.

        String query = solver.toString();
        query = query.replaceAll("\\(declare-fun (?:sin|cos|tan|asin|acos|atan2|atan) \\(Real\\) Real\\)", "");

        Solver s = this.context.mkSolver();
        s.fromString(query);

        return s;
    }
}
