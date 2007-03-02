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
 * $Id: Main.java,v 1.1 2007-03-02 19:02:00 ak138937 Exp $
 *
 * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.identity.tools.bundles;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;

public class Main implements SetupConstants{
    
    public static void main(String[] args) {
        boolean loadConfig = false;
        String configPropertiesFile = null;
        String configPath = null;
        String currentOS = null;
        Properties configProp = null;
        ResourceBundle bundle = ResourceBundle.getBundle(System.getProperty(
            SETUP_PROPERTIES_FILE, DEFAULT_PROPERTIES_FILE));
        if (System.getProperty(CHECK_VERSION) != null) {
            if (System.getProperty(CHECK_VERSION).equals(YES)) {
                System.exit(VersionCheck.isValid(bundle));
            }
        }
        if ((System.getProperty(PRINT_HELP) != null) &&
            System.getProperty(PRINT_HELP).equals(YES)){
            SetupUtils.printUsage(bundle);
            System.exit(0);
        }
        if ((System.getProperty(CONFIG_LOAD) != null) &&
            System.getProperty(CONFIG_LOAD).equals(YES)){
            loadConfig = true;
        }
        currentOS = SetupUtils.determineOS();
        if (loadConfig) {
            configPath = System.getProperty(AMCONFIG_PATH);
            configPropertiesFile = bundle.getString(CONFIG_FILE);
            try {
                if ((configPath == null) || (configPath.length() == 0)) {
                    configPath = SetupUtils.getUserInput(bundle.getString(
                        currentOS + QUESTION));
                }
                if (!configPath.endsWith(FILE_SEPARATOR)) {
                    configPath = configPath + FILE_SEPARATOR;
                }
                if (! (new File(configPath + bundle.getString(XML_CONFIG)))
                    .exists()) {
                    System.out.println(bundle.getString("message.error.dir"));
                    System.exit(1);
                }
                configProp = SetupUtils.loadProperties(configPath +
                    configPropertiesFile);
                configProp.setProperty(USER_INPUT,
                    configPath.substring(0, configPath.length() - 1));
                configProp.setProperty(CURRENT_PLATFORM, currentOS);
            } catch (IOException ex) {
                System.out.println(bundle.getString("message.error.dir"));
                System.exit(1);
                //ex.printStackTrace();
            }
        } else {
            configProp = new Properties();
        }
        SetupUtils.evaluateBundleValues(bundle, configProp);
        try {
            SetupUtils.copyAndFilterScripts(bundle, configProp);
            if (loadConfig) {
                System.out.println(bundle.getString("message.info.version." +
                    "tools") + " " + bundle.getString(TOOLS_VERSION));
                System.out.println(bundle.getString("message.info.version.am") +
                    " " + configProp.getProperty(AM_VERSION));
            }
        } catch (IOException ex) {
            System.out.println(bundle.getString("message.error.copy"));
            System.exit(1);
            //ex.printStackTrace();
        }
    }
    
}



