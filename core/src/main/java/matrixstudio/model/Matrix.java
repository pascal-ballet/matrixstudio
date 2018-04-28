package matrixstudio.model;

import fr.minibilles.basics.model.ChangeRecorder;
import fr.minibilles.basics.model.ModelObject;
import fr.minibilles.basics.serializer.Boost;
import fr.minibilles.basics.serializer.BoostObject;
import java.text.ParseException;
import matrixstudio.formula.EvaluationException;
import matrixstudio.formula.FormulaCache;


public abstract class Matrix implements ModelObject, BoostObject, Named {

	private Model model;

	private boolean random = true;

	private boolean ARGB = false;

	private boolean rainbow = false;

	private boolean ndRange = false;

	private String sizeX = "150";

	private String sizeY = "150";

	private String sizeZ = "150";

	private String name;

	public Matrix() {
	}

	protected Matrix(Boost boost) {
        boost.register(this);
        int version = boost.getFileVersion();
        model = boost.readObject(Model.class);
        random = boost.readBoolean();
        ndRange = boost.readBoolean();
        if (version >= 3) {
            sizeX = boost.readString();
            sizeY = boost.readString();
            sizeZ = boost.readString();
			if(version >= 5) {
				ARGB = boost.readBoolean();
			}
			if(version >= 6) {
				rainbow = boost.readBoolean();
			}
        }else {
            boost.readInt(); // deprecated size field.
            sizeX = Integer.toString(boost.readInt());
            sizeY = Integer.toString(boost.readInt());
            sizeZ = Integer.toString(boost.readInt());
        }
		name = boost.readString();
	}

	/**
	 * <p>Gets model.</p>
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * <p>Sets model.</p>
	 */
	public void setModel(Model newValue) {
		if (model == null ? newValue != null : (model.equals(newValue) == false)) {
			getChangeRecorder().recordChangeAttribute(this, "model", this.model);
			this.model= newValue;
		}
	}

	/**
	 * <p>Gets random.</p>
	 */
	public boolean isRandom() {
		return random;
	}

	/**
	 * <p>Sets random.</p>
	 */
	public void setRandom(boolean newValue) {
		if (random != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "random", this.random);
			this.random= newValue;
		}
	}

	/**
	 * <p>Gets ARGB.</p>
	 */
	public boolean isARGB() {
		return ARGB;
	}

	/**
	 * <p>Sets ARGB.</p>
	 */
	public void setARGB(boolean newValue) {
		if (ARGB != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "ARGB", this.ARGB);
			this.ARGB= newValue;
		}
	}

	/**
	 * <p>Gets rainbow.</p>
	 */
	public boolean isRainbow() {
		return rainbow;
	}

	/**
	 * <p>Sets rainbow.</p>
	 */
	public void setRainbow(boolean newValue) {
		if (rainbow != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "Rainbow", this.rainbow);
			this.rainbow = newValue;
		}
	}

	/**
	 * <p>Gets ndRange.</p>
	 */
	public boolean isNdRange() {
		return ndRange;
	}

	/**
	 * <p>Sets ndRange.</p>
	 */
	public void setNdRange(boolean newValue) {
		if (ndRange != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "ndRange", this.ndRange);
			this.ndRange= newValue;
		}
	}

	/**
	 * <p>Gets sizeX.</p>
	 */
	public String getSizeX() {
		return sizeX;
	}

	/**
	 * <p>Gets computed sizeX.</p>
	 */
	public int getSizeXValue() throws EvaluationException, ParseException {
		return FormulaCache.SHARED.computeValue(sizeX, getModel());
	}

	public int safeGetSizeXValue() {
        try {
            return getSizeXValue();
        } catch (ParseException | EvaluationException e) {
            return 0;
        }
    }

	/**
	 * <p>Sets sizeX.</p>
	 */
	public void setSizeX(String newValue) {
		if (sizeX != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "sizeX", this.sizeX);
			this.sizeX= newValue;
		}
	}

	/**
	 * <p>Gets sizeY.</p>
	 */
	public String getSizeY() {
		return sizeY;
	}

    /**
     * <p>Gets computed sizeY.</p>
     */
    public int getSizeYValue() throws EvaluationException, ParseException {
        return FormulaCache.SHARED.computeValue(sizeY, getModel());
    }

    public int safeGetSizeYValue() {
        try {
            return getSizeYValue();
        } catch (ParseException | EvaluationException e) {
            return 0;
        }
    }

	/**
	 * <p>Sets sizeY.</p>
	 */
	public void setSizeY(String newValue) {
		if (sizeY != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "sizeY", this.sizeY);
			this.sizeY= newValue;
		}
	}

	/**
	 * <p>Gets sizeZ.</p>
	 */
	public String getSizeZ() {
		return sizeZ;
	}

    /**
     * <p>Gets computed sizeZ.</p>
     */
    public int getSizeZValue() throws EvaluationException, ParseException {
        return FormulaCache.SHARED.computeValue(sizeZ, getModel());
    }

    public int safeGetSizeZValue() {
        try {
            return getSizeZValue();
        } catch (ParseException | EvaluationException e) {
            return 0;
        }
    }

	/**
	 * <p>Sets sizeZ.</p>
	 */
	public void setSizeZ(String newValue) {
		if (sizeZ != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "sizeZ", this.sizeZ);
			this.sizeZ= newValue;
		}
	}

	/**
	 * <p>Gets name.</p>
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>Sets name.</p>
	 */
	public void setName(String newValue) {
		if (name == null ? newValue != null : (name.equals(newValue) == false)) {
			getChangeRecorder().recordChangeAttribute(this, "name", this.name);
			this.name= newValue;
		}
	}

	public void initBlank(boolean force) {
		// TODO implement initBlank(...)
		throw new UnsupportedOperationException();
	}

	public String getCType() {
		// TODO implement getCType(...)
		throw new UnsupportedOperationException();
	}

	public void setToInitialValues() {
		// TODO implement setToInitialValues(...)
		throw new UnsupportedOperationException();
	}

	public Number getValueAt(int i, int j, int k) {
		// TODO implement getValueAt(...)
		throw new UnsupportedOperationException();
	}

	public void setValueAt(int i, int j, int k, Number v){
		// TODO implement setValueAt(...)
		throw new UnsupportedOperationException();
	}

	public void setInitValueAt(int i, int j, int k, Number v) {
		// TODO implement setInitValueAt(...)
		throw new UnsupportedOperationException();
	}

	public void writeToBoost(Boost boost) {
		boost.writeObject(model);
		boost.writeBoolean(random);
		boost.writeBoolean(ndRange);
		boost.writeString(sizeX);
		boost.writeString(sizeY);
		boost.writeString(sizeZ);
		boost.writeBoolean(ARGB);
		boost.writeBoolean(rainbow);
		boost.writeString(name);
	}

	/**
	 * Visitor accept method.
	 */
	public abstract void accept(ModelVisitor visitor);
	

	public ChangeRecorder getChangeRecorder() {
		if ( getModel() != null ) {
			return getModel().getChangeRecorder();
		}
		return ChangeRecorder.Stub;
	}

}

