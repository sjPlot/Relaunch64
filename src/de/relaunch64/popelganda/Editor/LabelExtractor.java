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

import de.relaunch64.popelganda.assemblers.Assembler;
import java.io.BufferedReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 *
 * @author Daniel Lüdecke
 * @author Soci / Singular
 */
public class LabelExtractor {

    /**
     * Retrieves a list of all labels from the current activated source code (see
     * {@link de.relaunch64.popelganda.Editor.EditorPanes#getActiveSourceCode() getActiveSourceCode()})
     * that start with the currently typed characters at the caret position (usually passed as
     * parameter {@code subWord}.
     *
     * @param subWord A string which filters the list of labels. Only labels that start with
     * {@code subWord} will
     * @param labels An array list of type String with all label names, which can be retrieved by
     * {@link #getNames(java.util.LinkedHashMap) getNames()}.
     *
     * @return An object array of sorted labels, where only those labels are returned that start
     * with {@code subWord}.
     */
    public static ArrayList<String> getSubNames(String subWord, ArrayList<String> labels) {
        // check for valid values
        if (null == labels || labels.isEmpty()) {
            return new ArrayList<>();
        }
        // remove all labels that do not start with already typed chars
        if (!subWord.isEmpty()) {
            for (int i = labels.size() - 1; i >= 0; i--) {
                if (!labels.get(i).startsWith(subWord)) {
                    labels.remove(i);
                }
            }
        }
        // return as object array
        return labels;
    }

    /**
     * Retrieves a list of all label names from the current activated source code (see
     * {@link de.relaunch64.popelganda.Editor.EditorPanes#getActiveSourceCode() getActiveSourceCode()}).
     *
     * @param map a linked hashmap, retrieved by
     * {@link #getLabels(java.lang.String, de.relaunch64.popelganda.assemblers.Assembler, int) getLabels()}.
     * The hashmap is saved in
     * {@link de.relaunch64.popelganda.assemblers.Assembler.labelList Assembler.labelList}.
     *
     * @return An array list of type String with all label names from the source code.
     */
    public static ArrayList<String> getNames(LinkedHashMap<String, Integer> map) {
        return new ArrayList<>(map.keySet());
    }

    /**
     * This method retrieves all labels from the current activated source code (see
     * {@link de.relaunch64.popelganda.Editor.EditorPanes#getActiveSourceCode() getActiveSourceCode()})
     * and returns both line number of labels and label names as linked HashMap.
     * <br><br>
     * The names only can be extracted with {@link #getNames(java.util.LinkedHashMap) getNames()},
     * while line numbers can be accessed via
     * {@link #getLineNumbers(java.util.LinkedHashMap) getLineNumbers()}.
     *
     * @param source the source code from where labels should be extracted.
     * @param assembler a reference to the
     * {@link de.relaunch64.popelganda.assemblers.Assembler Assembler-class}.
     * @param lineNumber the line number from where to start the labels. To extract labels from the
     * whole source, use 0. Else use the linenumbers where subzones start.
     *
     * @return All labels with their line numbers, or {@code null} if there are no labels in the
     * source code.
     */
    public static Assembler.labelList getLabels(String source, Assembler assembler, int lineNumber) {
        BufferedReader br = new BufferedReader(new StringReader(source));
        LineNumberReader lineReader = new LineNumberReader(br);
        return assembler.getLabels(lineReader, lineNumber);
    }

    /**
     * Retrieves a list of all label line numbers from the current activated source code (see
     * {@link de.relaunch64.popelganda.Editor.EditorPanes#getActiveSourceCode() getActiveSourceCode()}).
     *
     * @param map a linked hashmap, retrieved by
     * {@link #getLabels(java.lang.String, de.relaunch64.popelganda.assemblers.Assembler, int) getLabels()}.
     * The hashmap is saved in
     * {@link de.relaunch64.popelganda.assemblers.Assembler.labelList Assembler.labelList}.
     *
     * @return An array list of type Integer with all label line numbers from the source code.
     */
    public static ArrayList<Integer> getLineNumbers(LinkedHashMap<String, Integer> map) {
        return new ArrayList<>(map.values());
    }
}
