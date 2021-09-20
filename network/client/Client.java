package tetris.network.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

import tetris.JobCallBack;
import tetris.TetrisRender;
import tetris.TetrisRenderImpl;

public class Client {

	private static final String START = "START";
	public static final String ATTACK = "ATTACK";

	private TetrisRender tetris;

	void startClient() {

		try (SocketChannel socket = SocketChannel.open(new InetSocketAddress("127.0.0.1", 15000))) {
			Thread messageSenderThread;
			MessageSender messageSender = new MessageSender(socket);
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

						tetris.addJob(new JobCallBack() {
							@Override
							public void doJob() {
								tetris.addLine();
							}
						});
						continue;
					}
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
				}
				// Print Chatting Message
				buf.flip();
				out.write(buf);
				buf.clear();
			}
		} catch (IOException e) {
			System.out.println("IOException: connection is finished");
		}
	}

	public static String getRequestString(ByteBuffer original, int enterKeyOffset) {
		int originPos = original.position();
		int originLimit = original.limit();

		original.limit(originPos - enterKeyOffset); // remove enter key;
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
