package tetris;

public interface TetrisRender extends Runnable {
	public boolean moveBlockAndRender(JoyPad joyPad);

	public void addJob(JobCallBack jobCallBack);

	public void gameStart();

	public void addLine();
	/*	public void renderErased();
		public StringBuilder setGameBoard();
		public void renderGameBoard(String gameBoard);
		public void removePerfectLine();
	*/
}
