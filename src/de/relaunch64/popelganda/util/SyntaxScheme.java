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
import java.util.HashMap;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * @author Daniel Luedecke
 */
public class SyntaxScheme {
    public static final String DEFAULT_FONT_FAMILY = "Arial";
    public static final int DEFAULT_FONT_SIZE = 13;
    public static final SimpleAttributeSet DEFAULT_NORMAL;
    public static final SimpleAttributeSet DEFAULT_NUMBER;
    public static final SimpleAttributeSet DEFAULT_COMMENT;
    public static final SimpleAttributeSet DEFAULT_STRING;
    public static final SimpleAttributeSet DEFAULT_HEXA;
    public static final SimpleAttributeSet DEFAULT_BIN;
    public static final SimpleAttributeSet DEFAULT_MACRO;
    public static final SimpleAttributeSet DEFAULT_JUMP;
    public static final SimpleAttributeSet DEFAULT_KEYWORD;
 
    static {
        DEFAULT_NORMAL = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_NORMAL, Color.BLACK);
        StyleConstants.setFontFamily(DEFAULT_NORMAL, DEFAULT_FONT_FAMILY);
        StyleConstants.setFontSize(DEFAULT_NORMAL, DEFAULT_FONT_SIZE);
 
        DEFAULT_COMMENT = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_COMMENT, new java.awt.Color(150, 150, 150)); //grey
        StyleConstants.setFontFamily(DEFAULT_COMMENT, DEFAULT_FONT_FAMILY);
        StyleConstants.setFontSize(DEFAULT_COMMENT, DEFAULT_FONT_SIZE);
 
        DEFAULT_STRING = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_STRING, new java.awt.Color(220, 0, 220)); //dark pink
        StyleConstants.setFontFamily(DEFAULT_STRING, DEFAULT_FONT_FAMILY);
        StyleConstants.setFontSize(DEFAULT_STRING, DEFAULT_FONT_SIZE);
 
        DEFAULT_NUMBER = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_NUMBER, new java.awt.Color(160, 0, 0)); //red
        StyleConstants.setFontFamily(DEFAULT_NUMBER, DEFAULT_FONT_FAMILY);
        StyleConstants.setFontSize(DEFAULT_NUMBER, DEFAULT_FONT_SIZE);
 
        DEFAULT_HEXA = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_HEXA, new java.awt.Color(0, 120, 0)); //green
        StyleConstants.setFontFamily(DEFAULT_HEXA, DEFAULT_FONT_FAMILY);
        StyleConstants.setFontSize(DEFAULT_HEXA, DEFAULT_FONT_SIZE);
 
        DEFAULT_BIN = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_BIN, new java.awt.Color(0, 120, 120)); //green
        StyleConstants.setFontFamily(DEFAULT_BIN, DEFAULT_FONT_FAMILY);
        StyleConstants.setFontSize(DEFAULT_BIN, DEFAULT_FONT_SIZE);
 
        DEFAULT_JUMP = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_JUMP, new java.awt.Color(206, 103, 0)); //brown
        StyleConstants.setFontFamily(DEFAULT_JUMP, DEFAULT_FONT_FAMILY);
        StyleConstants.setFontSize(DEFAULT_JUMP, DEFAULT_FONT_SIZE);
        
        DEFAULT_MACRO = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_MACRO, new java.awt.Color(206, 103, 0)); //brown
        StyleConstants.setFontFamily(DEFAULT_MACRO, DEFAULT_FONT_FAMILY);
        StyleConstants.setFontSize(DEFAULT_MACRO, DEFAULT_FONT_SIZE);
        
        //default style for new keyword types
        DEFAULT_KEYWORD = new SimpleAttributeSet();
        StyleConstants.setForeground(DEFAULT_KEYWORD, new java.awt.Color(0, 0, 200)); //dark blue
        // StyleConstants.setBold(DEFAULT_KEYWORD, true);
        StyleConstants.setFontFamily(DEFAULT_KEYWORD, DEFAULT_FONT_FAMILY);
        StyleConstants.setFontSize(DEFAULT_KEYWORD, DEFAULT_FONT_SIZE);
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
        sasArray.put(ConstantsR64.STRING_BIN, DEFAULT_BIN);
        sasArray.put(ConstantsR64.STRING_JUMP, DEFAULT_JUMP);
        sasArray.put(ConstantsR64.STRING_MACRO, DEFAULT_MACRO);
        sasArray.put(ConstantsR64.STRING_KEYWORD, DEFAULT_KEYWORD);
        return sasArray;
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
                str = ",:{}()[]+-/%<=>&|^~*";
                break;
            case ConstantsR64.COMPILER_KICKASSEMBLER:
                str = ",;:{}()[]+-/%<=>&!|^~*";
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

//        asmKeywords.put("(", DEFAULT_KEYWORD);
//        asmKeywords.put(")", DEFAULT_KEYWORD);
//        asmKeywords.put("{", DEFAULT_KEYWORD);
//        asmKeywords.put("}", DEFAULT_KEYWORD);
        
        switch (compiler) {
            case ConstantsR64.COMPILER_ACME:
                asmKeywords.put("!DO", DEFAULT_KEYWORD);
                asmKeywords.put("!SET", DEFAULT_KEYWORD);
                asmKeywords.put("!ZONE", DEFAULT_KEYWORD);
                asmKeywords.put("!TO", DEFAULT_KEYWORD);
                asmKeywords.put("!CT", DEFAULT_KEYWORD);
                asmKeywords.put("!TX", DEFAULT_KEYWORD);
                asmKeywords.put("!BYTE", DEFAULT_KEYWORD);
                asmKeywords.put("!BIN", DEFAULT_KEYWORD);
                asmKeywords.put("UNTIL", DEFAULT_KEYWORD);
                break;
        }
        return asmKeywords;
    }    
}
