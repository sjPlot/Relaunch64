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

package de.relaunch64.popelganda;

import de.relaunch64.popelganda.Editor.ColorSchemes;
import de.relaunch64.popelganda.Editor.EditorPaneProperties;
import de.relaunch64.popelganda.Editor.EditorPanes;
import de.relaunch64.popelganda.Editor.InsertBreakPoint;
import de.relaunch64.popelganda.Editor.LabelExtractor;
import de.relaunch64.popelganda.Editor.RL64TextArea;
import de.relaunch64.popelganda.Editor.SectionExtractor;
import de.relaunch64.popelganda.assemblers.Assembler;
import de.relaunch64.popelganda.assemblers.Assemblers;
import de.relaunch64.popelganda.assemblers.ErrorHandler;
import de.relaunch64.popelganda.database.CustomScripts;
import de.relaunch64.popelganda.database.FindTerms;
import de.relaunch64.popelganda.database.Settings;
import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.FileTools;
import de.relaunch64.popelganda.util.IgnoreCaseComparator;
import de.relaunch64.popelganda.util.Tools;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.ProcessBuilder.Redirect;
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
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import org.gjt.sp.jedit.textarea.AntiAlias;
import org.gjt.sp.jedit.textarea.Gutter;
import org.gjt.sp.jedit.textarea.Selection;
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
    public int listGotoIndex = -1;
    private final static int GOTO_LABEL = 1;
    private final static int GOTO_SECTION = 2;
    private final static int GOTO_FUNCTION = 3;
    private final static int GOTO_MACRO = 4;
    private boolean dontSaveDividerLocation = false;
    private Timer resizeTimer = null;
    private final Settings settings;
    private final FindTerms findTerms;
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
        findTerms = new FindTerms();
        customScripts = new CustomScripts();
        errorHandler = new ErrorHandler();
        // load custom scripts
        customScripts.loadScripts();
        listGotoIndex = settings.getListGotoIndex();
        // init default laf
        setDefaultLookAndFeel();
        // check for os x
        if (ConstantsR64.IS_OSX) {
            setupMacOSXApplicationListener();
        }
        // init swing components
        initComponents();
        // remove borders on OS X
        if (ConstantsR64.IS_OSX && !settings.getNimbusOnOSX()) {
            jScrollPaneLog.setBorder(new javax.swing.border.MatteBorder(1, 0, 0, 0, Color.lightGray));
            jScrollPaneErrorLog.setBorder(new javax.swing.border.MatteBorder(1, 0, 0, 0, Color.lightGray));
            jScrollPaneSidebar.setBorder(new javax.swing.border.MatteBorder(1, 0, 1, 0, Color.darkGray));
            jTextFieldGoto.putClientProperty("JTextField.variant", "search");
            jSplitPane1.setBackground(new Color(128, 128, 128));
            jSplitPaneEditorList.setBackground(new Color(128, 128, 128));
            jSplitPane1.setDividerSize(2);
            jSplitPaneEditorList.setDividerSize(2);
            jSplitPane1.setOneTouchExpandable(false);
        }
        // hide find & replace textfield
        jPanelFind.setVisible(false);
        jPanelReplace.setVisible(false);
        jTabbedPane1.setTabLayoutPolicy(settings.getUseScrollTabs() ? JTabbedPane.SCROLL_TAB_LAYOUT : JTabbedPane.WRAP_TAB_LAYOUT);
        // set compiler log font to monospace
        javax.swing.UIDefaults uid = UIManager.getLookAndFeelDefaults();
        Object defaultFont = uid.get("defaultFont");
        // check if LaF supports default font
        if (defaultFont != null) {
            jTextAreaCompilerOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, ((Font) defaultFont).getSize()));
        }
        // init combo boxes
        initComboBoxes();
        // init listeners and accelerator table
        initListeners();
        // set sys info
        jTextAreaLog.setText(Tools.getSystemInformation() + System.lineSeparator());
        // set application icon
        getFrame().setIconImage(ConstantsR64.r64icon.getImage());
        getFrame().setTitle(ConstantsR64.APPLICATION_TITLE);
        // show/hide toolbar
        toggleToolbar();
        // init editorpane-dataclass
        editorPanes = new EditorPanes(jTabbedPane1, jComboBoxAssemblers, jComboBoxRunScripts, jLabelBufferSize, this, settings);
        // check if we have any parmater
        if (params != null && params.length > 0) {
            for (String p : params) {
                openFile(new File(p));
            }
        }
        // restore last opened files
        if (settings.getReopenOnStartup()) {
            reopenFiles();
        }
        // open empty if none present
        if (jTabbedPane1.getTabCount() < 1) {
            addNewTab();
        }
        // finally, check for updates
        checkForUpdates();
    }

    /**
     * This is an application listener that is initialised when running the program on mac os x. by
     * using this appListener, we can use the typical apple-menu bar which provides own about,
     * preferences and quit-menu-items.
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
            Object listener = Proxy.newProxyInstance(lc.getClassLoader(), new Class[]{lc}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
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
                            ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
                        }
                    }
                    return null;
                }

                private void setHandled(Object event, Boolean val) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
                    Method handleMethod = event.getClass().getMethod("setHandled", new Class[]{boolean.class});
                    handleMethod.invoke(event, new Object[]{val});
                }
            });
            try {
                // add application listener that listens to actions on the apple menu items
                Method m = appc.getMethod("addApplicationListener", lc);
                m.invoke(app, listener);
                // register that we want that Preferences menu. by default, only the about box is shown
                // but no pref-menu-item
                Method enablePreferenceMethod = appc.getMethod("setEnabledPreferencesMenu", new Class[]{boolean.class});
                enablePreferenceMethod.invoke(app, new Object[]{Boolean.TRUE});
            } catch (NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                ConstantsR64.r64logger.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            ConstantsR64.r64logger.log(Level.SEVERE, e.getLocalizedMessage());
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
            // default sorting of sidebar
            jComboBoxSortSidebar.setSelectedIndex(settings.getSidebarSort());
            // add custom renderer
            jListGoto.setModel(listGotoModel);
            jListGoto.getSelectionModel().addListSelectionListener(new SelectionListener(jListGoto));
            jListGoto.setCellRenderer(new RL64ListCellRenderer(settings));
        } catch (IllegalArgumentException ex) {
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
        if (scriptNames != null && scriptNames.length > 0) {
            // sort
            Arrays.sort(scriptNames);
            // add item to cb
            for (String sn : scriptNames) {
                jComboBoxRunScripts.addItem(sn);
            }
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
        JFrame r64frame = Relaunch64View.super.getFrame();
        r64frame.addWindowListener(this);
        r64frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // create resize timer. when goto-sidebar is hidden and window
        // is being resized, the split pane expands. the timer tracks the
        // resizing and hides the sidebar, if necessary
        resizeTimer = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (settings.getSidebarIsHidden()) {
                    toggleGotoListVisibility(settings.getSidebarIsHidden());
                }
                resizeTimer.stop();
            }
        });
        // resize listener to hide goto-sidebar if necessray
        r64frame.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (resizeTimer.isRunning()) {
                    resizeTimer.restart();
                } else {
                    resizeTimer.start();
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
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
                    editorPanes.changeAssembler(Assemblers.byID(jComboBoxAssemblers.getSelectedIndex()), 
                            jComboBoxRunScripts.getSelectedIndex(), editorPanes.getActiveAlternativeScript());
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
            }
        });
        jComboBoxSortSidebar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                settings.setSidebarSort(jComboBoxSortSidebar.getSelectedIndex());
                updateListContent(listGotoIndex);
            }
        });
        jTextFieldGoto.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                // if a navigation-key (arrows, page-down/up, home etc.) is pressed,
                // we assume a new item-selection, so behave like on a mouse-click and
                // filter the links
                int selected = -1;
                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        selected = jListGoto.getSelectedIndex() - 1;
                        if (selected < 0) {
                            selected = listGotoModel.getSize() - 1;
                        }   break;
                    case KeyEvent.VK_DOWN:
                        selected = jListGoto.getSelectedIndex() + 1;
                        if (selected >= listGotoModel.getSize()) {
                            selected = 0;
                        }   break;
                    case KeyEvent.VK_HOME:
                        selected = 0;
                        break;
                    case KeyEvent.VK_END:
                        selected = listGotoModel.getSize() - 1;
                        break;
                    case KeyEvent.VK_PAGE_DOWN:
                        selected = jListGoto.getSelectedIndex() + (jListGoto.getLastVisibleIndex() - jListGoto.getFirstVisibleIndex());
                        if (selected >= listGotoModel.getSize()) {
                            selected = listGotoModel.getSize() - 1;
                        }   break;
                    case KeyEvent.VK_PAGE_UP:
                        selected = jListGoto.getSelectedIndex() - (jListGoto.getLastVisibleIndex() - jListGoto.getFirstVisibleIndex());
                        if (selected < 0) {
                            selected = 0;
                        }   break;
                    case KeyEvent.VK_ENTER:
                        {
                            // get input
                            String text = jTextFieldGoto.getText();
                            // copy current list items into dummy list model
                            DefaultListModel<RL64ListItem> dummy = new DefaultListModel<>();
                            for (int i = 0; i < listGotoModel.getSize(); i++) {
                                dummy.addElement(listGotoModel.get(i));
                            }       // find items that can be removed
                            for (int i = listGotoModel.getSize() - 1; i >= 0; i--) {
                                RL64ListItem item = listGotoModel.get(i);
                                if (!item.getText().toLowerCase().contains(text.toLowerCase()) && !item.isHeader() && !item.isTitle()) {
                                    listGotoModel.remove(i);
                                }
                            }       // count items (excluding header and title) of list
                            int itemCount = 0;
                            for (int i = 0; i < listGotoModel.getSize(); i++) {
                                if (!listGotoModel.get(i).isHeader() && !listGotoModel.get(i).isTitle()) {
                                    itemCount++;
                                }
                            }       // if we have no items, filtering was insufficient. hence, restore old
                            // list by copying back from dummy
                            if (0 == itemCount) {
                                listGotoModel.clear();
                                for (int i = 0; i < dummy.getSize(); i++) {
                                    listGotoModel.addElement(dummy.get(i));
                                }
                                // indicate "not found" with red color
                                jTextFieldGoto.setForeground(new Color(160, 40, 40));
                            } else {
                                // matched filter-text, so black color
                                jTextFieldGoto.setForeground(Color.black);
                            }       evt.consume();
                            break;
                        }
                    case KeyEvent.VK_ESCAPE:
                        evt.consume();
                        jTextFieldGoto.setText("");
                        toggleGotoListVisibility(true);
                        return;
                    default:
                        {
                            String text = jTextFieldGoto.getText();
                            if (!text.trim().isEmpty()) {
                                for (int i = 0; i < listGotoModel.getSize(); i++) {
                                    RL64ListItem item = listGotoModel.get(i);
                                    if (item.getText().toLowerCase().startsWith(text.toLowerCase()) && !item.isHeader()) {
                                        jListGoto.setSelectedIndex(i);
                                        return;
                                    }
                                }
                            }       break;
                        }
                }
                if (selected != -1) {
                    jListGoto.setSelectedIndex(selected);
                }

            }
        });
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                editorPanes.updateTabbedPane();
                // and reset find/replace values, because the content has changed and former
                // find-index-values are no longer valid
                findReplace.resetValues();
                // check for external changes
                checkExternalFileChange();
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
        jComboBoxFind.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                jComboBoxFind.removeAllItems();
                ArrayList<String> ft = findTerms.getFindTerms();
                if (ft != null && !ft.isEmpty()) {
                    for (String i : ft) {
                        jComboBoxFind.addItem(i);
                    }
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        jComboBoxFind.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_X && (evt.isControlDown() || evt.isMetaDown())) {
                    ((JTextField) jComboBoxFind.getEditor().getEditorComponent()).cut();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_C && (evt.isControlDown() || evt.isMetaDown())) {
                    ((JTextField) jComboBoxFind.getEditor().getEditorComponent()).copy();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_V && (evt.isControlDown() || evt.isMetaDown())) {
                    ((JTextField) jComboBoxFind.getEditor().getEditorComponent()).paste();
                    evt.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent evt) {
                // when the user presses the escape-key, hide panel
                if (KeyEvent.VK_ESCAPE == evt.getKeyCode()) {
                    findCancel();
                    return;
                }
                if (KeyEvent.VK_ENTER == evt.getKeyCode()) {
                    if (!settings.getFindByType() || settings.getFindFieldFocus()) {
                        findNext();
                    } else {
                        editorPanes.getActiveEditorPane().requestFocusInWindow();
                    }
                    return;
                }
                if (settings.getFindByType() && !evt.isActionKey()) {
                    // get textfield component
                    JTextField tf = (JTextField) jComboBoxFind.getEditor().getEditorComponent();
                    // get find text
                    String text = tf.getText();
                    if (text != null && !text.isEmpty()) {
                        // get editor pane
                        RL64TextArea editorPane = editorPanes.getActiveEditorPane();
                        // get source code
                        String content = editorPane.getBuffer().getText();
                        // create ignore-case pattern for findtext
                        Pattern p = Pattern.compile("(?i)" + Pattern.quote(text));
                        // find pattern
                        Matcher m = p.matcher(content);
                        // any occurences?
                        if (m.find()) {
                            // if yes, select first occurence in source code
                            editorPane.setCaretPosition(m.start());
                            editorPane.scrollToCaret(true);
                            editorPane.setSelection(new Selection.Range(m.start(), m.end()));
                        }
                    }
                }

            }
        });

        jTextFieldReplace.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_X && (evt.isControlDown() || evt.isMetaDown())) {
                    jTextFieldReplace.cut();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_C && (evt.isControlDown() || evt.isMetaDown())) {
                    jTextFieldReplace.copy();
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_V && (evt.isControlDown() || evt.isMetaDown())) {
                    jTextFieldReplace.paste();
                    evt.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent evt) {
                // when the user presses the escape-key, hide panel
                if (KeyEvent.VK_ESCAPE == evt.getKeyCode()) {
                    replaceCancel();
                } else if (KeyEvent.VK_ENTER == evt.getKeyCode()) {
                    replaceTerm();
                }
            }
        });
        jTextFieldGotoLine.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                if (KeyEvent.VK_ENTER == evt.getKeyCode()) {
                    try {
                        int line = Integer.parseInt(jTextFieldGotoLine.getText());
                        editorPanes.gotoLine(line, 1);
                    } catch (NumberFormatException ex) {
                        specialFunctions();
                    }
                }
            }
        });
        jTabbedPane1.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3) {
                    closeFile();
                }
            }
        });
        jTabbedPaneLogs.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3) {
                    switch (jTabbedPaneLogs.getSelectedIndex()) {
                        case 0:
                            clearLog1();
                            break;
                        case 1:
                            clearLog2();
                            break;
                    }
                }
            }
        });
        jTextFieldConvDez.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                convertNumber("dez");
            }
        });
        jTextFieldConvHex.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                convertNumber("hex");
            }
        });
        jTextFieldConvBin.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                convertNumber("bin");
            }
        });
        jSplitPaneEditorList.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (dontSaveDividerLocation) {
                    dontSaveDividerLocation = false;
                } else {
                    settings.setDividerLocation(jSplitPaneEditorList.getDividerLocation());
                }
            }
        });
        recentDocsSubmenu.addMenuListener(new javax.swing.event.MenuListener() {
            @Override
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                setRecentDocumentMenuItem(recent1MenuItem, 1);
                setRecentDocumentMenuItem(recent2MenuItem, 2);
                setRecentDocumentMenuItem(recent3MenuItem, 3);
                setRecentDocumentMenuItem(recent4MenuItem, 4);
                setRecentDocumentMenuItem(recent5MenuItem, 5);
                setRecentDocumentMenuItem(recent6MenuItem, 6);
                setRecentDocumentMenuItem(recent7MenuItem, 7);
                setRecentDocumentMenuItem(recent8MenuItem, 8);
                setRecentDocumentMenuItem(recent9MenuItem, 9);
                setRecentDocumentMenuItem(recentAMenuItem, 10);
            }

            @Override
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            @Override
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
        });
        recent1MenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(1);
                if (fp != null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(1), settings.getRecentDocScript(1), settings.getRecentDocAltScript(1));
                }
            }
        });
        recent2MenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(2);
                if (fp != null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(2), settings.getRecentDocScript(2), settings.getRecentDocAltScript(2));
                }
            }
        });
        recent3MenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(3);
                if (fp != null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(3), settings.getRecentDocScript(3), settings.getRecentDocAltScript(3));
                }
            }
        });
        recent4MenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(4);
                if (fp != null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(4), settings.getRecentDocScript(4), settings.getRecentDocAltScript(4));
                }
            }
        });
        recent5MenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(5);
                if (fp != null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(5), settings.getRecentDocScript(5), settings.getRecentDocAltScript(5));
                }
            }
        });
        recent6MenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(6);
                if (fp != null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(6), settings.getRecentDocScript(6), settings.getRecentDocAltScript(6));
                }
            }
        });
        recent7MenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(7);
                if (fp != null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(7), settings.getRecentDocScript(7), settings.getRecentDocAltScript(7));
                }
            }
        });
        recent8MenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(8);
                if (fp != null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(8), settings.getRecentDocScript(8), settings.getRecentDocAltScript(8));
                }
            }
        });
        recent9MenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(9);
                if (fp != null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(9), settings.getRecentDocScript(9), settings.getRecentDocAltScript(9));
                }
            }
        });
        recentAMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                File fp = settings.getRecentDoc(10);
                if (fp != null && fp.exists()) {
                    openFile(fp, settings.getRecentDocAssembler(10), settings.getRecentDocScript(10), settings.getRecentDocAltScript(10));
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
                if (accob != null) {
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
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_C && (evt.isControlDown() || evt.isMetaDown())) {
                    jTextAreaCompilerOutput.copy();
                    evt.consume();
                }
            }
        });
        jTextAreaLog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_C && (evt.isControlDown() || evt.isMetaDown())) {
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
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
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
     * Updates the list with Goto-tokens, when a Goto-command from the Navigation-menu is called.
     * This method retrieves all requested tokens (labels, macros, sections...) and puts them into
     * the JList.
     * <br><br>
     * For more information on cell-rendering, see {@link RL64ListCellRenderer RL64ListCellRenderer}
     * and {@link RL64ListItem RL64ListItem}
     *
     * @param gotoIndex indicates which Goto-command from the Navigate-menu was used. One of
     * following values:
     * <ul>
     * <li>{@link #GOTO_FUNCTION}</li>
     * <li>{@link #GOTO_LABEL}</li>
     * <li>{@link #GOTO_MACRO}</li>
     * <li>{@link #GOTO_SECTION}</li>
     * </ul>
     * @param focusToTextfield {@code true} if the textfield for the Goto-list should gain the input
     * focus. Not useful in all cases, e.g. when opening new files, the user does not expect the
     * input focus to be outside the editor.
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
        for (int i = 0; i < editorPanes.getCount(); i++) {
            if (!eps.contains(i)) {
                eps.add(i);
            }
        }
        String borderTitle;
        // set border-title
        switch (gotoIndex) {
            case GOTO_SECTION:
                borderTitle = ConstantsR64.CB_GOTO_SECTION_STRING;
                break;
            case GOTO_LABEL:
                borderTitle = ConstantsR64.CB_GOTO_LABEL_STRING;
                break;
            case GOTO_FUNCTION:
                borderTitle = ConstantsR64.CB_GOTO_FUNCTION_STRING;
                break;
            case GOTO_MACRO:
                borderTitle = ConstantsR64.CB_GOTO_MACRO_STRING;
                break;
            default:
                borderTitle = ConstantsR64.CB_GOTO_LABEL_STRING;
                break;
        }
        listGotoModel.addElement(new RL64ListItem(borderTitle, null, false, true, 0, null));
        // indicates whether anything found
        boolean tokensFound = false;
        // go through all opened editorpanes
        for (int epIndex : eps) {
            // extract and retrieve sections from each editor pane
            ArrayList<String> token;
            switch (gotoIndex) {
                case GOTO_SECTION:
                    token = SectionExtractor.getSectionNames(editorPanes.getSourceCode(epIndex), editorPanes.getAssembler(epIndex).getLineComment());
                    break;
                case GOTO_LABEL:
                    token = LabelExtractor.getNames(LabelExtractor.getLabels(editorPanes.getSourceCode(epIndex), editorPanes.getAssembler(epIndex), 0).labels);
                    break;
                case GOTO_FUNCTION:
                    token = LabelExtractor.getNames(LabelExtractor.getLabels(editorPanes.getSourceCode(epIndex), editorPanes.getAssembler(epIndex), 0).functions);
                    break;
                case GOTO_MACRO:
                    token = LabelExtractor.getNames(LabelExtractor.getLabels(editorPanes.getSourceCode(epIndex), editorPanes.getAssembler(epIndex), 0).macros);
                    break;
                default:
                    listGotoIndex = GOTO_LABEL;
                    token = LabelExtractor.getNames(LabelExtractor.getLabels(editorPanes.getSourceCode(epIndex), editorPanes.getAssembler(epIndex), 0).labels);
                    break;
            }
            // check if anything found
            if (token != null && !token.isEmpty()) {
                // add header item
                File fp = editorPanes.getFilePath(epIndex);
                // a list item has several properties now, which will be rendered:
                // item text, icon, is header?, line number (not used), file path
                listGotoModel.addElement(new RL64ListItem(FileTools.getFileName(fp).toUpperCase(), null, true, false, 0, fp));
                // sort list
                switch (settings.getSidebarSort()) {
                    case Settings.SORT_NONCASE:
                        Collections.sort(token, new IgnoreCaseComparator());
                        break;
                    case Settings.SORT_CASE:
                        Collections.sort(token);
                        break;
                    default:
                        break;
                }
                // add all found section strings to combo box
                for (String arg : token) {
                    // items have a small margin, headings do not
                    listGotoModel.addElement(new RL64ListItem(arg, null, false, false, 0, fp));
                }
                tokensFound = true;
            }
        }
        // show sidebar and stuff only if we found anything
        if (tokensFound) {
            // clear textfield
            jTextFieldGoto.setText("");
            // check if focus is requested
            if (focusToTextfield) {
                // make splitpane visible if necessary
                toggleGotoListVisibility(false);
            }
            // reset scroll bars for sidebar
            javax.swing.JScrollBar verticalScrollBar = jScrollPaneSidebar.getVerticalScrollBar();
            javax.swing.JScrollBar horizontalScrollBar = jScrollPaneSidebar.getHorizontalScrollBar();
            verticalScrollBar.setValue(verticalScrollBar.getMinimum());
            horizontalScrollBar.setValue(horizontalScrollBar.getMinimum());
        }
        // save index
        settings.setListGotoIndex(listGotoIndex);
    }

    @Action
    public void openSourcefileFolder() {
        File f = editorPanes.getActiveFilePath();
        if (f != null && f.exists() && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(new File(f.getParent()));
            } catch (IOException | NullPointerException | SecurityException | IllegalArgumentException | UnsupportedOperationException ex) {
            }
        }
    }

    /**
     * Toggles the visibility of the Goto-list, i.e. either collapses or expands the splitpane on
     * demand.
     */
    @Action
    public void toggleGotoListVisibility() {
        toggleGotoListVisibility(!settings.getSidebarIsHidden());
    }

    /**
     * Toggles the visibility of the Goto-list, i.e. either collapses or expands the splitpane on
     * demand.
     *
     * @param hide {@code true} if splitpane should be collapsed (i.e. hide Goto-list),
     * {@code false} if it should be expanded (i.e. made visible). If, for instance,
     * {@code collapse} is {@code true} and splitpane already collapsed, nothing will happen.
     */
    private void toggleGotoListVisibility(boolean hide) {
        settings.setSidebarIsHidden(hide);
        dontSaveDividerLocation = true;
        if (hide) {
            settings.setDividerLocation(jSplitPaneEditorList.getDividerLocation());
            jSplitPaneEditorList.setDividerLocation(1.0d);
            editorPanes.setFocus();
        } else {
            int pos = settings.getDividerLocation();
            if (-1 == pos) {
                pos = getFrame().getWidth() - ConstantsR64.MIN_SIDEBAR_SIZE;
            }
            jSplitPaneEditorList.setDividerLocation(pos);
            jTextFieldGoto.requestFocusInWindow();
        }
    }

    /**
     * Parses input from the Goto-line-textfield. Used for quickly changing settings, may also be
     * used for special debug-functions or information etc.
     */
    private void specialFunctions() {
        String text = jTextFieldGotoLine.getText();
        switch (text) {
            case "aa":
                settings.setAntiAlias(AntiAlias.STANDARD);
                editorPanes.updateAntiAlias();
                break;
            case "aas":
                settings.setAntiAlias(AntiAlias.SUBPIXEL);
                editorPanes.updateAntiAlias();
                break;
            case "aan":
                settings.setAntiAlias(AntiAlias.NONE);
                editorPanes.updateAntiAlias();
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
                editorPanes.updateLineNumberAlignment();
                break;
            case "lac":
                settings.setLineNumerAlignment(Gutter.CENTER);
                editorPanes.updateLineNumberAlignment();
                break;
            case "lar":
                settings.setLineNumerAlignment(Gutter.RIGHT);
                editorPanes.updateLineNumberAlignment();
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
            case "of":
                openSourcefileFolder();
                break;
            case "ch":
                String compilerHelp = customScripts.getCompilerHelp(jComboBoxRunScripts.getSelectedItem());
                if (compilerHelp != null) {
                    jTextAreaLog.append(System.lineSeparator() + System.lineSeparator() + compilerHelp);
                    jTabbedPaneLogs.setSelectedIndex(0);
                }
                break;
        }
        try {
            if (text.startsWith("cs")) {
                int nr = Integer.parseInt(text.substring(2));
                if (nr >= 1 && nr <= ColorSchemes.SCHEME_NAMES.length) {
                    settings.setColorScheme(nr - 1);
                    editorPanes.updateColorScheme();
                }
            } else if (text.startsWith("fs")) {
                int size = Integer.parseInt(text.substring(2));
                settings.setMainFont(new Font(settings.getMainFont(Settings.FONTNAME), Font.PLAIN, size));
                editorPanes.updateFonts();
            } else if (text.startsWith("ts")) {
                int size = Integer.parseInt(text.substring(2));
                settings.setTabWidth(size);
                editorPanes.updateTabs();
            }
        } catch (IndexOutOfBoundsException | NumberFormatException ex) {
        }
        jTextFieldGotoLine.setText("");
    }

    /**
     * This method updates the menu-items with the recent documents
     */
    private void setRecentDocuments() {
        setRecentDocumentMenuItem(recent1MenuItem, 1);
        setRecentDocumentMenuItem(recent2MenuItem, 2);
        setRecentDocumentMenuItem(recent3MenuItem, 3);
        setRecentDocumentMenuItem(recent4MenuItem, 4);
        setRecentDocumentMenuItem(recent5MenuItem, 5);
        setRecentDocumentMenuItem(recent6MenuItem, 6);
        setRecentDocumentMenuItem(recent7MenuItem, 7);
        setRecentDocumentMenuItem(recent8MenuItem, 8);
        setRecentDocumentMenuItem(recent9MenuItem, 9);
        setRecentDocumentMenuItem(recentAMenuItem, 10);
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
        if (recDoc != null && recDoc.exists()) {
            // make menu visible, if recent document is valid
            menuItem.setVisible(true);
            // set filename as text
            menuItem.setText(FileTools.getFileName(recDoc));
            menuItem.setToolTipText(recDoc.getPath());
        }
    }

    private void convertNumber(String format) {
        String input;
        switch (format) {
            case "hex":
                input = jTextFieldConvHex.getText();
                try {
                    jTextFieldConvDez.setText(String.valueOf(Integer.parseInt(input, 16)));
                    jTextFieldConvBin.setText(Integer.toBinaryString(Integer.parseInt(input, 16)));
                } catch (NumberFormatException ex) {
                }
                break;
            case "bin":
                input = jTextFieldConvBin.getText();
                try {
                    jTextFieldConvDez.setText(String.valueOf(Integer.parseInt(input, 2)));
                    jTextFieldConvHex.setText(Integer.toHexString(Integer.parseInt(input, 2)));
                } catch (NumberFormatException ex) {
                }
                break;
            case "dez":
            case "dec":
                input = jTextFieldConvDez.getText();
                try {
                    jTextFieldConvHex.setText(Integer.toHexString(Integer.parseInt(input)));
                    jTextFieldConvBin.setText(Integer.toBinaryString(Integer.parseInt(input)));
                } catch (NumberFormatException ex) {
                }
                break;
        }
    }

    private void gotoTokenFromList() {
        // retrieve index of selected item
        Object selectedObject = jListGoto.getSelectedValue();
        // check if > 0 and key was return
        if (selectedObject != null) {
            // remembler focus owner
            java.awt.Component c = this.getFrame().getFocusOwner();
            final String compname = (c != null) ? c.getName() : null;
            // retrieve item information
            RL64ListItem listItem = (RL64ListItem) selectedObject;
            // retrieve opened tab
            int epIndex = editorPanes.getOpenedFileTab(listItem.getFile());
            // go on, if tab is opened and no heading is selected
            if (epIndex != -1 && !listItem.isHeader()) {
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
                    default:
                        editorPanes.gotoLabel(listItem.getText(), epIndex);
                        break;
                }
            }
            // scrolling to caret sets focus to editor component,
            // but we don't want that.
            if (compname != null) {
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
                                        } catch (IllegalArgumentException ex) {
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

    public void toggleTabbedPaneScrollPolicy() {
        jTabbedPane1.setTabLayoutPolicy(settings.getUseScrollTabs() ? JTabbedPane.SCROLL_TAB_LAYOUT : JTabbedPane.WRAP_TAB_LAYOUT);
    }

    public final void toggleToolbar() {
        jToolBar.setVisible(settings.getShowToolbar());
        int count = 0;
        for (int i = 0; i < jToolBar.getComponentCount(); i++) {
            Component c = jToolBar.getComponentAtIndex(i);
            if (c != null && c instanceof JButton) {
                ((JButton) c).setText(settings.getShowToolbarText() ? ConstantsR64.toolbarNames[count] : "");
                count++;
            }
        }
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
        for (int i = 0; i < editorPanes.getCount(); i++) {
            editorPanes.getEditorPane(i).spacesToTabs();
        }
    }

    @Action
    public void tabsToSpaces() {
        editorPanes.getActiveEditorPane().tabsToSpaces();
    }

    @Action
    public void allTabsToSpaces() {
        for (int i = 0; i < editorPanes.getCount(); i++) {
            editorPanes.getEditorPane(i).tabsToSpaces();
        }
    }

    @Action
    public void selectionToLowercase() {
        editorPanes.getActiveEditorPane().toLowerCase();
    }

    @Action
    public void selectionToUppercase() {
        editorPanes.getActiveEditorPane().toUpperCase();
    }

    public void autoConvertNumbers(String selection) {
        // check for valid selection
        if (selection != null && !selection.trim().isEmpty()) {
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
            } else if (selection.startsWith("$")) {
                selection = selection.substring(1);
                isHex = selection.matches("^[0-9A-Fa-f]+$");
            } else if (selection.startsWith("#%")) {
                selection = selection.substring(2);
                isBin = selection.matches("[0-1]+");
            } else if (selection.startsWith("%")) {
                selection = selection.substring(1);
                isBin = selection.matches("[0-1]+");
            } else if (selection.startsWith("#")) {
                selection = selection.substring(1);
                isDez = selection.matches("[0-9]+");
            } else if (selection.matches("[0-9]+")) {
                isDez = true;
            } else {
                isHex = selection.matches("^[0-9A-Fa-f]+$");
            }
            // is hex?
            if (isHex) {
                jTextFieldConvHex.setText(selection);
                convertNumber("hex");
            } else if (isDez) {
                jTextFieldConvDez.setText(selection);
                convertNumber("dez");
            } else if (isBin) {
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
        String selectedScriptName = (o != null) ? o.toString() : null;
        // open settings window
        if (null == settingsDlg) {
            settingsDlg = new SettingsDlg(getFrame(), this, settings, customScripts, editorPanes);
            settingsDlg.setLocationRelativeTo(getFrame());
        }
        Relaunch64App.getApplication().show(settingsDlg);
        // update custom scripta
        initScripts();
        // select previous script
        if (customScripts.findScript(selectedScriptName) != -1) {
            jComboBoxRunScripts.setSelectedItem(selectedScriptName);
        }
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
        String caretWord = editorPanes.getActiveEditorPane().getCaretString(true, "");
        // check if anything
        if (caretWord != null && !caretWord.isEmpty()) {
            // remember cursor
            int caret = editorPanes.getActiveEditorPane().getCaretPosition();
            int line = editorPanes.getActiveEditorPane().getCaretLine();
            // jump to label
            editorPanes.gotoLabel(caretWord);
            // check if line changed. if not, we don't want to "overwrite" 
            // old caret positions
            if (editorPanes.getActiveEditorPane().getCaretLine() != line) {
                // remember cursor
                editorPanes.saveLabelSourcePosition(caret);
            }
        }
    }

    @Action
    public void jumpBackToLabelSource() {
        editorPanes.gotoLabelSourcePosition();
    }

    @Action
    public void insertSection() {
        // open an input-dialog
        String sectionName = (String) JOptionPane.showInputDialog(getFrame(), "Section name:", "Insert section", JOptionPane.PLAIN_MESSAGE);
        // check
        if (sectionName != null && !sectionName.isEmpty()) {
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

    @Action
    public void openInExternalEditor() {
        // check if source file needs to be saved and auto save is active
        if (editorPanes.getActiveFilePath() == null || editorPanes.isModified()) {
            editorPanes.saveFile();
        }
        // retrieve ASM-Source file
        File sourceFile = editorPanes.getActiveFilePath();
        // no file :(
        if (sourceFile == null) {
            return;
        }
        // get external editor path
        String ee = settings.getExternalEditorPath();
        // no editor :(
        if (ee == null || ee.isEmpty()) {
            return;
        }
        // surround pathes with quotes, if necessary
        String sf = sourceFile.toString();
        if (sf.contains(" ") && !sf.startsWith("\"") && !sf.startsWith("'")) {
            sf = "\"" + sf + "\"";
        }
        // replace placeholders
        ee = ee.replace(Assembler.INPUT_FILE, sf);
        // tonkenize command line
        ProcessBuilder pb;
        // Start ProcessBuilder
        // pb = new ProcessBuilder(cmd.split(" "));
        pb = new ProcessBuilder(Tools.tokeniseCommandLine(ee));
        try {
            // start process
            pb.start();
            // log process
            ConstantsR64.r64logger.log(Level.INFO, "Open in external editor: {0}", ee);
        } catch (IOException ex) {
            // thread has not been terminated correctly, so log warning
            ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
        }
    }
    
    /**
     *
     */
    @Action
    public final void addNewTab() {
        editorPanes.addNewTab(null, null, "untitled", settings.getPreferredAssembler(), jComboBoxRunScripts.getSelectedIndex(), -1);
    }

    @Action
    public void openAllIncludeFiles() {
        BufferedReader br = new BufferedReader(new StringReader(editorPanes.getActiveSourceCode()));
        LineNumberReader lineReader = new LineNumberReader(br);
        // get current editor
        RL64TextArea ep = editorPanes.getActiveEditorPane();
        // create pattern for include directive
        String src = "(" + ep.getAssembler().getIncludeSourceDirective(")(.*?)");
        Pattern p = Pattern.compile(src);
        String line;
        try {
            // save values from source file
            File sourceFile = editorPanes.getActiveFilePath();
            Assembler asm = editorPanes.getActiveAssembler();
            int script = editorPanes.getActiveScript();
            int altScript = editorPanes.getActiveAlternativeScript();
            String commentChar = asm.getLineComment();
            // go through source and find includes
            while ((line = lineReader.readLine()) != null) {
                Matcher m = p.matcher(line);
                // do we have two groups?
                if (!line.startsWith(commentChar) && m.find() && m.groupCount() >= 2) {
                    // 2nd group is file. retrieve full path of include file to source file
                    File f = FileTools.getAbsolutePath(sourceFile, new File(m.group(2)));
                    // if it exists, open it
                    if (f != null && f.exists()) {
                        openFile(f, asm, script, altScript);
                    }
                }
            }
        } catch (IOException ex) {
        }
    }

    @Action
    public void openIncludeFile() {
        // get current editor
        RL64TextArea ep = editorPanes.getActiveEditorPane();
        // get current line-text
        String line = ep.getLineText(ep.getCaretLine());
        // create pattern for include directive
        String src = "(" + ep.getAssembler().getIncludeSourceDirective(")(.*?)");
        Pattern p = Pattern.compile(src);
        Matcher m = p.matcher(line);
        // do we have two groups?
        if (!line.startsWith(editorPanes.getActiveAssembler().getLineComment()) && m.find() && m.groupCount() >= 2) {
            // 2nd group is file. retrieve full path of include file to source file
            File f = FileTools.getAbsolutePath(editorPanes.getActiveFilePath(), new File(m.group(2)));
            // if it exists, open it
            if (f != null && f.exists()) {
                openFile(f, editorPanes.getActiveAssembler(), editorPanes.getActiveScript(), editorPanes.getActiveAlternativeScript());
            }
        }
    }

    @Action
    public void openFile() {
        File fileToOpen = FileTools.chooseFile(getFrame(), JFileChooser.OPEN_DIALOG, JFileChooser.FILES_ONLY, settings.getLastUsedPath().getAbsolutePath(), "", "Open ASM File", ConstantsR64.FILE_EXTENSIONS, "ASM-Files");
        openFile(fileToOpen);
    }

    
    /**
     * Check whether the currently active file has been externally changed.
     */
    private void checkExternalFileChange() {
        if (editorPanes != null) {
            // get current activated editor pane
            EditorPaneProperties ep = editorPanes.getActiveEditorPaneProperties();
            // is file changed?
            if (ep != null && ep.isFileModified()) {
                // open a confirm dialog
                int option = JOptionPane.showConfirmDialog(getFrame(), 
                        resourceMap.getString("externalChangesMsg"), 
                        resourceMap.getString("externalChangesTitle"), 
                        JOptionPane.YES_NO_CANCEL_OPTION, 
                        JOptionPane.PLAIN_MESSAGE);
                // if action is cancelled, return to the program
                if (JOptionPane.CANCEL_OPTION == option || JOptionPane.NO_OPTION == option || JOptionPane.CLOSED_OPTION == option /* User pressed cancel key */) {
                    // reset modified state
                    ep.setLastModified(ep.getFilePath().lastModified());
                    // set file as modified
                    editorPanes.setModified(true);
                } else if (JOptionPane.YES_OPTION == option) {
                    // reload current file
                    editorPanes.reloadFile();
                }
            } 
        }
    }
    
    private void openFile(File fileToOpen) {
        openFile(fileToOpen, settings.getPreferredAssembler());
    }

    private void openFile(File fileToOpen, Assembler assembler) {
        openFile(fileToOpen, assembler, jComboBoxRunScripts.getSelectedIndex(), -1);
    }

    private void openFile(File fileToOpen, Assembler assembler, int script, int alternativeScript) {
        // check if file could be opened
        if (editorPanes.loadFile(fileToOpen, assembler, script, alternativeScript)) {
            // add file path to recent documents history
            settings.addToRecentDocs(fileToOpen.toString(), assembler, script, alternativeScript);
            // and update menus
            setRecentDocuments();
            // save last used path
            settings.setLastUsedPath(fileToOpen);
            // select combobox item
            try {
                jComboBoxAssemblers.setSelectedIndex(assembler.getID());
                jComboBoxRunScripts.setSelectedIndex(script);
            } catch (IllegalArgumentException ex) {
            }
        }
    }

    private void updateRecentDoc() {
        // find current file
        File cf = editorPanes.getActiveFilePath();
        // find doc associated with current document
        int rd = settings.findRecentDoc(cf);
        // if we have valid values, update recent doc
        if (rd != -1 && cf != null) {
            settings.setRecentDoc(rd, cf.toString(), Assemblers.byID(jComboBoxAssemblers.getSelectedIndex()), 
                    jComboBoxRunScripts.getSelectedIndex(), editorPanes.getActiveAlternativeScript());
        }
    }

    private void reopenFiles() {
        // get reopen files
        ArrayList<Object[]> files = settings.getReopenFiles();
        // check if we have any
        if (files != null && !files.isEmpty()) {
            // retrieve set
            for (Object[] o : files) {
                // retrieve data: file path
                File fp = new File(o[0].toString());
                // retrieve data: assembler
                Assembler assembler = (Assembler) o[1];
                // retrieve data: script
                int script = Integer.parseInt(o[2].toString());
                // retrieve data: alternative script
                int altScript = -1;
                if (o.length > 2) altScript = Integer.parseInt(o[3].toString());
                // open file
                openFile(fp, assembler, script, altScript);
            }
        }
    }

    @Action
    public void saveFile() {
        if (editorPanes.saveFile()) {
            // add file path to recent documents history
            settings.addToRecentDocs(editorPanes.getActiveFilePath().getPath(), 
                    Assemblers.byID(jComboBoxAssemblers.getSelectedIndex()), 
                    jComboBoxRunScripts.getSelectedIndex(),
                    editorPanes.getActiveAlternativeScript());
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
            if (jTabbedPane1.getTabCount() > 0) {
                // retrieve selected file
                int selectedTab = jTabbedPane1.getSelectedIndex();
                try {
                    // close tab
                    jTabbedPane1.remove(selectedTab);
                } catch (IndexOutOfBoundsException ex) {
                    ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
                }
            }
            if (jTabbedPane1.getTabCount() < 1) {
                addNewTab();
            }
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
        for (int cnt = 0; cnt < count; cnt++) {
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
            settings.addToRecentDocs(editorPanes.getActiveFilePath().getPath(), 
                    Assemblers.byID(jComboBoxAssemblers.getSelectedIndex()), 
                    jComboBoxRunScripts.getSelectedIndex(),
                    editorPanes.getActiveAlternativeScript());
            // and update menus
            setRecentDocuments();
        }
    }

    /**
     * Runs the second selected user script and - depending on the script - compiles the source
     * code and start the compiled source in an emulator. If errors occur during the compile
     * process, the error log is shown and the caret jumps to the related position in the source.
     */
    @Action
    public void runScript2() {
        String scriptName = customScripts.getScriptName(editorPanes.getActiveAlternativeScript());
        // we have found a valid script name, so get script
        String script = customScripts.getScript(scriptName);
        
        runGenericScript(script, scriptName);
    }
    
    /**
     * Runs the currently selected user script and - depending on the script - compiles the source
     * code and start the compiled source in an emulator. If errors occur during the compile
     * process, the error log is shown and the caret jumps to the related position in the source.
     */
    @Action
    public void runScript() {
        String script = null;
        // check if user defined custom script
        String scriptName = Tools.getCustomScriptName(editorPanes.getActiveSourceCode(), editorPanes.getActiveAssembler().getLineComment());
        // init cb-item
        Object item;
        // if we found no custom script in source, or script name was not found,
        // select script from combo box
        if (null == scriptName || -1 == customScripts.findScript(scriptName)) {
            // get selected item
            item = jComboBoxRunScripts.getSelectedItem();
            // valid selection?
            if (item != null) {
                // get scriptname from selection
                scriptName = item.toString();
                // get script
                script = customScripts.getScript(scriptName);
            }
        } else {
            // we have found a valid script name, so get script
            script = customScripts.getScript(scriptName);
        }
        runGenericScript(script, scriptName);
    }
    
    private void runGenericScript(String script, String scriptName) {
        // log scriptname
        String log = "Executing script \"" + scriptName + "\"";
        ConstantsR64.r64logger.log(Level.INFO, log);
        // valid script?
        if (script != null && !script.isEmpty()) {
            // log offset
            int offset = 0;
            // clear old log
            clearLog1();
            clearLog2();
            // clear error lines
            errorHandler.clearErrors();
            // convert CRLF to LF (WIN)
            script = script.replaceAll("\r\n", "\n");
            // convert CR to LF (MAC)
            script = script.replaceAll("\r", "\n");
            // retrieve script lines
            // String[] lines = script.split("\n");
            String[] lines = Tools.extractCommandLines(script);
            // check if source file needs to be saved and auto save is active
            if (editorPanes.getActiveFilePath() == null || (settings.getSaveOnCompile() && editorPanes.isModified())) {
                editorPanes.saveFile();
            }
            // retrieve ASM-Source file
            File sourceFile = editorPanes.getActiveFilePath();
            // no file :(
            if (sourceFile == null) {
                return;
            }
            // set base path for relative paths
            errorHandler.setBasePath(sourceFile);
            // retrieve parent file. needed to construct output file paths
            String parentFile = (null == sourceFile.getParentFile()) ? sourceFile.toString() : sourceFile.getParentFile().toString();
            // create Output file
            File outFile = new File(parentFile + File.separator + FileTools.getFileName(sourceFile) + ".prg");
            // create compressed file
            File compressedFile = new File(parentFile + File.separator + FileTools.getFileName(sourceFile) + "-compressed.prg");
            // check if we have relative paths
            boolean useRelativePath = script.contains(Assembler.OUTPUT_FILE_REL);
            // set up Relaunch64 commandline-options
            boolean option_ignore_warnings = false;
            boolean option_wait_for_process = settings.getWaitForProcess();
            // iterate script
            for (String cmd : lines) {
                cmd = cmd.trim();
                if (!cmd.isEmpty()) {
                    // log process
                    log = "Processing script-line: " + cmd;
                    ConstantsR64.r64logger.log(Level.INFO, log);
                    // check if we have last process in script. needed below to check whether
                    // Relaunch64 should wait for last process to be finished or not
                    boolean isLastLine = cmd.equalsIgnoreCase(lines[lines.length - 1]);
                    // check if we have Relaunch64 command-line options, like 
                    // ignore warnings etc.
                    if (cmd.startsWith("R64 ")) {
                        // log-string
                        log = "Compile options:";
                        // get options
                        String[] cmd_options = cmd.split("-");
                        // check all options
                        for (String cmd_op : cmd_options) {
                            // options only in lower case
                            cmd_op = cmd_op.trim().toLowerCase();
                            // set options
                            switch (cmd_op) {
                                case "iw":
                                    option_ignore_warnings = true;
                                    log = log + " ignore warnings;";
                                    break;
                                case "wait":
                                    option_wait_for_process = true;
                                    log = log + " wait for script;";
                                    break;
                                case "nowait":
                                    option_wait_for_process = false;
                                    log = log + " don't wait for script;";
                                    break;
                            }
                        }
                        // log compiler options
                        ConstantsR64.r64logger.log(Level.INFO, log);
                    } else {
                        // check whether user wants to ignore the error stream
                        // and not wait for termination of running process
                        boolean ignoreErrorStream = cmd.startsWith("R64BG ");
                        if (ignoreErrorStream) {
                             cmd = cmd.replaceFirst("R64BG ","");
                        }
                        // surround pathes with quotes, if necessary
                        String sf = sourceFile.toString();
                        if (sf.contains(" ") && !sf.startsWith("\"") && !sf.startsWith("'")) {
                            sf = "\"" + sf + "\"";
                        }
                        String of = outFile.toString();
                        if (of.contains(" ") && !of.startsWith("\"") && !of.startsWith("'")) {
                            of = "\"" + of + "\"";
                        }
                        String cf = compressedFile.toString();
                        if (cf.contains(" ") && !cf.startsWith("\"") && !cf.startsWith("'")) {
                            cf = "\"" + cf + "\"";
                        }
                        // create relative paths of in- and output files
                        String sf_rel = FileTools.getFileName(sourceFile) + "." + FileTools.getFileExtension(sourceFile);
                        String of_rel = FileTools.getFileName(outFile) + "." + FileTools.getFileExtension(outFile);
                        String cf_rel = FileTools.getFileName(compressedFile) + "." + FileTools.getFileExtension(compressedFile);
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
                        String cruncherStart = Tools.getCruncherStart(editorPanes.getActiveSourceCode(),
                                editorPanes.getActiveAssembler().getLineComment());
                        // if we found cruncher-starttoken, replace placeholder 
                        if (cruncherStart != null) {
                            cmd = cmd.replace(ConstantsR64.ASSEMBLER_START_ADDRESS, cruncherStart);
                        }
                        try {
                            // log process
                            log = "Converted script-line: " + cmd;
                            ConstantsR64.r64logger.log(Level.INFO, log);
                            // write output to string builder. we need output both for printing
                            // it to the text area log, and to examine the string for possible errors.
                            StringBuilder compilerLog = new StringBuilder("");
                            ProcessBuilder pb;
                            Process p;
                            // Start ProcessBuilder
                            // pb = new ProcessBuilder(cmd.split(" "));
                            pb = new ProcessBuilder(Tools.tokeniseCommandLine(cmd));
                            // set parent directory to sourcecode fie
                            pb = pb.directory(sourceFile.getParentFile());
                            if (!ignoreErrorStream) pb = pb.redirectOutput(Redirect.PIPE).redirectError(Redirect.PIPE);
                            // start process
                            p = pb.start();
                            // create scanner to receive compiler messages
                            if (!ignoreErrorStream) {
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
                                // if we don't have last script line, or if each process should be waited
                                // for, wait for process to be finished
                                if (!isLastLine || (isLastLine && option_wait_for_process)) {
                                    // wait for other process to be finished
                                    p.waitFor();
                                    p.destroy();
                                }
                                // read and extract errors from log
                                offset = errorHandler.readErrorLines(compilerLog.toString(),
                                        editorPanes.getActiveAssembler(),
                                        offset,
                                        option_ignore_warnings);
                                // break loop if we have any errors
                                try {
                                    if (errorHandler.hasErrors() || p.exitValue() != 0) {
                                        break;
                                    }
                                } catch (IllegalThreadStateException ex) {
                                    // thread has not been terminated correctly, so log warning
                                    ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
                                    // destroy thread
                                    p.destroy();
                                    // and leave
                                    if (errorHandler.hasErrors()) {
                                        break;
                                    }
                                }
                            }
                        } catch (IOException | InterruptedException | SecurityException ex) {
                            ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
                            // check if permission denied
                            if (ex.getLocalizedMessage().toLowerCase().contains("permission denied")) {
                                ConstantsR64.r64logger.log(Level.INFO, "Permission denied. Try to define user scripts in the preferences and use \"open\" or \"/bin/sh\" as parameters (see Help on Preference pane tab)!");
                            }
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
                if (errorHandler.hasErrors()) {
                    errorHandler.gotoFirstError(editorPanes, jTextAreaCompilerOutput);
                }
            }
        } else {
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
        jTextAreaLog.setText(Tools.getSystemInformation() + System.lineSeparator());
    }

    @Action
    public void clearLog2() {
        // set sys info
        jTextAreaCompilerOutput.setText("");
    }

    @Action
    public void selectUserScripts() {
        if (settings.getShowToolbar()) {
            // show panel
            jComboBoxRunScripts.showPopup();
            jComboBoxRunScripts.requestFocusInWindow();
        } else {
            // get script names
            String[] sn = customScripts.getScriptNames();
            // check for null
            if (sn != null && sn.length > 0) {
                // sort arary
                Arrays.sort(sn);
                // if toolbar is not visible, show option dialog instead
                Object selection = JOptionPane.showInputDialog(getFrame(), "Select script:", "", JOptionPane.PLAIN_MESSAGE, null, sn, jComboBoxRunScripts.getSelectedItem());
                // check if null
                if (selection != null) {
                    // select script in cb
                    jComboBoxRunScripts.setSelectedItem(selection);
                    // change script for current file
                    editorPanes.getEditorPaneProperties(jTabbedPane1.getSelectedIndex()).setScript(customScripts.findScript(selection.toString()));
                    // update recent doc
                    updateRecentDoc();
                }
            }
        }
    }

    @Action
    public void selectAltUserScripts() {
        // get script names
        String[] sn = customScripts.getScriptNames();
        // check for null
        if (sn != null && sn.length > 0) {
            // get editor pane properties of current tab
            EditorPaneProperties ep = editorPanes.getEditorPaneProperties(jTabbedPane1.getSelectedIndex());
            // sort arary
            Arrays.sort(sn);
            // if toolbar is not visible, show option dialog instead
            Object selection = JOptionPane.showInputDialog(getFrame(), "Select script:", "", 
                    JOptionPane.PLAIN_MESSAGE, null, sn, 
                    customScripts.getScriptName(ep.getAltScript()));
            // check if null
            if (selection != null) {
                // change script for current file
                ep.setAltScript(customScripts.findScript(selection.toString()));
                // update recent doc
                updateRecentDoc();
            }
        }
    }

    @Action
    public void selectSyntax() {
        jComboBoxAssemblers.showPopup();
        jComboBoxAssemblers.requestFocusInWindow();
    }

    @Action
    public void selectLog() {
        int selected = jTabbedPaneLogs.getSelectedIndex();
        jTabbedPaneLogs.setSelectedIndex((0 == selected) ? 1 : 0);
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
        } else {
            // get findterm
            Object ft = jComboBoxFind.getSelectedItem();
            // if textfield is visible and textfield has focus
            if (ft != null && jComboBoxFind.isFocusOwner()) {
                String findTerm = ft.toString();
                // if find term has not changed, calling "find" will find next search term
                if (findTerms.getCurrentFindTerm().equalsIgnoreCase(findTerm)) {
                    findNext();
                    return;
                }
            }
        }
        // set focus to textfield
        jComboBoxFind.requestFocusInWindow();
        // retrieve editor component
        JTextField tf = (JTextField) jComboBoxFind.getEditor().getEditorComponent();
        // select item, if any, so user does not need to delete
        // before entering a new search term
        tf.setSelectionStart(0);
        tf.setSelectionEnd(tf.getText().length());
    }

    @Action
    public void findNext() {
        Object ft = jComboBoxFind.getSelectedItem();
        if (ft != null) {
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
                    jCheckBoxMatchCase.isSelected(),
                    settings.getFindFieldFocus()) ? Color.black : Color.red);
        }
    }

    @Action
    public void findPrev() {
        Object ft = jComboBoxFind.getSelectedItem();
        if (ft != null) {
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
                    jCheckBoxMatchCase.isSelected(),
                    settings.getFindFieldFocus()) ? Color.black : Color.red);
        }
    }

    @Action
    public void findCancel() {
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
        if (sel != null) {
            findReplace.initValues(jComboBoxFind.getSelectedItem().toString(),
                    jTextFieldReplace.getText(),
                    jTabbedPane1.getSelectedIndex(),
                    editorPanes.getActiveEditorPane(),
                    jCheckBoxRegEx.isSelected(),
                    jCheckBoxWholeWord.isSelected(),
                    jCheckBoxMatchCase.isSelected());
            int findCounter = 0;
            while (findReplace.replace(jCheckBoxRegEx.isSelected(), jCheckBoxWholeWord.isSelected(), jCheckBoxMatchCase.isSelected(), settings.getFindFieldFocus())) {
                findCounter++;
            }
            JOptionPane.showMessageDialog(getFrame(), String.valueOf(findCounter) + " occurences were replaced.");
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
            if (null == sel || sel.toString().isEmpty()) {
                jComboBoxFind.requestFocusInWindow();
            } else {
                jTextFieldReplace.requestFocusInWindow();
            }
        } // if textfield is already visible, replace term
        else {
            if (sel != null) {
                findReplace.initValues(jComboBoxFind.getSelectedItem().toString(),
                        jTextFieldReplace.getText(),
                        jTabbedPane1.getSelectedIndex(),
                        editorPanes.getActiveEditorPane(),
                        jCheckBoxRegEx.isSelected(),
                        jCheckBoxWholeWord.isSelected(),
                        jCheckBoxMatchCase.isSelected());
                jTextFieldReplace.setForeground(findReplace.replace(jCheckBoxRegEx.isSelected(), jCheckBoxWholeWord.isSelected(), jCheckBoxMatchCase.isSelected(), settings.getFindFieldFocus()) ? Color.black : Color.red);
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
        if (null == insertByteFromFileDlg) {
            insertByteFromFileDlg = new InsertByteFromFileDlg(getFrame(), settings, editorPanes.getActiveAssembler());
            insertByteFromFileDlg.setLocationRelativeTo(getFrame());
        }
        Relaunch64App.getApplication().show(insertByteFromFileDlg);
        // check for valid return value
        String bytetable = insertByteFromFileDlg.getByteTable();
        // insert bytes to source code
        if (bytetable != null && !bytetable.isEmpty()) {
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
        if (null == insertSinusTableDlg) {
            insertSinusTableDlg = new InsertSinusTableDlg(getFrame(), editorPanes.getActiveAssembler());
            insertSinusTableDlg.setLocationRelativeTo(getFrame());
        }
        Relaunch64App.getApplication().show(insertSinusTableDlg);
        // check for valid return value
        String bytetable = insertSinusTableDlg.getByteTable();
        // insert bytes to source code
        if (bytetable != null && !bytetable.isEmpty()) {
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
            } // check which laf was found and set appropriate default value 
            else if (!nimbusclassname.isEmpty()) {
                UIManager.setLookAndFeel(nimbusclassname);
            } else {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
        }
        if (ConstantsR64.IS_OSX) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", ConstantsR64.APPLICATION_SHORT_TITLE);
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            // aqua brushed look
            if (!settings.getNimbusOnOSX()) {
                Relaunch64View.super.getFrame().getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
            }
        }
        // System.setProperty("awt.useSystemAAFontSettings", "on");
        if (settings.getScaleFont()) {
            try { // Try to scale default font size according to screen resolution.
                Font fm = (Font) UIManager.getLookAndFeelDefaults().get("defaultFont");
                // check if laf supports default font
                if (fm != null) {
                    UIManager.getLookAndFeelDefaults().put("defaultFont", fm.deriveFont(fm.getSize2D() * Toolkit.getDefaultToolkit().getScreenResolution() / 96));
                }
            } catch (HeadlessException e) {
            }
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
        if (filesToOpen != null && !filesToOpen.isEmpty()) {
            for (File f : filesToOpen) {
                openFile(f);
            }
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
        checkExternalFileChange();
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    /**
     * This is the Exit-Listener. Here we put in all the things which should be done before closing
     * the window and exiting the program
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
     * method is called when there are modifications in one of the above mentioned datafiles, and a
     * new data-file is to be imported or opened, or when the application is about to quit.
     *
     * @param title the title of the message box, e.g. if the changes should be saved because the
     * user wants to quit the application of to open another data file
     * @return <i>true</i> if the changes have been successfully saved or if the user did not want
     * to save anything, and the program can go on. <i>false</i> if the user cancelled the dialog
     * and the program should <i>not</i> go on or not quit.
     */
    private boolean askForSaveChanges() {
        boolean changes = false;
        int count = editorPanes.getCount();
        // check whether we have any changes at all
        for (int i = 0; i < count; i++) {
            if (editorPanes.isModified(i)) {
                changes = true;
            }
        }
        // ask for save
        if (changes) {
            // open a confirm dialog
            int option = JOptionPane.showConfirmDialog(getFrame(), resourceMap.getString("msgSaveChangesOnExit"), resourceMap.getString("msgSaveChangesOnExitTitle"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            // if action is cancelled, return to the program
            if (JOptionPane.CANCEL_OPTION == option || JOptionPane.CLOSED_OPTION == option /* User pressed cancel key */) {
                return false;
            } else if (JOptionPane.YES_OPTION == option) {
                boolean saveok = true;
                for (int i = 0; i < count; i++) {
                    if (editorPanes.isModified(i)) {
                        if (!editorPanes.saveFile()) {
                            saveok = false;
                        }
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
    public class TextAreaHandler extends java.util.logging.StreamHandler {

        @Override
        public void publish(final LogRecord record) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    StringWriter text = new StringWriter();
                    PrintWriter out = new PrintWriter(text);
                    out.println(jTextAreaLog.getText());
                    out.printf("[%s] %s", record.getLevel(), getFormatter().formatMessage(record));
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

    private void checkForUpdates() {
        // check if check should be checked
        if (!settings.getCheckForUpdates()) {
            return;
        }
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
        jButtonCloseFind = new javax.swing.JButton();
        jPanelReplace = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldReplace = new javax.swing.JTextField();
        jButtonReplace = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jScrollPaneSidebar = new javax.swing.JScrollPane();
        jListGoto = new javax.swing.JList();
        jTextFieldGoto = new javax.swing.JTextField();
        jButtonRefreshGoto = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxSortSidebar = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPaneLogs = new javax.swing.JTabbedPane();
        jPanel6 = new javax.swing.JPanel();
        jScrollPaneLog = new javax.swing.JScrollPane();
        jTextAreaLog = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jScrollPaneErrorLog = new javax.swing.JScrollPane();
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
        jSeparator26 = new javax.swing.JPopupMenu.Separator();
        openIncludeFileMenuItem = new javax.swing.JMenuItem();
        openAllIncludedMenuItem = new javax.swing.JMenuItem();
        jSeparator27 = new javax.swing.JPopupMenu.Separator();
        openFolderMenuItem = new javax.swing.JMenuItem();
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
        jMenuItemToLowercase = new javax.swing.JMenuItem();
        jMenuItemToUppercase = new javax.swing.JMenuItem();
        jSeparator34 = new javax.swing.JPopupMenu.Separator();
        jMenuItemTabsToSpaces = new javax.swing.JMenuItem();
        jMenuItemAllTabsToSpaces = new javax.swing.JMenuItem();
        jSeparator25 = new javax.swing.JPopupMenu.Separator();
        jMenuItemSpaceToTab = new javax.swing.JMenuItem();
        jMenuItemAllSpaceToTab = new javax.swing.JMenuItem();
        jSeparator35 = new javax.swing.JPopupMenu.Separator();
        jMenuItemExternEditor = new javax.swing.JMenuItem();
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
        jumpBackToLabelMenuItem = new javax.swing.JMenuItem();
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
        debugScriptMenuIitem = new javax.swing.JMenuItem();
        jSeparator36 = new javax.swing.JPopupMenu.Separator();
        focusScriptMenuItem = new javax.swing.JMenuItem();
        selectAltScriptMenuItem = new javax.swing.JMenuItem();
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
        jToolBar = new javax.swing.JToolBar();
        tbNew = new javax.swing.JButton();
        tbFileOpen = new javax.swing.JButton();
        tbSave = new javax.swing.JButton();
        tbSaveAll = new javax.swing.JButton();
        jSeparator28 = new javax.swing.JToolBar.Separator();
        tbUndo = new javax.swing.JButton();
        tbRedo = new javax.swing.JButton();
        jSeparator33 = new javax.swing.JToolBar.Separator();
        tbCut = new javax.swing.JButton();
        tbCopy = new javax.swing.JButton();
        tbPaste = new javax.swing.JButton();
        jSeparator29 = new javax.swing.JToolBar.Separator();
        tbFind = new javax.swing.JButton();
        tbFindNext = new javax.swing.JButton();
        tbReplace = new javax.swing.JButton();
        jSeparator30 = new javax.swing.JToolBar.Separator();
        jComboBoxRunScripts = new javax.swing.JComboBox();
        tbRunScript = new javax.swing.JButton();
        tbPrevError = new javax.swing.JButton();
        tbNextError = new javax.swing.JButton();
        jSeparator32 = new javax.swing.JToolBar.Separator();
        tbCodeFold = new javax.swing.JButton();
        tbInsertSection = new javax.swing.JButton();
        jSeparator31 = new javax.swing.JToolBar.Separator();
        tbPreferences = new javax.swing.JButton();
        tbHelp = new javax.swing.JButton();

        mainPanel.setName("mainPanel"); // NOI18N

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(350);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setName("jSplitPane1"); // NOI18N
        jSplitPane1.setOneTouchExpandable(true);

        jPanel1.setName("jPanel1"); // NOI18N

        jSplitPaneEditorList.setBorder(null);
        jSplitPaneEditorList.setDividerLocation(575);
        jSplitPaneEditorList.setName("jSplitPaneEditorList"); // NOI18N

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

        jButtonCloseFind.setAction(actionMap.get("findCancel")); // NOI18N
        jButtonCloseFind.setBorderPainted(false);
        jButtonCloseFind.setContentAreaFilled(false);
        jButtonCloseFind.setName("jButtonCloseFind"); // NOI18N

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
                        .add(jButtonFindNext)
                        .add(0, 0, 0)
                        .add(jButtonCloseFind)))
                .add(0, 0, 0))
        );
        jPanelFindLayout.setVerticalGroup(
            jPanelFindLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelFindLayout.createSequentialGroup()
                .add(jPanelFindLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jLabel5)
                    .add(jButtonFindPrev)
                    .add(jButtonFindNext)
                    .add(jComboBoxFind, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonCloseFind))
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
            .add(0, 575, Short.MAX_VALUE)
            .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanelFind, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanelReplace, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 348, Short.MAX_VALUE)
            .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel7Layout.createSequentialGroup()
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(jPanelFind, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(jPanelReplace, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName(resourceMap.getString("jTabbedPane1.AccessibleContext.accessibleName")); // NOI18N

        jSplitPaneEditorList.setLeftComponent(jPanel7);

        jPanel8.setName("jPanel8"); // NOI18N

        jScrollPaneSidebar.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, resourceMap.getColor("jScrollPaneSidebar.border.matteColor"))); // NOI18N
        jScrollPaneSidebar.setName("jScrollPaneSidebar"); // NOI18N

        jListGoto.setBackground(ConstantsR64.OSX_BG_STYLE);
        jListGoto.setName("jListGoto"); // NOI18N
        jScrollPaneSidebar.setViewportView(jListGoto);

        jTextFieldGoto.setToolTipText(resourceMap.getString("jTextFieldGoto.toolTipText")); // NOI18N
        jTextFieldGoto.setName("jTextFieldGoto"); // NOI18N

        jButtonRefreshGoto.setAction(actionMap.get("refreshGotoList")); // NOI18N
        jButtonRefreshGoto.setBorder(null);
        jButtonRefreshGoto.setBorderPainted(false);
        jButtonRefreshGoto.setContentAreaFilled(false);
        jButtonRefreshGoto.setName("jButtonRefreshGoto"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jComboBoxSortSidebar.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "order of appearance", "case-sensitive", "non-case-sensitive" }));
        jComboBoxSortSidebar.setName("jComboBoxSortSidebar"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPaneSidebar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
            .add(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel8Layout.createSequentialGroup()
                        .add(jTextFieldGoto)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonRefreshGoto))
                    .add(jPanel8Layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jComboBoxSortSidebar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .add(jScrollPaneSidebar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jComboBoxSortSidebar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jTextFieldGoto, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonRefreshGoto, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jSplitPaneEditorList.setRightComponent(jPanel8);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSplitPaneEditorList)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPaneEditorList)
        );

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel2.setName("jPanel2"); // NOI18N

        jTabbedPaneLogs.setName("jTabbedPaneLogs"); // NOI18N

        jPanel6.setName("jPanel6"); // NOI18N

        jScrollPaneLog.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));
        jScrollPaneLog.setName("jScrollPaneLog"); // NOI18N

        jTextAreaLog.setEditable(false);
        jTextAreaLog.setBackground(ConstantsR64.OSX_BG_STYLE);
        jTextAreaLog.setWrapStyleWord(true);
        jTextAreaLog.setName("jTextAreaLog"); // NOI18N
        jScrollPaneLog.setViewportView(jTextAreaLog);

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 869, Short.MAX_VALUE)
            .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jScrollPaneLog, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 869, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 145, Short.MAX_VALUE)
            .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jScrollPaneLog, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
        );

        jTabbedPaneLogs.addTab(resourceMap.getString("jPanel6.TabConstraints.tabTitle"), jPanel6); // NOI18N

        jPanel5.setName("jPanel5"); // NOI18N

        jScrollPaneErrorLog.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));
        jScrollPaneErrorLog.setName("jScrollPaneErrorLog"); // NOI18N

        jTextAreaCompilerOutput.setEditable(false);
        jTextAreaCompilerOutput.setBackground(ConstantsR64.OSX_BG_STYLE);
        jTextAreaCompilerOutput.setName("jTextAreaCompilerOutput"); // NOI18N
        jScrollPaneErrorLog.setViewportView(jTextAreaCompilerOutput);

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPaneErrorLog, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 869, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPaneErrorLog, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
        );

        jTabbedPaneLogs.addTab(resourceMap.getString("jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPaneLogs)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPaneLogs)
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
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
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

        jSeparator26.setName("jSeparator26"); // NOI18N
        fileMenu.add(jSeparator26);

        openIncludeFileMenuItem.setAction(actionMap.get("openIncludeFile")); // NOI18N
        openIncludeFileMenuItem.setName("openIncludeFileMenuItem"); // NOI18N
        fileMenu.add(openIncludeFileMenuItem);

        openAllIncludedMenuItem.setAction(actionMap.get("openAllIncludeFiles")); // NOI18N
        openAllIncludedMenuItem.setName("openAllIncludedMenuItem"); // NOI18N
        fileMenu.add(openAllIncludedMenuItem);

        jSeparator27.setName("jSeparator27"); // NOI18N
        fileMenu.add(jSeparator27);

        openFolderMenuItem.setAction(actionMap.get("openSourcefileFolder")); // NOI18N
        openFolderMenuItem.setName("openFolderMenuItem"); // NOI18N
        fileMenu.add(openFolderMenuItem);

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

        jMenuItemToLowercase.setAction(actionMap.get("selectionToLowercase")); // NOI18N
        jMenuItemToLowercase.setName("jMenuItemToLowercase"); // NOI18N
        editMenu.add(jMenuItemToLowercase);

        jMenuItemToUppercase.setAction(actionMap.get("selectionToUppercase")); // NOI18N
        jMenuItemToUppercase.setName("jMenuItemToUppercase"); // NOI18N
        editMenu.add(jMenuItemToUppercase);

        jSeparator34.setName("jSeparator34"); // NOI18N
        editMenu.add(jSeparator34);

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

        jSeparator35.setName("jSeparator35"); // NOI18N
        editMenu.add(jSeparator35);

        jMenuItemExternEditor.setAction(actionMap.get("openInExternalEditor")); // NOI18N
        jMenuItemExternEditor.setName("jMenuItemExternEditor"); // NOI18N
        editMenu.add(jMenuItemExternEditor);

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

        jumpBackToLabelMenuItem.setAction(actionMap.get("jumpBackToLabelSource")); // NOI18N
        jumpBackToLabelMenuItem.setName("jumpBackToLabelMenuItem"); // NOI18N
        gotoMenu.add(jumpBackToLabelMenuItem);

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

        debugScriptMenuIitem.setAction(actionMap.get("runScript2")); // NOI18N
        debugScriptMenuIitem.setName("debugScriptMenuIitem"); // NOI18N
        sourceMenu.add(debugScriptMenuIitem);

        jSeparator36.setName("jSeparator36"); // NOI18N
        sourceMenu.add(jSeparator36);

        focusScriptMenuItem.setAction(actionMap.get("selectUserScripts")); // NOI18N
        focusScriptMenuItem.setName("focusScriptMenuItem"); // NOI18N
        sourceMenu.add(focusScriptMenuItem);

        selectAltScriptMenuItem.setAction(actionMap.get("selectAltUserScripts")); // NOI18N
        selectAltScriptMenuItem.setName("selectAltScriptMenuItem"); // NOI18N
        sourceMenu.add(selectAltScriptMenuItem);

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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 260, Short.MAX_VALUE)
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

        jToolBar.setBorder(null);
        jToolBar.setFloatable(false);
        jToolBar.setRollover(true);
        jToolBar.setName("jToolBar"); // NOI18N

        tbNew.setAction(actionMap.get("addNewTab")); // NOI18N
        tbNew.setText(resourceMap.getString("tbNew.text")); // NOI18N
        tbNew.setBorderPainted(false);
        tbNew.setFocusable(false);
        tbNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbNew.setName("tbNew"); // NOI18N
        tbNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbNew);

        tbFileOpen.setAction(actionMap.get("openFile")); // NOI18N
        tbFileOpen.setText(resourceMap.getString("tbFileOpen.text")); // NOI18N
        tbFileOpen.setBorderPainted(false);
        tbFileOpen.setFocusable(false);
        tbFileOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbFileOpen.setName("tbFileOpen"); // NOI18N
        tbFileOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbFileOpen);

        tbSave.setAction(actionMap.get("saveFile")); // NOI18N
        tbSave.setText(resourceMap.getString("tbSave.text")); // NOI18N
        tbSave.setBorderPainted(false);
        tbSave.setFocusable(false);
        tbSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbSave.setName("tbSave"); // NOI18N
        tbSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbSave);

        tbSaveAll.setAction(actionMap.get("saveAllFiles")); // NOI18N
        tbSaveAll.setText(resourceMap.getString("tbSaveAll.text")); // NOI18N
        tbSaveAll.setBorderPainted(false);
        tbSaveAll.setFocusable(false);
        tbSaveAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbSaveAll.setName("tbSaveAll"); // NOI18N
        tbSaveAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbSaveAll);

        jSeparator28.setName("jSeparator28"); // NOI18N
        jToolBar.add(jSeparator28);

        tbUndo.setAction(actionMap.get("undoAction")); // NOI18N
        tbUndo.setBorderPainted(false);
        tbUndo.setFocusable(false);
        tbUndo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbUndo.setName("tbUndo"); // NOI18N
        tbUndo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbUndo);

        tbRedo.setAction(actionMap.get("redoAction")); // NOI18N
        tbRedo.setBorderPainted(false);
        tbRedo.setFocusable(false);
        tbRedo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbRedo.setName("tbRedo"); // NOI18N
        tbRedo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbRedo);

        jSeparator33.setName("jSeparator33"); // NOI18N
        jToolBar.add(jSeparator33);

        tbCut.setAction(actionMap.get("cutAction")); // NOI18N
        tbCut.setBorderPainted(false);
        tbCut.setFocusable(false);
        tbCut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbCut.setName("tbCut"); // NOI18N
        tbCut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbCut);

        tbCopy.setAction(actionMap.get("copyAction")); // NOI18N
        tbCopy.setBorderPainted(false);
        tbCopy.setFocusable(false);
        tbCopy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbCopy.setName("tbCopy"); // NOI18N
        tbCopy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbCopy);

        tbPaste.setAction(actionMap.get("pasteAction")); // NOI18N
        tbPaste.setBorderPainted(false);
        tbPaste.setFocusable(false);
        tbPaste.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbPaste.setName("tbPaste"); // NOI18N
        tbPaste.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbPaste);

        jSeparator29.setName("jSeparator29"); // NOI18N
        jToolBar.add(jSeparator29);

        tbFind.setAction(actionMap.get("findStart")); // NOI18N
        tbFind.setText(resourceMap.getString("tbFind.text")); // NOI18N
        tbFind.setBorderPainted(false);
        tbFind.setFocusable(false);
        tbFind.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbFind.setName("tbFind"); // NOI18N
        tbFind.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbFind);

        tbFindNext.setAction(actionMap.get("findNext")); // NOI18N
        tbFindNext.setText(resourceMap.getString("tbFindNext.text")); // NOI18N
        tbFindNext.setBorderPainted(false);
        tbFindNext.setFocusable(false);
        tbFindNext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbFindNext.setName("tbFindNext"); // NOI18N
        tbFindNext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbFindNext);

        tbReplace.setAction(actionMap.get("replaceTerm")); // NOI18N
        tbReplace.setBorderPainted(false);
        tbReplace.setFocusable(false);
        tbReplace.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbReplace.setName("tbReplace"); // NOI18N
        tbReplace.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbReplace);

        jSeparator30.setName("jSeparator30"); // NOI18N
        jToolBar.add(jSeparator30);

        jComboBoxRunScripts.setName("jComboBoxRunScripts"); // NOI18N
        jToolBar.add(jComboBoxRunScripts);

        tbRunScript.setAction(actionMap.get("runScript")); // NOI18N
        tbRunScript.setBorderPainted(false);
        tbRunScript.setFocusable(false);
        tbRunScript.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbRunScript.setName("tbRunScript"); // NOI18N
        tbRunScript.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbRunScript);

        tbPrevError.setAction(actionMap.get("gotoPrevError")); // NOI18N
        tbPrevError.setText(resourceMap.getString("tbPrevError.text")); // NOI18N
        tbPrevError.setBorderPainted(false);
        tbPrevError.setFocusable(false);
        tbPrevError.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbPrevError.setName("tbPrevError"); // NOI18N
        tbPrevError.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbPrevError);

        tbNextError.setAction(actionMap.get("gotoNextError")); // NOI18N
        tbNextError.setText(resourceMap.getString("tbNextError.text")); // NOI18N
        tbNextError.setBorderPainted(false);
        tbNextError.setFocusable(false);
        tbNextError.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbNextError.setName("tbNextError"); // NOI18N
        tbNextError.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbNextError);

        jSeparator32.setName("jSeparator32"); // NOI18N
        jToolBar.add(jSeparator32);

        tbCodeFold.setAction(actionMap.get("surroundFolds")); // NOI18N
        tbCodeFold.setText(resourceMap.getString("tbCodeFold.text")); // NOI18N
        tbCodeFold.setBorderPainted(false);
        tbCodeFold.setFocusable(false);
        tbCodeFold.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbCodeFold.setName("tbCodeFold"); // NOI18N
        tbCodeFold.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbCodeFold);

        tbInsertSection.setAction(actionMap.get("insertSection")); // NOI18N
        tbInsertSection.setText(resourceMap.getString("tbInsertSection.text")); // NOI18N
        tbInsertSection.setBorderPainted(false);
        tbInsertSection.setFocusable(false);
        tbInsertSection.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbInsertSection.setName("tbInsertSection"); // NOI18N
        tbInsertSection.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbInsertSection);

        jSeparator31.setName("jSeparator31"); // NOI18N
        jToolBar.add(jSeparator31);

        tbPreferences.setAction(actionMap.get("settingsWindow")); // NOI18N
        tbPreferences.setText(resourceMap.getString("tbPreferences.text")); // NOI18N
        tbPreferences.setBorderPainted(false);
        tbPreferences.setFocusable(false);
        tbPreferences.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbPreferences.setName("tbPreferences"); // NOI18N
        tbPreferences.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbPreferences);

        tbHelp.setAction(actionMap.get("showHelp")); // NOI18N
        tbHelp.setText(resourceMap.getString("tbHelp.text")); // NOI18N
        tbHelp.setBorderPainted(false);
        tbHelp.setFocusable(false);
        tbHelp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbHelp.setName("tbHelp"); // NOI18N
        tbHelp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar.add(tbHelp);

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
        setToolBar(jToolBar);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem addTabMenuItem;
    private javax.swing.JMenuItem closeAllMenuItem;
    private javax.swing.JMenuItem closeFileMenuItem;
    private javax.swing.JMenuItem commentLineMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem debugScriptMenuIitem;
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
    private javax.swing.JButton jButtonCloseFind;
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
    private javax.swing.JComboBox jComboBoxSortSidebar;
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
    private javax.swing.JMenuItem jMenuItemExternEditor;
    private javax.swing.JMenuItem jMenuItemNextFold;
    private javax.swing.JMenuItem jMenuItemPrevFold;
    private javax.swing.JMenuItem jMenuItemShowHideGoto;
    private javax.swing.JMenuItem jMenuItemSpaceToTab;
    private javax.swing.JMenuItem jMenuItemSurroundFolds;
    private javax.swing.JMenuItem jMenuItemTabsToSpaces;
    private javax.swing.JMenuItem jMenuItemToLowercase;
    private javax.swing.JMenuItem jMenuItemToUppercase;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanelFind;
    private javax.swing.JPanel jPanelReplace;
    private javax.swing.JScrollPane jScrollPaneErrorLog;
    private javax.swing.JScrollPane jScrollPaneLog;
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
    private javax.swing.JPopupMenu.Separator jSeparator26;
    private javax.swing.JPopupMenu.Separator jSeparator27;
    private javax.swing.JToolBar.Separator jSeparator28;
    private javax.swing.JToolBar.Separator jSeparator29;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator30;
    private javax.swing.JToolBar.Separator jSeparator31;
    private javax.swing.JToolBar.Separator jSeparator32;
    private javax.swing.JToolBar.Separator jSeparator33;
    private javax.swing.JPopupMenu.Separator jSeparator34;
    private javax.swing.JPopupMenu.Separator jSeparator35;
    private javax.swing.JPopupMenu.Separator jSeparator36;
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
    private javax.swing.JToolBar jToolBar;
    private javax.swing.JMenuItem jumpBackToLabelMenuItem;
    private javax.swing.JMenuItem jumpToLabelMenuItem;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openAllIncludedMenuItem;
    private javax.swing.JMenuItem openFileMenuItem;
    private javax.swing.JMenuItem openFolderMenuItem;
    private javax.swing.JMenuItem openIncludeFileMenuItem;
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
    private javax.swing.JMenuItem selectAltScriptMenuItem;
    private javax.swing.JMenuItem settingsMenuItem;
    private javax.swing.JMenu sourceMenu;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JMenuItem switchLogPosMenuItem;
    private javax.swing.JButton tbCodeFold;
    private javax.swing.JButton tbCopy;
    private javax.swing.JButton tbCut;
    private javax.swing.JButton tbFileOpen;
    private javax.swing.JButton tbFind;
    private javax.swing.JButton tbFindNext;
    private javax.swing.JButton tbHelp;
    private javax.swing.JButton tbInsertSection;
    private javax.swing.JButton tbNew;
    private javax.swing.JButton tbNextError;
    private javax.swing.JButton tbPaste;
    private javax.swing.JButton tbPreferences;
    private javax.swing.JButton tbPrevError;
    private javax.swing.JButton tbRedo;
    private javax.swing.JButton tbReplace;
    private javax.swing.JButton tbRunScript;
    private javax.swing.JButton tbSave;
    private javax.swing.JButton tbSaveAll;
    private javax.swing.JButton tbUndo;
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
