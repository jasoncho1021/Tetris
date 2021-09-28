package tetris.gameserver.server;

import tetris.TetrisRender;

public abstract class ServerTetrisRender extends TetrisRender {
	public void addInput(Character input) {
	}

	public boolean isGameOver() {
		return false;
	}
}
