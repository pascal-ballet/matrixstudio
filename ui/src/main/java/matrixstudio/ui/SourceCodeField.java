package matrixstudio.ui;

import fr.minibilles.basics.notification.Notification;
import fr.minibilles.basics.ui.BasicsUI;
import fr.minibilles.basics.ui.Resources;
import fr.minibilles.basics.ui.field.AbstractField;
import fr.minibilles.basics.ui.field.text.FindAndReplaceShell;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import matrixstudio.model.Code;
import matrixstudio.model.Kernel;
import matrixstudio.model.Library;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;


public class SourceCodeField extends AbstractField implements RendererContext {

	private final String[] redKeywords = new String[] {
			"__kernel",
			"__global",
			"__local",
			"__private",
			"get_global_id"
	};
	
	
	private final String[] blueKeywords = new String[] {
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
	
	private final Pattern bluePattern;
	{
		StringBuilder regex = new StringBuilder();
		regex.append("\\b(?:");
		for ( String keyword : blueKeywords ) {
			if (regex.length() > 5) regex.append("|");
			regex.append(keyword);
		}
		regex.append(")\\b");
		bluePattern = Pattern.compile(regex.toString());
	}

	private final Pattern redPattern;
	{
		StringBuilder regex = new StringBuilder();
		regex.append("\\b(?:");
		for ( String keyword : redKeywords ) {
			if (regex.length() > 5) regex.append("|");
			regex.append(keyword);
		}
		regex.append(")\\b");
		redPattern = Pattern.compile(regex.toString());
	}
	
	private final Pattern greenPattern = Pattern.compile("\"[^\"]*\"");
	private final Pattern grayPattern = Pattern.compile("\\s*//.*$");
	
	protected StyledText styledText;
	
	private FindAndReplaceShell findAndReplaceShell; 

	protected boolean editable;
	private boolean initializing;
	
	private CodeCompletionHandler completionHandler = new CodeCompletionHandler() {
		public CompletionProposal[] getProposals(String wordStart) {
			List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
			if ( isEditable() ) {
				if ( code != null ) {
					for ( Library otherLibrary : code.getModel().getLibraryList() ) {
						for ( String functionName : otherLibrary.getFunctionNames() ) {
							if ( functionName.startsWith(wordStart) ) {
								CompletionProposal proposal = new CompletionProposal(wordStart.length(), functionName);
								proposals.add(proposal);
							}
						}
					}
				}
				
				for ( String string : blueKeywords ) {
					if ( string.startsWith(wordStart) ) {
						CompletionProposal proposal = new CompletionProposal(wordStart.length(), string);
						proposals.add(proposal);
					}
				}
				for ( String string : redKeywords ) {
					if ( string.startsWith(wordStart) ) {
						CompletionProposal proposal = new CompletionProposal(wordStart.length(), string);
						proposals.add(proposal);
					}
				}
			}
			return proposals.toArray(new CompletionProposal[proposals.size()]);
		}
	};
	
	private String value = null;
	
	private Code code = null;
	
	public SourceCodeField(String label, int style) {
		super(label, style);
	}
	
	public boolean activate() {
		if ( styledText != null ) return styledText.setFocus();
		return false;
	}
	
	public void createWidget(Composite parent) {
		createLabel(parent);
		createInfo(parent);
		createStyledText(parent);
		createButtonBar(parent);
	}

	
	private void computeStyleRanges(String line, int offset, Pattern pattern, Color color, int style, List<StyleRange> styles) {
		Matcher matcher = pattern.matcher(line);
		while ( !matcher.hitEnd() ) {
			if ( matcher.find() ) {
				int start = offset + matcher.start();
				int length = matcher.end() - matcher.start();
				styles.add(new StyleRange(start, length, color, null, style));
			}
		}
	}

	private int styledTextStyle() {
        return SWT.V_SCROLL | SWT.H_SCROLL;
	}

	/** Fired by {@link Display#timerExec(int, Runnable)} to update kernel.*/
	private Runnable updateContents = new Runnable() {
		public void run() {
			if ( code != null ) {
				code.getChangeRecorder().newOperation();
				
				final String oldValue = code.getWholeContents();
				code.setWholeContents(getValue());

				// notify listeners 
				notificationSupport.fireValueNotification(
						Notification.TYPE_UI,
						BasicsUI.NOTIFICATION_VALUE, 
						value, oldValue
					);
			}
		}
	};

	protected void createStyledText(Composite parent) {
		styledText = new StyledText(parent, styledTextStyle());
		attachFieldToWidget(styledText);
		
		findAndReplaceShell = new FindAndReplaceShell(styledText);

		styledText.setText(value == null ? "" : value);
		styledText.setEditable(editable);
		
		styledText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String newText = styledText.getText();
				int length = newText.length();
				newText = length == 0 ? null : newText;
				if ( getValue() == null ? newText != null : !getValue().equals(newText) ) {	
					setValue(newText, Notification.TYPE_UI);
					styledText.getDisplay().timerExec(500, updateContents);
				}
				refreshBackgroundColor();
			}
		});
		
		styledText.addLineStyleListener(new LineStyleListener() {
			public void lineGetStyle(LineStyleEvent event) {
				ArrayList<StyleRange> styles = new ArrayList<StyleRange>();
				computeStyleRanges(event.lineText, event.lineOffset, bluePattern, resources.getSystemColor(SWT.COLOR_DARK_BLUE), SWT.BOLD, styles);
				computeStyleRanges(event.lineText, event.lineOffset, redPattern, resources.getSystemColor(SWT.COLOR_DARK_RED), SWT.ITALIC, styles);
				computeStyleRanges(event.lineText, event.lineOffset, greenPattern, resources.getSystemColor(SWT.COLOR_DARK_GREEN), SWT.NONE, styles);
				computeStyleRanges(event.lineText, event.lineOffset, grayPattern, resources.getSystemColor(SWT.COLOR_DARK_GRAY), SWT.NONE, styles);
				event.styles = styles.toArray(new StyleRange[styles.size()]);
			}
		});
		
		installBracketsHighlight();
		installReadonlyLines();
		
		completionHandler.installCompletion(styledText);
	
		// sets layout
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, grabExcessVerticalSpace());
		data.minimumWidth = 80;
		data.minimumHeight = 80;
		data.horizontalSpan = fieldHorizontalSpan();
		data.verticalSpan = 1;
		styledText.setLayoutData(data);

		fireWidgetCreation(styledText);
	}

	private int closingIndex = -1;

	private void invalidateClosingIndex() {
		if ( closingIndex >= 0 && closingIndex < styledText.getCharCount() ) {
			Point point = styledText.getLocationAtOffset(closingIndex);
			styledText.redraw(point.x - 10, point.y - 8, 25, 33, false);
		}
	}

	private void installBracketsHighlight() {
		styledText.addCaretListener(new CaretListener() {
			public void caretMoved(CaretEvent event) {
				int caretOffset = event.caretOffset;
				invalidateClosingIndex();
				closingIndex = -1;
				if ( caretOffset > 0 ) {
					String text = styledText.getText();
					char carretChar = text.charAt(caretOffset - 1);
					boolean searchRight = true;
					char opposite = ' ';
					if ( '{' == carretChar ) { opposite = '}'; }
					if ( '[' == carretChar ) { opposite = ']'; }
					if ( '(' == carretChar ) { opposite = ')'; }
					if ( '}' == carretChar ) { searchRight = false; opposite = '{'; }
					if ( ']' == carretChar ) { searchRight = false; opposite = '['; }
					if ( ')' == carretChar ) { searchRight = false; opposite = '('; }
					if ( opposite == ' ' ) return;
					int count = 0;
					if ( searchRight ) {
						for (int i=caretOffset; i<text.length(); i++ ) {
							char c = text.charAt(i);
							if ( c == carretChar ) count++;
							if ( c == opposite ) {
								if ( count == 0) {
									closingIndex = i;
									invalidateClosingIndex();
									break;
								}
								count--;
							}
						}
					} else {
						for (int i=caretOffset-2; i>=0; i-- ) {
							char c = text.charAt(i);
							if ( c == carretChar ) count++;
							if ( c == opposite ) {
								if ( count == 0) {
									closingIndex = i;
									invalidateClosingIndex();
									break;
								}
								count--;
							}
						}
						
					}
				}
			}
		});
		
		
		styledText.addPaintListener(e -> {
			if ( closingIndex >= 0 ) {
				GC gc = e.gc;
				gc.setLineWidth(0);
				gc.setForeground(getResources().getSystemColor(SWT.COLOR_GRAY));
				Point point = styledText.getLocationAtOffset(closingIndex);
				gc.drawRectangle(point.x-1, point.y+1, 6, 15);
			}
		});

	}
	
	
	private void refreshBackgroundColor() {
		if ( code instanceof Kernel ) {
			Kernel kernel = (Kernel) code;
			int openingLine = styledText.getLineAtOffset(kernel.getOpeningBracketIndex());
			int closingLine = styledText.getLineAtOffset(styledText.getCharCount() - 3);
			
			styledText.setLineBackground(openingLine, closingLine - openingLine + 1, getResources().getSystemColor(SWT.COLOR_WHITE));
		}
	}
	
	private void installReadonlyLines() {
	 	styledText.addVerifyListener(new VerifyListener() {
			
			public void verifyText(VerifyEvent event) {
				if ( code instanceof Kernel ) {
					Kernel kernel = (Kernel) code;
					// protects from initial setText
					if (!initializing) {

						if ( 	event.start < kernel.getOpeningBracketIndex() ||
								event.end > styledText.getCharCount() - 3
							) {
							event.doit = false;
						}
					}
				}
			}
		});

	}
	
	public boolean grabExcessVerticalSpace() {
		return true;
	}
	
	/** @return the field's value */
	private String getValue() {
		return value;
	}
	
	private void setValue(String value) {
		setValue(value, Notification.TYPE_API);
	}
	

	private void setValue(String value, int type) {
		String oldValue = this.value;
		this.value = value;
		this.closingIndex = -1;
		if ( styledText != null && type != Notification.TYPE_UI ) {
			String text = value == null ? "" : value;
			if ( !text.equals(styledText.getText()) ) {
				styledText.setText(text);
				refreshBackgroundColor();
			}
		}
		notificationSupport.fireValueNotification(type, BasicsUI.NOTIFICATION_VALUE, value, oldValue);
	}
	
	public String getTooltip() {
		StringBuilder tooltip = new StringBuilder();
		if ( code != null ) {
			int carretOffset = styledText.getCaretOffset();
			int globalLineIndex = code.getGlobalLineIndex();
			tooltip.append("Lines from ");
			tooltip.append(globalLineIndex);
			tooltip.append(" to ");
			tooltip.append(globalLineIndex + code.getNumberOfLines());
			tooltip.append("\nCurrent line = ");
			tooltip.append(globalLineIndex + styledText.getLineAtOffset(carretOffset));
		}
		return tooltip.toString();
	}
	
	public boolean isEditable() {
		return editable;
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
		if ( styledText != null ) styledText.setEditable(editable);
	}
	
	public Code getCode() {
		return code;
	}

	public void setCode(Code code) {
		this.code = code;
		if ( styledText != null ) {
			if ( code instanceof Kernel ) {
				styledText.setBackground(getResources().getSystemColor(SWT.COLOR_GRAY));
			} else {
				styledText.setBackground(getResources().getSystemColor(SWT.COLOR_WHITE));
			}
		}
		
		initializing = true;
		if ( code != null ) {
			setEditable(true);
			setValue(code.getWholeContents());
		} else {
			setEditable(false);
			setValue(null);
		}
		initializing = false;
	}
		
	@Override
	public Resources getResources() {
		return super.getResources();
	}
	
	public void openSearchAndReplaceShell() {
		if ( styledText != null ) {
			findAndReplaceShell.open();
		}
	}

	public void scrollToCodeStart() {
		int line = code instanceof Kernel ? styledText.getLineAtOffset(((Kernel) code).getOpeningBracketIndex() - 1) : 0;
		styledText.setTopIndex(line);
	}

}
