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
 * $Id: ValidateSAML2.java,v 1.1 2008-04-10 23:15:04 veiming Exp $
 *
 * Copyright 2008 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.identity.workflow;

import com.sun.identity.saml2.common.SAML2Utils;
import com.sun.identity.saml2.jaxb.entityconfig.IDPSSOConfigElement;
import com.sun.identity.saml2.jaxb.entityconfig.SPSSOConfigElement;
import com.sun.identity.saml2.jaxb.metadata.IDPSSODescriptorElement;
import com.sun.identity.saml2.jaxb.metadata.SPSSODescriptorElement;
import com.sun.identity.saml2.jaxb.metadata.SingleLogoutServiceElement;
import com.sun.identity.saml2.jaxb.metadata.SingleSignOnServiceElement;
import com.sun.identity.saml2.meta.SAML2MetaException;
import com.sun.identity.saml2.meta.SAML2MetaManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ValidateSAML2 {
    private static final String LOGIN_URL = "/UI/Login";
    private static final String LOGOUT_URL = "/UI/Logout";
    
    private String realm;
    private String idpEntityId;
    private String spEntityId;
    private String idpMetaAlias;
    private String spMetaAlias;
    private String idpBaseURL;
    private String spBaseURL;

    private boolean bFedlet = false;
    private static ResourceBundle rb = ResourceBundle.getBundle(
        "workflowMessages");
    
    public ValidateSAML2(String realm, String idp, String sp) 
        throws WorkflowException {
        this.realm = realm;
        setIDPEntityId(idp);
        setSPEntityId(sp);
        validateIDP();
        validateSP();
    }
    
    private void validateIDP() 
        throws WorkflowException {
        try {
            SAML2MetaManager mm = SAML2Utils.getSAML2MetaManager();
            IDPSSODescriptorElement elt = mm.getIDPSSODescriptor(
                realm, idpEntityId);

            if (elt == null) {
                Object[] param = {idpEntityId};
                throw new WorkflowException("cannot.locate.idp", param);
            }
                        
            if (idpMetaAlias != null) {
                IDPSSOConfigElement idpConfig = mm.getIDPSSOConfig(realm,
                    idpEntityId);
                if (idpConfig == null) {
                    Object[] param = {idpEntityId};
                    throw new WorkflowException("cannot.locate.idp", param);
                } else {
                    if (!idpConfig.getMetaAlias().equals(idpMetaAlias)) {
                        Object[] param = {idpEntityId};
                        throw new WorkflowException("cannot.locate.idp", param);
                    }
                }
            }

            List ssoServiceList = elt.getSingleSignOnService();
            idpBaseURL = getIDPBaseURL(ssoServiceList);
            if (idpBaseURL == null) {
                Object[] param = {idpEntityId};
                throw new WorkflowException("cannot.locate.idp.loginURL", 
                    param);
            }
            validateURL(idpBaseURL);
        } catch (SAML2MetaException ex) {
            Object[] param = {idpEntityId};
            throw new WorkflowException("cannot.locate.idp", param);
        }
    }
    
    private String getIDPBaseURL(List ssoServiceList) {
        String url = null;
        if ((ssoServiceList != null) && !ssoServiceList.isEmpty()) {
            for (Iterator i = ssoServiceList.iterator();
                i.hasNext() && (url == null);) {
                SingleSignOnServiceElement sso =
                    (SingleSignOnServiceElement) i.next();
                if ((sso != null) && (sso.getBinding() != null)) {
                    String ssoURL = sso.getLocation();
                    int loc = ssoURL.indexOf("/metaAlias/");
                    if (loc != -1) {
                        String tmp = ssoURL.substring(0, loc);
                        loc = tmp.lastIndexOf("/");
                        url = tmp.substring(0, loc);
                    }
                }
            }
        }
        return url;
    }
    
    private void validateSP() 
        throws WorkflowException {
        try {
            SAML2MetaManager mm = SAML2Utils.getSAML2MetaManager();
            SPSSODescriptorElement elt = mm.getSPSSODescriptor(
                realm, spEntityId);
            if (elt == null) {
                Object[] param = {spEntityId};
                throw new WorkflowException("cannot.locate.sp", param);
            }

            if (spMetaAlias != null) {
                SPSSOConfigElement spConfig = mm.getSPSSOConfig(realm,
                    spEntityId);
                if (spConfig == null) {
                    Object[] param = {spEntityId};
                    throw new WorkflowException("cannot.locate.sp", param);
                } else {
                    if (!spConfig.getMetaAlias().equals(spMetaAlias)) {
                        Object[] param = {spEntityId};
                        throw new WorkflowException("cannot.locate.sp", param);
                    }
                }
            }
            List sloServiceList = elt.getSingleLogoutService();
            spBaseURL = getSPBaseURL(sloServiceList);
            if (spBaseURL == null) {
                bFedlet = true;
            } else {
                validateURL(spBaseURL);
            }
            
        } catch (SAML2MetaException ex) {
            Object[] param = {spEntityId};
            throw new WorkflowException("cannot.locate.sp", param);
        }
    }
    
    private void validateURL(String strUrl)
        throws WorkflowException {
        try {
            URL url = new URL(strUrl);
            URLConnection connection = url.openConnection();
            connection.connect();
        } catch (MalformedURLException ex) {
            Object[] params = {strUrl};
            throw new WorkflowException("cannot.reach.url", params);
        } catch (IOException ex) {
            Object[] params = {strUrl};
            throw new WorkflowException("cannot.reach.url", params);
        }
    }
    
    private String getSPBaseURL(List sloServiceList) {
        String url = null;
        if ((sloServiceList != null) && !sloServiceList.isEmpty()) {
            for (Iterator i = sloServiceList.iterator();
                i.hasNext() && (url == null);) {
                SingleLogoutServiceElement sso =
                    (SingleLogoutServiceElement) i.next();
                if ((sso != null) && (sso.getBinding() != null)) {
                    String ssoURL = sso.getLocation();
                    int loc = ssoURL.indexOf("/metaAlias/");
                    if (loc != -1) {
                        String tmp = ssoURL.substring(0, loc);
                        loc = tmp.lastIndexOf("/");
                        url = tmp.substring(0, loc);
                    }
                }
            }
        }
        return url;
    }

    private void setIDPEntityId(String idp) {
        int idx = idp.indexOf("(");
        if (idx != -1) {
            int idx1 = idp.indexOf(")", idx);
            if (idx1 != -1) {
                idpEntityId = idp.substring(0, idx);
                idpMetaAlias = idp.substring(idx+1, idx1);
            } else {
                idpEntityId = idp;
            }
        } else {
            idpEntityId = idp;
        }
    }

    private void setSPEntityId(String sp) {
        int idx = sp.indexOf("(");
        if (idx != -1) {
            int idx1 = sp.indexOf(")", idx);
            if (idx1 != -1) {
                spEntityId = sp.substring(0, idx);
                spMetaAlias = sp.substring(idx+1, idx1);
            } else {
                spEntityId = sp;
            }
        } else {
            spEntityId = sp;
        }
    }

    public static String getMessage(String key) {
        try {
            return rb.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public String getIDPEntityId() {
        return idpEntityId;
    }

    public String getSPEntityId() {
        return spEntityId;
    }

    public String getIDPLoginURL() {
        return idpBaseURL + LOGIN_URL;
    }

    public String getSPLoginURL() {
        return spBaseURL + LOGIN_URL;
    }
    
    public String getIDPLogoutURL() {
        return idpBaseURL + LOGOUT_URL;
    }

    public String getSPLogoutURL() {
        return spBaseURL + LOGOUT_URL;
    }
    
    public boolean isFedlet() {
        return bFedlet;
    }
    
    public boolean isIDPHosted() {
        return (idpMetaAlias != null) && (idpMetaAlias.length() > 0);
    }
    
    public String getSSOURL() {
        if (idpMetaAlias != null) {
            
            try {
                if (bFedlet) {
                    String url = idpBaseURL + "/idpssoinit?" +
                        "NameIDFormat=" +
                        URLEncoder.encode(
                            "urn:oasis:names:tc:SAML:2.0:nameid-format:transient", "UTF-8") +
                        "&metaAlias=" + URLEncoder.encode(idpMetaAlias, "UTF-8") +
                        "&spEntityID=" + URLEncoder.encode(spEntityId, "UTF-8") +
                        "&binding=" + URLEncoder.encode(
                            "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST", "UTF-8");
                    return url;
                } else {
                    return idpBaseURL + "/idpssoinit?metaAlias=" + 
                        URLEncoder.encode(idpMetaAlias, "UTF-8") + 
                        "&spEntityID=" + URLEncoder.encode(spEntityId, "UTF-8");
                }
            } catch (UnsupportedEncodingException ex) {
                return "";
            }
        } else {
            try {
                return spBaseURL + "/spssoinit?metaAlias=" + 
                    URLEncoder.encode(spMetaAlias, "UTF-8") + 
                    "&idpEntityID=" + URLEncoder.encode(idpEntityId, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                return "";
            }
        }
    }
    
    public String getSLOURL() {
        if (idpMetaAlias != null) {
            try {
                return idpBaseURL + 
                    "/saml2/jsp/idpSingleLogoutInit.jsp?metaAlias=" + 
                    URLEncoder.encode(idpMetaAlias, "UTF-8"); 
            } catch (UnsupportedEncodingException ex) {
                return "";
            }
        } else {
            try {
                return spBaseURL + 
                    "/saml2/jsp/spSingleLogoutInit.jsp?metaAlias=" + 
                    URLEncoder.encode(spMetaAlias, "UTF-8") +
                    "&idpEntityID=" + URLEncoder.encode(idpEntityId, "UTF-8"); 
            } catch (UnsupportedEncodingException ex) {
                return "";
            }
        }
    }

    public String getAccountTerminationURL() {
        if (idpMetaAlias != null) {
            try {
                return idpBaseURL +
                    "/saml2/jsp/idpMNIRequestInit.jsp?metaAlias=" +
                    URLEncoder.encode(idpMetaAlias, "UTF-8") +
                    "&spEntityID=" + URLEncoder.encode(spEntityId, "UTF-8") +
                    "&requestType=Terminate";
            } catch (UnsupportedEncodingException ex) {
                return "";
            }
        } else {
            try {
                return spBaseURL +
                    "/saml2/jsp/spMNIRequestInit.jsp?metaAlias=" +
                    URLEncoder.encode(spMetaAlias, "UTF-8") +
                    "&idpEntityID=" + URLEncoder.encode(idpEntityId, "UTF-8") +
                    "&requestType=Terminate";
            } catch (UnsupportedEncodingException ex) {
                return "";
            }
        }
    }
}
