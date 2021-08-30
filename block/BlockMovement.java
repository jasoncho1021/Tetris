package tetris.block;

import tetris.JoyPad;

public interface BlockMovement {
	public void setBlockToMap(boolean map[][]);

	public void doKeyEvent(JoyPad joyPad, final boolean[][] map);

	public boolean isCeil();

	public void recoverY();

	public void remove(boolean[][] map);

	public boolean isPossibleToPut(final boolean map[][]);

	public void setFutureBlockToStringBuilder(final boolean map[][], StringBuilder sb);
}
