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

package de.relaunch64.popelganda.Editor;

import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.util.Tools;
import de.relaunch64.popelganda.assemblers.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Deque;
import java.util.LinkedList;

/**
 *
 * @author Daniel Lüdecke
 */
public class LabelExtractor {
    /**
     * Retrieves a list of all labels from the current activated source code
     * (see {@link #getActiveSourceCode()}) that start with the currently
     * typed characters at the caret position (usually passed as parameter
     * {@code subWord}.
     * 
     * @param subWord A string which filters the list of labels. Only labels that start with
     * {@code subWord} will
     * @param source
     * @param compiler
     * 
     * @return An object array of sorted labels, where only those labels are returned that start with {@code subWord}.
     */
    public static Object[] getLabelNames(String subWord, String source, int compiler) {
        // get labels here
        ArrayList<String> labels = getLabelNames(false, source, compiler);
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
     * @param source
     * @param compiler
     * @return An array list of all label names from the source code.
     */
    public static ArrayList getLabelNames(boolean sortList, String source, int compiler) {
        // init return value
        ArrayList<String> retval = new ArrayList<>();
        // retrieve sections
        LinkedHashMap<Integer, String> map = getLabels(source, compiler);
        // check for valid value
        if (map!=null && !map.isEmpty()) {
            // retrieve only string values of sections
            Collection<String> vs = map.values();
            // create iterator
            Iterator<String> i = vs.iterator();
            // add all ssction names to return value
            while(i.hasNext()) retval.add(i.next());
            // return result
            return retval;
        }
        return null;
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
        StringReader sr = new StringReader(source);
        BufferedReader br = new BufferedReader(sr);
        LineNumberReader lineReader = new LineNumberReader(br);
        Assembler ass;     // TODO: this should be used instead of compiler
        switch (compiler) {
            case ConstantsR64.COMPILER_64TASS:
                ass = new Assembler_64tass();
                break;
            case ConstantsR64.COMPILER_ACME:
                ass = new Assembler_acme();
                break;
            case ConstantsR64.COMPILER_KICKASSEMBLER:
                ass = new Assembler_kick();
                break;
            case ConstantsR64.COMPILER_CA65:
                ass = new Assembler_ca65();
                break;
            case ConstantsR64.COMPILER_DREAMASS:
                ass = new Assembler_dreamass();
                break;
            case ConstantsR64.COMPILER_DASM:
                ass = new Assembler_dasm();
                break;
            case ConstantsR64.COMPILER_TMPX:
                ass = new Assembler_tmpx();
                break;
            default:
                ass = new Assembler_dummy();
                break;
        }
        return ass.getLabels(lineReader);
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
