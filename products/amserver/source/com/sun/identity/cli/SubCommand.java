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
 * $Id: SubCommand.java,v 1.2 2006-07-17 18:11:02 veiming Exp $
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.identity.cli;

import com.iplanet.sso.SSOToken;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This class contains definition of sub command.
 */
public class SubCommand {
    private IDefinition definition;
    private ResourceBundle rb;
    private String name;
    private String implClassName;
    private Map<String, List<String>> optionAliases = 
        new HashMap<String, List<String>>();
    private Set<String> setOptionAliases = new HashSet<String>();
    private List<String> mandatoryOptions = new ArrayList<String>();
    private List<String> optionalOptions = new ArrayList<String>();
    private Map<String, String> optionNameToShortName = 
        new HashMap<String, String>();
    private Set<String> unaryOptionNames = new HashSet<String>();
    private Set<String> singleOptionNames = new HashSet<String>();

    private static Set<String> reservedLongOptionNames = new HashSet<String>();
    private static Set<String> reservedShortOptionNames = new HashSet<String>();
    private static Map<String, String> mapLongToShortOptionName = 
        new HashMap<String, String>();

    static {
        try {
        Field[] allFields = CLIConstants.class.getFields();
        for (int i = 0; i < allFields.length; i++) {
            Field fld = (Field)allFields[i];
            if (fld.getName().startsWith(CLIConstants.PREFIX_ARGUMENT)) {
                reservedLongOptionNames.add((String)fld.get(null));

                String option = fld.getName().substring(
                    CLIConstants.PREFIX_ARGUMENT.length());
                Field fldShort = CLIConstants.class.getField(
                    CLIConstants.PREFIX_SHORT_ARGUMENT + option);
                mapLongToShortOptionName.put(
                    (String)fld.get(null), (String)fldShort.get(null));
            } else if (fld.getName().startsWith(
                CLIConstants.PREFIX_SHORT_ARGUMENT)
            ) {
                reservedShortOptionNames.add((String)fld.get(null));
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns <code>true</code> if an argument/option is reserved.
     *
     * @param name Short name of argument/option.
     * @return <code>true</code> if an argument/option is reserved.
     */
    public static boolean isReservedShortOption(String name) {
        return reservedShortOptionNames.contains(name);
    }

    /**
     * Returns <code>true</code> if an argument/option is reserved.
     *
     * @param name Full name of argument/option.
     * @return <code>true</code> if an argument/option is reserved.
     */
    public static boolean isReservedLongOption(String name) {
        return reservedLongOptionNames.contains(name);
    }

    /**
     * Returns short reserved argument/option name given its long name.
     *
     * @param longName Long name of reserved argument/option.
     * @return short reserved argument/option name given its long name.
     *         Returns null if short name is not found.
     */
    public static String getReservedShortOptionName(String longName) {
        return (String)mapLongToShortOptionName.get(longName);
    }

    /**
     * Returns long reserved argument/option name given its short name.
     *
     * @param shortName Short name of reserved argument/option.
     * @return long reserved argument/option name given its short name.
     *         Returns null if long name is not found.
     */
    public static String getReservedLongOptionName(String shortName) {
        String longName = null;
        for (Iterator i = mapLongToShortOptionName.keySet().iterator();
            i.hasNext() && (longName == null);
        ) {
            String key = (String)i.next();
            String value = (String)mapLongToShortOptionName.get(key);
            if (value.equals(shortName)) {
                longName = key;
            }
        }
        return longName;
    }

    /**
     * Creates a sub command object.
     *
     * @param defintion Definition class.
     * @param rb Resource Bundle.
     * @param name Name of the Sub Command.
     * @param mandatoryOptions Formated list of mandatory argument/options.
     * @param optionalOptions Formated list of optional argument/options.
     * @param optionAliases Formated list of argument/options aliases.
     * @param implClassName Implementation class name.
     * @throws CLIException if this object cannot be constructed.
     */
    public SubCommand(
        IDefinition definition,
        ResourceBundle rb,
        String name,
        String mandatoryOptions,
        String optionalOptions,
        String optionAliases,
        String implClassName
    ) throws CLIException {
        this.definition = definition;
        this.name = name;
        this.rb = rb;
        this.implClassName = implClassName;

        //this is use to clean duplicate short options.
        Set<String> shortOptions = new HashSet<String>();

        parseOptions(mandatoryOptions, this.mandatoryOptions, shortOptions);
        parseOptions(optionalOptions, this.optionalOptions, shortOptions);

        parseAliases(optionAliases);
    }

    /**
     * Returns name of the sub command.
     *
     * @return name of the sub command.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns description of the sub command.
     *
     * @return description of the sub command.
     */
    public String getDescription() {
        return rb.getString(CLIConstants.PREFIX_SUBCMD_RES +name);
    }

    /**
     * Returns list of optional argument/options.
     *
     * @return list of optional argument/options.
     */
    public List getOptionalOptions() {
        return optionalOptions;
    }

    /**
     * Returns list of mandatory argument/options.
     *
     * @return list of mandatory argument/options.
     */
    public List<String> getMandatoryOptions() {
        return mandatoryOptions;
    }

    /**
     * Returns option aliases.
     *
     * @param name Full name of argument/option.
     * @return get option aliases. Returns null of there are no aliases.
     */
    public List<String> getOptionAliases(String name) {
        return optionAliases.get(name);
    }

    /**
     * Returns option aliases group.
     *
     * @param name Full name of argument/option.
     * @return option aliases group.
     */
    public Set<String> getOptionAliasesGroup(String fullName) {
        Set<String> group = null;
        for (Iterator i = optionAliases.keySet().iterator();
            i.hasNext() && (group == null); ) {
            String opt = (String)i.next();
            List<String> list = getOptionAliases(opt);

            if (list != null) {
                for (Iterator j = list.iterator();
                    j.hasNext() && (group == null);
                ) {
                    String name = (String)j.next();
                    if (name.equals(fullName)) {
                        group = new HashSet<String>();
                        group.addAll(list);
                        group.add(opt);
                    }
                }
            }
        }
        return group;
    }

    /**
     * Returns <code>true</code> if option is an alias.
     *
     * @param name Full name of argument/option.
     * @return <code>true</code> if option is an alias.
     */
    public boolean isOptionAlias(String name) {
        return setOptionAliases.contains(name);
    }

    /**
     * Services a CLI request.
     *
     * @param rc Request Context.
     * @throws CLIException if request cannot be serviced.
     */
    public void execute(RequestContext rc)
        throws CLIException
    {
        CommandManager mgr = rc.getCommandManager();
        if (mgr.isVerbose()) {
            mgr.getOutputWriter().printlnMessage(
                rc.getResourceString("verbose-processing-sub-command"));
        }

        try {
            Class clazz = Class.forName(implClassName);
            CLICommand cmd = (CLICommand)clazz.newInstance();
            cmd.handleRequest(rc);
        } catch (IllegalAccessException e) {
            throw new CLIException(e.getMessage(),
                ExitCodes.SUBCOMMAND_IMPLEMENT_CLASS_ILLEGAL_ACCESS);
        } catch (InstantiationException e) {
            throw new CLIException(e.getMessage(),
                ExitCodes.SUBCOMMAND_IMPLEMENT_CLASS_CANNOT_INSTANTIATE);
        } catch (ClassNotFoundException e) {
            throw new CLIException(e.getMessage(),
                ExitCodes.SUBCOMMAND_IMPLEMENT_CLASS_NOTFOUND);
        }
    }

    /**
     * Returns <code>true</code> if the given options are valid in the
     * context of this sub command.
     *
     * @param options Map of argument/option full name to its values (List).
     * @param ssoToken Single Sign On token of the user.
     * @return <code>true</code> if the given options are valid.
     */
    public boolean validateOptions(
        Map<String, List<String>> options,
        SSOToken ssoToken
    ) {
        boolean valid = true;

        for (Iterator i = optionAliases.keySet().iterator();
            i.hasNext() && valid; 
        ) {
            String opt = (String)i.next();
            valid = validateAliasOptions(
                opt, optionAliases.get(opt), options);
        }

        for (Iterator i = mandatoryOptions.iterator(); i.hasNext() && valid; ) {
            String opt = (String)i.next();
            List values = (List)options.get(opt);
            if (values == null) {
                if ((ssoToken == null) || !definition.isAuthOption(opt)) {
                    List aliases = (List)optionAliases.get(opt);
                    if ((aliases == null) || aliases.isEmpty()) {
                        Set aliasGroup = getOptionAliasesGroup(opt);
                        valid = hasOptionValue(aliasGroup, options, ssoToken);
                    } else {
                        valid = hasOptionValue(aliases, options, ssoToken);
                    }
                }
            }
        }

        for (Iterator i = unaryOptionNames.iterator();
            i.hasNext() && valid;
        ) {
            String name = (String)i.next();
            List list = (List)options.get(name);
                valid = (list == null) || list.isEmpty();
        }

        for (Iterator i = singleOptionNames.iterator();
            i.hasNext() && valid;
        ) {
            String name = (String)i.next();
            List list = (List)options.get(name);
            valid = (list == null) || (list.size() == 1);
        }

        return valid;
    }

    private boolean hasOptionValue(
        Collection options,
        Map optionValues,
        SSOToken ssoToken
    ) {
        boolean has = false;
        if ((options != null) && (optionValues != null)) {
            for (Iterator i = options.iterator(); i.hasNext() && !has; ) {
                String opt = (String)i.next();
                if ((ssoToken != null) && definition.isAuthOption(opt)) {
                    has = true;
                } else {
                    List values = (List)optionValues.get(opt);
                    has = (values != null) && !values.isEmpty();
                }
            }
        }
        return has;
    }

    private boolean validateAliasOptions(
        String opt,
        List<String> aliases,
        Map options
    ) {
        Set<String> set = new HashSet<String>();
        set.add(opt);
        set.addAll(aliases);
        boolean existed = false;
        boolean valid = true;
        for (Iterator i = set.iterator(); i.hasNext() && valid; ) {
            String o = (String)i.next();
            if (options.containsKey(o)) {
                if (existed) {
                    valid = false;
                } else {
                    existed = true;
                }
            }
        }
        return valid;
    }

    /**
     * Returns long argument/option name given its short name.
     *
     * @param name short name of argument/option.
     * @return long argument/option name given its short name.
     *         Returns null if short name is not found.
     */
    public String getLongOptionName(String name) {
        String longName = null;
        for (Iterator i = optionNameToShortName.keySet().iterator();
            i.hasNext() && (longName == null); 
        ) {
            String opt = (String)i.next();
            String val = optionNameToShortName.get(opt);
            if (val.equals(name)) {
                longName = opt;
            }
        }
        return longName;
    }

    /**
     * Returns short argument/option name given its long name.
     *
     * @param name Long name of argument/option.
     * @return short argument/option name given its long name.
     *         Returns null if short name is not found.
     */
    public String getShortOptionName(String name) {
        return optionNameToShortName.get(name);
    }

    /**
     * Returns <true> if the option is supported.
     *
     * @param name Name of the argument/option.
     * @return <true> if the option is supported.
     */
    public boolean isSupportedOption(String name) {
        boolean isSupported = false;
        for (Iterator i = mandatoryOptions.iterator();
            i.hasNext() && !isSupported;
        ) {
            String opt = (String)i.next();
            isSupported = opt.equals(name);
        }
        for (Iterator i = optionalOptions.iterator();
            i.hasNext() && !isSupported;
        ) {
            String opt = (String)i.next();
            isSupported = opt.equals(name);
        }
        return isSupported;
    }

    /**
     * Returns the description of an argument/option.
     *
     * @param name Name of the argument/option.
     * @return the description of an argument/option.
     */
    public String getOptionDescription(String name) {
        return rb.getString(
            CLIConstants.PREFIX_SUBCMD_RES + this.name + "-" + name);
    }

    private void parseOptions(
        String strOpt,
        List<String> options,
        Set<String> shortOptions)
        throws CLIException
    {
        StringTokenizer st = new StringTokenizer(strOpt, "@");
        while (st.hasMoreTokens()) {
            StringTokenizer t = new StringTokenizer(st.nextToken(), "|");
            String name = t.nextToken();
            String shortName = t.nextToken();
            String type = t.nextToken();
            boolean unary = type.equals(CLIConstants.FLAG_UNARY);
            boolean single = type.equals(CLIConstants.FLAG_SINGLE);

            if (reservedLongOptionNames.contains(name)) {
                Object[] params = {name, this.name};
                throw new CLIException(MessageFormat.format(
                    CommandManager.resourceBundle.getString(
                    "exception-message-reserved-option"), params),
                    ExitCodes.RESERVED_OPTION);
            }

            if (reservedShortOptionNames.contains(shortName)) {
                Object[] params = {shortName, this.name};
                throw new CLIException(MessageFormat.format(
                    CommandManager.resourceBundle.getString(
                    "exception-message-reserved-option"), params),
                    ExitCodes.RESERVED_OPTION);
            }

            if (options.contains(name)) {
                Object[] params = {name, this.name};
                throw new CLIException(MessageFormat.format(
                    CommandManager.resourceBundle.getString(
                    "exception-message-duplicated-option"), params),
                    ExitCodes.DUPLICATED_OPTION);
            }

            if (shortOptions.contains(shortName)) {
                Object[] params = {shortName, this.name};
                throw new CLIException(MessageFormat.format(
                    CommandManager.resourceBundle.getString(
                    "exception-message-duplicated-option"), params),
                    ExitCodes.DUPLICATED_OPTION);
            }

            options.add(name);
            shortOptions.add(shortName);
            optionNameToShortName.put(name, shortName);
            if (unary) {
                unaryOptionNames.add(name);
            } else if (single) {
                singleOptionNames.add(name);
            }
        }
    }

    private void parseAliases(String aliases) {
        StringTokenizer st = new StringTokenizer(aliases, "@");
        while (st.hasMoreTokens()) {
            StringTokenizer t = new StringTokenizer(st.nextToken(), "|");
            String head = t.nextToken();
            String alias = t.nextToken();

            List<String> array = new ArrayList<String>();
            array.add(alias);
            setOptionAliases.add(alias);
            while (t.hasMoreTokens()) {
                String a = t.nextToken();
                array.add(a);
                setOptionAliases.add(a);
            }
            optionAliases.put(head, array);
        }
    }

    /**
     * Returns resource bundle of the sub command.
     *
     * @return resource bundle of the sub command.
     */
    public ResourceBundle getResourceBundle() {
        return rb;
    }

    /**
     * Returns <code>true</code> if option is unary.
     *
     * @return <code>true</code> if option is unary.
     */
    public boolean isUnaryOption(String cmdName) {
        return unaryOptionNames.contains(cmdName);        
    }

    /**
     * Returns <code>true</code> if option is binary.
     *
     * @return <code>true</code> if option is binary.
     */
    public boolean isBinaryOption(String cmdName) {
        return singleOptionNames.contains(cmdName);        
    }
}
