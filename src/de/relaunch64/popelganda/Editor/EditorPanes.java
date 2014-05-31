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
package de.relaunch64.popelganda.Editor;

import de.relaunch64.popelganda.Relaunch64View;
import de.relaunch64.popelganda.database.Settings;
import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.FileTools;
import java.awt.dnd.DropTarget;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.gjt.sp.jedit.buffer.BufferListener;
import org.gjt.sp.jedit.buffer.JEditBuffer;

/**
 *
 * @author Daniel Luedecke
 */
public class EditorPanes {
    private final List<EditorPaneProperties> editorPaneArray = new ArrayList<>();
    private JTabbedPane tabbedPane = null;
    private JComboBox jComboBoxCompiler = null;
    private JComboBox jComboBoxScripts = null;
    private final Relaunch64View mainFrame;
    private final Settings settings;
    private final org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class)
                                                                                                   .getContext().getResourceMap(Relaunch64View.class);
    public static final int DIRECTION_NEXT = 0;
    public static final int DIRECTION_PREV = 1;
    /**
     * 
     * @param tp
     * @param cbc
     * @param cbs
     * @param frame
     * @param set 
     */
    public EditorPanes(JTabbedPane tp, JComboBox cbc, JComboBox cbs, Relaunch64View frame, Settings set) {
        // reset editor list
        mainFrame = frame;
        settings = set;
        editorPaneArray.clear();
        tabbedPane = tp;
        jComboBoxCompiler = cbc;
        jComboBoxScripts = cbs;
    }
    /**
     * Adds a new editor pane to a new created tab of the tabbed pane. Usually only called from 
     * the method {@link #addNewTab(java.io.File, java.lang.String, java.lang.String, int)} that also
     * creates a new tab in the tabbed pane.
     * 
     * @param fp the file path to a file, usually used when a new file is opened via menu or drag&drop
     * @param content the content (e.g. the content of the loaded file) that should be set as default
     * text in the editor pane
     * @param title Tab title
     * @param compiler the default compiler for this editor pane, so the correct syntax highlighting is applied
     * @param script
     * @return the new total amount of existing tabs after this tab has been added.
     */
    public int addNewTab(File fp, String content, String title, int compiler, int script) {
        // create new editor pane
        final RL64TextArea editorPane = new RL64TextArea(settings);
        editorPane.setName("jEditorPaneMain");
        // enable drag&drop
        editorPane.setDragEnabled(true);
        DropTarget dropTarget = new DropTarget(editorPane, mainFrame);   
        // get default tab title and add new tab to tabbed pane
        tabbedPane.addTab(title, editorPane);
        // check for file path and set it as tool tip
        if (fp!=null && fp.exists()) {
            tabbedPane.setToolTipTextAt(tabbedPane.getTabCount()-1, fp.getPath());
        }
        // set compiler syntax style
        editorPane.setCompiler(compiler);
        editorPane.setCompilerSyntax();
        // add mode to buffer
        JEditBuffer buffer = editorPane.getBuffer();
        // set content, if available
        // TODO undo removes loaded text
        if (content!= null && !content.isEmpty()) {
            editorPane.setText(content);
        }
        else {
            editorPane.setText("");
        }
        // add caret listener
        /**
         * JDK 8 version of caret listener
         */
//        editorPane.addCaretListener((javax.swing.event.CaretEvent e) -> {
//            // retrieve selection
//            int selection = e.getMark()-e.getDot();
//            // here we have selected text
//            if (selection!=0) {
//                mainFrame.autoConvertNumbers(editorPane.getSelectedText());
//            }
//        });
        // add caret listener
        editorPane.addCaretListener(new javax.swing.event.CaretListener() {
            @Override public void caretUpdate(javax.swing.event.CaretEvent e) {
                // retrieve selection
                int selection = e.getMark()-e.getDot();
                // here we have selected text
                if (selection!=0) {
                    // convert numbers and show in textfields
                    mainFrame.autoConvertNumbers(getActiveEditorPane().getSelectedText());
                }
            }
        });
        // add buffer listener. the jEdit editor component has no document listener
        buffer.addBufferListener(new BufferListener() {
            @Override public void foldLevelChanged(JEditBuffer jeb, int i, int i1) {
            }
            @Override public void contentInserted(JEditBuffer jeb, int i, int i1, int i2, int i3) {
                setModified(true);
            }
            @Override public void contentRemoved(JEditBuffer jeb, int i, int i1, int i2, int i3) {
                setModified(true);
            }
            @Override public void preContentInserted(JEditBuffer jeb, int i, int i1, int i2, int i3) {
            }
            @Override public void preContentRemoved(JEditBuffer jeb, int i, int i1, int i2, int i3) {
            }
            @Override public void transactionComplete(JEditBuffer jeb) {
            }
            @Override public void foldHandlerChanged(JEditBuffer jeb) {
            }
            @Override public void bufferLoaded(JEditBuffer jeb) {
            }
        });
        // configure propeties of editor pane
        EditorPaneProperties editorPaneProperties = new EditorPaneProperties();
        // set editor pane
        editorPaneProperties.setEditorPane(editorPane);
        // set filepath
        editorPaneProperties.setFilePath(fp);
        // set script
        editorPaneProperties.setScript(script);
        // set modified false
        editorPaneProperties.setModified(false);
        // add editorpane to list
        editorPaneArray.add(editorPaneProperties);
        // select tab
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // set cursor
                setCursor(editorPane);
            }
        });
        // return current count
        return editorPaneArray.size();
    }
    /**
     * Get the current column of the caret in 
     * the editor pane (source code) {@code ep}.
     * 
     * @param ep The editor pane with the source code where the column number should be retrieved
     * @return The column number of the caret from the source code (editor pane) {@code ep}.
     */
    public int getColumn(RL64TextArea ep) {
        // retrieve caret position
        int caretPosition = ep.getCaretPosition();
        // substract line start offset
        return caretPosition-ep.getLineStartOffset(ep.getCaretLine());
    }
    /**
     * Get the current line number from caret position {@code caretPosition} in 
     * the editor pane (source code) {@code ep}. This is an alias function
     * which calls {@link #getRow(javax.swing.RL64TextArea, int)}.
     * 
     * @param ep The editor pane with the source code where the row (line) number should be retrieved
     * @param caretPosition The position of the caret, to determine in which row (line) the
     * caret is currently positioned.
     * @return The row (line) number of the caret from the source code in {@code ep}.
     */
    public int getLineNumber(RL64TextArea ep, int caretPosition) {
        return ep.getLineOfOffset(caretPosition);
    }
    public void gotoLine(int line, int column) {
        if (line > 0) {
            // get text area
            RL64TextArea ep = getActiveEditorPane();
            // set caret position
            ep.setCaretPosition(ep.getLineStartOffset(line-1)+column-1);
            // scroll and center caret position
            ep.scrollAndCenterCaret();
        }
    }
    /**
     * Sets the input focus to the editor pane.
     * 
     * @param editorPane 
     */
    private void setCursor(RL64TextArea editorPane) {
        // request input focus
        editorPane.requestFocusInWindow();
        try {
            editorPane.setCaretPosition(0);
            // scroll and center caret position
            editorPane.scrollToCaret(false);
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
        }
    }
    /**
     * The returns the comment char from the compiler syntax that is set
     * for the current source code.
     * 
     * @return The comment string of the compiler that is set for the currently
     * activated source code.
     */
    public String getCompilerCommentString() {
        return getCompilerCommentString(getActiveCompiler());
    }
    /**
     * The returns the comment char from the compiler syntax for
     * the compiler {@code compiler}.
     * 
     * @param compiler one of the compiler constans that indicate the 
     * compiler syntax.
     * @return The comment string of the compiler given in {@code compiler}.
     */
    public String getCompilerCommentString(int compiler) {
        return RL64TextArea.getCommentString(compiler);
    }
    /**
     * Inserts a (commented) section line into the source code. Sections are specific commented
     * line which may be used for source code navigation.
     * 
     * @param name the name of the section, used for navigating through the source.
     */
    public void insertSection(String name) {
        // retrieve section names
        ArrayList<String> names = SectionExtractor.getSectionNames(getActiveSourceCode(), getCompilerCommentString());
        // check whether we either have no sections or new name does not already exists
        if (null==names || names.isEmpty() || !names.contains(name)) {
            // get current editor
            RL64TextArea ep = getActiveEditorPane();
            // set up section name
            String insertString = getCompilerCommentString() + " ----- @" + name + "@ -----\n";
            // insert string
            insertString(insertString, ep.getLineStartOffset(ep.getCaretLine()));
        }
        else {
            ConstantsR64.r64logger.log(Level.WARNING, "Section name already exists. Could not insert section.");            
        }
    }
    public void insertFolds() {
        // get current editor
        RL64TextArea ep = getActiveEditorPane();
        // retrieve selected text
        String selection = ep.getSelectedText();
        if (selection!=null && !selection.isEmpty()) {
            // set up section name
            String insertString = getCompilerCommentString() + " {{{\n" + selection + getCompilerCommentString() + " }}}\n";
            // insert string
            ep.replaceSelection(insertString);
        }
        else {
            ConstantsR64.r64logger.log(Level.WARNING, "Section name already exists. Could not insert section.");            
        }
    }
    /**
     * Jumps to the line (scrolls the editor pane of the tab {@code selectedTab} to the related line 
     * and sets the caret to that line), which containts the section named {@code name}.
     * 
     * @param name the name of the section where to go.
     * @param selectedTab the tab, which contains the source where to go
     */
    public void gotoSection(String name, int selectedTab) {
        gotoLine(SectionExtractor.getSections(getSourceCode(selectedTab), getCompilerCommentString()), name);
    }
    /**
     * Jumps to the line (scrolls the editor pane of the tab {@code selectedTab} to the related line 
     * and sets the caret to that line), which containts the labek named {@code name}.
     * 
     * @param name the name of the label where to go.
     */
    public void gotoLabel(String name) {
        gotoLine(LabelExtractor.getLabels(getActiveSourceCode(), getActiveCompiler(), 0), name);
    }
    /**
     * Jumps to the line (scrolls the editor pane of the tab {@code selectedTab} to the related line 
     * and sets the caret to that line), which containts the label named {@code name}.
     * 
     * @param name the name of the label where to go.
     * @param selectedTab the tab, which contains the source where to go
     */
    public void gotoLabel(String name, int selectedTab) {
        gotoLine(LabelExtractor.getLabels(getSourceCode(selectedTab), getActiveCompiler(), 0), name);
    }
    /**
     * Jumps to the line (scrolls the editor pane of the tab {@code selectedTab} to the related line 
     * and sets the caret to that line), which containts the function named {@code name}.
     * 
     * @param name the name of the function where to go.
     */
    public void gotoFunction(String name) {
        gotoLine(FunctionExtractor.getFunctions(getActiveSourceCode(), getActiveCompiler()), name);
    }
    /**
     * Jumps to the line (scrolls the editor pane of the tab {@code selectedTab} to the related line 
     * and sets the caret to that line), which containts the function named {@code name}.
     * 
     * @param name the name of the function where to go.
     * @param selectedTab the tab, which contains the source where to go
     */
    public void gotoFunction(String name, int selectedTab) {
        gotoLine(FunctionExtractor.getFunctions(getSourceCode(selectedTab), getActiveCompiler()), name);
    }
    /**
     * Jumps to the line (scrolls the editor pane of the tab {@code selectedTab} to the related line 
     * and sets the caret to that line), which containts the macro named {@code name}.
     * 
     * @param name the name of the macro where to go.
     */
    public void gotoMacro(String name) {
        gotoLine(FunctionExtractor.getMacros(getActiveSourceCode(), getActiveCompiler()), name);
    }
    /**
     * Jumps to the line (scrolls the editor pane of the tab {@code selectedTab} to the related line 
     * and sets the caret to that line), which containts the macro named {@code name}.
     * 
     * @param name the name of the macro where to go.
     * @param selectedTab the tab, which contains the source where to go
     */
    public void gotoMacro(String name, int selectedTab) {
        gotoLine(FunctionExtractor.getMacros(getSourceCode(selectedTab), getActiveCompiler()), name);
    }
    /**
     * 
     * @return 
     */
    public boolean checkIfSyntaxChangeRequired() {
        // get selected compiler
        int selectedComp = jComboBoxCompiler.getSelectedIndex();
        // get selected tab
        int selectedTab = tabbedPane.getSelectedIndex();
        // check whether combo-box selection indicates a different compiler
        // from what was associated with the currently selected editor pane
        return (editorPaneArray.get(selectedTab).getEditorPane().getCompiler()!= selectedComp);
    }
    /**
     * 
     * @param compiler 
     * @param script
     */
    public void changeCompilerSyntax(int compiler, int script) {
        // get selected tab
        int selectedTab = tabbedPane.getSelectedIndex();
        if (selectedTab != -1) {
            // get editor pane
            EditorPaneProperties ep = editorPaneArray.get(selectedTab);
            // change syntax scheme for recent docs
            if(getFilePath(selectedTab)!=null) {
                int rd = settings.findRecentDoc(getFilePath(selectedTab).getPath());
                settings.setRecentDoc(rd, getFilePath(selectedTab).getPath(), compiler, script);
            }
            // get editor pane
            final RL64TextArea editorpane = ep.getEditorPane();
            // set new compiler scheme
            editorpane.setCompiler(compiler);
            // change compiler syntax
            editorpane.setCompilerSyntax();
            // update syntax scheme
            editorpane.setSyntaxScheme();
        }
    }
    public void updateColorScheme() {
        for (EditorPaneProperties ea : editorPaneArray) {
            // get editor pane
            final RL64TextArea editorpane = ea.getEditorPane();
            editorpane.setSyntaxScheme();
        }
    }
    public void updateAssemblyMode() {
        for (EditorPaneProperties ea : editorPaneArray) {
            // get editor pane
            final RL64TextArea editorpane = ea.getEditorPane();
            editorpane.setCompilerSyntax();
        }
    }
    public void setLineNumberAlignment(int alignment) {
        for (EditorPaneProperties ea : editorPaneArray) {
            // get editor pane
            RL64TextArea editorpane = ea.getEditorPane();
            editorpane.setLineNumberAlignment(alignment);
        }
    }
    public void setAntiAlias(String antialias) {
        for (EditorPaneProperties ea : editorPaneArray) {
            // get editor pane
            RL64TextArea editorpane = ea.getEditorPane();
            editorpane.setTextAntiAlias(antialias);
        }
    }
    public void setFonts(Font mf) {
        for (EditorPaneProperties ea : editorPaneArray) {
            // get editor pane
            final RL64TextArea editorpane = ea.getEditorPane();
            editorpane.setFonts(mf);
        }
    }
    public void setTabs(int tabSize) {
        for (EditorPaneProperties ea : editorPaneArray) {
            // get editor pane
            final RL64TextArea editorpane = ea.getEditorPane();
            editorpane.setTabs(tabSize);
        }
    }
    /**
     * 
     * @return 
     */
    public RL64TextArea getActiveEditorPane() {
        // get selected tab
        return getEditorPane(tabbedPane.getSelectedIndex());
    }
    public RL64TextArea getEditorPane(int index) {
        try {
            // get editor pane
            EditorPaneProperties ep = editorPaneArray.get(index);
            // get editor pane
            return ep.getEditorPane();
        }
        catch (IndexOutOfBoundsException ex) { 
            return null;
        }
    }
    /**
     * 
     * @return 
     */
    public String getActiveSourceCode() {
        return getSourceCode(getActiveEditorPane());
    }
    public String getSourceCode(int index) {
        RL64TextArea ep = getEditorPane(index);
        return getSourceCode(ep);
    }
    public String getSourceCode(RL64TextArea ep) {
        if (ep!=null) {
            return ep.getText();
        }
        return null;
    }
    public void setSourceCode(int index, String source) {
        RL64TextArea ep = getEditorPane(index);
        if (ep!=null) {
            ep.setText(source);
        }
    }
    public void setSourceCode(String source) {
        setSourceCode(tabbedPane.getSelectedIndex(), source);
    }
    /**
     * 
     * @return 
     */
    public int getActiveCompiler() {
        // get selected tab
        return getCompiler(tabbedPane.getSelectedIndex());
    }
    public int getCompiler(int index) {
        try {
            // get editor pane
            EditorPaneProperties ep = editorPaneArray.get(index);
            // get editor pane
            return ep.getEditorPane().getCompiler();
        }
        catch (IndexOutOfBoundsException ex) {
            return ConstantsR64.ASM_KICKASSEMBLER;
        }
    }
    public int getScript(int index) {
        try {
            // get editor pane
            EditorPaneProperties ep = editorPaneArray.get(index);
            // get editor pane
            return ep.getScript();
        }
        catch (IndexOutOfBoundsException ex) {
            return -1;
        }
    }
    /**
     * 
     * @return 
     */
    public File getActiveFilePath() {
        // get selected tab
        return getFilePath(tabbedPane.getSelectedIndex());
    }
    public File getFilePath(int index) {
        // get selected tab
        if (index != -1) {
            // get editor pane
            EditorPaneProperties ep = editorPaneArray.get(index);
            // get editor pane
            return ep.getFilePath();
        }
        return null;
    }
    /**
     * 
     * @return 
     */
    public EditorPaneProperties getActiveEditorPaneProperties() {
        // get selected tab
        return getEditorPaneProperties(tabbedPane.getSelectedIndex());
    }
    /**
     * 
     * @param selectedTab
     * @return 
     */
    public EditorPaneProperties getEditorPaneProperties(int selectedTab) {
        try {
            // get editor pane
            EditorPaneProperties ep = editorPaneArray.get(selectedTab);
            // get editor pane
            return ep;
        }
        catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }
    /**
     * 
     */
    public void setFocus() {
        // get active editor pane
       RL64TextArea ep = getActiveEditorPane();
       // check for valid value
       if (ep!=null) {
           // set input focus
           ep.requestFocusInWindow();
       }
    }
    /**
     * 
     * @param m 
     */
    private void setModified(boolean m) {
        // retrieve current tab
        int selectedTab = tabbedPane.getSelectedIndex();
        try {
            String title = tabbedPane.getTitleAt(selectedTab);
            if (m) {
                if (!title.startsWith("*")) {
                    title = "* "+title;
                    tabbedPane.setTitleAt(selectedTab, title);
                }
                editorPaneArray.get(selectedTab).setModified(m);
            }
            else {
                if (title.startsWith("* ")) {
                    title = title.substring(2);
                    tabbedPane.setTitleAt(selectedTab, title);
                }
                editorPaneArray.get(selectedTab).setModified(m);
            }
        }
        catch (IndexOutOfBoundsException ex) {
            ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
        }
    }
    /**
     * 
     * @param filepath 
     * @param compiler 
     * @param script 
     * @return  
     */
    public boolean loadFile(File filepath, int compiler, int script) {
        // check if file is already opened
        int opened = getOpenedFileTab(filepath);
        if (opened!=-1) {
            // if yes, select opened tab and do not open it twice
            tabbedPane.setSelectedIndex(opened);
            return false;
        }
        try {
            // check for valid value
            if (filepath!=null && filepath.exists()) {
                // read file
                byte[] buffer = new byte[(int) filepath.length()];
                try (InputStream in = new FileInputStream(filepath)) {
                    in.read(buffer);
                }
                catch (IOException ex) {
                    ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
                    return false;
                }
                finally {
                    String buf = new String(buffer);
                    boolean CRLF = buf.contains("\r\n");
                    if (CRLF) buf = buf.replaceAll("\r\n", "\n");
                    boolean CR = buf.contains("\r");
                    if (CR) buf = buf.replaceAll("\r", "\n");
                    boolean LF = buf.contains("\n");
                    // if yes, add new tab
                    int selectedTab = addNewTab(filepath, buf, getFileName(filepath), compiler, script)-1;
                    // set cursor
                    EditorPaneProperties epp = editorPaneArray.get(selectedTab);
                    setCursor(epp.getEditorPane());
                    epp.setLineEnd(LF ? (CRLF ? "\r\n" : (CR ? "\r" : "\n")) : System.getProperty("line.separator"));
                    return true;
                }
            }
        }
        catch (IndexOutOfBoundsException ex) {
            ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
            return false;
        }
        return false;
    }
    /**
     * Returns the index of the tab with the file {@code fp}.
     * @param fp The file path of a file which should be found in all
     * opened tabs.
     * @return The index of the tab with the file {@code fp}, or
     * -1 if {@code fp} is not opened in a tab yet.
     */
    public int getOpenedFileTab(File fp) {
        for (int i=0; i<tabbedPane.getTabCount(); i++) {
            File opened = editorPaneArray.get(i).getFilePath();
            if (opened!=null && opened.equals(fp)) return i;
        }
        return -1;
    }
    /**
     * 
     * @param filepath
     * @return 
     */
    private boolean saveFile(File filepath) {
        return saveFile(tabbedPane.getSelectedIndex(), filepath, false);
    }
    private boolean saveFile(int selectedTab, File filepath, boolean ignoreModified) {
        // check whether we have any tab selected
        if (selectedTab!=-1) {
            // check for modifications
            if (!ignoreModified && !editorPaneArray.get(selectedTab).isModified()) {
                return true;
            }
            // check for valid value
            if (filepath!=null && filepath.exists()) {
                // create filewriter
                Writer fw = null;
                try {
                    EditorPaneProperties epp = editorPaneArray.get(selectedTab);
                    // retrieve text
                    String content = epp.getEditorPane().getText();
                    String lineEnd = epp.getLineEnd();
                    if (!lineEnd.equals("\n")) content = content.replaceAll("\n", lineEnd);
                    fw = new FileWriter(filepath);
                    fw.write(content);
                }
                catch (IOException ex) {
                    ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
                    return false;
                }
                finally {
                    if (fw!=null) {
                        try {
                            fw.close();
                            setModified(false);
                            setTabTitle(selectedTab, filepath);
                            // set file path
                            editorPaneArray.get(selectedTab).setFilePath(filepath);
                        } catch (IOException ex) {
                            ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    /**
     * 
     * @return 
     */
    public boolean saveFile() {
        // retrieve current tab
        int selectedTab = tabbedPane.getSelectedIndex();
        // check whether we have any tab selected
        if (selectedTab!=-1) {
            // retrieve filename
            File fp = editorPaneArray.get(selectedTab).getFilePath();
            // check for valid value
            if (fp!=null && fp.exists()) {
                return saveFile(fp);
            }
            else {
                return saveFileAs();
            }
        }
        return false;
    }
    public boolean saveAllFiles() {
        // global error
        boolean allOk = true;
        for (int i=0; i<getCount(); i++) {
            // retrieve filename
            File fp = editorPaneArray.get(i).getFilePath();
            // check for valid value
            if (fp!=null && fp.exists()) {
                if (!saveFile(i,fp, false)) allOk = false;
            }
            else {
                if(!saveFileAs(i)) allOk = false;
            }
        }
        return allOk;
    }
    public boolean saveFileAs() {
        // retrieve current tab
        return saveFileAs(tabbedPane.getSelectedIndex());
    }
    /**
     * 
     * @param selectedTab
     * @return 
     */
    public boolean saveFileAs(int selectedTab) {
        // check whether we have any tab selected
        if (selectedTab!=-1) {
            // retrieve filename
            File fp = editorPaneArray.get(selectedTab).getFilePath();
            // init params
            String fpath = "";
            String fname = "";
            // check for valid value
            if (null!=fp) {
                fpath = fp.getPath();
                fname = fp.getName();
            }
            // choose file
            File fileToSave = FileTools.chooseFile(null, JFileChooser.SAVE_DIALOG, JFileChooser.FILES_ONLY, fpath, fname, "Save ASM File", ConstantsR64.FILE_EXTENSIONS, "ASM-Files");
            // check for valid value
            if (fileToSave!=null) {
                // check whether the user entered a file extension. if not,
                // add ".zkn3" as extension
                if (!FileTools.hasValidFileExtension(fileToSave)) {
                    fileToSave = new File(fileToSave.getPath()+".asm");
                }
                // check whether file exists
                if (!fileToSave.exists()) { 
                    try {
                        // if not, create file
                        if (fileToSave.createNewFile()) {
                            // save file
                            return saveFile(selectedTab, fileToSave, true);
                        }
                        else {
                            return false;
                        }
                    }
                    catch (IOException ex) {
                        ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
                    }
                }
                else {
                    // file exists, ask user to overwrite it...
                    int optionDocExists = JOptionPane.showConfirmDialog(null, resourceMap.getString("askForOverwriteFileMsg"), resourceMap.getString("askForOverwriteFileTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                    // if the user does *not* choose to overwrite, quit...
                    if (optionDocExists!=JOptionPane.YES_OPTION) {
                        return false;
                    }
                    else {
                        return saveFile(selectedTab, fileToSave, true);
                    }
                }
            }
        }
        return false;
    }
    /**
     * 
     * @param index
     * @param fp 
     */
    private void setTabTitle(int index, File fp) {
        // get filename
        String fn = getFileName(fp);
        // check whether we have any valid filepath at all
        if (fn!=null) {
            // set file-name and app-name in title-bar
            tabbedPane.setTitleAt(index, fn);
            tabbedPane.setToolTipTextAt(index, fp.getPath());
        }
        // if we don't have any title from the file name, simply set the applications title
        else {
            tabbedPane.setTitleAt(index, "untitled");
            tabbedPane.setToolTipTextAt(index, "");
        }
    }
    /**
     * 
     */
    public void updateTabbedPane() {
        // get selectect tab
        int selectedTab = tabbedPane.getSelectedIndex();
        // check for valid value
        if (selectedTab!=-1 && !editorPaneArray.isEmpty()) {
            try {
                // select compiler, so we update the highlight, if necessary
                jComboBoxCompiler.setSelectedIndex(editorPaneArray.get(selectedTab).getEditorPane().getCompiler());
                // select user script
                jComboBoxScripts.setSelectedIndex(editorPaneArray.get(selectedTab).getScript());
            }
            catch (IllegalArgumentException ex) {
            }
        }
    }
    /**
     * Gets the filename's name of a file-path (w/o extension).
     * 
     * @param f A filepath.
     * @return the filename's name of a file-path (w/o extension).
     */
    private String getFileName(File f) {
        // check whether we have any valid filepath at all
        if (f!=null && f.exists()) {
            String fname = f.getName();
            // find file-extension
            int extpos = fname.lastIndexOf(".");
            // set the filename as title
            if (extpos!=-1) {
                // return file-name
                return fname.substring(0,extpos);
            }
        }
        return null;
    }
    /**
     * The count of editor panes.
     * 
     * @return The count of editor panes.
     */
    public int getCount() {
        return (null==editorPaneArray) ? 0 : editorPaneArray.size();
    }
    /**
     * Checks whether the file / editor pane on the currently activated
     * JTabbedPane's selected tab is modified or not.
     * 
     * @return 
     */
    public boolean isModified() {
        return isModified(tabbedPane.getSelectedIndex());
    }
    /**
     * Checks whether the file / editor pane on the JTabbedPane with the
     * index {@code selectedTab} is modified or not.
     * 
     * @param selectedTab
     * @return {@code true} if editor pane's content on the JTabbedPane's tab
     * with index {@code selectedTab} is modified.
     */
    public boolean isModified(int selectedTab) {
        // retrieve active editor pane
        EditorPaneProperties ep = getEditorPaneProperties(selectedTab);
        // check for valid value
        if (ep!=null) {
            // check whether modified
            return (ep.isModified());
        }
        return false;
    }
    /**
     * Closes the current activated editor pane file. If editor content is
     * modified, an JOptionPane will popup and asks for saving changes.
     * After that, the currently activated editor pane will be removed
     * from the {@link #editorPaneArray}.
     * 
     * <b>Note that the currently selected tab from the {@link #tabbedPane}
     * has to be removed manually!</b>
     * 
     * @return {@code true} if editor pane (file) was successfully closed.
     */
    public boolean closeFile() {
        // retrieve active editor pane
        EditorPaneProperties ep = getActiveEditorPaneProperties();
        // check for valid value
        if (ep!=null) {
            // check whether modified
            if (ep.isModified()) {
                // if so, open a confirm dialog
                int option = JOptionPane.showConfirmDialog(null, resourceMap.getString("msgSaveChanges"), resourceMap.getString("msgSaveChangesTitle"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                // if action is cancelled, return to the program
                if (JOptionPane.CANCEL_OPTION == option || JOptionPane.CLOSED_OPTION==option /* User pressed cancel key */) {
                    return false;
                }
                // if save is requested, try to save
                if (JOptionPane.YES_OPTION == option && !saveFile()) {
                    return false;
                }
            }
            // get selected tab
            int selectedTab = tabbedPane.getSelectedIndex();
            try {
                // if save successful, remove data
                editorPaneArray.remove(selectedTab);
                // success
                return true;
            }
            catch (IndexOutOfBoundsException | UnsupportedOperationException ex) {
            }
        }
        return false;
    }
    /**
     * Inserts a (commented) separator line into the source. The current line of
     * the caret position is moved down and the separator line is inserted at
     * the beginning of the caret's current line.
     */
    public void insertSeparatorLine() {
        // get current editor
        RL64TextArea ep = getActiveEditorPane();
        // set up section name
        String insertString = getCompilerCommentString() + " ----------------------------------------\n";
        // insert string
        insertString(insertString, ep.getLineStartOffset(ep.getCaretLine()));
    }
    public void insertBreakPoint(int compiler) {
        // get current editor
        RL64TextArea ep = getActiveEditorPane();
        String insertString = "";
        switch (compiler) {
            case ConstantsR64.ASM_KICKASSEMBLER:
                insertString = ConstantsR64.STRING_BREAKPOINT_KICKASSEMBLER;
                break;
        }
        // insert string
        insertString(insertString, ep.getLineStartOffset(ep.getCaretLine()));
    }
    public void insertBreakPoint() {
        insertBreakPoint(getActiveCompiler());
    }
    /**
     * Generic goto-line-function. Goes to a line of a specific macro, function or label
     * given in {@code name}. The retrives function / macro / label names and line numbers
     * are passed as LinkedHasMap {@code map} and can be retrieved via
     * {@code getLabels()}, {@code getFunctions()} or {@code getMacros()}.
     * 
     * @param map A linked HashMap with label / macro / function names and linenumbers, retrieved via
     * {@code getLabels()}, {@code getFunctions()} or {@code getMacros()}.
     * @param name The name of the label / macro / function where to go
     */
    protected void gotoLine(LinkedHashMap<Integer, String> map, String name) {
        // names and linenumbers
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Integer> lines = new ArrayList<>();
        // check for valid value
        if (map!=null && !map.isEmpty() && name!=null && !name.isEmpty()) {
            // retrieve only string values of sections
            Collection<String> c = map.values();
            // create iterator
            Iterator<String> i = c.iterator();
            // add all ssction names to return value
            while(i.hasNext()) names.add(i.next());
            // retrieve only string values of sections
            Set<Integer> ks = map.keySet();
            // create iterator
            Iterator<Integer> ksi = ks.iterator();
            // add all label names to return value
            while(ksi.hasNext()) lines.add(ksi.next());
            // find section name
            int pos = names.indexOf(name);
            if (pos!=-1) {
                // retrieve associated line number
                gotoLine(lines.get(pos), 1);
            }
        }
    }
    public void insertString(String text) {
        insertString(text, getActiveEditorPane().getCaretPosition());
    }
    public void insertString(String text, int position) {
        JEditBuffer buffer = getActiveEditorPane().getBuffer();
        buffer.insert(position, text);
    }
    public void commentLine() {
        EditorPaneTools.commentLine(getActiveEditorPane(), getActiveCompiler());
    }
    public int getSelectedTab() {
        return tabbedPane.getSelectedIndex();
    }
    public void setSelectedTab(int tab) {
        try {
            tabbedPane.setSelectedIndex(tab);
        }
        catch (IndexOutOfBoundsException ex) {
        }
    }
    public void gotoNextLabel() {
        gotoLine(EditorPaneTools.findJumpToken(DIRECTION_NEXT, 
                getActiveEditorPane().getCaretLine()+1,
                LabelExtractor.getLabelLineNumbers(getActiveSourceCode(), getActiveCompiler(), 0)), 1);
    }
    public void gotoPrevLabel() {
        gotoLine(EditorPaneTools.findJumpToken(DIRECTION_PREV,
                getActiveEditorPane().getCaretLine()+1,
                LabelExtractor.getLabelLineNumbers(getActiveSourceCode(), getActiveCompiler(), 0)), 1);
    }
    public void gotoNextSection() {
        gotoLine(EditorPaneTools.findJumpToken(DIRECTION_NEXT,
                getActiveEditorPane().getCaretLine()+1,
                SectionExtractor.getSectionLineNumbers(getActiveSourceCode(), getCompilerCommentString())), 1);
    }
    public void gotoPrevSection() {
        gotoLine(EditorPaneTools.findJumpToken(DIRECTION_PREV,
                getActiveEditorPane().getCaretLine()+1,
                SectionExtractor.getSectionLineNumbers(getActiveSourceCode(), getCompilerCommentString())), 1);
    }
    public void undo() {
        RL64TextArea ep = getActiveEditorPane();
        JEditBuffer buffer = ep.getBuffer();
        if (buffer.canUndo()) {
            buffer.undo(ep);
        }
    }
    public void redo() {
        RL64TextArea ep = getActiveEditorPane();
        JEditBuffer buffer = ep.getBuffer();
        if (buffer.canRedo()) {
            buffer.redo(ep);
        }
    }
}
