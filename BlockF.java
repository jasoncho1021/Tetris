package tetris;

/**
 * x,y means the center point of block rotation == @ 
 * 
 *  X X X
 *  X @ X
 *  # # #
 */
@TetrisBlock
public class BlockF extends Block {

	public BlockF() {
		super(3); // shapeSize
	}

	@Override
	void initShape() {
		// [y][x]
		blockShape[1][1] = true;
		blockShape[2][0] = true;
		blockShape[2][1] = true;
		blockShape[2][2] = true;
	}

}