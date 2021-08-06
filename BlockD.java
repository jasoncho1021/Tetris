package tetris;

/**
 *  XXXX
 *  X##X
 *  X##X
 *  XXXX
 */
@TetrisBlock
public class BlockD extends Block {

	public BlockD() {
		super(4);
	}

	@Override
	void initShape() {
		blockShape[1][1] = true;
		blockShape[1][2] = true;
		blockShape[2][1] = true;
		blockShape[2][2] = true;
	}

	@Override
	protected boolean isPossibleToRotate(final boolean[][] map) {
		return false;
	}
}
