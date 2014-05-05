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

import java.awt.Color;

/**
 *
 * @author Daniel Luedecke
 */
public class HighlightSchemes {
    public static final int SCHEME_DEFAULT = 0;
    public static final int SCHEME_PAIRED = 1;
    public static final int SCHEME_C64 = 2;
    public static final String[] SCHEME_NAMES = new String[] {"Default", "Paired", "C64 Colors"};
    
    public static final int COLOR_NORMAL = 0;
    public static final int COLOR_COMMENT = 1;
    public static final int COLOR_STRING = 2;
    public static final int COLOR_NUMBER = 3;
    public static final int COLOR_HEX = 4;
    public static final int COLOR_BIN = 5;
    public static final int COLOR_JUMP = 6;
    public static final int COLOR_MACRO = 7;
    public static final int COLOR_LOHI = 8;
    public static final int COLOR_KEYWORD = 9;
    public static final int COLOR_COMPILERKEYWORD = 10;
    public static final int COLOR_ILLEGALOPCODE = 11;
    
    public static Color getColor(int scheme, int tokentype) {
        if (SCHEME_DEFAULT==scheme) {
            switch (tokentype) {
                case COLOR_NORMAL: return Color.BLACK;
                case COLOR_COMMENT: return new java.awt.Color(150, 150, 150); // grey
                case COLOR_STRING: return new java.awt.Color(220, 0, 220); //dark pink
                case COLOR_NUMBER: return new java.awt.Color(160, 0, 0); // red
                case COLOR_HEX: return new java.awt.Color(0, 120, 0); // green
                case COLOR_BIN: return new java.awt.Color(0, 120, 120); // cyan
                case COLOR_JUMP: return new java.awt.Color(206, 103, 0); // brown
                case COLOR_MACRO: return new java.awt.Color(206, 103, 0); // brown
                case COLOR_LOHI: return new java.awt.Color(128, 128, 0); // olive
                case COLOR_KEYWORD: return new java.awt.Color(0, 0, 200); // dark blue
                case COLOR_COMPILERKEYWORD: return new java.awt.Color(70, 130, 180); // dark cyan
                case COLOR_ILLEGALOPCODE: return new java.awt.Color(255, 69, 0); // orange red
            }
        }
        else if (SCHEME_PAIRED==scheme) {
            switch (tokentype) {
                case COLOR_NORMAL: return Color.BLACK;
                case COLOR_COMMENT: return new java.awt.Color(178,223,138);
                case COLOR_STRING: return new java.awt.Color(166,206,227);
                case COLOR_NUMBER: return new java.awt.Color(253,191,111);
                case COLOR_HEX: return new java.awt.Color(227,26,28);
                case COLOR_BIN: return new java.awt.Color(253,191,111);
                case COLOR_JUMP: return new java.awt.Color(202,178,214);
                case COLOR_MACRO: return new java.awt.Color(202,178,214);
                case COLOR_LOHI: return new java.awt.Color(251,154,153);
                case COLOR_KEYWORD: return new java.awt.Color(51,160,44);
                case COLOR_COMPILERKEYWORD: return new java.awt.Color(31,120,180);
                case COLOR_ILLEGALOPCODE: return new java.awt.Color(106,61,154);
            }
        }
        else if (SCHEME_C64==scheme) {
            switch (tokentype) {
                case COLOR_NORMAL: return Color.BLACK;
                case COLOR_COMMENT: return new java.awt.Color(192,192,192);
                case COLOR_STRING: return new java.awt.Color(60,179,113); // not original green
                case COLOR_NUMBER: return new java.awt.Color(156,116,72);
                case COLOR_HEX: return new java.awt.Color(224,160,64);
                case COLOR_BIN: return new java.awt.Color(84,84,84);
                case COLOR_JUMP: return new java.awt.Color(224,96,224); 
                case COLOR_MACRO: return new java.awt.Color(255,160,160);
                case COLOR_LOHI: return new java.awt.Color(95,158,160); // not original cyan
                case COLOR_KEYWORD: return new java.awt.Color(64,64,224);
                case COLOR_COMPILERKEYWORD: return new java.awt.Color(224,64,64);
                case COLOR_ILLEGALOPCODE: return new java.awt.Color(160,160,255);
            }
        }
        return Color.BLACK;
    }
}
