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
import de.relaunch64.popelganda.Editor.InsertBreakPoint;
import de.relaunch64.popelganda.Editor.LabelExtractor;
import de.relaunch64.popelganda.Editor.SectionExtractor;
import de.relaunch64.popelganda.assemblers.Assembler;
import de.relaunch64.popelganda.assemblers.Assemblers;
import de.relaunch64.popelganda.assemblers.ErrorHandler;
import de.relaunch64.popelganda.database.CustomScripts;
import de.relaunch64.popelganda.database.FindTerms;
import de.relaunch64.popelganda.database.Settings;
import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.FileTools;
import de.relaunch64.popelganda.util.Tools;
import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import org.gjt.sp.jedit.textarea.AntiAlias;
import org.gjt.sp.jedit.textarea.Gutter;
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
    private final EditorPanes editorPanes;
    private final ErrorHandler errorHandler;
    private final FindReplace findReplace;
    private final DefaultListModel<RL64ListItem> listGotoModel = new DefaultListModel<>();
    private int listGotoIndex = -1;
    private final static int GOTO_LABEL = 1;
    private final static int GOTO_SECTION = 2;
    private final static int GOTO_FUNCTION = 3;
    private final static int GOTO_MACRO = 4;
    private final Settings settings;
    private final FindTerms findTerms;
    private final CustomScripts customScripts;
    private Timer hidePanelTimer;
    class HidePanelTimer extends TimerTask {
        @Override public void run() {
            toggleScriptBoxVisibility();
            this.cancel();
        }
    }    
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
        findTerms = new FindTerms();
        customScripts = new CustomScripts();
        errorHandler = new ErrorHandler();
        // load custom scripts
        customScripts.loadScripts();
        // init default laf
        setDefaultLookAndFeel();
        // check for os x
        if (ConstantsR64.IS_OSX) setupMacOSXApplicationListener();
        // init swing components
        initComponents();
        // remove borders on OS X
        if (ConstantsR64.IS_OSX && !settings.getNimbusOnOSX()) {
            jTabbedPane1.setBorder(null);
            jTabbedPaneLogs.setBorder(null);
            jSplitPane1.setBorder(null);
            jSplitPaneEditorList.setBorder(null);
            jScrollPane2.setBorder(new javax.swing.border.MatteBorder(1, 0, 0, 0, Color.lightGray));
            jScrollPane3.setBorder(new javax.swing.border.MatteBorder(1, 0, 0, 0, Color.lightGray));
            jScrollPaneSidebar.setBorder(new javax.swing.border.MatteBorder(0, 0, 1, 0, Color.lightGray));
            jListGoto.setBorder(new javax.swing.border.TitledBorder(UIManager.getBorder("TitledBorder.aquaVariant"), ConstantsR64.CB_GOTO_DEFAULT_STRING));
        }
        // hide find & replace textfield
        jPanelFind.setVisible(false);
        jPanelReplace.setVisible(false);
        jButtonRefreshGoto.setEnabled(false);   
        jPanelSelectScript.setVisible(settings.getShowScriptBox());
        // set compiler log font to monospace
        javax.swing.UIDefaults uid = UIManager.getLookAndFeelDefaults();
        Object defaultFont = uid.get("defaultFont");
        // check if LaF supports default font
        if (defaultFont!=null) {
            jTextAreaCompilerOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, ((Font)defaultFont).getSize()));
        }
        // init combo boxes
        initComboBoxes();
        // init listeners and accelerator table
        initListeners();
        // set sys info
        jTextAreaLog.setText(Tools.getSystemInformation()+System.lineSeparator());
        // set application icon
        getFrame().setIconImage(ConstantsR64.r64icon.getImage());
        getFrame().setTitle(ConstantsR64.APPLICATION_TITLE);
        // init editorpane-dataclass
        editorPanes = new EditorPanes(jTabbedPane1, jComboBoxAssemblers, jComboBoxRunScripts, jLabelBufferSize, this, settings);
        // check if we have any parmater
        if (params!=null && params.length>0) {
            for (String p : params) openFile(new File(p));
        }
        // restore last opened files
        if (settings.getReopenOnStartup()) reopenFiles();
        // open empty if none present
        if (jTabbedPane1.getTabCount()<1) addNewTab();
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
                        // Relaunch64App.getApplication().exit();
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
    /**
     * Init the comboboxes with default values.
     */
    private void initComboBoxes() {
        jSplitPane1.setOrientation(settings.getLogSplitLayout());
        try {
            // init scripts
            initScripts();
            // init assembler combobox
            jComboBoxAssemblers.setSelectedIndex(settings.getPreferredAssembler().getID());
            // select last used script
            jComboBoxRunScripts.setSelectedIndex(settings.getLastUserScript());
            // add custom renderer
            jListGoto.setModel(listGotoModel);
            jListGoto.getSelectionModel().addListSelectionListener(new SelectionListener(jListGoto));
            jListGoto.setCellRenderer(new RL64ListCellRenderer(settings));
        }
        catch (IllegalArgumentException ex) {
        }
    }
    /**
     * Adds available scripts to script-combobox.
     */
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
     * Inits all relevant listeners.
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
//                editorPanes.changeAssembler(jComboBoxCompilers.getSelectedIndex());
//            }
//        });
        jComboBoxAssemblers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (editorPanes.checkIfSyntaxChangeRequired()) {
                    editorPanes.changeAssembler(Assemblers.byID(jComboBoxAssemblers.getSelectedIndex()), jComboBoxRunScripts.getSelectedIndex());
                }
                // update recent doc
                updateRecentDoc();
            }
        });
        jComboBoxRunScripts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                editorPanes.getEditorPaneProperties(jTabbedPane1.getSelectedIndex()).setScript(jComboBoxRunScripts.getSelectedIndex());
                // update recent doc
                updateRecentDoc();
                hidePanelTimer = new Timer();
                hidePanelTimer.schedule(new HidePanelTimer(), 2000);
            }
        });
        jTextFieldGoto.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                // if a navigation-key (arrows, page-down/up, home etc.) is pressed,
                // we assume a new item-selection, so behave like on a mouse-click and
                // filter the links
                int selected = -1;
                if (KeyEvent.VK_UP==evt.getKeyCode()) {
                    selected = jListGoto.getSelectedIndex()-1;
                    if (selected<0) selected = listGotoModel.getSize()-1;
                }
                else if (KeyEvent.VK_DOWN==evt.getKeyCode()) {
                    selected = jListGoto.getSelectedIndex()+1;
                    if (selected>=listGotoModel.getSize()) selected = 0;
                }
                else if (KeyEvent.VK_HOME==evt.getKeyCode()) {
                    selected = 0;
                }
                else if (KeyEvent.VK_END==evt.getKeyCode()) {
                    selected = listGotoModel.getSize()-1;
                }
                else if (KeyEvent.VK_PAGE_DOWN==evt.getKeyCode()) {
                    selected = jListGoto.getSelectedIndex() + (jListGoto.getLastVisibleIndex()-jListGoto.getFirstVisibleIndex());
                    if (selected>=listGotoModel.getSize()) selected = listGotoModel.getSize()-1;
                }
                else if (KeyEvent.VK_PAGE_UP==evt.getKeyCode()) {
                    selected = jListGoto.getSelectedIndex() - (jListGoto.getLastVisibleIndex()-jListGoto.getFirstVisibleIndex());
                    if (selected<0) selected = 0;
                }
                else if (KeyEvent.VK_ENTER==evt.getKeyCode()) {
                    String text = jTextFieldGoto.getText();
                    for (int i=listGotoModel.getSize()-1; i>=0; i--) {
                        RL64ListItem item = listGotoModel.get(i);
                        if (!item.getText().toLowerCase().contains(text.toLowerCase()) && !item.isHeader()) {
                            listGotoModel.remove(i);
                        }
                    }
                    jButtonRefreshGoto.setEnabled(true);
                    evt.consume();
                }
                else if (KeyEvent.VK_ESCAPE==evt.getKeyCode()) {
                    evt.consume();
                    jTextFieldGoto.setText("");
                    toggleGotoListVisibility(true);
                    return;
                }
                else {
                    String text = jTextFieldGoto.getText();
                    if (!text.trim().isEmpty()) {
                        for (int i=0; i<listGotoModel.getSize(); i++) {
                            RL64ListItem item = listGotoModel.get(i);
                            if (item.getText().toLowerCase().startsWith(text.toLowerCase()) && !item.isHeader()) {
                                jListGoto.setSelectedIndex(i);
                                return;
                            }
                        }
                    }
                }
                if (selected!=-1) {
                    jListGoto.setSelectedIndex(selected);
                }
                
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
        jComboBoxFind.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode()==KeyEvent.VK_X && (evt.isControlDown() || evt.isMetaDown())) {
                    ((JTextField)jComboBoxFind.getEditor().getEditorComponent()).cut();
                    evt.consume();
                }
                else if (evt.getKeyCode()==KeyEvent.VK_C && (evt.isControlDown() || evt.isMetaDown())) {
                    ((JTextField)jComboBoxFind.getEditor().getEditorComponent()).copy();
                    evt.consume();
                }
                else if (evt.getKeyCode()==KeyEvent.VK_V && (evt.isControlDown() || evt.isMetaDown())) {
                    ((JTextField)jComboBoxFind.getEditor().getEditorComponent()).paste();
                    evt.consume();
                }
            }
            @Override public void keyReleased(KeyEvent evt) {
                // when the user presses the escape-key, hide panel
                if (KeyEvent.VK_ESCAPE==evt.getKeyCode()) {
                    findCancel();
                }
                else if (KeyEvent.VK_ENTER==evt.getKeyCode()) {
                    findNext();
                }
            }
        });
        jComboBoxFind.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                jComboBoxFind.removeAllItems();
                ArrayList<String> ft = findTerms.getFindTerms();
                if (ft!=null && !ft.isEmpty()) {
                    for (String i : ft) jComboBoxFind.addItem(i);
                }
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }
            @Override public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        jTextFieldReplace.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode()==KeyEvent.VK_X && (evt.isControlDown() || evt.isMetaDown())) {
                    jTextFieldReplace.cut();
                    evt.consume();
                }
                else if (evt.getKeyCode()==KeyEvent.VK_C && (evt.isControlDown() || evt.isMetaDown())) {
                    jTextFieldReplace.copy();
                    evt.consume();
                }
                else if (evt.getKeyCode()==KeyEvent.VK_V && (evt.isControlDown() || evt.isMetaDown())) {
                    jTextFieldReplace.paste();
                    evt.consume();
                }
            }
            @Override public void keyReleased(KeyEvent evt) {
                // when the user presses the escape-key, hide panel
                if (KeyEvent.VK_ESCAPE==evt.getKeyCode()) {
                    replaceCancel();
                }
                else if (KeyEvent.VK_ENTER==evt.getKeyCode()) {
                    replaceTerm();
                }
            }
        });
        jTextFieldGotoLine.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent evt) {
                if (KeyEvent.VK_ENTER==evt.getKeyCode()) {
                    try {
                        int line = Integer.parseInt(jTextFieldGotoLine.getText());
                        editorPanes.gotoLine(line, 1);
                    }
                    catch (NumberFormatException ex) {
                        specialFunctions();
                    }
                }
            }
        });
        jTabbedPane1.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.getButton()==MouseEvent.BUTTON3) {
                    closeFile();
                }
            }
        });
        jTabbedPaneLogs.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.getButton()==MouseEvent.BUTTON3) {
                    switch (jTabbedPaneLogs.getSelectedIndex()) {
                        case 0: clearLog1(); break;
                        case 1: clearLog2(); break;
                    }
                }
            }
        });
        jTextFieldConvDez.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent evt) {
                convertNumber("dez");
            }
        });
        jTextFieldConvHex.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent evt) {
                convertNumber("hex");
            }
        });
        jTextFieldConvBin.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent evt) {
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
        recent1MenuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(1);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(1), settings.getRecentDocScript(1));
                }
        }
        });
        recent2MenuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(2);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(2), settings.getRecentDocScript(2));
                }
            }
        });
        recent3MenuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(3);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(3), settings.getRecentDocScript(3));
                }
            }
        });
        recent4MenuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(4);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(4), settings.getRecentDocScript(4));
                }
            }
        });
        recent5MenuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(5);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(5), settings.getRecentDocScript(5));
                }
            }
        });
        recent6MenuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(6);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(6), settings.getRecentDocScript(6));
                }
            }
        });
        recent7MenuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(7);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(7), settings.getRecentDocScript(7));
                }
            }
        });
        recent8MenuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(8);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(8), settings.getRecentDocScript(8));
                }
            }
        });
        recent9MenuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(9);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(9), settings.getRecentDocScript(9));
                }
            }
        });
        recentAMenuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(10);
                if (fp!=null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(10), settings.getRecentDocScript(10));
                }
            }
        });
        /**
         * JDK 8 Lambda
         */
//        recent1MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
//            File fp = settings.getRecentDoc(1);
//            if (fp!=null && fp.exists()) {
//                openFile(fp, settings.getRecentDocAssembler(1));
//            }
//        });
//        recent2MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
//            File fp = settings.getRecentDoc(2);
//            if (fp!=null && fp.exists()) {
//                openFile(fp, settings.getRecentDocAssembler(2));
//            }
//        });
//        recent3MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
//            File fp = settings.getRecentDoc(3);
//            if (fp!=null && fp.exists()) {
//                openFile(fp, settings.getRecentDocAssembler(3));
//            }
//        });
//        recent4MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
//            File fp = settings.getRecentDoc(4);
//            if (fp!=null && fp.exists()) {
//                openFile(fp, settings.getRecentDocAssembler(4));
//            }
//        });
//        recent5MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
//            File fp = settings.getRecentDoc(5);
//            if (fp!=null && fp.exists()) {
//                openFile(fp, settings.getRecentDocAssembler(5));
//            }
//        });
//        recent6MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
//            File fp = settings.getRecentDoc(6);
//            if (fp!=null && fp.exists()) {
//                openFile(fp, settings.getRecentDocAssembler(6));
//            }
//        });
//        recent7MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
//            File fp = settings.getRecentDoc(7);
//            if (fp!=null && fp.exists()) {
//                openFile(fp, settings.getRecentDocAssembler(7));
//            }
//        });
//        recent8MenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
//            File fp = settings.getRecentDoc(8);
//            if (fp!=null && fp.exists()) {
//                openFile(fp, settings.getRecentDocAssembler(8));
//            }
//        });
        // if we don't have OS X, we need to change action's accelerator keys
        // all "meta" (OS X command key) are converted to "ctrl"
        if (!ConstantsR64.IS_OSX) {
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
        jTextAreaCompilerOutput.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1 || e.getClickCount() != 1) {
                    return;
                }

                try {
                    int offset = jTextAreaCompilerOutput.viewToModel(e.getPoint());
                    int line = jTextAreaCompilerOutput.getLineOfOffset(offset);
                    errorHandler.gotoErrorByClick(editorPanes, jTextAreaCompilerOutput, line + 1);
                } catch (javax.swing.text.BadLocationException e2) {
                }

            }
        });
        jTextAreaCompilerOutput.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode()==KeyEvent.VK_C && (evt.isControlDown() || evt.isMetaDown())) {
                    jTextAreaCompilerOutput.copy();
                    evt.consume();
                }
            }
        });
        jTextAreaLog.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode()==KeyEvent.VK_C && (evt.isControlDown() || evt.isMetaDown())) {
                    jTextAreaLog.copy();
                    evt.consume();
                }
            }
        });
    }
    private class SelectionListener implements ListSelectionListener {
        JList list;
        SelectionListener(JList list) {
            this.list = list;
        }
        @Override
        public void valueChanged(ListSelectionEvent e) {
            // get list selection model
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            // set value-adjusting to true, so we don't fire multiple value-changed events...
            lsm.setValueIsAdjusting(true);
            // make selection visible
            list.ensureIndexIsVisible(jListGoto.getSelectedIndex());
            // goto token
            gotoTokenFromList();
        }
    }
    private void updateListContent(int gotoIndex) {
        updateListContent(gotoIndex, true);
    }
    public void updateListContent() {
        updateListContent(listGotoIndex, false);
    }
    /**
     * Updates the list with Goto-tokens, when a Goto-command from
     * the Navigation-menu is called. This method retrieves all requested
     * tokens (labels, macros, sections...) and puts them into the JList.
     * <br><br>
     * For more information on cell-rendering, see
     * {@link RL64ListCellRenderer RL64ListCellRenderer} and
     * {@link RL64ListItem RL64ListItem}
     * 
     * @param gotoIndex indicates which Goto-command from the Navigate-menu
     * was used. One of following values:
     * <ul>
     *  <li>{@link #GOTO_FUNCTION}</li>
     *  <li>{@link #GOTO_LABEL}</li>
     *  <li>{@link #GOTO_MACRO}</li>
     *  <li>{@link #GOTO_SECTION}</li>
     * </ul>
     * @param focusToTextfield {@code true} if the textfield for the Goto-list should
     * gain the input focus. Not useful in all cases, e.g. when opening new files, 
     * the user does not expect the input focus to be outside the editor.
     */
    private void updateListContent(int gotoIndex, boolean focusToTextfield) {
        // save index for list listener
        listGotoIndex = gotoIndex;
        listGotoModel.clear();
        // create a list with index numbers of tabs
        // (i.e. all index numbers of opened source code tabs)
        ArrayList<Integer> eps = new ArrayList<>();
        // add current activated source code index first
        eps.add(jTabbedPane1.getSelectedIndex());
        // than add indices of remaining tabs
        for (int i=0; i<editorPanes.getCount(); i++) {
            if (!eps.contains(i)) eps.add(i);
        }
        // go through all opened editorpanes
        // eps.stream().forEach((epIndex) -> {
        for (int epIndex : eps) {
            // extract and retrieve sections from each editor pane
            ArrayList<String> token;
            String borderTitle;
            switch (gotoIndex) {
                case GOTO_SECTION:
                    token = SectionExtractor.getSectionNames(editorPanes.getSourceCode(epIndex), editorPanes.getActiveAssembler().getLineComment());
                    borderTitle = ConstantsR64.CB_GOTO_SECTION_STRING;
                    break;
                case GOTO_LABEL:
                    token = LabelExtractor.getNames(LabelExtractor.getLabels(editorPanes.getSourceCode(epIndex), editorPanes.getAssembler(epIndex), 0).labels);
                    borderTitle = ConstantsR64.CB_GOTO_LABEL_STRING;
                    break;
                case GOTO_FUNCTION:
                    token = LabelExtractor.getNames(LabelExtractor.getLabels(editorPanes.getSourceCode(epIndex), editorPanes.getAssembler(epIndex), 0).functions);
                    borderTitle = ConstantsR64.CB_GOTO_FUNCTION_STRING;
                    break;
                case GOTO_MACRO:
                    token = LabelExtractor.getNames(LabelExtractor.getLabels(editorPanes.getSourceCode(epIndex), editorPanes.getAssembler(epIndex), 0).macros);
                    borderTitle = ConstantsR64.CB_GOTO_MACRO_STRING;
                    break;
                default:
                    listGotoIndex = GOTO_LABEL;
                    token = LabelExtractor.getNames(LabelExtractor.getLabels(editorPanes.getSourceCode(epIndex), editorPanes.getAssembler(epIndex), 0).labels);
                    borderTitle = ConstantsR64.CB_GOTO_LABEL_STRING;
                    break;
            }
            // check if anything found
            if (token!=null && !token.isEmpty()) {
                // make splitpane visible if necessary
                toggleGotoListVisibility(false);
                // add header item
                File fp = editorPanes.getFilePath(epIndex);
                // a list item has several properties now, which will be rendered:
                // item text, icon, is header?, line number (not used), file path
                listGotoModel.addElement(new RL64ListItem(FileTools.getFileName(fp), null, true, 0, fp));
                // sort list
                Collections.sort(token);
                // add all found section strings to combo box
                for (String arg : token) {
                    // items have a small margin, headings do not
                    listGotoModel.addElement(new RL64ListItem(arg, null, false, 0, fp));
                }
                // set border-title
                TitledBorder tb = (TitledBorder) jListGoto.getBorder();
                tb.setTitle(borderTitle);
            }
            // disable refresh button
            jButtonRefreshGoto.setEnabled(false);            
            // clear textfield
            jTextFieldGoto.setText("");
            if (focusToTextfield) jTextFieldGoto.requestFocusInWindow();
            // reset scroll bars for sidebar
            javax.swing.JScrollBar verticalScrollBar = jScrollPaneSidebar.getVerticalScrollBar();
            javax.swing.JScrollBar horizontalScrollBar = jScrollPaneSidebar.getHorizontalScrollBar();
            verticalScrollBar.setValue(verticalScrollBar.getMinimum());
            horizontalScrollBar.setValue(horizontalScrollBar.getMinimum());
        }
    }
    /**
     * Toggles the visibility of the Goto-list, i.e. either collapses or
     * expands the splitpane on demand.
     */
    @Action
    public void toggleGotoListVisibility() {
        boolean collapse = jSplitPaneEditorList.getRightComponent().getWidth()>1;
        toggleGotoListVisibility(collapse);
        if (!collapse) jTextFieldGoto.requestFocusInWindow();
    }
    /**
     * Toggles the visibility of the Goto-list, i.e. either collapses or
     * expands the splitpane on demand.
     * 
     * @param collapse {@code true} if splitpane should be collapsed (i.e.
     * hide Goto-list), {@code false} if it should be expanded (i.e. made visible).
     * If, for instance, {@code collapse} is {@code true} and splitpane already
     * collapsed, nothing will happen.
     */
    private void toggleGotoListVisibility(boolean collapse) {
        // initial value, if nothing changes
        String arrowButton = null;
        int width = 0;
        // check if splitpane is hidden (collapsed) and should be expanded
        if(0==jSplitPaneEditorList.getRightComponent().getWidth() && !collapse) {
            // "click" on the left arrow button
            arrowButton = "leftButton";
            // set default width for exception
            width = (int)getFrame().getWidth()*3/4;
        }
        // check if goto-list is visible and should be collapsed
        else if(jSplitPaneEditorList.getRightComponent().getWidth()>0 && collapse) {
            // "click" on the right arrow button
            arrowButton = "rightButton";
            // set default width for exception
            width = getFrame().getWidth();
            editorPanes.setFocus();
        }
        // check if we have any change for splitpane state
        if (arrowButton!=null) {
            try {
                // get "arrow" buttons from oneTouchExpandable splitPane
                Field buttonField = BasicSplitPaneDivider.class.getDeclaredField(arrowButton);
                buttonField.setAccessible(true);
                javax.swing.JButton button = (javax.swing.JButton) buttonField.get(((BasicSplitPaneUI) jSplitPaneEditorList.getUI()).getDivider());
                // simulate button click
                button.doClick();
                // may have to update splitpane ui
                jSplitPaneEditorList.updateUI();
                jSplitPaneEditorList.doLayout();
            }
            catch (NoSuchFieldException | NullPointerException | SecurityException | IllegalAccessException ex) {
                jSplitPaneEditorList.setDividerLocation(width);
            }
        }
    }
    /**
     * Parses input from the Goto-line-textfield. Used for quickly changing settings,
     * may also be used for special debug-functions or information etc.
     */
    private void specialFunctions() {
        String text = jTextFieldGotoLine.getText();
        switch (text) {
            case "aa":
                settings.setAntiAlias(AntiAlias.STANDARD);
                editorPanes.setAntiAlias(settings.getAntiAlias());
                break;
            case "aas":
                settings.setAntiAlias(AntiAlias.SUBPIXEL);
                editorPanes.setAntiAlias(settings.getAntiAlias());
                break;
            case "aan":
                settings.setAntiAlias(AntiAlias.NONE);
                editorPanes.setAntiAlias(settings.getAntiAlias());
                break;
            case "cs":
                settings.setAlternativeAssemblyMode(false);
                editorPanes.updateAssemblyMode();
                break;
            case "csa":
                settings.setAlternativeAssemblyMode(true);
                editorPanes.updateAssemblyMode();
                break;
            case "lal":
                settings.setLineNumerAlignment(Gutter.LEFT);
                editorPanes.setLineNumberAlignment(settings.getLineNumerAlignment());
                break;
            case "lac":
                settings.setLineNumerAlignment(Gutter.CENTER);
                editorPanes.setLineNumberAlignment(settings.getLineNumerAlignment());
                break;
            case "lar":
                settings.setLineNumerAlignment(Gutter.RIGHT);
                editorPanes.setLineNumberAlignment(settings.getLineNumerAlignment());
                break;
            case "cf":
                editorPanes.collapseAllFolds();
                editorPanes.setFocus();
                break;
            case "ef":
                editorPanes.expandAllFolds();
                editorPanes.setFocus();
                break;
            case "slh":
                settings.setShowLineHightlight(!settings.getShowLineHightlight());
                editorPanes.updateColorScheme();
                break;
            case "sfe":
                settings.setShowExtensionInTab(!settings.getShowExtensionInTab());
                editorPanes.updateTitles();
                break;
            case "scb":
                settings.setShowCloseButton(!settings.getShowCloseButton());
                editorPanes.updateTabCloseButtons();
                break;
            case "scf":
                settings.setCodeFolding(!settings.getCodeFolding());
                editorPanes.updateCodeFolding();
                break;
            case "ch":
                String compilerHelp = customScripts.getCompilerHelp(jComboBoxRunScripts.getSelectedItem());
                if (compilerHelp!=null) {
                    jTextAreaLog.append(System.lineSeparator()+System.lineSeparator()+compilerHelp);
                    jTabbedPaneLogs.setSelectedIndex(0);
                }
                break;
        }
        try {
            if (text.startsWith("cs")) {
                int nr = Integer.parseInt(text.substring(2));
                if (nr>=1 && nr<=ColorSchemes.SCHEME_NAMES.length) {
                    settings.setColorScheme(nr-1);
                    editorPanes.updateColorScheme();
                }
            }
            else if (text.startsWith("fs")) {
                int size = Integer.parseInt(text.substring(2));
                settings.setMainfont(new Font(settings.getMainfont(Settings.FONTNAME), Font.PLAIN, size));
                editorPanes.setFonts(settings.getMainFont());
            }
            else if (text.startsWith("ts")) {
                int size = Integer.parseInt(text.substring(2));
                settings.setTabWidth(size);
                editorPanes.setTabs(size);
            }
        }
        catch(IndexOutOfBoundsException | NumberFormatException ex) {
        }
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
     * Removes or adds a menu item for recently opened documents.
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
    private void gotoTokenFromList() {
        // retrieve index of selected item
        Object selectedObject = jListGoto.getSelectedValue();
        // check if > 0 and key was return
        if (selectedObject!=null) {
            // remembler focus owner
            java.awt.Component c = this.getFrame().getFocusOwner();
            final String compname = (c!=null) ? c.getName() : null;
            // retrieve item information
            RL64ListItem listItem = (RL64ListItem) selectedObject;
            // retrieve opened tab
            int epIndex = editorPanes.getOpenedFileTab(listItem.getFile());
            // go on, if tab is opened and no heading is selected
            if (epIndex!=-1 && !listItem.isHeader()) {
                // select specific tab where selected section is
                jTabbedPane1.setSelectedIndex(epIndex);
                // select where to go...
                switch (listGotoIndex) {
                    case GOTO_FUNCTION:
                        editorPanes.gotoFunction(listItem.getText(), epIndex);
                        break;
                    case GOTO_SECTION:
                        editorPanes.gotoSection(listItem.getText(), epIndex);
                        break;
                    case GOTO_LABEL:
                        editorPanes.gotoLabel(listItem.getText(), epIndex);
                    case GOTO_MACRO:
                        editorPanes.gotoMacro(listItem.getText(), epIndex);
                        break;
                }
            }
            // scrolling to caret sets focus to editor component,
            // but we don't want that.
            if (compname!=null) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        switch (compname) {
                            case "jListGoto":
                                jListGoto.requestFocusInWindow();
                                break;
                            default:
                                jTextFieldGoto.requestFocusInWindow();
                                // remove selection on focus, so prev. input
                                // is not selected and deleted by typing
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            jTextFieldGoto.setCaretPosition(jTextFieldGoto.getText().length());
                                        }
                                        catch (IllegalArgumentException ex) {
                                        }
                                    }
                                });
                                break;
                        }
                    }
                });
            }
        }
    }
    public void toggleScriptBoxVisibility() {
        jPanelSelectScript.setVisible(settings.getShowScriptBox());
    }
    @Action
    public void refreshGotoList() {
        updateListContent(listGotoIndex);
    }
    @Action
    public void spacesToTabs() {
        editorPanes.getActiveEditorPane().spacesToTabs();
    }
    @Action
    public void allSpacesToTabs() {
        for (int i=0; i<editorPanes.getCount(); i++) {
            editorPanes.getEditorPane(i).spacesToTabs();
        }
    }
    @Action
    public void tabsToSpaces() {
        editorPanes.getActiveEditorPane().tabsToSpaces();
    }
    @Action
    public void allTabsToSpaces() {
        for (int i=0; i<editorPanes.getCount(); i++) {
            editorPanes.getEditorPane(i).tabsToSpaces();
        }
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
    @Action
    public void surroundFolds() {
        editorPanes.insertFolds();
    }
    @Action
    public void expandFolds() {
        editorPanes.getActiveEditorPane().expandFold(true);
    }
    @Action
    public void collapseFolds() {
        editorPanes.getActiveEditorPane().collapseFold();
    }
    @Action
    public void expandAllFolds() {
        editorPanes.expandAllFolds();
        editorPanes.setFocus();
    }
    @Action
    public void collapseAllFolds() {
        editorPanes.collapseAllFolds();
        editorPanes.setFocus();
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
            settingsDlg = new SettingsDlg(getFrame(), this, settings, customScripts, editorPanes);
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
    public void cutAction() {
        editorPanes.getActiveEditorPane().cutText();
    }
    @Action
    public void copyAction() {
        editorPanes.getActiveEditorPane().copyText();
    }
    @Action
    public void pasteAction() {
        editorPanes.getActiveEditorPane().pasteText();
    }
    @Action
    public void gotoLine() {
        jTextFieldGotoLine.requestFocusInWindow();
    }
    @Action
    public void gotoSection() {
        updateListContent(GOTO_SECTION);
    }
    @Action
    public void gotoLabel() {
        updateListContent(GOTO_LABEL);
    }
    @Action
    public void gotoFunction() {
        updateListContent(GOTO_FUNCTION);
    }
    @Action
    public void gotoMacro() {
        updateListContent(GOTO_MACRO);
    }
    @Action
    public void jumpToLabel() {
        // get word under caret
        editorPanes.gotoLabel(editorPanes.getActiveEditorPane().getCaretString(true, ""));
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
        // goto next section
        editorPanes.gotoNextSection();
    }
    @Action
    public void gotoPrevSection() {
        // goto previous section
        editorPanes.gotoPrevSection();
    }
    @Action
    public void gotoNextError() {
        errorHandler.gotoNextError(editorPanes, jTextAreaCompilerOutput);
    }
    @Action
    public void gotoPrevError() {
        errorHandler.gotoPrevError(editorPanes, jTextAreaCompilerOutput);
    }
    @Action
    public void gotoNextLabel() {
        // goto next label
        editorPanes.gotoNextLabel();
    }
    @Action
    public void gotoPrevLabel() {
        // goto previous label
        editorPanes.gotoPrevLabel();
    }
    @Action
    public void setFocusToSource() {
        editorPanes.setFocus();
    }
    /**
     * 
     */
    @Action
    public final void addNewTab() {
        editorPanes.addNewTab(null, null, "untitled", settings.getPreferredAssembler(), jComboBoxRunScripts.getSelectedIndex());
    }
    @Action
    public void openFile() {
        File fileToOpen = FileTools.chooseFile(getFrame(), JFileChooser.OPEN_DIALOG, JFileChooser.FILES_ONLY, settings.getLastUsedPath().getAbsolutePath(), "", "Open ASM File", ConstantsR64.FILE_EXTENSIONS, "ASM-Files");
        openFile(fileToOpen);
    }
    private void openFile(File fileToOpen) {
        openFile(fileToOpen, settings.getPreferredAssembler());
    }
    private void openFile(File fileToOpen, Assembler assembler) {
        openFile(fileToOpen, assembler, jComboBoxRunScripts.getSelectedIndex());
    }
    private void openFile(File fileToOpen, Assembler assembler, int script) {
        // check if file could be opened
        if (editorPanes.loadFile(fileToOpen, assembler, script)) {
            // add file path to recent documents history
            settings.addToRecentDocs(fileToOpen.toString(), assembler, script);
            // and update menus
            setRecentDocuments();
            // save last used path
            settings.setLastUsedPath(fileToOpen);
            // select combobox item
            try {
                jComboBoxAssemblers.setSelectedIndex(assembler.getID());
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
            settings.setRecentDoc(rd, cf.toString(), Assemblers.byID(jComboBoxAssemblers.getSelectedIndex()), jComboBoxRunScripts.getSelectedIndex());
        }
    }
    private void reopenFiles() {
        // get reopen files
        ArrayList<Object[]> files = settings.getReopenFiles();
        // check if we have any
        if (files!=null && !files.isEmpty()) {
            // retrieve set
            for (Object[] o : files) {
                // retrieve data: file path
                File fp = new File(o[0].toString());
                // retrieve data: assembler
                Assembler assembler = (Assembler)o[1];
                // retrieve data: script
                int script = Integer.parseInt(o[2].toString());
                // open file
                openFile(fp, assembler, script);
            }
        }
    }
    @Action
    public void saveFile() {
        if (editorPanes.saveFile()) {
            // add file path to recent documents history
            settings.addToRecentDocs(editorPanes.getActiveFilePath().getPath(), Assemblers.byID(jComboBoxAssemblers.getSelectedIndex()), jComboBoxRunScripts.getSelectedIndex());
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
            if (jTabbedPane1.getTabCount()<1) addNewTab();
        }
    }
    /**
     * Closes all opened tabs
     */
    @Action
    public void closeAll() {
        // get current amount of tabes
        int count = jTabbedPane1.getTabCount();
        // and close file as often as we have tabs
        for (int cnt=0; cnt<count; cnt++) {
            closeFile();
        }
    }
    /**
     * Saves a file under a new file name / path.
     */
    @Action
    public void saveFileAs() {
        if (editorPanes.saveFileAs()) {
            // add file path to recent documents history
            settings.addToRecentDocs(editorPanes.getActiveFilePath().getPath(), Assemblers.byID(jComboBoxAssemblers.getSelectedIndex()), jComboBoxRunScripts.getSelectedIndex());
            // and update menus
            setRecentDocuments();
        }
    }
    /**
     * Runs the currently selected user script and - depending on the script - compiles the source code
     * and start the compiled source in an emulator. If errors occur during the compile process,
     * the error log is shown and the caret jumps to the related position in the source.
     */
    @Action
    public void runScript() {
        // check if user defined custom script
        String script = Tools.getCustomScriptName(editorPanes.getActiveSourceCode(), editorPanes.getActiveAssembler().getLineComment());
        // init cb-item
        Object item;
        // if we found no custom script in source, or script name was not found,
        // select script from combo box
        if (null==script || -1==customScripts.findScript(script)) {
            // get selected item
            item = jComboBoxRunScripts.getSelectedItem();
            // valid selection?
            if (item!=null) {
                // get script
                script = customScripts.getScript(item.toString());
            }
        }
        else {
            // we have found a valid script name, so get script
            script = customScripts.getScript(script);
        }
        // valid script?
        if (script!=null && !script.isEmpty()) {
            // log offset
            int offset = 0;
            // clear old log
            clearLog1();
            clearLog2();
            // clesr error lines
            errorHandler.clearErrors();
            // convert CRLF to LF (WIN)
            script = script.replaceAll("\r\n", "\n");
            // convert CR to LF (MAC)
            script = script.replaceAll("\r", "\n");
            // retrieve script lines
            String[] lines = script.split("\n");
            // check if source file needs to be saved and auto save is active
            if (editorPanes.getActiveFilePath() == null || (settings.getSaveOnCompile() && editorPanes.isModified())) editorPanes.saveFile();
            // retrieve ASM-Source file
            File sourceFile = editorPanes.getActiveFilePath();
            // no file :(
            if (sourceFile == null) return;
            // set base path for relative paths
            errorHandler.setBasePath(sourceFile);
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
                    // surround pathes with quotes, if necessary
                    String sf = sourceFile.toString();
                    if (sf.contains(" ") && !sf.startsWith("\"") && !sf.startsWith("'")) sf = "\""+sf+"\"";
                    String of = outFile.toString();
                    if (of.contains(" ") && !of.startsWith("\"") && !of.startsWith("'")) of = "\""+of+"\"";
                    String cf = compressedFile.toString();
                    if (cf.contains(" ") && !cf.startsWith("\"") && !cf.startsWith("'")) cf = "\""+cf+"\"";
                    // check if we have relative paths
                    boolean useRelativePath = cmd.contains(Assembler.OUTPUT_FILE_REL);
                    // create relative paths of in- and output files
                    String sf_rel = FileTools.getFileName(sourceFile)+"."+FileTools.getFileExtension(sourceFile);
                    String of_rel = FileTools.getFileName(outFile)+"."+FileTools.getFileExtension(outFile);
                    String cf_rel = FileTools.getFileName(compressedFile)+"."+FileTools.getFileExtension(compressedFile);
                    // replace placeholders
                    cmd = cmd.replace(Assembler.INPUT_FILE_REL, sf_rel);
                    cmd = cmd.replace(Assembler.INPUT_FILE, sf);
                    cmd = cmd.replace(Assembler.INPUT_FILENAME, FileTools.getFileName(sourceFile));
                    cmd = cmd.replace(Assembler.OUTPUT_FILE_REL, of_rel);
                    cmd = cmd.replace(Assembler.OUTPUT_FILE, of);
                    cmd = cmd.replace(Assembler.OUTPUT_FILENAME, FileTools.getFileName(outFile));
                    cmd = cmd.replace(ConstantsR64.ASSEMBLER_UNCOMPRESSED_FILE, (useRelativePath) ? of_rel : of);
                    cmd = cmd.replace(ConstantsR64.ASSEMBLER_COMPRESSED_FILE, (useRelativePath) ? cf_rel : cf);
                    cmd = cmd.replace(Assembler.SOURCE_DIR, parentFile);
                    // check if we have a cruncher-starttoken
                    String cruncherStart = Tools.getCruncherStart(editorPanes.getActiveSourceCode(), editorPanes.getActiveAssembler().getLineComment());
                    // if we found cruncher-starttoken, replace placeholder 
                    if (cruncherStart!=null) cmd = cmd.replace(ConstantsR64.ASSEMBLER_START_ADDRESS, cruncherStart);
                    try {
                        // log process
                        log = "Converted script-line: "+cmd;
                        ConstantsR64.r64logger.log(Level.INFO, log);
                        // write output to string builder. we need output both for printing
                        // it to the text area log, and to examine the string for possible errors.
                        StringBuilder compilerLog = new StringBuilder("");
                        ProcessBuilder pb;
                        Process p;
                        // Start ProcessBuilder
                        pb = new ProcessBuilder(cmd.split(" "));
                        // set parent directory to sourcecode fie
                        pb = pb.directory(sourceFile.getParentFile());
                        pb = pb.redirectInput(Redirect.PIPE).redirectError(Redirect.PIPE);
                        // start process
                        p = pb.start();
                        // create scanner to receive compiler messages
                        try (Scanner sc = new Scanner(p.getInputStream()).useDelimiter(System.lineSeparator())) {
                            // write output to string builder
                            while (sc.hasNextLine()) {
                                compilerLog.append(sc.nextLine()).append(System.lineSeparator());
                            }
                        }
                        try (Scanner sc = new Scanner(p.getErrorStream()).useDelimiter(System.lineSeparator())) {
                            // write output to string builder
                            while (sc.hasNextLine()) {
                                compilerLog.append(sc.nextLine()).append(System.lineSeparator());
                            }
                        }
                        // print log to text area
                        jTextAreaCompilerOutput.append(compilerLog.toString());
                        // wait for other process to be finished
                        p.waitFor();
                        p.destroy();
                        // read and extract errors from log
                        offset = errorHandler.readErrorLines(compilerLog.toString(), editorPanes.getActiveAssembler(), offset);
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
            // select assembler log if we have errors or any compiling information
            // if we have no content in assembler log nor any errors, don't show assembler log
            if (errorHandler.hasErrors() || !jTextAreaCompilerOutput.getText().trim().isEmpty()) {
                // show error log
                jTabbedPaneLogs.setSelectedIndex(1);
                // show first error
                if (errorHandler.hasErrors()) errorHandler.gotoFirstError(editorPanes, jTextAreaCompilerOutput);
            }
        }
        else {
            JOptionPane.showMessageDialog(getFrame(), "Please open preferences and add a 'compile and run' script first!");
        }
    }
    @Action
    public void gotoNextFold() {
        editorPanes.getActiveEditorPane().goToNextFold(false);
    }
    @Action
    public void gotoPrevFold() {
        editorPanes.getActiveEditorPane().goToPrevFold(false);
    }
    @Action
    public void switchLogPosition() {
        int currentlayout = settings.getLogSplitLayout();
        currentlayout = (JSplitPane.HORIZONTAL_SPLIT == currentlayout) ? JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT;
        settings.setLogSplitLayout(currentlayout);
        jSplitPane1.setOrientation(currentlayout);
    }
    @Action
    public void clearLog1() {
        // set sys info
        jTextAreaLog.setText(Tools.getSystemInformation()+System.lineSeparator());
    }
    @Action
    public void clearLog2() {
        // set sys info
        jTextAreaCompilerOutput.setText("");
    }
    @Action
    public void selectUserScripts() {
        // show panel
        jPanelSelectScript.setVisible(true);
        jComboBoxRunScripts.showPopup();
        jComboBoxRunScripts.requestFocusInWindow();
    }
    @Action
    public void selectSyntax() {
        jComboBoxAssemblers.showPopup();
        jComboBoxAssemblers.requestFocusInWindow();
    }
    @Action
    public void selectLog() {
        int selected = jTabbedPaneLogs.getSelectedIndex();
        jTabbedPaneLogs.setSelectedIndex((0==selected) ? 1 : 0);
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
        jComboBoxFind.requestFocusInWindow();
    }
    @Action
    public void findNext() {
        Object ft = jComboBoxFind.getSelectedItem();
        if (ft!=null) {
            String findTerm = ft.toString();
            findTerms.addFindTerm(findTerm, jCheckBoxWholeWord.isSelected(), jCheckBoxMatchCase.isSelected());
            findReplace.initValues(findTerm, 
                                   jTextFieldReplace.getText(), 
                                   jTabbedPane1.getSelectedIndex(), 
                                   editorPanes.getActiveEditorPane(), 
                                   jCheckBoxRegEx.isSelected(), 
                                   jCheckBoxWholeWord.isSelected(),
                                   jCheckBoxMatchCase.isSelected());
            jComboBoxFind.setForeground(findReplace.findNext(jCheckBoxRegEx.isSelected(), 
                                                             jCheckBoxWholeWord.isSelected(), 
                                                             jCheckBoxMatchCase.isSelected()) ? Color.black : Color.red);
        }
    }
    @Action
    public void findPrev() {
        Object ft = jComboBoxFind.getSelectedItem();
        if (ft!=null) {
            String findTerm = ft.toString();
            findTerms.addFindTerm(findTerm, jCheckBoxWholeWord.isSelected(), jCheckBoxMatchCase.isSelected());
            findReplace.initValues(findTerm, 
                                   jTextFieldReplace.getText(), 
                                   jTabbedPane1.getSelectedIndex(), 
                                   editorPanes.getActiveEditorPane(), 
                                   jCheckBoxRegEx.isSelected(),
                                   jCheckBoxWholeWord.isSelected(),
                                   jCheckBoxMatchCase.isSelected());
            jComboBoxFind.setForeground(findReplace.findPrev(jCheckBoxRegEx.isSelected(), 
                                                             jCheckBoxWholeWord.isSelected(), 
                                                             jCheckBoxMatchCase.isSelected()) ? Color.black : Color.red);
        }
    }
    private void findCancel() {
        // cancel replace
        replaceCancel();
        // reset values
        findReplace.resetValues();
        jComboBoxFind.setForeground(Color.black);
        // make it visible
        jPanelFind.setVisible(false);
        // set input focus in main textfield
        editorPanes.setFocus();
    }
    @Action
    public void replaceAll() {
        // check if we have selection
        Object sel = jComboBoxFind.getSelectedItem();
        if (sel!=null) {
            findReplace.initValues(jComboBoxFind.getSelectedItem().toString(),
                                   jTextFieldReplace.getText(), 
                                   jTabbedPane1.getSelectedIndex(), 
                                   editorPanes.getActiveEditorPane(),
                                   jCheckBoxRegEx.isSelected(),
                                   jCheckBoxWholeWord.isSelected(),
                                   jCheckBoxMatchCase.isSelected());
            int findCounter = 0;
            while (findReplace.replace(jCheckBoxRegEx.isSelected(), jCheckBoxWholeWord.isSelected(), jCheckBoxMatchCase.isSelected())) findCounter++;
            JOptionPane.showMessageDialog(getFrame(), String.valueOf(findCounter)+" occurences were replaced.");
        }
    }
    @Action
    public void replaceTerm() {
        // make it visible
        jPanelFind.setVisible(true);
        // check if we have selection
        Object sel = jComboBoxFind.getSelectedItem();
        // check whether textfield is visible
        if (!jPanelReplace.isVisible()) {
            // make it visible
            jPanelReplace.setVisible(true);
            // and set input focus in it
            if (null==sel || sel.toString().isEmpty()) {
                jComboBoxFind.requestFocusInWindow();
            }
            else {
                jTextFieldReplace.requestFocusInWindow();
            }
        }
        // if textfield is already visible, replace term
        else {
            if (sel!=null) {
                findReplace.initValues(jComboBoxFind.getSelectedItem().toString(), 
                                       jTextFieldReplace.getText(), 
                                       jTabbedPane1.getSelectedIndex(), 
                                       editorPanes.getActiveEditorPane(),
                                       jCheckBoxRegEx.isSelected(),
                                       jCheckBoxWholeWord.isSelected(),
                                       jCheckBoxMatchCase.isSelected());
                jTextFieldReplace.setForeground(findReplace.replace(jCheckBoxRegEx.isSelected(), jCheckBoxWholeWord.isSelected(), jCheckBoxMatchCase.isSelected()) ? Color.black : Color.red);
            }
        }
    }
    private void replaceCancel() {
        jTextFieldReplace.setForeground(Color.black);
        // hide replace textfield
        jPanelReplace.setVisible(false);
        // for some reasons, hiding the  find pane 
        // set input focus in main textfield
        editorPanes.setFocus();
    }
    @Action
    public void insertBytesFromFile() {
        // open dialog
        if (null==insertByteFromFileDlg) {
            insertByteFromFileDlg = new InsertByteFromFileDlg(getFrame(), settings, editorPanes.getActiveAssembler());
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
            insertSinusTableDlg = new InsertSinusTableDlg(getFrame(), editorPanes.getActiveAssembler());
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
        String nimbusclassname = "";
        String aquaclassname = "";
        // in case we find "nimbus" LAF, set this as default on non-mac-os
        // because it simply looks the best.
        for (UIManager.LookAndFeelInfo laf : installed_laf) {
            // check whether laf is nimbus
            if (laf.getClassName().toLowerCase().contains("aqua")) {
                aquaclassname = laf.getClassName();
            }
            if (laf.getClassName().toLowerCase().contains("nimbus")) {
                nimbusclassname = laf.getClassName();
            }
        }
        try {
            // check which laf was found and set appropriate default value 
            if (!aquaclassname.isEmpty() && !settings.getNimbusOnOSX()) {
                UIManager.setLookAndFeel(aquaclassname);
            }
            // check which laf was found and set appropriate default value 
            else if (!nimbusclassname.isEmpty()) {
                UIManager.setLookAndFeel(nimbusclassname);
            }
            else {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
        }
        if (ConstantsR64.IS_OSX) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", ConstantsR64.APPLICATION_SHORT_TITLE);
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        // System.setProperty("awt.useSystemAAFontSettings", "on");
        if (settings.getScaleFont()) {
            try { // Try to scale default font size according to screen resolution.
                Font fm = (Font)UIManager.getLookAndFeelDefaults().get("defaultFont");
                // check if laf supports default font
                if (fm!=null) {
                    UIManager.getLookAndFeelDefaults().put("defaultFont", fm.deriveFont(fm.getSize2D() * Toolkit.getDefaultToolkit().getScreenResolution() / 96));
                }
            } catch (HeadlessException e) { }
        }
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
        List<File> filesToOpen = Tools.drop(dtde, editorPanes);
        if (filesToOpen!=null && !filesToOpen.isEmpty()) {
            for (File f : filesToOpen) openFile(f);
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
        }
        @Override
        public void close() throws SecurityException {
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
        jSplitPaneEditorList = new javax.swing.JSplitPane();
        jPanel7 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelFind = new javax.swing.JPanel();
        jButtonFindPrev = new javax.swing.JButton();
        jButtonFindNext = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jCheckBoxRegEx = new javax.swing.JCheckBox();
        jCheckBoxWholeWord = new javax.swing.JCheckBox();
        jCheckBoxMatchCase = new javax.swing.JCheckBox();
        jComboBoxFind = new javax.swing.JComboBox();
        jPanelReplace = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldReplace = new javax.swing.JTextField();
        jButtonReplace = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jScrollPaneSidebar = new javax.swing.JScrollPane();
        jListGoto = new javax.swing.JList();
        jTextFieldGoto = new javax.swing.JTextField();
        jButtonRefreshGoto = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPaneLogs = new javax.swing.JTabbedPane();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaLog = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextAreaCompilerOutput = new javax.swing.JTextArea();
        jPanelSelectScript = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxRunScripts = new javax.swing.JComboBox();
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
        jSeparator24 = new javax.swing.JPopupMenu.Separator();
        jMenuItemTabsToSpaces = new javax.swing.JMenuItem();
        jMenuItemAllTabsToSpaces = new javax.swing.JMenuItem();
        jSeparator25 = new javax.swing.JPopupMenu.Separator();
        jMenuItemSpaceToTab = new javax.swing.JMenuItem();
        jMenuItemAllSpaceToTab = new javax.swing.JMenuItem();
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
        jSeparator21 = new javax.swing.JPopupMenu.Separator();
        jMenuItemNextFold = new javax.swing.JMenuItem();
        jMenuItemPrevFold = new javax.swing.JMenuItem();
        sourceMenu = new javax.swing.JMenu();
        runScriptMenuItem = new javax.swing.JMenuItem();
        focusScriptMenuItem = new javax.swing.JMenuItem();
        focusSyntaxMenuItem = new javax.swing.JMenuItem();
        jSeparator17 = new javax.swing.JPopupMenu.Separator();
        commentLineMenuItem = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        jMenuItemSurroundFolds = new javax.swing.JMenuItem();
        jMenuItemExpandFold = new javax.swing.JMenuItem();
        jMenuItemExpandAllFolds = new javax.swing.JMenuItem();
        jMenuItemCollapseFold = new javax.swing.JMenuItem();
        jMenuItemCollapseAllFolds = new javax.swing.JMenuItem();
        jSeparator23 = new javax.swing.JPopupMenu.Separator();
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
        jMenuItemShowHideGoto = new javax.swing.JMenuItem();
        viewLog1MenuItem = new javax.swing.JMenuItem();
        jSeparator19 = new javax.swing.JPopupMenu.Separator();
        switchLogPosMenuItem = new javax.swing.JMenuItem();
        jSeparator22 = new javax.swing.JPopupMenu.Separator();
        quickRefMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        settingsMenuItem = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        helpMenuItem = new javax.swing.JMenuItem();
        jSeparator18 = new javax.swing.JPopupMenu.Separator();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldConvDez = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldConvHex = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldConvBin = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldGotoLine = new javax.swing.JTextField();
        jLabelBufferSize = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxAssemblers = new javax.swing.JComboBox();

        mainPanel.setName("mainPanel"); // NOI18N

        jSplitPane1.setDividerLocation(350);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setName("jSplitPane1"); // NOI18N
        jSplitPane1.setOneTouchExpandable(true);

        jPanel1.setName("jPanel1"); // NOI18N

        jSplitPaneEditorList.setDividerLocation(550);
        jSplitPaneEditorList.setName("jSplitPaneEditorList"); // NOI18N
        jSplitPaneEditorList.setOneTouchExpandable(true);

        jPanel7.setName("jPanel7"); // NOI18N

        jTabbedPane1.setMinimumSize(new java.awt.Dimension(100, 100));
        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanelFind.setName("jPanelFind"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getContext().getActionMap(Relaunch64View.class, this);
        jButtonFindPrev.setAction(actionMap.get("findPrev")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.relaunch64.popelganda.Relaunch64App.class).getContext().getResourceMap(Relaunch64View.class);
        jButtonFindPrev.setIcon(resourceMap.getIcon("jButtonFindPrev.icon")); // NOI18N
        jButtonFindPrev.setText(resourceMap.getString("jButtonFindPrev.text")); // NOI18N
        jButtonFindPrev.setName("jButtonFindPrev"); // NOI18N

        jButtonFindNext.setAction(actionMap.get("findNext")); // NOI18N
        jButtonFindNext.setIcon(resourceMap.getIcon("jButtonFindNext.icon")); // NOI18N
        jButtonFindNext.setText(resourceMap.getString("jButtonFindNext.text")); // NOI18N
        jButtonFindNext.setName("jButtonFindNext"); // NOI18N

        jLabel5.setDisplayedMnemonic('i');
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jCheckBoxRegEx.setMnemonic('x');
        jCheckBoxRegEx.setText(resourceMap.getString("jCheckBoxRegEx.text")); // NOI18N
        jCheckBoxRegEx.setToolTipText(resourceMap.getString("jCheckBoxRegEx.toolTipText")); // NOI18N
        jCheckBoxRegEx.setName("jCheckBoxRegEx"); // NOI18N

        jCheckBoxWholeWord.setMnemonic('w');
        jCheckBoxWholeWord.setText(resourceMap.getString("jCheckBoxWholeWord.text")); // NOI18N
        jCheckBoxWholeWord.setToolTipText(resourceMap.getString("jCheckBoxWholeWord.toolTipText")); // NOI18N
        jCheckBoxWholeWord.setName("jCheckBoxWholeWord"); // NOI18N

        jCheckBoxMatchCase.setMnemonic('c');
        jCheckBoxMatchCase.setText(resourceMap.getString("jCheckBoxMatchCase.text")); // NOI18N
        jCheckBoxMatchCase.setToolTipText(resourceMap.getString("jCheckBoxMatchCase.toolTipText")); // NOI18N
        jCheckBoxMatchCase.setName("jCheckBoxMatchCase"); // NOI18N

        jComboBoxFind.setEditable(true);
        jComboBoxFind.setToolTipText(resourceMap.getString("jComboBoxFind.toolTipText")); // NOI18N
        jComboBoxFind.setName("jComboBoxFind"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanelFindLayout = new org.jdesktop.layout.GroupLayout(jPanelFind);
        jPanelFind.setLayout(jPanelFindLayout);
        jPanelFindLayout.setHorizontalGroup(
            jPanelFindLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelFindLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelFindLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanelFindLayout.createSequentialGroup()
                        .add(jCheckBoxRegEx)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jCheckBoxWholeWord)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jCheckBoxMatchCase)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(jPanelFindLayout.createSequentialGroup()
                        .add(jComboBoxFind, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonFindPrev)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonFindNext)))
                .addContainerGap())
        );
        jPanelFindLayout.setVerticalGroup(
            jPanelFindLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelFindLayout.createSequentialGroup()
                .add(jPanelFindLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jLabel5)
                    .add(jButtonFindPrev)
                    .add(jButtonFindNext)
                    .add(jComboBoxFind, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelFindLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jCheckBoxRegEx)
                    .add(jCheckBoxWholeWord)
                    .add(jCheckBoxMatchCase))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 549, Short.MAX_VALUE)
            .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanelFind, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanelReplace, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 347, Short.MAX_VALUE)
            .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel7Layout.createSequentialGroup()
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(jPanelFind, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(jPanelReplace, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName(resourceMap.getString("jTabbedPane1.AccessibleContext.accessibleName")); // NOI18N

        jSplitPaneEditorList.setLeftComponent(jPanel7);

        jPanel8.setName("jPanel8"); // NOI18N

        jScrollPaneSidebar.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 1, 0, resourceMap.getColor("jScrollPaneSidebar.border.matteColor"))); // NOI18N
        jScrollPaneSidebar.setName("jScrollPaneSidebar"); // NOI18N

        jListGoto.setBorder(javax.swing.BorderFactory.createTitledBorder(ConstantsR64.CB_GOTO_DEFAULT_STRING));
        jListGoto.setName("jListGoto"); // NOI18N
        jScrollPaneSidebar.setViewportView(jListGoto);

        jTextFieldGoto.setToolTipText(resourceMap.getString("jTextFieldGoto.toolTipText")); // NOI18N
        jTextFieldGoto.setName("jTextFieldGoto"); // NOI18N

        jButtonRefreshGoto.setAction(actionMap.get("refreshGotoList")); // NOI18N
        jButtonRefreshGoto.setBorder(null);
        jButtonRefreshGoto.setBorderPainted(false);
        jButtonRefreshGoto.setContentAreaFilled(false);
        jButtonRefreshGoto.setName("jButtonRefreshGoto"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPaneSidebar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
            .add(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .add(jTextFieldGoto)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButtonRefreshGoto)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .add(jScrollPaneSidebar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jTextFieldGoto)
                    .add(jButtonRefreshGoto, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jSplitPaneEditorList.setRightComponent(jPanel8);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSplitPaneEditorList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 796, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPaneEditorList)
        );

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel2.setName("jPanel2"); // NOI18N

        jTabbedPaneLogs.setName("jTabbedPaneLogs"); // NOI18N

        jPanel6.setName("jPanel6"); // NOI18N

        jScrollPane2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTextAreaLog.setEditable(false);
        jTextAreaLog.setWrapStyleWord(true);
        jTextAreaLog.setName("jTextAreaLog"); // NOI18N
        jScrollPane2.setViewportView(jTextAreaLog);

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 775, Short.MAX_VALUE)
            .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 775, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 136, Short.MAX_VALUE)
            .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE))
        );

        jTabbedPaneLogs.addTab(resourceMap.getString("jPanel6.TabConstraints.tabTitle"), jPanel6); // NOI18N

        jPanel5.setName("jPanel5"); // NOI18N

        jScrollPane3.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));
        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jTextAreaCompilerOutput.setEditable(false);
        jTextAreaCompilerOutput.setName("jTextAreaCompilerOutput"); // NOI18N
        jScrollPane3.setViewportView(jTextAreaCompilerOutput);

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 775, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
        );

        jTabbedPaneLogs.addTab(resourceMap.getString("jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        jPanelSelectScript.setName("jPanelSelectScript"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jComboBoxRunScripts.setName("jComboBoxRunScripts"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanelSelectScriptLayout = new org.jdesktop.layout.GroupLayout(jPanelSelectScript);
        jPanelSelectScript.setLayout(jPanelSelectScriptLayout);
        jPanelSelectScriptLayout.setHorizontalGroup(
            jPanelSelectScriptLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelSelectScriptLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jComboBoxRunScripts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelSelectScriptLayout.setVerticalGroup(
            jPanelSelectScriptLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelSelectScriptLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelSelectScriptLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jComboBoxRunScripts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPaneLogs)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelSelectScript, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jTabbedPaneLogs)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelSelectScript, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
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

        cutMenuItem.setAction(actionMap.get("cutAction")); // NOI18N
        cutMenuItem.setName("cutMenuItem"); // NOI18N
        editMenu.add(cutMenuItem);

        copyMenuItem.setAction(actionMap.get("copyAction")); // NOI18N
        copyMenuItem.setName("copyMenuItem"); // NOI18N
        editMenu.add(copyMenuItem);

        pasteMenuItem.setAction(actionMap.get("pasteAction")); // NOI18N
        pasteMenuItem.setName("pasteMenuItem"); // NOI18N
        editMenu.add(pasteMenuItem);

        jSeparator5.setName("jSeparator5"); // NOI18N
        editMenu.add(jSeparator5);

        selectAllMenuItem.setAction(actionMap.get("selectAllText")); // NOI18N
        selectAllMenuItem.setName("selectAllMenuItem"); // NOI18N
        editMenu.add(selectAllMenuItem);

        jSeparator24.setName("jSeparator24"); // NOI18N
        editMenu.add(jSeparator24);

        jMenuItemTabsToSpaces.setAction(actionMap.get("tabsToSpaces")); // NOI18N
        jMenuItemTabsToSpaces.setName("jMenuItemTabsToSpaces"); // NOI18N
        editMenu.add(jMenuItemTabsToSpaces);

        jMenuItemAllTabsToSpaces.setAction(actionMap.get("allTabsToSpaces")); // NOI18N
        jMenuItemAllTabsToSpaces.setName("jMenuItemAllTabsToSpaces"); // NOI18N
        editMenu.add(jMenuItemAllTabsToSpaces);

        jSeparator25.setName("jSeparator25"); // NOI18N
        editMenu.add(jSeparator25);

        jMenuItemSpaceToTab.setAction(actionMap.get("spacesToTabs")); // NOI18N
        jMenuItemSpaceToTab.setName("jMenuItemSpaceToTab"); // NOI18N
        editMenu.add(jMenuItemSpaceToTab);

        jMenuItemAllSpaceToTab.setAction(actionMap.get("allSpacesToTabs")); // NOI18N
        jMenuItemAllSpaceToTab.setName("jMenuItemAllSpaceToTab"); // NOI18N
        editMenu.add(jMenuItemAllSpaceToTab);

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

        jSeparator21.setName("jSeparator21"); // NOI18N
        gotoMenu.add(jSeparator21);

        jMenuItemNextFold.setAction(actionMap.get("gotoNextFold")); // NOI18N
        jMenuItemNextFold.setName("jMenuItemNextFold"); // NOI18N
        gotoMenu.add(jMenuItemNextFold);

        jMenuItemPrevFold.setAction(actionMap.get("gotoPrevFold")); // NOI18N
        jMenuItemPrevFold.setName("jMenuItemPrevFold"); // NOI18N
        gotoMenu.add(jMenuItemPrevFold);

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

        jMenuItemSurroundFolds.setAction(actionMap.get("surroundFolds")); // NOI18N
        jMenuItemSurroundFolds.setName("jMenuItemSurroundFolds"); // NOI18N
        sourceMenu.add(jMenuItemSurroundFolds);

        jMenuItemExpandFold.setAction(actionMap.get("expandFolds")); // NOI18N
        jMenuItemExpandFold.setName("jMenuItemExpandFold"); // NOI18N
        sourceMenu.add(jMenuItemExpandFold);

        jMenuItemExpandAllFolds.setAction(actionMap.get("expandAllFolds")); // NOI18N
        jMenuItemExpandAllFolds.setName("jMenuItemExpandAllFolds"); // NOI18N
        sourceMenu.add(jMenuItemExpandAllFolds);

        jMenuItemCollapseFold.setAction(actionMap.get("collapseFolds")); // NOI18N
        jMenuItemCollapseFold.setName("jMenuItemCollapseFold"); // NOI18N
        sourceMenu.add(jMenuItemCollapseFold);

        jMenuItemCollapseAllFolds.setAction(actionMap.get("collapseAllFolds")); // NOI18N
        jMenuItemCollapseAllFolds.setName("jMenuItemCollapseAllFolds"); // NOI18N
        sourceMenu.add(jMenuItemCollapseAllFolds);

        jSeparator23.setName("jSeparator23"); // NOI18N
        sourceMenu.add(jSeparator23);

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

        jMenuItemShowHideGoto.setAction(actionMap.get("toggleGotoListVisibility")); // NOI18N
        jMenuItemShowHideGoto.setName("jMenuItemShowHideGoto"); // NOI18N
        viewMenu.add(jMenuItemShowHideGoto);

        viewLog1MenuItem.setAction(actionMap.get("selectLog")); // NOI18N
        viewLog1MenuItem.setMnemonic('R');
        viewLog1MenuItem.setName("viewLog1MenuItem"); // NOI18N
        viewMenu.add(viewLog1MenuItem);

        jSeparator19.setName("jSeparator19"); // NOI18N
        viewMenu.add(jSeparator19);

        switchLogPosMenuItem.setAction(actionMap.get("switchLogPosition")); // NOI18N
        switchLogPosMenuItem.setToolTipText(resourceMap.getString("switchLogPosMenuItem.toolTipText")); // NOI18N
        switchLogPosMenuItem.setName("switchLogPosMenuItem"); // NOI18N
        viewMenu.add(switchLogPosMenuItem);

        jSeparator22.setName("jSeparator22"); // NOI18N
        viewMenu.add(jSeparator22);

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
        jLabel9.setToolTipText(resourceMap.getString("jLabel9.toolTipText")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jTextFieldGotoLine.setColumns(5);
        jTextFieldGotoLine.setToolTipText(resourceMap.getString("jTextFieldGotoLine.toolTipText")); // NOI18N
        jTextFieldGotoLine.setName("jTextFieldGotoLine"); // NOI18N

        jLabelBufferSize.setName("jLabelBufferSize"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(resourceMap.getString("jLabel1.toolTipText")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jComboBoxAssemblers.setModel(new javax.swing.DefaultComboBoxModel(Assemblers.names()));
        jComboBoxAssemblers.setToolTipText(resourceMap.getString("jComboBoxAssemblers.toolTipText")); // NOI18N
        jComboBoxAssemblers.setName("jComboBoxAssemblers"); // NOI18N

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel9)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldGotoLine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jComboBoxAssemblers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 170, Short.MAX_VALUE)
                .add(jLabelBufferSize)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jLabel9)
                    .add(jTextFieldGotoLine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6)
                    .add(jTextFieldConvDez, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7)
                    .add(jTextFieldConvHex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8)
                    .add(jTextFieldConvBin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelBufferSize)
                    .add(jLabel1)
                    .add(jComboBoxAssemblers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
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
    private javax.swing.JButton jButtonRefreshGoto;
    private javax.swing.JButton jButtonReplace;
    private javax.swing.JCheckBox jCheckBoxMatchCase;
    private javax.swing.JCheckBox jCheckBoxRegEx;
    private javax.swing.JCheckBox jCheckBoxWholeWord;
    private javax.swing.JComboBox jComboBoxAssemblers;
    private javax.swing.JComboBox jComboBoxFind;
    private javax.swing.JComboBox jComboBoxRunScripts;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelBufferSize;
    private javax.swing.JList jListGoto;
    private javax.swing.JMenuItem jMenuItemAllSpaceToTab;
    private javax.swing.JMenuItem jMenuItemAllTabsToSpaces;
    private javax.swing.JMenuItem jMenuItemCollapseAllFolds;
    private javax.swing.JMenuItem jMenuItemCollapseFold;
    private javax.swing.JMenuItem jMenuItemExpandAllFolds;
    private javax.swing.JMenuItem jMenuItemExpandFold;
    private javax.swing.JMenuItem jMenuItemNextFold;
    private javax.swing.JMenuItem jMenuItemPrevFold;
    private javax.swing.JMenuItem jMenuItemShowHideGoto;
    private javax.swing.JMenuItem jMenuItemSpaceToTab;
    private javax.swing.JMenuItem jMenuItemSurroundFolds;
    private javax.swing.JMenuItem jMenuItemTabsToSpaces;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanelFind;
    private javax.swing.JPanel jPanelReplace;
    private javax.swing.JPanel jPanelSelectScript;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPaneSidebar;
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
    private javax.swing.JPopupMenu.Separator jSeparator22;
    private javax.swing.JPopupMenu.Separator jSeparator23;
    private javax.swing.JPopupMenu.Separator jSeparator24;
    private javax.swing.JPopupMenu.Separator jSeparator25;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPaneEditorList;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPaneLogs;
    private javax.swing.JTextArea jTextAreaCompilerOutput;
    private javax.swing.JTextArea jTextAreaLog;
    private javax.swing.JTextField jTextFieldConvBin;
    private javax.swing.JTextField jTextFieldConvDez;
    private javax.swing.JTextField jTextFieldConvHex;
    private javax.swing.JTextField jTextFieldGoto;
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
    private javax.swing.JMenuItem switchLogPosMenuItem;
    private javax.swing.JMenuItem undoMenuItem;
    private javax.swing.JMenuItem viewLog1MenuItem;
    private javax.swing.JMenuItem viewMainTabMenuItem;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables

    private JDialog aboutBox;
    private QuickReferences quickReferenceDlg;
    private InsertByteFromFileDlg insertByteFromFileDlg;
    private InsertSinusTableDlg insertSinusTableDlg;
    private SettingsDlg settingsDlg;
}
