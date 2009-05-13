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
 * $Id: PrivilegeIndexStore.java,v 1.2 2009-05-09 01:08:45 veiming Exp $
 */

package com.sun.identity.entitlement;

import com.sun.identity.entitlement.interfaces.IThreadPool;
import com.sun.identity.entitlement.util.PrivilegeSearchFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Privilege Index Store is responsible to storing privilege in
 * a persistent data store.
 */
public abstract class PrivilegeIndexStore {
    private static Map<String, PrivilegeIndexStore> instances = new
        HashMap<String, PrivilegeIndexStore>();
    private static Class clazz;

    static {
        try {
            //TOFIX: configurable
            clazz = Class.forName(
                "com.sun.identity.entitlement.opensso.PolicyDataStore");
        } catch (ClassNotFoundException e) {
            PrivilegeManager.debug.error("PrivilegeIndexStore.static<init>", e);
        }
    }

    /**
     * Returns an instance of the privilege index store.
     *
     * @param realm Realm Name.
     * @return an instance of the privilege index store.
     */
    public synchronized static PrivilegeIndexStore getInstance(
        String realm) {
        if (clazz == null) {
            return null;
        }
        PrivilegeIndexStore impl = instances.get(realm);

        if (impl == null) {
            Class[] parameterTypes = {String.class};
            try {
                Constructor constructor = clazz.getConstructor(parameterTypes);
                Object[] args = {realm};
                impl = (PrivilegeIndexStore) constructor.newInstance(args);
                instances.put(realm, impl);
            } catch (InstantiationException ex) {
                PrivilegeManager.debug.error("PrivilegeIndexStore.getInstance",
                    ex);
            } catch (IllegalAccessException ex) {
                PrivilegeManager.debug.error("PrivilegeIndexStore.getInstance",
                    ex);
            } catch (IllegalArgumentException ex) {
                PrivilegeManager.debug.error("PrivilegeIndexStore.getInstance",
                    ex);
            } catch (InvocationTargetException ex) {
                PrivilegeManager.debug.error("PrivilegeIndexStore.getInstance",
                    ex);
            } catch (NoSuchMethodException ex) {
                PrivilegeManager.debug.error("PrivilegeIndexStore.getInstance",
                    ex);
            } catch (SecurityException ex) {
                PrivilegeManager.debug.error("PrivilegeIndexStore.getInstance",
                    ex);
            }
        }
        return impl;
    }

    /**
     * Adds a set of privileges to the data store. Proper indexes will be
     * created to speed up policy evaluation.
     *
     * @param privileges Privileges to be added.
     * @throws com.sun.identity.entitlement.EntitlementException if addition
     * failed.
     */
    public abstract void add(Set<Privilege> privileges)
        throws EntitlementException;

    /**
     * Deletes a set of privileges from data store.
     *
     * @param privileges Privileges to be deleted.
     * @throws com.sun.identity.entitlement.EntitlementException if deletion
     * failed.
     */
    public abstract void delete(Set<Privilege> privilege)
        throws EntitlementException;

    /**
     * Deletes a privilege from data store.
     *
     * @param privilegeName name of privilege to be deleted.
     * @throws com.sun.identity.entitlement.EntitlementException if deletion
     * failed.
     */
    public abstract void delete(String privilegeName)
        throws EntitlementException;

    /**
     * Deletes a privilege from data store.
     *
     * @param privilegeName name of privilege to be deleted.
     * @param notify <code>true</code> to notify changes.
     * @throws com.sun.identity.entitlement.EntitlementException if deletion
     * failed.
     */
    public abstract String delete(String privilegeName, boolean notify)
        throws EntitlementException;


    /**
     * Returns an iterator of matching privilege objects.
     *
     * @param indexes Resource search indexes.
     * @param subjectIndexes Subject search indexes.
     * @param bSubTree <code>true</code> for sub tree evaluation.
     * @param threadPool Thread pool for executing threads.
     * @return an iterator of matching privilege objects.
     * @throws com.sun.identity.entitlement.EntitlementException if results
     * cannot be obtained.
     */
    public abstract Iterator<Privilege> search(
        ResourceSearchIndexes indexes,
        Set<String> subjectIndexes,
        boolean bSubTree,
        IThreadPool threadPool
    ) throws EntitlementException;


    /**
     * Returns a set of privilege names that matched a set of search criteria.
     *
     * @param filters Set of search filter (criteria).
     * @param boolAnd <code>true</code> to be inclusive.
     * @param numOfEntries Number of maximum search entries.
     * @param sortResults <code>true</code> to have the result sorted.
     * @param ascendingOrder  <code>true</code> to have the result sorted in
     *        ascending order.
     * @return a set of privilege names that matched a set of search criteria.
     * @throws EntitlementException if search failed.
     */
    public abstract Set<String> searchPrivilegeNames(
        Set<PrivilegeSearchFilter> filters,
        boolean boolAnd,
        int numOfEntries,
        boolean sortResults,
        boolean ascendingOrder
    ) throws EntitlementException;
}