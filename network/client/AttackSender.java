package tetris.network.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class AttackSender implements Runnable {
	private SocketChannel socket;

	AttackSender(SocketChannel socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		ByteBuffer buf = ByteBuffer.allocate(1024);

		buf.put(Client.ATTACK.getBytes());
		buf.flip();
		try {
			socket.write(buf);
		} catch (IOException e) {

		}
		buf.clear();

	}
}