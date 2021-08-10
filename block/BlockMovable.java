package tetris.block;

import tetris.JoyPad;

public interface BlockMovable {
	public void setBlockToMap(boolean map[][]);

	public void doKeyEvent(JoyPad joyPad, final boolean[][] map);

	public boolean isCeil();

	public boolean isPossibleToPut(final boolean map[][]);

	public void recoverY();

	public void remove(boolean[][] map);
}
