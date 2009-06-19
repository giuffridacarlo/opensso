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
 * The class is used for representing SNMP metadata for the "SsoServerLoggingSvc" group.
 * The group is defined with the following oid: 1.3.6.1.4.1.42.2.230.3.1.1.2.1.12.
 */
public class SsoServerLoggingSvcMeta extends SnmpMibGroup
     implements Serializable, SnmpStandardMetaServer {

    /**
     * Constructor for the metadata associated to "SsoServerLoggingSvc".
     */
    public SsoServerLoggingSvcMeta(SnmpMib myMib, SnmpStandardObjectServer objserv) {
        objectserver = objserv;
        try {
            registerObject(9);
            registerObject(8);
            registerObject(7);
            registerObject(6);
            registerObject(5);
            registerObject(4);
            registerObject(3);
            registerObject(11);
            registerObject(2);
            registerObject(1);
            registerObject(10);
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
            case 9:
                return new SnmpInt(node.getSsoServerLoggingLoggers());

            case 8:
                return new SnmpCounter64(node.getSsoServerLoggingBufferSize());

            case 7:
                return new SnmpCounter64(node.getSsoServerLoggingBufferTime());

            case 6:
                return new SnmpString(node.getSsoServerLoggingTimeBuffering());

            case 5:
                return new SnmpString(node.getSsoServerLoggingSecure());

            case 4:
                return new SnmpCounter64(node.getSsoServerLoggingNumHistFiles());

            case 3:
                return new SnmpCounter64(node.getSsoServerLoggingMaxLogSize());

            case 11: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }

            case 2:
                return new SnmpString(node.getSsoServerLoggingLocation());

            case 1:
                return new SnmpString(node.getSsoServerLoggingType());

            case 10:
                return new SnmpCounter64(node.getSsoServerLoggingRecsRejected());

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
            case 9:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 8:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 7:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 6:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 5:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 4:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 3:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 11: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }

            case 2:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 1:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 10:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

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
            case 9:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 8:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 7:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 6:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 5:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 4:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 3:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 11: {
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
                }

            case 2:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 1:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            case 10:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);

            default:
                throw new SnmpStatusException(SnmpStatusException.snmpRspNotWritable);
        }
    }

    /**
     * Allow to bind the metadata description to a specific object.
     */
    protected void setInstance(SsoServerLoggingSvcMBean var) {
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

        switch((int)arc) {
            case 9:
            case 8:
            case 7:
            case 6:
            case 5:
            case 4:
            case 3:
            case 2:
            case 1:
            case 10:
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * Returns true if "arc" identifies a readable scalar object.
     */
    public boolean isReadable(long arc) {

        switch((int)arc) {
            case 9:
            case 8:
            case 7:
            case 6:
            case 5:
            case 4:
            case 3:
            case 2:
            case 1:
            case 10:
                return true;
            default:
                break;
        }
        return false;
    }


    // ------------------------------------------------------------
    // 
    // Implements the "skipVariable" method defined in "SnmpMibGroup".
    // See the "SnmpMibGroup" Javadoc API for more details.
    // 
    // ------------------------------------------------------------

    public boolean  skipVariable(long var, Object data, int pduVersion) {
        switch((int)var) {
            case 8:
            case 7:
            case 4:
            case 3:
            case 10:
                if (pduVersion==SnmpDefinitions.snmpVersionOne) return true;
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * Return the name of the attribute corresponding to the SNMP variable identified by "id".
     */
    public String getAttributeName(long id)
        throws SnmpStatusException {
        switch((int)id) {
            case 9:
                return "SsoServerLoggingLoggers";

            case 8:
                return "SsoServerLoggingBufferSize";

            case 7:
                return "SsoServerLoggingBufferTime";

            case 6:
                return "SsoServerLoggingTimeBuffering";

            case 5:
                return "SsoServerLoggingSecure";

            case 4:
                return "SsoServerLoggingNumHistFiles";

            case 3:
                return "SsoServerLoggingMaxLogSize";

            case 11: {
                throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
                }

            case 2:
                return "SsoServerLoggingLocation";

            case 1:
                return "SsoServerLoggingType";

            case 10:
                return "SsoServerLoggingRecsRejected";

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
            case 11:
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
            case 11:
                return tableSsoServerLoggingHdlrTable;
        default:
            break;
        }
        return null;
    }

    /**
     * Register the group's SnmpMibTable objects with the meta-data.
     */
    public void registerTableNodes(SnmpMib mib, MBeanServer server) {
        tableSsoServerLoggingHdlrTable = createSsoServerLoggingHdlrTableMetaNode("SsoServerLoggingHdlrTable", "SsoServerLoggingSvc", mib, server);
        if ( tableSsoServerLoggingHdlrTable != null)  {
            tableSsoServerLoggingHdlrTable.registerEntryNode(mib,server);
            mib.registerTableMeta("SsoServerLoggingHdlrTable", tableSsoServerLoggingHdlrTable);
        }

    }


    /**
     * Factory method for "SsoServerLoggingHdlrTable" table metadata class.
     * 
     * You can redefine this method if you need to replace the default
     * generated metadata class with your own customized class.
     * 
     * @param tableName Name of the table object ("SsoServerLoggingHdlrTable")
     * @param groupName Name of the group to which this table belong ("SsoServerLoggingSvc")
     * @param mib The SnmpMib object in which this table is registered
     * @param server MBeanServer for this table entries (may be null)
     * 
     * @return An instance of the metadata class generated for the
     *         "SsoServerLoggingHdlrTable" table (SsoServerLoggingHdlrTableMeta)
     * 
     **/
    protected SsoServerLoggingHdlrTableMeta createSsoServerLoggingHdlrTableMetaNode(String tableName, String groupName, SnmpMib mib, MBeanServer server)  {
        return new SsoServerLoggingHdlrTableMeta(mib, objectserver);
    }

    protected SsoServerLoggingSvcMBean node;
    protected SnmpStandardObjectServer objectserver = null;
    protected SsoServerLoggingHdlrTableMeta tableSsoServerLoggingHdlrTable = null;
}
