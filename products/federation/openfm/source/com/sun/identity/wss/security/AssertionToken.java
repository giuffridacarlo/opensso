/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: AssertionToken.java,v 1.7 2009-05-09 15:44:00 mallas Exp $
 *
 */


package com.sun.identity.wss.security;

import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.text.ParseException;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.RSAPublicKey;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import com.iplanet.sso.SSOException;
import com.sun.identity.shared.xml.XMLUtils;
import com.sun.identity.shared.encode.Base64;
import com.sun.identity.common.SystemConfigurationUtil;

import com.sun.identity.shared.DateUtils;
import com.sun.identity.shared.Constants;
import com.sun.identity.saml.assertion.Assertion;
import com.sun.identity.saml.assertion.NameIdentifier;
import com.sun.identity.saml.assertion.AuthenticationStatement;
import com.sun.identity.saml.assertion.Subject;
import com.sun.identity.saml.assertion.SubjectConfirmation;
import com.sun.identity.saml.assertion.SubjectStatement;
import com.sun.identity.saml.common.SAMLException;
import com.sun.identity.saml.common.SAMLConstants;
import com.sun.identity.saml.assertion.AttributeStatement;
import com.sun.identity.saml.assertion.Attribute;


/**
 * This class implements the interface <code>SecurityToken</code> for the 
 * SAML Assertions.
 */
public class AssertionToken implements SecurityToken {

      private SSOToken ssoToken = null;
      private String authType = "";
      private String authTime = "";
      private String certAlias = null;
      private Assertion assertion = null;
      private Element assertionE = null;
      private static final String KEY_INFO_TYPE =
         "com.sun.identity.liberty.ws.security.keyinfotype";
      
      private static String keyInfoType = SystemConfigurationUtil.getProperty(
                                          KEY_INFO_TYPE);
      /**
       * Constructor that initializes the AssertionToken.
       */ 
      public AssertionToken(AssertionTokenSpec spec, 
                  SSOToken ssoToken) throws SecurityException {
 
          if(spec == null) {
             WSSUtils.debug.error("AssertionToken: constructor: Assertion" +
                  " Token specification is null");
             throw new SecurityException(
                   WSSUtils.bundle.getString("tokenSpecNotSpecified"));
          }

          validateSSOToken(ssoToken);
          createAssertion(spec);
      }
      
      /**
       * Validates the SSOtoken and extract the required properties.
       */
      private void validateSSOToken(SSOToken ssoToken) 
                      throws SecurityException {
         try {
             SSOTokenManager.getInstance().validateToken(ssoToken);
             authType = ssoToken.getAuthType();
             authTime =  ssoToken.getProperty("authInstant");

         } catch (SSOException se) {
             WSSUtils.debug.error("AssertionToken.validateSSOToken: " +
               "SSOException", se);
             throw new SecurityException(
                   WSSUtils.bundle.getString("invalidSSOToken"));
         }
      }

      /**
       * Creates a SAML Assertion for the given token specification.
       */
      private void createAssertion(AssertionTokenSpec spec) 
                    throws SecurityException{

          SecurityMechanism securityMechanism = spec.getSecurityMechanism();
          NameIdentifier nameIdentifier = spec.getSenderIdentity();
          certAlias = spec.getSubjectCertAlias();

          if(nameIdentifier == null) {
             throw new SecurityException(
                   WSSUtils.bundle.getString("invalidAssertionTokenSpec"));
          }

          String confirmationMethod = spec.getConfirmationMethod();
          if(confirmationMethod == null) {
             confirmationMethod = 
                     getConfirmationMethod(securityMechanism.getURI());
          }

          
          String issuer = spec.getIssuer();
          if(issuer == null) {
             issuer =
                SystemConfigurationUtil.getProperty(Constants.AM_SERVER_HOST);
          }
          Date issueInstant = new Date();
          Set statements = new HashSet();
          AuthenticationStatement authStatement = 
                    createAuthenticationStatement(
                    nameIdentifier,confirmationMethod);
                    
          if(authStatement != null) {
             statements.add(authStatement);
          }
          Map attributes = spec.getClaimedAttributes();
          if(attributes != null && !attributes.isEmpty()) {
             AttributeStatement attrStatement = 
                             createAttributeStatement(spec);
             if(attrStatement != null) {
                statements.add(attrStatement);
             }
          }

          if(WSSUtils.debug.messageEnabled()) {
             WSSUtils.debug.message("AssertionToken.createAssertion: " +
              "Assertion constructs:\n" +
              "Confirmation method: " + confirmationMethod + "\n" +
              "Issuer: " + issuer + "\n");
          }

          try {
              assertion = new Assertion(spec.getAssertionID(), issuer,
                      issueInstant, statements); 
          } catch (SAMLException se) {
              WSSUtils.debug.error("AssertionToken.createAssertion: " +
               "SAMLException in creating the assertion.", se);
              throw new SecurityException(
                   WSSUtils.bundle.getString("unabletoGenerateAssertion"));
          }
      }

      /**
       * Returns the confirmation method for the given security mech.
       */
      private String getConfirmationMethod(String securityURI) 
               throws SecurityException {

          if(securityURI == null) {
             throw new SecurityException(
                  WSSUtils.bundle.getString("nullSecurityMechanism"));
          }

          if(securityURI.equals(SecurityMechanism.WSS_NULL_SAML_HK_URI)||
             securityURI.equals(SecurityMechanism.WSS_TLS_SAML_HK_URI) ||
             securityURI.equals(SecurityMechanism.WSS_CLIENT_TLS_SAML_HK_URI)){
             return SAMLConstants.CONFIRMATION_METHOD_HOLDEROFKEY;

          } else if(
             securityURI.equals(SecurityMechanism.WSS_NULL_SAML_SV_URI)||
             securityURI.equals(SecurityMechanism.WSS_TLS_SAML_SV_URI) ||
             securityURI.equals(SecurityMechanism.WSS_CLIENT_TLS_SAML_SV_URI)){
             return SAMLConstants.CONFIRMATION_METHOD_SENDERVOUCHES;

          } else {
             throw new SecurityException(
                   WSSUtils.bundle.getString("invalidConfirmationMethod"));
          }
      }

      /**
       * Creates an authentication statement.
       */
      private AuthenticationStatement createAuthenticationStatement(
            NameIdentifier nameIdentifier,
            String confirmationMethod) throws SecurityException {

          AuthenticationStatement authStatement = null;
          String authMethod = WSSUtils.getAuthMethodURI(authType);
          try {
              Date authInstant = DateUtils.stringToDate(authTime);
              Subject subject = null;
              
              if(confirmationMethod == null) {
                 throw new SecurityException(
                       WSSUtils.bundle.getString("nullConfirmationMethod"));
              }

              SubjectConfirmation subConfirmation = null;

              if(confirmationMethod.equals(
                   SAMLConstants.CONFIRMATION_METHOD_HOLDEROFKEY)) {
                 subConfirmation = new SubjectConfirmation(confirmationMethod);
                 subConfirmation.setKeyInfo(createKeyInfo());
 
              } else if(confirmationMethod.equals(
                   SAMLConstants.CONFIRMATION_METHOD_SENDERVOUCHES)) {
                 subConfirmation =  new SubjectConfirmation(confirmationMethod);

              } else {
                 throw new SecurityException(
                       WSSUtils.bundle.getString("invalidConfirmationMethod"));
              }
            
              subject = new Subject(nameIdentifier, subConfirmation);
              authStatement = new AuthenticationStatement(authMethod,
                              authInstant,  subject);

          } catch (SAMLException se) {
              WSSUtils.debug.error("AssertionToken.getAuthenticationStatement:"+
              "Failed to generate the authentication statement.", se);
              throw new SecurityException(
                       WSSUtils.bundle.getString("unabletoGenerateAssertion"));

          } catch (ParseException pe) {
              WSSUtils.debug.error("AssertionToken.getAuthenticationStatement:"+
              "Failed to generate the authentication statement.", pe);
              throw new SecurityException(
                       WSSUtils.bundle.getString("unabletoGenerateAssertion"));
          }

          return authStatement;
      }

      /** 
       * Returns the security token type.
       * @return String SAMLToken type.
       */
      public String getTokenType() {
          return SecurityToken.WSS_SAML_TOKEN;
      }

      /**
       * Convert the security token into DOM Object.
       * 
       * @return the DOM Document Element.
       *
       * @exception SecurityException if any failure is occured.
       */
      public Element toDocumentElement() throws SecurityException {

          if(assertionE != null) {
             return assertionE; 
          }
          Document document = XMLUtils.toDOMDocument(
                   assertion.toString(true, true), WSSUtils.debug); 
          if(document == null) {
             throw new SecurityException(
                 WSSUtils.bundle.getString("cannotConvertToDocument"));
          }
          return document.getDocumentElement();
      }

      public AssertionToken(Element element) 
                   throws SAMLException {
          assertionE = element;
          assertion = new Assertion(element);
      }

      public boolean isSenderVouches() {

        Set statements = assertion.getStatement();
        if (statements == null || statements.isEmpty()) {
            return false;
        }

        Iterator iter = statements.iterator();
        while(iter.hasNext()) {
            Object statement = iter.next();
            if (!(statement instanceof SubjectStatement)) {
                continue;
            }
            Subject subject = ((SubjectStatement)statement).getSubject();
            if (subject == null) {
                continue;
            }
            SubjectConfirmation sc = subject.getSubjectConfirmation();
            if (sc == null) {
                continue;
            }
            Set confirmationMethods = sc.getConfirmationMethod();
            if (confirmationMethods == null || confirmationMethods.isEmpty()) {
                continue;
            }
            if (confirmationMethods.contains(
                        SAMLConstants.CONFIRMATION_METHOD_SENDERVOUCHES)) {
                return true;
            }
        }
        return false;
      }

      /**
       * Returns X509 certificate of the authenticated subject.
       */
      public X509Certificate getX509Certificate() throws SecurityException {
          X509Certificate cert = 
                AMTokenProvider.getKeyProvider().getX509Certificate(certAlias);
          if(cert == null) {
             WSSUtils.debug.error("AssertionToken.getX509Certificate: " +
             "Could not get certificate for alias : " + certAlias);
             throw new SecurityException(
                   WSSUtils.bundle.getString("noCertificate"));
          }
          return cert;
      }

      /**
       * Creates keyinfo for the subject confirmation.
       */
      private Element createKeyInfo() throws SecurityException {

        X509Certificate cert = getX509Certificate();
        Document doc = null;
        try {
            doc = XMLUtils.newDocument();
        } catch (Exception e) {
            throw new SecurityException(e.getMessage());
        }

        String keyNameTextString = null;
        String base64CertString = null;

        PublicKey pk = null;
        try {
            pk = cert.getPublicKey();
            keyNameTextString = cert.getSubjectDN().getName();
            base64CertString = Base64.encode(cert.getEncoded());
        } catch (Exception e) {
            WSSUtils.debug.error("AssertionToken.createKeyInfo: ", e);
            throw new SecurityException(e.getMessage());
        }

        Element keyInfo = doc.createElementNS(
                            SAMLConstants.XMLSIG_NAMESPACE_URI,
                            SAMLConstants.TAG_KEYINFO);
        keyInfo.setAttribute("xmlns", SAMLConstants.XMLSIG_NAMESPACE_URI);

        if ( (keyInfoType!=null) && 
               (keyInfoType.equalsIgnoreCase("certificate")) ) {
          //put Certificate in KeyInfo
            Element x509Data = doc.createElementNS(
                                SAMLConstants.XMLSIG_NAMESPACE_URI,
                                SAMLConstants.TAG_X509DATA);
            Element x509Certificate = doc.createElementNS(
                                SAMLConstants.XMLSIG_NAMESPACE_URI,
                                SAMLConstants.TAG_X509CERTIFICATE);
            Text certText = doc.createTextNode(base64CertString);
            x509Certificate.appendChild(certText);
            keyInfo.appendChild(x509Data).appendChild(x509Certificate);
        } else {
            //put public key in keyinfo
            Element keyName = doc.createElementNS(
                            SAMLConstants.XMLSIG_NAMESPACE_URI,
                            SAMLConstants.TAG_KEYNAME);
            Text keyNameText = doc.createTextNode(keyNameTextString);

            Element keyvalue = doc.createElementNS(
                            SAMLConstants.XMLSIG_NAMESPACE_URI,
                            SAMLConstants.TAG_KEYVALUE);

            if (pk.getAlgorithm().equals("DSA")) {
                DSAPublicKey dsakey = (DSAPublicKey) pk;
                DSAParams dsaParams = dsakey.getParams();
                BigInteger _p = dsaParams.getP();
                BigInteger _q = dsaParams.getQ();
                BigInteger _g = dsaParams.getG();
                BigInteger _y = dsakey.getY();
                Element DSAKeyValue = doc.createElementNS(
                            SAMLConstants.XMLSIG_NAMESPACE_URI
                            , "DSAKeyValue");
                Element p = doc.createElementNS(
                                SAMLConstants.XMLSIG_NAMESPACE_URI, "P");
                Text value_p =
                        doc.createTextNode(Base64.encode(_p.toByteArray()));
                p.appendChild(value_p);
                DSAKeyValue.appendChild(p);

                Element q = doc.createElementNS(
                                SAMLConstants.XMLSIG_NAMESPACE_URI, "Q");
                Text value_q =
                        doc.createTextNode(Base64.encode(_q.toByteArray()));
                q.appendChild(value_q);
                DSAKeyValue.appendChild(q);

                Element g = doc.createElementNS(
                                SAMLConstants.XMLSIG_NAMESPACE_URI, "G");
                Text value_g =
                        doc.createTextNode(Base64.encode(_g.toByteArray()));
                g.appendChild(value_g);
                DSAKeyValue.appendChild(g);

                Element y = doc.createElementNS(
                                SAMLConstants.XMLSIG_NAMESPACE_URI, "Y");
                Text value_y =
                        doc.createTextNode(Base64.encode(_y.toByteArray()));
                y.appendChild(value_y);
                DSAKeyValue.appendChild(y);
                keyvalue.appendChild(DSAKeyValue);

            } else {
                // It is RSA
                RSAPublicKey rsakey = (RSAPublicKey) pk;
                BigInteger exponent = rsakey.getPublicExponent();
                BigInteger modulus  = rsakey.getModulus();
                Element RSAKeyValue = doc.createElementNS(
                                        SAMLConstants.XMLSIG_NAMESPACE_URI
                                        , "RSAKeyValue");
                Element modulusNode = doc.createElementNS(
                                        SAMLConstants.XMLSIG_NAMESPACE_URI
                                        , "Modulus");
                Element exponentNode = doc.createElementNS(
                                        SAMLConstants.XMLSIG_NAMESPACE_URI
                                        , "Exponent");
                RSAKeyValue.appendChild(modulusNode);
                RSAKeyValue.appendChild(exponentNode);
                Text modulusValue =
                    doc.createTextNode(Base64.encode(modulus.toByteArray()));
                modulusNode.appendChild(modulusValue);
                Text exponentValue =
                    doc.createTextNode(Base64.encode(exponent.toByteArray()));
                exponentNode.appendChild(exponentValue);
                keyvalue.appendChild(RSAKeyValue);
            }

            keyInfo.appendChild(keyName).appendChild(keyNameText);
            keyInfo.appendChild(keyvalue);
        }
        return keyInfo;
    }

     /**
      * Signs the Assertion Token.
      *
      * @exception SecurityException if unable to sign the assertion.
      */
     public void sign(String alias) throws SecurityException {
         try { 
            assertion.signXML(alias);
         } catch (SAMLException se) {
            WSSUtils.debug.error("AssertionToken.sign: exception", se);
            throw new SecurityException(
                      WSSUtils.bundle.getString("unabletoSign"));
         }
     }

     /**
      * Returns the generated <code>SAML Assertion</code>.
      *
      * @return <code>Assertion</code> generated saml assertion.
      */
     public Assertion getAssertion() {
         return assertion;
     }
     
     private AttributeStatement createAttributeStatement(
             AssertionTokenSpec spec) throws SecurityException {
         Map attributes = spec.getClaimedAttributes();         
         if(attributes == null) {
            return null;
         }         
         try {
             List samlAttributes = new ArrayList();         
             Iterator iter = attributes.keySet().iterator();
             while(iter.hasNext()) {
                 QName qName = (QName)iter.next();
                 String attrName = qName.getLocalPart();
                 String nameSpace = qName.getNamespaceURI();
                 if("NameID".equals(qName.getLocalPart())) {
                    continue; 
                 }
                 List values = (List)attributes.get(qName);
                 if(values == null || values.isEmpty()) {
                    continue; 
                 }
                 
                 List elementValues = new ArrayList();
                 for (Iterator iter1=values.iterator(); iter1.hasNext();) {
                     String value = (String)iter1.next();
                     String attrValue = "<AttributeValue>" + value +
                              "</AttributeValue>";
                     Element valueE =  XMLUtils.toDOMDocument(
                             attrValue, WSSUtils.debug).getDocumentElement();
                     elementValues.add(valueE);
                 }
                 
                 Attribute attr = new Attribute(attrName, nameSpace,
                         elementValues);             
                 samlAttributes.add(attr);            
             } 
             if(samlAttributes.isEmpty()) {
                return null;
             }
             Subject subject = new Subject(spec.getSenderIdentity());
             AttributeStatement attrStatement = 
                    new AttributeStatement(subject, samlAttributes);         
             return attrStatement;
         } catch (SAMLException se) {
             WSSUtils.debug.error("AssertionToken.createAttributeStatement: " +
                     "Unable to create attribute statement", se);
             throw new SecurityException(se.getMessage());
         }
     }

}
