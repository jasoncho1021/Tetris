package tetris.block.types;

import tetris.block.Block;
import tetris.block.TetrisBlock;

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
	protected void initShape() {
		blockShape[1][1] = true;
		blockShape[1][2] = true;
		blockShape[2][1] = true;
		blockShape[2][2] = true;
	}

}
