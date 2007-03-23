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
 * $Id: SecurityToken.java,v 1.1 2007-03-23 00:02:03 mallas Exp $
 *
 * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
 */


package com.sun.identity.wss.security;

import org.w3c.dom.Element;

 
/**
 * This interface represents ws-security token that can be inserted into
 * web services security header. 
 *
 * <p> Each security token must need to implement this interface along with
 * <code>SecurityTokenSpec</code> for generating the security tokens. 
 * @supported.all.api
 */
public interface SecurityToken {
      
     /**
      * The <code>URI</code> to identify the WS-Security SAML Security Token.
      */
     public static final String WSS_SAML_TOKEN = "urn:sun:wss:samltoken";

     /**
      * The <code>URI</code> to identify the WS-Security X509 Security Token
      */
     public static final String WSS_X509_TOKEN = "urn:sun:wss:x509token";

     /**
      * The <code>URI</code> to identify the WS-Security UserName Security Token
      */
     public static final String WSS_USERNAME_TOKEN = 
                                 "urn:sun:wss:usernametoken";

     /** 
      * Returns the security token type. The possible values are
      *      {@link #WSS_SAML_TOKEN},
      *      {@link #WSS_X509_TOKEN}
      *      {@link #WSS_USERNAME_TOKEN}
      */
      public String getTokenType();

      /**
       * Convert the security token into DOM Object.
       * 
       * @return the DOM Document Element.
       *
       * @exception SecurityException if any failure is occured.
       */
      public Element toDocumentElement() throws SecurityException;

}
