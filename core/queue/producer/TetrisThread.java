package tetris.core.queue.producer;

public abstract class TetrisThread implements Runnable {
	private volatile boolean isRunning;

	public void startRunning() {
		this.isRunning = true;
	}

	public void stopRunning() {
		this.isRunning = false;
	}

	public boolean isRunning() {
		return isRunning;
	}
}
