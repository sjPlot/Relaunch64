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

import de.relaunch64.popelganda.assemblers.Assemblers;
import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.FileTools;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.IllegalAddException;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author Daniel Lüdecke
 */
public class CustomScripts {

    private Document scriptFile;
    private final File filepath;
    private static final String ELEMENT_SCRIPT = "Script";
    private static final String ATTR_NAME = "script_name";
    private static final String ROOT_NAME = "CustomScripts";

    public CustomScripts() {
        // first of all, create the empty documents
        scriptFile = new Document(new Element(ROOT_NAME));
        // create file path to script file
        filepath = FileTools.createFilePath("relaunch64-scripts.xml");
    }

    /**
     * Loads the scripts from an XML file
     */
    public void loadScripts() {
        // if file exists, go on...
        if (filepath != null && filepath.exists()) {
            try {
                SAXBuilder builder = new SAXBuilder();
                scriptFile = builder.build(filepath);
            } catch (JDOMException | IOException ex) {
                ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
            }
        }
    }

    /**
     * Saves the scripts to xml-file
     */
    public void saveScripts() {
        // if file exists, go on...
        if (filepath != null) {
            OutputStream dest = null;
            try {
                XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
                dest = new FileOutputStream(filepath);
                out.output(scriptFile, dest);
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
     * Adds a new script.
     *
     * @param name the name of the new script
     * @param content the content of the new script
     * @return {@code true} if script was successfully added.
     */
    public boolean addScript(String name, String content) {
        // check for valid values
        if (null == name || name.isEmpty() || null == content || content.isEmpty()) {
            return false;
        }
        // check if element exists
        int pos = findScript(name);
        // script already exists, so update
        if (pos != -1) {
            // retrieve element
            Element el = retrieveElement(pos);
            // change content
            if (el != null) {
                el.setText(content);
            } else {
                return false;
            }
        } // script does not exist, so add
        else {
            // create new element
            Element el = new Element(ELEMENT_SCRIPT);
            // add attribute
            el.setAttribute(ATTR_NAME, name);
            // add content
            el.setText(content);
            // add to document
            scriptFile.getRootElement().addContent(el);
        }
        return true;
    }

    /**
     * Returns the content of the script with the name {@code name}.
     *
     * @param name the name of the script which content should be fetched
     * @return the content of the script with the name {@code name}.
     */
    public String getScript(String name) {
        // check if element exists
        int pos = findScript(name);
        // script already exists, so update
        if (pos != -1) {
            // retrieve element
            Element el = retrieveElement(pos);
            // check for valid value
            if (el != null) {
                return el.getText();
            }
        }
        return null;
    }

    /**
     * Returns the script name from the script at the position {@code index}.
     *
     * @param pos the index position of the script which name should be fetched
     * @return the script name from the script at the position {@code index}
     */
    public String getScriptName(int pos) {
        // retrieve element
        Element el = retrieveElement(pos);
        // check for valid value
        if (el != null) {
            return el.getAttributeValue(ATTR_NAME);
        }
        return null;
    }

    /**
     * Returns a string array with all names of available scripts.
     *
     * @return all available script names, or {@code null} if no scripts yet exits
     */
    public String[] getScriptNames() {
        ArrayList<String> snames = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            String sn = getScriptName(i);
            if (sn != null && !sn.isEmpty() && !snames.contains(sn)) {
                snames.add(sn);
            }
        }
        if (snames.isEmpty()) {
            return null;
        }
        return snames.toArray(new String[snames.size()]);
    }

    /**
     * Retrieves an element from the XML-database at the position {@code pos}.
     *
     * @param pos the position of the XML-element that should be fetched
     * @return the {@code Element} at position {@code pos} or {@code null} if no such element exists
     * or {@code pos} is out of bounds.
     */
    private Element retrieveElement(int pos) {
        // check for valid values
        if (pos < 0) {
            return null;
        }
        // create a list of all elements from the given xml file
        try {
            List<Element> elementList = scriptFile.getRootElement().getChildren();
            // and return the requestet Element
            try {
                return elementList.get(pos);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Finds the script with the name {@code name} and returns its data base index number.
     *
     * @param name The name of the script that should be found.
     * @return The index number of the found script in the XML data base, or -1 if no such script
     * with the name {@code name} exists (or {@code name} was {@code null} or empty).
     */
    public int findScript(String name) {
        if (null == name || name.trim().isEmpty()) {
            return -1;
        }
        // trim spaces
        name = name.trim();
        // create a list of all author elements from the author xml file
        try {
            List<Element> scriptList = scriptFile.getRootElement().getChildren();
            // and an iterator for the loop below
            Iterator<Element> iterator = scriptList.iterator();
            // counter for the return value if a found synonym matches the parameter
            int cnt = 0;

            while (iterator.hasNext()) {
                Element script = iterator.next();
                // if synonym-index-word matches the parameter string, return the position
                if (name.equalsIgnoreCase(script.getAttributeValue(ATTR_NAME))) {
                    return cnt;
                }
                // else increase counter
                cnt++;
            }
            // if no author was found, return -1
            return -1;
        } catch (IllegalStateException e) {
            return -1;
        }
    }

    /**
     * Returns the number of edited scripts.
     *
     * @return the number of edited scripts
     */
    public int getCount() {
        return scriptFile.getRootElement().getContentSize();
    }

    /**
     * Removes the script with the name {@code name}.
     *
     * @param name the name of the script that should be removed.
     * @return {@code true} if removal was successful
     */
    public boolean removeScript(String name) {
        int index = findScript(name);
        return removeScript(index);
    }

    /**
     * Removes the script at the index {@code index}. Use
     * {@link #findScript(java.lang.String) findScript()} to find the index of a script by name.
     *
     * @param index the index of the script that should be removed
     * @return {@code true} if removal was successful
     */
    public boolean removeScript(int index) {
        List<Element> children = scriptFile.getRootElement().getChildren();
        try {
            Element el = children.remove(index);
            if (el != null) {
                // reset document
                scriptFile = new Document(new Element(ROOT_NAME));
                // iterate and add remaining elements
                for (Element e : children) {
                    addScript(e.getAttributeValue(ATTR_NAME), e.getText());
                }
                return true;
            }
        } catch (UnsupportedOperationException | IndexOutOfBoundsException | IllegalAddException ex) {
            return false;
        }
        return false;
    }

    public String getCompilerHelp(Object scriptName) {
        // ret value
        String helpText = "";
        if (null == scriptName) {
            return null;
        }
        // iterate assemblers
        for (int i = 0; i < Assemblers.getCount(); i++) {
            // get file name
            String fn = Assemblers.byID(i).fileName();
            // get script...
            String script = getScript(scriptName.toString());
            // ... and scriptlines
            String[] scriptlines = script.split(System.lineSeparator());
            // check whether assembler is in current script
            for (String sl : scriptlines) {
                // if yes, proceed
                if (sl.contains(fn)) {
                    try {
                        // prepare command line
                        String file = sl.substring(0, sl.indexOf(fn) + fn.length()).trim();
                        String commandline = file + Assemblers.byID(i).getHelpCLI();
                        ProcessBuilder pb;
                        Process p;
                        // Start ProcessBuilder
                        pb = new ProcessBuilder(commandline.split(" "));
                        // set parent directory to sourcecode fie
                        pb = pb.directory(new File(file).getParentFile());
                        pb = pb.redirectOutput(ProcessBuilder.Redirect.PIPE).redirectError(ProcessBuilder.Redirect.PIPE);
                        // start process
                        p = pb.start();
                        // create scanner to receive compiler messages
                        try (Scanner sc = new Scanner(p.getInputStream()).useDelimiter(System.lineSeparator())) {
                            // write output to string builder
                            while (sc.hasNextLine()) {
                                helpText = helpText + sc.nextLine() + System.lineSeparator();
                            }
                        }
                        try (Scanner sc = new Scanner(p.getErrorStream()).useDelimiter(System.lineSeparator())) {
                            // write output to string builder
                            while (sc.hasNextLine()) {
                                helpText = helpText + sc.nextLine() + System.lineSeparator();
                            }
                        }
                    } catch (IOException ex) {
                        return null;
                    }
                    return helpText;
                }
            }
        }
        return null;
    }
}
