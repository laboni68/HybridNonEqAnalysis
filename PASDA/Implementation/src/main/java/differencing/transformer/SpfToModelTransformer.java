package differencing.transformer;

import differencing.domain.Error;
import differencing.domain.*;
import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;

import java.util.Stack;

public class SpfToModelTransformer {
    public Expression transform(gov.nasa.jpf.symbc.numeric.PathCondition pathCondition) {
        if (pathCondition == null || pathCondition.header == null) {
            return null;
        }

        return this.transform(pathCondition.header);
    }

    public Expression transform(gov.nasa.jpf.symbc.string.StringPathCondition pathCondition) {
        if (pathCondition == null || pathCondition.header == null) {
            return null;
        }

        return this.transform(pathCondition.header);
    }

    public Expression transform(gov.nasa.jpf.symbc.numeric.Constraint constraint) {
        if (constraint == null) {
            return null;
        }

        ConstraintExpressionFactoryVisitor visitor = new ConstraintExpressionFactoryVisitor();
        constraint.accept(visitor);
        return visitor.getExpression();
    }

    public Expression transform(gov.nasa.jpf.symbc.string.StringConstraint constraint) {
        if (constraint == null) {
            return null;
        }

        ConstraintExpressionFactoryVisitor visitor = new ConstraintExpressionFactoryVisitor();
        constraint.accept(visitor);
        return visitor.getExpression();
    }

    public Expression transform(gov.nasa.jpf.symbc.numeric.Expression expression) {
        if (expression == null) {
            return null;
        }

        ConstraintExpressionFactoryVisitor visitor = new ConstraintExpressionFactoryVisitor();
        expression.accept(visitor);
        return visitor.getExpression();
    }

    public Error transform(gov.nasa.jpf.Error error) {
        if (error == null) {
            return null;
        }

        // @TODO: Include more information from the original JPF error.
        return new Error("Error", error.getDetails());
    }

    private static class ConstraintExpressionFactoryVisitor extends ConstraintExpressionVisitor {
        private final Stack<Expression> stack = new Stack<>();

        public Expression getExpression() {
            assert this.stack.size() == 1;
            return this.stack.pop();
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.numeric.Constraint constraint) {
            Expression and = null;
            if (constraint.and != null) {
                and = this.stack.pop();
            }

            Expression right = this.stack.pop();
            Expression left = this.stack.pop();
            Operator op = Operator.get(constraint.getComparator().toString());

            Operation operation = new Operation(left, op, right);

            if (and == null) {
                this.stack.push(operation);
            } else {
                this.stack.push(new Operation(and, Operator.AND, operation));
            }
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.string.StringConstraint constraint) {
            Expression and = null;
            if (constraint.and() != null) {
                and = this.stack.pop();
            }

            Expression right = this.stack.pop();
            Expression left = this.stack.pop();
            Operator op = Operator.get(constraint.getComparator().toString());

            Operation top = new Operation(left, op, right);

            if (and == null) {
                this.stack.push(top);
            } else {
                this.stack.push(new Operation(top, Operator.AND, and));
            }
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.numeric.BinaryNonLinearIntegerExpression expression) {
            Expression right = this.stack.pop();
            Expression left = this.stack.pop();
            Operator op = Operator.get(expression.op.toString());

            this.stack.push(new Operation(left, op, right));
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.string.DerivedStringExpression expression) {
            Expression right = this.stack.pop();
            Expression left = this.stack.pop();
            Operator op = Operator.get(expression.op.toString());

            this.stack.push(new Operation(left, op, right));
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.numeric.BinaryLinearIntegerExpression expression) {
            Expression right = this.stack.pop();
            Expression left = this.stack.pop();
            Operator op = Operator.get(expression.getOp().toString());

            this.stack.push(new Operation(left, op, right));
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.numeric.BinaryRealExpression expression) {
            Expression right = this.stack.pop();
            Expression left = this.stack.pop();
            Operator op = Operator.get(expression.getOp().toString());

            this.stack.push(new Operation(left, op, right));
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.numeric.MathRealExpression expression) {
            Expression right = expression.getArg2() == null ? null : this.stack.pop();
            Expression left = expression.getArg1() == null ? null : this.stack.pop();
            Operator op = Operator.get(expression.getOp().toString());

            this.stack.push(new Operation(left, op, right));
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.numeric.IntegerConstant constant) {
            this.stack.push(new ConstantInteger(constant.value));
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.numeric.RealConstant constant) {
            this.stack.push(new ConstantReal(constant.value));
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.string.StringConstant constant) {
            this.stack.push(new ConstantString(constant.value));
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.numeric.SymbolicInteger variable) {
            this.stack.push(new VariableInteger(variable.getName()));
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.numeric.SymbolicIntFunction expr) {
            int numberOfArguments = expr.getSymArgs().length;
            Expression[] args = new Expression[numberOfArguments];
            for (int i = 0; i < numberOfArguments; i++) {
                args[numberOfArguments - (i + 1)] = this.stack.pop();
            }

            this.stack.push(new SymbolicIntegerFunction(expr.getName(), args));
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.numeric.SymbolicReal variable) {
            this.stack.push(new VariableReal(variable.getName()));
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.numeric.SymbolicRealFunction expr) {
            int numberOfArguments = expr.getSymArgs().length;
            Expression[] args = new Expression[numberOfArguments];
            for (int i = 0; i < numberOfArguments; i++) {
                args[numberOfArguments - (i + 1)] = this.stack.pop();
            }

            this.stack.push(new SymbolicRealFunction(expr.getName(), args));
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.string.StringSymbolic variable) {
            this.stack.push(new VariableString(variable.getName()));
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.string.SymbolicStringBuilder constant) {
            this.stack.push(new ConstantString(constant.toString()));
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.string.SymbolicStringFunction expr) {
            int numberOfArguments = expr.getSymArgs().length;
            Expression[] args = new Expression[numberOfArguments];
            for (int i = 0; i < numberOfArguments; i++) {
                args[numberOfArguments - (i + 1)] = this.stack.pop();
            }

            this.stack.push(new SymbolicStringFunction(expr.getName(), args));
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.mixednumstrg.SpecialIntegerExpression expression) {
            // @TODO: Not sure what to do here?
            throw new UnsupportedOperationException("postVisit(SpecialIntegerExpression) is not implemented!");
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.mixednumstrg.SpecialRealExpression expression) {
            // @TODO: Not sure what to do here?
            throw new UnsupportedOperationException("postVisit(SpecialRealExpression) is not implemented!");
        }

        @Override
        public void postVisit(gov.nasa.jpf.symbc.concolic.FunctionExpression expression) {
            // @TODO: Not sure what to do here?
            throw new UnsupportedOperationException("postVisit(FunctionExpression) is not implemented!");
        }
    }
}

