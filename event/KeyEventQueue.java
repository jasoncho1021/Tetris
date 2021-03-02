package tetris.event;

import java.util.LinkedList;
import java.util.Queue;

public class KeyEventQueue {

	private Queue<TetrisKeyEvent> keyQueue;
	private boolean access;

	public KeyEventQueue() {
		keyQueue = new LinkedList<TetrisKeyEvent>();
		access = true;
	}

	public TetrisKeyEvent pump() {
		if (access) {
			return keyQueue.poll();
		} else {
			return null;
		}
	}

	public void post(TetrisKeyEvent tetrisKeyEvent) {
		keyQueue.add(tetrisKeyEvent);
	}

	public void setAvailable() {
		access = true;
	}

	public void setUnable() {
		access = false;
	}
}
