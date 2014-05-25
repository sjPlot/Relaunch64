/*
 * Copyright (C) 2014 Soci/Singular
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.relaunch64.popelganda.Editor;

import de.relaunch64.popelganda.database.Settings;
import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.Tools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.gjt.sp.jedit.IPropertyManager;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.Registers;
import org.gjt.sp.jedit.syntax.ModeProvider;
import org.gjt.sp.jedit.textarea.AntiAlias;
import org.gjt.sp.jedit.textarea.StandaloneTextArea;
import org.gjt.sp.jedit.textarea.Gutter;
import org.gjt.sp.util.SyntaxUtilities;

/**
 * 
 * @author Soci/Singular
 */
public class RL64TextArea extends StandaloneTextArea {
    private final static Properties props;
    private static final IPropertyManager propertyManager;
    private final KeyListener keyListener;
    private final Settings settings;
    private int compiler;
    /**
     * auto-completion popup for labels and macros etc.
     */
    private JPopupMenu suggestionPopup = null;
    private JList suggestionList;
    private String suggestionSubWord;
    private String suggestionContinuedWord;
    private static final String sugListContainerName="sugListContainerName";
    /**
     * constant that defines which directives should be shown in auto-completion
     * popup. This one shows labels only.
     */
    private static final int SUGGESTION_LABEL = 1;
    /**
     * constant that defines which directives should be shown in auto-completion
     * popup. This one shows functions only.
     */
    private static final int SUGGESTION_FUNCTION = 2;
    /**
     * constant that defines which directives should be shown in auto-completion
     * popup. This one shows macros only.
     */
    private static final int SUGGESTION_MACRO = 3;
    /**
     * constant that defines which directives should be shown in auto-completion
     * popup. This one shows macros and funtions.
     */
    private static final int SUGGESTION_FUNCTION_MACRO = 4;
    /**
     * constant that defines which directives should be shown in auto-completion
     * popup. This one shows macros, functions and script-commands
     */
    private static final int SUGGESTION_FUNCTION_MACRO_SCRIPT = 5;
    /**
     * Load key and editor settings on class creation. Oblogatory. Personal
     * settings may be changed via setProperty().
     */
    static {
        props = new Properties();
        if (ConstantsR64.IS_OSX) {
            props.putAll(loadProperties("/de/relaunch64/popelganda/resources/Mac_OS_X_keys.props"));
        }
        else {
            props.putAll(loadProperties("/de/relaunch64/popelganda/resources/jEdit_keys.props"));
        }
        props.putAll(loadProperties("/de/relaunch64/popelganda/resources/jEdit.props"));
        propertyManager = new IPropertyManager() {
            @Override
            public String getProperty(String name) {
                return props.getProperty(name);
            }
        };
    }
    /**
     * Sets editor properties. Properties must be loaded from XML file on
     * class creation.
     * 
     * @param name
     * @param val 
     */
    public void setProperty(String name, String val) {
        props.setProperty(name, val);
    }
    /**
     * Loads properties from an internal XML file.
     * @param fileName 
     */
    public void propertiesFromFile(String fileName) {
        props.putAll(loadProperties("/de/relaunch64/popelganda/resources/" + fileName));
    }
    private static Properties loadProperties(String fileName) {
        Properties loadedProps = new Properties();
        InputStream in = StandaloneTextArea.class.getResourceAsStream(fileName);
        try {
            loadedProps.load(in);
        }
        catch (IOException e) {
            ConstantsR64.r64logger.log(Level.WARNING,e.getLocalizedMessage());
        }
        finally {
            try {
                if (in!=null) in.close();
            }
            catch (IOException ex) {
            }
        }
        return loadedProps;
    }
    /**
     * Processes key events. Default key handler of jEdit component, which should
     * work slightly faster than key listener
     * 
     * @param evt 
     */
    @Override
    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_RELEASED) {
            keyListener.keyReleased(evt);
        }
        if (!evt.isConsumed()) {
            super.processKeyEvent(evt);
        }
    }
    /**
     * Class that handles key events on editor component.
     */
    private class RL64KeyListener extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent evt) {
            // ctrl+space opens label-auto-completion
            if (evt.getKeyCode()==KeyEvent.VK_SPACE && evt.isControlDown() && !evt.isShiftDown() && !evt.isAltDown()) {
                showSuggestion(SUGGESTION_LABEL);
            }
            // ctrl+space opens macro-function-auto-completion
            else if (evt.getKeyCode()==KeyEvent.VK_SPACE && evt.isControlDown() && evt.isShiftDown() && !evt.isAltDown()) {
                showSuggestion(SUGGESTION_FUNCTION_MACRO_SCRIPT);
            }
        }
    }
    /**
     * OS X stuff. Default shortcuts don't work on OS X
     */
    public void cutText() {
        Registers.cut(this,'$');
    }
    /**
     * OS X stuff. Default shortcuts don't work on OS X
     */
    public void copyText() {
        Registers.copy(this,'$');
    }
    /**
     * OS X stuff. Default shortcuts don't work on OS X
     */
    public void pasteText() {
        Registers.paste(this,'$');
    }
    /**
     * Sets the font of the editor component and the gutter. Furthermore,
     * antialias-setting is updated
     */
    public final void setFonts(Font mf) {
        // set text font
        setProperty("view.font", mf.getFontName());
        setProperty("view.fontsize", String.valueOf(mf.getSize()));
        setProperty("view.fontstyle", "0");
        // set line numbers font
        setProperty("view.gutter.font", mf.getFontName());
        setProperty("view.gutter.fontsize", String.valueOf(mf.getSize()));
        setProperty("view.gutter.fontstyle", "0");
        // set fonts
        setFont(mf);
        getPainter().setFont(mf);
        getGutter().setFont(mf);
    }
    /**
     * Sets line number alignment og gutter.
     * @param alignment
     */
    public final void setLineNumberAlignment(int alignment) {
        String align;
        switch (alignment) {
            case Gutter.RIGHT: align = "right"; break;
            case Gutter.CENTER: align = "center"; break;
            default: align = "left"; break;
        }
        setProperty("view.gutter.numberAlignment", align);
    }
    /**
     * Sets antialiasing for text.
     * @param antialias
     */
    public final void setTextAntiAlias(String antialias) {
        setProperty("view.antiAlias", antialias);
    }
    /**
     * Sets tab-size for editor component.
     * @param tabSize
     */
    public final void setTabs(int tabSize) {
        setProperty("buffer.tabSize", String.valueOf(tabSize));
    }
    /**
     * Sets the compiler syntax. See {@code ConstantsR64.assemblymodes} for different values. This method
     * loads an XML file with syntax definition for keywords etc.
     */
    public final void setCompilerSyntax() {
        // set syntax style
        Mode mode = new Mode("asm");
        String pathToMode = "/de/relaunch64/popelganda/resources/modes/";
        if (settings.getAlternativeAssemblyMode()) pathToMode += "alt/";
        mode.setProperty("file", pathToMode + ConstantsR64.assemblers[compiler].syntaxFile());
        ModeProvider.instance.addMode(mode);
        // add mode to buffer
        getBuffer().setMode(mode);
    }
    /**
     * Sets the color scheme / highlight color. No parameter is needed, since the 
     * color scheme value is read from Settings class.
     */
    public final void setSyntaxScheme() {
        // get color scheme
        int scheme = settings.getColorScheme();
        // syntax colors for editor
        setProperty("view.fgColor", ColorSchemes.getColor(scheme, ColorSchemes.COLOR_NORMAL));
        setProperty("view.bgColor", ColorSchemes.getColor(scheme, ColorSchemes.BACKGROUND));
        setProperty("view.style.comment1", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMMENT));
        setProperty("view.style.comment2", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMMENT));
        setProperty("view.style.comment3", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMMENT));
        setProperty("view.style.comment4", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMMENT));
        setProperty("view.style.digit", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_NUMBER));
        // the alternative assembly mode highlights all numbers is same color
        if (settings.getAlternativeAssemblyMode()) {
            setProperty("view.style.literal3", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_STRING));
            setProperty("view.style.literal4", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_STRING));
            setProperty("view.style.keyword4", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMPILERKEYWORD));
        }
        // the default assembly mode highlights values and addresses in different colors
        else {
            setProperty("view.style.literal3", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_BIN));
            setProperty("view.style.literal4", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_HEX));
            setProperty("view.style.keyword4", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_JUMP));
        }
        setProperty("view.style.function", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_SCRIPTKEYWORD));
        setProperty("view.style.keyword1", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_KEYWORD));
        setProperty("view.style.keyword2", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_ILLEGALOPCODE));
        setProperty("view.style.keyword3", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMPILERKEYWORD));
        setProperty("view.style.literal1", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_STRING));
        setProperty("view.style.literal2", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_STRING));
        setProperty("view.style.label", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_JUMP));
        setProperty("view.style.operator", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_OPERATOR));
        // syntax colors for line numbers
        setProperty("view.gutter.bgColor", ColorSchemes.getColor(scheme, ColorSchemes.LINE_BACKGROUND));
        setProperty("view.gutter.fgColor", ColorSchemes.getColor(scheme, ColorSchemes.LINE_COLOR));
        setProperty("view.gutter.highlightColor", ColorSchemes.getColor(scheme, ColorSchemes.LINE_COLOR));
        setProperty("view.gutter.currentLineColor", ColorSchemes.getColor(scheme, ColorSchemes.LINE_HIGHLIGHT));
        setProperty("view.gutter.focusBorderColor", ColorSchemes.getColor(scheme, ColorSchemes.LINE_BORDER));
        setProperty("view.gutter.noFocusBorderColor", ColorSchemes.getColor(scheme, ColorSchemes.LINE_BORDER));
        // load color scheme
        Font mf = settings.getMainFont();
        // set for- and background color of text area
        getPainter().setBackground(SyntaxUtilities.parseColor(ColorSchemes.getColor(scheme, ColorSchemes.BACKGROUND), Color.black));
        getPainter().setForeground(SyntaxUtilities.parseColor(ColorSchemes.getColor(scheme, ColorSchemes.COLOR_NORMAL), Color.black));
        // set for- and background color of line numbers
        getGutter().setBackground(SyntaxUtilities.parseColor(ColorSchemes.getColor(scheme, ColorSchemes.LINE_BACKGROUND), Color.black));
        getGutter().setForeground(SyntaxUtilities.parseColor(ColorSchemes.getColor(scheme, ColorSchemes.LINE_COLOR), Color.black));
        getGutter().setHighlightedForeground(SyntaxUtilities.parseColor(ColorSchemes.getColor(scheme, ColorSchemes.LINE_COLOR), Color.black));
        getGutter().setCurrentLineForeground(SyntaxUtilities.parseColor(ColorSchemes.getColor(scheme, ColorSchemes.LINE_HIGHLIGHT), Color.black));
        Color bc = SyntaxUtilities.parseColor(ColorSchemes.getColor(scheme, ColorSchemes.LINE_BORDER), Color.black);
        getGutter().setBorder(1, bc, bc, bc);
        // TODO did not find out when it's best to call "setStyles" to make all stuff working
        // or if there's a specific order?
        getPainter().setStyles(SyntaxUtilities.loadStyles(mf.getFontName(), mf.getSize()));
    }
    /**
     * Set compiler of current editor / buffer. Frequently needed information, for various functions,
     * so we put this variable as a global field for this class.
     * 
     * @param c 
     */
    public final void setCompiler(int c) {
        this.compiler = c;
    }
    public int getCompiler() {
        return compiler;
    }
    /**
     * The StandaloneTextArea component of jEdit. Provides fast syntax highlighting and some other
     * useful features. Replaces the default Swing JEditorPane.
     * 
     * @param settings 
     */
    public RL64TextArea(Settings settings) {
        super(propertyManager);
        // save settings
        this.settings = settings;
        // set default compiler
        setCompiler(settings.getPreferredCompiler());
        // set syntaxscheme
        setSyntaxScheme();
        // set line number alignment
        setLineNumberAlignment(settings.getLineNumerAlignment());
        // set tab width
        setTabs(settings.getTabWidth());
        // set fonts
        setFonts(settings.getMainFont());
        // set antialias
        setTextAntiAlias(settings.getAntiAlias());
        // setup keylistener
        keyListener = new RL64KeyListener();
    }
    /**
     * Specified the list of delimiter strings that separate words/token for recognizing
     * the syntax highlighting.
     * 
     * @param compiler A constants indicating which ASM compiler is used. Refer to ConstantsR64
     * to retrieve the list of constants used.
     * @return A String with all delimiters for the highlight tokens.
     */
    public static String getDelimiterList(int compiler) {
        String str = ",;:{}()[]+-/%<=>&!|^~*";
        switch (compiler) {
            case ConstantsR64.ASM_TMPX:
            case ConstantsR64.ASM_ACME:
            case ConstantsR64.ASM_64TASS:
            case ConstantsR64.ASM_CA65:
            case ConstantsR64.ASM_DREAMASS:
            case ConstantsR64.ASM_DASM:
                str = ",:{}()[]+-/<=>&|^~*";
                break;
            case ConstantsR64.ASM_KICKASSEMBLER:
                str = ",;:{}()[]+-/<=>&|^~*";
                break;
        }
        return str;
    }
    /**
     * Returns the comment string of the assembler {@code compiler}.
     * 
     * @param assembler
     * @return 
     */
    public static String getCommentString(int assembler) {
        String str = "//";
        switch (assembler) {
            case ConstantsR64.ASM_ACME:
            case ConstantsR64.ASM_TMPX:
            case ConstantsR64.ASM_64TASS:
            case ConstantsR64.ASM_CA65:
            case ConstantsR64.ASM_DREAMASS:
            case ConstantsR64.ASM_DASM:
                str = ";";
                break;
            case ConstantsR64.ASM_KICKASSEMBLER:
                str = "//";
                break;
        }
        return str;
    }
    /**
     * 
     * @param compiler
     * @return 
     */
    protected static String getMacroPrefix(int compiler) {
        String str = ".";
        switch (compiler) {
            case ConstantsR64.ASM_ACME:
                str = "!";
                break;
            case ConstantsR64.ASM_DASM:
                str = "";
                break;
            case ConstantsR64.ASM_KICKASSEMBLER:
            case ConstantsR64.ASM_64TASS:
            case ConstantsR64.ASM_TMPX:
            case ConstantsR64.ASM_CA65:
                str = ".";
                break;
            case ConstantsR64.ASM_DREAMASS:
                str = "#";
                break;
        }
        return str;
    }
    /**
     * Create the auto-completion popup.
     * 
     * @author Guillaume Polet
     * 
     * This example was taken from
     * http://stackoverflow.com/a/10883946/2094622
     * and modified for own purposes.
     * 
     * @param type
     */
    protected void showSuggestion(int type) {
        // hide old popup
        hideSuggestion();
        try {
            // retrieve chars that have already been typed
            String macroPrefix = "";
            if (type==SUGGESTION_FUNCTION_MACRO_SCRIPT) {
                macroPrefix = getMacroPrefix(compiler);
            }
            suggestionSubWord = getCaretString(false, macroPrefix);
            // check for valid value
            if (null==suggestionSubWord) return;
            // init variable
            Object[] labels = null;
            switch(type) {
                case SUGGESTION_FUNCTION:
                    // retrieve label list, remove last colon
                    labels = FunctionExtractor.getFunctionNames(suggestionSubWord.trim(), getBuffer().getText(), getCompiler());
                    break;
                case SUGGESTION_MACRO:
                    // retrieve label list, remove last colon
                    labels = FunctionExtractor.getMacroNames(suggestionSubWord.trim(), getBuffer().getText(), getCompiler());
                    break;
                case SUGGESTION_LABEL:
                    suggestionSubWord = ConstantsR64.assemblers[getCompiler()].labelGetStart(getLineText(getCaretLine()), getCaretPosition()-getLineStartOffset(getCaretLine()));
                    if (suggestionSubWord.length() == 0) break;
                    // retrieve label list, remove last colon
                    labels = LabelExtractor.getLabelNames(suggestionSubWord, getBuffer().getText(), getCompiler(), getCaretLine());
                    break;
                case SUGGESTION_FUNCTION_MACRO:
                    // retrieve label list, remove last colon
                    labels = FunctionExtractor.getFunctionAndMacroNames(suggestionSubWord.trim(), getBuffer().getText(), getCompiler());
                    break;
                case SUGGESTION_FUNCTION_MACRO_SCRIPT:
                    // retrieve label list, remove last colon
                    labels = FunctionExtractor.getFunctionMacroScripts(suggestionSubWord.trim(), getBuffer().getText(), getCompiler());
                    break;
            }
            // check if we have any labels
            if (labels!=null && labels.length>0) {
                Point location = offsetToXY(getCaretPosition());
                // create suggestion pupup
                suggestionPopup = new JPopupMenu();
                suggestionPopup.removeAll();
                suggestionPopup.setOpaque(false);
                suggestionPopup.setBorder(null);
                // create JList with label items
                suggestionList = createSuggestionList(labels);
                suggestionContinuedWord = suggestionSubWord;
                // check minimum length of list and add scroll pane if list is too long
                if (labels.length>20) {
                    javax.swing.JScrollPane listScrollPane = new javax.swing.JScrollPane(suggestionList);
                    listScrollPane.setBorder(null);
                    listScrollPane.setPreferredSize(new java.awt.Dimension(150,300));
                    listScrollPane.setName(sugListContainerName);
                    suggestionPopup.add(listScrollPane, BorderLayout.CENTER);
                }
                else {
                    // else just add list w/o scroll pane
                    suggestionPopup.add(suggestionList, BorderLayout.CENTER);
                }
                suggestionPopup.show(this, location.x, getBaseline(0, 0) + location.y);
                // set input focus to popup
                /**
                 * JDK 8 Lambda
                 */
//                SwingUtilities.invokeLater(() -> {
//                    suggestionList.requestFocusInWindow();
//                });
                // set input focus to popup
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        suggestionList.requestFocusInWindow();
                    }
                });
            }
        }
        catch (IndexOutOfBoundsException ex) {
        }
    }
    /**
     * Closes the auto-suggestion popup.
     */
    protected void hideSuggestion() {
        if (suggestionPopup != null) {
            suggestionPopup.setVisible(false);
            suggestionPopup=null;
        }
        // set focus back to editorpane
        requestFocusInWindow();
    }
    /**
     * Creates a JList object that is added to the suggestion / auto-completion
     * popup.
     * 
     * @param labels the items of the list.
     * @return a JList
     */
    protected JList createSuggestionList(final Object[] labels) {
        // create list model
        DefaultListModel dlm = new DefaultListModel();
        // add items to list model. we need this for the key listener
        // we need to add in reverse order, because items are added at
        // start of list model
        for (int i=labels.length-1; i>=0; i--) dlm.add(0, labels[i]);
        // create list from list model
        JList sList = new JList(dlm);
        sList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sList.setSelectedIndex(0);
        sList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    insertSelection();
                }
            }
        });
        sList.addKeyListener(SugestionKeyListener);
        return sList;
    }
    /**
     * A key-listener for the suggestion/auto-completion popup that appears
     * when the user presses ctrl+space. The key-listener handles the
     * navigation through the list-component.
     * 
     * @author Guillaume Polet
     * 
     * This example was taken from
     * http://stackoverflow.com/a/10883946/2094622
     * and modified for own purposes.
     */
    KeyListener SugestionKeyListener = new java.awt.event.KeyAdapter() {
        @Override public void keyTyped(java.awt.event.KeyEvent evt) {
            if (evt.getKeyCode()==java.awt.event.KeyEvent.VK_UP) {
                evt.consume();
                int index = Math.min(suggestionList.getSelectedIndex() - 1, 0);
                suggestionList.setSelectedIndex(index);
            }
            else if (evt.getKeyCode()==java.awt.event.KeyEvent.VK_DOWN) {
                evt.consume();
                int index = Math.min(suggestionList.getSelectedIndex() + 1, suggestionList.getModel().getSize() - 1);
                suggestionList.setSelectedIndex(index);
            }
            else {
                // get typed char in list. we filter the list here when user continues
                // typing when popup is already shown
                String typedChar = String.valueOf(evt.getKeyChar());
                // check whether types char is character or digit
                Pattern p = Pattern.compile("[a-zA-Z0-9]+");
                Matcher m = p.matcher(typedChar);
                if (m.matches()) {
                    // if yes, "virtually" append to already typed chars of
                    // "suggestionSubWord", as if user would continue typing without
                    // suggestion popup
                    suggestionContinuedWord = suggestionContinuedWord+typedChar;
                    // get list model with list items
                    DefaultListModel dlm = (DefaultListModel) suggestionList.getModel();
                    // iterate items
                    for (int i=dlm.getSize()-1; i>=0; i--) {
                        String el = dlm.get(i).toString();
                        // check if item matches the "virtually" typed text
                        if (!el.startsWith(suggestionContinuedWord)) {
                            // if not, remove it
                            dlm.remove(i);
                        }
                    }
                    // TODO JList size could be changed when item list is reduced.
                }
            }
        }
        @Override public void keyReleased(java.awt.event.KeyEvent evt) {
            if (evt.getKeyCode()==java.awt.event.KeyEvent.VK_ENTER) {
                evt.consume();
                insertSelection();
            }
        }
    };
    /**
     * 
     * @param wholeWord
     * @param specialDelimiter
     * @return 
     */
    public String getCaretString(boolean wholeWord, String specialDelimiter) {
        final int position = getCaretPosition()-getLineStartOffset(getCaretLine())-1;
        String text = getLineText(getCaretLine());
        String addDelim;
        switch (getCompiler()) {
            // use colon as additional delimiter for following assemblers
            case ConstantsR64.ASM_ACME:
            case ConstantsR64.ASM_64TASS:
            case ConstantsR64.ASM_DREAMASS:
            case ConstantsR64.ASM_TMPX:
                addDelim = "\n\r:";
                break;
            default:
                addDelim = "\n\r";
                break;
        }
        // get position start
        int start = Math.max(0, position);
        boolean isSpecialDelimiter = false;
        // if we have specific delimiters, add these to the delimiter list.
        // e.g. KickAss script-directives need a "." as special delimiter
        if (specialDelimiter!=null && !specialDelimiter.isEmpty()) addDelim = addDelim + specialDelimiter;
        while (start > 0) {
            // check if we have a delimiter
            boolean isDelim = Tools.isDelimiter(text.substring(start, start+1), addDelim);
            // check if delimiter was special delimiter.
            if (isDelim && text.substring(start, start+1).equals(specialDelimiter)) isSpecialDelimiter = true;
            // check if we have any delimiter
            if (!Character.isWhitespace(text.charAt(start)) && !isDelim) {
                start--;
            } else {
                start++;
                break;
            }
        }
        if (start > position) {
            return null;
        }
        // check if we want the complete word at the caret
        if (wholeWord) {
            int end = start;
            while (end<text.length() && !Character.isWhitespace(text.charAt(end)) && !Tools.isDelimiter(text.substring(end, end+1), addDelim)) end++;
            // if we found a special delimiter at beginning, add it to return string
            if (isSpecialDelimiter) {
                return (specialDelimiter+text.substring(start, end));
            }
            else {
                return text.substring(start, end);
            }
        }
        // retrieve chars that have already been typed
        if (isSpecialDelimiter) {
            // if we found a special delimiter at beginning, add it to return string
            return (specialDelimiter+text.substring(start, position));
        }
        else {
            return text.substring(start, position);
        }
    }
    /**
     * Inserts the selected auto-completion label string at the current caret position.
     * 
     * @return {@code true} if auto-completion was successful.
     */
    protected boolean insertSelection() {
        if (suggestionList.getSelectedValue() != null) {
            final String selectedSuggestion = ((String) suggestionList.getSelectedValue()).substring(suggestionSubWord.length());
            getBuffer().insert(getCaretPosition(), selectedSuggestion);
            requestFocusInWindow();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (suggestionPopup!=null) {
                        suggestionPopup.setVisible(false);
                        suggestionPopup = null;
                    }
                }
            });
            /**
             * JDK 8 Lambda
             */
//                SwingUtilities.invokeLater(() -> {
//                    if (suggestionPopup!=null) suggestionPopup.setVisible(false);
//                });
            return true;
        }
        return false;
    }
}
