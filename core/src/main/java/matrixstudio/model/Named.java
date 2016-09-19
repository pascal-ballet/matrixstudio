package matrixstudio.model;

import org.xid.basics.model.ModelObject;
import org.xid.basics.serializer.BoostObject;

public interface Named extends ModelObject, BoostObject {

	String getName();
	

	void setName(String newValue);
	

	/**
	 * Visitor accept method.
	 */
	void accept(ModelVisitor visitor);
	

}

