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
    public Long evaluate(Map<String, Long> context) throws EvaluationException {
        Long result = child.evaluate(context);
        switch (operation) {
            case Minus:
                result = - result;
                break;
            default:
                break;
        }
        return result;
    }

}
