/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: RuleNoResourceEditViewBean.java,v 1.3 2008-12-18 20:49:32 veiming Exp $
 *
 */

package com.sun.identity.console.policy;

public class RuleNoResourceEditViewBean
    extends RuleEditViewBean
{
    public static final String DEFAULT_DISPLAY_URL =
        "/console/policy/RuleNoResourceEdit.jsp";

    /**
     * Creates a policy creation view bean.
     */
    public RuleNoResourceEditViewBean() {
        super("RuleNoResourceEdit", DEFAULT_DISPLAY_URL);
    }

    protected String getPropertyXMLFileName(boolean readonly) {
        if (!isReferralPolicy()) {
            return (readonly) ?
            "com/sun/identity/console/propertyPMRuleNoResourceAdd_Readonly.xml":
                "com/sun/identity/console/propertyPMRuleNoResourceAdd.xml";
        } else {
            return (readonly) ?
"com/sun/identity/console/propertyPMRuleNoResourceAddNoAction_Readonly.xml":
            "com/sun/identity/console/propertyPMRuleNoResourceAddNoAction.xml";
        }
    }
}
