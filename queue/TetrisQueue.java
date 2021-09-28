package tetris.queue;

public interface TetrisQueue<ItemBox> {
	public void add(ItemBox input);

	public void get(ItemBox output);
}
