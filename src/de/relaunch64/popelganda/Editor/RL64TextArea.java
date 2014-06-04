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

import de.relaunch64.popelganda.assemblers.Assembler;
import de.relaunch64.popelganda.assemblers.Assemblers;
import de.relaunch64.popelganda.database.Settings;
import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.Tools;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.gjt.sp.jedit.IPropertyManager;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.Registers;
import org.gjt.sp.jedit.syntax.ModeProvider;
import org.gjt.sp.jedit.textarea.Gutter;
import org.gjt.sp.jedit.textarea.StandaloneTextArea;
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
    private Assembler assembler;
    /**
     * auto-completion popup for labels and macros etc.
     */
    private JPopupMenu suggestionPopup = null;
    private int suggestionType = -1;
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
     * Load key and editor settings on class creation. Obligatory. Personal
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
     * @param evt the KeyEvent
     */
    @Override
    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_RELEASED) {
            keyListener.keyReleased(evt);
        } else if (evt.getID() == KeyEvent.KEY_PRESSED) {
            keyListener.keyPressed(evt);
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
                suggestionType = SUGGESTION_LABEL;
                // show popup
                if (!showSuggestionPopup(suggestionType)) {
                    // if no label found, try to find functions and macros
                    suggestionType = SUGGESTION_FUNCTION_MACRO_SCRIPT;
                    showSuggestionPopup(suggestionType);
                }
            }
            // ctrl+shift+space opens macro-function-auto-completion
            else if (evt.getKeyCode()==KeyEvent.VK_SPACE && evt.isControlDown() && evt.isShiftDown() && !evt.isAltDown()) {
                suggestionType = SUGGESTION_FUNCTION_MACRO_SCRIPT;
                // show popup
                showSuggestionPopup(suggestionType);
            }
            // if popup is shown, either...
            if (suggestionPopup!=null) {
                // ...close popup on action or escape-key
                if (evt.isActionKey() || evt.getKeyCode()==KeyEvent.VK_ESCAPE) {
                    hideSuggestion();
                }
                // or filter list by typing
                else {
                    showSuggestionPopup(suggestionType);
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent evt) {
            // insert indent on enter
            // we count tabs on current line, insert enter, and indent by same amount of
            // tabs as in previous line
            if (evt.getKeyCode()==KeyEvent.VK_ENTER && !evt.isControlDown() && !evt.isShiftDown() && !evt.isAltDown()) {
                // get text of current line
                String line = getLineText(getCaretLine());
                // insert enter
                insertEnterAndIndent();
                // check if we have any content in line
                if (line!=null && !line.isEmpty()) {
                    int tabcount = 0;
                    // count tabs at line start
                    while (tabcount<line.length() && line.charAt(tabcount)=='\t') {
                        tabcount++;
                    }
                    // insert tabs according to prev line
                    while (tabcount>0) {
                        insertTabAndIndent();
                        tabcount--;
                    }
                }
                evt.consume();
            }
            // when user opens suggestion list, no item is selected. the first, initial
            // key down/up or page down/up key will select the first / last item in
            // the suggestion list then.
            if (suggestionPopup != null) {
                if (evt.isActionKey() || evt.getKeyCode()==KeyEvent.VK_ESCAPE) {
                    switch (evt.getKeyCode()) {
                        case KeyEvent.VK_DOWN:
                        case KeyEvent.VK_PAGE_DOWN:
                            evt.consume();
                            suggestionList.requestFocusInWindow();
                            suggestionList.setSelectedIndex(0);
                            break;
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_PAGE_UP:
                            evt.consume();
                            suggestionList.requestFocusInWindow();
                            suggestionList.setSelectedIndex(suggestionList.getModel().getSize()-1);
                            suggestionList.ensureIndexIsVisible(suggestionList.getModel().getSize()-1);
                            break;
                        default: break;
                    }
                }
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
     * Sets the font of the editor component and the gutter.
     * 
     * @param mf the new font
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
        // fire property change message
        propertiesChanged();
    }
    /**
     * Sets line number alignment inside gutter.
     * 
     * @param alignment the number alignment. Use one of following constants:
     * <ul>
     * <li>Gutter.LEFT</li>
     * <li>Gutter.CENTER</li>
     * <li>Gutter.RIGHT</li>
     * </ul>
     * or use {@link de.relaunch64.popelganda.database.Settings#getLineNumerAlignment() getLineNumerAlignment()}.
     */
    public final void setLineNumberAlignment(int alignment) {
        String align;
        switch (alignment) {
            case Gutter.RIGHT: align = "right"; break;
            case Gutter.CENTER: align = "center"; break;
            default: align = "left"; break;
        }
        setProperty("view.gutter.numberAlignment", align);
        // fire property change message
        propertiesChanged();
    }
    /**
     * Sets antialiasing for text.
     * 
     * @param antialias the alias style. Use one of following constants:
     * <ul>
     * <li>AntiAlias.STANDARD</li>
     * <li>AntiAlias.SUBPIXEL</li>
     * <li>AntiAlias.NONE</li>
     * </ul>
     * or use {@link de.relaunch64.popelganda.database.Settings#getAntiAlias() getAntiAlias()}.
     */
    public final void setTextAntiAlias(String antialias) {
        setProperty("view.antiAlias", antialias);
        // fire property change message
        propertiesChanged();
    }
    /**
     * Sets tab-size for editor component.
     * 
     * @param tabSize the new indent size for tabs, measured in characters
     */
    public final void setTabs(int tabSize) {
        setProperty("buffer.tabSize", String.valueOf(tabSize));
        // fire property change message
        propertiesChanged();
    }
    /**
     * Sets the compiler syntax. See {@link de.relaunch64.popelganda.assemblers.Assembler#syntaxFile() syntaxFile()} 
     * for different values. This method loads an XML file with syntax definition for keywords etc.
     */
    public final void setAssemblyMode() {
        // set syntax style
        Mode mode = new Mode("asm");
        String pathToMode = "/de/relaunch64/popelganda/resources/modes/";
        if (settings.getAlternativeAssemblyMode()) pathToMode += "alt/";
        mode.setProperty("file", pathToMode + assembler.syntaxFile());
        ModeProvider.instance.addMode(mode);
        // add mode to buffer
        getBuffer().setMode(mode);
        // fire property change message
        propertiesChanged();
    }
    /**
     * Enables code-folding.
     */
    public final void setCodeFolding() {
        setProperty("buffer.folding", "explicit");
        // fire property change message
        propertiesChanged();
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
        // set text selection and caret color
        setProperty("view.selectionColor", ColorSchemes.getColor(scheme, ColorSchemes.COLOR_SELECTION));
        setProperty("view.multipleSelectionColor", ColorSchemes.getColor(scheme, ColorSchemes.COLOR_SELECTION));
        setProperty("view.caretColor", ColorSchemes.getColor(scheme, ColorSchemes.COLOR_NORMAL));
        setProperty("view.structureHighlightColor", ColorSchemes.getColor(scheme, ColorSchemes.COLOR_KEYWORD));
        // set code folding colors
        setProperty("view.style.foldLine.1", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_NORMAL)+" bgColor:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_SELECTION)+" style:b");
        setProperty("view.style.foldLine.2", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_KEYWORD)+" bgColor:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_SELECTION));
        setProperty("view.style.foldLine.3", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMPILERKEYWORD)+" bgColor:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_SELECTION));
        setProperty("view.gutter.foldColor", ColorSchemes.getColor(scheme, ColorSchemes.LINE_HIGHLIGHT));
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
        // or if there's a specific order?
        getPainter().setStyles(SyntaxUtilities.loadStyles(mf.getFontName(), mf.getSize()));
         // fire property change message
        propertiesChanged();
   }
    /**
     * Set compiler of current editor / buffer. Frequently needed information, for various functions,
     * so we put this variable as a global field for this class.
     * 
     * @param assembler a reference to the {@link de.relaunch64.popelganda.assemblers.Assembler Assembler-class}.
     */
    public final void setAssembler(Assembler assembler) {
        this.assembler = assembler;
    }
    /**
     * Returns the current {@link de.relaunch64.popelganda.assemblers.Assembler Assembler-class} 
     * associated with this text area instance.
     * 
     * @return the current {@link de.relaunch64.popelganda.assemblers.Assembler Assembler-class}
     * for this text area.
     */
    public Assembler getAssembler() {
        return assembler;
    }
    /**
     * The StandaloneTextArea component of jEdit. Provides fast syntax highlighting and some other
     * useful features. Replaces the default Swing JEditorPane.
     * 
     * @param settings a reference to the {@link de.relaunch64.popelganda.database.Settings Settings-class}.
     */
    public RL64TextArea(Settings settings) {
        super(propertyManager);
        // save settings
        this.settings = settings;
        // set explicit buffer folding
        setCodeFolding();
        // set default compiler
        setAssembler(settings.getPreferredAssembler());
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
     * Checks whether the caret is visible and if not, scrolls editor to caret before opening
     * the suggestion popup.
     * 
     * @param type the type of suggestions for the popup. See 
     * <ul>
     * <li>{@link de.relaunch64.popelganda.Editor.RL64TextArea#SUGGESTION_LABEL SUGGESTION_LABEL}</li>
     * <li>{@link de.relaunch64.popelganda.Editor.RL64TextArea#SUGGESTION_FUNCTION_MACRO_SCRIPT SUGGESTION_FUNCTION_MACRO_SCRIPT}</li>
     * </ul>
     * Currently not supported are
     * <ul>
     * <li>{@link de.relaunch64.popelganda.Editor.RL64TextArea#SUGGESTION_FUNCTION SUGGESTION_FUNCTION}</li>
     * <li>{@link de.relaunch64.popelganda.Editor.RL64TextArea#SUGGESTION_MACRO SUGGESTION_MACRO}</li>
     * <li>{@link de.relaunch64.popelganda.Editor.RL64TextArea#SUGGESTION_FUNCTION_MACRO SUGGESTION_FUNCTION_MACRO}</li>
     * </ul>
     * 
     * @return {@code true} if either auto-completion was performed or popup displayed. {@code false}
     * if no labels have been found and no popup is displayed.
     */
    protected boolean showSuggestionPopup(final int type) {
        // check for valid type
        if (-1==type) return false;
        // check if caret is visible. if not, location is NULL, i.e. exception thrown
        if (getCaretLine()<getFirstLine() || getCaretLine()>getLastPhysicalLine()) {
            // scroll to caret
            scrollToCaret(true);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    // call suggestion popup
                    showSuggestion(type);
                }
            });
        }
        else {
            return showSuggestion(type);
        }
        return true;
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
     * @param type the type of suggestions for the popup. See 
     * <ul>
     * <li>{@link de.relaunch64.popelganda.Editor.RL64TextArea#SUGGESTION_LABEL SUGGESTION_LABEL}</li>
     * <li>{@link de.relaunch64.popelganda.Editor.RL64TextArea#SUGGESTION_FUNCTION_MACRO_SCRIPT SUGGESTION_FUNCTION_MACRO_SCRIPT}</li>
     * </ul>
     * Currently not supported are
     * <ul>
     * <li>{@link de.relaunch64.popelganda.Editor.RL64TextArea#SUGGESTION_FUNCTION SUGGESTION_FUNCTION}</li>
     * <li>{@link de.relaunch64.popelganda.Editor.RL64TextArea#SUGGESTION_MACRO SUGGESTION_MACRO}</li>
     * <li>{@link de.relaunch64.popelganda.Editor.RL64TextArea#SUGGESTION_FUNCTION_MACRO SUGGESTION_FUNCTION_MACRO}</li>
     * </ul>
     * 
     * @return {@code true} if either auto-completion was performed or popup displayed. {@code false}
     * if no labels have been found and no popup is displayed.
     */
    protected boolean showSuggestion(int type) {
        // hide old popup
        hideSuggestion();
        try {
            // retrieve chars that have already been typed
            if (type==SUGGESTION_FUNCTION_MACRO_SCRIPT) {
                String macroPrefix = getAssembler().getMacroPrefix();
                suggestionSubWord = getCaretString(false, macroPrefix);
            } else {
                suggestionSubWord = getAssembler().labelGetStart(getLineText(getCaretLine()), getCaretPosition()-getLineStartOffset(getCaretLine()));
            }
            // check for valid value
            if (null==suggestionSubWord) return false;
            // init variable
            ArrayList<String> labels = null;
            // get all labels, functions and macros of current source code
            Assembler.labelList allLabels = LabelExtractor.getLabels(getBuffer().getText(), getAssembler(), getCaretLine() + 1);
            switch(type) {
                // retrieve only functions. currently not used.
                case SUGGESTION_FUNCTION:
                    labels = LabelExtractor.getSubNames(suggestionSubWord, LabelExtractor.getNames(allLabels.functions));
                    break;
                // retrieve only macros. currently not used.
                case SUGGESTION_MACRO:
                    // we have to copy labels into new arry first because of assemvler specific
                    // macro prefix here.
                    labels = new ArrayList<>();
                    for (Iterator it = LabelExtractor.getNames(allLabels.macros).iterator(); it.hasNext();) {
                        Object i = it.next();
                        labels.add(getAssembler().getMacroPrefix() + i.toString());
                    }
                    labels = LabelExtractor.getSubNames(suggestionSubWord, labels);
                    break;
                // retrieve only labels. shown on ctrl+space
                case SUGGESTION_LABEL:
                    labels = LabelExtractor.getSubNames(suggestionSubWord, LabelExtractor.getNames(allLabels.labels));
                    break;
                // retrieve functions and macros. currently not used.
                case SUGGESTION_FUNCTION_MACRO:
                    labels = LabelExtractor.getSubNames(suggestionSubWord, LabelExtractor.getNames(allLabels.functions));
                    labels.addAll(LabelExtractor.getSubNames(suggestionSubWord, LabelExtractor.getNames(allLabels.macros)));
                    break;
                // retrieve functions, macros and script-commands. used on ctrl+shift+space
                case SUGGESTION_FUNCTION_MACRO_SCRIPT:
                    // we have to copy labels into new arry first because of assemvler specific
                    // macro prefix here.
                    labels = new ArrayList<>();
                    for (Iterator it = LabelExtractor.getNames(allLabels.macros).iterator(); it.hasNext();) {
                        Object i = it.next();
                        labels.add(getAssembler().getMacroPrefix() + i.toString());
                    }
                    // get macro names
                    labels = LabelExtractor.getSubNames(suggestionSubWord, labels);
                    // add functions
                    labels.addAll(LabelExtractor.getSubNames(suggestionSubWord, LabelExtractor.getNames(allLabels.functions)));
                    // and add script-keywords
                    labels.addAll(LabelExtractor.getSubNames(suggestionSubWord, new ArrayList(Arrays.asList(getAssembler().getScriptKeywords()))));
                    break;
            }
            // check if we have any labels
            if (null==labels || labels.size()<1) return false;
            // single suggestion, just type it in
            if (1==labels.size()) { 
                final String selectedSuggestion = labels.get(0).substring(suggestionSubWord.length());
                getBuffer().insert(getCaretPosition(), selectedSuggestion);
                return true;
            }
            // insert longest prefix
            int k = suggestionSubWord.length();
            String prefix = labels.get(0).substring(k);
            int m = prefix.length();
            // all available labels are checked for the currently typed chars
            // (suggestionSubWord). if all remaining labels have even more common
            // chars that follow the currently typed string, we do some auto-complete
            // already here. for instance, if typing ".lo" means that only labels
            // ".loop1", ".loop2" and ".loop3" remain, we can at least automatically
            // insert "op" to complete ".loop".
            // useful for users that don't like to use the popup and want to type
            // remaining chars by hand.
            for (String i : labels) {
                i = i.substring(k);
                int end = (m < i.length()) ? m : i.length();
                int j;
                for (j=0; j<end; j++) {
                    if (prefix.charAt(j) != i.charAt(j)) break;
                }
                if (m > j) m = j;
                if (0==m) break;
            }
            if (m>0) {
                final String common = prefix.substring(0, m);
                getBuffer().insert(getCaretPosition(), common);
                suggestionSubWord = suggestionSubWord + common;
            }
            // sort list...
            if (settings.getSuggestionSortIgnoresCase()) {
                // ...while ignoring case
                Collections.sort(labels, new SortIgnoreCase());
            }
            else {
                // ...or case-sensitive
                Collections.sort(labels);
            }
            // get location of caret as coordinate
            Point location = offsetToXY(getCaretPosition() - suggestionSubWord.length());
            // check if null
            if (null==location) {
                location = new Point(0, 0);
            }
            // create suggestion pupup
            suggestionPopup = new JPopupMenu();
            suggestionPopup.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            // create JList with label items
            suggestionList = createSuggestionList(labels);
            suggestionContinuedWord = suggestionSubWord;
            // check minimum length of list and add scroll pane if list is too long
            if (labels.size() > 20) {
                javax.swing.JScrollPane listScrollPane = new javax.swing.JScrollPane(suggestionList);
                listScrollPane.setBorder(BorderFactory.createEmptyBorder());
                listScrollPane.setPreferredSize(new java.awt.Dimension(suggestionList.getFixedCellWidth() + (int)listScrollPane.getVerticalScrollBar().getPreferredSize().getWidth(), 300));
                listScrollPane.setName(sugListContainerName);
                suggestionPopup.add(listScrollPane);
            }
            else {
                // else just add list w/o scroll pane
                suggestionPopup.add(suggestionList);
            }
            // show popup below typed text
            suggestionPopup.show(this, 
                                location.x + getGutter().getWidth() - suggestionPopup.getMargin().left - suggestionPopup.getBorder().getBorderInsets(suggestionPopup).left, 
                                getBaseline(0, 0) + location.y - suggestionPopup.getMargin().top - suggestionPopup.getBorder().getBorderInsets(suggestionPopup).top + getPainter().getLineHeight());
            requestFocusInWindow();
        }
        catch (IndexOutOfBoundsException ex) {
        }
        return true;
    }
    /**
     * A sorter for sorting arrays while ignoring case.
     */
    private class SortIgnoreCase implements Comparator<Object> {
        @Override
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.toLowerCase().compareTo(s2.toLowerCase());
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
    protected JList createSuggestionList(final ArrayList<String> labels) {
        // create list model
        DefaultListModel dlm = new DefaultListModel();
        String longest = "";
        for (Object l : labels) {
            String label = (String) l;
            dlm.addElement(label);
            if (label.length() > longest.length()) longest = label;
        }
        // create list from list model
        JList sList = new JList(dlm);
        sList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sList.setFont(settings.getMainFont());
        sList.setPrototypeCellValue(longest);
        sList.setFixedCellHeight(getPainter().getLineHeight());
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
                Pattern p = Pattern.compile("[a-zA-Z0-9.]+"); // my first successful regex in this code, yeah! (by Daniel)
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
     * Retrieves the string that starts before the current caret position and ends under the current caret position.
     * 
     * @param wholeWord if {@code true}, the whole word under the caret is retrieved. if {@code false}, only the
     * string from before the caret until caret position.
     * @param specialDelimiter a list of additional delimiters that have to be considered beyond the
     * default delimiter list from {@link de.relaunch64.popelganda.util.Tools#isDelimiter(java.lang.Character, java.lang.String) isDelimiter()}.
     * 
     * @return the string under the caret.
     */
    public String getCaretString(boolean wholeWord, String specialDelimiter) {
        // get caret position in line offset
        final int position = getCaretPosition()-getLineStartOffset(getCaretLine())-1;
        // get line text
        String text = getLineText(getCaretLine());
        // if empty, return empty string
        if (text.trim().isEmpty()) return "";
        String addDelim;
            // use colon as additional delimiter for following assemblers
        if (getAssembler() == Assemblers.ASM_ACME ||
            getAssembler() == Assemblers.ASM_64TASS ||
            getAssembler() == Assemblers.ASM_DREAMASS ||
            getAssembler() == Assemblers.ASM_TMPX) {
            addDelim = "\n\r:";
        } else {
            addDelim = "\n\r";
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
        // check if we want the complete word at the caret
        try {
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
                return (specialDelimiter+text.substring(start, position+1));
            }
            else {
                return text.substring(start, position+1);
            }
        }
        catch (IndexOutOfBoundsException ex) {
            return null;
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
