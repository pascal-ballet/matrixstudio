package matrixstudio.ui.controller;

import matrixstudio.formula.FormulaCache;
import org.xid.basics.error.Diagnostic;
import org.xid.basics.error.Validator;

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
