package matrixstudio.formula;

import java.util.Map;

/**
 * Binary formula, one of:
 * <ul>
 * <li></li>
 * </ul>
 */
public class BinaryOperation implements Formula {
    public enum Operation { Plus, Minus, Multiply, Divide, Modulus }

    private final Operation operation;

    private final Formula left;
    private final Formula right;

    public BinaryOperation(Operation operation, Formula left, Formula right) {
        if (left == null || right == null || operation == null) throw new NullPointerException();
        this.operation = operation;
        this.left = left;
        this.right = right;
    }

    public Operation getOperation() {
        return operation;
    }

    public Formula getLeft() {
        return left;
    }

    public Formula getRight() {
        return right;
    }

    @Override
    public Long evaluate(Map<String, Long> context) throws EvaluationException {
        Long leftResult = left.evaluate(context);
        Long rightResult = right.evaluate(context);
        switch (operation) {
            case Plus:
                return leftResult + rightResult;
            case Minus:
                return leftResult - rightResult;
            case Multiply:
                return leftResult * rightResult;
            case Divide:
                return leftResult / rightResult;
            case Modulus:
                return leftResult % rightResult;
            default:
                throw new EvaluationException("No operator given");
        }
    }

}
