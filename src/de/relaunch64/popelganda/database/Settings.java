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
package de.relaunch64.popelganda.database;

import de.relaunch64.popelganda.Editor.ColorSchemes;
import de.relaunch64.popelganda.Editor.EditorPanes;
import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.FileTools;
import de.relaunch64.popelganda.assemblers.Assembler;
import de.relaunch64.popelganda.assemblers.Assemblers;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JSplitPane;
import org.gjt.sp.jedit.textarea.AntiAlias;
import org.gjt.sp.jedit.textarea.Gutter;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author Daniel Luedecke
 */
public class Settings {

    /**
     * Amount of stored recent documents
     */
    private static final int recentDocCount = 10;

    private static final String SETTING_RECENT_DOC = "recentDoc";
    private static final String SETTING_LAST_USED_PATH = "lastusedpath";
    private static final String SETTING_PREF_ASM = "preferredCompiler";
    private static final String SETTING_SIDEBAR_SORT = "sidebarsort";
    private static final String SETTING_SYNTAX_SCHEME = "syntaxscheme";
    private static final String SETTING_LAST_SCRIPT = "lastUserScript";
    private static final String SETTING_CODE_FOLDING = "codefolding";
    private static final String SETTING_CF_MANUAL = "codefolding_manual";
    private static final String SETTING_CF_DIRECTIVES = "codefolding_directive";
    private static final String SETTING_CF_STRUCTS = "codefolding_structural";
    private static final String SETTING_CF_SECTIONS = "codefolding_sections";
    private static final String SETTING_CF_BRACES = "codefolding_braces";
    private static final String SETTING_CF_LABELS = "codefolding_labels";
    private static final String SETTING_ALT_ASM_MODE = "alternativeAssemblyMode";
    private static final String SETTING_SHOW_LINEHIGLIGHT = "showlinehighlight";
    private static final String SETTING_SHOW_BUFFERSIZE = "showbuffersize";
    private static final String SETTING_SHOW_EXT_IN_TAB = "showfileextintab";
    private static final String SETTING_SHOW_CLOSEBUTTON = "showclosebutton";
    private static final String SETTING_SHOW_TOOLBAR = "showtoolbar";
    private static final String SETTING_SHOW_TOOLBARTEXT = "showtoolbartext";
    private static final String SETTING_SUGGEST_SORT_CASE = "suggestioncasesort";
    private static final String SETTING_ANTIALIAS = "antialias";
    private static final String SETTING_SCALE_FONT = "scalefont";
    private static final String REC_DOC_ASSEMBLER = "compiler";
    private static final String REC_DOC_SCRIPT = "script";
    private static final String REC_DOC_ALT_SCRIPT = "altscript";
    private static final String SETTING_MAINFONT = "editorfont";
    private static final String SETTING_CHECKUPDATES = "checkupdates";
    private static final String SETTING_FINDFIELDFOCUS = "findfieldfocus";
    private static final String SETTING_SAVEONCOMPILE = "saveoncompile";
    private static final String SETTING_NIMBUS_ON_OSX = "nimbusonosx";
    private static final String SETTING_USE_SCROLL_TABS = "usescrolltabs";
    private static final String SETTING_LOGSPLITLAYOUT = "logsplitlayout";
    private static final String SETTING_TABWIDTH = "tabwidth";
    private static final String SETTING_LISTGOTOINDEX = "listgotoindex";
    private static final String SETTING_LINE_NUMBER_ALIGNMENT = "linenumberalignment";
    private static final String SETTING_REOPEN_FILES_ON_STARTUP = "rofstartup";
    private static final String SETTING_REOPEN_FILES = "reopenfiles";
    private static final String SETTING_REOPEN_FILES_CHILD = "rof";
    private static final String SETTING_DIVIDER_LOCATION = "dividerlocation";
    private static final String SETTING_SIDEBAR_ISHIDDEN = "dividerishidden";
    private static final String SETTING_USE_NOTABS = "usenotabs";
    private static final String SETTING_FINDBYTYPE = "findbytype";
    private static final String SETTING_WAITFORPROCESS = "waitforprocess";
    private static final String SETTING_EXTERNALEDITOR = "externaleditor";

    private static final String ATTR_ASM = "compiler";
    private static final String ATTR_SCRIPT = "script";
    private static final String ATTR_ALT_SCRIPT = "alt_script";

    private final File filepath;
    public static final int FONTNAME = 1;
    public static final int FONTSIZE = 2;

    public static final int SORT_ORDER = 0;
    public static final int SORT_CASE = 1;
    public static final int SORT_NONCASE = 2;

    /**
     * XML-Document that stores the settings-information
     */
    private Document settingsFile;
    private Element root;

    public Settings() {
        // first of all, create the empty documents
        settingsFile = new Document(new Element("settings"));
        // create file path to settings file
        filepath = FileTools.createFilePath("relaunch64-settings.xml");
        // now fill the initoal elements
        fillElements();
    }

    /**
     * Loads the settings file
     */
    public void loadSettings() {
        // if file exists, go on...
        if (filepath != null && filepath.exists()) {
            try {
                SAXBuilder builder = new SAXBuilder();
                settingsFile = builder.build(filepath);
            } catch (JDOMException | IOException ex) {
                ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
            }
        }
        fillElements();
    }

    /**
     * Loads the settings file
     */
    public void saveSettings() {
        // if file exists, go on...
        if (filepath != null) {
            OutputStream dest = null;
            try {
                XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
                dest = new FileOutputStream(filepath);
                out.output(settingsFile, dest);
            } catch (IOException ex) {
                ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
            } finally {
                if (dest != null) {
                    try {
                        dest.close();
                    } catch (IOException ex) {
                        ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
                    }
                }
            }
        }
    }

    /**
     * This method creates all the settings-child-elements, but only, if they don't already exist.
     * We do this because when loading older settings-xml-document-structures, we might have new
     * elements that would not be initialised. but now we can call this method after loading the
     * xml-document, and create elements and default values for all new elements. This ensures
     * compatibility to older/news settings-file-versions.
     */
    private void fillElements() {
        root = settingsFile.getRootElement();
        for (int cnt = 0; cnt < recentDocCount; cnt++) {
            // create field-identifier
            String fi = SETTING_RECENT_DOC + String.valueOf(cnt + 1);
            // retrieve content
            if (null == root.getChild(fi)) {
                // create a filepath-element
                Element el = new Element(fi);
                el.setText("");
                // and add it to the document
                root.addContent(el);
            }
        }
        if (null == root.getChild(SETTING_REOPEN_FILES)) {
            // create element for font
            Element el = new Element(SETTING_REOPEN_FILES);
            root.addContent(el);
        }
        if (null == root.getChild(SETTING_MAINFONT)) {
            // create element for font
            Element el = new Element(SETTING_MAINFONT);
            root.addContent(el);
            el.setText(Font.MONOSPACED);
            el.setAttribute("size", "12");
        }
        
        genericElementInit(SETTING_LAST_USED_PATH, "");
        genericElementInit(SETTING_EXTERNALEDITOR, "");
        genericElementInit(SETTING_ANTIALIAS, AntiAlias.SUBPIXEL);
        genericElementInit(SETTING_SIDEBAR_ISHIDDEN, "1");
        genericElementInit(SETTING_USE_NOTABS, "0");
        genericElementInit(SETTING_FINDBYTYPE, "1");
        genericElementInit(SETTING_WAITFORPROCESS, "1");
        genericElementInit(SETTING_CODE_FOLDING, "1");
        genericElementInit(SETTING_CF_BRACES, "1");
        genericElementInit(SETTING_CF_DIRECTIVES, "1");
        genericElementInit(SETTING_CF_LABELS, "1");
        genericElementInit(SETTING_CF_MANUAL, "1");
        genericElementInit(SETTING_CF_STRUCTS, "1");
        genericElementInit(SETTING_CF_SECTIONS, "1");
        genericElementInit(SETTING_SCALE_FONT, "1");
        genericElementInit(SETTING_CHECKUPDATES, "1");
        genericElementInit(SETTING_FINDFIELDFOCUS, "0");
        genericElementInit(SETTING_ALT_ASM_MODE, "0");
        genericElementInit(SETTING_SHOW_LINEHIGLIGHT, "0");
        genericElementInit(SETTING_SHOW_BUFFERSIZE, "1");
        genericElementInit(SETTING_SHOW_EXT_IN_TAB, "0");
        genericElementInit(SETTING_SHOW_CLOSEBUTTON, "1");
        genericElementInit(SETTING_SHOW_TOOLBAR, "1");
        genericElementInit(SETTING_SHOW_TOOLBARTEXT, "1");
        genericElementInit(SETTING_SUGGEST_SORT_CASE, "0");
        genericElementInit(SETTING_REOPEN_FILES_ON_STARTUP, "1");
        genericElementInit(SETTING_SAVEONCOMPILE, "1");
        genericElementInit(SETTING_NIMBUS_ON_OSX, "0");
        genericElementInit(SETTING_USE_SCROLL_TABS, "0");
        genericElementInit(SETTING_PREF_ASM, String.valueOf(Assemblers.ASM_KICKASSEMBLER.getID()));
        genericElementInit(SETTING_SIDEBAR_SORT, String.valueOf(SORT_CASE));
        genericElementInit(SETTING_SYNTAX_SCHEME, String.valueOf(ColorSchemes.SCHEME_DEFAULT));
        genericElementInit(SETTING_LAST_SCRIPT, "0");
        genericElementInit(SETTING_LOGSPLITLAYOUT, String.valueOf(JSplitPane.HORIZONTAL_SPLIT));
        genericElementInit(SETTING_TABWIDTH, "4");
        genericElementInit(SETTING_LISTGOTOINDEX, "1");
        genericElementInit(SETTING_DIVIDER_LOCATION, "-1");
        genericElementInit(SETTING_LINE_NUMBER_ALIGNMENT, String.valueOf(Gutter.RIGHT));
    }

    private void genericElementInit(String attr, String value) {
        if (null == settingsFile.getRootElement().getChild(attr)) {
            // create a filepath-element
            Element el = new Element(attr);
            el.setText(value);
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
    }
    
    /**
     * Retrieves the recent document at the position {@code nr}. Returns {@code null} if recent
     * document does not exist or is empty
     *
     * @param nr the number of the requested recent document. use a value from 1 to
     * {@link #recentDocCount recentDocCount}.
     * @return the recent document (the file path) as string, or {@code null} if no such element or
     * path exists.
     */
    public File getRecentDoc(int nr) {
        // checl for valid parameter
        if (nr < 0) {
            return null;
        }
        // retrieve element
        Element el = root.getChild(SETTING_RECENT_DOC + String.valueOf(nr));
        // if we have any valid document
        if (el != null) {
            // check whether its value is empty
            String retval = el.getText();
            // and if not, return in
            if (!retval.isEmpty()) {
                return new File(retval);
            }
        }
        // else return null
        return null;
    }

    public int findRecentDoc(File f) {
        if (null == f) {
            return -1;
        }
        for (int i = 0; i < recentDocCount; i++) {
            File rf = getRecentDoc(i);
            if (rf != null && f.equals(rf)) {
                return i;
            }
        }
        return -1;
    }

    /**
     *
     * @param nr
     * @return
     */
    public Assembler getRecentDocAssembler(int nr) {
        // checl for valid parameter
        if (nr < 0) {
            return Assemblers.ASM_KICKASSEMBLER;
        }
        // retrieve element
        Element el = root.getChild(SETTING_RECENT_DOC + String.valueOf(nr));
        // if we have any valid document
        if (el != null) {
            // retrieve compiler attribute
            Attribute comp = el.getAttribute(REC_DOC_ASSEMBLER);
            // if we have any valid attribute
            if (comp != null) {
                try {
                    return Assemblers.byID(Integer.parseInt(comp.getValue()));
                } catch (NumberFormatException ex) {
                    return Assemblers.ASM_KICKASSEMBLER;
                }
            }
        }
        // else return null
        return Assemblers.ASM_KICKASSEMBLER;
    }

    /**
     *
     * @param nr
     * @return
     */
    public int getRecentDocScript(int nr) {
        // checl for valid parameter
        if (nr < 0) {
            return 0;
        }
        // retrieve element
        Element el = root.getChild(SETTING_RECENT_DOC + String.valueOf(nr));
        // if we have any valid document
        if (el != null) {
            // retrieve compiler attribute
            Attribute comp = el.getAttribute(REC_DOC_SCRIPT);
            // if we have any valid attribute
            if (comp != null) {
                try {
                    return Integer.parseInt(comp.getValue());
                } catch (NumberFormatException ex) {
                    return 0;
                }
            }
        }
        // else return null
        return 0;
    }

    /**
     * 
     * @param nr
     * @return 
     */
    public int getRecentDocAltScript(int nr) {
        // checl for valid parameter
        if (nr < 0) {
            return 0;
        }
        // retrieve element
        Element el = root.getChild(SETTING_RECENT_DOC + String.valueOf(nr));
        // if we have any valid document
        if (el != null) {
            // retrieve compiler attribute
            Attribute comp = el.getAttribute(REC_DOC_ALT_SCRIPT);
            // if we have any valid attribute
            if (comp != null) {
                try {
                    return Integer.parseInt(comp.getValue());
                } catch (NumberFormatException ex) {
                    return 0;
                }
            }
        }
        // else return null
        return 0;
    }

    /**
     *
     * @param doc
     * @return
     */
    public int findRecentDoc(String doc) {
        if (null == doc || doc.isEmpty()) {
            return -1;
        }
        // iterate all current recent documents
        for (int cnt = 1; cnt <= recentDocCount; cnt++) {
            if (getRecentDoc(cnt).getPath().equals(doc)) {
                return cnt;
            }
        }
        return -1;
    }

    /**
     * Retrieves the recent document at the position {@code nr}. Returns {@code null} if recent
     * document does not exist or is empty
     *
     * @param nr the number of the requested recent document. use a value from 1 to
     * {@link #recentDocCount recentDocCount}.
     * @return the recent document (the file path) as string, or {@code null} if no such element or
     * path exists.
     */
    private String getRecentDocAsString(int nr) {
        // retrieve element
        File rd = getRecentDoc(nr);
        // check for valid value
        if (rd != null) {
            return rd.toString();
        }
        // else return null
        return null;
    }

    /**
     * This method adds the file from the filepath {@code fp} to the list of recent documents and
     * rotates that list, if necessary.
     *
     * @param fp the filepath to the document that should be added to the list of recent documents
     * @param assembler
     * @param userScript
     * @param alternativeScript
     */
    public void addToRecentDocs(String fp, Assembler assembler, int userScript, int alternativeScript) {
        // check for valid parameter
        if (null == fp || fp.isEmpty()) {
            return;
        }
        // check whether file exists
        File dummy = new File(fp);
        if (!dummy.exists()) {
            return;
        }
        // create linked list
        LinkedList<String> recdocs = new LinkedList<>();
        // linked list for assemblers
        LinkedList<Assembler> recasms = new LinkedList<>();
        // linked list for scripts
        LinkedList<Integer> recscripts = new LinkedList<>();
        // linked list for alternative scripts
        LinkedList<Integer> recaltscripts = new LinkedList<>();
        // add new filepath to linked list
        recdocs.add(fp);
        recasms.add(assembler);
        recscripts.add(userScript);
        recaltscripts.add(alternativeScript);
        // iterate all current recent documents
        for (int cnt = 1; cnt <= recentDocCount; cnt++) {
            // retrieve recent document
            String recentDoc = getRecentDocAsString(cnt);
            Assembler asm = getRecentDocAssembler(cnt);
            int script = getRecentDocScript(cnt);
            int altScript = getRecentDocAltScript(cnt);
            // check whether the linked list already contains such a document
            if (recentDoc != null && !recentDoc.isEmpty()) {
                // check for existing file
                dummy = new File(recentDoc);
                // if not, add it to the list
                if (dummy.exists() && !recdocs.contains(recentDoc)) {
                    recasms.add(asm);
                    recscripts.add(script);
                    recdocs.add(recentDoc);
                    recaltscripts.add(altScript);
                }
            }
        }
        // iterate all current recent documents again
        for (int cnt = 1; cnt <= recentDocCount; cnt++) {
            // check for valid bounds of linked list
            if (recdocs.size() >= cnt) {
                // and set recent document
                setRecentDoc(cnt, recdocs.get(cnt - 1), recasms.get(cnt - 1), recscripts.get(cnt - 1), recaltscripts.get(cnt - 1));
            } // else fill remaining recent documents with empty strings
            else {
                setRecentDoc(cnt, "", Assemblers.ASM_KICKASSEMBLER, -1, -1);
            }
        }
    }

    /**
     * Add a new recent document to the position {@code nr} in the list of recent documents.
     *
     * @param nr the number of the requested recent document. use a value from 1 to
     * {@link #recentDocCount recentDocCount}.
     * @param fp the filepath to the recently used document as string
     * @param assembler
     * @param userScript
     * @param alternativeScript
     */
    public void setRecentDoc(int nr, String fp, Assembler assembler, int userScript, int alternativeScript) {
        // check for valid parameter
        if (null == fp || -1 == nr) {
            return;
        }
        // retrieve element
        Element el = root.getChild(SETTING_RECENT_DOC + String.valueOf(nr));
        // if element does not exist, create new...
        if (null == el) {
            el = new Element(SETTING_RECENT_DOC + String.valueOf(nr));
            // and add it to the document
            root.addContent(el);
        }
        // add filepath
        el.setText(fp);
        el.setAttribute(REC_DOC_ASSEMBLER, String.valueOf(assembler.getID()));
        el.setAttribute(REC_DOC_SCRIPT, String.valueOf(userScript));
        el.setAttribute(REC_DOC_ALT_SCRIPT, String.valueOf(alternativeScript));
    }

    public File getLastUsedPath() {
        Element el = root.getChild(SETTING_LAST_USED_PATH);
        // changed to Yoda condition
        return new File((null == el) ? "" : el.getText());
    }

    public void setLastUsedPath(File f) {
        Element el = root.getChild(SETTING_LAST_USED_PATH);
        if (null == el) {
            el = new Element(SETTING_LAST_USED_PATH);
            root.addContent(el);
        }
        el.setText(f.getAbsolutePath());
    }

    public String getAntiAlias() {
        return genericStringGetter(SETTING_ANTIALIAS, AntiAlias.SUBPIXEL);
    }

    public void setAntiAlias(String aa) {
        genericStringSetter(SETTING_ANTIALIAS, aa);
    }

    public boolean getScaleFont() {
        return genericBooleanGetter(SETTING_SCALE_FONT, false);
    }

    public void setScaleFont(boolean scale) {
        genericBooleanSetter(SETTING_SCALE_FONT, scale);
    }

    public boolean getSidebarIsHidden() {
        return genericBooleanGetter(SETTING_SIDEBAR_ISHIDDEN, false);
    }

    public void setSidebarIsHidden(boolean val) {
        genericBooleanSetter(SETTING_SIDEBAR_ISHIDDEN, val);
    }

    public boolean getUseNoTabs() {
        return genericBooleanGetter(SETTING_USE_NOTABS, false);
    }

    public void setUseNoTabs(boolean val) {
        genericBooleanSetter(SETTING_USE_NOTABS, val);
    }

    public boolean getFindByType() {
        return genericBooleanGetter(SETTING_FINDBYTYPE, false);
    }

    public void setFindByType(boolean val) {
        genericBooleanSetter(SETTING_FINDBYTYPE, val);
    }

    public boolean getWaitForProcess() {
        Element el = root.getChild(SETTING_WAITFORPROCESS);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setWaitForProcess(boolean val) {
        Element el = root.getChild(SETTING_WAITFORPROCESS);
        if (null == el) {
            el = new Element(SETTING_WAITFORPROCESS);
            root.addContent(el);
        }
        el.setText(val == Boolean.TRUE ? "1" : "0");
    }

    public boolean getCodeFolding() {
        Element el = root.getChild(SETTING_CODE_FOLDING);
        if (el != null) {
            return el.getText().equals("1");
        }
        return true;
    }

    public void setCodeFolding(boolean val) {
        Element el = root.getChild(SETTING_CODE_FOLDING);
        if (null == el) {
            el = new Element(SETTING_CODE_FOLDING);
            root.addContent(el);
        }
        el.setText(val == Boolean.TRUE ? "1" : "0");
    }

    public int getCodeFoldingTokens() {
        int foldtokens = 0;
        Element el = root.getChild(SETTING_CF_MANUAL);
        if (el != null) {
            foldtokens = foldtokens + (el.getText().equals("1") ? Assemblers.CF_TOKEN_MANUAL : 0);
        }
        el = root.getChild(SETTING_CF_BRACES);
        if (el != null) {
            foldtokens = foldtokens + (el.getText().equals("1") ? Assemblers.CF_TOKEN_BRACES : 0);
        }
        el = root.getChild(SETTING_CF_LABELS);
        if (el != null) {
            foldtokens = foldtokens + (el.getText().equals("1") ? Assemblers.CF_TOKEN_LABELS : 0);
        }
        el = root.getChild(SETTING_CF_DIRECTIVES);
        if (el != null) {
            foldtokens = foldtokens + (el.getText().equals("1") ? Assemblers.CF_TOKEN_DIRECTIVES : 0);
        }
        el = root.getChild(SETTING_CF_STRUCTS);
        if (el != null) {
            foldtokens = foldtokens + (el.getText().equals("1") ? Assemblers.CF_TOKEN_STRUCTS : 0);
        }
        el = root.getChild(SETTING_CF_SECTIONS);
        if (el != null) {
            foldtokens = foldtokens + (el.getText().equals("1") ? Assemblers.CF_TOKEN_SECTIONS : 0);
        }
        return foldtokens;
    }

    public void setCodeFoldingTokens(int tokens) {
        Element el = root.getChild(SETTING_CF_MANUAL);
        if (null == el) {
            el = new Element(SETTING_CF_MANUAL);
            root.addContent(el);
        }
        el.setText(((tokens & Assemblers.CF_TOKEN_MANUAL) != 0) ? "1" : "0");
        el = root.getChild(SETTING_CF_BRACES);
        if (null == el) {
            el = new Element(SETTING_CF_BRACES);
            root.addContent(el);
        }
        el.setText(((tokens & Assemblers.CF_TOKEN_BRACES) != 0) ? "1" : "0");
        el = root.getChild(SETTING_CF_LABELS);
        if (null == el) {
            el = new Element(SETTING_CF_LABELS);
            root.addContent(el);
        }
        el.setText(((tokens & Assemblers.CF_TOKEN_LABELS) != 0) ? "1" : "0");
        el = root.getChild(SETTING_CF_DIRECTIVES);
        if (null == el) {
            el = new Element(SETTING_CF_DIRECTIVES);
            root.addContent(el);
        }
        el.setText(((tokens & Assemblers.CF_TOKEN_DIRECTIVES) != 0) ? "1" : "0");
        el = root.getChild(SETTING_CF_STRUCTS);
        if (null == el) {
            el = new Element(SETTING_CF_STRUCTS);
            root.addContent(el);
        }
        el.setText(((tokens & Assemblers.CF_TOKEN_STRUCTS) != 0) ? "1" : "0");
        el = root.getChild(SETTING_CF_SECTIONS);
        if (null == el) {
            el = new Element(SETTING_CF_SECTIONS);
            root.addContent(el);
        }
        el.setText(((tokens & Assemblers.CF_TOKEN_SECTIONS) != 0) ? "1" : "0");
    }

    public Assembler getPreferredAssembler() {
        Element el = root.getChild(SETTING_PREF_ASM);
        try {
            if (el != null) {
                return Assemblers.byID(Integer.parseInt(el.getText()));
            }
        } catch (NumberFormatException ex) {
        }
        return Assemblers.ASM_KICKASSEMBLER;
    }

    public void setPreferredAssembler(Assembler assembler) {
        Element el = root.getChild(SETTING_PREF_ASM);
        if (null == el) {
            el = new Element(SETTING_PREF_ASM);
            root.addContent(el);
        }
        el.setText(String.valueOf(assembler.getID()));
    }

    public int getSidebarSort() {
        Element el = root.getChild(SETTING_SIDEBAR_SORT);
        try {
            if (el != null) {
                return Integer.parseInt(el.getText());
            }
        } catch (NumberFormatException ex) {
        }
        return SORT_CASE;
    }

    public void setSidebarSort(int sortstyle) {
        Element el = root.getChild(SETTING_SIDEBAR_SORT);
        if (null == el) {
            el = new Element(SETTING_SIDEBAR_SORT);
            root.addContent(el);
        }
        el.setText(String.valueOf(sortstyle));
    }

    public int getColorScheme() {
        Element el = root.getChild(SETTING_SYNTAX_SCHEME);
        try {
            if (el != null) {
                return Integer.parseInt(el.getText());
            }
        } catch (NumberFormatException ex) {
        }
        return ColorSchemes.SCHEME_DEFAULT;
    }

    public void setColorScheme(int scheme) {
        Element el = root.getChild(SETTING_SYNTAX_SCHEME);
        if (null == el) {
            el = new Element(SETTING_SYNTAX_SCHEME);
            root.addContent(el);
        }
        el.setText(String.valueOf(scheme));
    }

    public boolean getCheckForUpdates() {
        Element el = root.getChild(SETTING_CHECKUPDATES);
        if (el != null) {
            return el.getText().equals("1");
        }
        return true;
    }

    public void setCheckForUpdates(boolean val) {
        Element el = root.getChild(SETTING_CHECKUPDATES);
        if (null == el) {
            el = new Element(SETTING_CHECKUPDATES);
            root.addContent(el);
        }
        el.setText(val == Boolean.TRUE ? "1" : "0");
    }

    public boolean getFindFieldFocus() {
        Element el = root.getChild(SETTING_FINDFIELDFOCUS);
        if (el != null) {
            return el.getText().equals("1");
        }
        return true;
    }

    public void setFindFieldFocus(boolean val) {
        Element el = root.getChild(SETTING_FINDFIELDFOCUS);
        if (null == el) {
            el = new Element(SETTING_FINDFIELDFOCUS);
            root.addContent(el);
        }
        el.setText(val == Boolean.TRUE ? "1" : "0");
    }

    public boolean getAlternativeAssemblyMode() {
        Element el = root.getChild(SETTING_ALT_ASM_MODE);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setAlternativeAssemblyMode(boolean val) {
        Element el = root.getChild(SETTING_ALT_ASM_MODE);
        if (null == el) {
            el = new Element(SETTING_ALT_ASM_MODE);
            root.addContent(el);
        }
        el.setText(val == Boolean.TRUE ? "1" : "0");
    }

    public boolean getShowLineHightlight() {
        Element el = root.getChild(SETTING_SHOW_LINEHIGLIGHT);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setShowLineHightlight(boolean val) {
        Element el = root.getChild(SETTING_SHOW_LINEHIGLIGHT);
        if (null == el) {
            el = new Element(SETTING_SHOW_LINEHIGLIGHT);
            root.addContent(el);
        }
        el.setText(val == Boolean.TRUE ? "1" : "0");
    }

    public boolean getShowBufferSize() {
        Element el = root.getChild(SETTING_SHOW_BUFFERSIZE);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setShowBufferSize(boolean val) {
        Element el = root.getChild(SETTING_SHOW_BUFFERSIZE);
        if (null == el) {
            el = new Element(SETTING_SHOW_BUFFERSIZE);
            root.addContent(el);
        }
        el.setText(val == Boolean.TRUE ? "1" : "0");
    }

    public boolean getShowExtensionInTab() {
        Element el = root.getChild(SETTING_SHOW_EXT_IN_TAB);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setShowExtensionInTab(boolean val) {
        Element el = root.getChild(SETTING_SHOW_EXT_IN_TAB);
        if (null == el) {
            el = new Element(SETTING_SHOW_EXT_IN_TAB);
            root.addContent(el);
        }
        el.setText(val == Boolean.TRUE ? "1" : "0");
    }

    public boolean getShowCloseButton() {
        Element el = root.getChild(SETTING_SHOW_CLOSEBUTTON);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setShowCloseButton(boolean val) {
        Element el = root.getChild(SETTING_SHOW_CLOSEBUTTON);
        if (null == el) {
            el = new Element(SETTING_SHOW_CLOSEBUTTON);
            root.addContent(el);
        }
        el.setText(val == Boolean.TRUE ? "1" : "0");
    }

    public boolean getShowToolbar() {
        Element el = root.getChild(SETTING_SHOW_TOOLBAR);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setShowToolbar(boolean val) {
        Element el = root.getChild(SETTING_SHOW_TOOLBAR);
        if (null == el) {
            el = new Element(SETTING_SHOW_TOOLBAR);
            root.addContent(el);
        }
        el.setText(val == Boolean.TRUE ? "1" : "0");
    }

    public boolean getShowToolbarText() {
        Element el = root.getChild(SETTING_SHOW_TOOLBARTEXT);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setShowToolbarText(boolean val) {
        Element el = root.getChild(SETTING_SHOW_TOOLBARTEXT);
        if (null == el) {
            el = new Element(SETTING_SHOW_TOOLBARTEXT);
            root.addContent(el);
        }
        el.setText(val == Boolean.TRUE ? "1" : "0");
    }

    public boolean getSuggestionSortIgnoresCase() {
        Element el = root.getChild(SETTING_SUGGEST_SORT_CASE);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setSuggestionSortIgnoresCase(boolean val) {
        Element el = root.getChild(SETTING_SUGGEST_SORT_CASE);
        if (null == el) {
            el = new Element(SETTING_SUGGEST_SORT_CASE);
            root.addContent(el);
        }
        el.setText(val == Boolean.TRUE ? "1" : "0");
    }

    public boolean getSaveOnCompile() {
        Element el = root.getChild(SETTING_SAVEONCOMPILE);
        if (el != null) {
            return el.getText().equals("1");
        }
        return true;
    }

    public void setSaveOnCompile(boolean val) {
        Element el = root.getChild(SETTING_SAVEONCOMPILE);
        if (null == el) {
            el = new Element(SETTING_SAVEONCOMPILE);
            root.addContent(el);
        }
        el.setText(val == Boolean.TRUE ? "1" : "0");
    }

    public boolean getNimbusOnOSX() {
        Element el = root.getChild(SETTING_NIMBUS_ON_OSX);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setNimbusOnOSX(boolean val) {
        Element el = root.getChild(SETTING_NIMBUS_ON_OSX);
        if (null == el) {
            el = new Element(SETTING_NIMBUS_ON_OSX);
            root.addContent(el);
        }
        el.setText(val == Boolean.TRUE ? "1" : "0");
    }

    public boolean getUseScrollTabs() {
        Element el = root.getChild(SETTING_USE_SCROLL_TABS);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setUseScrollTabs(boolean val) {
        Element el = root.getChild(SETTING_USE_SCROLL_TABS);
        if (null == el) {
            el = new Element(SETTING_USE_SCROLL_TABS);
            root.addContent(el);
        }
        el.setText(val == Boolean.TRUE ? "1" : "0");
    }

    public boolean getReopenOnStartup() {
        return genericBooleanGetter(SETTING_REOPEN_FILES_ON_STARTUP, true);
    }

    public void setReopenOnStartup(boolean val) {
        genericBooleanSetter(SETTING_REOPEN_FILES_ON_STARTUP, val);
    }

    public int getTabWidth() {
        return genericIntGetter(SETTING_TABWIDTH, 4);
    }

    public void setTabWidth(int tabwidth) {
        genericIntSetter(SETTING_TABWIDTH, tabwidth);
    }

    public int getListGotoIndex() {
        return genericIntGetter(SETTING_LISTGOTOINDEX, -1);
    }

    public void setListGotoIndex(int tabwidth) {
        genericIntSetter(SETTING_LISTGOTOINDEX, tabwidth);
    }

    public String getExternalEditorPath() {
        return genericStringGetter(SETTING_EXTERNALEDITOR, "");
    }
    
    public void setExternalEditorPath(String path) {
        genericStringSetter(SETTING_EXTERNALEDITOR, path);
    }
    
    public int getDividerLocation() {
        return genericIntGetter(SETTING_DIVIDER_LOCATION, 4);
    }

    public void setDividerLocation(int pos) {
        genericIntSetter(SETTING_DIVIDER_LOCATION, pos);
    }

    public int getLastUserScript() {
        return genericIntGetter(SETTING_LAST_SCRIPT, 0);
    }

    public void setLastUserScript(int index) {
        genericIntSetter(SETTING_LAST_SCRIPT, index);
    }

    /**
     * Retrieves settings for the mainfont (the font used for the main-entry-textfield).
     *
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
     * @return the related font-information as string.
     */
    public String getMainFont(int what) {
        Element el = root.getChild(SETTING_MAINFONT);
        String retval = "";
        if (el != null) {
            switch (what) {
                case FONTNAME:
                    retval = el.getText();
                    break;
                case FONTSIZE:
                    retval = el.getAttributeValue("size");
                    break;
            }
        }
        return retval;
    }

    /**
     * Retrieves the main font as font-object.
     *
     * @return the main-font as {@code Font} variable.
     */
    public Font getMainFont() {
        Element el = root.getChild(SETTING_MAINFONT);
        int fsize = Integer.parseInt(el.getAttributeValue("size"));
        return new Font(el.getText(), Font.PLAIN, fsize);
    }

    public void setMainFont(Font f) {
        Element el = root.getChild(SETTING_MAINFONT);
        if (null == el) {
            el = new Element(SETTING_MAINFONT);
            root.addContent(el);
        }
        el.setText(f.getName());
        el.setAttribute("size", String.valueOf(f.getSize()));
    }

    public int getLogSplitLayout() {
        return genericIntGetter(SETTING_LOGSPLITLAYOUT, JSplitPane.HORIZONTAL_SPLIT);
    }

    public void setLogSplitLayout(int val) {
        genericIntSetter(SETTING_LOGSPLITLAYOUT, val);
    }

    public int getLineNumerAlignment() {
        return genericIntGetter(SETTING_LINE_NUMBER_ALIGNMENT, Gutter.RIGHT);
    }

    public void setLineNumerAlignment(int align) {
        genericIntSetter(SETTING_LINE_NUMBER_ALIGNMENT, align);
    }

    public ArrayList<Object[]> getReopenFiles() {
        // get reopen files
        Element el = root.getChild(SETTING_REOPEN_FILES);
        // check if we have any
        if (null == el) {
            return null;
        }
        // create return value
        ArrayList<Object[]> rofiles = new ArrayList<>();
        // retrieve all children, each element representing one
        // file that should be re-opened
        List<Element> children = el.getChildren();
        // iterate all children
        for (Element e : children) {
            // get file path
            File f = new File(e.getText());
            // check if exists
            if (f.exists()) {
                // get compiler value
                String attr_c = e.getAttributeValue(ATTR_ASM);
                String attr_s = e.getAttributeValue(ATTR_SCRIPT);
                String attr_as = e.getAttributeValue(ATTR_ALT_SCRIPT);
                // init defaults
                Assembler assembler = Assemblers.ASM_KICKASSEMBLER;
                int script = -1;
                int altScript = -1;
                // check if we have compiler value
                try {
                    if (attr_c != null) {
                        assembler = Assemblers.byID(Integer.parseInt(attr_c));
                    }
                    if (attr_s != null) {
                        script = Integer.parseInt(attr_s);
                    }
                    if (attr_as != null) {
                        altScript = Integer.parseInt(attr_as);
                    }
                } catch (NumberFormatException ex) {
                    assembler = Assemblers.ASM_KICKASSEMBLER;
                    script = -1;
                    altScript = -1;
                }
                // add compiler and filepath to return value
                rofiles.add(new Object[]{f, assembler, script, altScript});
            }
        }
        return rofiles;
    }

    public void setReopenFiles(EditorPanes ep) {
        Element el = root.getChild(SETTING_REOPEN_FILES);
        if (null == el) {
            el = new Element(SETTING_REOPEN_FILES);
            root.addContent(el);
        }
        // remove existing content
        el.removeContent();
        // iterate all editorpanes and store file pathes
        for (int i = 0; i < ep.getCount(); i++) {
            // get file path and compiler settings of each file
            File fp = ep.getFilePath(i);
            int c = ep.getAssembler(i).getID();
            int s = ep.getScript(i);
            int as = ep.getAlternativeScript(i);
            // save if exists
            if (fp != null && fp.exists()) {
                // create new child element
                Element child = new Element(SETTING_REOPEN_FILES_CHILD);
                // add path and compiler
                child.setText(fp.getAbsolutePath());
                child.setAttribute(ATTR_ASM, String.valueOf(c));
                child.setAttribute(ATTR_SCRIPT, String.valueOf(s));
                child.setAttribute(ATTR_ALT_SCRIPT, String.valueOf(as));
                // add to database
                el.addContent(child);
            }
        }
    }

    /**
     * Returns the setting (saved value) for boolean values. Returns
     * the argument {@code defaultValue} if element does not exist
     * in the settings file.
     * 
     * @param key the key of the specific settings
     * @param defaultValue a default value that will be returned in case the 
     * setting {@code key} does not exist.
     * @return the saved setting for {@code key} as integer value.
     */
    private boolean genericBooleanGetter(String key, boolean defaultValue) {
        Element el = settingsFile.getRootElement().getChild(key);
        if (el != null) {
            return el.getText().equals("1");
        }
        return defaultValue;
    }
    /**
     * Sets application preferences value for settings saved as boolean values.
     * 
     * @param key the key of the specific settings
     * @param val the values to be set
     */
    private void genericBooleanSetter(String key, boolean val) {
        Element el = settingsFile.getRootElement().getChild(key);
        if (null == el) {
            el = new Element(key);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }
    
    /**
     * Returns the setting (saved value) for integer values. Returns
     * the argument {@code defaultValue} if element does not exist
     * in the settings file.
     * 
     * @param key the key of the specific settings
     * @param defaultValue a default value that will be returned in case the 
     * setting {@code key} does not exist.
     * @return the saved setting for {@code key} as integer value.
     */
    private int genericIntGetter(String key, int defaultValue) {
        Element el = settingsFile.getRootElement().getChild(key);
        if (el != null) {
            try {
                return Integer.parseInt(el.getText());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    /**
     * Sets application preferences value for settings saved as integer values.
     * 
     * @param key the key of the specific settings
     * @param val the values to be set
     */
    private void genericIntSetter(String key, int val) {
        Element el = settingsFile.getRootElement().getChild(key);
        if (null == el) {
            el = new Element(key);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(val));
    }
    /**
     * Returns the setting (saved value) for string values. Returns
     * the argument {@code defaultValue} if element does not exist
     * in the settings file.
     * 
     * @param key the key of the specific settings
     * @param defaultValue a default value that will be returned in case the 
     * setting {@code key} does not exist.
     * @return the saved setting for {@code key} as integer value.
     */
    private String genericStringGetter(String key, String defaultValue) {
        Element el = settingsFile.getRootElement().getChild(key);
        String retval = defaultValue;
        if (el != null) {
            retval = el.getText();
        }
        return retval;
    }
    /**
     * Sets application preferences value for settings saved as string values.
     * 
     * @param key the key of the specific settings
     * @param val the values to be set
     */
    private void genericStringSetter(String key, String val) {
        Element el = settingsFile.getRootElement().getChild(key);
        if (null == el) {
            el = new Element(key);
            settingsFile.getRootElement().addContent(el);
        }
        if (val != null && !val.isEmpty()) {
            el.setText(val);
        } else {
            el.setText("");
        }
    }
    
}
