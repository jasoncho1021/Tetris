package tetris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import tetris.block.BlockContainer;
import tetris.block.BlockMovable;
import tetris.console.InputConsole;
import tetris.console.TetrisInputListener;

/**
 * ubuntu bash 창에서 play 가능한 상태
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
	private InputConsole inputConsole;

	private boolean map[][] = new boolean[GameProperties.HEIGHT_PLUS_HIDDEN_START_PLUS_BOTTOM_BORDER][GameProperties.WIDTH_PLUS_SIDE_BORDERS];
	private boolean isGameOver = false;

	private void start() {
		initBorder();
		initKeyListener();
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

	private void initKeyListener() {
		inputConsole = new InputConsole(new TetrisInputListener() {

			@Override
			public void receiveKey(char input) {
				rerender(input);
			}

		});
		Thread keyListeningThread = new Thread(inputConsole);
		keyListeningThread.start();
	}

	private void gameStart() {
		int gameState = 2;
		System.out.print(drawMap());
		setNewBlock();

		while (!isGameOver) {

			// keep falling
			while (true) {
				try {
					gameState = rerender(new Character('k'));

					/**
					 * 
					 * 
					 *  removePerfectLine 한 뒤
					 *  이 찰나에
					 *  keyboard 가 rerender 호출한다.
					 *  block.remove 호출해서 에러남.
					 * 
					 *  setNewBlock을 rerender에 넣어서 해결.
					 * 
					 */

					if (gameState == -1) {
						isGameOver = true;
						break;
					} else if (gameState == 0) {
						break;
					}

					Thread.sleep(1000); // hold lock from main thread, then rerender?

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		inputConsole.cancel();
	}

	private void setNewBlock() {
		block = blockContainer.getNewBlock();
	}

	private synchronized int rerender(Character input) {
		int gameState = 1;

		removePreviousFallingBlockFromMap();

		block.doKeyEvent(input, map);

		/*
		 * doKeyEvent -> 안 겹치면 이동 및 회전, 겹치면 안 겹쳤던 직전 상태 유지. 
		 * isTouchDown -> isPossibleToPut -> 겹치는 여부 확인. 
		 */
		if (isTouchDown()) {
			gameState = 0;

			if (block.isCeil()) {
				gameState = -1;
				return gameState;
			}
		}

		block.setBlockToMap(map);

		if (gameState == 0) {
			removePerfectLine();
			setNewBlock();
		}

		// 콘솔 화면 전체 삭제
		erase(GameProperties.HEIGHT_PLUS_BOTTOM_BORDER);

		System.out.print(drawMap());

		return gameState; // keep falling
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
