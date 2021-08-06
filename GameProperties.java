package tetris;

/**
 * X(hidden) = 2
 * H = 3
 * W = 5
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
	final static private int BOTTOM_BORDER = 1;
	final static private int SIDE_BORDERS = 2;
	final static int HIDDEN_START_HEIGHT = 2;
	final static private int HEIGHT = 30;
	final static int HEIGHT_PLUS_BOTTOM_BORDER = HEIGHT + BOTTOM_BORDER;
	final static int HEIGHT_PLUS_HIDDEN_START = HEIGHT + HIDDEN_START_HEIGHT;

	final static int HEIGHT_PLUS_HIDDEN_START_PLUS_BOTTOM_BORDER = HEIGHT_PLUS_HIDDEN_START + BOTTOM_BORDER;

	final static int WIDTH = 12;
	final static int WIDTH_PLUS_SIDE_BORDERS = WIDTH + SIDE_BORDERS;

	static boolean isIn(int x, int y) {
		if(0 <= y && y < GameProperties.HEIGHT_PLUS_HIDDEN_START && 0 < x && x < (GameProperties.WIDTH_PLUS_SIDE_BORDERS - 1)) {
			return true;
		}
		return false;
	}
}
