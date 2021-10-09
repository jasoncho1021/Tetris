package tetris.multiplay.v3.v_1.server.websocket;

public interface MessageConverter {
	public String read();

	public void write(String msg);
	
	public void close();
}
