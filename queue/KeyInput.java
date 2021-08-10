package tetris.queue;

import tetris.JoyPad;

public class KeyInput {

	public JoyPad joyPad;

	public KeyInput() {
	}

	public KeyInput(Character input) {
		this.joyPad = JoyPad.findByChar(input);
	}

}
