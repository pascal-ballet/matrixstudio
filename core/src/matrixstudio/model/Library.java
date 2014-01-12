package matrixstudio.model;

import org.xid.basics.serializer.BoostObject;
import org.xid.basics.serializer.Boost;
import org.xid.basics.model.ModelObject;
import org.xid.basics.model.ChangeRecorder;
import matrixstudio.model.ModelVisitor;
import matrixstudio.model.Code;


public class Library extends Code implements ModelObject, BoostObject {

	public Library() {
	}

	protected Library(Boost boost) {
		super(boost);
	}

	public String getWholeContents() {
		return getContents();
	}

	public void setWholeContents(String newContents) {
		setContents(newContents);
	}

	public void writeToBoost(Boost boost) {
		super.writeToBoost(boost);
	}

	/**
	 * Visitor accept method.
	 */
	public void accept(ModelVisitor visitor) {
		visitor.visitLibrary(this);
	}

	public ChangeRecorder getChangeRecorder() {
		if ( getModel() != null ) {
			return getModel().getChangeRecorder();
		}
		return super.getChangeRecorder();
	}

}

