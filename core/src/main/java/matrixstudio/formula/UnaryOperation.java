package matrixstudio.formula;

import java.util.Map;

/**
 * Unary operation formula
 */
public class UnaryOperation implements Formula {

    public enum Operation { Plus, Minus }

    private final Operation operation;
    private final Formula child;

    public UnaryOperation(Operation operation, Formula child) {
        if (child == null || operation == null) throw new NullPointerException();
        this.operation = operation;
        this.child = child;
    }

    public Operation getOperation() {
        return operation;
    }

    public Formula getChild() {
        return child;
    }

    @Override
    public int evaluate(Map<String, Integer> context) throws EvaluationException {
        int result = child.evaluate(context);
        switch (operation) {
            case Minus:
                result = - result;
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnaryOperation that = (UnaryOperation) o;

        if (operation != that.operation) return false;
        return child.equals(that.child);

    }

    @Override
    public int hashCode() {
        int result = operation.hashCode();
        result = 31 * result + child.hashCode();
        return result;
    }
}
