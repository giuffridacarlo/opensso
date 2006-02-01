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
 * $Id: AuthenticationServletBase.java,v 1.2 2006-02-01 00:22:35 beomsuk Exp $
 *
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */


 
package com.sun.identity.authentication.UI;

import java.io.IOException;

import javax.servlet.ServletException;

import com.iplanet.jato.ApplicationServletBase;
import com.iplanet.jato.CompleteRequestException;
import com.iplanet.jato.RequestContext;
import com.iplanet.jato.ViewBeanManager;

/**
 * This is the front controller of authentication UI
 */
public class AuthenticationServletBase extends ApplicationServletBase {

    /**
     * creates an instance of application servlet base
     */
    public AuthenticationServletBase() {
        super();
    }

    /**
     * Forwards to login view bean, in case of an invalid target
     * request handler (page).
     *
     * @param requestContext - request context
     * @param handlerName - name of handler
     * @throws ServletException
     */
    protected void onRequestHandlerNotFound(
        RequestContext requestContext,
        String handlerName)
        throws ServletException
    {        
        ViewBeanManager viewBeanManager = requestContext.getViewBeanManager();
        AuthExceptionViewBean vb = (AuthExceptionViewBean) 
            viewBeanManager.getViewBean(
            com.sun.identity.authentication.UI.AuthExceptionViewBean.class);
        vb.forwardTo(requestContext);
        throw new CompleteRequestException();
    }


    /**
     * Forwards to login view bean, in case of no handler specified
     *
     * @param requestContext - request context
     * @throws ServletException
     */
    protected void onRequestHandlerNotSpecified(RequestContext requestContext)
        throws ServletException
    {
        ViewBeanManager viewBeanManager = requestContext.getViewBeanManager();
        AuthExceptionViewBean vb = (AuthExceptionViewBean) 
            viewBeanManager.getViewBean(
            com.sun.identity.authentication.UI.AuthExceptionViewBean.class);
        vb.forwardTo(requestContext);
        throw new CompleteRequestException();
    }

    /**
     * Forwards to uncaught exception view bean, to respond to uncaught 
     * application error messages.
     *
     * @param requestContext - request context
     * @param e Exception that was not handled by the application.
     * @throws ServletException
     * @throws IOException
     */
    protected void onUncaughtException(
        RequestContext requestContext,
        Exception e)
        throws ServletException, IOException
    {
        ViewBeanManager viewBeanManager = requestContext.getViewBeanManager();
        AuthExceptionViewBean vb = (AuthExceptionViewBean) 
            viewBeanManager.getViewBean(
            com.sun.identity.authentication.UI.AuthExceptionViewBean.class);
        vb.forwardTo(requestContext);
        throw new CompleteRequestException();
    }

}

