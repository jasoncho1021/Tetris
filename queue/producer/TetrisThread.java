package tetris.queue.producer;

public abstract class TetrisThread implements Runnable {
	private volatile boolean isRunning;

	public synchronized void startRunning() {
		this.isRunning = true;
	}

	public synchronized void stopRunning() {
		this.isRunning = false;
	}

	public synchronized boolean isRunning() {
		return isRunning;
	}
}
