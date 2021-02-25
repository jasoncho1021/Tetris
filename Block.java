package tetris;

public abstract class Block {

	private int x;
	private int y;

	Block(int x, int y) {
		this.x = x;
		this.y = y;
	}

	void dropY() {
		y++;
	}

	void rotate(boolean isClockWise) {
		if (isClockWise) {
			rotateClockWise();
		} else {
			rotateAntiClockWise();
		}
	}

	abstract void rotateClockWise();

	abstract void rotateAntiClockWise();

}
