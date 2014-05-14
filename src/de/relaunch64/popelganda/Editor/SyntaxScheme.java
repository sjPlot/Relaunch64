/*
 * Relaunch64 - A Java Crossassembler for C64 machine language coding.
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

package de.relaunch64.popelganda.Editor;

import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.FileTools;
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
    public static final SimpleAttributeSet DEFAULT_LOHI;
    public static final SimpleAttributeSet DEFAULT_JUMP;
    public static final SimpleAttributeSet DEFAULT_KEYWORD;
    public static final SimpleAttributeSet DEFAULT_COMPILER_KEYWORD;
    public static final SimpleAttributeSet DEFAULT_SCRIPT_KEYWORD;
    public static final SimpleAttributeSet DEFAULT_ILLEGAL_OPCODE;
    public static final String DEF_FONT_NAME;
    public static final int DEF_FONT_SIZE;
    public static final Color DEF_BACK_COLOR;
    public static final Color DEF_LINE_BACK_COLOR;
    public static final Color DEF_LINE_COLOR;
    public static final Color DEF_LINE_BORDER_COLOR;
    public static final Color DEF_LINE_HIGHLIGHT_COLOR;
    
    static {
        // ******************************************
        // read syntax scheme
        // ******************************************
        Document syntaxFile = loadSyntax();
        // ******************************************
        // Set default colors
        // ******************************************
        int scheme = ColorSchemes.SCHEME_DEFAULT;
        Color cNormal = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_NORMAL);
        Color cComment = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMMENT);
        Color cString = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_STRING);
        Color cNumber = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_NUMBER);
        Color cHexa = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_HEX);
        Color cBin = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_BIN);
        Color cJump = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_JUMP);
        Color cLohi = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_LOHI);
        Color cKeywords = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_KEYWORD);
        Color cCompilerKeywords = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMPILERKEYWORD);
        Color cScriptKeywords = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_SCRIPTKEYWORD);
        Color cIllegalOpcodes = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_ILLEGALOPCODE);
        Color backgroundColor = ColorSchemes.getColor(scheme, ColorSchemes.BACKGROUND);
        Color lineBackground = ColorSchemes.getColor(scheme, ColorSchemes.LINE_BACKGROUND);
        Color lineBorder = ColorSchemes.getColor(scheme, ColorSchemes.LINE_BORDER);
        Color lineColor = ColorSchemes.getColor(scheme, ColorSchemes.LINE_COLOR);
        Color lineHighlight = ColorSchemes.getColor(scheme, ColorSchemes.LINE_HIGHLIGHT);
        // fonts & backgrund
        String fontFamily = ConstantsR64.DEFAULT_FONT;
        int fontSize = ConstantsR64.DEFAULT_FONT_SIZE;
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
            // script keywords color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cScriptKeywords");
            if (null==e) {
                e = new Element("cScriptKeywords");
                e.setAttribute("r", String.valueOf(cScriptKeywords.getRed()));
                e.setAttribute("g", String.valueOf(cScriptKeywords.getGreen()));
                e.setAttribute("b", String.valueOf(cScriptKeywords.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else cScriptKeywords = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                             Integer.parseInt(e.getAttributeValue("g")),
                                             Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // illegal opcodes
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cIllegalOpcodes");
            if (null==e) {
                e = new Element("cIllegalOpcodes");
                e.setAttribute("r", String.valueOf(cIllegalOpcodes.getRed()));
                e.setAttribute("g", String.valueOf(cIllegalOpcodes.getGreen()));
                e.setAttribute("b", String.valueOf(cIllegalOpcodes.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else cIllegalOpcodes = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                             Integer.parseInt(e.getAttributeValue("g")),
                                             Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // background color
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
            // line numbers background color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("lineBackground");
            if (null==e) {
                e = new Element("lineBackground");
                e.setAttribute("r", String.valueOf(lineBackground.getRed()));
                e.setAttribute("g", String.valueOf(lineBackground.getGreen()));
                e.setAttribute("b", String.valueOf(lineBackground.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else lineBackground = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                             Integer.parseInt(e.getAttributeValue("g")),
                                             Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // line numbers border color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("lineBorder");
            if (null==e) {
                e = new Element("lineBorder");
                e.setAttribute("r", String.valueOf(lineBorder.getRed()));
                e.setAttribute("g", String.valueOf(lineBorder.getGreen()));
                e.setAttribute("b", String.valueOf(lineBorder.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else lineBorder = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                             Integer.parseInt(e.getAttributeValue("g")),
                                             Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // line numbers foreground color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("lineColor");
            if (null==e) {
                e = new Element("lineColor");
                e.setAttribute("r", String.valueOf(lineColor.getRed()));
                e.setAttribute("g", String.valueOf(lineColor.getGreen()));
                e.setAttribute("b", String.valueOf(lineColor.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else lineColor = new Color(Integer.parseInt(e.getAttributeValue("r")),
                                             Integer.parseInt(e.getAttributeValue("g")),
                                             Integer.parseInt(e.getAttributeValue("b")));
            // ******************************************
            // line numbers highlight color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("lineHighlight");
            if (null==e) {
                e = new Element("lineHighlight");
                e.setAttribute("r", String.valueOf(lineHighlight.getRed()));
                e.setAttribute("g", String.valueOf(lineHighlight.getGreen()));
                e.setAttribute("b", String.valueOf(lineHighlight.getBlue()));
                syntaxFile.getRootElement().addContent(e);
            }
            else lineHighlight = new Color(Integer.parseInt(e.getAttributeValue("r")),
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
        DEF_LINE_BACK_COLOR = lineBackground;
        DEF_LINE_BORDER_COLOR = lineBorder;
        DEF_LINE_COLOR = lineColor;
        DEF_LINE_HIGHLIGHT_COLOR = lineHighlight;
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
        
        //default style for new script keyword types
        DEFAULT_SCRIPT_KEYWORD = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_SCRIPT_KEYWORD, cScriptKeywords);
        StyleConstants.setFontFamily(DEFAULT_SCRIPT_KEYWORD, fontFamily);
        StyleConstants.setFontSize(DEFAULT_SCRIPT_KEYWORD, fontSize);
        
        //default style for new keyword types
        DEFAULT_ILLEGAL_OPCODE = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_ILLEGAL_OPCODE, cIllegalOpcodes);
        // StyleConstants.setBold(DEFAULT_KEYWORD, true);
        StyleConstants.setFontFamily(DEFAULT_ILLEGAL_OPCODE, fontFamily);
        StyleConstants.setFontSize(DEFAULT_ILLEGAL_OPCODE, fontSize);
        
        // ******************************************
        // save syntax scheme
        // ******************************************
        saveSyntax(syntaxFile);
    }

    
    public SyntaxScheme() {
    }
    public static void changeScheme(int scheme) {
        // ******************************************
        // read syntax scheme
        // ******************************************
        Document syntaxFile = loadSyntax();
        changeScheme(scheme, syntaxFile);
        // ******************************************
        // save syntax scheme
        // ******************************************
        saveSyntax(syntaxFile);
    }
    public static void changeScheme(int scheme, Document syntaxFile) {
        // ******************************************
        // Set default colors
        // ******************************************
        Color cNormal = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_NORMAL);
        Color cComment = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMMENT);
        Color cString = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_STRING);
        Color cNumber = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_NUMBER);
        Color cHexa = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_HEX);
        Color cBin = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_BIN);
        Color cJump = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_JUMP);
        Color cLohi = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_LOHI);
        Color cKeywords = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_KEYWORD);
        Color cCompilerKeywords = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_COMPILERKEYWORD);
        Color cScriptKeywords = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_SCRIPTKEYWORD);
        Color cIllegalOpcodes = ColorSchemes.getColor(scheme, ColorSchemes.COLOR_ILLEGALOPCODE);
        Color backgroundColor = ColorSchemes.getColor(scheme, ColorSchemes.BACKGROUND);
        Color lineBackground = ColorSchemes.getColor(scheme, ColorSchemes.LINE_BACKGROUND);
        Color lineBorder = ColorSchemes.getColor(scheme, ColorSchemes.LINE_BORDER);
        Color lineColor = ColorSchemes.getColor(scheme, ColorSchemes.LINE_COLOR);
        Color lineHighlight = ColorSchemes.getColor(scheme, ColorSchemes.LINE_HIGHLIGHT);
        // ******************************************
        // set user colors or init xml-file
        // ******************************************
        try {
            Element e;
            // ******************************************
            // normal color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cNormal");
            e.setAttribute("r", String.valueOf(cNormal.getRed()));
            e.setAttribute("g", String.valueOf(cNormal.getGreen()));
            e.setAttribute("b", String.valueOf(cNormal.getBlue()));
            // ******************************************
            // comment color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cComment");
            e.setAttribute("r", String.valueOf(cComment.getRed()));
            e.setAttribute("g", String.valueOf(cComment.getGreen()));
            e.setAttribute("b", String.valueOf(cComment.getBlue()));
            // ******************************************
            // string color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cString");
            e.setAttribute("r", String.valueOf(cString.getRed()));
            e.setAttribute("g", String.valueOf(cString.getGreen()));
            e.setAttribute("b", String.valueOf(cString.getBlue()));
            // ******************************************
            // number color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cNumber");
            e.setAttribute("r", String.valueOf(cNumber.getRed()));
            e.setAttribute("g", String.valueOf(cNumber.getGreen()));
            e.setAttribute("b", String.valueOf(cNumber.getBlue()));
            // ******************************************
            // hex color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cHexa");
            e.setAttribute("r", String.valueOf(cHexa.getRed()));
            e.setAttribute("g", String.valueOf(cHexa.getGreen()));
            e.setAttribute("b", String.valueOf(cHexa.getBlue()));
            // ******************************************
            // binary color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cBin");
            e.setAttribute("r", String.valueOf(cBin.getRed()));
            e.setAttribute("g", String.valueOf(cBin.getGreen()));
            e.setAttribute("b", String.valueOf(cBin.getBlue()));
            // ******************************************
            // string color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cString");
            e.setAttribute("r", String.valueOf(cString.getRed()));
            e.setAttribute("g", String.valueOf(cString.getGreen()));
            e.setAttribute("b", String.valueOf(cString.getBlue()));
            // ******************************************
            // jump color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cJump");
            e.setAttribute("r", String.valueOf(cJump.getRed()));
            e.setAttribute("g", String.valueOf(cJump.getGreen()));
            e.setAttribute("b", String.valueOf(cJump.getBlue()));
            // ******************************************
            // lohi color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cLohi");
            e.setAttribute("r", String.valueOf(cLohi.getRed()));
            e.setAttribute("g", String.valueOf(cLohi.getGreen()));
            e.setAttribute("b", String.valueOf(cLohi.getBlue()));
            // ******************************************
            // keywords color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cKeywords");
            e.setAttribute("r", String.valueOf(cKeywords.getRed()));
            e.setAttribute("g", String.valueOf(cKeywords.getGreen()));
            e.setAttribute("b", String.valueOf(cKeywords.getBlue()));
            // ******************************************
            // comp. keywords color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cCompilerKeywords");
            e.setAttribute("r", String.valueOf(cCompilerKeywords.getRed()));
            e.setAttribute("g", String.valueOf(cCompilerKeywords.getGreen()));
            e.setAttribute("b", String.valueOf(cCompilerKeywords.getBlue()));
            // ******************************************
            // script keywords color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cScriptKeywords");
            e.setAttribute("r", String.valueOf(cScriptKeywords.getRed()));
            e.setAttribute("g", String.valueOf(cScriptKeywords.getGreen()));
            e.setAttribute("b", String.valueOf(cScriptKeywords.getBlue()));
            // ******************************************
            // illegal opcodes
            // ******************************************
            e = syntaxFile.getRootElement().getChild("cIllegalOpcodes");
            e.setAttribute("r", String.valueOf(cIllegalOpcodes.getRed()));
            e.setAttribute("g", String.valueOf(cIllegalOpcodes.getGreen()));
            e.setAttribute("b", String.valueOf(cIllegalOpcodes.getBlue()));
            // ******************************************
            // background color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("backgroundColor");
            e.setAttribute("r", String.valueOf(backgroundColor.getRed()));
            e.setAttribute("g", String.valueOf(backgroundColor.getGreen()));
            e.setAttribute("b", String.valueOf(backgroundColor.getBlue()));
            // ******************************************
            // line numbers background color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("lineBackground");
            e.setAttribute("r", String.valueOf(lineBackground.getRed()));
            e.setAttribute("g", String.valueOf(lineBackground.getGreen()));
            e.setAttribute("b", String.valueOf(lineBackground.getBlue()));
            // ******************************************
            // line numbers border color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("lineBorder");
            e.setAttribute("r", String.valueOf(lineBorder.getRed()));
            e.setAttribute("g", String.valueOf(lineBorder.getGreen()));
            e.setAttribute("b", String.valueOf(lineBorder.getBlue()));
            // ******************************************
            // line numbers foreground color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("lineColor");
            e.setAttribute("r", String.valueOf(lineColor.getRed()));
            e.setAttribute("g", String.valueOf(lineColor.getGreen()));
            e.setAttribute("b", String.valueOf(lineColor.getBlue()));
            // ******************************************
            // line numbers highlight color
            // ******************************************
            e = syntaxFile.getRootElement().getChild("lineHighlight");
            e.setAttribute("r", String.valueOf(lineHighlight.getRed()));
            e.setAttribute("g", String.valueOf(lineHighlight.getGreen()));
            e.setAttribute("b", String.valueOf(lineHighlight.getBlue()));
        }
        catch (NumberFormatException ex) {
        }
    }
    
    protected static File getFile() {
        return FileTools.createFilePath("relaunch64-syntaxscheme.xml");
    }
    public static Document loadSyntax() {
        File sFile = getFile();
        Document syntaxFile = new Document(new Element("SyntaxScheme"));
        if (sFile.exists()) {
            try {
                SAXBuilder builder = new SAXBuilder();
                syntaxFile = builder.build(sFile);
            }
            catch (JDOMException | IOException ex) {
            }
        }
        return syntaxFile;
    }
    public static boolean saveSyntax(Document syntaxFile) {
        File sFile = getFile();
        OutputStream dest = null;
        try {
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            dest = new FileOutputStream(sFile);
            out.output(syntaxFile, dest);
        }
        catch (IOException ex) {
            return false;
        }
        finally {
            if (dest!=null) {
                try {
                    dest.close();
                }
                catch (IOException ex) {
                    return false;
                }
            }
        }
        return true;
    }
    public static void setFont(Document syntaxFile, String fontFamily) {
        Element e = syntaxFile.getRootElement().getChild("fontFamily");
        if (null==e) {
            e = new Element("fontFamily");
            syntaxFile.getRootElement().addContent(e);
        }
        e.setText(fontFamily);
    }
    public static void setFontSize(Document syntaxFile, int fontSize) {
        Element e = syntaxFile.getRootElement().getChild("fontSize");
        if (null==e) {
            e = new Element("fontSize");
            syntaxFile.getRootElement().addContent(e);
        }
        e.setText(String.valueOf(fontSize));
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
        sasArray.put(ConstantsR64.STRING_KEYWORD, DEFAULT_KEYWORD);
        sasArray.put(ConstantsR64.STRING_COMPILER_KEYWORD, DEFAULT_COMPILER_KEYWORD);
        sasArray.put(ConstantsR64.STRING_SCRIPT_KEYWORD, DEFAULT_SCRIPT_KEYWORD);
        sasArray.put(ConstantsR64.STRING_ILLEGAL_OPCODE, DEFAULT_ILLEGAL_OPCODE);
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
    public static Color getLineBackgroundColor() {
        return DEF_LINE_BACK_COLOR;
    }
    public static Color getLineForegroundColor() {
        return DEF_LINE_COLOR;
    }
    public static Color getLineBorderColor() {
        return DEF_LINE_BORDER_COLOR;
    }
    public static Color getLineHighlightColor() {
        return DEF_LINE_HIGHLIGHT_COLOR;
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
            case ConstantsR64.COMPILER_64TASS:
            case ConstantsR64.COMPILER_CA65:
                str = ",:{}()[]+-/<=>&|^~*";
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
            case ConstantsR64.COMPILER_64TASS:
            case ConstantsR64.COMPILER_CA65:
                str = ".";
                break;
        }
        return str;
    }
    /**
     * 
     * @return 
     */
    public static HashMap<String, MutableAttributeSet> getKeywordHashMap() {
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
        asmKeywords.put("CLD", DEFAULT_KEYWORD);
        asmKeywords.put("CLV", DEFAULT_KEYWORD);
        asmKeywords.put("PHA", DEFAULT_KEYWORD);
        asmKeywords.put("PLA", DEFAULT_KEYWORD);
        asmKeywords.put("PHP", DEFAULT_KEYWORD);
        asmKeywords.put("PLP", DEFAULT_KEYWORD);
        asmKeywords.put("NOP", DEFAULT_KEYWORD);
        asmKeywords.put("BRK", DEFAULT_KEYWORD);
        return asmKeywords;
    }    
    public static HashMap<String, MutableAttributeSet> getIllegalOpcodeHashMap() {
        final HashMap<String, MutableAttributeSet> asmKeywords = new HashMap<>();
        asmKeywords.put("AAR", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("AHX", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("ALR", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("ANC", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("AXS", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("DCP", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("ISC", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("LAS", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("LAX", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("RLA", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("RRA", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("SAX", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("SBC", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("SHX", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("SHY", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("SLO", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("SRE", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("TAS", DEFAULT_ILLEGAL_OPCODE);
        asmKeywords.put("XAA", DEFAULT_ILLEGAL_OPCODE);
        return asmKeywords;
    }    
    /**
     * 
     * @param compiler
     * @return 
     */
    public static HashMap<String, MutableAttributeSet> getCompilerKeywordHashMap(int compiler) {
        final HashMap<String, MutableAttributeSet> compilerKeywords = new HashMap<>();
        switch (compiler) {
            case ConstantsR64.COMPILER_ACME:
                compilerKeywords.put("!08", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!8", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!16", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!24", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!32", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!ALIGN", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!AL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!AS", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!BIN", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!BINARY", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!BY", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!BYTE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!CONVTAB", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!CT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!DO", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!EOF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!ENDOFFILE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!ERROR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!IF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!IFDEF", DEFAULT_COMPILER_KEYWORD);                
                compilerKeywords.put("!INITMEN", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!FI", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!FILL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!FOR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!MACRO", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!PET", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!PSEUDOPC", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!RAW", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!RL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!RS", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!SCR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!SCRXOR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!SET", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!SL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!SRC", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!SOURCE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!TEXT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!TO", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!TX", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!WARN", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!WO", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!WORD", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!ZN", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("!ZONE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put("UNTIL", DEFAULT_COMPILER_KEYWORD);
                break;
            case ConstantsR64.COMPILER_KICKASSEMBLER:
                compilerKeywords.put(".ALIGN", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ASSERT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".BYTE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".CONST", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".DEFINE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".DWORD", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENUM", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".EVAL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".FILL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".FOR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".FUNCTION", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IMPORT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IMPORTONCE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".LABEL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".MACRO", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".PC", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".PRINT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".PSEUDOPC", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".RETURN", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".TEXT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".VAR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".WORD", DEFAULT_COMPILER_KEYWORD);
                break;
            case ConstantsR64.COMPILER_64TASS:
                compilerKeywords.put(".AL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ALIGN", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".AS", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ASSERT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".BEND", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".BINARY", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".BINCLUDE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".BLOCK", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".BREAK", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".BYTE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".CASE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".CDEF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".CERROR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".CHAR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".CHECK", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".COMMENT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".CONTINUE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".CPU", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".CWARN", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".DATABANK", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".DEFAULT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".DINT", DEFAULT_COMPILER_KEYWORD);                
                compilerKeywords.put(".DPAGE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".DSECTION", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".DSTRUCT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".DUNION", DEFAULT_COMPILER_KEYWORD);                
                compilerKeywords.put(".DWORD", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".EDEF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ELSE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ELSIF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENC", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".END", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDC", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDIF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDM", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDP", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDS", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDSWITCH", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDU", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDWEAK", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".EOR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ERROR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".FI", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".FILL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".FOR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".FUNCTION", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".GOTO", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".HERE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".HIDEMAC", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IFEQ", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IFMI", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IFNE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IFPL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".INCLUDE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".INT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".LBL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".LINT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".LOGICAL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".LONG", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".MACRO", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".NEXT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".NULL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".OFFS", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".OPTION", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".PAGE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".PEND", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".PROC", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".PROFF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".PRON", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".PRTEXT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".REPT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".RTA", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".SECTION", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".SEGMENT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".SEND", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".SHIFT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".SHIFTL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".SHOWMAC", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".STRUCT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".SWITCH", DEFAULT_COMPILER_KEYWORD);                
                compilerKeywords.put(".TEXT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".UNION", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".VAR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".WARN", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".WEAK", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".WORD", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".XL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".XS", DEFAULT_COMPILER_KEYWORD);
                break;
            // TODO ergänzen
            case ConstantsR64.COMPILER_CA65:
                compilerKeywords.put(".A16", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".A8", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ADDR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ALIGN", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ASCIIZ", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ASSERT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".AUTOIMPORT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".BANKBYTES", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".BSS", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".BYT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".BYTE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".CASE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".CHARMAP", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".CODE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".CONDES", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".CONSTRUCTOR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".DATA", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".DBYT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".DEBUGINFO", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".DEF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".DEFINED", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".DESTRUCTOR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".DWORD", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ELSE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ELSIF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".END", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDENUM", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDIF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDMAC", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDMACRO", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDPROC", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDREP", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDREPEAT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDSCOPE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENDSTRUCT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ENUM", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ERROR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".EXITMAC", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".EXITMACRO", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".EXPORT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".EXPORTZP", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".FARADDR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".FEATURE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".FILEOPT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".FOPT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".FORCEIMPORT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".GLOBAL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".GLOBALZP", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".HIBYTES", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".I16", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".I8", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IFBLANK", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IFCONST", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IFDEF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IFNBLANK", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IFNDEF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IFNREF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IFP02", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IFP816", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IFPC02", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IFPSC02", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IFREF", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IMPORT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".IMPORTZP", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".INCBIN", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".INCLUDE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".INTERRUPTOR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".LINECONT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".LIST", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".LISTBYTES", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".LOBYTES", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".LOCAL", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".LOCALCHAR", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".MACPACK", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".MAC", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".MACRO", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ORG", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".OUT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".P02", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".P816", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".PAGELEN", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".PAGELENGTH", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".PC02", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".POPSEG", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".PROC", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".PSC02", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".PUSHSEG", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".RELOG", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".REPEAT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".RES", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".RODATA", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".SCOPE", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".SEGMENT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".SETCPU", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".SMART", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".STRUCT", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".SUNPLUS", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".TAG", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".WARNING", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".WORD", DEFAULT_COMPILER_KEYWORD);
                compilerKeywords.put(".ZEROPAGE", DEFAULT_COMPILER_KEYWORD);
                break;
        }
        return compilerKeywords;
    }    
    /**
     * 
     * @param compiler
     * @return 
     */
    public static HashMap<String, MutableAttributeSet> getScriptKeywordHashMap(int compiler) {
        final HashMap<String, MutableAttributeSet> scriptKeywords = new HashMap<>();
        switch (compiler) {
            case ConstantsR64.COMPILER_ACME:
                break;
            case ConstantsR64.COMPILER_KICKASSEMBLER:
            case ConstantsR64.COMPILER_64TASS:
//                scriptKeywords.put(".ADD", DEFAULT_SCRIPT_KEYWORD);
//                scriptKeywords.put(".ASNUMBER", DEFAULT_SCRIPT_KEYWORD);
//                scriptKeywords.put(".ASBOOLEAN", DEFAULT_SCRIPT_KEYWORD);
//                scriptKeywords.put(".CHARAT", DEFAULT_SCRIPT_KEYWORD);
//                scriptKeywords.put(".GET", DEFAULT_SCRIPT_KEYWORD);
//                scriptKeywords.put(".GETDATA", DEFAULT_SCRIPT_KEYWORD);
//                scriptKeywords.put(".GETPIXEL", DEFAULT_SCRIPT_KEYWORD);
//                scriptKeywords.put(".GETMULTICOLORBYTE", DEFAULT_SCRIPT_KEYWORD);
//                scriptKeywords.put(".GETSINGLECOLORBYTE", DEFAULT_SCRIPT_KEYWORD);
//                scriptKeywords.put(".SIZE", DEFAULT_SCRIPT_KEYWORD);
//                scriptKeywords.put(".SUBSTRING", DEFAULT_SCRIPT_KEYWORD);
//                scriptKeywords.put(".STRING", DEFAULT_SCRIPT_KEYWORD);
//                scriptKeywords.put(".TOBINARYSTRING", DEFAULT_SCRIPT_KEYWORD);
//                scriptKeywords.put(".TOHEXSTRING", DEFAULT_SCRIPT_KEYWORD);
//                scriptKeywords.put(".TOINTSTRING", DEFAULT_SCRIPT_KEYWORD);
//                scriptKeywords.put(".TOOCTALSTRING", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("ABS", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("ACOS", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("ASIN", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("ATAN", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("ATAN2", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("CBRT", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("CEIL", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("COS", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("COSH", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("DEG", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("EXP", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("EXPM1", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("FLOOR", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("FRAC", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("HYPOT", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("LIST", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("LOG", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("LOG10", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("LOG1P", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("LOADPICTURE", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("LOADSID", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("MATRIX", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("MAX", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("MIN", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("MOD", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("MOVEMATRIX", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("PERSPECTIVEMATRIX", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("POW", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("RAD", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("RANDOM", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("ROTATIONMATRIX", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("ROUND", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("SCALEMATRIX", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("SIGN", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("SIN", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("SINH", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("SQRT", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("SIGNUM", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("TAN", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("TANH", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("TODEGREE", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("TORADIANS", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put("VECTOR", DEFAULT_SCRIPT_KEYWORD);
                break;
            case ConstantsR64.COMPILER_CA65:
                scriptKeywords.put(".BANKBYTE", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".BLANK", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".CONCAT", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".CONST", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".HIBYTE", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".HIWORD", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".IDENT", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".LEFT", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".LOBYTE", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".MATCH", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".MID", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".REF", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".REFERENCED", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".RIGHT", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".SIZEOF", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".STRAT", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".SPRINTF", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".STRING", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".STRLEN", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".TCOUNT", DEFAULT_SCRIPT_KEYWORD);
                scriptKeywords.put(".XMATCH", DEFAULT_SCRIPT_KEYWORD);
                break;
        }
        if (ConstantsR64.COMPILER_64TASS==compiler) {
            scriptKeywords.put("ALL", DEFAULT_SCRIPT_KEYWORD);
            scriptKeywords.put("ANY", DEFAULT_SCRIPT_KEYWORD);
            scriptKeywords.put("BOOL", DEFAULT_SCRIPT_KEYWORD);
            scriptKeywords.put("FORMAT", DEFAULT_SCRIPT_KEYWORD);
            scriptKeywords.put("INT", DEFAULT_SCRIPT_KEYWORD);
            scriptKeywords.put("LEN", DEFAULT_SCRIPT_KEYWORD);
            scriptKeywords.put("RANGE", DEFAULT_SCRIPT_KEYWORD);
            scriptKeywords.put("REPR", DEFAULT_SCRIPT_KEYWORD);
            scriptKeywords.put("STR", DEFAULT_SCRIPT_KEYWORD);
        }
        return scriptKeywords;
    }    
}
