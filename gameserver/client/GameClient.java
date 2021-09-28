package tetris.gameserver.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
	private ClientSender clientSender;
	private ClientReceiver clientReceiver;

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	{
		// Get the process id
		String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", "");
		// MDC
		MDC.put("PID", pid);
	}

	public static void main(String args[]) {
		if (args.length != 1) {
			System.out.println("USAGE: java TcpIpMultichatClient 대화명");
			System.exit(0);
		}
		GameClient gameClient = new GameClient();
		gameClient.startGame(args[0]);
	}

	void startGame(String userName) {
		Thread sender = null;
		Thread receiver = null;
		try {
			String serverIp = "127.0.0.1";
			// 소켓을 생성하여 연결을 요청한다.
			Socket socket = new Socket(serverIp, 7777);
			System.out.println("서버에 연결되었습니다.");

			clientSender = new ClientSender(socket.getOutputStream(), userName);
			sender = new Thread(clientSender);

			clientReceiver = new ClientReceiver(socket, clientSender);
			receiver = new Thread(clientReceiver);

			sender.start();
			receiver.start();

		} catch (ConnectException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
		} finally {
			try {
				if (receiver != null) {
					receiver.join();
					logger.debug("receiver join");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				if (sender != null) {
					clientSender.resumeChatting();
					clientSender.stopRunning();
					sender.join();
					logger.debug("sender join");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	static class ClientSender extends TetrisThread {
		private DataOutputStream out;
		private String name;
		private Object blockerObj = new Object();
		private volatile boolean isPaused;

		private Logger logger = LoggerFactory.getLogger(this.getClass());
		{
			// Get the process id
			String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", "");
			// MDC
			MDC.put("PID", pid);
		}

		ClientSender(OutputStream outputStream, String name) {
			try {
				out = new DataOutputStream(outputStream);
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
				while (isRunning() && out != null) {
					checkBlocking();
					if (System.in.available() != 0) {
						out.writeUTF("[" + name + "]" + scanner.nextLine());
					}
				}
			} catch (IOException e) {
			} finally {
				logger.debug("sender end");
				scanner.close();
			}
		}
	}

	static class ClientReceiver implements Runnable {
		private Socket socket;
		private DataInputStream in;
		private ConsoleListener consoleListener;
		private ClientSender clientSender;

		private Logger logger = LoggerFactory.getLogger(this.getClass());
		{
			// Get the process id
			String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", "");
			// MDC
			MDC.put("PID", pid);
		}

		ClientReceiver(Socket socket, ClientSender clientSender) {
			this.socket = socket;
			try {
				in = new DataInputStream(socket.getInputStream());
			} catch (IOException e) {
			}
			this.clientSender = clientSender;
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
			boolean first = true;
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
					} else if (msg.equals(GameServer.QUIT)) {
						System.out.println(msg);
						break;
					} else {
						// print chatting
						// print gameBoard
						if (clientSender.isChattingPasued()) {
							if (!first) {
								renderErased();
							}
							System.out.print(msg);
							first = false;
							continue;
						}
						System.out.println(msg);
					}

				} catch (IOException e) {
				}
			}
			logger.debug("receiver end");
		} // run
	}
}
