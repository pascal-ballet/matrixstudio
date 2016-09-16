package matrixstudio.formula;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for formula parser
 */
public class FormulaParserTest {
    @Test
    public void parseReference() throws Exception {
        Formula result = new FormulaParser("result").parse();
        Assert.assertEquals(result.getClass(), Reference.class);
        Assert.assertEquals(((Reference) result).getName(), "result");

        result = new FormulaParser("otherName").parse();
        Assert.assertEquals(result.getClass(), Reference.class);
        Assert.assertEquals(((Reference) result).getName(), "otherName");
    }

    @Test
    public void parseLiteral() throws Exception {
        Formula result = new FormulaParser("123").parse();
        Assert.assertEquals(result.getClass(), Literal.class);
        Assert.assertEquals((long) ((Literal) result).getValue(), 123l);

        result = new FormulaParser("+4567").parse();
        Assert.assertEquals(result.getClass(), Literal.class);
        Assert.assertEquals((long) ((Literal) result).getValue(), 4567l);

        result = new FormulaParser("-8910").parse();
        Assert.assertEquals(result.getClass(), Literal.class);
        Assert.assertEquals((long) ((Literal) result).getValue(), -8910l);
    }

}
