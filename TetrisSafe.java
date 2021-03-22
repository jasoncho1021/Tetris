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
	private final int drawHeight = 30;
	private final int height = drawHeight + hiddenStartHeight;
	private final int width = 25;

	private boolean map[][] = new boolean[height][width];
	private boolean stackedMap[][] = new boolean[height][width];

	Block block;

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
					stackedMap[row][col] = true;
					map[row][col] = true;
				}
				if (row == (height - 1)) {
					stackedMap[row][col] = true;
					map[row][col] = true;
				}
			}
		}
	}

	private boolean isGameOver = false;
	private boolean isValidBlock = true;
	private boolean alreadyTouchDown = false;

	private void printAndListen() {
		NumbersConsole numberConsole = new NumbersConsole(this);
		Thread keyListeningThread = new Thread(numberConsole);
		keyListeningThread.start();

		Block.height = height;
		Block.width = width;

		System.out.print(drawMap());

		while (!isGameOver) {
			setNewBlock();
			isValidBlock = true;
			alreadyTouchDown = false;

			int h = 0;
			while (isValidBlock && (h < height + 1)) {
				try {
					rerender(new Character('k'));

					h++;

					Thread.sleep(1000);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("end");

		numberConsole.cancel();
	}

	// synchronized
	synchronized private void rerender(Character input) {
		block.doKeyEvent(input, stackedMap);

		if (!alreadyTouchDown && isTouchDown()) {

			if (isCeilTouched()) {
				isValidBlock = false;
				isGameOver = true;
			}

			isValidBlock = false;
		}
	}

	private void setNewBlock() {
		block = new BlockA((width / 2), 0);
		block.setBlockToMap(map);
	}

	private boolean isTouchDown() {
		erase(drawHeight);
		removeObjectFromMap();

		// touchDown
		if (!block.setBlockToMap(map)) {
			block.recoverY();
			removeObjectFromMap();

			block.setBlockToMap(map);

			oneLineChecker();

			System.out.print(drawMap());
			saveObjectToMap();

			alreadyTouchDown = true;
			return true;
		} else {
			System.out.print(drawMap());
			return false;
		}
	}

	private boolean isCeilTouched() {
		for (int col = 1; col < width - 1; col++) {
			if (map[1][col] || map[0][col]) {
				return true;
			}
		}
		return false;
	}

	private String drawMap() {
		StringBuilder sb = new StringBuilder();
		for (int row = hiddenStartHeight; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (col == 0 || col == (width - 1)) {
					sb.append(row % 10);
				} else if (map[row][col]) {
					sb.append("#");
				} else {
					sb.append(" ");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private void oneLineChecker() {
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

	private void removeObjectFromMap() {
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				map[row][col] = stackedMap[row][col];
			}
		}
	}

	private void saveObjectToMap() {
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				stackedMap[row][col] = map[row][col];
			}
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
