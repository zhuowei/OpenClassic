package ch.spacebase.openclassic.client.scheduler;

import ch.spacebase.openclassic.api.scheduler.Task;

public class ClientTask implements Task {

	private static final Object nextLock = new Object();
	
	private static Integer next = 0;
	
	private final int id;
	private final Runnable task;
	private final Object owner;
	private final long delay;
	private final long period;
	private final boolean sync;
	
	private long counter = 0;
	private boolean running = true;
	
	public ClientTask(Object owner, Runnable task, boolean sync, long delay, long period) {
		synchronized(nextLock) {
			id = next++;
		}
		
		this.owner = owner;
		this.task = task;
		this.delay = delay;
		this.period = period;
		this.sync = sync;
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
	public boolean isSync() {
		return this.sync;
	}
	
	public void stop() {
		this.running = false;
	}
	
	public boolean run() {
		if(!this.running) return false;
		
		this.counter++;
		if(this.counter >= this.delay) {
			if(this.period == -1) {
				this.task.run();
				this.running = false;
			} else if((this.counter - this.delay) % this.period == 0) {
				this.task.run();
			}
		}
		
		return this.running;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + this.getTaskId() + ", " + (this.getOwner() != null ? this.getOwner().toString() : "null") + "}";
	}
	
}
