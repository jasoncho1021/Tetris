package tetris;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Random;

@Retention(RetentionPolicy.RUNTIME)
@interface TetrisBlock {
}

public abstract class Block {

	static int height;
	static int width;
	static Random random;
	static List<Class<?>> clazzList;

	static {
		random = new Random();
	}

	static void scan(String packageName) {
		clazzList = DiContainer.scanPackageAndGetClass(packageName, TetrisBlock.class);
		/*for (Class c : clazzList) {
			System.out.println(c.getName());
		}*/
	}

	static Block getNewBlock() {
		int idx = random.nextInt(clazzList.size());
		Class c = clazzList.get(idx);
		try {
			return (Block) c.newInstance();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// center position
	protected int x;
	protected int y;
	protected boolean[][] blockShape;
	protected int shapeSize;

	Block(int shapeSize) {
		this.x = (width / 2);
		this.y = 0;
		this.shapeSize = shapeSize;
		this.blockShape = new boolean[shapeSize][shapeSize];

		initShape();
	}

	abstract void initShape();

	private void dropY() {
		if (y < (height - 1)) {
			++y;
		}
	}

	void recoverY() {
		--y;
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

	protected void doKeyEvent(Character input, final boolean[][] map) {
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
		case 'd':
			rotateAntiClockWise();
			break;
		case 'f':
			rotateClockWise();
			break;
		default:
			break;
		}
	}

	public String toString() {
		return x + " " + y;
	}

	/*
	 * # #
	 * # X
	 * # X
	 */
	void rotateClockWise() {
		if (isTop()) {
			//return;
		}

		boolean tmp;

		// center touch wall
		moveCenterFromWall();

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

	void rotateAntiClockWise() {
		if (isTop()) {
			//return;
		}

		boolean tmp;

		moveCenterFromWall();

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

	// long block 일땐 if(y <= 1)
	private boolean isTop() {
		if (y == 0) {
			return true;
		}
		return false;
	}

	void setBlockToMap(boolean map[][]) {
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

	boolean isPossibleToPut(final boolean map[][]) {
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

	private void moveCenterFromWall() {
		if (x == 1) {
			//++x;
			x = x + (shapeSize - 2);
		} else if (x == width - 2) { // 수정 오른쪽 옆 통과 수정. -1 ==> -2 (양쪽 테두리 길이 제거)
			//--x;
			x = x - (shapeSize - 2);
		}
	}

	boolean isWall(int dx, final boolean[][] map) {
		int nx = 0;
		int ny = 0;
		for (int row = 0; row < shapeSize; row++) {
			for (int col = 0; col < shapeSize; col++) {
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

	void remove(boolean[][] map) {
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
					nx = x + (col - 1);
					ny = y + (row - 1);

					map[ny][nx] = false;
				}
			}
		}
	}

	/**
	 * 
	 * 44
	 * 
	 * 00 01 02 03
	 * 10 11 12 13
	 * 20 21 22 23
	 * 30 31 32 33
	 * 
	 * -1-1 -10 -11 -12
	 *       @   
	 */

	boolean isCeil() {
		int topY = shapeSize;
		topLoop: for (int row = 0; row < shapeSize; row++) {
			for (int col = 0; col < shapeSize; col++) {
				if (blockShape[row][col]) {
					topY = y + (row - 1);
					break topLoop;
				}
			}
		}

		if (topY < 2) { // hiddenHeight
			return true;
		}

		return false;
	}
}
