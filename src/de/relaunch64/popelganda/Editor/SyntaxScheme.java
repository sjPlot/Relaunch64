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
     * 
     * @return 
     */
    public static HashMap<String, MutableAttributeSet> getKeywordHashMap() {
        final HashMap<String, MutableAttributeSet> asmKeywords = new HashMap<>();
        final String opcodes[] = {
            "LDA", "STA", "INC", "DEC", "LDX", "STX", "INX", "DEX", "LDY",
            "STY", "INY", "DEY", "CMP", "CPX", "CPY", "TAX", "TAY", "TXA",
            "TYA", "TSX", "TXS", "AND", "ORA", "EOR", "ADC", "SBC", "SEC",
            "CLC", "ASL", "LSR", "ROL", "ROR", "BIT", "JMP", "JSR", "RTS",
            "RTI", "BCC", "BCS", "BEQ", "BNE", "BPL", "BMI", "BVC", "BVS",
            "SEI", "CLI", "SED", "CLD", "CLV", "PHA", "PLA", "PHP", "PLP",
            "NOP", "BRK"
        };

        for (String item : opcodes) {
            asmKeywords.put(item, DEFAULT_KEYWORD);
        }
        return asmKeywords;
    }    
    public static HashMap<String, MutableAttributeSet> getIllegalOpcodeHashMap() {
        final HashMap<String, MutableAttributeSet> asmKeywords = new HashMap<>();
        final String opcodes[] = {
            "AAR", "AHX", "ALR", "ANC", "AXS", "DCP", "ISC", "LAS", "LAX",
            "RLA", "RRA", "SAX", "SBC", "SHX", "SHY", "SLO", "SRE", "TAS",
            "XAA"
        };

        for (String item : opcodes) {
            asmKeywords.put(item, DEFAULT_ILLEGAL_OPCODE);
        }
        return asmKeywords;
    }    
    /**
     * 
     * @param compiler
     * @return 
     */
    public static HashMap<String, MutableAttributeSet> getCompilerKeywordHashMap(int compiler) {
        final HashMap<String, MutableAttributeSet> compilerKeywords = new HashMap<>();
        final String keywords[];
        switch (compiler) {
            case ConstantsR64.COMPILER_ACME:
                keywords = new String[] {
                    "!08", "!8", "!16", "!24", "!32", "!ALIGN", "!AL", "!AS",
                    "!BIN", "!BINARY", "!BY", "!BYTE", "!CONVTAB", "!CT",
                    "!DO", "!EOF", "!ENDOFFILE", "!ERROR", "!IF", "!IFDEF",
                    "!INITMEN", "!FI", "!FILL", "!FOR", "!MACRO", "!PET",
                    "!PSEUDOPC", "!RAW", "!RL", "!RS", "!SCR", "!SCRXOR",
                    "!SET", "!SL", "!SRC", "!SOURCE", "!TEXT", "!TO", "!TX",
                    "!WARN", "!WO", "!WORD", "!ZN", "!ZONE", "UNTIL"
                };
                break;
            case ConstantsR64.COMPILER_KICKASSEMBLER:
                keywords = new String[] {
                    ".ALIGN", ".ASSERT", ".BYTE", ".CONST", ".DEFINE",
                    ".DWORD", ".ENUM", ".EVAL", ".FILL", ".FOR",
                    ".FUNCTION", ".IF", ".IMPORT", ".IMPORTONCE", ".LABEL",
                    ".MACRO", ".PC", ".PRINT", ".PSEUDOPC", ".RETURN",
                    ".TEXT", ".VAR", ".WORD"
                };
                break;
            case ConstantsR64.COMPILER_DASM:
                keywords = new String[] {
                    "INCLUDE", "INCBIN", "INCDIR", "SEG", "SEG.U", "DC.B",
                    "DC.W", "DC.L", "DS.B", "DS.W", "DS.L", "DV.B", "DV.W",
                    "DV.L", "BYTE", "WORD", "LONG", "HEX", "ERR", "ORG",
                    "RORG", "PROCESSOR", "ECHO", "REND", "ALIGN",
                    "SUBROUTINE", "EQU", "EQM", "SET", "MAC", "ENDM",
                    "MEXIT", "IFCONST", "IFNCONST", "IF", "ELSE", "ENDIF",
                    "EIF", "REPEAT", "REPEND", "DAD", "LIST ON", "LIST OFF"
                };
                break;
            case ConstantsR64.COMPILER_64TASS:
                keywords = new String[] {
                    ".AL", ".ALIGN", ".AS", ".ASSERT", ".BEND", ".BINARY",
                    ".BINCLUDE", ".BLOCK", ".BREAK", ".BYTE", ".CASE", ".CDEF",
                    ".CERROR", ".CHAR", ".CHECK", ".COMMENT", ".CONTINUE",
                    ".CPU", ".CWARN", ".DATABANK", ".DEFAULT", ".DINT",
                    ".DPAGE", ".DSECTION", ".DSTRUCT", ".DUNION", ".DWORD",
                    ".EDEF", ".ELSE", ".ELSIF", ".ENC", ".END", ".ENDC",
                    ".ENDF", ".ENDIF", ".ENDM", ".ENDP", ".ENDS", ".ENDSWITCH",
                    ".ENDU", ".ENDWEAK", ".EOR", ".ERROR", ".FI", ".FILL",
                    ".FOR", ".FUNCTION", ".GOTO", ".HERE", ".HIDEMAC", ".IF",
                    ".IFEQ", ".IFMI", ".IFNE", ".IFPL", ".INCLUDE", ".INT",
                    ".LBL", ".LINT", ".LOGICAL", ".LONG", ".MACRO", ".NEXT",
                    ".NULL", ".OFFS", ".OPTION", ".PAGE", ".PEND", ".PROC",
                    ".PROFF", ".PRON", ".PRTEXT", ".REPT", ".RTA", ".SECTION",
                    ".SEGMENT", ".SEND", ".SHIFT", ".SHIFTL", ".SHOWMAC",
                    ".STRUCT", ".SWITCH", ".TEXT", ".UNION", ".VAR", ".WARN",
                    ".WEAK", ".WORD", ".XL", ".XS"
                };
                break;
            case ConstantsR64.COMPILER_CA65:
                keywords = new String[] {
                    ".A16", ".A8", ".ADDR", ".ALIGN", ".ASCIIZ", ".ASSERT",
                    ".AUTOIMPORT", ".BANKBYTES", ".BSS", ".BYT", ".BYTE",
                    ".CASE", ".CHARMAP", ".CODE", ".CONDES", ".CONSTRUCTOR",
                    ".DATA", ".DBYT", ".DEBUGINFO", ".DEF", ".DEFINED",
                    ".DESTRUCTOR", ".DWORD", ".ELSE", ".ELSIF", ".END",
                    ".ENDENUM", ".ENDIF", ".ENDMAC", ".ENDMACRO", ".ENDPROC",
                    ".ENDREP", ".ENDREPEAT", ".ENDSCOPE", ".ENDSTRUCT",
                    ".ENUM", ".ERROR", ".EXITMAC", ".EXITMACRO", ".EXPORT",
                    ".EXPORTZP", ".FARADDR", ".FEATURE", ".FILEOPT", ".FOPT",
                    ".FORCEIMPORT", ".GLOBAL", ".GLOBALZP", ".HIBYTES", ".I16",
                    ".I8", ".IF", ".IFBLANK", ".IFCONST", ".IFDEF",
                    ".IFNBLANK", ".IFNDEF", ".IFNREF", ".IFP02", ".IFP816",
                    ".IFPC02", ".IFPSC02", ".IFREF", ".IMPORT", ".IMPORTZP",
                    ".INCBIN", ".INCLUDE", ".INTERRUPTOR", ".LINECONT",
                    ".LIST", ".LISTBYTES", ".LOBYTES", ".LOCAL", ".LOCALCHAR",
                    ".MACPACK", ".MAC", ".MACRO", ".ORG", ".OUT", ".P02",
                    ".P816", ".PAGELEN", ".PAGELENGTH", ".PC02", ".POPSEG",
                    ".PROC", ".PSC02", ".PUSHSEG", ".RELOG", ".REPEAT", ".RES",
                    ".RODATA", ".SCOPE", ".SEGMENT", ".SETCPU", ".SMART",
                    ".STRUCT", ".SUNPLUS", ".TAG", ".WARNING", ".WORD",
                    ".ZEROPAGE"
                };
                break;
            case ConstantsR64.COMPILER_DREAMASS:
                keywords = new String[] {
                    ".BINCLUDE", ".BYTE", ".DB", ".DW", ".DT", ".TEXT", ".PET",
                    ".DP", ".SCR", ".DS", ".SEGMENT", ".SETPET", ".SETSCR",
                    ".WORD", ".DSB", ".ALIGN", ".(", ".)", ".", ".PSEUDOPC",
                    ".REALPC", "#ERROR", "#IF", "#IFDEF", "#IFFILE", "#IFNDEF",
                    "#IFNFILE", "#ELSIF", "#ELSIFDEF", "#ELSIFFILE",
                    "#ELSIFNDEF", "#ELSIFNFILE", "#ELSE", "#ENDIF", "#INCLUDE",
                    "#MACRO", "#OUTFILE", "#PRINT", "#SEGDEF", "#WARNING"
                };
                break;
            default:
                return compilerKeywords; // unsupported
        }

        for (String item : keywords) {
            compilerKeywords.put(item, DEFAULT_COMPILER_KEYWORD);
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
        final String keywords[];
        switch (compiler) {
            case ConstantsR64.COMPILER_ACME:
                keywords = new String[] {
                    "AND", "ARCCOS", "ARCSIN", "ARCTAN", "ASL", "ASR", "COS",
                    "DIV", "FLOAT", "INT", "LSL", "LSR", "MOD", "NOT", "OR",
                    "SIN", "TAN", "XOR"
                };
                break;
            case ConstantsR64.COMPILER_KICKASSEMBLER:
                keywords = new String[] {
//                    ".ADD", ".ASNUMBER", ".ASBOOLEAN", ".CHARAT", ".GET",
//                    ".GETDATA", ".GETPIXEL", ".GETMULTICOLORBYTE",
//                    ".GETSINGLECOLORBYTE", ".SIZE", ".SUBSTRING", ".STRING",
//                    ".TOBINARYSTRING", ".TOHEXSTRING", ".TOINTSTRING",
//                    ".TOOCTALSTRING",
                    "ABS", "ACOS", "ASIN", "ATAN", "ATAN2", "CBRT", "CEIL", "COS",
                    "COSH", "DEG", "EXP", "EXPM1", "FLOOR", "FRAC", "HYPOT",
                    "LIST", "LOG", "LOG10", "LOG1P", "LOADPICTURE", "LOADSID",
                    "MATRIX", "MAX", "MIN", "MOD", "MOVEMATRIX",
                    "PERSPECTIVEMATRIX", "POW", "RAD", "RANDOM",
                    "ROTATIONMATRIX", "ROUND", "SCALEMATRIX", "SIGN", "SIN",
                    "SINH", "SQRT", "SIGNUM", "TAN", "TANH", "TODEGREE",
                    "TORADIANS", "VECTOR"
                };
                break;
            case ConstantsR64.COMPILER_64TASS:
                keywords = new String[] {
                    "ABS", "ACOS", "ALL", "ANY", "ASIN", "ATAN", "ATAN2", "BOOL",
                    "CBRT", "CEIL", "COS", "COSH", "DEG", "EXP", "FLOAT",
                    "FLOOR", "FORMAT", "FRAC", "HYPOT", "INT", "LEN", "LOG",
                    "LOG10", "POW", "RAD", "RANGE", "REPR", "ROUND", "SIGN",
                    "SIN", "SINH", "SIZE", "SQRT", "STR", "TAN", "TANH",
                    "TRUNC"
                };
                break;
            case ConstantsR64.COMPILER_CA65:
                keywords = new String[] {
                    ".BANKBYTE", ".BLANK", ".CONCAT", ".CONST", ".HIBYTE",
                    ".HIWORD", ".IDENT", ".LEFT", ".LOBYTE", ".MATCH", ".MID",
                    ".REF", ".REFERENCED", ".RIGHT", ".SIZEOF", ".STRAT",
                    ".SPRINTF", ".STRING", ".STRLEN", ".TCOUNT", ".XMATCH"
                };
                break;
            default:
                return scriptKeywords; // unsupported
        }
        for (String item : keywords) {
            scriptKeywords.put(item, DEFAULT_SCRIPT_KEYWORD);
        }
        return scriptKeywords;
    }    
}
