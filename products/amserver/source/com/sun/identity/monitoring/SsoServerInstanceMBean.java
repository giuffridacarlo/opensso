package com.sun.identity.monitoring;

//
// Generated by mibgen version 5.1 (05/20/05) when compiling SUN-OPENSSO-SERVER-MIB in standard metadata mode.
//


// jmx imports
//
import com.sun.management.snmp.SnmpStatusException;

/**
 * This interface is used for representing the remote management interface for the "SsoServerInstance" MBean.
 */
public interface SsoServerInstanceMBean {

    /**
     * Access the "SsoServerRealmTable" variable.
     */
    public TableSsoServerRealmTable accessSsoServerRealmTable() throws SnmpStatusException;

    /**
     * Getter for the "SsoServerSFOStatus" variable.
     */
    public String getSsoServerSFOStatus() throws SnmpStatusException;

    /**
     * Getter for the "SsoServerConfigStoreType" variable.
     */
    public String getSsoServerConfigStoreType() throws SnmpStatusException;

    /**
     * Getter for the "SsoServerStartDate" variable.
     */
    public Byte[] getSsoServerStartDate() throws SnmpStatusException;

    /**
     * Getter for the "SsoServerMemberOfSite" variable.
     */
    public Integer getSsoServerMemberOfSite() throws SnmpStatusException;

    /**
     * Getter for the "SsoServerPort" variable.
     */
    public Integer getSsoServerPort() throws SnmpStatusException;

    /**
     * Getter for the "SsoServerHostname" variable.
     */
    public String getSsoServerHostname() throws SnmpStatusException;

    /**
     * Getter for the "SsoServerId" variable.
     */
    public String getSsoServerId() throws SnmpStatusException;

}
