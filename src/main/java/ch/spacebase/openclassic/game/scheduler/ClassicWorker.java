package ch.spacebase.openclassic.game.scheduler;

import ch.spacebase.openclassic.api.scheduler.Worker;

public class ClassicWorker implements Worker, Runnable {

	private final int id;
	private final Object owner;
	private final ClassicTask task;
	private Thread thread = null;
	private boolean shouldContinue = true;
	
	protected ClassicWorker(final ClassicTask task, final ClassicScheduler scheduler) {
		this.id = task.getTaskId();
		this.owner = task.getOwner();
		this.task = task;
		
		String name = "Client-Worker{Owner:" + (this.owner != null ? this.owner.getClass().getName() : "null") + ", id:" + this.id + "}";
		this.thread = new Thread(new Runnable() {
			@Override
			public void run() {
				task.run();
				scheduler.workerComplete(ClassicWorker.this);
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
	
	public ClassicTask getTask() {
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
