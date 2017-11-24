package matrixstudio.model;

import fr.minibilles.basics.model.ChangeRecorder;
import fr.minibilles.basics.model.ModelObject;
import fr.minibilles.basics.serializer.Boost;
import fr.minibilles.basics.serializer.BoostObject;


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

