package tetris.gameserver.server;

import tetris.queue.ItemBox;

public class AttackerId extends ItemBox<Integer> {

	public AttackerId() {

	}

	public AttackerId(Integer id) {
		setItem(id);
	}

}
