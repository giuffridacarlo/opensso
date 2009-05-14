package com.sun.identity.admin.dao;

import com.sun.identity.admin.model.IdRepoGroupViewSubject;
import com.sun.identity.admin.model.ViewSubject;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdType;
import java.io.Serializable;

public class IdRepoRoleSubjectDao extends IdRepoSubjectDao implements Serializable {
    protected IdType getIdType() {
        return IdType.GROUP;
    }

    protected ViewSubject newViewSubject(AMIdentity ami) {
        IdRepoGroupViewSubject gvs = (IdRepoGroupViewSubject)getSubjectType().newViewSubject();
        gvs.setName(ami.getUniversalId());
        
        return gvs;
    }

    public void decorate(ViewSubject vs) {
        // TODO
        // any role decoration?
    }

}
