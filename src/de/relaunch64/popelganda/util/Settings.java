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
package de.relaunch64.popelganda.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.logging.Level;
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
    private static final int recentDocCount = 8;
    
    private static final String SETTING_RECENT_DOC = "recentDoc";
    private static final String SETTING_PATH_KICKASSEMBLER = "pathKickAssembler";
    private static final String SETTING_PATH_ACME = "pathAcme";
    private static final String SETTING_PATH_EMU = "pathEmulator";
    private static final String SETTING_PARAM_KICKASSEMBLER = "paramKickAssembler";
    private static final String SETTING_PARAM_ACME = "paramAcme";
    
    private final File filepath;
    private final boolean IS_WINDOWS;
    /**
     * Indicates whether the OS is a windows OS
     * @return 
     */
    public boolean isWindows() {
        return IS_WINDOWS;
    }
    /**
     * XML-Document that stores the settings-information
     */
    private Document settingsFile;    

    public Settings() {
        // first of all, create the empty documents
        settingsFile = new Document(new Element("settings"));
        // check os
        IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");
        // create file path to settings file
        filepath = createFilePath("relaunch64-settings.xml");
        // now fill the initoal elements
        fillElements();
    }
    /**
     * 
     * @param filename
     * @return 
     */
    private File createFilePath(String filename) {
        // first of all, we want to check for a subdirectory ".Zettelkasten" in the user's home-directory
        File sFile = new File(System.getProperty("user.home")+java.io.File.separatorChar+filename);
        // return result
        return sFile;
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
        else {
            fillElements();
        }
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
        if (null==settingsFile.getRootElement().getChild(SETTING_PATH_KICKASSEMBLER)) {
            // create element
            Element el = new Element(SETTING_PATH_KICKASSEMBLER);
            el.setText("");
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_PATH_ACME)) {
            // create element
            Element el = new Element(SETTING_PATH_ACME);
            el.setText("");
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_PATH_EMU)) {
            // create element
            Element el = new Element(SETTING_PATH_EMU);
            el.setText("");
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_PARAM_KICKASSEMBLER)) {
            // create element
            Element el = new Element(SETTING_PARAM_KICKASSEMBLER);
            el.setText("");
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
        if (null==settingsFile.getRootElement().getChild(SETTING_PARAM_ACME)) {
            // create element
            Element el = new Element(SETTING_PARAM_ACME);
            el.setText("");
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
     */
    public void addToRecentDocs(String fp) {
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
        // add new filepath to linked list
        recdocs.add(fp);
        // iterate all current recent documents
        for (int cnt=1; cnt<=recentDocCount; cnt++) {
            // retrieve recent document
            String recentDoc = getRecentDocAsString(cnt);
            // check whether the linked list already contains such a document
            if (recentDoc!=null && !recentDoc.isEmpty()) {
                // check for existing file
                dummy = new File(recentDoc);
                // if not, add it to the list
                if (dummy.exists() && !recdocs.contains(recentDoc)) {
                    recdocs.add(recentDoc);
                }
            }
        }
        // iterate all current recent documents again
        for (int cnt=1; cnt<=recentDocCount; cnt++) {
            // check for valid bounds of linked list
            if (recdocs.size()>=cnt) {
                // and set recent document
                setRecentDoc(cnt, recdocs.get(cnt-1));
            }
            // else fill remaining recent documents with empty strings
            else {
                setRecentDoc(cnt, "");
            }
        }
    }
    /**
     * Add a new recent document to the position {@code nr} in the list of recent documents.
     * @param nr the number of the requested recent document. use a value from 1 to {@link #recentDocCount recentDocCount}.
     * @param fp the filepath to the recently used document as string
     */
    public void setRecentDoc(int nr, String fp) {
        // check for valid parameter
        if (null==fp ) {
            return;
        }
        // retrieve element
        Element el = settingsFile.getRootElement().getChild(SETTING_RECENT_DOC+String.valueOf(nr));
        // if element exists...
        if (el!=null) {
            // add filepath
            el.setText(fp);
        }
        else {
            // create a filepath-element
            el = new Element(SETTING_RECENT_DOC+String.valueOf(nr));
            // add filepath
            el.setText(fp);
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
    }
    /**
     * 
     * @param compiler
     * @return 
     */
    public String getCompilerParameter(int compiler) {
        Element el;
        switch (compiler) {
            case ConstantsR64.COMPILER_ACME:
                el = settingsFile.getRootElement().getChild(SETTING_PARAM_ACME);
                break;
            case ConstantsR64.COMPILER_KICKASSEMBLER:
                el = settingsFile.getRootElement().getChild(SETTING_PARAM_KICKASSEMBLER);
                break;
            default:
                el = settingsFile.getRootElement().getChild(SETTING_PARAM_KICKASSEMBLER);
                break;
        }
        String value = "";
        if (el!=null) {
            value = el.getText();
        }
        return value;
    }
    /**
     * 
     * @param compiler
     * @param param 
     */
    public void setCompilerParameter(int compiler, String param) {
        if (param!=null) {
            Element el = null;
            switch (compiler) {
                case ConstantsR64.COMPILER_ACME:
                    el = settingsFile.getRootElement().getChild(SETTING_PARAM_ACME);
                    if (null==el) {
                        el = new Element(SETTING_PARAM_ACME);
                        settingsFile.getRootElement().addContent(el);
                    }
                    break;
                case ConstantsR64.COMPILER_KICKASSEMBLER:
                    el = settingsFile.getRootElement().getChild(SETTING_PARAM_KICKASSEMBLER);
                    if (null==el) {
                        el = new Element(SETTING_PARAM_KICKASSEMBLER);
                        settingsFile.getRootElement().addContent(el);
                    }
                    break;
            }
            if (el!=null) {
                el.setText(param);
            }
        }
    }
    /**
     * 
     * @param compiler
     * @return 
     */
    public File getCompilerPath(int compiler) {
        Element el;
        switch (compiler) {
            case ConstantsR64.COMPILER_ACME:
                el = settingsFile.getRootElement().getChild(SETTING_PATH_ACME);
                break;
            case ConstantsR64.COMPILER_KICKASSEMBLER:
                el = settingsFile.getRootElement().getChild(SETTING_PATH_KICKASSEMBLER);
                break;
            default:
                el = settingsFile.getRootElement().getChild(SETTING_PATH_KICKASSEMBLER);
                break;
        }
        File value = null;
        if (el!=null) {
            value = new File(el.getText());
        }
        return value;
    }
    /**
     * 
     * @param compiler
     * @param path 
     */
    public void setCompilerPath(int compiler, File path) {
        if (path!=null) {
            Element el = null;
            switch (compiler) {
                case ConstantsR64.COMPILER_ACME:
                    el = settingsFile.getRootElement().getChild(SETTING_PATH_ACME);
                    if (null==el) {
                        el = new Element(SETTING_PATH_ACME);
                        settingsFile.getRootElement().addContent(el);
                    }
                    break;
                case ConstantsR64.COMPILER_KICKASSEMBLER:
                    el = settingsFile.getRootElement().getChild(SETTING_PATH_KICKASSEMBLER);
                    if (null==el) {
                        el = new Element(SETTING_PATH_KICKASSEMBLER);
                        settingsFile.getRootElement().addContent(el);
                    }
                    break;
            }
            if (el!=null) {
                el.setText(path.toString());
            }
        }
    }
    /**
     * 
     * @param emulator
     * @return 
     */
    public File getEmulatorPath(int emulator) {
        Element el;
        switch (emulator) {
            case ConstantsR64.EMU_VICE:
                el = settingsFile.getRootElement().getChild(SETTING_PATH_EMU);
                break;
            default:
                el = settingsFile.getRootElement().getChild(SETTING_PATH_EMU);
                break;
        }
        File value = null;
        if (el!=null) {
            value = new File(el.getText());
        }
        return value;
    }
    /**
     * 
     * @param compiler
     * @param path 
     */
    public void setEmulatorPath(int compiler, File path) {
        if (path!=null) {
            Element el = null;
            switch (compiler) {
                case ConstantsR64.EMU_VICE:
                    el = settingsFile.getRootElement().getChild(SETTING_PATH_EMU);
                    if (null==el) {
                        el = new Element(SETTING_PATH_EMU);
                        settingsFile.getRootElement().addContent(el);
                    }
                    break;
            }
            if (el!=null) {
                el.setText(path.toString());
            }
        }
    }
}
