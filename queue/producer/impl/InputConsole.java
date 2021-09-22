package tetris.queue.producer.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import tetris.queue.KeyInput;
import tetris.queue.TetrisQueue;
import tetris.queue.producer.TetrisThread;

public class InputConsole extends TetrisThread {

	private String ttyConfig;

	private TetrisQueue<KeyInput> tetrisQueue;

	public InputConsole(TetrisQueue<KeyInput> tetrisQueue) {
		this.tetrisQueue = tetrisQueue;
	}

	private void listenKey() {
		try {
			setTerminalToCBreak();

			while (isRunning()) { // polling with non-blocking syscall
				if (System.in.available() != 0) { // avoid calling blocking syscall by using non-blocking syscall as a condition
					tetrisQueue.add(new KeyInput((char) System.in.read())); // System.in.read() is blocking
				}
			}
		} catch (IOException e) {
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
		System.out.println("console end");
	}

}