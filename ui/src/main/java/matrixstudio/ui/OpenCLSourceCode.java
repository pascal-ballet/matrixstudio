package matrixstudio.ui;

import org.eclipse.swt.SWT;
import org.xid.basics.ui.field.text.SourceCode;
import org.xid.basics.ui.field.text.SourceCodeElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class OpenCLSourceCode extends SourceCode {

	public final static int CODE = 0;
	public final static int COMMENT = 1;
	public final static int STRING = 2;
	public final static int RED_KEYWORD = 3;
	public final static int BLUE_KEYWORD = 4;

	private String[] redKeywords;
	private String[] blueKeywords;
	

	@Override
	public int getDefaultStyle() {
		return CODE;
	}

	@Override
	public String[] getExpressions() {
		return new String[] {
				// comments
				"//.*",
				"/\\*" , 
				"\\*/", 
				// string
				"\"[^\"]*\"" 
			};
	}

	@Override
	public String[] getKeywords() {
		if ( redKeywords == null ) {
			redKeywords = new String[] {
					"__kernel",
					"__global",
					"__local",
					"__contant",
					"__private",
					"get_global_id"
			}; 
		}
		if ( blueKeywords == null ) {
			blueKeywords = new String[] {
					"void",
					"char",
					"constant",
					"int",
					"uint",
					"long",
					"ulong",
					"float",
					"bool",
					"return",
					"if",
					"else",
					"for",
					"while",
					"do"
			}; 
		}
		ArrayList<String> keywordList = new ArrayList<String>();
		keywordList.addAll(Arrays.asList(blueKeywords));
		keywordList.addAll(Arrays.asList(redKeywords));
		
		String[] keywords = new String[keywordList.size()];
		keywordList.toArray(keywords);
		return keywords;
	}

	@Override
	public int getStyle(int type) {
		switch ( type ) {
		case BLUE_KEYWORD:
			return SWT.COLOR_DARK_BLUE;
		case RED_KEYWORD:
			return SWT.COLOR_DARK_RED;
		case COMMENT:
			return SWT.COLOR_DARK_GRAY;
		case STRING:
			return SWT.COLOR_DARK_GREEN;
		}
		return SWT.COLOR_BLACK;
	}

	@Override
	public int getSystemColor(int type) {
		switch ( type ) {
		case BLUE_KEYWORD:
			return SWT.BOLD;
		case RED_KEYWORD:
			return SWT.ITALIC;
		}
		return SWT.NONE;
	}

	@Override
	protected int handleGroup(int lastIndex, Stack<Integer> typeStack, String group, int groupStart) {
		// all possible cases
		if ( group.equals("/*") ) {
			// comment start.
			addSourceCodeElement(new SourceCodeElement(lastIndex, typeStack.peek(), substring(lastIndex, groupStart)));
			typeStack.push(COMMENT);
			return groupStart;
			
		} else if ( group.equals("*/") ) { 
			// comment end.
			addSourceCodeElement(new SourceCodeElement(lastIndex, typeStack.peek(), substring(lastIndex, groupStart+2)));
			if ( typeStack.peek() == COMMENT) typeStack.pop();
			return groupStart+2;
			
		} else if ( group.startsWith("//") ) {
			// one line comment.
			addSourceCodeElement(new SourceCodeElement(lastIndex, typeStack.peek(), substring(lastIndex, groupStart)));
			addSourceCodeElement(new SourceCodeElement(groupStart, COMMENT, group));
			return groupStart+group.length();
			
		} else if ( group.startsWith("\"") && group.endsWith("\"") ) {
			// string
			addSourceCodeElement(new SourceCodeElement(lastIndex, typeStack.peek(), substring(lastIndex, groupStart)));
			addSourceCodeElement(new SourceCodeElement(groupStart, STRING, group));
			return groupStart+group.length();
			
		} else if ( isBlueKeyword(group) ) {
			// blue keyword 
			addSourceCodeElement(new SourceCodeElement(lastIndex, typeStack.peek(), substring(lastIndex, groupStart)));
			addSourceCodeElement(new SourceCodeElement(groupStart, RED_KEYWORD, group));
			return groupStart+group.length();
		} else if ( isRedKeyword(group) ) {
			// red keyword 
			addSourceCodeElement(new SourceCodeElement(lastIndex, typeStack.peek(), substring(lastIndex, groupStart)));
			addSourceCodeElement(new SourceCodeElement(groupStart, RED_KEYWORD, group));
			return groupStart+group.length();
		}
		return lastIndex;
	}
	
	public boolean isBlueKeyword(String word) {
		for ( String keyword : blueKeywords ) {
			if (keyword.equals(word) ) return true;
		}
		return false;
	}

	public boolean isRedKeyword(String word) {
		for ( String keyword : redKeywords ) {
			if (keyword.equals(word) ) return true;
		}
		return false;
	}
	
}
