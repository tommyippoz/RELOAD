/**
 * 
 */
package ippoz.multilayer.detector.commons.support;

import java.util.LinkedList;

/**
 * The Class ThreadScheduler.
 * Manages the scheduling of different thread simultaneously considering a load factor dependent on the number of virtual processors of the machine.
 *
 * @author Tommy
 */
public abstract class ThreadScheduler extends Thread {
	
	/** The thread list. */
	private LinkedList<? extends Thread> tList;
	
	/** The load factor. */
	private int loadFactor;
	
	/** The number of virtual processors of the machine. */
	private int nProc;
	
	/**
	 * Instantiates a new thread scheduler with a default load factor.
	 */
	public ThreadScheduler(){
		this(null, 4);
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
	public ThreadScheduler(LinkedList<? extends Thread> tList, int loadFactor){
		this.tList = tList;
		this.loadFactor = loadFactor;
		nProc = Runtime.getRuntime().availableProcessors();
	}
	
	/**
	 * Sets the thread list.
	 *
	 * @param tList the new thread list
	 */
	public void setThreadList(LinkedList<? extends Thread> tList){
		this.tList = tList;
	}
	
	/**
	 * Gets the thread list.
	 *
	 * @return the thread list
	 */
	public LinkedList<? extends Thread> getThreadList() {
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
					if(i+t < tList.size()){
						threadStart(tList.get(i+t), i+t+1);
						tList.get(i+t).start();
					}
				}
				for(int t=0;t<tWindow;t++){
					if(i+t < tList.size()){
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
