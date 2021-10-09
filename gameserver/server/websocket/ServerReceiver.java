package tetris.gameserver.server.websocket;

import java.util.Iterator;

import tetris.gameserver.server.ServerTetrisRender;
import tetris.gameserver.server.ServerTetrisRenderImpl;

public class ServerReceiver extends Thread {
	private MessageConverter messageConverter;
	private ServerTetrisRender tetris;
	private Integer userId;

	ServerReceiver(MessageConverter messageConverter) {
		this.messageConverter = messageConverter;
		init();
	}

	private void init() {
		userId = ControlTower.uniqueId.getAndIncrement();
		tetris = new ServerTetrisRenderImpl(messageConverter, userId, ControlTower.attackRequestQueue);

		ControlTower.games.put(userId, tetris);

		ControlTower.clients.put(userId + "", messageConverter);
	}

	public void run() {
		String name = "";
		try {
			name = messageConverter.read();
			sendToAll("#" + name + "님이 들어오셨습니다.");

			//ControlTower.clients.remove(userId + "");
			//ControlTower.clients.put(name, messageConverter);

			System.out.println("현재 서버접속자 수는 " + ControlTower.clients.size() + "입니다.");

			String nameMsg = "";
			String msg = "";
			while (messageConverter != null) {
				nameMsg = messageConverter.read();
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

			messageConverter.write(ControlTower.QUIT);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			messageConverter.close();

			//ControlTower.clients.remove(name);
			ControlTower.clients.remove(userId + "");
			ControlTower.games.remove(userId);

			sendToAll("#" + name + "님이 나가셨습니다.");
			System.out.println("현재 서버접속자 수는 " + ControlTower.clients.size() + "입니다.");
		}
	}

	private boolean processChattingMessage(String nameMsg, String msg) {
		if (msg.equals(ControlTower.QUIT)) {
			return false;
		}

		if (msg.equalsIgnoreCase(ControlTower.READY)) {
			ControlTower.readySet.add(userId);

			if (ControlTower.readySet.size() == ControlTower.games.size()) {
				sendToAll(ControlTower.START);
				startGame();
				ControlTower.readySet.clear();
				return true;
			}
		}

		// discard the keep pressed input after game over
		if (!tetris.isGameOver()) {
			return true;
		}

		sendToAll(nameMsg);
		return true;
	}

	void sendToAll(String msg) {
		Iterator<String> it = ControlTower.clients.keySet().iterator();
		while (it.hasNext()) {
			MessageConverter messageConverter = (MessageConverter) ControlTower.clients.get(it.next());
			messageConverter.write(msg);
		}
	}

	void startGame() {
		ServerTetrisRender tetris;
		Iterator<Integer> it = ControlTower.games.keySet().iterator();
		while (it.hasNext()) {
			Integer userId = it.next();
			tetris = ControlTower.games.get(userId);
			tetris.startRunning();
			Thread tetrisThread = new Thread(tetris);
			tetrisThread.start();
		}
	}
}
