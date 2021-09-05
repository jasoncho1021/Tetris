package tetris.queue.producer.impl;

import tetris.queue.KeyInput;
import tetris.queue.TetrisQueue;
import tetris.queue.producer.TetrisThread;

public class Producer extends TetrisThread {

	private TetrisQueue tetrisQueue;

	public Producer(TetrisQueue tetrisQueue) {
		this.tetrisQueue = tetrisQueue;
	}

	@Override
	public void run() {
		startRunning();
		while (isRunning()) {

			tetrisQueue.add(new KeyInput('k'));

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("producer interrupted");
			}
		}
		System.out.println("producer end");
	}

}
