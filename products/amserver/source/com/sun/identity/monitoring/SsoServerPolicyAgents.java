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
 * The class is used for implementing the "SsoServerPolicyAgents" group.
 * The group is defined with the following oid: 1.3.6.1.4.1.42.2.230.3.1.1.2.1.21.
 */
public class SsoServerPolicyAgents implements SsoServerPolicyAgentsMBean, Serializable {

    /**
     * Variable for storing the value of "SsoServerPolicy22AgentTable".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.21.5".
     */
    protected TableSsoServerPolicy22AgentTable SsoServerPolicy22AgentTable;

    /**
     * Variable for storing the value of "SsoServerPolicyJ2EEGroupTable".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.21.4".
     */
    protected TableSsoServerPolicyJ2EEGroupTable SsoServerPolicyJ2EEGroupTable;

    /**
     * Variable for storing the value of "SsoServerPolicyJ2EEAgentTable".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.21.3".
     */
    protected TableSsoServerPolicyJ2EEAgentTable SsoServerPolicyJ2EEAgentTable;

    /**
     * Variable for storing the value of "SsoServerPolicyWebGroupTable".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.21.2".
     */
    protected TableSsoServerPolicyWebGroupTable SsoServerPolicyWebGroupTable;

    /**
     * Variable for storing the value of "SsoServerPolicyWebAgentTable".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.21.1".
     */
    protected TableSsoServerPolicyWebAgentTable SsoServerPolicyWebAgentTable;


    /**
     * Constructor for the "SsoServerPolicyAgents" group.
     * If the group contains a table, the entries created through an SNMP SET will not be registered in Java DMK.
     */
    public SsoServerPolicyAgents(SnmpMib myMib) {
        SsoServerPolicy22AgentTable = new TableSsoServerPolicy22AgentTable (myMib);
        SsoServerPolicyJ2EEGroupTable = new TableSsoServerPolicyJ2EEGroupTable (myMib);
        SsoServerPolicyJ2EEAgentTable = new TableSsoServerPolicyJ2EEAgentTable (myMib);
        SsoServerPolicyWebGroupTable = new TableSsoServerPolicyWebGroupTable (myMib);
        SsoServerPolicyWebAgentTable = new TableSsoServerPolicyWebAgentTable (myMib);
    }


    /**
     * Constructor for the "SsoServerPolicyAgents" group.
     * If the group contains a table, the entries created through an SNMP SET will be AUTOMATICALLY REGISTERED in Java DMK.
     */
    public SsoServerPolicyAgents(SnmpMib myMib, MBeanServer server) {
        SsoServerPolicy22AgentTable = new TableSsoServerPolicy22AgentTable (myMib, server);
        SsoServerPolicyJ2EEGroupTable = new TableSsoServerPolicyJ2EEGroupTable (myMib, server);
        SsoServerPolicyJ2EEAgentTable = new TableSsoServerPolicyJ2EEAgentTable (myMib, server);
        SsoServerPolicyWebGroupTable = new TableSsoServerPolicyWebGroupTable (myMib, server);
        SsoServerPolicyWebAgentTable = new TableSsoServerPolicyWebAgentTable (myMib, server);
    }

    /**
     * Access the "SsoServerPolicy22AgentTable" variable.
     */
    public TableSsoServerPolicy22AgentTable accessSsoServerPolicy22AgentTable() throws SnmpStatusException {
        return SsoServerPolicy22AgentTable;
    }

    /**
     * Access the "SsoServerPolicy22AgentTable" variable as a bean indexed property.
     */
    public SsoServerPolicy22AgentEntryMBean[] getSsoServerPolicy22AgentTable() throws SnmpStatusException {
        return SsoServerPolicy22AgentTable.getEntries();
    }

    /**
     * Access the "SsoServerPolicyJ2EEGroupTable" variable.
     */
    public TableSsoServerPolicyJ2EEGroupTable accessSsoServerPolicyJ2EEGroupTable() throws SnmpStatusException {
        return SsoServerPolicyJ2EEGroupTable;
    }

    /**
     * Access the "SsoServerPolicyJ2EEGroupTable" variable as a bean indexed property.
     */
    public SsoServerPolicyJ2EEGroupEntryMBean[] getSsoServerPolicyJ2EEGroupTable() throws SnmpStatusException {
        return SsoServerPolicyJ2EEGroupTable.getEntries();
    }

    /**
     * Access the "SsoServerPolicyJ2EEAgentTable" variable.
     */
    public TableSsoServerPolicyJ2EEAgentTable accessSsoServerPolicyJ2EEAgentTable() throws SnmpStatusException {
        return SsoServerPolicyJ2EEAgentTable;
    }

    /**
     * Access the "SsoServerPolicyJ2EEAgentTable" variable as a bean indexed property.
     */
    public SsoServerPolicyJ2EEAgentEntryMBean[] getSsoServerPolicyJ2EEAgentTable() throws SnmpStatusException {
        return SsoServerPolicyJ2EEAgentTable.getEntries();
    }

    /**
     * Access the "SsoServerPolicyWebGroupTable" variable.
     */
    public TableSsoServerPolicyWebGroupTable accessSsoServerPolicyWebGroupTable() throws SnmpStatusException {
        return SsoServerPolicyWebGroupTable;
    }

    /**
     * Access the "SsoServerPolicyWebGroupTable" variable as a bean indexed property.
     */
    public SsoServerPolicyWebGroupEntryMBean[] getSsoServerPolicyWebGroupTable() throws SnmpStatusException {
        return SsoServerPolicyWebGroupTable.getEntries();
    }

    /**
     * Access the "SsoServerPolicyWebAgentTable" variable.
     */
    public TableSsoServerPolicyWebAgentTable accessSsoServerPolicyWebAgentTable() throws SnmpStatusException {
        return SsoServerPolicyWebAgentTable;
    }

    /**
     * Access the "SsoServerPolicyWebAgentTable" variable as a bean indexed property.
     */
    public SsoServerPolicyWebAgentEntryMBean[] getSsoServerPolicyWebAgentTable() throws SnmpStatusException {
        return SsoServerPolicyWebAgentTable.getEntries();
    }

}
