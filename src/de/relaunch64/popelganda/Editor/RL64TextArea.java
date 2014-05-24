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
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.gjt.sp.jedit.IPropertyManager;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.syntax.ModeProvider;
import org.gjt.sp.jedit.textarea.AntiAlias;
import org.gjt.sp.jedit.textarea.StandaloneTextArea;
import org.gjt.sp.util.SyntaxUtilities;

/**
 * 
 * @author Soci/Singular
 */
public class RL64TextArea extends StandaloneTextArea {
    private final static Properties props;
    private static final IPropertyManager propertyManager;
    private final Settings settings;
    private int compiler;
    
    private JPopupMenu suggestionPopup = null;
    private JList suggestionList;
    private String suggestionSubWord;
    private static final String sugListContainerName="sugListContainerName";
    
    private static final int SUGGESTION_LABEL = 1;
    private static final int SUGGESTION_FUNCTION = 2;
    private static final int SUGGESTION_MACRO = 3;
    private static final int SUGGESTION_FUNCTION_MACRO = 4;
    private static final int SUGGESTION_FUNCTION_MACRO_SCRIPT = 5;

    static {
        props = new Properties();
        if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
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

    public final void setFonts() {
        // set default font
        Font mf = settings.getMainFont();
        // set text font
        setProperty("view.font", mf.getFontName());
        setProperty("view.fontsize", String.valueOf(mf.getSize()));
        setProperty("view.fontstyle", "0");
        // set line numbers font
        setProperty("view.gutter.font", mf.getFontName());
        setProperty("view.gutter.fontsize", String.valueOf(mf.getSize()));
        setProperty("view.gutter.fontstyle", "0");
        // set line number alignment
        setLineNumberAlignment();
        // set fonts
        setFont(mf);
        getPainter().setFont(mf);
        getGutter().setFont(mf);
        // set antialias
        setTextAntiAlias();
    }

    public final void setLineNumberAlignment() {
        // set line number alignment
        getGutter().setLineNumberAlignment(settings.getLineNumerAlignment());
    }
    
    public final void setTextAntiAlias() {
        // set default font
        Font mf = settings.getMainFont();
        // set antialias
        setProperty("view.antiAlias", "true");
        getPainter().setAntiAlias(new AntiAlias(settings.getAntiAlias()));
        getPainter().setStyles(SyntaxUtilities.loadStyles(mf.getFontName(), mf.getSize()));
    }
    
    public final void setTabs() {
        // TODO indent doesn't seem to work
        // set default font
        Font mf = settings.getMainFont();
        setProperty("buffer.tabSize", String.valueOf(settings.getTabWidth()));
        setProperty("buffer.folding", "indent");
        getPainter().setStyles(SyntaxUtilities.loadStyles(mf.getFontName(), mf.getSize()));
    }
    
    public final void setCompilerSyntax(int compiler) {
        // set syntax style
        Mode mode = new Mode("asm");
        String pathToMode = (settings.getAlternativeAssemblyMode()) ? ConstantsR64.alternativeassemblymodes[compiler] : ConstantsR64.assemblymodes[compiler];
        mode.setProperty("file", pathToMode);
        ModeProvider.instance.addMode(mode);
        // add mode to buffer
        getBuffer().setMode(mode);
    }
    
    public final void setSyntaxScheme() {
        int scheme = settings.getSyntaxScheme();
        // TODO alternative color schemes need other color values for literal3 and 4, and keyword 4
        // syntax colors for editor
        setProperty("view.fgColor", ColorSchemes.getColor(scheme, ColorSchemes.COLOR_NORMAL));
        setProperty("view.bgColor", ColorSchemes.getColor(scheme, ColorSchemes.BACKGROUND));
        setProperty("view.style.comment1", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMMENT));
        setProperty("view.style.comment2", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMMENT));
        setProperty("view.style.comment3", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMMENT));
        setProperty("view.style.comment4", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMMENT));
        setProperty("view.style.digit", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_NUMBER));
        if (settings.getAlternativeAssemblyMode()) {
            setProperty("view.style.literal3", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_STRING));
            setProperty("view.style.literal4", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_STRING));
            setProperty("view.style.keyword4", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMPILERKEYWORD));
        }
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
        // TODO gutter linenumber background not shown correctly, on OS X!
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
    
    public final void setCompiler(int c) {
        this.compiler = c;
    }
    public int getCompiler() {
        return compiler;
    }
//    public RL64TextArea() {
//        super(propertyManager);
//    }
    public RL64TextArea(Settings settings) {
        super(propertyManager);
        // save settings
        this.settings = settings;
        // set default compiler
        setCompiler(settings.getPreferredCompiler());
        // set syntaxscheme
        setSyntaxScheme();
        // set tab width
        setTabs();
        // set fonts
        setFonts();
        // TODO perhaps we have to derive from http://www.jedit.org/api/org/gjt/sp/jedit/input/TextAreaInputHandler.html
    }
    /**
     * Specified the list of delimiter strings that separate words/token for recodgnizing
     * the syntax highlighting.
     * @param compiler A constants indicating which ASM compiler is used. Refer to ConstantsR64
     * to retrieve the list of constants used.
     * @return A String with all delimiters for the highlight tokens.
     */
    public static String getDelimiterList(int compiler) {
        String str = ",;:{}()[]+-/%<=>&!|^~*";
        switch (compiler) {
            case ConstantsR64.COMPILER_ACME:
            case ConstantsR64.COMPILER_64TASS:
            case ConstantsR64.COMPILER_CA65:
            case ConstantsR64.COMPILER_DREAMASS:
            case ConstantsR64.COMPILER_DASM:
                str = ",:{}()[]+-/<=>&|^~*";
                break;
            case ConstantsR64.COMPILER_KICKASSEMBLER:
                str = ",;:{}()[]+-/<=>&|^~*";
                break;
        }
        return str;
    }
    /**
     * 
     * @param compiler
     * @return 
     */
    public static String getCommentString(int compiler) {
        String str = "//";
        switch (compiler) {
            case ConstantsR64.COMPILER_ACME:
            case ConstantsR64.COMPILER_64TASS:
            case ConstantsR64.COMPILER_CA65:
            case ConstantsR64.COMPILER_DREAMASS:
            case ConstantsR64.COMPILER_DASM:
                str = ";";
                break;
            case ConstantsR64.COMPILER_KICKASSEMBLER:
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
    public static String getMacroString(int compiler) {
        String str = ".";
        switch (compiler) {
            case ConstantsR64.COMPILER_ACME:
                str = "!";
                break;
            case ConstantsR64.COMPILER_DASM:
                str = "";
                break;
            case ConstantsR64.COMPILER_KICKASSEMBLER:
            case ConstantsR64.COMPILER_64TASS:
            case ConstantsR64.COMPILER_CA65:
                str = ".";
                break;
            case ConstantsR64.COMPILER_DREAMASS:
                str = "#";
                break;
        }
        return str;
    }
    /**
     * @author Guillaume Polet
     * 
     * This example was taken from
     * http://stackoverflow.com/a/10883946/2094622
     * and modified for own purposes.
     * @param type
     */
    protected void showSuggestion(int type) {
        // hide old popup
        hideSuggestion();
        try {
            // retrieve chars that have already been typed
            String macroPrefix = "";
            if (type==SUGGESTION_FUNCTION_MACRO_SCRIPT) {
                switch (getCompiler()) {
                    case ConstantsR64.COMPILER_KICKASSEMBLER:
                    case ConstantsR64.COMPILER_64TASS:
                    case ConstantsR64.COMPILER_CA65:
                        macroPrefix = ".";
                        break;
                    case ConstantsR64.COMPILER_ACME:
                        macroPrefix = "!";
                        break;
                    case ConstantsR64.COMPILER_DREAMASS:
                        macroPrefix = "#";
                        break;
                    case ConstantsR64.COMPILER_DASM:
                        break;
                }
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
                    // retrieve label list, remove last colon
                    labels = LabelExtractor.getLabelNames(suggestionSubWord.trim(), true, getBuffer().getText(), getCompiler());
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
    protected void hideSuggestion() {
        if (suggestionPopup != null) {
            suggestionPopup.setVisible(false);
            suggestionPopup=null;
        }
        // set focus back to editorpane
        requestFocusInWindow();
    }
    protected JList createSuggestionList(final Object[] labels) {
        // create list
        JList sList = new JList(labels);
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
            if (evt.getKeyCode()==java.awt.event.KeyEvent.VK_DOWN) {
                evt.consume();
                int index = Math.min(suggestionList.getSelectedIndex() + 1, suggestionList.getModel().getSize() - 1);
                suggestionList.setSelectedIndex(index);
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
        final int position = getCaretPosition()-getLineStartOffset(getCaretLine());
        String text = getLineText(getCaretLine());
        String addDelim;
        switch (getCompiler()) {
            // use colon as additional delimiter for following assemblers
            case ConstantsR64.COMPILER_ACME:
            case ConstantsR64.COMPILER_64TASS:
            case ConstantsR64.COMPILER_DREAMASS:
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
                    if (suggestionPopup!=null) suggestionPopup.setVisible(false);
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
