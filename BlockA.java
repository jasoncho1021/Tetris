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
		initShape();
	}

	/*
	 *  X X X
	 *  # # #
	 *  X X #
	 */
	private void initShape() {
		// [y][x]
		blockShape[1][0] = true;
		blockShape[1][1] = true;
		blockShape[1][2] = true;
		blockShape[2][2] = true;
	}

	/*
	 * # #
	 * # X
	 * # X
	 */
	@Override
	void rotateClockWise(final boolean[][] map) {
		if (isTop()) {
			return;
		}

		boolean tmp;
		int s = 0, e = 2;

		// center touch wall
		moveCenterFromWall();

		for (int i = s, j = e; i < e; i++, j--) {
			tmp = blockShape[s][i];
			blockShape[s][i] = blockShape[j][s];
			blockShape[j][s] = blockShape[e][j];
			blockShape[e][j] = blockShape[i][e];
			blockShape[i][e] = tmp;
		}
	}

	@Override
	void rotateAntiClockWise(final boolean[][] map) {
		if (isTop()) {
			return;
		}

		boolean tmp;
		int s = 0, e = 2;

		moveCenterFromWall();

		for (int i = s, j = e; i < e; i++, j--) {
			tmp = blockShape[s][i];
			blockShape[s][i] = blockShape[i][e];
			blockShape[i][e] = blockShape[e][j];
			blockShape[e][j] = blockShape[j][s];
			blockShape[j][s] = tmp;
		}
	}

	// long block 일땐 if(y <= 1)
	private boolean isTop() {
		if (y == 0) {
			return true;
		}
		return false;
	}

	@Override
	boolean setBlockToMap(boolean map[][]) {
		int nx, ny;

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				if (blockShape[row][col]) {
					nx = x + (col - 1);
					ny = y + (row - 1);

					// touchDown
					if (map[ny][nx]) {
						return false;
					}

					map[ny][nx] = true;
				}
			}
		}

		return true;
	}

	private void isStuck(final boolean[][] map) {
		
	}

	private void moveCenterFromWall() {
		if (x == 1) {
			++x;
		} else if (x == width - 1) {
			--x;
		}
	}

	@Override
	boolean isWall(int dx, final boolean[][] map) {
		int nx, ny;

		nx = 0;
		ny = 0;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				if (blockShape[row][col]) {
					nx = dx + (col - 1);
					ny = y + (row - 1);

					// touchWall
					if (nx < 0 || width <= nx) {
						return true;
					}

					if (map[ny][nx]) { // stackedMap 전달 받으니 렉 안 걸림.
						return true;
					}
				}
			}
		}
		return false;
	}
}
