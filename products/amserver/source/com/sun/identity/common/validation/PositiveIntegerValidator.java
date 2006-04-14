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
 * $Id: PositiveIntegerValidator.java,v 1.2 2006-04-14 09:07:16 veiming Exp $
 *
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.identity.common.validation;

import com.iplanet.am.util.Locale;

/**
 * Validator for positive integer format.
 */
public class PositiveIntegerValidator
    extends ValidatorBase
{
    private static PositiveIntegerValidator instance =
        new PositiveIntegerValidator();

    /**
     * Avoid instantiation of this class.
     */
    private PositiveIntegerValidator() {
    }

    public static PositiveIntegerValidator getInstance() {
        return instance;
    }

    protected void performValidation(String strData)
        throws ValidationException
    {
        if ((strData == null) || (strData.trim().length() == 0)) {
            throw new ValidationException(resourceBundleName, "errorCode2"); 
        }

        try {
            int value = Integer.parseInt(strData);

            if (value < 0) {
                throw new ValidationException(resourceBundleName, "errorCode2");
            }
        } catch (NumberFormatException nfe) {
            throw new ValidationException(resourceBundleName, "errorCode2"); 
        }
    }

    /** Test */
    public static void main(String[] args) {
        PositiveIntegerValidator inst = getInstance();
        try {
            inst.validate("1");
            inst.validate("-1");
        } catch (ValidationException e) {
            System.out.println(e.getMessage());
        }
    }
}
