package matrixstudio.formula;

import java.util.Map;

/**
 * Formula inside parenthesis.
 */
public class SubFormula implements Formula {

    private final Formula child;

    public SubFormula(Formula child) {
        if (child == null) throw new NullPointerException();
        this.child = child;
    }

    public Formula getChild() {
        return child;
    }

    @Override
    public int evaluate(Map<String, Integer> context) throws EvaluationException {
        return child.evaluate(context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubFormula that = (SubFormula) o;

        return child.equals(that.child);

    }

    @Override
    public int hashCode() {
        return child.hashCode();
    }
}
