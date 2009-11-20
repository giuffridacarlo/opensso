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
 * $Id: LDAPAddRequest.java,v 1.1 2009-11-20 23:52:58 ww203982 Exp $
 */
package com.sun.identity.shared.ldap;

import com.sun.identity.shared.ldap.client.opers.JDAPProtocolOp;
import java.util.LinkedList;

public class LDAPAddRequest extends LDAPRequest {

    private String dn;
    private LDAPAttribute[] attrs;
    private LDAPEntry entry = null;

    protected LDAPAddRequest(String dn, LDAPAttribute[] attrs,
        LinkedList bytesList, int length) {
        super(bytesList, length);
        this.dn = dn;
        this.attrs = attrs;
    }

    public int getType() {
        return JDAPProtocolOp.ADD_REQUEST;
    }

    public String getDN() {
        return dn;
    }

    public LDAPEntry getLDAPEntry() {
        if (entry == null) {
            entry = new LDAPEntry(dn, new LDAPAttributeSet(attrs));
        }
        return entry;
    }
}
