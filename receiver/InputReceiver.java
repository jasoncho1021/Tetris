package tetris.receiver;

import tetris.GameException;
import tetris.JobCallBack;
import tetris.JoyPad;
import tetris.TetrisRender;
import tetris.block.BlockMovement;
import tetris.block.BlockState;
import tetris.block.container.BlockContainer;
import tetris.queue.KeyInput;
import tetris.queue.TetrisQueue;
import tetris.queue.impl.InputQueue;
import tetris.queue.producer.TetrisThread;
import tetris.queue.producer.impl.InputConsole;
import tetris.queue.producer.impl.Producer;

interface GameCallBack {
	void gameRendering(boolean[][] map);
}

public class InputReceiver extends TetrisThread {

	private BlockMovement block;
	private BlockContainer blockContainer;
	private TetrisThread inputConsole;
	private TetrisThread producer;
	private Thread consoleThread;
	private Thread producerThread;
	private TetrisQueue<KeyInput> tetrisQueue;

	private TetrisRender tetrisRender;

	private boolean[][] gameMap;

	public InputReceiver(TetrisRender tetrisRender) {
		this.tetrisRender = tetrisRender;
	}

	public void start() {
		try {
			initInputListener();
			gameStart();
		} catch (GameException e) {
			e.printGameExceptionStack();
		} finally {
			inputConsole.stopRunning();

			producer.stopRunning();
			producerThread.interrupt();

			try {
				consoleThread.join();
				System.out.println("console join");

				producerThread.join();
				System.out.println("producer join");

			} catch (InterruptedException ie) {
				System.out.println("console or producer join interrupted");
			}

		}
	}

	private void initInputListener() {
		this.tetrisQueue = InputQueue.getInstance();

		inputConsole = new InputConsole(this.tetrisQueue);
		consoleThread = new Thread(inputConsole);
		consoleThread.start();

		producer = new Producer(this.tetrisQueue);
		producerThread = new Thread(producer);
		producerThread.start();
	}

	private KeyInput keyInput;
	private JoyPad joyPad;

	private void gameStart() {

		blockContainer = BlockContainer.getInstance();
		setNewBlock();

		tetrisRender.addJob(new JobCallBack() {
			@Override
			public void doJob(boolean[][] tetrisMap) {
				gameMap = tetrisMap;
				block.setBlockToMap(gameMap);
				keyInput = new KeyInput('x');
				render(BlockState.FALLING, keyInput.getItem());
			}
		});

		while (true) {
			keyInput = new KeyInput();

			// polling, Consuming
			// blocking if queue is empty
			tetrisQueue.get(keyInput);

			joyPad = keyInput.getItem();
			if (joyPad == JoyPad.UNDEFINED) {
				continue;
			}

			if (joyPad == JoyPad.QUIT) {
				break;
			}

			tetrisRender.addJob(new JobCallBack() {
				@Override
				public void doJob(boolean[][] tetrisMap) {
					gameMap = tetrisMap;
					if (!moveBlockAndRender(joyPad)) {
						tetrisQueue.add(new KeyInput('z'));
					}
				}
			});
		}
	}

	private void setNewBlock() {
		int idx = blockContainer.getNextBlockId();
		block = blockContainer.getNewBlock(idx);
	}

	private void moveBlock(JoyPad joyPad) {
		removePreviousFallingBlockFromMap();
		block.doKeyEvent(joyPad, gameMap);
	}

	private boolean moveBlockAndRender(JoyPad joyPad) {

		moveBlock(joyPad);

		BlockState blockState = combineBlockToMap();
		if (blockState == BlockState.TOUCH_CEIL) {
			return false;
		}

		tetrisRender.renderErased();
		render(blockState, joyPad);

		return true; // TOUCH_DOWN, FALLING
	}

	private void render(BlockState blockState, JoyPad joyPad) {
		StringBuilder sb = tetrisRender.setGameBoard();
		sb.append(joyPad);
		sb.append("\n");

		// set futureBlock
		if (blockState == BlockState.FALLING) {
			block.setFutureBlockToStringBuilder(gameMap, sb);
		}

		tetrisRender.renderGameBoard(sb.toString());
	}

	private BlockState combineBlockToMap() {
		/*
		 * moveBlock -> 안 겹치면 이동 및 회전, 겹치면 안 겹쳤던 직전 상태 유지. 
		 * isTouchDown -> isPossibleToPut -> 겹치는 여부 확인. 
		 */
		BlockState blockState;

		if (isTouchDown()) {
			if (block.isCeil()) {
				return BlockState.TOUCH_CEIL;
			}

			blockState = BlockState.TOUCH_DOWN;
			block.setBlockToMap(gameMap);

			tetrisRender.removePerfectLine();
			setNewBlock();
		} else {
			// FALLING
			blockState = BlockState.FALLING;
			block.setBlockToMap(gameMap);
		}

		return blockState;
	}

	private void removePreviousFallingBlockFromMap() {
		block.remove(gameMap);
	}

	private boolean isTouchDown() {
		// 방향 회전 또는 이동 ==>  블럭 중심 좌표 변경

		/*
		 * rerender 함수 첫 줄에서 block 이동시킨 모양이
		 * 바닥 경계와 겹치거나 이미 쌓여 있는 블럭과 겹치는지 확인 후
		 * 복구 또는 계속 진행
		 */
		if (block.isPossibleToPut(gameMap)) {
			return false;
		} else {
			block.recoverY();
			return true;
		}
	}

	@Override
	public void run() {
		start();
		stopRunning();
		tetrisRender.addJob(new JobCallBack() {
			@Override
			public void doJob(boolean[][] tetrisMap) {
				System.out.println("Game over");
			}
		});
	}
}
