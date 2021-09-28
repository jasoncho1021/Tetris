package tetris.jobqueue;

import tetris.queue.TetrisQueue;

public interface JobQueue<ItemBox> extends TetrisQueue<ItemBox> {
	public void finish();
	public void init();
}
