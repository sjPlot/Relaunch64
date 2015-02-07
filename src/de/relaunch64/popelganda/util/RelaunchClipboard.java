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
package de.relaunch64.popelganda.util;

import java.util.ArrayList;

/**
 *
 * @author Daniel Luedecke
 */
public class RelaunchClipboard {
    
    private final ArrayList<String> clipboards = new ArrayList<>();
    private static final int MAX_COUNT = 10;
    
    public RelaunchClipboard() {
        clear();
    }
    private void clear() {
        clipboards.clear();
    }
    public void addToClipboard(String content) {
        if (content!=null && !content.isEmpty()) {
            if (clipboards.size()<MAX_COUNT) {
                clipboards.add(content);
            }
            else {
                rotateClipboard(content);
            }
        }
    }
    public void rotateClipboard(String addContent) {
        for (int i=1; i<clipboards.size(); i++) {
            clipboards.set(i-1, clipboards.get(i));
        }
        clipboards.remove(MAX_COUNT-1);
        if (addContent!=null && !addContent.isEmpty()) {
            clipboards.add(addContent);
        }
    }
    public String getClipboard(int index) {
        try {
            return clipboards.get(index);
        }
        catch (IndexOutOfBoundsException ex) {
            return "";
        }
    }
}
