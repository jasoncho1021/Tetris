package tetris.network.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;

public class SystemIn implements Runnable {
	private SocketChannel socket;

	SystemIn(SocketChannel socket) {
		this.socket = socket;
	}

	private volatile boolean flag = true;

	public void startSend() {
		flag = true;
	}

	public void stopSend() {
		flag = false;
	}

	private boolean isRunning() {
		return flag;
	}

	private void chattingSending() {
		ReadableByteChannel in = Channels.newChannel(System.in);
		ByteBuffer buf = ByteBuffer.allocate(1024);

		startSend();
		while (isRunning()) {
			try {
				if (System.in.available() != 0) {
					in.read(buf);
					buf.flip();
					socket.write(buf);
					buf.clear();
				}
			} catch (IOException e) {
				System.out.println("IOException: chatting is impossible");
				e.printStackTrace();
			}
		}
		System.out.println("chatting send stoped!");
	}

	private volatile boolean attack;

	public void setAttackSending() {
		attack = true;
	}

	public void usedAttackSending() {
		attack = false;
	}

	private boolean isAttackable() {
		return attack;
	}

	public void attackSending() {
		ByteBuffer buf = ByteBuffer.allocate(1024);

		startSend();

		while (isRunning()) {
			//System.out.println("yes1");
			//System.out.flush();

			if (isAttackable()) {
				buf.put(Client.ATTACK.getBytes());
				buf.flip();
				try {
					socket.write(buf);
				} catch (IOException e) {
					e.printStackTrace();
				}
				buf.clear();
				usedAttackSending();
			}
		}
		System.out.println("attack send stoped");
	}

	@Override
	public void run() {
		chattingSending();
		attackSending();
	}
}