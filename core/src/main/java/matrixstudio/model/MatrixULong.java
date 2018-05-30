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


public class MatrixULong extends Matrix implements ModelObject, BoostObject {

	private long[] matrix;

	private long[] matrixInit;

	public MatrixULong() {
	}

	protected MatrixULong(MSBoost boost) {
		super(boost);
		matrix = BoostUtil.readLongArray(boost);
		matrixInit = BoostUtil.readLongArray(boost);
	}

	/**
	 * <p>Gets matrix.</p>
	 */
	public long[] getMatrix() {
		return matrix;
	}

	/**
	 * <p>Sets matrix.</p>
	 */
	public void setMatrix(long[] newValue) {
		if (matrix != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "matrix", this.matrix);
			this.matrix= newValue;
		}
	}

	/**
	 * <p>Gets matrixInit.</p>
	 */
	public long[] getMatrixInit() {
		return matrixInit;
	}

	/**
	 * <p>Sets matrixInit.</p>
	 */
	public void setMatrixInit(long[] newValue) {
		if (matrixInit != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "matrixInit", this.matrixInit);
			this.matrixInit= newValue;
		}
	}

	public void initBlank(boolean force) {
        try {
            int size = getSizeXValue() * getSizeYValue() * getSizeZValue();
            if (force || matrix == null || matrix.length != size) {
                matrixInit = new long[size];
                matrix = new long[size];
                final Random random = new Random();
                if (isRandom()) {
                    for (int i = 0; i < matrix.length; i++) {
                        matrix[i] = random.nextLong();
                        matrixInit[i] = matrix[i];
                    }
                } else {
                    for (int i = 0; i < matrix.length; i++) {
                        matrix[i] = 0l;
                        matrixInit[i] = matrix[i];
                    }
                }
            }
        } catch (ParseException | EvaluationException e) {
            // stop init
        }
	}

	public String getCType() {
		return "ulong *";
	}

	public void setToInitialValues() {
		for ( int i=0; i<matrix.length; i++ ) {
			matrix[i] = matrixInit[i];
		}
	}

	public Number getValueAt(int i, int j, int k) {
        int sx = safeGetSizeXValue();
        int sy = safeGetSizeYValue();
        int sz = safeGetSizeZValue();

        if(i>=0 && j>=0 && k>=0 && i< sx && j< sy && k< sz) {
			return matrix[i+sx*j+sx*sy*k];
		} else {
			return 0f;
		}
	}

	public void setValueAt(int i, int j, int k, Number v) {
		matrix[i+safeGetSizeXValue()*j+safeGetSizeXValue()*safeGetSizeYValue()*k] = v.longValue();
	}

	public Number getInitValueAt(int i, int j, int k) {
		return matrixInit[i+safeGetSizeXValue()*j+safeGetSizeXValue()*safeGetSizeYValue()*k];
	}

	public void setInitValueAt(int i, int j, int k, Number v) {
		matrixInit[i+safeGetSizeXValue()*j+safeGetSizeXValue()*safeGetSizeYValue()*k] = v.longValue();
	}

	public void writeToBoost(Boost boost) {
		super.writeToBoost(boost);
		BoostUtil.writeLongArray(boost, matrix);
		BoostUtil.writeLongArray(boost, matrixInit);
	}

	/**
	 * Visitor accept method.
	 */
	public void accept(ModelVisitor visitor) {
		visitor.visitMatrixULong(this);
	}

	public ChangeRecorder getChangeRecorder() {
		if ( getModel() != null ) {
			return getModel().getChangeRecorder();
		}
		return super.getChangeRecorder();
	}

}

