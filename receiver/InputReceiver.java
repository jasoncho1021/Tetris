package tetris.receiver;

import tetris.GameException;
import tetris.JobCallBack;
import tetris.JoyPad;
import tetris.TetrisRender;
import tetris.queue.KeyInput;
import tetris.queue.TetrisQueue;
import tetris.queue.impl.InputQueue;
import tetris.queue.producer.TetrisThread;
import tetris.queue.producer.impl.InputConsole;
import tetris.queue.producer.impl.Producer;

public class InputReceiver extends TetrisThread {

	private TetrisThread inputConsole;
	private TetrisThread producer;
	private Thread consoleThread;
	private Thread producerThread;
	private TetrisQueue<KeyInput> tetrisQueue;
	private TetrisRender tetrisRender;

	public InputReceiver(TetrisRender tetrisRender) {
		this.tetrisRender = tetrisRender;
		startRunning();
	}

	private void gameStart() {
		initInputListener();

		while (true) {
			KeyInput keyInput = new KeyInput();

			// polling, Consuming
			// blocking if queue is empty
			tetrisQueue.get(keyInput);

			JoyPad joyPad = keyInput.getItem();
			if (joyPad == JoyPad.UNDEFINED) {
				continue;
			}

			if (joyPad == JoyPad.QUIT) {
				stopRunning();
				return;
			}

			tetrisRender.addJob(new JobCallBack() {
				@Override
				public void doJob() {
					if (!tetrisRender.moveBlockAndRender(joyPad)) {
						/* 어차피 이 스레드 사라지면 tetris참조도 사라짐..
						 * 따라서, finally에서 종료된거 확인하고 종료시켜야함.
						 * 아님.
						 */
						tetrisQueue.add(new KeyInput('z')); // 천장 닿아서 종료
						// joyPad == JoyPad.QUIT 으로 
					}
				}
			});
		}
	}

	private void initInputListener() {
		tetrisQueue = InputQueue.getInstance();

		inputConsole = new InputConsole(tetrisQueue);
		consoleThread = new Thread(inputConsole);
		consoleThread.start();

		producer = new Producer(tetrisQueue);
		producerThread = new Thread(producer);
		producerThread.start();
	}

	@Override
	public void run() {
		try {
			//startRunning();
			gameStart();
			//stopRunning();
		} catch (GameException e) {
			e.printGameExceptionStack();
		} finally {
			inputConsole.stopRunning();

			producer.stopRunning();
			producerThread.interrupt();

			try {
				if (consoleThread != null) {
					consoleThread.join();
					System.out.println("console join");
				}

				if (producerThread != null) {
					producerThread.join();
					System.out.println("producer join");
				}

				tetrisRender.addJob(new JobCallBack() {
					@Override
					public void doJob() {
						// add to JobQuee to notifyAll
					}
				});
			} catch (InterruptedException ie) {
				System.out.println("console or producer join interrupted");
			}

		}
	}

}
