package tetris.jobqueue;

import java.util.LinkedList;
import java.util.Queue;

import tetris.queue.TetrisQueue;

public class JobQueue implements TetrisQueue<JobInput> {
	private Queue<JobInput> queue;

	private JobQueue() {
		queue = new LinkedList<JobInput>();
	}

	public static TetrisQueue<JobInput> getInstance() {
		return LazyHolder.INSTANCE;
	}

	private static class LazyHolder {
		private static final TetrisQueue<JobInput> INSTANCE = new JobQueue();
	}

	@Override
	public void add(JobInput input) {
		synchronized (this) {
			queue.offer(input);
			notifyAll();
		}
	}

	@Override
	public void get(JobInput output) {
		JobInput queueInput;
		synchronized (this) {
			// blocking
			if (queue.isEmpty()) {
				try {
					wait();
				} catch (InterruptedException e) {
					System.out.println("getting from queue is interrupted");
				}
			}

			queueInput = queue.poll();
			output.setItem(queueInput.getItem());
			notifyAll();
		}
	}

}
