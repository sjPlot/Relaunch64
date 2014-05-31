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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.ArrayList;

/**
 *
 * @author Soci/Singular
 */
public class Assembler_64tass implements Assembler {
    final static String opcodes[] = { // must be sorted!
        "ADC", "ANC", "AND", "ANE", "ARR", "ASL", "ASR", "BCC", "BCS", "BEQ",
        "BGE", "BIT", "BLT", "BMI", "BNE", "BPL", "BRK", "BVC", "BVS", "CLC",
        "CLD", "CLI", "CLV", "CMP", "CPX", "CPY", "DCP", "DEC", "DEX", "DEY",
        "EOR", "GCC", "GCS", "GEQ", "GGE", "GLT", "GMI", "GNE", "GPL", "GVC",
        "GVS", "INC", "INX", "INY", "ISB", "JAM", "JMP", "JSR", "LAX", "LDA",
        "LDS", "LDX", "LDY", "LSR", "NOP", "ORA", "PHA", "PHP", "PLA", "PLP",
        "RLA", "ROL", "ROR", "RRA", "RTI", "RTS", "SAX", "SBC", "SBX", "SEC",
        "SED", "SEI", "SHA", "SHL", "SHR", "SHS", "SHX", "SHY", "SLO", "SRE",
        "STA", "STX", "STY", "TAX", "TAY", "TSX", "TXA", "TXS", "TYA"
    };

    final static String[] scriptKeywords = {
        "abs", "acos", "asin", "atan", "atan2", "cbrt", "ceil", "cos", "cosh",
        "deg", "exp", "floor", "frac", "hypot", "log", "log10",
        "max", "min", "mod", "rad", "round", "sign", "sin", "sinh", "sort",
        "signum", "tan", "tanh", "trunc", "bool", "len", "all", "any", "str",
        "repr", "format", "range"
    };

    @Override
    public String getDelimiterList() {
        return ",:{}()[]+-/<=>&|^~*";
    }
    
    @Override
    public String name() {
        return "64tass";
    }

    @Override
    public String syntaxFile() {
        return "assembly-64tass.xml";
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
    public String getMacroString() {
        return ".macro";
    }

    @Override
    public String getByteDirective() {
        return ".byte";
    }

    @Override
    public String getBasicStart(int start) {
        return "*= $801\n.word (+), 10\n.null $9e, \"" + Integer.toString(start) + "\"\n+ .word 0\n";
    }

    @Override
    public String getIncludeSourceDirective(String path) {
        return ".include \"" + path + "\"";
    }

    @Override
    public String getIncludeTextDirective(String path) {
        return ".binary \"" + path + "\"";
    }

    @Override
    public String getIncludeC64Directive(String path) {
        return ".binary \"" + path + "\",2";
    }

    @Override
    public String getIncludeBinaryDirective(String path) {
        return ".binary \"" + path + "\"";
    }

    @Override
    public String[] getScriptKeywords() {
        return scriptKeywords;
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
        // Daniel: I love this regex-stuff! Unfortunately I'm to old to understand it...
        Pattern p = Pattern.compile("(?i)^\\s*(?<label>[a-z_][a-z0-9_.]*\\b)?\\s*(?<directive>\\.(?:block|bend|proc|pend)\\b)?.*");
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
                        if (label.charAt(0) == '_') { // local label
                            if (scopeFound || lineNumber < 1) continue;
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
                        if (label.charAt(0) == '_') continue; // ignore
                    }

                    if (label.length() == 3 && Arrays.binarySearch(opcodes, label.toUpperCase()) >= 0) {
                        continue; // ignore opcodes
                    }
                    LinkedList<String> newlabel = (LinkedList)scopes.clone();
                    newlabel.addLast(label);
                    labels.add(new lineInfo(newlabel, lineReader.getLineNumber()));
                }

                String directive = m.group("directive"); // track scopes
                if (directive == null) continue;
                switch (directive.toLowerCase()) {
                    case ".block":
                    case ".proc":
                        if (label != null) scopes.add(label); // new scope
                        else scopes.add("");
                        break;
                    case ".bend":
                    case ".pend":
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
                    if (first) kbuild.append('.');
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
                    kbuild.append('.');
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
        Pattern p = Pattern.compile("(?i)([a-z0-9_.]*[a-z_]\\b)([^a-z0-9_.].*|$)");
        Matcher m = p.matcher(line2);
        if (!m.matches()) return "";
        return new StringBuffer(m.group(1)).reverse().toString();
    }

    @Override
    public ArrayList<ErrorInfo> readErrorLines(LineNumberReader lineReader) {
        final ArrayList<ErrorInfo> errors = new ArrayList<>();
        String line;     // j.asm:4:5: error: not defined 'i'
        Pattern p = Pattern.compile("^(?<file>.*?):(?<line>\\d+):(?<col>\\d+): (?:error|warning):.*");
        try {
            while ((line = lineReader.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (!m.matches()) continue;
                ErrorInfo e = new ErrorInfo(
                        Integer.parseInt(m.group("line")),
                        Integer.parseInt(m.group("col")),
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
