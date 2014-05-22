/*
 * Relaunch64 - A Java cross-development IDE for C64 machine language coding.
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
                case COLOR_STRING: return "#191970";
                case COLOR_NUMBER: return "#d2691e";
                case COLOR_HEX: return "#e31a1c";
                case COLOR_BIN: return "#fdbf6f";
                case COLOR_JUMP: return "#c79e5";
                case COLOR_OPERATOR: return "#008080";
                case COLOR_KEYWORD: return "#33a02c";
                case COLOR_COMPILERKEYWORD: return "#1f78b4";
                case COLOR_SCRIPTKEYWORD: return "#008080";
                case COLOR_ILLEGALOPCODE: return "#6a3d9a";
                case BACKGROUND: return "#fcfcfc"; // white
                case LINE_BACKGROUND: return "#f8f8f8"; // light gray
                case LINE_BORDER: return "#f4f4f4"; // light gray
                case LINE_COLOR: return "#000000"; // black
                case LINE_HIGHLIGHT: return "#dc143c"; // red
            }
            break;
        case SCHEME_C64:
            switch (tokentype) {
                case COLOR_NORMAL: return "#ffffff";
                case COLOR_COMMENT: return "#888888";
                case COLOR_STRING: return "#40e040";
                case COLOR_NUMBER: return "#ffa0a0";
                case COLOR_HEX: return "#e0a040";
                case COLOR_BIN: return "#c0c0c0";
                case COLOR_JUMP: return "#e060e0"; 
                case COLOR_OPERATOR: return "#60ffff";
                case COLOR_KEYWORD: return "#a0a0ff";
                case COLOR_COMPILERKEYWORD: return "#ffff64";
                case COLOR_SCRIPTKEYWORD: return "#60ffff";
                case COLOR_ILLEGALOPCODE: return "#e04040";
                case BACKGROUND: return "#4040e0";
                case LINE_BACKGROUND: return "#a0a0ff";
                case LINE_BORDER: return "#a0a0ff";
                case LINE_COLOR: return "#4040e0"; 
                case LINE_HIGHLIGHT: return "#ffffff";
            }
            break;
        case SCHEME_HELLO_KITTY:
            switch (tokentype) {
                case COLOR_NORMAL: return "#dc143c";
                case COLOR_COMMENT: return "#778899";
                case COLOR_STRING: return "#8b0008b";
                case COLOR_NUMBER: return "#da70d6";
                case COLOR_HEX: return "#c79e5";
                case COLOR_BIN: return "#d8bfd8";
                case COLOR_JUMP: return "#f08080"; 
                case COLOR_OPERATOR: return "#dda0dd";
                case COLOR_KEYWORD: return "#db7093";
                case COLOR_COMPILERKEYWORD: return "#cd5c5c";
                case COLOR_SCRIPTKEYWORD: return "#dda0dd";
                case COLOR_ILLEGALOPCODE: return "#ff6347";
                case BACKGROUND: return "#fffafa";
                case LINE_BACKGROUND: return "#fff0f5";
                case LINE_BORDER: return "#ffb6c1";
                case LINE_COLOR: return "#ffb6c1";
                case LINE_HIGHLIGHT: return "#dc143c";
            }
            break;
        case SCHEME_DARK:
            switch (tokentype) {
                case COLOR_NORMAL: return "#f5f5f5";
                case COLOR_COMMENT: return "#c0c0c0";
                case COLOR_STRING: return "#f0e68c";
                case COLOR_NUMBER: return "#6495ed";
                case COLOR_HEX: return "#b0c4de";
                case COLOR_BIN: return "#afeeee";
                case COLOR_JUMP: return "#db7093"; 
                case COLOR_OPERATOR: return "#8fbc8f";
                case COLOR_KEYWORD: return "#add8e6";
                case COLOR_COMPILERKEYWORD: return "#dda0dd";
                case COLOR_SCRIPTKEYWORD: return "#8fbc8f";
                case COLOR_ILLEGALOPCODE: return "#ffc0cb";
                case BACKGROUND: return "#373737";
                case LINE_BACKGROUND: return "#303030";
                case LINE_BORDER: return "#303030";
                case LINE_COLOR: return "#808080";
                case LINE_HIGHLIGHT: return "#d3d3d3";
            }
            break;
        case SCHEME_BLUES:
            switch (tokentype) {
                case COLOR_NORMAL: return "#70805a";
                case COLOR_COMMENT: return "#b0c4de";
                case COLOR_STRING: return "#4169e1";
                case COLOR_NUMBER: return "#008b8b";
                case COLOR_HEX: return "#da70d6";
                case COLOR_BIN: return "#6495ed";
                case COLOR_JUMP: return "#00bfff"; 
                case COLOR_OPERATOR: return "#5f9ea0";
                case COLOR_KEYWORD: return "#4682b4";
                case COLOR_COMPILERKEYWORD: return "#483d8b";
                case COLOR_SCRIPTKEYWORD: return "#5f9ea0";
                case COLOR_ILLEGALOPCODE: return "#0000cd";
                case BACKGROUND: return "#f8f8ff";
                case LINE_BACKGROUND: return "#f0f8ff";
                case LINE_BORDER: return "#4682b4";
                case LINE_COLOR: return "#00008b";
                case LINE_HIGHLIGHT: return "#c71585";
            }
            break;
        case SCHEME_SOLAR_LIGHT:
            switch (tokentype) {
                case COLOR_NORMAL: return "#5a6f75";
                case COLOR_COMMENT: return "#94a2a2";
                case COLOR_STRING: return "#35a298";
                case COLOR_NUMBER: return "#869817";
                case COLOR_HEX: return "#b48818";
                case COLOR_BIN: return "#6d74c3";
                case COLOR_JUMP: return "#d92f34"; 
                case COLOR_OPERATOR: return "#c8491f";
                case COLOR_KEYWORD: return "#318ed1";
                case COLOR_COMPILERKEYWORD: return "#0b3742";
                case COLOR_SCRIPTKEYWORD: return "#c8481f";
                case COLOR_ILLEGALOPCODE: return "#d03682";
                case BACKGROUND: return "#fdf6e5";
                case LINE_BACKGROUND: return "#eee9d6"; // fd,f6,e5
                case LINE_BORDER: return "#eee9d6";
                case LINE_COLOR: return "#94a2a2";
                case LINE_HIGHLIGHT: return "#0b3742";
            }
            break;
        case SCHEME_SOLAR_DARK:
            switch (tokentype) {
                case COLOR_NORMAL: return "#148,a2,a2"; 
                case COLOR_COMMENT: return "#5a,6f,117";
                case COLOR_STRING: return "#53,a2,152";
                case COLOR_NUMBER: return "#134,152,23";
                case COLOR_HEX: return "#49,8e,d1";
                case COLOR_BIN: return "#109,116,c3";
                case COLOR_JUMP: return "#b4,136,24"; 
                case COLOR_OPERATOR: return "#c8,73,1f";
                case COLOR_KEYWORD: return "#ee,e9,d6";
                case COLOR_COMPILERKEYWORD: return "#d9,47,52";
                case COLOR_SCRIPTKEYWORD: return "#c8,73,1f";
                case COLOR_ILLEGALOPCODE: return "#d0,54,130";
                case BACKGROUND: return "#11,55,66";
                case LINE_BACKGROUND: return "#3,44,54"; // 11,55,66
                case LINE_BORDER: return "#3,44,54";
                case LINE_COLOR: return "#132,8b,151";
                case LINE_HIGHLIGHT: return "#fd,f6,e5";
            }
            break;
        case SCHEME_DARK2:
            switch (tokentype) {
                case COLOR_NORMAL: return "#128,137,8b"; 
                case COLOR_COMMENT: return "#404546";
                case COLOR_STRING: return "#e9,170,145";
                case COLOR_NUMBER: return "#e4,101,b8";
                case COLOR_HEX: return "#97,172,123";
                case COLOR_BIN: return "#97,172,123";
                case COLOR_JUMP: return "#72,d1,cc"; 
                case COLOR_OPERATOR: return "#c7,116,68";
                case COLOR_KEYWORD: return "#128,b4,c0";
                case COLOR_COMPILERKEYWORD: return "#157,a2,101";
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
                case COLOR_NORMAL: return "#000000"; 
                case COLOR_COMMENT: return "#a0a0a0";
                case COLOR_STRING: return "#d8,23,69";
                case COLOR_NUMBER: return "#68,85,136";
                case COLOR_HEX: return "#bd,47,115";
                case COLOR_BIN: return "#68,85,136";
                case COLOR_JUMP: return "#23,151,101"; 
                case COLOR_OPERATOR: return "#e7,93,67";
                case COLOR_KEYWORD: return "#0,134,b3";
                case COLOR_COMPILERKEYWORD: return "#a5,42,42";
                case COLOR_SCRIPTKEYWORD: return "#e7,93,67";
                case COLOR_ILLEGALOPCODE: return "#d8,23,69";
                case BACKGROUND: return "#f8,f8,ff";
                case LINE_BACKGROUND: return "#ececec"; // 11,55,66
                case LINE_BORDER: return "#ececec";
                case LINE_COLOR: return "#a0a0a0";
                case LINE_HIGHLIGHT: return "#10,10,10";
            }
            break;
        case SCHEME_GREENBERET:
            switch (tokentype) {
                case COLOR_NORMAL: return "#8c,72,39"; 
                case COLOR_COMMENT: return "#156,166,156";
                case COLOR_STRING: return "#b8,134,11";
                case COLOR_NUMBER: return "#cd,133,63";
                case COLOR_HEX: return "#6b,8e,34";
                case COLOR_BIN: return "#6b,8e,34";
                case COLOR_JUMP: return "#0,100,0"; 
                case COLOR_OPERATOR: return "#0,128,128";
                case COLOR_KEYWORD: return "#0,128,0";
                case COLOR_COMPILERKEYWORD: return "#47,79,79";
                case COLOR_SCRIPTKEYWORD: return "#47,79,79";
                case COLOR_ILLEGALOPCODE: return "#8f,bc,8e";
                case BACKGROUND: return "#d8,e6,d8";
                case LINE_BACKGROUND: return "#b4,bf,b4"; // 11,55,66
                case LINE_BORDER: return "#b4,bf,b4";
                case LINE_COLOR: return "#8b,69,19";
                case LINE_HIGHLIGHT: return "#ad,ff,47";
            }
            break;
        case SCHEME_POPELGANDA:
            switch (tokentype) {
                case COLOR_NORMAL: return "#b9b9b4"; 
                case COLOR_COMMENT: return "#35,35,35";
                case COLOR_STRING: return "#c7,80,86";
                case COLOR_NUMBER: return "#ffffff";
                case COLOR_HEX: return "#8bcfea";
                case COLOR_BIN: return "#8bcfea";
                case COLOR_JUMP: return "#98,150,d9"; 
                case COLOR_OPERATOR: return "#133,153,144";
                case COLOR_KEYWORD: return "#afd9e5";
                case COLOR_COMPILERKEYWORD: return "#136,b3,136";
                case COLOR_SCRIPTKEYWORD: return "#133,153,144";
                case COLOR_ILLEGALOPCODE: return "#d9,2,28";
                case BACKGROUND: return "#77,77,77";
                case LINE_BACKGROUND: return "#63,63,63"; // 11,55,66
                case LINE_BORDER: return "#63,63,63";
                case LINE_COLOR: return "#b9b9b4";
                case LINE_HIGHLIGHT: return "#ffffff";
            }
            break;
        case SCHEME_DEFAULT_REDUCED:
            switch (tokentype) {
                case COLOR_NORMAL: return "#000000";
                case COLOR_COMMENT: return "#969696"; // grey
                case COLOR_STRING: return "#dc00dc"; //dark pink
                case COLOR_NUMBER: return "#a00000"; // red
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
                case BACKGROUND: return "#ffffff"; // white
                case LINE_BACKGROUND: return "#f8f8f8"; // light gray
                case LINE_BORDER: return "#f0f0f0"; // light gray
                case LINE_COLOR: return "#000000"; // black
                case LINE_HIGHLIGHT: return "#dc, 20, 3c"; // red
            }
            break;
        case SCHEME_POPELGANDA_REDUCED:
            switch (tokentype) {
                case COLOR_NORMAL: return "#b9b9b4"; 
                case COLOR_COMMENT: return "#35,35,35";
                case COLOR_STRING: return "#133,153,144";
                case COLOR_NUMBER: return "#ffffff";
                case COLOR_HEX:
                case COLOR_BIN:
                    return "#8bcfea";
                case COLOR_JUMP:
                case COLOR_OPERATOR:
                case COLOR_COMPILERKEYWORD:
                    return "#98,150,d9"; 
                case COLOR_KEYWORD: 
                case COLOR_SCRIPTKEYWORD:
                case COLOR_ILLEGALOPCODE:
                    return "#afd9e5";
                case BACKGROUND: return "#77,77,77";
                case LINE_BACKGROUND: return "#63,63,63"; // 11,55,66
                case LINE_BORDER: return "#63,63,63";
                case LINE_COLOR: return "#b9b9b4";
                case LINE_HIGHLIGHT: return "#ffffff";
            }
            break;
        case SCHEME_C64_REDUCED:
            switch (tokentype) {
                case COLOR_NORMAL:
                case COLOR_KEYWORD:
                case COLOR_SCRIPTKEYWORD:
                case COLOR_ILLEGALOPCODE: 
                    return "#a0a0ff";
                case COLOR_COMMENT: return "#c0c0c0";
                case COLOR_STRING: return "#a0ffa0";
                case COLOR_NUMBER:
                case COLOR_HEX: 
                case COLOR_BIN:
                    return "#ffffff";
                case COLOR_JUMP: 
                case COLOR_OPERATOR: 
                case COLOR_COMPILERKEYWORD: 
                    return "#ffff40";
                case BACKGROUND: return "#4040e0";
                case LINE_BACKGROUND: return "#a0a0ff";
                case LINE_BORDER: return "#a0a0ff";
                case LINE_COLOR: return "#4040e0"; 
                case LINE_HIGHLIGHT: return "#ffffff";
            }
            break;
        case SCHEME_GRAY:
            switch (tokentype) {
                case COLOR_NORMAL: return "#47,79,79"; 
                case COLOR_COMMENT: return "#bdbdbd";
                case COLOR_STRING: return "#136,b3,136";
                case COLOR_NUMBER: return "#149,149,149";
                case COLOR_HEX: return "#109,130,145";
                case COLOR_BIN: return "#109,130,145";
                case COLOR_JUMP: return "#47,79,79"; 
                case COLOR_OPERATOR: return "#47,79,79";
                case COLOR_KEYWORD: return "#108,113,128";
                case COLOR_COMPILERKEYWORD: return "#99,99,99";
                case COLOR_SCRIPTKEYWORD: return "#99,99,99";
                case COLOR_ILLEGALOPCODE: return "#bf,a4,128";
                case BACKGROUND: return "#f5,f5,f5";
                case LINE_BACKGROUND: return "#e6e6e6"; // 11,55,66
                case LINE_BORDER: return "#e6e6e6";
                case LINE_COLOR: return "#149,149,149";
                case LINE_HIGHLIGHT: return "#000000";
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
