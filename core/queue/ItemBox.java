package tetris.core.queue;

public abstract class ItemBox<K> {
	private K item;

	public void setItem(K item) {
		this.item = item;
	}
	
	public K getItem() {
		return this.item;
	}
}
