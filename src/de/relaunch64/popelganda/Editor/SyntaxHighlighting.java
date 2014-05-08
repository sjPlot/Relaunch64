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
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.HashMap;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

/**
 * @author camickr (primary author; java sun forums user)
 * @author David Underhill
 * 
 * This example was taken from
 * http://www.java-forum.org/awt-swing-swt/126947-syntaxhighlighting.html#post824914
 * and modified for own purposes.
 * 
 */
public class SyntaxHighlighting extends DefaultStyledDocument { 
    /**
     * Highlights syntax in a DefaultStyledDocument.  Allows any number of keywords.
     */
    private final DefaultStyledDocument doc;
    private final Element rootElement;
    private boolean multiLineComment;
    private final MutableAttributeSet normal;
    private final MutableAttributeSet comment;
    private final MutableAttributeSet quote;
    private final MutableAttributeSet hexa;
    private final MutableAttributeSet binary;
    private final MutableAttributeSet lohi;
    private final MutableAttributeSet jump;
    private final MutableAttributeSet number;
    private final MutableAttributeSet keyword;
    private final HashMap<String, MutableAttributeSet> keywords;
    private final HashMap<String, MutableAttributeSet> compilerKeywords;
    private final HashMap<String, MutableAttributeSet> illegalOpcodes;
    private int fontSize;
    private String fontName;
    private final String singleLineComment;
    private final String delimiterList;
    private final int compiler;
 
    @SuppressWarnings("LeakingThisInConstructor")
    public SyntaxHighlighting(final HashMap<String, MutableAttributeSet> keywords,
                              final HashMap<String, MutableAttributeSet> compilerKeywords,
                              final HashMap<String, MutableAttributeSet> illegalOpcodes,
                              String fname, int fsize, String slc, String dll,
                              final HashMap<String, MutableAttributeSet> attributes,
                              int comp, int tabWidth) {

        normal = attributes.get(ConstantsR64.STRING_NORMAL);
        comment = attributes.get(ConstantsR64.STRING_COMMENT);
        quote = attributes.get(ConstantsR64.STRING_STRING);
        number = attributes.get(ConstantsR64.STRING_NUMBER);
        hexa = attributes.get(ConstantsR64.STRING_HEXA);
        jump = attributes.get(ConstantsR64.STRING_JUMP);
        binary = attributes.get(ConstantsR64.STRING_BIN);
        lohi = attributes.get(ConstantsR64.STRING_LOHI);
        keyword = attributes.get(ConstantsR64.STRING_KEYWORD);

        singleLineComment = slc;
        delimiterList = dll;
        fontName = fname;
        fontSize = fsize;
        compiler = comp;
        
        doc = this;
        rootElement = doc.getDefaultRootElement();
        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        this.keywords = keywords;
        this.compilerKeywords = compilerKeywords;
        this.illegalOpcodes = illegalOpcodes;
        
        setTabs(tabWidth);
        setFontSize(fontSize);
    }
    @SuppressWarnings("PublicInnerClass")
    public enum ATTR_TYPE {
         Normal, Comment, Quote, Number, Hexa, LoHi, Binary, Jump;
    }
 
    /**
     * Sets the font of the specified attribute
     * @param attr   the attribute to apply this font to (normal, comment, string)
     * @param style  font style (Font.BOLD, Font.ITALIC, Font.PLAIN)
     */
    public void setAttributeFont(ATTR_TYPE attr, int style) {
        Font f = new Font(fontName, style, fontSize);
        if (attr == ATTR_TYPE.Comment) {
            setAttributeFont(comment, f);
        } else if (attr == ATTR_TYPE.Quote) {
            setAttributeFont(quote, f);
        } else if (attr == ATTR_TYPE.Number) {
            setAttributeFont(number, f);
        } else if (attr == ATTR_TYPE.Hexa) {
            setAttributeFont(hexa, f);
        } else if (attr == ATTR_TYPE.LoHi) {
            setAttributeFont(lohi, f);
        } else if (attr == ATTR_TYPE.Binary) {
            setAttributeFont(binary, f);
        } else if (attr == ATTR_TYPE.Jump) {
            setAttributeFont(jump, f);
        } else {
            setAttributeFont(normal, f);
        }
    }
 
    /**
     * Sets the font of the specified attribute
     * @param attr  attribute to apply this font to
     * @param f     the font to use 
     */
    public static void setAttributeFont(MutableAttributeSet attr, Font f) {
        StyleConstants.setBold(attr, f.isBold());
        StyleConstants.setItalic(attr, f.isItalic());
        StyleConstants.setFontFamily(attr, f.getFamily());
        StyleConstants.setFontSize(attr, f.getSize());
    }
 
    /**
     * Sets the foreground (font) color of the specified attribute
     * @param attr  the attribute to apply this font to (normal, comment, string)
     * @param c     the color to use 
     */
    public void setAttributeColor(ATTR_TYPE attr, Color c) {
        if (attr == ATTR_TYPE.Comment) {
            setAttributeColor(comment, c);
        } else if (attr == ATTR_TYPE.Quote) {
            setAttributeColor(quote, c);
        } else if (attr == ATTR_TYPE.Number) {
            setAttributeColor(number, c);
        } else if (attr == ATTR_TYPE.Hexa) {
            setAttributeColor(hexa, c);
        } else if (attr == ATTR_TYPE.LoHi) {
            setAttributeColor(lohi, c);
        } else if (attr == ATTR_TYPE.Binary) {
            setAttributeColor(binary, c);
        } else if (attr == ATTR_TYPE.Jump) {
            setAttributeColor(jump, c);
        } else {
            setAttributeColor(normal, c);
        }
    }
 
    /**
     * Sets the foreground (font) color of the specified attribute
     * @param attr  attribute to apply this color to
     * @param c  the color to use 
     */
    public static void setAttributeColor(MutableAttributeSet attr, Color c) {
        StyleConstants.setForeground(attr, c);
    }
 
    /**
     * Associates a keyword with a particular formatting style
     * @param keyword  the token or word to format
     * @param attr     how to format keyword
     */
    public void addKeyword(String keyword, MutableAttributeSet attr) {
        keywords.put(keyword, attr);
    }
 
    /**
     * Associates a keyword with a particular formatting style
     * @param compilerKeyword  the token or word to format
     * @param attr     how to format keyword
     */
    public void addCompilerKeyword(String compilerKeyword, MutableAttributeSet attr) {
        compilerKeywords.put(compilerKeyword, attr);
    }
 
    /**
     * Associates a keyword with a particular formatting style
     * @param keyword  the token or word to format
     * @param attr     how to format keyword
     */
    public void addIllegalOpcode(String keyword, MutableAttributeSet attr) {
        illegalOpcodes.put(keyword, attr);
    }
    
    /**
     * Gets the formatting for a keyword
     *
     * @param keyword  the token or word to stop formatting
     * @return how keyword is formatted, or null if no formatting is applied to it
     */
    public MutableAttributeSet getKeywordFormatting(String keyword) {
        return keywords.get(keyword);
    }
 
    /**
     * Gets the formatting for a keyword
     *
     * @param compilerKeyword  the token or word to stop formatting
     * @return how keyword is formatted, or null if no formatting is applied to it
     */
    public MutableAttributeSet getCompilerKeywordFormatting(String compilerKeyword) {
        return compilerKeywords.get(compilerKeyword);
    }
 
    /**
     * Gets the formatting for a keyword
     *
     * @param keyword  the token or word to stop formatting
     * @return how keyword is formatted, or null if no formatting is applied to it
     */
    public MutableAttributeSet getIllegalOpcodeFormatting(String keyword) {
        return illegalOpcodes.get(keyword);
    }
 
    /**
     * Removes an association between a keyword with a particular formatting style
     * @param keyword  the token or word to stop formatting
     */
    public void removeKeyword(String keyword) {
        keywords.remove(keyword);
    }
 
    /**
     * Removes an association between a keyword with a particular formatting style
     * @param compilerKeyword  the token or word to stop formatting
     */
    public void removeCompilerKeyword(String compilerKeyword) {
        compilerKeywords.remove(compilerKeyword);
    }
 
    /**
     * Removes an association between a keyword with a particular formatting style
     * @param keyword  the token or word to stop formatting
     */
    public void removeIllegalOpcode(String keyword) {
        illegalOpcodes.remove(keyword);
    }
 
    /**
     * sets the number of characters per tab
     * @param charactersPerTab
     */
    private void setTabs(int charactersPerTab) {
        Font f = new Font(fontName, Font.PLAIN, fontSize);
        FontMetrics fm = java.awt.Toolkit.getDefaultToolkit().getFontMetrics(f);
        int charWidth = fm.charWidth('w');
        int tabWidth = charWidth * charactersPerTab;
        TabStop[] tabs = new TabStop[35];
        for (int j = 0; j < tabs.length; j++) {
            int tab = j + 1;
            tabs[j] = new TabStop(tab * tabWidth);
        }
        TabSet tabSet = new TabSet(tabs);
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setTabSet(attributes, tabSet);
        int length = this.getLength();
        this.setParagraphAttributes(0, length, attributes, false);
    }
 
    @Override
    @SuppressWarnings("AssignmentToMethodParameter")
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        if (str.equals("{")) {
            str = addMatchingBrace(offset);
        }
        super.insertString(offset, str, a);
        processChangedLines(offset, str.length());
    }
 
    @Override
    public void remove(int offset, int length) throws BadLocationException {
        super.remove(offset, length);
        processChangedLines(offset, 0);
    }
 
    /*
     *  Determine how many lines have been changed,
     *  then apply highlighting to each line
     */
    public void processChangedLines(int offset, int length)
            throws BadLocationException {
        String content = doc.getText(0, doc.getLength());
        //  The lines affected by the latest document update
        int startLine = rootElement.getElementIndex(offset);
        int endLine = rootElement.getElementIndex(offset + length);
        //  Make sure all comment lines prior to the start line are commented
        //  and determine if the start line is still in a multi line comment
        setMultiLineComment(commentLinesBefore(content, startLine));
        //  Do the actual highlighting
        for (int i = startLine; i <= endLine; i++) {
            applyHighlighting(content, i);
        }
        //  Resolve highlighting to the next end multi line delimiter
        if (isMultiLineComment()) {
            commentLinesAfter(content, endLine);
        } else {
            highlightLinesAfter(content, endLine);
        }
    }
    
    private boolean commentLinesBefore(String content, int line) {
        int offset = rootElement.getElement(line).getStartOffset();
        //  Start of comment not found, nothing to do
        int startDelimiter = lastIndexOf(content, getStartDelimiter(), offset - 2);
        if (startDelimiter < 0) {
            return false;
        }
        //  Matching start/end of comment found, nothing to do
        int endDelimiter = indexOf(content, getEndDelimiter(), startDelimiter);
        if (endDelimiter < offset & endDelimiter != -1) {
            return false;
        }
        //  End of comment not found, highlight the lines
        doc.setCharacterAttributes(startDelimiter, offset - startDelimiter + 1, comment, false);
        return true;
    }
 
    private void commentLinesAfter(String content, int line) {
        int offset = rootElement.getElement(line).getEndOffset();
        //  End of comment not found, nothing to do
        int endDelimiter = indexOf(content, getEndDelimiter(), offset);
        if (endDelimiter < 0) {
            return;
        }
        //  Matching start/end of comment found, comment the lines
        int startDelimiter = lastIndexOf(content, getStartDelimiter(), endDelimiter);
        if (startDelimiter < 0 || startDelimiter <= offset) {
            doc.setCharacterAttributes(offset, endDelimiter - offset + 1, comment, false);
        }
    }
 
    private void highlightLinesAfter(String content, int line)
            throws BadLocationException {
        int offset = rootElement.getElement(line).getEndOffset();
        //  Start/End delimiter not found, nothing to do
        int startDelimiter = indexOf(content, getStartDelimiter(), offset);
        int endDelimiter = indexOf(content, getEndDelimiter(), offset);
        if (startDelimiter < 0) {
            startDelimiter = content.length();
        }
        if (endDelimiter < 0) {
            endDelimiter = content.length();
        }
        int delimiter = Math.min(startDelimiter, endDelimiter);
        if (delimiter < offset) {
            return;
        }
        //  Start/End delimiter found, reapply highlighting
        int endLine = rootElement.getElementIndex(delimiter);
        for (int i = line + 1; i < endLine; i++) {
            Element branch = rootElement.getElement(i);
            Element leaf = doc.getCharacterElement(branch.getStartOffset());
            AttributeSet as = leaf.getAttributes();
            if (as.isEqual(comment)) {
                applyHighlighting(content, i);
            }
        }
    }
 
    private void applyHighlighting(String content, int line)
            throws BadLocationException {
        int startOffset = rootElement.getElement(line).getStartOffset();
        int endOffset = rootElement.getElement(line).getEndOffset() - 1;
        int lineLength = endOffset - startOffset;
        int contentLength = content.length();
        if (endOffset >= contentLength) {
            endOffset = contentLength - 1;
        }
        if (endingMultiLineComment(content, startOffset, endOffset)
                || isMultiLineComment()
                || startingMultiLineComment(content, startOffset, endOffset)) {
            doc.setCharacterAttributes(startOffset, endOffset - startOffset + 1, comment, false);
            return;
        }
        doc.setCharacterAttributes(startOffset, lineLength, normal, true);
        int index = content.indexOf(getSingleLineDelimiter(), startOffset);
        if ((index > -1) && (index < endOffset)) {
            doc.setCharacterAttributes(index, endOffset - index + 1, comment, false);
            endOffset = index - 1;
        }
        checkForTokens(content, startOffset, endOffset);
    }
    
    private boolean startingMultiLineComment(String content, int startOffset, int endOffset)
            throws BadLocationException {
        int index = indexOf(content, getStartDelimiter(), startOffset);
        if ((index < 0) || (index > endOffset)) {
            return false;
        } else {
            setMultiLineComment(true);
            return true;
        }
    }
 
    private boolean endingMultiLineComment(String content, int startOffset, int endOffset)
            throws BadLocationException {
        int index = indexOf(content, getEndDelimiter(), startOffset);
        if ((index < 0) || (index > endOffset)) {
            return false;
        } else {
            setMultiLineComment(false);
            return true;
        }
    }
 
    private boolean isMultiLineComment() {
        return multiLineComment;
    }
 
    private void setMultiLineComment(boolean value) {
        multiLineComment = value;
    }
 
    @SuppressWarnings("AssignmentToMethodParameter")
    private void checkForTokens(String content, int startOffset, int endOffset) {
        // check for minimum length
        if (content.length()<2) {
            return;
        }
        while (startOffset <= endOffset) {
            try {
                //  skip the delimiters to find the start of a new token
                while (isDelimiter(content.substring(startOffset, startOffset + 1), ""/*, startOffset*/)) {
                    // doc.setCharacterAttributes(startOffset, startOffset+1, keyword, false);
                    if (startOffset < endOffset) {
                        startOffset++;
                    } else {
                        return;
                    }
                }
                //  Extract and process the entire token
                if (isQuoteDelimiter(content.substring(startOffset, startOffset + 1))) {
                    startOffset = getQuoteToken(content, startOffset, endOffset);
                }
                else if (isBinCharDelimiter(content.substring(startOffset, startOffset + 2))) {
                    startOffset = getBinCharToken(content, startOffset, endOffset, 2);
                }
                else if (isLoHiByteDelimiter(content.substring(startOffset, startOffset + 2))) {
                    startOffset = getLoHiByteToken(content, startOffset, endOffset);
                }
                else if (isJumpDelimiter(content.substring(startOffset, startOffset + 1))) {
                    startOffset = getJumpToken(content, startOffset, endOffset);
                }
                else if (isHexCharDelimiter(content.substring(startOffset, startOffset + 1))) {
                    startOffset = getHexCharToken(content, startOffset, endOffset);
                }
                else if (isHexAddressDelimiter(content.substring(startOffset, startOffset + 1))) {
                    startOffset = getHexAddressToken(content, startOffset, endOffset);
                } 
                else if (isBinCharDelimiter(content.substring(startOffset, startOffset + 1))) {
                    startOffset = getBinCharToken(content, startOffset, endOffset, 1);
                }
                else {
                    startOffset = getOtherToken(content, startOffset, endOffset);
                }
            }
            catch (IndexOutOfBoundsException ex) {
                if (startOffset < endOffset) {
                    startOffset++;
                } else {
                    return;
                }
            }
        }
    }
 
    private int getQuoteToken(String content, int startOffset, int endOffset) {
        String quoteDelimiter = content.substring(startOffset, startOffset + 1);
        String escapeString = getEscapeString(quoteDelimiter);
        int index;
        int endOfQuote = startOffset;
        index = content.indexOf(escapeString, endOfQuote + 1);
        while ((index > -1) && (index < endOffset)) {
            endOfQuote = index + 1;
            index = content.indexOf(escapeString, endOfQuote);
        }
        index = content.indexOf(quoteDelimiter, endOfQuote + 1);
        if ((index < 0) || (index > endOffset)) {
            endOfQuote = endOffset;
        } else {
            endOfQuote = index;
        }
        doc.setCharacterAttributes(startOffset, endOfQuote - startOffset + 1, quote, false);
        return endOfQuote + 1;
    }
 
    private int getBinCharToken(String content, int startOffset, int endOffset, int addValue) {
        int endOfToken = startOffset + addValue;
        while (endOfToken <= endOffset) {
            if (isDelimiter(content.substring(endOfToken, endOfToken + 1), "")) {
                break;
            }
            endOfToken++;
        }
        doc.setCharacterAttributes(startOffset, endOfToken - startOffset, binary, false);
        return endOfToken + 1;
    }
 
    private int getLoHiByteToken(String content, int startOffset, int endOffset) {
        int endOfToken = startOffset + 2;
        while (endOfToken <= endOffset) {
            if (isDelimiter(content.substring(endOfToken, endOfToken + 1), ".")) {
                break;
            }
            endOfToken++;
        }
        doc.setCharacterAttributes(startOffset, endOfToken - startOffset, lohi, false);
        return endOfToken + 1;
    }
 
    private int getHexCharToken(String content, int startOffset, int endOffset) {
        int endOfToken = startOffset + 1;
        while (endOfToken <= endOffset) {
            if (isDelimiter(content.substring(endOfToken, endOfToken + 1), "")) {
                break;
            }
            endOfToken++;
        }
        doc.setCharacterAttributes(startOffset, endOfToken - startOffset, number, false);
        return endOfToken + 1;
    }
 
    private int getJumpToken(String content, int startOffset, int endOffset) {
        int endOfToken = startOffset + 1;
        while (endOfToken <= endOffset) {
            if (isDelimiter(content.substring(endOfToken, endOfToken + 1), "")) {
                break;
            }
            endOfToken++;
        }
        doc.setCharacterAttributes(startOffset, endOfToken - startOffset, jump, false);
        return endOfToken + 1;
    }
 
    private int getHexAddressToken(String content, int startOffset, int endOffset) {
        int endOfToken = startOffset + 1;
        while (endOfToken <= endOffset) {
            if (isDelimiter(content.substring(endOfToken, endOfToken + 1), "")) {
                break;
            }
            endOfToken++;
        }
        doc.setCharacterAttributes(startOffset, endOfToken - startOffset, hexa, false);
        return endOfToken + 1;
    }
    private int getOtherToken(String content, int startOffset, int endOffset) {
        int endOfToken = startOffset + 1;
        while (endOfToken <= endOffset) {
            if (isDelimiter(content.substring(endOfToken, endOfToken + 1), "")) {
                break;
            }
            endOfToken++;
        }
        String token = content.substring(startOffset, endOfToken);
        MutableAttributeSet attr = keywords.get(token.toUpperCase());
        if (attr != null) {
            doc.setCharacterAttributes(startOffset, endOfToken - startOffset, attr, false);
        }
        else {
            attr = illegalOpcodes.get(token.toUpperCase());
            if (attr != null) {
                doc.setCharacterAttributes(startOffset, endOfToken - startOffset, attr, false);
            }
            else {
                attr = compilerKeywords.get(token.toUpperCase());
                if (attr != null) {
                    doc.setCharacterAttributes(startOffset, endOfToken - startOffset, attr, false);
                }
            }
        }
        return endOfToken + 1;
    }
 
    @SuppressWarnings({"AssignmentToMethodParameter", "NestedAssignment"})
    private int indexOf(String content, String needle, int offset) {
        int index;
        while ((index = content.indexOf(needle, offset)) != -1) {
            String text = getLine(content, index).trim();
            if (text.startsWith(needle) || text.endsWith(needle)) {
                break;
            } else {
                offset = index + 1;
            }
        }
 
        return index;
    }
 
    @SuppressWarnings({"AssignmentToMethodParameter", "NestedAssignment"})
    private int lastIndexOf(String content, String needle, int offset) {
        int index;
        while ((index = content.lastIndexOf(needle, offset)) != -1) {
            String text = getLine(content, index).trim();
            if (text.startsWith(needle) || text.endsWith(needle)) {
                break;
            } else {
                offset = index - 1;
            }
        }
 
        return index;
    }
 
    private String getLine(String content, int offset) {
        int line = rootElement.getElementIndex(offset);
        Element lineElement = rootElement.getElement(line);
        int start = lineElement.getStartOffset();
        int end = lineElement.getEndOffset();
        return content.substring(start, end - 1);
    }
 
    /*
     *  Override for other languages
     */
    protected boolean isDelimiter(String character, String additional_operands) {
        // Komma ergänzt
        String operands = delimiterList+additional_operands;
        // String operands = ";:{}()[]+-/%<=>!&|^~*";
        return Character.isWhitespace(character.charAt(0)) || operands.contains(character);
    }
    /*
     *  Override for other languages
     */
/*    
    protected boolean isDelimiter(String character, String additional_operands, int startOffset) {
        // Komma ergänzt
        String operands = delimiterList+additional_operands;
        // String operands = ";:{}()[]+-/%<=>!&|^~*";
        if (Character.isWhitespace(character.charAt(0))
                || operands.indexOf(character) != -1) {
            MutableAttributeSet attr = keywords.get(character);
            if (attr != null) {
                doc.setCharacterAttributes(startOffset, startOffset+1, attr, false);
                doc.setCharacterAttributes(startOffset+1, startOffset+2, normal, false);
            }
            return true;
        } else {
            return false;
        }
    }
 */
    /*
     *  Override for other languages
     */
    protected boolean isQuoteDelimiter(String character) {
        String quoteDelimiters = "\"'";
        return quoteDelimiters.contains(character);
    }
 
    /*
     *  Override for other languages
     */
    protected boolean isHexCharDelimiter(String character) {
        String quoteDelimiters = "#";
        return quoteDelimiters.contains(character);
    }
    
    /*
     *  Override for other languages
     */
    protected boolean isJumpDelimiter(String character) {
        String quoteDelimiters = ".";
        switch(compiler) {
            case ConstantsR64.COMPILER_ACME:
                quoteDelimiters = ".";
                break;
            case ConstantsR64.COMPILER_KICKASSEMBLER:
                quoteDelimiters = "!";
                break;
            case ConstantsR64.COMPILER_64TASS:
                return false;
        }
        return quoteDelimiters.contains(character);
    }
    
    /*
     *  Override for other languages
     */
    protected boolean isBinCharDelimiter(String character) {
        String quoteDelimiters1 = "#%";
        String quoteDelimiters2 = "%";
        return (quoteDelimiters1.contains(character) || quoteDelimiters2.contains(character));
    }
    
    /*
     *  Override for other languages
     */
    protected boolean isLoHiByteDelimiter(String character) {
        String quoteDelimiters = "#<";
        if (!quoteDelimiters.contains(character)) {
            quoteDelimiters = "#>";
            return quoteDelimiters.contains(character);
        } else {
            return true;
        }
    }
    
    /*
     *  Override for other languages
     */
    protected boolean isHexAddressDelimiter(String character) {
        String quoteDelimiters = "$";
        return quoteDelimiters.contains(character);
    }
    
    /*
     *  Override for other languages
     */
    protected String getStartDelimiter() {
        return "/*";
    }
 
    /*
     *  Override for other languages
     */
    protected String getEndDelimiter() {
        return "*/";
    }
 
    /*
     *  Override for other languages
     */
    protected String getSingleLineDelimiter() {
        return singleLineComment;
    }
 
    /*
     *  Override for other languages
     */
    protected String getEscapeString(String quoteDelimiter) {
        return "\\" + quoteDelimiter;
    }
 
    protected String addMatchingBrace(int offset) throws BadLocationException {
        StringBuilder whiteSpace = new StringBuilder(16);
        int line = rootElement.getElementIndex(offset);
        int i = rootElement.getElement(line).getStartOffset();
        while (true) {
            String temp = doc.getText(i, 1);
            if (temp.equals(" ") || temp.equals("\t")) {
                whiteSpace.append(temp);
                i++;
            } else {
                break;
            }
        }
        return "{\n" + whiteSpace.toString() + "\t\n" + whiteSpace.toString() + "}";
    }
 
    /** 
     * gets the current font size
     * @return  
     */
    public int getFontSize() {
        return fontSize;
    }
 
    /**
     * sets the current font size (affects all built-in styles)
     * @param fontSize
     */
    private void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        StyleConstants.setFontSize(normal, fontSize);
        StyleConstants.setFontSize(quote, fontSize);
        StyleConstants.setFontSize(number, fontSize);
        StyleConstants.setFontSize(hexa, fontSize);
        StyleConstants.setFontSize(lohi, fontSize);
        StyleConstants.setFontSize(binary, fontSize);
        StyleConstants.setFontSize(jump, fontSize);
        StyleConstants.setFontSize(comment, fontSize);
    }
 
    /** 
     * gets the current font family
     * @return 
     */
    public String getFontName() {
        return fontName;
    }
 
    /** 
     * sets the current font family (affects all built-in styles)
     * @param fontName
     */
    public void setFontName(String fontName) {
        this.fontName = fontName;
        StyleConstants.setFontFamily(normal, fontName);
        StyleConstants.setFontFamily(quote, fontName);
        StyleConstants.setFontFamily(number, fontName);
        StyleConstants.setFontFamily(hexa, fontName);
        StyleConstants.setFontFamily(lohi, fontName);
        StyleConstants.setFontFamily(binary, fontName);
        StyleConstants.setFontFamily(jump, fontName);
        StyleConstants.setFontFamily(comment, fontName);
    }
}
