package matrixstudio.formula;

import java.util.Map;

/**
 * Simple value literal for a formula
 */
public class Literal implements Formula {

    private final Number value;

    public Literal(Number value) {
        if (value == null) throw new NullPointerException();
        this.value = value;
    }

    public Number getValue() {
        return value;
    }

    @Override
    public Number evaluate(Map<String, Number> context) throws EvaluationException {
        return value;
    }
}
