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

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 *
 * @author danielludecke
 */
public class AcceleratorKeys {

    /**
     * The xml file which stores all accelerator key information of the main window.
     * This data is loaded and saved within the CSettings class. The data is get/set 
     * via getFile/setFile methods (see below)
     */
    private Document acceleratorKeysMain;
    /**
     * this is the mask key. on mac os, we ususally have the "meta"-key as mask,
     * on windows or linux, however, ctrl is used
     */
    private String mask=null;
    private String delkey=null;
    private String pluskey=null;
    private String minuskey=null;
    private String renamekey=null;
    private String historykey=null;
    /**
     * Constant used as parameter for the getCount method
     */
    public static final int MAINKEYS = 1;
    /**
     * The accelerator keys class. This class manages the accelerator keys. The user can define
     * own accelerator keys for each relevant action. Retrieving and setting this user defined
     * data is done by this class.
     * <br>
     * <br>
     * An XML-File could look like this:<br>
     * <br>
     * &lt;acceleratorkeys&gt;<br>
     * &nbsp;&nbsp;&lt;key action=&quot;newEntry&quot;&gt;control n&lt;/key&gt;<br>
     * &nbsp;&nbsp;&lt;key action=&quot;openDocument&quot;&gt;control o&lt;/key&gt;<br>
     * &lt;/acceleratorkeys&gt;<br>
     */
    public AcceleratorKeys() {
        // init the xml file which should store the accelerator keys
        acceleratorKeysMain = new Document(new Element("acceleratorkeys"));
        // init a default acceleratotr table
        initAcceleratorKeys();
    }
    
    
    /**
     * This method inits a default accelerator table. Usually, the CSettings-class loads
     * information from an xml file and overwrites these default settings, by passing the loaded
     * xml file via "setAcceleratorFile" to this class (see below)
     */
    public final void initAcceleratorKeys() {
        // check out which os we have, and set the appropriate mask-key
        if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
            mask="meta";
            delkey="BACK_SPACE";
            pluskey="CLOSE_BRACKET";
            minuskey="SLASH";
            renamekey="meta ENTER";
            historykey="control shift";
        }
        else {
            mask="control";
            delkey="DELETE";
            pluskey="PLUS";
            minuskey="MINUS";
            renamekey="F2";
            historykey="alt";
        }
        // We separate the initialisation of the accelerator tables for each 
        // window to keep an better overiew.
        initMainKeys();
    }
    
    
    /**
     * This method inits the accelerator table of the main window's menus. We separate
     * the initialisation of the accelerator tables for each window to keep an better
     * overiew.
     * 
     * This method creates all the acceleratorkeys-child-elements, but only, if they don't
     * already exist. We do this because when loading older acceleratorkeys-xml-document-structures,
     * we might have new elements that would not be initialised. but now we can call this 
     * method after loading the xml-document, and create elements and default values for all
     * new elements. This ensures compatibility to older/news settings-file-versions.
     */
    private void initMainKeys() {
        // this is our element variable which will be used below to set all the child elements
        Element acckey;
        
        // now we have to go through an endless list of accelerator keys. it is important
        // that the attribute values have exactly the same spelling like the actions' names
        // which can be found in the properties-files (resources). This ensures we can easily
        // assign accelerator keys to actions:
        //
        // javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(zettelkasten.ZettelkastenApp.class).getContext().getActionMap(ZettelkastenView.class, this);
        // AbstractAction ac = (AbstractAction) actionMap.get(CAcceleratorKey.getActionName());
        // KeyStroke ks = KeyStroke.getKeyStroke(CAcceleratorKey.getAccelerator());
        // ac.putValue(AbstractAction.ACCELERATOR_KEY, ks);        
        
        //
        // The actions of the main window's file menu
        //
        
        // the accelerator for the "addNewTab" action
        if (!findElement(MAINKEYS,"addNewTab")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "addNewTab");
            acckey.setText(mask+" N");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "openFile" action
        if (!findElement(MAINKEYS,"openFile")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "openFile");
            acckey.setText(mask+" O");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "saveFile" action
        if (!findElement(MAINKEYS,"saveFile")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "saveFile");
            acckey.setText(mask+" S");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "closeFile" action
        if (!findElement(MAINKEYS,"closeFile")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "closeFile");
            acckey.setText(mask+" W");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "closeAll" action
        if (!findElement(MAINKEYS,"closeAll")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "closeAll");
            acckey.setText(mask+" shift W");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "saveFileAs" action
        if (!findElement(MAINKEYS,"saveFileAs")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "saveFileAs");
            acckey.setText(mask+" shift S");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "quit" action
        if (!findElement(MAINKEYS,"quit")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "quit");
            acckey.setText(mask+" Q");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "selectAllText" action
        if (!findElement(MAINKEYS,"selectAllText")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "selectAllText");
            acckey.setText(mask+" A");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "undoAction" action
        if (!findElement(MAINKEYS,"undoAction")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "undoAction");
            acckey.setText(mask+" Z");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "redoAction" action
        if (!findElement(MAINKEYS,"redoAction")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "redoAction");
            acckey.setText(mask+" Y");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "findStart" action
        if (!findElement(MAINKEYS,"findStart")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "findStart");
            acckey.setText(mask+" F");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "findNext" action
        if (!findElement(MAINKEYS,"findNext")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "findNext");
            acckey.setText("F3");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "findPrev" action
        if (!findElement(MAINKEYS,"findPrev")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "findPrev");
            acckey.setText("shift F3");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "replaceTerm" action
        if (!findElement(MAINKEYS,"replaceTerm")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "replaceTerm");
            acckey.setText(mask+" R");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "runFile" action
        if (!findElement(MAINKEYS,"runFile")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "runFile");
            acckey.setText("F5");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "compileFile" action
        if (!findElement(MAINKEYS,"compileFile")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "compileFile");
            acckey.setText("F6");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
    }


    
    /**
     * Retrieves an xml-file with the requested accelerator information. This method is called
     * from within the CSettings-class, which handles the loading and saving of these xml files.
     * 
     * Following constants should be used as parameters:<br>
     * MAINKEYS<br>
     * NEWENTRYKEYS<br>
     * DESKTOPKEYS<br>
     * SEARCHRESULTSKEYS<br>
     * 
     * @param what (uses constants, see global field definition at top of source)
     * @return
     */
    public Document getDocument(int what) {
        // init variable
        Document doc;
        // select the right xml document, depending on which accelerator table is requested
        switch (what) {
            case MAINKEYS: doc = acceleratorKeysMain; break;
            default: doc = acceleratorKeysMain; break;
        }
        return doc;
    }
    
    /**
     * This method sets an accelerator file. This method called from within the CSettings class
     * where the data is loaded and the file/infiormation is passed to this method.
     * 
     * Following constants should be used as parameters:<br>
     * MAINKEYS<br>
     * NEWENTRYKEYS<br>
     * DESKTOPKEYS<br>
     * SEARCHRESULTSKEYS<br>
     * 
     * @param what (uses constants, see global field definition at top of source)
     * @param af
     */
    public void setDocument(int what, Document af) {
        // select the right xml document, depending on which accelerator table is requested
        // TODO wieder entfernen
//        switch (what) {
//            case MAINKEYS: acceleratorKeysMain=af; break;
//            case NEWENTRYKEYS: acceleratorKeysNewEntry=af; break;
//            case DESKTOPKEYS: acceleratorKeysDesktop=af; break;
//            case SEARCHRESULTSKEYS: acceleratorKeysSearchResults=af; break;
//            default: acceleratorKeysMain=af; break;
//        }
    }
    

    /**
     * This method returns the size of one of the xml data files. Following constants should
     * be used as parameters:<br>
     * MAINKEYS<br>
     * NEWENTRYKEYS<br>
     * DESKTOPKEYS<br>
     * SEARCHRESULTSKEYS<br>
     * 
     * @param what (uses constants, see global field definition at top of source)
     * @return the size of the requested data file
     */
    public int getCount(int what) {
        return getDocument(what).getRootElement().getContentSize();
    }
    
    
    /**
     * This methods returns the accelerator key of a given position in the xml-file
     * Following constants should be used as parameters:<br>
     * MAINKEYS<br>
     * NEWENTRYKEYS<br>
     * DESKTOPKEYS<br>
     * SEARCHRESULTSKEYS<br>
     * 
     * @param what (uses constants, see global field definition at top of source)
     * @param pos (a valid position of an element)
     * @return the string containing the accelerator key or {@code null} if nothing was found
     */
    public String getAcceleratorKey(int what, int pos) {
        // retrieve the element
        Element acckey = retrieveElement(what, pos);
        // if the element was not found, return an empty string
        if (null==acckey) {
            return null;
        }
        // else the value (i.e. the accelerator key string)
        return acckey.getText();
    }
    
    
    /**
     * This methods returns the accelerator key of a given position in the xml-file
     * Following constants should be used as parameters:<br>
     * MAINKEYS<br>
     * NEWENTRYKEYS<br>
     * DESKTOPKEYS<br>
     * SEARCHRESULTSKEYS<br>
     *
     * @param what uses constants, see global field definition at top of source
     * @param actioname the attribute (i.e. the action's name) we want to find
     * @return the string containing the accelerator key or {@code null}  if nothing was found
     */
    public String getAcceleratorKey(int what, String actionname) {
        // retrieve the element
        Element acckey = retrieveElement(what, actionname);
        // if the element was not found, return an empty string
        if (null==acckey) {
            return null;
        }
        // else the value (i.e. the accelerator key string)
        return acckey.getText();
    }


    /**
     * This methods returns the accelerator key of a given position in the xml-file
     * Following constants should be used as parameters:<br>
     * MAINKEYS<br>
     * NEWENTRYKEYS<br>
     * DESKTOPKEYS<br>
     * SEARCHRESULTSKEYS<br>
     * 
     * @param what uses constants, see global field definition at top of source
     * @param pos a valid position of an element
     * @return the string containing the accelerator key or {@code null} if nothing was found
     */
    public String getAcceleratorAction(int what, int pos) {
        // retrieve the element
        Element acckey = retrieveElement(what, pos);
        // return the matching string value of the element
        String retval;
        // if the element was not found, return an empty string
        if (null==acckey) {
            retval = null;
        }
        // else the value (i.e. the accelerator key string)
        else {
            retval = acckey.getAttributeValue("action");
        }
        
        return retval;
    }
    
    
    /**
     * This method sets an accelerator key of an related action. To change an accelerator key,
     * provide the action's name and the keystroke-value as string parameters. furthermore, we
     * have to tell the method, to which file the changes should be applied (param what).
     * 
     * Following constants should be used as parameters:<br>
     * MAINKEYS<br>
     * NEWENTRYKEYS<br>
     * DESKTOPKEYS<br>
     * SEARCHRESULTSKEYS<br>
     * 
     * @param what (uses constants, see global field definition at top of source)
     * @param action (the action's name, as string, e.g. "newEntry")
     * @param keystroke (the keystroke, e.g. "ctrl N" (win/linux) or "meta O" (mac)
     */
    public void setAccelerator(int what, String action, String keystroke) {
        // create a list of all elements from the xml file
        try {
            List<?> elementList = getDocument(what).getRootElement().getContent();
            // and an iterator for the loop below
            Iterator<?> iterator = elementList.iterator();

            // counter for the return value if a found element attribute matches the parameter
            int cnt = 1;
            // iterate loop
            while (iterator.hasNext()) {
                // retrieve each single element
                Element acckey = (Element) iterator.next();
                // if action-attribute matches the parameter string...
                if (action.equals(acckey.getAttributeValue("action"))) {
                    // ...set the new keystroke
                    acckey.setText(keystroke);
                    // and leave method
                    return;
                }
                // else increase counter
                cnt++;
            }
        }
        catch (IllegalStateException e) {
            ConstantsR64.r64logger.log(Level.WARNING,e.getLocalizedMessage());
        }
    }
    

    /**
     * This method looks for the occurence of the attribute "attr". All elements of
     * an xml-file are searched for the given attribute. If an element contains that
     * atrtribut, the method returns true, false otherwise.
     * 
     * @param doc (the xml-document where to look for the attribute)
     * @param attr (the attribute we want to find)
     * @return true if we have an element that contains that attribute, false otherwise
     */
    private boolean findElement(int what, String attr) {
        // create a list of all elements from the acceleratorkeys xml file
        try { 
            List<?> elementList = getDocument(what).getRootElement().getContent();
            // if we have any elements at all, go on
            if (elementList.size()>0) {
                // and an iterator for the loop below
                Iterator<?> iterator = elementList.iterator();
                // iterate loop
                while (iterator.hasNext()) {
                    // retrieve each single element
                    Element entry = (Element) iterator.next();
                    // try to get the requested element
                    String sv = entry.getAttributeValue("action");
                    // if it exists, return true
                    if (sv!=null && sv.equals(attr)) {
                        return true;
                    }
                }
                // if no attribute was found, return false
                return false;
            }
            else {
                return false;
            }
        }
        catch (IllegalStateException e) {
            return false;
        }
    }
    
    
    /**
     * This method looks for the occurence of the attribute "attr". All elements of
     * an xml-file are searched for the given attribute. If an element contains that
     * atrtribut, it is returned.
     *
     * @param doc the xml-document where to look for the attribute
     * @param attr the attribute (i.e. the action's name) we want to find
     * @return the element which matches the given action-name {@code attr} inside the document {@code doc},
     * or null if no match was found
     */
    private Element retrieveElement(int what, String attr) {
        // create a list of all elements from the acceleratorkeys xml file
        try {
            List<?> elementList = getDocument(what).getRootElement().getContent();
            // if we have any elements at all, go on
            if (elementList.size()>0) {
                // and an iterator for the loop below
                Iterator<?> iterator = elementList.iterator();
                // iterate loop
                while (iterator.hasNext()) {
                    // retrieve each single element
                    Element entry = (Element) iterator.next();
                    // try to get the requested element
                    String sv = entry.getAttributeValue("action");
                    // if it exists, return true
                    if (sv!=null && sv.equals(attr)) {
                        return entry;
                    }
                }
            }
            return null;
        }
        catch (IllegalStateException e) {
            return null;
        }
    }


    /**
     * This function retrieves an element of a xml document at a given
     * position. The position is a value from 1 to (size of xml file) - in contrary
     * to usual array handling where the range is from 0 to (size-1).
     * 
     * @param doc (the xml document where to look for elements)
     * @param pos (the position of the element)
     * @return the element if a match was found, otherwise null)
     */
    private Element retrieveElement(int what, int pos) {
        // create a list of all elements from the given xml file
        try { 
            List<?> elementList = getDocument(what).getRootElement().getContent();
            // and return the requestet Element
            try {
                return (Element) elementList.get(pos-1);
            }
            catch (IndexOutOfBoundsException e) {
                return null;
            }
        }
        catch (IllegalStateException e) {
            return null;
        }
    }
}
