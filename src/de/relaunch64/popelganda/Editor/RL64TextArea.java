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
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import org.gjt.sp.jedit.IPropertyManager;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.syntax.ModeProvider;
import org.gjt.sp.jedit.textarea.AntiAlias;
import org.gjt.sp.jedit.textarea.StandaloneTextArea;
import org.gjt.sp.util.IOUtilities;
import org.gjt.sp.util.SyntaxUtilities;

/**
 * 
 * @author Soci/Singular
 */
public class RL64TextArea extends StandaloneTextArea {
    final static Properties props;
    static IPropertyManager propertyManager;

    static {
        props = new Properties();
        props.putAll(loadProperties("/de/relaunch64/popelganda/resources/jedit_keys.props"));
        props.putAll(loadProperties("/de/relaunch64/popelganda/resources/jedit.props"));
        propertyManager = new IPropertyManager() {
            @Override
            public String getProperty(String name) {
                return props.getProperty(name);
            }
        };
    }

    public void setProperty(String name, String val) {
        props.setProperty(name, val);
    }

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
            IOUtilities.closeQuietly(in);
        }
        return loadedProps;
    }

    public final void setFonts(Settings settings) {
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
        setLineNumberAlignment(settings);
        // set fonts
        setFont(mf);
        getPainter().setFont(mf);
        getGutter().setFont(mf);
        // set antialias
        setTextAntiAlias(AntiAlias.STANDARD, settings);
        // getPainter().setTextAntiAlias(new AntiAlias(getProperty("view.antiAlias")));
        getPainter().setStyles(SyntaxUtilities.loadStyles(mf.getFontName(), mf.getSize()));
    }

    public final void setLineNumberAlignment(Settings settings) {
        // set line number alignment
        getGutter().setLineNumberAlignment(settings.getLineNumerAlignment());
    }
    
    public final void setTextAntiAlias(String aliasstyle, Settings settings) {
        // set default font
        Font mf = settings.getMainFont();
        // set antialias
        setProperty("view.antiAlias", "true");
        getPainter().setAntiAlias(new AntiAlias(aliasstyle));
        // getPainter().setTextAntiAlias(new AntiAlias(getProperty("view.antiAlias")));
        getPainter().setStyles(SyntaxUtilities.loadStyles(mf.getFontName(), mf.getSize()));
    }
    
    public final void setTabs(Settings settings) {
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
        mode.setProperty("file", ConstantsR64.assemblymodes[compiler]);
        ModeProvider.instance.addMode(mode);
        // add mode to buffer
        getBuffer().setMode(mode);
    }
    
    public final void setSyntaxScheme(Settings settings, int scheme) {
        // syntax colors for editor
        setProperty("view.fgColor", ColorSchemes.getColor(scheme, ColorSchemes.COLOR_NORMAL));
        setProperty("view.bgColor", ColorSchemes.getColor(scheme, ColorSchemes.BACKGROUND));
        setProperty("view.style.comment1", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMMENT));
        setProperty("view.style.comment2", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMMENT));
        setProperty("view.style.comment3", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMMENT));
        setProperty("view.style.comment4", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMMENT));
        setProperty("view.style.digit", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_NUMBER));
        setProperty("view.style.literal3", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_BIN));
        setProperty("view.style.literal4", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_HEX));
        setProperty("view.style.function", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_SCRIPTKEYWORD));
        setProperty("view.style.keyword1", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_KEYWORD));
        setProperty("view.style.keyword2", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_ILLEGALOPCODE));
        setProperty("view.style.keyword3", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMPILERKEYWORD));
        setProperty("view.style.literal1", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_STRING));
        setProperty("view.style.literal2", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_STRING));
        setProperty("view.style.label", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_JUMP));
        setProperty("view.style.keyword4", "color:"+ColorSchemes.getColor(scheme, ColorSchemes.COLOR_JUMP));
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
        getPainter().setStyles(SyntaxUtilities.loadStyles(mf.getFontName(), mf.getSize()));
        // set for- and background color of text area
        getPainter().setBackground(SyntaxUtilities.parseColor(ColorSchemes.getColor(scheme, ColorSchemes.BACKGROUND), Color.black));
        getPainter().setForeground(SyntaxUtilities.parseColor(ColorSchemes.getColor(scheme, ColorSchemes.COLOR_NORMAL), Color.black));
        // TODO gutter linenumber background not shown correctly
        // set for- and background color of line numbers
        getGutter().setBackground(SyntaxUtilities.parseColor(ColorSchemes.getColor(scheme, ColorSchemes.LINE_BACKGROUND), Color.black));
        getGutter().setForeground(SyntaxUtilities.parseColor(ColorSchemes.getColor(scheme, ColorSchemes.LINE_COLOR), Color.black));
        getGutter().setHighlightedForeground(SyntaxUtilities.parseColor(ColorSchemes.getColor(scheme, ColorSchemes.LINE_COLOR), Color.black));
        getGutter().setCurrentLineForeground(SyntaxUtilities.parseColor(ColorSchemes.getColor(scheme, ColorSchemes.LINE_HIGHLIGHT), Color.black));
        Color bc = SyntaxUtilities.parseColor(ColorSchemes.getColor(scheme, ColorSchemes.LINE_BORDER), Color.black);
        getGutter().setBorder(1, bc, bc, bc);
    }
    
//    public RL64TextArea() {
//        super(propertyManager);
//    }
    public RL64TextArea(Settings settings) {
        super(propertyManager);
        // set syntaxscheme
        setSyntaxScheme(settings, ColorSchemes.SCHEME_DEFAULT);
        // set tab width
        setTabs(settings);
        // set fonts
        setFonts(settings);
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
}
