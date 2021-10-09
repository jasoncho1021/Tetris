package tetris.multiplay.jobqueue;

import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tetris.multiplay.JobCallBack;

public class JobQueueImpl implements JobQueue<JobInput> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	{
		// Get the process id
		String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", "");
		// MDC
		MDC.put("PID", pid);
	}

	private Queue<JobInput> queue;
	private volatile boolean isPossibleAdd;

	private JobQueueImpl() {
		queue = new LinkedList<JobInput>();
		init();
	}

	public static JobQueue<JobInput> getInstance() {
		return LazyHolder.INSTANCE;
	}

	private static class LazyHolder {
		private static final JobQueue<JobInput> INSTANCE = new JobQueueImpl();
	}

	public void init() {
		isPossibleAdd = true;
		queue.clear();
	}

	public void stopAdd() {
		isPossibleAdd = false;
	}

	public void finish() {
		logger.debug("clear!");
		synchronized (this) {
			logger.debug("clear");
			stopAdd();
			queue.clear();
			queue.offer(new JobInput(new JobCallBack() {
				@Override
				public void doJob() {
					logger.debug("nothing");
				}
			}));
			notifyAll();
		}
	}

	@Override
	public void add(JobInput input) {
		synchronized (this) {
			if (isPossibleAdd) {
				queue.offer(input);
				notifyAll();
			}
		}
	}

	@Override
	public void get(JobInput output) {
		JobInput queueInput;
		synchronized (this) {
			// blocking
			while (queue.isEmpty()) {
				try {
					wait();
				} catch (InterruptedException e) {
					System.out.println("getting from queue is interrupted");
					return;
				}
			}

			queueInput = queue.poll();
			output.setItem(queueInput.getItem());
			//notifyAll();
		}
	}

}
