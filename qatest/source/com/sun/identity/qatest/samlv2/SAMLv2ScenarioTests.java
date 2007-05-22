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
 * $Id: SAMLv2ScenarioTests.java,v 1.3 2007-05-22 19:22:01 mrudulahg Exp $
 *
 * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.identity.qatest.samlv2;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.sun.identity.qatest.common.AccessManager;
import com.sun.identity.qatest.common.TestCommon;
import com.sun.identity.qatest.common.SAMLv2Common;
import com.sun.identity.qatest.common.TestConstants;
import com.sun.identity.qatest.common.webtest.DefaultTaskHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This class tests various samlv2 scenarios
 * 1. SP Initiated SSO, SLO, Termination
 * 2. SP Initiated SSO, SLO, Termination with POST/SOAP profile
 * 3. IDP Initiated SSO, SLO, Termination
 * 4. IDP Initiated SSO, SLO, Termination with POST/SOAP profile
 * 5. SP Initiated SSO & SLO with transient federation.
 * 6. SP Initiated SSO & SLO with transient federation with POST/SOAP profile
 * 7. IDP Initiated SSO & SLO with transient federation.
 * 8. IDP Initiated SSO & SLO with transient federation with POST/SOAP profile
 */
public class SAMLv2ScenarioTests extends TestCommon {
    
    private WebClient webClient;
    private AccessManager amC;
    private DefaultTaskHandler task1;
    private Map<String, String> configMap;
    ArrayList spuserlist = new ArrayList();
    ArrayList idpuserlist = new ArrayList();
    private String  baseDir;
    private HtmlPage page;
    private URL url;
    
    /**
     * This is constructor for this class.
     */
    public SAMLv2ScenarioTests() {
        super("SAMLv2ScenarioTests");
    }
    
    /**
     * Create the webClient which should be run before each test.
     */
    @BeforeMethod(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec",
    "ldapv3_sec"})
    private void getWebClient() 
    throws Exception {
        try {
            webClient = new WebClient(BrowserVersion.MOZILLA_1_0);
        } catch (Exception e) {
            log(Level.SEVERE, "getWebClient", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * This is setup method. It creates required users for test
     */
    @BeforeClass(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec",
    "ldapv3_sec"})
    public void setup() 
    throws Exception {
        AccessManager amCidp;
        List<String> list;
        try {
            ResourceBundle rb_amconfig = ResourceBundle.getBundle(
                    TestConstants.TEST_PROPERTY_AMCONFIG);
            baseDir = getBaseDir() + SAMLv2Common.fileseparator
                    + rb_amconfig.getString(TestConstants.KEY_ATT_SERVER_NAME)
                    + SAMLv2Common.fileseparator + "built"
                    + SAMLv2Common.fileseparator + "classes"
                    + SAMLv2Common.fileseparator;
            //Upload global properties file in configMap
            configMap = new HashMap<String, String>();
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestConfigData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestData",
                    configMap);
            String spurl = configMap.get(TestConstants.KEY_SP_PROTOCOL) +
                    "://" + configMap.get(TestConstants.KEY_SP_HOST) + ":" +
                    configMap.get(TestConstants.KEY_SP_PORT) +
                    configMap.get(TestConstants.KEY_SP_DEPLOYMENT_URI);
            String idpurl = configMap.get(TestConstants.KEY_IDP_PROTOCOL) +
                    "://" + configMap.get(TestConstants.KEY_IDP_HOST) + ":" +
                    configMap.get(TestConstants.KEY_IDP_PORT) +
                    configMap.get(TestConstants.KEY_IDP_DEPLOYMENT_URI);
            
            getWebClient();
            list = new ArrayList();
            consoleLogin(webClient, spurl,
                    configMap.get(TestConstants.KEY_SP_AMADMIN_USER),
                    configMap.get(TestConstants.KEY_SP_AMADMIN_PASSWORD));
            
            amC = new AccessManager(spurl);
            consoleLogin(webClient, idpurl,
                    configMap.get(TestConstants.KEY_IDP_AMADMIN_USER),
                    configMap.get(TestConstants.KEY_IDP_AMADMIN_PASSWORD));
            amCidp = new AccessManager(idpurl);
            
            String scenArray[] = {"samlv2Scen1", "samlv2Scen2", "samlv2Scen3",
            "samlv2Scen4", "samlv2Scen5", "samlv2Scen6", "samlv2Scen7",
            "samlv2Scen8"};
            for (int i = 0; i < scenArray.length; i++) {
                SAMLv2Common.getEntriesFromResourceBundle(scenArray[i],
                        configMap);
                //create sp user first
                list.clear();
                list.add("sn=" + configMap.get(TestConstants.KEY_SP_USER));
                list.add("cn=" + configMap.get(TestConstants.KEY_SP_USER));
                list.add("userpassword=" +
                        configMap.get(TestConstants.KEY_SP_USER_PASSWORD));
                list.add("inetuserstatus=Active");
                amC.createIdentity(webClient,
                        configMap.get(TestConstants.KEY_SP_REALM),
                        configMap.get(TestConstants.KEY_SP_USER), "User", list);
                spuserlist.add(configMap.get(TestConstants.KEY_SP_USER));
                
                //create idp user
                list.clear();
                list.add("sn=" + configMap.get(TestConstants.KEY_IDP_USER));
                list.add("cn=" + configMap.get(TestConstants.KEY_IDP_USER));
                list.add("userpassword=" +
                        configMap.get(TestConstants.KEY_IDP_USER_PASSWORD));
                list.add("inetuserstatus=Active");
                amCidp.createIdentity(webClient,
                        configMap.get(TestConstants.KEY_IDP_REALM),
                        configMap.get(TestConstants.KEY_IDP_USER), "User",
                        list);
                idpuserlist.add(configMap.get(TestConstants.KEY_IDP_USER));
            }
            consoleLogout(webClient, spurl + "/UI/Logout");
            consoleLogout(webClient, idpurl + "/UI/Logout");
        } catch (Exception e) {
            log(Level.SEVERE, "setup", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("setup");
    }
    
    
    /**
     * Run saml2 scenario 1
     * @DocTest: SAML2|Perform SP initiated SSO, SLO & Termination.
     */
    @Test(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"})
    public void samlv2Scenario1()
    throws Exception {
        entering("samlv2Scenario1", null);
        try {
            log(logLevel, "samlv2Scenario1", "\nRunning: samlv2Scenario1\n");
            
            configMap = new HashMap<String, String>();
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestConfigData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2Scen1", configMap);
            log(logLevel, "samlv2Scenario1", "Map:" + configMap);
            
            //Create xml's for each actions.
            String[] arrActions = {"scen1spsamlv2ssoinit", "scen1spsamlv2slo",
            "scen1spsamlv2terminate"};
            String ssoxmlfile = baseDir + arrActions[0] + ".xml";
            SAMLv2Common.getxmlSPInitSSO(ssoxmlfile, configMap, "artifact",
                    false);
            String sloxmlfile = baseDir + arrActions[1] + ".xml";
            SAMLv2Common.getxmlSPSLO(sloxmlfile, configMap, "http");
            String terminatexmlfile = baseDir + arrActions[2] + ".xml";
            SAMLv2Common.getxmlSPTerminate(terminatexmlfile, configMap, "http");
            
            for (int i = 0; i < arrActions.length; i++) {
                log(logLevel, "samlv2Scenario1",
                        "Inside for loop. value of i is " + arrActions[i]);
                task1 = new DefaultTaskHandler(baseDir + arrActions[i]
                        + ".xml");
                page = task1.execute(webClient);
            }
        } catch (Exception e) {
            log(Level.SEVERE, "samlv2Scenario1", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("samlv2Scenario1");
    }
    
    /**
     *
     * Run saml2 scenario 2
     * @DocTest: SAML2|Perform IDP initiated SSO, SLO & Termination.
     */
    @Test(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"})
    public void samlv2Scenario2()
    throws Exception {
        entering("samlv2Scenario2", null);
        try {
            log(logLevel, "samlv2Scenario2", "\nRunning: samlv2Scenario2\n");
            
            configMap = new HashMap<String, String>();
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestConfigData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2Scen2", configMap);
            log(logLevel, "samlv2Scenario2", "Map:" + configMap);
            
            String[] arrActions = {"scen2idplogin", "scen2idpsamlv2ssoinit",
            "scen2idpsamlv2slo", "scen2idpsamlv2terminate"};
            String loginxmlfile = baseDir + arrActions[0] + ".xml";
            SAMLv2Common.getxmlIDPLogin(loginxmlfile, configMap);
            String ssoxmlfile = baseDir + arrActions[1] + ".xml";
            SAMLv2Common.getxmlIDPInitSSO(ssoxmlfile, configMap, "artifact",
                    false);
            String sloxmlfile = baseDir + arrActions[2] + ".xml";
            SAMLv2Common.getxmlIDPSLO(sloxmlfile, configMap, "http");
            String terminatexmlfile = baseDir + arrActions[3] + ".xml";
            SAMLv2Common.getxmlIDPTerminate(terminatexmlfile, configMap,
                    "http");
            
            for (int i = 0; i < arrActions.length; i++) {
                log(logLevel, "samlv2Scenario2",
                        "Inside for loop. value of i is " + arrActions[i]);
                task1 = new DefaultTaskHandler(baseDir + arrActions[i]
                        + ".xml");
                page = task1.execute(webClient);
            }
        } catch (Exception e) {
            log(Level.SEVERE, "samlv2Scenario2", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("samlv2Scenario2");
    }
    
    
    /**
     * Run saml2 scenario 3
     * @DocTest: SAML2|Perform SP init SSO, SLO, Term with post/soap binding
     */
    @Test(groups={"ff_sec", "ds_sec", "ldapv3_sec"})
    public void samlv2Scenario3()
    throws Exception {
        entering("samlv2Scenario3", null);
        try {
            log(logLevel, "samlv2Scenario3", "\nRunning: samlv2Scenario3\n");
            getWebClient();
            configMap = new HashMap<String, String>();
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestConfigData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2Scen3", configMap);
            log(logLevel, "samlv2Scenario3", "Map:" + configMap);
            
            //Create xml's for each actions.
            String[] arrActions = {"scen3spsamlv2ssoinit", "scen3spsamlv2slo",
            "scen3spsamlv2terminate"};
            String ssoxmlfile = baseDir + arrActions[0] + ".xml";
            SAMLv2Common.getxmlSPInitSSO(ssoxmlfile, configMap, "post", false);
            String sloxmlfile = baseDir + arrActions[1] + ".xml";
            SAMLv2Common.getxmlSPSLO(sloxmlfile, configMap, "soap");
            String terminatexmlfile = baseDir + arrActions[2] + ".xml";
            SAMLv2Common.getxmlSPTerminate(terminatexmlfile, configMap, "soap");
            
            for (int i = 0; i < arrActions.length; i++) {
                log(logLevel, "samlv2Scenario3",
                        "Inside for loop. value of i is " + arrActions[i]);
                task1 = new DefaultTaskHandler(baseDir + arrActions[i]
                        + ".xml");
                page = task1.execute(webClient);
            }
        } catch (Exception e) {
            log(Level.SEVERE, "samlv2Scenario3", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("samlv2Scenario3");
    }
    
    /**
     * Run saml2 scenario 4
     * @DocTest: SAML2|IDP Init SSO, SLO, Term with Post/SOAP binding.
     */
    @Test(groups={"ff_sec", "ds_sec", "ldapv3_sec"})
    public void samlv2Scenario4()
    throws Exception {
        entering("samlv2Scenario4", null);
        try {
            log(logLevel, "samlv2Scenario4", "\nRunning: samlv2Scenario4\n");
            
            configMap = new HashMap<String, String>();
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestConfigData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2Scen4", configMap);
            log(logLevel, "samlv2Scenario4", "Map:" + configMap);
            
            String[] arrActions = {"scen4idplogin", "scen4idpsamlv2ssoinit",
            "scen4idpsamlv2slo", "scen4idpsamlv2terminate"};
            String loginxmlfile = baseDir + arrActions[0] + ".xml";
            SAMLv2Common.getxmlIDPLogin(loginxmlfile, configMap);
            String ssoxmlfile = baseDir + arrActions[1] + ".xml";
            SAMLv2Common.getxmlIDPInitSSO(ssoxmlfile, configMap, "post", false);
            String sloxmlfile = baseDir + arrActions[2] + ".xml";
            SAMLv2Common.getxmlIDPSLO(sloxmlfile, configMap, "soap");
            String terminatexmlfile = baseDir + arrActions[3] + ".xml";
            SAMLv2Common.getxmlIDPTerminate(terminatexmlfile, configMap,
                    "soap");
            for (int i = 0; i < arrActions.length; i++) {
                log(logLevel, "samlv2Scenario4",
                        "Inside for loop. value of i is " + arrActions[i]);
                task1 = new DefaultTaskHandler(baseDir + arrActions[i]
                        + ".xml");
                page = task1.execute(webClient);
            }
        } catch (Exception e) {
            log(Level.SEVERE, "samlv2Scenario4", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("samlv2Scenario4");
    }
    
    
    /**
     * Run saml2 scenario 5
     * @DocTest: SAML2|Perform SP Init SSO & SLO with transient federation.
     */
    @Test(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"})
    public void samlv2Scenario5()
    throws Exception {
        entering("samlv2Scenario5", null);
        try {
            log(logLevel, "samlv2Scenario5", "\nRunning: samlv2Scenario5\n");
            
            configMap = new HashMap<String, String>();
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestConfigData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2Scen5", configMap);
            configMap.put("urlparams", "NameIDFormat=transient");
            log(logLevel, "samlv2Scenario5", "Map:" + configMap);
            
            //Create xml's for each actions.
            String[] arrActions = {"scen5spsamlv2ssoinit", "scen5spsamlv2slo",
            "scen5spsamlv2ssoinit", "scen5spsamlv2slo"};
            String ssoxmlfile = baseDir + arrActions[0] + ".xml";
            SAMLv2Common.getxmlSPInitSSO(ssoxmlfile, configMap, "artifact",
                    false);
            String sloxmlfile = baseDir + arrActions[1] + ".xml";
            SAMLv2Common.getxmlSPSLO(sloxmlfile, configMap, "http");
            
            for (int i = 0; i < arrActions.length; i++) {
                log(logLevel, "samlv2Scenario5",
                        "Inside for loop. value of i is " + arrActions[i]);
                task1 = new DefaultTaskHandler(baseDir + arrActions[i]
                        + ".xml");
                page = task1.execute(webClient);
            }
        } catch (Exception e) {
            log(Level.SEVERE, "samlv2Scenario5", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("samlv2Scenario5");
    }
    
    /**
     * Run saml2 scenario 6
     * @DocTest: SAML2|SP Init SSO, SLO with transient fed with POST/SOAP
     */
    @Test(groups={"ff_sec", "ds_sec", "ldapv3_sec"})
    public void samlv2Scenario6()
    throws Exception {
        entering("samlv2Scenario6", null);
        try {
            log(logLevel, "samlv2Scenario6", "Running: samlv2Scenario6");
            
            configMap = new HashMap<String, String>();
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestConfigData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2Scen6", configMap);
            configMap.put("urlparams", "NameIDFormat=transient");
            log(logLevel, "samlv2Scenario6", "Map:" + configMap);
            
            //Create xml's for each actions.
            String[] arrActions = {"scen6spsamlv2ssoinit", "scen6spsamlv2slo",
            "scen6spsamlv2ssoinit", "scen6spsamlv2slo"};
            String ssoxmlfile = baseDir + arrActions[0] + ".xml";
            SAMLv2Common.getxmlSPInitSSO(ssoxmlfile, configMap, "post", false);
            String sloxmlfile = baseDir + arrActions[1] + ".xml";
            SAMLv2Common.getxmlSPSLO(sloxmlfile, configMap, "soap");
            
            for (int i  = 0; i  <  arrActions.length;  i++) {
                log(logLevel, "samlv2Scenario6",
                        "Inside for loop. value of i is " + arrActions[i]);
                task1 = new DefaultTaskHandler(baseDir + arrActions[i]
                        + ".xml");
                page = task1.execute(webClient);
            }
        } catch (Exception e) {
            log(Level.SEVERE, "samlv2Scenario6", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("samlv2Scenario6");
    }
    
    
    /**
     * Run saml2 scenario 7
     * @DocTest: SAML2| IDP initiated SSO, SLO with transient federation.
     */
    @Test(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"})
    public void samlv2Scenario7()
    throws Exception {
        entering("samlv2Scenario7", null);
        try {
            log(logLevel, "samlv2Scenario7", "\nRunning: samlv2Scenario7\n");
            
            configMap = new HashMap<String, String>();
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestConfigData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2Scen7", configMap);
            configMap.put("urlparams", "NameIDFormat=transient");
            log(logLevel, "samlv2Scenario7", "Map:" + configMap);
            
            //Create xml's for each actions.
            String[] arrActions = {"scen7idplogin", "scen7idpsamlv2ssoinit",
            "scen7idpsamlv2slo", "scen7idplogin", "scen7idpsamlv2ssoinit",
            "scen7idpsamlv2slo"};
            String loginxmlfile = baseDir + arrActions[0] + ".xml";
            SAMLv2Common.getxmlIDPLogin(loginxmlfile, configMap);
            String ssoxmlfile = baseDir + arrActions[1] + ".xml";
            SAMLv2Common.getxmlIDPInitSSO(ssoxmlfile, configMap, "artifact",
                    false);
            String sloxmlfile = baseDir + arrActions[2] + ".xml";
            SAMLv2Common.getxmlIDPSLO(sloxmlfile, configMap, "http");
            
            for (int i = 0; i < arrActions.length; i++) {
                log(logLevel, "samlv2Scenario7",
                        "Inside for loop. value of i is " + arrActions[i]);
                task1 = new DefaultTaskHandler(baseDir + arrActions[i]
                        + ".xml");
                page = task1.execute(webClient);
            }
        } catch (Exception e) {
            log(Level.SEVERE, "samlv2Scenario7", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("samlv2Scenario7");
    }
    
    /**
     * Run saml2 scenario 8
     * @DocTest: SAML2|IDP Init SSO, SLO with transient fed POST/SOAP binding
     */
    @Test(groups={"ff_sec", "ds_sec", "ldapv3_sec"})
    public void samlv2Scenario8()
    throws Exception {
        entering("samlv2Scenario8", null);
        try {
            log(logLevel, "samlv2Scenario8", "\nRunning: samlv2Scenario8\n");
            
            configMap = new HashMap<String, String>();
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestConfigData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2TestData",
                    configMap);
            SAMLv2Common.getEntriesFromResourceBundle("samlv2Scen8", configMap);
            configMap.put("urlparams", "NameIDFormat=transient");
            log(logLevel, "samlv2Scenario8", "Map:" + configMap);
            
            //Create xml's for each actions.
            String[] arrActions = {"scen8idplogin", "scen8idpsamlv2ssoinit",
            "scen8idpsamlv2slo", "scen8idplogin", "scen8idpsamlv2ssoinit",
            "scen8idpsamlv2slo"};
            String loginxmlfile = baseDir + arrActions[0] + ".xml";
            SAMLv2Common.getxmlIDPLogin(loginxmlfile, configMap);
            String ssoxmlfile = baseDir + arrActions[1] + ".xml";
            SAMLv2Common.getxmlIDPInitSSO(ssoxmlfile, configMap, "post", false);
            String sloxmlfile = baseDir + arrActions[2] + ".xml";
            SAMLv2Common.getxmlIDPSLO(sloxmlfile, configMap, "http");
            
            for (int i = 0; i < arrActions.length; i++) {
                log(logLevel, "samlv2Scenario8",
                        "Inside for loop value of i is " + arrActions[i]);
                task1 = new DefaultTaskHandler(baseDir + arrActions[i]
                        + ".xml");
                page = task1.execute(webClient);
            }
        } catch (Exception e) {
            log(Level.SEVERE, "samlv2Scenario8", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("samlv2Scenario8");
    }
    
    
    /**
     * This methods deletes all the users as part of cleanup
     */
    @AfterClass(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"})
    public void cleanup()
    throws Exception {
        entering("cleanup", null);
        try {
            log(logLevel, "Cleanup", "Entering Cleanup: ");
            getWebClient();
            
            // delete sp users
            String spurl = configMap.get(TestConstants.KEY_SP_PROTOCOL) +
                    "://" + configMap.get(TestConstants.KEY_SP_HOST) + ":" +
                    configMap.get(TestConstants.KEY_SP_PORT) +
                    configMap.get(TestConstants.KEY_SP_DEPLOYMENT_URI);
            log(logLevel, "cleanup", "sp users to delete : " + spuserlist);
            consoleLogin(webClient, spurl,
                    configMap.get(TestConstants.KEY_SP_AMADMIN_USER),
                    configMap.get(TestConstants.KEY_SP_AMADMIN_PASSWORD));
            amC = new AccessManager(spurl);
            amC.deleteIdentities(webClient,
                    configMap.get(TestConstants.KEY_SP_REALM), spuserlist,
                    "User");
            consoleLogout(webClient, spurl + "/UI/Logout");
            
            // Create idp users
            String idpurl = configMap.get(TestConstants.KEY_IDP_PROTOCOL) +
                    "://" + configMap.get(TestConstants.KEY_IDP_HOST) + ":" +
                    configMap.get(TestConstants.KEY_IDP_PORT) +
                    configMap.get(TestConstants.KEY_IDP_DEPLOYMENT_URI);
            log(logLevel, "cleanup", "idp users to delete : " + idpuserlist);
            consoleLogin(webClient, idpurl,
                    configMap.get(TestConstants.KEY_IDP_AMADMIN_USER),
                    configMap.get(TestConstants.KEY_IDP_AMADMIN_PASSWORD));
            amC = new AccessManager(idpurl);
            amC.deleteIdentities(webClient,
                    configMap.get(TestConstants.KEY_IDP_REALM), idpuserlist,
                    "User");
            consoleLogout(webClient, idpurl + "/UI/Logout");
        } catch (Exception e) {
            log(Level.SEVERE, "cleanup", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("cleanup");
    }
    
}
