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
 * $Id: StringOutputWriter.java,v 1.1 2006-10-05 23:04:17 veiming Exp $
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.identity.cli;


/**
 * String Output Writer for CLI.
 */
public class StringOutputWriter implements IOutput {
    private StringBuffer buff = new StringBuffer();
    
    /**
     * Prints message.
     *
     * @param str Message string.
     */
    public void printMessage(String str) {
        buff.append(str).append("\n");
    }

    /**
     * Prints message with new line.
     *
     * @param str Message string.
     */
    public void printlnMessage(String str) {
        buff.append(str).append("\n");
    }

    /**
     * Prints error.
     *
     * @param str Error message string.
     */
    public void printError(String str) {
        buff.append(str).append("\n");
    }

    /**
     * Prints error with new line.
     *
     * @param str Error message string.
     */
    public void printlnError(String str) {
        buff.append(str).append("\n");
    }
    
    public String getMessages() {
        return buff.toString();
    }
    
}
