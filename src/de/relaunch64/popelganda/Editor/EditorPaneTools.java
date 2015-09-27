/*
 * Relaunch64 - A Java cross-development IDE for C64 machine language coding.
 * Copyright (C) 2001-2015 by Daniel Lüdecke (http://www.danielluedecke.de)
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

import de.relaunch64.popelganda.util.Tools;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.gjt.sp.jedit.textarea.Selection;

/**
 *
 * @author Daniel Luedecke
 */
public class EditorPaneTools {

    /**
     * (Un-)comments currently selected text.
     *
     * @param ep a reference to the current editor pane (instance of
     * {@link de.relaunch64.popelganda.Editor.RL64TextArea RL64TextArea}).
     * @param commentString the comment string from the current assembler.
     */
    public static void commentLine(RL64TextArea ep, String commentString) {
        // check for text selection
        String selString = ep.getSelectedText();
        // if we have selection, add tab to each selected line
        if (selString != null && !selString.isEmpty()) {
            // get start offset of selection
            int selstart = ep.getSelection(0).getStart();
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
                    chardiff = chardiff - commentString.length();
                } else {
                    // else add comment string
                    l = commentString + l;
                    chardiff = chardiff + commentString.length();
                }
                // append fixed line
                sb.append(l);
                // append new line, if needed
                if (countNewLines > 0) {
                    sb.append("\n");
                }
                countNewLines--;
            }
            // insert string
            ep.replaceSelection(sb.toString());
            // reselect text
            ep.setSelection(new Selection.Range(selstart, selstart + sb.length()));
            ep.requestFocusInWindow();
        }
    }

    /**
     *
     * @param direction
     * @param currentLine
     * @param ln
     * @return
     */
    public static int findJumpToken(int direction, int currentLine, ArrayList<Integer> ln) {
        // check for valid values
        if (null == ln || ln.isEmpty()) {
            return 0;
        }
        // check if we found anything
        switch (direction) {
            case EditorPanes.DIRECTION_NEXT:
                for (Integer ln1 : ln) {
                    // if we found a line number greater than current
                    // line, we found the next label from caret position
                    if (ln1 > currentLine) {
                        return ln1;
                    }
                }
                return ln.get(0);
            case EditorPanes.DIRECTION_PREV:
                // iterate all line numbers
                for (int i = ln.size() - 1; i >= 0; i--) {
                    // if we found a line number smaller than current
                    // line, we found the previous label from caret position
                    if (ln.get(i) < currentLine) {
                        return ln.get(i);
                    }
                }
                return ln.get(ln.size() - 1);
            default:
                return 0;
        }
    }
}
