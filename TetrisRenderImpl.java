package tetris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import tetris.block.BlockMovement;
import tetris.block.BlockState;
import tetris.block.container.BlockContainer;
import tetris.jobqueue.JobInput;
import tetris.jobqueue.JobQueue;
import tetris.network.client.MessageSender;
import tetris.queue.KeyInput;
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

public class TetrisRenderImpl extends TetrisRender {
	private boolean map[][] = new boolean[GameProperties.HEIGHT_PLUS_HIDDEN_START_PLUS_BOTTOM_BORDER][GameProperties.WIDTH_PLUS_SIDE_BORDERS];
	private BlockMovement block;
	private BlockContainer blockContainer;
	private TetrisQueue<JobInput> jobQueue = JobQueue.getInstance();

	private volatile boolean isRunning;

	public void startRunning() {
		this.isRunning = true;
	}

	public void stopRunning() {
		this.isRunning = false;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void gameStart() {
		InputReceiver inputReceiver;
		Thread inputReceiverThread = null;
		try {
			blockContainer = BlockContainer.getInstance();
			setNewBlock();

			initBorder();

			// before producer thread add keyInput in inputReceiverThread
			addJob(new JobCallBack() {
				@Override
				public void doJob() {
					block.setBlockToMap(map);
					KeyInput keyInput = new KeyInput('x');
					render(BlockState.FALLING, keyInput.getItem());
				}
			});

			inputReceiver = new InputReceiver(this);
			inputReceiverThread = new Thread(inputReceiver);
			inputReceiverThread.start();

			JobInput jobInput;
			while (inputReceiver.isRunning()) {
				jobInput = new JobInput();
				jobQueue.get(jobInput); // blocking,,until inputReceiver addJob or Attack addJob
				doJobCallBack(jobInput.getItem());
			}

		} catch (GameException e) {
			e.printGameExceptionStack();
		} finally {
			try {
				if (inputReceiverThread != null) {
					inputReceiverThread.join();
					System.out.println("inputReceiver join");
				}
			} catch (InterruptedException e) {
				System.out.println("inputReceiver join interrupted");
			}
		}

	}

	public void addJob(JobCallBack jobCallBack) {
		jobQueue.add(new JobInput(jobCallBack));
	}

	private void doJobCallBack(JobCallBack jobCallBack) {
		jobCallBack.doJob();
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

	private int flipper = 1;

	public void addLine() {
		removePreviousFallingBlockFromMap();

		boolean tempRow[] = new boolean[GameProperties.WIDTH_PLUS_SIDE_BORDERS];
		boolean bufRow[] = new boolean[GameProperties.WIDTH_PLUS_SIDE_BORDERS];

		for (int col = 1; col < GameProperties.WIDTH_PLUS_SIDE_BORDERS; col++) {
			if ((col % 2) == flipper) {
				bufRow[col] = true;
			}
		}
		// add
		for (int row = GameProperties.HEIGHT_PLUS_HIDDEN_START - 1; row >= 0; row--) {
			for (int col = 1; col < GameProperties.WIDTH_PLUS_SIDE_BORDERS; col++) {
				tempRow[col] = map[row][col];
			}

			for (int col = 1; col < GameProperties.WIDTH_PLUS_SIDE_BORDERS; col++) {
				map[row][col] = bufRow[col];
				bufRow[col] = tempRow[col];
			}
		}

		flipper = 1 - flipper;

		moveBlockAndRender(JoyPad.UNDEFINED); // producer 에서 sleep 이후에 넣으면 InputReceiver 쪽 로직에서 종료시킬 것임.
	}

	public boolean moveBlockAndRender(JoyPad joyPad) {
		moveBlock(joyPad);

		BlockState blockState = combineBlockToMap();
		if (blockState == BlockState.TOUCH_CEIL) {
			return false;
		}

		renderErased();
		render(blockState, joyPad);

		return true; // TOUCH_DOWN, FALLING
	}

	private void moveBlock(JoyPad joyPad) {
		removePreviousFallingBlockFromMap();
		block.doKeyEvent(joyPad, map);
	}

	private void removePreviousFallingBlockFromMap() {
		block.remove(map);
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

	private void setNewBlock() {
		int idx = blockContainer.getNextBlockId();
		block = blockContainer.getNewBlock(idx);
	}

	private void render(BlockState blockState, JoyPad joyPad) {
		StringBuilder sb = setGameBoard();
		sb.append(joyPad);
		sb.append("\n");

		// set futureBlock
		if (blockState == BlockState.FALLING) {
			block.setFutureBlockToStringBuilder(map, sb);
		}

		renderGameBoard(sb.toString());
	}

	public void renderErased() {
		renderGameBoard(erase(GameProperties.HEIGHT_PLUS_BOTTOM_BORDER_PLUS_INPUT));
	}

	public StringBuilder setGameBoard() {
		StringBuilder sb = new StringBuilder();
		int lineNum = 1;
		for (int row = GameProperties.HIDDEN_START_HEIGHT; row < GameProperties.HEIGHT_PLUS_HIDDEN_START_PLUS_BOTTOM_BORDER; row++) {
			for (int col = 0; col < GameProperties.WIDTH_PLUS_SIDE_BORDERS; col++) {
				if (col == 0 || col > GameProperties.WIDTH) { // side line
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
		int num = 0;

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
			if (tempRow == (GameProperties.HEIGHT_PLUS_HIDDEN_START)) {
				for (int col = 0; col < GameProperties.WIDTH_PLUS_SIDE_BORDERS; col++) {
					tempMap[tempRow][col] = map[row][col];
				}
				tempRow--;
			} else if (blockCount < GameProperties.WIDTH_PLUS_SIDE_BORDERS) {
				for (int col = 0; col < GameProperties.WIDTH_PLUS_SIDE_BORDERS; col++) {
					tempMap[tempRow][col] = map[row][col];
				}
				tempRow--;
			} else {
				num++;
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

		if (num > 0) {
			messageSender.sendAttackMessage();
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

	public TetrisRenderImpl() {
	}

	private MessageSender messageSender;

	public TetrisRenderImpl(MessageSender messageSender) {
		this.messageSender = messageSender;
	}

	public static void main(String[] args) {
		TetrisRenderImpl tetrisGameImpl = new TetrisRenderImpl();
		tetrisGameImpl.gameStart();
	}

	@Override
	public void run() {
		gameStart();
		stopRunning();
		messageSender.resumeChatting();
	}

}
