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

	/**
	 * x,y means the center point of block rotation == @ 
	 * 
	 *  X X X
	 *  # @ #
	 *  X X #
	 * 
	 * @param x
	 * @param y
	 */
	public BlockA(int x, int y) {
		super(x, y);
		initShape();
	}

	public static void main(String[] args) {
		BlockA block = new BlockA(0, 1);

		StringBuilder sb;
		for (int k = 0; k < 4; k++) {
			sb = new StringBuilder();

			for (int row = 0; row < 3; row++) {
				for (int col = 0; col < 3; col++) {
					if (block.blockShape[row][col]) {
						sb.append("#");
					} else {
						sb.append("*");
					}
				}
				sb.append("\n");
			}

			System.out.println(sb.toString());
			block.rotateAntiClockWise();
		}
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
	void rotateClockWise() {
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
	void rotateAntiClockWise() {
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

					// draw
					map[ny][nx] = true;
				}
			}
		}

		return true;
	}

	@Override
	boolean isPossibleToPut(final boolean map[][]) {
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
				}
			}
		}
		return true;
	}

	private void moveCenterFromWall() {
		if (x == 1) {
			++x;
		} else if (x == width - 2) { // 수정 오른쪽 옆 통과 수정. -1 ==> -2
			--x;
		}
	}

	@Override
	boolean isWall(int dx, final boolean[][] map) {
		int nx = 0;
		int ny = 0;
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

	@Override
	void remove(boolean[][] map) {
		int nx = 0;
		int ny = 0;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				if (blockShape[row][col]) {
					//  중심 + ( 9 방향 offset)
					/**
					 *  00 01 02 
					 *  10 11 12
					 *  20 21 22
					 *  
					 *  -1-1 -10 -11
					 *   0-1  00  01
					 *   1-1  10  11
					 */
					nx = x + (col - 1);
					ny = y + (row - 1);

					map[ny][nx] = false;
				}
			}
		}
	}

	@Override
	boolean isCeil() {

		int topY = 3;
		topLoop: for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				if (blockShape[row][col]) {
					topY = y + (row - 1);
					break topLoop;
				}
			}
		}

		if (topY < 2) {
			return true;
		}

		return false;
	}
}
