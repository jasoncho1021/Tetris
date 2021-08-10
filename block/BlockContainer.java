package tetris.block;

import java.util.List;
import java.util.Random;

public class BlockContainer {

	private Random random;
	private List<Class<?>> clazzList;

	private BlockContainer() {
		random = new Random();
		scan(getClass().getPackage().getName());
	}

	public static BlockContainer getInstance() {
		return LazyHolder.INSTANCE;
	}

	private static class LazyHolder {
		private static final BlockContainer INSTANCE = new BlockContainer();
	}

	private void scan(String packageName) {
		clazzList = DiScanner.scanPackageAndGetClass(packageName, TetrisBlock.class);
	}

	public Block getNewBlock() {
		int idx = random.nextInt(clazzList.size());
		Class<?> c = clazzList.get(idx);
		try {
			return (Block) c.newInstance();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return null;
	}
}
