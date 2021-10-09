package tetris.multiplay.v3.v_0.server;

import java.util.LinkedList;
import java.util.Queue;

import tetris.core.queue.KeyInput;
import tetris.core.queue.TetrisQueue;

public class ServerTetrisQueueImpl implements TetrisQueue<KeyInput> {

	private Queue<KeyInput> queue;

	public ServerTetrisQueueImpl() {
		queue = new LinkedList<KeyInput>();
	}

	@Override
	public void add(KeyInput input) {
		synchronized (this) {
			queue.offer(input);
			notifyAll();
		}
	}

	@Override
	public void get(KeyInput keyOutput) {
		KeyInput queueInput;
		synchronized (this) {
			// blocking
			while (queue.isEmpty()) {
				try {
					wait();
				} catch (InterruptedException e) {
					System.out.println("getting from queue is interrupted");
				}
			}

			queueInput = queue.poll();
			keyOutput.setItem(queueInput.getItem());
		}
	}

}
