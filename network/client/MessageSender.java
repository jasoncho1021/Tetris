package tetris.network.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;

public class MessageSender implements Runnable {

	private SocketChannel socket;
	private Object blockerObj = new Object();
	private volatile boolean isPaused;
	private volatile boolean runningFlag;

	MessageSender(SocketChannel socket) {
		this.socket = socket;
	}

	public void startSend() {
		runningFlag = true;
	}

	public void stopSend() {
		runningFlag = false;
	}

	private boolean isRunning() {
		return runningFlag;
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

	private void sendChatting() {
		startSend();
		isPaused = false;

		ReadableByteChannel in = Channels.newChannel(System.in);
		ByteBuffer buf = ByteBuffer.allocate(1024);
		while (isRunning()) {
			checkBlocking();
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
				break;
			}
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

	public void sendAttackMessage() {
		ByteBuffer buf = ByteBuffer.allocate(1024);

		buf.put(Client.ATTACK.getBytes());
		buf.flip();
		try {
			socket.write(buf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		buf.clear();
	}

	@Override
	public void run() {
		sendChatting();
		System.out.println("msg end");
	}

}
