package tetris.jobqueue;

import tetris.queue.TetrisQueue;

public interface JobQueue<T> extends TetrisQueue<T> {
	public void finish();
	public void init();
}
