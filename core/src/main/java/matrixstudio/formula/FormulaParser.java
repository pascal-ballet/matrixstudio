package matrixstudio.formula;

import java.text.ParseException;

/**
 * Formula parser
 */
public class FormulaParser {

    private final static char EOL = '\u0000';
    private final int ff = 3;

    private final ParserPredicate reference = (i, c) -> i == 0 && Character.isJavaIdentifierStart(c) || i > 0 && Character.isJavaIdentifierPart(c);
    private final ParserPredicate literal = (i, c) -> i == 0 && (c == '+' || c == '-') || Character.isDigit(c);
    private final ParserPredicate unaryOp = (i, c) -> c == '+' || c == '-' ;
    private final ParserPredicate binaryOp = (i, c) -> c == '+' || c == '-' || c == '*' || c == '/' || c == '%';

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
        int position = current + start;
        return expression.substring(position, position+length);
    }

    private void skip(int n) {
        current += n;
    }

    public Formula parse() throws ParseException {
        return readFormula();
    }

    private Formula readFormula() throws ParseException {


        char c = peek(0);
        while (c != ')' && c != EOL ) {
            if (c == '(') {
                skip(1);
                SubFormula subFormula = new SubFormula(readFormula());
                skip(1); // skips ')'
                return subFormula;
            } else if (test(unaryOp, 1)) {
                // TODO
            } else if (test(reference, ff)) {
                return new Reference(readString(reference));
            } else if (test(literal, ff)) {
                String string = readString(literal);
                return new Literal(Long.parseLong(string));
            }
            c = peek(0);
        }

        return null;
    }

    private boolean testSubFormula() {
        return peek(0) == '(';
    }

    private boolean test(ParserPredicate predicate, int max) {
        for (int i = 0; i < max; i++) {
            if (predicate.test(i, peek(i)) == false) return false;
        }
        return true;
    }

    private String readString(ParserPredicate predicate) {
        int i = 0;

        while (true) {
            char peek = peek(i);
            if (peek == EOL) break;

            if (predicate.test(i, peek)) {
                i++;
            } else {
                break;
            }
        }

        String result = peek(0, i);
        skip(i);
        readSeparators();
        return result;
    }

    private void readSeparators() {
        char c;
        while (1 <= (c = peek(0)) && c <= 32) {
            skip(1);
        }
    }

    @FunctionalInterface
    private interface ParserPredicate {
        boolean test(int position, Character character);
    }

}
