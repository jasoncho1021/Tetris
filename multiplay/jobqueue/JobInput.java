package tetris.multiplay.jobqueue;

import tetris.core.queue.ItemBox;
import tetris.multiplay.JobCallBack;

public class JobInput extends ItemBox<JobCallBack> {

	public JobInput() {
	}

	public JobInput(JobCallBack jobCallBack) {
		setItem(jobCallBack);
	}
}
