package tetris.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

import tetris.TetrisGame;

public class Client {

	public static void main(String[] args) {
		Thread systemInThread;

		try (SocketChannel socket = SocketChannel.open(new InetSocketAddress("127.0.0.1", 15000))) {
			// 송신
			SystemIn systemIn = new SystemIn(socket);
			systemInThread = new Thread(systemIn);
			systemInThread.start();

			// 수신
			ByteBuffer buf = ByteBuffer.allocate(1024);
			WritableByteChannel out = Channels.newChannel(System.out);

			while (true) {
				socket.read(buf);

				if (isGameStartCommand(buf)) {
					systemIn.stopSend();
					TetrisGame tetris = new TetrisGame();
					tetris.start();

					buf.clear();
					continue;
				}

				buf.flip();
				out.write(buf);
				buf.clear();
			}
		} catch (IOException e) {
			System.out.println("IOException: connection is finished");
		}

	}

	public static boolean isGameStartCommand(ByteBuffer original) {
		int originPos = original.position();
		int originLimit = original.limit();

		System.out.println("P:" + originPos);
		System.out.println("L:" + originLimit);
		original.limit(originPos - 1); // remove enter key;
		original.position(0);
		System.out.println("mid:" + original.position());
		byte[] b = new byte[original.limit()];
		original.get(b);
		String pressedKey = new String(b);
		System.out.println("after:" + original.position());
		System.out.println(pressedKey + "/" + pressedKey.length());
		if ("START".equalsIgnoreCase(pressedKey)) {
			return true;
		}

		original.limit(originLimit);
		original.position(originPos);
		return false;
	}

}

class SystemIn implements Runnable {
	SocketChannel socket;

	SystemIn(SocketChannel socket) {
		this.socket = socket;
	}

	boolean flag = true;

	void stopSend() {
		flag = false;
	}

	boolean isRunning() {
		return flag;
	}

	@Override
	public void run() {
		ReadableByteChannel in = Channels.newChannel(System.in);
		ByteBuffer buf = ByteBuffer.allocate(1024);

		try {
			while (isRunning()) {
				if (System.in.available() != 0) { // non-blocking
					in.read(buf);
					buf.flip();
					socket.write(buf);
					buf.clear();
				}
			}
			System.out.println("send stoped");
		} catch (IOException e) {
			System.out.println("IOException: chatting is impossible");
		}
	}
}
