package com.longhuang.programme.utils;

import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
/**
 * 线程池工具类
 * @author fada
 *
 */
public class ThreadPoolManager {
	private ThreadPoolManager(){
		num = Runtime.getRuntime().availableProcessors();
		/**
		 * 进行优先级处理 
		 */
		Comparator<? super Runnable> comparator=new Comparator<Runnable>() {
			@Override
			public int compare(Runnable lhs, Runnable rhs) {
				return lhs.hashCode()>rhs.hashCode()? 1:-1;
			}
		};
		workQueue = new PriorityBlockingQueue<Runnable>(num*10, comparator);
		executor = new ThreadPoolExecutor(num*2, num*2, 8, TimeUnit.SECONDS, workQueue, new ThreadPoolExecutor.CallerRunsPolicy());
	}
	
	private static final ThreadPoolManager manager= new ThreadPoolManager();
	public int num;
	private ThreadPoolExecutor executor;
	private PriorityBlockingQueue<Runnable> workQueue;

	public static ThreadPoolManager getInstance(){
		return manager;
	}
	
	/**
	 * 
	  * 方法描述：停止所有线程,包括等待
	  * 创建人：fada<br/>
	  * 参数名称：
	  * 返回值：
	  * 创建时间：2013-4-11 下午1:45:13
	  * 修改人：
	  * 修改时间：2013-4-11 下午1:45:13
	  * 修改备注：
	 */
	public void stopAllTask(){
		if (!executor.isShutdown()) {
			executor.shutdownNow();
		}
	}

	public void addTask(Runnable runnable){
		if(executor.isShutdown()){
			executor = new ThreadPoolExecutor(num*2, num*2, 8, TimeUnit.SECONDS, workQueue, new ThreadPoolExecutor.CallerRunsPolicy());
		}
		executor.execute(runnable);

	}
	public void removeTask(Runnable runnable){
		if (executor.isShutdown()) return;
		executor.remove(runnable);
	}
}
