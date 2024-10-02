package differencing.domain;

import java.util.Objects;

public class VariableString implements Expression {
    public String name;

    public VariableString(String name) {
        this.name = name;
    }

    @Override
    public void accept(ModelVisitor visitor) {
        visitor.preVisit(this);
        visitor.postVisit(this);
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariableString that = (VariableString) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
