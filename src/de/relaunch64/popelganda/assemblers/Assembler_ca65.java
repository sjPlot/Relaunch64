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
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ArrayList;

/**
 *
 * @author Soci/Singular
 */
public class Assembler_ca65 implements Assembler
{
    @Override
    public String name() {
        return "ca65";
    }

    @Override
    public String syntaxFile() {
        return "assembly-ca65.xml";
    }

    @Override
    public String getLineComment() {
        return ";";
    }

    @Override
    public String getMacroPrefix() {
        return ".";
    }

    @Override
    public String getByteDirective() {
        return ".byte";
    }

    @Override
    public String getIncludeSourceDirective(String path) {
        return ".include \"" + path + "\"";
    }

    @Override
    public String getIncludeTextDirective(String path) {
        return ".incbin \"" + path + "\"";
    }

    @Override
    public String getIncludeC64Directive(String path) {
        return ".incbin \"" + path + "\",2";
    }

    @Override
    public String getIncludeBinaryDirective(String path) {
        return ".incbin \"" + path + "\"";
    }

    @Override
    public LinkedHashMap getLabels(LineNumberReader lineReader, int lineNumber) {
        class lineInfo {
            final LinkedList<String> name;
            final int line;
            lineInfo(LinkedList<String> name, int line) {
                this.name = name;
                this.line = line;
            }
        }
        LinkedHashMap<Integer, String> labelValues = new LinkedHashMap<>();
        LinkedHashMap<Integer, String> localLabelValues = new LinkedHashMap<>();
        LinkedList<lineInfo> labels = new LinkedList<>();
        String line;
        Pattern p = Pattern.compile("^\\s*(?:(?<label>[a-zA-Z_@][a-zA-Z0-9_]*)\\s*[:=])?\\s*(?<directive>\\.(?:scope|endscope)\\b)?\\s*(?<label2>[a-zA-Z_][a-zA-Z0-9_]*)?.*");
        LinkedList<String> myscope = new LinkedList<>(), scopes = new LinkedList<>();
        boolean scopeFound = false;
        try {
            while ((line = lineReader.readLine()) != null) {
                if (lineReader.getLineNumber() == lineNumber) myscope = (LinkedList)scopes.clone();

                Matcher m = p.matcher(line);
                if (!m.matches()) continue;

                String label = m.group("label");
                if (label != null) {
                    if (lineNumber > 0) {
                        if (label.charAt(0) == '@') { // local label
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
                    } else {
                        if (label.charAt(0) == '@') continue; // ignore
                    }

                    LinkedList<String> newlabel = (LinkedList)scopes.clone();
                    newlabel.addLast(label);
                    labels.add(new lineInfo(newlabel, lineReader.getLineNumber()));
                }

                String directive = m.group("directive"); // track scopes
                if (directive == null) continue;
                String label2 = m.group("label2"); // track scopes
                switch (directive.toLowerCase()) {
                    case ".scope":
                        if (label2 != null) scopes.add(label2); // new scope
                        break;
                    case ".endscope":
                        if (!scopes.isEmpty()) scopes.removeLast(); // leave scope
                        break;
                }
            }
        }
        catch (IOException ex) {
        }
        labelValues.putAll(localLabelValues);
        // Simple global scope
        if (myscope.isEmpty() || lineNumber < 1) {
            for (lineInfo label : labels) {
                String fullLabel;
                StringBuilder kbuild = new StringBuilder();
                boolean first = false;

                for (String s : label.name) { // build full name
                    if (first) kbuild.append("::");
                    kbuild.append(s);
                    first = true;
                }

                fullLabel = kbuild.toString();
                if (!labelValues.containsValue(fullLabel)) {
                    labelValues.put(label.line, fullLabel); // add if not listed already
                }
            }
            return labelValues;
        }
        // Local scope
        for (lineInfo label : labels) {
            ListIterator<String> myscopei = myscope.listIterator(0);
            StringBuilder kbuild = new StringBuilder();
            boolean different = false, anon = false;

            for (String name : label.name) {
                if (different) {
                    kbuild.append("::");
                } else {
                    if (myscopei.hasNext() && myscopei.next().equals(name)) continue;
                    different = true;
                }
                if (name.length() == 0) {
                    anon = true;
                    break;
                }
                kbuild.append(name);
            }
            if (anon) continue;

            if (!different) kbuild.append(label.name.getLast());
            if (kbuild.length() == 0) continue;

            String fullLabel = kbuild.toString();
            if (!labelValues.containsValue(fullLabel)) {
                labelValues.put(label.line, fullLabel); // add if not listed already
            }
        }
        return labelValues;
    }

    @Override
    public LinkedHashMap getFunctions(LineNumberReader lineReader) {
        return new LinkedHashMap<>();
    }

    @Override
    public String labelGetStart(String line, int pos) {
        String line2 = new StringBuffer(line.substring(0, pos)).reverse().toString();
        Pattern p = Pattern.compile("(?i)(([a-z0-9_]|::)*[a-z_@])([^a-z0-9_:].*|$)");
        Matcher m = p.matcher(line2);
        if (!m.matches()) return "";
        return new StringBuffer(m.group(1)).reverse().toString();
    }

    @Override
    public ArrayList<ErrorInfo> readErrorLines(LineNumberReader lineReader) {
        final ArrayList<ErrorInfo> errors = new ArrayList<>();
        String line;     // j.asm(4): Error: Symbol `i' is undefined
        Pattern p = Pattern.compile("^(?<file>.*?)\\((?<line>\\d+)\\): (?:Error|Warning):.*");
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
