/*
 * Relaunch64 - A Java Crossassembler for C64 machine language coding.
 * Copyright (C) 2001-2013 by Daniel Lüdecke (http://www.danielluedecke.de)
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

import de.relaunch64.popelganda.Relaunch64View;
import java.awt.dnd.DropTarget;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyledEditorKit;

/**
 *
 * @author Daniel Luedecke
 */
public class EditorPanes {
    private List<EditorPaneProperties> editorPaneArray = new ArrayList<EditorPaneProperties>();
    private JTabbedPane tabbedPane = null;
    private JComboBox comboBox = null;
    private JTextField textField = null;
    private Relaunch64View mainFrame;
    private Settings settings;
    private org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class)
                                                                                                   .getContext().getResourceMap(Relaunch64View.class);
    
    /**
     * 
     * @param tp
     * @param cb
     * @param tf
     * @param frame
     * @param set 
     */
    public EditorPanes(JTabbedPane tp, JComboBox cb, JTextField tf, Relaunch64View frame, Settings set) {
        // reset editor list
        mainFrame = frame;
        settings = set;
        editorPaneArray.clear();
        tabbedPane = tp;
        comboBox = cb;
        textField = tf;
    }
    /**
     * Adds a new editor pane to a new created tab of the tabbed pane. Usually only called from 
     * the method {@link #addNewTab(java.io.File, java.lang.String, java.lang.String, int)} that also
     * creates a new tab in the tabbed pane.
     * 
     * @param editorPane 
     * @param fp the file path to a file, usually used when a new file is opened via menu or drag&drop
     * @param content the content (e.g. the content of the loaded file) that should be set as default
     * text in the editor pane
     * @param c the default compiler for this editor pane, so the correct syntax highlighting is applied
     * @return the new total amount of existing tabs after this tab has been added.
     */
    public int addEditorPane(JEditorPane editorPane, File fp, String content, int c) {
        // set syntax scheme
        setSyntaxScheme(editorPane, c);
        // set content, if available
        if (content!= null && !content.isEmpty()) {
            editorPane.setText(content);
        }
        else {
            editorPane.setText("");
        }
        // add document listener ro recognize changes
        DocumentListener docListen = addDocumentListenerToEditorPane(editorPane);
        MyUndoManager undoman = addUndoManagerToEditorPane(editorPane);
        // add key listener
        editorPane.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                autoInsertTab();
            }
        });
        // configure propeties of editor pane
        EditorPaneProperties editorPaneProperties = new EditorPaneProperties(settings);
        // set editor pane
        editorPaneProperties.setEditorPane(editorPane);
        // set document listener
        editorPaneProperties.setDocListener(docListen);
        // set undo manager
        editorPaneProperties.setUndoManager(undoman);
        // set filepath
        editorPaneProperties.setFilePath(fp);
        // set compiler
        editorPaneProperties.setCompiler(c);
        // set default parameter
        editorPaneProperties.setParam(editorPaneProperties.getDefaultParam(c));
        // set modified false
        editorPaneProperties.setModified(false);
        // add editorpane to list
        editorPaneArray.add(editorPaneProperties);
        // set cursor
        setCursor(editorPane);
        // select tab
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
        // return current count
        return editorPaneArray.size();
    }
    private void autoInsertTab() {
        // TODO anzahl tabs voriger zeile auslesen und setzn
    }
    public void undo() {
        EditorPaneProperties ep = getActiveEditorPaneProperties();
        if (ep!=null) {
            ep.getUndoManager().undo();
            setFocus();
        }
    }
    public void redo() {
        EditorPaneProperties ep = getActiveEditorPaneProperties();
        if (ep!=null) {
            ep.getUndoManager().redo();
            setFocus();
        }
    }
    /**
     * 
     * @param editorPane 
     */
    private void setCursor(JEditorPane editorPane) {
        // request input focus
        editorPane.requestFocusInWindow();
        try {
            editorPane.setCaretPosition(0);
        }
        catch (IllegalArgumentException ex) {
        }
    }
    /**
     * 
     * @param editorPane
     * @return 
     */
    private DocumentListener addDocumentListenerToEditorPane(JEditorPane editorPane) {
        DocumentListener docListen = new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { setModified(true); }
            @Override public void insertUpdate(DocumentEvent e) { setModified(true); }
            @Override public void removeUpdate(DocumentEvent e) { setModified(true); }
        };
        // add document listener ro recognize changes
        editorPane.getDocument().addDocumentListener(docListen);
        return docListen;
    }
    private MyUndoManager addUndoManagerToEditorPane(JEditorPane editorPane) {
        MyUndoManager undomanager = new MyUndoManager();
        editorPane.getDocument().addUndoableEditListener(undomanager);
        return undomanager;
    }
    /**
     * 
     * @param editorPaneProp 
     */
    private void removeDocumentListenerFromEditorPane(EditorPaneProperties editorPaneProp) {
        // get editor pane
        JEditorPane editorPane = editorPaneProp.getEditorPane();
        // get doc listener
        DocumentListener docListen = editorPaneProp.getDocListener();
        // check for valid values
        if (editorPane != null && docListen != null) {
            editorPane.getDocument().removeDocumentListener(docListen);
            editorPaneProp.setDocListener(null);
        }
    }
    /**
     * 
     * @param editorPane
     * @param c 
     */
    private void setSyntaxScheme(JEditorPane editorPane, int c) {
        // declare compiler var as final
        final int compiler = c;
        // get hash map with keywords
        final HashMap<String, MutableAttributeSet> kw = SyntaxScheme.getKeywordHashMap(compiler);
        // create new editor kit
        EditorKit editorKit = new StyledEditorKit() {
            // and set highlight scheme
            @Override
            public Document createDefaultDocument() {
                return new SyntaxHighlighting(kw, SyntaxScheme.DEFAULT_FONT_FAMILY,
                                                  SyntaxScheme.DEFAULT_FONT_SIZE,
                                                  SyntaxScheme.getCommentString(compiler),
                                                  SyntaxScheme.getDelimiterList(compiler),
                                                  SyntaxScheme.getStyleAttributes(),
                                                  compiler);
            }
        };
        // link editorkit to editorpane
        editorPane.setEditorKitForContentType("text/plain", editorKit);
        editorPane.setContentType("text/plain");
    }
    /**
     * 
     * @return 
     */
    public boolean checkIfSyntaxChangeRequired() {
        // get selected compiler
        int selectedComp = comboBox.getSelectedIndex();
        // get selected tab
        int selectedTab = tabbedPane.getSelectedIndex();
        // check whether combo-box selection indicates a different compiler
        // from what was associated with the currently selected editor pane
        return (editorPaneArray.get(selectedTab).getCompiler()!= selectedComp);
    }
    /**
     * 
     * @param compiler 
     */
    public void changeSyntaxScheme(int compiler) {
        // get selected tab
        int selectedTab = tabbedPane.getSelectedIndex();
        if (selectedTab != -1) {
            // get editor pane
            EditorPaneProperties ep = editorPaneArray.get(selectedTab);
            // disable undo/redo events
            ep.getUndoManager().enableRegisterUndoEvents(false);
            // get editor pane
            JEditorPane editorpane = ep.getEditorPane();
            // remove listener
            removeDocumentListenerFromEditorPane(ep);
            // save content, may be deleted due to syntax highlighting
            String text = editorpane.getText();
            // change syntax scheme
            setSyntaxScheme(editorpane, compiler);
            // set text back
            editorpane.setText(text);
            // add document listener again
            ep.setDocListener(addDocumentListenerToEditorPane(editorpane));
            // enable undo/redo events again
            ep.getUndoManager().enableRegisterUndoEvents(true);
            // set new compiler scheme
            ep.setCompiler(compiler);
            // set cursor
            setCursor(editorpane);
        }
    }
    /**
     * 
     * @param fp
     * @param content
     * @param title
     * @param compiler
     * @return 
     */
    public int addNewTab(File fp, String content, String title, int compiler) {
        // create new scroll pane
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane();
        // and new editor pane
        JEditorPane editorPane = new JEditorPane();
        editorPane.setName("jEditorPaneMain");
        // init line numbers
        EditorPaneLineNumbers epln = new EditorPaneLineNumbers(editorPane);
        scrollPane.setRowHeaderView(epln);
        // enable drag&drop
        editorPane.setDragEnabled(true);
        DropTarget dropTarget = new DropTarget(editorPane, mainFrame);   
        // set editorpane as viewport of scrollpane
        scrollPane.setViewportView(editorPane);
        // get default tab title and add new tab to tabbed pane
        tabbedPane.addTab(title, scrollPane);
        // check for file path and set it as tool tip
        if (fp!=null && fp.exists()) {
            tabbedPane.setToolTipTextAt(tabbedPane.getTabCount()-1, fp.getPath());
        }
        // enable syntax highlightinh
        return addEditorPane(editorPane, fp, content, compiler);
    }
    /**
     * 
     * @return 
     */
    public JEditorPane getActiveEditorPane() {
        // get selected tab
        int selectedTab = tabbedPane.getSelectedIndex();
        if (selectedTab != -1) {
            // get editor pane
            EditorPaneProperties ep = editorPaneArray.get(selectedTab);
            // get editor pane
            return ep.getEditorPane();
        }
        return null;
    }
    /**
     * 
     * @return 
     */
    public int getActiveCompiler() {
        // get selected tab
        int selectedTab = tabbedPane.getSelectedIndex();
        if (selectedTab != -1) {
            // get editor pane
            EditorPaneProperties ep = editorPaneArray.get(selectedTab);
            // get editor pane
            return ep.getCompiler();
        }
        return ConstantsR64.COMPILER_KICKASSEMBLER;
    }
    /**
     * 
     * @return 
     */
    public File getActiveFilePath() {
        // get selected tab
        int selectedTab = tabbedPane.getSelectedIndex();
        if (selectedTab != -1) {
            // get editor pane
            EditorPaneProperties ep = editorPaneArray.get(selectedTab);
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
        int selectedTab = tabbedPane.getSelectedIndex();
        if (selectedTab != -1) {
            // get editor pane
            EditorPaneProperties ep = editorPaneArray.get(selectedTab);
            // get editor pane
            return ep;
        }
        return null;
    }
    /**
     * 
     */
    public void setFocus() {
        // get active editor pane
       JEditorPane ep = getActiveEditorPane();
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
     */
    public void loadFile(File filepath, int compiler) {
        // retrieve current tab
        int selectedTab = tabbedPane.getSelectedIndex();
        // check whether we have any tab selected
        if (selectedTab!=-1) {
            try {
                // check for valid value
                if (filepath!=null && filepath.exists()) {
                    // read file
                    byte[] buffer = new byte[(int) filepath.length()];
                    try {
                        InputStream in = new FileInputStream(filepath);
                        in.read(buffer);
                        in.close();
                    }
                    catch (IOException ex) {
                        ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
                    }
                    finally {
                        // if yes, add new tab
                        selectedTab = addNewTab(filepath, new String(buffer), getFileName(filepath), compiler)-1;
                        // set cursor
                        setCursor(editorPaneArray.get(selectedTab).getEditorPane());
                    }
                }
            }
            catch (IndexOutOfBoundsException ex) {
                ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
            }
        }
    }
    /**
     * 
     * @param filepath
     * @return 
     */
    private boolean saveFile(File filepath) {
        // retrieve current tab
        int selectedTab = tabbedPane.getSelectedIndex();
        // check whether we have any tab selected
        if (selectedTab!=-1) {
            // check for modifications
            if (!editorPaneArray.get(selectedTab).isModified()) {
                return true;
            }
            // check for valid value
            if (filepath!=null && filepath.exists()) {
                // create filewriter
                Writer fw = null;
                try {
                    // retrieve text
                    String content = editorPaneArray.get(selectedTab).getEditorPane().getText();
                    fw = new FileWriter(filepath);
                    fw.write(content);
                }
                catch (IOException ex) {
                    ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
                    return false;
                }
                finally {
                    if (fw != null) {
                        try {
                            fw.close();
                            setModified(false);
                            setTabTitle(selectedTab, filepath);
                            return true;
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
    /**
     * 
     * @return 
     */
    public boolean saveFileAs() {
        // retrieve current tab
        int selectedTab = tabbedPane.getSelectedIndex();
        // check whether we have any tab selected
        if (selectedTab!=-1) {
            // retrieve filename
            File fp = editorPaneArray.get(selectedTab).getFilePath();
            // init params
            String fpath = "";
            String fname = "";
            // check for valid value
            if (null==fp) {
                fpath = fp.getPath();
                fname = fp.getName();
            }
            // choose file
            File fileToSave = Tools.chooseFile(null, JFileChooser.SAVE_DIALOG, JFileChooser.FILES_ONLY, fpath, fname, "Save ASM File", ConstantsR64.FILE_EXTENSIONS, "ASM-Files");
            // check for valid value
            if (fileToSave!=null) {
                // check whether the user entered a file extension. if not,
                // add ".zkn3" as extension
                if (!Tools.hasValidFileExtension(fileToSave)) {
                    fileToSave = new File(fileToSave.getPath()+"asm");
                }
                // check whether file exists
                if (!fileToSave.exists()) { 
                    try {
                        // if not, create file
                        if (fileToSave.createNewFile()) {
                            // save file
                            return saveFile(fileToSave);
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
                        return saveFile(fileToSave);
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
            // select compiler, so we update the highlight, if necessary
            comboBox.setSelectedIndex(editorPaneArray.get(selectedTab).getCompiler());
            // update the parameter
            String par = editorPaneArray.get(selectedTab).getParam();
            textField.setText((par!=null)?par:"");
        }
    }
    /**
     * 
     * @param f
     * @return 
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
     * 
     */
    public void updateParams() {
        // get selected tab
        int selectedTab = tabbedPane.getSelectedIndex();
        // check whether textbox indicates a different param
        // from what was associated with the currently selected editor pane
        String savedParam = editorPaneArray.get(selectedTab).getParam();
        String curParam = textField.getText();
        // check for valid values
        if (savedParam!=null && curParam!=null) { 
            // check whether we have changes
            if (!savedParam.equals(curParam)) {
                // store new value
                editorPaneArray.get(selectedTab).setParam(curParam);
            }
        }
    }
    /**
     * 
     * @return 
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
                // if no save is requested, exit immediately
                if (JOptionPane.NO_OPTION == option) {
                    return true;
                }
                // if action is cancelled, return to the program
                if (JOptionPane.CANCEL_OPTION == option || JOptionPane.CLOSED_OPTION==option /* User pressed cancel key */) {
                    return false;
                }
                // else, save file now
                if(!saveFile()) {
                    return false;
                }  
            }
            // get selected tab
            int selectedTab = tabbedPane.getSelectedIndex();
            // if save successful, remove data
            editorPaneArray.remove(selectedTab);
            // success
            return true;
        }
        return false;
    }
}
