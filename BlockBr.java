package tetris;

/**
 *  XXX
 *  X@#
 *  ##X
 */
@TetrisBlock
public class BlockBr extends Block {

	public BlockBr() {
		super(3);
	}

	@Override
	void initShape() {
		blockShape[1][1] = true;
		blockShape[1][2] = true;
		blockShape[2][0] = true;
		blockShape[2][1] = true;
	}

}
