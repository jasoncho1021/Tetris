package tetris.gameserver.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServer {
	public static final String START = "START";
	public static final String GAMEOVER = "GAMEOVER";
	public static final String READY = "r";
	public static final String QUIT = "Q";
	public static final Character GAMEQUIT = 'z';

	private HashMap<String, DataOutputStream> clients;

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
	}

	public void start() {
		ServerSocket serverSocket = null;
		Socket socket = null;
		try {
			serverSocket = new ServerSocket(7777);
			System.out.println("서버가 시작되었습니다.");
			while (true) {
				socket = serverSocket.accept();
				System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "]" + "에서 접속하였습니다.");
				ServerReceiver thread = new ServerReceiver(socket);
				thread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // start()

	void sendToAll(String msg) {
		Iterator<String> it = clients.keySet().iterator();
		while (it.hasNext()) {
			try {
				DataOutputStream out = (DataOutputStream) clients.get(it.next());
				out.writeUTF(msg);
			} catch (IOException e) {
			}
		} // while
	} // sendToAll

	public static void main(String args[]) {
		new GameServer().start();
	}

	class ServerReceiver extends Thread {
		Socket socket;
		DataInputStream in;
		DataOutputStream out;
		private ServerTetrisRender tetris;

		ServerReceiver(Socket socket) {
			this.socket = socket;
			try {
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
				tetris = new ServerTetrisRenderImpl(out);
			} catch (IOException e) {
			}
		}

		public void run() {
			String name = "";
			try {
				name = in.readUTF();
				sendToAll("#" + name + "님이 들어오셨습니다.");
				clients.put(name, out);
				System.out.println("현재 서버접속자 수는 " + clients.size() + "입니다.");

				String nameMsg = "";
				String msg = "";
				Thread tetrisThread = null;
				while (in != null) {
					nameMsg = in.readUTF();
					System.out.println("full:" + nameMsg);

					msg = nameMsg.substring(nameMsg.indexOf("]") + 1);
					System.out.println(msg);

					if (tetris.isRunning()) {
						Character input = msg.charAt(0);
						tetris.addInput(input);
					} else {
						if (msg.equalsIgnoreCase(READY)) {
							out.writeUTF(START);

							tetrisThread = new Thread(tetris);
							tetris.startRunning();
							tetrisThread.start();
							continue;
						}

						if (tetrisThread != null && tetrisThread.isAlive()) {
							continue;
						}
						sendToAll("ss:" + nameMsg);
					}
				}
			} catch (IOException e) {
				// ignore
			} finally {
				sendToAll("#" + name + "님이 나가셨습니다.");
				clients.remove(name);
				System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "]" + "에서 접속을 종료하였습니다.");
				System.out.println("현재 서버접속자 수는 " + clients.size() + "입니다.");
			} // try
		} // run
	} // ReceiverThread
}
