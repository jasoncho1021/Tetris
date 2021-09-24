package tetris.queue;

public interface TetrisQueue<T> {
	public void add(T input);

	public void get(T output);
}
