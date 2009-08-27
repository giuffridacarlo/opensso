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
 * $Id: RestTest.java,v 1.1 2009-08-26 23:40:11 rh221556 Exp $
 */

package com.sun.identity.entitlement;

import com.iplanet.am.util.SystemProperties;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.entitlement.opensso.SubjectUtils;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.AMIdentityRepository;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdType;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.identity.security.AdminTokenAction;
import java.security.AccessController;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.ws.rs.core.MultivaluedMap;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * To Test REST interface
 * @author Ravi Hingarajiya <ravi.hingarajiya@sun.com>
 */
public class RestTest {
    private static final String REALM = "/";
    private static final String PRIVILEGE_NAME = "RestTestPrivilege";
    private static final SSOToken adminToken = (SSOToken)
        AccessController.doPrivileged(AdminTokenAction.getInstance());
    private Subject adminSubject = SubjectUtils.createSuperAdminSubject();
    private static final String RESOURCE_NAME = "http://www.resttest.com";
    private AMIdentity user;
    private WebResource client;

    @BeforeClass
    public void setup() throws Exception {
        PrivilegeManager pm = PrivilegeManager.getInstance(REALM,
            adminSubject);
        Privilege privilege = Privilege.getNewInstance();
        privilege.setName(PRIVILEGE_NAME);

        Map<String, Boolean> actions = new HashMap<String, Boolean>();
        actions.put("GET", true);
        Entitlement entitlement = new Entitlement(RESOURCE_NAME + "/*",
            actions);
        privilege.setEntitlement(entitlement);
        EntitlementSubject sbj = new AuthenticatedESubject();
        privilege.setSubject(sbj);

        IPCondition ipc = new IPCondition("127.0.0.0", "128.0.0.0");
        privilege.setCondition(ipc);
        pm.addPrivilege(privilege);
        AMIdentityRepository amir = new AMIdentityRepository(
            adminToken, REALM);
        user = createUser(amir, "RestTestUser");

        client = Client.create().resource(
            SystemProperties.getServerInstanceName() +
            "/ws/1/entitlement/entitlement");
    }

    @AfterClass
    public void cleanup() throws Exception {
        PrivilegeManager pm = PrivilegeManager.getInstance(REALM,
            adminSubject);
        pm.removePrivilege(PRIVILEGE_NAME);
        AMIdentityRepository amir = new AMIdentityRepository(
            adminToken, REALM);
        Set<AMIdentity> users = new HashSet<AMIdentity>();
        users.add(user);
        amir.deleteIdentities(users);
    }

    private AMIdentity createUser(AMIdentityRepository amir, String id)
        throws SSOException, IdRepoException {
        Map<String, Set<String>> attrValues = new HashMap<String, Set<String>>();
        Set<String> set = new HashSet<String>();
        set.add(id);
        attrValues.put("givenname", set);
        attrValues.put("sn", set);
        attrValues.put("cn", set);
        attrValues.put("userpassword", set);
        return amir.createIdentity(IdType.USER, id, attrValues);
    }

    @Test
    public void positiveTest() throws Exception {
        MultivaluedMap params = getMultivaluedMap();
        params.add("env", IPCondition.REQUEST_IP + "=127.1.1.1");
        String json = client.queryParams(params).accept("application/json").
           get(String.class);
        Entitlement ent = new Entitlement(new JSONObject(json));
        boolean result = ent.getActionValue("GET");
        if (!result) {
            throw new Exception("RESTTest.positiveTest failed");
        }
    }

    @Test
    public void negativeTest() throws Exception {
        MultivaluedMap params = getMultivaluedMap();
        params.add("env", IPCondition.REQUEST_IP + "=128.1.1.1");
        String json = client.queryParams(params).accept("application/json").
           get(String.class);
        Entitlement ent = new Entitlement(new JSONObject(json));
        Boolean result = ent.getActionValue("GET");
        if ((result != null) && (result.booleanValue())) {
            throw new Exception("RESTTest.negativeTest failed");
        }
    }

    private MultivaluedMap getMultivaluedMap() {
        MultivaluedMap params = new MultivaluedMapImpl();
        params.add("subject", user.getUniversalId());
        params.add("resource", RESOURCE_NAME + "/index.html");
        params.add("action", "GET");
        params.add("realm", REALM);
        return params;
    }



}