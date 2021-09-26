package tetris.gameserver.server;

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
import tetris.queue.producer.TetrisThread;
import tetris.queue.producer.impl.Producer;

public class ServerInputReceiver extends TetrisThread {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	{
		// Get the process id
		String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", "");
		// MDC
		MDC.put("PID", pid);
	}

	private TetrisThread producer;
	private Thread producerThread;
	private TetrisQueue<KeyInput> tetrisQueue;
	private TetrisRender tetrisRender;

	public ServerInputReceiver(TetrisRender tetrisRender) {
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
				/*
				 * InputReceiver 리소스 정리하는 동안 jobQueue에 들어온 addLine 명령들을 수행하지 않기 위해 미리 중단.
				 * while(inputReceiver.isRunning())
				*/
				logger.debug("break");
				if (isRunning()) {
					tetrisRender.stopRunning();
					tetrisRender.finishJob();
					stopRunning();
				}
				return;
			}

			if (isRunning()) {
				tetrisRender.addJob(new JobCallBack() {
					@Override
					public void doJob() {
						//logger.debug("addJob: moveBlockAndRender");
						if (!tetrisRender.moveBlockAndRender(joyPad)) {
							logger.debug("ceil");
							tetrisQueue.add(new KeyInput('z')); // 천장 닿아서 종료
							// joyPad == JoyPad.QUIT 으로 
							tetrisRender.stopRunning();
							stopRunning();
						}
					}
				});
			}
		}
	}

	private void initInputListener() {
		tetrisQueue = new ServerTetrisQueueImpl();

		producer = new Producer(tetrisQueue);
		producerThread = new Thread(producer);
		producerThread.start();
	}

	public void addInput(Character input) {
		tetrisQueue.add(new KeyInput(input));
	}

	@Override
	public void run() {
		try {
			gameStart();
		} catch (GameException e) {
			e.printGameExceptionStack();
		} finally {
			logger.debug("finally");

			producer.stopRunning();
			producerThread.interrupt();

			try {

				if (producerThread != null) {
					producerThread.join();
					logger.debug("producer join");
				}

			} catch (InterruptedException ie) {
				logger.debug("console or producer join interrupted");
			}

		}
	}

}
