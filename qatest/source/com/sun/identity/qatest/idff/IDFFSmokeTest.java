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
 * $Id: IDFFSmokeTest.java,v 1.3 2007-06-25 23:11:37 mrudulahg Exp $
 *
 * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.identity.qatest.idff;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.sun.identity.qatest.common.FederationManager;
import com.sun.identity.qatest.common.IDFFCommon;
import com.sun.identity.qatest.common.MultiProtocolCommon;
import com.sun.identity.qatest.common.TestCommon;
import com.sun.identity.qatest.common.TestConstants;
import com.sun.identity.qatest.common.webtest.DefaultTaskHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * This class tests IDFF Federation, SLO, SSO, Name registration & Termination 
 * 1. SP Initiated Federation
 * 2. SP Initiated SLO
 * 3. SP Initiated SSO 
 * 4. SP Initiated Name Registration
 * 5. SP Initiated Termination
 * 6. IDP Initiated SLO. As IDP init federation is not supported, 
 * SP init federation is performed first to follow IDP init SLO. 
 * 7. IDP Initiated Name registration. 
 * 8. IDP Initiated Termination
 */
public class IDFFSmokeTest extends IDFFCommon {
    
    private WebClient webClient;
    private FederationManager fmSP;
    private FederationManager fmIDP;
    private DefaultTaskHandler task;
    private HtmlPage page;
    private Map<String, String> configMap;
    private String  baseDir;
    private URL url;
    private String xmlfile;
    private String spurl;
    private String idpurl;
    private String spmetadata;
    private String spmetadataext;

    /** Creates a new instance of IDFFSmokeTest */
    public IDFFSmokeTest() {
        super("IDFFSmokeTest");
    }
    
    /**
     * Create the webClient 
     */
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
    @Parameters({"ssoprofile", "sloprofile", "terminationprofile", 
    "registrationprofile"})
    @BeforeClass(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"})
    public void setup(String strSSOProfile, String strSLOProfile, 
            String strTermProfile, String strRegProfile)
    throws Exception {
        Object[] params = {strSSOProfile, strSLOProfile, strTermProfile, 
                strRegProfile};
        entering("setup", params);
        Reporter.log("setup parameters: " + params);
        List<String> list;
        try {
            baseDir = getTestBase();
            //Upload global properties file in configMap
            configMap = new HashMap<String, String>();
            configMap = getMapFromResourceBundle("idffTestConfigData");
            configMap.putAll(getMapFromResourceBundle("idffTestData"));
            configMap.putAll(getMapFromResourceBundle("IDFFSmokeTest"));
            log(logLevel, "setup", "Map is " + configMap);
            spurl = configMap.get(TestConstants.KEY_SP_PROTOCOL) +
                    "://" + configMap.get(TestConstants.KEY_SP_HOST) + ":" +
                    configMap.get(TestConstants.KEY_SP_PORT) +
                    configMap.get(TestConstants.KEY_SP_DEPLOYMENT_URI);
            idpurl = configMap.get(TestConstants.KEY_IDP_PROTOCOL) +
                    "://" + configMap.get(TestConstants.KEY_IDP_HOST) + ":" +
                    configMap.get(TestConstants.KEY_IDP_PORT) +
                    configMap.get(TestConstants.KEY_IDP_DEPLOYMENT_URI);
            } catch (Exception e) {
                log(Level.SEVERE, "setup", e.getMessage(), null);
                e.printStackTrace();
                throw e;
            }
        try {
            getWebClient();
            list = new ArrayList();
            consoleLogin(webClient, spurl,
                    configMap.get(TestConstants.KEY_SP_AMADMIN_USER),
                    configMap.get(TestConstants.KEY_SP_AMADMIN_PASSWORD));
            consoleLogin(webClient, idpurl, configMap.get(
                    TestConstants.KEY_IDP_AMADMIN_USER),
                    configMap.get(TestConstants.KEY_IDP_AMADMIN_PASSWORD));
            fmSP = new FederationManager(spurl);
            fmIDP = new FederationManager(idpurl);
            
            list = new ArrayList();
            list.add("sn=" + configMap.get(TestConstants.KEY_SP_USER));
            list.add("cn=" + configMap.get(TestConstants.KEY_SP_USER));
            list.add("userpassword=" +
                    configMap.get(TestConstants.KEY_SP_USER_PASSWORD));
            list.add("inetuserstatus=Active");
            fmSP.createIdentity(webClient,
                    configMap.get(TestConstants.KEY_SP_REALM),
                    configMap.get(TestConstants.KEY_SP_USER), "User", list);
            log(logLevel, "setup", "SP user created is " + list);
            
            // Create idp users
            list.clear();
            list.add("sn=" + configMap.get(TestConstants.KEY_IDP_USER));
            list.add("cn=" + configMap.get(TestConstants.KEY_IDP_USER));
            list.add("userpassword=" +
                    configMap.get(TestConstants.KEY_IDP_USER_PASSWORD));
            list.add("inetuserstatus=Active");
            fmIDP.createIdentity(webClient,
                    configMap.get(TestConstants.KEY_IDP_REALM),
                    configMap.get(TestConstants.KEY_IDP_USER), "User", list);
            log(logLevel, "setup", "IDP user created is " + list);
            
            //If any of the profile is diff than the default profile, 
            //then only delete & import the metadata. Else leave it as it is. 
            if (strSSOProfile.equals("post") || strSLOProfile.equals("soap") ||
                    strTermProfile.equals("soap") || strRegProfile.equals("soap")) {
                HtmlPage spmetaPage = fmSP.exportEntity(webClient,
                        (String)configMap.get(TestConstants.KEY_SP_ENTITY_NAME), 
                        (String)configMap.get(TestConstants.KEY_SP_REALM),
                        false, true, true, "idff");
                spmetadataext = MultiProtocolCommon.getExtMetadataFromPage(
                        spmetaPage);            
                spmetadata = MultiProtocolCommon.getMetadataFromPage(spmetaPage);            

                //if profile is set to post, change the metadata & run the tests. 
                log(logLevel, "setup", "SSO Profile is set to " + strSSOProfile);
                log(logLevel, "setup", "SLO Profile is set to " + strSLOProfile);
                log(logLevel, "setup", "Termination Profile is set to " + 
                        strTermProfile);
                log(logLevel, "setup", "Registration Profile is set to " + 
                        strRegProfile);
                String spmetadataextmod = spmetadataext;
                String spmetadatamod = spmetadata;
                if (strSSOProfile.equals("post")) {
                    log(logLevel, "setup", "Change SSO Profile to post");
                    spmetadataextmod = setSPSSOProfile(spmetadataext, "post");
                }
                if (strSLOProfile.equals("soap")) {
                    log(logLevel, "setup", "Change SLO Profile to soap");
                    spmetadatamod = setSPSLOProfile(spmetadata, "soap");
                } 
                if (strTermProfile.equals("soap")) {
                    log(logLevel, "setup", "Change Termination Profile to soap");
                    spmetadatamod = setSPTermProfile(spmetadatamod, "soap");
                }
                if (strRegProfile.equals("soap")) {
                    log(logLevel, "setup", "Change Registration Profile to soap");
                    spmetadatamod = setSPRegProfile(spmetadatamod, "soap");
                }
                
                log(logLevel, "setup", "Modified SP Metadata is: " + 
                        spmetadatamod);
                log(logLevel, "setup", "Modified SP Extended Metadata is: " + 
                        spmetadataextmod);
                
                //Remove & Import Entity with modified metadata. 
                log(logLevel, "setup", "Since SP metadata have changed, " +
                        "delete SP entity & Import it again. "); 
                HtmlPage spDeleteEntityPage = fmSP.deleteEntity(webClient, 
                        (String)configMap.get(TestConstants.KEY_SP_ENTITY_NAME), 
                        (String)configMap.get(TestConstants.KEY_SP_REALM), false, 
                        "idff");
                if (spDeleteEntityPage.getWebResponse().getContentAsString().
                        contains("deleted for entity, " +
                        configMap.get(TestConstants.KEY_SP_ENTITY_NAME))) {
                    log(logLevel, "setup", "Deleted SP entity on SP side");
                } else {
                    log(logLevel, "setup", "Couldnt delete SP entity on SP " +
                            "side" + spDeleteEntityPage.getWebResponse().
                            getContentAsString());
                    assert false;
                }  
                HtmlPage idpDeleteEntityPage = fmIDP.deleteEntity(webClient, 
                        (String)configMap.get(TestConstants.KEY_SP_ENTITY_NAME), 
                        (String)configMap.get(TestConstants.KEY_IDP_REALM), false, 
                        "idff");
                if (idpDeleteEntityPage.getWebResponse().getContentAsString().
                        contains("deleted for entity, " +
                        configMap.get(TestConstants.KEY_SP_ENTITY_NAME))) {
                    log(logLevel, "setup", "Deleted SP entity on IDP side");
                } else {
                    log(logLevel, "setup", "Couldnt delete SP entity on IDP " +
                            "side" + spDeleteEntityPage.getWebResponse().
                            getContentAsString());
                    assert false;
                }  

                Thread.sleep(9000);
                HtmlPage importSPMeta = fmSP.importEntity(webClient,
                        (String)configMap.get(TestConstants.KEY_SP_REALM), 
                        spmetadatamod, spmetadataextmod, null, "idff");
                if (!importSPMeta.getWebResponse().getContentAsString().
                        contains("Import file, web.")) {
                    log(Level.SEVERE, "setup", "Couldn't import SP " +
                            "metadata on SP side" + importSPMeta.getWebResponse().
                            getContentAsString(), null);
                } else {
                     log(logLevel, "setup", "Successfully imported modified " +
                             "SP entity on SP side", null);
                }
                spmetadataextmod = spmetadataextmod.replaceAll(
                        "hosted=\"true\"", "hosted=\"false\"");
                spmetadataextmod = spmetadataextmod.replaceAll(
                        "hosted=\"1\"", "hosted=\"0\"");
                spmetadataextmod = spmetadataextmod.replaceAll(
                        (String)configMap.get(TestConstants.KEY_SP_COT),
                        (String)configMap.get(TestConstants.KEY_IDP_COT));

                importSPMeta = fmIDP.importEntity(webClient,
                        (String)configMap.get(TestConstants.KEY_IDP_REALM), 
                        spmetadatamod, spmetadataextmod, null, "idff");
                if (!importSPMeta.getWebResponse().getContentAsString().
                        contains("Import file, web.")) {
                    log(Level.SEVERE, "setup", "Couldn't import SP " +
                            "metadata on IDP side" + importSPMeta.getWebResponse().
                            getContentAsString(), null);
                } else {
                     log(logLevel, "setup", "Successfully imported modified " +
                             "SP entity on IDP side", null);
                }
            }
        } catch (Exception e) {
            log(Level.SEVERE, "setup", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        } finally {
            consoleLogout(webClient, spurl + "/UI/Logout");
            consoleLogout(webClient, idpurl + "/UI/Logout");
        }
        exiting("setup");
    }
    
    /**
     * @DocTest: IDFF|Perform SP initiated federation.
     */
    @Test(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"})
    public void testSPInitFederation()
    throws Exception {
        entering("testSPInitFederation", null);
        try {
            log(logLevel, "testSPInitFederation", 
                    "Running: testSPInitFederation");
            getWebClient();
            log(logLevel, "testSPInitFederation", "Login to SP with " + 
                    TestConstants.KEY_SP_USER);
            consoleLogin(webClient, spurl, 
                    configMap.get(TestConstants.KEY_SP_USER),
                    configMap.get(TestConstants.KEY_SP_USER_PASSWORD));
            xmlfile = baseDir + "testspinitfederation.xml";
            getxmlSPIDFFFederate(xmlfile, configMap);
            log(logLevel, "testSPInitFederation", "Run " + xmlfile);
            task = new DefaultTaskHandler(xmlfile);
            page = task.execute(webClient);
        } catch (Exception e) {
            log(Level.SEVERE, "testSPInitFederation", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("testSPInitFederation");
    }
   
    /**
     * @DocTest: IDFF|Perform SP initiated SLO.
     */
    @Test(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"},
    dependsOnMethods={"testSPInitFederation"})
    public void testSPInitSLO()
    throws Exception {
        entering("testSPInitSLO", null);
        try {
            log(logLevel, "testSPInitSLO", "Running: testSPInitSLO");
            xmlfile = baseDir + "testspinitslo.xml";
            getxmlSPIDFFLogout(xmlfile, configMap);
            log(logLevel, "testSPInitSLO", "Run " + xmlfile);
            task = new DefaultTaskHandler(xmlfile);
            page = task.execute(webClient);
        } catch (Exception e) {
            log(Level.SEVERE, "testSPInitSLO", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("testSPInitSLO");
    }
   
    /**
     * @DocTest: IDFF|Perform SP initiated SSO.
     */
    @Test(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"},
    dependsOnMethods={"testSPInitSLO"})
    public void testSPInitSSO()
    throws Exception {
        entering("testSPInitSSO", null);
        try {
            log(logLevel, "testSPInitSSO", "Running: testSPInitSSO");
            log(logLevel, "testSPInitSSO", "Login to IDP with " + 
                    TestConstants.KEY_IDP_USER);
            consoleLogin(webClient, idpurl, 
                    configMap.get(TestConstants.KEY_IDP_USER),
                    configMap.get(TestConstants.KEY_IDP_USER_PASSWORD));
            xmlfile = baseDir + "testspinitsso.xml";
            getxmlSPIDFFSSO(xmlfile, configMap);
            log(logLevel, "testSPInitSSO", "Run " + xmlfile);
            task = new DefaultTaskHandler(xmlfile);
            page = task.execute(webClient);
        } catch (Exception e) {
            log(Level.SEVERE, "testSPInitSSO", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("testSPInitSSO");
    }
   
    /**
     * @DocTest: IDFF|Perform SP initiated Name registration.
     */
    @Test(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"},
    dependsOnMethods={"testSPInitSSO"})
    public void testSPInitNameReg()
    throws Exception {
        entering("testSPInitNameReg", null);
        try {
            log(logLevel, "testSPInitNameReg", "Running: testSPInitNameReg");
            xmlfile = baseDir + "testspinitnamereg.xml";
            getxmlSPIDFFNameReg(xmlfile, configMap);
            log(logLevel, "testSPInitNameReg", "Run " + xmlfile);
            task = new DefaultTaskHandler(xmlfile);
            page = task.execute(webClient);
        } catch (Exception e) {
            log(Level.SEVERE, "testSPInitNameReg", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("testSPInitNameReg");
    }
   
    /**
     * @DocTest: IDFF|Perform SP initiated Termination.
     */
    @Test(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"},
    dependsOnMethods={"testSPInitNameReg"})
    public void testSPInitTerminate()
    throws Exception {
        entering("testSPInitTerminate", null);
        try {
            log(logLevel, "testSPInitTerminate", 
                    "Running: testSPInitTerminate");
            xmlfile = baseDir + "testspinitterminate.xml";
            getxmlSPIDFFTerminate(xmlfile, configMap);
            log(logLevel, "testSPInitTerminate", "Run " + xmlfile);
            task = new DefaultTaskHandler(xmlfile);
            page = task.execute(webClient);
        } catch (Exception e) {
            log(Level.SEVERE, "testSPInitTerminate", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("testSPInitTerminate");
    }

    /**
     * @DocTest: IDFF|Perform IDP initiated SLO.
     */
    @Test(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"},
    dependsOnMethods={"testSPInitTerminate"})
    public void testIDPInitSLO()
    throws Exception {
        entering("testIDPInitSLO", null);
        try {
            log(logLevel, "testIDPInitSLO", "Running: testIDPInitSLO");
            xmlfile = baseDir + "testspinitfederation.xml";
            getWebClient();
            consoleLogin(webClient, spurl, 
                    configMap.get(TestConstants.KEY_SP_USER),
                    configMap.get(TestConstants.KEY_SP_USER_PASSWORD));
            getxmlSPIDFFFederate(xmlfile, configMap);
            log(logLevel, "testIDPInitSLO", "Run " + xmlfile);
            task = new DefaultTaskHandler(xmlfile);
            page = task.execute(webClient);
            xmlfile = baseDir + "testidpinitslo.xml";
            getxmlIDPIDFFLogout(xmlfile, configMap);
            log(logLevel, "testIDPInitSLO", "Run " + xmlfile);
            task = new DefaultTaskHandler(xmlfile);
            page = task.execute(webClient);
        } catch (Exception e) {
            log(Level.SEVERE, "testIDPInitSLO", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("testIDPInitSLO");
    }
   
    /**
     * @DocTest: IDFF|Perform IDP initiated Name registration.
     */
    @Test(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"},
    dependsOnMethods={"testIDPInitSLO"})
    public void testIDPInitNameReg()
    throws Exception {
        entering("testIDPInitNameReg", null);
        try {
            log(logLevel, "testIDPInitNameReg", "Running: testIDPInitNameReg");
            consoleLogin(webClient, idpurl, 
                    configMap.get(TestConstants.KEY_IDP_USER),
                    configMap.get(TestConstants.KEY_IDP_USER_PASSWORD));
            xmlfile = baseDir + "testidpinitnamereg.xml";
            getxmlIDPIDFFNameReg(xmlfile, configMap);
            log(logLevel, "testIDPInitNameReg", "Run " + xmlfile);
            task = new DefaultTaskHandler(xmlfile);
            page = task.execute(webClient);
        } catch (Exception e) {
            log(Level.SEVERE, "testIDPInitNameReg", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("testIDPInitNameReg");
    }
   
    /**
     * @DocTest: IDFF|Perform IDP initiated Termination.
     */
    @Test(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"},
    dependsOnMethods={"testIDPInitNameReg"})
    public void testIDPInitTerminate()
    throws Exception {
        entering("testIDPInitTerminate", null);
        try {
            log(logLevel, "testIDPInitTerminate", "Running: " +
                    "testIDPInitTerminate");
            xmlfile = baseDir + "testspinitterminate.xml";
            getxmlIDPIDFFTerminate(xmlfile, configMap);
            log(logLevel, "testIDPInitTerminate", "Run " + xmlfile);
            task = new DefaultTaskHandler(xmlfile);
            page = task.execute(webClient);
        } catch (Exception e) {
            log(Level.SEVERE, "testIDPInitTerminate", e.getMessage(), null);
            e.printStackTrace();
            throw e;
        }
        exiting("testIDPInitTerminate");
    }

    /**
     * This methods deletes all the users as part of cleanup
     */
    @Parameters({"ssoprofile", "sloprofile", "terminationprofile", 
    "registrationprofile"})
    @AfterClass(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"})
    public void cleanup(String strSSOProfile, String strSLOProfile, 
            String strTermProfile, String strRegProfile)
    throws Exception {
        Object[] params = {strSSOProfile, strSLOProfile, strTermProfile, 
                strRegProfile};
        entering("cleanup", params);
        Reporter.log("Cleanup parameters: " + params);
        ArrayList idList;
        try {
            log(logLevel, "cleanup", "Entering Cleanup");
            getWebClient();
            consoleLogin(webClient, spurl,
                    configMap.get(TestConstants.KEY_SP_AMADMIN_USER),
                    configMap.get(TestConstants.KEY_SP_AMADMIN_PASSWORD));
            fmSP = new FederationManager(spurl);
            idList = new ArrayList();
            idList.add(configMap.get(TestConstants.KEY_SP_USER));
            log(logLevel, "cleanup", "sp users to delete :" +
                    configMap.get(TestConstants.KEY_SP_USER));
            fmSP.deleteIdentities(webClient,
                    configMap.get(TestConstants.KEY_SP_REALM), idList,
                    "User");
            
            // Create idp users
            consoleLogin(webClient, idpurl,
                    configMap.get(TestConstants.KEY_IDP_AMADMIN_USER),
                    configMap.get(TestConstants.KEY_IDP_AMADMIN_PASSWORD));
            fmIDP = new FederationManager(idpurl);
            idList = new ArrayList();
            idList.add(configMap.get(TestConstants.KEY_IDP_USER));
            log(logLevel, "cleanup", "idp users to delete :" +
                    configMap.get(TestConstants.KEY_IDP_USER));
            fmIDP.deleteIdentities(webClient,
                    configMap.get(TestConstants.KEY_IDP_REALM), idList,
                    "User");
            
            //If any of the profile is diff than the default profile, 
            //then only delete & import the metadata. Else leave it as it is. 
            if (strSSOProfile.equals("post") || strSLOProfile.equals("soap") ||
                    strTermProfile.equals("soap") || strRegProfile.equals("soap")) {
                //Remove & Import Entity with modified metadata. 
                log(logLevel, "setup", "Since SP metadata have changed, " +
                        "delete SP entity & Import it again. "); 
                HtmlPage spDeleteEntityPage = fmSP.deleteEntity(webClient, 
                        (String)configMap.get(TestConstants.KEY_SP_ENTITY_NAME), 
                        (String)configMap.get(TestConstants.KEY_SP_REALM), false, 
                        "idff");
                if (spDeleteEntityPage.getWebResponse().getContentAsString().
                        contains("deleted for entity, " +
                        configMap.get(TestConstants.KEY_SP_ENTITY_NAME))) {
                    log(logLevel, "setup", "Deleted SP entity on SP side");
                } else {
                    log(logLevel, "setup", "Couldnt delete SP entity on SP " +
                            "side" + spDeleteEntityPage.getWebResponse().
                            getContentAsString());
                    assert false;
                }  
                HtmlPage idpDeleteEntityPage = fmIDP.deleteEntity(webClient, 
                        (String)configMap.get(TestConstants.KEY_SP_ENTITY_NAME), 
                        (String)configMap.get(TestConstants.KEY_IDP_REALM), 
                        false, "idff");
                if (idpDeleteEntityPage.getWebResponse().getContentAsString().
                        contains("deleted for entity, " +
                        configMap.get(TestConstants.KEY_SP_ENTITY_NAME))) {
                    log(logLevel, "setup", "Deleted SP entity on IDP side");
                } else {
                    log(logLevel, "setup", "Couldnt delete SP entity on IDP " +
                            "side" + spDeleteEntityPage.getWebResponse().
                            getContentAsString());
                    assert false;
                }  

                Thread.sleep(9000);
                HtmlPage importSPMeta = fmSP.importEntity(webClient,
                        (String)configMap.get(TestConstants.KEY_SP_REALM), 
                        spmetadata, spmetadataext, null, "idff");
                if (!importSPMeta.getWebResponse().getContentAsString().
                        contains("Import file, web.")) {
                    log(Level.SEVERE, "setup", "Couldn't import SP " +
                            "metadata on SP side" + importSPMeta.
                            getWebResponse().getContentAsString());
                } else {
                     log(logLevel, "setup", "Successfully imported modified " +
                             "SP entity on SP side");
                }
                
                spmetadataext = spmetadataext.replaceAll(
                        "hosted=\"true\"", "hosted=\"false\"");
                spmetadataext = spmetadataext.replaceAll(
                        "hosted=\"1\"", "hosted=\"0\"");
                spmetadataext = spmetadataext.replaceAll(
                        (String)configMap.get(TestConstants.KEY_SP_COT),
                        (String)configMap.get(TestConstants.KEY_IDP_COT));
                importSPMeta = fmIDP.importEntity(webClient,
                        (String)configMap.get(TestConstants.KEY_IDP_REALM), 
                        spmetadata, spmetadataext, null, "idff");
                if (!importSPMeta.getWebResponse().getContentAsString().
                        contains("Import file, web.")) {
                    log(Level.SEVERE, "setup", "Couldn't import SP " +
                            "metadata on IDP side" + importSPMeta.getWebResponse().
                            getContentAsString());
                } else {
                     log(logLevel, "setup", "Successfully imported modified " +
                             "SP entity on IDP side");
                }
            }
        } catch (Exception e) {
            log(Level.SEVERE, "cleanup", e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            consoleLogout(webClient, spurl + "/UI/Logout");
            consoleLogout(webClient, idpurl + "/UI/Logout");
        }
        exiting("cleanup");
    }
}