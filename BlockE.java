package tetris;

/**
 *  XXXX
 *  ####
 *  XXXX
 *  XXXX
 */
@TetrisBlock
public class BlockE extends Block {

	public BlockE() {
		super(4);
	}

	@Override
	void initShape() {
		blockShape[1][0] = true;
		blockShape[1][1] = true;
		blockShape[1][2] = true;
		blockShape[1][3] = true;
	}

}
