/**
 * 
 */
package ippoz.reload.commons.support;

import java.util.List;

/**
 * The Class ThreadScheduler.
 * Manages the scheduling of different thread simultaneously considering a load factor dependent on the number of virtual processors of the machine.
 *
 * @author Tommy
 */
public abstract class ThreadScheduler extends Thread {
	
	/** The thread list. */
	private List<? extends Thread> tList;
	
	/** The load factor. */
	private int loadFactor;
	
	/** The number of virtual processors of the machine. */
	private int nProc;
	
	/**
	 * Instantiates a new thread scheduler with a default load factor (2*logical cores).
	 */
	public ThreadScheduler(){
		this(null, (int) (2*Runtime.getRuntime().availableProcessors()));
	}
	
	/**
	 * Instantiates a new thread scheduler.
	 *
	 * @param loadFactor the custom load factor
	 */
	public ThreadScheduler(int loadFactor){
		this(null, loadFactor);
	}
	
	/**
	 * Instantiates a new thread scheduler.
	 *
	 * @param tList the thread list
	 * @param loadFactor the load factor
	 */
	public ThreadScheduler(List<? extends Thread> tList, int loadFactor){
		this.tList = tList;
		this.loadFactor = loadFactor;
		nProc = Runtime.getRuntime().availableProcessors();
	}
	
	/**
	 * Sets the thread list.
	 *
	 * @param trainerList the new thread list
	 */
	public void setThreadList(List<? extends Thread> trainerList){
		this.tList = trainerList;
	}
	
	public int getLoadFactor(){
		return loadFactor;
	}
	
	/**
	 * Gets the thread list.
	 *
	 * @return the thread list
	 */
	public List<? extends Thread> getThreadList() {
		return tList;
	}
	
	/**
	 * Returns the thread number.
	 *
	 * @return the thread number
	 */
	public int threadNumber(){
		if(tList != null)
			return tList.size();
		else return 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		int tWindow = nProc*loadFactor;
		try {
			initRun();
			for(int i=0;i<tList.size();i=i+tWindow){
				for(int t=0;t<tWindow;t++){
					if(i+t < tList.size() && tList.get(i+t) != null){
						threadStart(tList.get(i+t), i+t+1);
						tList.get(i+t).start();
					}
				}
				for(int t=0;t<tWindow;t++){
					if(i+t < tList.size() && tList.get(i+t) != null){
						tList.get(i+t).join();
						threadComplete(tList.get(i+t), i+t+1);
					}
				}
			}
		} catch (InterruptedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to schedule Threads");
		}
	}
	
	public void flush(){
		tList = null;
	}
	
	/**
	 * Inits the scheduling structure.
	 */
	protected abstract void initRun();
	
	/**
	 * Event called each time a thread is started.
	 *
	 * @param t the started thread
	 * @param tIndex the thread index
	 */
	protected abstract void threadStart(Thread t, int tIndex);
	
	/**
	 * Event called each time a thread is completed.
	 *
	 * @param t the thread
	 * @param tIndex the thread index
	 */
	protected abstract void threadComplete(Thread t, int tIndex);
	
}
