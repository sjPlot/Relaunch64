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

package de.relaunch64.popelganda;

import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.EditorPaneLineNumbers;
import de.relaunch64.popelganda.util.EditorPanes;
import de.relaunch64.popelganda.util.Settings;
import de.relaunch64.popelganda.util.Tools;
import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The application's main frame.
 */
public class Relaunch64View extends FrameView implements WindowListener, DropTargetListener {
    private EditorPanes editorPanes;
    private final FindReplace findReplace;
    private final List<String> compilerParams = new ArrayList<>();
    /**
     * Initiate the settings-class. this class first of all loads some user settings
     * and e.g. the filepath of the currently used datafile, so it can be automatically opened
     */
    private final Settings settings;
    private File outputFile = null;
    private final org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class)
                                                                                                   .getContext().getResourceMap(Relaunch64View.class);
    
    public Relaunch64View(SingleFrameApplication app, Settings set) {
        super(app);
        ConstantsR64.r64logger.addHandler(new TextAreaHandler());
        // tell logger to log everthing
        ConstantsR64.r64logger.setLevel(Level.ALL);
        // init database variables
        settings = set;
        findReplace = new FindReplace();
        // init default laf
        setDefaultLookAndFeel();
        // init swing components
        initComponents();
        // hide find & replace textfield
        jPanelFind.setVisible(false);
        jPanelReplace.setVisible(false);
        // init listeners and accelerator table
        initListeners();
        initDropTargets();
        // set sys info
        jTextAreaLog.setText(Tools.getSystemInformation()+System.getProperty("line.separator"));
        // set application icon
        getFrame().setIconImage(ConstantsR64.r64icon.getImage());
        getFrame().setTitle(ConstantsR64.APPLICATION_TITLE);
        // init editorpane-dataclass
        editorPanes = new EditorPanes(jTabbedPane1, jComboBoxCompilers, this, settings);
        // init line numbers
        // init line numbers
        EditorPaneLineNumbers epln = new EditorPaneLineNumbers(jEditorPaneMain, settings);
        jScrollPaneMainEditorPane.setRowHeaderView(epln);
        // init syntax highlighting for editor pane
        editorPanes.addEditorPane(jEditorPaneMain, null, null, ConstantsR64.COMPILER_KICKASSEMBLER);
        // set input focus
        jEditorPaneMain.requestFocusInWindow();
    }
    /**
     * 
     */
    private void initListeners() {
        // add an exit-listener, which offers saving etc. on
        // exit, when we have unaved changes to the data file
        getApplication().addExitListener(new ConfirmExit());
        // add window-listener. somehow I lost the behaviour that clicking on the frame's
        // upper right cross on Windows OS, quits the application. Instead, it just makes
        // the frame disapear, but does not quit, so it looks like the application was quit
        // but asking for changes took place. So, we simply add a windows-listener additionally
        Relaunch64View.super.getFrame().addWindowListener(this);
        Relaunch64View.super.getFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // init actionlistener
        jComboBoxCompilers.addActionListener((java.awt.event.ActionEvent evt) -> {
            if (editorPanes.checkIfSyntaxChangeRequired()) {
                editorPanes.changeSyntaxScheme(jComboBoxCompilers.getSelectedIndex());
            }
        });
        jTabbedPane1.addChangeListener((javax.swing.event.ChangeEvent evt) -> {
            editorPanes.updateTabbedPane();
            // and reset find/replace values, because the content has changed and former
            // find-index-values are no longer valid
            findReplace.resetValues();
        });
        jTextFieldFind.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                // when the user presses the escape-key, hide panel
                if (KeyEvent.VK_ESCAPE==evt.getKeyCode()) {
                    findCancel();
                }
                else if (KeyEvent.VK_ENTER==evt.getKeyCode()) {
                    findNext();
                }
            }
        });
        jTextFieldReplace.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                // when the user presses the escape-key, hide panel
                if (KeyEvent.VK_ESCAPE==evt.getKeyCode()) {
                    replaceCancel();
                }
                else if (KeyEvent.VK_ENTER==evt.getKeyCode()) {
                    replaceTerm();
                }
            }
        });
        jTabbedPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.getButton()==MouseEvent.BUTTON3) {
                    closeFile();
                }
            }
        });
        jTextFieldConvDez.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                convertNumber("dez");
            }
        });
        jTextFieldConvHex.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                convertNumber("hex");
            }
        });
        jTextFieldConvBin.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                convertNumber("bin");
            }
        });
        recentDocsSubmenu.addMenuListener(new javax.swing.event.MenuListener() {
            @Override public void menuSelected(javax.swing.event.MenuEvent evt) {
                setRecentDocumentMenuItem(recent1MenuItem,1);
                setRecentDocumentMenuItem(recent2MenuItem,2);
                setRecentDocumentMenuItem(recent3MenuItem,3);
                setRecentDocumentMenuItem(recent4MenuItem,4);
                setRecentDocumentMenuItem(recent5MenuItem,5);
                setRecentDocumentMenuItem(recent6MenuItem,6);
                setRecentDocumentMenuItem(recent7MenuItem,7);
                setRecentDocumentMenuItem(recent8MenuItem,8);
            }
            @Override public void menuDeselected(javax.swing.event.MenuEvent evt) {}
            @Override public void menuCanceled(javax.swing.event.MenuEvent evt) {}
        });
        recent1MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
            File fp = settings.getRecentDoc(1);
            if (fp!=null && fp.exists()) {
                openFile(fp, settings.getRecentDocCompiler(1));
            }
        });
        recent2MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
            File fp = settings.getRecentDoc(2);
            if (fp!=null && fp.exists()) {
                openFile(fp, settings.getRecentDocCompiler(2));
            }
        });
        recent3MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
            File fp = settings.getRecentDoc(3);
            if (fp!=null && fp.exists()) {
                openFile(fp, settings.getRecentDocCompiler(3));
            }
        });
        recent4MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
            File fp = settings.getRecentDoc(4);
            if (fp!=null && fp.exists()) {
                openFile(fp, settings.getRecentDocCompiler(4));
            }
        });
        recent5MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
            File fp = settings.getRecentDoc(5);
            if (fp!=null && fp.exists()) {
                openFile(fp, settings.getRecentDocCompiler(5));
            }
        });
        recent6MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
            File fp = settings.getRecentDoc(6);
            if (fp!=null && fp.exists()) {
                openFile(fp, settings.getRecentDocCompiler(6));
            }
        });
        recent7MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
            File fp = settings.getRecentDoc(7);
            if (fp!=null && fp.exists()) {
                openFile(fp, settings.getRecentDocCompiler(7));
            }
        });
        recent8MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
            File fp = settings.getRecentDoc(8);
            if (fp!=null && fp.exists()) {
                openFile(fp, settings.getRecentDocCompiler(8));
            }
        });
    }
    /**
     * This method updates the menu-items with the recent documents
     */
    private void setRecentDocuments() {
        setRecentDocumentMenuItem(recent1MenuItem,1);
        setRecentDocumentMenuItem(recent2MenuItem,2);
        setRecentDocumentMenuItem(recent3MenuItem,3);
        setRecentDocumentMenuItem(recent4MenuItem,4);
        setRecentDocumentMenuItem(recent5MenuItem,5);
        setRecentDocumentMenuItem(recent6MenuItem,6);
        setRecentDocumentMenuItem(recent7MenuItem,7);
        setRecentDocumentMenuItem(recent8MenuItem,8);
    }
    /**
     * 
     * @param menuItem
     * @param recentDocNr 
     */
    private void setRecentDocumentMenuItem(javax.swing.JMenuItem menuItem, int recentDocNr) {
        // first, hide all menu-items
        menuItem.setVisible(false);
        // retrieve recent document
        File recDoc = settings.getRecentDoc(recentDocNr);
        // check whether we have any valid value
        if (recDoc!=null && recDoc.exists()) {
            // make menu visible, if recent document is valid
            menuItem.setVisible(true);
            // set filename as text
            menuItem.setText(Tools.getFileName(recDoc));
            menuItem.setToolTipText(recDoc.getPath());
        }
    }  
    private void convertNumber(String format) {
        String input;
        switch(format) {
            case "hex":
                input = jTextFieldConvHex.getText();
                try {
                    jTextFieldConvDez.setText(String.valueOf(Integer.parseInt(input, 16)));
                    jTextFieldConvBin.setText(Integer.toBinaryString(Integer.parseInt(input, 16)));
                }
                catch(NumberFormatException ex) {}
                break;
            case "bin":
                input = jTextFieldConvBin.getText();
                try {
                    jTextFieldConvDez.setText(String.valueOf(Integer.parseInt(input, 2)));
                    jTextFieldConvHex.setText(Integer.toHexString(Integer.parseInt(input, 2)));
                }
                catch(NumberFormatException ex) {}
                break;
            case "dez": 
            case "dec": 
                input = jTextFieldConvDez.getText();
                try {
                    jTextFieldConvHex.setText(Integer.toHexString(Integer.parseInt(input)));
                    jTextFieldConvBin.setText(Integer.toBinaryString(Integer.parseInt(input)));
                }
                catch(NumberFormatException ex) {}
                break;
        }
    }
    /**
     * 
     */
    private void initDropTargets() {
        jEditorPaneMain.setDragEnabled(true);
        DropTarget dropTarget = new DropTarget(jEditorPaneMain, this);   
    }
    public void autoConvertNumbers(String selection) {
        // check for valid selection
        if (selection!=null && !selection.trim().isEmpty()) {
            // trim
            selection = selection.trim();
            // init finders
            boolean isHex = false;
            boolean isDez = false;
            boolean isBin = false;
            // look for prefixes
            if (selection.startsWith("#$")) {
                selection = selection.substring(2);
                isHex = selection.matches("^[0-9A-Fa-f]+$");
            }
            else if (selection.startsWith("$")) {
                selection = selection.substring(1);
                isHex = selection.matches("^[0-9A-Fa-f]+$");
            }
            else if (selection.startsWith("#%")) {
                selection = selection.substring(2);
                isBin = selection.matches("[0-1]+");
            }
            else if (selection.startsWith("%")) {
                selection = selection.substring(1);
                isBin = selection.matches("[0-1]+");
            }
            else if (selection.startsWith("#")) {
                selection = selection.substring(1);
                isDez = selection.matches("[0-9]+");
            }
            else if (selection.matches("[0-9]+")) {
                isDez = true;
            }
            else {
                isHex = selection.matches("^[0-9A-Fa-f]+$");
            }
            // is hex?
            if (isHex) {
                jTextFieldConvHex.setText(selection);
                convertNumber("hex");
            }
            else if (isDez) {
                jTextFieldConvDez.setText(selection);
                convertNumber("dez");
            }
            else if (isBin) {
                jTextFieldConvBin.setText(selection);
                convertNumber("bin");
            }
        }
    }
    /**
     * 
     */
    @Action
    public void settingsWindow() {
        if (null == settingsDlg) {
            settingsDlg = new SettingsDlg(getFrame(),settings);
            settingsDlg.setLocationRelativeTo(getFrame());
        }
        Relaunch64App.getApplication().show(settingsDlg);
        // save the settings
        saveSettings();
    }
    @Action
    public void undoAction() {
        editorPanes.undo();
    }
    @Action
    public void redoAction() {
        editorPanes.redo();
    }
    @Action
    public void gotoLine() {
    }
    /**
     * 
     */
    @Action
    public void addNewTab() {
        editorPanes.addNewTab(null, null, "untitled", jComboBoxCompilers.getSelectedIndex());
    }
    @Action
    public void openFile() {
        File fileToOpen = Tools.chooseFile(getFrame(), JFileChooser.OPEN_DIALOG, JFileChooser.FILES_ONLY, "", "", "Open ASM File", ConstantsR64.FILE_EXTENSIONS, "ASM-Files");
        openFile(fileToOpen);
    }
    private void openFile(File fileToOpen) {
        openFile(fileToOpen, jComboBoxCompilers.getSelectedIndex());
    }
    private void openFile(File fileToOpen, int compiler) {
        editorPanes.loadFile(fileToOpen, compiler);        
        // add file path to recent documents history
        settings.addToRecentDocs(fileToOpen.toString(), compiler);
        // and update menus
        setRecentDocuments();
    }
    @Action
    public void saveFile() {
        editorPanes.saveFile();
    }
    @Action
    public void closeFile() {
        if (editorPanes.closeFile()) {
            // check how many tabs are still open
            if (jTabbedPane1.getTabCount()>0) {
                // retrieve selected file
                int selectedTab = jTabbedPane1.getSelectedIndex();
                try {
                    // close tab
                    jTabbedPane1.remove(selectedTab);
                }
                catch (IndexOutOfBoundsException ex) {
                    ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
                }
            }
            if (jTabbedPane1.getTabCount()<1) {
                // init editorpane-dataclass
                editorPanes = new EditorPanes(jTabbedPane1, jComboBoxCompilers, this, settings);
                // init syntax highlighting for editor pane
                editorPanes.addNewTab(null, null, "untitled", ConstantsR64.COMPILER_KICKASSEMBLER);
                // set input focus
                jEditorPaneMain.requestFocusInWindow();
            }
        }
    }
    @Action
    public void closeAll() {
        // get current amount of tabes
        int count = jTabbedPane1.getTabCount();
        // and close file as often as we have tabs
        for (int cnt=0; cnt<count; cnt++) {
            closeFile();
        }
    }
    @Action
    public void saveFileAs() {
        editorPanes.saveFileAs();
    }
    @Action
    public void runFile() {
        // first, compile file
        compileFile();
        if (outputFile!=null && outputFile.exists()) {
            // get path to emulator
            // TODO parameter anpassen
            File emuPath = settings.getEmulatorPath(0);
            // check for valid value
            if (emuPath!=null && emuPath.exists()) {
                try {
                    ProcessBuilder pb;
                    Process p;
                    // Start ProcessBuilder
                    if (settings.isWindows()) {
                        pb = new ProcessBuilder(emuPath.toString(), outputFile.toString());
                        pb = pb.directory(emuPath.getParentFile());
                        pb = pb.redirectInput(Redirect.PIPE).redirectError(Redirect.PIPE);
                        // start process
                        p = pb.start();
                    }
                    // ProcessBuilder throws Permission Denied on Unix, so we use runtime instead
                    else {
                        p = Runtime.getRuntime().exec(new String[] {"open", emuPath.toString(), outputFile.toString()});
                    }
                    // write output to text area
                    // create scanner to receive compiler messages
                    try (Scanner sc = new Scanner(p.getInputStream()).useDelimiter(System.getProperty("line.separator"))) {
                        // write output to text area
                        while (sc.hasNextLine()) {
                            jTextAreaCompilerOutput.append(System.getProperty("line.separator")+sc.nextLine());
                        }
                    }
                    try (Scanner sc = new Scanner(p.getErrorStream()).useDelimiter(System.getProperty("line.separator"))) {
                        // write output to text area
                        while (sc.hasNextLine()) {
                            jTextAreaCompilerOutput.append(System.getProperty("line.separator")+sc.nextLine());
                        }
                    }
                    // finally, append new line
                    jTextAreaCompilerOutput.append(System.getProperty("line.separator"));
                    // wait for other process to be finished
                    p.waitFor();
                    p.destroy();
                }
                catch (IOException | InterruptedException ex) {
                    ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
                }
            }
            else {
                ConstantsR64.r64logger.log(Level.WARNING,"No filepath to emulator specified! Could not run the compiled file in an emulator.");
            }
        }
    }
    @Action
    public void compileFile() {
        // update compiler params
        updateCompilerParams();
        // reste outfile
        outputFile = null;
        // get current compiler
        int compiler = editorPanes.getActiveCompiler();
        // get path to compiler
        File compPath = settings.getCompilerPath(compiler);
        // check for valid value
        if (compPath!=null && compPath.exists()) {
            // get assemble file
            File afile = editorPanes.getActiveFilePath();
            // check whether it exists
            if (afile!=null && afile.exists()) {
                // retrieve on-the-fly-parameter
                Object selectedItem = jComboBoxParam.getSelectedItem();
                String[] params;
                if (selectedItem!=null && !selectedItem.toString().isEmpty()) {
                    // retrieve on-the-fly-parameter
                    params = selectedItem.toString().split(" ");
                }
                else {
                    // retrieve default parameter
                    params = settings.getCompilerParameter(compiler).split(" ");
                }
                // convert file object to string
                String path = compPath.toString();
                try {
                    // create argument list
                    List<String> args = new ArrayList<>();
                    // compiler-path is first argument
                    args.add(path);
                    // iterate parameter parts
                    for (String param : params) {
                        param = param.trim();
                        if (param.equals(ConstantsR64.ASSEMBLER_INPUT_FILE)) {
                            String infile = (ConstantsR64.COMPILER_ACME==compiler) ? afile.getName() : afile.toString();
                            param = param.replace(ConstantsR64.ASSEMBLER_INPUT_FILE, infile);
                        }
                        if (param.equals(ConstantsR64.ASSEMBLER_OUPUT_FILE)) {
                            // output file
                            String outfile = afile.getParentFile().toString()+File.separator+Tools.getFileName(afile)+".prg";
                            param = param.replace(ConstantsR64.ASSEMBLER_OUPUT_FILE, outfile);
                        }
                        if (!param.isEmpty()) args.add(param);
                    }
                    // use parameters according to the compiler
                    switch (compiler) {
                        // **************************************
                        // Preparing Parameters for Compiler Kickassembler
                        // **************************************
                        case ConstantsR64.COMPILER_KICKASSEMBLER:
                            if (!args.contains(afile.toString())) args.add(afile.toString());
                            args.add(0, "java");
                            args.add(1, "-jar");
                            break;
                        // **************************************
                        // Preparing Parameters for Compiler ACME
                        // **************************************
                        case ConstantsR64.COMPILER_ACME:
                            // retrieve source and check for "!to" command
                            String source = editorPanes.getActiveSourceCode();
                            // find !to
                            if (source!=null && source.contains("!to")) {
                                // if we have a !to command, we don't use the --outfile parameter
                                int poff = args.indexOf("--outfile");
                                if (poff!=-1) {
                                    try {
                                        args.remove(poff+1);
                                        args.remove(poff);
                                    }
                                    catch(IndexOutOfBoundsException | UnsupportedOperationException ex) {
                                        ConstantsR64.r64logger.log(Level.SEVERE, ex.getLocalizedMessage());
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    StringBuilder sb = new StringBuilder("");
                    args.stream().forEach((arg) -> {
                        sb.append(System.getProperty("line.separator")).append(arg);
                    });
                    // log compiler paramater
                    String log = "Compiler-Parameter: "+sb.toString();
                    ConstantsR64.r64logger.log(Level.INFO, log);
                    // *************************************************
                    // start new process builder
                    // *************************************************
                    ProcessBuilder pb = new ProcessBuilder(args);
                    // if we have ACME, set different working directory
                    String wd = (ConstantsR64.COMPILER_ACME==compiler) ?
                            afile.getParentFile().toString() :
                            compPath.getParentFile().toString();
                    // ste process working dir
                    pb = pb.directory(new File(wd));
                    pb = pb.redirectInput(Redirect.PIPE).redirectError(Redirect.PIPE);
                    // log working directory
                    log = "Working-Directory: "+pb.directory().getAbsolutePath();
                    ConstantsR64.r64logger.log(Level.INFO, log);
                    // start process
                    Process p = pb.start();
                    // write output to text area
                    // create scanner to receive compiler messages
                    try (Scanner sc = new Scanner(p.getInputStream()).useDelimiter(System.getProperty("line.separator"))) {
                        // write output to text area
                        while (sc.hasNextLine()) {
                            jTextAreaCompilerOutput.append(System.getProperty("line.separator")+sc.nextLine());
                        }
                    }
                    // write output to text area
                    // create scanner to receive compiler messages
                    try (Scanner sc = new Scanner(p.getErrorStream()).useDelimiter(System.getProperty("line.separator"))) {
                        // write output to text area
                        while (sc.hasNextLine()) {
                            jTextAreaCompilerOutput.append(System.getProperty("line.separator")+sc.nextLine());
                        }
                    }
                    // finally, append new line
                    jTextAreaCompilerOutput.append(System.getProperty("line.separator"));
                    // wait for other process to be finished
                    p.waitFor();
                    p.destroy();
                    // specifiy output file
                    outputFile = Tools.setFileExtension(afile, "prg");
                    // check if exists
                    if (!outputFile.exists()) outputFile = null;
                }
                catch (IOException | InterruptedException ex) {
                    ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
                }
            }
            else {
                ConstantsR64.r64logger.log(Level.WARNING,"No filepath to source file specified! Could not compile file.");
            }
        }
        else {
            ConstantsR64.r64logger.log(Level.WARNING,"No filepath to compiler specified! Could not compile file.");
        }
        // say that we've finished
        ConstantsR64.r64logger.log(Level.INFO, "*** Compiling finished ***");
        // finally, append new line
        jTextAreaLog.append(System.getProperty("line.separator"));
    }
    private void updateCompilerParams() {
        // get selected item
        Object selectedItem = jComboBoxParam.getEditor().getItem();
        if (selectedItem!=null) {
            // get current text from compiler params
            String text = selectedItem.toString();
            // check for valid value and if text does not already is in list
            if (text!=null && !text.isEmpty() && !compilerParams.contains(text)) compilerParams.add(text);
            // check for length
            if (compilerParams.size()>10) compilerParams.remove(0);
            // update combo box
            jComboBoxParam.removeAllItems();
            compilerParams.stream().forEach((para) -> {
                jComboBoxParam.addItem(para);
            });
            // set selection
            jComboBoxParam.setSelectedItem(text);
            jComboBoxParam.setMaximumRowCount(jComboBoxParam.getItemCount());
        }
    }
    @Action
    public void clearLog1() {
        // set sys info
        jTextAreaLog.setText(Tools.getSystemInformation()+System.getProperty("line.separator"));
    }
    @Action
    public void clearLog2() {
        // set sys info
        jTextAreaCompilerOutput.setText("");
    }
    @Action
    public void selectAllText() {
    }
    @Action
    public void findStart() {
        // check whether textfield is visible
        if (!jPanelFind.isVisible()) {
            // make it visible
            jPanelFind.setVisible(true);
        }
        jTextFieldFind.requestFocusInWindow();
    }
    @Action
    public void findNext() {
        findReplace.initValues(jTextFieldFind.getText(), jTextFieldReplace.getText(), jTabbedPane1.getSelectedIndex(), editorPanes.getActiveEditorPane());
        jTextFieldFind.setForeground(findReplace.findNext() ? Color.black : Color.red);
    }
    @Action
    public void findPrev() {
        findReplace.initValues(jTextFieldFind.getText(), jTextFieldReplace.getText(), jTabbedPane1.getSelectedIndex(), editorPanes.getActiveEditorPane());
        jTextFieldFind.setForeground(findReplace.findPrev() ? Color.black : Color.red);
    }
    private void findCancel() {
        // reset values
        findReplace.resetValues();
        jTextFieldFind.setForeground(Color.black);
        // make it visible
        jPanelFind.setVisible(false);
        // set input focus in main textfield
        editorPanes.setFocus();
    }
    @Action
    public void replaceTerm() {
        // check whether textfield is visible
        if (!jPanelReplace.isVisible()) {
            // make it visible
            jPanelReplace.setVisible(true);
            // and set input focus in it
            jTextFieldReplace.requestFocusInWindow();
        }
        // if textfield is already visible, replace term
        else {
            findReplace.initValues(jTextFieldFind.getText(), jTextFieldReplace.getText(), jTabbedPane1.getSelectedIndex(), editorPanes.getActiveEditorPane());
            jTextFieldReplace.setForeground(findReplace.replace() ? Color.black : Color.red);
        }
    }
    private void replaceCancel() {
        jTextFieldReplace.setForeground(Color.black);
        // hide replace textfield
        jPanelReplace.setVisible(false);
        // set input focus in main textfield
        editorPanes.setFocus();
    }
    /**
     * 
     */
    private void setDefaultLookAndFeel() {
        // retrieve all installed Look and Feels
        UIManager.LookAndFeelInfo[] installed_laf = UIManager.getInstalledLookAndFeels();
        // init found-variables
        boolean laf_aqua_found = false;
        boolean laf_nimbus_found = false;
        String aquaclassname = "";
        String nimbusclassname = "";
        String lafclassname;
        // in case we find "nimbus" LAF, set this as default on non-mac-os
        // because it simply looks the best.
        for (UIManager.LookAndFeelInfo laf : installed_laf) {
            // check whether laf is mac os x
            if (laf.getName().equalsIgnoreCase("mac os x") || laf.getClassName().contains("Aqua")) {
                laf_aqua_found = true;
                aquaclassname = laf.getClassName();
            }
            // check whether laf is nimbus
            if (laf.getName().equalsIgnoreCase("nimbus") || laf.getClassName().contains("Nimbus")) {
                laf_nimbus_found = true;
                nimbusclassname = laf.getClassName();
            }
        }
        // check which laf was found and set appropriate default value 
        if (laf_nimbus_found) {
            lafclassname = nimbusclassname;
        }
        else if (laf_aqua_found) {
            lafclassname = aquaclassname;
        }
        else {
            lafclassname = UIManager.getSystemLookAndFeelClassName();
        }
        try {
            // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel(lafclassname);
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
        }
    }
    /**
     * 
     * @return 
     */
    private boolean saveDocument() {
        return editorPanes.saveFile();
    }
    /**
     * 
     */
    private void saveSettings() {
        settings.saveSettings();
    }
    /**
     * 
     */
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = Relaunch64App.getApplication().getMainFrame();
            aboutBox = new Relaunch64AboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        Relaunch64App.getApplication().show(aboutBox);
    }

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
        // 
        boolean validDropLocation = false;
        // get transferable
        Transferable tr = dtde.getTransferable();
        try {
            // check whether we have files dropped into textarea
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                // drag&drop was link action
                dtde.acceptDrop(DnDConstants.ACTION_LINK);
                // retrieve drop component
                Component c = dtde.getDropTargetContext().getDropTarget().getComponent();
                // check for valid value
                if (c!=null) {
                    // retrieve component's name
                    String name = c.getName();
                    // check for valid value
                    if (name!=null && !name.isEmpty()) {
                        // check if files were dropped in entry field
                        // in this case, image files will we inserted into
                        // the entry, not attached as attachments
                        if (name.equalsIgnoreCase("jEditorPaneMain")) {
                            validDropLocation = true;
                        }
                        else {
                            ConstantsR64.r64logger.log(Level.WARNING,"No valid drop location, drop rejected");
                            dtde.rejectDrop();
                        }
                    }
                }
                // retrieve list of dropped files
                java.util.List files = (java.util.List)tr.getTransferData(DataFlavor.javaFileListFlavor);
                // check for valid values
                if (files!=null && files.size()>0) {
                    // create list with final image files
                    List<File> anyfiles = new ArrayList<>();
                    // dummy
                    File file;
                    for (Object file1 : files) {
                        // get each single object from droplist
                        file = (File) file1;
                        // check whether it is a file
                        if (file.isFile()) {
                            // if it's an image, add it to image file list
                            if (Tools.hasValidFileExtension(file) && validDropLocation) {
                                // if so, add it to list
                                anyfiles.add(file);
                            }
                        }
                    }
                    // check if we have any valid values,
                    // i.e. any files have been dragged and dropped
                    // if so, insert attachments
                    if (anyfiles.size()>0) {
                        anyfiles.stream().forEach((f) -> {
                            openFile(f);
                        });
                    }
                }
                dtde.getDropTargetContext().dropComplete(true);
            } else {
                ConstantsR64.r64logger.log(Level.WARNING,"DataFlavor.javaFileListFlavor is not supported, drop rejected");
                dtde.rejectDrop();
            }
        }
        catch (IOException | UnsupportedFlavorException ex) {
            ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
            dtde.rejectDrop();
        }
    }
    /**
     * This is the Exit-Listener. Here we put in all the things which should be done
     * before closing the window and exiting the program 
     */
    private class ConfirmExit implements Application.ExitListener {
        @Override
        public boolean canExit(EventObject e) {
            // save the settings
            saveSettings();
            // return true to say "yes, we can", or false if exiting should be cancelled
            return askForSaveChanges();
        }
        @Override
        public void willExit(EventObject e) {
        }
    }
    @Override
    public void windowOpened(WindowEvent e) {
    }
    @Override
    public void windowClosing(WindowEvent e) {
        // call the general exit-handler from the desktop-application-api
        // here we do all the stuff we need when exiting the application
        Relaunch64App.getApplication().exit();
    }
    @Override
    public void windowClosed(WindowEvent e) {
    }
    @Override
    public void windowIconified(WindowEvent e) {
    }
    @Override
    public void windowDeiconified(WindowEvent e) {
    }
    @Override
    public void windowActivated(WindowEvent e) {
    }
    @Override
    public void windowDeactivated(WindowEvent e) {
    }
    /**
     * This method checks whether there are unsaved changes in the data-files (maindata, bookmarks,
     * searchrequests, desktop-data...) and prepares a msg to save these changes. Usually, this
     * method is called when there are modifications in one of the above mentioned datafiles, and
     * a new data-file is to be imported or opened, or when the application
     * is about to quit.
     *
     * @param title the title of the message box, e.g. if the changes should be saved because the user
     * wants to quit the application of to open another data file
     * @return <i>true</i> if the changes have been successfully saved or if the user did not want to save anything, and
     * the program can go on. <i>false</i> if the user cancelled the dialog and the program should <i>not</i> go on
     * or not quit.
     */
    private boolean askForSaveChanges() {
        // check whether we have any changes at all
//        while(editorPanes.getActiveEditorPane()!=null) {
//            boolean success = editorPanes.closeFile();
//            if (!success) return false;
//        }
        // no changes, so everything is ok
        return true;
    }
    /**
     * 
     */
    public class TextAreaHandler extends java.util.logging.Handler {
        @Override
        public void publish(final LogRecord record) {
            SwingUtilities.invokeLater(() -> {
                StringWriter text = new StringWriter();
                PrintWriter out = new PrintWriter(text);
                out.println(jTextAreaLog.getText());
                // get source class name
                String scn = record.getSourceClassName();
                int p = scn.lastIndexOf(".");
                if (p>0) {
                    scn = scn.substring(p+1);
                }
                out.printf("%s.%s"+System.getProperty("line.separator")+"[%s] %s"+System.getProperty("line.separator"), scn ,record.getSourceMethodName(), record.getLevel(), record.getMessage());
                jTextAreaLog.setText(text.toString());
            });
        }

        public JTextArea getTextArea() {
            return jTextAreaLog;
        }
        @Override
        public void flush() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public void close() throws SecurityException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPaneMainEditorPane = new javax.swing.JScrollPane();
        jEditorPaneMain = new javax.swing.JEditorPane();
        jPanelFind = new javax.swing.JPanel();
        jTextFieldFind = new javax.swing.JTextField();
        jButtonFindPrev = new javax.swing.JButton();
        jButtonFindNext = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jPanelReplace = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldReplace = new javax.swing.JTextField();
        jButtonReplace = new javax.swing.JButton();
        jComboBoxParam = new javax.swing.JComboBox();
        jButtonRun = new javax.swing.JButton();
        jComboBoxCompilers = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaLog = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jButtonCarLog1 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextAreaCompilerOutput = new javax.swing.JTextArea();
        jButtonClearLog2 = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        addTabMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        openFileMenuItem = new javax.swing.JMenuItem();
        recentDocsSubmenu = new javax.swing.JMenu();
        recent1MenuItem = new javax.swing.JMenuItem();
        recent2MenuItem = new javax.swing.JMenuItem();
        recent3MenuItem = new javax.swing.JMenuItem();
        recent4MenuItem = new javax.swing.JMenuItem();
        recent5MenuItem = new javax.swing.JMenuItem();
        recent6MenuItem = new javax.swing.JMenuItem();
        recent7MenuItem = new javax.swing.JMenuItem();
        recent8MenuItem = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        closeFileMenuItem = new javax.swing.JMenuItem();
        closeAllMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        selectAllMenuItem = new javax.swing.JMenuItem();
        findMenu = new javax.swing.JMenu();
        findStartMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        findPrevMenuItem = new javax.swing.JMenuItem();
        findNextMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        replaceMenuItem = new javax.swing.JMenuItem();
        sourceMenu = new javax.swing.JMenu();
        runDefaultMenuItem = new javax.swing.JMenuItem();
        compileMenuItem = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        gotoLineMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        settingsMenuItem = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldConvDez = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldConvHex = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldConvBin = new javax.swing.JTextField();

        mainPanel.setName("mainPanel"); // NOI18N

        jSplitPane1.setDividerLocation(550);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jScrollPaneMainEditorPane.setName("jScrollPaneMainEditorPane"); // NOI18N

        jEditorPaneMain.setName("jEditorPaneMain"); // NOI18N
        jScrollPaneMainEditorPane.setViewportView(jEditorPaneMain);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getContext().getResourceMap(Relaunch64View.class);
        jTabbedPane1.addTab(resourceMap.getString("jScrollPaneMainEditorPane.TabConstraints.tabTitle"), jScrollPaneMainEditorPane); // NOI18N

        jPanelFind.setName("jPanelFind"); // NOI18N

        jTextFieldFind.setToolTipText(resourceMap.getString("jTextFieldFind.toolTipText")); // NOI18N
        jTextFieldFind.setName("jTextFieldFind"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getContext().getActionMap(Relaunch64View.class, this);
        jButtonFindPrev.setAction(actionMap.get("findPrev")); // NOI18N
        jButtonFindPrev.setIcon(resourceMap.getIcon("jButtonFindPrev.icon")); // NOI18N
        jButtonFindPrev.setText(resourceMap.getString("jButtonFindPrev.text")); // NOI18N
        jButtonFindPrev.setName("jButtonFindPrev"); // NOI18N

        jButtonFindNext.setAction(actionMap.get("findNext")); // NOI18N
        jButtonFindNext.setIcon(resourceMap.getIcon("jButtonFindNext.icon")); // NOI18N
        jButtonFindNext.setText(resourceMap.getString("jButtonFindNext.text")); // NOI18N
        jButtonFindNext.setName("jButtonFindNext"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanelFindLayout = new org.jdesktop.layout.GroupLayout(jPanelFind);
        jPanelFind.setLayout(jPanelFindLayout);
        jPanelFindLayout.setHorizontalGroup(
            jPanelFindLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelFindLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldFind)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButtonFindPrev)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButtonFindNext)
                .addContainerGap())
        );
        jPanelFindLayout.setVerticalGroup(
            jPanelFindLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelFindLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                .add(jLabel5)
                .add(jTextFieldFind, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jButtonFindPrev)
                .add(jButtonFindNext))
        );

        jPanelReplace.setName("jPanelReplace"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jTextFieldReplace.setToolTipText(resourceMap.getString("jTextFieldReplace.toolTipText")); // NOI18N
        jTextFieldReplace.setName("jTextFieldReplace"); // NOI18N

        jButtonReplace.setAction(actionMap.get("replaceTerm")); // NOI18N
        jButtonReplace.setIcon(resourceMap.getIcon("jButtonReplace.icon")); // NOI18N
        jButtonReplace.setText(resourceMap.getString("jButtonReplace.text")); // NOI18N
        jButtonReplace.setName("jButtonReplace"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanelReplaceLayout = new org.jdesktop.layout.GroupLayout(jPanelReplace);
        jPanelReplace.setLayout(jPanelReplaceLayout);
        jPanelReplaceLayout.setHorizontalGroup(
            jPanelReplaceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelReplaceLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldReplace)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButtonReplace)
                .addContainerGap())
        );
        jPanelReplaceLayout.setVerticalGroup(
            jPanelReplaceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelReplaceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                .add(jTextFieldReplace, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jLabel4)
                .add(jButtonReplace))
        );

        jComboBoxParam.setEditable(true);
        jComboBoxParam.setName("jComboBoxParam"); // NOI18N

        jButtonRun.setAction(actionMap.get("runFile")); // NOI18N
        jButtonRun.setName("jButtonRun"); // NOI18N

        jComboBoxCompilers.setModel(new javax.swing.DefaultComboBoxModel(ConstantsR64.COMPILER_NAMES));
        jComboBoxCompilers.setName("jComboBoxCompilers"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE)
            .add(jPanelFind, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanelReplace, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jComboBoxCompilers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jComboBoxParam, 0, 403, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButtonRun))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBoxCompilers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1)
                    .add(jButtonRun)
                    .add(jComboBoxParam, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelFind, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelReplace, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel2.setName("jPanel2"); // NOI18N

        jSplitPane2.setDividerLocation(250);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setName("jSplitPane2"); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTextAreaLog.setEditable(false);
        jTextAreaLog.setWrapStyleWord(true);
        jTextAreaLog.setName("jTextAreaLog"); // NOI18N
        jScrollPane2.setViewportView(jTextAreaLog);

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jButtonCarLog1.setAction(actionMap.get("clearLog1")); // NOI18N
        jButtonCarLog1.setName("jButtonCarLog1"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane2)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 90, Short.MAX_VALUE)
                .add(jButtonCarLog1))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jButtonCarLog1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
        );

        jSplitPane2.setTopComponent(jPanel3);

        jPanel4.setName("jPanel4"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jTextAreaCompilerOutput.setEditable(false);
        jTextAreaCompilerOutput.setName("jTextAreaCompilerOutput"); // NOI18N
        jScrollPane3.setViewportView(jTextAreaCompilerOutput);

        jButtonClearLog2.setAction(actionMap.get("clearLog2")); // NOI18N
        jButtonClearLog2.setName("jButtonClearLog2"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jButtonClearLog2))
            .add(jScrollPane3)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jButtonClearLog2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE))
        );

        jSplitPane2.setRightComponent(jPanel4);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSplitPane2)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane2)
        );

        jSplitPane1.setRightComponent(jPanel2);

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1)
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        addTabMenuItem.setAction(actionMap.get("addNewTab")); // NOI18N
        addTabMenuItem.setName("addTabMenuItem"); // NOI18N
        fileMenu.add(addTabMenuItem);

        jSeparator2.setName("jSeparator2"); // NOI18N
        fileMenu.add(jSeparator2);

        openFileMenuItem.setAction(actionMap.get("openFile")); // NOI18N
        openFileMenuItem.setName("openFileMenuItem"); // NOI18N
        fileMenu.add(openFileMenuItem);

        recentDocsSubmenu.setText(resourceMap.getString("recentDocsSubmenu.text")); // NOI18N
        recentDocsSubmenu.setName("recentDocsSubmenu"); // NOI18N

        recent1MenuItem.setName("recent1MenuItem"); // NOI18N
        recentDocsSubmenu.add(recent1MenuItem);

        recent2MenuItem.setName("recent2MenuItem"); // NOI18N
        recentDocsSubmenu.add(recent2MenuItem);

        recent3MenuItem.setName("recent3MenuItem"); // NOI18N
        recentDocsSubmenu.add(recent3MenuItem);

        recent4MenuItem.setName("recent4MenuItem"); // NOI18N
        recentDocsSubmenu.add(recent4MenuItem);

        recent5MenuItem.setName("recent5MenuItem"); // NOI18N
        recentDocsSubmenu.add(recent5MenuItem);

        recent6MenuItem.setName("recent6MenuItem"); // NOI18N
        recentDocsSubmenu.add(recent6MenuItem);

        recent7MenuItem.setName("recent7MenuItem"); // NOI18N
        recentDocsSubmenu.add(recent7MenuItem);

        recent8MenuItem.setName("recent8MenuItem"); // NOI18N
        recentDocsSubmenu.add(recent8MenuItem);

        fileMenu.add(recentDocsSubmenu);

        jSeparator7.setName("jSeparator7"); // NOI18N
        fileMenu.add(jSeparator7);

        saveMenuItem.setAction(actionMap.get("saveFile")); // NOI18N
        saveMenuItem.setName("saveMenuItem"); // NOI18N
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setAction(actionMap.get("saveFileAs")); // NOI18N
        saveAsMenuItem.setName("saveAsMenuItem"); // NOI18N
        fileMenu.add(saveAsMenuItem);

        jSeparator1.setName("jSeparator1"); // NOI18N
        fileMenu.add(jSeparator1);

        closeFileMenuItem.setAction(actionMap.get("closeFile")); // NOI18N
        closeFileMenuItem.setName("closeFileMenuItem"); // NOI18N
        fileMenu.add(closeFileMenuItem);

        closeAllMenuItem.setAction(actionMap.get("closeAll")); // NOI18N
        closeAllMenuItem.setName("closeAllMenuItem"); // NOI18N
        fileMenu.add(closeAllMenuItem);

        jSeparator6.setName("jSeparator6"); // NOI18N
        fileMenu.add(jSeparator6);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText(resourceMap.getString("editMenu.text")); // NOI18N
        editMenu.setName("editMenu"); // NOI18N

        undoMenuItem.setAction(actionMap.get("undoAction")); // NOI18N
        undoMenuItem.setName("undoMenuItem"); // NOI18N
        editMenu.add(undoMenuItem);

        redoMenuItem.setAction(actionMap.get("redoAction")); // NOI18N
        redoMenuItem.setName("redoMenuItem"); // NOI18N
        editMenu.add(redoMenuItem);

        jSeparator10.setName("jSeparator10"); // NOI18N
        editMenu.add(jSeparator10);

        cutMenuItem.setAction(actionMap.get("cut"));
        cutMenuItem.setName("cutMenuItem"); // NOI18N
        editMenu.add(cutMenuItem);

        copyMenuItem.setAction(actionMap.get("copy"));
        copyMenuItem.setName("copyMenuItem"); // NOI18N
        editMenu.add(copyMenuItem);

        pasteMenuItem.setAction(actionMap.get("paste"));
        pasteMenuItem.setName("pasteMenuItem"); // NOI18N
        editMenu.add(pasteMenuItem);

        jSeparator5.setName("jSeparator5"); // NOI18N
        editMenu.add(jSeparator5);

        selectAllMenuItem.setAction(actionMap.get("selectAllText")); // NOI18N
        selectAllMenuItem.setName("selectAllMenuItem"); // NOI18N
        editMenu.add(selectAllMenuItem);

        menuBar.add(editMenu);

        findMenu.setText(resourceMap.getString("findMenu.text")); // NOI18N
        findMenu.setName("findMenu"); // NOI18N

        findStartMenuItem.setAction(actionMap.get("findStart")); // NOI18N
        findStartMenuItem.setName("findStartMenuItem"); // NOI18N
        findMenu.add(findStartMenuItem);

        jSeparator3.setName("jSeparator3"); // NOI18N
        findMenu.add(jSeparator3);

        findPrevMenuItem.setAction(actionMap.get("findPrev")); // NOI18N
        findPrevMenuItem.setName("findPrevMenuItem"); // NOI18N
        findMenu.add(findPrevMenuItem);

        findNextMenuItem.setAction(actionMap.get("findNext")); // NOI18N
        findNextMenuItem.setName("findNextMenuItem"); // NOI18N
        findMenu.add(findNextMenuItem);

        jSeparator4.setName("jSeparator4"); // NOI18N
        findMenu.add(jSeparator4);

        replaceMenuItem.setAction(actionMap.get("replaceTerm")); // NOI18N
        replaceMenuItem.setName("replaceMenuItem"); // NOI18N
        findMenu.add(replaceMenuItem);

        menuBar.add(findMenu);

        sourceMenu.setText(resourceMap.getString("sourceMenu.text")); // NOI18N
        sourceMenu.setName("sourceMenu"); // NOI18N

        runDefaultMenuItem.setAction(actionMap.get("runFile")); // NOI18N
        runDefaultMenuItem.setName("runDefaultMenuItem"); // NOI18N
        sourceMenu.add(runDefaultMenuItem);

        compileMenuItem.setAction(actionMap.get("compileFile")); // NOI18N
        compileMenuItem.setName("compileMenuItem"); // NOI18N
        sourceMenu.add(compileMenuItem);

        jSeparator8.setName("jSeparator8"); // NOI18N
        sourceMenu.add(jSeparator8);

        gotoLineMenuItem.setAction(actionMap.get("gotoLine")); // NOI18N
        gotoLineMenuItem.setName("gotoLineMenuItem"); // NOI18N
        sourceMenu.add(gotoLineMenuItem);

        menuBar.add(sourceMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        settingsMenuItem.setAction(actionMap.get("settingsWindow")); // NOI18N
        settingsMenuItem.setName("settingsMenuItem"); // NOI18N
        helpMenu.add(settingsMenuItem);

        jSeparator9.setName("jSeparator9"); // NOI18N
        helpMenu.add(jSeparator9);

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        jPanel5.setName("jPanel5"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jTextFieldConvDez.setColumns(5);
        jTextFieldConvDez.setName("jTextFieldConvDez"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jTextFieldConvHex.setColumns(4);
        jTextFieldConvHex.setName("jTextFieldConvHex"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jTextFieldConvBin.setColumns(8);
        jTextFieldConvBin.setName("jTextFieldConvBin"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldConvDez, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jLabel7)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldConvHex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jLabel8)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldConvBin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(5, 5, 5))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(jTextFieldConvDez, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7)
                    .add(jTextFieldConvHex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8)
                    .add(jTextFieldConvBin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5))
        );

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelSeparator)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, statusPanelLayout.createSequentialGroup()
                .add(0, 624, Short.MAX_VALUE)
                .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem addTabMenuItem;
    private javax.swing.JMenuItem closeAllMenuItem;
    private javax.swing.JMenuItem closeFileMenuItem;
    private javax.swing.JMenuItem compileMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenu findMenu;
    private javax.swing.JMenuItem findNextMenuItem;
    private javax.swing.JMenuItem findPrevMenuItem;
    private javax.swing.JMenuItem findStartMenuItem;
    private javax.swing.JMenuItem gotoLineMenuItem;
    private javax.swing.JButton jButtonCarLog1;
    private javax.swing.JButton jButtonClearLog2;
    private javax.swing.JButton jButtonFindNext;
    private javax.swing.JButton jButtonFindPrev;
    private javax.swing.JButton jButtonReplace;
    private javax.swing.JButton jButtonRun;
    private javax.swing.JComboBox jComboBoxCompilers;
    private javax.swing.JComboBox jComboBoxParam;
    private javax.swing.JEditorPane jEditorPaneMain;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanelFind;
    private javax.swing.JPanel jPanelReplace;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPaneMainEditorPane;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextAreaCompilerOutput;
    private javax.swing.JTextArea jTextAreaLog;
    private javax.swing.JTextField jTextFieldConvBin;
    private javax.swing.JTextField jTextFieldConvDez;
    private javax.swing.JTextField jTextFieldConvHex;
    private javax.swing.JTextField jTextFieldFind;
    private javax.swing.JTextField jTextFieldReplace;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openFileMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem recent1MenuItem;
    private javax.swing.JMenuItem recent2MenuItem;
    private javax.swing.JMenuItem recent3MenuItem;
    private javax.swing.JMenuItem recent4MenuItem;
    private javax.swing.JMenuItem recent5MenuItem;
    private javax.swing.JMenuItem recent6MenuItem;
    private javax.swing.JMenuItem recent7MenuItem;
    private javax.swing.JMenuItem recent8MenuItem;
    private javax.swing.JMenu recentDocsSubmenu;
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JMenuItem replaceMenuItem;
    private javax.swing.JMenuItem runDefaultMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JMenuItem settingsMenuItem;
    private javax.swing.JMenu sourceMenu;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JMenuItem undoMenuItem;
    // End of variables declaration//GEN-END:variables

    private JDialog aboutBox;
    private SettingsDlg settingsDlg;
}
