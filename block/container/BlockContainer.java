package tetris.block.container;

import java.util.List;
import java.util.Random;

import tetris.GameException;
import tetris.block.Block;
import tetris.block.TetrisBlock;

public class BlockContainer {

	private Random random;
	private List<Class<?>> clazzList;

	private BlockContainer() {
		random = new Random();
		scan(getClass());
	}

	public static BlockContainer getInstance() {
		return LazyHolder.INSTANCE;
	}

	private static class LazyHolder {
		private static final BlockContainer INSTANCE = new BlockContainer();
	}

	private void scan(Class<?> clazz) {
		clazzList = DiScanner.scanPackageAndGetClass(clazz, TetrisBlock.class);
	}

	public Block getNewBlock() {
		try {
			int idx = random.nextInt(clazzList.size());
			Class<?> c = clazzList.get(idx);
			return (Block) c.newInstance();
		} catch (RuntimeException | InstantiationException | IllegalAccessException e) {
			throw new GameException(e);
		}
	}
}
