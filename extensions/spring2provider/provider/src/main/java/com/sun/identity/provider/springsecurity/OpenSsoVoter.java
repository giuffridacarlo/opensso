/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008 Sun Microsystems Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
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
 * 
 * "Portions Copyrighted 2008 Miguel Angel Alonso Negro <miguelangel.alonso@gmail.com>"
 *
 * $Id: OpenSsoVoter.java,v 1.1 2008-12-03 00:34:24 superpat7 Exp $
 *
 */
 package com.sun.identity.provider.springsecurity;

import java.util.Iterator;
import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttribute;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.vote.AccessDecisionVoter;

/**
 *
 * @author miguelangel.alonso
 */
public class OpenSsoVoter implements AccessDecisionVoter {

    public static final String OPENSSO_ALLOW = "allow";
    public static final String OPENSSO_DENY = "deny";
    
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    public boolean supports(Class clazz) {
        return true;
    }

    public int vote(Authentication authentication, Object object, ConfigAttributeDefinition config) {
        for (Iterator it = config.getConfigAttributes().iterator(); it.hasNext();) {
            ConfigAttribute configAttribute = (ConfigAttribute)it.next();
            if(configAttribute.getAttribute().equals(OPENSSO_ALLOW)){
                return ACCESS_GRANTED;
            } else if (configAttribute.getAttribute().equals(OPENSSO_DENY)){
                return ACCESS_DENIED;
            }
        }
        return ACCESS_ABSTAIN;
    }

}
