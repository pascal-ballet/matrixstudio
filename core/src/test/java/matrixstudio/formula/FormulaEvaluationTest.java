package matrixstudio.formula;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests formula evaluation
 */
public class FormulaEvaluationTest {

    private final Map<String, Integer> context = new HashMap<>();

    @Before
    public void setup() {
        context.put("a", 5);
        context.put("b", 7);
        context.put("c", 9);
        context.put("d", -3);
        context.put("e", -5);
    }

    @Test
    public void evaluateLiteral() throws Exception {
        testExpression("5", 5);
        testExpression("-123", -123);
    }

    @Test
    public void evaluateReference() throws Exception {
        testExpression("a", 5);
        testExpression("c", 9);
    }

    @Test
    public void evaluateFormula() throws Exception {
        testExpression("a+5", 10);
        testExpression("a+c-5", 9);
        testExpression("a-b+5-(3*e)", 18);
        testExpression("3*2+1", 7);
        testExpression("1 + a + b * 4 - 3 + (a/2) * 3", 37);
        testExpression("1 + 2 * 2 - 3", 2);
    }


    private void testExpression(String expression, long expectedValue) throws Exception {
        Formula result = new FormulaParser(expression).parse();
        Assert.assertEquals(expectedValue, result.evaluate(context));
    }

}
