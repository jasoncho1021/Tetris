package tetris.gameserver.server;

import tetris.queue.TetrisQueue;

public interface AttackReqQueue<ItemBox> extends TetrisQueue<ItemBox> {
	public void init();
}
