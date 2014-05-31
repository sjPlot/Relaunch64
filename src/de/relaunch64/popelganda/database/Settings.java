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
package de.relaunch64.popelganda.database;

import de.relaunch64.popelganda.Editor.ColorSchemes;
import de.relaunch64.popelganda.Editor.EditorPanes;
import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.FileTools;
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
    private static final String SETTING_PREF_COMP = "preferredCompiler";
    private static final String SETTING_SYNTAX_SCHEME = "syntaxscheme";
    private static final String SETTING_LAST_SCRIPT = "lastUserScript";
    private static final String SETTING_ALT_ASM_MODE = "alternativeAssemblyMode";
    private static final String SETTING_ANTIALIAS = "antialias";
    private static final String SETTING_SCALE_FONT = "scalefont";
    private static final String REC_DOC_COMPILER = "compiler";
    private static final String REC_DOC_SCRIPT = "script";
    private static final String SETTING_MAINFONT = "editorfont";
    private static final String SETTING_CHECKUPDATES = "checkupdates";
    private static final String SETTING_SAVEONCOMPILE = "saveoncompile";
    private static final String SETTING_NIMBUS_ON_OSX = "nimbusonosx";
    private static final String SETTING_LOGSPLITLAYOUT = "logsplitlayout";
    private static final String SETTING_BOTHLOGRUNSPLITLAYOUT = "bothlogrubsplitlayout";
    private static final String SETTING_TABWIDTH = "tabwidth";
    private static final String SETTING_LINE_NUMBER_ALIGNMENT = "linenumberalignment";
    private static final String SETTING_REOPEN_FILES_ON_STARTUP = "rofstartup";
    private static final String SETTING_REOPEN_FILES = "reopenfiles";
    private static final String SETTING_REOPEN_FILES_CHILD = "rof";

    private static final String ATTR_COMPILER = "compiler";
    private static final String ATTR_SCRIPT = "script";

    private final File filepath;
    public static final int FONTNAME = 1;
    public static final int FONTSIZE = 2;

    public static final int SPLITPANE_LOG = 1;
    public static final int SPLITPANE_RUN = 2;
    public static final int SPLITPANE_BOTHLOGRUN = 3;
    
    /**
     * XML-Document that stores the settings-information
     */
    private Document settingsFile;    

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
        if (filepath!=null && filepath.exists()) {
            try {
                SAXBuilder builder = new SAXBuilder();
                settingsFile = builder.build(filepath);
            }
            catch (JDOMException | IOException ex) {
                ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
            }
        }
        fillElements();
    }
    /**
     * Loads the settings file
     */
    public void saveSettings() {
        // if file exists, go on...
        if (filepath!=null) {
            OutputStream dest = null;
            try {
                XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
                dest = new FileOutputStream(filepath);
                out.output(settingsFile, dest);
            }
            catch (IOException ex) {
                ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
            }
            finally {
                if (dest!=null) {
                    try {
                        dest.close();
                    }
                    catch (IOException ex) {
                        ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
                    }
                }
            }
        }
    }
    /**
     * This method creates all the settings-child-elements, but only, if they don't
     * already exist. We do this because when loading older settings-xml-document-structures,
     * we might have new elements that would not be initialised. but now we can call this 
     * method after loading the xml-document, and create elements and default values for all
     * new elements. This ensures compatibility to older/news settings-file-versions.
     */
    private void fillElements() {
        // init standard font. on mac, it's helvetica
        String font = "Helvetica";
        // on older windows arial
        if (ConstantsR64.IS_WINDOWS) {
            font = "Arial";
            // on new windows Calibri
            if (ConstantsR64.IS_WINDOWS7 || ConstantsR64.IS_WINDOWS8) {
                font="Calibri";
            }
        } 
        // and on linux we take Nimbus Sans L Regular
        else if (ConstantsR64.IS_LINUX) {
            font = "Nimbus Sans L Regular";
        }
        for (int cnt=0; cnt<recentDocCount; cnt++) {
            // create field-identifier
            String fi = SETTING_RECENT_DOC+String.valueOf(cnt+1);
            // retrieve content
            if (null==settingsFile.getRootElement().getChild(fi)) {
                // create a filepath-element
                Element el = new Element(fi);
                el.setText("");
                // and add it to the document
                settingsFile.getRootElement().addContent(el);
            }
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_REOPEN_FILES)) {
            // create element for font
            Element el = new Element(SETTING_REOPEN_FILES);
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_MAINFONT)) {
            // create element for font
            Element el = new Element(SETTING_MAINFONT);
            settingsFile.getRootElement().addContent(el);
            el.setText(font);
            el.setAttribute("size", "11");
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_LAST_USED_PATH)) {
            // create element
            Element el = new Element(SETTING_LAST_USED_PATH);
            el.setText("");
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_ANTIALIAS)) {
            // create element
            Element el = new Element(SETTING_ANTIALIAS);
            el.setText(AntiAlias.SUBPIXEL);
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_SCALE_FONT)) {
            // create element
            Element el = new Element(SETTING_SCALE_FONT);
            el.setText("1");
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_CHECKUPDATES)) {
            // create element
            Element el = new Element(SETTING_CHECKUPDATES);
            el.setText("1");
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_ALT_ASM_MODE)) {
            // create element
            Element el = new Element(SETTING_ALT_ASM_MODE);
            el.setText("0");
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_REOPEN_FILES_ON_STARTUP)) {
            // create element
            Element el = new Element(SETTING_REOPEN_FILES_ON_STARTUP);
            el.setText("1");
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_SAVEONCOMPILE)) {
            // create element
            Element el = new Element(SETTING_SAVEONCOMPILE);
            el.setText("1");
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_NIMBUS_ON_OSX)) {
            // create element
            Element el = new Element(SETTING_NIMBUS_ON_OSX);
            el.setText("0");
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_PREF_COMP)) {
            // create element
            Element el = new Element(SETTING_PREF_COMP);
            el.setText(String.valueOf(ConstantsR64.ASM_KICKASSEMBLER));
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_SYNTAX_SCHEME)) {
            // create element
            Element el = new Element(SETTING_SYNTAX_SCHEME);
            el.setText(String.valueOf(ColorSchemes.SCHEME_DEFAULT));
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_LAST_SCRIPT)) {
            // create element
            Element el = new Element(SETTING_LAST_SCRIPT);
            el.setText("0");
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_LOGSPLITLAYOUT)) {
            // create a filepath-element
            Element el = new Element(SETTING_LOGSPLITLAYOUT);
            el.setText(String.valueOf(JSplitPane.HORIZONTAL_SPLIT));
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_BOTHLOGRUNSPLITLAYOUT)) {
            // create a filepath-element
            Element el = new Element(SETTING_BOTHLOGRUNSPLITLAYOUT);
            el.setText(String.valueOf(JSplitPane.VERTICAL_SPLIT));
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_TABWIDTH)) {
            // create a filepath-element
            Element el = new Element(SETTING_TABWIDTH);
            el.setText("4");
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_LINE_NUMBER_ALIGNMENT)) {
            // create a filepath-element
            Element el = new Element(SETTING_LINE_NUMBER_ALIGNMENT);
            el.setText(String.valueOf(Gutter.RIGHT));
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
    }
    /**
     * Retrieves the recent document at the position {@code nr}. Returns {@code null} if recent document
     * does not exist or is empty
     * @param nr the number of the requested recent document. use a value from 1 to {@link #recentDocCount recentDocCount}.
     * @return the recent document (the file path) as string, or {@code null} if no such element or path exists.
     */
    public File getRecentDoc(int nr) {
        // checl for valid parameter
        if (nr<0) return null;
        // retrieve element
        Element el = settingsFile.getRootElement().getChild(SETTING_RECENT_DOC+String.valueOf(nr));
        // if we have any valid document
        if (el!=null) {
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
        if (null==f) return -1;
        for (int i=0; i<recentDocCount; i++) {
            File rf = getRecentDoc(i);
            if (rf!=null && f.equals(rf)) return i;
        }
        return -1;
    }
    /**
     * 
     * @param nr
     * @return 
     */
    public int getRecentDocCompiler(int nr) {
        // checl for valid parameter
        if (nr<0) return 0;
        // retrieve element
        Element el = settingsFile.getRootElement().getChild(SETTING_RECENT_DOC+String.valueOf(nr));
        // if we have any valid document
        if (el!=null) {
            // retrieve compiler attribute
            Attribute comp = el.getAttribute(REC_DOC_COMPILER);
            // if we have any valid attribute
            if (comp!=null) {
                try {
                    return Integer.parseInt(comp.getValue());
                }
                catch (NumberFormatException ex) {
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
    public int getRecentDocScript(int nr) {
        // checl for valid parameter
        if (nr<0) return 0;
        // retrieve element
        Element el = settingsFile.getRootElement().getChild(SETTING_RECENT_DOC+String.valueOf(nr));
        // if we have any valid document
        if (el!=null) {
            // retrieve compiler attribute
            Attribute comp = el.getAttribute(REC_DOC_SCRIPT);
            // if we have any valid attribute
            if (comp!=null) {
                try {
                    return Integer.parseInt(comp.getValue());
                }
                catch (NumberFormatException ex) {
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
        if (null==doc || doc.isEmpty()) return -1;
        // iterate all current recent documents
        for (int cnt=1; cnt<=recentDocCount; cnt++) {
            if (getRecentDoc(cnt).getPath().equals(doc)) return cnt;
        }
        return -1;
    }
    /**
     * Retrieves the recent document at the position {@code nr}. Returns {@code null} if recent document
     * does not exist or is empty
     * @param nr the number of the requested recent document. use a value from 1 to {@link #recentDocCount recentDocCount}.
     * @return the recent document (the file path) as string, or {@code null} if no such element or path exists.
     */
    private String getRecentDocAsString(int nr) {
        // retrieve element
        File rd = getRecentDoc(nr);
        // check for valid value
        if (rd!=null) {
            return rd.toString();
        }
        // else return null
        return null;
    }
    /**
     * This method adds the file from the filepath {@code fp} to the list of recent
     * documents and rotates that list, if necessary.
     * @param fp the filepath to the document that should be added to the list of recent documents
     * @param compiler
     * @param userScript
     */
    public void addToRecentDocs(String fp, int compiler, int userScript) {
        // check for valid parameter
        if (null==fp || fp.isEmpty()) {
            return;
        }
        // check whether file exists
        File dummy = new File(fp);
        if (!dummy.exists()) {
            return;
        }
        // create linked list
        LinkedList<String> recdocs = new LinkedList<>();
        // linked list for compilers
        LinkedList<Integer> reccomps = new LinkedList<>();
        // linked list for scripts
        LinkedList<Integer> recscripts = new LinkedList<>();
        // add new filepath to linked list
        recdocs.add(fp);
        reccomps.add(compiler);
        recscripts.add(userScript);
        // iterate all current recent documents
        for (int cnt=1; cnt<=recentDocCount; cnt++) {
            // retrieve recent document
            String recentDoc = getRecentDocAsString(cnt);
            int comp = getRecentDocCompiler(cnt);
            int script = getRecentDocScript(cnt);
            // check whether the linked list already contains such a document
            if (recentDoc!=null && !recentDoc.isEmpty()) {
                // check for existing file
                dummy = new File(recentDoc);
                // if not, add it to the list
                if (dummy.exists() && !recdocs.contains(recentDoc)) {
                    reccomps.add(comp);
                    recscripts.add(script);
                    recdocs.add(recentDoc);
                }
            }
        }
        // iterate all current recent documents again
        for (int cnt=1; cnt<=recentDocCount; cnt++) {
            // check for valid bounds of linked list
            if (recdocs.size()>=cnt) {
                // and set recent document
                setRecentDoc(cnt, recdocs.get(cnt-1), reccomps.get(cnt-1), recscripts.get(cnt-1));
            }
            // else fill remaining recent documents with empty strings
            else {
                setRecentDoc(cnt, "", -1, -1);
            }
        }
    }
    /**
     * Add a new recent document to the position {@code nr} in the list of recent documents.
     * @param nr the number of the requested recent document. use a value from 1 to {@link #recentDocCount recentDocCount}.
     * @param fp the filepath to the recently used document as string
     * @param compiler
     * @param userScript
     */
    public void setRecentDoc(int nr, String fp, int compiler, int userScript) {
        // check for valid parameter
        if (null==fp || -1==nr) {
            return;
        }
        // retrieve element
        Element el = settingsFile.getRootElement().getChild(SETTING_RECENT_DOC+String.valueOf(nr));
        // if element does not exist, create new...
        if (null==el) {
            el = new Element(SETTING_RECENT_DOC+String.valueOf(nr));
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        // add filepath
        el.setText(fp);
        el.setAttribute(REC_DOC_COMPILER, String.valueOf(compiler));
        el.setAttribute(REC_DOC_SCRIPT, String.valueOf(userScript));
    }
    public File getLastUsedPath() {
        Element el = settingsFile.getRootElement().getChild(SETTING_LAST_USED_PATH);
        File value = null;
        if (el!=null) {
            value = new File(el.getText());
        }
        return value;
    }
    public void setLastUsedPath(File f) {
        Element el = settingsFile.getRootElement().getChild(SETTING_LAST_USED_PATH);
        if (null==el) {
            el = new Element(SETTING_LAST_USED_PATH);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(f.getAbsolutePath());
    }
    public String getAntiAlias() {
        Element el = settingsFile.getRootElement().getChild(SETTING_ANTIALIAS);
        if (el!=null) return el.getText();
        return AntiAlias.SUBPIXEL;
    }
    public void setAntiAlias(String aa) {
        Element el = settingsFile.getRootElement().getChild(SETTING_ANTIALIAS);
        if (null==el) {
            el = new Element(SETTING_ANTIALIAS);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(aa);
    }
    public boolean getScaleFont() {
        Element el = settingsFile.getRootElement().getChild(SETTING_SCALE_FONT);
        if (el!=null) return el.getText().equals("1");
        return true;
    }
    public void setScaleFont(boolean scale) {
        Element el = settingsFile.getRootElement().getChild(SETTING_SCALE_FONT);
        if (null==el) {
            el = new Element(SETTING_SCALE_FONT);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(scale==Boolean.TRUE ? "1":"0");
    }
    public int getPreferredCompiler() {
        Element el = settingsFile.getRootElement().getChild(SETTING_PREF_COMP);
        try {
            if (el!=null) return Integer.parseInt(el.getText());
        }
        catch (NumberFormatException ex) {
        }
        return ConstantsR64.ASM_KICKASSEMBLER;
    }
    public void setPreferredCompiler(int compiler) {
        Element el = settingsFile.getRootElement().getChild(SETTING_PREF_COMP);
        if (null==el) {
            el = new Element(SETTING_PREF_COMP);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(compiler));
    }
    public int getColorScheme() {
        Element el = settingsFile.getRootElement().getChild(SETTING_SYNTAX_SCHEME);
        try {
            if (el!=null) return Integer.parseInt(el.getText());
        }
        catch (NumberFormatException ex) {
        }
        return ColorSchemes.SCHEME_DEFAULT;
    }
    public void setColorScheme(int scheme) {
        Element el = settingsFile.getRootElement().getChild(SETTING_SYNTAX_SCHEME);
        if (null==el) {
            el = new Element(SETTING_SYNTAX_SCHEME);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(scheme));
    }
    public boolean getCheckForUpdates() {
        Element el = settingsFile.getRootElement().getChild(SETTING_CHECKUPDATES);
        if (el!=null) return el.getText().equals("1");
        return true;
    }
    public void setCheckForUpdates(boolean val)  {
        Element el = settingsFile.getRootElement().getChild(SETTING_CHECKUPDATES);
        if (null==el) {
            el = new Element(SETTING_CHECKUPDATES);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(val==Boolean.TRUE ? "1":"0");
    }
    public boolean getAlternativeAssemblyMode() {
        Element el = settingsFile.getRootElement().getChild(SETTING_ALT_ASM_MODE);
        if (el!=null) return el.getText().equals("1");
        return false;
    }
    public void setAlternativeAssemblyMode(boolean val)  {
        Element el = settingsFile.getRootElement().getChild(SETTING_ALT_ASM_MODE);
        if (null==el) {
            el = new Element(SETTING_ALT_ASM_MODE);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(val==Boolean.TRUE ? "1":"0");
    }
    public boolean getSaveOnCompile() {
        Element el = settingsFile.getRootElement().getChild(SETTING_SAVEONCOMPILE);
        if (el!=null) return el.getText().equals("1");
        return true;
    }
    public void setSaveOnCompile(boolean val)  {
        Element el = settingsFile.getRootElement().getChild(SETTING_SAVEONCOMPILE);
        if (null==el) {
            el = new Element(SETTING_SAVEONCOMPILE);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(val==Boolean.TRUE ? "1":"0");
    }
    public boolean getNimbusOnOSX() {
        Element el = settingsFile.getRootElement().getChild(SETTING_NIMBUS_ON_OSX);
        if (el!=null) return el.getText().equals("1");
        return false;
    }
    public void setNimbusOnOSX(boolean val)  {
        Element el = settingsFile.getRootElement().getChild(SETTING_NIMBUS_ON_OSX);
        if (null==el) {
            el = new Element(SETTING_NIMBUS_ON_OSX);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(val==Boolean.TRUE ? "1":"0");
    }
    public boolean getReopenOnStartup() {
        Element el = settingsFile.getRootElement().getChild(SETTING_REOPEN_FILES_ON_STARTUP);
        if (el!=null) return el.getText().equals("1");
        return true;
    }
    public void setReopenOnStartup(boolean val)  {
        Element el = settingsFile.getRootElement().getChild(SETTING_REOPEN_FILES_ON_STARTUP);
        if (null==el) {
            el = new Element(SETTING_REOPEN_FILES_ON_STARTUP);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(val==Boolean.TRUE ? "1":"0");
    }
    public int getTabWidth() {
        Element el = settingsFile.getRootElement().getChild(SETTING_TABWIDTH);
        if (el!=null) {
            try {
                return Integer.parseInt(el.getText());
            }
            catch (NumberFormatException ex) {
                return 4;
            }
        }
        return 4;
    }
    public void setTabWidth(int tabwidth) {
        Element el = settingsFile.getRootElement().getChild(SETTING_TABWIDTH);
        if (null==el) {
            el = new Element(SETTING_TABWIDTH);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(tabwidth));
    }
    public int getLastUserScript() {
        Element el = settingsFile.getRootElement().getChild(SETTING_LAST_SCRIPT);
        try {
            if (el!=null) return Integer.parseInt(el.getText());
        }
        catch (NumberFormatException ex) {
        }
        return 0;
    }
    public void setLastUserScript(int index) {
        Element el = settingsFile.getRootElement().getChild(SETTING_LAST_SCRIPT);
        if (null==el) {
            el = new Element(SETTING_LAST_SCRIPT);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(index));
    }
    /**
     * Retrieves settings for the mainfont (the font used for the main-entry-textfield).
     * @param what (indicates, which font-characteristic we want to have. use following constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
     * @return the related font-information as string.
     */
    public String getMainfont(int what) {
        Element el = settingsFile.getRootElement().getChild(SETTING_MAINFONT);
        String retval = "";
        if (el!=null) {
            switch (what) {
                case FONTNAME: retval = el.getText(); break;
                case FONTSIZE: retval = el.getAttributeValue("size"); break;
            }
        }
        return retval;
    }
    /**
     * Retrieves the main font as font-object.
     * @return the main-font as {@code Font} variable.
     */
    public Font getMainFont() {
        Element el = settingsFile.getRootElement().getChild(SETTING_MAINFONT);
        int fsize = Integer.parseInt(el.getAttributeValue("size"));
        return new Font(el.getText(), Font.PLAIN, fsize);
    }
    public void setMainfont(Font f) {
        Element el = settingsFile.getRootElement().getChild(SETTING_MAINFONT);
        if (null==el) {
            el = new Element(SETTING_MAINFONT);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(f.getName());
        el.setAttribute("size", String.valueOf(f.getSize()));
    }
    public int getSearchFrameSplitLayout(int splitpane) {
        String splitname = SETTING_LOGSPLITLAYOUT;
        switch (splitpane) {
            case SPLITPANE_LOG:
                splitname = SETTING_LOGSPLITLAYOUT;
                break;
            case SPLITPANE_BOTHLOGRUN:
                splitname = SETTING_BOTHLOGRUNSPLITLAYOUT;
                break;
        }
        // get attribute which stores last used desktop number
        Element el = settingsFile.getRootElement().getChild(splitname);
        // check for valid value
        if (el!=null) {
            try {
                // retrieve value
                return Integer.parseInt(el.getText());
            }
            catch (NumberFormatException e) {
                return JSplitPane.HORIZONTAL_SPLIT;
            }
        }
        return JSplitPane.HORIZONTAL_SPLIT;
    }
    public void setSearchFrameSplitLayout(int val, int splitpane) {
        String splitname = SETTING_LOGSPLITLAYOUT;
        switch (splitpane) {
            case SPLITPANE_LOG:
                splitname = SETTING_LOGSPLITLAYOUT;
                break;
            case SPLITPANE_BOTHLOGRUN:
                splitname = SETTING_BOTHLOGRUNSPLITLAYOUT;
        }
        Element el = settingsFile.getRootElement().getChild(splitname);
        if (null==el) {
            el = new Element(splitname);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(val));
    }
    public int getLineNumerAlignment() {
        Element el = settingsFile.getRootElement().getChild(SETTING_LINE_NUMBER_ALIGNMENT);
        if (el!=null) {
            try {
                return Integer.parseInt(el.getText());
            }
            catch (NumberFormatException ex) {
                return Gutter.RIGHT;
            }
        }
        return Gutter.RIGHT;
    }
    public void setLineNumerAlignment(int align) {
        Element el = settingsFile.getRootElement().getChild(SETTING_LINE_NUMBER_ALIGNMENT);
        if (null==el) {
            el = new Element(SETTING_LINE_NUMBER_ALIGNMENT);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(align));
    }
    public ArrayList<Object[]> getReopenFiles() {
        // get reopen files
        Element el = settingsFile.getRootElement().getChild(SETTING_REOPEN_FILES);
        // check if we have any
        if (null==el) return null;
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
                String attr_c = e.getAttributeValue(ATTR_COMPILER);
                String attr_s = e.getAttributeValue(ATTR_SCRIPT);
                // init defaults
                int compiler = ConstantsR64.ASM_KICKASSEMBLER;
                int script = -1;
                // check if we have compiler value
                try {
                    if (attr_c!=null) compiler = Integer.parseInt(attr_c);
                    if (attr_s!=null) script = Integer.parseInt(attr_s);
                }
                catch (NumberFormatException ex) {
                    compiler = ConstantsR64.ASM_KICKASSEMBLER;
                    script = -1;
                }
                // add compiler and filepath to return value
                rofiles.add(new Object[]{f,compiler,script});
            }
        }
        return rofiles;
    }
    public void setReopenFiles(EditorPanes ep) {
        Element el = settingsFile.getRootElement().getChild(SETTING_REOPEN_FILES);
        if (null==el) {
            el = new Element(SETTING_REOPEN_FILES);
            settingsFile.getRootElement().addContent(el);
        }
        // remove existing content
        el.removeContent();
        // iterate all editorpanes and store file pathes
        for (int i=0; i<ep.getCount(); i++) {
            // get file path and compiler settings of each file
            File fp = ep.getFilePath(i);
            int c = ep.getCompiler(i);
            int s = ep.getScript(i);
            // save if exists
            if (fp!=null && fp.exists()) {
                // create new child element
                Element child = new Element(SETTING_REOPEN_FILES_CHILD);
                // add path and compiler
                child.setText(fp.getAbsolutePath());
                child.setAttribute(ATTR_COMPILER, String.valueOf(c));
                child.setAttribute(ATTR_SCRIPT, String.valueOf(s));
                // add to database
                el.addContent(child);
            }
        }
    }
}
