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
 * $Id: IHttpServletRequest.java,v 1.1 2007-12-20 20:19:18 veiming Exp $
 *
 * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.identity.setup;

import java.util.Locale;
import java.util.Map;


public interface IHttpServletRequest {
    /**
     * Returns the locale of the request.
     *
     * @return the locale of the request.
     */
    Locale getLocale();
    
    /**
     * Adds parmeter.
     *
     * @param parameterName Name of Parameter. 
     * @param parameterValue Value of Parameter. 
     */
    void addParameter(String parameterName, Object parameterValue);
    
    /**
     * Returns all parameters values.
     *
     * @return all parameters values
     */
    Map getParameterMap();

    /**
     * Returns the context path.
     *
     * @return the context path.
     */
    String getContextPath();
}
