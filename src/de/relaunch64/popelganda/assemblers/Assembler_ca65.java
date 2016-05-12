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
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ArrayList;

/**
 *
 * @author Soci/Singular
 */
class Assembler_ca65 implements Assembler
{
    /**
     * String array with Math functions and scripting language keywords, in
     * case the assembler supports this. Used for syntax highlighting keywords.
     */
    final static String[] scriptKeywords = {
        ".addrsize", ".and", ".bank", ".bankbyte", ".bitand", ".bitnot",
        ".bitor", ".bitxor", ".blank", ".concat", ".const", ".hibyte",
        ".hiword", ".ident", ".left", ".lobyte", ".loword", ".match", ".max",
        ".mid", ".min", ".mod", ".not", ".or", ".ref", ".referenced", ".right",
        ".shl", ".shr", ".sizeof", ".sprintf", ".strat", ".string", ".strlen",
        ".tcount", ".xor", ".xmatch"
    };

    @Override
    public String name() {
        return "ca65";
    }

    @Override
    public String fileName() {
        return "ca65";
    }
    /**
     * Assembler ID.
     * @return the unique assembler ID.
     */
    @Override
    public int getID() {
        return Assemblers.ID_CA65;
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
        return "";
    }

    @Override
    public String getByteDirective() {
        return ".byte";
    }

    @Override
    public String getBasicStart(int start) {
        return ".org $7ff\n!word $801, (:+), 10\n.byte $9e\n.asciiz \"" + Integer.toString(start) + "\"\n: .word 0\n";
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
    public String[] getScriptKeywords() {
        return scriptKeywords;
    }

    @Override
    public String getDefaultCommandLine(String fp) {
        return fp + " -o " + OUTPUT_FILE_REL;
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
        class lineInfo {
            final LinkedList<String> name;
            final int line;
            lineInfo(LinkedList<String> name, int line) {
                this.name = name;
                this.line = line;
            }
        }
        labelList returnValue = new labelList(null, null, null);
        LinkedHashMap<String, Integer> localLabelValues = new LinkedHashMap<>();
        LinkedList<lineInfo> labels = new LinkedList<>();
        String line;
        Pattern p = Pattern.compile("^\\s*(?:(?<label>[a-zA-Z_@][a-zA-Z0-9_]*)(?::|\\s*(?==)))?\\s*(?<directive>\\.(?:scope|endscope|macro|endmacro)\\b)?\\s*(?<name>[a-zA-Z_][a-zA-Z0-9_]*\\b)?.*");
        LinkedList<String> myscope = new LinkedList<>(), scopes = new LinkedList<>();
        boolean scopeFound = false;
        try {
            while ((line = lineReader.readLine()) != null) {
                if (lineReader.getLineNumber() == lineNumber) myscope = (LinkedList)scopes.clone();

                Matcher m = p.matcher(line);
                if (!m.matches()) continue;

                String label = m.group("label");
                if (label != null) {
                    boolean local = (label.charAt(0) == '@');
                    if (lineNumber > 0) {
                        if (local) { // local label
                            if (!scopeFound) localLabelValues.put(label, lineReader.getLineNumber());
                        }  else {
                            if (lineNumber < lineReader.getLineNumber()) {
                                scopeFound = true;
                            } else {
                                localLabelValues.clear();
                            }
                        }
                    }

                    if (!local) {
                        LinkedList<String> newlabel = (LinkedList)scopes.clone();
                        newlabel.addLast(label);
                        labels.add(new lineInfo(newlabel, lineReader.getLineNumber()));
                    }
                }

                String directive = m.group("directive"); // track scopes
                if (directive == null) continue;
                label = m.group("name"); // track scopes
                switch (directive.toLowerCase()) {
                    case ".scope":
                        scopes.add((label != null) ? label : ""); // new scope
                        break;
                    case ".endscope":
                    case ".endmacro":
                        if (!scopes.isEmpty()) scopes.removeLast(); // leave scope
                        break;
                    case ".macro":
                        if (label != null) returnValue.macros.put(label, lineReader.getLineNumber());
                        scopes.add("");
                        break;
                }
            }
        }
        catch (IOException ex) {
        }
        returnValue.labels.putAll(localLabelValues);
        // Simple global scope
        if (myscope.isEmpty() || lineNumber < 1) {
            for (lineInfo label : labels) {
                String fullLabel;
                StringBuilder kbuild = new StringBuilder();
                boolean first = false, anon = false;

                for (String name : label.name) { // build full name
                    if (name.length() == 0) {
                        anon = true;
                        break;
                    }
                    if (first) kbuild.append("::");
                    kbuild.append(name);
                    first = true;
                }
                if (anon) continue;

                fullLabel = kbuild.toString();
                returnValue.labels.put(fullLabel, label.line);
            }
            return returnValue;
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
            returnValue.labels.put(fullLabel, label.line);
        }
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
        Pattern p = Pattern.compile("(?i)(([a-z0-9_]|::)*[a-z_@])([^a-z0-9_:].*|$)");
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
        String line;     // j.asm(4): Error: Symbol `i' is undefined
        Pattern p = Pattern.compile("^(?<file>.*?)\\((?<line>\\d+)\\): (?<type>Error|Warning):.*");
        try {
            while ((line = lineReader.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (!m.matches()) continue;
                ErrorInfo e = new ErrorInfo(
                        Integer.parseInt(m.group("line")),
                        1,
                        lineReader.getLineNumber(), 1,
                        ignore_warnings && m.group("type").equals("Warning"),
                        m.group("file")
                        );
                errors.add(e);
            }
        }
        catch (IOException ex) {
        }
        return errors;
    }

    private static final Pattern directivePattern = Pattern.compile("^\\s*(?:[a-zA-Z_@][a-zA-Z0-9_]*:)?\\s*(?<directive>\\.[a-zA-Z0-9_]+\\b).*");
    private static final Pattern labelPattern = Pattern.compile("^\\s*(?<label>[a-zA-Z_@][a-zA-Z0-9_]*)(?::|\\s*(?==))\\s*(?<equal>=)?.*");
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

        if ((foldtokens & Assemblers.CF_TOKEN_LABELS) != 0) {
            Matcher m = labelPattern.matcher(line);
            boolean check = (foldLevel & 1) == 1;
            if (m.matches()) {
                String label = m.group("label");
                if (label != null && m.group("equal") == null) {
                    foldLevel |= 1;
                    check = false;
                }
            }
            if (check) {
                m = labelPattern.matcher(buffer.getLineText(lineIndex + 1));
                if (m.matches()) {
                    String label = m.group("label");
                    if (label != null && m.group("equal") == null) {
                        foldLevel &= ~1;
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
                        case ".if":
                        case ".ifblank":
                        case ".ifnblank":
                        case ".ifconst":
                        case ".ifdef":
                        case ".ifndef":
                        case ".ifp02":
                        case ".ifp816":
                        case ".ifpc02":
                        case ".ifpsc02":
                        case ".ifref":
                            if ((foldtokens & Assemblers.CF_TOKEN_DIRECTIVES) != 0) foldLevel = (foldLevel & ~1) + 2;
                            break;
                        case ".enum":
                        case ".mac":
                        case ".macro":
                        case ".proc":
                        case ".repeat":
                        case ".scope":
                        case ".struct":
                        case ".union":
                            if ((foldtokens & Assemblers.CF_TOKEN_STRUCTS) != 0) foldLevel = (foldLevel & ~1) + 2;
                            break;
                        case ".endif":
                            if ((foldtokens & Assemblers.CF_TOKEN_DIRECTIVES) != 0) foldLevel = (foldLevel & ~1) - 2;
                            break;
                        case ".endenum":
                        case ".endstruct":
                        case ".endunion":
                        case ".endscope":
                        case ".endrep":
                        case ".endrepeat":
                        case ".endproc":
                        case ".endmac":
                        case ".endmacro":
                            if ((foldtokens & Assemblers.CF_TOKEN_STRUCTS) != 0) foldLevel = (foldLevel & ~1) - 2;
                            break;
                    }
                }
            }
            if ((foldLevel & 1) == 1 && (foldtokens & (Assemblers.CF_TOKEN_SECTIONS | Assemblers.CF_TOKEN_LABELS)) != 0) {
                m = directivePattern.matcher(buffer.getLineText(lineIndex + 1));

                if (m.matches()) {
                    String directive = m.group("directive");
                    if (directive != null) {
                        switch (directive.toLowerCase()) {
                            case ".if":
                            case ".ifblank":
                            case ".ifnblank":
                            case ".ifconst":
                            case ".ifdef":
                            case ".ifndef":
                            case ".ifp02":
                            case ".ifp816":
                            case ".ifpc02":
                            case ".ifpsc02":
                            case ".ifref":
                            case ".endif":
                                if ((foldtokens & Assemblers.CF_TOKEN_DIRECTIVES) != 0) foldLevel &= ~1;
                                break;
                            case ".enum":
                            case ".mac":
                            case ".macro":
                            case ".proc":
                            case ".repeat":
                            case ".scope":
                            case ".struct":
                            case ".union":
                            case ".endenum":
                            case ".endstruct":
                            case ".endunion":
                            case ".endscope":
                            case ".endrep":
                            case ".endrepeat":
                            case ".endproc":
                            case ".endmac":
                            case ".endmacro":
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
