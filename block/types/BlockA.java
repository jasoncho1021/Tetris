package tetris.block.types;

import tetris.block.Block;
import tetris.block.TetrisBlock;

/**
 * x,y means the center point of block rotation == @ 
 * 
 *  X X X
 *  # @ #
 *  X X #
 */
@TetrisBlock
public class BlockA extends Block {

	public BlockA() {
		super(3); // shapeSize
	}

	@Override
	protected void initShape() {
		// [y][x]
		blockShape[1][0] = true;
		blockShape[1][1] = true;
		blockShape[1][2] = true;
		blockShape[2][2] = true;
	}

}
