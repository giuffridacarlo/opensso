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
 * $Id: SsoServerSAML1SvcImpl.java,v 1.1 2009-06-19 02:23:18 bigfatrat Exp $
 *
 */

package com.sun.identity.monitoring;

import com.sun.identity.shared.debug.Debug;
import com.sun.management.snmp.agent.SnmpMib;
import javax.management.MBeanServer;

/**
 * This class extends the "SsoServerSAML1Svc" class.
 */
public class SsoServerSAML1SvcImpl extends SsoServerSAML1Svc {
    private static Debug debug = null;
    private static String myMibName;

    protected SsoServerSAML1CacheEntryImpl assertCache = null;
    protected SsoServerSAML1CacheEntryImpl artifactCache = null;
    protected SsoServerSAML1EndPointEntryImpl soapEP = null;
    protected SsoServerSAML1EndPointEntryImpl pprofEP = null;
    protected SsoServerSAML1EndPointEntryImpl samlAwareEP = null;

    public static final String ASSERTIONS = "Assertions";
    public static final String ARTIFACTS = "Artifacts";
    public static final String CREAD = "Read";
    public static final String CWRITE = "Write";
    public static final String CHIT = "Hit";
    public static final String CMISS = "Miss";
    public static final String SOAPRCVR = "SAMLSOAPReceiver";
    public static final String POSTPROFILE = "SAMLPostProfile";
    public static final String SAMLAWARE = "SAMLAware";
    public static final String EPRQTIN = "RqtIn";
    public static final String EPRQTOUT = "RqtOut";
    public static final String EPRQTFAILED = "RqtFailed";
    public static final String EPRQTABORTED = "RqtAborted";

    /**
     * Constructor
     */
    public SsoServerSAML1SvcImpl(SnmpMib myMib) {
        super(myMib);
        myMibName = myMib.getMibName();
        init(myMib, null);
    }

    public SsoServerSAML1SvcImpl(SnmpMib myMib, MBeanServer server) {
        super(myMib, server);
        myMibName = myMib.getMibName();
        init(myMib, server);
    }

    private void init (SnmpMib myMib, MBeanServer server) {
        if (debug == null) {
            debug = Debug.getInstance("amMonitoring");
        }
    }

    /*
     *  Increment the number of reads, writes, hits, or misses
     *  for the SAML1 Assertions or Artifacts Cache.
     *  @param assertOrArtifact Whether the Assertions or Artifacts Cache
     *  @param rWHM Read, Write, Hit, or Miss entry to increment
     */
    public void incSAML1Cache(String assertOrArtifact, String rWHM) {
        String classModule = "SsoServerSAML1SvcImpl.incSAML1Cache:";

        if (!Agent.isRunning()) {
            return;
        }

        SsoServerSAML1CacheEntryImpl ce = null;
        if (assertOrArtifact.equals(ASSERTIONS)) {
            ce = assertCache;
        } else if (assertOrArtifact.equals(ARTIFACTS)) {
            ce = artifactCache;
        } else {
            debug.error(classModule + "no such cache: " + assertOrArtifact);
            return;
        }

        long li;
        if (rWHM.equals(CREAD)) {
            li = ce.SsoServerSAML1CacheReads.longValue();
            li++;
            ce.SsoServerSAML1CacheReads = Long.valueOf(li);
        } else if (rWHM.equals(CWRITE)) {
            li = ce.SsoServerSAML1CacheWrites.longValue();
            li++;
            ce.SsoServerSAML1CacheWrites = Long.valueOf(li);
        } else if (rWHM.equals(CHIT)) {
            li = ce.SsoServerSAML1CacheHits.longValue();
            li++;
            ce.SsoServerSAML1CacheHits = Long.valueOf(li);
        } else if (rWHM.equals(CMISS)) {
            li = ce.SsoServerSAML1CacheMisses.longValue();
            li++;
            ce.SsoServerSAML1CacheMisses = Long.valueOf(li);
        } else {
            debug.error(classModule + "no such cache action: " + rWHM);
            return;
        }
    }

    /*
     *  Increment the number of Request counters (In, Out, Failed, Aborted)
     *  for one of the SAML1 End Points (SOAPReceiver, POSTProfile, SAMLAWARE).
     *  @param endPoint Which EndPoint
     *  @param rType In, out, failed, or aborted counter to increment
     */
    public void incSAML1EndPoint(String endPoint, String rType) {
        String classModule = "SsoServerSAML1SvcImpl.incSAML1EndPoint:";

        if (!Agent.isRunning()) {
            return;
        }

        SsoServerSAML1EndPointEntryImpl ee = null;
        if (endPoint.equals(SOAPRCVR)) {
            ee = soapEP;
        } else if (endPoint.equals(POSTPROFILE)) {
            ee = pprofEP;
        } else if (endPoint.equals(SAMLAWARE)) {
            ee = samlAwareEP;
        } else {
            debug.error(classModule + "no such endpoint: " + endPoint);
            return;
        }

        long li;
        if (rType.equals(EPRQTIN)) {
            li = ee.SsoServerSAML1EndPointRqtIn.longValue();
            li++;
            ee.SsoServerSAML1EndPointRqtIn = Long.valueOf(li);
        } else if (rType.equals(EPRQTOUT)) {
            li = ee.SsoServerSAML1EndPointRqtOut.longValue();
            li++;
            ee.SsoServerSAML1EndPointRqtOut = Long.valueOf(li);
        } else if (rType.equals(EPRQTFAILED)) {
            li = ee.SsoServerSAML1EndPointRqtFailed.longValue();
            li++;
            ee.SsoServerSAML1EndPointRqtFailed = Long.valueOf(li);
        } else if (rType.equals(EPRQTABORTED)) {
            li = ee.SsoServerSAML1EndPointRqtAborted.longValue();
            li++;
            ee.SsoServerSAML1EndPointRqtAborted = Long.valueOf(li);
        } else {
            debug.error(classModule + "no such counter: " + rType);
            return;
        }
    }

    public void setSAML1EndPointOperational(String endPoint) {
        String classModule =
            "SsoServerSAML1SvcImpl.setSAML1EndPointOperational:";

        if (!Agent.isRunning()) {
            return;
        }

        SsoServerSAML1EndPointEntryImpl ee = null;
        if (endPoint.equals(SOAPRCVR)) {
            ee = soapEP;
        } else if (endPoint.equals(POSTPROFILE)) {
            ee = pprofEP;
        } else if (endPoint.equals(SAMLAWARE)) {
            ee = samlAwareEP;
        } else {
            debug.error(classModule + "no such endpoint: " + endPoint);
            return;
        }

        ee.SsoServerSAML1EndPointStatus = "operational";
    }
}