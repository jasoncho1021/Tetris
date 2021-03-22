package tetris;

public abstract class Block {

	static int height;
	static int width;
	// center position
	int x;
	int y;

	private boolean stopFlag;

	Block(int x, int y) {
		this.x = x;
		this.y = y;
		this.stopFlag = false;
	}

	void stopDrop() {
		stopFlag = true;
		--y; // recovery once here
	}

	void startDrop() {
		stopFlag = false;
	}

	boolean isStop() {
		return stopFlag;
	}

	void dropY() {
		if (!stopFlag && (y < (height - 1))) {
			++y;
		}
	}

	void recoverY() {
		--y;
	}

	void moveLeft(final boolean[][] map) {
		if (!isWall(x - 1, map)) {
			--x;
		}
	}

	void moveRight(final boolean[][] map) {
		if (!isWall(x + 1, map)) {
			++x;
		}
	}

	void doKeyEvent(Character input, final boolean[][] stackedMap) {
		switch (input) {
		case 'j':
			moveLeft(stackedMap);
			break;
		case 'l':
			moveRight(stackedMap);
			break;
		case 'k':
			dropY();
			break;
		case 'd':
			rotateAntiClockWise(stackedMap);
			break;
		case 'f':
			rotateClockWise(stackedMap);
			break;
		default:
			break;
		}
	}

	abstract void rotateClockWise(final boolean[][] map);

	abstract void rotateAntiClockWise(final boolean[][] map);

	abstract boolean setBlockToMap(boolean[][] map);

	abstract boolean isWall(int nx, final boolean[][] map);

}
