package tetris.gameserver.server.websocket;

public interface MessageConverter {
	public String read();

	public void write(String msg);
	
	public void close();
}
