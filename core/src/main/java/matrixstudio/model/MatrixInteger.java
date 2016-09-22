package matrixstudio.model;

import matrixstudio.formula.EvaluationException;
import org.xid.basics.model.ChangeRecorder;
import org.xid.basics.model.ModelObject;
import org.xid.basics.serializer.Boost;
import org.xid.basics.serializer.BoostObject;
import org.xid.basics.serializer.BoostUtil;

import java.text.ParseException;
import java.util.Random;


public class MatrixInteger extends Matrix implements ModelObject, BoostObject {

	private int[] matrix;

	private int[] matrixInit;

	public MatrixInteger() {
	}

	protected MatrixInteger(Boost boost) {
		super(boost);
		matrix = BoostUtil.readIntArray(boost);
		matrixInit = BoostUtil.readIntArray(boost);
	}

	/**
	 * <p>Gets matrix.</p>
	 */
	public int[] getMatrix() {
		return matrix;
	}

	/**
	 * <p>Sets matrix.</p>
	 */
	public void setMatrix(int[] newValue) {
		if (matrix != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "matrix", this.matrix);
			this.matrix= newValue;
		}
	}

	/**
	 * <p>Gets matrixInit.</p>
	 */
	public int[] getMatrixInit() {
		return matrixInit;
	}

	/**
	 * <p>Sets matrixInit.</p>
	 */
	public void setMatrixInit(int[] newValue) {
		if (matrixInit != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "matrixInit", this.matrixInit);
			this.matrixInit= newValue;
		}
	}

	public void initBlank() {
        try {
            int size = getSizeXValue() * getSizeYValue() * getSizeZValue();
            if (matrix == null || matrix.length != size) {
                matrixInit = new int[size];
                matrix = new int[size];
                final Random random = new Random();
                if (isRandom()) {
                    for (int i = 0; i < matrix.length; i++) {
                        matrix[i] = random.nextInt();
                        matrixInit[i] = matrix[i];
                    }
                } else {
                    for (int i = 0; i < matrix.length; i++) {
                        matrix[i] = 0;
                        matrixInit[i] = matrix[i];
                    }
                }
            }
        } catch (ParseException | EvaluationException e) {
            // stop init
        }
	}

	public String getCType() {
		return "int *";
	}

	public void setToInitialValues() {
		for ( int i=0; i<matrix.length; i++ ) {
			matrix[i] = matrixInit[i];
		}
	}

    public Integer getValueAt(int i, int j, int k) {
        int x = safeGetSizeXValue();
        int y = safeGetSizeYValue();
        int z = safeGetSizeZValue();

        if(i>=0 && j>=0 && k>=0 && i< x && j< y && k< z) {
            return matrix[i+x*j+y*y*k];
        } else {
            return 0;
        }
    }

    public void setValueAt(int i, int j, int k, Number v) {
        matrix[i+safeGetSizeXValue()*j+safeGetSizeXValue()*safeGetSizeYValue()*k] = v.intValue();
    }

    public void setInitValueAt(int i, int j, int k, Number v) {
        matrixInit[i+safeGetSizeXValue()*j+safeGetSizeXValue()*safeGetSizeYValue()*k] = v.intValue();
    }

	public void writeToBoost(Boost boost) {
		super.writeToBoost(boost);
		BoostUtil.writeIntArray(boost, matrix);
		BoostUtil.writeIntArray(boost, matrixInit);
	}

	/**
	 * Visitor accept method.
	 */
	public void accept(ModelVisitor visitor) {
		visitor.visitMatrixInteger(this);
	}

	public ChangeRecorder getChangeRecorder() {
		if ( getModel() != null ) {
			return getModel().getChangeRecorder();
		}
		return super.getChangeRecorder();
	}

}

