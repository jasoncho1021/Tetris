package tetris;

/**
 *  XXX
 *  X##
 *  ##X
 */
@TetrisBlock
public class BlockC extends Block {

	public BlockC() {
		super(3);
	}

	@Override
	void initShape() {
		// [y][x]
		blockShape[1][1] = true;
		blockShape[1][2] = true;
		blockShape[2][0] = true;
		blockShape[2][1] = true;
	}

}
