package differencing.domain;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SymbolicStringFunction implements Expression {
    public String name;
    public Expression[] args;

    public SymbolicStringFunction(String name, Expression[] args) {
        this.name = name;
        this.args = args;
    }

    @Override
    public void accept(ModelVisitor visitor) {
        visitor.preVisit(this);
        for (Expression arg: args) {
            arg.accept(visitor);
        }
        visitor.postVisit(this);
    }

    @Override
    public String toString() {
        Stream<String> argStrings = Arrays.stream(this.args).map(Object::toString);
        String argsString = argStrings.collect(Collectors.joining(","));
        return this.name + "(" + argsString + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymbolicStringFunction that = (SymbolicStringFunction) o;
        return Objects.equals(name, that.name) && Arrays.equals(args, that.args);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }
}
