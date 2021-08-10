package tetris.block.types;

import tetris.block.Block;
import tetris.block.TetrisBlock;

/*
 * 1. 회전 경로에 있는 장애물은 고려 안 함.
 * 2. 회전 결과 모양이 겹치는 지만 판단함.
 * 
 * 차라리, 회전 경로 템플릿 찍어내서 비교하는게 더 나을 듯.
 * BLockA는 그렇게 해보자.
 * 
 * isPossibleToRotate 에서 
 * 1) 결과 위치 장애물
 * 2) 경로 장애물
 * 
 * moveCenter 는?
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
		super(4);
	}

	@Override
	protected void initShape() {
		blockShape[1][0] = true;
		blockShape[1][1] = true;
		blockShape[1][2] = true;
		blockShape[1][3] = true;
	}

	/*
		moveCenterFromWall() {
			copy-
			if(needCenterMove) {
			move
			}
			
			if(possibleMove()) {
			return
			}
			
			restore-
		}
	 */

	/**
	 * a)돌리고
	 * b) 그 row에서 col만 떙기자.
	 * 
	 * 1) C 기준. 좌우 // 상하
	 * 1-1) 처음부터 양쪽 다 막히면 이동금지.
	 * 1-2) 나오다가 양쪽 다 막히면 이동금지.
	 * 1-3) 성공
	 * 
	 * 2) 우,하 다 걸리면 어쩌지?
	 * 따로 따로 로직 짠다. 그러면 자연스럽게 대각선 이동된다.
	 * 
	 */

	// x, y
	private static int mask[][] = { { 1, 0 }, { -1, 0 }, { 0, -1 }, { 2, 0 }, { -2, 0 }, { 0, -2 } };

	@Override
	protected int[][] getMask() {
		return mask;
	}

	/*protected boolean isPossibleToRotateOld(int dir, final boolean[][] map) {
		if (dir == 0) {
			return moveHorizon(y, map);
		} else if (dir == 2) { // --- , 좌우 벽,블럭 겹침 파악.
			// 상하는 겹칠일 없다. | -> -- 높이가 위로 당겨지기 때문에
			return moveHorizon(y + 1, map);
		} else if (dir == 1) {
			return moveVertical(x + 1, map);
		} else if (dir == 3) {
			return moveVertical(x, map);
		}

		// 에러
		return false;
	}

	private boolean moveHorizon(int ny, final boolean[][] map) {
		boolean lf = false;
		int lx = x;
		for (int col = 1; col >= 0; col--) {
			lx = x + (col - 1);
			if (lx <= 0 || map[ny][lx]) { // 벽 || 블럭
				lf = true;
				break;
			}
		}

		boolean rf = false;
		int rx = x + 1;
		for (int col = 2; col <= 3; col++) {
			rx = x + (col - 1);
			if (rx >= (GameProperties.WIDTH - 1) || map[ny][rx]) {
				rf = true;
				break;
			}
		}

		if (lf && rf) { // 회전 된 결과 모양이 양쪽 동시에 겹침. 회전 못 함.
			return false;
		}

		int gap;
		if (lf) {
			*//**
			 *    C
			 *  # # # #
			 *    | ㅣ
			 *   L1 L2 
			 *//*
			gap = x - lx;
			if (gap == 0) { // L2
				rx = x + 2 + 2;
				if ((rx - 1) > 0 && !map[ny][rx - 1] && !map[ny][rx]) {
					x = x + 2;
					return true;
				} else {
					return false;
				}
			} else if (gap == 1) { // L1
				rx = x + 1 + 2;
				if (rx > 0 && !map[ny][rx]) {
					x = x + 1;
					return true;
				} else {
					return false;
				}
			}
		}

		if (rf) { // 오른쪽에서 겹침.
			gap = rx - x;
			if (gap == 1) {
				lx = x - 1 - 2;
				if ((lx > 0) && !map[ny][lx + 1] && !map[ny][lx]) {
					x = x - 2;
					return true;
				} else {
					return false;
				}
			} else if (gap == 2) {
				lx = x - 1 - 1;
				if ((lx > 0) && !map[ny][lx]) {
					x = x - 1;
					return true;
				} else {
					return false;
				}
			}
		}

		return true;
	}

	private boolean moveVertical(int nx, final boolean[][] map) {
		int dy = y + 2;
		for (int row = 2; row < 4; row++) {
			dy = y + (row - 1);
			if (dy < GameProperties.HEIGHT_PLUS_HIDDEN_START || map[dy][x]) {
				break;
			}
		}

		int gap = dy - y;
		int hy;
		if (gap == 1) {
			hy = y - 1 - 2;
			if ((hy >= 0) && !map[hy][x] && !map[hy + 1][x]) {
				y = y - 2;
				return true;
			} else {
				return false;
			}
		} else if (gap == 2) {
			hy = y - 1 - 1;
			if ((hy >= 0) && !map[hy][x]) {
				y = y - 1;
				return true;
			} else {
				return false;
			}
		}

		return true;
	}*/

}
