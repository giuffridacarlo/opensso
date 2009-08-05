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
 * $Id: web_agent.c,v 1.9 2009-08-05 22:00:44 subbae Exp $
 *
 *
 */


/*
 * web_agent.c: 
 *
 * This file implements functions required to do session validation and user's
 * URL access check. Some functions are implemented as a NSAPI SAF to be 
 * called by NSAPI directives which are defined in obj.conf file. Other 
 * supportive funtions need not be NSAPI SAFs.
 *
 * The Server Application Functions(SAF) in this file are PathCheck and Init
 * directive functions
 * 
 */

/*
 * Header Files
 */

#include <string.h>
#include <nsapi.h>

#include "am_properties.h"
#include "am_web.h"

#include <stdio.h>
#if     defined(WINNT)
#define snprintf        _snprintf
#define strtok_r(s1, s2, p) strtok(s1, s2)
#endif

#define AGENT_BOOTSTRAP_FILE "/OpenSSOAgentBootstrap.properties"
#define AGENT_CONFIG_FILE "/OpenSSOAgentConfiguration.properties"
#define DSAME_CONF_DIR "dsameconfdir"
#define   EMPTY_STRING  ""

typedef struct agent_props {
    am_properties_t agent_bootstrap_props;
    am_properties_t agent_config_props;
} agent_props_t;

static agent_props_t agent_props = {
    AM_PROPERTIES_NULL
};

boolean_t agentInitialized = B_FALSE;
static CRITICAL initLock;
const char getMethod[] = "GET";

void init_at_request();

int send_data(const char *msg, Session *sn, Request *rq) {
    int len = msg != NULL?strlen(msg):0;
    int retVal = REQ_ABORTED;

    if(len > 0) {
        char buf[50];
        buf[0] = '\0';
        sprintf(buf, "%d", len);
        protocol_status(sn, rq, PROTOCOL_OK, NULL); 
        param_free(pblock_remove("content-type", rq->srvhdrs));
        pblock_nvinsert("content-type", "text/html", rq->srvhdrs); 
        pblock_nvinsert("content-length", buf, rq->srvhdrs); 
    
        /* Send the headers to the client*/
        protocol_start_response(sn, rq);
    
        /* Write the output using net_write*/
        if (IO_ERROR == net_write(sn->csd, (char *)msg, len)) {
            retVal = REQ_EXIT; 
        } else {
            retVal = net_flush(sn->csd);
        }

    }
    return retVal; 
}

/*
 * this function redirects the user to the auth login url 
 */

static int do_redirect(Session *sn, Request *rq, am_status_t status,
                am_policy_result_t *policy_result,
                const char *original_url, const char* method,
                void* agent_config) {
    int retVal = REQ_ABORTED;
    char *redirect_url = NULL;
    const am_map_t advice_map = policy_result->advice_map;
    am_status_t ret = AM_SUCCESS;

    ret = am_web_get_url_to_redirect(status, advice_map,
                        original_url, method,
                        AM_RESERVED, &redirect_url, agent_config);

    if(ret == AM_SUCCESS && redirect_url != NULL) {
        char *advice_txt = NULL;
        if (policy_result->advice_string != NULL) {
            ret = am_web_build_advice_response(policy_result, redirect_url,
                                               &advice_txt);
            am_web_log_debug("do_redirect(): policy status=%s,",
                             "response[%s]", am_status_to_string(status),
                             advice_txt);
            if(ret == AM_SUCCESS) {
                retVal = send_data(advice_txt, sn, rq);
            } else {
                am_web_log_error("do_redirect(): Error while building "
                                 "advice response body:%s",
                                 am_status_to_string(ret));
                retVal = REQ_EXIT;
            }
        } else {
            am_web_log_debug("do_redirect() policy status = %s, ",
                             "redirection URL is %s",
                              am_status_to_string(status), redirect_url);

            /* redirection is enabled by the PathCheck directive */
            /* Set the return code to 302 Redirect */
            protocol_status(sn, rq, PROTOCOL_REDIRECT, NULL);

            /* set the new URL to redirect */
            //pblock_nvinsert("url", redirect_url, rq->vars);
            pblock_nvinsert("escape", "no", rq->vars);

            param_free(pblock_remove("Location", rq->srvhdrs));
            pblock_nvinsert("Location", redirect_url, rq->srvhdrs);
            protocol_start_response(sn, rq);

            am_web_free_memory(redirect_url);
        }
    } else if(ret == AM_NO_MEMORY) {
        /* Set the return code 500 Internal Server Error. */
        protocol_status(sn, rq, PROTOCOL_SERVER_ERROR, NULL);
        am_web_log_error("do_redirect() Status code= %s.",
                          am_status_to_string(status));
    } else {
        /* Set the return code 403 Forbidden */
        protocol_status(sn, rq, PROTOCOL_FORBIDDEN, NULL);
        am_web_log_info("do_redirect() Status code= %s.",
                        am_status_to_string(status));
    }

    return retVal;
}


/**
 * This function is used for sending a 302 redirect in the browser.
 */
static void do_url_redirect(Session *sn, Request *rq, char* redirect_url)
{
    /* Set the return code to 302 Redirect */
    protocol_status(sn, rq, PROTOCOL_REDIRECT, NULL);

    /* set the new URL to redirect */
    pblock_nvinsert("escape", "no", rq->vars);

    param_free(pblock_remove("Location", rq->srvhdrs));
    pblock_nvinsert("Location", redirect_url, rq->srvhdrs);
    protocol_start_response(sn, rq);
}

static int do_deny(Session *sn, Request *rq, am_status_t status) {
    int retVal = REQ_ABORTED;
    /* Set the return code 403 Forbidden */
    protocol_status(sn, rq, PROTOCOL_FORBIDDEN, NULL);
    am_web_log_info("do_redirect() Status code= %s.",
                     am_status_to_string(status));
    return retVal;
}


/*
 * update the agent cache from the listener response.  Any response without a
 * valid state for the session would result on the cache being updated
 */

static int handle_notification(Session *sn, 
                               Request *rq,
                               void* agent_config)
{
    int result;
    char *content_length_header;
    size_t content_length;

    /* fixme GETPOST use new getRequestBody() routine here.... */
    result = request_header(CONTENT_LENGTH_HDR, &content_length_header, sn,rq);
    if (REQ_PROCEED == result && NULL != content_length_header &&
        sscanf(content_length_header, "%u", &content_length) == 1) {
        char ch;
        size_t data_length = 0;
        char *buf = NULL;

        buf = system_malloc(content_length);
        if (buf != NULL) {
            for (data_length = 0; data_length < content_length; data_length++){
                ch = netbuf_getc(sn->inbuf);
                if (ch == IO_ERROR || ch == IO_EOF) {
                    break;
                }
                buf[data_length] = (char) ch;
            }
            am_web_handle_notification(buf, data_length, agent_config);
            system_free(buf);
        } else {
            am_web_log_error("handle_notification() unable to allocate memory "
            "for notification data, size = %u", content_length);
        }
        result = REQ_PROCEED;
    } else {
        am_web_log_error("handle_notification() %s content-length header",
                         (REQ_PROCEED == result &&
                         NULL != content_length_header) ?
                         "unparsable" : "missing");
    }

    return result;
}

/**
  * Function Name: process_notification
  *
  * Processes both session and policy notifications coming from Access Manager.
  * Implemented as a NSAPI SAF. Works together with Service directive.
  * 
  * Input:  As defined by a SAF
  * Output: As defined by a SAF
*/


NSAPI_PUBLIC int process_notification(pblock *param, Session *sn, Request *rq)
{
    void* agent_config = NULL;
    agent_config = am_web_get_agent_configuration();
    process_new_notification(param,sn,rq,agent_config);
    am_web_delete_agent_configuration(agent_config);
    return REQ_PROCEED;
}

static int process_new_notification(pblock *param,
                                    Session *sn,
                                    Request *rq,
                                    void* agent_config)
{
    handle_notification(sn, rq, agent_config);

    /* Use the protocol_status function to set the status of the
     * response before calling protocol_start_response.
     */
    protocol_status(sn, rq, PROTOCOL_OK, NULL);

    /* Although we would expect the ObjectType stage to
     * set the content-type, set it here just to be
     * completely sure that it gets set to text/html.
     */
    param_free(pblock_remove("content-type", rq->srvhdrs));
    pblock_nvinsert("content-type", "text/html", rq->srvhdrs);

    pblock_nvinsert("content-length", "2", rq->srvhdrs);

    /* Send the headers to the client*/
    protocol_start_response(sn, rq);

    /* Write the output using net_write*/
    if (IO_ERROR == net_write(sn->csd, "OK", 2)) {
        return REQ_EXIT;
    }
    return REQ_PROCEED;
}

NSAPI_PUBLIC void agent_cleanup(void *args) {
    am_properties_destroy(agent_props.agent_bootstrap_props);
    am_web_cleanup();
    crit_terminate(initLock);
}

/*
 * Function Name: web_agent_init
 *
 * Initializes different things required by the web agent.  Implemented as
 * as a NSAPI SAF.  It initializes the Agent Toolkit, which initializes
 * the DSAME Remote SDK (AM) and any necessary support libraries.
 *
 * NOTE: Until am_web_init() returns successfully, the routine should use
 * log_error rather than the am_web_log_* routines.
 *
 * Input:  As defined by a SAF
 * Output: As defined by a SAF
 */

NSAPI_PUBLIC int web_agent_init(pblock *param, Session *sn, Request *rq)
{
    am_status_t status;
    int nsapi_status = REQ_PROCEED;
    char *temp_buf = NULL;
    char *agent_bootstrap_file = NULL;
    char *agent_config_file = NULL;

    initLock = crit_init();

    temp_buf = pblock_findval(DSAME_CONF_DIR, param);

    if (temp_buf != NULL) {
        agent_bootstrap_file = 
            system_malloc(strlen(temp_buf) + sizeof(AGENT_BOOTSTRAP_FILE));
        agent_config_file =
            system_malloc(strlen(temp_buf) + sizeof(AGENT_CONFIG_FILE));

        if (agent_bootstrap_file != NULL) {
            strcpy(agent_bootstrap_file, temp_buf);
            strcat(agent_bootstrap_file, AGENT_BOOTSTRAP_FILE);
        } else {
            log_error(LOG_FAILURE, "URL Access Agent: ", sn, rq,
                     "web_agent_init() unable to allocate memory for bootstrap "
                     "file name", DSAME_CONF_DIR);
            nsapi_status = REQ_ABORTED;
	}

        if (agent_config_file != NULL) {
            strcpy(agent_config_file, temp_buf);
            strcat(agent_config_file, AGENT_CONFIG_FILE);
        } else {
            log_error(LOG_FAILURE, "URL Access Agent: ", sn, rq,
                 "web_agent_init() unable to allocate memory for local config "
                 "file name", DSAME_CONF_DIR);
            nsapi_status = REQ_ABORTED;
        }

        status = am_properties_create(&agent_props.agent_bootstrap_props);
        if(status == AM_SUCCESS) {
            status = am_properties_load(agent_props.agent_bootstrap_props,
                                        agent_bootstrap_file);
            if(status == AM_SUCCESS) {
                //this is where the agent config info is passed from filter code
                //to amsdk. Not sure why agent_props is required.
                status = am_web_init(agent_bootstrap_file,
                                     agent_config_file);
                system_free(agent_bootstrap_file);
                system_free(agent_config_file);
                if (AM_SUCCESS != status) {
                    log_error(LOG_FAILURE, "URL Access Agent: ", sn, rq,
                             "Initialization of the agent failed: "
                             "status = %s (%d)", am_status_to_string(status),
                              status);
                    nsapi_status = REQ_ABORTED;
                 }
            } else {
                log_error(LOG_FAILURE, "web_agent_init():", sn, rq,
                         "Error while creating properties object= %s",
                          am_status_to_string(status));
                nsapi_status = REQ_ABORTED;
            }
        } else {
            log_error(LOG_FAILURE, "web_agent_init():", sn, rq,
                      "Error while creating properties object= %s",
                      am_status_to_string(status));
             nsapi_status = REQ_ABORTED;
        }
    } else {
        log_error(LOG_FAILURE, "URL Access Agent: ", sn, rq,
                 "web_agent_init() %s variable not defined in magnus.conf",
                  DSAME_CONF_DIR);
        nsapi_status = REQ_ABORTED;
    }

    if(nsapi_status == REQ_PROCEED) {
        daemon_atrestart(&agent_cleanup, NULL);
    }

    return nsapi_status;
}

/**
 * convert uppercase letters to lowercase 
 */
static char *to_lower(char *string)
{
    int c, i, len;   
    if (string == NULL) {
        return string;
    }
    len = strlen(string);
    for (i = 0; i < len; ++i) {
        c = string[i];
        if (isupper(c)) {
            string[i] = tolower(c);
        }
    }
    return string;
}

static am_status_t set_header(const char *key, const char *values,
                              void **args) {
    Request *rq = (Request *)args[0];
    pb_param *hdr_pp = pblock_find("full-headers", rq->reqpb);
    char *the_key = key;

    if (hdr_pp != NULL) {
	if(values != NULL && *values != '\0') { //Added by bn152013 for 6739097
            int append = 0;
            int length = strlen(the_key) + strlen(values) + 5;
            if (hdr_pp->value != NULL) {
                size_t len = strlen(hdr_pp->value);
                length += len;
                append = 1;
                hdr_pp->value = (char *) system_realloc(hdr_pp->value, length);
                hdr_pp->value[len] = '\0';
            } else {
                hdr_pp->value = (char *) system_malloc(length);
                hdr_pp->value[0]='\0';
            }

            if ( hdr_pp->value == NULL) {
                return (AM_NO_MEMORY);
            }

            if (append)
                strcat(hdr_pp->value, the_key);
            else
                strcpy(hdr_pp->value, the_key);


            strcat(hdr_pp->value, ": ");
            strcat(hdr_pp->value, values);
            strcat(hdr_pp->value, "\r\n");
        } else {
            if(key[0] != '\0' && hdr_pp->value != NULL) {
                char *ptr = strstr(hdr_pp->value, the_key);
                if(ptr != NULL) {
                    char *end_ptr = ptr + strlen(the_key);
                    while(*end_ptr != '\r' && *end_ptr != '\n')
                        ++end_ptr;

                    end_ptr += 2;

                    while(*end_ptr != '\0') {
                        *ptr = *end_ptr;
                        ptr += 1;
                        end_ptr += 1;
                    }
                    *ptr = '\0';
                }
            }
        }
    }

    if(values != NULL && *values != '\0') { //Added by bn152013 for 6739097
        pblock_nvinsert(the_key, (char *)values, rq->headers);
    } else {
        param_free(pblock_remove(the_key, rq->headers));
    }
    return (AM_SUCCESS);
}

static am_status_t set_header_attr_as_cookie(const char *values, void **args)
{
    Request *rq = NULL;
    am_status_t sts = AM_SUCCESS;
    char *cookie_header = NULL;
    char *new_cookie_header = NULL;

    if (args == NULL || args[0] == NULL || 
	values == NULL || values[0] == '\0') {
	sts = AM_INVALID_ARGUMENT;
    }
    else {
        rq = (Request *)args[0];
	cookie_header = pblock_findval("cookie", rq->headers);

	sts = am_web_set_cookie(cookie_header, values, &new_cookie_header);
	if (sts == AM_SUCCESS && new_cookie_header != NULL && 
		new_cookie_header != cookie_header) {
	    sts = set_header("cookie", new_cookie_header, args);
	    free(new_cookie_header);
	}
    }
    return sts;
}


static am_status_t get_cookie_sync(const char *cookieName,
                                   char** dpro_cookie,
                                   void **args) 
{
    am_status_t ret = AM_SUCCESS;
    return ret;
}

static am_status_t reset_cookie(const char *header, void **args)
{
    am_status_t ret = AM_INVALID_ARGUMENT;	
    if (header != NULL && args !=NULL) {
        Request *rq = (Request *)args[0];
        if (rq == NULL) {
           am_web_log_debug("in reset_cookie: Invalid Request structure");
        } else {
            pblock_nvinsert("set-cookie", header, rq->srvhdrs);
    	    ret = AM_SUCCESS;
	}
    }
    return ret;
}


static am_status_t set_cookie_in_response(const char *header, void **args)
{
    am_status_t ret = AM_INVALID_ARGUMENT;
    if (header != NULL && args !=NULL) {
        Request *rq = (Request *)args[0];
        if (rq == NULL) {
           am_web_log_error("in set_cookie(): Invalid Request structure");
           ret = AM_NO_MEMORY;
        } else {
	    pblock_nvinsert("set-cookie", header, rq->srvhdrs);
	    ret = AM_SUCCESS;
        }
    }
    return ret;
}

static am_status_t set_cookie(const char *header, void **args)
{
    am_status_t ret = AM_INVALID_ARGUMENT;	
    if (header != NULL && args !=NULL) {
        Request *rq = (Request *)args[0];
        if (rq == NULL) {
           am_web_log_error("in set_cookie: Invalid Request structure");
        } else {
            pblock_nvinsert("set-cookie", header, rq->srvhdrs);
    	    ret = AM_SUCCESS;
	}
    }
    return ret;
}


char * get_post_assertion_data(Session *sn, Request *rq, char *url)
{
    int i = 0;
    char *body = NULL;
    int cl = 0;
    char *cl_str = NULL;

    /**
    * content length and body
    *
    * note: memory allocated in here should be released by
    * other function such as: "policy_unregister_post"
    */

    request_header("content-length", &cl_str, sn, rq);
    if(cl_str == NULL)
	    cl_str = pblock_findval("content-length", rq->headers);
    if(cl_str == NULL)
	    return body;
    if(PR_sscanf(cl_str, "%ld", &cl) == 1) {
        body =  (char *)malloc(cl + 1);
	if(body != NULL){
	    for (i = 0; i < cl; i++) {
	        int ch = netbuf_getc(sn->inbuf);
		if (ch==IO_ERROR || ch == IO_EOF) {
		    break;
	 	}	
		body[i] = ch;
	    }  

	    body[i] = '\0';
	}
    } else {
        am_web_log_error("Error reading POST content body");
    }

    am_web_log_max_debug("Read POST content body : %s", body);


    /**
    * need to reset content length before redirect, 
    * otherwise, web server will wait for serveral minutes
    * for non existant data
    */
    param_free(pblock_remove("content-length", rq->headers));
    pblock_nvinsert("content-length", "0", rq->headers);
    return body;

}

int getISCookie(const char *cookie, char **dpro_cookie,
                 void* agent_config) {
    char *loc = NULL;
    char *marker = NULL;
    int length = 0;
    char *search_cookie = NULL;

    if (cookie != NULL) {
      	const char* cookieName = am_web_get_cookie_name(agent_config);
      	if (cookieName != NULL && cookieName[0] != '\0') {
      	    length = 2+strlen(cookieName);
      	    search_cookie = (char *)malloc(length);
      	    if (search_cookie !=NULL) {
      	        search_cookie = strcpy(search_cookie,cookieName);
      	        search_cookie = strcat(search_cookie,"=");
      	        length = 0;
      	    } else {
      	    	am_web_log_error("iws_agent::getISCookie "
      	    	"unable to allocate, size = %u", length);
      	        return REQ_ABORTED;
      	    }
        }

        loc = strstr(cookie, search_cookie);

        // look for last cookie
        while (loc) {
            char *tmp = strstr(loc+1, search_cookie);
            if (tmp) {
                loc = tmp;
            } else {
                break;
            }
        }

        if (search_cookie !=NULL) {
            free(search_cookie);
        }

	if (loc) {
            loc = loc + am_web_get_cookie_name_len(agent_config) + 1;
            while (*loc == ' ') {
                ++loc;
            }

            // skip leading space
            while (*loc == ' ') {
                ++loc;
            }

           // look for end of cookie
            marker = loc;
            while ((*loc != '\0') && (*loc != ';')) {
                ++loc;
            }
            length = loc - marker;

            if (length > 0) {
                *dpro_cookie = malloc(length+1);
                if (*dpro_cookie == NULL) {
                    am_web_log_error("iws_agent::getISCookie "
                    "unable to allocate, size = %u", length);
                    return REQ_ABORTED;
                }
                memcpy(*dpro_cookie, marker, length);
                (*dpro_cookie)[length] = '\0';
            }

        } else {
            am_web_log_warning("OpenSSO Server Cookie not found.");
        }
    }

    return REQ_PROCEED;
}


static void set_method(void ** args, char * orig_req){
    Request *rq = (Request *)args[0];
    if (rq != NULL) {
      pblock_nvinsert(REQUEST_METHOD, orig_req, rq->reqpb);
    }
}

int numdigits(int value) {
    int temp;
    int count=0;

    temp = value;
    while ( temp > 0) {
        count ++;
        temp = temp/10;
    }
    return count;
}

char *replace_host_port(const char *request_url, const char *agent_host,
                        int agent_port, void* agent_config)
{
    char *modified_url = NULL;

    am_web_log_debug("replace_host_port(): Orginal request url => %s",
                     request_url);
    if ( request_url != NULL && agent_host != NULL ) {
        char *temp_url = strdup(request_url);
        if ( temp_url != NULL) {
             char *holder = NULL;
             char *proto = strtok_r(temp_url, "/", &holder);
             char *host_url = strtok_r(NULL, "/", &holder);

             if ( proto != NULL && host_url != NULL) {
                 char *loc1 = strchr(request_url, '/');
                 if ( loc1 != NULL) {
                     char *loc2 = strchr(loc1+1, '/');
                     if ( loc2 != NULL) {
                         char *loc3 = strchr(loc2+1, '/');
                         if (loc3 != NULL) {
                             int new_len = strlen(proto) + 2 +
                                           strlen(agent_host) +
                                           1 + numdigits(agent_port) +
                                           strlen(loc3) + 1;
                             modified_url = (char *)malloc(new_len);
                             if ( modified_url != NULL) {
                                  sprintf(modified_url, "%s//%s:%d%s", proto, 
                                                agent_host, agent_port, loc3);
                                  am_web_log_debug("replace_host_port() Modified url => %s", 
                                                    modified_url);
                             }
                         }
                     }
                 }
             }
             free(temp_url);
        }
   }
   return modified_url;
}

/*
 * Function Name: validate_session_policy
 * This is the NSAPI directive funtion which gets called for each request
 * It does session validation and policy check for each request.
 * Input: As defined by a SAF
 * Output: As defined by a SAF
 */
NSAPI_PUBLIC int
validate_session_policy(pblock *param, Session *sn, Request *rq) {
    char *dpro_cookie = NULL;
    am_status_t status = AM_SUCCESS;
    int  requestResult = REQ_ABORTED;
    int	 notifResult = REQ_ABORTED;
    const char *ruser = NULL;
    am_map_t env_parameter_map = NULL;
    am_policy_result_t result = AM_POLICY_RESULT_INITIALIZER;
    void *args[] = { (void *)rq };
    char *method = NULL;
    char *request_url = NULL;
    char *orig_req = NULL ;
    char *path_info = NULL;
    char * response = NULL;
    char *clf_req = NULL;
    const char *query = pblock_findval(REQUEST_QUERY, rq->reqpb);
    char *uri = pblock_findval(REQUEST_URI, rq->reqpb);
    char *virt_host = NULL;
    char *server_protocol = NULL;
    void* agent_config = NULL;
//    boolean_t useSunwMethod = am_web_use_sunwmethod();
    char* logout_url = NULL;
    am_status_t cdStatus = AM_FAILURE;
    char* cookie_name= NULL;
    int cookie_header_len;
    char* cookie_header = NULL;
    char* header_str = NULL;

    // check if agent is initialized.
    // if not initialized, then call agent init function
    // This needs to be synchronized as only one time agent
    // initialization needs to be done.

    if(agentInitialized != B_TRUE){
        //Start critical section
        crit_enter(initLock);
        if(agentInitialized != B_TRUE){
            am_web_log_debug("validate_session_policy : "
                             "Will call init");
            init_at_request(); 
      	    if(agentInitialized != B_TRUE){
      	           am_web_log_error("validate_session_policy : "
                   " Agent is still not intialized");
                //deny the access
                requestResult =  do_deny(sn, rq, status);
                return requestResult;
      	    }  else {
                am_web_log_debug("validate_session_policy : "
                "Agent intialized");
            }
        }
        //end critical section
        crit_exit(initLock);
    }

    if (am_web_is_max_debug_on()) {
        // Dump the entire set of request headers
        header_str = pblock_pblock2str(rq->reqpb, NULL);
        am_web_log_max_debug("validate_session_policy() Headers: %s", 
                              header_str);
        system_free(header_str);
    }

    // Get request method, path info
    method = pblock_findval(REQUEST_METHOD, rq->reqpb);
    path_info = pblock_findval(PATH_INFO, rq->reqpb);

    agent_config = am_web_get_agent_configuration();

    // Construct request url
    if (uri != NULL && *uri == '/') {
        char *temp = NULL;
        // Get the query
        query = pblock_findval(REQUEST_QUERY, rq->reqpb);
        if (query != NULL) {
            temp = malloc(strlen(query) + 2);
            if (temp == NULL) {
                am_web_log_error("%s: Unable to allocate memory for temp.");
                return REQ_ABORTED;
            }
            temp[0] = '?';
            temp[1] = '\0';
            strcat(temp, query);
        }
        if (am_web_is_proxy_override_host_port_set(agent_config) == AM_FALSE) {
            // Get the virtual host
            virt_host = pblock_findval("ampxy_host", rq->headers);
            if(virt_host == NULL) {
                am_web_log_error("validate_session_policy() vitual host is null");
                return REQ_ABORTED;
            }
            am_web_log_max_debug("validate_session_policy() ampxy_host = %s", virt_host);
            server_protocol = to_lower(strtok(http_uri2url("/", ""), ":"));
            request_url = (char *)malloc(strlen(server_protocol) + 3 +
                             strlen(virt_host) + strlen(uri) + 1);
            if (request_url == NULL) {
                am_web_log_error("validate_session_policy() Unable to allocate memory for "
                                   "request_url.");
                return REQ_ABORTED;
            }
            sprintf(request_url,"%s://%s%s",server_protocol,virt_host,uri);
            // Add the query
            if (temp != NULL) {
                int url_len = strlen(request_url);
                request_url = (char *)realloc(request_url, url_len +
                               strlen(temp) + 1);
                if (request_url == NULL) {
                    am_web_log_error("validate_session_policy() Unable to reallocate memory "
                                     "for request_url.");
                  return REQ_ABORTED;
                }
                strcat(request_url,temp);
                am_web_log_max_debug("validate_session_policy() Query = %s", temp);
            }
        } else {
            request_url = http_uri2url(uri, temp ? temp : "");
        }
        if (temp != NULL) {
            free(temp);
        }
    } else {
        request_url = strdup(uri);
        if (request_url == NULL) {
            am_web_log_error("validate_session_policy() Unable to allocate memory for "
                               "request_url");
            return REQ_ABORTED;
        } 
    }
    if (request_url == NULL) {
        am_web_log_error("validate_session_policy() request_url is null.");
        return REQ_ABORTED;
    }
    if (am_web_is_proxy_override_host_port_set(agent_config) == AM_TRUE) {
        const char *agent_host = am_web_get_agent_server_host(agent_config);
        int agent_port = am_web_get_agent_server_port(agent_config);
        if (agent_host != NULL) {
            char *temp = NULL;
            temp = replace_host_port(request_url, agent_host, agent_port,
                                     agent_config);
            if (temp != NULL) {
                free(request_url);
                request_url=temp;
            }
        }
    }
    am_web_log_debug("validate_session_policy() Request url =  %s", request_url);

    // Check for magic notification URL
    if (B_TRUE == am_web_is_notification(request_url, agent_config)) {
        am_web_free_memory(request_url);
        am_web_delete_agent_configuration(agent_config);
        return REQ_PROCEED;
    }
    // Check if the SSO token is in the cookie header
    if (getISCookie(pblock_findval(COOKIE_HDR, rq->headers), &dpro_cookie,
                                   agent_config) == REQ_ABORTED) {
        am_web_free_memory(request_url);
        am_web_delete_agent_configuration(agent_config);
        return REQ_ABORTED;
    }
    if(dpro_cookie != NULL)
        am_web_log_debug("validate_session_policy(): Cookie Found: %s",
                          dpro_cookie); 
    else
        am_web_log_debug("validate_session_policy(): Cookie Not Found in "
                         " request headers."); 

    // In CDSSO mode, check if the sso token is in the post data
    if (dpro_cookie == NULL &&
        am_web_is_cdsso_enabled(agent_config) == B_TRUE ) {
        if (strcmp(method, REQUEST_METHOD_POST) == 0 &&
               (am_web_is_url_enforced(request_url, path_info, 
               pblock_findval(REQUEST_IP_ADDR, 
               sn->client),agent_config) == B_TRUE)) {
               response = get_post_assertion_data(sn, rq, request_url);
            // Set the original request method to GET always
            orig_req = (char *)malloc(strlen(getMethod)+1);
            if (orig_req != NULL) {
                strcpy(orig_req,getMethod);
                status = am_web_check_cookie_in_post(args, &dpro_cookie,
                                               &request_url,
                                               &orig_req, method, response,
                                               B_FALSE, set_cookie, 
                                               set_method, agent_config);
                if (status == AM_SUCCESS) {
                    // Set back the original clf-request attribute
                    int clf_reqSize = 0;
                    if ((query != NULL) && (strlen(query) > 0)) {
                        clf_reqSize = strlen(orig_req) + strlen(uri) +
                                      strlen (query) + strlen(server_protocol) + 4;
                    } else {
                        clf_reqSize = strlen(orig_req) + strlen(uri) +
                                      strlen(server_protocol) + 3;
                    }
                    clf_req = malloc(clf_reqSize);
                    if (clf_req == NULL) {
                        am_web_log_error("validate_session_policy() "
                                      "Unable to allocate %i bytes for clf_req",
                                      clf_reqSize);
                        status = AM_NO_MEMORY;
                    } else {
                        memset (clf_req,'\0',clf_reqSize);
                        strcpy(clf_req, orig_req);
                        strcat(clf_req, " ");
                        strcat(clf_req, uri);
                        if ((query != NULL) && (strlen(query) > 0)) {
                            strcat(clf_req, "?");
                            strcat(clf_req, query);
                        }
                        strcat(clf_req, " ");
                        strcat(clf_req, server_protocol);
                        am_web_log_debug("validate_session_policy(): "
                        		 "clf-request set to %s", clf_req);
                    }
                    pblock_nvinsert(REQUEST_CLF, clf_req, rq->reqpb);
                }
            } else {
                am_web_log_error("validate_session_policy() : Unable to "
                                 "allocate memory for orig_req");
            }
        }
    }

    if (dpro_cookie != NULL) {
        am_web_log_debug("validate_session_policy() cookie is %s", dpro_cookie);
    } else {
        am_web_log_debug("validate_session_policy() request has no cookie");
    }

    status = am_map_create(&env_parameter_map);
    if (status != AM_SUCCESS) {
        am_web_log_error("validate_session_policy() unable to create map, "
                         "status = %s (%d)", am_status_to_string(status),
                         status);
    }


    // Check if access is allowed.
    if(status == AM_SUCCESS) {
        // Check if client ip header property is set
        const char* client_ip_header_name =
            am_web_get_client_ip_header_name(agent_config);

        // Check if client hostname header property is set
        const char* client_hostname_header_name =
            am_web_get_client_hostname_header_name(agent_config);
        char* ip_header = NULL;
        char* hostname_header = NULL;
        char* client_ip_from_ip_header = NULL;
        char* client_hostname_from_hostname_header = NULL;

        // If client ip header property is set, then try to
        // retrieve header value.
        if(client_ip_header_name != NULL && client_ip_header_name[0] != '\0') {
            ip_header = pblock_findval(client_ip_header_name, rq->headers);

            // Usually client ip header value is: client, proxy1, proxy2....
            // Process client ip header value to get the correct value.
            am_web_get_client_ip(ip_header,
                &client_ip_from_ip_header);
        }

        // If client hostname header property is set, then try to
        // retrieve header value.
        if(client_hostname_header_name != NULL && client_hostname_header_name[0] != '\0') {
            hostname_header = pblock_findval(client_hostname_header_name, rq->headers);

           // Usually client hostname header value is: client, proxy1, proxy2....
           // Process client hostname header value to get the correct value.
            am_web_get_client_hostname(hostname_header,
                &client_hostname_from_hostname_header);
        }

        // If client IP value is present from above processing, then
        // set it to env_param_map. Else use from request structure.
        if(client_ip_from_ip_header != NULL && client_ip_from_ip_header[0] != '\0') {
            am_web_set_host_ip_in_env_map(client_ip_from_ip_header,
                                  client_hostname_from_hostname_header,
                                  env_parameter_map,
                                  agent_config);
            status = am_web_is_access_allowed(dpro_cookie, 
                     request_url,
                     path_info, 
                     method,
                     client_ip_from_ip_header,
                     env_parameter_map, 
                     &result, 
                     agent_config);
        } else {
            status = am_web_is_access_allowed(dpro_cookie, 
                     request_url,
                     path_info, 
                     method,
                     pblock_findval(REQUEST_IP_ADDR, sn->client),
                     env_parameter_map, 
                     &result, 
                     agent_config);
        }
        am_map_destroy(env_parameter_map);
        am_web_free_memory(client_ip_from_ip_header);
        am_web_free_memory(client_hostname_from_hostname_header);
    }

    switch(status) {
    case AM_SUCCESS:
        // Set remote user and authentication type
        ruser = result.remote_user;
        if (ruser != NULL) {
            pb_param *pbuser = pblock_remove(AUTH_USER_VAR, rq->vars);
            pb_param *pbauth = pblock_remove(AUTH_TYPE_VAR, rq->vars);
            if (pbuser != NULL) {
                param_free(pbuser);
            }
            pblock_nvinsert(AUTH_USER_VAR, ruser, rq->vars);
            if (pbauth != NULL) {
                param_free(pbauth);
            }
            pblock_nvinsert(AUTH_TYPE_VAR, AM_WEB_AUTH_TYPE_VALUE, rq->vars);
            am_web_log_debug("validate_session_policy() access allowed to %s",
                              ruser);
        } else {
            am_web_log_debug("validate_session_policy() Remote user not set,"
                 " allowing access to the url as it is in not enforced list");
        }

        if (am_web_is_logout_url(request_url,  agent_config) == B_TRUE) {
            (void)am_web_logout_cookies_reset(reset_cookie, args, agent_config);
        }
        // set LDAP user attributes to http header
        status = am_web_result_attr_map_set(&result, set_header, 
                                           set_cookie_in_response, 
                                           set_header_attr_as_cookie, 
                                           get_cookie_sync, args, agent_config);
        if (status != AM_SUCCESS) {
            am_web_log_error("am_web_result_attr_map_set failed, "
                        "status = %s (%d)",
                        am_status_to_string(status), status);
            requestResult = REQ_ABORTED;
        } else {
            requestResult = REQ_PROCEED;
        }
    break;

    case AM_ACCESS_DENIED:
        am_web_log_debug("validate_session_policy()  Access denied to %s",
                    result.remote_user ? result.remote_user :
                    "unknown user");
        requestResult = do_redirect(sn, rq, status, &result,
                                request_url, method, agent_config);
        break;

    case AM_INVALID_SESSION:

        if (am_web_is_cdsso_enabled(agent_config) == B_TRUE)
        {
            cdStatus = am_web_do_cookie_domain_set(set_cookie, args, EMPTY_STRING, agent_config);
            if(cdStatus != AM_SUCCESS) {
                am_web_log_error("validate_session_policy : CDSSO reset cookie failed");
            }
        }

        am_web_do_cookies_reset(reset_cookie, args, agent_config);
        requestResult =  do_redirect(sn, rq, status, &result,
                                 request_url, method,
                                 agent_config);
    break;

    case AM_INVALID_FQDN_ACCESS:
        // Redirect to self with correct FQDN - no post preservation
        requestResult = do_redirect(sn, rq, status, &result,
                                request_url, method, agent_config);
    break;

    case AM_INVALID_ARGUMENT:
    case AM_NO_MEMORY:

    case AM_REDIRECT_LOGOUT:
        status = am_web_get_logout_url(&logout_url, agent_config);
        if(status == AM_SUCCESS)
        {
            do_url_redirect(sn,rq,logout_url);
        }
        else
        {
            requestResult = REQ_ABORTED;
            am_web_log_debug("validate_session_policy(): "
                             "am_web_get_logout_url failed. ");
        }
    break;

    default:
        am_web_log_error("validate_session_policy() Status: %s (%d)",
                          am_status_to_string(status), status);
        requestResult = REQ_ABORTED;
    break;
    }

    am_web_clear_attributes_map(&result);
    am_policy_result_destroy(&result);
    am_web_free_memory(dpro_cookie);
    am_web_free_memory(request_url);
    am_web_free_memory(logout_url);

    am_web_delete_agent_configuration(agent_config);

    am_web_log_max_debug("validate_session_policy() "
                         " Completed handling request with status: %s.",
                         am_status_to_string(status));

    if (orig_req != NULL) {
        free(orig_req);
        orig_req = NULL;
    }
    if (response != NULL) {
        free(response);
        response = NULL;
    }
    if (clf_req != NULL) {
        free(clf_req);
        clf_req = NULL;
    }

    return requestResult;
}

NSAPI_PUBLIC int
add_agent_header(pblock *param, Session *sn, Request *rq)
{
    const char *thisfunc = "add_agent_header()";
    int  requestResult = REQ_ABORTED;
    char *host         = NULL ;
    char *host_name    = NULL;
    void *args[]       = { (void *)rq };
    am_status_t ret    = AM_FAILURE;
    char *header_str   = NULL;

    // NSAPI function pblock_pblock2str expects "full-headers" 
    // to be non-null value. Therefore, add a logic to check whether 
    // "full-headers" is null and if it is, return error to the client.
    // - Forward port of fix in CRT (657).
    pb_param *hdr_pp = pblock_find("full-headers", rq->reqpb);
    if (hdr_pp != NULL && hdr_pp->value == NULL) {
        am_web_log_error ("add_agent_header():Header not found.");
        return REQ_ABORTED;
    }

    header_str   = pblock_pblock2str(rq->reqpb, NULL);

    am_web_log_max_debug("%s: Headers: %s", thisfunc, header_str);
    system_free(header_str);
    requestResult = request_header ("host",&host,sn,rq );
    if (REQ_PROCEED == requestResult && host != NULL) {
        host_name = strdup (host);
        if (host_name == NULL){
            am_web_log_debug("%s: Unable to allocate memory for host_name",
                              thisfunc);
            am_web_free_memory(host);
            return REQ_ABORTED;
        }
        am_web_log_max_debug("%s: Host = %s ", thisfunc, host_name);
        ret = set_header("ampxy_host",host_name,args);
        if (ret == AM_SUCCESS) {
            header_str = pblock_pblock2str(rq->reqpb, NULL);
            am_web_log_max_debug("%s: headers = %s", thisfunc, header_str);
            system_free(header_str);
            requestResult = REQ_NOACTION;
            am_web_log_max_debug("%s: Host replace success %d ", thisfunc,
                                  requestResult);
        } else {
            am_web_log_max_debug("%s: Host replace failed", thisfunc);
        }
        if(host_name != NULL) {
            free(host_name);
        }
    } else {
        am_web_log_error ("%s: Header not found.", thisfunc);
    }

    return requestResult;
}

/**
* This function is invoked to initialize the agent
* during the first request.
*/
void init_at_request()
{
    am_status_t status;
    status = am_agent_init(&agentInitialized);
    if (status != AM_SUCCESS) {
        log_error(LOG_FAILURE, "URL Access Agent: ", NULL, NULL,
            "Initialization of the agent failed: "
            "status = %s (%d)", am_status_to_string(status), status);
    }
}

