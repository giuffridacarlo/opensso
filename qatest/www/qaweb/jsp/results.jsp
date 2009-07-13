<?xml version="1.0" encoding="UTF-8"?>
<!--
   DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

   Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved

   The contents of this file are subject to the terms
   of the Common Development and Distribution License
   (the License). You may not use this file except in
   compliance with the License.

   You can obtain a copy of the License at
   https://opensso.dev.java.net/public/CDDLv1.0.html or
   opensso/legal/CDDLv1.0.txt
   See the License for the specific language governing
   permission and limitations under the License.

   When distributing Covered Code, include this CDDL
   Header Notice in each file and include the License file
   at opensso/legal/CDDLv1.0.txt.
   If applicable, add the following below the CDDL Header,
   with the fields enclosed by brackets [] replaced by
   your own identifying information:
   "Portions Copyrighted [year] [name of copyright owner]"

   $Id:
-->
<jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:ice="http://www.icesoft.com/icefaces/component">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <html id="outputHtml1">
            <head id="outputHead1">
                <ice:outputStyle id="outputStyle1" href="../resources/stylesheet.css"/>
                <ice:outputStyle id="outputStyle2" href="../xmlhttp/css/xp/xp.css"/>
            </head>
            <body id="outputBody1" style="-rave-layout: grid">
                <ice:form id="resultsForm">
                    <ice:graphicImage id="graphicImage1" value="../images/logo.gif"/>
                    <p><ice:outputText id="QATestFramwork" style=" color: silver; size: 5; font: Arial" value="OpenSSO QA Test Framework"/></p>
                    <hr style=" color: silver"/>
                    <ice:outputConnectionStatus/>
                    <ice:outputText id="outputLabel1" style=" font-weight:bolder; font-size:large" value="QA Automation Execution has Started .Please check the results after given Time"/>
                    <ice:outputText id="outputLabel2" style=" font-weight:bolder; font-size:large" value="You can close the browser"/>
                </ice:form>
            </body>
        </html>
    </f:view>
</jsp:root>
