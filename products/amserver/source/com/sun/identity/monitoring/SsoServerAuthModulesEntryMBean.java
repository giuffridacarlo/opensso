package com.sun.identity.monitoring;

//
// Generated by mibgen version 5.1 (05/20/05) when compiling SUN-OPENSSO-SERVER-MIB in standard metadata mode.
//


// jmx imports
//
import com.sun.management.snmp.SnmpStatusException;

/**
 * This interface is used for representing the remote management interface for the "SsoServerAuthModulesEntry" MBean.
 */
public interface SsoServerAuthModulesEntryMBean {

    /**
     * Getter for the "SsoServerAuthModuleFailureCount" variable.
     */
    public Long getSsoServerAuthModuleFailureCount() throws SnmpStatusException;

    /**
     * Getter for the "SsoServerAuthModuleSuccessCount" variable.
     */
    public Long getSsoServerAuthModuleSuccessCount() throws SnmpStatusException;

    /**
     * Getter for the "SsoServerAuthModuleType" variable.
     */
    public String getSsoServerAuthModuleType() throws SnmpStatusException;

    /**
     * Getter for the "SsoServerAuthModuleName" variable.
     */
    public String getSsoServerAuthModuleName() throws SnmpStatusException;

    /**
     * Getter for the "SsoServerAuthModuleIndex" variable.
     */
    public Integer getSsoServerAuthModuleIndex() throws SnmpStatusException;

    /**
     * Getter for the "SsoServerRealmIndex" variable.
     */
    public Integer getSsoServerRealmIndex() throws SnmpStatusException;

}