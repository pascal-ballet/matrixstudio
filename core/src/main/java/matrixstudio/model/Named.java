package matrixstudio.model;

import org.xid.basics.model.ModelObject;
import org.xid.basics.serializer.BoostObject;

public interface Named extends ModelObject, BoostObject {

	public String getName();
	

	public void setName(String newValue);
	

	/**
	 * Visitor accept method.
	 */
	public void accept(ModelVisitor visitor);
	

}

