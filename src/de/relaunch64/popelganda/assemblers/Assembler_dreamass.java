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

import de.relaunch64.popelganda.assemblers.ErrorHandler.ErrorInfo;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.ArrayList;

/**
 *
 * @author Soci/Singular
 */
class Assembler_dreamass implements Assembler
{
    final static String opcodes[] = { // must be sorted!
        "ACN", "ADC", "AHX", "ALR", "ANC", "AND", "ARR", "ASL", "AXS", "BCC",
        "BCS", "BEQ", "BIT", "BMI", "BNE", "BPL", "BRK", "BVC", "BVS", "CLC",
        "CLD", "CLI", "CLV", "CMP", "CPX", "CPY", "DCP", "DEC", "DEX", "DEY",
        "EOR", "INC", "INX", "INY", "ISC", "JMP", "JSR", "KI0", "KI1", "KI2",
        "KI3", "KI4", "KI5", "KI6", "KI7", "KI9", "KIB", "KID", "KIF", "LAS",
        "LAX", "LDA", "LDX", "LDY", "LSR", "NO0", "NO2", "NO4", "NO6", "NO8",
        "NOC", "NOE", "NOP", "NOX", "NOY", "ORA", "PHA", "PHP", "PLA", "PLP",
        "RLA", "ROL", "ROR", "RRA", "RTI", "RTS", "SAX", "SBC", "SBI", "SEC",
        "SED", "SEI", "SHX", "SLO", "SRE", "STA", "STX", "STY", "TAS", "TAX",
        "TAY", "TSX", "TXA", "TXS", "TYA", "XAA"
    };

    @Override
    public String name() {
        return "DreamAss";
    }

    @Override
    public String fileName() {
        return "dreamass";
    }
    /**
     * Assembler ID.
     * @return the unique assembler ID.
     */
    @Override
    public int getID() {
        return Assemblers.ID_DREAMASS;
    }

    @Override
    public String syntaxFile() {
        return "assembly-dreamass.xml";
    }

    @Override
    public String getLineComment() {
        return ";";
    }

    @Override
    public String getMacroPrefix() {
        return "#";
    }

    @Override
    public String getMacroString() {
        return "#macro";
    }

    @Override
    public String getByteDirective() {
        return ".byte";
    }

    @Override
    public String getBasicStart(int start) {
        return "*= $801\n.(\n.word s, 10\n.pet $9e, \"" + Integer.toString(start) + "\", 0\ns .word 0\n.)\n";
    }

    @Override
    public String getIncludeSourceDirective(String path) {
        return "#include \"" + path + "\"";
    }

    @Override
    public String getIncludeTextDirective(String path) {
        return ".binclude \"" + path + "\"";
    }

    @Override
    public String getIncludeC64Directive(String path) {
        return ".binclude \"" + path + "\",2";
    }

    @Override
    public String getIncludeBinaryDirective(String path) {
        return ".binclude \"" + path + "\"";
    }

    @Override
    public String[] getScriptKeywords() {
        return new String[] {};
    }

    @Override
    public String getDefaultCommandLine(String fp) {
        return fp + " -I" + SOURCE_DIR + " -o " + OUTPUT_FILE + " " + INPUT_FILE;
    }

    @Override
    public LinkedHashMap getLabels(LineNumberReader lineReader, int lineNumbers) {
        LinkedHashMap<Integer, String> labelValues = new LinkedHashMap<>();
        Pattern p = Pattern.compile("^\\s*(?<label>[a-zA-Z_][a-zA-Z0-9_]*\\b).*");
        String line;
        try {
            while ((line = lineReader.readLine()) != null) {
                Matcher m = p.matcher(line);

                if (!m.matches()) continue;
                String label = m.group("label");

                if (label != null) {
                    if (label.length() == 3 && Arrays.binarySearch(opcodes, label.toUpperCase()) >= 0) continue;
                    if (!labelValues.containsValue(label)) {
                        labelValues.put(lineReader.getLineNumber(), label); // add if not listed already
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

    @Override
    public String labelGetStart(String line, int pos) {
        String line2 = new StringBuffer(line.substring(0, pos)).reverse().toString();
        Pattern p = Pattern.compile("(?i)([a-z0-9_]*[a-z_]\\b).*");
        Matcher m = p.matcher(line2);
        if (!m.matches()) return "";
        return new StringBuffer(m.group(1)).reverse().toString();
    }

    @Override
    public ArrayList<ErrorInfo> readErrorLines(LineNumberReader lineReader) {
        final ArrayList<ErrorInfo> errors = new ArrayList<>();
        String line;     // j.asm:4: error:variable undefined: i
        Pattern p = Pattern.compile("^(?<file>.*?):(?<line>\\d+): (?:error|warning):.*");
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
