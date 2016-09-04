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
package de.relaunch64.popelganda.Editor;

import java.io.File;

/**
 * Saves specific global properties of editor panes 
 * ({@link de.relaunch64.popelganda.Editor.RL64TextArea RL64TextArea})
 * which need quick access and are needed to handle multiple opened
 * documents.
 * 
 * @author Daniel Luedecke
 */
public class EditorPaneProperties {
    private RL64TextArea editorPane;
    private boolean modified;
    private File filePath;
    private int script;
    private int alternativeScript;
    private String lineEnd;
    private int labelSourcePosition;
    private int labelDefinitionPosition;
    private long lastModified;
    
    public EditorPaneProperties() {
        resetEditorPanesProperties();
    }
    public final void resetEditorPanesProperties() {
        editorPane = null;
        modified = false;
        filePath = null;
        script = 0;
        alternativeScript = -1;
        lastModified = 0L;
        lineEnd = System.lineSeparator();
    }
    public RL64TextArea getEditorPane() {
        return editorPane;
    }
    public void setLabelSourcePosition(int caretPosition) {
        labelSourcePosition = caretPosition;
    }
    public int getLabelSourcePosition() {
        return labelSourcePosition;
    }
    public void setLabelDefintionPosition(int caretPosition) {
        labelDefinitionPosition = caretPosition;
    }
    public int getLabelDefintionPosition() {
        return labelDefinitionPosition;
    }
    public void setEditorPane(RL64TextArea ep) {
        editorPane = ep;
    }
    public long getLastModified() {
        return lastModified;
    }
    public void setLastModified(long m) {
        lastModified = m;
    }
    public boolean isFileModified() {
        // first check for valid file path
        if (filePath != null && filePath.exists()) {
            // then check for modification date
            if (lastModified != 0L) {
                return lastModified != filePath.lastModified();
            }
        }
        return false;
    }
    public boolean isModified() {
        return modified;
    }
    public void setModified(boolean m) {
        modified = m;
    }
    public File getFilePath() {
        return filePath;
    }
    public void setFilePath(File fp) {
        filePath = fp;
    }
    public int getScript() {
        return script;
    }
    public void setScript(int s) {
        script = s;
    }
    public void setAltScript(int s) {
        alternativeScript = s;
    }
    public int getAltScript() {
        return alternativeScript;
    }
    public String getLineEnd() {
        return lineEnd;
    }
    public void setLineEnd(String s) {
        lineEnd = s;
    }
}
