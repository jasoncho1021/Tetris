package tetris;

/**
 * "GAME BOARD"
 * 
 * X(hidden start height) = 2
 * H = 3
 * W = 5
 * B = 1 // Bottom Border
 * S = 2 // Side Bordersf
 * 
 *0		S  X  S
 *1		S  X  S
 *2		S  H  S
 *3		SWWWWWS
 *4		S  H  S
 *5		S  B  S
 * 
 * 
 * @author glenn
 *
 */
public class GameProperties {
	private static final int BOTTOM_BORDER = 1;
	private static final int SIDE_BORDERS = 2;
	private static final int HEIGHT = 20;

	static final int WIDTH = 12;

	public static final int HIDDEN_START_HEIGHT = 2;
	public static final int HEIGHT_PLUS_BOTTOM_BORDER = HEIGHT + BOTTOM_BORDER;
	public static final int HEIGHT_PLUS_HIDDEN_START = HEIGHT + HIDDEN_START_HEIGHT;
	public static final int HEIGHT_PLUS_HIDDEN_START_PLUS_BOTTOM_BORDER = HEIGHT_PLUS_HIDDEN_START + BOTTOM_BORDER;
	public static final int WIDTH_PLUS_SIDE_BORDERS = WIDTH + SIDE_BORDERS;

	public static boolean isIn(int x, int y) {
		if (0 <= y && y < GameProperties.HEIGHT_PLUS_HIDDEN_START && 0 < x
				&& x < (GameProperties.WIDTH_PLUS_SIDE_BORDERS - 1)) {
			return true;
		}
		return false;
	}
}

enum BlockState {
	FALLING, TOUCH_DOWN, TOUCH_CEIL
}
