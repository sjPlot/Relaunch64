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
package de.relaunch64.popelganda.Editor;

import de.relaunch64.popelganda.database.Settings;
import de.relaunch64.popelganda.util.Tools;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyledEditorKit;
import org.gjt.sp.jedit.textarea.Selection;
import org.gjt.sp.jedit.textarea.StandaloneTextArea;

/**
 *
 * @author Daniel Luedecke
 */
public class EditorPaneTools {
    public static void commentLine(StandaloneTextArea ep, int compiler) {
        // retrieve comment string
        String commentString = SyntaxScheme.getCommentString(compiler);
        // check for text selection
        String selString = ep.getSelectedText();
        // if we have selection, add tab to each selected line
        if (selString!=null && !selString.isEmpty()) {
            // remember selection range
            
            // TODO selection setzne
            
//            int selstart = ep.getSelectionStart();
//            int selend = ep.getSelectionEnd();
            int selstart = 1;
            int selend = 2;
            // retrieve lines
            String[] lines = selString.split("\n");
            // count new lines
            int countNewLines = Tools.countMatches(selString, '\n');
            // create string builder for new insert string
            StringBuilder sb = new StringBuilder("");
            // store difference in length of selection
            // after adding / removing comment strings
            int chardiff = 0;
            // add tab infront of each line
            for (String l : lines) {
                // if first char (after possible whitespace) is comment char, 
                // remove it
                if (l.trim().startsWith(commentString)) {
                    // remove comment char
                    l = l.replaceFirst(Pattern.quote(commentString), "");
                    chardiff = chardiff-commentString.length();
                }
                else {
                    // else add comment string
                    l = commentString+l;
                    chardiff = chardiff+commentString.length();
                }
                // append fixed line
                sb.append(l);
                // append new line, if needed
                if (countNewLines>0) sb.append("\n");
                countNewLines--;
            }
            // insert string
            ep.replaceSelection(sb.toString());
            // re-select text
            ep.setSelection(new Selection.Range(selstart, selend+chardiff));
        }
    }
    public static String findJumpToken(int direction, int currentLine, ArrayList<Integer> ln, ArrayList<String> names) {
        // check for valid values
        if (null==ln || null==names) return null;
        String dest = null;
        // check if we found anything
        boolean labelFound = false;
        int wrap = 0;
        switch (direction) {
            case EditorPanes.DIRECTION_NEXT:
                // iterate all line numbers
                for (int i=0; i<ln.size(); i++) {
                    // if we found a line number greater than current
                    // line, we found the next label from caret position
                    if (ln.get(i)>currentLine) {
                        dest = names.get(i);
                        labelFound = true;
                        break;
                    }
                }
                break;
            case EditorPanes.DIRECTION_PREV:
                wrap = ln.size()-1;
                // iterate all line numbers
                for (int i=ln.size()-1; i>=0; i--) {
                    // if we found a line number smaller than current
                    // line, we found the previous label from caret position
                    if (ln.get(i)<currentLine) {
                        dest = names.get(i);
                        labelFound = true;
                        break;
                    }
                }
                break;
        }
        try {
            // found anything?
            // if not, start from beginning
            if (!labelFound) dest = names.get(wrap);
        }
        catch (IndexOutOfBoundsException ex) {
        }
        return dest;
    }
    /**
     * 
     * @param editorPane
     * @param settings
     * @param c 
     * @return  
     */
    public static JEditorPane setSyntaxScheme(JEditorPane editorPane, final Settings settings, int c) {
        // declare compiler var as final
        final int compiler = c;
        // get hash map with keywords
        final HashMap<String, MutableAttributeSet> kw = SyntaxScheme.getKeywordHashMap();
        // get hash map with compiler specific keywords
        final HashMap<String, MutableAttributeSet> ckw = SyntaxScheme.getCompilerKeywordHashMap(compiler);
        // get hash map with script specific keywords
        final HashMap<String, MutableAttributeSet> skw = SyntaxScheme.getScriptKeywordHashMap(compiler);
        // get hash map with illegal opcodes
        final HashMap<String, MutableAttributeSet> io = SyntaxScheme.getIllegalOpcodeHashMap();
        // create new editor kit
        EditorKit editorKit = new StyledEditorKit() {
            // and set highlight scheme
            @Override
            public Document createDefaultDocument() {
                return new SyntaxHighlighting(kw, ckw, skw, io,
                                              SyntaxScheme.getFontName(),
                                              SyntaxScheme.getFontSize(),
                                              SyntaxScheme.getCommentString(compiler),
                                              SyntaxScheme.getDelimiterList(compiler),
                                              SyntaxScheme.getStyleAttributes(),
                                              compiler,
                                              settings.getTabWidth());
            }
        };
        // link editorkit to editorpane
        editorPane.setEditorKitForContentType("text/plain", editorKit);
        editorPane.setContentType("text/plain");
        return editorPane;
    }
}
