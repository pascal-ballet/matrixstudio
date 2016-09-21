package matrixstudio.formula;

import matrixstudio.model.Model;
import matrixstudio.model.Parameter;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * Contains pre parsed formula for optimization
 */
public class FormulaCache {

    public static FormulaCache SHARED = new FormulaCache();

    private Map<String, Formula> formulaMap = new HashMap<>();
    private WeakHashMap<String, ParseException> errorMap = new WeakHashMap<>();

    public Formula parseFormula(String formula) throws ParseException {
        Formula result = null;
        ParseException exception = null;
        if (formulaMap.containsKey(formula)) {
            result = formulaMap.get(formula);
            exception = errorMap.get(formula);
        } else {
            try {
                result = new FormulaParser(formula).parse();
                formulaMap.put(formula, result);
            } catch (ParseException e) {
                exception = e;
                errorMap.put(formula, e);
            }
        }

        if (result == null) throw exception;
        return result;
    }

    public int computeValue(String expression, Model model) throws ParseException, EvaluationException {
        Map<String, Integer> context = new HashMap<>();
        for (Parameter parameter : model.getParameterList()) {
            try {
                Formula formula = parseFormula(parameter.getFormula());
                int result = formula.evaluate(context);

                if (Objects.equals(expression, parameter.getFormula())) return result;

                context.put(parameter.getName(), result);
            } catch (ParseException | EvaluationException e) {
                continue;
            }
        }

        Formula formula = parseFormula(expression);
        return formula.evaluate(context);
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
