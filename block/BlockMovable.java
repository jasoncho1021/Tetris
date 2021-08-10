package tetris.block;

public interface BlockMovable {
	public void setBlockToMap(boolean map[][]);

	public void doKeyEvent(Character input, final boolean[][] map);

	public boolean isCeil();

	public boolean isPossibleToPut(final boolean map[][]);

	public void recoverY();

	public void remove(boolean[][] map);
}
