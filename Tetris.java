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
public class Tetris {

	private int height = 30;
	private int width = 15;
	private boolean map[][] = new boolean[height][width];
	private boolean stackedMap[][] = new boolean[height][width];

	Block block;

	public static void main(String[] args) {
		Tetris tetris = new Tetris();
		tetris.init();
		tetris.printAndListen();
	}

	private void init() {
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

	/*
	 * 	센터 중심으로 , 회전상태, 그리기
	 *  벽충돌 로직은?
	 *
	 *  1. 키 눌린다
	 *  2. 블럭 상태(중심위치, 회전) 바꾼다
	 *  3. 벽, 바닥 충돌 감지. // 블록이 판단.
	 *  4. 블럭 지운다.
	 *  4. 전체 맵에 박는다.
	 *     for
	 *       for 
	 *
	 */

	private void printAndListen() {
		NumbersConsole numberConsole = new NumbersConsole(this);
		Thread keyListeningThread = new Thread(numberConsole);
		keyListeningThread.start();

		// 블럭 착륙할 때마다 랜덤으로 숫자 뽑는다. 숫자와 맵핑되는 BlockA.class 불러와서 객체생성하기.
		block = new BlockA(width / 2, 0);
		block.setBlockToMap(map);
		System.out.print(drawMap());

		for (int i = 0; i < height; i++) {
			try {
				Thread.sleep(1000);

				print(block);

				block.dropY();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		numberConsole.cancel();
	}

	private String drawMap() {
		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (map[row][col]) {
					sb.append("#");
				} else {
					sb.append(" ");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private void removeObjectFromMap() {
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				map[row][col] = stackedMap[row][col];
			}
		}
	}

	private void print(Block block) {
		erase(height);
		removeObjectFromMap();

		block.setBlockToMap(map);
		System.out.print(drawMap());
	}

	void receiveKey(char input) {
		switch (input) {
		case 'j':
			block.x--;
			break;
		case 'l':
			block.x++;
			break;
		case 'k':
			block.dropY();
			break;
		case 'd':
			block.rotateAntiClockWise();
			break;
		case 'f':
			block.rotateClockWise();
			break;
		default:
			break;
		}

		print(block);
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
