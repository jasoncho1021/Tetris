package tetris;

public abstract class Block {

	static int height;
	static int width;
	// center position
	int x;
	int y;

	Block(int x, int y) {
		this.x = x;
		this.y = y;
	}

	void dropY() {
		++y;
	}

	void recoverY() {
		--y;
	}

	void moveLeft() {
		if (!isWall(x-1)) {
			--x;
		}
	}

	void moveRight() {
		if (!isWall(x+1)) {
			++x;
		}
	}

	abstract void rotateClockWise();

	abstract void rotateAntiClockWise();

	abstract boolean setBlockToMap(boolean[][] map);

	abstract boolean isWall(int nx);

}
