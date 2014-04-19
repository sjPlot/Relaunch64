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

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
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
public class SyntaxScheme {
    public static final SimpleAttributeSet DEFAULT_NORMAL;
    public static final SimpleAttributeSet DEFAULT_NUMBER;
    public static final SimpleAttributeSet DEFAULT_COMMENT;
    public static final SimpleAttributeSet DEFAULT_STRING;
    public static final SimpleAttributeSet DEFAULT_HEXA;
    public static final SimpleAttributeSet DEFAULT_BIN;
    public static final SimpleAttributeSet DEFAULT_MACRO;
    public static final SimpleAttributeSet DEFAULT_LOHI;
    public static final SimpleAttributeSet DEFAULT_JUMP;
    public static final SimpleAttributeSet DEFAULT_KEYWORD;
    public static final SimpleAttributeSet DEFAULT_COMPILER_KEYWORD;
    public static final String DEF_FONT_NAME;
    public static final int DEF_FONT_SIZE;
    public static final Color DEF_BACK_COLOR;
    
    static {
        // ******************************************
        // read syntax scheme
        // ******************************************
        File sFile = Tools.createFilePath("relaunch64-syntaxscheme.xml");
        Document syntaxFile = new Document(new Element("SyntaxScheme"));
        if (sFile.exists()) {
            try {
                SAXBuilder builder = new SAXBuilder();
                syntaxFile = builder.build(sFile);
            }
            catch (JDOMException | IOException ex) {
            }
        }
        // ******************************************
        // Set default colors
        // ******************************************
        Color cNormal = Color.BLACK;
        Color cComment = new java.awt.Color(150, 150, 150); // grey
        Color cString = new java.awt.Color(220, 0, 220); //dark pink
        Color cNumber = new java.awt.Color(160, 0, 0); // red
        Color cHexa = new java.awt.Color(0, 120, 0); // green
        Color cBin = new java.awt.Color(0, 120, 120); // cyan
        Color cJump = new java.awt.Color(206, 103, 0); // brown
        Color cMacro = new java.awt.Color(206, 103, 0); // brown
        Color cLohi = new java.awt.Color(128, 128, 0); // olive
        Color cKeywords = new java.awt.Color(0, 0, 200); // dark blue
        Color cCompilerKeywords = new java.awt.Color(70, 130, 180); // dark cyan
        // fonts & backgrund
        String fontFamily = ConstantsR64.DEFAULT_FONT;
        int fontSize = ConstantsR64.DEFAULT_FONT_SIZE;
        Color backgroundColor = ConstantsR64.DEFAULT_BACKGROUND_COLOR;
        // ******************************************
        // set user colors or init xml-file
        // ******************************************
        try {
            Element e;
            // ******************************************
            // normal color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cNormal");
            if (null==e) {
                e = new Element("cNormal");
                e.setAttribute("r", String.valueOf(cNormal.getRed()));
                e.setAttribute("g", String.valueOf(cNormal.getGreen()));
                e.setAttribute("b", String.valueOf(cNormal.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else cNormal = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                     Integer.parseInt(e.getAttributeValue("g")),
                                     Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // comment color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cComment");
            if (null==e) {
                e = new Element("cComment");
                e.setAttribute("r", String.valueOf(cComment.getRed()));
                e.setAttribute("g", String.valueOf(cComment.getGreen()));
                e.setAttribute("b", String.valueOf(cComment.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else cComment = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                      Integer.parseInt(e.getAttributeValue("g")),
                                      Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // string color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cString");
            if (null==e) {
                e = new Element("cString");
                e.setAttribute("r", String.valueOf(cString.getRed()));
                e.setAttribute("g", String.valueOf(cString.getGreen()));
                e.setAttribute("b", String.valueOf(cString.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else cString = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                     Integer.parseInt(e.getAttributeValue("g")),
                                     Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // number color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cNumber");
            if (null==e) {
                e = new Element("cNumber");
                e.setAttribute("r", String.valueOf(cNumber.getRed()));
                e.setAttribute("g", String.valueOf(cNumber.getGreen()));
                e.setAttribute("b", String.valueOf(cNumber.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else cNumber = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                     Integer.parseInt(e.getAttributeValue("g")),
                                     Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // hex color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cHexa");
            if (null==e) {
                e = new Element("cHexa");
                e.setAttribute("r", String.valueOf(cHexa.getRed()));
                e.setAttribute("g", String.valueOf(cHexa.getGreen()));
                e.setAttribute("b", String.valueOf(cHexa.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else cHexa = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                   Integer.parseInt(e.getAttributeValue("g")),
                                   Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // binary color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cBin");
            if (null==e) {
                e = new Element("cBin");
                e.setAttribute("r", String.valueOf(cBin.getRed()));
                e.setAttribute("g", String.valueOf(cBin.getGreen()));
                e.setAttribute("b", String.valueOf(cBin.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else cBin = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                  Integer.parseInt(e.getAttributeValue("g")),
                                  Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // string color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cString");
            if (null==e) {
                e = new Element("cString");
                e.setAttribute("r", String.valueOf(cString.getRed()));
                e.setAttribute("g", String.valueOf(cString.getGreen()));
                e.setAttribute("b", String.valueOf(cString.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else cString = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                     Integer.parseInt(e.getAttributeValue("g")),
                                     Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // jump color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cJump");
            if (null==e) {
                e = new Element("cJump");
                e.setAttribute("r", String.valueOf(cJump.getRed()));
                e.setAttribute("g", String.valueOf(cJump.getGreen()));
                e.setAttribute("b", String.valueOf(cJump.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else cJump = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                   Integer.parseInt(e.getAttributeValue("g")),
                                   Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // macro color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cMacro");
            if (null==e) {
                e = new Element("cMacro");
                e.setAttribute("r", String.valueOf(cMacro.getRed()));
                e.setAttribute("g", String.valueOf(cMacro.getGreen()));
                e.setAttribute("b", String.valueOf(cMacro.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else cMacro = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                    Integer.parseInt(e.getAttributeValue("g")),
                                    Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // lohi color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cLohi");
            if (null==e) {
                e = new Element("cLohi");
                e.setAttribute("r", String.valueOf(cLohi.getRed()));
                e.setAttribute("g", String.valueOf(cLohi.getGreen()));
                e.setAttribute("b", String.valueOf(cLohi.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else cLohi = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                   Integer.parseInt(e.getAttributeValue("g")),
                                   Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // keywords color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cKeywords");
            if (null==e) {
                e = new Element("cKeywords");
                e.setAttribute("r", String.valueOf(cKeywords.getRed()));
                e.setAttribute("g", String.valueOf(cKeywords.getGreen()));
                e.setAttribute("b", String.valueOf(cKeywords.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else cKeywords = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                       Integer.parseInt(e.getAttributeValue("g")),
                                       Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // comp. keywords color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cCompilerKeywords");
            if (null==e) {
                e = new Element("cCompilerKeywords");
                e.setAttribute("r", String.valueOf(cCompilerKeywords.getRed()));
                e.setAttribute("g", String.valueOf(cCompilerKeywords.getGreen()));
                e.setAttribute("b", String.valueOf(cCompilerKeywords.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else cCompilerKeywords = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                               Integer.parseInt(e.getAttributeValue("g")),
                                               Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // comp. keywords color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("backgroundColor");
            if (null==e) {
                e = new Element("backgroundColor");
                e.setAttribute("r", String.valueOf(backgroundColor.getRed()));
                e.setAttribute("g", String.valueOf(backgroundColor.getGreen()));
                e.setAttribute("b", String.valueOf(backgroundColor.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else backgroundColor = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                             Integer.parseInt(e.getAttributeValue("g")),
                                             Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // default font
            // ******************************************
            e = syntaxFile.getRootElement().getChild("fontFamily");
            if (null==e) {
                e = new Element("fontFamily");
                e.setText(fontFamily);
                syntaxFile.getRootElement().addContent(e);
            }
            else fontFamily = e.getText();
            // ******************************************
            // default font size
            // ******************************************
            e = syntaxFile.getRootElement().getChild("fontSize");
            if (null==e) {
                e = new Element("fontSize");
                e.setText(String.valueOf(fontSize));
                syntaxFile.getRootElement().addContent(e);
            }
            else fontSize = Integer.parseInt(e.getText());
        }
        catch (NumberFormatException ex) {
        }

        
        // ******************************************
        // set fonts / back color
        // ******************************************
        DEF_FONT_NAME = fontFamily;
        DEF_FONT_SIZE = fontSize;
        DEF_BACK_COLOR = backgroundColor;
        // ******************************************
        // set attribute sets
        // ******************************************
        DEFAULT_NORMAL = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_NORMAL, cNormal);
        StyleConstants.setFontFamily(DEFAULT_NORMAL, fontFamily);
        StyleConstants.setFontSize(DEFAULT_NORMAL, fontSize);
 
        DEFAULT_COMMENT = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_COMMENT, cComment);
        StyleConstants.setFontFamily(DEFAULT_COMMENT, fontFamily);
        StyleConstants.setFontSize(DEFAULT_COMMENT, fontSize);
 
        DEFAULT_STRING = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_STRING, cString); 
        StyleConstants.setFontFamily(DEFAULT_STRING, fontFamily);
        StyleConstants.setFontSize(DEFAULT_STRING, fontSize);
 
        DEFAULT_NUMBER = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_NUMBER, cNumber);
        StyleConstants.setFontFamily(DEFAULT_NUMBER, fontFamily);
        StyleConstants.setFontSize(DEFAULT_NUMBER, fontSize);
 
        DEFAULT_HEXA = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_HEXA, cHexa);
        StyleConstants.setFontFamily(DEFAULT_HEXA, fontFamily);
        StyleConstants.setFontSize(DEFAULT_HEXA, fontSize);
 
        DEFAULT_BIN = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_BIN, cBin);
        StyleConstants.setFontFamily(DEFAULT_BIN, fontFamily);
        StyleConstants.setFontSize(DEFAULT_BIN, fontSize);
 
        DEFAULT_JUMP = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_JUMP, cJump);
        StyleConstants.setFontFamily(DEFAULT_JUMP, fontFamily);
        StyleConstants.setFontSize(DEFAULT_JUMP, fontSize);
        
        DEFAULT_MACRO = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_MACRO, cMacro);
        StyleConstants.setFontFamily(DEFAULT_MACRO, fontFamily);
        StyleConstants.setFontSize(DEFAULT_MACRO, fontSize);
        
        DEFAULT_LOHI = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_LOHI, cLohi);
        StyleConstants.setFontFamily(DEFAULT_LOHI, fontFamily);
        StyleConstants.setFontSize(DEFAULT_LOHI, fontSize);
        
        //default style for new keyword types
        DEFAULT_KEYWORD = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_KEYWORD, cKeywords);
        // StyleConstants.setBold(DEFAULT_KEYWORD, true);
        StyleConstants.setFontFamily(DEFAULT_KEYWORD, fontFamily);
        StyleConstants.setFontSize(DEFAULT_KEYWORD, fontSize);
        
        //default style for new keyword types
        DEFAULT_COMPILER_KEYWORD = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_COMPILER_KEYWORD, cCompilerKeywords);
        // StyleConstants.setBold(DEFAULT_COMPILER_KEYWORD, true);
        StyleConstants.setFontFamily(DEFAULT_COMPILER_KEYWORD, fontFamily);
        StyleConstants.setFontSize(DEFAULT_COMPILER_KEYWORD, fontSize);
        
        // ******************************************
        // save syntax scheme
        // ******************************************
        OutputStream dest = null;
        try {
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            dest = new FileOutputStream(sFile);
            out.output(syntaxFile, dest);
        }
        catch (IOException ex) {
        }
        finally {
            if (dest!=null) {
                try {
                    dest.close();
                }
                catch (IOException ex) {
                }
            }
        }
    }

    
    public SyntaxScheme() {
    }
    /**
     * This method initializes all mutable attribute sets for the different highlight tokens
     * (normal text, comments, strings, numbers etc. and how they should be highlighted).
     * Can be used as StylesDocument for jEditorPanes.
     * @return A HashMap containing all MutableAttributeSets for the StyledDocument property of
     * jEditorPanes to enable Syntax Highlighting.
     */
    public static HashMap<String, MutableAttributeSet> getStyleAttributes() {
        final HashMap<String, MutableAttributeSet> sasArray = new HashMap<>();
        sasArray.put(ConstantsR64.STRING_NORMAL, DEFAULT_NORMAL);
        sasArray.put(ConstantsR64.STRING_COMMENT, DEFAULT_COMMENT);
        sasArray.put(ConstantsR64.STRING_STRING, DEFAULT_STRING);
        sasArray.put(ConstantsR64.STRING_NUMBER, DEFAULT_NUMBER);
        sasArray.put(ConstantsR64.STRING_HEXA, DEFAULT_HEXA);
        sasArray.put(ConstantsR64.STRING_LOHI, DEFAULT_LOHI);
        sasArray.put(ConstantsR64.STRING_BIN, DEFAULT_BIN);
        sasArray.put(ConstantsR64.STRING_JUMP, DEFAULT_JUMP);
        sasArray.put(ConstantsR64.STRING_MACRO, DEFAULT_MACRO);
        sasArray.put(ConstantsR64.STRING_KEYWORD, DEFAULT_KEYWORD);
        sasArray.put(ConstantsR64.STRING_COMPILER_KEYWORD, DEFAULT_COMPILER_KEYWORD);
        return sasArray;
    }
    public static String getFontName() {
        return DEF_FONT_NAME;
    }
    public static int getFontSize() {
        return DEF_FONT_SIZE;
    }
    public static Color getBackgroundColor() {
        return DEF_BACK_COLOR;
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
            case ConstantsR64.COMPILER_KICKASSEMBLER:
                str = ".";
                break;
        }
        return str;
    }
    /**
     * 
     * @param compiler
     * @return 
     */
    public static HashMap<String, MutableAttributeSet> getKeywordHashMap(int compiler) {
        final HashMap<String, MutableAttributeSet> asmKeywords = new HashMap<>();
        asmKeywords.put("LDA", DEFAULT_KEYWORD);
        asmKeywords.put("STA", DEFAULT_KEYWORD);
        asmKeywords.put("INC", DEFAULT_KEYWORD);
        asmKeywords.put("DEC", DEFAULT_KEYWORD);
        asmKeywords.put("LDX", DEFAULT_KEYWORD);
        asmKeywords.put("STX", DEFAULT_KEYWORD);
        asmKeywords.put("INX", DEFAULT_KEYWORD);
        asmKeywords.put("DEX", DEFAULT_KEYWORD);
        asmKeywords.put("LDY", DEFAULT_KEYWORD);
        asmKeywords.put("STY", DEFAULT_KEYWORD);
        asmKeywords.put("INY", DEFAULT_KEYWORD);
        asmKeywords.put("DEY", DEFAULT_KEYWORD);
        asmKeywords.put("CMP", DEFAULT_KEYWORD);
        asmKeywords.put("CPX", DEFAULT_KEYWORD);
        asmKeywords.put("CPY", DEFAULT_KEYWORD);
        asmKeywords.put("TAX", DEFAULT_KEYWORD);
        asmKeywords.put("TAY", DEFAULT_KEYWORD);
        asmKeywords.put("TXA", DEFAULT_KEYWORD);
        asmKeywords.put("TYA", DEFAULT_KEYWORD);
        asmKeywords.put("TSX", DEFAULT_KEYWORD);
        asmKeywords.put("TXS", DEFAULT_KEYWORD);
        asmKeywords.put("AND", DEFAULT_KEYWORD);
        asmKeywords.put("ORA", DEFAULT_KEYWORD);
        asmKeywords.put("EOR", DEFAULT_KEYWORD);
        asmKeywords.put("ADC", DEFAULT_KEYWORD);
        asmKeywords.put("SBC", DEFAULT_KEYWORD);
        asmKeywords.put("SEC", DEFAULT_KEYWORD);
        asmKeywords.put("CLC", DEFAULT_KEYWORD);
        asmKeywords.put("ASL", DEFAULT_KEYWORD);
        asmKeywords.put("LSR", DEFAULT_KEYWORD);
        asmKeywords.put("ROL", DEFAULT_KEYWORD);
        asmKeywords.put("ROR", DEFAULT_KEYWORD);
        asmKeywords.put("BIT", DEFAULT_KEYWORD);
        asmKeywords.put("JMP", DEFAULT_KEYWORD);
        asmKeywords.put("JSR", DEFAULT_KEYWORD);
        asmKeywords.put("RTS", DEFAULT_KEYWORD);
        asmKeywords.put("RTI", DEFAULT_KEYWORD);
        asmKeywords.put("BCC", DEFAULT_KEYWORD);
        asmKeywords.put("BCS", DEFAULT_KEYWORD);
        asmKeywords.put("BEQ", DEFAULT_KEYWORD);
        asmKeywords.put("BNE", DEFAULT_KEYWORD);
        asmKeywords.put("BPL", DEFAULT_KEYWORD);
        asmKeywords.put("BMI", DEFAULT_KEYWORD);
        asmKeywords.put("BVC", DEFAULT_KEYWORD);
        asmKeywords.put("BVS", DEFAULT_KEYWORD);
        asmKeywords.put("SEI", DEFAULT_KEYWORD);
        asmKeywords.put("CLI", DEFAULT_KEYWORD);
        asmKeywords.put("SED", DEFAULT_KEYWORD);
        asmKeywords.put("CLS", DEFAULT_KEYWORD);
        asmKeywords.put("PHA", DEFAULT_KEYWORD);
        asmKeywords.put("PLA", DEFAULT_KEYWORD);
        asmKeywords.put("PHP", DEFAULT_KEYWORD);
        asmKeywords.put("PLP", DEFAULT_KEYWORD);
        asmKeywords.put("NOP", DEFAULT_KEYWORD);
        asmKeywords.put("BRK", DEFAULT_KEYWORD);
        return asmKeywords;
    }    
    /**
     * 
     * @param compiler
     * @return 
     */
    public static HashMap<String, MutableAttributeSet> getCompilerKeywordHashMap(int compiler) {
        final HashMap<String, MutableAttributeSet> asmKeywords = new HashMap<>();
        switch (compiler) {
            case ConstantsR64.COMPILER_ACME:
                asmKeywords.put("!08", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!8", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!16", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!24", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!32", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!ALIGN", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!AL", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!AS", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!BIN", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!BINARY", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!BY", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!BYTE", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!CONVTAB", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!CT", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!DO", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!EOF", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!ENDOFFILE", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!ERROR", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!IF", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!IFDEF", DEFAULT_COMPILER_KEYWORD);                
                asmKeywords.put("!INITMEN", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!FI", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!FILL", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!FOR", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!MACRO", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!PET", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!PSEUDOPC", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!RAW", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!RL", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!RS", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!SCR", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!SCRXOR", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!SET", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!SL", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!SRC", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!SOURCE", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!TEXT", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!TO", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!TX", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!WARN", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!WO", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!WORD", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!ZN", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("!ZONE", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put("UNTIL", DEFAULT_COMPILER_KEYWORD);
                break;
            case ConstantsR64.COMPILER_KICKASSEMBLER:
                asmKeywords.put(".ALIGN", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".ASSERT", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".BYTE", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".CONST", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".DEFINE", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".DWORD", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".ENUM", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".EVAL", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".FILL", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".FOR", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".FUNCTION", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".GET", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".IF", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".IMPORT", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".IMPORTONCE", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".LABEL", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".MACRO", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".PC", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".PRINT", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".PSEUDOPC", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".TEXT", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".VAR", DEFAULT_COMPILER_KEYWORD);
                asmKeywords.put(".WORD", DEFAULT_COMPILER_KEYWORD);
                break;
        }
        return asmKeywords;
    }    
}
