/**
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
 * $Id: SsoServerSAML1CacheEntryMeta.java,v 1.1 2009-06-19 02:23:18 bigfatrat Exp $
 *
 */

package com.sun.identity.monitoring;

//
// Generated by mibgen version 5.1 (05/20/05) when compiling
// SUN-OPENSSO-SERVER-MIB in standard metadata mode.
//

import java.io.Serializable;
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
import com.sun.management.snmp.agent.SnmpMibNode;
import com.sun.management.snmp.agent.SnmpMib;
import com.sun.management.snmp.agent.SnmpMibEntry;
import com.sun.management.snmp.agent.SnmpStandardObjectServer;
import com.sun.management.snmp.agent.SnmpStandardMetaServer;
import com.sun.management.snmp.agent.SnmpMibSubRequest;
import com.sun.management.snmp.agent.SnmpMibTable;
import com.sun.management.snmp.EnumRowStatus;
import com.sun.management.snmp.SnmpDefinitions;

/**
 * The class is used for representing SNMP metadata for the
 * "SsoServerSAML1CacheEntry" group.
 * The group is defined with the following oid:
 * 1.3.6.1.4.1.42.2.230.3.1.1.2.1.16.1.1.
 */
public class SsoServerSAML1CacheEntryMeta extends SnmpMibEntry
     implements Serializable, SnmpStandardMetaServer {

    /**
     * Constructor for the metadata associated to "SsoServerSAML1CacheEntry".
     */
    public SsoServerSAML1CacheEntryMeta(
        SnmpMib myMib,
        SnmpStandardObjectServer objserv)
    {
        objectserver = objserv;
        varList = new int[5];
        varList[0] = 6;
        varList[1] = 5;
        varList[2] = 4;
        varList[3] = 3;
        varList[4] = 2;
        SnmpMibNode.sort(varList);
    }

    /**
     * Get the value of a scalar variable
     */
    public SnmpValue get(long var, Object data)
        throws SnmpStatusException {
        switch((int)var) {
            case 6:
                return new SnmpCounter64(node.getSsoServerSAML1CacheMisses());

            case 5:
                return new SnmpCounter64(node.getSsoServerSAML1CacheHits());

            case 4:
                return new SnmpCounter64(node.getSsoServerSAML1CacheWrites());

            case 3:
                return new SnmpCounter64(node.getSsoServerSAML1CacheReads());

            case 2:
                return new SnmpString(node.getSsoServerSAML1CacheName());

            case 1:
                throw new SnmpStatusException(
                    SnmpStatusException.noSuchInstance);
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
            case 6:
                throw new SnmpStatusException(
                    SnmpStatusException.snmpRspNotWritable);

            case 5:
                throw new SnmpStatusException(
                    SnmpStatusException.snmpRspNotWritable);

            case 4:
                throw new SnmpStatusException(
                    SnmpStatusException.snmpRspNotWritable);

            case 3:
                throw new SnmpStatusException(
                    SnmpStatusException.snmpRspNotWritable);

            case 2:
                throw new SnmpStatusException(
                    SnmpStatusException.snmpRspNotWritable);

            case 1:
                throw new SnmpStatusException(
                    SnmpStatusException.snmpRspNotWritable);

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
            case 6:
                throw new SnmpStatusException(
                    SnmpStatusException.snmpRspNotWritable);

            case 5:
                throw new SnmpStatusException(
                    SnmpStatusException.snmpRspNotWritable);

            case 4:
                throw new SnmpStatusException(
                    SnmpStatusException.snmpRspNotWritable);

            case 3:
                throw new SnmpStatusException(
                    SnmpStatusException.snmpRspNotWritable);

            case 2:
                throw new SnmpStatusException(
                    SnmpStatusException.snmpRspNotWritable);

            case 1:
                throw new SnmpStatusException(
                    SnmpStatusException.snmpRspNotWritable);

            default:
                throw new SnmpStatusException(
                    SnmpStatusException.snmpRspNotWritable);
        }
    }

    /**
     * Allow to bind the metadata description to a specific object.
     */
    protected void setInstance(SsoServerSAML1CacheEntryMBean var) {
        node = var;
    }


    // ------------------------------------------------------------
    // 
    // Implements the "get" method defined in "SnmpMibEntry".
    // See the "SnmpMibEntry" Javadoc API for more details.
    // 
    // ------------------------------------------------------------

    public void get(SnmpMibSubRequest req, int depth)
        throws SnmpStatusException {
        objectserver.get(this,req,depth);
    }


    // ------------------------------------------------------------
    // 
    // Implements the "set" method defined in "SnmpMibEntry".
    // See the "SnmpMibEntry" Javadoc API for more details.
    // 
    // ------------------------------------------------------------

    public void set(SnmpMibSubRequest req, int depth)
        throws SnmpStatusException {
        objectserver.set(this,req,depth);
    }


    // ------------------------------------------------------------
    // 
    // Implements the "check" method defined in "SnmpMibEntry".
    // See the "SnmpMibEntry" Javadoc API for more details.
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
            case 6:
            case 5:
            case 4:
            case 3:
            case 2:
            case 1:
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
            case 6:
            case 5:
            case 4:
            case 3:
            case 2:
                return true;
            default:
                break;
        }
        return false;
    }


    // ------------------------------------------------------------
    // 
    // Implements the "skipVariable" method defined in "SnmpMibEntry".
    // See the "SnmpMibEntry" Javadoc API for more details.
    // 
    // ------------------------------------------------------------

    public boolean  skipVariable(long var, Object data, int pduVersion) {
        switch((int)var) {
            case 6:
            case 5:
            case 4:
            case 3:
                if (pduVersion==SnmpDefinitions.snmpVersionOne) return true;
                break;
            case 1:
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * Return the name of the attribute corresponding to the SNMP variable
     * identified by "id".
     */
    public String getAttributeName(long id)
        throws SnmpStatusException {
        switch((int)id) {
            case 6:
                return "SsoServerSAML1CacheMisses";

            case 5:
                return "SsoServerSAML1CacheHits";

            case 4:
                return "SsoServerSAML1CacheWrites";

            case 3:
                return "SsoServerSAML1CacheReads";

            case 2:
                return "SsoServerSAML1CacheName";

            case 1:
                return "SsoServerSAML1CacheIndex";

            default:
                break;
        }
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }

    protected SsoServerSAML1CacheEntryMBean node;
    protected SnmpStandardObjectServer objectserver = null;
}
