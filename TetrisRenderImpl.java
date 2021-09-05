package tetris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import tetris.jobqueue.JobInput;
import tetris.jobqueue.JobQueue;
import tetris.queue.TetrisQueue;
import tetris.receiver.InputReceiver;

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

public class TetrisRenderImpl implements TetrisRender {

	private boolean map[][] = new boolean[GameProperties.HEIGHT_PLUS_HIDDEN_START_PLUS_BOTTOM_BORDER][GameProperties.WIDTH_PLUS_SIDE_BORDERS];

	private TetrisQueue<JobInput> jobQueue = JobQueue.getInstance();

	public void gameStart() {
		initBorder();

		InputReceiver inputReceiver = new InputReceiver(this);
		inputReceiver.startRunning();

		Thread inputReceiverThread = new Thread(inputReceiver);

		inputReceiverThread.start();

		JobInput jobInput;
		while (inputReceiver.isRunning()) {
			jobInput = new JobInput();
			jobQueue.get(jobInput);
			doJobCallBack(jobInput.getItem());
		}

		try {
			inputReceiverThread.join();
			System.out.println("inputReceiver join");
		} catch (InterruptedException e) {
			System.out.println("inputReceiver join interrupted");
		}
	}

	@Override
	public void addJob(JobCallBack jobCallBack) {
		jobQueue.add(new JobInput(jobCallBack));
	}

	private void doJobCallBack(JobCallBack jobCallBack) {
		jobCallBack.doJob(map);
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

	public void renderErased() {
		renderGameBoard(erase(GameProperties.HEIGHT_PLUS_BOTTOM_BORDER_PLUS_INPUT));
	}

	public StringBuilder setGameBoard() {
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

		return sb;
	}

	public void removePerfectLine() {
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

	public void renderGameBoard(String gameBoard) {
		System.out.print(gameBoard);
	}

	public static void main(String[] args) {
		TetrisRenderImpl tetrisGameImpl = new TetrisRenderImpl();
		tetrisGameImpl.gameStart();
	}

}
