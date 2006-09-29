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
 * $Id: AmASAgentServiceResolver.java,v 1.1 2006-09-29 00:32:42 huacui Exp $
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.identity.agents.appserver.v81;

import com.sun.identity.agents.arch.ServiceResolver;
import com.sun.identity.agents.filter.GenericJ2EELogoutHandler;
import com.sun.identity.agents.realm.GenericExternalVerificationHandler;

/**
 * The agent service resolver class for Sun Appserver 8.1
 */
public class AmASAgentServiceResolver extends ServiceResolver {

    /* (non-Javadoc)
     * @see ServiceResolver#getGlobalJ2EEAuthHandlerImpl()
     */
    public String getGlobalJ2EEAuthHandlerImpl() {
        return AmASJ2EEAuthHandler.class.getName();
    }

    /* (non-Javadoc)
     * @see com.sun.identity.agents.arch.ServiceResolver#getSessionBindingFlag()
     */
    public boolean getSessionBindingFlag() {
        return true;
    }

    /* (non-Javadoc)
     * @see ServiceResolver#getGlobalJ2EELogoutHandlerImpl()
     */
    public String getGlobalJ2EELogoutHandlerImpl() {
        return GenericJ2EELogoutHandler.class.getName();
    }

    /* (non-Javadoc)
     * @see ServiceResolver#getGlobalVerificationHandlerImpl()
     */
    public String getGlobalVerificationHandlerImpl() {        
        return GenericExternalVerificationHandler.class.getName();
    }

}
