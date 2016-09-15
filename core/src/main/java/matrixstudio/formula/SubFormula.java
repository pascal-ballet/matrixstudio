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
    public Number evaluate(Map<String, Number> context) throws EvaluationException {
        return child.evaluate(context);
    }
}
