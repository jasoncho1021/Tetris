package tetris.queue;

public abstract class TetrisProducer implements Runnable {
	private boolean isRunning;

	public void startProduce() {
		this.isRunning = true;
	}

	public void stopProduce() {
		this.isRunning = false;
	}

	protected boolean isRunning() {
		return isRunning;
	}
}
