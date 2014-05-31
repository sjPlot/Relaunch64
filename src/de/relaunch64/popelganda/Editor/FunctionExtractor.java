/*
 * Relaunch64 - A Java cross-development IDE for C64 machine language coding.
 * Copyright (C) 2001-2014 by Daniel Lüdecke (http://www.danielluedecke.de)
 * 
 * Homepage: http://www.popelganda.de
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Dieses Programm ist freie Software. Sie können es unter den Bedingungen der GNU
 * General Public License, wie von der Free Software Foundation veröffentlicht, weitergeben
 * und/oder modifizieren, entweder gemäß Version 3 der Lizenz oder (wenn Sie möchten)
 * jeder späteren Version.
 * 
 * Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, daß es Ihnen von Nutzen sein 
 * wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne die implizite Garantie der MARKTREIFE oder 
 * der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Details finden Sie in der 
 * GNU General Public License.
 * 
 * Sie sollten ein Exemplar der GNU General Public License zusammen mit diesem Programm 
 * erhalten haben. Falls nicht, siehe <http://www.gnu.org/licenses/>.
 */

package de.relaunch64.popelganda.Editor;

import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.Tools;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 *
 * @author Daniel Lüdecke
 */
public class FunctionExtractor {

    public static ArrayList getFunctionNames(String source, int compiler) {
        return getFunctionOrMacroNames(getFunctionString(compiler), source, compiler);
    }
    /**
     * Retrieves a list of all functions from the current activated source code
     * (see {@link #getActiveSourceCode()}) that start with the currently
     * typed characters at the caret position (usually passed as parameter
     * {@code subWord}.
     * 
     * @param subWord A string which filters the list of labels. Only labels that start with
     * {@code subWord} will be returned in this list.
     * @param source
     * @param compiler
     * 
     * @return An array list of sorted functions, where only those functions are returned 
     * that start with {@code subWord}.
     */
    public static ArrayList<String> getFunctionNamesAsList(String subWord, String source, int compiler) {
        // get labels here
        ArrayList<String> functions = getFunctionNames(source, compiler);
        // check for valid values
        if (null==functions || functions.isEmpty()) return null;
        // remove all labels that do not start with already typed chars
        if (!subWord.isEmpty()) {
            for (int i=functions.size()-1; i>=0; i--) {
                if (!functions.get(i).startsWith(subWord)) functions.remove(i);
            }
        }
        // sort list
        Collections.sort(functions);
        // return as object array
        return functions;
    }
    /**
     * Retrieves a list of all functions from the current activated source code
     * (see {@link #getActiveSourceCode()}) that start with the currently
     * typed characters at the caret position (usually passed as parameter
     * {@code subWord}.
     * 
     * @param subWord A string which filters the list of labels. Only labels that start with
     * {@code subWord} will be returned in this list.
     * @param source
     * @param compiler
     * 
     * @return An object array of sorted functions, where only those functions are returned 
     * that start with {@code subWord}.
     */
    public static Object[] getFunctionNames(String subWord, String source, int compiler) {
        ArrayList<String> functions = getFunctionNamesAsList(subWord, source, compiler);
        return (functions!=null) ? functions.toArray() : null;
    }    
    /**
     * Retrieves a list of all macros from the current activated source code
     * (see {@link #getActiveSourceCode()}) that start with the currently
     * typed characters at the caret position (usually passed as parameter
     * {@code subWord}.
     * 
     * @param subWord A string which filters the list of labels. Only labels that start with
     * {@code subWord} will be returned in this list.
     * @param source
     * @param compiler
     * 
     * @return An array list of sorted functions, where only those macros are returned 
     * that start with {@code subWord}.
     */
    public static ArrayList<String> getMacroNamesAsList(String subWord, String source, int compiler) {
        // get labels here
        ArrayList<String> macros = getMacroNames(source, compiler);
        // check for valid values
        if (null==macros || macros.isEmpty()) return null;
        // remove all labels that do not start with already typed chars
        if (!subWord.isEmpty()) {
            for (int i=macros.size()-1; i>=0; i--) {
                if (!macros.get(i).startsWith(subWord)) macros.remove(i);
            }
        }
        // sort list
        Collections.sort(macros);
        return macros;
    }
    /**
     * Retrieves a list of all macros from the current activated source code
     * (see {@link #getActiveSourceCode()}) that start with the currently
     * typed characters at the caret position (usually passed as parameter
     * {@code subWord}.
     * 
     * @param subWord A string which filters the list of labels. Only labels that start with
     * {@code subWord} will be returned in this list.
     * @param source
     * @param compiler
     * 
     * @return An object array of sorted functions, where only those macros are returned 
     * that start with {@code subWord}.
     */
    public static Object[] getMacroNames(String subWord, String source, int compiler) {
        ArrayList<String> macros = getMacroNamesAsList(subWord, source, compiler);
        return (macros!=null) ? macros.toArray() : null;
    }
    /**
     * Retrieves a list of all functions and macros from the current activated source code
     * (see {@link #getActiveSourceCode()}) that start with the currently
     * typed characters at the caret position (usually passed as parameter
     * {@code subWord}.
     * 
     * @param subWord A string which filters the list of labels. Only labels that start with
     * {@code subWord} will be returned in this list.
     * @param source
     * @param compiler
     * 
     * @return An object array of sorted functions and macros, where only those functions and macros
     * are returned that start with {@code subWord}.
     */
    public static Object[] getFunctionAndMacroNames(String subWord, String source, int compiler) {
        // get labels here
        ArrayList<String> functions = getFunctionNamesAsList(subWord, source, compiler);
        // get macros here
        ArrayList<String> macros = getMacroNamesAsList(subWord, source, compiler);
        // prepare return value
        ArrayList<String> retval = new ArrayList<>();
        if (functions!=null && !functions.isEmpty()) retval.addAll(functions);
        if (macros!=null && !macros.isEmpty()) retval.addAll(macros);
        if (!retval.isEmpty()) {
            // sort list
            Collections.sort(retval);
            // return as object array
            return retval.toArray();
        }
        return null;
    }
    /**
     * Retrieves a list of all functions and macros from the current activated source code
     * (see {@link #getActiveSourceCode()}) that start with the currently
     * typed characters at the caret position (usually passed as parameter
     * {@code subWord}.
     * 
     * @param subWord A string which filters the list of labels. Only labels that start with
     * {@code subWord} will be returned in this list.
     * @param source
     * @param compiler
     * 
     * @return An object array of sorted functions and macros, where only those functions and macros
     * are returned that start with {@code subWord}.
     */
    public static Object[] getFunctionMacroScripts(String subWord, String source, int compiler) {
        // get labels here
        ArrayList<String> functions = getFunctionNamesAsList(subWord, source, compiler);
        // get labels here
        ArrayList<String> macros = getMacroNamesAsList(subWord, source, compiler);
        // get scripts  here
        ArrayList<String> scripts = new ArrayList<>();
        switch(compiler) {
            case ConstantsR64.ASM_KICKASSEMBLER:
                scripts.addAll(Arrays.asList(ConstantsR64.SCRIPT_KEYWORDS_KICKASS));
                break;
            case ConstantsR64.ASM_64TASS:
                scripts.addAll(Arrays.asList(ConstantsR64.SCRIPT_KEYWORDS_64TASS));
                break;
        }
        // check for valid values
        if (!scripts.isEmpty()) {
            // remove all labels that do not start with already typed chars
            if (!subWord.isEmpty()) {
                for (int i=scripts.size()-1; i>=0; i--) {
                    if (!scripts.get(i).startsWith(subWord)) scripts.remove(i);
                }
            }
        }
        ArrayList<String> retval = new ArrayList<>();
        if (functions!=null && !functions.isEmpty()) retval.addAll(functions);
        if (macros!=null && !macros.isEmpty()) retval.addAll(macros);
        if (!scripts.isEmpty()) retval.addAll(scripts);
        if (!retval.isEmpty()) {
            // sort list
            Collections.sort(retval);
            // return as object array
            return retval.toArray();
        }
        return null;
    }

    /**
     * This method retrieves all functions from the current activated source code
     * (see {@link #getActiveSourceCode()}) and returns both line number of function
     * and function name as linked HashMap.
     *
     * @param source
     * @param compiler
     * @return All functions with their line numbers, or {@code null} if there are no functions
     * in the source code.
     */
    public static LinkedHashMap getFunctions(String source, int compiler) {
        return getFunctionsOrMacros(getFunctionString(compiler), source, compiler);
    }

    public static String getFunctionString(int compiler) {
        switch (compiler) {
            case ConstantsR64.ASM_KICKASSEMBLER:
            case ConstantsR64.ASM_64TASS:
            default:
                return ConstantsR64.STRING_FUNCTION_KICKASSEMBLER;
        }
    }

    public static String getFunctionOrMacroFromLine(String line, int compiler, String funmacString) {
        line = line.trim();
        if (isValidFunctionOrMacro(line, funmacString)) {
            // separator strings
            int i = 0;
            String keyword;
            line = line.substring(funmacString.length()).trim();
            try {
                while (!Tools.isDelimiter(line.substring(i, i + 1), "") && i < line.length()) {
                    i++;
                }
                keyword = line.substring(0, i);
            } catch (IndexOutOfBoundsException ex) {
                keyword = line.substring(0, line.length());
            }
            if (keyword!=null && !keyword.isEmpty() && keyword.length() > 1) {
                return keyword;
            }
        }
        return null;
    }

    public static ArrayList getFunctionOrMacroNames(String funmacString, String source, int compiler) {
        ArrayList<String> retval = new ArrayList<>();
        LinkedHashMap<Integer, String> map = getFunctionsOrMacros(funmacString, source, compiler);
        if (map != null && !map.isEmpty()) {
            Collection<String> c = map.values();
            Iterator<String> i = c.iterator();
            while (i.hasNext()) {
                retval.add(i.next());
            }
            return retval;
        }
        return null;
    }

    public static LinkedHashMap getFunctionsOrMacros(String funmacString, String source, int compiler) {
        LinkedHashMap<Integer, String> functions = new LinkedHashMap<>();
        // init vars
        int lineNumber = 0;
        String line;
        if (source != null) {
            BufferedReader br = new BufferedReader(new StringReader(source));
            LineNumberReader lineReader = new LineNumberReader(br);
            try {
                while ((line = lineReader.readLine()) != null) {
                    lineNumber++;
                    String keyword = getFunctionOrMacroFromLine(line, compiler, funmacString);
                    if (keyword!=null) {
                        // in some cases, we need to add a specific macro token,
                        // like colon for kickass or plus-sign for ACME
                        keyword = addMacroToken(keyword, funmacString, compiler);
                        // if keyword does not already exist, add it to results list
                        if (!functions.containsValue(keyword)) {
                            functions.put(lineNumber, keyword);
                        }
                    }
                }
            } catch (IOException ex) {
            }
        }
        return functions;
    }

    protected static String addMacroToken(String keyword, String funmacString, int compiler) {
        // for dream ass, add "."
        if (ConstantsR64.ASM_DREAMASS==compiler) {
            keyword = "."+keyword;
        }
        if (ConstantsR64.ASM_KICKASSEMBLER==compiler && funmacString.equals(ConstantsR64.assemblers[compiler].getMacroString())) {
            keyword = ":"+keyword;
        }
        if (ConstantsR64.ASM_ACME==compiler && funmacString.equals(ConstantsR64.assemblers[compiler].getMacroString())) {
            keyword = "+"+keyword;
        }
        return keyword;
    }
    
    public static LinkedHashMap getMacros(String source, int compiler) {
        return getFunctionsOrMacros(ConstantsR64.assemblers[compiler].getMacroString(), source, compiler);
    }

    public static ArrayList getMacroNames(String source, int compiler) {
        return getFunctionOrMacroNames(ConstantsR64.assemblers[compiler].getMacroString(), source, compiler);
    }

    public static boolean isValidFunctionOrMacro(String keyword, String funmacString) {
        keyword = keyword.trim();
        return keyword.startsWith(funmacString);
    }
}
