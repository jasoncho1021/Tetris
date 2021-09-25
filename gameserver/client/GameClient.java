package tetris.gameserver.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tetris.GameException;
import tetris.GameProperties;
import tetris.gameserver.server.GameServer;
import tetris.queue.producer.TetrisThread;

public class GameClient {

	static ClientSender clientSender;
	static ClientReceiver clientReceiver;

	public static void main(String args[]) {
		if (args.length != 1) {
			System.out.println("USAGE: java TcpIpMultichatClient 대화명");
			System.exit(0);
		}
		GameClient gameClient = new GameClient();
		gameClient.startGame(args[0]);

	} // main

	void startGame(String userName) {
		try {
			String serverIp = "127.0.0.1";
			// 소켓을 생성하여 연결을 요청한다.
			Socket socket = new Socket(serverIp, 7777);
			System.out.println("서버에 연결되었습니다.");

			clientSender = new ClientSender(socket, userName);
			Thread sender = new Thread(clientSender);

			clientReceiver = new ClientReceiver(socket);
			Thread receiver = new Thread(clientReceiver);

			sender.start();
			receiver.start();
		} catch (ConnectException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
		}
	}

	static class ClientSender extends TetrisThread {
		private Socket socket;
		private DataOutputStream out;
		private String name;
		private Object blockerObj = new Object();
		private volatile boolean isPaused;

		ClientSender(Socket socket, String name) {
			this.socket = socket;
			try {
				out = new DataOutputStream(socket.getOutputStream());
				this.name = name;
			} catch (Exception e) {
			}
		}

		public boolean isChattingPasued() {
			return isPaused;
		}

		public void resumeChatting() {
			synchronized (blockerObj) {
				isPaused = false;
				blockerObj.notifyAll();
			}
		}

		public void pauseChatting() {
			synchronized (blockerObj) {
				isPaused = true;
			}
		}

		private void checkBlocking() {
			synchronized (blockerObj) {
				while (isPaused) {
					try {
						blockerObj.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		public void run() {
			startRunning();
			isPaused = false;

			Scanner scanner = new Scanner(System.in);
			try {
				if (out != null) {
					out.writeUTF(name);
				}
				while (isRunning()) {
					checkBlocking();
					if (System.in.available() != 0) {
						out.writeUTF("[" + name + "]" + scanner.nextLine());
					}
				}
			} catch (IOException e) {
			} finally {
				//	scanner.close();
			}
		}
	}

	static class ClientReceiver implements Runnable {
		private Socket socket;
		private DataInputStream in;
		private ConsoleListener consoleListener;

		private Logger logger = LoggerFactory.getLogger(this.getClass());

		{
			// Get the process id
			String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", "");
			// MDC
			MDC.put("PID", pid);
		}

		ClientReceiver(Socket socket) {
			this.socket = socket;
			try {
				in = new DataInputStream(socket.getInputStream());
			} catch (IOException e) {
			}
		}

		public void renderErased() {
			System.out.print(erase(GameProperties.HEIGHT_PLUS_BOTTOM_BORDER_PLUS_INPUT));
		}

		private String erase(int rowsToErase) {
			/*
			 * dir
			 * 1. eclipse : project/
			 * 2. console : project/bin
			 */
			String dir = System.getProperty("user.dir");
			String path = dir + "/multilineEraser.sh";

			String[] cmd = { path, String.valueOf(rowsToErase) };

			String gameBoard = "";
			try {
				Process process = Runtime.getRuntime().exec(cmd);
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				StringBuilder builder = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				gameBoard = builder.toString();
			} catch (IOException e) {
				throw new GameException(e);
			}
			return gameBoard;
		}

		public void run() {
			String msg = "";
			boolean first = false;
			Thread consoleListenerThread = null;
			while (in != null) {
				try {
					msg = in.readUTF();
					if (msg.equalsIgnoreCase(GameServer.START)) {
						clientSender.pauseChatting();

						consoleListener = new ConsoleListener(new DataOutputStream(socket.getOutputStream()));
						consoleListenerThread = new Thread(consoleListener);
						consoleListenerThread.start();

						first = true;
					} else if (msg.equalsIgnoreCase(GameServer.GAMEOVER)) {
						consoleListener.stopRunning();
						clientSender.resumeChatting();
					} else {
						/*
						 * print chatting
						 * print gameBoard
						 */
						if (clientSender.isChattingPasued()) {
							if (first) {
								System.out.print(msg);
								first = false;
								continue;
							}
							renderErased();
							System.out.print(msg);
							continue;
						}
						System.out.println(msg);
					}
				} catch (IOException e) {
				}
			}
		} // run
	}
}
