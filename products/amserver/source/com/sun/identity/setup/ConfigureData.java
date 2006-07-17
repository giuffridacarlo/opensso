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
 * $Id: ConfigureData.java,v 1.1 2006-07-17 18:11:25 veiming Exp $
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.identity.setup;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdType;
import com.sun.identity.policy.PolicyException;
import com.sun.identity.policy.PolicyManager;
import com.sun.identity.policy.PolicyUtils;
import com.sun.identity.sm.OrganizationConfigManager;
import com.sun.identity.sm.SMSException;
import com.sun.identity.sm.SchemaType;
import com.sun.identity.sm.ServiceSchema;
import com.sun.identity.sm.ServiceSchemaManager;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.servlet.ServletContext;

/**
 * Configures product bootstrap data.
 */
public class ConfigureData {
    private String baseDir;
    private SSOToken ssoToken;
    private String hostname;
    private ServletContext sctx;

    /**
     * Constructs a new instance.
     *
     * @param baseDir Directory where data is stored.
     * @param sctx Servlet Context.
     * @param hostname Host name of the machine running the product.
     * @param ssoToken Administrator Single Sign On token which to be used
     *        to configure the product.
     */
    public ConfigureData(
        String baseDir,
        ServletContext sctx,
        String hostname,
        SSOToken ssoToken
    ) {
        this.baseDir = baseDir;
        this.sctx = sctx;
        this.hostname = hostname;
        this.ssoToken = ssoToken;
    }

    /**
     * Configures the product.
     *
     * @throws SMSException if service management API failed.
     * @throws SSOException if Single Sign On token is invalid.
     * @throws IOException if IO operations failed.
     * @throws PolicyException if policy cannot be loaded.
     */
    public void configure()
        throws SMSException, SSOException, IOException, PolicyException
    {
        modifyClientDataService();
        modifyClientDetectionService();
        createRealmAndPolicies();
        setRealmAttributes();
    }

    private void modifyClientDataService()
        throws SMSException, SSOException, IOException
    {
        Map map = new HashMap();
        map.put("profileManagerXML",
            getFileContentInSet("SunAMClientData.xml"));
        modifySchemaDefaultValues("SunAMClientData", SchemaType.GLOBAL,
            null, map);
    }

    private void modifyClientDetectionService()
        throws SMSException, SSOException, IOException
    {
        Map map = new HashMap();
        Set set1 = new HashSet(2);
        set1.add("Active");
        map.put("iplanet-am-client-detection-enabled", set1);

        Set set2 = new HashSet(2);
        set2.add("com.sun.mobile.cdm.FEDIClientDetector");
        map.put("iplanet-am-client-detection-class", set2);

        modifySchemaDefaultValues("iPlanetAMClientDetection",
            SchemaType.GLOBAL, null, map);
    }

    private void createRealmAndPolicies()
        throws SMSException, SSOException, PolicyException,
            FileNotFoundException
    {
        createRealm("/sunamhiddenrealmdelegationservicepermissions");
        createPolicies("/sunamhiddenrealmdelegationservicepermissions",
            baseDir + "/defaultDelegationPolicies.xml");
    }

    private void setRealmAttributes()
        throws SMSException
    {
        Map map = new HashMap();
        Set set1 = new HashSet(2);
        set1.add("Active");
        map.put("sunOrganizationStatus", set1);
        Set set2 = new HashSet(4);
        set2.add(hostname);
        set2.add("red");
        map.put("sunOrganizationAliases", set2);
        setRealmAttributes("/", "sunIdentityRepositoryService", map);
    }

    private void setRealmAttributes(
        String realmName,
        String serviceName,
        Map values
    ) throws SMSException {
        OrganizationConfigManager ocm = new OrganizationConfigManager(
            ssoToken, realmName);
        ocm.setAttributes(serviceName, values);
    }

    private void createPolicies(String realmName, String xmlFile)
        throws FileNotFoundException, PolicyException, SSOException
    {
        PolicyManager pm = new PolicyManager(ssoToken, realmName);
        PolicyUtils.createPolicies(pm, sctx.getResourceAsStream(xmlFile));
    }

    private void modifySchemaDefaultValues(
        String serviceName,
        SchemaType schemaType,
        String subSchema,
        Map values
    ) throws SMSException, SSOException, IOException {
        ServiceSchema ss = getServiceSchema(serviceName, schemaType, subSchema);
        ss.setAttributeDefaults(values);
    }
        
    private ServiceSchema getServiceSchema(
        String serviceName,
        SchemaType schemaType,
        String subSchema
    ) throws SMSException, SSOException {
        ServiceSchemaManager ssm = new ServiceSchemaManager(
            serviceName, ssoToken);
        ServiceSchema ss = ssm.getSchema(schemaType);
                                                                                
        if (subSchema != null) {
            boolean done = false;
            StringTokenizer st = new StringTokenizer(subSchema, "/");

            while (st.hasMoreTokens() && !done) {
                String str = st.nextToken();
                                                                                
                if (str != null) {
                    ss = ss.getSubSchema(str);
                    if (ss == null) {
                        throw new RuntimeException(
                            "SubSchema" + str + "does not exist");
                    }
                } else {
                    done = true;
                }
            }
        }
        return ss;
    }

    private Set getFileContentInSet(String fileName)
        throws IOException
    {
        Set set = new HashSet(2);
        set.add(getFileContent(fileName));
        return set;
    }

    private String getFileContent(String fileName)
        throws IOException
    {
        InputStreamReader fin = new InputStreamReader(
            sctx.getResourceAsStream(baseDir + "/" + fileName));
        StringBuffer sbuf = new StringBuffer();
        char[] cbuf = new char[1024];
        int len;

        while ((len = fin.read(cbuf)) > 0) {
            sbuf.append(cbuf, 0, len);
        }

        return sbuf.toString();
    }

    private void createRealm(String realmName)
        throws SMSException
    {
        String parentRealm = getParentRealm(realmName);
        String childRealm = getChildRealm(realmName);
        
        OrganizationConfigManager ocm = new OrganizationConfigManager(
            ssoToken, parentRealm);
        ocm.createSubOrganization (childRealm, null);
    }

    private static String getParentRealm(String path) {
        String parent = "/";
        path = normalizeRealm(path);
        if ((path != null) && (path.length() > 0)) {
            int idx = path.lastIndexOf('/');
            if (idx > 0) {
                parent = path.substring(0, idx);
            }
        }
        return parent;
    }
                                                                                
    private static String getChildRealm(String path) {
        String child = "/";
        path = normalizeRealm(path);
        if ((path != null) && (path.length() > 0)) {
            int idx = path.lastIndexOf('/');
            if (idx != -1) {
                child = path.substring(idx+1);
            }
        }
        return child;
    }

    private static String normalizeRealm(String path) {
        if (path != null) {
            path = path.trim();
            if (path.length() > 0) {
                while (path.indexOf("//") != -1) {
                    path = path.replaceAll("//", "/");
                }
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() -1);
                }
            }
        }
        return path.trim();
    }
}
