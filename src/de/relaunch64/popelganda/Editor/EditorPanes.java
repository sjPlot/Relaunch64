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
package de.relaunch64.popelganda.Editor;

import de.relaunch64.popelganda.Relaunch64View;
import de.relaunch64.popelganda.database.Settings;
import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.FileTools;
import de.relaunch64.popelganda.util.Tools;
import java.awt.BorderLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

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
    private boolean eatReaturn = false;
    private JPopupMenu suggestionPopup = null;
    private JList suggestionList;
    private String suggestionSubWord;
    private static final String sugListContainerName="sugListContainerName";
    private final org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class)
                                                                                                   .getContext().getResourceMap(Relaunch64View.class);
    
    private static final int SUGGESTION_LABEL = 1;
    private static final int SUGGESTION_FUNCTION = 2;
    private static final int SUGGESTION_MACRO = 3;
    private static final int SUGGESTION_FUNCTION_MACRO = 4;
    private static final int SUGGESTION_FUNCTION_MACRO_SCRIPT = 5;
    
    public static final int DIRECTION_NEXT = 0;
    public static final int DIRECTION_PREV = 1;

    /**
     * @author Guillaume Polet
     * 
     * This example was taken from
     * http://stackoverflow.com/a/10883946/2094622
     * and modified for own purposes.
     */
    KeyListener SugestionKeyListener = new java.awt.event.KeyAdapter() {
        @Override public void keyTyped(java.awt.event.KeyEvent evt) {
            if (evt.getKeyCode()==java.awt.event.KeyEvent.VK_UP) {
                evt.consume();
                int index = Math.min(suggestionList.getSelectedIndex() - 1, 0);
                suggestionList.setSelectedIndex(index);
            }
            if (evt.getKeyCode()==java.awt.event.KeyEvent.VK_DOWN) {
                evt.consume();
                int index = Math.min(suggestionList.getSelectedIndex() + 1, suggestionList.getModel().getSize() - 1);
                suggestionList.setSelectedIndex(index);
            }
        }
        @Override public void keyReleased(java.awt.event.KeyEvent evt) {
            if (evt.getKeyCode()==java.awt.event.KeyEvent.VK_ENTER) {
                evt.consume();
                insertSelection();
            }
        }
    };
    
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
     * @param editorPane 
     * @param fp the file path to a file, usually used when a new file is opened via menu or drag&drop
     * @param content the content (e.g. the content of the loaded file) that should be set as default
     * text in the editor pane
     * @param c the default compiler for this editor pane, so the correct syntax highlighting is applied
     * @param script
     * @return the new total amount of existing tabs after this tab has been added.
     */
    public int addEditorPane(JEditorPane editorPane, File fp, String content, int c, int script) {
        // set syntax scheme
        editorPane = EditorPaneTools.setSyntaxScheme(editorPane, settings, c);
        // set backcolor
        // we need this hack for Nimbus LaF,
        // see http://stackoverflow.com/questions/22674575/jtextpane-background-color
        UIDefaults defaults = new UIDefaults();
        defaults.put("EditorPane[Enabled].backgroundPainter", SyntaxScheme.getBackgroundColor());
        editorPane.putClientProperty("Nimbus.Overrides", defaults);
        editorPane.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
        editorPane.setBackground(SyntaxScheme.getBackgroundColor());
        // set content, if available
        if (content!= null && !content.isEmpty()) {
            editorPane.setText(content);
        }
        else {
            editorPane.setText("");
        }
        // remove default traversal keys
        editorPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        editorPane.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);        
        editorPane.setFocusTraversalKeysEnabled(false);
        // add document listener ro recognize changes
        DocumentListener docListen = addDocumentListenerToEditorPane(editorPane);
        MyUndoManager undoman = addUndoManagerToEditorPane(editorPane);
        // add key listener
        editorPane.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent evt) {
                JEditorPane ep = (JEditorPane)evt.getSource();
                // cycle through open tabs
                if (KeyEvent.VK_TAB==evt.getKeyCode() && evt.isControlDown()) {
                    // get selected tab
                    int selected = tabbedPane.getSelectedIndex();
                    // cycle backwards?
                    if (evt.isShiftDown()) {
                        selected--;
                        if (selected<0) selected = tabbedPane.getTabCount()-1;
                    }
                    else {
                        // cycle forward
                        selected++;
                        if (selected>=tabbedPane.getTabCount()) selected = 0;
                    }
                    // select new tab
                    tabbedPane.setSelectedIndex(selected);
                    evt.consume();
                }
                // tab key w/o shift and control: indent selection or set tab
                else if (KeyEvent.VK_TAB==evt.getKeyCode() && !evt.isShiftDown()) {
                    // check for text selection
                    String selString = ep.getSelectedText();
                    // if we have selection, add tab to each selected line
                    if (selString!=null && !selString.isEmpty()) {
                        // remember selection range
                        int selstart = ep.getSelectionStart();
                        int selend = ep.getSelectionEnd();
                        // retrieve lines
                        String[] lines = selString.split("\n");
                        // check if selstring ends with return
                        boolean clearNewLine = !selString.endsWith("\n");
                        // create string builder for new insert string
                        StringBuilder sb = new StringBuilder("");
                        // add tab infront of each line
                        for (String l : lines) {
                            // check if we have specific line length
                            if (!l.isEmpty()) {
                                // check if first char is opcode token
                                int firstWhiteCharPos = -1;
                                if (Tools.startsWithOpcodeToken(l)) {
                                    // if yes, find offset of first whitespace
                                    // we insert tab at that position then
                                    for (int i=0; i<l.length(); i++) {
                                        if (Character.isWhitespace(l.charAt(i))) {
                                            firstWhiteCharPos = i;
                                            break;
                                        }
                                    }
                                }
                                // insert tab "in between", when we have labels or
                                // opcodes at line start
                                if (firstWhiteCharPos!=-1) {
                                    try {
                                        sb.append(l.substring(0, firstWhiteCharPos)).append("\t").append(l.substring(firstWhiteCharPos)).append("\n");
                                    }
                                    catch (IndexOutOfBoundsException ex) {
                                        sb.append("\t").append(l).append("\n");
                                    }
                                }
                                // insert tab at line start
                                else {
                                    sb.append("\t").append(l).append("\n");
                                }
                            }
                        }
                        // need to remove \n?
                        if (clearNewLine) sb = sb.deleteCharAt(sb.length()-1);
                        // copy replace text to string
                        String replacement = sb.toString();
                        // insert string
                        ep.replaceSelection(replacement);
                        // re-select text
                        ep.setSelectionStart(selstart);
                        ep.setSelectionEnd(selend+(replacement.length()-selString.length()));
                    }
                    else {
                        try {
                            // insert tab
                            ep.getDocument().insertString(ep.getCaretPosition(), "\t", null);
                        }
                        catch (BadLocationException ex) {
                        }
                    }
                    evt.consume();
                }
                // if user presses shift+tab, selection will be outdented
                else if (KeyEvent.VK_TAB==evt.getKeyCode() && evt.isShiftDown()) {
                    // check for text selection
                    String selString = ep.getSelectedText();
                    // if we have selection, add tab to each selected line
                    if (selString!=null && !selString.isEmpty()) {
                        // remember selection range
                        int selstart = ep.getSelectionStart();
                        int selend = ep.getSelectionEnd();
                        // retrieve lines
                        String[] lines = selString.split("\n");
                        // check if selstring ends with return
                        boolean clearNewLine = !selString.endsWith("\n");
                        // create string builder for new insert string
                        StringBuilder sb = new StringBuilder("");
                        // add tab infront of each line
                        for (String l : lines) {
                            // check if we have any line length
                            if (!l.isEmpty()) {
                                // is first char tab?
                                if (!l.startsWith("\t")) {
                                    // if not, find position of first tab
                                    int lastTab = l.lastIndexOf("\t");
                                    // do we have any tabs?
                                    if (lastTab!=-1) {
                                        try {
                                            // if yes, check if we have white char before tab
                                            if (Character.isWhitespace(l.charAt(lastTab-1))) {
                                                // if yes, we can remove first tab
                                                // without sticking together separated words
                                                l = l.replaceFirst("\t", "");
                                            }
                                        }
                                        catch (IndexOutOfBoundsException ex) {
                                            // remove first tab
                                            l = l.replaceFirst("\t", "");
                                        }
                                    }
                                }
                                else {
                                    // remove first tab
                                    l = l.replaceFirst("\t", "");
                                }
                                // append fixex line
                                sb.append(l).append("\n");
                            }
                        }
                        // need to remove \n?
                        if (clearNewLine) sb = sb.deleteCharAt(sb.length()-1);
                        // copy replace text to string
                        String replacement = sb.toString();
                        // insert string
                        ep.replaceSelection(replacement);
                        // re-select text
                        ep.setSelectionStart(selstart);
                        ep.setSelectionEnd(selend-(selString.length()-replacement.length()));
                    }
                    evt.consume();
                }
            }
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                // after enter-key, insert tabs automatically to match text start to column of previous line
                if (KeyEvent.VK_ENTER==evt.getKeyCode() && !evt.isShiftDown() && !eatReaturn) EditorPaneTools.autoInsertTab(getActiveEditorPane());
                // if enter-key should not auto-insert tab, do nothing
                else if (KeyEvent.VK_ENTER==evt.getKeyCode() && eatReaturn) eatReaturn = false;
                // ctrl+space opens label-auto-completion
                else if (evt.getKeyCode()==KeyEvent.VK_SPACE && evt.isControlDown() && !evt.isShiftDown() && !evt.isAltDown()) {
                    showSuggestion(SUGGESTION_LABEL);
                }
                else if (evt.getKeyCode()==KeyEvent.VK_SPACE && evt.isControlDown() && evt.isShiftDown()) {
                    showSuggestion(SUGGESTION_FUNCTION_MACRO_SCRIPT);
                }
                else if (Character.isWhitespace(evt.getKeyChar())) {
                    hideSuggestion();
                }
            }
        });
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
        // add focus listener
        editorPane.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent evt) {
                // we use this method to hide the auto-completion popup. this does
                // not automatically hide when the JList is wrapped in a JScrollPane
                if (suggestionPopup != null) {
                    suggestionPopup.setVisible(false);
                    suggestionPopup=null;
                }
            }
        });
        // configure propeties of editor pane
        EditorPaneProperties editorPaneProperties = new EditorPaneProperties();
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
        // set script
        editorPaneProperties.setScript(script);
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
    /**
     * Get the current row from caret position in acitvated editor pane (source code).
     * @return The row number of the caret from the current source code.
     */
    public int getCurrentRow() {
        // get current editor pane / source code
        JEditorPane ep = getActiveEditorPane();
        // and get caret
        int caretPosition = ep.getCaretPosition();
        // retrieve row from generic function
        return getRow(ep, caretPosition);
    }
    /**
     * Get the current row from caret position {@code caretPosition} in 
     * the editor pane (source code) {@code ep}. This is the generic
     * getRow-/getLineNumber function.
     * 
     * @param ep The editor pane with the source code where the row number should be retrieved
     * @param caretPosition The position of the caret, to determine in which row the
     * caret is currently positioned.
     * @return The row number of the caret from the source code in {@code ep}.
     */
    public int getRow(JEditorPane ep, int caretPosition) {
        return (ep!=null) ? ep.getDocument().getDefaultRootElement().getElementIndex(caretPosition) : 0;
    }
    /**
     * Get the current column of the caret in 
     * the editor pane (source code) {@code ep}.
     * 
     * @param ep The editor pane with the source code where the column number should be retrieved
     * @return The column number of the caret from the source code (editor pane) {@code ep}.
     */
    public int getColumn(JEditorPane ep) {
        // retrieve caret position
        int caretPosition = ep.getCaretPosition();
        // store original caret position
        int oriCaret = caretPosition;
        // get current line number
        int currentLine = ep.getDocument().getDefaultRootElement().getElementIndex(caretPosition);
        // decrease caret counter until we reach 0 or previous line
        while(currentLine==ep.getDocument().getDefaultRootElement().getElementIndex(caretPosition) && caretPosition>=0) caretPosition--;
        // column number is difference between original caret position and position of caret
        // in previous line
        return oriCaret-caretPosition;
    }
    /**
     * Get the current line number from caret position {@code caretPosition} in 
     * the editor pane (source code) {@code ep}. This is an alias function
     * which calls {@link #getRow(javax.swing.JEditorPane, int)}.
     * 
     * @param ep The editor pane with the source code where the row (line) number should be retrieved
     * @param caretPosition The position of the caret, to determine in which row (line) the
     * caret is currently positioned.
     * @return The row (line) number of the caret from the source code in {@code ep}.
     */
    public int getLineNumber(JEditorPane ep, int caretPosition) {
        return getRow(ep, caretPosition);
    }
    /**
     * Get the current row (line number) from caret position in 
     * acitvated editor pane (source code).
     * 
     * @return The row (line) number of the caret from the current source code.
     */
    public int getCurrentLineNumber() {
        return getCurrentRow()+1;
    }
    /**
     * 
     * @param caretpos 
     */
    public void gotoLineFromCaret(int caretpos) {
        JEditorPane ep = getActiveEditorPane();
        EditorPaneTools.gotoLine(ep, getRow(ep, caretpos));
    }
    /**
     * Scrolls the currently active source code to the line {@code line}.
     * 
     * @param line The line where the to scroll within the source code.
     * @return {@code true} if the goto was successful.
     */
    public boolean gotoLine(int line) {
        return EditorPaneTools.gotoLine(getActiveEditorPane(), line);
    }
    /**
     * Undoes the last edit action.
     */
    public void undo() {
        EditorPaneProperties ep = getActiveEditorPaneProperties();
        if (ep!=null) {
            // undo last edit
            ep.getUndoManager().undo();
            // set focus to editorpane
            setFocus();
        }
    }
    /**
     * Redoes the last {@link #undo()} action.
     */
    public void redo() {
        EditorPaneProperties ep = getActiveEditorPaneProperties();
        if (ep!=null) {
            // redo last edit
            ep.getUndoManager().redo();
            // set focus
            setFocus();
        }
    }
    /**
     * Sets the input focus the the editor pane
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
     * Adds a document listener to the JEditorPane {@code editorPane}.
     * 
     * @param editorPane The JEditorPane where the document listener should be added to.
     * @return An installed document listener that is saved in the {@link #editorPaneArray}
     * if needed to re-install etc.
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
    /**
     * Sets up an own undo manager. The default undo-manager would consider each 
     * syntax-highlighting step as own undo-event. To prevent this, the custom 
     * undo-manager only receives text-input/changes as undoable events.
     * 
     * @param editorPane The JEditorPane where the undo manager should be added to.
     * @return An installed undo manager that is saved in the {@link #editorPaneArray}
     * if needed to re-install etc.
     */
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
    private void removeUndoManagerFromEditorPane(EditorPaneProperties editorPaneProp) {
        // get editor pane
        JEditorPane editorPane = editorPaneProp.getEditorPane();
        // get doc listener
        MyUndoManager undomanager = editorPaneProp.getUndoManager();
        // check for valid values
        if (editorPane != null && undomanager != null) {
            editorPane.getDocument().removeUndoableEditListener(undomanager);
            editorPaneProp.setUndoManager(null);
        }
    }

    public String getCompilerCommentString() {
        return getCompilerCommentString(getActiveCompiler());
    }
    public String getCompilerCommentString(int compiler) {
        return SyntaxScheme.getCommentString(compiler);
    }
    public void insertSection(String name) {
        eatReaturn = true;
        // retrieve section names
        ArrayList<String> names = SectionExtractor.getSectionNames(getActiveSourceCode(), getCompilerCommentString());
        // check whether we either have no sections or new name does not already exists
        if (null==names || names.isEmpty() || !names.contains(name)) {
            // get current editor
            JEditorPane ep = getActiveEditorPane();
            // retrieve element and check whether line is inside bounds
            Element e = ep.getDocument().getDefaultRootElement().getElement(getCurrentLineNumber()-1);
            if (e!=null) {
                try {
                    // set up section name
                    String insertString = getCompilerCommentString() + " ----- @" + name + "@ -----" + System.getProperty("line.separator");
                    // insert section
                    ep.getDocument().insertString(e.getStartOffset(), insertString, SyntaxScheme.DEFAULT_COMMENT);
                }
                catch (BadLocationException ex) {}
            }
        }
        else {
            ConstantsR64.r64logger.log(Level.WARNING, "Section name already exists. Could not insert section.");            
        }
    }
    public void gotoSection(String name) {
        gotoLine(SectionExtractor.getSections(getActiveSourceCode(), getCompilerCommentString()), name);
    }
    public void gotoSection(String name, int index) {
        gotoLine(SectionExtractor.getSections(getSourceCode(index), getCompilerCommentString()), name);
    }
    public void gotoLabel(String name) {
        gotoLine(LabelExtractor.getLabels(getActiveSourceCode(), getActiveCompiler()), name);
    }
    public void gotoLabel(String name, int index) {
        gotoLine(LabelExtractor.getLabels(getSourceCode(index), getActiveCompiler()), name);
    }
    public void gotoFunction(String name) {
        gotoLine(FunctionExtractor.getFunctions(getActiveSourceCode(), getActiveCompiler()), name);
    }
    public void gotoFunction(String name, int index) {
        gotoLine(FunctionExtractor.getFunctions(getSourceCode(index), getActiveCompiler()), name);
    }
    public void gotoMacro(String name) {
        gotoLine(FunctionExtractor.getMacros(getActiveSourceCode(), getActiveCompiler()), name);
    }
    public void gotoMacro(String name, int index) {
        gotoLine(FunctionExtractor.getMacros(getSourceCode(index), getActiveCompiler()), name);
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
        return (editorPaneArray.get(selectedTab).getCompiler()!= selectedComp);
    }
    /**
     * 
     * @param compiler 
     * @param script
     */
    public void changeSyntaxScheme(int compiler, int script) {
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
            // disable undo/redo events
            // ep.getUndoManager().enableRegisterUndoEvents(false);
            // get editor pane
            JEditorPane editorpane = ep.getEditorPane();
            // remove listeners
            removeDocumentListenerFromEditorPane(ep);
            removeUndoManagerFromEditorPane(ep);
            // save content, may be deleted due to syntax highlighting
            String text = editorpane.getText();
            // change syntax scheme
            editorpane = EditorPaneTools.setSyntaxScheme(editorpane, settings, compiler);
            // set text back
            editorpane.setText(text);
            // add document listener again
            ep.setDocListener(addDocumentListenerToEditorPane(editorpane));
            // enable undo/redo events again
            ep.setUndoManager(addUndoManagerToEditorPane(editorpane));
            // ep.getUndoManager().enableRegisterUndoEvents(true);
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
     * @param script
     * @return 
     */
    public int addNewTab(File fp, String content, String title, int compiler, int script) {
        // create new scroll pane
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane();
        // and new editor pane
        JEditorPane editorPane = new JEditorPane();
        editorPane.setName("jEditorPaneMain");
        // init line numbers
        EditorPaneLineNumbers epln = new EditorPaneLineNumbers(editorPane, settings);
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
        return addEditorPane(editorPane, fp, content, compiler, script);
    }
    /**
     * 
     * @return 
     */
    public JEditorPane getActiveEditorPane() {
        // get selected tab
        return getEditorPane(tabbedPane.getSelectedIndex());
    }
    public JEditorPane getEditorPane(int index) {
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
        JEditorPane ep = getEditorPane(index);
        if (ep!=null) {
            return ep.getText();
        }
        return null;
    }
    public String getSourceCode(JEditorPane ep) {
        if (ep!=null) {
            return ep.getText();
        }
        return null;
    }
    public void setSourceCode(int index, String source) {
        JEditorPane ep = getEditorPane(index);
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
            return ep.getCompiler();
        }
        catch (IndexOutOfBoundsException ex) {
            return ConstantsR64.COMPILER_KICKASSEMBLER;
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
        // retrieve current tab
        int selectedTab = tabbedPane.getSelectedIndex();
        // check whether we have any tab selected
        if (selectedTab!=-1) {
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
                        boolean updateTabPane = closeInitialTab();
                        // if yes, add new tab
                        selectedTab = addNewTab(filepath, new String(buffer), getFileName(filepath), compiler, script)-1;
                        // check whether compiler combobox needs update
                        if (updateTabPane) updateTabbedPane();
                        // set cursor
                        setCursor(editorPaneArray.get(selectedTab).getEditorPane());
                        return true;
                    }
                }
            }
            catch (IndexOutOfBoundsException ex) {
                ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
                return false;
            }
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
    private boolean closeInitialTab() {
        // check if inital tab is empty and unused, and then remove it
        // initial tab has no file path
        File f = editorPaneArray.get(0).getFilePath();
        // title is "untitled"
        String t = tabbedPane.getTitleAt(0);
        // initial tab has no content
        String c = editorPaneArray.get(0).getEditorPane().getText();
        // and is not modified.
        boolean m = editorPaneArray.get(0).isModified();
        if (null==f && t.equalsIgnoreCase("untitled") && (null==c || c.isEmpty()) && !m) {
            // remove empty initial tab
            try {
                // if save successful, remove data
                editorPaneArray.remove(0);
                tabbedPane.remove(0);
                return true;
            }
            catch (IndexOutOfBoundsException | UnsupportedOperationException ex) {
            }
        }
        return false;
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
                            // if user confirms with enter key,
                            // we don't need auto inser tab...
                            eatReaturn = true;
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
                jComboBoxCompiler.setSelectedIndex(editorPaneArray.get(selectedTab).getCompiler());
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
        eatReaturn = true;
        // get current editor
        JEditorPane ep = getActiveEditorPane();
        // retrieve element and check whether line is inside bounds
        Element e = ep.getDocument().getDefaultRootElement().getElement(getCurrentLineNumber()-1);
        if (e!=null) {
            try {
                // set up section name
                String insertString = getCompilerCommentString() + " ----------------------------------------" + System.getProperty("line.separator");
                // insert section
                ep.getDocument().insertString(e.getStartOffset(), insertString, SyntaxScheme.DEFAULT_COMMENT);
            }
            catch (BadLocationException ex) {}
        }
    }
    public void insertBreakPoint(int compiler) {
        // get current editor
        JEditorPane ep = getActiveEditorPane();
        // retrieve element and check whether line is inside bounds
        Element e = ep.getDocument().getDefaultRootElement().getElement(getCurrentLineNumber()-1);
        if (e!=null) {
            try {
                String insertString = "";
                switch (compiler) {
                    case ConstantsR64.COMPILER_KICKASSEMBLER:
                        insertString = ConstantsR64.STRING_BREAKPOINT_KICKASSEMBLER + System.getProperty("line.separator");
                        break;
                }
                // insert section
                ep.getDocument().insertString(e.getStartOffset(), insertString, null);
            }
            catch (BadLocationException ex) {}
        }
    }
    public void insertBreakPoint() {
        insertBreakPoint(getActiveCompiler());
    }

    
    public void preventAutoInsertTab() {
        eatReaturn = true;
    }
    
    /**
     * @author Guillaume Polet
     * 
     * This example was taken from
     * http://stackoverflow.com/a/10883946/2094622
     * and modified for own purposes.
     * @param type
     */
    protected void showSuggestion(int type) {
        // hide old popup
        hideSuggestion();
        try {
            // retrieve chars that have already been typed
            String macroPrefix = "";
            if (type==SUGGESTION_FUNCTION_MACRO_SCRIPT) {
                switch (getActiveCompiler()) {
                    case ConstantsR64.COMPILER_KICKASSEMBLER:
                    case ConstantsR64.COMPILER_64TASS:
                    case ConstantsR64.COMPILER_CA65:
                        macroPrefix = ".";
                        break;
                    case ConstantsR64.COMPILER_ACME:
                        macroPrefix = "!";
                        break;
                    case ConstantsR64.COMPILER_DREAMASS:
                        macroPrefix = "#";
                        break;
                }
            }
            suggestionSubWord = getCaretString(false, macroPrefix);
            // check for valid value
            if (null==suggestionSubWord) return;
    //        if (suggestionSubWord.length() < 2) {
    //            return;
    //        }
            JEditorPane ep = getActiveEditorPane();
            // init variable
            Object[] labels = null;
            switch(type) {
                case SUGGESTION_FUNCTION:
                    // retrieve label list, remove last colon
                    labels = FunctionExtractor.getFunctionNames(suggestionSubWord.trim(), getActiveSourceCode(), getActiveCompiler());
                    break;
                case SUGGESTION_MACRO:
                    // retrieve label list, remove last colon
                    labels = FunctionExtractor.getMacroNames(suggestionSubWord.trim(), getActiveSourceCode(), getActiveCompiler());
                    break;
                case SUGGESTION_LABEL:
                    // retrieve label list, remove last colon
                    labels = LabelExtractor.getLabelNames(suggestionSubWord.trim(), true, getActiveSourceCode(), getActiveCompiler());
                    break;
                case SUGGESTION_FUNCTION_MACRO:
                    // retrieve label list, remove last colon
                    labels = FunctionExtractor.getFunctionAndMacroNames(suggestionSubWord.trim(), getActiveSourceCode(), getActiveCompiler());
                    break;
                case SUGGESTION_FUNCTION_MACRO_SCRIPT:
                    // retrieve label list, remove last colon
                    labels = FunctionExtractor.getFunctionMacroScripts(suggestionSubWord.trim(), getActiveSourceCode(), getActiveCompiler());
                    break;
            }
            // check if we have any labels
            if (labels!=null && labels.length>0) {
                Point location;
                try {
                    final int position = ep.getCaretPosition();
                    location = ep.modelToView(position).getLocation();
                } catch (BadLocationException e2) {
                    return;
                }
                // create suggestion pupup
                suggestionPopup = new JPopupMenu();
                suggestionPopup.removeAll();
                suggestionPopup.setOpaque(false);
                suggestionPopup.setBorder(null);
                // create JList with label items
                suggestionList = createSuggestionList(labels);
                // check minimum length of list and add scroll pane if list is too long
                if (labels.length>20) {
                    javax.swing.JScrollPane listScrollPane = new javax.swing.JScrollPane(suggestionList);
                    listScrollPane.setBorder(null);
                    listScrollPane.setPreferredSize(new java.awt.Dimension(150,300));
                    listScrollPane.setName(sugListContainerName);
                    suggestionPopup.add(listScrollPane, BorderLayout.CENTER);
                }
                else {
                    // else just add list w/o scroll pane
                    suggestionPopup.add(suggestionList, BorderLayout.CENTER);
                }
                suggestionPopup.show(ep, location.x, ep.getBaseline(0, 0) + location.y);
                // set input focus to popup
                /**
                 * JDK 8 Lambda
                 */
//                SwingUtilities.invokeLater(() -> {
//                    suggestionList.requestFocusInWindow();
//                });
                // set input focus to popup
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        suggestionList.requestFocusInWindow();
                    }
                });
            }
        }
        catch (IndexOutOfBoundsException ex) {
        }
    }
    protected void hideSuggestion() {
        if (suggestionPopup != null) {
            suggestionPopup.setVisible(false);
            suggestionPopup=null;
        }
        // set focus back to editorpane
        getActiveEditorPane().requestFocusInWindow();
    }
    protected JList createSuggestionList(final Object[] labels) {
        // create list
        JList sList = new JList(labels);
        sList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sList.setSelectedIndex(0);
        sList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    insertSelection();
                }
            }
        });
        sList.addKeyListener(SugestionKeyListener);
        return sList;
    }
    /**
     * Inserts the selected auto-completion label string at the current caret position.
     * 
     * @return {@code true} if auto-completion was successful.
     */
    protected boolean insertSelection() {
        if (suggestionList.getSelectedValue() != null) {
            try {
                JEditorPane ep = getActiveEditorPane();
                final String selectedSuggestion = ((String) suggestionList.getSelectedValue()).substring(suggestionSubWord.length());
                ep.getDocument().insertString(ep.getCaretPosition(), selectedSuggestion, null);
                ep.requestFocusInWindow();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (suggestionPopup!=null) suggestionPopup.setVisible(false);
                    }
                });
                /**
                 * JDK 8 Lambda
                 */
//                SwingUtilities.invokeLater(() -> {
//                    if (suggestionPopup!=null) suggestionPopup.setVisible(false);
//                });
                return true;
            }
            catch (BadLocationException e1) {
            }
        }
        return false;
    }
    /**
     * 
     * @param wholeWord
     * @param specialDelimiter
     * @return 
     */
    public String getCaretString(boolean wholeWord, String specialDelimiter) {
        JEditorPane ep = getActiveEditorPane();
        final int position = ep.getCaretPosition();
        String text;
        // retrieve text from caret to last whitespace
        try {
            if (wholeWord) {
                text = ep.getDocument().getText(0, ep.getDocument().getLength());
            }
            else {
                text = ep.getDocument().getText(0, position);
            }
        }
        catch(BadLocationException ex) {
            return null;
        }
        String addDelim;
        switch (getActiveCompiler()) {
            // use colon as additional delimiter for following assemblers
            case ConstantsR64.COMPILER_ACME:
            case ConstantsR64.COMPILER_64TASS:
            case ConstantsR64.COMPILER_DREAMASS:
                addDelim = "\n\r:";
                break;
            default:
                addDelim = "\n\r";
                break;
        }
        // get position start
        int start = Math.max(0, position - 1);
        boolean isSpecialDelimiter = false;
        // if we have specific delimiters, add these to the delimiter list.
        // e.g. KickAss script-directives need a "." as special delimiter
        if (specialDelimiter!=null && !specialDelimiter.isEmpty()) addDelim = addDelim + specialDelimiter;
        while (start > 0) {
            // check if we have a delimiter
            boolean isDelim = Tools.isDelimiter(text.substring(start, start+1), addDelim);
            // check if delimiter was special delimiter.
            if (isDelim && text.substring(start, start+1).equals(specialDelimiter)) isSpecialDelimiter = true;
            // check if we have any delimiter
            if (!Character.isWhitespace(text.charAt(start)) && !isDelim) {
                start--;
            } else {
                start++;
                break;
            }
        }
        if (start > position) {
            return null;
        }
        // check if we want the complete word at the caret
        if (wholeWord) {
            int end = start;
            while (end<text.length() && !Character.isWhitespace(text.charAt(end)) && !Tools.isDelimiter(text.substring(end, end+1), addDelim)) end++;
            // if we found a special delimiter at beginning, add it to return string
            if (isSpecialDelimiter) {
                return (specialDelimiter+text.substring(start, end));
            }
            else {
                return text.substring(start, end);
            }
        }
        // retrieve chars that have already been typed
        if (isSpecialDelimiter) {
            // if we found a special delimiter at beginning, add it to return string
            return (specialDelimiter+text.substring(start, position));
        }
        else {
            return text.substring(start, position);
        }
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
     * @return {@code true} if the goto-line was successful.
     */
    protected boolean gotoLine(LinkedHashMap<Integer, String> map, String name) {
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
                return gotoLine(lines.get(pos));
            }
        }
        return false;
    }
    public void insertString(String text) {
        insertString(text, getActiveEditorPane().getCaretPosition());
    }
    public void insertString(String text, int position) {
        JEditorPane ep = getActiveEditorPane();
        try {
            ep.getDocument().insertString(position, text, null);
            ep.requestFocusInWindow();
        }
        catch (BadLocationException ex) {
        }
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
        // goto next label
        gotoLabel(EditorPaneTools.findJumpToken(DIRECTION_NEXT,
                                                getCurrentLineNumber(),
                                                LabelExtractor.getLabelLineNumbers(getActiveSourceCode(), getActiveCompiler()),
                                                LabelExtractor.getLabelNames(false, false, getActiveSourceCode(), getActiveCompiler())));
    }
    public void gotoPrevLabel() {
        gotoLabel(EditorPaneTools.findJumpToken(DIRECTION_PREV,
                                                getCurrentLineNumber(),
                                                LabelExtractor.getLabelLineNumbers(getActiveSourceCode(), getActiveCompiler()),
                                                LabelExtractor.getLabelNames(false, false, getActiveSourceCode(), getActiveCompiler())));
    }
    public void gotoNextSection() {
        gotoSection(EditorPaneTools.findJumpToken(DIRECTION_NEXT,
                                                  getCurrentLineNumber(),
                                                  SectionExtractor.getSectionLineNumbers(getActiveSourceCode(), getCompilerCommentString()),
                                                  SectionExtractor.getSectionNames(getActiveSourceCode(), getCompilerCommentString())));
    }
    public void gotoPrevSection() {
        gotoSection(EditorPaneTools.findJumpToken(DIRECTION_PREV,
                                                  getCurrentLineNumber(),
                                                  SectionExtractor.getSectionLineNumbers(getActiveSourceCode(), getCompilerCommentString()),
                                                  SectionExtractor.getSectionNames(getActiveSourceCode(), getCompilerCommentString())));
    }
}
