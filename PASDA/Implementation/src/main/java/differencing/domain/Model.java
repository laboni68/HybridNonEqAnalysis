package differencing.domain;

public interface Model {
    void accept(ModelVisitor visitor);
}
