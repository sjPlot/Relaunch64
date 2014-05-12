/*
 * Relaunch64 - A Java Crossassembler for C64 machine language coding.
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
package de.relaunch64.popelganda.util;

import de.relaunch64.popelganda.Editor.EditorPanes;
import static de.relaunch64.popelganda.util.Tools.isDelimiter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Daniel Luedecke
 */
public class ErrorHandler {
    private final ArrayList<Integer> errorLines = new ArrayList<>();
    private final ArrayList<File> errorFiles = new ArrayList<>();
    
    public ErrorHandler() {
        clearErrors();
    }
    
    public void readErrorLines(String log, int compiler) {
        // create buffered reader, needed for line number reader
        BufferedReader br = new BufferedReader(new StringReader(log));
        LineNumberReader lineReader = new LineNumberReader(br);
        String line;
        // check for valid values
        if (log!=null && !log.isEmpty()) {
            // read line by line
            try {
                // TODO errorfiles auslesen
                int err;
                File errf;
                while ((line = lineReader.readLine())!=null) {
                    err = -1;
                    errf = null;
                    // check if line contains error-tokenand line number
                    // ACME-Syntax
                    if ((line.toLowerCase().contains("error") || line.toLowerCase().contains("warning")) && line.toLowerCase().contains("line")) {
                        err = getErrorLineFromLine(line, "line ");
                        errf = getErrorFileFromLine(line, compiler);
                    }
                    // check if we have error, but not line
                    // kick ass syntax
                    else if ((line.toLowerCase().contains("error") || line.toLowerCase().contains("warning")) && !line.toLowerCase().contains("line")) {
                        // read line
                        line = lineReader.readLine();
                        if (line!=null) {
                            err = getErrorLineFromLine(line, "line ");
                            errf = getErrorFileFromLine(line, compiler);
                        }
                    }
                    // check if we have no "line", but colon
                    // tass syntax
                   else if (line.toLowerCase().contains(":") && !line.toLowerCase().contains("error") && !line.toLowerCase().contains("warning")) {
                        err = getErrorLineFromLine(line, ":");
                        errf = getErrorFileFromLine(line, compiler);
                    }
                    // check if we found error line
                    if (err!=-1 && !errorLines.contains(err)) {
                        errorLines.add(err);
                    }
                    // check if we found error line
                    if (errf!=null && !errorFiles.contains(errf)) {
                        errorFiles.add(errf);
                    }
                }
                // sort list
                Collections.sort(errorLines);
                Collections.sort(errorFiles);
            }
            catch (IOException ex) {
            }
        }
   }
    protected int getErrorLineFromLine(String line, String token) {
        if (line!=null && !line.isEmpty()) {
            int start = line.toLowerCase().indexOf(token, 0);
            if (start!=-1) {
                int end = start+token.length();
                try {
                    while (!isDelimiter(line.charAt(end), ":")) end++;
                    return Integer.parseInt(line.substring(start+token.length(), end));
                }
                catch (NumberFormatException | IndexOutOfBoundsException ex) {
                    return -1;
                }
            }
        }
        return -1;
    }
    protected File getErrorFileFromLine(String line, int compiler) {
        // TODO error file auslesen
        String file = null;
        try {
            switch (compiler) {
                case ConstantsR64.COMPILER_KICKASSEMBLER:
                    int start = line.indexOf(" in ");
                    if (start!=-1) {
                        file = line.substring(start+4);
                    }
                    break;
                case ConstantsR64.COMPILER_64TASS:
                    start = line.indexOf(":");
                    if (start!=-1) {
                        file = line.substring(0,start);
                    }
                    break;
            }
        }
        catch (IndexOutOfBoundsException ex) {
            return null;
        }
        // create file
        File fp = (file!=null) ? new File(file) : null;
        // check if exists
        return (fp!=null && fp.exists()) ? fp : null;
    }
    public void gotoNextError(EditorPanes editorPanes) {
        // check if we found anything
        boolean errorFound = false;
        // get current line of caret
        int currentLine = editorPanes.getCurrentLineNumber();
        for (Integer errorLine : errorLines) {
            // if we found a line number greater than current
            // line, we found the next label from caret position
            if (errorLine > currentLine) {
                editorPanes.gotoLine(errorLine);
                errorFound = true;
                break;
            }
        }
        try {
            // found anything?
            // if not, start from beginning
            if (!errorFound) editorPanes.gotoLine(errorLines.get(0));
        }
        catch (IndexOutOfBoundsException ex) {
        }
    }
    public void gotoPrevError(EditorPanes editorPanes) {
        // check if we found anything
        boolean errorFound = false;
        // get current line of caret
        int currentLine = editorPanes.getCurrentLineNumber();
        // iterate all line numbers
        for (int i=errorLines.size()-1; i>=0; i--) {
            // if we found a line number greater than current
            // line, we found the next label from caret position
            if (errorLines.get(i)<currentLine) {
                editorPanes.gotoLine(errorLines.get(i));
                errorFound = true;
                break;
            }
        }
        try {
            // found anything?
            // if not, start from beginning
            if (!errorFound) editorPanes.gotoLine(errorLines.get(errorLines.size()-1));
        }
        catch (IndexOutOfBoundsException ex) {
        }
    }
    public final void clearErrors() {
        errorLines.clear();
        errorFiles.clear();
    }
    public boolean hasErrors() {
        return (errorLines!=null && !errorLines.isEmpty());
    }
    public void gotoFirstError(final EditorPanes ep) {
        // check array
        if (!errorLines.isEmpty()) {
            ep.gotoLine(errorLines.get(0));
        }
    }
    public File getErrorFile() {
        if (!errorFiles.isEmpty()) {
            return errorFiles.get(0);
        }
        return null;
    }
}
