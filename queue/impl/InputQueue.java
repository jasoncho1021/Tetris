package tetris.queue.impl;

import java.util.LinkedList;
import java.util.Queue;

import tetris.queue.KeyInput;
import tetris.queue.TetrisQueue;

public class InputQueue implements TetrisQueue<KeyInput> {
	private Queue<KeyInput> queue;

	private InputQueue() {
		queue = new LinkedList<KeyInput>();
	}

	public static TetrisQueue<KeyInput> getInstance() {
		return LazyHolder.INSTANCE;
	}

	private static class LazyHolder {
		private static final TetrisQueue<KeyInput> INSTANCE = new InputQueue();
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
