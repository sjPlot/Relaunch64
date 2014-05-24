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

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Deque;
import java.util.Arrays;

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

    @Override
    public String name() {
        return "64tass";
    }

    @Override
    public String syntaxFile() {
        return "assembly-64tass.xml";
    }

    @Override
    public LinkedHashMap getLabels(LineNumberReader lineReader, int lineNumber) {
        LinkedHashMap<Integer, String> labelValues = new LinkedHashMap<>();
        String line;
        Matcher m;
        // Daniel: I love this regex-stuff! Unfortunately I'm to old to understand it...
        Pattern p = Pattern.compile("(?i)^\\s*(?<label>[a-z][a-z0-9_.]*\\b)?\\s*(?<directive>\\.(?:block|bend|proc|pend)\\b)?.*");
        Deque<String> scopes = new LinkedList<>();

        try {
            while ((line = lineReader.readLine()) != null) {
                m = p.matcher(line);

                if (!m.matches()) continue;
                String label = m.group("label");

                if (label != null) {
                    String fullLabel;
                    if (label.length() == 3 && Arrays.binarySearch(opcodes, label.toUpperCase()) >= 0) {
                        continue; // ignore opcodes
                    }

                    if (scopes.isEmpty()) {
                        fullLabel = label; // global scope
                    } else {
                        StringBuilder kbuild = new StringBuilder();

                        for (String s : scopes) { // build full name
                            if (s.length() == 0) continue;
                            kbuild.append(s);
                            kbuild.append('.');
                        }
                        kbuild.append(label);
                        fullLabel = kbuild.toString();
                    }

                    if (!labelValues.containsValue(fullLabel)) {
                        labelValues.put(lineReader.getLineNumber(), fullLabel); // add if not listed already
                    }
                }

                String directive = m.group("directive"); // track scopes
                if (directive != null) {
                    directive = directive.toLowerCase();
                    switch (directive) {
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
        }
        catch (IOException ex) {
        }
        return labelValues;
    }

    @Override
    public LinkedHashMap getFunctions(LineNumberReader lineReader) {
        return new LinkedHashMap<>();
    }
}
