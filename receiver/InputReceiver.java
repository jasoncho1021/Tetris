package tetris.receiver;

import java.lang.management.ManagementFactory;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	{
		// Get the process id
		String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", "");
		// MDC
		MDC.put("PID", pid);
	}

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
				stopRunning(); // InputReceiver 리소스 정리하는 동안 jobQueue에 들어온 addLine 명령들을 수행하지 않기 위해 미리 중단.
				logger.debug("break");
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
			gameStart();
		} catch (GameException e) {
			e.printGameExceptionStack();
		} finally {
			logger.debug("finally");
			inputConsole.stopRunning();

			producer.stopRunning();
			producerThread.interrupt();

			try {
				if (consoleThread != null) {
					consoleThread.join();
					logger.debug("console join");
				}

				if (producerThread != null) {
					producerThread.join();
					logger.debug("producer join");
				}

				tetrisRender.addJob(new JobCallBack() {
					@Override
					public void doJob() {
						// add this job to break jobQueue.get() blocking state
					}
				});
			} catch (InterruptedException ie) {
				logger.debug("console or producer join interrupted");
			}

		}
	}

}
