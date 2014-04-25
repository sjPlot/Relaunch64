/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.relaunch64.popelganda;

import de.relaunch64.popelganda.database.CustomScripts;
import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.database.Settings;
import de.relaunch64.popelganda.util.Tools;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import org.jdesktop.application.Action;

/**
 *
 * @author danielludecke
 */
public class SettingsDlg extends javax.swing.JDialog {
    private final Settings settings;
    private final CustomScripts scripts;
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
        java.awt.Font mf = settings.getMainFont();
        jComboBoxCompilers.setSelectedIndex(0);
        jComboBoxEmulators.setSelectedIndex(0);
        jComboBoxPrefComp.setSelectedIndex(settings.getPreferredCompiler());
        jComboBoxPrefEmu.setSelectedIndex(settings.getPreferredEmulator());
        initScripts();
        initListeners();
        // set application icon
        setIconImage(ConstantsR64.r64icon.getImage());
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
        jComboBoxCustomScripts.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // get selected script
                String scriptname = scripts.getScriptName(jComboBoxCustomScripts.getSelectedIndex());
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
//            setModifiedTabCompiler(false);
//        });
//        jComboBoxEmulators.addActionListener((java.awt.event.ActionEvent evt) -> {
//            updateEmulatorSettings();
//            setModifiedTabEmulator(false);
//        });
        jComboBoxCompilers.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateCompilerSettings();
                setModifiedTabCompiler(false);
            }
        });
        jComboBoxPrefComp.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModifiedTabCompiler(true);
            }
        });
        jComboBoxPrefEmu.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModifiedTabCompiler(true);
            }
        });
        jComboBoxEmulators.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateEmulatorSettings();
                setModifiedTabEmulator(false);
            }
        });
        jTextFieldCompilerParam.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                setModifiedTabCompiler(true);
            }
        });
        jTextFieldCompilerPath.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                setModifiedTabCompiler(true);
            }
        });
        jTextFieldEmulatorPath.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                setModifiedTabEmulator(true);
            }
        });
    }
    /**
     * 
     */
    private void updateCompilerSettings() {
        // get selectec compiler
        int selected = jComboBoxCompilers.getSelectedIndex();
        if (selected!=-1) {
            // retrieve compiler path
            File path = settings.getCompilerPath(selected);
            // update textfield
            jTextFieldCompilerPath.setText((path!=null)?path.toString():"");
            // retrieve compiler params
            String param = settings.getCompilerParameter(selected);
            // update textfield
            jTextFieldCompilerParam.setText((param!=null)?param:"");
            // reset apply button
            setModifiedTabCompiler(false);
            // set parameter information
            String parainfo = "";
            // set info depending on compiler
            switch (selected) {
                case ConstantsR64.COMPILER_KICKASSEMBLER:
                    parainfo = "No parameter required. Refer to manual for parameter list.";
                    break;
                case ConstantsR64.COMPILER_ACME:
                    parainfo = "Recommended: \""+ConstantsR64.DEFAULT_ACME_PARAM+"\"";
                    break;
                case ConstantsR64.COMPILER_64TASS:
                    parainfo = "Recommended: \""+ConstantsR64.DEFAULT_64TASS_PARAM+"\"";
                    break;
            }
            // update lable text
            jLabelParamInfo.setText(parainfo);
        }
    }
    /**
     * 
     */
    private void updateEmulatorSettings() {
        // get selectec compiler
        int selected = jComboBoxEmulators.getSelectedIndex();
        if (selected!=-1) {
            // retrieve compiler path
            File path = settings.getEmulatorPath(selected);
            // update textfield
            jTextFieldEmulatorPath.setText((path!=null)?path.toString():"");
            // reset apply button
            setModifiedTabEmulator(false);
        }
    }
    @Action
    public void showScriptHelp() {
        
    }
    /**
     * 
     */
    @Action
    public void browseCompiler() {
        JFileChooser fc = new JFileChooser();
        // set dialog's title
        fc.setDialogTitle("Choose Compiler");
        // accept all files as choosable
        fc.setAcceptAllFileFilterUsed(true);
        // only directories should be selected
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int option = fc.showOpenDialog(null);
        // if a file was chosen, set the filepath
        if (JFileChooser.APPROVE_OPTION == option) {
            // get the filepath...
            jTextFieldCompilerPath.setText(fc.getSelectedFile().toString());
            setModifiedTabCompiler(true);
        }
    }
    /**
     * 
     */
    @Action
    public void browseEmulator() {
        JFileChooser fc = new JFileChooser();
        // set dialog's title
        fc.setDialogTitle("Choose Emulator");
        // accept all files as choosable
        fc.setAcceptAllFileFilterUsed(true);
        // only directories should be selected
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int option = fc.showOpenDialog(null);
        // if a file was chosen, set the filepath
        if (JFileChooser.APPROVE_OPTION == option) {
            // get the filepath...
            jTextFieldEmulatorPath.setText(fc.getSelectedFile().toString());
            setModifiedTabEmulator(true);
        }
    }
    @Action
    public void changeEditorFont() {
        if (null == fontDlg) {
            fontDlg = new FontChooser(null, settings.getMainFont());
            fontDlg.setLocationRelativeTo(null);
        }
        Relaunch64App.getApplication().show(fontDlg);
        java.awt.Font f = fontDlg.getSelectedFont();
        if (f!=null) settings.setMainfont(f);
    }
    /**
     * 
     */
    @Action(enabledProperty = "modifiedTabCompiler")
    public void applyCompilerChanges() {
        // get selectec compiler
        int selected = jComboBoxCompilers.getSelectedIndex();
        if (selected!=-1) {
            // retrieve compiler path
            File path = new File(jTextFieldCompilerPath.getText());
            if (path.exists()) {
                settings.setCompilerPath(selected, path);
            }
            // if no valid path, check for environment
            else if (Tools.isToolInEnvironment(path.toString())) {
                settings.setCompilerPath(selected, path);
            }
            // retrieve compiler param
            String param = jTextFieldCompilerParam.getText();
            settings.setCompilerParameter(selected, param);
            // add preferred compiler
            settings.setPreferredCompiler(jComboBoxPrefComp.getSelectedIndex());
            // reset apply button
            setModifiedTabCompiler(false);
        }
    }
    /**
     * 
     */
    @Action(enabledProperty = "modifiedTabEmulator")
    public void applyEmulatorChanges() {
        // get selectec compiler
        int selected = jComboBoxEmulators.getSelectedIndex();
        if (selected!=-1) {
            // retrieve compiler path
            File path = new File(jTextFieldEmulatorPath.getText());
            if (path.exists()) {
                settings.setEmulatorPath(selected, path);
            }
            // if no valid path, check for environment
            else if (Tools.isToolInEnvironment(path.toString())) {
                settings.setEmulatorPath(selected, path);
            }
            // add preferred compiler
            settings.setPreferredEmulator(jComboBoxPrefEmu.getSelectedIndex());
            // reset apply button
            setModifiedTabEmulator(false);
        }
    }
    @Action
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
    }
    /**
     * 
     */
    @Action
    public void resetCompilerParameter() {
        // get selectec compiler
        int selected = jComboBoxCompilers.getSelectedIndex();
        if (selected!=-1) {
            jTextFieldCompilerParam.setText(settings.getDefaultCompilerParam(selected));
            // enable apply button
            setModifiedTabEmulator(true);
        }
    }
    /**
     * 
     */
    private boolean modifiedTabCompiler = false;
    public boolean isModifiedTabCompiler() {
        return modifiedTabCompiler;
    }
    public final void setModifiedTabCompiler(boolean b) {
        boolean old = isModifiedTabCompiler();
        this.modifiedTabCompiler = b;
        firePropertyChange("modifiedTabCompiler", old, isModifiedTabCompiler());
    }
    /**
     * 
     */
    private boolean modifiedTabEmulator = false;
    public boolean isModifiedTabEmulator() {
        return modifiedTabEmulator;
    }
    public final void setModifiedTabEmulator(boolean b) {
        boolean old = isModifiedTabEmulator();
        this.modifiedTabEmulator = b;
        firePropertyChange("modifiedTabEmulator", old, isModifiedTabEmulator());
    }
    
    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jComboBoxCompilers = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldCompilerPath = new javax.swing.JTextField();
        jButtonBrowseCompilerPath = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldCompilerParam = new javax.swing.JTextField();
        jButtonApplyCompiler = new javax.swing.JButton();
        jLabelParamInfo = new javax.swing.JLabel();
        jButtonReset = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jComboBoxPrefComp = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jComboBoxEmulators = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldEmulatorPath = new javax.swing.JTextField();
        jButtonBrowseEmulatorPath = new javax.swing.JButton();
        jButtonApplyEmu = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jComboBoxPrefEmu = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jComboBoxCustomScripts = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldScriptName = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaUserScript = new javax.swing.JTextArea();
        jButtonApplyScript = new javax.swing.JButton();
        jButtonScriptHelp = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getContext().getResourceMap(SettingsDlg.class);
        setTitle(resourceMap.getString("FormSettings.title")); // NOI18N
        setModal(true);
        setName("FormSettings"); // NOI18N
        setResizable(false);

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jComboBoxCompilers.setModel(new javax.swing.DefaultComboBoxModel(ConstantsR64.COMPILER_NAMES));
        jComboBoxCompilers.setName("jComboBoxCompilers"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jTextFieldCompilerPath.setName("jTextFieldCompilerPath"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getContext().getActionMap(SettingsDlg.class, this);
        jButtonBrowseCompilerPath.setAction(actionMap.get("browseCompiler")); // NOI18N
        jButtonBrowseCompilerPath.setName("jButtonBrowseCompilerPath"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jTextFieldCompilerParam.setName("jTextFieldCompilerParam"); // NOI18N

        jButtonApplyCompiler.setAction(actionMap.get("applyCompilerChanges")); // NOI18N
        jButtonApplyCompiler.setName("jButtonApplyCompiler"); // NOI18N

        jLabelParamInfo.setText(resourceMap.getString("jLabelParamInfo.text")); // NOI18N
        jLabelParamInfo.setName("jLabelParamInfo"); // NOI18N

        jButtonReset.setAction(actionMap.get("resetCompilerParameter")); // NOI18N
        jButtonReset.setName("jButtonReset"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jComboBoxPrefComp.setModel(new javax.swing.DefaultComboBoxModel(ConstantsR64.COMPILER_NAMES));
        jComboBoxPrefComp.setName("jComboBoxPrefComp"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jButtonReset)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonApplyCompiler))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(jLabel1)
                            .add(jLabel2)
                            .add(jLabel6))
                        .add(6, 6, 6)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jComboBoxCompilers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabelParamInfo)
                            .add(jComboBoxPrefComp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldCompilerParam, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 378, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jTextFieldCompilerPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 378, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButtonBrowseCompilerPath)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBoxCompilers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jTextFieldCompilerPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonBrowseCompilerPath))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(jTextFieldCompilerParam, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelParamInfo)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBoxPrefComp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 79, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonApplyCompiler)
                    .add(jButtonReset))
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jComboBoxEmulators.setModel(new javax.swing.DefaultComboBoxModel(ConstantsR64.EMU_NAMES));
        jComboBoxEmulators.setName("jComboBoxEmulators"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jTextFieldEmulatorPath.setName("jTextFieldEmulatorPath"); // NOI18N

        jButtonBrowseEmulatorPath.setAction(actionMap.get("browseEmulator")); // NOI18N
        jButtonBrowseEmulatorPath.setName("jButtonBrowseEmulatorPath"); // NOI18N

        jButtonApplyEmu.setAction(actionMap.get("applyEmulatorChanges")); // NOI18N
        jButtonApplyEmu.setName("jButtonApplyEmu"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jComboBoxPrefEmu.setModel(new javax.swing.DefaultComboBoxModel(ConstantsR64.EMU_NAMES));
        jComboBoxPrefEmu.setName("jComboBoxPrefEmu"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(jLabel5)
                    .add(jLabel7))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jComboBoxEmulators, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(jComboBoxPrefEmu, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(jButtonApplyEmu))
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(jTextFieldEmulatorPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 377, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButtonBrowseEmulatorPath)))
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(jComboBoxEmulators, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel5)
                            .add(jTextFieldEmulatorPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel7)
                            .add(jComboBoxPrefEmu, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jButtonBrowseEmulatorPath))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 133, Short.MAX_VALUE)
                .add(jButtonApplyEmu)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jComboBoxCustomScripts.setName("jComboBoxCustomScripts"); // NOI18N

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jTextFieldScriptName.setColumns(20);
        jTextFieldScriptName.setName("jTextFieldScriptName"); // NOI18N

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextAreaUserScript.setName("jTextAreaUserScript"); // NOI18N
        jScrollPane1.setViewportView(jTextAreaUserScript);

        jButtonApplyScript.setAction(actionMap.get("applyScript")); // NOI18N
        jButtonApplyScript.setName("jButtonApplyScript"); // NOI18N

        jButtonScriptHelp.setAction(actionMap.get("showScriptHelp")); // NOI18N
        jButtonScriptHelp.setName("jButtonScriptHelp"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                        .add(jButtonScriptHelp)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jButtonApplyScript))
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel8)
                            .add(jLabel9))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel3Layout.createSequentialGroup()
                                .add(jComboBoxCustomScripts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, Short.MAX_VALUE))
                            .add(jTextFieldScriptName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE))))
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
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonApplyScript)
                    .add(jButtonScriptHelp))
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApplyCompiler;
    private javax.swing.JButton jButtonApplyEmu;
    private javax.swing.JButton jButtonApplyScript;
    private javax.swing.JButton jButtonBrowseCompilerPath;
    private javax.swing.JButton jButtonBrowseEmulatorPath;
    private javax.swing.JButton jButtonReset;
    private javax.swing.JButton jButtonScriptHelp;
    private javax.swing.JComboBox jComboBoxCompilers;
    private javax.swing.JComboBox jComboBoxCustomScripts;
    private javax.swing.JComboBox jComboBoxEmulators;
    private javax.swing.JComboBox jComboBoxPrefComp;
    private javax.swing.JComboBox jComboBoxPrefEmu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelParamInfo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextAreaUserScript;
    private javax.swing.JTextField jTextFieldCompilerParam;
    private javax.swing.JTextField jTextFieldCompilerPath;
    private javax.swing.JTextField jTextFieldEmulatorPath;
    private javax.swing.JTextField jTextFieldScriptName;
    // End of variables declaration//GEN-END:variables
    private FontChooser fontDlg;
}
