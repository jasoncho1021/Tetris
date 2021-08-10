package tetris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import tetris.block.BlockContainer;
import tetris.block.BlockMovable;
import tetris.queue.InputQueue;
import tetris.queue.KeyInput;
import tetris.queue.TetrisProducer;
import tetris.queue.TetrisQueue;
import tetris.queue.producer.Producer;
import tetris.queue.producer.console.InputConsole;

/**
 * ubuntu bash 창에서 play 가능합니다.
 * --> bash 옵션을 사용하여 화면에 출력된 문자열들을 덮어쓰거나 입력 키값을 엔터 없이 받는다. 
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

	private BlockMovable block;
	private BlockContainer blockContainer = BlockContainer.getInstance();
	private TetrisQueue tetrisQueue = InputQueue.getInstance();
	private TetrisProducer inputConsole;
	private TetrisProducer producer;

	private boolean map[][] = new boolean[GameProperties.HEIGHT_PLUS_HIDDEN_START_PLUS_BOTTOM_BORDER][GameProperties.WIDTH_PLUS_SIDE_BORDERS];

	private void start() {
		initBorder();
		initInputListener();
		gameStart();
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
		inputConsole = new InputConsole(this.tetrisQueue);
		Thread consoleThread = new Thread(inputConsole);
		consoleThread.start();

		producer = new Producer(this.tetrisQueue);
		Thread producerThread = new Thread(producer);
		producerThread.start();
	}

	/**
	 *  main.gameStart() == Consumer
	 */
	private void gameStart() {
		BlockState blockState = BlockState.FALLING;
		KeyInput keyInput;

		System.out.print(drawMap());
		setNewBlock();

		while (true) {
			keyInput = new KeyInput();

			// Consuming
			// blocking if queue is empty
			tetrisQueue.get(keyInput);

			if (keyInput.joyPad == JoyPad.QUIT) {
				break;
			}

			blockState = rerender(keyInput.joyPad);

			if (blockState == BlockState.TOUCH_CEIL) {
				break;
			}
		}

		inputConsole.stopProduce();
		producer.stopProduce();
	}

	private void setNewBlock() {
		block = blockContainer.getNewBlock();
	}

	/**
	 * 기존 동시 접근 케이스 때문에 --> synchronized 적용했다.
	 * main -> rerender
	 * Console -> rerender
	 * 
	 * inputConsole 이 rerender 호출했던 방식에서 tetrisQueue.add 만 하는 방식으로 전환.
	 * 
	 * 수정 후) Polling 방식, tetrisQueue.get 내부에서 while문으로 blocking 시킨다.
	 * main -> rerender
	 * Console -> TetrisQueue
	 * 
	 * main만 접근하므로 synchronized 제거.
	 */
	private BlockState rerender(JoyPad joyPad) {
		BlockState blockState = BlockState.FALLING;

		removePreviousFallingBlockFromMap();

		block.doKeyEvent(joyPad, map);

		/*
		 * doKeyEvent -> 안 겹치면 이동 및 회전, 겹치면 안 겹쳤던 직전 상태 유지. 
		 * isTouchDown -> isPossibleToPut -> 겹치는 여부 확인. 
		 */
		if (isTouchDown()) {
			blockState = BlockState.TOUCH_DOWN;

			if (block.isCeil()) {
				blockState = BlockState.TOUCH_CEIL;
				return blockState;
			}
		}

		block.setBlockToMap(map);

		if (blockState == BlockState.TOUCH_DOWN) {
			removePerfectLine();
			setNewBlock();
		}

		// 콘솔 화면 전체 삭제
		erase(GameProperties.HEIGHT_PLUS_BOTTOM_BORDER);

		System.out.print(drawMap());

		return blockState;
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

	private String drawMap() {
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

	private void erase(int rowsToErase) {
		String home = System.getenv("HOME");
		String path = home + "/multilineEraser.sh";

		String[] cmd = { path, String.valueOf(rowsToErase) };

		try {
			Process process = Runtime.getRuntime().exec(cmd);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			String result = builder.toString();
			System.out.print(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		TetrisGame tetris = new TetrisGame();
		tetris.start();
	}

}
