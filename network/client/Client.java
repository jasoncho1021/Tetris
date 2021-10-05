package tetris.network.client;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tetris.GameException;
import tetris.TetrisRender;
import tetris.TetrisRenderImpl;

public class Client {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	{
		// Get the process id
		String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", "");
		// MDC
		MDC.put("PID", pid);
	}

	private static final String START = "START";
	public static final String ATTACK = "ATTACK";
	public static final String QUIT = "QUIT";

	private TetrisRender tetris;

	void startClient() {

		Thread messageSenderThread = null;
		MessageSender messageSender = null;

		try (SocketChannel socket = SocketChannel.open(new InetSocketAddress("127.0.0.1", 15000))) {
			messageSender = new MessageSender(socket);
			messageSenderThread = new Thread(messageSender);
			messageSenderThread.start();

			// 수신
			ByteBuffer buf = ByteBuffer.allocate(1024);
			WritableByteChannel out = Channels.newChannel(System.out);

			tetris = new TetrisRenderImpl(messageSender);
			String keyWord;

			while (true) {

				socket.read(buf);

				if (tetris.isRunning()) {
					keyWord = getRequestString(buf, 0);
					// ATTACK
					buf.clear();

					if (ATTACK.equalsIgnoreCase(keyWord)) {
						tetris.addLineJob();
					}
					continue;
				} else {
					keyWord = getRequestString(buf, 1); // 끝문자 '\n' '@' 제거
					// ATTAC

					if (START.equalsIgnoreCase(keyWord)) {
						messageSender.pauseChatting();

						tetris.startRunning();
						Thread tetrisThread = new Thread(tetris);
						tetrisThread.start();

						buf.clear();
						continue;
					}

					if (QUIT.equals(keyWord)) {
						break;
					}
				}
				// Print Chatting Message
				buf.flip();
				out.write(buf);
				buf.clear();
			}

		} catch (IOException e) {
			logger.debug("IOException: connection is finished");
		} catch (GameException ge) {
			logger.debug("server is down");
		} finally {
			if (messageSender != null) {
				messageSender.stopSend();
				try {
					if (messageSenderThread != null) {
						messageSenderThread.join();
						logger.debug("msgThread join");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String getRequestString(ByteBuffer original, int enterKeyOffset) {
		int originPos = original.position();
		int originLimit = original.limit();

		try {
			original.limit(originPos - enterKeyOffset); // remove enter key;
		} catch (IllegalArgumentException ie) {
			throw new GameException(ie);
		}
		original.position(0);

		byte[] b = new byte[original.limit()];
		original.get(b);
		String pressedKey = new String(b);

		original.limit(originLimit);
		original.position(originPos);

		return pressedKey;
	}

	public static void main(String[] args) {
		Client client = new Client();
		client.startClient();
	}
}
