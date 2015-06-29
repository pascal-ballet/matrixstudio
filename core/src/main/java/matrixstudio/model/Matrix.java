package matrixstudio.model;

import org.xid.basics.model.ChangeRecorder;
import org.xid.basics.model.ModelObject;
import org.xid.basics.serializer.Boost;
import org.xid.basics.serializer.BoostObject;


public abstract class Matrix implements ModelObject, BoostObject, Named {

	private Model model;

	private boolean random = false;

	private boolean ndRange = false;

	private int size = 512;

	private int sizeX = 512;

	private int sizeY = 256;

	private int sizeZ = 1;

	private String name;

	public Matrix() {
	}

	protected Matrix(Boost boost) {
		boost.register(this);
		model = boost.readObject(Model.class);
		random = boost.readBoolean();
		ndRange = boost.readBoolean();
		size = boost.readInt();
		sizeX = boost.readInt();
		sizeY = boost.readInt();
		sizeZ = boost.readInt();
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
	 * <p>Gets size.</p>
	 */
	public int getSize() {
		return size;
	}

	/**
	 * <p>Sets size.</p>
	 */
	public void setSize(int newValue) {
		if (size != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "size", this.size);
			this.size= newValue;
		}
	}

	/**
	 * <p>Gets sizeX.</p>
	 */
	public int getSizeX() {
		return sizeX;
	}

	/**
	 * <p>Sets sizeX.</p>
	 */
	public void setSizeX(int newValue) {
		if (sizeX != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "sizeX", this.sizeX);
			this.sizeX= newValue;
		}
	}

	/**
	 * <p>Gets sizeY.</p>
	 */
	public int getSizeY() {
		return sizeY;
	}

	/**
	 * <p>Sets sizeY.</p>
	 */
	public void setSizeY(int newValue) {
		if (sizeY != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "sizeY", this.sizeY);
			this.sizeY= newValue;
		}
	}

	/**
	 * <p>Gets sizeZ.</p>
	 */
	public int getSizeZ() {
		return sizeZ;
	}

	/**
	 * <p>Sets sizeZ.</p>
	 */
	public void setSizeZ(int newValue) {
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

	public void initBlank() {
		// TODO implement initBlank(...)
		throw new UnsupportedOperationException();
	}

	public int getLength() {
		return sizeX * sizeY * sizeZ;
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

	public void setValueAt(int i, int j, int k, Number v) {
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
		boost.writeInt(size);
		boost.writeInt(sizeX);
		boost.writeInt(sizeY);
		boost.writeInt(sizeZ);
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

