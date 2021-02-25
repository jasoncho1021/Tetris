package tetris;

/**
 * 
 * just 3X3 roatation.
 * except long block
 * 
 * @author glenn
 *
 */
public class BlockA extends Block {

	private boolean[][] blockShape = new boolean[3][3];

	public BlockA(int x, int y) {
		super(x, y);
	}

	/*
	 *  ㅁㅁㅁ
	 *     ㅁ
	 */
	private void initShape() {
		// [y][x]
		blockShape[1][0] = true;
		blockShape[1][1] = true;
		blockShape[1][2] = true;
		blockShape[2][2] = true;
	}

	@Override
	void rotateClockWise() {
		boolean tmp;
		int s = 0, e = 2;
		for (int i = s, j = e; i < e; i++, j--) {
			tmp = blockShape[s][i];
			blockShape[s][i] = blockShape[j][s];
			blockShape[j][s] = blockShape[e][j];
			blockShape[e][j] = blockShape[i][e];
			blockShape[i][e] = tmp;
		}
	}

	@Override
	void rotateAntiClockWise() {
		boolean tmp;
		int s = 0, e = 2;
		for (int i = s, j = e; i < e; i++, j--) {
			tmp = blockShape[s][i];
			blockShape[s][i] = blockShape[i][e];
			blockShape[i][e] = blockShape[e][j];
			blockShape[e][j] = blockShape[j][s];
			blockShape[j][s] = tmp;
		}
	}

	void isWall() {

	}

	void isTouchDown() {

	}
}
