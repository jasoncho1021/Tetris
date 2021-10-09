package tetris.gameserver.server.websocket;

import java.net.ServerSocket;
import java.net.Socket;

public class TcpSocketServer implements Runnable {

	public void accept() {
		ServerSocket serverSocket = null;
		Socket socket = null;

		try {
			serverSocket = new ServerSocket(7777);
			System.out.println("TCP 서버가 시작되었습니다.");
			while (true) {
				socket = serverSocket.accept();
				TcpSocketMessageConverter tcpSocketMessageConverter = new TcpSocketMessageConverter(socket);
				ServerReceiver thread = new ServerReceiver(tcpSocketMessageConverter);
				thread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		new TcpSocketServer().accept();
	}

	@Override
	public void run() {
		accept();
	}

}
