/*
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
 * $Id: Iis7Agent.cpp,v 1.1 2009-02-13 23:58:05 robertis Exp $
 *
 *
 */

#include "Iis7Agent.h"

#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <httpserv.h>
#include <string.h>
#include <stdio.h>
#include <stdarg.h>
#include <nspr.h>

typedef struct agent_props {
    am_properties_t agent_bootstrap_props;
    am_properties_t agent_config_props;
} agent_props_t;

static agent_props_t agent_props = {
    AM_PROPERTIES_NULL
};

boolean_t agentInitialized = B_FALSE;

#define AGENT_DESCRIPTION   "Sun OpenSSO Policy Agent 3.0 for Microsoft IIS 7.0"
const CHAR agentDescription[]       = { AGENT_DESCRIPTION };
const CHAR httpProtocol[]           = "http";
const CHAR httpsProtocol[]          = "https";
const CHAR httpProtocolDelimiter[]  = "://";
// Do not change. Used to see if port number needed to reconstructing URL.
const CHAR httpPortDefault[]        = "80";
const CHAR httpsPortDefault[]       = "443";
const CHAR httpPortDelimiter[]      = ":";
const CHAR pszCrlf[]                = "\r\n";

// Responses the agent uses to requests.
typedef enum {aaDeny, aaAllow, aaLogin} tAgentAction;
tAgentConfig agentConfig;

BOOL readAgentConfigFile = FALSE;
CRITICAL_SECTION initLock;

BOOL isCdssoEnabled = FALSE;

#define RESOURCE_INITIALIZER \
    { NULL, 0, NULL, 0, AM_POLICY_RESULT_INITIALIZER }

/*
 * This is the function invoked by RegisterModule
 * when the agent module DLL is loaded at startup.
 * */
BOOL RegisterAgentModule()
{
    HMODULE nsprHandle = NULL;
    PR_Init(PR_SYSTEM_THREAD, PR_PRIORITY_NORMAL, 0);
    nsprHandle = LoadLibrary("libnspr4.dll");
    InitializeCriticalSection(&initLock);
    return TRUE;
}

/*
 *This function gets invoked at every request by OnBeginRequest.
 *
 * */
REQUEST_NOTIFICATION_STATUS ProcessRequest(IHttpContext* pHttpContext, 
                                    IHttpEventProvider* pProvider)
{
    const char* thisfunc = "ProcessRequest";
    am_status_t status = AM_SUCCESS;
    am_status_t cookieStatus = AM_SUCCESS;
    am_status_t doDenyStatus = AM_SUCCESS;
    REQUEST_NOTIFICATION_STATUS retStatus = RQ_NOTIFICATION_CONTINUE; 
    string requestURL;
    string pathInfo;
    PCSTR reqMethod = NULL;
    char* requestMethod = NULL;
    DWORD requestMethodSize = 0;
    PCSTR requestClientIP = NULL;
    DWORD requestClientIPSize = 0;
    CHAR* orig_req_method = NULL;
    CHAR* query = NULL;
    CHAR* dpro_cookie = NULL;
    BOOL isLocalAlloc = FALSE;
    BOOL redirectRequest = FALSE;
    am_map_t env_parameter_map = NULL;
    tOphResources OphResources = RESOURCE_INITIALIZER;
    tOphResources* pOphResources = &OphResources;
    CHAR *set_cookies_list = NULL;
    CHAR *set_headers_list = NULL;
    CHAR *request_hdrs = NULL;
    char* logout_url = NULL;
    DWORD returnValue = 1;
    CHAR *tmpPecb = NULL;
    void *args[] = {(void *) tmpPecb, (void *) &set_headers_list,
                    (void *) &set_cookies_list, (void *) &request_hdrs };

    void *agent_config=NULL;
    IHttpRequest* req = pHttpContext->GetRequest();
    IHttpResponse* res = pHttpContext->GetResponse();


    if (readAgentConfigFile == FALSE) {
        EnterCriticalSection(&initLock);
        if (readAgentConfigFile == FALSE) {
            loadAgentPropertyFile(pHttpContext);
            readAgentConfigFile = TRUE;
        }
        LeaveCriticalSection(&initLock);
    }

    if(agentInitialized != B_TRUE){
        EnterCriticalSection(&initLock);
        if(agentInitialized != B_TRUE){
            am_web_log_debug("ProcessRequest: Will call init");
            init_at_request(); 
            if(agentInitialized != B_TRUE){
                am_web_log_error("ProcessRequest: Agent intialization failed.");
                do_deny(pHttpContext);
                retStatus = RQ_NOTIFICATION_FINISH_REQUEST;
                return retStatus;
            }  else {
                am_web_log_debug("ProcessRequest: Agent intialized");
            }
        }
        LeaveCriticalSection(&initLock);
    }

    agent_config = am_web_get_agent_configuration();

    if ((am_web_is_cdsso_enabled(agent_config) == B_TRUE)){
        isCdssoEnabled = TRUE;
    }


    req->SetHeader("Cache-Control","no-cache",strlen("no-cache"),TRUE);
    res->SetHeader("Cache-Control","no-store",strlen("no-store"),TRUE);

    res->DisableKernelCache(9);


    status = get_request_url(pHttpContext,requestURL, pathInfo, pOphResources);

    if ((status == AM_SUCCESS) && (B_TRUE == am_web_is_notification(requestURL.c_str(), agent_config)))
    { 
        string data="";
        GetEntity(pHttpContext, data);
        am_web_handle_notification(data.c_str(), data.size(), agent_config);
        OphResourcesFree(pOphResources);
        retStatus = RQ_NOTIFICATION_FINISH_REQUEST;
        return retStatus;
    }

    // Get the request method
    status = GetVariable(pHttpContext,"REQUEST_METHOD", &reqMethod, &requestMethodSize);

    // Check for SSO Token in Http Cookie
    if (status == AM_SUCCESS) 
    {
        if(requestMethodSize >0)
        {
            requestMethod = (char*)malloc(requestMethodSize+1);
            memset(requestMethod, 0, requestMethodSize+1);
            strncpy(requestMethod, (char*)reqMethod, requestMethodSize);
        }
        am_web_log_debug("%s: requestMethod = %s",thisfunc, requestMethod);
        // Get the HTTP_COOKIE header
        CHAR* cookieValue = NULL;    
        int length = 0;
        int i = 0;
        cookieStatus = GetVariable(pHttpContext,"HTTP_COOKIE", 
                &pOphResources->cookies, &pOphResources->cbCookies);

        if ((cookieStatus == AM_SUCCESS)  &&  (pOphResources->cbCookies > 0)) 
        {
            const char *cookieName = am_web_get_cookie_name(agent_config);

            // Look for the iPlanetDirectoryPro cookie
            if (cookieName != NULL) {
                cookieValue = strstr((char *)(pOphResources->cookies), cookieName);
                while (cookieValue) {
                    char *marker = strstr(cookieValue+1, cookieName);
                    if (marker) {
                        cookieValue = marker;
                    } else {
                        break;
                    }
                }
                if (cookieValue != NULL) {
                    cookieValue = strchr(cookieValue ,'=');
                    cookieValue = &cookieValue[1]; // 1 vs 0 skips over '='
                    // find the end of the cookie
                    length = 0;
                    for (i=0;(cookieValue[i] != ';') &&
                              (cookieValue[i] != '\0'); i++) {
                        length++;
                    }
                    cookieValue[length]='\0';
                    if (length < URL_SIZE_MAX-1) {
                        if (length > 0) {
                            dpro_cookie = (CHAR *) malloc(length+1);
                            if (dpro_cookie != NULL) {
                                strncpy(dpro_cookie, cookieValue, length);
                                dpro_cookie[length] = '\0';
                                isLocalAlloc = TRUE;
                                am_web_log_debug("%s: SSO token found in "
                                    " cookie header.", thisfunc);
                            } 
                            else {
                                am_web_log_error("%s: Unable to allocate memory"
                                    " for cookie, size = %u", thisfunc, length);
                                cookieStatus = AM_NO_MEMORY;
                            }
                        }
                    }
                }
            }
        }

    }


    //  Get the remote address.
    if (status == AM_SUCCESS) {
        status = GetVariable(pHttpContext,"REMOTE_ADDR", 
                            &requestClientIP, &requestClientIPSize);
    }

    //  process post data in CDSSO
    if (status == AM_SUCCESS) 
    {
        //In CDSSO mode, check if the sso token is in the post data
        if ((am_web_is_cdsso_enabled(agent_config) == B_TRUE) && 
                (strcmp(requestMethod, REQUEST_METHOD_POST) == 0)) 
        {
            string reqClientIP= requestClientIP;

            if ((dpro_cookie == NULL) && 
                (am_web_is_url_enforced(requestURL.c_str(), pathInfo.c_str(), 
                reqClientIP.c_str(), agent_config) == B_TRUE)) 
            {

                string response = "";
                GetEntity(pHttpContext, response);
                if (status == AM_SUCCESS) {
                        //Set original method to GET
                        orig_req_method = strdup(REQUEST_METHOD_GET);
                        if (orig_req_method != NULL) {
                            am_web_log_debug("%s: Request method set to GET.", 
                                    thisfunc);
                        } else {
                            am_web_log_error("%s: Not enough memory to ", 
                                    "allocate orig_req_method.", thisfunc);
                            status = AM_NO_MEMORY;
                        }
                    pHttpContext->GetRequest()->SetHttpMethod(orig_req_method);

                    if (status == AM_SUCCESS) {
                        if(dpro_cookie != NULL) {
                            free(dpro_cookie);
                            dpro_cookie = NULL;
                        }
                        char* req_url= new char [requestURL.size()+1];
                        strcpy(req_url,requestURL.c_str());

                        status = am_web_check_cookie_in_post(args, &dpro_cookie, 
                                &req_url, &orig_req_method, requestMethod,
                                (char*)response.c_str(), B_FALSE, set_cookie, 
                                set_method, agent_config);
                        if (status == AM_SUCCESS) {
                            requestURL = req_url;
                            if(req_url != NULL) {
                                delete [] req_url;
                                req_url = NULL;
                            }
                            isLocalAlloc = FALSE;
                            am_web_log_debug("%s: SSO token found in "
                                             "assertion.",thisfunc);
                                redirectRequest = TRUE;
                        } else {
                            am_web_log_debug("%s: SSO token not found in "
                                   "assertion. Redirecting to login page.",
                                   thisfunc);
                            status = AM_INVALID_SESSION;
                        }
                    }
                }
            }
        }
    }

    //  Create env map
    if (status == AM_SUCCESS) {
        am_web_log_debug("%s: requestClientIP = %s",
                        thisfunc, requestClientIP);

        status = am_map_create(&env_parameter_map);
        am_web_log_debug("%s: status after "
                        "am_map_create = %s (%d)",thisfunc,
                        am_status_to_string(status),
                        status);
    }

    if (status == AM_SUCCESS) {
        //Check if the user is authorized to access the resource
        status = am_web_is_access_allowed(dpro_cookie, requestURL.c_str(),
                                        pathInfo.c_str(), requestMethod,
                                        (char *)requestClientIP,
                                        env_parameter_map,
                                        &OphResources.result,
                                        agent_config);

        am_web_log_debug("%s: status after "
                         "am_web_is_access_allowed = %s (%d)",thisfunc,
                         am_status_to_string(status), status);
        am_map_destroy(env_parameter_map);
    }

    //  Check for status and proceed accordingly
    switch(status) {
        case AM_SUCCESS:
            if (am_web_is_logout_url(requestURL.c_str(),agent_config) == B_TRUE)
            {
                (void)am_web_logout_cookies_reset(reset_cookie,args,agent_config);
            }

            status = am_web_result_attr_map_set(&OphResources.result,
                                        set_header, set_cookie_in_response,
                                        set_header_attr_as_cookie,
                                        get_cookie_sync, args, agent_config);
            if (status == AM_SUCCESS) {
                if ((set_headers_list != NULL) || (set_cookies_list != NULL) 
                        || (redirectRequest == TRUE)) {


                    //the following function also invokes set_headers_in_context() 
                    //to set all the headers in the httpContext.
                    status = set_request_headers(pHttpContext, args);
                }
            }
            if (status == AM_SUCCESS) {
                if (set_cookies_list != NULL && strlen(set_cookies_list) > 0) 
                {

                    //this call sets only cookies
                    set_headers_in_context(pHttpContext, set_cookies_list, FALSE);
                }

                //now set remote user
                if (pOphResources->result.remote_user != NULL) 
                {
                    const char * ruser = pOphResources->result.remote_user;
                    wchar_t *remoteUser = (wchar_t *)pHttpContext->
                        AllocateRequestMemory((strlen(ruser)+1) * sizeof(wchar_t));
                    mbstowcs( remoteUser, ruser, strlen(ruser) + 1);
                    pHttpContext->SetServerVariable("REMOTE_USER", remoteUser);

                }

                if (redirectRequest == TRUE) 
                {
                    am_web_log_debug("%s: Request redirected to orignal url "
                            "after return from CDC servlet",thisfunc);
                    retStatus = redirect_to_request_url(pHttpContext, 
                            requestURL.c_str(), request_hdrs);
                    return retStatus;
                } 
                else 
                {
                    //let the webserver handle the request
                    //now that the session is validated.
                    return RQ_NOTIFICATION_CONTINUE; 
                }
            }
            if (set_cookies_list != NULL) {
                free(set_cookies_list);
                set_cookies_list = NULL;
            }
            break;

        case AM_INVALID_SESSION:
            am_web_log_info("%s: Invalid session.",thisfunc);
            am_web_do_cookies_reset(reset_cookie, args, agent_config);
            returnValue =do_redirect(pHttpContext, status, &OphResources.result,
                         requestURL.c_str(), requestMethod, args, agent_config);
            break;

        case AM_ACCESS_DENIED:
            am_web_log_info("%s: Access denied to %s",thisfunc,
                            OphResources.result.remote_user ?
                            OphResources.result.remote_user : "unknown user");
            returnValue = do_redirect(pHttpContext, status, &OphResources.result,
                              requestURL.c_str(), requestMethod, 
                              args, agent_config);
            break;

        case AM_INVALID_FQDN_ACCESS:
            am_web_log_info("%s: Invalid FQDN access",thisfunc);
            returnValue = do_redirect(pHttpContext, status, &OphResources.result,
                              requestURL.c_str(), requestMethod, 
                              args, agent_config);
            break;

        case AM_REDIRECT_LOGOUT:
            status = am_web_get_logout_url(&logout_url, agent_config);
            if(status == AM_SUCCESS)
            {
                //reset the cookie in the agent domain
                PCSTR pcHeader = "Set-Cookie";
                char * tmpVal = "=;Max-Age=300;Path=/";
                const char *cookie_name = am_web_get_cookie_name(agent_config);
                PCSTR pcValue = (PCSTR)pHttpContext->
                    AllocateRequestMemory(strlen(cookie_name)+ strlen(tmpVal)+1);
                strcpy((char*)pcValue,cookie_name);
                strcat((char*)pcValue,tmpVal);
                strcat((char*)pcValue,"\0");
                res->SetHeader(pcHeader, pcValue, (USHORT)strlen(pcValue),FALSE);
                res->Redirect(logout_url, true, false);
            }
            else
            {
                am_web_log_debug("validate_session_policy(): "
                    "am_web_get_logout_url failed. ");
                return RQ_NOTIFICATION_FINISH_REQUEST;
            }
        break;

        case AM_INVALID_ARGUMENT:
        case AM_NO_MEMORY:
        case AM_FAILURE:
        default:
            am_web_log_error("%s: status: %s (%d)",thisfunc,
                              am_status_to_string(status), status);

            HRESULT hr = res->SetStatus(500,"Internal Server Error", 0, S_OK);
            if (FAILED(hr))
            {
                am_web_log_error("%s: Cannot set status to 500 .",thisfunc);
            }
            break;
    }

    if (requestMethod != NULL) {
        free(requestMethod);
        requestMethod = NULL;
    }

    if (request_hdrs != NULL) {
        free(request_hdrs);
        request_hdrs = NULL;
    }

    return retStatus;
}

/*
 * This function loads the bootstrap and the configuration files and 
 * invokes am_web_init.
 *
 * */
BOOL loadAgentPropertyFile(IHttpContext* pHttpContext)
{
    BOOL gotInstanceId = FALSE;
    PCSTR instanceId = NULL;
    DWORD instanceIdSize = 0;
    CHAR* agent_bootstrap_file  = NULL;
    am_status_t status = AM_SUCCESS;
    am_status_t polsPolicyStatus = AM_SUCCESS;
    BOOL statusContinue      = FALSE;
    CHAR debugMsg[2048]   = "";
    char *agent_config_file = NULL;    

    agentConfig.bAgentInitSuccess = FALSE; 

    if(S_OK == (pHttpContext->GetServerVariable("INSTANCE_ID", &instanceId, 
                    &instanceIdSize)))
    {
        instanceId = (PCSTR)pHttpContext->AllocateRequestMemory(instanceIdSize+1);
        if(instanceId == NULL)
        {
            sprintf(debugMsg, "%d: Could not allocate memory", instanceIdSize);
            status = AM_NO_MEMORY;
        }
        else
        {
            if(S_OK != (pHttpContext->GetServerVariable("INSTANCE_ID", &instanceId, 
                            &instanceIdSize)))
            {
                sprintf(debugMsg, "%d: Invalid Instance Id ", instanceIdSize);
                status = AM_FAILURE;
            }
        }
    }
    else
    {
        sprintf(debugMsg, "GetServerVariable failed.");
        status = AM_FAILURE;
    }


    if (status == AM_SUCCESS) {
        string instanceIdStr = instanceId;


        BOOL getPath = iisaPropertiesFilePathGet(&agent_bootstrap_file, 
                instanceIdStr, TRUE);

        if (getPath == FALSE) {
            sprintf(debugMsg,"%s: iisaPropertiesFilePathGet() failed.", 
                    agentDescription);
            logPrimitive(debugMsg);
            free(agent_bootstrap_file);
            agent_bootstrap_file = NULL;
            SetLastError(IISA_ERROR_PROPERTIES_FILE_PATH_GET);
            return FALSE;
        }


    }

    if (iisaPropertiesFilePathGet(&agent_config_file, instanceId, FALSE)
                                         == FALSE) {
        sprintf(debugMsg, "%s: iisaPropertiesFilePathGet() returned failure",
                agentDescription);
        logPrimitive(debugMsg);
        free(agent_config_file);
        agent_config_file = NULL;
        SetLastError(IISA_ERROR_PROPERTIES_FILE_PATH_GET);
        return FALSE;
    }

    polsPolicyStatus = am_web_init(agent_bootstrap_file, agent_config_file);
    free(agent_bootstrap_file);
    agent_bootstrap_file = NULL;
    free(agent_config_file);
    agent_config_file = NULL;

    if (AM_SUCCESS != polsPolicyStatus) {
        // Use logPrimitive() AND am_web_log_error() here since a policy_init()
        //   failure could mean am_web_log_error() isn't initialized.
        sprintf(debugMsg,
            "%s: Initialization of the agent failed: status = %s (%d)",
            agentDescription, am_status_to_string(polsPolicyStatus),
            polsPolicyStatus);
        logPrimitive(debugMsg);
        SetLastError(IISA_ERROR_INIT_POLICY);
        return FALSE;
    }

    // Record success initializing agent.
    agentConfig.bAgentInitSuccess = TRUE;
    return TRUE;
}

/*
 * This function retirieves the location of config files from the registry and 
 * returns the complete path of the files.
 *
 * */
BOOL iisaPropertiesFilePathGet(CHAR** propertiesFileFullPath, string instanceId,
        BOOL isBootStrapFile)
{
    // Max WINAPI path
    const DWORD dwPropertiesFileFullPathSize = MAX_PATH + 1;
    CHAR szPropertiesFileName[1000];
    string agentApplicationSubKey = "Software\\Sun Microsystems\\OpenSSO IIS7 Agent\\Identifier_";
    const CHAR agentDirectoryKeyName[]       = "Path";
    DWORD dwPropertiesFileFullPathLen        = dwPropertiesFileFullPathSize;
    HKEY hKey                                = NULL;
    LONG lRet                                = ERROR_SUCCESS;
    CHAR debugMsg[2048]                      = "";


    if(isBootStrapFile) {
        strcpy(szPropertiesFileName,"OpenSSOAgentBootstrap.properties");
    }
    else {
        strcpy(szPropertiesFileName,"OpenSSOAgentConfiguration.properties");
    }

    if(!instanceId.empty()) {
        agentApplicationSubKey.append(instanceId);
    }
    ///////////////////////////////////////////////////////////////////
    //  get the location of the properties file from the registry
    lRet = RegOpenKeyEx(HKEY_LOCAL_MACHINE, agentApplicationSubKey.c_str(),
                        0, KEY_READ, &hKey);


    if(lRet != ERROR_SUCCESS) {
        sprintf(debugMsg,
                "%s(%d) Opening registry key %s%s failed with error code %d",
                __FILE__, __LINE__, "HKEY_LOCAL_MACHINE\\",
                agentApplicationSubKey, lRet);
        logPrimitive(debugMsg);
        return FALSE;
    }

    // free'd by caller, even when there's an error.
    *propertiesFileFullPath = (CHAR*) malloc(dwPropertiesFileFullPathLen);
    if (*propertiesFileFullPath == NULL) {
        sprintf(debugMsg,
              "%s(%d) Insufficient memory for propertiesFileFullPath %d bytes",
             __FILE__, __LINE__, dwPropertiesFileFullPathLen);
        logPrimitive(debugMsg);
        return FALSE;
    }
    lRet = RegQueryValueEx(hKey, agentDirectoryKeyName, NULL, NULL,
                           (LPBYTE)*propertiesFileFullPath,
                           &dwPropertiesFileFullPathLen);
    string filePath = *propertiesFileFullPath;
    if (lRet != ERROR_SUCCESS || *propertiesFileFullPath == NULL ||
        (*propertiesFileFullPath)[0] == '\0') {
        sprintf(debugMsg,
          "%s(%d) Reading registry value %s\\%s\\%s failed with error code %d",
          __FILE__, __LINE__,
          "HKEY_LOCAL_MACHINE\\", agentApplicationSubKey,
          agentDirectoryKeyName, lRet);
        logPrimitive(debugMsg);
        return FALSE;
    }
    if (*propertiesFileFullPath &&
        (**propertiesFileFullPath == '\0')) {
        sprintf(debugMsg,
                "%s(%d) Properties file directory path is NULL.",
                __FILE__, __LINE__);
        logPrimitive(debugMsg);
        return FALSE;
    }
    if (*(*propertiesFileFullPath + dwPropertiesFileFullPathLen - 1) !=
        '\0') {
        sprintf(debugMsg,
             "%s(%d) Properties file directory path missing NULL termination.",
             __FILE__, __LINE__);
        logPrimitive(debugMsg);
        return FALSE;
    }
    // closes system registry
    RegCloseKey(hKey);
    if ((strlen(*propertiesFileFullPath) + 2 /* size of \\ */ +
         strlen(szPropertiesFileName) + 1) > dwPropertiesFileFullPathSize) {
        sprintf(debugMsg,
              "%s(%d) Properties file directory path exceeds Max WINAPI path.",
              __FILE__, __LINE__);
        logPrimitive(debugMsg);
        return FALSE;
    }
    strcat(*propertiesFileFullPath, "\\");
    strcat(*propertiesFileFullPath, szPropertiesFileName);
    filePath = *propertiesFileFullPath;
    return TRUE;
}


/*
 * Logs into the event viewer logs as Application Error. 
 * This is used when the policy agent logging is not initialized yet.
 *
 * */
void logPrimitive(CHAR *message)
{
    HANDLE hes        = NULL;
    const CHAR* rsz[] = {message};

    if (message == NULL) {
    return;
    }

    hes = RegisterEventSource(0, agentDescription);
    if (hes) {
        ReportEvent(hes, EVENTLOG_ERROR_TYPE, 0, 0, 0, 1, 0, rsz, 0);
        DeregisterEventSource(hes);
    }
}

/*
 *Invoked when the agent module DLL is unloaded at shutdown.
 *
 * */
void TerminateAgent()
{
    am_status_t status = am_web_cleanup();
    DeleteCriticalSection(&initLock);
}

/*
 * Retrieves the complete request URL in the context.
 *
 * */
am_status_t get_request_url(IHttpContext* pHttpContext,
                      string& requestURL, string& pathInfo,
                      tOphResources* pOphResources)
{
    const char *thisfunc = "get_request_url()";

    PCSTR requestHostHeader = NULL;
    DWORD requestHostHeaderSize	= 0;
    BOOL gotRequestHost = FALSE;

    const CHAR* requestProtocol = NULL;
    PCSTR requestProtocolType  = NULL;
    DWORD requestProtocolTypeSize = 0;
    BOOL  gotRequestProtocol = FALSE;

    CHAR  defaultPort[TCP_PORT_ASCII_SIZE_MAX + 1] = "";
    PCSTR  requestPort = NULL;
    DWORD requestPortSize = 0;
    BOOL  gotRequestPort = FALSE;

    PCSTR queryString = NULL;
    DWORD queryStringSize = 0;
    BOOL  gotQueryString = FALSE;

    PCSTR baseUrl = NULL;
    const char* colon_ptr = NULL;
    DWORD baseUrlLength = 0;
    BOOL  gotUrl = FALSE;
    
    PCSTR path_info = NULL;
    DWORD pathInfoSize = 0;
    BOOL gotPathInfo = FALSE;
    CHAR* newPathInfo = NULL;

    BOOL gotScriptName = FALSE;
    PCSTR scriptName = NULL;
    DWORD scriptNameSize = 0;
    
    am_status_t status = AM_SUCCESS;

    // Get the protocol type (HTTP / HTTPS)
    status = GetVariable(pHttpContext,"HTTPS", &requestProtocolType, 
                                &requestProtocolTypeSize);

    if (status == AM_SUCCESS) 
    {
        am_web_log_debug("%s: requestProtocolType = %s", thisfunc, requestProtocolType);
        if(strncmp(requestProtocolType,"on", 2) == 0) {
            requestProtocol = httpsProtocol;
            strcpy(defaultPort, httpsPortDefault);
        } else if(strncmp(requestProtocolType,"off", 3) == 0) {
            requestProtocol = httpProtocol;
            strcpy(defaultPort, httpPortDefault);
        }

        // Get the host name
        status = GetVariable(pHttpContext,"HEADER_Host", 
                        &requestHostHeader, &requestHostHeaderSize);

    }

    if ((status == AM_SUCCESS) && (requestHostHeader != NULL)) 
    {
        am_web_log_debug("%s: HEADER_Host = %s", thisfunc, requestHostHeader);
        colon_ptr = strchr(requestHostHeader, ':');
        if (colon_ptr != NULL) {
            requestPort = (PCSTR)pHttpContext->
                                AllocateRequestMemory((strlen(colon_ptr)) + 1 );
            strncpy((char *)requestPort, colon_ptr + 1, strlen(colon_ptr)-1);
        } 
        else{
            status=GetVariable(pHttpContext,"SERVER_PORT", &requestPort, 
                                        &requestPortSize);
        }
    }


    if (status == AM_SUCCESS) {
        am_web_log_debug("%s: SERVER_PORT = %s", thisfunc, requestPort);

        pOphResources->cbUrl = strlen(requestProtocol)          +
                               strlen(httpProtocolDelimiter)    +
                               strlen(requestHostHeader)        +
                               strlen(httpPortDelimiter)        +
                               strlen(requestPort)              +
                               URL_SIZE_MAX;
        pOphResources->url = (CHAR *) malloc(pOphResources->cbUrl);
        if (pOphResources->url == NULL) {
            am_web_log_error("%s: Not enough memory pOphResources->cbUrl", thisfunc);
            status = AM_NO_MEMORY;
        }
    }

    if (status == AM_SUCCESS) {
        strcpy(pOphResources->url, requestProtocol);
        strcat(pOphResources->url, httpProtocolDelimiter);
        strcat(pOphResources->url, requestHostHeader);

        // Add the port number if it's not the default HTTP(S) port and
        // there's no port delimiter in the Host: header indicating
        // that the port is not present in the Host: header.
        if (strstr(requestHostHeader, httpPortDelimiter) == NULL) {
            if (strcmp(requestPort, defaultPort) != 0) {
                strcat(pOphResources->url, httpPortDelimiter);
                strcat(pOphResources->url, requestPort);
            // following 2 "else if" were added based on
            // instruction that port number has to be added for IIS
            } else if (strcmp(requestProtocol, httpProtocol) == 0) {
                strcat(pOphResources->url, httpPortDelimiter);
                strcat(pOphResources->url, httpPortDefault);
            } else if (strcmp(requestProtocol, httpsProtocol) == 0) {
                strcat(pOphResources->url, httpPortDelimiter);
                strcat(pOphResources->url, httpsPortDefault);
            }
        }

        //Get the base url
        status = GetVariable(pHttpContext,"URL", &baseUrl, &baseUrlLength);

    }

    if (status == AM_SUCCESS) 
    {
        am_web_log_debug("%s: URL = %s", thisfunc, baseUrl);
        // Get the path info .
        status = GetVariable(pHttpContext,"PATH_INFO", &path_info, 
                                        &pathInfoSize);
    }

    if (status == AM_SUCCESS) {
        am_web_log_debug("%s: PATH_INFO = %s", thisfunc, path_info);

        // Get the script name
        status = GetVariable(pHttpContext,"SCRIPT_NAME", &scriptName, 
                                                    &scriptNameSize);
    }

    if (status == AM_SUCCESS) {
        am_web_log_debug("%s: SCRIPT_NAME = %s",thisfunc, scriptName);
        
        //Remove the script name from path_info to get the real path info
        if (path_info != NULL && scriptName != NULL) {
            path_info += strlen(scriptName);
            newPathInfo = strdup(path_info);
            if (newPathInfo != NULL) {
                pathInfo = newPathInfo;
                am_web_log_debug("%s: Reconstructed path info = %s", thisfunc, pathInfo );
            } else {
               am_web_log_error("%s: Unable to allocate newPathInfo.", thisfunc);
               status = AM_NO_MEMORY;
            }
        }
    }
    
    if (status == AM_SUCCESS) {
        strcat(pOphResources->url, baseUrl);
        // Add the path info to the base url
        if ((newPathInfo != NULL) && (strlen(newPathInfo) > 0)) {
            strcat(pOphResources->url, newPathInfo);
        }

        // Get the query string
        status = GetVariable(pHttpContext,"QUERY_STRING", &queryString, 
                                &queryStringSize);
        if(queryString != NULL)
        {
            if(strlen((char*)queryString) > 0)
            {
                strcat(pOphResources->url, "?");
                strcat(pOphResources->url, queryString);
                string qrystr= (char*)queryString;
            }
        }
        requestURL = pOphResources->url;
        if (!requestURL.empty()) {
           am_web_log_debug("%s: Constructed request url = %s", thisfunc, requestURL.c_str());
        }
    }

    return status;
}


/*
 * Retrives the server variables using GetServerVariable.
 *
 * */
am_status_t GetVariable(IHttpContext* pHttpContext, PCSTR varName, 
                            PCSTR* pVarVal, DWORD* pVarValSize) 
{
    const char* thisfunc = "GetVariable";
    am_status_t status = AM_SUCCESS;

    if(S_OK == (pHttpContext->GetServerVariable(varName, pVarVal, pVarValSize)))
    {
        *pVarVal = (PCSTR)pHttpContext->AllocateRequestMemory((*pVarValSize)+1);
        if(pVarVal == NULL)
        {
            am_web_log_error("%s: Could not allocate memory", thisfunc);
            status = AM_NO_MEMORY;
        }
        else
        {
            if(S_OK != (pHttpContext->GetServerVariable(varName, pVarVal, pVarValSize)))
            {
                am_web_log_error("%s: Invalid Server Variable %s", thisfunc,pVarVal);
                status = AM_FAILURE;
            }
            else
            {
                am_web_log_debug("%s: Server Variable received %s", thisfunc, *pVarVal);
                if (pVarVal != NULL){
                    status = AM_SUCCESS;
                }
            }
        }
    }
    else
    {
        am_web_log_error("%s: GetVariable failed.", thisfunc);
        status = AM_FAILURE;
    }

    return status;

}

void OphResourcesFree(tOphResources* pOphResources)
{
    if (pOphResources->url != NULL) {
        free(pOphResources->url);
        pOphResources->url       = NULL;
        pOphResources->cbUrl        = 0;
    } 
    //cookies are not freed because they are allocated
    //by httpContext.

    am_web_clear_attributes_map(&pOphResources->result);
    am_policy_result_destroy(&pOphResources->result);
    return;
}


/*
 * Retrieves entity data from the request.
 *
 * */
void GetEntity(IHttpContext* pHttpContext, string& data)
{
    HRESULT hr;
    IHttpRequest* pHttpRequest = pHttpContext->GetRequest();
    DWORD cbBytesReceived = pHttpRequest->GetRemainingEntityBytes();
    int cb = (int)cbBytesReceived;
    void * pvRequestBody = pHttpContext->AllocateRequestMemory(cbBytesReceived);
    void * entityBody;
    data.clear();

    if (cbBytesReceived > 0)
    {
        while (pHttpRequest->GetRemainingEntityBytes() != 0)
        {
            hr = pHttpRequest->ReadEntityBody(pvRequestBody,cbBytesReceived,false,
                                                    &cbBytesReceived,NULL);
            if (FAILED(hr)) {
                return;
            }
            data.append((char*)pvRequestBody,(int)cbBytesReceived);
        }
    }

    //set it back in the request
    entityBody = pHttpContext->AllocateRequestMemory(data.length());
    strcpy((char*)entityBody,data.c_str());
    pHttpRequest->InsertEntityBody(entityBody,strlen((char*)entityBody));

}


/*
 * This function is invoked in CDSSO when the cookie is set in the agent's domain.
 * The one set by the browser in the server domain is meaningless.
 * Invoked by am_web_check_cookie_in_post. It constructs the cookie data 
 * from the LARES post data and set it as a cookie here.
 * */
static am_status_t set_cookie(const char *header, void **args)
{
     am_status_t status = AM_SUCCESS;
     CHAR** ptr = NULL;
     CHAR* set_cookies_list = NULL;

     if (header != NULL && args != NULL ) {
#if defined(_AMD64_)
        size_t cookie_length = 0;
#else
        int cookie_length = 0;
#endif
        char* cdssoCookie = NULL;
        char* tmpStr = NULL;

        ptr = (CHAR **) args[2];
        set_cookies_list = *ptr;

        cookie_length = strlen("Set-Cookie:") + strlen(header)
                                            + strlen("\r\n");
        cdssoCookie = (char *) malloc(cookie_length + 1);

        if (status == AM_SUCCESS) {
            if (cdssoCookie != NULL) {
                sprintf(cdssoCookie, "Set-Cookie:%s\r\n", header);

                if (set_cookies_list == NULL) {
                    set_cookies_list = (char *) malloc(cookie_length + 1);
                    if (set_cookies_list != NULL) {
                        memset(set_cookies_list, 0, sizeof(char) *
                                                        cookie_length + 1);
                        strcpy(set_cookies_list, cdssoCookie);
                    } else {
                        am_web_log_error("set_cookie():Not enough memory 0x%x "
                                                    "bytes.",cookie_length + 1);
                        status = AM_NO_MEMORY;
                    }
                } else {
                    tmpStr = set_cookies_list;
                    set_cookies_list = (char *) malloc(strlen(tmpStr) +
                                                            cookie_length + 1);
                    if (set_cookies_list == NULL) {
                        am_web_log_error("set_cookie():Not enough memory 0x%x "
                                                    "bytes.",cookie_length + 1);
                        status = AM_NO_MEMORY;
                    } else {
                        memset(set_cookies_list,0,sizeof(set_cookies_list));
                        strcpy(set_cookies_list,tmpStr);
                        strcat(set_cookies_list,cdssoCookie);
                    }
                }
                free(cdssoCookie);

                if (tmpStr) {
                    free(tmpStr);
                    tmpStr = NULL;
                }
            } else {
                am_web_log_error("set_cookie():Not enough memory 0x%x bytes.",
                                                            cookie_length + 1);
                status = AM_NO_MEMORY;
            }
        }
    } else {
        am_web_log_error("set_cookie(): Invalid arguments obtained");
        status = AM_INVALID_ARGUMENT;
    }

    if (set_cookies_list && set_cookies_list[0] != '\0') {
        am_web_log_info("set_cookie():set_cookies_list = %s", set_cookies_list);
        *ptr = set_cookies_list;
    }

    return status;
}

/*
 * Not implemented.
 *
 * */
static void set_method(void ** args, char * orig_req)
{
}

// Function to reset all the cookies before redirecting to AM
static am_status_t reset_cookie(const char *header, void **args)
{
   am_status_t status = AM_SUCCESS;

   if (header != NULL && args != NULL) {

#if defined(_AMD64_)
        size_t reset_cookie_length = 0;
#else
        int reset_cookie_length = 0;
#endif
        char *resetCookie = NULL;
        char *tmpStr = NULL;
        CHAR* set_cookies_list = NULL;
        CHAR** ptr = NULL;

        ptr = (CHAR **) args[2];
        set_cookies_list = *ptr;

        reset_cookie_length = strlen("Set-Cookie:") + strlen(header)
                                                  + strlen("\r\n");
        resetCookie = (char *) malloc(reset_cookie_length + 1);


        if (status == AM_SUCCESS) {
          if (resetCookie != NULL) {
             memset(resetCookie, 0, sizeof(char) * reset_cookie_length + 1);
             sprintf(resetCookie, "Set-Cookie:%s\r\n", header);

             if (set_cookies_list == NULL) {
               set_cookies_list = (char *) malloc(reset_cookie_length + 1);
               if (set_cookies_list != NULL) {
                   memset(set_cookies_list, 0, sizeof(char) *
                                        reset_cookie_length + 1);
                   strcpy(set_cookies_list, resetCookie);
               } else {
               am_web_log_error("reset_cookie():Not enough memory 0x%x bytes.",
                                reset_cookie_length + 1);
                status = AM_NO_MEMORY;
               }
             } else {
                 tmpStr = set_cookies_list;
                 set_cookies_list = (char *) malloc(strlen(tmpStr) +
                                     reset_cookie_length + 1);
                 if (set_cookies_list == NULL) {
                   am_web_log_error("reset_cookie():Not enough memory 0x%x "
                            "bytes.", reset_cookie_length + 1);
                   status = AM_NO_MEMORY;
                 } else {
                     memset(set_cookies_list, 0, sizeof(set_cookies_list));
                     strcpy(set_cookies_list, tmpStr);
                     strcat(set_cookies_list, resetCookie);
                 }
             }
         am_web_log_debug("reset_cookie(): set_cookies_list ==> %s",
                               set_cookies_list);
         free(resetCookie);

         if (tmpStr != NULL) {
            free(tmpStr);
         }
         if (status != AM_NO_MEMORY) {
            *ptr = set_cookies_list;
         }

          } else {
             am_web_log_error("reset_cookie():Not enough memory 0x%x bytes.",
                               reset_cookie_length + 1);
             status = AM_NO_MEMORY;
          }
       }
   } else {
          am_web_log_error("reset_cookie(): Invalid arguments obtained");
          status = AM_INVALID_ARGUMENT;
   }
   return status;
}

// Set attributes as HTTP headers
static am_status_t set_header(const char *key, const char *values, void **args)
{
     am_status_t status = AM_SUCCESS;
     CHAR** ptr = NULL;
     CHAR* set_headers_list = NULL;

     if(key != NULL && values !=NULL ){
         string skey = key;
         string svalues = values;
     }

     if (key != NULL && args != NULL ) {
        int cookie_length = 0;
        char* httpHeaderName = NULL;
        char* tmpHeader = NULL;
#if defined(_AMD64_)
        size_t header_length = 0;
#else
        int header_length = 0;
#endif

        ptr = (CHAR **) args[1];
        set_headers_list = *ptr;

          header_length = strlen(key) + strlen("\r\n") + 1;
          if (values != NULL) {
             header_length += strlen(values);
          }
          httpHeaderName = (char *) malloc(header_length + 1);


       if (status == AM_SUCCESS) {
          if (httpHeaderName != NULL) {
             memset(httpHeaderName, 0, sizeof(char) * (header_length + 1));
             strcpy(httpHeaderName, key);
             strcat(httpHeaderName, ":");
             if (values != NULL) {
                strcat(httpHeaderName, values);
             }
             strcat(httpHeaderName, "\r\n");

             if (set_headers_list == NULL) {
                set_headers_list = (char *) malloc(header_length + 1);
                if (set_headers_list != NULL) {
                    memset(set_headers_list, 0, sizeof(char) *
                                                header_length + 1);
                    strcpy(set_headers_list, httpHeaderName);
                } else {
                    am_web_log_error("set_header():Not enough memory 0x%x "
                             "bytes.",header_length + 1);
                    status = AM_NO_MEMORY;
                }
             } else {
                 tmpHeader = set_headers_list;
                 set_headers_list = (char *) malloc(strlen(tmpHeader) +
                                                    header_length + 1);
                 if (set_headers_list == NULL) {
                    am_web_log_error("set_header():Not enough memory 0x%x "
                             "bytes.",header_length + 1);
                    status = AM_NO_MEMORY;
                 } else {
                    memset(set_headers_list, 0, sizeof(set_headers_list));
                    strcpy(set_headers_list, tmpHeader);
                    strcat(set_headers_list, httpHeaderName);
                 }
              }
              free(httpHeaderName);
              if (tmpHeader) {
                free(tmpHeader);
                tmpHeader = NULL;
              }
            } else {
               am_web_log_error("set_header():Not enough memory 0x%x bytes.",
                                 cookie_length + 1);
               status = AM_NO_MEMORY;
            }
         }
       } else {
          am_web_log_error("set_header(): Invalid arguments obtained");
          status = AM_INVALID_ARGUMENT;
     }

     if (set_headers_list && set_headers_list[0] != '\0') {
        am_web_log_info("set_header():set_headers_list = %s", set_headers_list);
        *ptr = set_headers_list;
     }

     return status;
}

// Set attributes as cookies
static am_status_t set_cookie_in_response(const char *header, void **args)
{
    am_status_t status = AM_SUCCESS;

    if (header != NULL && args != NULL ) {
#if defined(_AMD64_)
        size_t header_length = 0;
#else
        int header_length = 0;
#endif

        CHAR* httpHeader = NULL;
        CHAR* new_cookie_str = NULL;
        CHAR* tmpHeader = NULL;
        CHAR** ptr = NULL;
        CHAR* set_cookies_list = NULL;

        ptr = (CHAR **) args[2];
        set_cookies_list = *ptr;

            header_length = strlen("Set-Cookie:") + strlen("\r\n")
                            + strlen(header) + 1;
            httpHeader = (char *)malloc(header_length);
            if (httpHeader != NULL) {
                sprintf(httpHeader, "Set-Cookie:%s\r\n", header);
                if (set_cookies_list == NULL) {
                       set_cookies_list = (char *) malloc(header_length + 1);
                   if (set_cookies_list != NULL) {
                      memset(set_cookies_list, 0, sizeof(char) *
                                              header_length + 1);
                      strcpy(set_cookies_list, httpHeader);
                       } else {
                         am_web_log_error("set_cookie_in_response(): Not "
                                          "enough memory 0x%x bytes.",
                                          header_length + 1);
                         status = AM_NO_MEMORY;
                       }
                } else {
                    tmpHeader = set_cookies_list;
                    set_cookies_list = (char *)malloc(strlen(tmpHeader) +
                                       header_length + 1);
                    if (set_cookies_list == NULL) {
                        am_web_log_error("set_cookie_in_response():Not "
                                         "enough memory 0x%x bytes.",
                                         header_length + 1);
                        status = AM_NO_MEMORY;
                    } else {
                      memset(set_cookies_list,0,sizeof(set_cookies_list));
                      strcpy(set_cookies_list,tmpHeader);
                      strcat(set_cookies_list, httpHeader);
                    }
                }
                if (new_cookie_str) {
                   am_web_free_memory(new_cookie_str);
                }
            } else {
                    am_web_log_error("set_cookie_in_response(): Not enough "
                                     "memory 0x%x bytes.", header_length + 1);
            }
            free(httpHeader);

            if (tmpHeader != NULL) {
                free(tmpHeader);
            }

            if (status != AM_NO_MEMORY) {
                *ptr = set_cookies_list;
            }

     } else {
       am_web_log_error("set_cookie_in_response():Invalid arguments obtained");
       status = AM_INVALID_ARGUMENT;
     }
     return status;
}

/*
 * Not implemented here. Similar fucntionality is implemented inside 
 * set_headers_in_context.
 * */
static am_status_t set_header_attr_as_cookie(const char *header, void **args)
{
  return AM_SUCCESS;
}


// Not implemented.
static am_status_t get_cookie_sync(const char *cookieName, char** dpro_cookie, void **args)
{
   am_status_t status = AM_SUCCESS;
   return status;
}

/*
 * Utility function used when setting the attributes as cookies in the first request itself
 * */
void ConstructReqCookieValue(string& completeString,string value)
{
    size_t c1 =0, c2=0;

    c1=value.find_first_of('=');
    c2=value.find_first_of(';');
    int diffr= (int) c2-c1;
    if(diffr>1)
    {
        string newKey = value.substr(0,c1);
        string newValue = value.substr(0,c2);
        if(completeString.find(newKey) == string::npos)
        {
            completeString.append(";"+newValue);
        }
    }
}


//Sets the headers in httpContext. Used when setting attributes as headers.
am_status_t set_headers_in_context(IHttpContext *pHttpContext, 
                                string headersList, BOOL isRequest)
{
    am_status_t status = AM_SUCCESS;
    string st = headersList;
    size_t cl =0, cr=0;
    int h1=0; 
    string header="", value="";
    PCSTR pcHeader, pcValue;
    string tmpCookieString="";

    IHttpRequest* pHttpRequest = pHttpContext->GetRequest();
    IHttpResponse* pHttpResponse = pHttpContext->GetResponse();

    do
    {
        cl=st.find_first_of(':');
        cr=st.find_first_of("\r\n");

        if(cl!=string::npos && cr!=string::npos)
        {
            header = st.substr(h1,cl);
            value = st.substr(cl+2,cr-cl-2);
            st= st.substr(cr+2);

            ConstructReqCookieValue(tmpCookieString, value);

            pcHeader = (PCSTR)pHttpContext->AllocateRequestMemory(header.length()+1);
            pcValue = (PCSTR)pHttpContext->AllocateRequestMemory(value.length()+1);
            memset((void*)pcHeader,0,header.length() + 1);
            memset((void*)pcValue,0,value.length() + 1);
            strcpy((char*)pcHeader, header.c_str());
            strcpy((char*)pcValue, value.c_str());
            if(isRequest)
            {
                pHttpContext->GetRequest()->SetHeader(pcHeader,pcValue,
                                               (USHORT)strlen(pcValue),TRUE);
            }
            else
            {
                //this is for the browser to set the cookies
                pHttpContext->GetResponse()->SetHeader(pcHeader,pcValue,
                                               (USHORT)strlen(pcValue),FALSE);
            }

        }

    }
    while(cl!=string::npos);


    //Set the cookie in the request header
    //similar to what set_header_attr_as_cookie is supposed to do
    if(!isCdssoEnabled && !isRequest && tmpCookieString.length()>0)
    {
        PCSTR pszCookie;
        PCSTR newCookie;
        USHORT cchCookie;

        pszCookie = pHttpRequest->GetHeader("Cookie",&cchCookie);
        pszCookie = (PCSTR) pHttpContext->AllocateRequestMemory( cchCookie + 1 );

        if (pszCookie==NULL)
        {
            return AM_FAILURE;
        }

        pszCookie = pHttpRequest->GetHeader("Cookie",&cchCookie);
        newCookie = (PCSTR) pHttpContext->
                AllocateRequestMemory(cchCookie + tmpCookieString.length() + 1 );
        strcpy((char*)newCookie, (char*)pszCookie);
        strcat((char*)newCookie, tmpCookieString.c_str());
        strcat((char*)newCookie,"\0");

        pHttpRequest->SetHeader("Cookie",newCookie,(USHORT)strlen(newCookie),TRUE);
    }

    return status;
}


am_status_t set_request_headers(IHttpContext *pHttpContext, void** args)
{
    const char *thisfunc = "set_request_headers()";
    am_status_t status = AM_SUCCESS;
    PCSTR httpHeaders = NULL;
    CHAR* httpHeadersC = NULL;
    DWORD httpHeadersSize = 0;
    size_t http_headers_length = 0;
    CHAR* key = NULL;
    CHAR* pkeyStart = NULL;
    CHAR* tmpAttributeList = NULL;
    CHAR* pTemp = NULL;
    int i, j;
    int iKeyStart, keyLength;
    int iValueStart, iValueEnd, iHdrStart;
    BOOL isEmptyValue = FALSE;

    CHAR* set_headers_list = *((CHAR**) args[1]);
    CHAR* set_cookies_list = *((CHAR**) args[2]);
    CHAR** ptr = (CHAR **) args[3];
    CHAR* request_hdrs = *ptr;

    //Get the original headers from the request
	status = GetVariable(pHttpContext,"ALL_RAW", &httpHeaders, &httpHeadersSize);

	httpHeadersC = (CHAR*) malloc(strlen(httpHeaders) + 1); 
	strcpy(httpHeadersC, httpHeaders);
        
    //Remove profile attributes from original request headers, if any,
    //to avoid tampering
    if ((status == AM_SUCCESS) && (set_headers_list != NULL)) {
        pkeyStart = set_headers_list;
        iKeyStart=0;
        for (i = 0; i < strlen(set_headers_list); ++i) {
           if (set_headers_list[i] == ':') {
               keyLength = i + 1 - iKeyStart;
               key = (char *)malloc(keyLength + 1);
               if (key != NULL) {
                   memset(key,0,keyLength + 1);
                   strncpy (key,pkeyStart,keyLength);
                   if (strlen(key) > 0) {
                       status = remove_key_in_headers(key, &httpHeadersC);
                   }
                   free(key);
                   key = NULL;
               } else {
                   am_web_log_error("%s:Not enough memory "
                             "to allocate key variable", thisfunc);
                   status = AM_NO_MEMORY;
                   break;
                }
                pkeyStart = set_headers_list;
            }
            if ((set_headers_list[i] == '\r') && (set_headers_list[i+1] == '\n'))
            {
                iKeyStart = i+2;
                pkeyStart = pkeyStart + i + 2;
            }
        }
    }


        //Remove empty values from set_headers_list 
        //also set these non empty headers in pHttpContext
        if ((status == AM_SUCCESS) && (set_headers_list != NULL)) {
            tmpAttributeList = (char*)malloc(strlen(set_headers_list)+1);
            if (tmpAttributeList != NULL) {
                memset(tmpAttributeList,0,strlen(set_headers_list)+1);
                strcpy(tmpAttributeList,set_headers_list);
                memset(set_headers_list,0,strlen(tmpAttributeList)+1);
                iValueStart = 0;
                iValueEnd = 0;
                for (i = 0; i < strlen(tmpAttributeList); ++i) {
                    if (tmpAttributeList[i] == ':') {
                        iValueStart = i + 1;
                    }
                    if ((tmpAttributeList[i] == '\r') &&
                         (tmpAttributeList[i+1] == '\n')) {
                        iHdrStart = iValueEnd;
                        iValueEnd = i;
                        isEmptyValue = TRUE;
                        if ((iValueStart > 0 ) && (iValueEnd > iValueStart)) {
                            for (j=iValueStart ; j < iValueEnd ; j++) {
                                if (tmpAttributeList[j] != ' ') {
                                    isEmptyValue = FALSE;
                                    break;
                                }
                            }
                        }
                        if (isEmptyValue == FALSE) {
                            for (j=iHdrStart ; j<iValueEnd ; j++) {
                                if ((tmpAttributeList[j] != '\r') &&
                                        (tmpAttributeList[j] != '\n')) {
                                    pTemp = tmpAttributeList + j;
                                    strncat(set_headers_list, pTemp, 1);
                                }
                            }
                            strcat(set_headers_list,pszCrlf);
                        }
                    }
                }
            } else {
                   am_web_log_error("%s:Not enough memory to allocate "
                                 "tmpAttributeList.", thisfunc);
                   status = AM_NO_MEMORY;
            }
            if (tmpAttributeList != NULL) {
                free(tmpAttributeList);
                tmpAttributeList = NULL;
            }
        }



        //set the non-empty headers in pHttpContext
        string headersList ="";
        if(set_headers_list)
            headersList = set_headers_list;
        set_headers_in_context(pHttpContext, headersList, TRUE);


        //Add custom headers and/or set_cookie header to original headers
        if (status == AM_SUCCESS) {
            http_headers_length = strlen(httpHeadersC);
            if (set_headers_list != NULL) {
                http_headers_length = http_headers_length + 
                                strlen(set_headers_list);
            }
            if (set_cookies_list != NULL) {
                http_headers_length = http_headers_length +
                               strlen(set_cookies_list);
            }
            request_hdrs = (char *)malloc(http_headers_length + 1);
            if (request_hdrs != NULL) {
                memset(request_hdrs,0, http_headers_length + 1);
                strcpy(request_hdrs, httpHeadersC);
                if (set_headers_list != NULL) {
                    strcat(request_hdrs,set_headers_list);
                }
                if (set_cookies_list != NULL) {
                    strcat(request_hdrs,set_cookies_list);
                }
                *ptr = request_hdrs;
                am_web_log_debug("set_request_headers(): Final headers: %s",
                                                      request_hdrs);
            } else {
                am_web_log_error("set_request_headers():Not enough memory "
                               "to allocate request_hdrs");
                status = AM_NO_MEMORY;
            }
        }

        if (httpHeadersC != NULL) {
            free(httpHeadersC);
            httpHeadersC = NULL;
        }
        if (set_headers_list != NULL) {
            free(set_headers_list);
            set_headers_list = NULL;
        }
    
    return status;
}

/*
 * Invoked during CDSSO. It jsut returns RQ_NOTIFICATION_CONTINUE now to let the 
 * IIS handle the request. If more processing is required, it can be implemented
 * here.
 *
 * */
REQUEST_NOTIFICATION_STATUS redirect_to_request_url(IHttpContext* pHttpContext,
                                  const char *redirect_url, 
                                  const char *set_cookies_list)
{
    return RQ_NOTIFICATION_CONTINUE;
}

/*
 * Invoked when redireting the response to either server login page
 * or 403, 500 responses.
 *
 * */
static DWORD do_redirect(IHttpContext* pHttpContext,
             am_status_t status,
             am_policy_result_t *policy_result,
             const char *original_url,
             const char *method,
             void** args,
             void* agent_config)
{
    const char *thisfunc = "do_redirect()";
    size_t redirect_hdr_len = 0;
    char *redirect_url = NULL;
    DWORD redirect_url_len = 0;
#if defined(_AMD64_)
    DWORD64 advice_headers_len = 0;
#else
    DWORD advice_headers_len = 0;
#endif
    char *advice_headers = NULL;
    const char advice_headers_template[] = {
             "Content-Length: %d\r\n"
             "Content-Type: text/html\r\n"
             "\r\n"
    };

    DWORD returnValue = 0;
    am_status_t ret = AM_SUCCESS;
    const am_map_t advice_map = policy_result->advice_map;
    HRESULT hr;

    IHttpResponse * pHttpResponse = pHttpContext->GetResponse();
    if(pHttpResponse == NULL) {
        am_web_log_error("%s: pHttpResponse is NULL.", thisfunc);
        return returnValue;
    }

    ret = am_web_get_url_to_redirect(status, advice_map, original_url,
                             method, AM_RESERVED,&redirect_url, agent_config);

    // Compute the length of the redirect response.  Using the size of
    // the format string overallocates by a couple of bytes, but that is
    // not a significant issue given the short life span of the allocation.


    switch(status) {
        case AM_ACCESS_DENIED:
        case AM_INVALID_SESSION:
        case AM_INVALID_FQDN_ACCESS:

            //if advice string is present, send it as POST data, sending it as query string
            //is removed in Agents 3.0
            if ((ret == AM_SUCCESS) && (redirect_url != NULL) && 
                    (policy_result->advice_string != NULL)) 
            {
                char *advice_txt = NULL;
                ret = am_web_build_advice_response(policy_result, redirect_url, 
                                                        &advice_txt);
                am_web_log_debug("%s: policy status=%s, response[%s]", 
                           thisfunc, am_status_to_string(status), advice_txt);

                if(ret == AM_SUCCESS) 
                {
                    size_t data_length = (advice_txt != NULL)?strlen(advice_txt):0;
                    if(data_length > 0) 
                    {
                        PCSTR d_length;
                        char buff[256];
                        itoa(data_length,buff,10);

                        advice_headers_len = strlen(advice_headers_template) + 3;
                        advice_headers = (char *) malloc(advice_headers_len);

                        hr = pHttpResponse->SetStatus(200,"Status OK",0, S_OK);
                        hr = pHttpResponse->SetHeader("Content-Type","text/html",
                                                    strlen("text/html"),TRUE);
                        hr = pHttpResponse->SetHeader("Content-Length",buff, 
                                                        strlen(buff),TRUE);
                        if (FAILED(hr)){
                            am_web_log_error("%s: SetHeader failed.", thisfunc);
                        }

                        DWORD cbSent;
                        PCSTR pszBuffer = advice_txt;
                        HTTP_DATA_CHUNK dataChunk;
                        dataChunk.DataChunkType = HttpDataChunkFromMemory;
                        dataChunk.FromMemory.pBuffer = (PVOID) pszBuffer;
                        dataChunk.FromMemory.BufferLength = (USHORT) data_length;
                        hr = pHttpResponse->WriteEntityChunks(&dataChunk,1,
                                                            FALSE,TRUE,&cbSent);
                    }

                } 
                else 
                {
                    am_web_log_error("%s: Error while building " 
                                        "advice response body:%s",
                                     thisfunc, am_status_to_string(ret));
                }

            } 
            else 
            {

                // redirection to OpenSSO Login page.
                // because policy advice string is null.
                if (ret == AM_SUCCESS && redirect_url != NULL) {
                    CHAR* set_cookies_list = *((CHAR**) args[2]);
                    am_web_log_debug("%s: policy status = %s, " 
                                    "redirection URL is %s", thisfunc, 
                                    am_status_to_string(status), redirect_url);


                    if(set_cookies_list != NULL) {
                        set_headers_in_context(pHttpContext, set_cookies_list, 
                                                            FALSE);
                    }
                    pHttpResponse->Redirect(redirect_url, true, false);

                    if (FAILED(hr)) {
                        am_web_log_error("%s: SetHeader failed.", thisfunc);
                    }

                } 
                //redirect url might be null or status is not success
                //redirect to 403 Forbidden or 500 Internal Server error page.
                else 
                {
                    pHttpResponse->Clear();
                    PCSTR pszBuffer;
                    //if status is access denied, send 403.
                    //for every other error, send 500.
                    if(status == AM_ACCESS_DENIED)
                    {
                        pszBuffer = "403 Forbidden";
                        hr = pHttpResponse->SetStatus(403,"Forbidden",0, S_OK, NULL);
                        hr = pHttpResponse->SetHeader("Content-Length","13",
                                                (USHORT)strlen("13"),TRUE);
                        hr = pHttpResponse->SetHeader("Content-Type","text/plain",
                                            (USHORT)strlen("text/plain"), TRUE);
                    }
                    else 
                    {
                        pszBuffer = "500 Internal Server Error";
                        hr = pHttpResponse->SetStatus(500,"Internal Server Error",
                                                                0, S_OK);
                        hr = pHttpResponse->SetHeader("Content-Length","25",
                                                    (USHORT)strlen("25"),TRUE);
                        hr = pHttpResponse->SetHeader("Content-Type","text/html",
                                            (USHORT)strlen("text/html"), TRUE);
                    }

                    HTTP_DATA_CHUNK dataChunk;
                    dataChunk.DataChunkType = HttpDataChunkFromMemory;
                    DWORD cbSent;
                    dataChunk.FromMemory.pBuffer = (PVOID) pszBuffer;
                    dataChunk.FromMemory.BufferLength = (USHORT) strlen(pszBuffer);
                    hr = pHttpResponse->WriteEntityChunks(&dataChunk,1,FALSE,
                                            TRUE,&cbSent);

                    if (FAILED(hr)) {
                    }
                    am_web_log_error("%s: Error while calling "
                        "am_web_get_redirect_url(): status = %s",
                        thisfunc, am_status_to_string(ret));
                }

            }

            if (redirect_url) {
                am_web_free_memory(redirect_url);
            }
            if (advice_headers) {
                free(advice_headers);
            }
            break;

        default:
            // All the default values are set to send 500 code.
            break;
    }
    return returnValue;
}


/* 
 * This function checks if the profile attribute key is in the original 
 * request headers. If it is remove it in order to avoid tampering.
 */
am_status_t remove_key_in_headers(char* key, char** httpHeaders)
{
    const char *thisfunc = "remove_custom_attribute_in_header()";
    am_status_t status = AM_SUCCESS;
    CHAR* pStartHdr =NULL;
    CHAR* pEndHdr =NULL;    
    CHAR* tmpHdr=NULL;  
    size_t len;
    
    pStartHdr = strstr(*httpHeaders,key);
    if (pStartHdr != NULL) {
        tmpHdr = (char*)malloc(strlen(*httpHeaders) + 1);
        if (tmpHdr != NULL) {
            memset(tmpHdr,0,strlen(*httpHeaders) + 1);
            len = strlen(*httpHeaders) - strlen(pStartHdr);
            strncpy(tmpHdr,*httpHeaders,len);
            pEndHdr = strstr(pStartHdr,pszCrlf);
            if (pEndHdr != NULL) {
                pEndHdr = pEndHdr + 2;
                strcat(tmpHdr,pEndHdr);
            }
            am_web_log_info("%s: Attribute %s was found and removed from "
                                "the original request headers.",thisfunc, key);
        } else {
            am_web_log_error("%s: Not enough memory to allocate tmpHdr.",
                             thisfunc);
            status = AM_NO_MEMORY;
        }
    }
    if (tmpHdr != NULL) {
        memset(*httpHeaders,0,strlen(*httpHeaders) + 1);
        strcpy(*httpHeaders,tmpHdr);
        free(tmpHdr);
        tmpHdr = NULL;
    }
    
    return status;
}

/*
 * Gets invoked at the first request. It intializes the agent toolkit.
 *
 * */
void init_at_request()
{
    am_status_t status;
    char debugMsg[2048]="";
    status = am_agent_init(&agentInitialized);
    if (status != AM_SUCCESS) {
        sprintf(debugMsg, "%s: am_agent_init() returned failure. ",
                 am_status_to_string(status));
        logPrimitive(debugMsg);
    } 
} 

/*
 * Invoked when denying requests when the OpenSSO is not responding
 * properly.
 * */
void do_deny(IHttpContext* pHttpContext)
{
    am_status_t status=AM_SUCCESS;
    HRESULT hr;
    IHttpResponse* pHttpResponse = pHttpContext->GetResponse();
    pHttpResponse->Clear();
    PCSTR pszBuffer;
    pszBuffer = "403 Forbidden";
    hr = pHttpResponse->SetStatus(403,"Forbidden",0, S_OK, NULL);
    hr = pHttpResponse->SetHeader("Content-Length","13",(USHORT)strlen("13"),
                                                                    TRUE);
    hr = pHttpResponse->SetHeader("Content-Type","text/plain",
                        (USHORT)strlen("text/plain"), TRUE);
    HTTP_DATA_CHUNK dataChunk;
    dataChunk.DataChunkType = HttpDataChunkFromMemory;
    DWORD cbSent;
    dataChunk.FromMemory.pBuffer = (PVOID) pszBuffer;
    dataChunk.FromMemory.BufferLength = (USHORT) strlen(pszBuffer);
    hr = pHttpResponse->WriteEntityChunks(&dataChunk,1,FALSE,TRUE,&cbSent);
    if (FAILED(hr)) {
        status=AM_FAILURE;
    }
}



