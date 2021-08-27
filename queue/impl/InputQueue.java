package tetris.queue.impl;

import java.util.LinkedList;
import java.util.Queue;

import tetris.queue.KeyInput;
import tetris.queue.TetrisQueue;

public class InputQueue implements TetrisQueue {
	private Queue<KeyInput> queue;

	private InputQueue() {
		queue = new LinkedList<KeyInput>();
	}

	public static TetrisQueue getInstance() {
		return LazyHolder.INSTANCE;
	}

	private static class LazyHolder {
		private static final TetrisQueue INSTANCE = new InputQueue();
	}

	@Override
	public void add(KeyInput keyInput) {
		synchronized (this) {
			queue.offer(keyInput);
			notifyAll();
		}
	}

	@Override
	public void get(KeyInput keyOutput) {
		KeyInput queueInput;
		synchronized (this) {
			// blocking
			if (queue.isEmpty()) {
				try {
					wait();
				} catch (InterruptedException e) {
					System.out.println("getting from queue is interrupted");
				}
			}

			queueInput = queue.poll();
			keyOutput.joyPad = queueInput.joyPad;
			notifyAll();
		}
	}

}
