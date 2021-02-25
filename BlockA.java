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

	// 중앙, 시계방향
	static int[] dx = { 0, 0, 1, 1, 1, 0, -1, -1, -1 };
	static int[] dy = { 0, -1, -1, 0, 1, 1, 1, 0, -1 };
	static int[] px = { 1, 1, 2, 2, 2, 1, 0, 0, 0 };
	static int[] py = { 1, 0, 0, 1, 2, 2, 2, 1, 0 };

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

	@Override
	void setBlockToMap(boolean map[][]) {
		int nx, ny;
		for (int i = 0; i < 9; i++) {
			if (blockShape[py[i]][px[i]]) {
				nx = x + dx[i];
				ny = y + dy[i];
				map[ny][nx] = true;
			}
		}
	}

	void isWall() {

	}

	void isTouchDown() {

	}
}
