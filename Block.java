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
	int x;
	int y;

	private boolean stopFlag;
	boolean[][] blockShape;

	Block(int y) {
		this.x = (width / 2);
		this.y = y;
		this.stopFlag = false;

		initShape();
	}

	void stopDrop() {
		stopFlag = true;
		--y; // recovery once here
	}

	void startDrop() {
		stopFlag = false;
	}

	boolean isStop() {
		return stopFlag;
	}

	void dropY() {
		if (!stopFlag && (y < (height - 1))) {
			++y;
		}
	}

	void recoverY() {
		--y;
	}

	void moveLeft(final boolean[][] map) {
		if (!isWall(x - 1, map)) {
			--x;
		}
	}

	void moveRight(final boolean[][] map) {
		if (!isWall(x + 1, map)) {
			++x;
		}
	}

	void doKeyEvent(Character input, final boolean[][] map) {
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

	abstract void remove(boolean[][] map);

	abstract void rotateClockWise();

	abstract void rotateAntiClockWise();

	abstract void setBlockToMap(boolean[][] map);

	abstract boolean isPossibleToPut(final boolean[][] map);

	abstract boolean isWall(int nx, final boolean[][] map);

	abstract boolean isCeil();

	abstract void initShape();
}
