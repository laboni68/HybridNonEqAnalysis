package differencing.domain;

import java.util.Objects;

public class ConstantInteger implements Expression {
    public long value;

    public ConstantInteger(long value) {
        this.value = value;
    }

    @Override
    public void accept(ModelVisitor visitor) {
        visitor.preVisit(this);
        visitor.postVisit(this);
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstantInteger that = (ConstantInteger) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
