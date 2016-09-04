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

import de.relaunch64.popelganda.Relaunch64View;
import de.relaunch64.popelganda.database.Settings;
import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.FileTools;
import de.relaunch64.popelganda.assemblers.Assembler;
import de.relaunch64.popelganda.assemblers.Assemblers;
import java.awt.dnd.DropTarget;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
    private JComboBox jComboBoxAssembler = null;
    private JComboBox jComboBoxScripts = null;
    private JLabel jLabelBufferSize = null;
    private final Relaunch64View mainFrame;
    private final Settings settings;
    private final org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class)
            .getContext().getResourceMap(Relaunch64View.class);
    public static final int DIRECTION_NEXT = 0;
    public static final int DIRECTION_PREV = 1;

    private class TabPanel extends JPanel { /* Draws a tab with close button */


        private final JLabel tabLabel;
        private final JButton closeButton;
        private final Settings settings;
        java.awt.Component component;

        public TabPanel(String title, Settings settings) {
            super(new FlowLayout(FlowLayout.LEFT, (ConstantsR64.IS_OSX && !settings.getNimbusOnOSX()) ? 2 : 0, (ConstantsR64.IS_OSX && !settings.getNimbusOnOSX()) ? 2 : 0));
            super.setOpaque(false);
            this.settings = settings;
            // set text label for tab
            this.tabLabel = new JLabel(title + (this.settings.getShowCloseButton() ? "  " : ""));
            this.closeButton = new JButton("");
            // add close button
            this.closeButton.setIcon(ConstantsR64.tabcloseicon);
            // and roll-over button
            this.closeButton.setRolloverEnabled(true);
            this.closeButton.setRolloverIcon(ConstantsR64.tabclosehovericon);
            this.closeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
            this.closeButton.setBorder(null);
            // show or hide button
            setCloseButtonVisible(this.settings.getShowCloseButton());
            // add to component
            super.add(this.tabLabel);
            super.add(this.closeButton);
            // add action listener to close tabs on click
            this.closeButton.addActionListener(new java.awt.event.ActionListener() { /* closes a tab */
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    java.awt.Component oldtab = tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex());
                    tabbedPane.setSelectedIndex(tabbedPane.indexOfTabComponent(component));
                    mainFrame.closeFile();
                    if (component != oldtab) {
                        tabbedPane.setSelectedIndex(tabbedPane.indexOfTabComponent(oldtab)); /* reselect old tab */

                    }
                }
            });
        }

        public final void setTitle(String title) {
            // put space between title and close-button
            this.tabLabel.setText(title + (this.settings.getShowCloseButton() ? "  " : ""));
        }

        public final String getTitle() {
            // put space between title and close-button
            return this.tabLabel.getText().trim();
        }

        public void setTabComponent(java.awt.Component component) {
            this.component = component;
        }

        public final void setCloseButtonVisible(boolean visible) {
            this.closeButton.setVisible(visible);
        }
    };

    /**
     *
     * @param tp
     * @param cbc
     * @param cbs
     * @param lbs
     * @param frame
     * @param set
     */
    public EditorPanes(JTabbedPane tp, JComboBox cbc, JComboBox cbs, JLabel lbs, Relaunch64View frame, Settings set) {
        // reset editor list
        mainFrame = frame;
        settings = set;
        editorPaneArray.clear();
        tabbedPane = tp;
        jComboBoxAssembler = cbc;
        jComboBoxScripts = cbs;
        jLabelBufferSize = lbs;
    }

    /**
     * Adds a new editor pane to a new created tab of the tabbed pane.
     *
     * @param fp the file path to a file, usually used when a new file is opened via menu or
     * drag&drop
     * @param content the content (e.g. the content of the loaded file) that should be set as
     * default text in the editor pane
     * @param title Tab title
     * @param assembler the default assembler for this editor pane, so the correct syntax
     * highlighting is applied
     * @param script the current selected user script that will be associated with this sourcecode
     * on init
     * @param altScript the alternative selected user script that will be associated with this sourcecode
     * on init
     * @return the new total amount of existing tabs after this tab has been added.
     */
    public int addNewTab(File fp, String content, String title, Assembler assembler, int script, int altScript) {
        // create new editor pane
        final RL64TextArea editorPane = new RL64TextArea(settings);
        editorPane.setName("jEditorPaneMain");
        // enable drag&drop
        editorPane.setDragEnabled(true);
        DropTarget dropTarget = new DropTarget(editorPane, mainFrame);
        // check whether extenstion should be shown as well
        if (settings.getShowExtensionInTab() && fp != null) {
            title = title + "." + FileTools.getFileExtension(fp);
        }
        // get default tab title and add new tab to tabbed pane
        tabbedPane.addTab(title, editorPane);
        // check for file path and set it as tool tip
        if (fp != null && fp.exists()) {
            tabbedPane.setToolTipTextAt(tabbedPane.getTabCount() - 1, fp.getPath());
        }
        TabPanel tabPanel = new TabPanel(title, settings);
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, tabPanel);
        tabPanel.setTabComponent(tabbedPane.getTabComponentAt(tabbedPane.getTabCount() - 1));
        // set assembler syntax style
        editorPane.setAssembler(assembler);
        editorPane.setAssemblyMode();
        // add mode to buffer
        JEditBuffer buffer = editorPane.getBuffer();
        // limit undo, so initial text setting will not be undoable
        buffer.setUndoLimit(0); // haha, cheater! ;-)
        // set content, if available
        if (content != null && !content.isEmpty()) {
            editorPane.setText(content);
        } else {
            editorPane.setText("");
        }
        buffer.setUndoLimit(100);
        // add caret listener
        editorPane.addCaretListener(new javax.swing.event.CaretListener() {
            @Override
            public void caretUpdate(javax.swing.event.CaretEvent e) {
                // retrieve selection
                int selection = e.getMark() - e.getDot();
                // here we have selected text
                if (selection != 0) {
                    // convert numbers and show in textfields
                    mainFrame.autoConvertNumbers(getActiveEditorPane().getSelectedText());
                }
            }
        });
        // add buffer listener. the jEdit editor component has no document listener
        buffer.addBufferListener(new BufferListener() {
            @Override
            public void foldLevelChanged(JEditBuffer jeb, int i, int i1) {
            }

            @Override
            public void contentInserted(JEditBuffer jeb, int i, int i1, int i2, int i3) {
                setModified(true);
                updateLabelBufferSize();
            }

            @Override
            public void contentRemoved(JEditBuffer jeb, int i, int i1, int i2, int i3) {
                setModified(true);
                updateLabelBufferSize();
            }

            @Override
            public void preContentInserted(JEditBuffer jeb, int i, int i1, int i2, int i3) {
            }

            @Override
            public void preContentRemoved(JEditBuffer jeb, int i, int i1, int i2, int i3) {
            }

            @Override
            public void transactionComplete(JEditBuffer jeb) {
            }

            @Override
            public void foldHandlerChanged(JEditBuffer jeb) {
            }

            @Override
            public void bufferLoaded(JEditBuffer jeb) {
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
        // set alternative script
        editorPaneProperties.setAltScript(altScript);
        // set modified false
        editorPaneProperties.setModified(false);
        // add editorpane to list
        editorPaneArray.add(editorPaneProperties);
        // select tab
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
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

    public void updateLabelBufferSize() {
        if (!settings.getShowBufferSize()) {
            jLabelBufferSize.setText("");
            return;
        }
        int s = getActiveEditorPane().getBufferLength();
        int l = getActiveEditorPane().getBuffer().getLineCount();
        jLabelBufferSize.setText((s > 0) ? String.format(Locale.ENGLISH, "(%d lines, %.2fKB)", l, (float) s / 1024) : "");
    }

    /**
     * Get the current column of the caret in the editor pane (source code) {@code ep}.
     *
     * @param ep The editor pane with the source code where the column number should be retrieved
     * @return The column number of the caret from the source code (editor pane) {@code ep}.
     */
    public int getColumn(RL64TextArea ep) {
        // retrieve caret position
        int caretPosition = ep.getCaretPosition();
        // substract line start offset
        return caretPosition - ep.getLineStartOffset(ep.getCaretLine());
    }

    /**
     * Get the current line number from caret position {@code caretPosition} in the editor pane
     * (source code) {@code ep}. This is an alias function which calls
     * {@link #getRow(javax.swing.RL64TextArea, int)}.
     *
     * @param ep The editor pane with the source code where the row (line) number should be
     * retrieved
     * @param caretPosition The position of the caret, to determine in which row (line) the caret is
     * currently positioned.
     * @return The row (line) number of the caret from the source code in {@code ep}.
     */
    public int getLineNumber(RL64TextArea ep, int caretPosition) {
        return ep.getLineOfOffset(caretPosition);
    }

    /**
     * Generic goto line function. Scrolls the active editor pane to the line {@code line} and sets
     * the caret to the column {@code column}.
     *
     * @param line
     * @param column
     */
    public void gotoLine(int line, int column) {
        if (line > 0) {
            // get text area
            RL64TextArea ep = getActiveEditorPane();
            // set caret position
            ep.setCaretPosition(ep.getLineStartOffset(line - 1) + column - 1);
            // scroll and center caret position
            ep.scrollAndCenterCaret();
            // expand folds, if any
            ep.expandFold(true);
        }
    }

    public void expandAllFolds() {
        toggleFold(true);
    }

    public void collapseAllFolds() {
        toggleFold(false);
    }

    protected void toggleFold(boolean expand) {
        // get text area
        RL64TextArea ep = getActiveEditorPane();
        // set caret position to start of buffer
        ep.goToBufferStart(false);
        // expand or collapse first-line-fold
        if (expand) {
            ep.expandFold(true);
        } else {
            ep.collapseFold();
        }
        // remember caret offset
        int oldcaret = ep.getCaretPosition();
        int newcaret = -1;
        // check if we cycled through all folds
        while (oldcaret != newcaret) {
            // remember caret offset
            oldcaret = ep.getCaretPosition();
            // goto next fold
            ep.goToNextFold(false);
            newcaret = ep.getCaretPosition();
            // expand
            if (expand) {
                ep.expandFold(true);
            } else {
                ep.collapseFold();
            }
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
        } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
        }
    }

    /**
     * Sets the input focus to the editor pane and positions the cursor
     * at {@code pos}.
     *
     * @param editorPane
     * @param pos the new cursor position
     */
    private void setCursor(RL64TextArea editorPane, int pos) {
        // request input focus
        editorPane.requestFocusInWindow();
        try {
            editorPane.setCaretPosition(pos);
            // scroll and center caret position
            editorPane.scrollToCaret(false);
        } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
        }
    }

    /**
     * Inserts a (commented) section line into the source code. Sections are specific commented line
     * which may be used for source code navigation.
     *
     * @param name the name of the section, used for navigating through the source.
     */
    public void insertSection(String name) {
        // retrieve section names
        ArrayList<String> names = SectionExtractor.getSectionNames(getActiveSourceCode(), getActiveAssembler().getLineComment());
        // check whether we either have no sections or new name does not already exists
        if (null == names || names.isEmpty() || !names.contains(name)) {
            // get current editor
            RL64TextArea ep = getActiveEditorPane();
            // set up section name
            String insertString = getActiveAssembler().getLineComment() + " ----- @" + name + "@ -----\n";
            // insert string
            insertString(insertString, ep.getLineStartOffset(ep.getCaretLine()));
        } else {
            ConstantsR64.r64logger.log(Level.WARNING, "Section name already exists. Could not insert section.");
        }
    }

    /**
     * Inserts fold tags into the source. Selected text will be surrounded by fold tags.
     */
    public void insertFolds() {
        // get current editor
        RL64TextArea ep = getActiveEditorPane();
        // retrieve selected text
        String selection = ep.getSelectedText();
        if (selection != null && !selection.isEmpty()) {
            // set up section name
            String insertString = getActiveAssembler().getLineComment() + " {{{\n" + selection + getActiveAssembler().getLineComment() + " }}}\n";
            // insert string
            ep.replaceSelection(insertString);
        } else {
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
        gotoLine(SectionExtractor.getSections(getSourceCode(selectedTab), getActiveAssembler().getLineComment()), name);
    }

    /**
     * Jumps to the line (scrolls the editor pane of the tab {@code selectedTab} to the related line
     * and sets the caret to that line), which containts the labek named {@code name}.
     *
     * @param name the name of the label where to go.
     */
    public void gotoLabel(String name) {
        gotoLine(LabelExtractor.getLabels(getActiveSourceCode(), getActiveAssembler(), 0).labels, name);
    }

    /**
     * Jumps to the line (scrolls the editor pane of the tab {@code selectedTab} to the related line
     * and sets the caret to that line), which containts the label named {@code name}.
     *
     * @param name the name of the label where to go.
     * @param selectedTab the tab, which contains the source where to go
     */
    public void gotoLabel(String name, int selectedTab) {
        gotoLine(LabelExtractor.getLabels(getSourceCode(selectedTab), getActiveAssembler(), 0).labels, name);
    }

    /**
     * Jumps to the line (scrolls the editor pane of the tab {@code selectedTab} to the related line
     * and sets the caret to that line), which containts the function named {@code name}.
     *
     * @param name the name of the function where to go.
     */
    public void gotoFunction(String name) {
        gotoLine(LabelExtractor.getLabels(getActiveSourceCode(), getActiveAssembler(), 0).functions, name);
    }

    /**
     * Jumps to the line (scrolls the editor pane of the tab {@code selectedTab} to the related line
     * and sets the caret to that line), which containts the function named {@code name}.
     *
     * @param name the name of the function where to go.
     * @param selectedTab the tab, which contains the source where to go
     */
    public void gotoFunction(String name, int selectedTab) {
        gotoLine(LabelExtractor.getLabels(getSourceCode(selectedTab), getActiveAssembler(), 0).functions, name);
    }

    /**
     * Jumps to the line (scrolls the editor pane of the tab {@code selectedTab} to the related line
     * and sets the caret to that line), which containts the macro named {@code name}.
     *
     * @param name the name of the macro where to go.
     */
    public void gotoMacro(String name) {
        gotoLine(LabelExtractor.getLabels(getActiveSourceCode(), getActiveAssembler(), 0).macros, name);
    }

    /**
     * Jumps to the line (scrolls the editor pane of the tab {@code selectedTab} to the related line
     * and sets the caret to that line), which containts the macro named {@code name}.
     *
     * @param name the name of the macro where to go.
     * @param selectedTab the tab, which contains the source where to go
     */
    public void gotoMacro(String name, int selectedTab) {
        gotoLine(LabelExtractor.getLabels(getSourceCode(selectedTab), getActiveAssembler(), 0).macros, name);
    }

    /**
     * Checks if the selected assembler (from the combo box) differs from the editor pane's
     * associated assmbler. If yes, consider calling
     * {@link #changeAssembler(de.relaunch64.popelganda.assemblers.Assembler, int)} to change the
     * highlighting etc.
     *
     * @return {@code true} if user-selected assembler and editor pane's assembler differ.
     */
    public boolean checkIfSyntaxChangeRequired() {
        // get selected assembler
        int selectedComp = jComboBoxAssembler.getSelectedIndex();
        // get selected tab
        int selectedTab = tabbedPane.getSelectedIndex();
        // check whether combo-box selection indicates a different assembler
        // from what was associated with the currently selected editor pane
        return (editorPaneArray.get(selectedTab).getEditorPane().getAssembler().getID() != selectedComp);
    }

    /**
     * Changes the assembler that is associated with the current activated editor pane. Furtherm
     *
     * @param assembler
     * @param script
     * @param altScript
     */
    public void changeAssembler(Assembler assembler, int script, int altScript) {
        // get selected tab
        int selectedTab = tabbedPane.getSelectedIndex();
        if (selectedTab != -1) {
            // get editor pane
            EditorPaneProperties ep = editorPaneArray.get(selectedTab);
            // change syntax scheme for recent docs
            if (getFilePath(selectedTab) != null) {
                int rd = settings.findRecentDoc(getFilePath(selectedTab).getPath());
                settings.setRecentDoc(rd, getFilePath(selectedTab).getPath(), assembler, script, altScript);
            }
            // get editor pane
            final RL64TextArea editorpane = ep.getEditorPane();
            // set new assembler scheme
            editorpane.setAssembler(assembler);
            // change assembler syntax
            editorpane.setAssemblyMode();
            // update syntax scheme
            editorpane.setSyntaxScheme();
        }
    }

    /**
     * Updates color schemes for all opened editor panes. Call this method when user selects a new
     * syntax highlight color scheme.
     */
    public void updateColorScheme() {
        for (EditorPaneProperties ea : editorPaneArray) {
            // get editor pane
            final RL64TextArea editorpane = ea.getEditorPane();
            editorpane.setSyntaxScheme();
        }
    }

    /**
     * Updates code folding options for all opened editor panes.
     */
    public void updateCodeFolding() {
        for (EditorPaneProperties ea : editorPaneArray) {
            // get editor pane
            final RL64TextArea editorpane = ea.getEditorPane();
            editorpane.setCodeFolding();
        }
    }

    /**
     * Changes visibility of close buttons and adjusts titles of all tabs.
     */
    public void updateTabCloseButtons() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            // change close button visibility
            ((TabPanel) tabbedPane.getTabComponentAt(i)).setCloseButtonVisible(settings.getShowCloseButton());
            // adjust title
            String title = ((TabPanel) tabbedPane.getTabComponentAt(i)).getTitle();
            // whether spaces between label and title are needed depends on button visibility
            ((TabPanel) tabbedPane.getTabComponentAt(i)).setTitle(title);
        }
    }

    public void updateTitles() {
        for (int i = 0; i < editorPaneArray.size(); i++) {
            setTabTitle(i, editorPaneArray.get(i).getFilePath());
        }
    }

    /**
     * Updates assembly mode for all opened editor panes. Call this method when user changes the
     * assembly mode.
     */
    public void updateAssemblyMode() {
        for (EditorPaneProperties ea : editorPaneArray) {
            // get editor pane
            final RL64TextArea editorpane = ea.getEditorPane();
            editorpane.setAssemblyMode();
        }
    }

    /**
     * Updates line number alignment for gutters of all opened editor panes. Call this method when
     * user changes the line number alignment.
     *
     * For number alignment, use one of following constants:
     * <ul>
     * <li>Gutter.LEFT</li>
     * <li>Gutter.CENTER</li>
     * <li>Gutter.RIGHT</li>
     * </ul>
     * or use
     * {@link de.relaunch64.popelganda.database.Settings#getLineNumerAlignment() getLineNumerAlignment()}.
     */
    public void updateLineNumberAlignment() {
        for (EditorPaneProperties ea : editorPaneArray) {
            // get editor pane
            RL64TextArea editorpane = ea.getEditorPane();
            editorpane.setLineNumberAlignment();
        }
    }

    /**
     * Updates anti aliasing for all opened editor panes. Call this method when user changes the
     * anti alias style.
     * <br><br>
     * For alias style, use one of following constants:
     * <ul>
     * <li>AntiAlias.STANDARD</li>
     * <li>AntiAlias.SUBPIXEL</li>
     * <li>AntiAlias.NONE</li>
     * </ul>
     * or use {@link de.relaunch64.popelganda.database.Settings#getAntiAlias() getAntiAlias()}.
     */
    public void updateAntiAlias() {
        for (EditorPaneProperties ea : editorPaneArray) {
            // get editor pane
            RL64TextArea editorpane = ea.getEditorPane();
            editorpane.setTextAntiAlias();
        }
    }

    /**
     * Updates font settings for all opened editor panes. Call this method when user changes font
     * type or size.
     */
    public void updateFonts() {
        for (EditorPaneProperties ea : editorPaneArray) {
            // get editor pane
            final RL64TextArea editorpane = ea.getEditorPane();
            editorpane.setFonts();
        }
    }

    /**
     * Updates the tab sizefor all opened editor panes. Call this method when user changes tab size.
     * This method takes immediately effect and changes the indent size of all opened editor panes.
     */
    public void updateTabs() {
        for (EditorPaneProperties ea : editorPaneArray) {
            // get editor pane
            final RL64TextArea editorpane = ea.getEditorPane();
            editorpane.setTabs();
        }
    }

    /**
     * Returns the active editor pane (instance of {@code RL64TextArea}). An editor pane is
     * considered as <em>active</em> if it's displayed in the currently selected tab.
     *
     * @return Returns the active editor pane, or {@code null} if an error occured.
     */
    public RL64TextArea getActiveEditorPane() {
        // get selected tab
        return getEditorPane(tabbedPane.getSelectedIndex());
    }

    /**
     * Returns the editor pane (instance of {@code RL64TextArea}) with the index number
     * {@code index}. The instances of RL64TextAreas are stored in the {@code editorPaneArray},
     * which is an array of {@link EditorPaneProperties}.
     *
     * @param index the index number of the editor pane that should be retrieved. this index relates
     * to the selected tab and equals the index of the
     * {@link de.relaunch64.popelganda.Editor.EditorPaneProperties EditorPaneProperties} in the
     * {@link #editorPaneArray editorPaneArray}.
     * @return Returns the editor pane with the index number {@code index}, or {@code null} if an
     * error occured or {@code index} is out of bounds.
     */
    public RL64TextArea getEditorPane(int index) {
        try {
            // get editor pane
            EditorPaneProperties ep = editorPaneArray.get(index);
            // get editor pane
            return ep.getEditorPane();
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    /**
     * Returns the active sourcecode, ie the content of the currently active editor pane (instance
     * of {@code RL64TextArea}). An editor pane is considered as <em>active</em> if it's displayed
     * in the currently selected tab.
     *
     * @return the source code (content) of the currently active editor pane.
     */
    public String getActiveSourceCode() {
        return getSourceCode(getActiveEditorPane());
    }

    /**
     * Returns the sourcecode, ie the content of the editor pane (instance of {@code RL64TextArea})
     * with the index number {@code index}. The instances of RL64TextAreas are stored in the
     * {@link #editorPaneArray}, which is an array of {@link EditorPaneProperties}
     *
     * @param index the index of the tab whose source code should be returned. the index of the
     * selected tab equals the index of the {@link EditorPaneProperties} in the
     * {@link #editorPaneArray editorPaneArray}.
     * @return the source code (content) of the editor pane with the index number {@code index}.
     */
    public String getSourceCode(int index) {
        return getSourceCode(getEditorPane(index));
    }

    /**
     * Returns the sourcecode, ie the content of the editor pane (instance of {@code RL64TextArea})
     * {@code ep}.
     *
     * @param ep a reference to the {@link RL64TextArea}.
     * @return the source code from the {@link RL64TextArea} {@code ep}.
     */
    public String getSourceCode(RL64TextArea ep) {
        if (ep != null) {
            return ep.getText();
        }
        return null;
    }

    /**
     * Sets (overwrites) the source code of the editor pane / tab at the index {@code index}.
     *
     * @param index the index of the tab whose source code should be changed. the index of the
     * selected tab equals the index of the {@link EditorPaneProperties} in the
     * {@link #editorPaneArray editorPaneArray}.
     * @param source the new content
     */
    public void setSourceCode(int index, String source) {
        RL64TextArea ep = getEditorPane(index);
        if (ep != null) {
            ep.setText(source);
        }
    }

    public void setSourceCode(String source) {
        setSourceCode(tabbedPane.getSelectedIndex(), source);
    }

    /**
     * Returns the assembler of the currently activated editor pane / tab.
     *
     * @return the assembler of the currently activated editor pane / tab.
     */
    public Assembler getActiveAssembler() {
        // get selected tab
        return getAssembler(tabbedPane.getSelectedIndex());
    }

    /**
     * Returns the assembler of the editor pane / tab at the index {@code index}.
     *
     * @param index the index of the tab whose assembler should be returned. the index of the
     * selected tab equals the index of the {@link EditorPaneProperties} in the
     * {@link #editorPaneArray editorPaneArray}.
     * @return the assembler of the editor pane / tab at the index {@code index}.
     */
    public Assembler getAssembler(int index) {
        try {
            // get editor pane
            EditorPaneProperties ep = editorPaneArray.get(index);
            // get editor pane
            return ep.getEditorPane().getAssembler();
        } catch (IndexOutOfBoundsException ex) {
            return Assemblers.ASM_KICKASSEMBLER;
        }
    }

    /**
     * returns the selected script which is associated with the sourcecode at the tab-index
     * {@code index}.
     *
     * @param index the index of the tab whose run-script should be returned. the index of the
     * selected tab equals the index of the {@link EditorPaneProperties} in the
     * {@link #editorPaneArray editorPaneArray}.
     * @return the run-script of the editor pane / tab at the index {@code index}.
     */
    public int getScript(int index) {
        try {
            // get editor pane
            EditorPaneProperties ep = editorPaneArray.get(index);
            // get editor pane
            return ep.getScript();
        } catch (IndexOutOfBoundsException ex) {
            return -1;
        }
    }

    /**
     * returns the alternative (2nd) script which is associated with the sourcecode at the tab-index
     * {@code index}.
     *
     * @param index the index of the tab whose run-script should be returned. the index of the
     * selected tab equals the index of the {@link EditorPaneProperties} in the
     * {@link #editorPaneArray editorPaneArray}.
     * @return the alternative run-script of the editor pane / tab at the index {@code index}.
     */
    public int getAlternativeScript(int index) {
        try {
            // get editor pane
            EditorPaneProperties ep = editorPaneArray.get(index);
            // get editor pane
            return ep.getAltScript();
        } catch (IndexOutOfBoundsException ex) {
            return -1;
        }
    }

    /**
     * Returns the run-script of the currently activated editor pane / tab.
     *
     * @return the run-script of the currently activated editor pane / tab.
     */
    public int getActiveScript() {
        return getScript(tabbedPane.getSelectedIndex());
    }

    /**
     * Returns the alternative run-script of the currently activated editor pane / tab.
     *
     * @return the alternative run-script of the currently activated editor pane / tab.
     */
    public int getActiveAlternativeScript() {
        return getAlternativeScript(tabbedPane.getSelectedIndex());
    }

    /**
     * Returns the filepath of the currently activated editor pane / tab.
     *
     * @return the filepath of the currently activated editor pane / tab, or {@code null} if file
     * does not exist (e.g. it's not yet saved) or {@code index} is not valid.
     */
    public File getActiveFilePath() {
        // get selected tab
        return getFilePath(tabbedPane.getSelectedIndex());
    }

    /**
     * Returns the filepath of the editor pane / tab at the index {@code index}.
     *
     * @param index the index of the tab whose filepath should be returned. the index of the
     * selected tab equals the index of the {@link EditorPaneProperties} in the
     * {@link #editorPaneArray editorPaneArray}.
     * @return the filepath of the editor pane / tab at the index {@code index}, or {@code null} if
     * file does not exist (e.g. it's not yet saved) or {@code index} is not valid.
     */
    public File getFilePath(int index) {
        // get selected tab
        if (index != -1) {
            try {
                // get editor pane
                EditorPaneProperties ep = editorPaneArray.get(index);
                // get editor pane
                return ep.getFilePath();
            } catch (IndexOutOfBoundsException ex) {
                return null;
            }
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
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    /**
     * Requests the input focus for the currently activated editor pane.
     */
    public void setFocus() {
        // get active editor pane
        RL64TextArea ep = getActiveEditorPane();
        // check for valid value
        if (ep != null) {
            // set input focus
            ep.requestFocusInWindow();
        }
    }

    /**
     * Sets the modified state for the currently activated editor pane. A modified tab is marked by
     * an "*" infront of the name.
     *
     * @param m the modified state. use {@code true} if source code has unsaved changes.
     */
    public void setModified(boolean m) {
        // retrieve current tab
        int selectedTab = tabbedPane.getSelectedIndex();
        try {
            String title = tabbedPane.getTitleAt(selectedTab);
            if (m) {
                if (!title.startsWith("*")) {
                    title = "* " + title;
                    tabbedPane.setTitleAt(selectedTab, title);
                    ((TabPanel) tabbedPane.getTabComponentAt(selectedTab)).setTitle(title);
                }
                editorPaneArray.get(selectedTab).setModified(m);
            } else {
                if (title.startsWith("* ")) {
                    title = title.substring(2);
                    tabbedPane.setTitleAt(selectedTab, title);
                    ((TabPanel) tabbedPane.getTabComponentAt(selectedTab)).setTitle(title);
                }
                editorPaneArray.get(selectedTab).setModified(m);
            }
        } catch (IndexOutOfBoundsException ex) {
            ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
        }
    }

    /**
     * Opens a file in a new tab.
     *
     * @param filepath the filepath to the file that should be opened.
     * @param assembler a reference to the
     * {@link de.relaunch64.popelganda.assemblers.Assembler Assembler-class}.
     * @param script the user script that should initially be associated with the opened file
     * @param altScript the alternative user script that should initially be associated with the opened file
     *
     * @return {@code true} if a new file was opened, {@code false} if an error occured or file was
     * already opened.
     */
    public boolean loadFile(File filepath, Assembler assembler, int script, int altScript) {
        // check if file is already opened
        int opened = getOpenedFileTab(filepath);
        if (opened != -1) {
            // if yes, select opened tab and do not open it twice
            tabbedPane.setSelectedIndex(opened);
            return false;
        }
        try {
            // check for valid value
            if (filepath != null && filepath.exists()) {
                // read file
                byte[] buffer = new byte[(int) filepath.length()];
                InputStream in = null;
                try {
                    in = new FileInputStream(filepath);                    
                    in.read(buffer);
                } catch (IOException ex) {
                    ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
                    return false;
                } finally {
                    String buf = new String(buffer);
                    boolean CRLF = buf.contains("\r\n");
                    if (CRLF) {
                        buf = buf.replaceAll("\r\n", "\n");
                    }
                    boolean CR = buf.contains("\r");
                    if (CR) {
                        buf = buf.replaceAll("\r", "\n");
                    }
                    boolean LF = buf.contains("\n");
                    // if yes, add new tab
                    int selectedTab = addNewTab(filepath, buf, FileTools.getFileName(filepath), assembler, script, altScript) - 1;
                    // get editor pane properties
                    EditorPaneProperties epp = editorPaneArray.get(selectedTab);
                    // set cursor
                    setCursor(epp.getEditorPane());
                    // set line endings
                    epp.setLineEnd(LF ? (CRLF ? "\r\n" : (CR ? "\r" : "\n")) : System.lineSeparator());
                    // save modifed date
                    epp.setLastModified(filepath.lastModified());
                    // update labels, in case we have them shown
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            // set cursor
                            mainFrame.updateListContent();
                        }
                    });
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException e) {
                        ConstantsR64.r64logger.log(Level.WARNING, e.getLocalizedMessage());
                        return false;
                    }
                }
            } else {
                return false;
            }
        } catch (IndexOutOfBoundsException ex) {
            ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     * Reloads a file after external changes. Loads in the content of the file that is associated with
     * the currently selected tab and then sets the text to the editor pane.
     * 
     * @return 
     */
    public boolean reloadFile() {
        // get filepath of active tab
        File filepath = getActiveFilePath();
        try {
            // check for valid value
            if (filepath != null && filepath.exists()) {
                // read file
                byte[] buffer = new byte[(int) filepath.length()];
                InputStream in = null;
                try {
                    in = new FileInputStream(filepath);                    
                    in.read(buffer);
                } catch (IOException ex) {
                    ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
                    return false;
                } finally {
                    String buf = new String(buffer);
                    boolean CRLF = buf.contains("\r\n");
                    if (CRLF) {
                        buf = buf.replaceAll("\r\n", "\n");
                    }
                    boolean CR = buf.contains("\r");
                    if (CR) {
                        buf = buf.replaceAll("\r", "\n");
                    }
                    boolean LF = buf.contains("\n");
                    // get current editor pane properties
                    EditorPaneProperties epp = getActiveEditorPaneProperties();
                    // get cursor
                    int cursorpos = epp.getEditorPane().getCaretPosition();
                    // set text
                    epp.getEditorPane().setText(buf);
                    // set cursor
                    setCursor(epp.getEditorPane(), cursorpos);
                    epp.setLineEnd(LF ? (CRLF ? "\r\n" : (CR ? "\r" : "\n")) : System.lineSeparator());
                    // update labels, in case we have them shown
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            // set cursor
                            mainFrame.updateListContent();
                        }
                    });
                    // don't set modified state after refresh
                    epp.setModified(false);
                    // reset modified state
                    epp.setLastModified(filepath.lastModified());
                    // set tab modified false
                    setModified(false);
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException e) {
                        ConstantsR64.r64logger.log(Level.WARNING, e.getLocalizedMessage());
                        return false;
                    }
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
            return false;
        }
        return true;
    }
    
    /**
     * Returns the index of the tab with the file {@code fp}.
     *
     * @param fp The file path of a file which should be found in all opened tabs.
     * @return The index of the tab with the file {@code fp}, or -1 if {@code fp} is not opened in a
     * tab yet.
     */
    public int getOpenedFileTab(File fp) {
        // if fp null, we have no tab with file opend
        if (null == fp) {
            return -1;
        }
        // do we have relative path? try to get absolute path
        if (fp.isAbsolute()) {
            fp = fp.getAbsoluteFile();
        }
        // iterate all tabs
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            // get associated file path for each tab
            File opened = editorPaneArray.get(i).getFilePath();
            // if we found filename, return tab-index
            if (opened != null && opened.equals(fp)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Saves the file from the currently selected tab to the filepath {@code filepath}.
     *
     * @param filepath the filepath where to save the file.
     * @return {@code true} if file was successfully saved, {@code false} otherwise.
     */
    private boolean saveFile(File filepath) {
        return saveFile(tabbedPane.getSelectedIndex(), filepath, false);
    }

    /**
     * Saves the file from the tab {@code selectedTab} to the filepath {@code filepath}.
     *
     * @param selectedTab the index number of the tab whose content should be saved.
     * @param filepath the filepath where to save the file.
     * @param ignoreModified when new files are created (new tab), they have no specific
     * modified-status. in such cases, for instance, use {@code true} for this parameter to force
     * saving, even if file is not modified.
     * @return {@code true} if file was successfully saved, {@code false} otherwise.
     */
    private boolean saveFile(int selectedTab, File filepath, boolean ignoreModified) {
        // check whether we have any tab selected
        if (selectedTab != -1) {
            // check for modifications
            if (!ignoreModified && !editorPaneArray.get(selectedTab).isModified()) {
                return true;
            }
            // check for valid value
            if (filepath != null && filepath.exists()) {
                // create filewriter
                Writer fw = null;
                try {
                    EditorPaneProperties epp = editorPaneArray.get(selectedTab);
                    // retrieve text
                    String content = epp.getEditorPane().getText();
                    String lineEnd = epp.getLineEnd();
                    if (!lineEnd.equals("\n")) {
                        content = content.replaceAll("\n", lineEnd);
                    }
                    fw = new FileWriter(filepath);
                    fw.write(content);
                } catch (IOException ex) {
                    ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
                    return false;
                } finally {
                    if (fw != null) {
                        try {
                            fw.close();
                            setModified(false);
                            setTabTitle(selectedTab, filepath);
                            // set file path
                            editorPaneArray.get(selectedTab).setFilePath(filepath);
                            // set last modified
                            editorPaneArray.get(selectedTab).setLastModified(filepath.lastModified());
                        } catch (IOException ex) {
                            ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Saves the content of the currently selected tab.
     *
     * @return {@code true} if file was successfully saved, {@code false} otherwise.
     */
    public boolean saveFile() {
        // retrieve current tab
        int selectedTab = tabbedPane.getSelectedIndex();
        // check whether we have any tab selected
        if (selectedTab != -1) {
            // retrieve filename
            File fp = editorPaneArray.get(selectedTab).getFilePath();
            // check for valid value
            if (fp != null && fp.exists()) {
                return saveFile(fp);
            } else {
                return saveFileAs();
            }
        }
        return false;
    }

    /**
     * Saves all opened files.
     *
     * @return {@code true} if all files could be saved properly, {@code false} if at least one save
     * attempt failed.
     */
    public boolean saveAllFiles() {
        // global error
        boolean allOk = true;
        for (int i = 0; i < getCount(); i++) {
            // retrieve filename
            File fp = editorPaneArray.get(i).getFilePath();
            // check for valid value
            if (fp != null && fp.exists()) {
                if (!saveFile(i, fp, false)) {
                    allOk = false;
                }
            } else {
                if (!saveFileAs(i)) {
                    allOk = false;
                }
            }
        }
        return allOk;
    }

    /**
     * Saves the file / content of the current selected tab under a new file name and path.
     *
     * @return {@code true} if file was successfully saved, {@code false} otherwise.
     */
    public boolean saveFileAs() {
        // retrieve current tab
        return saveFileAs(tabbedPane.getSelectedIndex());
    }

    /**
     * Saves the file / content of the tab with the index {@code selectedTab} under a new file name
     * and path.
     *
     * @param selectedTab the index number of the tab whose content should be saved.
     * @return {@code true} if file was successfully saved, {@code false} otherwise.
     */
    public boolean saveFileAs(int selectedTab) {
        // check whether we have any tab selected
        if (selectedTab != -1) {
            // retrieve filename
            File fp = editorPaneArray.get(selectedTab).getFilePath();
            // init params
            String fpath;
            String fname = "";
            // check for valid value
            if (null != fp) {
                fpath = fp.getPath();
                fname = fp.getName();
            } else {
                fpath = settings.getLastUsedPath().getAbsolutePath();
            }
            // choose file
            File fileToSave = FileTools.chooseFile(null, JFileChooser.SAVE_DIALOG, JFileChooser.FILES_ONLY, fpath, fname, "Save ASM File", ConstantsR64.FILE_EXTENSIONS, "ASM-Files");
            // check for valid value
            if (fileToSave != null) {
                // check whether the user entered a file extension. if not,
                // add ".asm" as extension
                if (!FileTools.isSupportedFileType(fileToSave, FileTools.FILE_TYPE_ASM)) {
                    fileToSave = new File(fileToSave.getPath() + ".asm");
                }
                // check whether file exists
                if (!fileToSave.exists()) {
                    try {
                        // if not, create file
                        if (fileToSave.createNewFile()) {
                            // save file
                            return saveFile(selectedTab, fileToSave, true);
                        } else {
                            return false;
                        }
                    } catch (IOException ex) {
                        ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
                    }
                } else {
                    // file exists, ask user to overwrite it...
                    int optionDocExists = JOptionPane.showConfirmDialog(null, resourceMap.getString("askForOverwriteFileMsg"), resourceMap.getString("askForOverwriteFileTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                    // if the user does *not* choose to overwrite, quit...
                    if (optionDocExists != JOptionPane.YES_OPTION) {
                        return false;
                    } else {
                        return saveFile(selectedTab, fileToSave, true);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Sets the title of the tab with the index {@code index}.
     *
     * @param index the index of the tab whose title should be changed.
     * @param fp the file which is associated with the tab. used to set the file path as tool tip of
     * the tab.
     */
    private void setTabTitle(int index, File fp) {
        // get filename
        String fn = FileTools.getFileName(fp);
        // check whether we have any valid filepath at all
        if (fn != null && !fn.isEmpty()) {
            // check whether extenstion should be shown as well
            if (settings.getShowExtensionInTab()) {
                fn = fn + "." + FileTools.getFileExtension(fp);
            }
            // set file-name and app-name in title-bar
            tabbedPane.setTitleAt(index, fn);
            ((TabPanel) tabbedPane.getTabComponentAt(index)).setTitle(fn);
            tabbedPane.setToolTipTextAt(index, fp.getPath());
        } // if we don't have any title from the file name, simply set the applications title
        else {
            tabbedPane.setTitleAt(index, "untitled");
            ((TabPanel) tabbedPane.getTabComponentAt(index)).setTitle("untitled");
            tabbedPane.setToolTipTextAt(index, "");
        }
    }

    /**
     * This method does not really do what the name might suggests. If another tab is selected, this
     * method updates the combo boxes for assembler and user scripts whith the values that are
     * associated with the selected editor pane (tab).
     */
    public void updateTabbedPane() {
        // get selectect tab
        int selectedTab = tabbedPane.getSelectedIndex();
        // check for valid value
        if (selectedTab != -1 && !editorPaneArray.isEmpty()) {
            try {
                // select assembler, so we update the highlight, if necessary
                jComboBoxAssembler.setSelectedIndex(editorPaneArray.get(selectedTab).getEditorPane().getAssembler().getID());
                // select user script
                jComboBoxScripts.setSelectedIndex(editorPaneArray.get(selectedTab).getScript());
                // update kb-size
                updateLabelBufferSize();
            } catch (IllegalArgumentException ex) {
            }
        }
    }

    /**
     * The count of editor panes.
     *
     * @return The count of editor panes.
     */
    public int getCount() {
        return (null == editorPaneArray) ? 0 : editorPaneArray.size();
    }

    /**
     * Checks whether the file / editor pane on the currently activated JTabbedPane's selected tab
     * is modified or not.
     *
     * @return {@code true} if editor pane's content of the selected JTabbedPane's tab is modified.
     */
    public boolean isModified() {
        return isModified(tabbedPane.getSelectedIndex());
    }

    /**
     * Checks whether the file / editor pane on the JTabbedPane with the index {@code selectedTab}
     * is modified or not.
     *
     * @param selectedTab the tab which should be checked for modifications
     * @return {@code true} if editor pane's content on the JTabbedPane's tab with index
     * {@code selectedTab} is modified.
     */
    public boolean isModified(int selectedTab) {
        // retrieve active editor pane
        EditorPaneProperties ep = getEditorPaneProperties(selectedTab);
        // check for valid value
        if (ep != null) {
            // check whether modified
            return (ep.isModified());
        }
        return false;
    }

    /**
     * Closes the current activated editor pane file. If editor content is modified, a JOptionPane
     * will popup and asks for saving changes. After that, the currently activated editor pane will
     * be removed from the {@link #editorPaneArray}.
     *
     * <b>Note that the currently selected tab from the {@link #tabbedPane} has to be removed
     * manually (e.g. to be done in the method that calls this method)!</b>
     *
     * @return {@code true} if editor pane (file) was successfully closed.
     */
    public boolean closeFile() {
        // retrieve active editor pane
        EditorPaneProperties ep = getActiveEditorPaneProperties();
        // check for valid value
        if (ep != null) {
            // check whether modified
            if (ep.isModified()) {
                // if so, open a confirm dialog
                int option = JOptionPane.showConfirmDialog(null, resourceMap.getString("msgSaveChanges"), resourceMap.getString("msgSaveChangesTitle"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                // if action is cancelled, return to the program
                if (JOptionPane.CANCEL_OPTION == option || JOptionPane.CLOSED_OPTION == option /* User pressed cancel key */) {
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
                // update labels, in case we have them shown
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        // set cursor
                        mainFrame.updateListContent();
                    }
                });
                // success
                return true;
            } catch (IndexOutOfBoundsException | UnsupportedOperationException ex) {
            }
        }
        return false;
    }

    /**
     * Inserts a (commented) separator line into the source. The current line of the caret position
     * is moved down and the separator line is inserted at the beginning of the caret's current
     * line.
     */
    public void insertSeparatorLine() {
        // get current editor
        RL64TextArea ep = getActiveEditorPane();
        // set up section name
        String insertString = getActiveAssembler().getLineComment() + " ----------------------------------------\n";
        // insert string
        insertString(insertString, ep.getLineStartOffset(ep.getCaretLine()));
    }

    /**
     * Inserts a breakpoint macro to make use of VICE's debugging feature. Currently only works with
     * KickAss, should be rewritten.
     *
     * @param assembler not used, only applies to kickass
     */
    public void insertBreakPoint(Assembler assembler) {
        // get current editor
        RL64TextArea ep = getActiveEditorPane();
        String insertString = "";
        if (assembler == Assemblers.ASM_KICKASSEMBLER) {
            insertString = ConstantsR64.STRING_BREAKPOINT_KICKASSEMBLER;
        }
        // insert string
        insertString(insertString, ep.getLineStartOffset(ep.getCaretLine()));
    }

    /**
     * Inserts a breakpoint macro to make use of VICE's debugging feature. Currently only works with
     * KickAss, should be rewritten.
     */
    public void insertBreakPoint() {
        // TODO may be more generic, see http://codebase64.org/doku.php?id=base:using_the_vice_monitor
        insertBreakPoint(getActiveAssembler());
    }

    /**
     * Generic goto-line-function. Goes to a line of a specific macro, function or label given in
     * {@code name}. The retrives function / macro / label names and line numbers are passed as
     * LinkedHasMap {@code map} and can be retrieved via {@code getLabels()}, {@code getFunctions()}
     * or {@code getMacros()}.
     *
     * @param map A linked HashMap with label / macro / function names and linenumbers, retrieved
     * via {@code getLabels()}, {@code getFunctions()} or {@code getMacros()}.
     * @param name The name of the label / macro / function where to go
     */
    protected void gotoLine(LinkedHashMap<String, Integer> map, String name) {
        try {
            gotoLine(map.get(name), 1);
        } catch (Exception e) {
        }
    }

    /**
     * Inserts the string {@code text} at the caret position into the currently activated editor
     * pane. An editor pane is considered as <em>active</em> if it's displayed in the currently
     * selected tab.
     *
     * @param text the content that should be pasted into the source
     */
    public void insertString(String text) {
        insertString(text, getActiveEditorPane().getCaretPosition());
    }

    /**
     * Inserts the string {@code text} at the specific caret position {@code position} into the
     * currently activated editor pane. An editor pane is considered as <em>active</em> if it's
     * displayed in the currently selected tab.
     *
     * @param text the content that should be pasted into the source
     * @param position the caret position where to paste the content ({@code text}).
     */
    public void insertString(String text, int position) {
        JEditBuffer buffer = getActiveEditorPane().getBuffer();
        buffer.insert(position, text);
    }

    /**
     * (Un-)comments the current text selection.
     */
    public void commentLine() {
        EditorPaneTools.commentLine(getActiveEditorPane(), getActiveAssembler().getLineComment());
    }

    /**
     * Gets the currently selected tab.
     *
     * @return the index number of the currently selected tab
     */
    public int getSelectedTab() {
        return tabbedPane.getSelectedIndex();
    }

    /**
     * Selects the tab with the index {@code tab}. May be used on compiling a source, to open a
     * certain tab, which contains errors.
     *
     * @param tab the index of the tab that should be selected.
     */
    public void setSelectedTab(int tab) {
        try {
            tabbedPane.setSelectedIndex(tab);
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    /**
     * Scrolls the caret to the next label (if any) in the current editor pane. If last label is
     * reached, wraps around and jumps to first label.
     */
    public void gotoNextLabel() {
        gotoLine(EditorPaneTools.findJumpToken(DIRECTION_NEXT,
                getActiveEditorPane().getCaretLine() + 1,
                LabelExtractor.getLineNumbers(LabelExtractor.getLabels(getActiveSourceCode(), getActiveAssembler(), 0).labels)), 1);
    }

    /**
     * Scrolls the caret to the previous label (if any) in the current editor pane. If first label
     * is reached, wraps around and jumps to last label.
     */
    public void gotoPrevLabel() {
        gotoLine(EditorPaneTools.findJumpToken(DIRECTION_PREV,
                getActiveEditorPane().getCaretLine() + 1,
                LabelExtractor.getLineNumbers(LabelExtractor.getLabels(getActiveSourceCode(), getActiveAssembler(), 0).labels)), 1);
    }

    /**
     * Scrolls the caret to the next section (if any) in the current editor pane. If last section is
     * reached, wraps around and jumps to first section.
     */
    public void gotoNextSection() {
        gotoLine(EditorPaneTools.findJumpToken(DIRECTION_NEXT,
                getActiveEditorPane().getCaretLine() + 1,
                SectionExtractor.getSectionLineNumbers(getActiveSourceCode(), getActiveAssembler().getLineComment())), 1);
    }

    /**
     * Scrolls the caret to the previous section (if any) in the current editor pane. If first
     * section is reached, wraps around and jumps to last section.
     */
    public void gotoPrevSection() {
        gotoLine(EditorPaneTools.findJumpToken(DIRECTION_PREV,
                getActiveEditorPane().getCaretLine() + 1,
                SectionExtractor.getSectionLineNumbers(getActiveSourceCode(), getActiveAssembler().getLineComment())), 1);
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

    public void saveLabelSourcePosition(int caret) {
        getActiveEditorPaneProperties().setLabelSourcePosition(caret);
    }

    public void gotoLabelSourcePosition() {
        // get text area
        RL64TextArea ep = getActiveEditorPane();
        int caret = getActiveEditorPaneProperties().getLabelSourcePosition();
        // check for value
        if (caret >= 0) {
            // set caret position
            ep.setCaretPosition(caret);
            // scroll and center caret position
            ep.scrollAndCenterCaret();
        }
    }
}
