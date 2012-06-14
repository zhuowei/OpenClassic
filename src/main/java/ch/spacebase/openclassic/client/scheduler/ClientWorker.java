package ch.spacebase.openclassic.client.scheduler;

import ch.spacebase.openclassic.api.scheduler.Worker;

public class ClientWorker implements Worker, Runnable {

	private final int id;
	private final Object owner;
	private final ClientTask task;
	private Thread thread = null;
	private boolean shouldContinue = true;
	
	protected ClientWorker(final ClientTask task, final ClientScheduler scheduler) {
		this.id = task.getTaskId();
		this.owner = task.getOwner();
		this.task = task;
		
		String name = "Worker{Owner:" + (this.owner != null ? this.owner.getClass().getName() : "null") + ", id:" + this.id + "}";
		this.thread = new Thread(new Runnable() {
			@Override
			public void run() {
				task.run();
				scheduler.workerComplete(ClientWorker.this);
			}
		}, name);
		
		this.thread.start();
	}
	
	@Override
	public int getTaskId() {
		return this.id;
	}

	@Override
	public Object getOwner() {
		return this.owner;
	}

	@Override
	public Thread getThread() {
		return this.thread;
	}
	
	public ClientTask getTask() {
		return this.task;
	}

	public boolean shouldContinue() {
		return this.shouldContinue;
	}

	public void cancel() {
		if (this.thread == null) return;
		
		if (!this.thread.isAlive()) {
			this.thread.interrupt();
			return;
		}
		
		this.task.stop();
	}

	@Override
	public void run() {
		this.shouldContinue = this.task.run();
	}
	
}
