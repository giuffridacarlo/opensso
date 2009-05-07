/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
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
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: EntitlementServiceTest.java,v 1.2 2009-05-07 22:13:33 veiming Exp $
 */

package com.sun.identity.entitlement.opensso;

import com.sun.identity.entitlement.PolicyConfigFactory;
import com.sun.identity.entitlement.interfaces.IPolicyConfig;
import com.sun.identity.unittest.UnittestLog;
import org.testng.annotations.Test;

public class EntitlementServiceTest {
    @Test
    public void hasEntitlementDITs() {
        IPolicyConfig pc = PolicyConfigFactory.getPolicyConfig();
        boolean result = pc.hasEntitlementDITs();
        UnittestLog.logMessage(
            "EntitlementServiceTest.hasEntitlementDITs: returns " + result);
    }
    
    @Test
    public void migratedToEntitlementService() {
        IPolicyConfig pc = PolicyConfigFactory.getPolicyConfig();
        boolean result = pc.migratedToEntitlementService();
        UnittestLog.logMessage(
            "EntitlementServiceTest.migratedToEntitlementService: returns " +
            result);
    }
}