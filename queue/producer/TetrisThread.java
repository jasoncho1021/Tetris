package tetris.queue.producer;

public abstract class TetrisThread implements Runnable {
	private boolean isRunning;

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
