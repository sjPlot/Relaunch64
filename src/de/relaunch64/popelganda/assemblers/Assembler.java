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
import java.io.LineNumberReader;
import java.util.LinkedHashMap;
import java.util.ArrayList;

public interface Assembler 
{
    static final String INPUT_FILE = "SOURCEFILE";
    static final String INPUT_FILE_REL = "RSOURCEFILE";
    static final String SOURCE_DIR = "SOURCEDIR";
    static final String OUTPUT_FILE = "OUTFILE";
    static final String OUTPUT_FILE_REL = "ROUTFILE";
    static final String INPUT_FILENAME = "SOURCENAME";
    static final String OUTPUT_FILENAME = "OUTNAME";
    /**
     * A class with information on all extracted labels, functions and macros
     * of a source code file. Information are saved as HashMaps with names
     * and line numbers ({@code HashMap<String, Integer>}).
     * This static class has three hashmaps:
     * {@code labels},  {@code functions} and {@code macros}.
     */
    static class labelList {
        /**
         * A linked HashMap with all label names and their line numbers
         * from a source code.
         */
         public final LinkedHashMap labels;
        /**
         * A linked HashMap with all function names and their line numbers
         * from a source code.
         */
         public final LinkedHashMap functions;
        /**
         * A linked HashMap with all macro names and their line numbers
         * from a source code.
         */
         public final LinkedHashMap macros;
         /**
          * Saves names and line numbers of labels, functions and macros of the current
          * source code. Information are saved as HashMaps with names
          * and line numbers ({@code HashMap<String, Integer>}).
          * This static class has three hashmaps:
          * {@code labels},  {@code functions} and {@code macros}.
          * 
          * @param labels A linked HashMap with all label names and the line numbers
          * where the labels are.
          * @param functions A linked HashMap with all function names and the line numbers
          * where the functions are.
          * @param macros A linked HashMap with all macro names and the line numbers
          * where the macros are.
          */
         labelList(LinkedHashMap<String, Integer> labels, LinkedHashMap<String, Integer> functions, LinkedHashMap<String, Integer> macros) {
             this.labels = (labels != null) ? labels : new LinkedHashMap<String, Integer>();
             this.functions = (functions != null) ? functions : new LinkedHashMap<String, Integer>();
             this.macros = (macros != null) ? macros : new LinkedHashMap<String, Integer>();
         }
    }

    String name();
    String fileName();
    int getID();
    String syntaxFile();

    String getMacroPrefix();
    String getLineComment();
    String getByteDirective();
    String getBasicStart(int start);
    String getIncludeSourceDirective(String path);
    String getIncludeTextDirective(String path);
    String getIncludeC64Directive(String path);
    String getIncludeBinaryDirective(String path);
    String[] getScriptKeywords();
    String getDefaultCommandLine(String fp);
    String getHelpCLI();
    /**
     * Extracts all labels, functions and macros of a source code file. Information
     * on names and linenumbers of labels, functions and macros are saved as linked
     * hashmaps. Information can then be accessed via 
     * {@link labelList#labels labelList.labels},
     * {@link labelList#functions labelList.functions} and
     * {@link labelList#macros labelList.macros}.
     * 
     * @param lineReader a LineNumberReader from the source code content, which is
     * created in {@link de.relaunch64.popelganda.Editor.LabelExtractor#getLabels(java.lang.String, de.relaunch64.popelganda.assemblers.Assembler, int) LabelExtractor.getLabels()}.
     * @param line the line number, from where to start the search for labels/functions/macros.
     * use 0 to extract all labels/functions/macros. use any specific line number to extract only
     * global labels/functions/macros and local labels/functions/macros within scope.
     * @return a {@link labelList labelList} 
     * with information (names and line numbers) about all extracted labels/functions/macros.
     */
    labelList getLabels(LineNumberReader lineReader, int line);
    /**
     * Returns the label name part before the cursor.
     * 
     * @param line Currect line content
     * @param pos Caret position
     * 
     * @return Label name part before the cursor.
     */
    String labelGetStart(String line, int pos);
    /**
     * Parses the error messages from the error log and adds the information
     * to the {@link de.relaunch64.popelganda.assemblers.ErrorHandler.ErrorInfo}.
     * 
     * @param lineReader a LineNumberReader from the error log, which is created
     * by {@link de.relaunch64.popelganda.assemblers.ErrorHandler#readErrorLines(java.lang.String, de.relaunch64.popelganda.assemblers.Assembler) readErrorLines()}.
     * @param ignore_warnings weather to ignore warnings or not
     * @return an ArrayList of {@link de.relaunch64.popelganda.assemblers.ErrorHandler.ErrorInfo} for
     * each logged error.
     */
    ArrayList<ErrorInfo> readErrorLines(LineNumberReader lineReader, boolean ignore_warnings);
    /**
     * Gets fold level compared to previous line
     * 
     * @param buffer
     * @param lineIndex
     * @param foldtokens
     * Access settings for foldtokens like this:<br>
     * if ((foldtokens & Assemblers.CF_TOKEN_MANUAL)!=0) ...<br>
     * valid constants are:<br>
     * <ul>
     * <li>CF_TOKEN_MANUAL</li>
     * <li>CF_TOKEN_BRACES</li>
     * <li>CF_TOKEN_LABELS</li>
     * <li>CF_TOKEN_DIRECTIVES</li>
     * <li>CF_TOKEN_STRUCTS</li>
     * </ul>
     * @return 
     */
    int getFoldLevel(JEditBuffer buffer, int lineIndex, int foldtokens);
}
