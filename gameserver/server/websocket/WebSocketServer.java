package tetris.gameserver.server.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocketServer implements Runnable{

	void runServer() throws IOException, NoSuchAlgorithmException {
		ServerSocket server = new ServerSocket(8099);
		try {
			while (true) {
				Socket socket = server.accept();

				InputStream in = socket.getInputStream();
				OutputStream out = socket.getOutputStream();

				Scanner s = new Scanner(in, "UTF-8");

				String data = s.useDelimiter("\\r\\n\\r\\n").next();
				Matcher get = Pattern.compile("^GET").matcher(data);

				if (get.find()) {
					Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
					match.find();
					byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n" + "Connection: Upgrade\r\n"
							+ "Upgrade: websocket\r\n" + "Sec-WebSocket-Accept: "
							+ Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(
									(match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
							+ "\r\n\r\n").getBytes("UTF-8");
					out.write(response, 0, response.length);
				}

				WebSocketMessageConverter webSocketMessageConverter = new WebSocketMessageConverter(socket);
				ServerReceiver thread = new ServerReceiver(webSocketMessageConverter);
				thread.start();
			}
		} finally {
			server.close();
		}
	}

	@Override
	public void run() {
		try {
			runServer();
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}
}