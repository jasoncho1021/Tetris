package tetris.core.block.types;

import tetris.core.block.Block;
import tetris.core.block.TetrisBlock;

/**
 * x,y means the center point of block rotation == @ 
 * 
 *  X X X
 *  # @ #
 *  # X X
 */
@TetrisBlock
public class BlockAr extends Block {

	public BlockAr() {
		super(3); // shapeSize
	}

	@Override
	protected void initShape() {
		// [y][x]
		blockShape[1][0] = true;
		blockShape[1][1] = true;
		blockShape[1][2] = true;
		blockShape[2][0] = true;
	}

}