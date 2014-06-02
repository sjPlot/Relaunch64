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
package de.relaunch64.popelganda;

import de.relaunch64.popelganda.Editor.ColorSchemes;
import de.relaunch64.popelganda.Editor.EditorPanes;
import de.relaunch64.popelganda.database.CustomScripts;
import de.relaunch64.popelganda.database.Settings;
import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.assemblers.Assembler;
import de.relaunch64.popelganda.assemblers.Assemblers;
import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.gjt.sp.jedit.textarea.AntiAlias;
import org.gjt.sp.jedit.textarea.Gutter;
import org.jdesktop.application.Action;

/**
 *
 * @author Daniel Lüdecke
 */
public class SettingsDlg extends javax.swing.JDialog implements DropTargetListener {
    private final Settings settings;
    private final CustomScripts scripts;
    private final EditorPanes editorPanes;
    private Font mainfont;
    private static final String quickHelpText = "Enter command lines here or drag & drop executable files\n(first compiler, then cruncher (optional), finally emulator)\nfrom explorer window to automatically generate a script.\n\nPress help-button for more details and examples.";
    /**
     * Creates new form SettingsDlg
     * @param parent
     * @param s
     * @param scr
     * @param ep
     */
    public SettingsDlg(java.awt.Frame parent, Settings s, CustomScripts scr, EditorPanes ep) {
        super(parent);
        settings = s;
        scripts = scr;
        editorPanes = ep;
        initComponents();
        jLabelRestart.setVisible(false);
        // set font-preview for change-font-label
        mainfont = settings.getMainFont();
        jLabelFont.setText(mainfont.getFontName());
        jLabelFont.setFont(mainfont);
        // restart-to-apply-text hidden
        // set preferred compiler settings
        jComboBoxPrefASM.setSelectedIndex(settings.getPreferredAssembler().getID());
        // init line number alignment
        switch (settings.getLineNumerAlignment()) {
            case Gutter.RIGHT:
                jComboBoxLineNumberAlign.setSelectedIndex(0);
                break;
            case Gutter.CENTER:
                jComboBoxLineNumberAlign.setSelectedIndex(1);
                break;
            case Gutter.LEFT:
                jComboBoxLineNumberAlign.setSelectedIndex(2);
                break;
        }
        switch (settings.getAntiAlias()) {
            case AntiAlias.NONE:
                jComboBoxAntiAlias.setSelectedIndex(0);
                break;
            case AntiAlias.STANDARD:
                jComboBoxAntiAlias.setSelectedIndex(1);
                break;
            case AntiAlias.SUBPIXEL:
                jComboBoxAntiAlias.setSelectedIndex(2);
                break;
        }
        // init other settings
        jCheckBoxCheckUpdates.setSelected(settings.getCheckForUpdates());
        jCheckBoxSaveOnCompile.setSelected(settings.getSaveOnCompile());
        jCheckBoxReopenFiles.setSelected(settings.getReopenOnStartup());
        jCheckBoxNimbusOnOSX.setSelected(settings.getNimbusOnOSX());
        jCheckBoxScaleFonts.setSelected(settings.getScaleFont());
        jCheckBoxSuggestCaseSort.setSelected(!settings.getSuggestionSortIgnoresCase());
        jCheckBoxAlternativeAssemblyMode.setSelected(settings.getAlternativeAssemblyMode());
        // init schemes
        initSchemes();
        // init user scripts, including combo box setting etc
        initScripts();
        // init listeners afterwards, because script-init fires events
        initListeners();
        // get tab char
        jTextFieldTabWidth.setText(String.valueOf(settings.getTabWidth()));
        // set application icon
        setIconImage(ConstantsR64.r64icon.getImage());
        // set Mnemonic keys
        jButtonApplyScript.setDisplayedMnemonicIndex(0);
        jButtonRemoveScript.setDisplayedMnemonicIndex(0);
        // disable apply buttons
        setModifiedTabScript(false);
        setModifiedTabFont(false);
        setModifiedTabScheme(false);
        setModifiedTabOther(false);
    }
    private void initSchemes() {
        jComboBoxSyntaxScheme.removeAllItems();
        for (String sn : ColorSchemes.SCHEME_NAMES) jComboBoxSyntaxScheme.addItem(sn);
        jComboBoxSyntaxScheme.setMaximumRowCount(jComboBoxSyntaxScheme.getItemCount());
        try {
            jComboBoxSyntaxScheme.setSelectedIndex(settings.getColorScheme());
        }
        catch (IllegalArgumentException ex) {
        }
        updateSchemePreview();
    }
    private void updateSchemePreview() {
        String imagepath = ConstantsR64.colorpreviews[jComboBoxSyntaxScheme.getSelectedIndex()];
        jLabelSchemePreview.setIcon(new ImageIcon(ConstantsR64.class.getResource(imagepath)));
    }
    private void initScripts() {
        // get all action listeners from the combo box
        ActionListener[] al = jComboBoxCustomScripts.getActionListeners();
        // remove all action listeners so we don't fire several action-events
        // when we update the combo box. we can set the action listener later again
        for (ActionListener listener : al) jComboBoxCustomScripts.removeActionListener(listener);
        // init custom scripts
        jComboBoxCustomScripts.removeAllItems();
        // retrieve all script names
        String[] scriptNames = scripts.getScriptNames();
        // check if we have any
        if (scriptNames!=null && scriptNames.length>0) {
            // sort
            Arrays.sort(scriptNames);
            // add item to cb
            for (String sn : scriptNames) jComboBoxCustomScripts.addItem(sn);
            setRemovePossible(true);
        }
        else {
            resetScriptFields();
        }
        jComboBoxCustomScripts.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // get selected script
                String scriptname = jComboBoxCustomScripts.getSelectedItem().toString();
                // check if valid
                if (scriptname!=null && !scriptname.isEmpty()) {
                    // set script name to textfield
                    jTextFieldScriptName.setText(scriptname);
                    // set content to text area
                    jTextAreaUserScript.setForeground(Color.black);
                    jTextAreaUserScript.setText(scripts.getScript(scriptname));
                    // change button text to update
                    jButtonApplyScript.setText("Update script");
                }
                else {
                    // set script name to textfield
                    jTextFieldScriptName.setText("");
                    // set content to text area
                    jTextAreaUserScript.setText("");
                    // change button text to update
                    jButtonApplyScript.setText("Add script");
                }
                setModifiedTabScript(false);
                setRemovePossible(true);
            }
        });
        try {
            jComboBoxCustomScripts.setSelectedIndex(0);
        }
        catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
        }
    }
    private void initListeners() {
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        /**
         * JDK 8 Lambda
         */
//        ActionListener cancelAction = (ActionEvent evt) -> {
//            setVisible(false);
//            dispose();
//        };
        ActionListener cancelAction = new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                setVisible(false);
                dispose();
            }
        };        
        getRootPane().registerKeyboardAction(cancelAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        /**
         * JDK 8 Lambda
         */
//        jComboBoxCompilers.addActionListener((java.awt.event.ActionEvent evt) -> {
//            updateCompilerSettings();
//            setModifiedTabScript(false);
//        });
//        jComboBoxEmulators.addActionListener((java.awt.event.ActionEvent evt) -> {
//            updateEmulatorSettings();
//            setModifiedTabEmulator(false);
//        });
        jCheckBoxCheckUpdates.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModifiedTabOther(true);
            }
        });
        jCheckBoxSaveOnCompile.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModifiedTabOther(true);
            }
        });
        jCheckBoxNimbusOnOSX.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModifiedTabOther(true);
            }
        });
        jCheckBoxAlternativeAssemblyMode.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModifiedTabScheme(true);
            }
        });
        jCheckBoxReopenFiles.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModifiedTabOther(true);
            }
        });
        jComboBoxPrefASM.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModifiedTabFont(true);
            }
        });
        jComboBoxSyntaxScheme.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModifiedTabScheme(true);
                updateSchemePreview();
            }
        });
        jComboBoxLineNumberAlign.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModifiedTabFont(true);
            }
        });
        jComboBoxAntiAlias.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModifiedTabFont(true);
            }
        });
        jCheckBoxScaleFonts.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLabelRestart.setVisible(true);
                setModifiedTabFont(true);
            }
        });
        jCheckBoxSuggestCaseSort.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModifiedTabFont(true);
            }
        });
        jTextAreaUserScript.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode()==KeyEvent.VK_X && (evt.isControlDown() || evt.isMetaDown())) {
                    jTextAreaUserScript.cut();
                    evt.consume();
                }
                else if (evt.getKeyCode()==KeyEvent.VK_C && (evt.isControlDown() || evt.isMetaDown())) {
                    jTextAreaUserScript.copy();
                    evt.consume();
                }
                else if (evt.getKeyCode()==KeyEvent.VK_V && (evt.isControlDown() || evt.isMetaDown())) {
                    jTextAreaUserScript.paste();
                    evt.consume();
                }
            }
        });
        jTextAreaUserScript.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { switchButtonLabel(); }
            @Override public void insertUpdate(DocumentEvent e) { switchButtonLabel(); }
            @Override public void removeUpdate(DocumentEvent e) { switchButtonLabel(); }
        });
        jTextFieldScriptName.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { switchButtonLabel(); }
            @Override public void insertUpdate(DocumentEvent e) { switchButtonLabel(); }
            @Override public void removeUpdate(DocumentEvent e) { switchButtonLabel(); }
        });
        jTextFieldTabWidth.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { setModifiedTabFont(true); }
            @Override public void insertUpdate(DocumentEvent e) { setModifiedTabFont(true); }
            @Override public void removeUpdate(DocumentEvent e) { setModifiedTabFont(true); }
        });
        jTextAreaUserScript.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent evt) {
                String content = jTextAreaUserScript.getText();
                if (content.equalsIgnoreCase(quickHelpText)) {
                    jTextAreaUserScript.setText("");
                    jTextAreaUserScript.setForeground(Color.black);
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent evt) {
                String content = jTextAreaUserScript.getText();
                if (content.isEmpty()) {
                    jTextAreaUserScript.setText(quickHelpText);
                    jTextAreaUserScript.setForeground(Color.lightGray);
                }
            }
        });
        jTextAreaUserScript.setDragEnabled(true);
        DropTarget dropTarget = new DropTarget(jTextAreaUserScript, this);   
    }
    private void switchButtonLabel() {
        String name = jTextFieldScriptName.getText();
        String content = jTextAreaUserScript.getText();
        if (name!=null) jButtonApplyScript.setText((scripts.findScript(name)!=-1) ? "Update script" : "Add script");
        jButtonApplyScript.setDisplayedMnemonicIndex(0);
        setModifiedTabScript(name!=null && !name.isEmpty() && !content.isEmpty() && !content.equals(quickHelpText));
    }
    @Action
    public void showScriptHelp() {
        if (helpBox == null) {
            helpBox = new Relaunch64AboutBox(null, org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getClass().getResource("/de/relaunch64/popelganda/resources/help_userscripts.html"));
            helpBox.setLocationRelativeTo(this);
        }
        Relaunch64App.getApplication().show(helpBox);
        helpBox.dispose();
        helpBox = null;
    }
    @Action
    public void changeEditorFont() {
        if (null == fontDlg) {
            fontDlg = new FontChooser(null, settings.getMainFont());
            fontDlg.setLocationRelativeTo(null);
        }
        Relaunch64App.getApplication().show(fontDlg);
        java.awt.Font f = fontDlg.getSelectedFont();
        if (f!=null) {
            mainfont = f;
            jLabelFont.setFont(f);
            jLabelFont.setText(f.getFontName());
            // save font in settings
            settings.setMainfont(mainfont);
            // apply font to editor panes
            editorPanes.setFonts(settings.getMainFont());
        }
    }
    @Action(enabledProperty = "modifiedTabFont")
    public void applyFontTab() {
        // get tab width
        try {
            settings.setTabWidth(Integer.parseInt(jTextFieldTabWidth.getText()));
        }
        catch (NumberFormatException ex) {
        }
        settings.setPreferredAssembler(Assemblers.byID(jComboBoxPrefASM.getSelectedIndex()));
        settings.setColorScheme(jComboBoxSyntaxScheme.getSelectedIndex());
        settings.setScaleFont(jCheckBoxScaleFonts.isSelected());
        settings.setSuggestionSortIgnoresCase(!jCheckBoxSuggestCaseSort.isSelected());
        switch(jComboBoxLineNumberAlign.getSelectedIndex()) {
            case 0:
                settings.setLineNumerAlignment(Gutter.RIGHT);
                break;
            case 1:
                settings.setLineNumerAlignment(Gutter.CENTER);
                break;
            case 2:
                settings.setLineNumerAlignment(Gutter.LEFT);
                break;
        }
        switch(jComboBoxAntiAlias.getSelectedIndex()) {
            case 0:
                settings.setAntiAlias(AntiAlias.NONE);
                break;
            case 1:
                settings.setAntiAlias(AntiAlias.STANDARD);
                break;
            case 2:
                settings.setAntiAlias(AntiAlias.SUBPIXEL);
                break;
        }
        setModifiedTabFont(false);
        editorPanes.setLineNumberAlignment(settings.getLineNumerAlignment());
        editorPanes.setTabs(settings.getTabWidth());
        editorPanes.setAntiAlias(settings.getAntiAlias());
    }
    @Action(enabledProperty = "modifiedTabScheme")
    public void applyColorScheme() {
        settings.setAlternativeAssemblyMode(jCheckBoxAlternativeAssemblyMode.isSelected());
        settings.setColorScheme(jComboBoxSyntaxScheme.getSelectedIndex());
        setModifiedTabScheme(false);
        editorPanes.updateColorScheme();
        editorPanes.updateAssemblyMode();
    }
    @Action
    public void addNewScript() {
        resetScriptFields();
        jTextFieldScriptName.requestFocusInWindow();
    }
    private void resetScriptFields() {
        jTextFieldScriptName.setText("");
        // if we have no scripts, set default quick help text
        jTextAreaUserScript.setText(quickHelpText);
        jTextAreaUserScript.setForeground(Color.lightGray);
        setRemovePossible(false);
    }
    @Action(enabledProperty = "modifiedTabScript")
    public void applyScript() {
        // get script name
        String name = jTextFieldScriptName.getText();
        // get content
        String content = jTextAreaUserScript.getText();
        // add script
        if (scripts.addScript(name, content)) {
            initScripts();
            try {
                jComboBoxCustomScripts.setSelectedItem(name);
            }
            catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            }
        }
        setModifiedTabScript(false);
    }
    @Action(enabledProperty = "modifiedTabOther")
    public void applyOther() {
        settings.setCheckForUpdates(jCheckBoxCheckUpdates.isSelected());
        settings.setSaveOnCompile(jCheckBoxSaveOnCompile.isSelected());
        settings.setNimbusOnOSX(jCheckBoxNimbusOnOSX.isSelected());
        settings.setReopenOnStartup(jCheckBoxReopenFiles.isSelected());
        setModifiedTabOther(false);
    }
    @Action(enabledProperty = "removePossible")
    public void removeScript() {
        if (jComboBoxCustomScripts.getSelectedIndex()<0) return;
        String name = jComboBoxCustomScripts.getSelectedItem().toString();
        if (scripts.removeScript(name)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    initScripts();
                    setModifiedTabScript(false);
                }
            });
        }
    }
    private boolean modifiedTabScript = false;
    public boolean isModifiedTabScript() {
        return modifiedTabScript;
    }
    public final void setModifiedTabScript(boolean b) {
        boolean old = isModifiedTabScript();
        this.modifiedTabScript = b;
        firePropertyChange("modifiedTabScript", old, isModifiedTabScript());
    }
    private boolean modifiedTabOther = false;
    public boolean isModifiedTabOther() {
        return modifiedTabOther;
    }
    public final void setModifiedTabOther(boolean b) {
        boolean old = isModifiedTabOther();
        this.modifiedTabOther = b;
        firePropertyChange("modifiedTabOther", old, isModifiedTabOther());
    }
    private boolean modifiedTabFont = false;
    public boolean isModifiedTabFont() {
        return modifiedTabFont;
    }
    public final void setModifiedTabFont(boolean b) {
        boolean old = isModifiedTabFont();
        this.modifiedTabFont = b;
        firePropertyChange("modifiedTabFont", old, isModifiedTabFont());
    }
    private boolean modifiedTabScheme = false;
    public boolean isModifiedTabScheme() {
        return modifiedTabScheme;
    }
    public final void setModifiedTabScheme(boolean b) {
        boolean old = isModifiedTabScheme();
        this.modifiedTabScheme = b;
        firePropertyChange("modifiedTabScheme", old, isModifiedTabScheme());
    }
    private boolean removePossible = false;
    public boolean isRemovePossible() {
        return removePossible;
    }
    public final void setRemovePossible(boolean b) {
        boolean old = isRemovePossible();
        this.removePossible = b;
        firePropertyChange("removePossible", old, isRemovePossible());
    }
    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jComboBoxCustomScripts = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldScriptName = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaUserScript = new javax.swing.JTextArea();
        jButtonApplyScript = new javax.swing.JButton();
        jButtonScriptHelp = new javax.swing.JButton();
        jButtonRemoveScript = new javax.swing.JButton();
        jButtonNewScript = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabelFont = new javax.swing.JLabel();
        jButtonFont = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jTextFieldTabWidth = new javax.swing.JTextField();
        jComboBoxPrefASM = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jButtonApplyTabAndFont = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxLineNumberAlign = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jComboBoxAntiAlias = new javax.swing.JComboBox();
        jCheckBoxScaleFonts = new javax.swing.JCheckBox();
        jLabelRestart = new javax.swing.JLabel();
        jCheckBoxSuggestCaseSort = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxSyntaxScheme = new javax.swing.JComboBox();
        jButtonApplyScheme = new javax.swing.JButton();
        jLabelSchemePreview = new javax.swing.JLabel();
        jCheckBoxAlternativeAssemblyMode = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jCheckBoxCheckUpdates = new javax.swing.JCheckBox();
        jCheckBoxSaveOnCompile = new javax.swing.JCheckBox();
        jCheckBoxReopenFiles = new javax.swing.JCheckBox();
        jCheckBoxNimbusOnOSX = new javax.swing.JCheckBox();
        jButtonApplyOther = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getContext().getResourceMap(SettingsDlg.class);
        setTitle(resourceMap.getString("FormSettings.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(400, 380));
        setModal(true);
        setName("FormSettings"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        jLabel8.setDisplayedMnemonic('s');
        jLabel8.setLabelFor(jComboBoxCustomScripts);
        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jComboBoxCustomScripts.setName("jComboBoxCustomScripts"); // NOI18N

        jLabel9.setDisplayedMnemonic('m');
        jLabel9.setLabelFor(jTextFieldScriptName);
        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jTextFieldScriptName.setColumns(20);
        jTextFieldScriptName.setName("jTextFieldScriptName"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextAreaUserScript.setName("jTextAreaUserScript"); // NOI18N
        jScrollPane1.setViewportView(jTextAreaUserScript);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getContext().getActionMap(SettingsDlg.class, this);
        jButtonApplyScript.setAction(actionMap.get("applyScript")); // NOI18N
        jButtonApplyScript.setName("jButtonApplyScript"); // NOI18N

        jButtonScriptHelp.setAction(actionMap.get("showScriptHelp")); // NOI18N
        jButtonScriptHelp.setName("jButtonScriptHelp"); // NOI18N

        jButtonRemoveScript.setAction(actionMap.get("removeScript")); // NOI18N
        jButtonRemoveScript.setName("jButtonRemoveScript"); // NOI18N

        jButtonNewScript.setAction(actionMap.get("addNewScript")); // NOI18N
        jButtonNewScript.setMnemonic('n');
        jButtonNewScript.setName("jButtonNewScript"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                        .add(jButtonScriptHelp)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jButtonRemoveScript)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonApplyScript))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 525, Short.MAX_VALUE)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel8)
                            .add(jLabel9))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel3Layout.createSequentialGroup()
                                .add(jComboBoxCustomScripts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButtonNewScript)
                                .add(0, 0, Short.MAX_VALUE))
                            .add(jTextFieldScriptName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(jComboBoxCustomScripts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonNewScript))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(jTextFieldScriptName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonApplyScript)
                    .add(jButtonScriptHelp)
                    .add(jButtonRemoveScript))
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jPanel5.setName("jPanel5"); // NOI18N

        jLabel10.setDisplayedMnemonic('f');
        jLabel10.setLabelFor(jButtonFont);
        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jLabelFont.setName("jLabelFont"); // NOI18N

        jButtonFont.setAction(actionMap.get("changeEditorFont")); // NOI18N
        jButtonFont.setName("jButtonFont"); // NOI18N

        jLabel11.setDisplayedMnemonic('t');
        jLabel11.setLabelFor(jTextFieldTabWidth);
        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        jTextFieldTabWidth.setColumns(4);
        jTextFieldTabWidth.setText(resourceMap.getString("jTextFieldTabWidth.text")); // NOI18N
        jTextFieldTabWidth.setName("jTextFieldTabWidth"); // NOI18N

        jComboBoxPrefASM.setModel(new javax.swing.DefaultComboBoxModel(Assemblers.names()));
        jComboBoxPrefASM.setName("jComboBoxPrefASM"); // NOI18N

        jLabel6.setDisplayedMnemonic('c');
        jLabel6.setLabelFor(jComboBoxPrefASM);
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jButtonApplyTabAndFont.setAction(actionMap.get("applyFontTab")); // NOI18N
        jButtonApplyTabAndFont.setName("jButtonApplyTabAndFont"); // NOI18N

        jLabel1.setDisplayedMnemonic('l');
        jLabel1.setLabelFor(jComboBoxLineNumberAlign);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jComboBoxLineNumberAlign.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Right", "Center", "Left" }));
        jComboBoxLineNumberAlign.setName("jComboBoxLineNumberAlign"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jComboBoxAntiAlias.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Standard", "Subpixel" }));
        jComboBoxAntiAlias.setName("jComboBoxAntiAlias"); // NOI18N

        jCheckBoxScaleFonts.setText(resourceMap.getString("jCheckBoxScaleFonts.text")); // NOI18N
        jCheckBoxScaleFonts.setToolTipText(resourceMap.getString("jCheckBoxScaleFonts.toolTipText")); // NOI18N
        jCheckBoxScaleFonts.setName("jCheckBoxScaleFonts"); // NOI18N

        jLabelRestart.setForeground(resourceMap.getColor("jLabelRestart.foreground")); // NOI18N
        jLabelRestart.setText(resourceMap.getString("jLabelRestart.text")); // NOI18N
        jLabelRestart.setName("jLabelRestart"); // NOI18N

        jCheckBoxSuggestCaseSort.setText(resourceMap.getString("jCheckBoxSuggestCaseSort.text")); // NOI18N
        jCheckBoxSuggestCaseSort.setToolTipText(resourceMap.getString("jCheckBoxSuggestCaseSort.toolTipText")); // NOI18N
        jCheckBoxSuggestCaseSort.setName("jCheckBoxSuggestCaseSort"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel5Layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(jButtonApplyTabAndFont))
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel5Layout.createSequentialGroup()
                                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel6)
                                    .add(jLabel10)
                                    .add(jLabel11)
                                    .add(jLabel1)
                                    .add(jLabel3))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jComboBoxAntiAlias, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jComboBoxLineNumberAlign, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jPanel5Layout.createSequentialGroup()
                                        .add(jLabelFont)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jButtonFont))
                                    .add(jTextFieldTabWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jComboBoxPrefASM, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(jPanel5Layout.createSequentialGroup()
                                .add(jCheckBoxScaleFonts)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabelRestart))
                            .add(jCheckBoxSuggestCaseSort))
                        .add(0, 196, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(jLabelFont)
                    .add(jButtonFont))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(jTextFieldTabWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(jComboBoxPrefASM, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jComboBoxLineNumberAlign, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jComboBoxAntiAlias, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jCheckBoxSuggestCaseSort)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jCheckBoxScaleFonts)
                    .add(jLabelRestart))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 51, Short.MAX_VALUE)
                .add(jButtonApplyTabAndFont)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        jLabel2.setDisplayedMnemonic('s');
        jLabel2.setLabelFor(jComboBoxSyntaxScheme);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jComboBoxSyntaxScheme.setName("jComboBoxSyntaxScheme"); // NOI18N

        jButtonApplyScheme.setAction(actionMap.get("applyColorScheme")); // NOI18N
        jButtonApplyScheme.setName("jButtonApplyScheme"); // NOI18N

        jLabelSchemePreview.setName("jLabelSchemePreview"); // NOI18N

        jCheckBoxAlternativeAssemblyMode.setText(resourceMap.getString("jCheckBoxAlternativeAssemblyMode.text")); // NOI18N
        jCheckBoxAlternativeAssemblyMode.setToolTipText(resourceMap.getString("jCheckBoxAlternativeAssemblyMode.toolTipText")); // NOI18N
        jCheckBoxAlternativeAssemblyMode.setName("jCheckBoxAlternativeAssemblyMode"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(jButtonApplyScheme))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jComboBoxSyntaxScheme, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jLabelSchemePreview)
                            .add(jCheckBoxAlternativeAssemblyMode))
                        .add(0, 423, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jComboBoxSyntaxScheme, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelSchemePreview)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jCheckBoxAlternativeAssemblyMode)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 239, Short.MAX_VALUE)
                .add(jButtonApplyScheme)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jCheckBoxCheckUpdates.setText(resourceMap.getString("jCheckBoxCheckUpdates.text")); // NOI18N
        jCheckBoxCheckUpdates.setToolTipText(resourceMap.getString("jCheckBoxCheckUpdates.toolTipText")); // NOI18N
        jCheckBoxCheckUpdates.setName("jCheckBoxCheckUpdates"); // NOI18N

        jCheckBoxSaveOnCompile.setText(resourceMap.getString("jCheckBoxSaveOnCompile.text")); // NOI18N
        jCheckBoxSaveOnCompile.setToolTipText(resourceMap.getString("jCheckBoxSaveOnCompile.toolTipText")); // NOI18N
        jCheckBoxSaveOnCompile.setName("jCheckBoxSaveOnCompile"); // NOI18N

        jCheckBoxReopenFiles.setText(resourceMap.getString("jCheckBoxReopenFiles.text")); // NOI18N
        jCheckBoxReopenFiles.setToolTipText(resourceMap.getString("jCheckBoxReopenFiles.toolTipText")); // NOI18N
        jCheckBoxReopenFiles.setName("jCheckBoxReopenFiles"); // NOI18N

        jCheckBoxNimbusOnOSX.setText(resourceMap.getString("jCheckBoxNimbusOnOSX.text")); // NOI18N
        jCheckBoxNimbusOnOSX.setToolTipText(resourceMap.getString("jCheckBoxNimbusOnOSX.toolTipText")); // NOI18N
        jCheckBoxNimbusOnOSX.setName("jCheckBoxNimbusOnOSX"); // NOI18N

        jButtonApplyOther.setAction(actionMap.get("applyOther")); // NOI18N
        jButtonApplyOther.setName("jButtonApplyOther"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBoxCheckUpdates)
                    .add(jCheckBoxSaveOnCompile)
                    .add(jCheckBoxReopenFiles)
                    .add(jCheckBoxNimbusOnOSX))
                .addContainerGap(433, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jButtonApplyOther)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jCheckBoxCheckUpdates)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jCheckBoxSaveOnCompile)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jCheckBoxReopenFiles)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jCheckBoxNimbusOnOSX)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 179, Short.MAX_VALUE)
                .add(jButtonApplyOther)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jTabbedPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApplyOther;
    private javax.swing.JButton jButtonApplyScheme;
    private javax.swing.JButton jButtonApplyScript;
    private javax.swing.JButton jButtonApplyTabAndFont;
    private javax.swing.JButton jButtonFont;
    private javax.swing.JButton jButtonNewScript;
    private javax.swing.JButton jButtonRemoveScript;
    private javax.swing.JButton jButtonScriptHelp;
    private javax.swing.JCheckBox jCheckBoxAlternativeAssemblyMode;
    private javax.swing.JCheckBox jCheckBoxCheckUpdates;
    private javax.swing.JCheckBox jCheckBoxNimbusOnOSX;
    private javax.swing.JCheckBox jCheckBoxReopenFiles;
    private javax.swing.JCheckBox jCheckBoxSaveOnCompile;
    private javax.swing.JCheckBox jCheckBoxScaleFonts;
    private javax.swing.JCheckBox jCheckBoxSuggestCaseSort;
    private javax.swing.JComboBox jComboBoxAntiAlias;
    private javax.swing.JComboBox jComboBoxCustomScripts;
    private javax.swing.JComboBox jComboBoxLineNumberAlign;
    private javax.swing.JComboBox jComboBoxPrefASM;
    private javax.swing.JComboBox jComboBoxSyntaxScheme;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelFont;
    private javax.swing.JLabel jLabelRestart;
    private javax.swing.JLabel jLabelSchemePreview;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextAreaUserScript;
    private javax.swing.JTextField jTextFieldScriptName;
    private javax.swing.JTextField jTextFieldTabWidth;
    // End of variables declaration//GEN-END:variables
    private JDialog helpBox;
    private FontChooser fontDlg;

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }
    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }
    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }
    @Override
    public void dragExit(DropTargetEvent dte) {
    }
    @Override
    public void drop(DropTargetDropEvent dtde) {
        // get transferable
        Transferable tr = dtde.getTransferable();
        try {
            // check whether we have files dropped into textarea
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                // drag&drop was link action
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                // retrieve list of dropped files
                List files = (java.util.List)tr.getTransferData(DataFlavor.javaFileListFlavor);
                // check for valid values
                if (files!=null && files.size()>0) {
                    // dummy
                    File file;
                    for (Object file1 : files) {
                        // get each single object from droplist
                        file = (File) file1;
                        // check whether it is a file
                        if (file.isFile()) {
                            // retrieve complete path as string
                            String fp = file.getAbsolutePath();
                            // check if we have white spaces- if yes, enquote
                            if (fp.contains(" ")) fp = "\""+fp+"\"";
                            // retrieve current content
                            String text = jTextAreaUserScript.getText();
                            // define output file. if we have a cruncher with "compressed file" placeholder,
                            // we assume that output file will be compressed.
                            String outputfile = (text.contains(ConstantsR64.ASSEMBLER_COMPRESSED_FILE)) ? ConstantsR64.ASSEMBLER_COMPRESSED_FILE : Assembler.OUTPUT_FILE;
                            String insert = "";
                            Assembler assembler = Assemblers.byFileName(fp);
                            if (assembler != null) {
                                insert = assembler.getDefaultCommandLine(fp);
                            }
                            else if (fp.toLowerCase().contains("exomizer")) {
                                insert = fp+" "+ConstantsR64.DEFAULT_EXOMIZER_PARAM;
                            }
                            else if (fp.toLowerCase().contains("pucrunch")) {
                                insert = fp+" "+ConstantsR64.DEFAULT_PUCRUNCH_PARAM;
                            }
                            else if (fp.toLowerCase().contains("x64")) {
                                insert = fp+" "+outputfile;
                            }
                            else if (fp.toLowerCase().contains("emu64")) {
                                insert = fp+" "+outputfile;
                            }
                            else if (fp.toLowerCase().contains("ccs64")) {
                                insert = fp+" "+outputfile;
                            }
                            // on OS X, check for Applications folder
                            if (ConstantsR64.IS_OSX && fp.toLowerCase().contains("/Applications/") && !insert.startsWith("java")) insert = "open "+insert;
                            // black color
                            jTextAreaUserScript.setForeground(Color.black);
                            // if text field is not empty and is not the default
                            // quick help text, insert new line
                            if (!text.isEmpty() && !text.equalsIgnoreCase(quickHelpText)) {
                                jTextAreaUserScript.append(System.lineSeparator());
                            }
                            else {
                                jTextAreaUserScript.setText("");
                            }
                            // insert string in textfield
                            jTextAreaUserScript.append(insert);
                        }
                    }
                }
                dtde.getDropTargetContext().dropComplete(true);
            } else {
                dtde.rejectDrop();
            }
        }
        catch (IOException | UnsupportedFlavorException ex) {
            dtde.rejectDrop();
        }
    }
}
