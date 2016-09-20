package matrixstudio.formula;

import java.text.ParseException;
import java.util.LinkedList;

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
        if (gEnd > expression.length()) gEnd = expression.length();

        return expression.substring(gStart, gEnd);
    }

    private void skip(int n) {
        current += n;
    }

    public Formula parse() throws ParseException {
        Formula formula = readFormula();
        if (peek(0) != EOL) throw new ParseException("Unexpected token '"+ peek(0) +"'", current);
        return formula;
    }

    private Formula readFormula() throws ParseException {
        return readInfixP1();
    }

    private Formula readInfixP1() throws ParseException {
        Formula left = readInfixP2();

        char c = peek(0);
        LinkedList<BinaryOperation> parts = new LinkedList<>();
        while (c == '+' || c == '-' ) {
            String op = Character.toString(c);
            readSymbol(op);
            Formula part = readInfixP2();
            parts.add(new BinaryOperation(BinaryOperation.fromSymbol(op), null, part));
            c = peek(0);
        }

        Formula result = left;
        while(!parts.isEmpty()) {
            BinaryOperation part = parts.removeFirst();
            result = new BinaryOperation(part.getOperation(), result, part.getRight());
        }
        return result;
    }

    private Formula readInfixP2() throws ParseException {
        Formula left = readTerminal();

        char c = peek(0);
        LinkedList<BinaryOperation> parts = new LinkedList<>();
        while (c == '*' || c == '/' || c == '%' ) {
            String op = Character.toString(c);
            readSymbol(op);
            Formula part = readInfixP2();

            parts.add(new BinaryOperation(BinaryOperation.fromSymbol(op), null, part));
            c = peek(0);
        }

        Formula result = left;
        while(!parts.isEmpty()) {
            BinaryOperation part = parts.removeFirst();
            result = new BinaryOperation(part.getOperation(), result, part.getRight());
        }
        return result;
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

        try {
            String result = peek(0, i);
            skip(i);
            readSeparators();
            return new Literal(Integer.parseInt(result));
        } catch (NumberFormatException e){
            return null;
        }
    }

    private void readSymbol(String symbol) throws ParseException {
        String read = peek(0, symbol.length());
        if (!read.equals(symbol)) throw new ParseException("Expecting '"+ symbol +"' instead of '"+peek(0, 10) +"'", current);
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
        return "Parser on ("+ current +") '" + peek(0, 10) + "'";
    }
}
