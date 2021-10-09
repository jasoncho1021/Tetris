package tetris.core.queue;

import tetris.core.JoyPad;

public class KeyInput extends ItemBox<JoyPad> {

	public KeyInput() {
	}

	public KeyInput(Character input) {
		setItem(JoyPad.findByChar(input));
	}
}
