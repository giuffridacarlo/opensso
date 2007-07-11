/* The contents of this file are subject to the terms
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
 * $Id: WSSEncryptionProvider.java,v 1.1 2007-07-11 06:12:45 mrudul_uchil Exp $
 *
 * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.identity.wss.xmlenc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.security.Key;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;
import java.util.ArrayList;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import com.sun.org.apache.xml.internal.security.encryption.EncryptedData;
import com.sun.org.apache.xml.internal.security.encryption.EncryptedKey;
import com.sun.org.apache.xml.internal.security.encryption.ReferenceList;
import com.sun.org.apache.xml.internal.security.encryption.Reference;
import com.sun.org.apache.xml.internal.security.encryption.EncryptionMethod;
import com.sun.org.apache.xml.internal.security.encryption.
       XMLEncryptionException;
import com.sun.org.apache.xml.internal.security.keys.KeyInfo;
import com.sun.org.apache.xml.internal.security.utils.IdResolver;
import com.sun.org.apache.xml.internal.security.keys.content.X509Data;

import com.sun.identity.xmlenc.EncryptionException;
import com.sun.identity.xmlenc.EncryptionUtils;
import com.sun.identity.xmlenc.AMEncryptionProvider;
import com.sun.identity.xmlenc.EncryptionConstants;

import java.security.cert.X509Certificate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import com.sun.identity.wss.security.WSSConstants;
import com.sun.identity.wss.security.WSSUtils;
import com.sun.identity.wss.security.SecurityToken;
import com.sun.identity.saml.common.SAMLConstants;
import com.sun.identity.saml.common.SAMLUtils;
import com.sun.org.apache.xml.internal.security.keys.storage.StorageResolver;
import com.sun.org.apache.xml.internal.security.keys.storage.
       implementations.KeyStoreResolver;
import com.sun.org.apache.xml.internal.security.keys.keyresolver.
       implementations.X509CertificateResolver;
import com.sun.org.apache.xml.internal.security.keys.keyresolver.
       implementations.X509SubjectNameResolver;
import com.sun.org.apache.xml.internal.security.keys.keyresolver.
       implementations.X509IssuerSerialResolver;
import com.sun.org.apache.xml.internal.security.keys.keyresolver.
       implementations.X509SKIResolver;

/**
 * <code>WSSEncryptionProvider</code> is a class for encrypting and 
 * decrypting WSS XML Documents which implements 
 * <code>AMEncryptionProvider</code>.
 */ 
public class WSSEncryptionProvider extends AMEncryptionProvider {
    
    /** Creates a new instance of WSSEncryptionProvider */
    public WSSEncryptionProvider() {
        super();
    }


    /**
     * Encrypts the given WSS XML element in a given XML Context document.
     * @param doc the context XML Document.
     * @param element Element to be encrypted.
     * @param encDataEncAlg Encryption Key Algorithm.
     * @param encDataEncAlgStrength Encryption Key Strength.
     * @param certAlias Key Encryption Key cert alias.
     * @param kekStrength Key Encryption Key Strength.
     * @param tokenType Security token type.     
     * @param providerID Provider ID.
     * @return org.w3c.dom.Document XML Document replaced with encrypted data
     *         for a given XML element.
     */
    public org.w3c.dom.Document encryptAndReplaceWSSBody(
        org.w3c.dom.Document doc,
        org.w3c.dom.Element element,
        java.lang.String encDataEncAlg,
        int encDataEncAlgStrength,
        String certAlias,
        int kekStrength,
        java.lang.String tokenType,
        java.lang.String providerID)
     throws EncryptionException {
        Document resultDoc = null;
        
        java.security.Key kek = keyProvider.getPublicKey(certAlias);
        
        if(doc == null || element == null || kek == null) { 
           EncryptionUtils.debug.error("WSSEncryptionProvider.encryptAnd" +
           "ReplaceWSSBody: Null values for doc or element or public key");
           throw new EncryptionException(EncryptionUtils.bundle.getString(
            "nullValues"));
        }

        if(EncryptionUtils.debug.messageEnabled()) {
            EncryptionUtils.debug.message("WSSEncryptionProvider.encrypt" +
                "AndReplaceWSSBody: DOC input = " 
                + WSSUtils.print(doc));
        }

        org.w3c.dom.Element root = (Element) doc.getDocumentElement().
            getElementsByTagNameNS(WSSConstants.WSSE_NS,
            SAMLConstants.TAG_SECURITY).item(0);
       
        
        SecretKey secretKeyEncData = null;
        if(providerID != null) {
           if(keyMap.containsKey(providerID)) {
              secretKeyEncData = (SecretKey)keyMap.get(providerID);
           } else {
              secretKeyEncData = 
                  generateSecretKey(encDataEncAlg, encDataEncAlgStrength);
              keyMap.put(providerID, secretKeyEncData);
           }
        } else {
           secretKeyEncData = 
               generateSecretKey(encDataEncAlg, encDataEncAlgStrength);
        }

        if(secretKeyEncData == null) {
           throw new EncryptionException(EncryptionUtils.bundle.getString(
           "generateKeyError"));
        }

        // Get SOAP Body id
        String bodyId  = element.getAttribute(WSSConstants.WSU_ID);
        
        try {
            XMLCipher cipher = null;
            
            // ENCRYPTED KEY
            String keyEncAlg = kek.getAlgorithm();

            if(keyEncAlg.equals(EncryptionConstants.RSA)) {
               cipher = XMLCipher.getInstance(XMLCipher.RSA_v1dot5);

            } else if(keyEncAlg.equals(EncryptionConstants.TRIPLEDES)) {
               cipher = XMLCipher.getInstance(XMLCipher.TRIPLEDES_KeyWrap);

            } else if(keyEncAlg.equals(EncryptionConstants.AES)) {

               if (kekStrength == 0 || kekStrength == 128) {
                   cipher = XMLCipher.getInstance(XMLCipher.AES_128_KeyWrap);
               } else if(kekStrength == 192) {
                   cipher = XMLCipher.getInstance(XMLCipher.AES_192_KeyWrap);
               } else if(kekStrength == 256) {
                   cipher = XMLCipher.getInstance(XMLCipher.AES_256_KeyWrap);
               } else {
                   throw new EncryptionException(
                   EncryptionUtils.bundle.getString("invalidKeyStrength"));
               }
            } else {
                  throw new EncryptionException(
                   EncryptionUtils.bundle.getString("unsupportedKeyAlg"));
            } 

            // Encrypt the key with key encryption key 
            cipher.init(XMLCipher.WRAP_MODE, kek);
            EncryptedKey encryptedKey = cipher.encryptKey(doc, 
                secretKeyEncData);
	        KeyInfo insideKi = new KeyInfo(doc);
            X509Data x509Data = new X509Data(doc);
            x509Data.addCertificate((X509Certificate)
			    keyProvider.getCertificate((PublicKey) kek));
            insideKi.add(x509Data);
	    
            // SecurityTokenReference   
            Element securityTokenRef = doc.createElementNS(WSSConstants.WSSE_NS,
                "wsse:" + SAMLConstants.TAG_SECURITYTOKENREFERENCE);
            securityTokenRef.setAttributeNS(SAMLConstants.NS_XMLNS,
                WSSConstants.TAG_XML_WSSE, WSSConstants.WSSE_NS);
            securityTokenRef.setAttributeNS(SAMLConstants.NS_XMLNS,
                WSSConstants.TAG_XML_WSU, WSSConstants.WSU_NS);
            String secRefId = SAMLUtils.generateID();
            securityTokenRef.setAttributeNS(WSSConstants.WSU_NS, 
                WSSConstants.WSU_ID, secRefId);            
            insideKi.addUnknownElement(securityTokenRef);
            IdResolver.registerElementById(securityTokenRef, secRefId);
            
            Element reference = doc.createElementNS(WSSConstants.WSSE_NS,
                SAMLConstants.TAG_REFERENCE);            
            reference.setPrefix(WSSConstants.WSSE_TAG); 
                        
            String searchType = null;
            if (SecurityToken.WSS_X509_TOKEN.equals(tokenType)) {                
                reference.setAttributeNS(null, WSSConstants.TAG_VALUETYPE, 
                    WSSConstants.WSSE_X509_NS + "#X509v3");
                searchType = SAMLConstants.BINARYSECURITYTOKEN;
            } else if (SecurityToken.WSS_USERNAME_TOKEN.equals(tokenType)) {
                reference.setAttributeNS(null, WSSConstants.TAG_VALUETYPE, 
                    WSSConstants.TAG_USERNAME_VALUE_TYPE);
                searchType = WSSConstants.TAG_USERNAME_TOKEN;
            } else if (SecurityToken.WSS_SAML_TOKEN.equals(tokenType)) {
                reference.setAttributeNS(null, WSSConstants.TAG_VALUETYPE, 
                    WSSConstants.ASSERTION_VALUE_TYPE);
                searchType = SAMLConstants.TAG_ASSERTION;
            }
            Element bsf = (Element)root.getElementsByTagNameNS(
                WSSConstants.WSSE_NS,searchType).item(0);
            if (bsf != null) {        
                String certId = bsf.getAttributeNS(WSSConstants.WSU_NS,
                    SAMLConstants.TAG_ID);                
                reference.setAttributeNS(null, SAMLConstants.TAG_URI,"#"
                                         +certId);
            }
            
            securityTokenRef.appendChild(reference);
            encryptedKey.setKeyInfo(insideKi);
                        
            ReferenceList refList = 
                cipher.createReferenceList(ReferenceList.DATA_REFERENCE);
            if (refList != null) {
                Reference dataRef = refList.newDataReference("#" + bodyId);
                refList.add(dataRef);
                encryptedKey.setReferenceList(refList);
            }            
            	    
            // ENCRYPTED KEY END
                        
            
            // ENCRYPTED DATA
            String encAlgorithm = 
                  getEncryptionAlgorithm(encDataEncAlg, encDataEncAlgStrength);
            cipher = XMLCipher.getInstance(encAlgorithm);            
            cipher.init(XMLCipher.ENCRYPT_MODE, secretKeyEncData);

            EncryptedData builder = cipher.getEncryptedData();            
            builder.setId(bodyId);
            
            EncryptionMethod encMethod = 
                cipher.createEncryptionMethod(encAlgorithm);
            builder.setEncryptionMethod(encMethod);

            Node firstNodeInsideBody = element.getFirstChild();
                
            // ENCRYPTED DATA END
            
            resultDoc = cipher.doFinal(doc, (Element) firstNodeInsideBody);
            
            root.appendChild(cipher.martial(doc, encryptedKey));
           
            if(EncryptionUtils.debug.messageEnabled()) {
                EncryptionUtils.debug.message("WSSEncryptionProvider.encrypt" +
                    "AndReplaceWSSBody: Encrypted DOC = " 
                    + WSSUtils.print(resultDoc));
            }
	    	    
        } catch (Exception xe) {
            EncryptionUtils.debug.error("WSSEncryptionProvider.encryptAnd" +
            "ReplaceWSSBody: XML Encryption error : ", xe); 
            throw new EncryptionException(xe);
        }
        
        return resultDoc;
    }
    
}



