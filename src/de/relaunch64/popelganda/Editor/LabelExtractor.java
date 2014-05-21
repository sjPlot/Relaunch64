/*
 * Copyright (C) 2014 Luedeke
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

import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.Tools;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 *
 * @author Luedeke
 */
public class LabelExtractor {
    public static boolean isValidLabel(String keyword, int compiler) {
        // check for valid chars
        keyword = keyword.trim();
        return !keyword.startsWith(RL64TextArea.getMacroString(compiler)) && !keyword.startsWith(RL64TextArea.getCommentString(compiler)) && !keyword.startsWith("$") && !keyword.startsWith("#");
    }
    public static String getLabelFromLine(String line, int compiler, boolean removeLastColon) {
        // check for valid chars
        line = line.trim();
        // check if we have valid label start
        if (isValidLabel(line, compiler)) {
            // separator strings
            int i = 0;
            String addDelim;
            switch (compiler) {
                case ConstantsR64.COMPILER_ACME:
                case ConstantsR64.COMPILER_64TASS:
                    addDelim=":";
                    break;
                case ConstantsR64.COMPILER_KICKASSEMBLER:
                case ConstantsR64.COMPILER_CA65:
                case ConstantsR64.COMPILER_DREAMASS:
                case ConstantsR64.COMPILER_DASM:
                    addDelim="";
                    break;
                default:
                    addDelim="";
                    break;
            }
            String keyword;
            try {
                while (!Tools.isDelimiter(line.substring(i, i+1), addDelim) && i<line.length()) i++;
                keyword = line.substring(0, i);
            }
            catch (IndexOutOfBoundsException ex) {
                keyword = line.substring(0, line.length());
            }
            // in case of kickassembler, we need to remove colon from auto-suggestion
            if (keyword.endsWith(":") && removeLastColon) keyword = keyword.substring(0, keyword.length()-1);
            // check if a) keyword is not null and not empty, b) is longer than 2 chars and 3) is not an ASM keyword
            if (keyword!=null && !keyword.isEmpty() && !ColorSchemes.getKeywordList().contains(keyword.toUpperCase())) return keyword;
        }
        return null;
    }
    /**
     * Retrieves a list of all labels from the current activated source code
     * (see {@link #getActiveSourceCode()}) that start with the currently
     * typed characters at the caret position (usually passed as parameter
     * {@code subWord}.
     * 
     * @param subWord A string which filters the list of labels. Only labels that start with
     * {@code subWord} will
     * @param removeLastColon returned in this list.
     * @param source
     * @param compiler
     * 
     * @return An object array of sorted labels, where only those labels are returned that start with {@code subWord}.
     */
    public static Object[] getLabelNames(String subWord, boolean removeLastColon, String source, int compiler) {
        // get labels here
        ArrayList<String> labels = LabelExtractor.getLabelNames(false, removeLastColon, source, compiler);
        // check for valid values
        if (null==labels || labels.isEmpty()) return null;
//        labels.stream().forEach((label) -> {
//            System.out.println(label);
//        });
        // remove all labels that do not start with already typed chars
        if (!subWord.isEmpty()) {
            for (int i=labels.size()-1; i>=0; i--) {
                if (!labels.get(i).startsWith(subWord)) labels.remove(i);
            }
        }
        // sort list
        Collections.sort(labels);
        // return as object array
        return labels.toArray();
    }
    /**
     * Retrieves a list of all labels from the current activated source code
     * (see {@link #getActiveSourceCode()}).
     * 
     * @param sortList If {@code true}, labels are sorted in alphabetical order.
     * @param removeLastColon
     * @param source
     * @param compiler
     * @return An array list of all label names from the source code.
     */
    public static ArrayList getLabelNames(boolean sortList, boolean removeLastColon, String source, int compiler) {
        // prepare return values
        ArrayList<String> labelValues = new ArrayList<>();
        String line;
        // go if not null
        if (source!=null) {
            // create buffered reader, needed for line number reader
            BufferedReader br = new BufferedReader(new StringReader(source));
            LineNumberReader lineReader = new LineNumberReader(br);
            // read line by line
            try {
                while ((line = lineReader.readLine())!=null) {
                    // extract label
                    String keyword = getLabelFromLine(line, compiler, removeLastColon);
                    // check if we have valid label and if it's new. If yes, add to results
                    if (keyword!=null && !labelValues.contains(keyword)) {
                        labelValues.add(keyword);
                    }
                }
                // sort list
                if (sortList) Collections.sort(labelValues);
            }
            catch (IOException ex) {
            }
        }
        return labelValues;
    }
    /**
     * This method retrieves all labels from the current activated source code
     * (see {@link #getActiveSourceCode()}) and returns both line number of label
     * and label name as linked HashMap.
     * 
     * @param source
     * @param compiler
     * @return All labels with their line numbers, or {@code null} if there are no labels
     * in the source code.
     */
    public static LinkedHashMap getLabels(String source, int compiler) {
        // prepare return values
        LinkedHashMap<Integer, String> labelValues = new LinkedHashMap<>();
        // init vars
        int lineNumber = 0;
        String line;
        // go if not null
        if (source!=null) {
            // create buffered reader, needed for line number reader
            BufferedReader br = new BufferedReader(new StringReader(source));
            LineNumberReader lineReader = new LineNumberReader(br);
            // read line by line
            try {
                while ((line = lineReader.readLine())!=null) {
                    // increase line counter
                    lineNumber++;
                    //reset the input (matcher)
                    String keyword = getLabelFromLine(line, compiler, false);
                    // check if we have valid label and if it's new. If yes, add to results
                    if (keyword!=null && !labelValues.containsValue(keyword)) {
                        // if yes, add to return value
                        labelValues.put(lineNumber, keyword);
                    }
                }
            }
            catch (IOException ex) {
            }
        }
        return labelValues;
    }
    public static ArrayList getLabelLineNumbers(String source, int compiler) {
        // init return value
        ArrayList<Integer> retval = new ArrayList<>();
        // retrieve sections
        LinkedHashMap<Integer, String> map = getLabels(source, compiler);
        // check for valid value
        if (map!=null && !map.isEmpty()) {
            // retrieve only string values of sections
            Set<Integer> ks = map.keySet();
            // create iterator
            Iterator<Integer> i = ks.iterator();
            // add all ssction names to return value
            while(i.hasNext()) retval.add(i.next());
            // return result
            return retval;
        }
        return null;
    }
}
