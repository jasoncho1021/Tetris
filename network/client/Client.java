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
	TetrisRender tetris;

	void startClient() {
		Thread systemInThread;

		try (SocketChannel socket = SocketChannel.open(new InetSocketAddress("127.0.0.1", 15000))) {
			// 송신
			SystemIn systemIn = new SystemIn(socket);
			systemInThread = new Thread(systemIn);
			systemInThread.start();

			// 수신
			ByteBuffer buf = ByteBuffer.allocate(1024);
			WritableByteChannel out = Channels.newChannel(System.out);

			boolean isGameStarted = false;

			tetris = null;
			String content;

			while (true) {

				socket.read(buf);

				if (isGameStarted) { // ATTACK
					content = getRequestString(buf, 0);
				} else { // ATTAC
					content = getRequestString(buf, 1); // '\n' 제거
				}
				System.out.println(content + "/" + content.length());

				if (START.equalsIgnoreCase(content)) {
					//systemInThread.interrupt();
					systemIn.stopSend();

					buf.clear();
					isGameStarted = true;

					/*	Thread attackThread = new Thread() {
							public void run() {
								try {
									while (true) {
										Thread.sleep(1000);
										System.out.println("please attack!!");
										systemIn.setAttackSending();
									}
								} catch (InterruptedException e) {
					
								}
							}
						};
					
						System.out.println("attackThread started");
						attackThread.start();
					*/
					//tetris = new TetrisRenderImpl(new AttackSender(socket));

					tetris = new TetrisRenderImpl(systemIn);
					tetris.gameStart();
					continue;
				} else if (ATTACK.equalsIgnoreCase(content)) {
					buf.clear();

					tetris.addJob(new JobCallBack() {
						@Override
						public void doJob() {
							
						}
					});
					continue;
				}

				/*if (!isGameStarted) {
					buf.flip();
					out.write(buf);
				}*/

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
