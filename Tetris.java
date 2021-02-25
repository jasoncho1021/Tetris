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
		tput cuu1
		i=$(( i + 1 ))
	done

 * @author glenn
 *
 */
public class Tetris {

	private int H = 30;
	private int W = 15;
	private boolean map[][] = new boolean[H][W];

	private Block[] blocks;

	private void init() {
		blocks = new Block[1];
		blocks[0] = new BlockA(0, W / 2);
	}

	public static void main(String[] args) {
		printAndListen();
	}

	static void printAndListen() {
		NumbersConsole numberConsole = new NumbersConsole();
		Thread keyListeningThread = new Thread(numberConsole);
		keyListeningThread.start();

		printmap(numberConsole);

		numberConsole.cancel();
	}

	/*
	 *   print -> data( <- nc ) ,,   print vs key 
	 *   0. 키눌림 
	 *   1. 데이터 변환
	 *   2. 눌린 키 값 초기화.
	 */
	static void printmap(NumbersConsole nc) {
		for (int i = 0; i < 5; i++) {
			String input = nc.getInput();
			System.out.print("first:" + input + "\n" + "sec:" + input + "\n");
			try {
				Thread.sleep(2000);
				// 콘솔에서 실행시 eclipse와 달리 /bin/bin/ 2번으로 인식 된다.
				// String userDirectory = System.getProperty("user.dir"); 
				String home = System.getenv("HOME");
				String path = home + "/multilineEraser.sh";
				int rowsToErase = 2;

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
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
