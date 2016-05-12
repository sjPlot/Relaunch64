/*
 * Relaunch64 - A Java cross-development IDE for C64 machine language coding.
 * Copyright (C) 2001-2015 by Daniel Lüdecke (http://www.danielluedecke.de)
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
import org.gjt.sp.jedit.buffer.JEditBuffer;
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
class Assembler_dasm implements Assembler
{
    @Override
    public String name() {
        return "DASM";
    }

    @Override
    public String fileName() {
        return "dasm";
    }
    /**
     * Assembler ID.
     * @return the unique assembler ID.
     */
    @Override
    public int getID() {
        return Assemblers.ID_DASM;
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
    public String getDefaultCommandLine(String fp) {
        return fp + " " + INPUT_FILE_REL + " -o" + OUTPUT_FILE_REL;
    }

    @Override
    public String getHelpCLI() {
        return "";
    }

    /**
     * Extracts all labels, functions and macros of a source code file. Information
     * on names and linenumbers of labels, functions and macros are saved as linked
     * hashmaps. Information can then be accessed via 
     * {@link Assembler.labelList#labels labelList.labels},
     * {@link Assembler.labelList#functions labelList.functions} and
     * {@link Assembler.labelList#macros labelList.macros}.
     * 
     * @param lineReader a LineNumberReader from the source code content, which is
     * created in {@link de.relaunch64.popelganda.Editor.LabelExtractor#getLabels(java.lang.String, de.relaunch64.popelganda.assemblers.Assembler, int) LabelExtractor.getLabels()}.
     * @param lineNumber the line number, from where to start the search for labels/functions/macros.
     * use 0 to extract all labels/functions/macros. use any specific line number to extract only
     * global labels/functions/macros and local labels/functions/macros within scope.
     * @return a {@link Assembler.labelList labelList} 
     * with information (names and line numbers) about all extracted labels/functions/macros.
     */
    @Override
    public labelList getLabels(LineNumberReader lineReader, int lineNumber) {
        labelList returnValue = new labelList(null, null, null);
        LinkedHashMap<String, Integer> localLabelValues = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> localLabelValues2 = new LinkedHashMap<>();
        Pattern p = Pattern.compile("(?i)(?<label>^[a-z_.][a-z0-9_]*\\b\\$?)?(?:^\\s*(?<directive>(?:mac|endm)\\b)\\s*(?<name>[a-z_][a-z0-9_]*\\b)?)?(?:\\s*(?<subroutine>subroutine\\b))?.*"); // label always in first column
        String line;
        boolean macro = false;
        boolean scopeFound = false, scopeFound2 = false;
        try {
            while ((line = lineReader.readLine()) != null) {
                Matcher m = p.matcher(line);

                if (!m.matches()) continue;
                String label = m.group("label");

                if (label != null) {
                    if (lineNumber > 0) {
                        if (label.charAt(0) == '.') { // local label
                            if (!scopeFound) localLabelValues.put(label, lineReader.getLineNumber());
                            continue;
                        } 
                        if (label.charAt(label.length()-1) == '$') { // local $ label
                            if (!scopeFound2) localLabelValues2.put(label, lineReader.getLineNumber());
                            continue;
                        } 
                        if (lineNumber < lineReader.getLineNumber()) {
                            scopeFound2 = true;
                        } else {
                            localLabelValues2.clear();
                        }
                    }
                    returnValue.labels.put(label, lineReader.getLineNumber());
                }

                String directive = m.group("directive");
                if (lineNumber > 0 && directive == null && m.group("subroutine") != null) {
                    if (lineNumber < lineReader.getLineNumber()) {
                        scopeFound = true;
                    } else {
                        localLabelValues.clear();
                    }
                }
                if (directive == null) continue;
                switch (directive.toLowerCase()) {
                    case "mac":
                        if (lineNumber < lineReader.getLineNumber()) {
                            scopeFound = true;
                            scopeFound2 = true;
                        } else {
                            localLabelValues.clear();
                            localLabelValues2.clear();
                        }
                        macro = true;
                        label = m.group("name");
                        if (label != null) returnValue.macros.put(label, lineReader.getLineNumber());
                        break;
                    case "endm":
                        if (lineNumber < lineReader.getLineNumber()) {
                            scopeFound = true;
                            scopeFound2 = true;
                        } else {
                            localLabelValues.clear();
                            localLabelValues2.clear();
                        }
                        macro = false;
                        break;
                }
            }
        }
        catch (IOException ex) {
        }
        returnValue.labels.putAll(localLabelValues);
        returnValue.labels.putAll(localLabelValues2);
        return returnValue;
    }
    /**
     * Returns the label name part before the cursor.
     * 
     * @param line Currect line content
     * @param pos Caret position
     * 
     * @return Label name part before the cursor.
     */
    @Override
    public String labelGetStart(String line, int pos) {
        String line2 = new StringBuffer(line.substring(0, pos)).reverse().toString();
        Pattern p = Pattern.compile("(?i)(\\$?[a-z0-9_]*[a-z_.])([^a-z0-9_].*|$)");
        Matcher m = p.matcher(line2);
        if (!m.matches()) return "";
        return new StringBuffer(m.group(1)).reverse().toString();
    }
    /**
     * Parses the error messages from the error log and adds the information
     * to the {@link ErrorHandler.ErrorInfo}.
     * 
     * @param lineReader a LineNumberReader from the error log, which is created
     * by {@link ErrorHandler#readErrorLines(java.lang.String, de.relaunch64.popelganda.assemblers.Assembler) readErrorLines()}.
     * @param ignore_warnings weather to ignore warnings or not
     * @return an ArrayList of {@link ErrorHandler.ErrorInfo} for
     * each logged error.
     */
    @Override
    public ArrayList<ErrorInfo> readErrorLines(LineNumberReader lineReader, boolean ignore_warnings) {
        final ArrayList<ErrorInfo> errors = new ArrayList<>();
        String line;     // a.asm (5): error: Syntax Error 'o o'.
        Pattern p = Pattern.compile("^(?<file>.*?) \\((?<line>\\d+)\\): (?<type>error|warning|fatal): .*");
        try {
            while ((line = lineReader.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (!m.matches()) continue;
                ErrorInfo e = new ErrorInfo(
                        Integer.parseInt(m.group("line")),
                        1,
                        lineReader.getLineNumber(), 1,
                        ignore_warnings && m.group("type").equals("warning"),
                        m.group("file")
                        );
                errors.add(e);
            }
        }
        catch (IOException ex) {
        }
        return errors;
    }

    private static final Pattern directivePattern = Pattern.compile("(?i)(?:^[a-z_.][a-z0-9_]*\\b\\$?)?\\s*(?<directive>[a-z0-9]+\\b).*"); // label always in first column
    private static final Pattern sectionPattern = Pattern.compile("^\\s*;.*@(.*?)@.*");

    // folding by directives, plus manual folding
    @Override
    public int getFoldLevel(JEditBuffer buffer, int lineIndex, int foldtokens) {
        String line = buffer.getLineText(lineIndex);
        int foldLevel = buffer.getFoldLevel(lineIndex) - 1;

        if ((foldtokens & Assemblers.CF_TOKEN_SECTIONS) != 0) {
            Matcher m = sectionPattern.matcher(line);
            if (m.matches()) foldLevel |= 1;
            else if ((foldLevel & 1) == 1) {
                m = sectionPattern.matcher(buffer.getLineText(lineIndex + 1));
                if (m.matches()) foldLevel &= ~1;
            }
        }
        if ((foldtokens & Assemblers.CF_TOKEN_STRUCTS) != 0) {
            Matcher m = directivePattern.matcher(line);
            boolean check = (foldLevel & 1) == 1;
            if (m.matches()) {
                String directive = m.group("directive");
                if (directive != null) {
                    if (directive.toLowerCase().equals("subroutine")) {
                        foldLevel |= 1;
                        check = false;
                    }
                }
            }
            if (check) {
                m = directivePattern.matcher(buffer.getLineText(lineIndex + 1));
                if (m.matches()) {
                    String directive = m.group("directive");
                    if (directive != null) {
                        if (directive.toLowerCase().equals("subroutine")) foldLevel &= ~1;
                    }
                }
            }
        }
        
        if ((foldtokens & (Assemblers.CF_TOKEN_DIRECTIVES | Assemblers.CF_TOKEN_STRUCTS)) != 0) {
            Matcher m = directivePattern.matcher(line);
            if (m.matches()) {
                String directive = m.group("directive");
                if (directive != null) {
                    switch (directive.toLowerCase()) {
                        case "ifconst":
                        case "ifnconst":
                        case "if":
                            if ((foldtokens & Assemblers.CF_TOKEN_DIRECTIVES) != 0) foldLevel = (foldLevel & ~1) + 2;
                            break;
                        case "mac":
                        case "rorg":
                        case "repeat":
                            if ((foldtokens & Assemblers.CF_TOKEN_STRUCTS) != 0) foldLevel = (foldLevel & ~1) + 2;
                            break;
                        case "subroutine":
                            foldLevel |= 1;
                            break;
                        case "endif":
                        case "eif":
                            if ((foldtokens & Assemblers.CF_TOKEN_DIRECTIVES) != 0) foldLevel = (foldLevel & ~1) - 2;
                            break;
                        case "endm":
                        case "rend":
                        case "repend":
                            if ((foldtokens & Assemblers.CF_TOKEN_STRUCTS) != 0) foldLevel = (foldLevel & ~1) - 2;
                            break;
                    }
                }
            }
            if ((foldLevel & 1) == 1) {
                m = directivePattern.matcher(buffer.getLineText(lineIndex + 1));
                if (m.matches()) {
                    String directive = m.group("directive");
                    if (directive != null) {
                        switch (directive.toLowerCase()) {
                            case "subroutine":
                                foldLevel &= ~1;
                                break;
                            case "ifconst":
                            case "ifnconst":
                            case "if":
                            case "endif":
                            case "eif":
                                if ((foldtokens & Assemblers.CF_TOKEN_DIRECTIVES) != 0) foldLevel &= ~1;
                                break;
                            case "mac":
                            case "rorg":
                            case "repeat":
                            case "endm":
                            case "rend":
                            case "repend":
                                if ((foldtokens & Assemblers.CF_TOKEN_STRUCTS) != 0) foldLevel &= ~1;
                                break;
                        }
                    }
                }
            }
        }

        if ((foldtokens & Assemblers.CF_TOKEN_MANUAL) != 0) {
            boolean quote = false;
            boolean quote2 = false;
            boolean comment = false;
            int count = 0;
            for (int i = 0; i < line.length(); i++) {
                if (comment) {
                    if ((foldtokens & Assemblers.CF_TOKEN_MANUAL) == 0) break;
                    switch (line.charAt(i)) {
                        case '{': 
                            if (count < 0) count = 0;
                            count++;
                            if (count == 3) {
                                count = 0;
                                foldLevel += 2;
                            }
                            break;
                        case '}': 
                            if (count > 0) count = 0;
                            count--;
                            if (count == -3) {
                                count = 0;
                                foldLevel -= 2;
                            }
                            break;
                        default: count = 0;
                    }
                    continue;
                }
                switch (line.charAt(i)) {
                    case '"': if (!quote2) quote = !quote; break;
                    case '\'': if (!quote) quote2 = !quote2; break;
                    case ';': if (!quote && !quote2) comment = true; break;
                }
            }
        }
        return foldLevel + 1;
    }
}
