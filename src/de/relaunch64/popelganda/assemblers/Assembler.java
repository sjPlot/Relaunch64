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
import java.io.LineNumberReader;
import java.util.LinkedHashMap;
import java.util.ArrayList;

public interface Assembler 
{
    static final String INPUT_FILE = "SOURCEFILE";
    static final String SOURCE_DIR = "SOURCEDIR";
    static final String OUTPUT_FILE = "OUTFILE";
    static class labelList {
         public final LinkedHashMap labels;
         public final LinkedHashMap functions;
         public final LinkedHashMap macros;
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

    String getMacroString();
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
     * Returns error list
     * 
     * @param lineReader LineNumberReader for log
     * 
     * @return List of errors
     */
    ArrayList<ErrorInfo> readErrorLines(LineNumberReader lineReader);
}
