package tetris;

public class GameException extends RuntimeException {

	private Exception e;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GameException(Exception e) {
		this.e = e;
	}

	public void printGameExceptionStack() {
		//e.printStackTrace();
		System.out.println("GameException occured:" + e.getMessage());
	}

}
