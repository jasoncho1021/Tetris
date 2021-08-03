package tetris;

/**
 * 
 * just 3X3 roatation.
 * except long block
 * 
 * @author glenn
 *
 */
@TetrisBlock
public class BlockA extends Block {

	//private boolean[][] blockShape;// = new boolean[3][3];

	/**
	 * x,y means the center point of block rotation == @ 
	 * 
	 *  X X X
	 *  # @ #
	 *  X X #
	 * 
	 * @param x
	 * @param y
	 */

	public BlockA() {
		super(3); // shapeSize
	}

	/*
	 *  X X X
	 *  # # #
	 *  X X #
	 */
	@Override
	void initShape() {
		// [y][x]
		blockShape[1][0] = true;
		blockShape[1][1] = true;
		blockShape[1][2] = true;
		blockShape[2][2] = true;
	}

	public static void main(String[] args) {
		BlockA block = new BlockA();
		block.testRotation();
	}

	private void testRotation() {

		StringBuilder sb;
		for (int k = 0; k < 4; k++) {
			sb = new StringBuilder();

			for (int row = 0; row < shapeSize; row++) {
				for (int col = 0; col < shapeSize; col++) {
					if (blockShape[row][col]) {
						sb.append("#");
					} else {
						sb.append("*");
					}
				}
				sb.append("\n");
			}

			System.out.println(sb.toString());
			rotateClockWise();
		}

	}

}
