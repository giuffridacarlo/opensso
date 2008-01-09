@echo off

: The contents of this file are subject to the terms
: of the Common Development and Distribution License
: (the License). You may not use this file except in
: compliance with the License.
:
: You can obtain a copy of the License at
: https://opensso.dev.java.net/public/CDDLv1.0.html or
: opensso/legal/CDDLv1.0.txt
: See the License for the specific language governing
: permission and limitations under the License.
:
: When distributing Covered Code, include this CDDL
: Header Notice in each file and include the License file
: at opensso/legal/CDDLv1.0.txt.
: If applicable, add the following below the CDDL Header,
: with the fields enclosed by brackets [] replaced by
: your own identifying information:
: "Portions Copyrighted [year] [name of copyright owner]"
:
: $Id: amverifyarchive.bat,v 1.6 2008-01-09 04:18:29 beomsuk Exp $
:
: Copyright 2006 Sun Microsystems Inc. All Rights Reserved


set TOOLS_HOME="@TOOLS_HOME@"

setlocal
:WHILE
if x%1==x goto WEND
set PARAMS=%PARAMS% %1
shift
goto WHILE
:WEND

set TOOLS_CLASSPATH=@CONFIG_DIR@;%TOOLS_HOME%/lib/amadm_setup.jar;%TOOLS_HOME%/lib/OpenDS.jar;%TOOLS_HOME%/lib/db.jar;%TOOLS_HOME%/lib/j2ee.jar;%TOOLS_HOME%/lib/ldapjdk.jar;%TOOLS_HOME%/lib/activation.jar;%TOOLS_HOME%/lib/ldapjdk.jar;%TOOLS_HOME%/lib/opensso.jar;%TOOLS_HOME%/lib/opensso-sharedlib.jar;%TOOLS_HOME%/locale;%TOOLS_HOME%/config


java -Xms64m -Xmx256m -classpath %TOOLS_CLASSPATH% -D"bootstrap.dir=@CONFIG_DIR@" -D"java.version.current=java.vm.version" -D"java.version.expected=1.4+"  -D"am.version.current=com.iplanet.am.version" -D"am.version.expected=8.0" -D"amconfig=AMConfig" -D"java.protocol.handler.pkgs=com.iplanet.services.comm" -D"max_conn_pool=10" -D"min_conn_pool=1" -D"s1is.java.util.logging.config.class=com.sun.identity.log.s1is.LogConfigReader" -D"java.util.logging.manager=com.sun.identity.log.LogManager" -D"com.iplanet.services.configpath=@CONFIG_DIR@" -D"com.sun.identity.configFilePath=@CONFIG_DIR@" -D"com.iplanet.coreservices.configpath=@CONFIG_DIR@"  -D"LOG_COMPATMODE=Off" -D"com.iplanet.am.logstatus=INACTIVE" com.sun.identity.log.cli.ISArchiveVerify %PARAMS%
endlocal
:END

