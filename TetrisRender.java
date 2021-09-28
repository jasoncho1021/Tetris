package tetris;

import tetris.queue.producer.TetrisThread;
import tetris.receiver.InputReceiverCallBack;

public abstract class TetrisRender extends TetrisThread {

	public void addMoveBlockJob(JoyPad joyPad, InputReceiverCallBack callBack) {
	}

	public void gameStart() {
	}

	public void addLineJob() {
	}

	public void finishJob() {
	}
}
