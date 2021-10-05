package tetris.gameserver.server.websocket;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tetris.gameserver.server.AttackRequestQueue;
import tetris.gameserver.server.AttackerId;
import tetris.gameserver.server.ServerTetrisRender;
import tetris.queue.TetrisQueue;
import tetris.queue.producer.TetrisThread;

public class ControlTower {
	public static final String START = "START";
	public static final String GAMEOVER = "GAMEOVER";
	public static final String READY = "r";
	public static final String QUIT = "Q";
	public static final Character GAMEQUIT = 'z';

	public static TetrisQueue<AttackerId> attackRequestQueue;
	public static HashMap<String, MessageConverter> clients;
	public static HashMap<Integer, ServerTetrisRender> games;
	public static Set<Integer> readySet;
	public static AtomicInteger uniqueId;

	private static Logger logger = LoggerFactory.getLogger(ControlTower.class);

	static {
		// Get the process id
		String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", "");
		// MDC
		MDC.put("PID", pid);

		clients = new HashMap<>();
		Collections.synchronizedMap(clients);

		games = new HashMap<>();
		Collections.synchronizedMap(games); // ServerReceiver thread 에서 접근하기 때문에 

		readySet = new HashSet<>();
		Collections.synchronizedSet(readySet);

		uniqueId = new AtomicInteger(0);

		attackRequestQueue = AttackRequestQueue.getInstace();
	}

	private void start() {
		AttackListener attackListener = new AttackListener();
		Thread attackListenerThread = new Thread(attackListener);
		attackListenerThread.start();

		TcpSocketServer tcpSocketServer = new TcpSocketServer();
		WebSocketServer webSocketServer = new WebSocketServer();

		Thread tcpThread = new Thread(tcpSocketServer);
		Thread wsThread = new Thread(webSocketServer);

		tcpThread.start();
		wsThread.start();
	}

	public static void main(String args[]) {
		new ControlTower().start();
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

}
