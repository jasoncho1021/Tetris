package tetris;

public abstract class Block {

	// center position
	int x;
	int y;

	Block(int x, int y) {
		this.x = x;
		this.y = y;
	}

	void dropY() {
		y++;
	}

	abstract void rotateClockWise();

	abstract void rotateAntiClockWise();

	abstract void setBlockToMap(boolean[][] map);

}
