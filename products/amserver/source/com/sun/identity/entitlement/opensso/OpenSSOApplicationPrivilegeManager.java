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
 * $Id: OpenSSOApplicationPrivilegeManager.java,v 1.2 2009-10-22 21:03:34 veiming Exp $
 */

package com.sun.identity.entitlement.opensso;

import com.iplanet.sso.SSOToken;
import com.sun.identity.common.DisplayUtils;
import com.sun.identity.delegation.DelegationManager;
import com.sun.identity.entitlement.Application;
import com.sun.identity.entitlement.ApplicationManager;
import com.sun.identity.entitlement.ApplicationPrivilege;
import com.sun.identity.entitlement.ApplicationPrivilegeManager;
import com.sun.identity.entitlement.Entitlement;
import com.sun.identity.entitlement.EntitlementCondition;
import com.sun.identity.entitlement.EntitlementException;
import com.sun.identity.entitlement.Evaluator;
import com.sun.identity.entitlement.IPrivilege;
import com.sun.identity.entitlement.Privilege;
import com.sun.identity.entitlement.PrivilegeManager;
import com.sun.identity.entitlement.ReferralPrivilege;
import com.sun.identity.entitlement.ResourceMatch;
import com.sun.identity.entitlement.ResourceSearchIndexes;
import com.sun.identity.entitlement.SubjectAttributesManager;
import com.sun.identity.entitlement.TimeCondition;
import com.sun.identity.entitlement.interfaces.ResourceName;
import com.sun.identity.entitlement.util.SearchFilter;
import com.sun.identity.security.AdminTokenAction;
import com.sun.identity.shared.ldap.util.DN;
import com.sun.identity.sm.DNMapper;
import com.sun.identity.sm.SMSEntry;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.AccessController;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
 import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.security.auth.Subject;

public class OpenSSOApplicationPrivilegeManager extends
    ApplicationPrivilegeManager {
    private static final String RESOURCE_PREFIX = "/entitlements/1.0";
    private static final String APPL_NAME = 
        DelegationManager.DELEGATION_SERVICE;

    private static final String HIDDEN_REALM_DN =
        "o=sunamhiddenrealmdelegationservicepermissions,ou=services,";

    private String realm;
    private Subject caller;
    private boolean bPolicyAdmin;
    private Permission delegatables;
    private Permission readables;
    private Permission modifiables;
    private String resourcePrefix;

    private SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
            AdminTokenAction.getInstance());
    private Subject dsameUserSubject = SubjectUtils.createSubject(adminToken);

    public OpenSSOApplicationPrivilegeManager(String realm, Subject caller)
        throws EntitlementException {
        super();
        this.realm = realm;
        this.caller = caller;
        bPolicyAdmin = isPolicyAdmin();
        init();
    }

    public Set<String> getDelegatableResourceNames(String applicationName) {
        Set<String> resourceNames = delegatables.getResourceNames(
            applicationName);
        return (resourceNames == null) ? Collections.EMPTY_SET :
            resourceNames;
    }

    public void addPrivilege(ApplicationPrivilege appPrivilege)
        throws EntitlementException {
        validatePrivilege(appPrivilege);
        Privilege p = toPrivilege(appPrivilege);
        PrivilegeManager pm = PrivilegeManager.getInstance(getHiddenRealmDN(),
            caller);
        pm.addPrivilege(p);
        delegatables.put(p);
    }

    private void validatePrivilege(ApplicationPrivilege appPrivilege)
        throws EntitlementException {
        Set<String> applicationNames = appPrivilege.getApplicationNames();
        if ((applicationNames == null) || applicationNames.isEmpty()) {
            throw new EntitlementException(320);
        }

        for (String n : applicationNames) {
            Application application = ApplicationManager.getApplication(
                dsameUserSubject, realm, n);
            if (application == null) {
                String[] params = {n};
                throw new EntitlementException(321, params);
            }

            Set<String> resources = appPrivilege.getResourceNames(n);
            if ((resources == null) || resources.isEmpty()) {
                throw new EntitlementException(322);
            }
            for (String r : resources) {
                if (!isDelegatableResource(application, r)) {
                    String[] params = {r};
                    throw new EntitlementException(323, params);
                }
            }
        }
    }

    public void removePrivilege(String name) throws EntitlementException {
        if (delegatables.hasPrivilege(name)) {
            PrivilegeManager pm = PrivilegeManager.getInstance(
                getHiddenRealmDN(), dsameUserSubject);
            pm.removePrivilege(name);
            delegatables.removePrivilege(name);
        } else {
            //TOFIX: not permission warning
        }
    }

    public void replacePrivilege(ApplicationPrivilege appPrivilege)
        throws EntitlementException {
        if (delegatables.hasPrivilege(appPrivilege.getName())) {
            validatePrivilege(appPrivilege);
            Privilege p = toPrivilege(appPrivilege);
            PrivilegeManager pm = PrivilegeManager.getInstance(
                getHiddenRealmDN(), dsameUserSubject);
            pm.modifyPrivilege(p);
            delegatables.put(p);
        } else {
            //TOFIX: not permission warning
        }
    }

    private Privilege toPrivilege(ApplicationPrivilege appPrivilege)
        throws EntitlementException {
        try {
            Privilege p = Privilege.getNewInstance();
            p.setName(appPrivilege.getName());
            p.setDescription(appPrivilege.getDescription());
            Set<String> res = createDelegationResources(appPrivilege);
            Entitlement entitlement = new Entitlement(APPL_NAME, res,
                getActionValues(appPrivilege.getActionValues()));
            p.setEntitlement(entitlement);
            p.setSubject(appPrivilege.getSubject());
            p.setCondition(appPrivilege.getCondition());
            return p;
        } catch (UnsupportedEncodingException ex) {
            String[] params = {};
            throw new EntitlementException(324, params);
        }
    }

    private Map<String, Boolean> getActionValues(
        ApplicationPrivilege.PossibleAction actions) {
        Map<String, Boolean> map = new HashMap<String, Boolean>();

        switch (actions) {
            case READ:
                map.put(ACTION_READ, true);
                break;
            case READ_MODIFY:
                map.put(ACTION_READ, true);
                map.put(ACTION_MODIFY, true);
                break;
            case READ_MODIFY_DELEGATE:
                map.put(ACTION_READ, true);
                map.put(ACTION_MODIFY, true);
                map.put(ACTION_DELEGATE, true);
                break;
            case READ_DELEGATE:
                map.put(ACTION_READ, true);
                map.put(ACTION_DELEGATE, true);
                break;
        }

        return map;
    }

    private ApplicationPrivilege.PossibleAction getActionValues(
        Map<String, Boolean> map) {

        Boolean bRead = map.get(ACTION_READ);
        boolean read = (bRead != null) && bRead.booleanValue();
        Boolean bModify = map.get(ACTION_MODIFY);
        boolean modify = (bModify != null) && bModify.booleanValue();
        Boolean bDelegate = map.get(ACTION_DELEGATE);
        boolean delegate = (bDelegate != null) && bDelegate.booleanValue();

        if (read && modify && delegate) {
            return ApplicationPrivilege.PossibleAction.READ_MODIFY_DELEGATE;
        }
        if (read && delegate) {
            return ApplicationPrivilege.PossibleAction.READ_DELEGATE;
        }
        if (read && modify) {
            return ApplicationPrivilege.PossibleAction.READ_MODIFY;
        }

        return ApplicationPrivilege.PossibleAction.READ;
    }

    private ApplicationPrivilege toApplicationPrivilege(Privilege p) 
        throws EntitlementException {
        ApplicationPrivilege ap = new ApplicationPrivilege(p.getName());
        ap.setDescription(p.getDescription());
        Entitlement ent = p.getEntitlement();
        Set<String> resourceNames = ent.getResourceNames();
        Map<String, Set<String>> mapAppToRes =
            getApplicationPrivilegeResourceNames(resourceNames);
        ap.setApplicationResources(mapAppToRes);
        ap.setActionValues(getActionValues(ent.getActionValues()));
        ap.setSubject(p.getSubject());
        EntitlementCondition cond = p.getCondition();
        if (cond instanceof TimeCondition) {
            ap.setCondition((TimeCondition)cond);
        }
        return ap;
    }



    private Set<String> createDelegationResources(ApplicationPrivilege ap)
        throws UnsupportedEncodingException {
        Set<String> results = new HashSet<String>();
        Set<String> applicationNames = ap.getApplicationNames();
        for (String name : applicationNames) {
            results.add(createDelegationResources(name, ap.getResourceNames(
                name)));
        }
        return results;
    }

    private String createDelegationResources(
        String applicationName,
        Set<String> res) throws UnsupportedEncodingException {
        StringBuilder buff = new StringBuilder();
        buff.append(resourcePrefix).append("/").append(applicationName).append(
            "?");
        boolean first = true;

        for (String r : res) {
            if (first) {
                first = false;
            } else {
                buff.append("&");
            }
            buff.append(URLEncoder.encode(r, "UTF-8"));
        }
        return buff.toString();
    }

    private boolean isDelegatableResource(Application appl, String res) {
        Set<String> resources = getDelegatableResourceNames(appl.getName());

        if ((resources != null) && !resources.isEmpty()) {
            ResourceName resComp = appl.getResourceComparator();
            for (String r : resources) {
                ResourceMatch result = resComp.compare(res, r, true);
                if (result.equals(ResourceMatch.EXACT_MATCH) ||
                    result.equals(ResourceMatch.SUB_RESOURCE_MATCH) ||
                    result.equals(ResourceMatch.WILDCARD_MATCH)) {
                    return true;
                }
            }
        }
        return false;
    }

    public ApplicationPrivilege getPrivilege(String name)
        throws EntitlementException {
        Privilege privilege = delegatables.getPrivilege(name);
        if (privilege == null) {
            String[] param = {name};
            throw new EntitlementException(325, param);
        }
        return toApplicationPrivilege(privilege);
    }

    
    @Override
    public Set<String> search(Set<SearchFilter> filters) {
        Set<String> names = new HashSet<String>();
        Set<String> allNames = delegatables.getPrivilegeNames();

        if ((filters == null) || filters.isEmpty()) {
            names.addAll(allNames);
        } else {
            for (String name : allNames) {
                Privilege p = delegatables.getPrivilege(name);
                if (matchFilter(p, filters)) {
                    names.add(name);
                }
            }
        }

        return names;
    }

    private boolean matchFilter(Privilege p, Set<SearchFilter> filters) {
        for (SearchFilter filter : filters) {
            filter.getFilter();
            String filterName = filter.getName();

            if (filterName.equals(Privilege.NAME_ATTRIBUTE)) {
                if (attrCompare(p.getName(), filter)) {
                    return true;
                }
            } else if (filterName.equals(Privilege.DESCRIPTION_ATTRIBUTE)) {
                if (attrCompare(p.getDescription(), filter)) {
                    return true;
                }
            } else if (filterName.equals(Privilege.CREATED_BY_ATTRIBUTE)) {
                if (attrCompare(p.getCreatedBy(), filter)) {
                    return true;
                }
            } else if (filterName.equals(Privilege.LAST_MODIFIED_BY_ATTRIBUTE)) {
                if (attrCompare(p.getLastModifiedBy(), filter)) {
                    return true;
                }
            } else if (filterName.equals(Privilege.CREATION_DATE_ATTRIBUTE)) {
                if (attrCompare(p.getCreationDate(), filter)) {
                    return true;
                }
            } else if (filterName.equals(
                Privilege.LAST_MODIFIED_DATE_ATTRIBUTE)) {
                if (attrCompare(p.getLastModifiedDate(), filter)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean attrCompare(String value, SearchFilter filter) {
        String pattern = filter.getValue();
        if (pattern != null) {
            if (pattern.equalsIgnoreCase(value) ||
                DisplayUtils.wildcardMatch(value, pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean attrCompare(long value, SearchFilter filter) {
        long filterLong = filter.getNumericValue();
        SearchFilter.Operator operator = filter.getOperator();

        if (operator == SearchFilter.Operator.EQUAL_OPERATOR) {
            return (filterLong == value);
        }
        if (operator == SearchFilter.Operator.GREATER_THAN_OPERATOR) {
            return (value > filterLong);
        }

        return (value < filterLong);
    }

    private void init()
        throws EntitlementException {
        resourcePrefix = "sms://" + DNMapper.orgNameToDN(realm) +
            RESOURCE_PREFIX;
        initPrivilegeNames();
    }

    private void initPrivilegeNames()
        throws EntitlementException {
        Set<String> hostIndex = new HashSet<String>();
        hostIndex.add("://" + DNMapper.orgNameToDN(realm));
        Set<String> pathIndex = new HashSet<String>();
        pathIndex.add(RESOURCE_PREFIX + "/*");
        ResourceSearchIndexes rIndex = new ResourceSearchIndexes(
            hostIndex, pathIndex, null);
        SubjectAttributesManager sam = SubjectAttributesManager.getInstance(
            dsameUserSubject);

        Set<String> subjectIndex = (bPolicyAdmin) ? Collections.EMPTY_SET :
            sam.getSubjectSearchFilter(caller, APPL_NAME);
        OpenSSOIndexStore db = new OpenSSOIndexStore(dsameUserSubject,
            getHiddenRealmDN());
        Iterator<IPrivilege> results = db.search("/", rIndex, subjectIndex,
            false, false);

        delegatables = new Permission(ACTION_DELEGATE, bPolicyAdmin,
            resourcePrefix);
        modifiables = new Permission(ACTION_MODIFY, bPolicyAdmin,
            resourcePrefix);
        readables = new Permission(ACTION_READ, bPolicyAdmin,
            resourcePrefix);

        while (results.hasNext()) {
            Privilege p = (Privilege) results.next();
            delegatables.evaluate(p);
            modifiables.evaluate(p);
            readables.evaluate(p);
        }
    }

    private void addToMap(
        Map<String, Set<String>> map1,
        Map<String, Set<String>> map2) {
        if ((map2 != null) && !map2.isEmpty()) {
            for (String key2 : map2.keySet()) {
                Set<String> set1 = map1.get(key2);
                Set<String> set2 = map2.get(key2);

                if ((set1 == null) || set1.isEmpty()) {
                    map1.put(key2, set2);
                } else {
                    set1.addAll(set2);
                }
            }
        }
    }

    private static Map<String, Set<String>>
        getApplicationPrivilegeResourceNames(Set<String> resources) {
        Map<String, Set<String>> results = new HashMap<String, Set<String>>();
        for (String r : resources) {
            Map<String, Set<String>> map =
                getApplicationPrivilegeResourceNames(r);
            if ((map != null) && !map.isEmpty()) {
                results.putAll(map);
            }
        }
        return results;
    }

    private static Map<String, Set<String>>
        getApplicationPrivilegeResourceNames(String res) {
        int idx = res.indexOf('?');
        if (idx == -1) {
            return Collections.EMPTY_MAP;
        }
        String applicationName = res.substring(0, idx);
        int idx2 = applicationName.lastIndexOf("/");
        if (idx2 != -1) {
            applicationName = applicationName.substring(idx2 +1);
        }
        res = res.substring(idx+1);
        Set<String> resources = new HashSet<String>();

        StringTokenizer st = new StringTokenizer(res, "&");
        while (st.hasMoreTokens()) {
            try {
                String s = st.nextToken();
                resources.add(URLDecoder.decode(s, "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                //TOFIX: error condition
                return Collections.EMPTY_MAP;
            }
        }

        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        map.put(applicationName, resources);
        return map;
    }


    private boolean isPolicyAdmin() {
        if (isDsameUser()) {
            return true;
        }
        Subject adminSubject = SubjectUtils.createSuperAdminSubject();
        try {
            Evaluator eval = new Evaluator(adminSubject, APPL_NAME);
            Set<String> actions = new HashSet<String>();
            actions.add(ACTION_MODIFY);
            String res = "sms://" + DNMapper.orgNameToDN(realm) +
                "/iPlanetAMPolicyService/*";
            Entitlement e = new Entitlement(res, actions);
            return eval.hasEntitlement(getHiddenRealmDN(),
                caller, e, Collections.EMPTY_MAP);
        } catch (EntitlementException ex) {
            PrivilegeManager.debug.error(
                "OpenSSOApplicationPrivilegeManager.isPolicyAdmin", ex);
            return false;
        }
    }

    private boolean isDsameUser() {
        Set<Principal> principals = caller.getPrincipals();
        if ((principals == null) || principals.isEmpty()) {
            return false;
        }

        String dsameuserDN = "id=dsameuser,ou=user," +
            SMSEntry.getRootSuffix();
        String adminUser = "id=amadmin,ou=user," +
            SMSEntry.getRootSuffix();
        Principal p = principals.iterator().next();
        String principalName = p.getName();
        
        if (DN.isDN(principalName)) {
            DN principalDN = new DN(p.getName());
            DN adminDN = new DN(adminUser);

            if (principalDN.equals(adminDN)) {
                return true;
            }
            DN dsameuser = new DN(dsameuserDN);

            if (principalDN.equals(dsameuser)) {
                return true;
            }
        }

        return false;
    }

    private static String getHiddenRealmDN() {
        return HIDDEN_REALM_DN + SMSEntry.getRootSuffix();
    }

    @Override
    public boolean hasPrivilege(
        Privilege p,
        ApplicationPrivilege.Action action
    ) {
        Permission permission = getPermissionObject(action);
        return permission.hasPermission(p);
    }

    @Override
    public boolean hasPrivilege(
        ReferralPrivilege p,
        ApplicationPrivilege.Action action
    ) {
        Permission permission = getPermissionObject(action);
        return permission.hasPermission(p);
    }

    @Override
    public Set<String> getResources(String applicationName,
        ApplicationPrivilege.Action action) {
        Permission p = getPermissionObject(action);
        return p.getResourceNames(applicationName);
    }

    @Override
    public Set<String> getApplications(ApplicationPrivilege.Action action) {
        Permission p = getPermissionObject(action);
        return p.getApplications();
    }

    private Permission getPermissionObject(ApplicationPrivilege.Action action)
    {
        Permission p = readables;
        if (action == ApplicationPrivilege.Action.MODIFY) {
            p = modifiables;
        } else if (action == ApplicationPrivilege.Action.DELEGATE) {
            p =delegatables;
        }
        return p;
    }

    private class Permission {
        private Map<String, Privilege> privileges;
        private Map<String, Set<String>> appNameToResourceNames;
        private String action;
        private boolean bPolicyAdmin;
        private String resourcePrefix;

        private Permission(String action, boolean bPolicyAdmin,
            String resourcePrefix) {
            this.action = action;
            this.bPolicyAdmin = bPolicyAdmin;
            this.resourcePrefix = resourcePrefix;
            privileges = new HashMap<String, Privilege>();
            appNameToResourceNames = new HashMap<String, Set<String>>();

            if (bPolicyAdmin) {
                appNameToResourceNames.putAll(getAllResourceNamesInAllAppls());
            }
        }

        private Set<String> getApplications() {
            if (appNameToResourceNames.isEmpty()) {
                return Collections.EMPTY_SET;
            }
            Set<String> results = new HashSet<String>();
            results.addAll(appNameToResourceNames.keySet());
            return results;
        }

        private void put(Privilege p) {
            privileges.put(p.getName(), p);
        }

        private boolean hasPrivilege(String name) {
            return privileges.keySet().contains(name);
        }

        private void removePrivilege(String name) {
            privileges.remove(name);
        }

        private Privilege getPrivilege(String name) {
            return privileges.get(name);
        }

        private Set<String> getPrivilegeNames() {
            return privileges.keySet();
        }

        private Set<String> getResourceNames(String applName) {
            return appNameToResourceNames.get(applName);
        }

        private Map<String, Set<String>> getAllResourceNamesInAllAppls() {
            Map<String, Set<String>> map = new HashMap<String, Set<String>>();
            Set<String> applNames = ApplicationManager.getApplicationNames(
                dsameUserSubject, realm);

            for (String s : applNames) {
                Application appl = ApplicationManager.getApplication(
                    dsameUserSubject, realm, s);
                map.put(s, appl.getResources());
            }
            return map;
        }

        private void evaluate(Privilege p) {
            Map<String, Boolean> actionValues =
                p.getEntitlement().getActionValues();
            Boolean desiredAction = actionValues.get(action);

            if ((desiredAction != null) && desiredAction.booleanValue()) {
                Map<String, Set<String>> map = getResourceNames(p);

                if ((map != null) && !map.isEmpty()) {
                    addToMap(appNameToResourceNames, map);
                    privileges.put(p.getName(), p);
                }
            }
        }

        private Map<String, Set<String>> getResourceNames(Privilege p) {
            Entitlement ent = p.getEntitlement();

            for (String res : ent.getResourceNames()) {
                if (!res.startsWith(resourcePrefix)) {
                    return Collections.EMPTY_MAP;
                }
            }

            return getApplicationPrivilegeResourceNames(
                ent.getResourceNames());
        }

        private boolean hasPermission(Privilege privilege) {
            Entitlement ent = privilege.getEntitlement();
            String applName = ent.getApplicationName();
            Application appl = ApplicationManager.getApplication(
                dsameUserSubject, realm, applName);
            if (appl == null) {
                return false;
            }
            ResourceName resComp = appl.getResourceComparator();
            Set<String> pResources = ent.getResourceNames();

            Set<String> resources = appNameToResourceNames.get(applName);
            if ((resources == null) || resources.isEmpty()) {
                return false;
            }

            for (String r : pResources) {
                if (!isSubResource(resComp, resources, r)) {
                    return false;
                }
            }
            return true;
        }

        private boolean hasPermission(ReferralPrivilege privilege) {
            Map<String, Set<String>> map =
                privilege.getMapApplNameToResources();

            Set<String> applicationNames = map.keySet();
            for (String applName : applicationNames) {
                Application appl = ApplicationManager.getApplication(
                    dsameUserSubject, realm, applName);
                if (appl == null) {
                    return false;
                }
                ResourceName resComp = appl.getResourceComparator();
                Set<String> pResources = map.get(applName);

                Set<String> resources = appNameToResourceNames.get(applName);
                if ((resources == null) || resources.isEmpty()) {
                    return false;
                }

                for (String r : pResources) {
                    if (!isSubResource(resComp, resources, r)) {
                        return false;
                    }
                }
            }
            return true;
        }

        private boolean isSubResource(
            ResourceName resComp,
            Set<String> resources,
            String res
        ) {
            for (String s : resources) {
                ResourceMatch result = resComp.compare(res, s, true);
                if (result.equals(ResourceMatch.EXACT_MATCH) ||
                    result.equals(ResourceMatch.SUB_RESOURCE_MATCH) ||
                    result.equals(ResourceMatch.WILDCARD_MATCH)) {
                    return true;
                }
            }
            return false;
        }
    }
}
