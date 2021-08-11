package tetris.queue.producer.impl;

import tetris.queue.KeyInput;
import tetris.queue.TetrisQueue;
import tetris.queue.producer.TetrisProducer;

public class Producer extends TetrisProducer {

	private TetrisQueue tetrisQueue;

	public Producer(TetrisQueue tetrisQueue) {
		this.tetrisQueue = tetrisQueue;
	}

	@Override
	public void run() {
		startProduce();
		while (isRunning()) {

			tetrisQueue.add(new KeyInput('k'));

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
