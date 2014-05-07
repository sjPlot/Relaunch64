/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.relaunch64.popelganda;

import de.relaunch64.popelganda.Editor.EditorPaneLineNumbers;
import de.relaunch64.popelganda.Editor.HighlightSchemes;
import de.relaunch64.popelganda.Editor.SyntaxScheme;
import de.relaunch64.popelganda.database.CustomScripts;
import de.relaunch64.popelganda.database.Settings;
import de.relaunch64.popelganda.util.ConstantsR64;
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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.application.Action;
import org.jdom2.Document;

/**
 *
 * @author danielludecke
 */
public class SettingsDlg extends javax.swing.JDialog implements DropTargetListener {
    private final Settings settings;
    private final CustomScripts scripts;
    private Font mainfont;
    private static final String quickHelpText = "Enter command lines here or drag & drop executable files\n(first compiler, then cruncher (optional), finally emulator)\nfrom explorer window to automatically generate a script.\n\nPress help-button for more details and examples.";
    /**
     * Creates new form SettingsDlg
     * @param parent
     * @param s
     * @param scr
     */
    public SettingsDlg(java.awt.Frame parent, Settings s, CustomScripts scr) {
        super(parent);
        settings = s;
        scripts = scr;
        initComponents();
        // set font-preview for change-font-label
        mainfont = settings.getMainFont();
        jLabelFont.setText(mainfont.getFontName());
        jLabelFont.setFont(mainfont);
        // restart-to-apply-text hidden
        jLabelRestart.setVisible(false);
        // set preferred compiler settings
        jComboBoxPrefComp.setSelectedIndex(settings.getPreferredCompiler());
        // init line number alignment
        if (settings.getLineNumerAlignment() == EditorPaneLineNumbers.RIGHT) jComboBoxLineNumberAlign.setSelectedIndex(0);
        else if (settings.getLineNumerAlignment() == EditorPaneLineNumbers.CENTER) jComboBoxLineNumberAlign.setSelectedIndex(1);
        else if (settings.getLineNumerAlignment() == EditorPaneLineNumbers.LEFT) jComboBoxLineNumberAlign.setSelectedIndex(2);
        // init other settings
        jCheckBoxCheckUpdates.setSelected(settings.getCheckForUpdates());
        jCheckBoxSaveOnCompile.setSelected(settings.getSaveOnCompile());
        jCheckBoxReopenFiles.setSelected(settings.getReopenOnStartup());
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
        jButtonApplyTabAndFont.setDisplayedMnemonicIndex(0);
        jButtonRemoveScript.setDisplayedMnemonicIndex(0);
        // disable apply buttons
        setModifiedTabScript(false);
        setModifiedTabFont(false);
    }
    private void initSchemes() {
        jComboBoxSyntaxScheme.removeAllItems();
        for (String sn : HighlightSchemes.SCHEME_NAMES) jComboBoxSyntaxScheme.addItem(sn);
        try {
            jComboBoxSyntaxScheme.setSelectedIndex(settings.getSyntaxScheme());
        }
        catch (IllegalArgumentException ex) {
        }
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
                settings.setCheckForUpdates(jCheckBoxCheckUpdates.isSelected());
            }
        });
        jCheckBoxSaveOnCompile.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settings.setSaveOnCompile(jCheckBoxSaveOnCompile.isSelected());
            }
        });
        jCheckBoxReopenFiles.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settings.setReopenOnStartup(jCheckBoxReopenFiles.isSelected());
            }
        });
        jComboBoxPrefComp.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModifiedTabFont(true);
            }
        });
        jComboBoxSyntaxScheme.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModifiedTabFont(true);
                jLabelRestart.setVisible(true);
            }
        });
        jComboBoxLineNumberAlign.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLabelRestart.setVisible(true);
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
            jLabelRestart.setVisible(true);
            setModifiedTabFont(true);
        }
    }
    @Action(enabledProperty = "modifiedTabFont")
    public void applyFontTab() {
        settings.setMainfont(mainfont);
        // update syntax scheme
        Document doc = SyntaxScheme.loadSyntax();
        SyntaxScheme.setFont(doc, mainfont.getFamily());
        SyntaxScheme.setFontSize(doc, mainfont.getSize());
        SyntaxScheme.changeScheme(jComboBoxSyntaxScheme.getSelectedIndex(), doc);
        SyntaxScheme.saveSyntax(doc);
        // get tab width
        try {
            settings.setTabWidth(Integer.parseInt(jTextFieldTabWidth.getText()));
        }
        catch (NumberFormatException ex) {
        }
        settings.setPreferredCompiler(jComboBoxPrefComp.getSelectedIndex());
        settings.setSyntaxScheme(jComboBoxSyntaxScheme.getSelectedIndex());
        if (jComboBoxLineNumberAlign.getSelectedIndex() == 0) settings.setLineNumerAlignment(EditorPaneLineNumbers.RIGHT);
        else if (jComboBoxLineNumberAlign.getSelectedIndex() == 1) settings.setLineNumerAlignment(EditorPaneLineNumbers.CENTER);
        else if (jComboBoxLineNumberAlign.getSelectedIndex() == 2) settings.setLineNumerAlignment(EditorPaneLineNumbers.LEFT);
        setModifiedTabFont(false);
        jLabelRestart.setVisible(false);
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
    /**
     * 
     */
    private boolean modifiedTabScript = false;
    public boolean isModifiedTabScript() {
        return modifiedTabScript;
    }
    public final void setModifiedTabScript(boolean b) {
        boolean old = isModifiedTabScript();
        this.modifiedTabScript = b;
        firePropertyChange("modifiedTabScript", old, isModifiedTabScript());
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
        jComboBoxPrefComp = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jButtonApplyTabAndFont = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxLineNumberAlign = new javax.swing.JComboBox();
        jLabelRestart = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxSyntaxScheme = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jCheckBoxCheckUpdates = new javax.swing.JCheckBox();
        jCheckBoxSaveOnCompile = new javax.swing.JCheckBox();
        jCheckBoxReopenFiles = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getContext().getResourceMap(SettingsDlg.class);
        setTitle(resourceMap.getString("FormSettings.title")); // NOI18N
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

        jComboBoxPrefComp.setModel(new javax.swing.DefaultComboBoxModel(ConstantsR64.COMPILER_NAMES));
        jComboBoxPrefComp.setName("jComboBoxPrefComp"); // NOI18N

        jLabel6.setDisplayedMnemonic('c');
        jLabel6.setLabelFor(jComboBoxPrefComp);
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

        jLabelRestart.setForeground(resourceMap.getColor("jLabelRestart.foreground")); // NOI18N
        jLabelRestart.setText(resourceMap.getString("jLabelRestart.text")); // NOI18N
        jLabelRestart.setName("jLabelRestart"); // NOI18N

        jLabel2.setDisplayedMnemonic('y');
        jLabel2.setLabelFor(jComboBoxSyntaxScheme);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jComboBoxSyntaxScheme.setName("jComboBoxSyntaxScheme"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(282, Short.MAX_VALUE)
                .add(jLabelRestart)
                .add(18, 18, 18)
                .add(jButtonApplyTabAndFont)
                .add(6, 6, 6))
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel6)
                    .add(jLabel10)
                    .add(jLabel11)
                    .add(jLabel1)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jComboBoxSyntaxScheme, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jComboBoxLineNumberAlign, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jLabelFont)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonFont))
                    .add(jTextFieldTabWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jComboBoxPrefComp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(jLabelFont)
                    .add(jButtonFont))
                .add(18, 18, 18)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(jTextFieldTabWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(jComboBoxPrefComp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jComboBoxLineNumberAlign, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jComboBoxSyntaxScheme, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 83, Short.MAX_VALUE)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonApplyTabAndFont)
                    .add(jLabelRestart))
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

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

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBoxCheckUpdates)
                    .add(jCheckBoxSaveOnCompile)
                    .add(jCheckBoxReopenFiles))
                .addContainerGap(417, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jCheckBoxCheckUpdates)
                .add(18, 18, 18)
                .add(jCheckBoxSaveOnCompile)
                .add(18, 18, 18)
                .add(jCheckBoxReopenFiles)
                .addContainerGap(212, Short.MAX_VALUE))
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
    private javax.swing.JButton jButtonApplyScript;
    private javax.swing.JButton jButtonApplyTabAndFont;
    private javax.swing.JButton jButtonFont;
    private javax.swing.JButton jButtonNewScript;
    private javax.swing.JButton jButtonRemoveScript;
    private javax.swing.JButton jButtonScriptHelp;
    private javax.swing.JCheckBox jCheckBoxCheckUpdates;
    private javax.swing.JCheckBox jCheckBoxReopenFiles;
    private javax.swing.JCheckBox jCheckBoxSaveOnCompile;
    private javax.swing.JComboBox jComboBoxCustomScripts;
    private javax.swing.JComboBox jComboBoxLineNumberAlign;
    private javax.swing.JComboBox jComboBoxPrefComp;
    private javax.swing.JComboBox jComboBoxSyntaxScheme;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelFont;
    private javax.swing.JLabel jLabelRestart;
    private javax.swing.JPanel jPanel1;
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
                            String outputfile = (text.contains(ConstantsR64.ASSEMBLER_COMPRESSED_FILE)) ? ConstantsR64.ASSEMBLER_COMPRESSED_FILE : ConstantsR64.ASSEMBLER_OUPUT_FILE;
                            String insert = "";
                            if (fp.toLowerCase().contains("acme")) {
                                insert = fp+" "+ConstantsR64.DEFAULT_ACME_PARAM;
                            }
                            else if (fp.toLowerCase().contains("kickass")) {
                                insert = "java -jar "+fp+" "+ConstantsR64.ASSEMBLER_INPUT_FILE;
                            }
                            else if (fp.toLowerCase().contains("64tass")) {
                                insert = fp+" "+ConstantsR64.DEFAULT_64TASS_PARAM;
                            }
                            else if (fp.toLowerCase().contains("exomizer")) {
                                insert = fp+" "+ConstantsR64.DEFAULT_EXOMIZER_PARAM;
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
                            if (settings.isOSX() && fp.toLowerCase().contains("/Applications/") && !insert.startsWith("java")) insert = "open "+insert;
                            // black color
                            jTextAreaUserScript.setForeground(Color.black);
                            // if text field is not empty and is not the default
                            // quick help text, insert new line
                            if (!text.isEmpty() && !text.equalsIgnoreCase(quickHelpText)) {
                                jTextAreaUserScript.append(System.getProperty("line.separator"));
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
