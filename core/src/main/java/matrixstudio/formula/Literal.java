package matrixstudio.formula;

import java.util.Map;

/**
 * Simple value literal for a formula
 */
public class Literal implements Formula {

    private final Long value;

    public Literal(Long value) {
        if (value == null) throw new NullPointerException();
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public long evaluate(Map<String, Long> context) throws EvaluationException {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Literal literal = (Literal) o;

        return value.equals(literal.value);

    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
