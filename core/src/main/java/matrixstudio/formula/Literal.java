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
    public Long evaluate(Map<String, Long> context) throws EvaluationException {
        return value;
    }
}
