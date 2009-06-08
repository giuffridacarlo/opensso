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
 * $Id: EntitlementsStats.java,v 1.1 2009-06-08 05:09:06 arviranga Exp $
 */

package com.sun.identity.entitlement.opensso;

import java.util.Set;

import com.sun.identity.entitlement.util.NetworkMonitor;
import com.sun.identity.shared.stats.StatsListener;
import com.sun.identity.shared.stats.Stats;

public class EntitlementsStats implements StatsListener {
	
	private Stats stats;
	
	public EntitlementsStats(Stats stats) {
		this.stats = stats;
	}

	@Override
	public void printStats() {
		StringBuffer sb = new StringBuffer(100);
		sb.append("Entitlements statistics:\n");
		sb.append("-----------------------------");
		Set<String> ntwStats = NetworkMonitor.getInstanceNames();
		for (String ntwStat : ntwStats) {
			NetworkMonitor nm = NetworkMonitor.getInstance(ntwStat);
			sb.append("NetworkMonitor: " + ntwStat);
			sb.append("Response Time(ms): " + nm.responseTime());
			sb.append("Throughput: " + nm.throughput());
			nm.reset();
			sb.append("-----------------------------");
		}
		sb.append("-----------------------------");
		stats.record(sb.toString());
	}
}