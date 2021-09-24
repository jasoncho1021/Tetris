package tetris;

import tetris.queue.producer.TetrisThread;

public abstract class TetrisRender extends TetrisThread {
	// for InputReceiver
	public boolean moveBlockAndRender(JoyPad joyPad) {
		return false;
	}

	// for Client
	public void addJob(JobCallBack jobCallBack) {
	}

	public void gameStart() {
	}

	public void addLine() {
	}

	public void finishJob() {
	}
}
