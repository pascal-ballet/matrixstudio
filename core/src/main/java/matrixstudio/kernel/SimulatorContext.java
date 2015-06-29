package matrixstudio.kernel;

import matrixstudio.model.Model;

public interface SimulatorContext {

    Model getModel();

	void log(String message);
	
	void warning(String message);
	
	void error(String message);

    void recordPNG();

    void recordMPEG();
}
