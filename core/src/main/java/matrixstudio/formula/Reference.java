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
    public Long evaluate(Map<String, Long> context) throws EvaluationException {
        Long result = context.get(name);
        if (result == null) throw new EvaluationException("Parameter '" +  name + "' isn't defined");
        return result;
    }
}
