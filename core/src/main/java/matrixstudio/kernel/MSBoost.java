package matrixstudio.kernel;

import fr.minibilles.basics.serializer.JBoost;

/**
 */
public class MSBoost extends JBoost {

	private final boolean safeRead;

	public MSBoost(String type, int version, boolean safeRead) {
		super(type, version);
		this.safeRead = safeRead;
	}

	public boolean isSafeRead() {
		return safeRead;
	}

	public void endObject() {
		while (lookAheadChar != '}') nextToken();
	}
}
