package tetris.queue;

public interface TetrisQueue<K> {
	public void add(K input);

	public void get(K output);
}
