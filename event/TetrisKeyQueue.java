package tetris.event;

import java.util.LinkedList;
import java.util.Queue;

public class TetrisKeyQueue {

	private Queue<Character> tetrisKeyQueue;
	private boolean access;

	public TetrisKeyQueue() {
		tetrisKeyQueue = new LinkedList<Character>();
		access = true;
	}

	public Character pump() {
		if (access && !tetrisKeyQueue.isEmpty()) {
			return tetrisKeyQueue.poll();
		}
		return null;
	}

	public void post(Character input) {
		tetrisKeyQueue.add(input);
	}

	public void setEnable() {
		access = true;
	}

	public void setUnable() {
		access = false;
	}

	public void clearQueue() {
		tetrisKeyQueue.clear();
	}

}
