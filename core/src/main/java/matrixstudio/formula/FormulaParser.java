package matrixstudio.formula;

import java.text.ParseException;

/**
 * Formula parser
 */
public class FormulaParser {

    private final static char EOL = '\u0000';

    private final String expression;

    private int current = 0;

    public FormulaParser(String expression) {
        this.expression = expression;
    }

    private char peek(int index) {
        int position = current + index;
        if (position < 0 || position >= expression.length()) return EOL;
        return expression.charAt(position);
    }

    private String peek(int start, int length) {
        int gStart = current + start;
        int gEnd = gStart + length;

        return expression.substring(gStart, gEnd);
    }

    private void skip(int n) {
        current += n;
    }

    public Formula parse() throws ParseException {
        return readFormula();
    }

    private Formula readFormula() throws ParseException {
        Formula left = readTerminal();

        return readBinary(left);
    }

    /** Reads binary Multiply and Divide */
    private Formula readBinary(Formula left) throws ParseException {
        char c = peek(0);

        if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%') {
            String op = Character.toString(c);
            readSymbol(op);
            Formula right = readFormula();
            return new BinaryOperation(BinaryOperation.fromSymbol(op), left, right);
        }

        return left;

    }



    private Formula readTerminal() throws ParseException {
        char c = peek(0);
        if ( c == '(' ) {
            readSymbol("(");
            Formula subFormula = readFormula();
            readSymbol(")");
            return new SubFormula(subFormula);
        }
        Formula result = readReference();
        if (result != null) return result;

        result = readLiteral();
        if (result != null) return result;

        throw new ParseException("Unexpected token '"+ c +"'", current);
    }

    private Reference readReference() {
        int i = 0;

        while (true) {
            char c = peek(i);

            if (c != EOL && (i == 0 && (Character.isJavaIdentifierStart(c)) || i > 0 && Character.isJavaIdentifierPart(c))) {
                i++;
            } else {
                break;
            }
        }

        if (i > 0) {
            String result = peek(0, i);
            skip(i);
            readSeparators();
            return new Reference(result);
        } else {
            return null;
        }
    }

    private Literal readLiteral() {
        int i = 0;

        while (true) {
            char c = peek(i);

            if (c != EOL && (i == 0 && (c == '+' || c == '-') || Character.isDigit(c))) {
                i++;
            } else {
                break;
            }
        }

        String result = peek(0, i);
        skip(i);
        readSeparators();
        return new Literal(Long.parseLong(result));
    }

    private void readSymbol(String symbol) throws ParseException {
        String read = peek(0, symbol.length());
        if (!read.equals(symbol)) throw new ParseException("Expected '"+ symbol +"'", current);
        skip(symbol.length());
        readSeparators();
    }

    private void readSeparators() {
        char c = peek(0);
        while ( 1 <= c  && c <= 32) {
            skip(1);
            c = peek(0);
        }
    }

    @Override
    public String toString() {
        return "Parser on '" + peek(current, 10);
    }
}
