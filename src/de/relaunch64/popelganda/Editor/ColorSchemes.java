/*
 * Relaunch64 - A Java Crossassembler for C64 machine language coding.
 * Copyright (C) c81-2014 by Daniel Lüdecke (http://www.danielluedecke.de)
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
import java.util.Arrays;
import java.util.List;

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
    public static final int COLOR_OPERATOR = 8;
    public static final int COLOR_KEYWORD = 9;
    public static final int COLOR_COMPILERKEYWORD = 10;
    public static final int COLOR_SCRIPTKEYWORD = 11;
    
    public static final int BACKGROUND = 99;
    public static final int LINE_BACKGROUND = 98;
    public static final int LINE_BORDER = 97;
    public static final int LINE_COLOR = 96;
    public static final int LINE_HIGHLIGHT = 95;
    
    public static String getColor(int scheme, int tokentype) {
        switch (scheme) {
        case SCHEME_DEFAULT:
            switch (tokentype) {
                case COLOR_NORMAL: return "#000000";
                case COLOR_COMMENT: return "#969696"; // grey
                case COLOR_STRING: return "#dc00dc"; //dark pink
                case COLOR_NUMBER: return "#a00000"; // red
                case COLOR_HEX: return "#007800"; // green
                case COLOR_BIN: return "#007878"; // cyan
                case COLOR_JUMP: return "#ce6700"; // brown
                case COLOR_OPERATOR: return "#808000"; // olive
                case COLOR_KEYWORD: return "#0000c8"; // dark blue
                case COLOR_COMPILERKEYWORD: return "#4682b4"; // dark cyan
                case COLOR_SCRIPTKEYWORD: return "#808000"; // olive
                case COLOR_ILLEGALOPCODE: return "#ff45000"; // orange red
                case BACKGROUND: return "#ffffff"; // white
                case LINE_BACKGROUND: return "#f8f8f8"; // light gray
                case LINE_BORDER: return "#f0f0f0"; // light gray
                case LINE_COLOR: return "#000000"; // black
                case LINE_HIGHLIGHT: return "#dc143c"; // red
            }
            break;
        case SCHEME_PAIRED:
            switch (tokentype) {
                case COLOR_NORMAL: return "#000000";
                case COLOR_COMMENT: return "#bdb76b";
                case COLOR_STRING: return "#25,25,112";
                case COLOR_NUMBER: return "#d2,105,30";
                case COLOR_HEX: return "#e3,26,28";
                case COLOR_BIN: return "#fd,bf,111";
                case COLOR_JUMP: return "#c7,21,133";
                case COLOR_OPERATOR: return "#0,128,128";
                case COLOR_KEYWORD: return "#51,a0,44";
                case COLOR_COMPILERKEYWORD: return "#31,120,b4";
                case COLOR_SCRIPTKEYWORD: return "#0,128,128";
                case COLOR_ILLEGALOPCODE: return "#106,61,154";
                case BACKGROUND: return "#fc,fc,fc"; // white
                case LINE_BACKGROUND: return "#f8, f8, f8"; // light gray
                case LINE_BORDER: return "#f4, f4, f4"; // light gray
                case LINE_COLOR: return "#0,0,0"; // black
                case LINE_HIGHLIGHT: return "#dc, 20, 3c"; // red
            }
            break;
        case SCHEME_C64:
            switch (tokentype) {
                case COLOR_NORMAL: return "#ffffff";
                case COLOR_COMMENT: return "#136,136,136";
                case COLOR_STRING: return "#64,e0,64";
                case COLOR_NUMBER: return "#ff,a0,a0";
                case COLOR_HEX: return "#e0,a0,64";
                case COLOR_BIN: return "#c0,c0,c0";
                case COLOR_JUMP: return "#e0,96,e0"; 
                case COLOR_OPERATOR: return "#96,ff,ff";
                case COLOR_KEYWORD: return "#a0,a0,ff";
                case COLOR_COMPILERKEYWORD: return "#ffff64";
                case COLOR_SCRIPTKEYWORD: return "#96,ff,ff";
                case COLOR_ILLEGALOPCODE: return "#e0,64,64";
                case BACKGROUND: return "#64,64,e0";
                case LINE_BACKGROUND: return "#a0a0ff";
                case LINE_BORDER: return "#a0a0ff";
                case LINE_COLOR: return "#64,64,e0"; 
                case LINE_HIGHLIGHT: return "#ffffff";
            }
            break;
        case SCHEME_HELLO_KITTY:
            switch (tokentype) {
                case COLOR_NORMAL: return "#dc,20,3c";
                case COLOR_COMMENT: return "#119,136,153";
                case COLOR_STRING: return "#8b,0,8b";
                case COLOR_NUMBER: return "#218,112,214";
                case COLOR_HEX: return "#c7,21,133";
                case COLOR_BIN: return "#d8,bf,d8";
                case COLOR_JUMP: return "#f0,128,128"; 
                case COLOR_OPERATOR: return "#221,a0,221";
                case COLOR_KEYWORD: return "#219,112,147";
                case COLOR_COMPILERKEYWORD: return "#205,92,92";
                case COLOR_SCRIPTKEYWORD: return "#221,a0,221";
                case COLOR_ILLEGALOPCODE: return "#ff,99,71";
                case BACKGROUND: return "#ff,250,250";
                case LINE_BACKGROUND: return "#ff,f0,245";
                case LINE_BORDER: return "#ff,182,193";
                case LINE_COLOR: return "#ff,182,193";
                case LINE_HIGHLIGHT: return "#dc,20,3c";
            }
            break;
        case SCHEME_DARK:
            switch (tokentype) {
                case COLOR_NORMAL: return "#245,245,245";
                case COLOR_COMMENT: return "#c0,c0,c0";
                case COLOR_STRING: return "#f0,e6,140";
                case COLOR_NUMBER: return "#100,149,237";
                case COLOR_HEX: return "#176,196,222";
                case COLOR_BIN: return "#af,238,238";
                case COLOR_JUMP: return "#219,112,147"; 
                case COLOR_OPERATOR: return "#8f,bc,8f";
                case COLOR_KEYWORD: return "#173,d8,e6";
                case COLOR_COMPILERKEYWORD: return "#221,a0,221";
                case COLOR_SCRIPTKEYWORD: return "#8f,bc,8f";
                case COLOR_ILLEGALOPCODE: return "#ff,c0,203";
                case BACKGROUND: return "#55,55,55";
                case LINE_BACKGROUND: return "#48,48,48";
                case LINE_BORDER: return "#48,48,48";
                case LINE_COLOR: return "#128,128,128";
                case LINE_HIGHLIGHT: return "#d3,d3,d3";
            }
            break;
        case SCHEME_BLUES:
            switch (tokentype) {
                case COLOR_NORMAL: return "#112,128,144";
                case COLOR_COMMENT: return "#176,196,222";
                case COLOR_STRING: return "#65,105,225";
                case COLOR_NUMBER: return "#0,8b,8b";
                case COLOR_HEX: return "#218,112,214";
                case COLOR_BIN: return "#100,149,237";
                case COLOR_JUMP: return "#0,bf,ff"; 
                case COLOR_OPERATOR: return "#95,158,a0";
                case COLOR_KEYWORD: return "#70,130,b4";
                case COLOR_COMPILERKEYWORD: return "#72,61,8b";
                case COLOR_SCRIPTKEYWORD: return "#95,158,a0";
                case COLOR_ILLEGALOPCODE: return "#0,0,205";
                case BACKGROUND: return "#f8,f8,ff";
                case LINE_BACKGROUND: return "#f0,f8,ff";
                case LINE_BORDER: return "#70,130,b4";
                case LINE_COLOR: return "#0,0,8b";
                case LINE_HIGHLIGHT: return "#c7,21,133";
            }
            break;
        case SCHEME_SOLAR_LIGHT:
            switch (tokentype) {
                case COLOR_NORMAL: return "#90,111,117";
                case COLOR_COMMENT: return "#148,162,162";
                case COLOR_STRING: return "#53,162,152";
                case COLOR_NUMBER: return "#134,152,23";
                case COLOR_HEX: return "#b4,136,24";
                case COLOR_BIN: return "#109,116,195";
                case COLOR_JUMP: return "#217,47,52"; 
                case COLOR_OPERATOR: return "#c8,73,31";
                case COLOR_KEYWORD: return "#49,142,209";
                case COLOR_COMPILERKEYWORD: return "#11,55,66";
                case COLOR_SCRIPTKEYWORD: return "#c8,73,31";
                case COLOR_ILLEGALOPCODE: return "#208,54,130";
                case BACKGROUND: return "#fd,246,229";
                case LINE_BACKGROUND: return "#238,233,214"; // fd,246,229
                case LINE_BORDER: return "#238,233,214";
                case LINE_COLOR: return "#148,162,162";
                case LINE_HIGHLIGHT: return "#11,55,66";
            }
            break;
        case SCHEME_SOLAR_DARK:
            switch (tokentype) {
                case COLOR_NORMAL: return "#148,162,162"; 
                case COLOR_COMMENT: return "#90,111,117";
                case COLOR_STRING: return "#53,162,152";
                case COLOR_NUMBER: return "#134,152,23";
                case COLOR_HEX: return "#49,142,209";
                case COLOR_BIN: return "#109,116,195";
                case COLOR_JUMP: return "#b4,136,24"; 
                case COLOR_OPERATOR: return "#c8,73,31";
                case COLOR_KEYWORD: return "#238,233,214";
                case COLOR_COMPILERKEYWORD: return "#217,47,52";
                case COLOR_SCRIPTKEYWORD: return "#c8,73,31";
                case COLOR_ILLEGALOPCODE: return "#208,54,130";
                case BACKGROUND: return "#11,55,66";
                case LINE_BACKGROUND: return "#3,44,54"; // 11,55,66
                case LINE_BORDER: return "#3,44,54";
                case LINE_COLOR: return "#132,8b,151";
                case LINE_HIGHLIGHT: return "#fd,246,229";
            }
            break;
        case SCHEME_DARK2:
            switch (tokentype) {
                case COLOR_NORMAL: return "#128,137,8b"; 
                case COLOR_COMMENT: return "#64,69,70";
                case COLOR_STRING: return "#233,170,145";
                case COLOR_NUMBER: return "#228,101,184";
                case COLOR_HEX: return "#97,172,123";
                case COLOR_BIN: return "#97,172,123";
                case COLOR_JUMP: return "#72,209,204"; 
                case COLOR_OPERATOR: return "#c7,116,68";
                case COLOR_KEYWORD: return "#128,b4,c0";
                case COLOR_COMPILERKEYWORD: return "#157,162,101";
                case COLOR_SCRIPTKEYWORD: return "#c7,116,68";
                case COLOR_ILLEGALOPCODE: return "#4,136,174";
                case BACKGROUND: return "#15,17,18";
                case LINE_BACKGROUND: return "#10,11,12"; // 11,55,66
                case LINE_BORDER: return "#10,11,12";
                case LINE_COLOR: return "#102,102,102";
                case LINE_HIGHLIGHT: return "#4,136,174";
            }
            break;
        case SCHEME_GITHUB:
            switch (tokentype) {
                case COLOR_NORMAL: return "#0,0,0"; 
                case COLOR_COMMENT: return "#a0,a0,a0";
                case COLOR_STRING: return "#d8,23,69";
                case COLOR_NUMBER: return "#68,85,136";
                case COLOR_HEX: return "#bd,47,115";
                case COLOR_BIN: return "#68,85,136";
                case COLOR_JUMP: return "#23,151,101"; 
                case COLOR_OPERATOR: return "#e7,93,67";
                case COLOR_KEYWORD: return "#0,134,179";
                case COLOR_COMPILERKEYWORD: return "#a5,42,42";
                case COLOR_SCRIPTKEYWORD: return "#e7,93,67";
                case COLOR_ILLEGALOPCODE: return "#d8,23,69";
                case BACKGROUND: return "#f8,f8,ff";
                case LINE_BACKGROUND: return "#ec,ec,ec"; // 11,55,66
                case LINE_BORDER: return "#ec,ec,ec";
                case LINE_COLOR: return "#a0,a0,a0";
                case LINE_HIGHLIGHT: return "#10,10,10";
            }
            break;
        case SCHEME_GREENBERET:
            switch (tokentype) {
                case COLOR_NORMAL: return "#140,72,39"; 
                case COLOR_COMMENT: return "#156,166,156";
                case COLOR_STRING: return "#184,134,11";
                case COLOR_NUMBER: return "#205,133,63";
                case COLOR_HEX: return "#6b,142,34";
                case COLOR_BIN: return "#6b,142,34";
                case COLOR_JUMP: return "#0,100,0"; 
                case COLOR_OPERATOR: return "#0,128,128";
                case COLOR_KEYWORD: return "#0,128,0";
                case COLOR_COMPILERKEYWORD: return "#47,79,79";
                case COLOR_SCRIPTKEYWORD: return "#47,79,79";
                case COLOR_ILLEGALOPCODE: return "#8f,bc,142";
                case BACKGROUND: return "#d8,e6,d8";
                case LINE_BACKGROUND: return "#b4,bf,b4"; // 11,55,66
                case LINE_BORDER: return "#b4,bf,b4";
                case LINE_COLOR: return "#8b,69,19";
                case LINE_HIGHLIGHT: return "#173,ff,47";
            }
            break;
        case SCHEME_POPELGANDA:
            switch (tokentype) {
                case COLOR_NORMAL: return "#185,185,b4"; 
                case COLOR_COMMENT: return "#35,35,35";
                case COLOR_STRING: return "#c7,80,86";
                case COLOR_NUMBER: return "#ff,ff,ff";
                case COLOR_HEX: return "#8b,207,234";
                case COLOR_BIN: return "#8b,207,234";
                case COLOR_JUMP: return "#98,150,217"; 
                case COLOR_OPERATOR: return "#133,153,144";
                case COLOR_KEYWORD: return "#af,217,229";
                case COLOR_COMPILERKEYWORD: return "#136,179,136";
                case COLOR_SCRIPTKEYWORD: return "#133,153,144";
                case COLOR_ILLEGALOPCODE: return "#217,2,28";
                case BACKGROUND: return "#77,77,77";
                case LINE_BACKGROUND: return "#63,63,63"; // 11,55,66
                case LINE_BORDER: return "#63,63,63";
                case LINE_COLOR: return "#185,185,b4";
                case LINE_HIGHLIGHT: return "#ff,ff,ff";
            }
            break;
        case SCHEME_DEFAULT_REDUCED:
            switch (tokentype) {
                case COLOR_NORMAL: return "#000000";
                case COLOR_COMMENT: return "#969696"; // grey
                case COLOR_STRING: return "#dc, 0, dc"; //dark pink
                case COLOR_NUMBER: return "#a0, 0, 0"; // red
                case COLOR_HEX: 
                case COLOR_BIN:
                    return "#0, 120, 0"; // green
                case COLOR_JUMP: 
                case COLOR_OPERATOR:
                case COLOR_COMPILERKEYWORD:
                    return "#70, 130, b4"; // dark cyan
                case COLOR_KEYWORD: 
                case COLOR_ILLEGALOPCODE: 
                case COLOR_SCRIPTKEYWORD:
                    return "#0, 0, c8"; // dark blue
                case BACKGROUND: return "#ff, ff, ff"; // white
                case LINE_BACKGROUND: return "#f8, f8, f8"; // light gray
                case LINE_BORDER: return "#f0, f0, f0"; // light gray
                case LINE_COLOR: return "#0,0,0"; // black
                case LINE_HIGHLIGHT: return "#dc, 20, 3c"; // red
            }
            break;
        case SCHEME_POPELGANDA_REDUCED:
            switch (tokentype) {
                case COLOR_NORMAL: return "#185,185,b4"; 
                case COLOR_COMMENT: return "#35,35,35";
                case COLOR_STRING: return "#133,153,144";
                case COLOR_NUMBER: return "#ff,ff,ff";
                case COLOR_HEX:
                case COLOR_BIN:
                    return "#8b,207,234";
                case COLOR_JUMP:
                case COLOR_OPERATOR:
                case COLOR_COMPILERKEYWORD:
                    return "#98,150,217"; 
                case COLOR_KEYWORD: 
                case COLOR_SCRIPTKEYWORD:
                case COLOR_ILLEGALOPCODE:
                    return "#af,217,229";
                case BACKGROUND: return "#77,77,77";
                case LINE_BACKGROUND: return "#63,63,63"; // 11,55,66
                case LINE_BORDER: return "#63,63,63";
                case LINE_COLOR: return "#185,185,b4";
                case LINE_HIGHLIGHT: return "#ff,ff,ff";
            }
            break;
        case SCHEME_C64_REDUCED:
            switch (tokentype) {
                case COLOR_NORMAL:
                case COLOR_KEYWORD:
                case COLOR_SCRIPTKEYWORD:
                case COLOR_ILLEGALOPCODE: 
                    return "#a0,a0,ff";
                case COLOR_COMMENT: return "#c0,c0,c0";
                case COLOR_STRING: return "#a0,ff,a0";
                case COLOR_NUMBER:
                case COLOR_HEX: 
                case COLOR_BIN:
                    return "#ffffff";
                case COLOR_JUMP: 
                case COLOR_OPERATOR: 
                case COLOR_COMPILERKEYWORD: 
                    return "#ff,ff,64";
                case BACKGROUND: return "#64,64,e0";
                case LINE_BACKGROUND: return "#a0,a0,ff";
                case LINE_BORDER: return "#a0,a0,ff";
                case LINE_COLOR: return "#64,64,e0"; 
                case LINE_HIGHLIGHT: return "#ff,ff,ff";
            }
            break;
        case SCHEME_GRAY:
            switch (tokentype) {
                case COLOR_NORMAL: return "#47,79,79"; 
                case COLOR_COMMENT: return "#bd,bd,bd";
                case COLOR_STRING: return "#136,179,136";
                case COLOR_NUMBER: return "#149,149,149";
                case COLOR_HEX: return "#109,130,145";
                case COLOR_BIN: return "#109,130,145";
                case COLOR_JUMP: return "#47,79,79"; 
                case COLOR_OPERATOR: return "#47,79,79";
                case COLOR_KEYWORD: return "#108,113,128";
                case COLOR_COMPILERKEYWORD: return "#99,99,99";
                case COLOR_SCRIPTKEYWORD: return "#99,99,99";
                case COLOR_ILLEGALOPCODE: return "#bf,164,128";
                case BACKGROUND: return "#245,245,245";
                case LINE_BACKGROUND: return "#e6,e6,e6"; // 11,55,66
                case LINE_BORDER: return "#e6,e6,e6";
                case LINE_COLOR: return "#149,149,149";
                case LINE_HIGHLIGHT: return "#0,0,0";
            }
            break;
        }
        return "#000000";
    }
    public static List<String> getKeywordList() {
        final String opcodes[] = {
            "LDA", "STA", "INC", "DEC", "LDX", "STX", "INX", "DEX", "LDY",
            "STY", "INY", "DEY", "CMP", "CPX", "CPY", "TAX", "TAY", "TXA",
            "TYA", "TSX", "TXS", "AND", "ORA", "EOR", "ADC", "SBC", "SEC",
            "CLC", "ASL", "LSR", "ROL", "ROR", "BIT", "JMP", "JSR", "RTS",
            "RTI", "BCC", "BCS", "BEQ", "BNE", "BPL", "BMI", "BVC", "BVS",
            "SEI", "CLI", "SED", "CLD", "CLV", "PHA", "PLA", "PHP", "PLP",
            "NOP", "BRK"
        };
        return Arrays.asList(opcodes);
    }
    public static List<String> getIllegalOpcodeList() {
        final String opcodes[] = {
            "AAR", "AHX", "ALR", "ANC", "AXS", "DCP", "ISC", "LAS", "LAX",
            "RLA", "RRA", "SAX", "SBC", "SHX", "SHY", "SLO", "SRE", "TAS",
            "XAA"
        };
        return Arrays.asList(opcodes);
    }
    public static List<String> getCompilerKeywordList(int compiler) {
        String keywords[] = null;
        switch (compiler) {
            case ConstantsR64.COMPILER_ACME:
                keywords = new String[] {
                    "!08", "!8", "!16", "!24", "!32", "!ALIGN", "!AL", "!AS",
                    "!BIN", "!BINARY", "!BY", "!BYTE", "!CONVTAB", "!CT",
                    "!DO", "!EOF", "!ENDOFFILE", "!ERROR", "!IF", "!IFDEF",
                    "!INITMEN", "!FI", "!FILL", "!FOR", "!MACRO", "!PET",
                    "!PSEUDOPC", "!RAW", "!RL", "!RS", "!SCR", "!SCRXOR",
                    "!SET", "!SL", "!SRC", "!SOURCE", "!TEXT", "!TO", "!TX",
                    "!WARN", "!WO", "!WORD", "!ZN", "!ZONE", "UNTIL"
                };
                break;
            case ConstantsR64.COMPILER_KICKASSEMBLER:
                keywords = new String[] {
                    ".ALIGN", ".ASSERT", ".BYTE", ".CONST", ".DEFINE",
                    ".DWORD", ".ENUM", ".EVAL", ".FILL", ".FOR",
                    ".FUNCTION", ".IF", ".IMPORT", ".IMPORTONCE", ".LABEL",
                    ".MACRO", ".PC", ".PRINT", ".PSEUDOPC", ".RETURN",
                    ".TEXT", ".VAR", ".WORD"
                };
                break;
            case ConstantsR64.COMPILER_DASM:
                keywords = new String[] {
                    "INCLUDE", "INCBIN", "INCDIR", "SEG", "SEG.U", "DC.B",
                    "DC.W", "DC.L", "DS.B", "DS.W", "DS.L", "DV.B", "DV.W",
                    "DV.L", "BYTE", "WORD", "LONG", "HEX", "ERR", "ORG",
                    "RORG", "PROCESSOR", "ECHO", "REND", "ALIGN",
                    "SUBROUTINE", "EQU", "EQM", "SET", "MAC", "ENDM",
                    "MEXIT", "IFCONST", "IFNCONST", "IF", "ELSE", "ENDIF",
                    "EIF", "REPEAT", "REPEND", "DAD", "LIST ON", "LIST OFF"
                };
                break;
            case ConstantsR64.COMPILER_64TASS:
                keywords = new String[] {
                    ".AL", ".ALIGN", ".AS", ".ASSERT", ".BEND", ".BINARY",
                    ".BINCLUDE", ".BLOCK", ".BREAK", ".BYTE", ".CASE", ".CDEF",
                    ".CERROR", ".CHAR", ".CHECK", ".COMMENT", ".CONTINUE",
                    ".CPU", ".CWARN", ".DATABANK", ".DEFAULT", ".DINT",
                    ".DPAGE", ".DSECTION", ".DSTRUCT", ".DUNION", ".DWORD",
                    ".EDEF", ".ELSE", ".ELSIF", ".ENC", ".END", ".ENDC",
                    ".ENDF", ".ENDIF", ".ENDM", ".ENDP", ".ENDS", ".ENDSWITCH",
                    ".ENDU", ".ENDWEAK", ".EOR", ".ERROR", ".FI", ".FILL",
                    ".FOR", ".FUNCTION", ".GOTO", ".HERE", ".HIDEMAC", ".IF",
                    ".IFEQ", ".IFMI", ".IFNE", ".IFPL", ".INCLUDE", ".INT",
                    ".LBL", ".LINT", ".LOGICAL", ".LONG", ".MACRO", ".NEXT",
                    ".NULL", ".OFFS", ".OPTION", ".PAGE", ".PEND", ".PROC",
                    ".PROFF", ".PRON", ".PRTEXT", ".REPT", ".RTA", ".SECTION",
                    ".SEGMENT", ".SEND", ".SHIFT", ".SHIFTL", ".SHOWMAC",
                    ".STRUCT", ".SWITCH", ".TEXT", ".UNION", ".VAR", ".WARN",
                    ".WEAK", ".WORD", ".XL", ".XS"
                };
                break;
            case ConstantsR64.COMPILER_CA65:
                keywords = new String[] {
                    ".A16", ".A8", ".ADDR", ".ALIGN", ".ASCIIZ", ".ASSERT",
                    ".AUTOIMPORT", ".BANKBYTES", ".BSS", ".BYT", ".BYTE",
                    ".CASE", ".CHARMAP", ".CODE", ".CONDES", ".CONSTRUCTOR",
                    ".DATA", ".DBYT", ".DEBUGINFO", ".DEF", ".DEFINED",
                    ".DESTRUCTOR", ".DWORD", ".ELSE", ".ELSIF", ".END",
                    ".ENDENUM", ".ENDIF", ".ENDMAC", ".ENDMACRO", ".ENDPROC",
                    ".ENDREP", ".ENDREPEAT", ".ENDSCOPE", ".ENDSTRUCT",
                    ".ENUM", ".ERROR", ".EXITMAC", ".EXITMACRO", ".EXPORT",
                    ".EXPORTZP", ".FARADDR", ".FEATURE", ".FILEOPT", ".FOPT",
                    ".FORCEIMPORT", ".GLOBAL", ".GLOBALZP", ".HIBYTES", ".I16",
                    ".I8", ".IF", ".IFBLANK", ".IFCONST", ".IFDEF",
                    ".IFNBLANK", ".IFNDEF", ".IFNREF", ".IFP02", ".IFP816",
                    ".IFPC02", ".IFPSC02", ".IFREF", ".IMPORT", ".IMPORTZP",
                    ".INCBIN", ".INCLUDE", ".INTERRUPTOR", ".LINECONT",
                    ".LIST", ".LISTBYTES", ".LOBYTES", ".LOCAL", ".LOCALCHAR",
                    ".MACPACK", ".MAC", ".MACRO", ".ORG", ".OUT", ".P02",
                    ".P816", ".PAGELEN", ".PAGELENGTH", ".PC02", ".POPSEG",
                    ".PROC", ".PSC02", ".PUSHSEG", ".RELOG", ".REPEAT", ".RES",
                    ".RODATA", ".SCOPE", ".SEGMENT", ".SETCPU", ".SMART",
                    ".STRUCT", ".SUNPLUS", ".TAG", ".WARNING", ".WORD",
                    ".ZEROPAGE"
                };
                break;
            case ConstantsR64.COMPILER_DREAMASS:
                keywords = new String[] {
                    ".BINCLUDE", ".BYTE", ".DB", ".DW", ".DT", ".TEXT", ".PET",
                    ".DP", ".SCR", ".DS", ".SEGMENT", ".SETPET", ".SETSCR",
                    ".WORD", ".DSB", ".ALIGN", ".(", ".)", ".", ".PSEUDOPC",
                    ".REALPC", "#ERROR", "#IF", "#IFDEF", "#IFFILE", "#IFNDEF",
                    "#IFNFILE", "#ELSIF", "#ELSIFDEF", "#ELSIFFILE",
                    "#ELSIFNDEF", "#ELSIFNFILE", "#ELSE", "#ENDIF", "#INCLUDE",
                    "#MACRO", "#OUTFILE", "#PRINT", "#SEGDEF", "#WARNING"
                };
                break;
        }
        return (keywords!=null) ? Arrays.asList(keywords) : null;
    }
    public static List<String> getScriptKeywordList(int compiler) {
        String keywords[] = null;
        switch (compiler) {
            case ConstantsR64.COMPILER_ACME:
                keywords = new String[] {
                    "AND", "ARCCOS", "ARCSIN", "ARCTAN", "ASL", "ASR", "COS",
                    "DIV", "FLOAT", "INT", "LSL", "LSR", "MOD", "NOT", "OR",
                    "SIN", "TAN", "XOR"
                };
                break;
            case ConstantsR64.COMPILER_KICKASSEMBLER:
                keywords = new String[] {
// ".ADD", ".ASNUMBER", ".ASBOOLEAN", ".CHARAT", ".GET",
// ".GETDATA", ".GETPIXEL", ".GETMULTICOLORBYTE",
// ".GETSINGLECOLORBYTE", ".SIZE", ".SUBSTRING", ".STRING",
// ".TOBINARYSTRING", ".TOHEXSTRING", ".TOINTSTRING",
// ".TOOCTALSTRING",
                    "ABS", "ACOS", "ASIN", "ATAN", "ATAN2", "CBRT", "CEIL", "COS",
                    "COSH", "DEG", "EXP", "EXPM1", "FLOOR", "FRAC", "HYPOT",
                    "LIST", "LOG", "LOG10", "LOG1P", "LOADPICTURE", "LOADSID",
                    "MATRIX", "MAX", "MIN", "MOD", "MOVEMATRIX",
                    "PERSPECTIVEMATRIX", "POW", "RAD", "RANDOM",
                    "ROTATIONMATRIX", "ROUND", "SCALEMATRIX", "SIGN", "SIN",
                    "SINH", "SQRT", "SIGNUM", "TAN", "TANH", "TODEGREE",
                    "TORADIANS", "VECTOR"
                };
                break;
            case ConstantsR64.COMPILER_64TASS:
                keywords = new String[] {
                    "ABS", "ACOS", "ALL", "ANY", "ASIN", "ATAN", "ATAN2", "BOOL",
                    "CBRT", "CEIL", "COS", "COSH", "DEG", "EXP", "FLOAT",
                    "FLOOR", "FORMAT", "FRAC", "HYPOT", "INT", "LEN", "LOG",
                    "LOG10", "POW", "RAD", "RANGE", "REPR", "ROUND", "SIGN",
                    "SIN", "SINH", "SIZE", "SQRT", "STR", "TAN", "TANH",
                    "TRUNC"
                };
                break;
            case ConstantsR64.COMPILER_CA65:
                keywords = new String[] {
                    ".BANKBYTE", ".BLANK", ".CONCAT", ".CONST", ".HIBYTE",
                    ".HIWORD", ".IDENT", ".LEFT", ".LOBYTE", ".MATCH", ".MID",
                    ".REF", ".REFERENCED", ".RIGHT", ".SIZEOF", ".STRAT",
                    ".SPRINTF", ".STRING", ".STRLEN", ".TCOUNT", ".XMATCH"
                };
                break;
        }
        return (keywords!=null) ? Arrays.asList(keywords) : null;
    }
}
