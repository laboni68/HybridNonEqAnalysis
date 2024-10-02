package differencing.domain;

import java.util.Objects;

public class Operation implements Expression {
    public Expression left;
    public Operator op;
    public Expression right;

    public Operation(
        final Expression left,
        final Operator op,
        final Expression right
    ) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public void accept(ModelVisitor visitor) {
        visitor.preVisit(this);
        this.op.accept(visitor);
        if (this.left != null) {
            this.left.accept(visitor);
        }
        if (this.right != null) {
            this.right.accept(visitor);
        }
        visitor.postVisit(this);
    }

    @Override
    public String toString() {
        if (this.left == null) {
            assert this.right == null;
            return "(" + this.op + ")";
        } else if (this.right == null) {
            return this.op + " (" + this.left + ")";
        }
        return "(" + this.left + " " + this.op + " " + this.right + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operation operation = (Operation) o;
        return Objects.equals(left, operation.left)
            && op == operation.op
            && Objects.equals(right, operation.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, op, right);
    }
}
