package com.sun.identity.monitoring;

//
// Generated by mibgen version 5.1 (05/20/05) when compiling SUN-OPENSSO-SERVER-MIB in standard metadata mode.
//

// java imports
//
import java.io.Serializable;

// jmx imports
//
import javax.management.MBeanServer;
import com.sun.management.snmp.SnmpCounter;
import com.sun.management.snmp.SnmpCounter64;
import com.sun.management.snmp.SnmpGauge;
import com.sun.management.snmp.SnmpInt;
import com.sun.management.snmp.SnmpUnsignedInt;
import com.sun.management.snmp.SnmpIpAddress;
import com.sun.management.snmp.SnmpTimeticks;
import com.sun.management.snmp.SnmpOpaque;
import com.sun.management.snmp.SnmpString;
import com.sun.management.snmp.SnmpStringFixed;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpNull;
import com.sun.management.snmp.SnmpValue;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpStatusException;

// jdmk imports
//
import com.sun.management.snmp.agent.SnmpMib;
import com.sun.management.snmp.agent.SnmpMibGroup;
import com.sun.management.snmp.agent.SnmpStandardObjectServer;
import com.sun.management.snmp.agent.SnmpStandardMetaServer;
import com.sun.management.snmp.agent.SnmpMibSubRequest;
import com.sun.management.snmp.agent.SnmpMibTable;
import com.sun.management.snmp.EnumRowStatus;
import com.sun.management.snmp.SnmpDefinitions;

/**
 * The class is used for representing SNMP metadata for the "SsoServerTopology" group.
 * The group is defined with the following oid: 1.3.6.1.4.1.42.2.230.3.1.1.2.1.8.
 */
public class SsoServerTopologyMeta extends SnmpMibGroup
     implements Serializable, SnmpStandardMetaServer {

    /**
     * Constructor for the metadata associated to "SsoServerTopology".
     */
    public SsoServerTopologyMeta(SnmpMib myMib, SnmpStandardObjectServer objserv) {
        objectserver = objserv;
        try {
            registerObject(3);
            registerObject(2);
            registerObject(1);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Get the value of a scalar variable
     */
    public SnmpValue get(long var, Object data)
        throws SnmpStatusException {
        switch((int)var) {
            case 3: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }

            case 2: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }

            case 1: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }

            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }

    /**
     * Set the value of a scalar variable
     */
    public SnmpValue set(SnmpValue x, long var, Object data)
        throws SnmpStatusException {
        switch((int)var) {
            case 3: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }

            case 2: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }

            case 1: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }

            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
    }

    /**
     * Check the value of a scalar variable
     */
    public void check(SnmpValue x, long var, Object data)
        throws SnmpStatusException {
        switch((int) var) {
            case 3: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }

            case 2: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }

            case 1: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }

            default:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
        }
    }

    /**
     * Allow to bind the metadata description to a specific object.
     */
    protected void setInstance(SsoServerTopologyMBean var) {
        node = var;
    }


    // ------------------------------------------------------------
    // 
    // Implements the "get" method defined in "SnmpMibGroup".
    // See the "SnmpMibGroup" Javadoc API for more details.
    // 
    // ------------------------------------------------------------

    public void get(SnmpMibSubRequest req, int depth)
        throws SnmpStatusException {
        objectserver.get(this,req,depth);
    }


    // ------------------------------------------------------------
    // 
    // Implements the "set" method defined in "SnmpMibGroup".
    // See the "SnmpMibGroup" Javadoc API for more details.
    // 
    // ------------------------------------------------------------

    public void set(SnmpMibSubRequest req, int depth)
        throws SnmpStatusException {
        objectserver.set(this,req,depth);
    }


    // ------------------------------------------------------------
    // 
    // Implements the "check" method defined in "SnmpMibGroup".
    // See the "SnmpMibGroup" Javadoc API for more details.
    // 
    // ------------------------------------------------------------

    public void check(SnmpMibSubRequest req, int depth)
        throws SnmpStatusException {
        objectserver.check(this,req,depth);
    }

    /**
     * Returns true if "arc" identifies a scalar object.
     */
    public boolean isVariable(long arc) {

        return false;
    }

    /**
     * Returns true if "arc" identifies a readable scalar object.
     */
    public boolean isReadable(long arc) {

        return false;
    }


    // ------------------------------------------------------------
    // 
    // Implements the "skipVariable" method defined in "SnmpMibGroup".
    // See the "SnmpMibGroup" Javadoc API for more details.
    // 
    // ------------------------------------------------------------

    public boolean  skipVariable(long var, Object data, int pduVersion) {
        return false;
    }

    /**
     * Return the name of the attribute corresponding to the SNMP variable identified by "id".
     */
    public String getAttributeName(long id)
        throws SnmpStatusException {
        switch((int)id) {
            case 3: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }

            case 2: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }

            case 1: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }

            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }

    /**
     * Returns true if "arc" identifies a table object.
     */
    public boolean isTable(long arc) {

        switch((int)arc) {
            case 3:
                return true;
            case 2:
                return true;
            case 1:
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * Returns the table object identified by "arc".
     */
    public SnmpMibTable getTable(long arc) {

        switch((int)arc) {
            case 3:
                return tableSsoServerSiteMapTable;
            case 2:
                return tableSsoServerSitesTable;
            case 1:
                return tableSsoServerServerTable;
        default:
            break;
        }
        return null;
    }

    /**
     * Register the group's SnmpMibTable objects with the meta-data.
     */
    public void registerTableNodes(SnmpMib mib, MBeanServer server) {
        tableSsoServerSiteMapTable = createSsoServerSiteMapTableMetaNode("SsoServerSiteMapTable", "SsoServerTopology", mib, server);
        if ( tableSsoServerSiteMapTable != null)  {
            tableSsoServerSiteMapTable.registerEntryNode(mib,server);
            mib.registerTableMeta("SsoServerSiteMapTable", tableSsoServerSiteMapTable);
        }

        tableSsoServerSitesTable = createSsoServerSitesTableMetaNode("SsoServerSitesTable", "SsoServerTopology", mib, server);
        if ( tableSsoServerSitesTable != null)  {
            tableSsoServerSitesTable.registerEntryNode(mib,server);
            mib.registerTableMeta("SsoServerSitesTable", tableSsoServerSitesTable);
        }

        tableSsoServerServerTable = createSsoServerServerTableMetaNode("SsoServerServerTable", "SsoServerTopology", mib, server);
        if ( tableSsoServerServerTable != null)  {
            tableSsoServerServerTable.registerEntryNode(mib,server);
            mib.registerTableMeta("SsoServerServerTable", tableSsoServerServerTable);
        }

    }


    /**
     * Factory method for "SsoServerSiteMapTable" table metadata class.
     * 
     * You can redefine this method if you need to replace the default
     * generated metadata class with your own customized class.
     * 
     * @param tableName Name of the table object ("SsoServerSiteMapTable")
     * @param groupName Name of the group to which this table belong ("SsoServerTopology")
     * @param mib The SnmpMib object in which this table is registered
     * @param server MBeanServer for this table entries (may be null)
     * 
     * @return An instance of the metadata class generated for the
     *         "SsoServerSiteMapTable" table (SsoServerSiteMapTableMeta)
     * 
     **/
    protected SsoServerSiteMapTableMeta createSsoServerSiteMapTableMetaNode(String tableName, String groupName, SnmpMib mib, MBeanServer server)  {
        return new SsoServerSiteMapTableMeta(mib, objectserver);
    }


    /**
     * Factory method for "SsoServerSitesTable" table metadata class.
     * 
     * You can redefine this method if you need to replace the default
     * generated metadata class with your own customized class.
     * 
     * @param tableName Name of the table object ("SsoServerSitesTable")
     * @param groupName Name of the group to which this table belong ("SsoServerTopology")
     * @param mib The SnmpMib object in which this table is registered
     * @param server MBeanServer for this table entries (may be null)
     * 
     * @return An instance of the metadata class generated for the
     *         "SsoServerSitesTable" table (SsoServerSitesTableMeta)
     * 
     **/
    protected SsoServerSitesTableMeta createSsoServerSitesTableMetaNode(String tableName, String groupName, SnmpMib mib, MBeanServer server)  {
        return new SsoServerSitesTableMeta(mib, objectserver);
    }


    /**
     * Factory method for "SsoServerServerTable" table metadata class.
     * 
     * You can redefine this method if you need to replace the default
     * generated metadata class with your own customized class.
     * 
     * @param tableName Name of the table object ("SsoServerServerTable")
     * @param groupName Name of the group to which this table belong ("SsoServerTopology")
     * @param mib The SnmpMib object in which this table is registered
     * @param server MBeanServer for this table entries (may be null)
     * 
     * @return An instance of the metadata class generated for the
     *         "SsoServerServerTable" table (SsoServerServerTableMeta)
     * 
     **/
    protected SsoServerServerTableMeta createSsoServerServerTableMetaNode(String tableName, String groupName, SnmpMib mib, MBeanServer server)  {
        return new SsoServerServerTableMeta(mib, objectserver);
    }

    protected SsoServerTopologyMBean node;
    protected SnmpStandardObjectServer objectserver = null;
    protected SsoServerSiteMapTableMeta tableSsoServerSiteMapTable = null;
    protected SsoServerSitesTableMeta tableSsoServerSitesTable = null;
    protected SsoServerServerTableMeta tableSsoServerServerTable = null;
}
