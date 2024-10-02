package differencing.transformer;

import com.microsoft.z3.*;
import differencing.domain.*;
import differencing.domain.Error;
import differencing.domain.Model;

import java.util.Arrays;
import java.util.Stack;

public class ModelToZ3Transformer extends ModelVisitor {
    private final Context context;
    private final Stack<Expr<?>> stack = new Stack<>();

    public ModelToZ3Transformer(Context context) {
        this.context = context;
    }

    public Expr<?> transform(int value) {
        return this.context.mkInt(value);
    }

    public Expr<?> transform(double value) {
        return this.context.mkFP(value, this.context.mkFPSortDouble());
    }

    public Expr<?> transform(String value) {
        return value == null ? null : this.context.mkString(value);
    }

    public Expr<?> transform(Model model) {
        if (model == null) {
            return this.context.mkTrue();
        } else {
            model.accept(this);
            assert this.stack.size() == 1;
            return this.stack.pop();
        }
    }

    @Override
    public void postVisit(Operation operation) {
        Expr<?> right = operation.right == null ? null : this.stack.pop();
        Expr<?> left = operation.left == null ? null : this.stack.pop();

        Expr<?> expr;
        switch (operation.op) {
            case EQ:
                expr = this.context.mkEq(left, right);
                break;
            case NE:
                expr = this.context.mkNot(this.context.mkEq(left, right));
                break;
            case LT:
                expr = this.context.mkLt((Expr<? extends ArithSort>) left, (Expr<? extends ArithSort>) right);
                break;
            case LE:
                expr = this.context.mkLe((Expr<? extends ArithSort>) left, (Expr<? extends ArithSort>) right);
                break;
            case GT:
                expr = this.context.mkGt((Expr<? extends ArithSort>) left, (Expr<? extends ArithSort>) right);
                break;
            case GE:
                expr = this.context.mkGe((Expr<? extends ArithSort>) left, (Expr<? extends ArithSort>) right);
                break;
            case PLUS:
                expr = this.context.mkAdd((Expr<? extends ArithSort>) left, (Expr<? extends ArithSort>) right);
                break;
            case MINUS:
                expr = this.context.mkSub((Expr<? extends ArithSort>) left, (Expr<? extends ArithSort>) right);
                break;
            case MUL:
                expr = this.context.mkMul((Expr<? extends ArithSort>) left, (Expr<? extends ArithSort>) right);
                break;
            case DIV:
                expr = this.context.mkDiv((Expr<? extends ArithSort>) left, (Expr<? extends ArithSort>) right);
                break;
            case MOD:
                expr = this.context.mkMod((Expr<IntSort>) left, (Expr<IntSort>) right);
                break;
            case AND:
                expr = this.context.mkAnd((Expr<BoolSort>) left, (Expr<BoolSort>) right);
                break;
            case OR:
                expr = this.context.mkOr((Expr<BoolSort>) left, (Expr<BoolSort>) right);
                break;
            case XOR:
                expr = this.context.mkXor((Expr<BoolSort>) left, (Expr<BoolSort>) right);
                break;
            case POW:
                expr = this.context.mkPower((Expr<? extends ArithSort>) left, (Expr<? extends ArithSort>) right);
                break;
            case SQRT:
                expr = this.context.mkPower((Expr<? extends ArithSort>) left, this.context.mkReal(1, 2));
                break;
            case EXP:
                expr = this.context.mkPower(this.context.mkReal("2.718281828459045"), (Expr<? extends RealSort>) left);
                break;
            case LOG:
            case SIN:
            case COS:
            case TAN:
            case ASIN:
            case ACOS:
            case ATAN:
            case ATAN2:
                 expr = this.context.mkApp(
                     this.context.mkFuncDecl(
                         operation.op.toString(),
                         this.context.mkRealSort(),
                         this.context.mkRealSort()
                     ),
                     left
                 );
                 break;
            default:
                // For operations for which we DON'T KNOW we don't support them, throw a
                // RuntimeException. This should produce an ERROR classification.
                throw new RuntimeException("Unable to transform operation '" + operation + "' to z3.");
        }
        this.stack.push(expr);
    }

    @Override
    public void postVisit(ConstantInteger constant) {
        Expr<?> expr = this.context.mkInt(constant.value);
        this.stack.push(expr);
    }

    @Override
    public void postVisit(ConstantReal constant) {
        Expr<?> expr = this.context.mkReal(Double.toString(constant.value));
        this.stack.push(expr);
    }

    @Override
    public void postVisit(ConstantString constant) {
        Expr<?> expr = this.context.mkString(constant.value);
        this.stack.push(expr);
    }

    @Override
    public void postVisit(VariableInteger variable) {
        Expr<?> expr = this.context.mkIntConst(variable.name);
        this.stack.push(expr);
    }

    @Override
    public void postVisit(VariableReal variable) {
        Expr<?> expr = this.context.mkRealConst(variable.name);
        this.stack.push(expr);
    }

    @Override
    public void postVisit(VariableString variable) {
        Expr<?> expr = this.context.mkConst(
            this.context.mkSymbol(variable.name),
            this.context.mkStringSort()
        );
        this.stack.push(expr);
    }

    @Override
    public void postVisit(SymbolicIntegerFunction function) {
        Expr<?>[] args = this.popArgs(function.args.length);
        Sort[] domain = Arrays.stream(args).map(Expr::getSort).toArray(Sort[]::new);
        Sort range = this.context.mkIntSort();
        FuncDecl<?> decl = this.context.mkFuncDecl(function.name, domain, range);
        Expr<?> expr = this.context.mkApp(decl, args);
        this.stack.push(expr);
    }

    @Override
    public void postVisit(SymbolicRealFunction function) {
        Expr<?>[] args = this.popArgs(function.args.length);
        Sort[] domain = Arrays.stream(args).map(Expr::getSort).toArray(Sort[]::new);
        Sort range = this.context.mkRealSort();
        FuncDecl<?> decl = this.context.mkFuncDecl(function.name, domain, range);
        Expr<?> expr = this.context.mkApp(decl, args);
        this.stack.push(expr);
    }

    @Override
    public void postVisit(SymbolicStringFunction function) {
        Expr<?>[] args = this.popArgs(function.args.length);
        Sort[] domain = Arrays.stream(args).map(Expr::getSort).toArray(Sort[]::new);
        Sort range = this.context.mkStringSort();
        FuncDecl<?> decl = this.context.mkFuncDecl(function.name, domain, range);
        Expr<?> expr = this.context.mkApp(decl, args);
        this.stack.push(expr);
    }

    @Override
    public void postVisit(Error error) {
        // Only models without errors should be transformed to z3.
        throw new RuntimeException("Unable to transform error '" + error + "' to z3.");
    }

    private Expr<?>[] popArgs(int n) {
        Expr<?>[] args = new Expr<?>[n];
        for (int i = n - 1; i >= 0; i--) {
            args[i] = this.stack.pop();
        }
        return args;
    }
}
