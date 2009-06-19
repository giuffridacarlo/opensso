package com.sun.identity.monitoring;

//
// Generated by mibgen version 5.1 (05/20/05) when compiling SUN-OPENSSO-SERVER-MIB.
//

// java imports
//
import java.io.Serializable;

// jmx imports
//
import com.sun.management.snmp.SnmpStatusException;

// jdmk imports
//
import com.sun.management.snmp.agent.SnmpMib;

/**
 * The class is used for implementing the "SsoServerFedCOTMemberEntry" group.
 * The group is defined with the following oid: 1.3.6.1.4.1.42.2.230.3.1.1.2.1.18.2.1.
 */
public class SsoServerFedCOTMemberEntry implements SsoServerFedCOTMemberEntryMBean, Serializable {

    /**
     * Variable for storing the value of "FedCOTMemberType".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.18.2.1.3".
     */
    protected String FedCOTMemberType = new String("JDMK 5.1");

    /**
     * Variable for storing the value of "FedCOTMemberName".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.18.2.1.2".
     */
    protected String FedCOTMemberName = new String("JDMK 5.1");

    /**
     * Variable for storing the value of "FedCOTMemberIndex".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.18.2.1.1".
     */
    protected Integer FedCOTMemberIndex = new Integer(1);

    /**
     * Variable for storing the value of "SsoServerRealmIndex".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.9.1.1".
     */
    protected Integer SsoServerRealmIndex = new Integer(1);

    /**
     * Variable for storing the value of "FedCOTIndex".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.18.1.1.1".
     */
    protected Integer FedCOTIndex = new Integer(1);

    /**
     * Variable for storing the value of "FedCOTName".
     * The variable is identified by: "1.3.6.1.4.1.42.2.230.3.1.1.2.1.18.1.1.2".
     */
    protected String FedCOTName = new String("JDMK 5.1");


    /**
     * Constructor for the "SsoServerFedCOTMemberEntry" group.
     */
    public SsoServerFedCOTMemberEntry(SnmpMib myMib) {
    }

    /**
     * Getter for the "FedCOTMemberType" variable.
     */
    public String getFedCOTMemberType() throws SnmpStatusException {
        return FedCOTMemberType;
    }

    /**
     * Getter for the "FedCOTMemberName" variable.
     */
    public String getFedCOTMemberName() throws SnmpStatusException {
        return FedCOTMemberName;
    }

    /**
     * Getter for the "FedCOTMemberIndex" variable.
     */
    public Integer getFedCOTMemberIndex() throws SnmpStatusException {
        return FedCOTMemberIndex;
    }

    /**
     * Getter for the "SsoServerRealmIndex" variable.
     */
    public Integer getSsoServerRealmIndex() throws SnmpStatusException {
        return SsoServerRealmIndex;
    }

    /**
     * Getter for the "FedCOTIndex" variable.
     */
    public Integer getFedCOTIndex() throws SnmpStatusException {
        return FedCOTIndex;
    }

    /**
     * Getter for the "FedCOTName" variable.
     */
    public String getFedCOTName() throws SnmpStatusException {
        return FedCOTName;
    }

}
