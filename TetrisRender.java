package tetris;

public interface TetrisRender {
	public void renderErased();
	public StringBuilder setGameBoard();
	public void renderGameBoard(String gameBoard);
	public void removePerfectLine();
	public void addJob(JobCallBack jobCallBack);
}
