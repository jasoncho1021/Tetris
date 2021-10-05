package tetris;

public class GameException extends RuntimeException {

	private Exception e;
	private String msg;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GameException(Exception e) {
		this.e = e;
	}

	public GameException(String msg) {
		this.msg = msg;
	}

	@Override
	public void printStackTrace() {
		String emsg = "GameException occured: ";
		if (e != null) {
			emsg += e.getMessage();
		} else {
			emsg += msg;
		}
		System.out.println(emsg);
	}

}
