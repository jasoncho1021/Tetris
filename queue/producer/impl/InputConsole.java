package tetris.queue.producer.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import tetris.queue.KeyInput;
import tetris.queue.TetrisQueue;
import tetris.queue.producer.TetrisProducer;

public class InputConsole extends TetrisProducer {

	private String ttyConfig;

	private TetrisQueue tetrisQueue;

	public InputConsole(TetrisQueue tetrisQueue) {
		this.tetrisQueue = tetrisQueue;
	}

	public void listenKey() {
		try {
			setTerminalToCBreak();

			startProduce();
			while (isRunning()) {
				if (System.in.available() != 0) {
					tetrisQueue.add(new KeyInput((char) System.in.read()));
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