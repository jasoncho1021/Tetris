package tetris.server;

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

	public static void main(String[] args) {
		Set<SocketChannel> allClient = new HashSet<>();

		try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {

			serverSocket.bind(new InetSocketAddress(15000));
			serverSocket.configureBlocking(false);

			Selector selector = Selector.open();
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);

			System.out.println("---- Ready to access to the Server ----");

			ByteBuffer inputBuf = ByteBuffer.allocate(1024);
			ByteBuffer outputBuf = ByteBuffer.allocate(1024);

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

						clientSocket.write(ByteBuffer.wrap("Fill in ID: ".getBytes()));

						clientSocket.register(selector, SelectionKey.OP_READ, new ClientInfo());
					} else if (key.isReadable()) {

						SocketChannel readSocket = (SocketChannel) key.channel();
						ClientInfo info = (ClientInfo) key.attachment();

						try {
							readSocket.read(inputBuf);
						} catch (Exception e) {
							key.cancel();
							allClient.remove(readSocket);

							String end = info.getID() + "'s connection is finished\n";
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

						if (info.isID()) {
							inputBuf.limit(inputBuf.position() - 2);
							inputBuf.position(0);
							byte[] b = new byte[inputBuf.limit()];
							inputBuf.get(b);
							info.setID(new String(b));

							String enter = info.getID() + " is entered \n";
							System.out.print(enter);

							outputBuf.put(enter.getBytes());

							for (SocketChannel s : allClient) {
								outputBuf.flip();
								s.write(outputBuf);
							}

							inputBuf.clear();
							outputBuf.clear();
							continue;
						}
						
						inputBuf.flip();
						outputBuf.put((info.getID() + " : ").getBytes());
						outputBuf.put(inputBuf);
						outputBuf.flip();
						
						
						for(SocketChannel s : allClient) {
							if(!readSocket.equals(s)) {
								s.write(outputBuf);
								outputBuf.flip();
							}
						}
						
						inputBuf.clear();
						outputBuf.clear();
					}
				}
			}
		} catch ( IOException e) {
			e.printStackTrace();
		}

	}

}

class ClientInfo {

	private boolean idCheck = true;
	private String id;

	boolean isID() {
		return idCheck;
	}

	private void setCheck() {
		idCheck = false;
	}

	String getID() {
		return id;
	}

	void setID(String id) {
		this.id = id;
		setCheck();
	}
}
