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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

/**
 *
 * @author Daniel Luedecke
 */
public class ErrorHandler {
    private final ArrayList<Integer> errorLines = new ArrayList<>();
    private final ArrayList<Integer> errorLinesInLog = new ArrayList<>();
    private final ArrayList<File> errorFiles = new ArrayList<>();
    private int errorIndex = -1;
    private String basePath;
    
    public ErrorHandler() {
        clearErrors();
    }
    
    public void setBasePath(String bp) {
        basePath = bp;
    }
    public String getBasePath() {
        return basePath;
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
                int linenumber=1;
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
                        errorLinesInLog.add(linenumber);
                    }
                    // check if we found error line
                    if (errf!=null && !errorFiles.contains(errf)) {
                        errorFiles.add(errf);
                    }
                    linenumber++;
                }
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
                    while (!Tools.isDelimiter(line.charAt(end), ":")) end++;
                    return Integer.parseInt(line.substring(start+token.length(), end));
                }
                catch (NumberFormatException | IndexOutOfBoundsException ex) {
                    return -1;
                }
            }
        }
        return -1;
    }
    // TODO CA65 fehlermeldungen?
    protected File getErrorFileFromLine(String line, int compiler) {
        String file = null;
        try {
            switch (compiler) {
                case ConstantsR64.COMPILER_KICKASSEMBLER:
                    int start = line.indexOf(" in ");
                    if (start!=-1) {
                        file = line.substring(start+4);
                    }
                    break;
                case ConstantsR64.COMPILER_ACME:
                    start = line.toLowerCase().indexOf(" - file ");
                    if (start!=-1) {
                        int end = line.indexOf(", ", start+8);
                        if (end!=-1) {
                            file = line.substring(start+8, end);
                        }
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
        return fp;
    }
    public void gotoNextError(EditorPanes editorPanes, JTextArea log) {
        // check array
        if (!errorLines.isEmpty()) {
            // incease index
            errorIndex++;
            // check index
            if (errorIndex<0 || errorIndex>=errorLines.size()) errorIndex = 0;
            // scroll log
            scrollToErrorInLog(log);
            // open error tab
            openErrorTab(editorPanes);
            // goto error line
            gotoErrorLine(editorPanes);
        }
    }
    public void gotoPrevError(EditorPanes editorPanes, JTextArea log) {
        // check array
        if (!errorLines.isEmpty()) {
            // incease index
            errorIndex--;
            // check index
            if (errorIndex<0 || errorIndex>=errorLines.size()) errorIndex = errorLines.size()-1;
            // scroll log
            scrollToErrorInLog(log);
            // open error tab
            openErrorTab(editorPanes);
            // goto error line
            gotoErrorLine(editorPanes);
        }
    }
    protected void gotoErrorLine(final EditorPanes editorPanes) {
        // goto error line
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // goto error line
                editorPanes.gotoLine(errorLines.get(errorIndex));
                // set focus in edior pane
                editorPanes.setFocus();
            }
        });
    }
    protected void openErrorTab(EditorPanes editorPanes) {
        try {
            File ef = getAbsoluteErrorFilePath();
            // is error file opened?
            int errorTab = editorPanes.getOpenedFileTab(ef);
            // if not, open it
            if (-1==errorTab) {
                editorPanes.loadFile(ef, editorPanes.getActiveCompiler(), 0);
            }
            // if tab is opened, but not selected, select it
            else if (errorTab!=editorPanes.getSelectedTab()) {
                editorPanes.setSelectedTab(errorTab);
            }
        }
        catch (IndexOutOfBoundsException ex) {
        }
    }
    protected File getAbsoluteErrorFilePath() {
        return getAbsoluteErrorFilePath(errorIndex);
    }
    protected File getAbsoluteErrorFilePath(int fileindex) {
        // check index bounds
        if (fileindex<0 || fileindex>=errorFiles.size()) fileindex = 0;
        // does path exist, or is it relative?
        return FileTools.getAbsolutePath(new File(basePath), errorFiles.get(fileindex));
    }
    public final void clearErrors() {
        errorLines.clear();
        errorFiles.clear();
        errorLinesInLog.clear();
        errorIndex = -1;
        basePath = "";
    }
    public boolean hasErrors() {
        return (errorLines!=null && !errorLines.isEmpty());
    }
    public void gotoFirstError(EditorPanes ep, JTextArea log) {
        // check array
        if (!errorLines.isEmpty()) {
            errorIndex = 0;
            // scroll log
            scrollToErrorInLog(log);
            // open error tab
            openErrorTab(ep);
            // goto error line
            gotoErrorLine(ep);
        }
    }
    public File getErrorFile() {
        if (!errorFiles.isEmpty()) {
            return getAbsoluteErrorFilePath();
        }
        return null;
    }
    public void scrollToErrorInLog(JTextArea ta) {
        int line = errorLinesInLog.get(errorIndex);
        try {
            // retrieve element and check whether line is inside bounds
            Element e = ta.getDocument().getDefaultRootElement().getElement(line);
            if (e!=null) {
                // retrieve caret of requested line
                int caret = e.getStartOffset();
                // set new caret position
                ta.setCaretPosition(caret);
                // scroll rect to visible
                ta.scrollRectToVisible(ta.modelToView(caret));
                // scroll some lines back, if possible
                e = ta.getDocument().getDefaultRootElement().getElement(line-1);
                if (e!=null) caret = e.getStartOffset();
                // scroll rect to visible
                ta.scrollRectToVisible(ta.modelToView(caret));
                // scroll some lines further, if possible
                e = ta.getDocument().getDefaultRootElement().getElement(line+1);
                if (e!=null) caret = e.getStartOffset();
                // scroll rect to visible
                ta.scrollRectToVisible(ta.modelToView(caret));
            }
        }
        catch(BadLocationException | IllegalArgumentException | IndexOutOfBoundsException ex) {
        }
    }
}
