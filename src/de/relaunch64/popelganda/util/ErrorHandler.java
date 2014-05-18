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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

/**
 *
 * @author Daniel Luedecke
 */
public class ErrorHandler {
    private int errorIndex = -1;
    private String basePath;
    
   private class ErrorInfo {
        public final int line;
        public final int column;
        public final int logline;
        public final File file;
        public ErrorInfo(int line, int column, int logline, File file) {
            this.line = line;
            this.column = column;
            this.logline = logline;
            this.file = file;
        }
   }
   private final ArrayList<ErrorInfo> errors = new ArrayList<>();

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
        String line, pattern;
        Pattern p;
        int FilenameGroup, LineGroup, ColumnGroup;
        switch (compiler) {
            case ConstantsR64.COMPILER_ACME: // Error - File j.asm, line 4 (Zone <untitled>): Value not defined.
                pattern = "^(Error|Warning|Serious error) - File ([^,]*), line (\\d+) .*";
                FilenameGroup = 2;
                LineGroup = 3;
                ColumnGroup = 0;
                break;
            case ConstantsR64.COMPILER_KICKASSEMBLER: // at line 2, column 1 in /tmp/j.asm
                pattern = "^at line (\\d+), column (\\d+) in (.*)";
                FilenameGroup = 3;
                LineGroup = 1;
                ColumnGroup = 2;
                break;
            case ConstantsR64.COMPILER_64TASS: // j.asm:4:5: error: not defined 'i'
                pattern = "^([^:]*):(\\d+):(\\d+): (error|warning):.*";
                FilenameGroup = 1;
                LineGroup = 2;
                ColumnGroup = 3;
                break;
            case ConstantsR64.COMPILER_CA65: // j.asm(4): Error: Symbol `i' is undefined
                pattern = "^([^(]*)\\((\\d+)\\): (Error|Warning):.*";
                FilenameGroup = 1;
                LineGroup = 2;
                ColumnGroup = 0;
                break;
            case ConstantsR64.COMPILER_DREAMASS: // j.asm:4: error:variable undefined: i
                pattern = "^([^:]*):(\\d+): (error|warning):.*";
                FilenameGroup = 1;
                LineGroup = 2;
                ColumnGroup = 0;
                break;
            default:
                return; // Unsupported
        }
        p = Pattern.compile(pattern);
        // check for valid values
        if (log!=null && !log.isEmpty()) {
            // read line by line
            try {
                int linenumber=1;
                while ((line = lineReader.readLine())!=null) {
                    Matcher m = p.matcher(line);
                    // check if we found error line
                    if (m.matches()) {
                       int column = 1;
                       if (ColumnGroup != 0) column = Integer.parseInt(m.group(ColumnGroup));
                       ErrorInfo e = new ErrorInfo(Integer.parseInt(m.group(LineGroup)), column, linenumber, new File(m.group(FilenameGroup)));
                       errors.add(e);
                    }
                    linenumber++;
                }
            }
            catch (IOException ex) {
            }
        }
    }
    protected void gotoError(EditorPanes editorPanes, JTextArea log, int index) {
        if (hasErrors()) {
            // index
            errorIndex = index % errors.size();
            if (errorIndex < 0) errorIndex += errors.size();
            // scroll log
            scrollToErrorInLog(log);
            // open error tab
            openErrorTab(editorPanes);
            // goto error line
            gotoErrorLine(editorPanes);
        }
    }
    public void gotoPrevError(EditorPanes editorPanes, JTextArea log) {
        gotoError(editorPanes, log, errorIndex-1);
    }
    public void gotoNextError(EditorPanes editorPanes, JTextArea log) {
        gotoError(editorPanes, log, errorIndex+1);
    }
    public void gotoFirstError(EditorPanes editorPanes, JTextArea log) {
        gotoError(editorPanes, log, 0);
    }
    protected void gotoErrorLine(final EditorPanes editorPanes) {
        // goto error line
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // goto error line
                editorPanes.gotoLine(errors.get(errorIndex).line);
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
        if (fileindex<0 || fileindex>=errors.size()) fileindex = 0;
        // does path exist, or is it relative?
        return FileTools.getAbsolutePath(new File(basePath), errors.get(fileindex).file);
    }
    public final void clearErrors() {
        errors.clear();
        errorIndex = -1;
        basePath = "";
    }
    public boolean hasErrors() {
        return (errors!=null && !errors.isEmpty());
    }
    public File getErrorFile() {
        if (hasErrors()) {
            return getAbsoluteErrorFilePath();
        }
        return null;
    }
    public void scrollToErrorInLog(JTextArea ta) {
        int line = errors.get(errorIndex).logline;
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
