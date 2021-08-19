package tetris.queue.producer;

public abstract class TetrisProducer implements Runnable {
	private boolean isRunning;

	public void startProduce() {
		this.isRunning = true;
	}

	public void stopProduce() {
		this.isRunning = false;
	}

	public boolean isRunning() {
		return isRunning;
	}
}
