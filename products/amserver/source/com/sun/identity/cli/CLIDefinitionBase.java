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
 * $Id: CLIDefinitionBase.java,v 1.2 2006-09-21 18:29:12 veiming Exp $
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.identity.cli;

import com.sun.identity.cli.annotation.DefinitionClassInfo;
import com.sun.identity.cli.annotation.Macro;
import com.sun.identity.cli.annotation.SubCommandInfo;
import com.sun.identity.cli.tools.CLIDefinitionGenerator;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * This is the base class for CLI definition class.
 */
public abstract class CLIDefinitionBase implements IDefinition {
    private String version;
    private List<SubCommand> subCommands = new ArrayList<SubCommand>();
    private String definitionClass;
    protected ResourceBundle rb;

    /**
     * Constructs an instance of this class.
     *
     * @param definitionClass Definition class name.
     */
    public CLIDefinitionBase(String definitionClass)
        throws CLIException {
        this.definitionClass = definitionClass;
        Class defClass = getDefinitionClass();
        getProductName(defClass);
        getCommands(defClass);
    }

    private Class getDefinitionClass()
        throws CLIException {
        try {
            return Class.forName(definitionClass);
        } catch (ClassNotFoundException e) {
            throw new CLIException(e, ExitCodes.MISSING_DEFINITION_CLASS);
        }
    }

    private void getProductName(Class clazz) 
        throws CLIException {
        try {
            Field pdtField = clazz.getDeclaredField(
                CLIConstants.FLD_PRODUCT_NAME);

            if (pdtField != null) {
                DefinitionClassInfo classInfo = pdtField.getAnnotation(
                    DefinitionClassInfo.class);

                rb = ResourceBundle.getBundle(classInfo.resourceBundle());
                version = classInfo.version();
            } else {
                throw new CLIException("Incorrect Definiton, class" +
                    definitionClass + " missing product field",
                    ExitCodes.INCORRECT_DEFINITION_CLASS);

            }
        } catch (NoSuchFieldException e) {
            throw new CLIException(e,
                ExitCodes.INCORRECT_DEFINITION_CLASS);
        }
    }

    private void getCommands(Class clazz) 
        throws CLIException 
    {
        Field[] fields = clazz.getDeclaredFields();

        for (Field fld : fields) {
            SubCommandInfo info = fld.getAnnotation(SubCommandInfo.class);

            if (info != null) {
                if ((info.implClassName() == null) ||
                    (info.description() == null)
                ) {
                    throw new CLIException("Incorrect Definiton, class" +
                        definitionClass + " missing product field",
                        ExitCodes.INCORRECT_DEFINITION_CLASS);
                }

                List<String> mandatoryOptions = CLIDefinitionGenerator.toList(
                    info.mandatoryOptions());
                List<String> optionalOptions = CLIDefinitionGenerator.toList(
                    info.optionalOptions());
                List<String> optionAliases = CLIDefinitionGenerator.toList(
                    info.optionAliases());

                if ((info.macro() != null) && (info.macro().length() > 0)) {
                    try {
                        Field fldMarco = clazz.getDeclaredField(info.macro());
                        Macro macroInfo =(Macro)fldMarco.getAnnotation(
                            Macro.class);
                        CLIDefinitionGenerator.appendToList(mandatoryOptions,
                            macroInfo.mandatoryOptions());
                        CLIDefinitionGenerator.appendToList(optionalOptions,
                            macroInfo.optionalOptions());
                        CLIDefinitionGenerator.appendToList(optionAliases,
                            macroInfo.optionAliases());
                    } catch (NoSuchFieldException e) {
                        throw new CLIException(e,
                            ExitCodes.INCORRECT_DEFINITION_CLASS);
                    }
                }

                String subcmdName = fld.getName().replace('_', '-');
                subCommands.add(new SubCommand(
                    this, rb, subcmdName, mandatoryOptions, optionalOptions,
                    optionAliases, info.implClassName()));
            }
        }
    }

    /**
     * Returns version.
     *
     * @return version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns a list of sub commands.
     *
     * @return a list of sub commands.
     */
    public List getSubCommands() {
        return subCommands;
    }

    /**
     * Returns sub command object.
     *
     * @param name Name of sub command.
     * @return sub command object.
     */
    public SubCommand getSubCommand(String name) {
        SubCommand result = null;
        for (Iterator i = subCommands.iterator();
            i.hasNext() && (result == null);
        ) {
            SubCommand cmd = (SubCommand)i.next();
            if (cmd.getName().equals(name)) {
                result = cmd;
            }
        }
        return result;
    }
}
