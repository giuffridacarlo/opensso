/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: PluginSchemaImpl.java,v 1.4 2008-07-06 05:48:29 arviranga Exp $
 *
 */

package com.sun.identity.sm;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.shared.xml.XMLUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The class <code>PluginSchemaImpl</code> provides the interfaces to obtain
 * the schema information of a plugin that is configured for a service.
 */
class PluginSchemaImpl extends ServiceSchemaImpl {

    protected String interfaceName;

    protected String iclass;

    protected String jarURL;

    protected String resourceBundleURL;

    protected String i18nFileName;

    protected String viewBeanURL;

    protected String serviceName;

    protected String version;

    protected String schemaDocument;

    CachedSMSEntry smsEntry;

    PluginSchemaImpl() {
        // do nothing
    }

    PluginSchemaImpl(SSOToken token, String serviceName, String version,
            String pluginName, String interfaceName, String orgName)
            throws SMSException {
        this.serviceName = serviceName;
        this.version = version;
        this.name = pluginName;
        this.interfaceName = interfaceName;
        StringBuffer sb = new StringBuffer(100);
        // Construct the DN and get CachedSMSEntry
        sb.append("ou=").append(pluginName).append(",").append("ou=").append(
                interfaceName).append(",").append(
                CreateServiceConfig.PLUGIN_CONFIG_NODE).append("ou=").append(
                version).append(",").append("ou=").append(serviceName).append(
                ",").append(SMSEntry.SERVICES_RDN).append(",");
        try {
            smsEntry = CachedSMSEntry.getInstance(token, sb.toString()
                    + orgName);
            smsEntry.addServiceListener(this);
        } catch (SSOException ssoe) {
            throw (new SMSException(ssoe, "sms-INVALID_SSO_TOKEN"));
        }
        if ((smsEntry.getSMSEntry().getAttributeValues(
                SMSEntry.ATTR_PLUGIN_SCHEMA) == null)
                && !orgName.equals(SMSEntry.baseDN)) {
            // Try to get the SMSEntry for base DN
            try {
                smsEntry = CachedSMSEntry.getInstance(token, sb.toString()
                        + SMSEntry.baseDN);
                smsEntry.addServiceListener(this);
            } catch (SSOException ssoe) {
                throw (new SMSException(ssoe, "sms-INVALID_SSO_TOKEN"));
            }
        }
        attrSchemas = new HashMap();
        update();
    }

    String getName() {
        return (name);
    }

    String getVersion() {
        return (version);
    }

    String getInterfaceName() {
        return (interfaceName);
    }

    String getClassName() {
        return (iclass);
    }

    String getJarURL() {
        return (jarURL);
    }

    String getI18NJarURL() {
        return (resourceBundleURL);
    }

    String getI18NFileName() {
        return (i18nFileName);
    }

    String getPropertiesViewBeanURL() {
        return (viewBeanURL);
    }

    String getI18NKey() {
        return (i18nKey);
    }

    Set getAttributeSchemaNames() {
        return (new HashSet(attrSchemas.keySet()));
    }

    AttributeSchemaImpl getAttributeSchema(String attrSchemaName) {
        return ((AttributeSchemaImpl) attrSchemas.get(attrSchemaName));
    }

    CachedSMSEntry getCachedSMSEntry() {
        return (smsEntry);
    }

    synchronized void updateAndNotifyListeners() {
        update();
    }

    synchronized void update() {
        Node pluginNode = null;
        String[] schemaAttrs = smsEntry.getSMSEntry().getAttributeValues(
                SMSEntry.ATTR_PLUGIN_SCHEMA);
        if (schemaAttrs != null) {
            // Construct the XML document and get the plugin node
            schemaDocument = schemaAttrs[0];
            Document doc = null;
            try {
                doc = SMSSchema.getXMLDocument(schemaAttrs[0], false);
            } catch (SMSException smse) {
                SMSEntry.debug.error("PluginSchemaImpl: XML parser error: "
                        + serviceName + "(" + version + "): " + name, smse);
                return;
            }
            NodeList nodes = doc.getElementsByTagName(SMSUtils.PLUGIN_SCHEMA);
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (XMLUtils.getNodeAttributeValue(nodes.item(i),
                            SMSUtils.NAME).equals(name)) {
                        pluginNode = nodes.item(i);
                        break;
                    }
                }
            }
        }
        if (pluginNode == null) {
            // Plugin Schema might have been removed
            attrSchemas = new HashMap();
            attrValidators = new HashMap();
            iclass = jarURL = resourceBundleURL = i18nFileName = i18nKey = null;
            schemaDocument = SMSSchema.getDummyXML(serviceName, version);
            return;
        }

        // Get the named attributes
        iclass = XMLUtils.getNodeAttributeValue(pluginNode,
                SMSUtils.PLUGIN_SCHEMA_CLASS_NAME);
        jarURL = XMLUtils.getNodeAttributeValue(pluginNode,
                SMSUtils.PLUGIN_SCHEMA_JAR_URL);
        resourceBundleURL = XMLUtils.getNodeAttributeValue(pluginNode,
                SMSUtils.RESOURCE_BUNDLE_URL);
        i18nFileName = XMLUtils.getNodeAttributeValue(pluginNode,
                SMSUtils.PROPERTIES_FILENAME);
        viewBeanURL = XMLUtils.getNodeAttributeValue(pluginNode,
                SMSUtils.PROPERTIES_VIEW_BEAN_URL);
        i18nKey = XMLUtils.getNodeAttributeValue(pluginNode, SMSUtils.I18N_KEY);

        // Get attribute schemas
        Map newAttributeSchemas = new HashMap();
        Map newAttrValidators = new HashMap();
        for (Iterator items = XMLUtils.getChildNodes(pluginNode,
                SMSUtils.SCHEMA_ATTRIBUTE).iterator(); items.hasNext();) {
            Node node = (Node) items.next();
            String aname = XMLUtils.getNodeAttributeValue(node, SMSUtils.NAME);
            AttributeSchemaImpl asi = (AttributeSchemaImpl) attrSchemas
                    .get(aname);
            if (asi == null) {
                asi = new AttributeSchemaImpl(node);
            } else {
                asi.update(node);
            }
            newAttributeSchemas.put(aname, asi);
            newAttrValidators.put(aname, new AttributeValidator(asi));
        }
        attrSchemas = newAttributeSchemas;
        attrValidators = newAttrValidators;
    }

    AttributeValidator getAttributeValidator(String attrName) {
        AttributeValidator av = (AttributeValidator) attrValidators
                .get(attrName);
        if (av == null) {
            AttributeSchemaImpl as = getAttributeSchema(attrName);
            if (as == null) {
                return (null);
            }
            av = new AttributeValidator(as);
            attrValidators.put(attrName, av);
        }
        return (av);
    }

    Document getDocumentCopy() {
        try {
            return (SMSSchema.getXMLDocument(schemaDocument, false));
        } catch (SMSException e) {
            SMSEntry.debug.error("PluginSchemaImpl:: unable to "
                    + "generate XML document: " + name);
        }
        return (null);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(100);
        if (getName() != null) {
            sb.append("Plugin Schema name: ").append(getName()).append("\n");
        }
        // Attributes
        if (attrSchemas.size() > 0) {
            sb.append("Attribute Schemas:\n");
            Iterator items = attrSchemas.keySet().iterator();
            while (items.hasNext()) {
                sb.append(attrSchemas.get(items.next()).toString());
            }
        }
        return (sb.toString());
    }

    static PluginSchemaImpl getInstance(SSOToken token, String serviceName,
            String version, String pluginName, String iName, String orgName)
            throws SMSException {
        String oName = DNMapper.orgNameToDN(orgName);
        StringBuffer sb = new StringBuffer(100);
        sb.append(oName).append(iName).append(pluginName).append(serviceName)
                .append(version);
        String cName = sb.toString().toLowerCase();
        // Check the cache
        PluginSchemaImpl answer = (PluginSchemaImpl) pluginSchemas.get(cName);
        if (answer != null) {
            if (!SMSEntry.cacheSMSEntries) {
                answer.update();
            }
            return (answer);
        }

        synchronized (pluginSchemasMutex) {
            if ((answer = (PluginSchemaImpl) pluginSchemas.get(cName)) == null) 
            {
                answer = new PluginSchemaImpl(token, serviceName, version,
                        pluginName, iName, oName);
                Map sudoPluginSchemas = new HashMap(pluginSchemas);
                sudoPluginSchemas.put(cName, answer);
                pluginSchemas = sudoPluginSchemas;
            }
        }
        return (answer);
    }

    // Clears the cache
    static void clearCache() {
        pluginSchemas = new HashMap();
    }

    private static Map pluginSchemas = new HashMap();

    private static final String pluginSchemasMutex = "PluginSchemaMutext";
}
