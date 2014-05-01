/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.relaunch64.popelganda;

import de.relaunch64.popelganda.Editor.SyntaxScheme;
import de.relaunch64.popelganda.database.CustomScripts;
import de.relaunch64.popelganda.database.Settings;
import de.relaunch64.popelganda.util.ConstantsR64;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
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
public class SettingsDlg extends javax.swing.JDialog {
    private final Settings settings;
    private final CustomScripts scripts;
    private Font mainfont;
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
        mainfont = settings.getMainFont();
        jLabelFont.setText(mainfont.getFontName());
        jLabelFont.setFont(mainfont);
        jComboBoxPrefComp.setSelectedIndex(settings.getPreferredCompiler());
        initScripts();
        initListeners();
        // get tab char
        jTextFieldTabWidth.setText(String.valueOf(settings.getTabWidth()));
        // set application icon
        setIconImage(ConstantsR64.r64icon.getImage());
        // set Mnemonic keys
        jLabel8.setDisplayedMnemonic('s');
        jLabel9.setDisplayedMnemonic('n');
        jButtonApplyScript.setDisplayedMnemonicIndex(0);
        jButtonRemoveScript.setDisplayedMnemonicIndex(0);
        // disable apply buttons
        setModifiedTabScript(false);
        setModifiedTabFont(false);
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
        }
        else {
            jTextFieldScriptName.setText("");
            jTextAreaUserScript.setText("");
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
        jComboBoxPrefComp.addActionListener(new java.awt.event.ActionListener() {
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
    }
    private void switchButtonLabel() {
        String name = jTextFieldScriptName.getText();
        String content = jTextAreaUserScript.getText();
        if (name!=null) jButtonApplyScript.setText((scripts.findScript(name)!=-1) ? "Update script" : "Add script");
        jButtonApplyScript.setDisplayedMnemonicIndex(0);
        setModifiedTabScript(name!=null && !name.isEmpty() && !content.isEmpty());
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
        SyntaxScheme.saveSyntax(doc);
        // get tab width
        try {
            settings.setTabWidth(Integer.parseInt(jTextFieldTabWidth.getText()));
        }
        catch (NumberFormatException ex) {
        }
        settings.setPreferredCompiler(jComboBoxPrefComp.getSelectedIndex());
        setModifiedTabFont(false);
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
    @Action
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
    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
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
        jPanel5 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabelFont = new javax.swing.JLabel();
        jButtonFont = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jTextFieldTabWidth = new javax.swing.JTextField();
        jComboBoxPrefComp = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jButtonApplyTabAndFont = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getContext().getResourceMap(SettingsDlg.class);
        setTitle(resourceMap.getString("FormSettings.title")); // NOI18N
        setModal(true);
        setName("FormSettings"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        jLabel8.setLabelFor(jComboBoxCustomScripts);
        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jComboBoxCustomScripts.setName("jComboBoxCustomScripts"); // NOI18N

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
                    .add(jComboBoxCustomScripts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(jTextFieldScriptName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonApplyScript)
                    .add(jButtonScriptHelp)
                    .add(jButtonRemoveScript))
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jPanel5.setName("jPanel5"); // NOI18N

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jLabelFont.setName("jLabelFont"); // NOI18N

        jButtonFont.setAction(actionMap.get("changeEditorFont")); // NOI18N
        jButtonFont.setName("jButtonFont"); // NOI18N

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        jTextFieldTabWidth.setColumns(4);
        jTextFieldTabWidth.setText(resourceMap.getString("jTextFieldTabWidth.text")); // NOI18N
        jTextFieldTabWidth.setName("jTextFieldTabWidth"); // NOI18N

        jComboBoxPrefComp.setModel(new javax.swing.DefaultComboBoxModel(ConstantsR64.COMPILER_NAMES));
        jComboBoxPrefComp.setName("jComboBoxPrefComp"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jButtonApplyTabAndFont.setAction(actionMap.get("applyFontTab")); // NOI18N
        jButtonApplyTabAndFont.setName("jButtonApplyTabAndFont"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel6)
                    .add(jLabel10)
                    .add(jLabel11))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jLabelFont)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonFont))
                    .add(jTextFieldTabWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jComboBoxPrefComp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(481, Short.MAX_VALUE)
                .add(jButtonApplyTabAndFont)
                .add(6, 6, 6))
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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 155, Short.MAX_VALUE)
                .add(jButtonApplyTabAndFont)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

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
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonApplyScript;
    private javax.swing.JButton jButtonApplyTabAndFont;
    private javax.swing.JButton jButtonFont;
    private javax.swing.JButton jButtonRemoveScript;
    private javax.swing.JButton jButtonScriptHelp;
    private javax.swing.JComboBox jComboBoxCustomScripts;
    private javax.swing.JComboBox jComboBoxPrefComp;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelFont;
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
}
