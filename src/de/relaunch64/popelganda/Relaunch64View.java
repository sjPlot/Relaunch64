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

package de.relaunch64.popelganda;

import de.relaunch64.popelganda.Editor.EditorPaneLineNumbers;
import de.relaunch64.popelganda.Editor.EditorPanes;
import de.relaunch64.popelganda.Editor.FunctionExtractor;
import de.relaunch64.popelganda.Editor.InsertBreakPoint;
import de.relaunch64.popelganda.Editor.LabelExtractor;
import de.relaunch64.popelganda.Editor.SectionExtractor;
import de.relaunch64.popelganda.database.CustomScripts;
import de.relaunch64.popelganda.database.Settings;
import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.ErrorHandler;
import de.relaunch64.popelganda.util.FileTools;
import de.relaunch64.popelganda.util.Tools;
import java.awt.Color;
import java.awt.Component;
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
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.PopupMenuEvent;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.application.TaskService;

/**
 * The application's main frame.
 */
public class Relaunch64View extends FrameView implements WindowListener, DropTargetListener {
    private EditorPanes editorPanes;
    private final ErrorHandler errorHandler;
    private final FindReplace findReplace;
    private final List<Integer> comboBoxHeadings = new ArrayList<>();
    private final List<Integer> comboBoxHeadingsEditorPaneIndex = new ArrayList<>();
    private final static int GOTO_LABEL = 1;
    private final static int GOTO_SECTION = 2;
    private final static int GOTO_FUNCTION = 3;
    private final static int GOTO_MACRO = 4;
    private int comboBoxGotoIndex = -1;
    private final Settings settings;
    private final CustomScripts customScripts;
    private final org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class)
                                                                                                   .getContext().getResourceMap(Relaunch64View.class);
    
    public Relaunch64View(SingleFrameApplication app, Settings set, String[] params) {
        super(app);
        ConstantsR64.r64logger.addHandler(new TextAreaHandler());
        // tell logger to log everthing
        ConstantsR64.r64logger.setLevel(Level.ALL);
        // init database variables
        settings = set;
        findReplace = new FindReplace();
        customScripts = new CustomScripts();
        errorHandler = new ErrorHandler();
        // load custom scripts
        customScripts.loadScripts();
        // init default laf
        setDefaultLookAndFeel();
        // check for os x
        if (settings.isOSX()) setupMacOSXApplicationListener();
        // init swing components
        initComponents();
        // hide find & replace textfield
        jPanelFind.setVisible(false);
        jPanelReplace.setVisible(false);
        // init combo boxes
        initComboBoxes();
        // init listeners and accelerator table
        initListeners();
        initDropTargets();
        // set sys info
        jTextAreaLog.setText(Tools.getSystemInformation()+System.getProperty("line.separator"));
        // set application icon
        getFrame().setIconImage(ConstantsR64.r64icon.getImage());
        getFrame().setTitle(ConstantsR64.APPLICATION_TITLE);
        // init editorpane-dataclass
        editorPanes = new EditorPanes(jTabbedPane1, jComboBoxCompilers, jComboBoxRunScripts, this, settings);
        // init line numbers
        // init line numbers
        EditorPaneLineNumbers epln = new EditorPaneLineNumbers(jEditorPaneMain, settings);
        jScrollPaneMainEditorPane.setRowHeaderView(epln);
        // init syntax highlighting for editor pane
        editorPanes.addEditorPane(jEditorPaneMain, null, null, settings.getPreferredCompiler(), settings.getLastUserScript());
        // check if we have any parmater
        if (params!=null && params.length>0) {
            for (String p : params) openFile(new File(p));
        }
        // restore last opened files
        if (settings.getReopenOnStartup()) reopenFiles();
        // set input focus
        /**
         * JDK 8 Lamda
         */
//        SwingUtilities.invokeLater(() -> {
//            jEditorPaneMain.requestFocusInWindow();
//        });
        // set input focus
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                checkForUpdates();
                jEditorPaneMain.requestFocusInWindow();
            }
        });
    }
    /**
     * This is an application listener that is initialised when running the program
     * on mac os x. by using this appListener, we can use the typical apple-menu bar
     * which provides own about, preferences and quit-menu-items.
     */
    private void setupMacOSXApplicationListener() {
    // <editor-fold defaultstate="collapsed" desc="Application-listener initiating the stuff for the Apple-menu.">
        try {
            // get mac os-x application class
            Class appc = Class.forName("com.apple.eawt.Application");
            // create a new instance for it.
            Object app = appc.newInstance();
            // get the application-listener class. here we can set our action to the apple menu
            Class lc = Class.forName("com.apple.eawt.ApplicationListener");
            Object listener = Proxy.newProxyInstance(lc.getClassLoader(), new Class[] { lc }, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy,Method method,Object[] args) {
                    if (method.getName().equals("handleQuit")) {
                        // call the general exit-handler from the desktop-application-api
                        // here we do all the stuff we need when exiting the application
                        Relaunch64App.getApplication().exit();
                    }
                    if (method.getName().equals("handlePreferences")) {
                        // show settings window
                        settingsWindow();
                    }
                    if (method.getName().equals("handleAbout")) {
                        // show own aboutbox
                        showAboutBox();
                        try {
                            // set handled to true, so other actions won't take place any more.
                            // if we leave this out, a second, system-own aboutbox would be displayed
                            setHandled(args[0], Boolean.TRUE);
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                            ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
                        }
                    }
                    return null;
                }
                private void setHandled(Object event, Boolean val) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
                    Method handleMethod = event.getClass().getMethod("setHandled", new Class[] {boolean.class});
                    handleMethod.invoke(event, new Object[] {val});
                }
            });
            // tell about success
            ConstantsR64.r64logger.log(Level.INFO,"Apple Class Loader successfully initiated.");
            try {
                // add application listener that listens to actions on the apple menu items
                Method m = appc.getMethod("addApplicationListener", lc);
                m.invoke(app, listener);
                // register that we want that Preferences menu. by default, only the about box is shown
                // but no pref-menu-item
                Method enablePreferenceMethod = appc.getMethod("setEnabledPreferencesMenu", new Class[] {boolean.class});
                enablePreferenceMethod.invoke(app, new Object[] {Boolean.TRUE});
                // tell about success
                ConstantsR64.r64logger.log(Level.INFO,"Apple Preference Menu successfully initiated.");
            } catch (NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                ConstantsR64.r64logger.log(Level.SEVERE,ex.getLocalizedMessage());
            }
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            ConstantsR64.r64logger.log(Level.SEVERE,e.getLocalizedMessage());
        }
    // </editor-fold>
    }
    
    private void initComboBoxes() {
        jSplitPane2.setOrientation(settings.getSearchFrameSplitLayout(Settings.SPLITPANE_BOTHLOGRUN));
        jSplitPane1.setOrientation(settings.getSearchFrameSplitLayout(Settings.SPLITPANE_LOG));
        try {
            // init scripts
            initScripts();
            // init emulator combobox
            jComboBoxCompilers.setSelectedIndex(settings.getPreferredCompiler());
            // select last used script
            jComboBoxRunScripts.setSelectedIndex(settings.getLastUserScript());
            // init goto comboboxes
            jComboBoxGoto.removeAllItems();
            jComboBoxGoto.addItem(ConstantsR64.CB_GOTO_DEFAULT_STRING);
            jComboBoxGoto.setRenderer(new ComboBoxRenderer());
            jComboBoxGoto.setMaximumRowCount(20);
        }
        catch (IllegalArgumentException ex) {
        }
    }
    private void initScripts() {
        // init custom scripts
        jComboBoxRunScripts.removeAllItems();
        // retrieve all script names
        String[] scriptNames = customScripts.getScriptNames();
        // check if we have any
        if (scriptNames!=null && scriptNames.length>0) {
            // sort
            Arrays.sort(scriptNames);
            // add item to cb
            for (String sn : scriptNames) jComboBoxRunScripts.addItem(sn);
        }
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
        /**
         * JDK 8 Lamda
         */
//        jComboBoxCompilers.addActionListener((java.awt.event.ActionEvent evt) -> {
//            if (editorPanes.checkIfSyntaxChangeRequired()) {
//                editorPanes.changeSyntaxScheme(jComboBoxCompilers.getSelectedIndex());
//            }
//        });
        jComboBoxCompilers.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (editorPanes.checkIfSyntaxChangeRequired()) {
                    editorPanes.changeSyntaxScheme(jComboBoxCompilers.getSelectedIndex(), jComboBoxRunScripts.getSelectedIndex());
                }
                // update recent doc
                updateRecentDoc();
            }
        });
        jComboBoxRunScripts.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                editorPanes.getEditorPaneProperties(jTabbedPane1.getSelectedIndex()).setScript(jComboBoxRunScripts.getSelectedIndex());
                // update recent doc
                updateRecentDoc();
            }
        });
        jComboBoxGoto.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent e) {
                if (KeyEvent.VK_ENTER==e.getKeyCode()) editorPanes.preventAutoInsertTab();
            }
        });
        jComboBoxGoto.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            boolean cbCancelled = false;
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                jComboBoxGoto.removeAllItems();
                String header = ConstantsR64.CB_GOTO_DEFAULT_STRING;
                // add all names to combobox, depending on which menu item was selected
                switch (comboBoxGotoIndex) {
                    case GOTO_FUNCTION:
                        header = ConstantsR64.CB_GOTO_FUNCTION_STRING;
                        break;
                    case GOTO_SECTION:
                        header = ConstantsR64.CB_GOTO_SECTION_STRING;
                        break;
                    case GOTO_LABEL:
                        header = ConstantsR64.CB_GOTO_LABEL_STRING;
                        break;
                    case GOTO_MACRO:
                        header = ConstantsR64.CB_GOTO_MACRO_STRING;
                        break;
                }
                // add first element as "title" of combo box
                jComboBoxGoto.addItem(header);
                // check if we have any valid goto-menu-call
                if (comboBoxGotoIndex<1) return;
                // create a list with index numbers of tabs
                // (i.e. all index numbers of opened source code tabs)
                ArrayList<Integer> eps = new ArrayList<>();
                // here we store all items that should be added to the combo box
                // we don't add items directly to the cb, because labels / section names
                // may appear multiple times over all source codes, thus scrolling combo
                // box with keys won't work. We check this array list before adding items,
                // and if it is a multiple occurence, we add a suffix to avoid equal
                // item names in cb.
                ArrayList<String> completeComboBoxList = new ArrayList<>();
                // add current activated source code index first
                eps.add(jTabbedPane1.getSelectedIndex());
                // than add indices of remaining tabs
                for (int i=0; i<editorPanes.getCount(); i++) {
                    if (!eps.contains(i)) eps.add(i);
                }
                // clear headings
                comboBoxHeadings.clear();
                // clear heading related editor pane indices
                comboBoxHeadingsEditorPaneIndex.clear();
                // go through all opened editorpanes
                // eps.stream().forEach((epIndex) -> {
                for (int epIndex : eps) {
                    // extract and retrieve sections from each editor pane
                    ArrayList<String> token;
                    int doubleCounter = 2;
                    switch (comboBoxGotoIndex) {
                        case GOTO_SECTION:
                            token = SectionExtractor.getSectionNames(editorPanes.getSourceCode(epIndex), editorPanes.getCompilerCommentString());
                            break;
                        case GOTO_LABEL:
                            token = LabelExtractor.getLabelNames(true, false, editorPanes.getSourceCode(epIndex), editorPanes.getCompiler(epIndex));
                            break;
                        case GOTO_FUNCTION:
                            token = FunctionExtractor.getFunctionNames(editorPanes.getSourceCode(epIndex), editorPanes.getCompiler(epIndex));
                            break;
                        case GOTO_MACRO:
                            token = FunctionExtractor.getMacroNames(editorPanes.getSourceCode(epIndex), editorPanes.getCompiler(epIndex));
                            break;
                        default:
                            token = SectionExtractor.getSectionNames(editorPanes.getSourceCode(epIndex), editorPanes.getCompilerCommentString());
                            break;
                    }
                    // check if anything found
                    if (token!=null && !token.isEmpty()) {
                        // add item index of header to list. we need this for the custom cell renderer
                        comboBoxHeadings.add(completeComboBoxList.size()+1);
                        // add index of related editor pane
                        comboBoxHeadingsEditorPaneIndex.add(epIndex);
                        // if yes, add file name of editor pane as "heading" for following sections
                        completeComboBoxList.add(FileTools.getFileName(editorPanes.getEditorPaneProperties(epIndex).getFilePath()));
                        // add all found section strings to combo box
                        for (String arg : token) {
                            // items have a small margin, headings do not
                            String item = "   "+arg;
                            if (completeComboBoxList.contains(item)) {
                                // we have found multiple occurence of equal item names,
                                // so we add a suffix to it.
                                item = item+" [#"+String.valueOf(doubleCounter)+"]";
                                doubleCounter++;
                            }
                            // add item to list
                            completeComboBoxList.add(item);
                        }
                    }
                }
                // finally, add all items to combo box
                for (String arg : completeComboBoxList) jComboBoxGoto.addItem(arg);
                /**
                 * JDK 8 Lamda
                 */
//                completeComboBoxList.stream().forEach((arg) -> {
//                    jComboBoxGoto.addItem(arg);
//                });
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // check if combo box was cancelled
                if (cbCancelled) {
                    // if yes, reset flag and return
                    cbCancelled = false;
                    return;
                }
                // retrieve index of selected item
                int selectedIndex = jComboBoxGoto.getSelectedIndex();
                // check if > 0 and key was return
                if (selectedIndex>0) {
                    int index = 0;
                    // retrieve index position of selectedIndex within combo box heading. by this, we 
                    // get the source tab where the section is located
                    for (int i : comboBoxHeadings) if (i<=selectedIndex) index++;
                    // NOTE: The below line is the lamba-equivalent to the above line
                    /**
                     * JDK 8 Lamda
                     */
                    // index = comboBoxHeadings.stream().filter((i) -> (i<=selectedIndex)).map((_item) -> 1).reduce(index, Integer::sum);
                    // select specific tab where selected section is
                    jTabbedPane1.setSelectedIndex(comboBoxHeadingsEditorPaneIndex.get(index-1));
                    // set focus
                    editorPanes.setFocus();
                    // retrieve values
                    String item = jComboBoxGoto.getSelectedItem().toString().trim();
                    int epIndex = comboBoxHeadingsEditorPaneIndex.get(index-1);
                    // in case we have equal items multiple times (because they're spread
                    // over different source tabs), we number them in order to let the combo box
                    // work. Now we must remove this index.
                    item = item.replaceAll(" \\[#\\d+\\]", "");
                    // select where to go...
                    switch (comboBoxGotoIndex) {
                        case GOTO_FUNCTION:
                            // goto function
                            editorPanes.gotoFunction(item, epIndex);
                            break;
                        case GOTO_SECTION:
                            // goto section
                            editorPanes.gotoSection(item, epIndex);
                            break;
                        case GOTO_LABEL:
                            // goto label
                            editorPanes.gotoLabel(item, epIndex);
                        case GOTO_MACRO:
                            // goto label
                            editorPanes.gotoMacro(item, epIndex);
                            break;
                    }
                }
            }
            @Override public void popupMenuCanceled(PopupMenuEvent e) {
                cbCancelled = true;
            }
        });
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override public void stateChanged(javax.swing.event.ChangeEvent e) {
                editorPanes.updateTabbedPane();
                // and reset find/replace values, because the content has changed and former
                // find-index-values are no longer valid
                findReplace.resetValues();
            }
        });
        /**
         * JDK 8 Lambda
         */
//        jTabbedPane1.addChangeListener((javax.swing.event.ChangeEvent evt) -> {
//            editorPanes.updateTabbedPane();
//            // and reset find/replace values, because the content has changed and former
//            // find-index-values are no longer valid
//            findReplace.resetValues();
//        });
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
        jTextFieldGotoLine.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                if (KeyEvent.VK_ENTER==evt.getKeyCode()) {
                    try {
                        int line = Integer.parseInt(jTextFieldGotoLine.getText());
                        editorPanes.gotoLine(line);
                    }
                    catch (NumberFormatException ex) {
                        specialFunctions();
                    }
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
        jTabbedPaneLogs.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.getButton()==MouseEvent.BUTTON3) {
                    switch (jTabbedPaneLogs.getSelectedIndex()) {
                        case 0: clearLog1(); break;
                        case 1: clearLog2(); break;
                    }
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
                setRecentDocumentMenuItem(recent9MenuItem,9);
                setRecentDocumentMenuItem(recentAMenuItem,10);
            }
            @Override public void menuDeselected(javax.swing.event.MenuEvent evt) {}
            @Override public void menuCanceled(javax.swing.event.MenuEvent evt) {}
        });
        recent1MenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(1);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocCompiler(1), settings.getRecentDocScript(1));
                }
        }
        });
        recent2MenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(2);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocCompiler(2), settings.getRecentDocScript(2));
                }
            }
        });
        recent3MenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(3);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocCompiler(3), settings.getRecentDocScript(3));
                }
            }
        });
        recent4MenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(4);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocCompiler(4), settings.getRecentDocScript(4));
                }
            }
        });
        recent5MenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(5);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocCompiler(5), settings.getRecentDocScript(5));
                }
            }
        });
        recent6MenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(6);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocCompiler(6), settings.getRecentDocScript(6));
                }
            }
        });
        recent7MenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(7);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocCompiler(7), settings.getRecentDocScript(7));
                }
            }
        });
        recent8MenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(8);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocCompiler(8), settings.getRecentDocScript(8));
                }
            }
        });
        recent9MenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(9);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocCompiler(9), settings.getRecentDocScript(9));
                }
            }
        });
        recentAMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(10);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocCompiler(10), settings.getRecentDocScript(10));
                }
            }
        });
        /**
         * JDK 8 Lambda
         */
//        recent1MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
//            File fp = settings.getRecentDoc(1);
//            if (fp!=null && fp.exists()) {
//                openFile(fp, settings.getRecentDocCompiler(1));
//            }
//        });
//        recent2MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
//            File fp = settings.getRecentDoc(2);
//            if (fp!=null && fp.exists()) {
//                openFile(fp, settings.getRecentDocCompiler(2));
//            }
//        });
//        recent3MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
//            File fp = settings.getRecentDoc(3);
//            if (fp!=null && fp.exists()) {
//                openFile(fp, settings.getRecentDocCompiler(3));
//            }
//        });
//        recent4MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
//            File fp = settings.getRecentDoc(4);
//            if (fp!=null && fp.exists()) {
//                openFile(fp, settings.getRecentDocCompiler(4));
//            }
//        });
//        recent5MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
//            File fp = settings.getRecentDoc(5);
//            if (fp!=null && fp.exists()) {
//                openFile(fp, settings.getRecentDocCompiler(5));
//            }
//        });
//        recent6MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
//            File fp = settings.getRecentDoc(6);
//            if (fp!=null && fp.exists()) {
//                openFile(fp, settings.getRecentDocCompiler(6));
//            }
//        });
//        recent7MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
//            File fp = settings.getRecentDoc(7);
//            if (fp!=null && fp.exists()) {
//                openFile(fp, settings.getRecentDocCompiler(7));
//            }
//        });
//        recent8MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
//            File fp = settings.getRecentDoc(8);
//            if (fp!=null && fp.exists()) {
//                openFile(fp, settings.getRecentDocCompiler(8));
//            }
//        });
        // if we don't have OS X, we need to change action's accelerator keys
        // all "meta" (OS X command key) are converted to "ctrl"
        if (!settings.isOSX()) {
            // get application's actionmap
            javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getContext().getActionMap(Relaunch64View.class, this);
            // get all action values ("keys")
            Object[] keys = actionMap.keys();
            // iterate all actions
            for (Object o : keys) {
                // get accelerator key for action
                Object accob = actionMap.get(o).getValue(javax.swing.Action.ACCELERATOR_KEY);
                if (accob!=null) {
                    String acckey = accob.toString();
                    // check if it contains "meta"
                    if (acckey.contains("meta")) {
                        // and replace it with ctrl
                        acckey = acckey.replace("meta", "ctrl");
                        // set back new key
                        actionMap.get(o).putValue(javax.swing.Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(acckey));
                    }
                }
            }
        }
    }
    private void specialFunctions() {
        // String text = jTextFieldGotoLine.getText();
        jTextFieldGotoLine.setText("");
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
        setRecentDocumentMenuItem(recent9MenuItem,9);
        setRecentDocumentMenuItem(recentAMenuItem,10);
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
            menuItem.setText(FileTools.getFileName(recDoc));
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
        // retrieve last selected script name
        Object o = jComboBoxRunScripts.getSelectedItem();
        String selectedScriptName = (o!=null) ? o.toString() : null;
        // open settings window
        if (null == settingsDlg) {
            settingsDlg = new SettingsDlg(getFrame(), settings, customScripts);
            settingsDlg.setLocationRelativeTo(getFrame());
        }
        Relaunch64App.getApplication().show(settingsDlg);
        // update custom scripta
        initScripts();
        // select previous script
        if (customScripts.findScript(selectedScriptName)!=-1) jComboBoxRunScripts.setSelectedItem(selectedScriptName);
        // save the settings
        saveSettings();
    }
    @Action
    public void commentLine() {
        editorPanes.commentLine();
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
        jTextFieldGotoLine.requestFocusInWindow();
    }
    @Action
    public void gotoSection() {
        comboBoxGotoIndex = GOTO_SECTION;
        jComboBoxGoto.showPopup();
        jComboBoxGoto.requestFocusInWindow();
    }
    @Action
    public void gotoLabel() {
        comboBoxGotoIndex = GOTO_LABEL;
        jComboBoxGoto.showPopup();
        jComboBoxGoto.requestFocusInWindow();
    }
    @Action
    public void gotoFunction() {
        comboBoxGotoIndex = GOTO_FUNCTION;
        jComboBoxGoto.showPopup();
        jComboBoxGoto.requestFocusInWindow();
    }
    @Action
    public void gotoMacro() {
        comboBoxGotoIndex = GOTO_MACRO;
        jComboBoxGoto.showPopup();
        jComboBoxGoto.requestFocusInWindow();
    }
    @Action
    public void jumpToLabel() {
        // get word under caret
        String label = editorPanes.getCaretString(true, "");
        // if we have kick ass, add colon
        if (label!=null && ConstantsR64.COMPILER_KICKASSEMBLER==editorPanes.getActiveCompiler()) label = label+":";
        editorPanes.gotoLabel(label);
    }
    @Action
    public void insertSection() {
        // open an input-dialog
        String sectionName = (String)JOptionPane.showInputDialog(getFrame(),"Section name:", "Insert section", JOptionPane.PLAIN_MESSAGE);
        // check
        if (sectionName!=null && !sectionName.isEmpty()) {
            editorPanes.insertSection(sectionName);
        }
    }
    @Action
    public void insertSeparatorLine() {
        editorPanes.insertSeparatorLine();
    }
    @Action
    public void gotoNextSection() {
        // retrieve line numbers and section names
        ArrayList<Integer> ln = SectionExtractor.getSectionLineNumbers(editorPanes.getActiveSourceCode(), editorPanes.getCompilerCommentString());
        ArrayList<String> names = SectionExtractor.getSectionNames(editorPanes.getActiveSourceCode(), editorPanes.getCompilerCommentString());
        // get current line of caret
        int currentLine = editorPanes.getCurrentLineNumber();
        String dest = null;
        // check if we found anything
        boolean labelFound = false;
        // iterate all line numbers
        for (int i=0; i<ln.size(); i++) {
            // if we found a line number greater than current
            // line, we found the next section from caret position
            if (ln.get(i)>currentLine) {
                dest = names.get(i);
                labelFound = true;
                break;
            }
        }
        try {
            // found anything?
            // if not, start from beginning
            if (!labelFound) dest = names.get(0);
        }
        catch (IndexOutOfBoundsException ex) {
        }
        // goto next section
        editorPanes.gotoSection(dest);
    }
    @Action
    public void gotoPrevSection() {
        // retrieve line numbers and section names
        ArrayList<Integer> ln = SectionExtractor.getSectionLineNumbers(editorPanes.getActiveSourceCode(), editorPanes.getCompilerCommentString());
        ArrayList<String> names = SectionExtractor.getSectionNames(editorPanes.getActiveSourceCode(), editorPanes.getCompilerCommentString());
        // get current line of caret
        int currentLine = editorPanes.getCurrentLineNumber();
        String dest = null;
        // check if we found anything
        boolean labelFound = false;
        // iterate all line numbers
        for (int i=ln.size()-1; i>=0; i--) {
            // if we found a line number smaller than current
            // line, we found the previous section from caret position
            if (ln.get(i)<currentLine) {
                dest = names.get(i);
                labelFound = true;
                break;
            }
        }
        try {
            // found anything?
            // if not, start from beginning
            if (!labelFound) dest = names.get(names.size()-1);
        }
        catch (IndexOutOfBoundsException ex) {
        }
        // goto previous section
        editorPanes.gotoSection(dest);
    }
    @Action
    public void gotoNextError() {
        errorHandler.gotoNextError(editorPanes);
    }
    @Action
    public void gotoPrevError() {
        errorHandler.gotoPrevError(editorPanes);
    }
    @Action
    public void gotoNextLabel() {
        // retrieve line numbers and label names
        ArrayList<Integer> ln = LabelExtractor.getLabelLineNumbers(editorPanes.getActiveSourceCode(), editorPanes.getActiveCompiler());
        ArrayList<String> names = LabelExtractor.getLabelNames(false, false, editorPanes.getActiveSourceCode(), editorPanes.getActiveCompiler());
        // get current line of caret
        int currentLine = editorPanes.getCurrentLineNumber();
        String dest = null;
        // check if we found anything
        boolean labelFound = false;
        // iterate all line numbers
        for (int i=0; i<ln.size(); i++) {
            // if we found a line number greater than current
            // line, we found the next label from caret position
            if (ln.get(i)>currentLine) {
                dest = names.get(i);
                labelFound = true;
                break;
            }
        }
        try {
            // found anything?
            // if not, start from beginning
            if (!labelFound) dest = names.get(0);
        }
        catch (IndexOutOfBoundsException ex) {
        }
        // goto next label
        editorPanes.gotoLabel(dest);
    }
    @Action
    public void gotoPrevLabel() {
        // retrieve line numbers and label names
        ArrayList<Integer> ln = LabelExtractor.getLabelLineNumbers(editorPanes.getActiveSourceCode(), editorPanes.getActiveCompiler());
        ArrayList<String> names = LabelExtractor.getLabelNames(false, false, editorPanes.getActiveSourceCode(), editorPanes.getActiveCompiler());
        // get current line of caret
        int currentLine = editorPanes.getCurrentLineNumber();
        String dest = null;
        // check if we found anything
        boolean labelFound = false;
        // iterate all line numbers
        for (int i=ln.size()-1; i>=0; i--) {
            // if we found a line number smaller than current
            // line, we found the previous label from caret position
            if (ln.get(i)<currentLine) {
                dest = names.get(i);
                labelFound = true;
                break;
            }
        }
        try {
            // found anything?
            // if not, start from beginning
            if (!labelFound) dest = names.get(names.size()-1);
        }
        catch (IndexOutOfBoundsException ex) {
        }
        // goto previous label
        editorPanes.gotoLabel(dest);
    }
    @Action
    public void setFocusToSource() {
        editorPanes.setFocus();
    }
    /**
     * 
     */
    @Action
    public void addNewTab() {
        editorPanes.addNewTab(null, null, "untitled", settings.getPreferredCompiler(), jComboBoxRunScripts.getSelectedIndex());
    }
    @Action
    public void openFile() {
        File fileToOpen = FileTools.chooseFile(getFrame(), JFileChooser.OPEN_DIALOG, JFileChooser.FILES_ONLY, settings.getLastUsedPath().getAbsolutePath(), "", "Open ASM File", ConstantsR64.FILE_EXTENSIONS, "ASM-Files");
        openFile(fileToOpen);
    }
    private void openFile(File fileToOpen) {
        openFile(fileToOpen, settings.getPreferredCompiler());
    }
    private void openFile(File fileToOpen, int compiler) {
        openFile(fileToOpen, compiler, jComboBoxRunScripts.getSelectedIndex());
    }
    private void openFile(File fileToOpen, int compiler, int script) {
        // check if file could be opened
        if (editorPanes.loadFile(fileToOpen, compiler, script)) {
            // add file path to recent documents history
            settings.addToRecentDocs(fileToOpen.toString(), compiler, script);
            // and update menus
            setRecentDocuments();
            // save last used path
            settings.setLastUsedPath(fileToOpen);
            // select combobox item
            try {
                jComboBoxCompilers.setSelectedIndex(compiler);
                jComboBoxRunScripts.setSelectedIndex(script);
            }
            catch (IllegalArgumentException ex) {
            }
        } 
    }
    private void updateRecentDoc() {
        // find current file
        File cf = editorPanes.getActiveFilePath();
        // find doc associated with current document
        int rd = settings.findRecentDoc(cf);
        // if we have valid values, update recent doc
        if (rd!=-1 && cf!=null) {
            settings.setRecentDoc(rd, cf.toString(), jComboBoxCompilers.getSelectedIndex(), jComboBoxRunScripts.getSelectedIndex());
        }
    }
    private void reopenFiles() {
        // get reopen files
        ArrayList<Object[]> files = settings.getReopenFiles();
        // check if we have any
        if (files!=null && !files.isEmpty()) {
            // retrieve set
            for (Object[] o : files) {
                // retrieve data
                File fp = new File(o[0].toString());
                int compiler = Integer.parseInt(o[1].toString());
                int script = Integer.parseInt(o[2].toString());
                // open file
                openFile(fp, compiler, script);
            }
        }
    }
    @Action
    public void saveFile() {
        if (editorPanes.saveFile()) {
            // add file path to recent documents history
            settings.addToRecentDocs(editorPanes.getActiveFilePath().getPath(), jComboBoxCompilers.getSelectedIndex(), jComboBoxRunScripts.getSelectedIndex());
            // and update menus
            setRecentDocuments();
        }
    }
    @Action
    public void saveAllFiles() {
        editorPanes.saveAllFiles();
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
                editorPanes = new EditorPanes(jTabbedPane1, jComboBoxCompilers, jComboBoxRunScripts, this, settings);
                // init syntax highlighting for editor pane
                editorPanes.addNewTab(null, null, "untitled", settings.getPreferredCompiler(), jComboBoxRunScripts.getSelectedIndex());
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
        if (editorPanes.saveFileAs()) {
            // add file path to recent documents history
            settings.addToRecentDocs(editorPanes.getActiveFilePath().getPath(), jComboBoxCompilers.getSelectedIndex(), jComboBoxRunScripts.getSelectedIndex());
            // and update menus
            setRecentDocuments();
        }
    }
    @Action
    public void runScript() {
        // get selected item
        Object item = jComboBoxRunScripts.getSelectedItem();
        // valid selection?
        if (item!=null) {
            // get script
            String script = customScripts.getScript(item.toString());
            // valid script?
            if (script!=null && !script.isEmpty()) {
                // clear old log
                clearLog1();
                clearLog2();
                // clesr error lines
                errorHandler.clearErrors();
                // remove \r
                script = script.replaceAll("\r", "");
                // retrieve script lines
                String[] lines = script.split("\n");
                // check if source file needs to be saved and auto save is active
                if (settings.getSaveOnCompile() && editorPanes.isModified()) editorPanes.saveFile();
                // retrieve ASM-Source file
                File sourceFile = editorPanes.getActiveFilePath();
                // retrieve parent file. needed to construct output file paths
                String parentFile = (null==sourceFile.getParentFile()) ? sourceFile.toString() : sourceFile.getParentFile().toString();
                // create Output file
                File outFile = new File(parentFile+File.separator+FileTools.getFileName(sourceFile)+".prg");
                // create compressed file
                File compressedFile = new File(parentFile+File.separator+FileTools.getFileName(sourceFile)+"-compressed.prg");
                // iterate script
                for (String cmd : lines) {
                    cmd = cmd.trim();
                    if (!cmd.isEmpty()) {
                        // log process
                        String log = "Processing script-line: "+cmd;
                        ConstantsR64.r64logger.log(Level.INFO, log);
                        // surround pathes with quotes
                        String sf = sourceFile.toString();
                        if (sf.contains(" ") && !sf.startsWith("\"") && !sf.startsWith("'")) sf = "\""+sf+"\"";
                        String of = outFile.toString();
                        if (of.contains(" ") && !of.startsWith("\"") && !of.startsWith("'")) of = "\""+of+"\"";
                        String cf = compressedFile.toString();
                        if (cf.contains(" ") && !cf.startsWith("\"") && !cf.startsWith("'")) cf = "\""+cf+"\"";
                        // replace input and output file
                        cmd = cmd.replace(ConstantsR64.ASSEMBLER_INPUT_FILE, sf);
                        cmd = cmd.replace(ConstantsR64.ASSEMBLER_OUPUT_FILE, of);
                        cmd = cmd.replace(ConstantsR64.ASSEMBLER_UNCOMPRESSED_FILE, of);
                        cmd = cmd.replace(ConstantsR64.ASSEMBLER_COMPRESSED_FILE, cf);
                        // check if we have a cruncher-starttoken
                        String cruncherStart = Tools.getCruncherStart(editorPanes.getActiveSourceCode(), editorPanes.getActiveCompiler());
                        if (cruncherStart!=null) cmd = cmd.replace(ConstantsR64.ASSEMBLER_START_ADDRESS, cruncherStart);
                        try {
                            // log process
                            log = "Converted script-line: "+cmd;
                            ConstantsR64.r64logger.log(Level.INFO, log);
                            // write output to text area
                            StringBuilder compilerLog = new StringBuilder("");
                            ProcessBuilder pb;
                            Process p;
                            // Start ProcessBuilder
                            pb = new ProcessBuilder(cmd.split(" "));
                            pb = pb.directory(sourceFile.getParentFile());
                            pb = pb.redirectInput(Redirect.PIPE).redirectError(Redirect.PIPE);
                            // start process
                            p = pb.start();
                            // write output to text area
                            // create scanner to receive compiler messages
                            try (Scanner sc = new Scanner(p.getInputStream()).useDelimiter(System.getProperty("line.separator"))) {
                                // write output to text area
                                while (sc.hasNextLine()) {
                                    compilerLog.append(System.getProperty("line.separator")).append(sc.nextLine());
                                }
                            }
                            try (Scanner sc = new Scanner(p.getErrorStream()).useDelimiter(System.getProperty("line.separator"))) {
                                // write output to text area
                                while (sc.hasNextLine()) {
                                    compilerLog.append(System.getProperty("line.separator")).append(sc.nextLine());
                                }
                            }
                            // finally, append new line
                            compilerLog.append(System.getProperty("line.separator"));
                            // print log to text area
                            jTextAreaCompilerOutput.append(compilerLog.toString());                            
                            // wait for other process to be finished
                            p.waitFor();
                            p.destroy();
                            // read and extract errors from log
                            errorHandler.readErrorLines(compilerLog.toString(), editorPanes.getActiveCompiler());
                            // break loop if we have any errors
                            if (errorHandler.hasErrors() || p.exitValue()!=0) break;
                        }
                        catch (IOException | InterruptedException | SecurityException ex) {
                            ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
                            // check if permission denied
                            if (ex.getLocalizedMessage().toLowerCase().contains("permission denied")) {
                                ConstantsR64.r64logger.log(Level.INFO, "Permission denied. Try to define user scripts in the preferences and use \"open\" or \"/bin/sh\" as parameters (see Help on Preference pane tab)!");
                            }
                        }
                    }
                }
                // select error log if we have errors
                if (errorHandler.hasErrors()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            // show error log
                            selectLog2();
                            // get and open error file
                            openFile(errorHandler.getErrorFile(), editorPanes.getActiveCompiler());
                            // goto error line
                            errorHandler.gotoFirstError(editorPanes);
                            // set focus in edior pane
                            editorPanes.setFocus();
                        }
                    });
                }
            }
        }
        else {
            JOptionPane.showMessageDialog(getFrame(), "Please open preferences and add a 'compile and run' script first!");
        }
    }
    @Action
    public void switchLogPosition() {
        int currentlayout = settings.getSearchFrameSplitLayout(Settings.SPLITPANE_LOG);
        currentlayout = (JSplitPane.HORIZONTAL_SPLIT == currentlayout) ? JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT;
        settings.setSearchFrameSplitLayout(currentlayout, Settings.SPLITPANE_LOG);
        jSplitPane1.setOrientation(currentlayout);
    }
    @Action
    public void switchBothPosition() {
        int currentlayout = settings.getSearchFrameSplitLayout(Settings.SPLITPANE_BOTHLOGRUN);
        currentlayout = (JSplitPane.HORIZONTAL_SPLIT == currentlayout) ? JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT;
        settings.setSearchFrameSplitLayout(currentlayout, Settings.SPLITPANE_BOTHLOGRUN);
        jSplitPane2.setOrientation(currentlayout);
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
    public void selectUserScripts() {
        jComboBoxRunScripts.showPopup();
        jComboBoxRunScripts.requestFocusInWindow();
    }
    @Action
    public void selectSyntax() {
        jComboBoxCompilers.showPopup();
        jComboBoxCompilers.requestFocusInWindow();
    }
    @Action
    public void selectLog1() {
        jTabbedPaneLogs.setSelectedIndex(0);
    }
    @Action
    public void selectLog2() {
        jTabbedPaneLogs.setSelectedIndex(1);
    }
    @Action
    public void selectAllText() {
        editorPanes.getActiveEditorPane().selectAll();
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
        // cancel replace
        replaceCancel();
        // reset values
        findReplace.resetValues();
        jTextFieldFind.setForeground(Color.black);
        // make it visible
        jPanelFind.setVisible(false);
        // set input focus in main textfield
        editorPanes.setFocus();
    }
    @Action
    public void replaceAll() {
        findReplace.initValues(jTextFieldFind.getText(), jTextFieldReplace.getText(), jTabbedPane1.getSelectedIndex(), editorPanes.getActiveEditorPane(), true);
        int findCounter = 0;
        while (findReplace.replace()) findCounter++;
        JOptionPane.showMessageDialog(getFrame(), String.valueOf(findCounter)+" occurences were replaced.");
    }
    @Action
    public void replaceTerm() {
        // make it visible
        jPanelFind.setVisible(true);
        // check whether textfield is visible
        if (!jPanelReplace.isVisible()) {
            // make it visible
            jPanelReplace.setVisible(true);
            // and set input focus in it
            if (jTextFieldFind.getText().isEmpty()) {
                jTextFieldFind.requestFocusInWindow();
            }
            else {
                jTextFieldReplace.requestFocusInWindow();
            }
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
    @Action
    public void insertBytesFromFile() {
        // open dialog
        if (null==insertByteFromFileDlg) {
            insertByteFromFileDlg = new InsertByteFromFileDlg(getFrame(), settings, editorPanes.getActiveCompiler());
            insertByteFromFileDlg.setLocationRelativeTo(getFrame());
        }
        Relaunch64App.getApplication().show(insertByteFromFileDlg);
        // check for valid return value
        String bytetable = insertByteFromFileDlg.getByteTable();
        // insert bytes to source code
        if (bytetable!=null && !bytetable.isEmpty()) {
            editorPanes.insertString(bytetable);
        }
        insertByteFromFileDlg = null;
    }
    @Action
    public void insertBasicStart() {
        Tools.insertBasicStart(editorPanes);
    }
    @Action
    public void insertBreakPoint() {
        InsertBreakPoint.insertBreakPoint(editorPanes);
    }
    @Action
    public void removeAllBreakPoints() {
        InsertBreakPoint.removeBreakPoints(editorPanes);
    }
    @Action
    public void insertSinusTable() {
        // open dialog
        if (null==insertSinusTableDlg) {
            insertSinusTableDlg = new InsertSinusTableDlg(getFrame(), editorPanes.getActiveCompiler());
            insertSinusTableDlg.setLocationRelativeTo(getFrame());
        }
        Relaunch64App.getApplication().show(insertSinusTableDlg);
        // check for valid return value
        String bytetable = insertSinusTableDlg.getByteTable();
        // insert bytes to source code
        if (bytetable!=null && !bytetable.isEmpty()) {
            editorPanes.insertString(bytetable);
        }
        insertSinusTableDlg = null;
    }
    /**
     * 
     */
    private void setDefaultLookAndFeel() {
        // retrieve all installed Look and Feels
        UIManager.LookAndFeelInfo[] installed_laf = UIManager.getInstalledLookAndFeels();
        // init found-variables
        String classname = "";
        // in case we find "nimbus" LAF, set this as default on non-mac-os
        // because it simply looks the best.
        for (UIManager.LookAndFeelInfo laf : installed_laf) {
            // check whether laf is nimbus
            if (laf.getClassName().toLowerCase().contains("nimbus")) {
                classname = laf.getClassName();
                break;
            }
        }
        // check which laf was found and set appropriate default value 
        if (!classname.isEmpty()) {
            try {
                // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                UIManager.setLookAndFeel(classname);
            } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
            }
        }
        if (settings.isOSX()) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", ConstantsR64.APPLICATION_SHORT_TITLE);
        }
        System.setProperty("awt.useSystemAAFontSettings", "on");
    }
    /**
     * 
     */
    private void saveSettings() {
        settings.setLastUserScript(jComboBoxRunScripts.getSelectedIndex());
        // save currently opened tabs, in case they should be restored on next startup
        settings.setReopenFiles(editorPanes);
        settings.saveSettings();
        customScripts.saveScripts();
    }
    @Action
    public void showHelp() {
        if (aboutBox == null) {
            JFrame mainFrame = Relaunch64App.getApplication().getMainFrame();
            aboutBox = new Relaunch64AboutBox(mainFrame, org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getClass().getResource("/de/relaunch64/popelganda/resources/help.html"));
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        Relaunch64App.getApplication().show(aboutBox);
        aboutBox.dispose();
        aboutBox = null;
    }
    @Action
    public void showQuickReference() {
        if (quickReferenceDlg == null) {
            JFrame mainFrame = Relaunch64App.getApplication().getMainFrame();
            quickReferenceDlg = new QuickReferences(mainFrame);
            quickReferenceDlg.setLocationRelativeTo(mainFrame);
        }
        Relaunch64App.getApplication().show(quickReferenceDlg);
    }
    /**
     * 
     */
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = Relaunch64App.getApplication().getMainFrame();
            aboutBox = new Relaunch64AboutBox(mainFrame, org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getClass().getResource("/de/relaunch64/popelganda/resources/licence.html"));
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        Relaunch64App.getApplication().show(aboutBox);
        aboutBox.dispose();
        aboutBox = null;
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
                dtde.acceptDrop(DnDConstants.ACTION_LINK | DnDConstants.ACTION_COPY_OR_MOVE);
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
                    List<File> includefiles = new ArrayList<>();
                    List<File> linkedfiles = new ArrayList<>();
                    // dummy
                    File file;
                    for (Object file1 : files) {
                        // get each single object from droplist
                        file = (File) file1;
                        // check whether it is a file
                        if (file.isFile()) {
                            // if we have link action, only insert paths
                            if (dtde.getDropAction()==DnDConstants.ACTION_LINK && validDropLocation) {
                                linkedfiles.add(file);
                            }
                            // if it's an asm, add it to asm file list
                            else if (FileTools.hasValidFileExtension(file) && validDropLocation) {
                                // if so, add it to list
                                anyfiles.add(file);
                            }
                            // if it's an include file, add it to include file list
                            else if (FileTools.hasValidIncludeFileExtension(file) && validDropLocation) {
                                // if so, add it to list
                                includefiles.add(file);
                            }
                        }
                    }
                    // check if we have any valid values,
                    // i.e. any files have been dragged and dropped
                    // if so, include files
                    if (linkedfiles.size()>0) {
                        for (File f : linkedfiles) {
                            editorPanes.insertString("\""+f.toString()+"\""+System.getProperty("line.separator"));
                        }
                    }
                    // check if we have any valid values,
                    // i.e. any files have been dragged and dropped
                    // if so, include files
                    if (includefiles.size()>0) {
                        for (File f : includefiles) {
                            String insert = "";
                            // if user hold down ctrl-key, import bytes from file
                            if (dtde.getDropAction()==DnDConstants.ACTION_COPY) {
                                insert = Tools.getByteTableFromFile(f, editorPanes.getActiveCompiler());
                            }
                            // else use include-directive
                            else {
                                // retrieve relative path of iimport file
                                String relpath = FileTools.getRelativePath(editorPanes.getActiveFilePath(), f);
                                if (FileTools.getFileExtension(f).equalsIgnoreCase("bin")) {
                                    switch (editorPanes.getActiveCompiler()) {
                                        case ConstantsR64.COMPILER_ACME:
                                            insert = "!bin \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_KICKASSEMBLER:
                                            insert = ".import binary \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_64TASS:
                                            insert = ".binary \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                    }
                                }
                                else if (FileTools.getFileExtension(f).equalsIgnoreCase("txt")) {
                                    switch (editorPanes.getActiveCompiler()) {
                                        case ConstantsR64.COMPILER_ACME:
                                            insert = "!bin \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_KICKASSEMBLER:
                                            insert = ".import text \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_64TASS:
                                            insert = ".binary \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                    }
                                }
                                else if (FileTools.getFileExtension(f).equalsIgnoreCase("c64")) {
                                    switch (editorPanes.getActiveCompiler()) {
                                        case ConstantsR64.COMPILER_ACME:
                                            insert = "!bin \""+relpath+"\",,2"+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_KICKASSEMBLER:
                                            insert = ".import c64 \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_64TASS:
                                            insert = ".binary \""+relpath+"\",2"+System.getProperty("line.separator");
                                            break;
                                    }
                                }
                            }
                            editorPanes.insertString(insert);
                        }
                    }
                    // check if we have any valid values,
                    // i.e. any files have been dragged and dropped
                    // if so, open asm files
                    if (anyfiles.size()>0) {
                        for (File f : anyfiles) {
                            // if user hold down ctrl-key, use import-directive for asm-files
                            if (dtde.getDropAction()==DnDConstants.ACTION_COPY) {
                                String insert = "";
                                String relpath = FileTools.getRelativePath(editorPanes.getActiveFilePath(), f);
                                switch (editorPanes.getActiveCompiler()) {
                                    case ConstantsR64.COMPILER_ACME:
                                        insert = "!src \""+relpath+"\""+System.getProperty("line.separator");
                                        break;
                                    case ConstantsR64.COMPILER_KICKASSEMBLER:
                                        insert = ".import source \""+relpath+"\""+System.getProperty("line.separator");
                                        break;
                                    case ConstantsR64.COMPILER_64TASS:
                                        insert = ".binclude \""+relpath+"\""+System.getProperty("line.separator");
                                        break;
                                }
                                editorPanes.insertString(insert);
                            }
                            else {
                                // else open files
                                openFile(f);
                            }
                        }
                        /**
                         * JDK 8 Lamda
                         */
//                        anyfiles.stream().forEach((f) -> {
//                            openFile(f);
//                        });
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
        boolean changes = false;
        int count = editorPanes.getCount();
        // check whether we have any changes at all
        for (int i=0; i<count; i++) {
            if (editorPanes.isModified(i)) changes = true;
        }
        // ask for save
        if (changes) {
            // open a confirm dialog
            int option = JOptionPane.showConfirmDialog(getFrame(), resourceMap.getString("msgSaveChangesOnExit"), resourceMap.getString("msgSaveChangesOnExitTitle"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            // if action is cancelled, return to the program
            if (JOptionPane.CANCEL_OPTION==option || JOptionPane.CLOSED_OPTION==option /* User pressed cancel key */) {
                return false;
            }
            else if (JOptionPane.YES_OPTION == option) {
                boolean saveok = true;
                for (int i=0; i<count; i++) {
                    if (editorPanes.isModified(i)) {
                        if (!editorPanes.saveFile()) saveok=false;
                    }
                }
                return saveok;
            }
        }
        // no changes, so everything is ok
        return true;
    }
    /**
     * 
     */
    public class TextAreaHandler extends java.util.logging.Handler {
        @Override
        public void publish(final LogRecord record) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    StringWriter text = new StringWriter();
                    PrintWriter out = new PrintWriter(text);
                    out.println(jTextAreaLog.getText());
                    out.printf("[%s] %s", record.getLevel(), record.getMessage());
                    jTextAreaLog.setText(text.toString());
                }
            });
            /**
             * JDK 8 Lambda
             */
//            SwingUtilities.invokeLater(() -> {
//                StringWriter text = new StringWriter();
//                PrintWriter out = new PrintWriter(text);
//                out.println(jTextAreaLog.getText());
//                out.printf("[%s] %s", record.getLevel(), record.getMessage());
//                jTextAreaLog.setText(text.toString());
//            });
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
    public void checkForUpdates() {
        // check if check should be checked
        if (!settings.getCheckForUpdates()) return;
        Task cfuT = checkForUpdate();
        // get the application's context...
        ApplicationContext appC = Application.getInstance().getContext();
        // ...to get the TaskMonitor and TaskService
        TaskMonitor tM = appC.getTaskMonitor();
        TaskService tS = appC.getTaskService();
        // with these we can execute the task and bring it to the foreground
        // i.e. making the animated progressbar and busy icon visible
        tS.execute(cfuT);
        tM.setForegroundTask(cfuT);
    }
    public final Task checkForUpdate() {
        return new CheckForUpdates(org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class));
    }
    private class ComboBoxRenderer implements ListCellRenderer {
        protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // get default renderer
            Component renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            // check if current row is a combo box header
            if (comboBoxHeadings!=null & !comboBoxHeadings.isEmpty() && comboBoxHeadings.contains(index)) {
                // if yes, change font style
                renderer.setFont(renderer.getFont().deriveFont(Font.BOLD));
                renderer.setForeground(new Color(153, 51, 51));
            }
            // return renderer
            return renderer;
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
        jPanel2 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jComboBoxRunScripts = new javax.swing.JComboBox();
        jButtonRunScript = new javax.swing.JButton();
        jComboBoxCompilers = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jTabbedPaneLogs = new javax.swing.JTabbedPane();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaLog = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextAreaCompilerOutput = new javax.swing.JTextArea();
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
        recent9MenuItem = new javax.swing.JMenuItem();
        recentAMenuItem = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        saveAllMenuItem = new javax.swing.JMenuItem();
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
        replaceAllMenuItem = new javax.swing.JMenuItem();
        gotoMenu = new javax.swing.JMenu();
        gotoFunctionMenuItem = new javax.swing.JMenuItem();
        gotoLabelMenuItem = new javax.swing.JMenuItem();
        gotoLineMenuItem = new javax.swing.JMenuItem();
        gotoMacroMenuItem = new javax.swing.JMenuItem();
        gotoSectionMenuItem = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        jumpToLabelMenuItem = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        gotoNextLabel = new javax.swing.JMenuItem();
        gotoPrevLabel = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JPopupMenu.Separator();
        gotoNextSectionMenuItem = new javax.swing.JMenuItem();
        gotoPrevSectionMenuItem = new javax.swing.JMenuItem();
        jSeparator20 = new javax.swing.JPopupMenu.Separator();
        gotoNextErrorMenuItem = new javax.swing.JMenuItem();
        gotoPrevErrorMenuItem = new javax.swing.JMenuItem();
        sourceMenu = new javax.swing.JMenu();
        runScriptMenuItem = new javax.swing.JMenuItem();
        focusScriptMenuItem = new javax.swing.JMenuItem();
        focusSyntaxMenuItem = new javax.swing.JMenuItem();
        jSeparator17 = new javax.swing.JPopupMenu.Separator();
        commentLineMenuItem = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        insertSectionMenuItem = new javax.swing.JMenuItem();
        insertSeparatorMenuItem = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JPopupMenu.Separator();
        insertBreakPointMenuItem = new javax.swing.JMenuItem();
        removeBreakpointMenuItem = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JPopupMenu.Separator();
        insertBasicStartMenuItem = new javax.swing.JMenuItem();
        insertBytesFromFileMenuItem = new javax.swing.JMenuItem();
        insertSinusMenuItem = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JPopupMenu.Separator();
        viewMainTabMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        viewLog1MenuItem = new javax.swing.JMenuItem();
        viewLog2MenuItem = new javax.swing.JMenuItem();
        jSeparator19 = new javax.swing.JPopupMenu.Separator();
        switchBothMenuItem = new javax.swing.JMenuItem();
        switchLogPosMenuItem = new javax.swing.JMenuItem();
        jSeparator21 = new javax.swing.JPopupMenu.Separator();
        quickRefMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        settingsMenuItem = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        helpMenuItem = new javax.swing.JMenuItem();
        jSeparator18 = new javax.swing.JPopupMenu.Separator();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldConvDez = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldConvHex = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldConvBin = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldGotoLine = new javax.swing.JTextField();
        jComboBoxGoto = new javax.swing.JComboBox();

        mainPanel.setName("mainPanel"); // NOI18N

        jSplitPane1.setDividerLocation(480);
        jSplitPane1.setName("jSplitPane1"); // NOI18N
        jSplitPane1.setOneTouchExpandable(true);

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

        jLabel5.setDisplayedMnemonic('i');
        jLabel5.setLabelFor(jTextFieldFind);
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

        jLabel4.setDisplayedMnemonic('R');
        jLabel4.setLabelFor(jTextFieldReplace);
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

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
            .add(jPanelFind, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanelReplace, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelFind, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelReplace, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel2.setName("jPanel2"); // NOI18N

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setName("jSplitPane2"); // NOI18N
        jSplitPane2.setOneTouchExpandable(true);

        jPanel3.setName("jPanel3"); // NOI18N

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel9.border.title"))); // NOI18N
        jPanel9.setName("jPanel9"); // NOI18N

        jComboBoxRunScripts.setName("jComboBoxRunScripts"); // NOI18N

        jButtonRunScript.setAction(actionMap.get("runScript")); // NOI18N
        jButtonRunScript.setName("jButtonRunScript"); // NOI18N

        jComboBoxCompilers.setModel(new javax.swing.DefaultComboBoxModel(ConstantsR64.COMPILER_NAMES));
        jComboBoxCompilers.setName("jComboBoxCompilers"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel9Layout = new org.jdesktop.layout.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jComboBoxRunScripts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jComboBoxCompilers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 161, Short.MAX_VALUE)
                .add(jButtonRunScript)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel9Layout.createSequentialGroup()
                .add(jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBoxRunScripts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonRunScript)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBoxCompilers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        jSplitPane2.setTopComponent(jPanel3);

        jPanel4.setName("jPanel4"); // NOI18N

        jTabbedPaneLogs.setName("jTabbedPaneLogs"); // NOI18N

        jPanel6.setName("jPanel6"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTextAreaLog.setEditable(false);
        jTextAreaLog.setWrapStyleWord(true);
        jTextAreaLog.setName("jTextAreaLog"); // NOI18N
        jScrollPane2.setViewportView(jTextAreaLog);

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 384, Short.MAX_VALUE)
            .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 469, Short.MAX_VALUE)
            .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE))
        );

        jTabbedPaneLogs.addTab(resourceMap.getString("jPanel6.TabConstraints.tabTitle"), jPanel6); // NOI18N

        jPanel5.setName("jPanel5"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jTextAreaCompilerOutput.setEditable(false);
        jTextAreaCompilerOutput.setFont(resourceMap.getFont("jTextAreaCompilerOutput.font")); // NOI18N
        jTextAreaCompilerOutput.setName("jTextAreaCompilerOutput"); // NOI18N
        jScrollPane3.setViewportView(jTextAreaCompilerOutput);

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
        );

        jTabbedPaneLogs.addTab(resourceMap.getString("jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPaneLogs)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jTabbedPaneLogs)
                .add(0, 0, 0))
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

        fileMenu.setMnemonic('F');
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

        recent9MenuItem.setName("recent9MenuItem"); // NOI18N
        recentDocsSubmenu.add(recent9MenuItem);

        recentAMenuItem.setName("recentAMenuItem"); // NOI18N
        recentDocsSubmenu.add(recentAMenuItem);

        fileMenu.add(recentDocsSubmenu);

        jSeparator7.setName("jSeparator7"); // NOI18N
        fileMenu.add(jSeparator7);

        saveMenuItem.setAction(actionMap.get("saveFile")); // NOI18N
        saveMenuItem.setName("saveMenuItem"); // NOI18N
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setAction(actionMap.get("saveFileAs")); // NOI18N
        saveAsMenuItem.setName("saveAsMenuItem"); // NOI18N
        fileMenu.add(saveAsMenuItem);

        saveAllMenuItem.setAction(actionMap.get("saveAllFiles")); // NOI18N
        saveAllMenuItem.setName("saveAllMenuItem"); // NOI18N
        fileMenu.add(saveAllMenuItem);

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

        editMenu.setMnemonic('E');
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

        findMenu.setMnemonic('D');
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

        replaceAllMenuItem.setAction(actionMap.get("replaceAll")); // NOI18N
        replaceAllMenuItem.setName("replaceAllMenuItem"); // NOI18N
        findMenu.add(replaceAllMenuItem);

        menuBar.add(findMenu);

        gotoMenu.setMnemonic('N');
        gotoMenu.setText(resourceMap.getString("gotoMenu.text")); // NOI18N
        gotoMenu.setName("gotoMenu"); // NOI18N

        gotoFunctionMenuItem.setAction(actionMap.get("gotoFunction")); // NOI18N
        gotoFunctionMenuItem.setName("gotoFunctionMenuItem"); // NOI18N
        gotoMenu.add(gotoFunctionMenuItem);

        gotoLabelMenuItem.setAction(actionMap.get("gotoLabel")); // NOI18N
        gotoLabelMenuItem.setName("gotoLabelMenuItem"); // NOI18N
        gotoMenu.add(gotoLabelMenuItem);

        gotoLineMenuItem.setAction(actionMap.get("gotoLine")); // NOI18N
        gotoLineMenuItem.setName("gotoLineMenuItem"); // NOI18N
        gotoMenu.add(gotoLineMenuItem);

        gotoMacroMenuItem.setAction(actionMap.get("gotoMacro")); // NOI18N
        gotoMacroMenuItem.setName("gotoMacroMenuItem"); // NOI18N
        gotoMenu.add(gotoMacroMenuItem);

        gotoSectionMenuItem.setAction(actionMap.get("gotoSection")); // NOI18N
        gotoSectionMenuItem.setName("gotoSectionMenuItem"); // NOI18N
        gotoMenu.add(gotoSectionMenuItem);

        jSeparator11.setName("jSeparator11"); // NOI18N
        gotoMenu.add(jSeparator11);

        jumpToLabelMenuItem.setAction(actionMap.get("jumpToLabel")); // NOI18N
        jumpToLabelMenuItem.setName("jumpToLabelMenuItem"); // NOI18N
        gotoMenu.add(jumpToLabelMenuItem);

        jSeparator13.setName("jSeparator13"); // NOI18N
        gotoMenu.add(jSeparator13);

        gotoNextLabel.setAction(actionMap.get("gotoNextLabel")); // NOI18N
        gotoNextLabel.setName("gotoNextLabel"); // NOI18N
        gotoMenu.add(gotoNextLabel);

        gotoPrevLabel.setAction(actionMap.get("gotoPrevLabel")); // NOI18N
        gotoPrevLabel.setName("gotoPrevLabel"); // NOI18N
        gotoMenu.add(gotoPrevLabel);

        jSeparator12.setName("jSeparator12"); // NOI18N
        gotoMenu.add(jSeparator12);

        gotoNextSectionMenuItem.setAction(actionMap.get("gotoNextSection")); // NOI18N
        gotoNextSectionMenuItem.setName("gotoNextSectionMenuItem"); // NOI18N
        gotoMenu.add(gotoNextSectionMenuItem);

        gotoPrevSectionMenuItem.setAction(actionMap.get("gotoPrevSection")); // NOI18N
        gotoPrevSectionMenuItem.setName("gotoPrevSectionMenuItem"); // NOI18N
        gotoMenu.add(gotoPrevSectionMenuItem);

        jSeparator20.setName("jSeparator20"); // NOI18N
        gotoMenu.add(jSeparator20);

        gotoNextErrorMenuItem.setAction(actionMap.get("gotoNextError")); // NOI18N
        gotoNextErrorMenuItem.setName("gotoNextErrorMenuItem"); // NOI18N
        gotoMenu.add(gotoNextErrorMenuItem);

        gotoPrevErrorMenuItem.setAction(actionMap.get("gotoPrevError")); // NOI18N
        gotoPrevErrorMenuItem.setName("gotoPrevErrorMenuItem"); // NOI18N
        gotoMenu.add(gotoPrevErrorMenuItem);

        menuBar.add(gotoMenu);

        sourceMenu.setMnemonic('S');
        sourceMenu.setText(resourceMap.getString("sourceMenu.text")); // NOI18N
        sourceMenu.setName("sourceMenu"); // NOI18N

        runScriptMenuItem.setAction(actionMap.get("runScript")); // NOI18N
        runScriptMenuItem.setText(resourceMap.getString("runScriptMenuItem.text")); // NOI18N
        runScriptMenuItem.setToolTipText(resourceMap.getString("runScriptMenuItem.toolTipText")); // NOI18N
        runScriptMenuItem.setName("runScriptMenuItem"); // NOI18N
        sourceMenu.add(runScriptMenuItem);

        focusScriptMenuItem.setAction(actionMap.get("selectUserScripts")); // NOI18N
        focusScriptMenuItem.setName("focusScriptMenuItem"); // NOI18N
        sourceMenu.add(focusScriptMenuItem);

        focusSyntaxMenuItem.setAction(actionMap.get("selectSyntax")); // NOI18N
        focusSyntaxMenuItem.setName("focusSyntaxMenuItem"); // NOI18N
        sourceMenu.add(focusSyntaxMenuItem);

        jSeparator17.setName("jSeparator17"); // NOI18N
        sourceMenu.add(jSeparator17);

        commentLineMenuItem.setAction(actionMap.get("commentLine")); // NOI18N
        commentLineMenuItem.setName("commentLineMenuItem"); // NOI18N
        sourceMenu.add(commentLineMenuItem);

        jSeparator8.setName("jSeparator8"); // NOI18N
        sourceMenu.add(jSeparator8);

        insertSectionMenuItem.setAction(actionMap.get("insertSection")); // NOI18N
        insertSectionMenuItem.setName("insertSectionMenuItem"); // NOI18N
        sourceMenu.add(insertSectionMenuItem);

        insertSeparatorMenuItem.setAction(actionMap.get("insertSeparatorLine")); // NOI18N
        insertSeparatorMenuItem.setName("insertSeparatorMenuItem"); // NOI18N
        sourceMenu.add(insertSeparatorMenuItem);

        jSeparator14.setName("jSeparator14"); // NOI18N
        sourceMenu.add(jSeparator14);

        insertBreakPointMenuItem.setAction(actionMap.get("insertBreakPoint")); // NOI18N
        insertBreakPointMenuItem.setName("insertBreakPointMenuItem"); // NOI18N
        sourceMenu.add(insertBreakPointMenuItem);

        removeBreakpointMenuItem.setAction(actionMap.get("removeAllBreakPoints")); // NOI18N
        removeBreakpointMenuItem.setMnemonic('R');
        removeBreakpointMenuItem.setName("removeBreakpointMenuItem"); // NOI18N
        sourceMenu.add(removeBreakpointMenuItem);

        jSeparator15.setName("jSeparator15"); // NOI18N
        sourceMenu.add(jSeparator15);

        insertBasicStartMenuItem.setAction(actionMap.get("insertBasicStart")); // NOI18N
        insertBasicStartMenuItem.setMnemonic('B');
        insertBasicStartMenuItem.setName("insertBasicStartMenuItem"); // NOI18N
        sourceMenu.add(insertBasicStartMenuItem);

        insertBytesFromFileMenuItem.setAction(actionMap.get("insertBytesFromFile")); // NOI18N
        insertBytesFromFileMenuItem.setMnemonic('F');
        insertBytesFromFileMenuItem.setName("insertBytesFromFileMenuItem"); // NOI18N
        sourceMenu.add(insertBytesFromFileMenuItem);

        insertSinusMenuItem.setAction(actionMap.get("insertSinusTable")); // NOI18N
        insertSinusMenuItem.setMnemonic('S');
        insertSinusMenuItem.setName("insertSinusMenuItem"); // NOI18N
        sourceMenu.add(insertSinusMenuItem);

        jSeparator16.setName("jSeparator16"); // NOI18N
        sourceMenu.add(jSeparator16);

        viewMainTabMenuItem.setAction(actionMap.get("setFocusToSource")); // NOI18N
        viewMainTabMenuItem.setName("viewMainTabMenuItem"); // NOI18N
        sourceMenu.add(viewMainTabMenuItem);

        menuBar.add(sourceMenu);

        viewMenu.setMnemonic('V');
        viewMenu.setText(resourceMap.getString("viewMenu.text")); // NOI18N
        viewMenu.setName("viewMenu"); // NOI18N

        viewLog1MenuItem.setAction(actionMap.get("selectLog1")); // NOI18N
        viewLog1MenuItem.setMnemonic('R');
        viewLog1MenuItem.setName("viewLog1MenuItem"); // NOI18N
        viewMenu.add(viewLog1MenuItem);

        viewLog2MenuItem.setAction(actionMap.get("selectLog2")); // NOI18N
        viewLog2MenuItem.setMnemonic('C');
        viewLog2MenuItem.setName("viewLog2MenuItem"); // NOI18N
        viewMenu.add(viewLog2MenuItem);

        jSeparator19.setName("jSeparator19"); // NOI18N
        viewMenu.add(jSeparator19);

        switchBothMenuItem.setAction(actionMap.get("switchBothPosition")); // NOI18N
        switchBothMenuItem.setName("switchBothMenuItem"); // NOI18N
        viewMenu.add(switchBothMenuItem);

        switchLogPosMenuItem.setAction(actionMap.get("switchLogPosition")); // NOI18N
        switchLogPosMenuItem.setName("switchLogPosMenuItem"); // NOI18N
        viewMenu.add(switchLogPosMenuItem);

        jSeparator21.setName("jSeparator21"); // NOI18N
        viewMenu.add(jSeparator21);

        quickRefMenuItem.setAction(actionMap.get("showQuickReference")); // NOI18N
        quickRefMenuItem.setMnemonic('Q');
        quickRefMenuItem.setName("quickRefMenuItem"); // NOI18N
        viewMenu.add(quickRefMenuItem);

        menuBar.add(viewMenu);

        helpMenu.setMnemonic('O');
        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        settingsMenuItem.setAction(actionMap.get("settingsWindow")); // NOI18N
        settingsMenuItem.setMnemonic('P');
        settingsMenuItem.setName("settingsMenuItem"); // NOI18N
        helpMenu.add(settingsMenuItem);

        jSeparator9.setName("jSeparator9"); // NOI18N
        helpMenu.add(jSeparator9);

        helpMenuItem.setAction(actionMap.get("showHelp")); // NOI18N
        helpMenuItem.setName("helpMenuItem"); // NOI18N
        helpMenu.add(helpMenuItem);

        jSeparator18.setName("jSeparator18"); // NOI18N
        helpMenu.add(jSeparator18);

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

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

        jLabel9.setDisplayedMnemonic('g');
        jLabel9.setLabelFor(jTextFieldGotoLine);
        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jTextFieldGotoLine.setColumns(5);
        jTextFieldGotoLine.setName("jTextFieldGotoLine"); // NOI18N

        jComboBoxGoto.setName("jComboBoxGoto"); // NOI18N

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .add(statusPanelSeparator)
                .add(239, 239, 239))
            .add(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel9)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldGotoLine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jComboBoxGoto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
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
                .addContainerGap(193, Short.MAX_VALUE))
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jLabel9)
                    .add(jTextFieldGotoLine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6)
                    .add(jTextFieldConvDez, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7)
                    .add(jTextFieldConvHex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8)
                    .add(jTextFieldConvBin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jComboBoxGoto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem addTabMenuItem;
    private javax.swing.JMenuItem closeAllMenuItem;
    private javax.swing.JMenuItem closeFileMenuItem;
    private javax.swing.JMenuItem commentLineMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenu findMenu;
    private javax.swing.JMenuItem findNextMenuItem;
    private javax.swing.JMenuItem findPrevMenuItem;
    private javax.swing.JMenuItem findStartMenuItem;
    private javax.swing.JMenuItem focusScriptMenuItem;
    private javax.swing.JMenuItem focusSyntaxMenuItem;
    private javax.swing.JMenuItem gotoFunctionMenuItem;
    private javax.swing.JMenuItem gotoLabelMenuItem;
    private javax.swing.JMenuItem gotoLineMenuItem;
    private javax.swing.JMenuItem gotoMacroMenuItem;
    private javax.swing.JMenu gotoMenu;
    private javax.swing.JMenuItem gotoNextErrorMenuItem;
    private javax.swing.JMenuItem gotoNextLabel;
    private javax.swing.JMenuItem gotoNextSectionMenuItem;
    private javax.swing.JMenuItem gotoPrevErrorMenuItem;
    private javax.swing.JMenuItem gotoPrevLabel;
    private javax.swing.JMenuItem gotoPrevSectionMenuItem;
    private javax.swing.JMenuItem gotoSectionMenuItem;
    private javax.swing.JMenuItem helpMenuItem;
    private javax.swing.JMenuItem insertBasicStartMenuItem;
    private javax.swing.JMenuItem insertBreakPointMenuItem;
    private javax.swing.JMenuItem insertBytesFromFileMenuItem;
    private javax.swing.JMenuItem insertSectionMenuItem;
    private javax.swing.JMenuItem insertSeparatorMenuItem;
    private javax.swing.JMenuItem insertSinusMenuItem;
    private javax.swing.JButton jButtonFindNext;
    private javax.swing.JButton jButtonFindPrev;
    private javax.swing.JButton jButtonReplace;
    private javax.swing.JButton jButtonRunScript;
    private javax.swing.JComboBox jComboBoxCompilers;
    private javax.swing.JComboBox jComboBoxGoto;
    private javax.swing.JComboBox jComboBoxRunScripts;
    private javax.swing.JEditorPane jEditorPaneMain;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelFind;
    private javax.swing.JPanel jPanelReplace;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPaneMainEditorPane;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator14;
    private javax.swing.JPopupMenu.Separator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator16;
    private javax.swing.JPopupMenu.Separator jSeparator17;
    private javax.swing.JPopupMenu.Separator jSeparator18;
    private javax.swing.JPopupMenu.Separator jSeparator19;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator20;
    private javax.swing.JPopupMenu.Separator jSeparator21;
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
    private javax.swing.JTabbedPane jTabbedPaneLogs;
    private javax.swing.JTextArea jTextAreaCompilerOutput;
    private javax.swing.JTextArea jTextAreaLog;
    private javax.swing.JTextField jTextFieldConvBin;
    private javax.swing.JTextField jTextFieldConvDez;
    private javax.swing.JTextField jTextFieldConvHex;
    private javax.swing.JTextField jTextFieldFind;
    private javax.swing.JTextField jTextFieldGotoLine;
    private javax.swing.JTextField jTextFieldReplace;
    private javax.swing.JMenuItem jumpToLabelMenuItem;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openFileMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem quickRefMenuItem;
    private javax.swing.JMenuItem recent1MenuItem;
    private javax.swing.JMenuItem recent2MenuItem;
    private javax.swing.JMenuItem recent3MenuItem;
    private javax.swing.JMenuItem recent4MenuItem;
    private javax.swing.JMenuItem recent5MenuItem;
    private javax.swing.JMenuItem recent6MenuItem;
    private javax.swing.JMenuItem recent7MenuItem;
    private javax.swing.JMenuItem recent8MenuItem;
    private javax.swing.JMenuItem recent9MenuItem;
    private javax.swing.JMenuItem recentAMenuItem;
    private javax.swing.JMenu recentDocsSubmenu;
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JMenuItem removeBreakpointMenuItem;
    private javax.swing.JMenuItem replaceAllMenuItem;
    private javax.swing.JMenuItem replaceMenuItem;
    private javax.swing.JMenuItem runScriptMenuItem;
    private javax.swing.JMenuItem saveAllMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JMenuItem settingsMenuItem;
    private javax.swing.JMenu sourceMenu;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JMenuItem switchBothMenuItem;
    private javax.swing.JMenuItem switchLogPosMenuItem;
    private javax.swing.JMenuItem undoMenuItem;
    private javax.swing.JMenuItem viewLog1MenuItem;
    private javax.swing.JMenuItem viewLog2MenuItem;
    private javax.swing.JMenuItem viewMainTabMenuItem;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables

    private JDialog aboutBox;
    private QuickReferences quickReferenceDlg;
    private InsertByteFromFileDlg insertByteFromFileDlg;
    private InsertSinusTableDlg insertSinusTableDlg;
    private SettingsDlg settingsDlg;
}
