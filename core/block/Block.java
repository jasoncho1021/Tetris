package tetris.core.block;

import java.util.LinkedList;
import java.util.List;

import tetris.core.GameProperties;
import tetris.core.JoyPad;

public abstract class Block implements BlockMovement {
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

	protected Block(int shapeSize, int y) {
		init(shapeSize, y);
	}

	protected Block(int shapeSize) {
		init(shapeSize, 0);
	}

	private void init(int shapeSize, int y) {
		this.x = (GameProperties.WIDTH_PLUS_SIDE_BORDERS / 2);
		this.y = y;
		this.shapeSize = shapeSize;
		this.blockShape = new boolean[shapeSize][shapeSize];

		initShape();
	}

	protected abstract void initShape();

	// block 의 중심 좌표만 수정한다. map에 적용하지는 않는다.
	@Override
	public void doKeyEvent(JoyPad joyPad, final boolean[][] map) {
		switch (joyPad) {
		case LEFT:
			moveLeft(map);
			break;
		case RIGHT:
			moveRight(map);
			break;
		case DOWN:
			dropY();
			break;
		case ANTI_CLOCK_WISE_ROTATION:
			if (isTop()) {
				return;
			}
			rotateAntiClockWise();
			if (!isPossibleToRotate(map)) {
				rotateClockWise();
			}
			break;
		case CLOCK_WISE_ROTATION:
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

	// 방금 초기화된 블럭인지 확인하기 위해, y축 값 확인 
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

	private boolean isPossibleToRotate(final boolean[][] map) {
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

	static class Pos {
		int x, y;

		Pos(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pos other = (Pos) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}

	}

	@Override
	public void setFutureBlockToStringBuilder(final boolean map[][], StringBuilder sb) {
		List<Pos> blockPosList = new LinkedList<Pos>();
		int nx, ny;
		for (int row = 0; row < shapeSize; row++) {
			for (int col = 0; col < shapeSize; col++) {
				if (blockShape[row][col]) {
					ny = y + (row - 1);
					nx = x + (col - 1);
					if (map[ny][nx]) {
						blockPosList.add(new Pos(nx, ny));
					}
				}
			}
		}

		int fy = y;

		loop: while (true) {
			fy++;
			for (int row = 0; row < shapeSize; row++) {
				for (int col = 0; col < shapeSize; col++) {
					/**
					 * y 	[ |   ]
					 * by	[ v ^ ]  ny
					 *  	[   | ]  fy
					 
					 *    a a
					 *  a x b    b b
					 *  b b    b b
					 */
					if (blockShape[row][col]) {
						ny = fy + (row - 1);
						nx = x + (col - 1);

						if (!blockPosList.contains(new Pos(nx, ny))) {
							if (map[ny][nx]) { // original 블럭이 아닌 아래 쪽 쌓인 블럭 또는 바닥과 충돌 했을 때
								break loop;
							}
						}
					}
				}
			}
		}

		fy--;// 충돌 나지 않은 지점이였던 한 칸 위로

		int idx;
		for (int row = 0; row < shapeSize; row++) {
			for (int col = 0; col < shapeSize; col++) {
				if (blockShape[row][col]) {
					nx = x + (col - 1);
					ny = fy + (row - 1);

					if (blockPosList.contains(new Pos(nx, ny))) {
						continue;
					}

					idx = (ny * (GameProperties.WIDTH_PLUS_SIDE_BORDERS + 1)) + (nx)
							- (2 * (GameProperties.WIDTH_PLUS_SIDE_BORDERS + 1));
					sb.setCharAt(idx, '*');
				}
			}
		}
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
