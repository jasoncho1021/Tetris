package tetris;

public enum JoyPad {
	DOWN('k'), LEFT('j'), RIGHT('l'), CLOCK_WISE_ROTATION('f'), ANTI_CLOCK_WISE_ROTATION('d'), QUIT('z');

	private char key;

	JoyPad(char key) {
		this.key = key;
	}

	public char getKey() {
		return this.key;
	}

	public static JoyPad findByChar(Character input) {
		for (JoyPad joyPad : JoyPad.values()) {
			if (joyPad.getKey() == input) {
				return joyPad;
			}
		}
		throw new RuntimeException();
	}
}
