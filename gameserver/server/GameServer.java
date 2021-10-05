package tetris.gameserver.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tetris.queue.TetrisQueue;
import tetris.queue.producer.TetrisThread;

public class GameServer {
	public static final String START = "START";
	public static final String GAMEOVER = "GAMEOVER";
	public static final String READY = "r";
	public static final String QUIT = "Q";
	public static final Character GAMEQUIT = 'z';

	private HashMap<String, DataOutputStream> clients;

	private TetrisQueue<AttackerId> attackRequestQueue = AttackRequestQueue.getInstace();
	private AtomicInteger uniqueId = new AtomicInteger(0);
	private HashMap<Integer, ServerTetrisRender> games;

	private Set<Integer> readySet = new HashSet<Integer>();

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	{
		// Get the process id
		String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", "");
		// MDC
		MDC.put("PID", pid);
	}

	GameServer() {
		clients = new HashMap<>();
		Collections.synchronizedMap(clients);

		games = new HashMap<>();
		Collections.synchronizedMap(games); // ServerReceiver thread 에서 접근하기 때문에 
	}

	public void accept() {
		ServerSocket serverSocket = null;
		Socket socket = null;

		AttackListener attackListener = null;
		try {
			serverSocket = new ServerSocket(7777);
			System.out.println("서버가 시작되었습니다.");
			while (true) {
				socket = serverSocket.accept();
				System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "]" + "에서 접속하였습니다.");
				ServerReceiver thread = new ServerReceiver(socket);
				thread.start();

				if (attackListener == null) {
					attackListener = new AttackListener();
					Thread attackListenerThread = new Thread(attackListener);
					attackListenerThread.start();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void sendToAll(String msg) {
		Iterator<String> it = clients.keySet().iterator();
		while (it.hasNext()) {
			try {
				DataOutputStream out = (DataOutputStream) clients.get(it.next());
				out.writeUTF(msg);
			} catch (IOException e) {
			}
		}
	}

	void startGame() {
		ServerTetrisRender tetris;
		Iterator<Integer> it = games.keySet().iterator();
		while (it.hasNext()) {
			Integer userId = it.next();
			tetris = games.get(userId);
			tetris.startRunning();
			Thread tetrisThread = new Thread(tetris);
			tetrisThread.start();
		}
	}

	public static void main(String args[]) {
		new GameServer().accept();
	}

	private class AttackListener extends TetrisThread {
		private AttackerId output = new AttackerId();
		private Iterator<Integer> it;
		private ServerTetrisRender tetris;

		@Override
		public void run() {
			startRunning();
			while (isRunning()) {
				attackRequestQueue.get(output);

				it = games.keySet().iterator();
				while (it.hasNext()) {
					Integer userId = it.next();
					if (userId.equals(output.getItem())) {
						continue;
					}

					tetris = games.get(userId);
					tetris.addLineJob();
				}

			}
		}
	}

	class ServerReceiver extends Thread {
		private Socket socket;
		private DataInputStream in;
		private DataOutputStream out;
		private ServerTetrisRender tetris;
		private Integer userId;

		ServerReceiver(Socket socket) {
			this.socket = socket;
			try {
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());

				userId = uniqueId.getAndIncrement();
				tetris = new ServerTetrisRenderImpl(out, userId, attackRequestQueue);

				games.put(userId, tetris);

				clients.put(userId + "", out);

			} catch (IOException e) {
			}
		}

		private boolean processChattingMessage(String nameMsg, String msg) {
			if (msg.equals(QUIT)) {
				return false;
			}

			if (msg.equalsIgnoreCase(READY)) {
				readySet.add(userId);

				if (readySet.size() == games.size()) {
					sendToAll(START);
					startGame();
					readySet.clear();
					return true;
				}
			}

			// discard the keep pressed input after game over
			if (!tetris.isGameOver()) {
				return true;
			}

			sendToAll("ss:" + nameMsg);
			return true;
		}

		public void run() {
			String name = "";
			try {
				name = in.readUTF();
				sendToAll("#" + name + "님이 들어오셨습니다.");

				clients.remove(userId + "");

				clients.put(name, out);
				System.out.println("현재 서버접속자 수는 " + clients.size() + "입니다.");

				String nameMsg = "";
				String msg = "";
				while (in != null) {
					nameMsg = in.readUTF();
					msg = nameMsg.substring(nameMsg.indexOf("]") + 1);

					if (tetris.isRunning()) {
						Character input = msg.charAt(0);
						tetris.addInput(input);
						continue;
					}

					if (!processChattingMessage(nameMsg, msg)) {
						break;
					}
				}
			} catch (IOException e) {

			} finally {
				sendToAll("#" + name + "님이 나가셨습니다.");
				try {
					out.writeUTF(QUIT);
				} catch (IOException e) {
					e.printStackTrace();
				}
				clients.remove(name);
				games.remove(userId);
				System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "]" + "에서 접속을 종료하였습니다.");
				System.out.println("현재 서버접속자 수는 " + clients.size() + "입니다.");
			}
		}
	}
}
