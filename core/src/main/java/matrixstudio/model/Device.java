package matrixstudio.model;


public enum Device {
ANY(0)
,CPU(1)
,GPU(2)
;
	private final int value;

	private Device(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}

