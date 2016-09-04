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

import de.relaunch64.popelganda.Editor.EditorPanes;
import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.FileTools;
import java.io.BufferedReader;
import java.io.File;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.DefaultHighlighter;

/**
 *
 * @author Daniel Luedecke
 * @author Soci/Singular
 */
public class ErrorHandler {
    private int errorIndex = -1;
    private String basePath;
    
    public static class ErrorInfo {
        public final int line;
        public final int column;
        public final int logline;
        public final int loglen;
        public final boolean info_only;
        public final File file;
        /**
         * Info class that saves information about errors of a compiled
         * source code.
         * 
         * @param line the line of the source code, which contains errors or warnings
         * @param column the column of the source code, where an error or warning was found
         * @param logline the line of the error message in the error log
         * @param loglen the length of the error message in lines. most assemblers have 
         * all error information in one line. however, KickAss for instance,
         * uses two lines.
         * @param info_only
         * @param file the source file where the error was found. can be used to
         * open the file with errors (if not already opened), by using the
         * {@link #getAbsoluteErrorFilePath()} method.
         */
        public ErrorInfo(int line, int column, int logline, int loglen, boolean info_only, String file) {
            this.line = line;
            this.column = column;
            this.logline = logline;
            this.loglen = loglen;
            this.info_only = info_only;
            this.file = new File(file);
        }
    }
    private final ArrayList<ErrorInfo> errors = new ArrayList<>();
    private final ArrayList<ErrorInfo> infos = new ArrayList<>();

    public ErrorHandler() {
        clearErrors();
    }
    /**
     * Sets the base path of the currently compiled file. This
     * path is used for include files that may have errors, so
     * include files can be opened and the error line shown.
     * 
     * @param bp the currently compiled source file, which will be
     * used to find other relative included asm files.
     */
    public void setBasePath(File bp) {
        basePath = FileTools.getFilePath(bp);
    }
    /**
     * Returns the basepath, ie the file path of the currently compiled
     * source code (w/o filename).
     * 
     * @return the file path of the currently compiled
     * source code, w/o file name and extension.
     */
    public String getBasePath() {
        return basePath;
    }
    /**
     * Parses the content of the error log and adds all extracted errors
     * to the {@link #errors}-array, where information about errors are saved.
     * 
     * @param log the content of the error log
     * @param assembler the assembler that was used to comipile the file, in order
     * to correctly parse the error messages.
     * @param offset
     * @param ignore_warnings weather to ignore warnings or not
     * @return 
     */
    public int readErrorLines(String log, Assembler assembler, int offset, boolean ignore_warnings) {
        // create buffered reader, needed for line number reader
        StringReader sr = new StringReader(log);
        BufferedReader br = new BufferedReader(sr);
        LineNumberReader lineReader = new LineNumberReader(br);
        lineReader.setLineNumber(offset);
        // find all errors, use assembler specific error parsing
        // to detect line and column numbers of warnings and errors.
        ArrayList<ErrorInfo> newerrors = assembler.readErrorLines(lineReader, ignore_warnings);
        for (int n = 0; n < newerrors.size(); n++) {
            ErrorInfo e = newerrors.get(n);
            if (e.info_only) {
                infos.add(e);
            } else {
                errors.add(e);
            }
        }
        return lineReader.getLineNumber();
    }
    /**
     * Sets the caret to the error indicated by {@code index} and highlights
     * the selected error line in the log.
     * 
     * @param editorPanes
     * @param log
     * @param index 
     */
    protected void gotoError(EditorPanes editorPanes, JTextArea log, int index) {
        // check if any errors at all
        if (hasErrors()) {
            ErrorInfo e;
            // index
            errorIndex = index % errors.size();
            if (errorIndex < 0) errorIndex += errors.size();
            e = errors.get(errorIndex);
            // scroll log
            scrollToErrorInLog(log, e);
            // open error tab
            openErrorTab(editorPanes, e);
            // goto error line
            gotoErrorLine(editorPanes, e);
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
    public void gotoErrorByClick(EditorPanes editorPanes, JTextArea log, int line) {
        for (int n = 0; n < errors.size(); n++) {
            ErrorInfo e = errors.get(n);
            if (e.logline <= line && e.logline + e.loglen > line) {
                gotoError(editorPanes, log, n);
                return;
            }
        }
        for (int n = 0; n < infos.size(); n++) {
            ErrorInfo e = infos.get(n);
            if (e.logline <= line && e.logline + e.loglen > line) {
                // scroll log
                scrollToErrorInLog(log, e);
                // open error tab
                openErrorTab(editorPanes, e);
                // goto error line
                gotoErrorLine(editorPanes, e);
                return;
            }
        }
    }
    protected void gotoErrorLine(final EditorPanes editorPanes, ErrorInfo error) {
        final ErrorInfo ei = error;
        // goto error line
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // goto error line
                editorPanes.gotoLine(ei.line, ei.column);
                // set focus in edior pane
                editorPanes.setFocus();
            }
        });
    }
    protected void openErrorTab(EditorPanes editorPanes, ErrorInfo error) {
        try {
            File ef = getAbsoluteErrorFilePath(error);
            // check for null
            if (ef != null) {
                // is error file opened?
                int errorTab = editorPanes.getOpenedFileTab(ef);
                // if not, open it
                if (-1 == errorTab) {
                    editorPanes.loadFile(ef, editorPanes.getActiveAssembler(), 0, -1);
                } // if tab is opened, but not selected, select it
                else if (errorTab != editorPanes.getSelectedTab()) {
                    editorPanes.setSelectedTab(errorTab);
                }
            }
        } catch (IndexOutOfBoundsException ex) {
        }
    }
    protected File getAbsoluteErrorFilePath(ErrorInfo error) {
        // does path exist, or is it relative?
        return FileTools.getAbsolutePath(new File(basePath), error.file);
    }
    public final void clearErrors() {
        errors.clear();
        infos.clear();
        errorIndex = -1;
        basePath = "";
    }
    public boolean hasErrors() {
        return (errors != null && !errors.isEmpty());
    }
    public void scrollToErrorInLog(JTextArea ta, ErrorInfo error) {
        int line = error.logline - 1;
        int len = error.loglen;
        if (line < 0) line = 0;
        try {
            // retrieve element and check whether line is inside bounds
            Element e = ta.getDocument().getDefaultRootElement().getElement(line);
            if (e!=null) {
                // retrieve caret of requested line
                int caret = e.getStartOffset(), start, end;
                // set new caret position
                ta.setCaretPosition(caret);
                // scroll rect to visible
                ta.scrollRectToVisible(ta.modelToView(caret));
                // scroll some lines back, if possible
                e = ta.getDocument().getDefaultRootElement().getElement(line + len);
                if (e!=null) caret = e.getStartOffset();
                end = caret;
                // scroll rect to visible
                ta.scrollRectToVisible(ta.modelToView(caret));
                // scroll some lines further, if possible
                e = ta.getDocument().getDefaultRootElement().getElement(line);
                if (e!=null) caret = e.getStartOffset();
                start = caret;
                // scroll rect to visible
                ta.scrollRectToVisible(ta.modelToView(caret));
                // create highlighter
                HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(ConstantsR64.OSX_HIGHLIGHT_STYLE);
                ta.getHighlighter().removeAllHighlights();
                // highlight current error line
                ta.getHighlighter().addHighlight(start, end, painter);
            }
        }
        catch(BadLocationException | IllegalArgumentException | IndexOutOfBoundsException ex) {
        }
    }
}
