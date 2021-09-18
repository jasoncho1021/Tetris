package tetris.network.client;

import tetris.JobCallBack;
import tetris.JoyPad;
import tetris.TetrisRender;
import tetris.jobqueue.JobInput;
import tetris.jobqueue.JobQueue;
import tetris.queue.TetrisQueue;

public class DummyJobQueue implements TetrisRender {

	private TetrisQueue<JobInput> jobQueue = JobQueue.getInstance();

	private void doJobCallBack(JobCallBack jobCallBack) {
		System.out.println("doJob!");
		jobCallBack.doJob();
	}

	@Override
	public boolean moveBlockAndRender(JoyPad joyPad) {
		System.out.println("move!");
		return false;
	}

	@Override
	public void addJob(JobCallBack jobCallBack) {
		System.out.println("addJob!");
		jobQueue.add(new JobInput(jobCallBack));
	}

	@Override
	public void gameStart() {
		JobInput jobInput;
		System.out.println("game started");
		while (true) {
			jobInput = new JobInput();
			jobQueue.get(jobInput); // blocking,,until inputReceiver addJob or Attack addJob
			doJobCallBack(jobInput.getItem());
		}

	}

	@Override
	public void addLine() {
		System.out.println("addLIne!");
	}

	@Override
	public void run() {
		gameStart();

	}

}
