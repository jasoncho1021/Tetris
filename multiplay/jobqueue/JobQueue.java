package tetris.multiplay.jobqueue;

import tetris.core.queue.TetrisQueue;

public interface JobQueue<ItemBox> extends TetrisQueue<ItemBox> {
	public void finish();
	public void init();
}
