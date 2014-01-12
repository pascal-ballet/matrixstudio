package matrixstudio.kernel;

import matrixstudio.model.Model;
import org.xid.basics.error.Diagnostic;
import org.xid.basics.error.ErrorHandler;

public interface SimulatorContext {

    Model getModel();

	void log(String message);
	
	void warning(String message);
	
	void error(String message);

    void recordPNG();

    void recordMPEG();
}
