package ch.spacebase.openclassic.game.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import ch.spacebase.openclassic.api.scheduler.Scheduler;
import ch.spacebase.openclassic.api.scheduler.Task;
import ch.spacebase.openclassic.api.scheduler.Worker;

public class ClassicScheduler implements Scheduler {

    private final List<ClassicTask> newTasks = new ArrayList<ClassicTask>();
    private final List<ClassicTask> oldTasks = new ArrayList<ClassicTask>();
    private final List<ClassicTask> tasks = new ArrayList<ClassicTask>();
    
    private final List<ClassicWorker> activeWorkers = Collections.synchronizedList(new ArrayList<ClassicWorker>());
	private final String prefix;
	
	public ClassicScheduler(String prefix) {
		this.prefix = prefix;
	}
    
	public void stop() {
		this.cancelAllTasks();
	}
	
	private int schedule(ClassicTask task) {
		synchronized(this.newTasks) {
			this.newTasks.add(task);
		}
		
		return task.getTaskId();
	}
	
	public void tick() {
		synchronized(this.newTasks) {
			for(ClassicTask task : this.newTasks) {
				this.tasks.add(task);
			}
			
			this.newTasks.clear();
		}

		synchronized(this.oldTasks) {
			for(ClassicTask task : this.oldTasks) {
				this.tasks.remove(task);
			}
			
			this.oldTasks.clear();
		}

		for(Iterator<ClassicTask> it = this.tasks.iterator(); it.hasNext();) {
			ClassicTask task = it.next();
			boolean cont = false;
				
			try {
				if(task.isSync()) {
					cont = task.run();
				} else {
					this.activeWorkers.add(new ClassicWorker(this.prefix, task, this));
				}
			} finally {
				if(!cont) it.remove();
			}
		}
	}

	@Override
	public int scheduleTask(Object obj, Runnable task) {
		return scheduleDelayedTask(obj, task, 0);
	}
	
	@Override
	public int scheduleDelayedTask(Object obj, Runnable task, long delay) {
		return scheduleRepeatingTask(obj, task, delay, -1);
	}

	@Override
	public int scheduleRepeatingTask(Object obj, Runnable task, long delay, long period) {
		return schedule(new ClassicTask(obj, task, true, delay, period));
	}

	@Override
	public int scheduleAsyncTask(Object obj, Runnable task) {
		return scheduleAsyncDelayedTask(obj, task, 0);
	}
	
	@Override
	public int scheduleAsyncDelayedTask(Object obj, Runnable task, long delay) {
		return scheduleAsyncRepeatingTask(obj, task, delay, -1);
	}

	@Override
	public int scheduleAsyncRepeatingTask(Object obj, Runnable task, long delay, long period) {
		return schedule(new ClassicTask(obj, task, false, delay, period));
	}

	@Override
	public <T> Future<T> callSyncMethod(Object plugin, Callable<T> task) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void cancelTask(int taskId) {
		synchronized(this.oldTasks) {
			for(ClassicTask task : this.tasks) {
				if(task.getTaskId() == taskId) {
					this.oldTasks.add(task);
					return;
				}
			}
		}
	}

	@Override
	public void cancelTasks(Object plugin) {
		synchronized(this.oldTasks) {
			for(ClassicTask task : this.tasks) {
				if(task.getOwner() == plugin) {
					this.oldTasks.add(task);
				}
			}
		}
	}

	@Override
	public void cancelAllTasks() {
		synchronized(this.oldTasks) {
			for(ClassicTask task : this.tasks) {
				this.oldTasks.add(task);
			}
		}
	}
	
	@Override
	public boolean isRunning(int id) {
		for(Task task : this.tasks) {
			if(task.getTaskId() == id) return true;
		}
		
		return false;
	}

	@Override
	public boolean isQueued(int id) {
		for(Task task : this.oldTasks) {
			if(task.getTaskId() == id) return true;
		}
		
		return false;
	}

	@Override
	public List<Worker> getActiveWorkers() {
		return new ArrayList<Worker>(this.activeWorkers);
	}

	@Override
	public List<Task> getPendingTasks() {
		ArrayList<Task> result = new ArrayList<Task>();
		
		for(ClassicTask task : this.tasks) {
			result.add(task);
		}
		
		return result;
	}

	public synchronized void workerComplete(ClassicWorker worker) {
		this.activeWorkers.remove(worker);
		
		if(!worker.shouldContinue()) {
			this.oldTasks.add(worker.getTask());
		} else {
			synchronized(this.newTasks) {
				this.newTasks.add(worker.getTask());
			}
		}
	}
}
