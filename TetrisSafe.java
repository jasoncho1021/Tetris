package tetris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

/*
  [TODO]
   1. 터치다운 됐을 때 옆 장애물 간격 보고 회전 안 시키는 로직도 필요할 듯.
   2. 공중에서 벽면이 아닌 블럭 옆면으로 막혔을때
   3 .sb.setCharAt(index, ch); 으로 height * width 반복 횟수 줄이기.
*/
public class TetrisSafe {

	private final int hiddenStartHeight = 2;
	private final int drawHeight = 10 + 1;//30;
	private final int height = drawHeight + hiddenStartHeight;
	private final int width = 9 + 2;//25;

	private boolean map[][] = new boolean[height][width];

	private Block block;

	public static void main(String[] args) {
		TetrisSafe tetris = new TetrisSafe();
		tetris.start();
	}

	void start() {
		initBorder();
		printAndListen();
	}

	private void initBorder() {
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (col == 0 || col == (width - 1)) {
					map[row][col] = true;
				}
				if (row == (height - 1)) {
					map[row][col] = true;
				}
			}
		}
	}

	private boolean isGameOver = false;

	private void printAndListen() {
		NumbersConsole numberConsole = new NumbersConsole(this);
		Thread keyListeningThread = new Thread(numberConsole);
		keyListeningThread.start();

		Block.height = height;
		Block.width = width;

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
					 *  keyboard 가 rerender 호출해서.
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

		numberConsole.cancel();
	}

	private void setNewBlock() {
		block = new BlockA((width / 2), 0); // ceil touch 우회. 근데, 이러면 블럭 모양마다 y 값이 달라야한다.
		//block.setBlockToMap(map);
	}

	// synchronized
	synchronized private int rerender(Character input) {
		int gameState = 1;

		removePreviousFallingBlockFromMap();

		block.doKeyEvent(input, map);

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
		erase(drawHeight);

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
		for (int row = hiddenStartHeight; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (col == 0 || col == (width - 1)) {
					if (row == height - 1) {
						sb.append(" ");
					} else {
						sb.append(row % 10);
					}
				} else if (map[row][col]) {
					if (row == height - 1) {
						sb.append("^");
					} else {
						sb.append("#");
					}
				} else {
					sb.append(" ");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private void removePerfectLine() {
		boolean tempMap[][] = new boolean[height][width];

		int blockCount;
		int tempRow = height - 1;
		for (int row = height - 1; row >= 0; row--) {
			blockCount = 0;
			for (int col = 0; col < width; col++) {
				if (map[row][col]) {
					blockCount++;
				}
			}

			// 외벽 포함해서(true) 통째로 카운트
			if (tempRow == (height - 1) || blockCount < width) {
				for (int col = 0; col < width; col++) {
					tempMap[tempRow][col] = map[row][col];
				}
				tempRow--;
			}
		}

		for (int row = height - 1; row > tempRow; row--) {
			for (int col = 0; col < width; col++) {
				map[row][col] = tempMap[row][col];
			}
		}

		while (tempRow >= 0) {
			map[tempRow][0] = true;
			map[tempRow][(width - 1)] = true;
			tempRow--;
		}
	}

	void receiveKey(char input) {
		rerender(input);
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

}
