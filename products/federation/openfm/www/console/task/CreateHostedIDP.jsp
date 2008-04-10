<%--
   The contents of this file are subject to the terms
   of the Common Development and Distribution License
   (the License). You may not use this file except in
   compliance with the License.

   You can obtain a copy of the License at
   https://opensso.dev.java.net/public/CDDLv1.0.html or
   opensso/legal/CDDLv1.0.txt
   See the License for the specific language governing
   permission and limitations under the License.

   When distributing Covered Code, include this CDDL
   Header Notice in each file and include the License file
   at opensso/legal/CDDLv1.0.txt.
   If applicable, add the following below the CDDL Header,
   with the fields enclosed by brackets [] replaced by
   your own identifying information:
   "Portions Copyrighted [year] [name of copyright owner]"

   $Id: CreateHostedIDP.jsp,v 1.7 2008-04-10 23:15:05 veiming Exp $

   Copyright 2008 Sun Microsystems Inc. All Rights Reserved
--%>

<%@ page info="CreateHostedIDP" language="java" %>
<%@taglib uri="/WEB-INF/jato.tld" prefix="jato" %>
<%@taglib uri="/WEB-INF/cc.tld" prefix="cc" %>
<jato:useViewBean
    className="com.sun.identity.console.task.CreateHostedIDPViewBean"
    fireChildDisplayEvents="true" >

<cc:i18nbundle baseName="amConsole" id="amConsole"
    locale="<%=((com.sun.identity.console.base.AMViewBeanBase)viewBean).getUserLocale()%>"/>

<cc:header name="hdrCommon" pageTitle="webconsole.title" bundleID="amConsole" copyrightYear="2008" fireDisplayEvents="true">

<link rel="stylesheet" type="text/css" href="../console/css/opensso.css" />

<script language="javascript" src="../console/js/am.js"></script>
<script language="javascript" src="../com_sun_web_ui/js/dynamic.js"></script>

<div id="main" style="position: absolute; margin: 0; border: none; padding: 0; width:auto; height:1000">

<cc:form name="CreateHostedIDP" method="post">
<jato:hidden name="szCache" />
<script language="javascript">
    function confirmLogout() {
        return confirm("<cc:text name="txtLogout" defaultValue="masthead.logoutMessage" bundleID="amConsole"/>");
    }

    function openWindow(fieldName) {
        selectWin = window.open('../federation/FileUploader', fieldName,
            'height=300,width=650,top=' +
            ((screen.height-(screen.height/2))-(500/2)) +
            ',left=' + ((screen.width-650)/2));
        selectWin.focus();
    }

    function metadataOptionSelect(radio) {
        var infodiv = document.getElementById("info");
        var metadiv = document.getElementById("meta");
        hasMetaData = radio.value;
        if (radio.value == 'yes') {
            infodiv.style.display = 'none';
            metadiv.style.display = 'block';
            document.getElementById('cotsection').style.display = 'none';
            document.getElementById('cotq').style.display = 'none';
            document.getElementById('cottf').style.display = 'none';
            document.getElementById('cotchoice').style.display = 'none';
        } else {
            infodiv.style.display = 'block';
            metadiv.style.display = 'none';
            document.getElementById('cotsection').style.display = 'display';
            var realm = frm.elements['CreateHostedIDP.tfRealm'].value;
            getCircleOfTrust(realm);
        }
    }

    function metaOptionSelect(radio) {
        if (radio.value == 'url') {
            frm.elements['CreateHostedIDP.tfMetadataFileURL'].style.display = '';
            frm.elements['CreateHostedIDP.btnMetadata'].style.display = 'none';
            document.getElementById('metadatafilename').style.display = 'none';
        } else {
            frm.elements['CreateHostedIDP.tfMetadataFileURL'].style.display = 'none';
            frm.elements['CreateHostedIDP.btnMetadata'].style.display = '';
            document.getElementById('metadatafilename').style.display = '';
        }
    }

    function extendedOptionSelect(radio) {
        if (radio.value == 'url') {
            frm.elements['CreateHostedIDP.tfExtendedFileURL'].style.display = '';
            frm.elements['CreateHostedIDP.btnExtendedFile'].style.display = 'none';
            document.getElementById('extendedfilename').style.display = 'none';
        } else {
            frm.elements['CreateHostedIDP.tfExtendedFileURL'].style.display = 'none';
            frm.elements['CreateHostedIDP.btnExtendedFile'].style.display = '';
            document.getElementById('extendedfilename').style.display = '';
        }
    }

    function cancelOp() {
        document.location.replace("../task/Home");
        return false;
    }

    function realmSelect(radio) {
    	getCircleOfTrust(radio.value);
    }

    function cotOptionSelect(radio) {
        var ans = radio.value;
        if (ans == 'yes') {
            document.getElementById('cotchoice').style.display = 'block';
            document.getElementById('cottf').style.display = 'none';
            frm.elements['CreateHostedIDP.tfCOT'].value = '';
        } else {
            document.getElementById('cotchoice').style.display = 'none';
            document.getElementById('cottf').style.display = 'block';
        }
    }

    function getExtendedData() {
        var extRadio = getRadioVal(frm, 'CreateHostedIDP.radioExtendedData');
        var extended = (extRadio == 'url') ?
            frm.elements['CreateHostedIDP.tfExtendedFileURL'].value :
            frm.elements['CreateHostedIDP.tfExtendedFile'].value;
        extended = extended.replace(/^\s+/, "");
        extended = extended.replace(/\s+$/, "");
        return extended;
    }

    function getCircleOfTrustFromExt() {
        var extended = getExtendedData();
        if (extended.length == 0) {
            return;
        }

        document.getElementById('dlg').style.top = '400px';
        fade();
        document.getElementById('dlg').innerHTML = '<center>' + 
            msgGetCOTs + '</center>';
        var url = "../console/ajax/AjaxProxy.jsp";
        var params = 'locale=' + userLocale +
            '&class=com.sun.identity.workflow.GetCircleOfTrusts' + 
            '&extendeddata=' + escape(extended);
        ajaxPost(ajaxObj, url, params, circleOfTrust);
    }

    function hideRealm() {
        var frm = document.forms['CreateHostedIDP'];
        var realmobj = frm.elements['CreateHostedIDP.tfRealm'];
        if (realmobj.options.length < 2) {
            document.getElementById('realmlbl').style.display = 'none';
            document.getElementById('realmfld').style.display = 'none';
        }
    }
</script>

<cc:primarymasthead name="mhCommon" bundleID="amConsole"  logoutOnClick="return confirmLogout();" locale="<%=((com.sun.identity.console.base.AMViewBeanBase)viewBean).getUserLocale()%>"/>
<table border="0" cellpadding="10" cellspacing="0" width="100%">
    <tr>
        <td>
        <cc:alertinline name="ialertCommon" bundleID="amConsole" />
        </td>
    </tr>
</table>

<%-- PAGE CONTENT --------------------------------------------------------- --%>
<cc:pagetitle name="pgtitle" bundleID="amConsole" pageTitleText="page.title.configure.hosted.idp" showPageTitleSeparator="true" viewMenuLabel="" pageTitleHelpMessage="" showPageButtonsTop="true" showPageButtonsBottom="false" />

<cc:propertysheet name="propertyAttributes" bundleID="amConsole" showJumpLinks="false"/>

</cc:form>
</cc:header>
</div>
<div id="dlg" class="dvs" style="height:200px"></div>

<script language="javascript">
    hideRealm();

    var msgConfiguring = "<cc:text name="txtConfiguring" defaultValue="configure.provider.waiting" bundleID="amConsole" escape="false" />";

    var msgConfigured = '<cc:text name="txtConfigured" defaultValue="configure.provider.done" bundleID="amConsole" escape="false" /><p><div class="TtlBtnDiv"><input name="yesSp" type="submit" class="Btn1" value="<cc:text name="txtYesBtnSP" defaultValue="ajax.yes.sp.button" bundleID="amConsole" escape="false" />" onClick="createRemoteSP();return false;" /> <input name="yesFedlet" type="submit" class="Btn1" value="<cc:text name="txtYesBtnSP" defaultValue="ajax.yes.fedlet.button" bundleID="amConsole" escape="false" />" onClick="createFedlet();return false;" /> <input name="noSp" type="submit" class="Btn1" value="<cc:text name="txtCloseBtn" defaultValue="ajax.neither.button" bundleID="amConsole" escape="false" />" onClick="document.location.replace(\'../task/Home\');return false;" /></div></p>';

    var closeBtn = '<p>&nbsp;</p><p><div class="TtlBtnDiv"><input name="btnClose" type="submit" class="Btn1" value="<cc:text name="txtCloseBtn" defaultValue="ajax.close.button" bundleID="amConsole" escape="false" />" onClick="focusMain();return false;" /></div></p>';

    var msgGetCOTs = "<cc:text name="txtConfigured" defaultValue="configure.provider.get.cots" bundleID="amConsole" escape="false" />";

    var msgMissingAttrMappingValues = "<cc:text name="txtMissingAttrValues" defaultValue="configure.provider.missing.attribute.mapping.values" bundleID="amConsole" escape="false" />" + "<p>" + closeBtn + "</p>";

    var hasMetaData = 'no';
    var frm = document.forms['CreateHostedIDP'];
    var btn1 = frm.elements['CreateHostedIDP.button1'];
    btn1.onclick = submitPage;
    var btn2 = frm.elements['CreateHostedIDP.button2'];
    btn2.onclick = cancelOp;
    var ajaxObj = getXmlHttpRequestObject();
    var data = '';
    var selectOptionCache;
    var userLocale = "<% viewBean.getUserLocale().toString(); %>";

    function submitPage() {
        document.getElementById('dlg').style.top = '300px';
        fade();
        if (document.getElementById('cotsection').style.display != 'block') {
            var extended = getExtendedData();
            if (extended.length > 0) {
                if (hasMetaData) {
                    getCircleOfTrustFromExt();
                } else {
                    var realm = frm.elements['CreateHostedIDP.tfRealm'].value;
                    getCircleOfTrust(realm);
                }
                focusMain();
                return false;
            }
        }
        document.getElementById('dlg').innerHTML = '<center>' + 
        msgConfiguring + '</center>';
        var url = "../console/ajax/AjaxProxy.jsp";
        var params = 'locale=' + userLocale +
            '&class=com.sun.identity.workflow.CreateHostedIDP' + getData();
        ajaxPost(ajaxObj, url, params, configured);
        return false;
    }

    function getData() {
        var cot;
        var cotRadio = getRadioVal(frm, 'CreateHostedIDP.radioCOT');
        if (cotRadio == "yes") {
            cot = frm.elements['CreateHostedIDP.choiceCOT'].value;
        } else {
            cot = frm.elements['CreateHostedIDP.tfCOT'].value;
        }
        if (hasMetaData == "yes") {
            var metaRadio = getRadioVal(frm, 'CreateHostedIDP.radioMeta');
            var meta = (metaRadio == 'url') ?
                frm.elements['CreateHostedIDP.tfMetadataFileURL'].value :
                frm.elements['CreateHostedIDP.tfMetadataFile'].value;
            var extRadio = getRadioVal(frm, 'CreateHostedIDP.radioExtendedData');
            var extended = (extRadio == 'url') ?
                frm.elements['CreateHostedIDP.tfExtendedFileURL'].value :
                frm.elements['CreateHostedIDP.tfExtendedFile'].value;

            return "&metadata=" + escape(meta) +
                "&extendeddata=" + escape(extended) +
                "&cot=" + escape(cot) +
                "&attributemappings=" + escape(getNameAttributeMapping());
        } else {
            var realm = frm.elements['CreateHostedIDP.tfRealm'].value;
            return "&entityId=" +
            escape(frm.elements['CreateHostedIDP.tfEntityId'].value) +
            "&realm=" + escape(realm) +
            "&idpscert=" +
            escape(frm.elements['CreateHostedIDP.tfSigningKey'].value) +
            "&cot=" + escape(cot) +
            "&attributemappings=" + escape(getNameAttributeMapping());
        }
    }

    function getNameAttributeMapping() {
        var attrMappings = '';
        var table = getActionTable();
        var rows = table.getElementsByTagName('TR');
        for (var i = rows.length-1; i >=3; --i) {
            var inputs = rows[i].getElementsByTagName('input');
            var cb = inputs[0];
            attrMappings += cb.getAttribute("value") + '|';
        }
        return attrMappings;
    }

    function getCircleOfTrust(realm) {
        var url = "../console/ajax/AjaxProxy.jsp";
        var params = 'locale=' + userLocale +
            '&class=com.sun.identity.workflow.GetCircleOfTrusts' + 
            '&realm=' + escape(realm);
        ajaxPost(ajaxObj, url, params, circleOfTrust);
    }

    function circleOfTrust() {
        if (ajaxObj.readyState == 4) {
            var result = ajaxObj.responseText;
            var status = result.substring(0, result.indexOf('|'));
            var result = result.substring(result.indexOf('|') +1);
            var msg = '';
            if (status == 0) {
                document.getElementById('cotsection').style.display = 'block';
                result = result.replace(/^\s+/, '');
                result = result.replace(/\s+$/, '');
                if (result.length == 0) {
                    document.getElementById('cotq').style.display = 'none';
                    document.getElementById('cotchoice').style.display = 'none';
                    document.getElementById('cottf').style.display = 'block';
                    chooseRadio(frm, 'CreateHostedIDP.radioCOT', 'no');
                } else {
                    var cots = result.split('|');
                    var choiceCOT = frm.elements['CreateHostedIDP.choiceCOT'];
                    for (var i = 0; i < cots.length; i++) {
                        choiceCOT.options[i] = new Option(cots[i], cots[i]);
                    }
                    document.getElementById('cotq').style.display = 'block';
                    document.getElementById('cotchoice').style.display = 'block';
                    document.getElementById('cottf').style.display = 'none';
                    chooseRadio(frm, 'CreateHostedIDP.radioCOT', 'yes');
                }
                focusMain();
            } else {
                msg = '<center><p>' + result + '</p></center>';
	        msg = msg + '<center>' +  closeBtn + '</center>';
                document.getElementById('dlg').innerHTML = msg;
                document.getElementById('cotsection').style.display = 'none';
                ajaxObj = getXmlHttpRequestObject();
            }
        }
    }

    function createFedlet() {
        var cot;
        var cotRadio = getRadioVal(frm, 'CreateHostedIDP.radioCOT');
        if (cotRadio == "yes") {
            cot = frm.elements['CreateHostedIDP.choiceCOT'].value;
        } else {
            cot = frm.elements['CreateHostedIDP.tfCOT'].value;
        }
        document.location.replace('CreateFedlet?cot=' + cot + '&' + data);
    }

    function createRemoteSP() {
        var cot;
        var cotRadio = getRadioVal(frm, 'CreateHostedIDP.radioCOT');
        if (cotRadio == "yes") {
            cot = frm.elements['CreateHostedIDP.choiceCOT'].value;
        } else {
            cot = frm.elements['CreateHostedIDP.tfCOT'].value;
        }
        document.location.replace('CreateRemoteSP?cot=' + cot + '&' + data);
    }

    function configured() {
        if (ajaxObj.readyState == 4) {
            var result = ajaxObj.responseText;
            var status = result.substring(0, result.indexOf('|'));
            var result = result.substring(result.indexOf('|') +1);
            var msg = '';
            if (status == 0) {
                var idx = result.indexOf('|||');
                data = result.substring(idx +3);
                result = result.substring(0, idx);
                msg = '<center><p>' + result + '</p></center>';
                msg = msg + '<center>' +  msgConfigured + '</center>';
            } else {
                msg = '<center><p>' + result + '</p></center>';
		msg = msg + '<center>' +  closeBtn + '</center>';
                ajaxObj = getXmlHttpRequestObject();
            }
            document.getElementById('dlg').innerHTML = msg;
        }
    }

    function addAttrMapping() {
        var name = frm.elements['CreateHostedIDP.tfAttrMappingName'].value;
        var assertn = frm.elements['CreateHostedIDP.tfAttrMappingAssertion'].value;
        name = name.replace(/^\s+/, '');
        name = name.replace(/\s+$/, '');
        assertn = assertn.replace(/^\s+/, '');
        assertn = assertn.replace(/\s+$/, '');
        if ((name == '') || (assertn == '')) {
            document.getElementById('dlg').style.top = '600px';
            fade();
            document.getElementById('dlg').innerHTML = '<center>' + 
                msgMissingAttrMappingValues  + '</center>';
        } else {
            addPropertyRow(name, assertn);
            frm.elements['CreateHostedIDP.tfAttrMappingName'].value = '';
            frm.elements['CreateHostedIDP.tfAttrMappingAssertion'].value = '';
            var userAttrs = frm.elements['CreateHostedIDP.menuUserAttributes'];
            if (userAttrs.options[0].values != '') {
                var cache = new Array();
                for (var i = 0; i < userAttrs.options.length; i++) {
                    cache[i] = userAttrs.options[i];
                }
                userAttrs.options[0] = selectOptionCache;
                for (var i = 0; i < cache.length; i++) {
                    userAttrs.options[i+1] = cache[i];
                }
            }
            userAttrs.selectedIndex = 0;
        }
    }

    function addPropertyRow(name, assertn) {
        var table = getActionTable();
        var tBody = table.getElementsByTagName("TBODY").item(0);
        var row = document.createElement("TR");
        var cell1 = document.createElement("TD");
        var cell2 = document.createElement("TD");
        var cell3 = document.createElement("TD");

        cell1.setAttribute("align", "center");
        cell1.setAttribute("valign", "top");

        var cb = document.createElement("input");
        var textnode1 = document.createTextNode(assertn);
        var textnode2 = document.createTextNode(name);
        cb.setAttribute("type", "checkbox");
        cb.setAttribute("value", assertn + "=" + name);
        cb.setAttribute("onclick", "toggleTblButtonState('CreateHostedIDP', 'CreateHostedIDP.tblattrmapping', 'tblButton', 'CreateHostedIDP.deleteAttrMappingBtn', this)");
        cell1.appendChild(cb);
        cell2.appendChild(textnode1);
        cell3.appendChild(textnode2);
        row.appendChild(cell1);
        row.appendChild(cell2);
        row.appendChild(cell3);
        tBody.appendChild(row);
    }

    function getActionTable() {
        var nodes = document.getElementsByTagName("table");
        var len = nodes.length;
        for (var i = 0; i < len; i++) {
            if (nodes[i].className == 'Tbl') {
                return nodes[i];
            }
        }
     } 

    function deletePropertyRow() {
        var table = getActionTable();
        var rows = table.getElementsByTagName('TR');
        for (var i = rows.length-1; i >=3; --i) {
            var inputs = rows[i].getElementsByTagName('input');
            var cb = inputs[0];
            if (cb.checked) {
                table.deleteRow(i-1);
            }
        }
        tblBtnCounter['tblButton'] = 0;
        ccSetButtonDisabled('CreateHostedIDP.deleteAttrMappingBtn', 'CreateHostedIDP', true);
        return false;
    }

    function signKeySelect(menu) {
        if (menu.value == 'test') {
            document.getElementById('signTest').style.display = '';
        } else {
            document.getElementById('signTest').style.display = 'none';
        }
    }

    function userAttrSelect(menu) {
        if (menu.options[0].value == '') {
            selectOptionCache = menu.options[0];
            menu.options[0] = null;
        }
        frm.elements['CreateHostedIDP.tfAttrMappingName'].value = menu.value;
    }

    frm.elements['CreateHostedIDP.tfMetadataFileURL'].style.display = 'none';
    frm.elements['CreateHostedIDP.tfExtendedFileURL'].style.display = 'none';
    getCircleOfTrust('/');
    getActionTable().deleteRow(2);
</script>

</jato:useViewBean>
