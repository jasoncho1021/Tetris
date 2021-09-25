package tetris.jobqueue;

import tetris.JobCallBack;
import tetris.queue.ItemBox;

public class JobInput extends ItemBox<JobCallBack> {

	public JobInput() {
	}

	public JobInput(JobCallBack jobCallBack) {
		setItem(jobCallBack);
	}
}
