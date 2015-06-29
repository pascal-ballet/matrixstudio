package matrixstudio.ui.controller;

import matrixstudio.kernel.Simulator;
import matrixstudio.model.Matrix;
import matrixstudio.ui.MatrixField;
import org.xid.basics.ui.BasicsUI;
import org.xid.basics.ui.controller.Controller;
import org.xid.basics.ui.field.CompositeField;


public class MatrixController extends Controller<Matrix> {

	private MatrixField matrixField;
	private final Simulator simulator;
	
	public MatrixController(Simulator simulator) {
		this.simulator = simulator;
	}
	
	@Override
	public CompositeField createFields() {
		matrixField = new MatrixField(simulator, null, BasicsUI.NO_INFO);
		// link simulator user input to matrixField.
		simulator.setInputProvider(matrixField);
		return new CompositeField("Matrix", BasicsUI.GROUP, matrixField);
	}
	
	@Override
	public void refreshFields() {
		matrixField.refresh();
	}
	
	@Override
	public void setSubject(Matrix subject) {
		super.setSubject(subject);
		matrixField.setValue(subject);
	}
}
