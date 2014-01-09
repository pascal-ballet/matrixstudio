package matrixstudio.ui;

import org.xid.basics.error.Diagnostic;
import org.xid.basics.error.ErrorHandler;

public class LogHandler implements ErrorHandler {

	private final MatrixStudio ui;
	
	public LogHandler(MatrixStudio ui) {
		this.ui = ui;
	}
	
	public void log(String message) {
		handleError(Diagnostic.INFO, message);
	}
	
	public void warning(String message) {
		handleError(Diagnostic.WARNING, message);
	}
	
	public void error(String message) {
		handleError(Diagnostic.ERROR, message);
	}
	
	public void handleError(int type, String message) {
		handleError(new Diagnostic.Stub(type, message));
	}

	public void handleError(Diagnostic diagnostic) {
		switch( diagnostic.getLevel() ) {
		case Diagnostic.INFO:
			ui.log(diagnostic.getMessage());
			break;
		case Diagnostic.WARNING:
			ui.warning(diagnostic.getMessage());
			break;
		case Diagnostic.ERROR:
			ui.error(diagnostic.getMessage());
			break;
		}
	}

}
