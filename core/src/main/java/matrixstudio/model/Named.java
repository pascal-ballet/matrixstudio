package matrixstudio.model;

import fr.minibilles.basics.model.ModelObject;
import fr.minibilles.basics.serializer.BoostObject;

public interface Named extends ModelObject, BoostObject {

	String getName();
	

	void setName(String newValue);
	

	/**
	 * Visitor accept method.
	 */
	void accept(ModelVisitor visitor);
	

}

