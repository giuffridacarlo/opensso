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
 * $Id: SamlV2HostedCreateWizardBean.java,v 1.1 2009-08-19 05:40:54 veiming Exp $
 */
package com.sun.identity.admin.model;

import com.icesoft.faces.context.effects.Effect;
import com.iplanet.am.util.SystemProperties;
import com.sun.identity.console.base.model.AMSystemConfig;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

public class SamlV2HostedCreateWizardBean
        extends WizardBean
        implements Serializable {

    private String selectedRealm = "/";
    private boolean meta = false;
    private boolean cot = true;
    private String newEntityName;
    private String newCotName;
    private String selectedCot;
    private List<SelectItem> availableRealmsList;
    private List<SelectItem> availableCotList;
    private CotsBean cotsBean;
    private String stdMetaFile;
    private String stdMetaFilename;
    private int stdMetaFileProgress;
    private String extMetaFile;
    private String extMetaFilename;
    private int extMetaFileProgress;
    private Effect samlV2HostedCreateEntityInputEffect;

    SamlV2HostedCreateWizardBean() {
        super();
        newEntityName = (AMSystemConfig.isConsoleRemote) ? SystemProperties.getServerInstanceName() : getRequestURL();
    }

    public void reset() {
        super.reset();
        selectedRealm = null;
        meta = false;
        cot = true;
        newEntityName = (AMSystemConfig.isConsoleRemote) ? SystemProperties.getServerInstanceName() : getRequestURL();
        newCotName = null;
        selectedCot = null;
        availableRealmsList = null;
        availableCotList = null;
        cotsBean = null;
        stdMetaFile = null;
        stdMetaFilename = null;
        extMetaFile = null;
        extMetaFilename = null;
        stdMetaFileProgress = 0;
        extMetaFileProgress = 0;
    }

    protected String getRequestURL() {
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String uri = req.getRequestURI().toString();
        int idx = uri.indexOf('/', 1);
        uri = uri.substring(0, idx);
        return req.getScheme() + "://" + req.getServerName() +
                ":" + req.getServerPort() + uri;
    }

    public String getSelectedRealm() {
        return selectedRealm;
    }

    public void setSelectedRealm(String selectedRealm) {
        this.selectedRealm = selectedRealm;
        int idx = selectedRealm.indexOf("(");
        int end = selectedRealm.indexOf(")");
        String realm = selectedRealm.substring(idx + 1, end).trim();
        cotsBean = new CotsBean();
        cotsBean.setCotBeans(realm);
        if (getAvailableCotList().size() == 0) {
            setCot(false);
        }
    }

    public boolean isMeta() {
        return meta;
    }

    public void setMeta(boolean meta) {
        this.meta = meta;
    }

    public boolean isCot() {
        return cot;
    }

    public void setCot(boolean cot) {
        this.cot = cot;
    }

    public String getNewEntityName() {
        return newEntityName;
    }

    public void setNewEntityName(String entityName) {
        this.newEntityName = entityName;
    }

    public String getNewCotName() {
        return newCotName;
    }

    public void setNewCotName(String cotName) {
        this.newCotName = cotName;
    }

    public String getSelectedCot() {
        return selectedCot;
    }

    public void setSelectedCot(String selectedCot) {
        this.selectedCot = selectedCot;
    }

    public List<SelectItem> getAvailableRealmsList() {
        availableRealmsList = new ArrayList<SelectItem>();
        RealmsBean rlmbean = RealmsBean.getInstance();
        availableRealmsList = rlmbean.getRealmBeanItems();
        RealmBean baseRealmBean = rlmbean.getBaseRealmBean();
        availableRealmsList.add(0, new SelectItem(baseRealmBean, baseRealmBean.getTitle()));
        return availableRealmsList;
    }

    public List<SelectItem> getAvailableCotList() {
        availableCotList = new ArrayList<SelectItem>();
        availableCotList = cotsBean.getCotBeanItems();
        return availableCotList;
    }

    public String getStdMetaFile() {
        return stdMetaFile;
    }

    public void setStdMetaFile(String file) {
        this.stdMetaFile = file;
    }

    public String getStdMetaFilename() {
        return stdMetaFilename;
    }

    public void setStdMetaFilename(String name) {
        this.stdMetaFilename = name;
    }

    public String getExtMetaFile() {
        return extMetaFile;
    }

    public void setExtMetaFile(String file) {
        this.extMetaFile = file;
    }

    public String getExtMetaFilename() {
        return extMetaFilename;
    }

    public void setExtMetaFilename(String name) {
        this.extMetaFilename = name;
    }

    public int getStdMetaFileProgress() {
        return stdMetaFileProgress;
    }

    public void setStdMetaFileProgress(int fileProgress) {
        this.stdMetaFileProgress = fileProgress;
    }

    public int getExtMetaFileProgress() {
        return extMetaFileProgress;
    }

    public void setExtMetaFileProgress(int fileProgress) {
        this.extMetaFileProgress = fileProgress;
    }

    public Effect getSamlV2HostedCreateEntityInputEffect() {
        return samlV2HostedCreateEntityInputEffect;
    }

    public void setSamlV2HostedCreateEntityInputEffect(Effect samlV2HostedCreateEntityInputEffect) {
        this.samlV2HostedCreateEntityInputEffect = samlV2HostedCreateEntityInputEffect;
    }

}
