package tetris.multiplay.v3.v_0.server;

import tetris.multiplay.v2.client.TetrisRender;

public abstract class ServerTetrisRender extends TetrisRender {
	public void addInput(Character input) {
	}

	public boolean isGameOver() {
		return false;
	}
}
