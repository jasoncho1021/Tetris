package tetris.multiplay.v3.v_1.server.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import tetris.core.GameException;

public class WebSocketMessageConverter implements MessageConverter {

	private Socket socket;
	private InputStream in;
	private OutputStream out;

	public WebSocketMessageConverter(Socket socket) {
		this.socket = socket;
		System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "]" + "에서 접속하였습니다.");
		try {
			this.in = socket.getInputStream();
			this.out = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	*  %x0 denotes a continuation frame
	
	*  %x1 denotes a text frame				 1000 0001
	
	*  %x2 denotes a binary frame
	
	*  %x3-7 are reserved for further non-control frames
	
	*  %x8 denotes a connection close        1000 1000
	
	*  %x9 denotes a ping
	
	*  %xA denotes a pong
	
	*  %xB-F are reserved for further control frames
	*  
	 */
	@Override
	public String read() {
		int buffLenth = 1024;
		int len = 0;
		byte[] b = new byte[buffLenth];
		try {
			len = in.read(b);
		} catch (IOException e) {
			throw new GameException(e);
		}
		int opCode;
		if (len != -1) {

			opCode = (int) (b[0] & 0xFF);
			if (opCode == 136) {
				throw new GameException("connection close from client");
			}

			byte rLength = 0;
			int rMaskIndex = 2;
			int rDataStart = 0;
			//b[0] is always text in my case so no need to check;
			byte data = b[1];
			byte op = (byte) 127;
			rLength = (byte) (data & op);

			if (rLength == (byte) 126)
				rMaskIndex = 4;
			if (rLength == (byte) 127)
				rMaskIndex = 10;

			byte[] masks = new byte[4];

			int j = 0;
			int i = 0;
			for (i = rMaskIndex; i < (rMaskIndex + 4); i++) {
				masks[j] = b[i];
				j++;
			}

			rDataStart = rMaskIndex + 4;

			int messLen = len - rDataStart;

			byte[] message = new byte[messLen];

			for (i = rDataStart, j = 0; i < len; i++, j++) {
				message[j] = (byte) (b[i] ^ masks[j % 4]);
			}

			return new String(message);
		}

		return null;
	}

	@Override
	public void write(String msg) {
		byte[] rawData = msg.getBytes();
		int frameCount = 0;
		byte[] frame = new byte[10];

		frame[0] = (byte) 129;

		if (rawData.length <= 125) {
			frame[1] = (byte) rawData.length;
			frameCount = 2;
		} else if (rawData.length >= 126 && rawData.length <= 65535) {
			frame[1] = (byte) 126;
			int len = rawData.length;
			frame[2] = (byte) ((len >> 8) & (byte) 255);
			frame[3] = (byte) (len & (byte) 255);
			frameCount = 4;
		} else {
			frame[1] = (byte) 127;
			int len = rawData.length;
			frame[2] = (byte) ((len >> 56) & (byte) 255);
			frame[3] = (byte) ((len >> 48) & (byte) 255);
			frame[4] = (byte) ((len >> 40) & (byte) 255);
			frame[5] = (byte) ((len >> 32) & (byte) 255);
			frame[6] = (byte) ((len >> 24) & (byte) 255);
			frame[7] = (byte) ((len >> 16) & (byte) 255);
			frame[8] = (byte) ((len >> 8) & (byte) 255);
			frame[9] = (byte) (len & (byte) 255);
			frameCount = 10;
		}

		int bLength = frameCount + rawData.length;

		byte[] reply = new byte[bLength];

		int bLim = 0;
		for (int i = 0; i < frameCount; i++) {
			reply[bLim] = frame[i];
			bLim++;
		}
		for (int i = 0; i < rawData.length; i++) {
			reply[bLim] = rawData[i];
			bLim++;
		}

		try {
			out.write(reply);
			out.flush();
		} catch (IOException e) {
			throw new GameException(e);
		}
	}

	@Override
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
