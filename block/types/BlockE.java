package tetris.block.types;

import tetris.block.Block;
import tetris.block.TetrisBlock;

/*
 * 1. 회전 경로에 있는 장애물은 고려 안 함.
 * 2. 회전 결과 모양이 겹치는 지만 판단함.
 * 
 * 차라리, 회전 경로 템플릿 찍어내서 비교하는게 더 나을 듯. 밀어내기 안 해도 되니까
 * 
 * isPossibleToRotate 에서 
 * 1) 결과 위치 장애물
 * 2) 경로 장애물
 * 
 */

/**
 *  XXXX
 *  #@##
 *  XXXX
 *  XXXX
 */

/** AntiClockWise Rotation Obstacles Pos

	* 0 0 0
	# C # #
	0 0 * *
	* 0 * *

	0 # * *
	0 C * *
	0 # 0 0
	* # 0 *

	* * 0 *
	* C 0 0
	# # # #
	0 0 0 *

	* 0 # *
	0 C # 0
	* * # 0
	* * # 0

 * @author glenn
 *
 */
@TetrisBlock
public class BlockE extends Block {

	public BlockE() {
		// shapeSize, start height
		super(4, 1);
	}

	@Override
	protected void initShape() {
		blockShape[1][0] = true;
		blockShape[1][1] = true;
		blockShape[1][2] = true;
		blockShape[1][3] = true;
	}

	// x, y
	private static int mask[][] = { { 1, 0 }, { -1, 0 }, { 0, -1 }, { 2, 0 }, { -2, 0 }, { 0, -2 } };

	@Override
	protected int[][] getMask() {
		return mask;
	}

}
