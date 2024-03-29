package tetris.multiplay.v3.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import tetris.core.queue.producer.TetrisThread;

public class ConsoleListener extends TetrisThread{

	private String ttyConfig;

	private DataOutputStream out;

	public ConsoleListener(DataOutputStream out) {
		this.out = out;
	}

	private void listenKey() {
		try {
			setTerminalToCBreak();

			while (isRunning()) { // polling with non-blocking syscall
				if (System.in.available() != 0) { // avoid calling blocking syscall by using non-blocking syscall as a condition
					out.writeUTF(((char) System.in.read()) + "");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("IOException");
		} catch (InterruptedException e) {
			System.err.println("console interrupted");
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
		startRunning();
		listenKey();
	}
}
