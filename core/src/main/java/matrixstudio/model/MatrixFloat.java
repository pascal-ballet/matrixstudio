package matrixstudio.model;

import org.xid.basics.model.ChangeRecorder;
import org.xid.basics.model.ModelObject;
import org.xid.basics.serializer.Boost;
import org.xid.basics.serializer.BoostObject;
import org.xid.basics.serializer.BoostUtil;

import java.util.Random;


public class MatrixFloat extends Matrix implements ModelObject, BoostObject {

	private float[] matrix;

	private float[] matrixInit;

	public MatrixFloat() {
	}

	protected MatrixFloat(Boost boost) {
		super(boost);
		matrix = BoostUtil.readFloatArray(boost);
		matrixInit = BoostUtil.readFloatArray(boost);
	}

	/**
	 * <p>Gets matrix.</p>
	 */
	public float[] getMatrix() {
		return matrix;
	}

	/**
	 * <p>Sets matrix.</p>
	 */
	public void setMatrix(float[] newValue) {
		if (matrix != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "matrix", this.matrix);
			this.matrix= newValue;
		}
	}

	/**
	 * <p>Gets matrixInit.</p>
	 */
	public float[] getMatrixInit() {
		return matrixInit;
	}

	/**
	 * <p>Sets matrixInit.</p>
	 */
	public void setMatrixInit(float[] newValue) {
		if (matrixInit != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "matrixInit", this.matrixInit);
			this.matrixInit= newValue;
		}
	}

	public void initBlank() {
		matrixInit = new float[getSizeX()*getSizeY()*getSizeZ()];
		matrix = new float[getSizeX()*getSizeY()*getSizeZ()];
		
		Random random = new Random();
		for ( int i=0; i<matrix.length; i++) {
			if(isRandom()) {
				matrix[i] = random.nextFloat(); matrixInit[i] = matrix[i];
			} else {
				matrix[i] = 0; matrixInit[i] = 0;
			}
		}
	}

	public String getCType() {
        return "float *";
	}

	public void setToInitialValues() {
		for ( int i=0; i<matrix.length; i++ ) {
			matrix[i] = matrixInit[i];
		}
	}

	public Float getValueAt(int i, int j, int k) {
		if(i>=0 && j>=0 && k>=0 && i<getSizeX() && j<getSizeY() && k<getSizeZ()) {
			return matrix[i+getSizeX()*j+getSizeX()*getSizeY()*k];
		} else {
			return 0f;
		}
	}

	public void setValueAt(int i, int j, int k, Number v) {
		matrix[i+getSizeX()*j+getSizeX()*getSizeY()*k] = v.floatValue();
	}

	public void setInitValueAt(int i, int j, int k, Number v) {
		matrixInit[i+getSizeX()*j+getSizeX()*getSizeY()*k] = v.floatValue();
	}

	public void writeToBoost(Boost boost) {
		super.writeToBoost(boost);
		BoostUtil.writeFloatArray(boost, matrix);
		BoostUtil.writeFloatArray(boost, matrixInit);
	}

	/**
	 * Visitor accept method.
	 */
	public void accept(ModelVisitor visitor) {
		visitor.visitMatrixFloat(this);
	}

	public ChangeRecorder getChangeRecorder() {
		if ( getModel() != null ) {
			return getModel().getChangeRecorder();
		}
		return super.getChangeRecorder();
	}

}

