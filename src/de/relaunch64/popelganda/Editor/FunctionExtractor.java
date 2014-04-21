/*
 * Copyright (C) 2014 Luedeke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.relaunch64.popelganda.Editor;

import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.Tools;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 *
 * @author Luedeke
 */
public class FunctionExtractor {

    public static ArrayList getFunctionNames(String source, int compiler) {
        return getFunctionOrMacroNames(getFunctionString(compiler), source, compiler);
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
            case ConstantsR64.COMPILER_KICKASSEMBLER:
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
            if (keyword != null && !keyword.isEmpty() && keyword.length() > 2) {
                return keyword;
            }
        }
        return null;
    }

    public static String getFunctionFromLine(String line, int compiler) {
        return getFunctionOrMacroFromLine(line, compiler, getFunctionString(compiler));
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
        if (compiler != ConstantsR64.COMPILER_KICKASSEMBLER) {
            return null;
        }
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
                    if (keyword != null && !functions.containsValue(keyword)) {
                        functions.put(lineNumber, keyword);
                    }
                }
            } catch (IOException ex) {
            }
        }
        return functions;
    }

    public static LinkedHashMap getMacros(String source, int compiler) {
        return getFunctionsOrMacros(getMacroString(compiler), source, compiler);
    }

    public static String getMacroString(int compiler) {
        switch (compiler) {
            case ConstantsR64.COMPILER_KICKASSEMBLER:
            default:
                return ConstantsR64.STRING_MACRO_KICKASSEMBLER;
        }
    }

    public static boolean isValidFunction(String keyword, int compiler) {
        return isValidFunctionOrMacro(keyword, getFunctionString(compiler));
    }

    public static ArrayList getMacroNames(String source, int compiler) {
        return getFunctionOrMacroNames(getMacroString(compiler), source, compiler);
    }

    public static boolean isValidFunctionOrMacro(String keyword, String funmacString) {
        keyword = keyword.trim();
        return keyword.startsWith(funmacString);
    }

    public static String getMacroFromLine(String line, int compiler) {
        return getFunctionOrMacroFromLine(line, compiler, getMacroString(compiler));
    }
    public static boolean isValidMacro(String keyword, int compiler) {
        return isValidFunctionOrMacro(keyword, getMacroString(compiler));
    }
}
