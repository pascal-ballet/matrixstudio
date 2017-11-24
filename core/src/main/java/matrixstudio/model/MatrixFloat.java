package matrixstudio.model;

import fr.minibilles.basics.model.ChangeRecorder;
import fr.minibilles.basics.model.ModelObject;
import fr.minibilles.basics.serializer.Boost;
import fr.minibilles.basics.serializer.BoostObject;
import fr.minibilles.basics.serializer.BoostUtil;
import java.text.ParseException;
import java.util.Random;
import matrixstudio.formula.EvaluationException;
import matrixstudio.kernel.MSBoost;


public class MatrixFloat extends Matrix implements ModelObject, BoostObject {

	private float[] matrix;

	private float[] matrixInit;

	public MatrixFloat() {
	}

	protected MatrixFloat(MSBoost boost) {
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

	public void initBlank(boolean force) {
        try {
            int size = getSizeXValue() * getSizeYValue() * getSizeZValue();
            if (force || matrix == null || matrix.length != size) {
                matrixInit = new float[size];
                matrix = new float[size];

                Random random = new Random();
                for (int i = 0; i < matrix.length; i++) {
                    if (isRandom()) {
                        matrix[i] = random.nextFloat();
                        matrixInit[i] = matrix[i];
                    } else {
                        matrix[i] = 0;
                        matrixInit[i] = 0;
                    }
                }
            }
        } catch (ParseException | EvaluationException e) {
            // stop init
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
        int x = safeGetSizeXValue();
        int y = safeGetSizeYValue();
        int z = safeGetSizeZValue();

        if(i>=0 && j>=0 && k>=0 && i< x && j< y && k< z) {
            return matrix[i+x*j+y*y*k];
        } else {
            return 0f;
        }
    }

    public void setValueAt(int i, int j, int k, Number v) {
        matrix[i+safeGetSizeXValue()*j+safeGetSizeXValue()*safeGetSizeYValue()*k] = v.floatValue();
    }

    public void setInitValueAt(int i, int j, int k, Number v) {
        matrixInit[i+safeGetSizeXValue()*j+safeGetSizeXValue()*safeGetSizeYValue()*k] = v.floatValue();
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

