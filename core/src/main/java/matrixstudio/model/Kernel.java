package matrixstudio.model;

import fr.minibilles.basics.model.ChangeRecorder;
import fr.minibilles.basics.model.ModelObject;
import fr.minibilles.basics.serializer.Boost;
import fr.minibilles.basics.serializer.BoostObject;


public class Kernel extends Code implements ModelObject, BoostObject {

	private String contents = "";
        private long duration = 0;
        
	public Kernel() {
	}

	protected Kernel(Boost boost) {
		super(boost);
		contents = boost.readString();
	}

	/**
	 * <p>Gets contents.</p>
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * <p>Sets contents.</p>
	 */
	public void setContents(String newValue) {
		if (contents == null ? newValue != null : (contents.equals(newValue) == false)) {
			getChangeRecorder().recordChangeAttribute(this, "contents", this.contents);
			this.contents= newValue;
		}
	}

	/**
	 * <p>Gets duration of the task having executed this kernel (in millis). Made for profiling purpose.</p>
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * <p>Sets duration of the task having executed this kernel (in millis). Made for profiling purpose.</p>
	 */
	public void setDuration(long newValue) {
            this.duration= newValue;
	}
        
        
	/**
	 * <p><b>createKernelProrotype</b>: Create kernel function's prototype.
	 * @return a {@link String} with the prototype.</p>
	 */
	public String createKernelProrotype() {
		final StringBuilder proto = new StringBuilder();
		proto.append("__kernel void ");
		proto.append(getName());
		proto.append("(\n\t");
		proto.append("uint rand, uint step, uint mouseX, uint mouseY, uint mouseZ, uint mouseBtn, int key,\n\t");
		proto.append("uint workSizeX, uint workSizeY, uint workSizeZ");
		for ( Matrix matrix : getModel().getMatrixList() ) {
			proto.append(",\n\t__global ");
			proto.append(matrix.getCType());
			proto.append(" ");
			proto.append(matrix.getName());

            proto.append(", uint ");
			proto.append(matrix.getName());
			proto.append("_SX");

			proto.append(", uint ");
			proto.append(matrix.getName());
			proto.append("_SY");

            proto.append(", uint ");
            proto.append(matrix.getName());
            proto.append("_SZ");
		}
		proto.append(")");
		return proto.toString();
	}

	/**
	 * <p><b>getWholeContents</b>: Returns complete code template including owner class adaptation.
	 * @return a {@link String} with the complete code.</p>
	 */
	public String getWholeContents() {
		final StringBuilder codeWithPrototype = new StringBuilder();
		codeWithPrototype.append(createKernelProrotype());
		codeWithPrototype.append("\n{\n");
		codeWithPrototype.append(getContents() == null ? "" : getContents());
		codeWithPrototype.append("\n}\n");
		return codeWithPrototype.toString();
	}

	public void setWholeContents(String newContents) {
		final int start = newContents.indexOf("\n{\n") + 3;
		final int end = newContents.length() - 3;
		setContents(newContents.substring(start,end));
	}

	public int getOpeningBracketIndex() {
		return createKernelProrotype().length() + 3;
	}

	public int getClosingBracketIndex() {
		return createKernelProrotype().length() + 3 + (getContents() == null ? 0 : getContents().length());
	}

	public void writeToBoost(Boost boost) {
		super.writeToBoost(boost);
		boost.writeString(contents);
	}

	/**
	 * Visitor accept method.
	 */
	public void accept(ModelVisitor visitor) {
		visitor.visitKernel(this);
	}

	public ChangeRecorder getChangeRecorder() {
		if ( getModel() != null ) {
			return getModel().getChangeRecorder();
		}
		return super.getChangeRecorder();
	}

}

