package tetris.core.queue.producer.impl;

import tetris.core.queue.KeyInput;
import tetris.core.queue.TetrisQueue;
import tetris.core.queue.producer.TetrisThread;

public class Producer extends TetrisThread {

	private TetrisQueue<KeyInput> tetrisQueue;

	public Producer(TetrisQueue<KeyInput> tetrisQueue) {
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
