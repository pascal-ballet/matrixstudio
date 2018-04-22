package matrixstudio.kernel;

import fr.minibilles.basics.sexp.model.Referencer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import matrixstudio.model.Code;
import matrixstudio.model.Task;

public class ModelReferencer implements Referencer {

	private final Map<Object, String> references = new HashMap<Object, String>();

	private int taskCount = 0;

	@Override
	public boolean pushContext(Object object) {
		return false;
	}

	@Override
	public boolean popContext(Object object) {
		return false;
	}

	@Override
	public void forceReference(Object object, String reference) {
		references.put(object, reference);
	}

	@Override
	public String referenceFor(Object object) {
		if (references.containsKey(object)) {
			return references.get(object);
		}
		else {
			if (object instanceof Code) {
				return ((Code) object).getName();
			}
			else if (object instanceof Task) {
				return Integer.toString(taskCount++);
			}
			return null;
		}
	}


	@Override
	public Map<String, Object> builtins() {
		return Collections.emptyMap();
	}
}
