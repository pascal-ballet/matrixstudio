package matrixstudio.ui.controller;

import fr.minibilles.basics.error.Diagnostic;
import fr.minibilles.basics.error.Validator;
import matrixstudio.formula.FormulaCache;

/**
 * Validator for formula
 */
public class FormulaValidator implements Validator<String> {

    final private FormulaCache cache = FormulaCache.SHARED;

    private String message = null;

    @Override
    public boolean isValid(String value) {
        if (value == null) {
            message = "Formula can't be null";
        } else {
            Exception e = cache.isFormulaValid(value);
            message = e != null ? e.getMessage() : null;
        }
        return message == null;
    }

    @Override
    public Diagnostic getDiagnostic() {
        return new Diagnostic.Stub(Diagnostic.ERROR, message);
    }
}
