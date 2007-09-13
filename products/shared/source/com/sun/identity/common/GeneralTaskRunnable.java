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
 * $Id: GeneralTaskRunnable.java,v 1.1 2007-09-13 18:12:17 ww203982 Exp $
 *
 * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.identity.common;

/**
 * An abstract class which works as a base class of scheduleable unit with the
 * implementations of some functions of TaskRunnable interface.
 * 
 */

public abstract class GeneralTaskRunnable implements TaskRunnable {

    protected volatile TaskRunnable nextTask;
    protected volatile TaskRunnable previousTask;
    protected volatile TaskRunnable headTask;
    
    /**
     * Sets the head task for this linkable TaskRunnable
     *
     * The head task of this TaskRunnable will be set. HeadTask works as a lock
     * when the elements of the linked task is going to be changed.
     *
     * @param headTask The HeadTaskRunnable
     */
    
    public void setHeadTask(TaskRunnable headTask) {
        // synchronize when set the value
        synchronized (this) {
            this.headTask = headTask;
        }
    }
    
    /**
     * Returns the head task of this linkable TaskRunnable.
     *
     * @return The head task of this linkable TaskRunnable
     */
    
    public TaskRunnable getHeadTask() {
        // no need to synchronize for single operation
        return headTask;
    }
    
    /**
     * Returns the TaskRunnable previous to this TaskRunnable in the
     * linked-list.
     *
     * The correct HeadTaskRunnable must be locked before calling this function.
     *
     * @return previous TaskRunnable object or null if it is not set
     */
    
    public TaskRunnable previous() {
        return previousTask;
    }
    
    /**
     * Returns the TaskRunnable next to this TaskRunnable in the linked-list.
     *
     * The correct HeadTaskRunnable must be locked before calling this function.
     *
     * @return next TaskRunnable object or null if it is not set
     */
    
    public TaskRunnable next() {
        return nextTask;
    }
    
    /**
     * Sets the TaskRunnable previous to this TaskRunnable in the linked-list.
     *
     * The correct HeadTaskRunnable must be locked before calling this function.
     *
     * @param task The previous TaskRunnable
     */
    
    public void setPrevious(TaskRunnable task) {
        previousTask = task;
    }
    
    /** 
     * Sets the TaskRunnable next to this TaskRunnable in the linked-list.
     *
     * The correct HeadTaskRunnable must be locked before calling this function.
     *
     * @param task The next TaskRunnable
     */
    
    public void setNext(TaskRunnable task) {
        nextTask = task;
    }
    
    /**
     * Returns the scheduled time of this TaskRunnable.
     *
     * @return A long value indicate the time this TaskRunnable is scheduled,
     *         or -1 if it is not scheduled yet
     */
    
    public long scheduledExecutionTime() {
        // synchronize for more than 1 operation
        synchronized (this) {
            if (headTask != null) {
                return headTask.scheduledExecutionTime();
            }
        }
        return -1;
    }
    
}
