package tetris;

public interface TetrisRender {
	public boolean moveBlockAndRender(JoyPad joyPad);

	public void addJob(JobCallBack jobCallBack);

	public void gameStart();
	/*	public void renderErased();
		public StringBuilder setGameBoard();
		public void renderGameBoard(String gameBoard);
		public void removePerfectLine();
	*/
}
