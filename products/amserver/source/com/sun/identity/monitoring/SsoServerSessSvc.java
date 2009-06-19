package com.sun.identity.monitoring;

//
// Generated by mibgen version 5.1 (05/20/05) when compiling SUN-OPENSSO-SERVER-MIB.
//

// java imports
//
import java.io.Serializable;

// jmx imports
//
import javax.management.MBeanServer;
import com.sun.management.snmp.SnmpString;
import com.sun.management.snmp.SnmpStatusException;

// jdmk imports
//
import com.sun.management.snmp.agent.SnmpMib;

/**
 * The class is used for implementing the "SsoServerSessSvc" group.
 * The group is defined with the following oid: 1.3.6.1.4.1.42.2.230.3.1.1.2.1.11.
 */
public class SsoServerSessSvc implements SsoServerSessSvcMBean, Serializable {

    /**
     * Variable for storing the value of "SsoServerSessAveSessSize".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.11.6".
     */
    protected Integer SsoServerSessAveSessSize = new Integer(1);

    /**
     * Variable for storing the value of "SsoServerSessNotifListnrCount".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.11.5".
     */
    protected Long SsoServerSessNotifListnrCount = new Long(1);

    /**
     * Variable for storing the value of "SsoServerSessNotifCount".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.11.4".
     */
    protected Long SsoServerSessNotifCount = new Long(1);

    /**
     * Variable for storing the value of "SsoServerSessValidationsCount".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.11.3".
     */
    protected Long SsoServerSessValidationsCount = new Long(1);

    /**
     * Variable for storing the value of "SsoServerSessCreatedCount".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.11.2".
     */
    protected Long SsoServerSessCreatedCount = new Long(1);

    /**
     * Variable for storing the value of "SsoServerSessActiveCount".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.11.1".
     */
    protected Long SsoServerSessActiveCount = new Long(1);

    /**
     * Variable for storing the value of "SsoServerSessSFOBroker".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.11.7".
     */
    protected String SsoServerSessSFOBroker = new String("JDMK 5.1");


    /**
     * Constructor for the "SsoServerSessSvc" group.
     * If the group contains a table, the entries created through an SNMP SET will not be registered in Java DMK.
     */
    public SsoServerSessSvc(SnmpMib myMib) {
    }


    /**
     * Constructor for the "SsoServerSessSvc" group.
     * If the group contains a table, the entries created through an SNMP SET will be AUTOMATICALLY REGISTERED in Java DMK.
     */
    public SsoServerSessSvc(SnmpMib myMib, MBeanServer server) {
    }

    /**
     * Getter for the "SsoServerSessAveSessSize" variable.
     */
    public Integer getSsoServerSessAveSessSize() throws SnmpStatusException {
        return SsoServerSessAveSessSize;
    }

    /**
     * Getter for the "SsoServerSessNotifListnrCount" variable.
     */
    public Long getSsoServerSessNotifListnrCount() throws SnmpStatusException {
        return SsoServerSessNotifListnrCount;
    }

    /**
     * Getter for the "SsoServerSessNotifCount" variable.
     */
    public Long getSsoServerSessNotifCount() throws SnmpStatusException {
        return SsoServerSessNotifCount;
    }

    /**
     * Getter for the "SsoServerSessValidationsCount" variable.
     */
    public Long getSsoServerSessValidationsCount() throws SnmpStatusException {
        return SsoServerSessValidationsCount;
    }

    /**
     * Getter for the "SsoServerSessCreatedCount" variable.
     */
    public Long getSsoServerSessCreatedCount() throws SnmpStatusException {
        return SsoServerSessCreatedCount;
    }

    /**
     * Getter for the "SsoServerSessActiveCount" variable.
     */
    public Long getSsoServerSessActiveCount() throws SnmpStatusException {
        return SsoServerSessActiveCount;
    }

    /**
     * Getter for the "SsoServerSessSFOBroker" variable.
     */
    public String getSsoServerSessSFOBroker() throws SnmpStatusException {
        return SsoServerSessSFOBroker;
    }

}
