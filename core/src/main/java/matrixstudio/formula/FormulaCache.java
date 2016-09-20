package matrixstudio.formula;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains pre parsed formula for optimization
 */
public class FormulaCache {

    private Map<String, Formula> formulaMap = new HashMap<>();
    private Map<String, ParseException> errorMap = new HashMap<>();

    public Formula parseFormula(String formula) throws ParseException {
        Formula result = null;
        ParseException exception = null;
        if (formulaMap.containsKey(formula)) {
            result = formulaMap.get(formula);
            exception = errorMap.get(formula);
        } else {
            try {
                result = new FormulaParser(formula).parse();
            } catch (ParseException e) {
                exception = e;
            }
        }

        if (result == null) throw exception;
        return result;
    }

    public Exception isFormulaValid(String formula) {
        try {
            parseFormula(formula);
            return null;
        } catch (ParseException e) {
            return e;
        }
    }
}
