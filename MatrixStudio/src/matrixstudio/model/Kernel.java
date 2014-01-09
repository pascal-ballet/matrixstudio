package matrixstudio.model;

import org.xid.basics.serializer.BoostObject;
import org.xid.basics.serializer.Boost;
import org.xid.basics.model.ModelObject;
import org.xid.basics.model.ChangeRecorder;
import matrixstudio.model.ModelVisitor;
import matrixstudio.model.Code;


public class Kernel extends Code implements ModelObject, BoostObject {

	private String contents = "";

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
	 * <p><b>createKernelProrotype</b>: Create kernel function's prototype.
	 * @return a {@link String} with the prototype.</p>
	 */
	public String createKernelProrotype() {
		final StringBuilder proto = new StringBuilder();
		proto.append("__kernel void ");
		proto.append(getName());
		proto.append("( uint rand, uint step, uint mouseX, uint mouseY, uint mouseBtn");
		for ( Matrix matrix : getModel().getMatrixList() ) {
			proto.append(",\n\t__global ");
			proto.append(matrix.getCType());
			proto.append(" ");
			proto.append(matrix.getName());
			proto.append(", uint "+matrix.getName()+"_SX");
			proto.append(", uint "+matrix.getName()+"_SY");
			proto.append(", uint "+matrix.getName()+"_SZ");
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

