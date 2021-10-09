package tetris.multiplay.v3.v_0.server;

import tetris.core.queue.TetrisQueue;

public interface AttackReqQueue<ItemBox> extends TetrisQueue<ItemBox> {
	public void init();
}
