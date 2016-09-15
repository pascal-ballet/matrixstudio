package matrixstudio.formula;

import java.util.Map;

/**
 * Reference to another parameter
 */
public class Reference implements Formula {

    private final String name;

    public Reference(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Number evaluate(Map<String, Number> context) throws EvaluationException {
        Number result = context.get(name);
        if (result == null) throw new EvaluationException("Parameter '" +  name + "' isn't defined");
        return result;
    }
}
