package tetris.gameserver.server.websocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import tetris.GameException;

public class TcpSocketMessageConverter implements MessageConverter {
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	public TcpSocketMessageConverter(Socket socket) {
		this.socket = socket;
		try {
			this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			throw new GameException(e);
		}
	}

	@Override
	public String read() {
		try {
			return in.readUTF();
		} catch (IOException e) {
			throw new GameException(e);
		}
	}

	@Override
	public void write(String msg) {
		try {
			out.writeUTF(msg);
		} catch (IOException e) {
			throw new GameException(e);
		}
	}

	@Override
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			throw new GameException(e);
		}
	}
}
