package tetris;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class NumbersConsole implements Runnable {

	private String ttyConfig;

	private String input = "iinit";

	private boolean isCanceled = false;

	private Tetris tetris;

	NumbersConsole(Tetris tetris) {
		this.tetris = tetris;
	}

	public String getInput() {
		String temp = input;
		input = "";
		return temp;
	}

	public void cancel() {
		isCanceled = true;
	}

	public void listenKey() {
		try {
			setTerminalToCBreak();

			while (!isCanceled) {

				if (System.in.available() != 0) {
					int c = System.in.read();
					input = String.valueOf((char) c);
					//System.out.println(input);
					tetris.printmap("from:"+getInput());
/*					if (c == 0x1B) {
						break;
					}*/
				}

			} // end while
		} catch (IOException e) {
			System.err.println("IOException");
		} catch (InterruptedException e) {
			System.err.println("InterruptedException");
		} finally {
			try {
				stty(ttyConfig.trim());
			} catch (Exception e) {
				System.err.println("Exception restoring tty config");
			}
		}
	}

	private void setTerminalToCBreak() throws IOException, InterruptedException {

		ttyConfig = stty("-g");

		// set the console to be character-buffered instead of line-buffered
		stty("-icanon min 1");

		// disable character echoing
		stty("-echo");
	}

	/**
	 *  Execute the stty command with the specified arguments
	 *  against the current active terminal.
	 */
	private String stty(final String args) throws IOException, InterruptedException {
		String cmd = "stty " + args + " < /dev/tty";

		return exec(new String[] { "sh", "-c", cmd });
	}

	/**
	 *  Execute the specified command and return the output
	 *  (both stdout and stderr).
	 */
	private String exec(final String[] cmd) throws IOException, InterruptedException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		Process p = Runtime.getRuntime().exec(cmd);
		int c;
		InputStream in = p.getInputStream();

		while ((c = in.read()) != -1) {
			bout.write(c);
		}

		in = p.getErrorStream();

		while ((c = in.read()) != -1) {
			bout.write(c);
		}

		p.waitFor();

		String result = new String(bout.toByteArray());
		return result;
	}

	@Override
	public void run() {
		listenKey();
	}

}