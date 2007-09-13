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
 * $Id: RecoverableTimerTask.java,v 1.1 2007-09-13 18:12:18 ww203982 Exp $
 *
 * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.identity.common;

import java.util.TimerTask;

/**
 * RecoverableTimerTask is a TimerTask which supports Recoverable interface.
 */

public abstract class RecoverableTimerTask extends TimerTask {
    
    protected Recoverable recoverable;
    
    /**
     * Assigns the recoverable for the TimerTask.
     *
     * @param recoverable The recoverable interface to be run when errors occur
     */
    
    public void setRecoverable(Recoverable recoverable) {
        synchronized (this) {
            this.recoverable = recoverable;
        }
    }
    
}