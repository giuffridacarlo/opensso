package com.sun.identity.monitoring;

//
// Generated by mibgen version 5.1 (05/20/05) when compiling SUN-OPENSSO-SERVER-MIB in standard metadata mode.
//


// jmx imports
//
import com.sun.management.snmp.SnmpStatusException;

/**
 * This interface is used for representing the remote management interface for the "SsoServerWSSAgentsWSPAgentEntry" MBean.
 */
public interface SsoServerWSSAgentsWSPAgentEntryMBean {

    /**
     * Getter for the "WssAgentsWSPAgentSvcEndPoint" variable.
     */
    public String getWssAgentsWSPAgentSvcEndPoint() throws SnmpStatusException;

    /**
     * Getter for the "WssAgentsWSPAgentProxy" variable.
     */
    public String getWssAgentsWSPAgentProxy() throws SnmpStatusException;

    /**
     * Getter for the "WssAgentsWSPAgentName" variable.
     */
    public String getWssAgentsWSPAgentName() throws SnmpStatusException;

    /**
     * Getter for the "WssAgentsWSPAgentIndex" variable.
     */
    public Integer getWssAgentsWSPAgentIndex() throws SnmpStatusException;

    /**
     * Getter for the "SsoServerRealmIndex" variable.
     */
    public Integer getSsoServerRealmIndex() throws SnmpStatusException;

}
