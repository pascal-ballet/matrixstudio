package matrixstudio.kernel;

import org.xid.basics.serializer.JBoost;

/**
 */
public class MSBoost extends JBoost {

	private final boolean safeRead;

	public MSBoost(String type, int version) {
		this(type, version, false);
	}

	public MSBoost(String type, int version, boolean safeRead) {
		super(type, version);
		this.safeRead = safeRead;
	}

	public boolean isSafeRead() {
		return safeRead;
	}
}
