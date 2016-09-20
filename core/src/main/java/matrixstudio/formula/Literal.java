package matrixstudio.formula;

import java.util.Map;

/**
 * Simple value literal for a formula
 */
public class Literal implements Formula {

    private final int value;

    public Literal(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int evaluate(Map<String, Integer> context) throws EvaluationException {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Literal literal = (Literal) o;

        return value == literal.value;

    }

    @Override
    public int hashCode() {
        return value;
    }
}
