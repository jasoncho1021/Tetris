package tetris.multiplay.v2.client;

import tetris.core.JoyPad;
import tetris.core.queue.producer.TetrisThread;
import tetris.multiplay.receiver.InputReceiverCallBack;

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
