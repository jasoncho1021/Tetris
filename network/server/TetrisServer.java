package tetris.network.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TetrisServer {

	private static final String READY = "r";

	public static void main(String[] args) {

		try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {

			serverSocket.bind(new InetSocketAddress(15000));
			serverSocket.configureBlocking(false);

			Selector selector = Selector.open();
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);

			System.out.println("---- Ready to access to the Server ----");

			Set<SocketChannel> allClient = new HashSet<>();
			Set<Integer> readyClientID = new HashSet<>();

			ByteBuffer inputBuf = ByteBuffer.allocate(1024);
			ByteBuffer outputBuf = ByteBuffer.allocate(1024);

			Integer autoIncrementIdx = 0;
			while (true) {
				selector.select();

				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();

					if (key.isAcceptable()) {
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel clientSocket = server.accept();

						clientSocket.configureBlocking(false);

						allClient.add(clientSocket);

						clientSocket
								.write(ByteBuffer.wrap("press the 'r' key to prepare to start the game.\n".getBytes()));

						ClientInfo clientInfo = new ClientInfo(autoIncrementIdx++);
						System.out.println("connected clientId:" + clientInfo.getID());

						clientSocket.register(selector, SelectionKey.OP_READ, clientInfo);

					} else if (key.isReadable()) {

						SocketChannel readSocket = (SocketChannel) key.channel();
						ClientInfo info = (ClientInfo) key.attachment();

						System.out.println("------------");

						try {
							readSocket.read(inputBuf);
						} catch (Exception e) {
							key.cancel();
							allClient.remove(readSocket);

							String end = info.getID() + "'s connection is finished by EXCEPTION\n";
							System.out.print(end);

							outputBuf.put(end.getBytes());
							for (SocketChannel s : allClient) {
								if (!readSocket.equals(s)) {
									outputBuf.flip();
									s.write(outputBuf);
								}
							}
							outputBuf.clear();
							continue;
						}

						// r 키 하나만 있는지 확인.
						String requestString = getRequestString(inputBuf);
						System.out.println(info.getID() + ":" + requestString);

						if (READY.equalsIgnoreCase(requestString)) {
							String enter = info.getID() + " is ready \n";
							System.out.print(enter);
							inputBuf.clear();

							outputBuf.put("START@".getBytes());
							outputBuf.flip();
							readSocket.write(outputBuf);
							outputBuf.clear();

							/*							readyClientID.add(info.getID());
														if (readyClientID.size() == allClient.size()) {
															outputBuf.put("START@".getBytes());
							
															// 모든 클라이언트에게 메세지 출력
															for (SocketChannel s : allClient) {
																outputBuf.flip();
																s.write(outputBuf);
															}
															outputBuf.clear();
														}*/
							continue;
						} else if("ATTAC".equalsIgnoreCase(requestString)) {
							// talk
							System.out.println(requestString + "/" + requestString.length());
							outputBuf.put("ATTACK".getBytes());
							outputBuf.flip();

							for (SocketChannel s : allClient) {
								if (!readSocket.equals(s)) {
									s.write(outputBuf);
									outputBuf.flip();
								}
							}

							inputBuf.clear();
							outputBuf.clear();
							continue;
						}

						// talk
						inputBuf.flip(); // enter 제거 없이 바로 전송.
						outputBuf.put((info.getID() + " : ").getBytes());
						outputBuf.put(inputBuf);
						outputBuf.flip();

						for (SocketChannel s : allClient) {
							if (!readSocket.equals(s)) {
								s.write(outputBuf);
								outputBuf.flip();
							}
						}

						inputBuf.clear();
						outputBuf.clear();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String getRequestString(ByteBuffer original) {
		int originPos = original.position();
		int originLimit = original.limit();

		if (originPos > 0) {
			original.limit(originPos - 1); // remove enter key;
		}
		original.position(0);

		byte[] b = new byte[original.limit()];
		original.get(b);
		String requestString = new String(b);

		original.limit(originLimit);
		original.position(originPos);
		return requestString;
	}

}

class ClientInfo {

	private Integer id;

	public ClientInfo(Integer id) {
		this.id = id;
	}

	public Integer getID() {
		return this.id;
	}
}
