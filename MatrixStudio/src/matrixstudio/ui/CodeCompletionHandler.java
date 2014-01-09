package matrixstudio.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;


public abstract class CodeCompletionHandler {

	private Shell completionShell;
	private Table completionTable;

	private StyledText styledText;
	
	private void createPopupShell() {
		completionShell = new Shell(getDisplay(), SWT.ON_TOP);
		completionShell.setLayout(new FillLayout());
		completionTable = new Table(completionShell, SWT.SINGLE);
	}
	
	public void installCompletion(final StyledText styledText) {
		this.styledText = styledText;
		createPopupShell();
		styledText.addListener(SWT.KeyDown, new Listener() {
			public void handleEvent(Event event) {
				switch (event.keyCode) {
				case SWT.ARROW_RIGHT:
				case SWT.ARROW_LEFT:
					if ( isCompletionActive() ) {
						openRefreshCompletion();
					}
					break;
				case SWT.ARROW_DOWN:
					if ( isCompletionActive() ) {
						int index = (completionTable.getSelectionIndex() + 1) % completionTable.getItemCount();
						completionTable.setSelection(index);
						event.doit = false;
					}
					break;
				case SWT.ARROW_UP:
					if ( isCompletionActive() ) {
						int index = completionTable.getSelectionIndex() - 1;
						if (index < 0) index = completionTable.getItemCount() - 1;
						completionTable.setSelection(index);
						event.doit = false;
					}
					break;
				case SWT.CR:
					if ( isCompletionActive() ) {
						acceptCompletion();
						closeCompletion();
					}
					break;
				case ' ':
					if ((event.stateMask & SWT.CTRL) != 0) {
						openRefreshCompletion();
					}
					break;
				case SWT.CTRL:
					// prevent completion to be closed
					break;
				default:
					closeCompletion();
					break;
				}
			}
		});	

		completionTable.addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event event) {
				acceptCompletion();
				closeCompletion();
			}
		});
		completionTable.addListener(SWT.KeyDown, new Listener() {
			public void handleEvent(Event event) {
				if (event.keyCode == SWT.ESC) {
					closeCompletion();
				}
			}
		});

		Listener focusOutListener = new Listener() {
			public void handleEvent(Event event) {
				/* async is needed to wait until focus reaches its new Control */
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (getDisplay().isDisposed()) return;
						Control control = getDisplay().getFocusControl();
						if (control == null || (control != styledText && control != completionTable)) {
							closeCompletion();
						}
					}
				});
			}
		};
		completionTable.addListener(SWT.FocusOut, focusOutListener);
		styledText.addListener(SWT.FocusOut, focusOutListener);

		styledText.addListener(SWT.Move, new Listener() {
			public void handleEvent(Event event) {
				closeCompletion();
			}
		});
		
		styledText.addVerifyKeyListener(new VerifyKeyListener() {
			
			public void verifyKey(VerifyEvent event) {
				if ( event.keyCode == SWT.CR ) {
					if ( isCompletionActive() ) {
						event.doit = false;
					}
				}
			}
		});
	}
	
	public void openRefreshCompletion() {
		String string = styledText.getText();
		if (string.length() == 0) {
			completionShell.setVisible(false);
		} else {

			int index = styledText.getCaretOffset();
			
			int wordStart = index -1;
			while ( wordStart >= 0 && !Character.isWhitespace(string.charAt(wordStart)) ) {
				wordStart--;
			}
			wordStart++;
			
			String word = "";
			if ( wordStart < index ) {
				word = string.substring(wordStart, index);
			}

			CompletionProposal[] proposals = getProposals(word);
			if ( proposals == null || proposals.length == 0 ) {
				closeCompletion();
				return;
			}
			
			int itemCount = completionTable.getItemCount();
			
			// Adjust items number
			if ( itemCount < proposals.length ) {
				for ( int i=itemCount; i<proposals.length; i++ ) {
					new TableItem(completionTable, SWT.NONE);
				}
			} else if ( itemCount > proposals.length ) {
				for ( int i=itemCount-1; i>=proposals.length; i-- ) {
					completionTable.getItem(i).dispose();
				}
			}
			
			int width = 0;
			int height = 10; 
			TableItem[] items = completionTable.getItems();
			for (int i = 0; i < items.length; i++) {
				items[i].setText(proposals[i].getProposal());
				items[i].setData(proposals[i]);
				Rectangle textBounds = items[i].getTextBounds(0);
				width = Math.max(width, textBounds.width + 20);
				height += textBounds.height;
			}
			if ( width < 100 ) width = 100;
			if ( width > 400 ) width = 400;
			if ( height > 100 ) height = 100;


			Point point = getDisplay().map(styledText, null, styledText.getCaret().getLocation());
			FontData fontData = styledText.getFont().getFontData()[0];
			GC gc = new GC(styledText);
			gc.setFont(styledText.getFont());
			point.x -= gc.textExtent(word).x;
			point.y += fontData.getHeight();
			
			completionShell.setLocation(point);
			completionShell.setSize(width, height);
			completionShell.setVisible(true);
		}
	}
	
	public class CompletionProposal {
		private final int index;
		private final String proposal;
		
		public CompletionProposal(int index, String proposal) {
			this.index = index;
			this.proposal = proposal;
		}
		
		public int getIndex() {
			return index;
		}
		
		public String getProposal() {
			return proposal;
		}
	}
	
	public abstract CompletionProposal[] getProposals(String wordStart);

	public void closeCompletion() {
		completionShell.setVisible(false);	
	}
	
	public boolean isCompletionActive() {
		return completionShell.isVisible();
	}
	
	public void acceptCompletion() {
		int selectionIndex = completionTable.getSelectionIndex();
		if ( selectionIndex >= 0 ) {
			CompletionProposal proposal = (CompletionProposal) completionTable.getItem(selectionIndex).getData();
			String insertedText = proposal.getProposal().substring(proposal.getIndex());
			styledText.getContent().replaceTextRange(styledText.getCaretOffset(), 0, insertedText);
			styledText.setCaretOffset(styledText.getCaretOffset() + insertedText.length());
		}
	}
	
	private Display getDisplay() {
		return styledText.getDisplay();
	}

}
