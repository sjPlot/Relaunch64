/*
 * Relaunch64 - A Java cross-development IDE for C64 machine language coding.
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
package de.relaunch64.popelganda;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.gjt.sp.jedit.textarea.Selection;
import de.relaunch64.popelganda.Editor.RL64TextArea;

/**
 *
 * @author Daniel Luedecke
 */
public class FindReplace {
    private int findpos = -1;
    private final LinkedList<Integer[]> findselections = new LinkedList<>();
    private String findText;
    private String replaceText;
    private String content;
    private int activeTab = -1;
    private int lastActiveTab = -1;
    private RL64TextArea editorPane = null;
    
    /**
     * 
     * @param ft The find term as string
     * @param rt The replace term as string
     * @param cn The content (of the editor pane) that should be searched through
     * @param at the currently active tab from which the search was startet
     * @param ep the currently active editor pane from which the search was startet
     */
    FindReplace() {
        resetValues();
    }
    public void initValues(String ft, String rt, int at, RL64TextArea ep, boolean isRegEx, boolean wholeWord, boolean matchCase) {
        initValues(ft, rt, at, ep, isRegEx, wholeWord, matchCase, false);
    }
    public void initValues(String ft, String rt, int at, RL64TextArea ep, boolean isRegEx, boolean wholeWord, boolean matchCase, boolean forceInit) {
        boolean newFindTerm = ((ft!=null && findText!=null && !findText.equalsIgnoreCase(ft)) || 
                               (rt!=null && replaceText!=null && !replaceText.equalsIgnoreCase(rt)));
        findText = ft;
        replaceText = rt;
        activeTab = at;
        editorPane = ep;
        // save old content
        String oldContent = content;
        // update content
        updateContent();
        // if user has done many changes, reset matcher
        if (oldContent!=null && content!=null && (Math.abs(oldContent.length()-content.length())>1)) forceInit = true;
        if (newFindTerm || forceInit) initmatcher(isRegEx, wholeWord, matchCase);
    }
    /**
     * 
     */
    public final void resetValues() {
        // init list where we store the start/end-positions of the found terms
        findselections.clear();
        findpos = -1;
        content = "";
        findText = "";
        replaceText = "";
    }
    /**
     * 
     * @return 
     */
    private boolean initmatcher(boolean isRegEx, boolean wholeWord, boolean matchCase) {
        Matcher findmatcher;
        // retrieve findtext
        String text = findText;
        updateContent();
        // if we have no findtext, reset buttons
        if (null==text || text.isEmpty()) {
            resetValues();
            return false;
        }
        // if we have no content, reset buttons
        if (null==content || content.isEmpty()) {
            resetValues();
            return false;
        }
        // set currently used tab
        lastActiveTab = activeTab;
        // create find pattern
        Pattern p;
        // check whether the user wants to find a regular expression or not
        // if not, prepare findterm and surround it with the regular expressions
        // for whole word and matchcase.
        if (!isRegEx) {
            // if the findterm contains meta-chars of a regular expression, although no regular
            // expression search is requested, escape all these meta-chars...
            text = Pattern.quote(text);
            // when we have a whole-word-find&replace, surround findterm with
            // the regular expression that indicates word beginning and ending (i.e. whole word)
            if (wholeWord) text = "\\b"+text+"\\b";
            // when the find & replace is *not* case-sensitive, set regular expression
            // to ignore the case...
            if (!matchCase) text = "(?i)"+text;
            // the final findterm now might look like this:
            // "(?i)\\b<findterm>\\b", in case we ignore case and have whole word search
        }
        try {
            // create a pattern from the first search term. try to compile
            // it, thus considering as a regular expression term
            p = Pattern.compile(text);
        }
        catch (PatternSyntaxException e) {
            // if compiling failed, consider it as usual (non reg ex)
            // search term and re-compile pattern.
            text = Pattern.quote(text);
            p = Pattern.compile(text);
        }
        // now we know we have a valid regular expression. we now want to
        // retrieve all matching groups
        findmatcher = p.matcher(content);
        // init findpos...
        findpos = -1;
        // init list where we store the start/end-positions of the found terms
        findselections.clear();
        // find all matches and copy the start/end-positions to our arraylist
        // we now can easily retrieve the found terms and their positions via this
        // array, thus navigation with find-next and find-prev-buttons is simple
        while (findmatcher.find()) {
            findselections.add(new Integer[] {findmatcher.start(),findmatcher.end()});
        }
        return (findselections.size()>0);
    }
    protected void selectFindTerm() {
        // set caret
        editorPane.setCaretPosition(findselections.get(findpos)[0]);
        editorPane.scrollToCaret(true);
        // select next occurence of find term
        editorPane.setSelection(new Selection.Range(findselections.get(findpos)[0], findselections.get(findpos)[1]));
        editorPane.requestFocusInWindow();
    }
    /**
     * 
     * @param isRegEx
     * @param wholeWord
     * @param matchCase
     * @return 
     */
    public boolean findNext(boolean isRegEx, boolean wholeWord, boolean matchCase) {
        // when we have no founds or when the user changed the tab, init matcher
        if (findselections.isEmpty() || lastActiveTab!=activeTab) {
            initmatcher(isRegEx, wholeWord, matchCase);
        }
        // check whether we have any found at all
        if (findselections.size()>0) {
            try {
                // increase our find-counter
                findpos++;
                // as long as we haven't reached the last match...
                if (findpos<findselections.size()) {
                    // when we have a negative index (might be possible, when
                    // using the "findPrev"-method and the first match was found.
                    // in this case, findpos was zero and by "findpos--" it was decreased to -1
                    if (findpos<0) {
                        findpos=0;
                    }
                }
                else {
                    findpos=0;
                }
                // select next occurence of find term
                selectFindTerm();
            }
            catch (IllegalArgumentException ex) {
            }
        }
        else {
            // reset find values
            resetValues();
            // nothing found
            return false;
        }
        return true;
    }
    public boolean findPrev() {
        // check whether we have any found at all
        if (findselections.size()>0) {
            // decrease our find-counter
            findpos--;
            // as long as we havem't reached the last match...
            if (findpos>=0) {
                // when we have a larger index that array-size (might be possible, when
                // using the "findNext"-method and the last match was found.
                // in this case, findpos is equal to the array-size and by "findpos++"
                // it was increased to a larger index than array size
                if (findpos>=findselections.size()) {
                    findpos=findselections.size()-1;
                }
            }
            // when we reached the first match, start from end again
            else {
                findpos = findselections.size()-1;
            }
            // select next occurence of find term
            selectFindTerm();
        }
        else {
            resetValues();
            return false;
        }
        return true;
    }
    public boolean replace(boolean isRegEx, boolean wholeWord, boolean matchCase) {
        if (editorPane.getText()!=null) {
            if (editorPane.getSelectedText()!=null) {
                editorPane.replaceSelection(replaceText);
            }
            if (initmatcher(isRegEx, wholeWord, matchCase)) {
                findNext(isRegEx, wholeWord, matchCase);
            }
            else {
                resetValues();
                return false;
            }
        }
        else {
            resetValues();
            return false;
        }
        return true;
    }
    public void replaceAll(boolean isRegEx, boolean wholeWord, boolean matchCase) {
        if (initmatcher(isRegEx, wholeWord, matchCase)) {
            for (int cnt=findselections.size()-1;cnt>=0; cnt--) {
                // select next occurence of find term
                editorPane.setSelection(new Selection.Range(findselections.get(findpos)[0], findselections.get(findpos)[1]));
                if (editorPane.getSelectedText()!=null) {
                    editorPane.replaceSelection(replaceText);
                }
            }
        }
        if (findselections.size()>0) {
            editorPane.setCaretPosition(findselections.get(findselections.size()-1)[1]);
            // reset values
            resetValues();
        }
    }
    private void updateContent() {
        // check for values
        if (editorPane.getText().length()>0) {
            content = editorPane.getText();
        }
    }
}
