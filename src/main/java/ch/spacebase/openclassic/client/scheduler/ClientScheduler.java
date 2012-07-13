package ch.spacebase.openclassic.client.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import ch.spacebase.openclassic.api.scheduler.Scheduler;
import ch.spacebase.openclassic.api.scheduler.Task;
import ch.spacebase.openclassic.api.scheduler.Worker;

public class ClientScheduler implements Scheduler {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final List<ClientTask> newTasks = new ArrayList<ClientTask>();
    private final List<ClientTask> oldTasks = new ArrayList<ClientTask>();
    private final List<ClientTask> tasks = new ArrayList<ClientTask>();
    
    private final List<ClientWorker> activeWorkers = Collections.synchronizedList(new ArrayList<ClientWorker>());
	
	public ClientScheduler() {
        /* this.executor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    tick();
                } catch (Exception e) {
                    OpenClassic.getLogger().log(java.util.logging.Level.SEVERE, "Error while ticking: {0}", e);
                    e.printStackTrace();
                }
            }
        }, 0, Constants.TICK_MILLISECONDS, TimeUnit.MILLISECONDS); */
	}
	
	public void stop() {
		this.cancelAllTasks();
		this.executor.shutdown();
	}
	
	private int schedule(ClientTask task) {
		synchronized(this.newTasks) {
			this.newTasks.add(task);
		}
		
		return task.getTaskId();
	}
	
	public void tick() {
		synchronized(this.newTasks) {
			for(ClientTask task : this.newTasks) {
				this.tasks.add(task);
			}
			
			this.newTasks.clear();
		}

		synchronized(this.oldTasks) {
			for(ClientTask task : this.oldTasks) {
				this.tasks.remove(task);
			}
			
			this.oldTasks.clear();
		}

		for(Iterator<ClientTask> it = this.tasks.iterator(); it.hasNext();) {
			ClientTask task = it.next();
			boolean cont = false;
				
			try {
				if(task.isSync()) {
					cont = task.run();
				} else {
					this.activeWorkers.add(new ClientWorker(task, this));
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
		return schedule(new ClientTask(obj, task, true, delay, period));
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
		return schedule(new ClientTask(obj, task, false, delay, period));
	}

	@Override
	public <T> Future<T> callSyncMethod(Object plugin, Callable<T> task) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void cancelTask(int taskId) {
		synchronized(this.oldTasks) {
			for(ClientTask task : this.tasks) {
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
			for(ClientTask task : this.tasks) {
				if(task.getOwner() == plugin) {
					this.oldTasks.add(task);
				}
			}
		}
	}

	@Override
	public void cancelAllTasks() {
		synchronized(this.oldTasks) {
			for(ClientTask task : this.tasks) {
				this.oldTasks.add(task);
			}
		}
	}

	@Override
	public List<Worker> getActiveWorkers() {
		return new ArrayList<Worker>(this.activeWorkers);
	}

	@Override
	public List<Task> getPendingTasks() {
		ArrayList<Task> result = new ArrayList<Task>();
		
		for(ClientTask task : this.tasks) {
			result.add(task);
		}
		
		return result;
	}

	public synchronized void workerComplete(ClientWorker worker) {
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
