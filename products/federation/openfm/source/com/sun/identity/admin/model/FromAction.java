package com.sun.identity.admin.model;

import java.util.HashMap;
import java.util.Map;

public enum FromAction {
    HOME("home"),
    POLICY("policy"),
    POLICY_CREATE("policy-create"),
    POLICY_MANAGE("policy-manage"),
    POLICY_EDIT("policy-edit"),
    REFERRAL_CREATE("referral-create"),
    REFERRAL_MANAGE("referral-manage"),
    REFERRAL_EDIT("referral-edit"),
    DELEGATION_CREATE("delegation-create"),
    NEWS("news"),
    FEDERATION("federation"),
    SAMLV2_HOSTED_SP_CREATE("samlv2-hosted-sp-create"),
    SAMLV2_REMOTE_SP_CREATE("samlv2-remote-sp-create"),
    SAMLV2_HOSTED_IDP_CREATE("samlv2-hosted-idp-create"),
    SAMLV2_REMOTE_IDP_CREATE("samlv2-remote-idp-create"),
    WEB_SERVICE_SECURITY("wss"),
    WSP_CREATE("wsp-create"),
    WSP_MANAGE("wsp-manage"),
    WSC_CREATE("wsc-create"),
    WSC_MANAGE("wsc-manage"),
    STS_MANAGE("sts-manage"),
    WEB_SERVICE_SECURITY_CREATE("wss-create"),
    APPLICATION("application"),
    APPLICATION_CREATE("application-create"),
    APPLICATION_MANAGE("application-manage"),
    APPLICATION_EDIT("application-edit"),
    PERMISSION_DENIED("permission-denied");

    private static final Map<String,FromAction> actionValues = new HashMap<String,FromAction>() {
        {
            put(HOME.getAction(), HOME);
            put(POLICY.getAction(), POLICY);
            put(POLICY_CREATE.getAction(), POLICY_CREATE);
            put(POLICY_MANAGE.getAction(), POLICY_MANAGE);
            put(POLICY_EDIT.getAction(), POLICY_EDIT);
            put(REFERRAL_CREATE.getAction(), REFERRAL_CREATE);
            put(REFERRAL_MANAGE.getAction(), REFERRAL_MANAGE);
            put(REFERRAL_EDIT.getAction(), REFERRAL_EDIT);
            put(DELEGATION_CREATE.getAction(), DELEGATION_CREATE);
            put(SAMLV2_HOSTED_SP_CREATE.getAction(), SAMLV2_HOSTED_SP_CREATE);
            put(SAMLV2_REMOTE_SP_CREATE.getAction(), SAMLV2_REMOTE_SP_CREATE);
            put(SAMLV2_HOSTED_IDP_CREATE.getAction(), SAMLV2_HOSTED_IDP_CREATE);
            put(SAMLV2_REMOTE_IDP_CREATE.getAction(), SAMLV2_REMOTE_IDP_CREATE);
            put(FEDERATION.getAction(), FEDERATION);
            put(NEWS.getAction(), NEWS);
            put(WEB_SERVICE_SECURITY.getAction(), WEB_SERVICE_SECURITY);
            put(WSP_CREATE.getAction(), WSP_CREATE);
            put(WSP_MANAGE.getAction(), WSP_MANAGE);
            put(WSC_CREATE.getAction(), WSC_CREATE);
            put(WSC_MANAGE.getAction(), WSC_MANAGE);
            put(STS_MANAGE.getAction(), STS_MANAGE);
            put(WEB_SERVICE_SECURITY_CREATE.getAction(), WEB_SERVICE_SECURITY_CREATE);
            put(APPLICATION.getAction(), APPLICATION);
            put(APPLICATION_CREATE.getAction(), APPLICATION_CREATE);
            put(APPLICATION_EDIT.getAction(), APPLICATION_EDIT);
            put(APPLICATION_MANAGE.getAction(), APPLICATION_MANAGE);
            put(PERMISSION_DENIED.getAction(), PERMISSION_DENIED);
        }
    };

    private String action;

    FromAction(String action) {
        this.action = action;
    }

    public Permission toPermission() {
        return Permission.valueOf(this.toString());
    }

    public String getAction() {
        return action;
    }

    public static FromAction valueOfAction(String action) {
        return actionValues.get(action);
    }

    public ViewId getViewId() {
        return ViewId.valueOf(this.toString());
    }
}
