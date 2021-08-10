package tetris.block;

import tetris.GameProperties;

public abstract class Block implements BlockMovable {

	/* 
	 * default setting 3 X 3
	 * 1) +
	 * 2) x
	 */
	// x, y
	private static int mask[][] = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 }, { 1, -1 }, { 1, 1 }, { -1, 1 },
			{ -1, -1 } };

	// rotation center position
	private int x;
	private int y;
	private int shapeSize;

	protected boolean[][] blockShape;

	protected Block(int shapeSize) {
		this.x = (GameProperties.WIDTH_PLUS_SIDE_BORDERS / 2);
		this.y = 0;
		this.shapeSize = shapeSize;
		this.blockShape = new boolean[shapeSize][shapeSize];

		initShape();
	}

	protected abstract void initShape();

	@Override
	public void doKeyEvent(Character input, final boolean[][] map) {
		switch (input) {
		case 'j':
			moveLeft(map);
			break;
		case 'l':
			moveRight(map);
			break;
		case 'k':
			dropY();
			break;
		case 'd': // AntiClockWise
			if (isTop()) {
				return;
			}
			rotateAntiClockWise();
			if (!isPossibleToRotate(map)) {
				rotateClockWise();
			}
			break;
		case 'f': // ClockWise
			if (isTop()) {
				return;
			}
			rotateClockWise();
			if (!isPossibleToRotate(map)) {
				rotateAntiClockWise();
			}
			break;
		default:
			break;
		}
	}

	private void moveLeft(final boolean[][] map) {
		if (!isWall(x - 1, map)) {
			--x;
		}
	}

	private void moveRight(final boolean[][] map) {
		if (!isWall(x + 1, map)) {
			++x;
		}
	}

	private boolean isWall(int dx, final boolean[][] map) {
		int nx = 0;
		int ny = 0;
		for (int row = 0; row < shapeSize; row++) {
			for (int col = 0; col < shapeSize; col++) {
				if (blockShape[row][col]) {
					nx = dx + (col - 1);
					ny = y + (row - 1);

					// touchWall
					if (nx <= 0 || (GameProperties.WIDTH_PLUS_SIDE_BORDERS - 1) <= nx) {
						return true;
					}

					// touchOtherBlock
					if (map[ny][nx]) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void dropY() {
		if (y < GameProperties.HEIGHT_PLUS_HIDDEN_START) {
			++y;
		}
	}

	private boolean isTop() {
		if (y == 0) {
			return true;
		}
		return false;
	}

	private void rotateClockWise() {
		boolean tmp;
		for (int s = 0, e = shapeSize - 1; s < e; s++, e--) {
			for (int i = s, j = e; i < e; i++, j--) {
				tmp = blockShape[s][i];
				blockShape[s][i] = blockShape[j][s];
				blockShape[j][s] = blockShape[e][j];
				blockShape[e][j] = blockShape[i][e];
				blockShape[i][e] = tmp;
			}
		}
	}

	private void rotateAntiClockWise() {
		boolean tmp;
		for (int s = 0, e = shapeSize - 1; s < e; s++, e--) {
			for (int i = s, j = e; i < e; i++, j--) {
				tmp = blockShape[s][i];
				blockShape[s][i] = blockShape[i][e];
				blockShape[i][e] = blockShape[e][j];
				blockShape[e][j] = blockShape[j][s];
				blockShape[j][s] = tmp;
			}
		}
	}

	protected boolean isPossibleToRotate(final boolean[][] map) {
		int cx, cy;
		int[][] mask = getMask();
		if (isOverlapped(x, y, map)) {
			for (int i = 0; i < mask.length; i++) {
				cx = x + mask[i][0];
				cy = y + mask[i][1];

				if (GameProperties.isIn(cx, cy)) {
					if (!isOverlapped(cx, cy, map)) {
						x = cx;
						y = cy;
						return true;
					}
				}
			}
			return false;
		}

		return true;
	}

	protected int[][] getMask() {
		return mask;
	}

	private boolean isOverlapped(int cx, int cy, final boolean[][] map) {
		int ny, nx;
		for (int row = 0; row < shapeSize; row++) {
			for (int col = 0; col < shapeSize; col++) {
				if (blockShape[row][col]) {
					ny = cy + (row - 1);
					nx = cx + (col - 1);

					if (GameProperties.isIn(nx, ny)) {
						if (map[ny][nx]) {
							return true;
						}
					} else {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void recoverY() {
		--y;
	}

	@Override
	public boolean isPossibleToPut(final boolean map[][]) {
		int nx, ny;
		for (int row = 0; row < shapeSize; row++) {
			for (int col = 0; col < shapeSize; col++) {
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

	@Override
	public void setBlockToMap(boolean map[][]) {
		int nx, ny;

		for (int row = 0; row < shapeSize; row++) {
			for (int col = 0; col < shapeSize; col++) {
				if (blockShape[row][col]) {
					nx = x + (col - 1);
					ny = y + (row - 1);

					// draw
					map[ny][nx] = true;
				}
			}
		}
	}

	@Override
	public void remove(boolean[][] map) {
		int nx = 0;
		int ny = 0;
		for (int row = 0; row < shapeSize; row++) {
			for (int col = 0; col < shapeSize; col++) {
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
					nx = x + (col - 1); // (1,1) center 정규화.
					ny = y + (row - 1);

					map[ny][nx] = false;
				}
			}
		}
	}

	@Override
	public boolean isCeil() {
		int topY = shapeSize;
		topLoop: for (int row = 0; row < shapeSize; row++) {
			for (int col = 0; col < shapeSize; col++) {
				if (blockShape[row][col]) {
					topY = y + (row - 1);
					break topLoop;
				}
			}
		}

		if (topY < GameProperties.HIDDEN_START_HEIGHT) { // hiddenHeight
			return true;
		}

		return false;
	}
}
