package tetris.gameserver.server;

import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tetris.queue.TetrisQueue;

public class AttackRequestQueue implements AttackReqQueue<AttackerId> {
	private Queue<AttackerId> queue;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	{
		// Get the process id
		String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", "");
		// MDC
		MDC.put("PID", pid);
	}

	public AttackRequestQueue() {
		queue = new LinkedList<AttackerId>();
	}

	public static TetrisQueue<AttackerId> getInstace() {
		return LazyHolder.INSTANCE;
	}

	private static class LazyHolder {
		private static final TetrisQueue<AttackerId> INSTANCE = new AttackRequestQueue();
	}

	@Override
	public void add(AttackerId input) {
		synchronized (this) {
			logger.debug("add");
			queue.offer(input);
			notifyAll();
		}
	}

	@Override
	public void get(AttackerId output) {
		AttackerId queueOutput;
		synchronized (this) {
			while (queue.isEmpty()) {
				try {
					logger.debug("get blocked");
					wait();
				} catch (InterruptedException e) {
					System.out.println("getting from queue is interrupted");
				}
			}
		}

		logger.debug("get");
		queueOutput = queue.poll();
		output.setItem(queueOutput.getItem());
	}

	@Override
	public synchronized void init() {
		queue.clear();
	}

}
