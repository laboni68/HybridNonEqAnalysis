package differencing.domain;

public abstract class ModelVisitor {
    public void preVisit(Model model) {}
    public void preVisit(Expression expression) {}
    public void preVisit(Operation operation) {}
    public void preVisit(Operator operator) {}
    public void preVisit(ConstantInteger constant) {}
    public void preVisit(ConstantReal constant) {}
    public void preVisit(ConstantString constant) {}
    public void preVisit(VariableInteger variable) {}
    public void preVisit(VariableReal variable) {}
    public void preVisit(VariableString variable) {}
    public void preVisit(SymbolicIntegerFunction function) {}
    public void preVisit(SymbolicRealFunction function) {}
    public void preVisit(SymbolicStringFunction function) {}
    public void preVisit(Error error) {}

    public void postVisit(Model model) {}
    public void postVisit(Expression expression) {}
    public void postVisit(Operation operation) {}
    public void postVisit(Operator operator) {}
    public void postVisit(ConstantInteger constant) {}
    public void postVisit(ConstantReal constant) {}
    public void postVisit(ConstantString constant) {}
    public void postVisit(VariableInteger variable) {}
    public void postVisit(VariableReal variable) {}
    public void postVisit(VariableString variable) {}
    public void postVisit(SymbolicIntegerFunction function) {}
    public void postVisit(SymbolicRealFunction function) {}
    public void postVisit(SymbolicStringFunction function) {}
    public void postVisit(Error error) {}
}
