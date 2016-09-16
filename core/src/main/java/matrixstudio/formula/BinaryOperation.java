package matrixstudio.formula;

import java.text.ParseException;
import java.util.Map;

/**
 * Binary formula, one of: <code>+, -, *, /, %</code>/
 */
public class BinaryOperation implements Formula {
    public enum Operation {
        Plus, Minus, Multiply, Divide, Modulus
    }

    public static Operation fromSymbol(String symbol) throws ParseException {
        switch (symbol) {
            case "+":
                return Operation.Plus;
            case "-":
                return Operation.Minus;
            case "*":
                return Operation.Multiply;
            case "/":
                return Operation.Divide;
            case "%":
                return Operation.Modulus;
            default:
                throw new ParseException("Unknown operator '"+ symbol +"'", 0);
        }
    }

    private final Operation operation;

    private final Formula left;
    private final Formula right;

    public BinaryOperation(Operation operation, Formula left, Formula right) {
        //if (left == null || right == null || operation == null) throw new NullPointerException();
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
    public long evaluate(Map<String, Long> context) throws EvaluationException {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BinaryOperation that = (BinaryOperation) o;

        if (operation != that.operation) return false;
        if (!left.equals(that.left)) return false;
        return right.equals(that.right);

    }

    @Override
    public int hashCode() {
        int result = operation.hashCode();
        result = 31 * result + left.hashCode();
        result = 31 * result + right.hashCode();
        return result;
    }
}
