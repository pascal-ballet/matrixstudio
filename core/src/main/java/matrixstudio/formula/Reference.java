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
    public int evaluate(Map<String, Integer> context) throws EvaluationException {
        Integer result = context.get(name);
        if (result == null) throw new EvaluationException("Parameter '" +  name + "' isn't defined");
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reference reference = (Reference) o;

        return name.equals(reference.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
