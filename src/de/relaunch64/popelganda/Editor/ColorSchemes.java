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
public class ColorSchemes {
    public static final int SCHEME_DEFAULT = 0;
    public static final int SCHEME_PAIRED = 1;
    public static final int SCHEME_C64 = 2;
    public static final int SCHEME_HELLO_KITTY = 3;
    public static final int SCHEME_DARK = 4;
    public static final int SCHEME_DARK2 = 5;
    public static final int SCHEME_BLUES = 6;
    public static final int SCHEME_GITHUB = 7;
    public static final int SCHEME_SOLAR_LIGHT = 8;
    public static final int SCHEME_SOLAR_DARK = 9;
    public static final int SCHEME_POPELGANDA = 10;
    public static final int SCHEME_GREENBERET = 11;
    public static final int SCHEME_GRAY = 12;
    public static final int SCHEME_DEFAULT_REDUCED = 13;
    public static final int SCHEME_POPELGANDA_REDUCED = 14;
    public static final int SCHEME_C64_REDUCED = 15;

    public static final String[] SCHEME_NAMES = new String[] {"Default", "Paired", "C64", "Hello Kitty", "Dark", "Dark 2", "Blue Max", "GitHub", "Solarized (light)", "Solarized (dark)", "Popelganda", "Green Beret", "Grayskull", "Reduced (light)", "Reduced (dark)", "Reduced (C64)"};
    
    public static final int COLOR_NORMAL = 0;
    public static final int COLOR_COMMENT = 1;
    public static final int COLOR_STRING = 2;
    public static final int COLOR_NUMBER = 3;
    public static final int COLOR_HEX = 4;
    public static final int COLOR_BIN = 5;
    public static final int COLOR_JUMP = 6;
    public static final int COLOR_ILLEGALOPCODE = 7;
    public static final int COLOR_LOHI = 8;
    public static final int COLOR_KEYWORD = 9;
    public static final int COLOR_COMPILERKEYWORD = 10;
    public static final int COLOR_SCRIPTKEYWORD = 11;
    
    public static final int BACKGROUND = 99;
    public static final int LINE_BACKGROUND = 98;
    public static final int LINE_BORDER = 97;
    public static final int LINE_COLOR = 96;
    public static final int LINE_HIGHLIGHT = 95;
    
    public static Color getColor(int scheme, int tokentype) {
        switch (scheme) {
        case SCHEME_DEFAULT:
            switch (tokentype) {
                case COLOR_NORMAL: return Color.BLACK;
                case COLOR_COMMENT: return new Color(150, 150, 150); // grey
                case COLOR_STRING: return new Color(220, 0, 220); //dark pink
                case COLOR_NUMBER: return new Color(160, 0, 0); // red
                case COLOR_HEX: return new Color(0, 120, 0); // green
                case COLOR_BIN: return new Color(0, 120, 120); // cyan
                case COLOR_JUMP: return new Color(206, 103, 0); // brown
                case COLOR_LOHI: return new Color(128, 128, 0); // olive
                case COLOR_KEYWORD: return new Color(0, 0, 200); // dark blue
                case COLOR_COMPILERKEYWORD: return new Color(70, 130, 180); // dark cyan
                case COLOR_SCRIPTKEYWORD: return new Color(128, 128, 0); // olive
                case COLOR_ILLEGALOPCODE: return new Color(255, 69, 0); // orange red
                case BACKGROUND: return new Color(255, 255, 255); // white
                case LINE_BACKGROUND: return new Color(248, 248, 248); // light gray
                case LINE_BORDER: return new Color(240, 240, 240); // light gray
                case LINE_COLOR: return new Color(0,0,0); // black
                case LINE_HIGHLIGHT: return new Color(220, 20, 60); // red
            }
            break;
        case SCHEME_PAIRED:
            switch (tokentype) {
                case COLOR_NORMAL: return Color.BLACK;
                case COLOR_COMMENT: return new Color(189,183,107);
                case COLOR_STRING: return new Color(25,25,112);
                case COLOR_NUMBER: return new Color(210,105,30);
                case COLOR_HEX: return new Color(227,26,28);
                case COLOR_BIN: return new Color(253,191,111);
                case COLOR_JUMP: return new Color(199,21,133);
                case COLOR_LOHI: return new Color(0,128,128);
                case COLOR_KEYWORD: return new Color(51,160,44);
                case COLOR_COMPILERKEYWORD: return new Color(31,120,180);
                case COLOR_SCRIPTKEYWORD: return new Color(0,128,128);
                case COLOR_ILLEGALOPCODE: return new Color(106,61,154);
                case BACKGROUND: return new Color(252,252,252); // white
                case LINE_BACKGROUND: return new Color(248, 248, 248); // light gray
                case LINE_BORDER: return new Color(244, 244, 244); // light gray
                case LINE_COLOR: return new Color(0,0,0); // black
                case LINE_HIGHLIGHT: return new Color(220, 20, 60); // red
            }
            break;
        case SCHEME_C64:
            switch (tokentype) {
                case COLOR_NORMAL: return Color.white;
                case COLOR_COMMENT: return new Color(136,136,136);
                case COLOR_STRING: return new Color(64,224,64);
                case COLOR_NUMBER: return new Color(255,160,160);
                case COLOR_HEX: return new Color(224,160,64);
                case COLOR_BIN: return new Color(192,192,192);
                case COLOR_JUMP: return new Color(224,96,224); 
                case COLOR_LOHI: return new Color(96,255,255);
                case COLOR_KEYWORD: return new Color(160,160,255);
                case COLOR_COMPILERKEYWORD: return new Color(255,255,64);
                case COLOR_SCRIPTKEYWORD: return new Color(96,255,255);
                case COLOR_ILLEGALOPCODE: return new Color(224,64,64);
                case BACKGROUND: return new Color(64,64,224);
                case LINE_BACKGROUND: return new Color(160,160,255);
                case LINE_BORDER: return new Color(160,160,255);
                case LINE_COLOR: return new Color(64,64,224); 
                case LINE_HIGHLIGHT: return new Color(255,255,255);
            }
            break;
        case SCHEME_HELLO_KITTY:
            switch (tokentype) {
                case COLOR_NORMAL: return new Color(220,20,60);
                case COLOR_COMMENT: return new Color(119,136,153);
                case COLOR_STRING: return new Color(139,0,139);
                case COLOR_NUMBER: return new Color(218,112,214);
                case COLOR_HEX: return new Color(199,21,133);
                case COLOR_BIN: return new Color(216,191,216);
                case COLOR_JUMP: return new Color(240,128,128); 
                case COLOR_LOHI: return new Color(221,160,221);
                case COLOR_KEYWORD: return new Color(219,112,147);
                case COLOR_COMPILERKEYWORD: return new Color(205,92,92);
                case COLOR_SCRIPTKEYWORD: return new Color(221,160,221);
                case COLOR_ILLEGALOPCODE: return new Color(255,99,71);
                case BACKGROUND: return new Color(255,250,250);
                case LINE_BACKGROUND: return new Color(255,240,245);
                case LINE_BORDER: return new Color(255,182,193);
                case LINE_COLOR: return new Color(255,182,193);
                case LINE_HIGHLIGHT: return new Color(220,20,60);
            }
            break;
        case SCHEME_DARK:
            switch (tokentype) {
                case COLOR_NORMAL: return new Color(245,245,245);
                case COLOR_COMMENT: return new Color(192,192,192);
                case COLOR_STRING: return new Color(240,230,140);
                case COLOR_NUMBER: return new Color(100,149,237);
                case COLOR_HEX: return new Color(176,196,222);
                case COLOR_BIN: return new Color(175,238,238);
                case COLOR_JUMP: return new Color(219,112,147); 
                case COLOR_LOHI: return new Color(143,188,143);
                case COLOR_KEYWORD: return new Color(173,216,230);
                case COLOR_COMPILERKEYWORD: return new Color(221,160,221);
                case COLOR_SCRIPTKEYWORD: return new Color(143,188,143);
                case COLOR_ILLEGALOPCODE: return new Color(255,192,203);
                case BACKGROUND: return new Color(55,55,55);
                case LINE_BACKGROUND: return new Color(48,48,48);
                case LINE_BORDER: return new Color(48,48,48);
                case LINE_COLOR: return new Color(128,128,128);
                case LINE_HIGHLIGHT: return new Color(211,211,211);
            }
            break;
        case SCHEME_BLUES:
            switch (tokentype) {
                case COLOR_NORMAL: return new Color(112,128,144);
                case COLOR_COMMENT: return new Color(176,196,222);
                case COLOR_STRING: return new Color(65,105,225);
                case COLOR_NUMBER: return new Color(0,139,139);
                case COLOR_HEX: return new Color(218,112,214);
                case COLOR_BIN: return new Color(100,149,237);
                case COLOR_JUMP: return new Color(0,191,255); 
                case COLOR_LOHI: return new Color(95,158,160);
                case COLOR_KEYWORD: return new Color(70,130,180);
                case COLOR_COMPILERKEYWORD: return new Color(72,61,139);
                case COLOR_SCRIPTKEYWORD: return new Color(95,158,160);
                case COLOR_ILLEGALOPCODE: return new Color(0,0,205);
                case BACKGROUND: return new Color(248,248,255);
                case LINE_BACKGROUND: return new Color(240,248,255);
                case LINE_BORDER: return new Color(70,130,180);
                case LINE_COLOR: return new Color(0,0,139);
                case LINE_HIGHLIGHT: return new Color(199,21,133);
            }
            break;
        case SCHEME_SOLAR_LIGHT:
            switch (tokentype) {
                case COLOR_NORMAL: return new Color(90,111,117);
                case COLOR_COMMENT: return new Color(148,162,162);
                case COLOR_STRING: return new Color(53,162,152);
                case COLOR_NUMBER: return new Color(134,152,23);
                case COLOR_HEX: return new Color(180,136,24);
                case COLOR_BIN: return new Color(109,116,195);
                case COLOR_JUMP: return new Color(217,47,52); 
                case COLOR_LOHI: return new Color(200,73,31);
                case COLOR_KEYWORD: return new Color(49,142,209);
                case COLOR_COMPILERKEYWORD: return new Color(11,55,66);
                case COLOR_SCRIPTKEYWORD: return new Color(200,73,31);
                case COLOR_ILLEGALOPCODE: return new Color(208,54,130);
                case BACKGROUND: return new Color(253,246,229);
                case LINE_BACKGROUND: return new Color(238,233,214); // 253,246,229
                case LINE_BORDER: return new Color(238,233,214);
                case LINE_COLOR: return new Color(148,162,162);
                case LINE_HIGHLIGHT: return new Color(11,55,66);
            }
            break;
        case SCHEME_SOLAR_DARK:
            switch (tokentype) {
                case COLOR_NORMAL: return new Color(148,162,162); 
                case COLOR_COMMENT: return new Color(90,111,117);
                case COLOR_STRING: return new Color(53,162,152);
                case COLOR_NUMBER: return new Color(134,152,23);
                case COLOR_HEX: return new Color(49,142,209);
                case COLOR_BIN: return new Color(109,116,195);
                case COLOR_JUMP: return new Color(180,136,24); 
                case COLOR_LOHI: return new Color(200,73,31);
                case COLOR_KEYWORD: return new Color(238,233,214);
                case COLOR_COMPILERKEYWORD: return new Color(217,47,52);
                case COLOR_SCRIPTKEYWORD: return new Color(200,73,31);
                case COLOR_ILLEGALOPCODE: return new Color(208,54,130);
                case BACKGROUND: return new Color(11,55,66);
                case LINE_BACKGROUND: return new Color(3,44,54); // 11,55,66
                case LINE_BORDER: return new Color(3,44,54);
                case LINE_COLOR: return new Color(132,139,151);
                case LINE_HIGHLIGHT: return new Color(253,246,229);
            }
            break;
        case SCHEME_DARK2:
            switch (tokentype) {
                case COLOR_NORMAL: return new Color(128,137,139); 
                case COLOR_COMMENT: return new Color(64,69,70);
                case COLOR_STRING: return new Color(233,170,145);
                case COLOR_NUMBER: return new Color(228,101,184);
                case COLOR_HEX: return new Color(97,172,123);
                case COLOR_BIN: return new Color(97,172,123);
                case COLOR_JUMP: return new Color(72,209,204); 
                case COLOR_LOHI: return new Color(199,116,68);
                case COLOR_KEYWORD: return new Color(128,180,192);
                case COLOR_COMPILERKEYWORD: return new Color(157,162,101);
                case COLOR_SCRIPTKEYWORD: return new Color(199,116,68);
                case COLOR_ILLEGALOPCODE: return new Color(4,136,174);
                case BACKGROUND: return new Color(15,17,18);
                case LINE_BACKGROUND: return new Color(10,11,12); // 11,55,66
                case LINE_BORDER: return new Color(10,11,12);
                case LINE_COLOR: return new Color(102,102,102);
                case LINE_HIGHLIGHT: return new Color(4,136,174);
            }
            break;
        case SCHEME_GITHUB:
            switch (tokentype) {
                case COLOR_NORMAL: return new Color(0,0,0); 
                case COLOR_COMMENT: return new Color(160,160,160);
                case COLOR_STRING: return new Color(216,23,69);
                case COLOR_NUMBER: return new Color(68,85,136);
                case COLOR_HEX: return new Color(189,47,115);
                case COLOR_BIN: return new Color(68,85,136);
                case COLOR_JUMP: return new Color(23,151,101); 
                case COLOR_LOHI: return new Color(231,93,67);
                case COLOR_KEYWORD: return new Color(0,134,179);
                case COLOR_COMPILERKEYWORD: return new Color(165,42,42);
                case COLOR_SCRIPTKEYWORD: return new Color(231,93,67);
                case COLOR_ILLEGALOPCODE: return new Color(216,23,69);
                case BACKGROUND: return new Color(248,248,255);
                case LINE_BACKGROUND: return new Color(236,236,236); // 11,55,66
                case LINE_BORDER: return new Color(236,236,236);
                case LINE_COLOR: return new Color(160,160,160);
                case LINE_HIGHLIGHT: return new Color(10,10,10);
            }
            break;
        case SCHEME_GREENBERET:
            switch (tokentype) {
                case COLOR_NORMAL: return new Color(140,72,39); 
                case COLOR_COMMENT: return new Color(156,166,156);
                case COLOR_STRING: return new Color(184,134,11);
                case COLOR_NUMBER: return new Color(205,133,63);
                case COLOR_HEX: return new Color(107,142,34);
                case COLOR_BIN: return new Color(107,142,34);
                case COLOR_JUMP: return new Color(0,100,0); 
                case COLOR_LOHI: return new Color(0,128,128);
                case COLOR_KEYWORD: return new Color(0,128,0);
                case COLOR_COMPILERKEYWORD: return new Color(47,79,79);
                case COLOR_SCRIPTKEYWORD: return new Color(47,79,79);
                case COLOR_ILLEGALOPCODE: return new Color(143,188,142);
                case BACKGROUND: return new Color(216,230,216);
                case LINE_BACKGROUND: return new Color(180,191,180); // 11,55,66
                case LINE_BORDER: return new Color(180,191,180);
                case LINE_COLOR: return new Color(139,69,19);
                case LINE_HIGHLIGHT: return new Color(173,255,47);
            }
            break;
        case SCHEME_POPELGANDA:
            switch (tokentype) {
                case COLOR_NORMAL: return new Color(185,185,180); 
                case COLOR_COMMENT: return new Color(35,35,35);
                case COLOR_STRING: return new Color(199,80,86);
                case COLOR_NUMBER: return new Color(255,255,255);
                case COLOR_HEX: return new Color(139,207,234);
                case COLOR_BIN: return new Color(139,207,234);
                case COLOR_JUMP: return new Color(98,150,217); 
                case COLOR_LOHI: return new Color(133,153,144);
                case COLOR_KEYWORD: return new Color(175,217,229);
                case COLOR_COMPILERKEYWORD: return new Color(136,179,136);
                case COLOR_SCRIPTKEYWORD: return new Color(133,153,144);
                case COLOR_ILLEGALOPCODE: return new Color(217,2,28);
                case BACKGROUND: return new Color(77,77,77);
                case LINE_BACKGROUND: return new Color(63,63,63); // 11,55,66
                case LINE_BORDER: return new Color(63,63,63);
                case LINE_COLOR: return new Color(185,185,180);
                case LINE_HIGHLIGHT: return new Color(255,255,255);
            }
            break;
        case SCHEME_DEFAULT_REDUCED:
            switch (tokentype) {
                case COLOR_NORMAL: return Color.BLACK;
                case COLOR_COMMENT: return new Color(150, 150, 150); // grey
                case COLOR_STRING: return new Color(220, 0, 220); //dark pink
                case COLOR_NUMBER: return new Color(160, 0, 0); // red
                case COLOR_HEX: 
                case COLOR_BIN:
                    return new Color(0, 120, 0); // green
                case COLOR_JUMP: 
                case COLOR_LOHI:
                case COLOR_COMPILERKEYWORD:
                    return new Color(70, 130, 180); // dark cyan
                case COLOR_KEYWORD: 
                case COLOR_ILLEGALOPCODE: 
                case COLOR_SCRIPTKEYWORD:
                    return new Color(0, 0, 200); // dark blue
                case BACKGROUND: return new Color(255, 255, 255); // white
                case LINE_BACKGROUND: return new Color(248, 248, 248); // light gray
                case LINE_BORDER: return new Color(240, 240, 240); // light gray
                case LINE_COLOR: return new Color(0,0,0); // black
                case LINE_HIGHLIGHT: return new Color(220, 20, 60); // red
            }
            break;
        case SCHEME_POPELGANDA_REDUCED:
            switch (tokentype) {
                case COLOR_NORMAL: return new Color(185,185,180); 
                case COLOR_COMMENT: return new Color(35,35,35);
                case COLOR_STRING: return new Color(133,153,144);
                case COLOR_NUMBER: return new Color(255,255,255);
                case COLOR_HEX:
                case COLOR_BIN:
                    return new Color(139,207,234);
                case COLOR_JUMP:
                case COLOR_LOHI:
                case COLOR_COMPILERKEYWORD:
                    return new Color(98,150,217); 
                case COLOR_KEYWORD: 
                case COLOR_SCRIPTKEYWORD:
                case COLOR_ILLEGALOPCODE:
                    return new Color(175,217,229);
                case BACKGROUND: return new Color(77,77,77);
                case LINE_BACKGROUND: return new Color(63,63,63); // 11,55,66
                case LINE_BORDER: return new Color(63,63,63);
                case LINE_COLOR: return new Color(185,185,180);
                case LINE_HIGHLIGHT: return new Color(255,255,255);
            }
            break;
        case SCHEME_C64_REDUCED:
            switch (tokentype) {
                case COLOR_NORMAL:
                case COLOR_KEYWORD:
                case COLOR_SCRIPTKEYWORD:
                case COLOR_ILLEGALOPCODE: 
                    return new Color(160,160,255);
                case COLOR_COMMENT: return new Color(192,192,192);
                case COLOR_STRING: return new Color(160,255,160);
                case COLOR_NUMBER:
                case COLOR_HEX: 
                case COLOR_BIN:
                    return Color.white;
                case COLOR_JUMP: 
                case COLOR_LOHI: 
                case COLOR_COMPILERKEYWORD: 
                    return new Color(255,255,64);
                case BACKGROUND: return new Color(64,64,224);
                case LINE_BACKGROUND: return new Color(160,160,255);
                case LINE_BORDER: return new Color(160,160,255);
                case LINE_COLOR: return new Color(64,64,224); 
                case LINE_HIGHLIGHT: return new Color(255,255,255);
            }
            break;
        case SCHEME_GRAY:
            switch (tokentype) {
                case COLOR_NORMAL: return new Color(47,79,79); 
                case COLOR_COMMENT: return new Color(189,189,189);
                case COLOR_STRING: return new Color(136,179,136);
                case COLOR_NUMBER: return new Color(149,149,149);
                case COLOR_HEX: return new Color(109,130,145);
                case COLOR_BIN: return new Color(109,130,145);
                case COLOR_JUMP: return new Color(47,79,79); 
                case COLOR_LOHI: return new Color(47,79,79);
                case COLOR_KEYWORD: return new Color(108,113,128);
                case COLOR_COMPILERKEYWORD: return new Color(99,99,99);
                case COLOR_SCRIPTKEYWORD: return new Color(99,99,99);
                case COLOR_ILLEGALOPCODE: return new Color(191,164,128);
                case BACKGROUND: return new Color(245,245,245);
                case LINE_BACKGROUND: return new Color(230,230,230); // 11,55,66
                case LINE_BORDER: return new Color(230,230,230);
                case LINE_COLOR: return new Color(149,149,149);
                case LINE_HIGHLIGHT: return new Color(0,0,0);
            }
            break;
        }
        return Color.BLACK;
    }
}
