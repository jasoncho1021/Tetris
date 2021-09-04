package tetris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import tetris.block.BlockMovement;
import tetris.block.container.BlockContainer;
import tetris.queue.KeyInput;
import tetris.queue.TetrisQueue;
import tetris.queue.impl.InputQueue;
import tetris.queue.producer.TetrisProducer;
import tetris.queue.producer.impl.InputConsole;
import tetris.queue.producer.impl.Producer;

/**
 * ubuntu bash 창에서 play 가능합니다.
 * 
 * "multilineEraser.sh"
	if [ $# -ne 1 ]; then
		exit 0
	fi

	i=1
	while [ "$i" -le $1 ]; do
		tput cuu1 #Move the cursor up 1 line
		tput el	#Clear from the cursor to the end of the line
		i=$(( i + 1 ))
	done

 * @author glenn
 *
 */

public class TetrisGame {

	private BlockMovement block;
	private BlockContainer blockContainer;
	private TetrisQueue tetrisQueue;
	private TetrisProducer inputConsole;
	private TetrisProducer producer;
	private Thread consoleThread;
	private Thread producerThread;

	private boolean map[][] = new boolean[GameProperties.HEIGHT_PLUS_HIDDEN_START_PLUS_BOTTOM_BORDER][GameProperties.WIDTH_PLUS_SIDE_BORDERS];

	public void start() {
		try {
			initBorder();
			initInputListener();
			gameStart();
		} catch (GameException e) {
			e.printGameExceptionStack();
		} finally {
			inputConsole.stopProduce();

			producer.stopProduce();
			producerThread.interrupt();

			try {
				consoleThread.join();
				System.out.println("console join");

				producerThread.join();
				System.out.println("producer join");

				System.out.println("game ended");
			} catch (InterruptedException ie) {
				System.out.println("join interrupted");
			}

		}
	}

	private void initBorder() {
		for (int row = 0; row < GameProperties.HEIGHT_PLUS_HIDDEN_START_PLUS_BOTTOM_BORDER; row++) {
			for (int col = 0; col < GameProperties.WIDTH_PLUS_SIDE_BORDERS; col++) {
				if (col == 0 || col > GameProperties.WIDTH) {
					map[row][col] = true;
				}
				if (row == GameProperties.HEIGHT_PLUS_HIDDEN_START) {
					map[row][col] = true;
				}
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

	/**
	 *  main.gameStart() == Consumer
	 */
	private KeyInput keyInput;

	private void gameStart() {

		blockContainer = BlockContainer.getInstance();
		setNewBlock();

		block.setBlockToMap(map);
		keyInput = new KeyInput('x');
		renderGameBoard(setGameBoard(BlockState.FALLING));

		while (true) {
			keyInput = new KeyInput();

			// polling, Consuming
			// blocking if queue is empty
			tetrisQueue.get(keyInput);

			if (keyInput.joyPad == JoyPad.UNDEFINED) {
				continue;
			}

			if (keyInput.joyPad == JoyPad.QUIT) {
				break;
			}

			if (!moveBlockAndRender(keyInput.joyPad)) {
				break;
			}
		}
	}

	private void setNewBlock() {
		int idx = blockContainer.getNextBlockId();
		block = blockContainer.getNewBlock(idx);
	}

	private void moveBlock(JoyPad joyPad) {
		removePreviousFallingBlockFromMap();
		block.doKeyEvent(joyPad, map);
	}

	private boolean moveBlockAndRender(JoyPad joyPad) {

		moveBlock(joyPad);

		BlockState blockState = combineBlockToMap();
		if (blockState == BlockState.TOUCH_CEIL) {
			return false;
		}

		render(blockState);

		return true; // TOUCH_DOWN, FALLING
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
			block.setBlockToMap(map);

			removePerfectLine();
			setNewBlock();
		} else {
			// FALLING
			blockState = BlockState.FALLING;
			block.setBlockToMap(map);
		}

		return blockState;
	}

	private void render(BlockState blockState) {
		// 콘솔 화면 전체 삭제
		renderGameBoard(erase(GameProperties.HEIGHT_PLUS_BOTTOM_BORDER_PLUS_INPUT));
		renderGameBoard(setGameBoard(blockState));
	}

	private void removePreviousFallingBlockFromMap() {
		block.remove(map);
	}

	private boolean isTouchDown() {
		// 방향 회전 또는 이동 ==>  블럭 중심 좌표 변경

		/*
		 * rerender 함수 첫 줄에서 block 이동시킨 모양이
		 * 바닥 경계와 겹치거나 이미 쌓여 있는 블럭과 겹치는지 확인 후
		 * 복구 또는 계속 진행
		 */
		if (block.isPossibleToPut(map)) {
			return false;
		} else {
			block.recoverY();
			return true;
		}
	}

	private String setGameBoard(BlockState blockState) {
		StringBuilder sb = new StringBuilder();
		int lineNum = 1;
		for (int row = GameProperties.HIDDEN_START_HEIGHT; row < GameProperties.HEIGHT_PLUS_HIDDEN_START_PLUS_BOTTOM_BORDER; row++) {
			for (int col = 0; col < GameProperties.WIDTH_PLUS_SIDE_BORDERS; col++) {
				if (col == 0 || col > GameProperties.WIDTH) {
					if (row == GameProperties.HEIGHT_PLUS_HIDDEN_START) {
						sb.append(" ");
					} else {
						sb.append(Integer.toHexString(lineNum));
					}
				} else if (map[row][col]) {
					if (row == GameProperties.HEIGHT_PLUS_HIDDEN_START) {
						sb.append("^");
					} else {
						sb.append("#");
					}
				} else {
					sb.append(" ");
				}
			}
			sb.append("\n");
			lineNum = (++lineNum) % 16;
			if (lineNum == 0) {
				lineNum++;
			}
		}
		sb.append(keyInput.joyPad);
		sb.append("\n");

		// set futureBlock
		if (blockState == BlockState.FALLING) {
			block.setFutureBlockToStringBuilder(map, sb);
		}
		return sb.toString();
	}

	private void removePerfectLine() {
		boolean tempMap[][] = new boolean[GameProperties.HEIGHT_PLUS_HIDDEN_START_PLUS_BOTTOM_BORDER][GameProperties.WIDTH_PLUS_SIDE_BORDERS];

		int blockCount;
		int tempRow = GameProperties.HEIGHT_PLUS_HIDDEN_START;
		for (int row = GameProperties.HEIGHT_PLUS_HIDDEN_START; row >= 0; row--) {
			blockCount = 0;
			for (int col = 0; col < GameProperties.WIDTH_PLUS_SIDE_BORDERS; col++) {
				if (map[row][col]) {
					blockCount++;
				}
			}

			// 완벽한 줄들 건너 뜀 == (삭제)
			// 완벽하지 못 한 줄들 땡김.
			// 외벽 포함해서(true) 통째로 카운트
			if (tempRow == (GameProperties.HEIGHT_PLUS_HIDDEN_START)
					|| blockCount < GameProperties.WIDTH_PLUS_SIDE_BORDERS) {
				for (int col = 0; col < GameProperties.WIDTH_PLUS_SIDE_BORDERS; col++) {
					tempMap[tempRow][col] = map[row][col];
				}
				tempRow--;
			}
		}

		for (int row = GameProperties.HEIGHT_PLUS_HIDDEN_START; row > tempRow; row--) {
			for (int col = 0; col < GameProperties.WIDTH_PLUS_SIDE_BORDERS; col++) {
				map[row][col] = tempMap[row][col];
			}
		}

		// 나머지 텅 빈 윗 줄들 양쪽 테두리 만들기.
		while (tempRow >= 0) {
			map[tempRow][0] = true;
			map[tempRow][(GameProperties.WIDTH_PLUS_SIDE_BORDERS - 1)] = true;
			tempRow--;
		}
	}

	private String erase(int rowsToErase) {
		/*
		 * dir
		 * 1. eclipse : project/
		 * 2. console : project/bin
		 */
		String dir = System.getProperty("user.dir");
		String path = dir + "/multilineEraser.sh";

		String[] cmd = { path, String.valueOf(rowsToErase) };

		String gameBoard = "";
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			gameBoard = builder.toString();
		} catch (IOException e) {
			throw new GameException(e);
		}
		return gameBoard;
	}

	private void renderGameBoard(String gameBoard) {
		System.out.print(gameBoard);
	}

	public static void main(String[] args) {
		TetrisGame tetris = new TetrisGame();
		tetris.start();
	}

}
