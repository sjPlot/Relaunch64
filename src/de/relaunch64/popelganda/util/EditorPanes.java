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

import com.sun.glass.events.KeyEvent;
import de.relaunch64.popelganda.Relaunch64View;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.Utilities;

/**
 *
 * @author Daniel Luedecke
 */
public class EditorPanes {
    private final List<EditorPaneProperties> editorPaneArray = new ArrayList<>();
    private JTabbedPane tabbedPane = null;
    private JComboBox comboBox = null;
    private final Relaunch64View mainFrame;
    private final Settings settings;
    private boolean eatReaturn = false;
    private JPopupMenu suggestionPopup = null;
    private JList suggestionList;
    private String suggestionSubWord;
    private final org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class)
                                                                                                   .getContext().getResourceMap(Relaunch64View.class);
    
    /**
     * 
     * @param tp
     * @param cb
     * @param frame
     * @param set 
     */
    public EditorPanes(JTabbedPane tp, JComboBox cb, Relaunch64View frame, Settings set) {
        // reset editor list
        mainFrame = frame;
        settings = set;
        editorPaneArray.clear();
        tabbedPane = tp;
        comboBox = cb;
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
        // set backcolor
        editorPane.setBackground(SyntaxScheme.getBackgroundColor());
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
                if (KeyEvent.VK_ENTER==evt.getKeyCode() && !evt.isShiftDown() && !eatReaturn) autoInsertTab();
                else if (KeyEvent.VK_ENTER==evt.getKeyCode() && eatReaturn) eatReaturn = false;
                else if (evt.getKeyCode() == KeyEvent.VK_SPACE && evt.isControlDown()) {
                    showSuggestion();
                }
                else if (Character.isWhitespace(evt.getKeyChar())) {
                    hideSuggestion();
                }
            }
        });
        // add caret listener
        editorPane.addCaretListener((javax.swing.event.CaretEvent e) -> {
            // retrieve selection
            int selection = e.getMark()-e.getDot();
            // here we have selected text
            if (selection!=0) {
                mainFrame.autoConvertNumbers(editorPane.getSelectedText());
            }
        });
        // add focus listener
        editorPane.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent evt) {
                if (suggestionPopup != null) {
                    suggestionPopup.setVisible(false);
                    suggestionPopup=null;
                }
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
        JEditorPane ep = getActiveEditorPane();
        try {
            int caret = ep.getCaretPosition();
            // get start offset of current row
            int rowstart = Utilities.getRowStart(ep, caret);
            // get start offset of previous row
            int prevrow = Utilities.getRowStart(ep, rowstart-1);
            // if we have a valid value, go on
            if (prevrow>=0) {
                int offlen = rowstart-prevrow;
                // get line string
                String line = ep.getText(prevrow, offlen);
                StringBuilder tabs = new StringBuilder("");
                // iterate line string and read amount of leading spaces / tabs
                for (int i=0; i<offlen; i++) {
                    // get each char
                    char c = line.charAt(i);
                    if (' '==c || '\t'==c) {
                        tabs.append(c);
                    }
                    else {
                        break;
                    }
                }
                ep.getDocument().insertString(caret, tabs.toString(), null);
            }
        } catch (BadLocationException ex) {
        }
    }
    public int getCurrentRow() {
        JEditorPane ep = getActiveEditorPane();
        int caretPosition = ep.getCaretPosition();
        return getRow(ep, caretPosition);
    }
    public int getRow(JEditorPane ep, int caretPosition) {
        return (ep!=null) ? ep.getDocument().getDefaultRootElement().getElementIndex(caretPosition) : 0;
    }
    
    public int getLineNumber(JEditorPane ep, int caretPosition) {
        return getRow(ep, caretPosition);
    }
    public int getCurrentLineNumber() {
        return getCurrentRow();
    }
    public void gotoLineFromCaret(int caretpos) {
        JEditorPane ep = getActiveEditorPane();
        gotoLine(ep, getRow(ep, caretpos));
    }
    public void gotoLine(int line) {
        gotoLine(getActiveEditorPane(), line);
    }
    public void gotoLine(JEditorPane ep, int line) {
        if (line>0) {
            line--;
            if (ep!=null) {
                try {
                    // retrieve element and check whether line is inside bounds
                    Element e = ep.getDocument().getDefaultRootElement().getElement(line);
                    if (e!=null) {
                        // retrieve caret of requested line
                        int caret = e.getStartOffset();
                        // set new caret position
                        ep.setCaretPosition(caret);
                        // scroll some lines back, if possible
                        e = ep.getDocument().getDefaultRootElement().getElement(line-10);
                        if (e!=null) caret = e.getStartOffset();
                        // scroll rect to visible
                        ep.scrollRectToVisible(ep.modelToView(caret));
                        // scroll some lines further, if possible
                        e = ep.getDocument().getDefaultRootElement().getElement(line+10);
                        if (e!=null) caret = e.getStartOffset();
                        // scroll rect to visible
                        ep.scrollRectToVisible(ep.modelToView(caret));
                        // request focus
                        ep.requestFocusInWindow();
                    }
                }
                catch(BadLocationException | IllegalArgumentException ex) {
                }
            }
        }
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
    private JEditorPane setSyntaxScheme(JEditorPane editorPane, int c) {
        // declare compiler var as final
        final int compiler = c;
        // get hash map with keywords
        final HashMap<String, MutableAttributeSet> kw = SyntaxScheme.getKeywordHashMap(compiler);
        // get hash map with compiler specific keywords
        final HashMap<String, MutableAttributeSet> ckw = SyntaxScheme.getCompilerKeywordHashMap(compiler);
        // create new editor kit
        EditorKit editorKit = new StyledEditorKit() {
            // and set highlight scheme
            @Override
            public Document createDefaultDocument() {
                return new SyntaxHighlighting(kw, ckw,
                                              SyntaxScheme.getFontName(),
                                              SyntaxScheme.getFontSize(),
                                              SyntaxScheme.getCommentString(compiler),
                                              SyntaxScheme.getDelimiterList(compiler),
                                              SyntaxScheme.getStyleAttributes(),
                                              compiler);
            }
        };
        // link editorkit to editorpane
        editorPane.setEditorKitForContentType("text/plain", editorKit);
        editorPane.setContentType("text/plain");
        return editorPane;
    }

    public String getCompilerCommentString() {
        return getCompilerCommentString(getActiveCompiler());
    }
    public String getCompilerCommentString(int compiler) {
        return SyntaxScheme.getCommentString(compiler);
    }
    public LinkedHashMap getSections() {
        // prepare return values
        LinkedHashMap<Integer, String> sectionValues = new LinkedHashMap<>();
        // retrieve current source and compiler
        String s = getActiveSourceCode();
        String c = getCompilerCommentString();
        // init vars
        int lineNumber = 0;
        String line;
        // go if not null
        if (s!=null) {
            // create buffered reader, needed for line number reader
            BufferedReader br = new BufferedReader(new StringReader(s));
            LineNumberReader lineReader = new LineNumberReader(br);
            // Section-pattern is a comment line with "@<section description>@"
            Pattern p = Pattern.compile("@(.*?)@");
            Matcher m = p.matcher("");
            // read line by line
            try {
                while ((line = lineReader.readLine())!=null) {
                    // increase line counter
                    lineNumber++;
                    //reset the input (matcher)
                    m.reset(line); 
                    line = line.trim();
                    // check if line is a comment line and contains section pattern
                    if (line.startsWith(c) && m.find()) {
                        // if yes, add to return value
                        sectionValues.put(lineNumber, m.group(1));
                    }
                }
            }
            catch (IOException ex) {
            }
        }
        return sectionValues;
    }
    public ArrayList getSectionNames() {
        // init return value
        ArrayList<String> retval = new ArrayList<>();
        // retrieve sections
        LinkedHashMap<Integer, String> map = getSections();
        // check for valid value
        if (map!=null && !map.isEmpty()) {
            // retrieve only string values of sections
            Collection<String> c = map.values();
            // create iterator
            Iterator<String> i = c.iterator();
            // add all ssction names to return value
            while(i.hasNext()) retval.add(i.next());
            // return result
            return retval;
        }
        return null;
    }
    public ArrayList getSectionLineNumbers() {
        // init return value
        ArrayList<Integer> retval = new ArrayList<>();
        // retrieve sections
        LinkedHashMap<Integer, String> map = getSections();
        // check for valid value
        if (map!=null && !map.isEmpty()) {
            // retrieve only string values of sections
            Set<Integer> ks = map.keySet();
            // create iterator
            Iterator<Integer> i = ks.iterator();
            // add all ssction names to return value
            while(i.hasNext()) retval.add(i.next());
            // return result
            return retval;
        }
        return null;
    }
    public void insertSection(String name) {
        eatReaturn = true;
        // retrieve section names
        ArrayList<String> names = getSectionNames();
        // check whether we either have no sections or new name does not already exists
        if (null==names || names.isEmpty() || !names.contains(name)) {
            // get current editor
            JEditorPane ep = getActiveEditorPane();
            // retrieve element and check whether line is inside bounds
            Element e = ep.getDocument().getDefaultRootElement().getElement(getCurrentLineNumber());
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
        // names and linenumbers
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Integer> lines = new ArrayList<>();
        // retrieve sections
        LinkedHashMap<Integer, String> map = getSections();
        // check for valid value
        if (map!=null && !map.isEmpty()) {
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
            // add all ssction names to return value
            while(ksi.hasNext()) lines.add(ksi.next());
            // find section name
            int pos = names.indexOf(name);
            if (pos!=-1) {
                // retrieve associated line number
                gotoLine(lines.get(pos));
            }
        }
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
            // change syntax scheme for recent docs
            if(getActiveFilePath()!=null) {
                int rd = settings.findRecentDoc(getActiveFilePath().getPath());
                settings.setRecentDoc(rd, getActiveFilePath().getPath(), compiler);
            }
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
    public String getActiveSourceCode() {
        JEditorPane ep = getActiveEditorPane();
        if (ep!=null) {
            return ep.getText();
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
        return getEditorPaneProperties(tabbedPane.getSelectedIndex());
    }
    /**
     * 
     * @param selectedTab
     * @return 
     */
    public EditorPaneProperties getEditorPaneProperties(int selectedTab) {
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
     * @param compiler 
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
                    try (InputStream in = new FileInputStream(filepath)) {
                        in.read(buffer);
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
        return saveFile(tabbedPane.getSelectedIndex(), filepath);
    }
    private boolean saveFile(int selectedTab, File filepath) {
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
                if (!saveFile(i,fp)) allOk = false;
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
            File fileToSave = Tools.chooseFile(null, JFileChooser.SAVE_DIALOG, JFileChooser.FILES_ONLY, fpath, fname, "Save ASM File", ConstantsR64.FILE_EXTENSIONS, "ASM-Files");
            // check for valid value
            if (fileToSave!=null) {
                // check whether the user entered a file extension. if not,
                // add ".zkn3" as extension
                if (!Tools.hasValidFileExtension(fileToSave)) {
                    fileToSave = new File(fileToSave.getPath()+".asm");
                }
                // check whether file exists
                if (!fileToSave.exists()) { 
                    try {
                        // if not, create file
                        if (fileToSave.createNewFile()) {
                            // save file
                            return saveFile(selectedTab, fileToSave);
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
                        return saveFile(selectedTab, fileToSave);
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
            }
            catch (IndexOutOfBoundsException | UnsupportedOperationException ex) {
            }
            // success
            return true;
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
        Element e = ep.getDocument().getDefaultRootElement().getElement(getCurrentLineNumber());
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
    
    
    protected void showSuggestion() {
        // hide old popup
        hideSuggestion();
        JEditorPane ep = getActiveEditorPane();
        // get caret to retrieve position
        final int position = ep.getCaretPosition();
        Point location;
        try {
            location = ep.modelToView(position).getLocation();
        } catch (BadLocationException e2) {
            return;
        }
        String text;
        // retrieve text from caret to last whitespace
        try {
            text = ep.getDocument().getText(0, position);
        }
        catch(BadLocationException ex) {
            return;
        }
        String addDelim = (ConstantsR64.COMPILER_ACME==getActiveCompiler()) ? "\n\r:" : "\n\r";
        int start = Math.max(0, position - 1);
        while (start > 0) {
            char a = text.charAt(start);
            String b = text.substring(start, start+1);
            boolean c = Tools.isDelimiter(text.substring(start, start+1), addDelim);
            
            if (!Character.isWhitespace(text.charAt(start)) && !Tools.isDelimiter(text.substring(start, start+1), addDelim)) {
                start--;
            } else {
                start++;
                break;
            }
        }
        if (start > position) {
            return;
        }
        try {
            // retrieve chars that have already been typed
            suggestionSubWord = text.substring(start, position);
    //        if (suggestionSubWord.length() < 2) {
    //            return;
    //        }
            // retrieve label list
            Object[] labels = getLabelList(suggestionSubWord.trim());
            // check if we have any labels
            if (labels!=null && labels.length>0) {
                // create suggestion pupup
                suggestionPopup = new JPopupMenu();
                suggestionPopup.removeAll();
                suggestionPopup.setOpaque(false);
                suggestionPopup.setBorder(null);
                suggestionPopup.add(suggestionList = createSuggestionList(labels), BorderLayout.CENTER);
                suggestionPopup.show(ep, location.x, ep.getBaseline(0, 0) + location.y);
                // set input focus to popup
                SwingUtilities.invokeLater(() -> {
                    suggestionList.requestFocusInWindow();
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
        sList.addKeyListener(new java.awt.event.KeyAdapter() {
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
        });
        return sList;
    }

    protected boolean insertSelection() {
        if (suggestionList.getSelectedValue() != null) {
            try {
                JEditorPane ep = getActiveEditorPane();
                final String selectedSuggestion = ((String) suggestionList.getSelectedValue()).substring(suggestionSubWord.length());
                ep.getDocument().insertString(ep.getCaretPosition(), selectedSuggestion, null);
                ep.requestFocusInWindow();
                return true;
            } catch (BadLocationException e1) {
            }
        }
        return false;
    }

    public ArrayList getLabelsList() {
        // prepare return values
        ArrayList<String> sectionValues = new ArrayList<>();
        // retrieve current source and compiler
        String s = getActiveSourceCode();
        int c = getActiveCompiler();
        String line;
        // go if not null
        if (s!=null) {
            // create buffered reader, needed for line number reader
            BufferedReader br = new BufferedReader(new StringReader(s));
            LineNumberReader lineReader = new LineNumberReader(br);
            // read line by line
            try {
                while ((line = lineReader.readLine())!=null) {
                    String keyword = Tools.getLabelFromLine(line, c);
                    // check if we have valid label and if it's new. If yes, add to results
                    if (keyword!=null && !sectionValues.contains(keyword)) {
                        sectionValues.add(keyword);
                    }
                }
            }
            catch (IOException ex) {
            }
        }
        return sectionValues;
    }
    
    public Object[] getLabelList(String subWord) {
        // get labels here
        ArrayList<String> labels = getLabelsList();
        // check for valid values
        if (null==labels || labels.isEmpty()) return null;
//        labels.stream().forEach((label) -> {
//            System.out.println(label);
//        });
        // remove all labels that do not start with already typed chars
        if (!subWord.isEmpty()) {
            for (int i=labels.size()-1; i>=0; i--) {
                if (!labels.get(i).startsWith(subWord)) labels.remove(i);
            }
        }
        // sort list
        Collections.sort(labels);
        // return as object array
        return labels.toArray();
    }
}
