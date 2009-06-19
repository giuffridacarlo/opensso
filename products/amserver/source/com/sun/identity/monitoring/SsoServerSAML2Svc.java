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
 * The class is used for implementing the "SsoServerSAML2Svc" group.
 * The group is defined with the following oid: 1.3.6.1.4.1.42.2.230.3.1.1.2.1.17.
 */
public class SsoServerSAML2Svc implements SsoServerSAML2SvcMBean, Serializable {

    /**
     * Variable for storing the value of "SsoServerSAML2IDPTable".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.17.6".
     */
    protected TableSsoServerSAML2IDPTable SsoServerSAML2IDPTable;

    /**
     * Variable for storing the value of "SsoServerSAML2RemoteIDPCount".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.17.5".
     */
    protected Long SsoServerSAML2RemoteIDPCount = new Long(1);

    /**
     * Variable for storing the value of "SsoServerSAML2HostedIDPCount".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.17.4".
     */
    protected Long SsoServerSAML2HostedIDPCount = new Long(1);

    /**
     * Variable for storing the value of "SsoServerSAML2FedSessionCount".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.17.3".
     */
    protected Long SsoServerSAML2FedSessionCount = new Long(1);

    /**
     * Variable for storing the value of "SsoServerSAML2IDPSessionCount".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.17.2".
     */
    protected Long SsoServerSAML2IDPSessionCount = new Long(1);

    /**
     * Variable for storing the value of "SsoServerSAML2Status".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.17.1".
     */
    protected String SsoServerSAML2Status = new String("JDMK 5.1");

    /**
     * Variable for storing the value of "SsoServerSAML2SPTable".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.17.7".
     */
    protected TableSsoServerSAML2SPTable SsoServerSAML2SPTable;


    /**
     * Constructor for the "SsoServerSAML2Svc" group.
     * If the group contains a table, the entries created through an SNMP SET will not be registered in Java DMK.
     */
    public SsoServerSAML2Svc(SnmpMib myMib) {
        SsoServerSAML2IDPTable = new TableSsoServerSAML2IDPTable (myMib);
        SsoServerSAML2SPTable = new TableSsoServerSAML2SPTable (myMib);
    }


    /**
     * Constructor for the "SsoServerSAML2Svc" group.
     * If the group contains a table, the entries created through an SNMP SET will be AUTOMATICALLY REGISTERED in Java DMK.
     */
    public SsoServerSAML2Svc(SnmpMib myMib, MBeanServer server) {
        SsoServerSAML2IDPTable = new TableSsoServerSAML2IDPTable (myMib, server);
        SsoServerSAML2SPTable = new TableSsoServerSAML2SPTable (myMib, server);
    }

    /**
     * Access the "SsoServerSAML2IDPTable" variable.
     */
    public TableSsoServerSAML2IDPTable accessSsoServerSAML2IDPTable() throws SnmpStatusException {
        return SsoServerSAML2IDPTable;
    }

    /**
     * Access the "SsoServerSAML2IDPTable" variable as a bean indexed property.
     */
    public SsoServerSAML2IDPEntryMBean[] getSsoServerSAML2IDPTable() throws SnmpStatusException {
        return SsoServerSAML2IDPTable.getEntries();
    }

    /**
     * Getter for the "SsoServerSAML2RemoteIDPCount" variable.
     */
    public Long getSsoServerSAML2RemoteIDPCount() throws SnmpStatusException {
        return SsoServerSAML2RemoteIDPCount;
    }

    /**
     * Getter for the "SsoServerSAML2HostedIDPCount" variable.
     */
    public Long getSsoServerSAML2HostedIDPCount() throws SnmpStatusException {
        return SsoServerSAML2HostedIDPCount;
    }

    /**
     * Getter for the "SsoServerSAML2FedSessionCount" variable.
     */
    public Long getSsoServerSAML2FedSessionCount() throws SnmpStatusException {
        return SsoServerSAML2FedSessionCount;
    }

    /**
     * Getter for the "SsoServerSAML2IDPSessionCount" variable.
     */
    public Long getSsoServerSAML2IDPSessionCount() throws SnmpStatusException {
        return SsoServerSAML2IDPSessionCount;
    }

    /**
     * Getter for the "SsoServerSAML2Status" variable.
     */
    public String getSsoServerSAML2Status() throws SnmpStatusException {
        return SsoServerSAML2Status;
    }

    /**
     * Access the "SsoServerSAML2SPTable" variable.
     */
    public TableSsoServerSAML2SPTable accessSsoServerSAML2SPTable() throws SnmpStatusException {
        return SsoServerSAML2SPTable;
    }

    /**
     * Access the "SsoServerSAML2SPTable" variable as a bean indexed property.
     */
    public SsoServerSAML2SPEntryMBean[] getSsoServerSAML2SPTable() throws SnmpStatusException {
        return SsoServerSAML2SPTable.getEntries();
    }

}
