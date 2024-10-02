package differencing.domain;

import java.util.Objects;

public class Error implements Model {
    public String type;
    public String message;

    public Error(String type, String message) {
        this.type = type;
        this.message = message;
    }

    @Override
    public void accept(ModelVisitor visitor) {
        visitor.preVisit(this);
        visitor.postVisit(this);
    }

    public String toString() {
        return type + ": " + message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Error error = (Error) o;
        return Objects.equals(type, error.type) && Objects.equals(message, error.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, message);
    }
}
