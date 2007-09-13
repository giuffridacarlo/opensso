/* The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://opensso.dev.java.net/public/CDDLv1.0.html or
 * opensso/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at opensso/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: TimerPool.java,v 1.1 2007-09-13 18:12:20 ww203982 Exp $
 *
 * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.identity.common;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TreeMap;

/**
 * TimerPool is a scheduleable version of ThreadPool.
 */

public class TimerPool implements Triggerable {
    
    private int poolSize;
    private String name;
    private int busyThreadCount;
    private int currentThreadCount;
    private volatile boolean shutdownThePool;
    private boolean daemon;
    private WorkerThread[] threads;
    private Scheduler scheduler;
    private SortedMap taskList;
    private Date nextRun;

    /**
     * Constructor of TimerPool.
     *
     * @param name The name of the TimerPool
     * @param poolSize The size of the TimerPool
     * @param daemon The boolean to indicate whether the threads in TimerPool
     *        are daemon
     */
    
    public TimerPool(String name, int poolSize, boolean daemon) {
        this.name = name;
	this.poolSize = poolSize;
        this.busyThreadCount = 0;
        this.currentThreadCount = 0;
        this.daemon = daemon;
        this.shutdownThePool = false;
        this.threads = new WorkerThread[poolSize];
        this.scheduler = new Scheduler(this);
        this.scheduler.start();
        this.taskList = Collections.synchronizedSortedMap(new TreeMap());
        synchronized (this) {
            createThreads(poolSize);
        }
    }
    
    /**
     * Creates threads to the TimerPool.
     *
     * @param timersToCreate Number of threads in the TimerPool after creation
     */
    
    private void createThreads(int timersToCreate) {
        if (timersToCreate > poolSize) {
            timersToCreate = poolSize;
        }
        for (int i = currentThreadCount; i < timersToCreate; i++) {
            threads[i - busyThreadCount] = new WorkerThread(name, this);
            threads[i - busyThreadCount].setDaemon(daemon);
            threads[i - busyThreadCount].start();
        }
        currentThreadCount = timersToCreate;
    }
    
    /**
     * Returns thread which is free in the pool.
     *
     * @return A thread which is free from the TimerPool
     */
    
    private WorkerThread getAvailableThread() {
        WorkerThread t = null;
        synchronized (this) {
            if (currentThreadCount == busyThreadCount) {
                createThreads(poolSize);
            }
            t = threads[currentThreadCount - busyThreadCount - 1];
            threads[currentThreadCount - busyThreadCount - 1] = null;
            busyThreadCount++;
        }
        return t;
    }
    
    /**
     * Runs the next timeout task in the sorted container.
     */
    
    private void runNext() {
        WorkerThread t = null;
        HeadTaskRunnable task = null;
        synchronized (this) {
            while (busyThreadCount == poolSize) {
                try {
                    wait();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (nextRun != null) {
                long now = System.currentTimeMillis();
                if (nextRun.getTime() <= now ) {
                    if ((task = (HeadTaskRunnable) taskList.remove(nextRun)) 
                        != null) {
                        t = getAvailableThread();
                    }
                }
                try {
                    nextRun = (Date) taskList.firstKey();
                    long delay = now - nextRun.getTime();
                    scheduler.setWait((delay >= 0 ? delay : 0));
                } catch(NoSuchElementException ex) {
                    nextRun = null;
                }
            }
        }
        if ((t != null) && (task != null)) {
            t.runTask(task);
        }
    }
    
    /**
     * Decreases the number of current threads in the TimerPool.
     */
    
    private synchronized void deductCurrentThreadCount(){
        currentThreadCount--;
        busyThreadCount--;
    }
    
    /**
     * Replaces the scheduler thread in the TimerPool.
     */
    
    private synchronized void replaceScheduler() {
        scheduler.terminate();
        scheduler = new Scheduler(this);
        scheduler.start();
    }
    
    /**
     * Returns the thread to the TimerPool.
     *
     * @param t The thread to be returned to the TimerPool
     */
    
    private void returnThread(WorkerThread t) {        
        if (shutdownThePool) {
            t.terminate();
            synchronized (this) {
                busyThreadCount--;
                if(busyThreadCount == 0){
                    notify();
                }
            }
        } else {
            
            synchronized (this) {
                busyThreadCount--;
                // return timers from the end of array
                threads[currentThreadCount - busyThreadCount - 1] = t;
                notify();
            }
        }
    }
    
    /**
     * Schedules a TaskRunnable to the TimerPool.
     *
     * @param task The TaskRunnable to be scheduled
     * @param time The time to run the TaskRunnable
     */
    
    public void schedule(TaskRunnable task, Date time) throws
        IllegalArgumentException, IllegalStateException {
        if (shutdownThePool) {
            throw new IllegalStateException();
        } else {
            if ((task != null) && (time != null)) {
                HeadTaskRunnable head = null;
                synchronized (taskList) {
                    if((head = (HeadTaskRunnable) taskList.get(time)) == null) {
                        task.setNext(null);
                        taskList.put(time, new HeadTaskRunnable(this, task,
                            time));        
                    }
                    
                }
                if (head == null) {
                    synchronized (this) {
                        if ((nextRun == null) ||
                            (time.getTime() < nextRun.getTime())) {
                            nextRun = time;
                            long delay = time.getTime() -
                                System.currentTimeMillis();
                            scheduler.setWait(((delay < 0) ? 0 : delay));
                        }
                    }
                } else {
                    synchronized (head) {
                        task.setHeadTask(head);
                        task.setPrevious(head);
                        task.setNext(head.next());
                        head.next().setPrevious(task);
                        head.setNext(task);
                    }
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
    }
    
    /**
     * Schedules the TaskRunnable to the TimerPool.
     *
     * @param task The TaskRunnable to be scheduled
     * @param delay The time (in ms) to wait before running the task
     */
    
    public void schedule(TaskRunnable task, long delay) throws
        IllegalArgumentException, IllegalStateException {
        schedule(task, new Date(System.currentTimeMillis() + delay));
    }
    
    /**
     * Implements the trigger function for Triggerable interface.
     */
    
    public void trigger(Date time) {
        // no need to synchronize for single operation
        taskList.remove(time);
    }
    
    /**
     * Shuts down the TimerPool.
     */
    
    public synchronized void shutdown() {
        if(!shutdownThePool) {
            shutdownThePool = true;
            scheduler.terminate();
            for(int i = 0; i < currentThreadCount - busyThreadCount; i++) {
                // terminate the thread from the beginning of the array
                threads[i].terminate();
            }
            while(busyThreadCount != 0){
                try{
                    // wait if there are threads running, it will be notified
                    // when they all back.
                    wait();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            currentThreadCount = busyThreadCount = 0;
            threads = null;
        }
    }
    
    /**
     * WorkerThread is the threads which actually do the jobs. A WorkerThread
     * will be assigned to the task when it is time.
     */
    
    private class WorkerThread extends Thread{
        
        private TimerPool pool;
        private HeadTaskRunnable task;
        private volatile boolean shouldTerminate;
        private volatile boolean needReturn;

        /**
         * Constructor of WorkerThread.
         *
         * @param name The name of the thread
         * @param pool The TimerPool the thread belongs to
         */
        
        public WorkerThread(String name, TimerPool pool) {
            setName(name);
            this.pool = pool;
            this.shouldTerminate = false;
            this.needReturn = true;
        }
    
        /**
         * Runs the task.
         *
         * @param toRun The head of the tasks to be run
         */
        
        public synchronized void runTask(HeadTaskRunnable toRun) {
            this.task = toRun;
            // Although the thread may not in wait state when this function
            // is called (the taskList is not empty), it doesn't hurt to
            // call it.  getState method can check whether the Thread is
            // waiting, but it is available in jdk1.5 or newer.
            this.notify();
        }
        
        /**
         * Terminates this WorkerThread.
         */
        
        // terminate the thread pool when daemon is set to false
        // it is better to have a way to terminate the thread pool
        public synchronized void terminate() {
            shouldTerminate = true;
            needReturn = false;
            this.notify();
        }
        
        /**
         * Implements the run method with errors handling for Thread object.
         */
        
        public void run() {
            HeadTaskRunnable localHeadTask = null;
            TaskRunnable localTask = null;
            TaskRunnable runTask = null;
            WorkerThread t = this;
            while (true) {
                try{
                    synchronized(this) {
                        if (!shouldTerminate){
                            this.wait();
                        }
                        // need a local copy because they may be changed after
                        // leaving synchronized block.
                        localHeadTask = task;
                    }
                    if (shouldTerminate) {
                        // we may need to log something here!
                        break;
                    }
                    if(localHeadTask != null){
                        synchronized (localHeadTask) {
                        // skip head task
                            localTask = localHeadTask.next();
                            do {
                                runTask = localTask;
                                localTask = localTask.next();
                                // cut the connection before run the task.
                                runTask.setNext(null);
                                runTask.run();
                                if (runTask.getRunPeriod() >= 0) {
                                    pool.schedule(runTask, new Date(
                                        localHeadTask.scheduledExecutionTime()
                                        + runTask.getRunPeriod()));
                                }
                            } while (localTask != null);
                        }
                    }
                } catch (RuntimeException ex) {
                    // decide what to log here
                    if (localHeadTask != null) {
                        synchronized (localHeadTask) {
                            if ((runTask.getRunPeriod() >= 0) &&
                                (runTask.scheduledExecutionTime() ==
                                localHeadTask.scheduledExecutionTime())){
                                pool.schedule(runTask, new Date(
                                    localHeadTask.scheduledExecutionTime()
                                    + runTask.getRunPeriod()));
                            }
                            if (localTask != null) {
                                localHeadTask.setNext(localTask);
                                localTask.setPrevious(localHeadTask);
                            }
                        }
                    }
                    synchronized (pool) {
                        pool.deductCurrentThreadCount();
                        if (localTask != null) {
                            WorkerThread thread = pool.getAvailableThread();
                            thread.runTask(localHeadTask);
                        }
                    }
                    shouldTerminate = true;
                    needReturn = false;
                } catch (Exception ex) {
                    // don't need to rethrow
	        } catch (Throwable e) {
                    // decide what to log here
                    if (localHeadTask != null) {
                        synchronized (localHeadTask) {
                            if ((runTask.getRunPeriod() >= 0) &&
                                (runTask.scheduledExecutionTime() ==
                                localHeadTask.scheduledExecutionTime())){
                                pool.schedule(runTask, new Date(
                                    localHeadTask.scheduledExecutionTime()
                                    + runTask.getRunPeriod()));
                            }
                            if (localTask != null) {
                                localHeadTask.setNext(localTask);
                                localTask.setPrevious(localHeadTask);
                            }
                        }
                    }
                    synchronized (pool) {
                        pool.deductCurrentThreadCount();
                        if (localTask != null) {
                            WorkerThread thread = pool.getAvailableThread();
                            thread.runTask(localHeadTask);
                        }
                    }
                    shouldTerminate = true;
                    needReturn = false;
                    // rethrow Error here
                    throw new Error(e);
	        } finally {
                    localHeadTask = null;
                    localTask = null;
                    // the thread may has returned already if shutdown is
                    // called.
                    if (needReturn) {
                        pool.returnThread(t);
                    }
                }
                if (shouldTerminate) {
                    // we may need to log something here!
                    break;
                }
	    }
        }
    }
    
    /**
     * Scheduler is the one who handles the time to wait and assign a thread to
     * the task when it is time. If there is no thread availabe when it is time
     * to run a particular task, Scheduler will wait until thread available.
     */
    
    private class Scheduler extends Thread {
        
        private volatile boolean shouldTerminate;
        private long delay;
        private TimerPool pool;
        
        /**
         * Constructor of Scheduler.
         *
         * @param pool The TimerPool the Scheduler belongs to
         */
        
        public Scheduler(TimerPool pool) {
            this.shouldTerminate = false;
            this.delay = -1;
            this.pool = pool;
        }
        
        /**
         * Sets the time (in ms) to wait.
         *
         * @param delay The time (in ms) to wait
         */
        
        public synchronized void setWait(long delay) {
            this.delay = delay;
            this.notify();
        }
        
        /**
         * Terminates this Scheduler.
         */
        
        public synchronized void terminate() {
            shouldTerminate = true;
            this.notify();
        }
        
        /**
         * Implements run method with errors handling for Thread object.
         */
        
        public void run() {
            long localDelay = -1;
            while (true) {
                try {
                    synchronized (this) {
                        if (!shouldTerminate) {
                            if (delay > 0) {
                                this.wait(delay);
                            } else {
                                if (delay < 0) {
                                    this.wait();
                                }
                            }
                        }
                    }
                    if (shouldTerminate) {
                        break;
                    }
                    pool.runNext();
                } catch (RuntimeException ex) {
                    pool.replaceScheduler();
                } catch (Exception ex) {
                } catch (Throwable t) {
                    pool.replaceScheduler();
                    throw new Error(t);
                }
                if (shouldTerminate) {
                    break;
                }
            }
        }
    }
    
}