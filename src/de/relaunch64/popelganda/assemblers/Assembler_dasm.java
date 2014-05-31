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
package de.relaunch64.popelganda.assemblers;

import de.relaunch64.popelganda.util.ErrorHandler.ErrorInfo;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.LinkedHashMap;
import java.util.ArrayList;

/**
 *
 * @author Soci/Singular
 */
public class Assembler_dasm implements Assembler
{
    @Override
    public String name() {
        return "DASM";
    }

    @Override
    public String getDelimiterList() {
        return ",:{}()[]+-/<=>&|^~*";
    }
    
    @Override
    public String syntaxFile() {
        return "assembly-dasm.xml";
    }

    @Override
    public String getLineComment() {
        return ";";
    }

    @Override
    public String getMacroPrefix() {
        return "";
    }

    @Override
    public String getMacroString() {
        return "mac";
    }

    @Override
    public String getByteDirective() {
        return "dc.b";
    }

    @Override
    public String getBasicStart(int start) {
        return " org $801\n dc.w .0, 10\n dc.b $9e, \"" + Integer.toString(start) + "\", 0\n.0 dc.w 0\n";
    }

    @Override
    public String getIncludeSourceDirective(String path) {
        return "include \"" + path + "\"";
    }

    @Override
    public String getIncludeTextDirective(String path) {
        return "incbin \"" + path + "\"";
    }

    @Override
    public String getIncludeC64Directive(String path) {
        return "incbin \"" + path + "\"";
    }

    @Override
    public String getIncludeBinaryDirective(String path) {
        return "incbin \"" + path + "\"";
    }

    @Override
    public String[] getScriptKeywords() {
        return new String[] {};
    }

    @Override
    public LinkedHashMap getLabels(LineNumberReader lineReader, int lineNumber) {
        LinkedHashMap<Integer, String> labelValues = new LinkedHashMap<>();
        LinkedHashMap<Integer, String> localLabelValues = new LinkedHashMap<>();
        Pattern p = Pattern.compile("^(?<label>[a-zA-Z_.][a-zA-Z0-9_]*\\b).*"); // always in first column
        String line;
        boolean scopeFound = false;
        try {
            while ((line = lineReader.readLine()) != null) {
                Matcher m = p.matcher(line);

                if (!m.matches()) continue;
                String label = m.group("label");

                if (label != null) {
                    if (lineNumber > 0) {
                        if (label.charAt(0) == '.') { // local label
                            if (scopeFound) continue;
                            if (!localLabelValues.containsValue(label)) {
                                localLabelValues.put(lineReader.getLineNumber(), label); // add if not listed already
                            }
                            continue;
                        } 
                        if (lineNumber < lineReader.getLineNumber()) {
                            scopeFound = true;
                        } else {
                            localLabelValues.clear();
                        }
                    }
                    if (!labelValues.containsValue(label)) {
                        labelValues.put(lineReader.getLineNumber(), label); // add if not listed already
                    }
                }
            }
        }
        catch (IOException ex) {
        }
        labelValues.putAll(localLabelValues);
        return labelValues;
    }

    @Override
    public LinkedHashMap getFunctions(LineNumberReader lineReader) {
        return new LinkedHashMap<>();
    }

    @Override
    public String labelGetStart(String line, int pos) {
        String line2 = new StringBuffer(line.substring(0, pos)).reverse().toString();
        Pattern p = Pattern.compile("(?i)([a-z0-9_]*[a-z_.])([^a-z0-9_].*|$)");
        Matcher m = p.matcher(line2);
        if (!m.matches()) return "";
        return new StringBuffer(m.group(1)).reverse().toString();
    }

    @Override
    public ArrayList<ErrorInfo> readErrorLines(LineNumberReader lineReader) {
        final ArrayList<ErrorInfo> errors = new ArrayList<>();
        String line;     // a.asm (5): error: Syntax Error 'o o'.
        Pattern p = Pattern.compile("^(?<file>.*?) \\((?<line>\\d+)\\): (?:error|warning|fatal): .*");
        try {
            while ((line = lineReader.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (!m.matches()) continue;
                ErrorInfo e = new ErrorInfo(
                        Integer.parseInt(m.group("line")),
                        1,
                        lineReader.getLineNumber(),
                        m.group("file")
                        );
                errors.add(e);
            }
        }
        catch (IOException ex) {
        }
        return errors;
    }
}
