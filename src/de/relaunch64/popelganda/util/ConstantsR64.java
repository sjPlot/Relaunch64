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

package de.relaunch64.popelganda.util;

import de.relaunch64.popelganda.Relaunch64View;
import de.relaunch64.popelganda.assemblers.*;
import java.awt.Color;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 *
 * @author Daniel Luedecke
 */
public class ConstantsR64 {
    /**
     * This variable stores the current programme and build version number
     */
    public static final String BUILD_VERSION = "3.1.0 (Build 20140525)";
    /**
     * This variable stores the application title that appears in the menu bar or log
     */
    public static final String APPLICATION_SHORT_TITLE = "Relaunch64";
    /**
     * This variable stores the application title that appears in the main window
     * Add "Release Candidate" or "Beta" to this title
     */
    public static final String APPLICATION_TITLE = "Relaunch64 (devel-build)";
    /**
     * This constants stores the website-address where the app can be downloaded
     */
    public static final String UPDATE_URI = "http://sourceforge.net/projects/relaunch64/files/";
    /**
     * This constants stores the address of the file that contains update information for the current app version
     */
    public static final String UPDATE_INFO_URI = "http://www.popelganda.de/update.txt";
    /**
     * 
     */
    public final static Logger r64logger = Logger.getLogger(Relaunch64View.class.getName());
    /**
     * This is the constant for the application's icon
     */
    public static final ImageIcon r64icon = new ImageIcon(ConstantsR64.class.getResource("/de/relaunch64/popelganda/resources/icons/r64_16x16.png"));

    public static final String[] colorpreviews = new String[] {
        "/de/relaunch64/popelganda/resources/img/scheme_01.png",
        "/de/relaunch64/popelganda/resources/img/scheme_02.png",
        "/de/relaunch64/popelganda/resources/img/scheme_03.png",
        "/de/relaunch64/popelganda/resources/img/scheme_04.png",
        "/de/relaunch64/popelganda/resources/img/scheme_05.png",
        "/de/relaunch64/popelganda/resources/img/scheme_06.png",
        "/de/relaunch64/popelganda/resources/img/scheme_07.png",
        "/de/relaunch64/popelganda/resources/img/scheme_08.png",
        "/de/relaunch64/popelganda/resources/img/scheme_09.png",
        "/de/relaunch64/popelganda/resources/img/scheme_10.png",
        "/de/relaunch64/popelganda/resources/img/scheme_11.png",
        "/de/relaunch64/popelganda/resources/img/scheme_12.png",
        "/de/relaunch64/popelganda/resources/img/scheme_13.png",
        "/de/relaunch64/popelganda/resources/img/scheme_14.png",
        "/de/relaunch64/popelganda/resources/img/scheme_15.png",
        "/de/relaunch64/popelganda/resources/img/scheme_16.png"
    };
    
    public static final String STRING_NORMAL = "normal";
    public static final String STRING_COMMENT = "comment";
    public static final String STRING_STRING = "string";
    public static final String STRING_NUMBER = "number";
    public static final String STRING_KEYWORD = "keyword";
    public static final String STRING_COMPILER_KEYWORD = "compilerkeyword";
    public static final String STRING_SCRIPT_KEYWORD = "scriptkeyword";
    public static final String STRING_ILLEGAL_OPCODE = "illegalopcode";
    public static final String STRING_HEXA = "hexa";
    public static final String STRING_LOHI = "lohi";
    public static final String STRING_BIN = "binary";
    public static final String STRING_MACRO = "macro";
    public static final String STRING_JUMP = "jump";

    public static final String STRING_FUNCTION_KICKASSEMBLER = ".function";
    public static final String STRING_MACRO_KICKASSEMBLER = ".macro";
    public static final String STRING_MACRO_TMPX = ".macro";
    public static final String STRING_MACRO_ACME = "!macro";
    public static final String STRING_MACRO_DASM = "mac";
    public static final String STRING_MACRO_DREAMASS = "#macro";
    public static final String STRING_BREAKPOINT_KICKASSEMBLER = ":break()\n";
    public static final String STRING_STRUCT_CA65 = ".struct";
    
    public static final String DEFAULT_FONT = java.awt.Font.MONOSPACED;
    public static final int DEFAULT_FONT_SIZE = 13;
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.white;
    
    public static final String ASSEMBLER_INPUT_FILE = "SOURCEFILE";
    public static final String ASSEMBLER_SOURCE_DIR = "SOURCEDIR";
    public static final String ASSEMBLER_OUPUT_FILE = "OUTFILE";
    public static final String ASSEMBLER_START_ADDRESS = "START";
    public static final String ASSEMBLER_UNCOMPRESSED_FILE = "UNCOMPFILE";
    public static final String ASSEMBLER_COMPRESSED_FILE = "COMPFILE";
    
    public static final String DEFAULT_ACME_PARAM = "--outfile "+ASSEMBLER_OUPUT_FILE+" --format cbm "+ASSEMBLER_INPUT_FILE;
    public static final String DEFAULT_DREAMASS_PARAM = "-I"+ASSEMBLER_SOURCE_DIR+" -o "+ASSEMBLER_OUPUT_FILE+" "+ASSEMBLER_INPUT_FILE;
    public static final String DEFAULT_64TASS_PARAM = "-C -a -i "+ASSEMBLER_INPUT_FILE+" -o "+ASSEMBLER_OUPUT_FILE;
    public static final String DEFAULT_CA65_PARAM = "-o "+ASSEMBLER_OUPUT_FILE;
    public static final String DEFAULT_EXOMIZER_PARAM = "sfx "+ASSEMBLER_START_ADDRESS+" "+ASSEMBLER_UNCOMPRESSED_FILE+" -o "+ASSEMBLER_COMPRESSED_FILE;    
    public static final String DEFAULT_PUCRUNCH_PARAM = ASSEMBLER_UNCOMPRESSED_FILE+" "+ASSEMBLER_COMPRESSED_FILE+" -x"+ASSEMBLER_START_ADDRESS;
    public static final String DEFAULT_KICKASS_PARAM = ASSEMBLER_INPUT_FILE;
    
    public static final String CB_GOTO_DEFAULT_STRING = "Goto ...";
    public static final String CB_GOTO_SECTION_STRING = "Goto section ...";
    public static final String CB_GOTO_LABEL_STRING = "Goto label ...";
    public static final String CB_GOTO_FUNCTION_STRING = "Goto function ...";
    public static final String CB_GOTO_MACRO_STRING = "Goto macro ...";
    
    public static final int ASM_KICKASSEMBLER = 0;
    public static final int ASM_ACME = 1;
    public static final int ASM_64TASS = 2;
    public static final int ASM_CA65 = 3;
    public static final int ASM_DREAMASS = 4;
    public static final int ASM_DASM = 5;
    public static final int ASM_TMPX = 6;

    public static final Assembler assemblers[] = {
        new Assembler_kick(),
        new Assembler_acme(),
        new Assembler_64tass(),
        new Assembler_ca65(),
        new Assembler_dreamass(),
        new Assembler_dasm(),
        new Assembler_tmpx()
    };
    
    // CAUTION! MUST HAVE SAME SIZE AND ORDER AS DEFAULT COMPILER AVAILABLE
    public static final String[] ASM_NAMES = new String[] { "Kick Assembler", "ACME", "64tass", "ca65", "DreamAss", "DASM", "TMPx" };
    
    public static final String[] FILE_EXTENSIONS = new String[] {".a", ".asm", ".src", ".s"};
    public static final String[] FILE_EXTENSIONS_INCLUDES = new String[] {".bin", ".c64", ".txt", ".koa", ".prg", ".pck", ".sid", ".raw", ".iff", ".fli", ".afli", ".ifli"};
    
    public static final String[] SCRIPT_KEYWORDS_KICKASS = new String[] {
        ".add", ".asNumber", ".asBoolean", ".charAt", ".get", ".getData", ".getPixel",
        ".getMulticolorByte", ".getSinglecolorByte", ".size", ".substring", ".string",
        ".toBinaryString", ".toHexString", ".toIntString", ".toOctalString",
        "abs", "acos", "asin", "atan", "atan2", "cbrt", "ceil", "cos", "cosh",
        "exp", "expm1", "floor", "hypot", "List", "log", "log10", "log1p",
        "LoadPicture", "LoadSid", "Matrix", "max", "min", "mod", "MoveMatrix",
        "PerspectiveMatrix", "round", "ScaleMatrix", "sin", "sinh", "sort",
        "signum", "tan", "tanh", "toDegree", "toRadians", "Vector"   
    };
            
    public static final String[] SCRIPT_KEYWORDS_64TASS = new String[] {
        "abs", "acos", "asin", "atan", "atan2", "cbrt", "ceil", "cos", "cosh",
        "deg", "exp", "floor", "frac", "hypot", "log", "log10",
        "max", "min", "mod", "rad", "round", "sign", "sin", "sinh", "sort",
        "signum", "tan", "tanh", "trunc", "bool", "len", "all", "any", "str",
        "repr", "format", "range"
    };
    
    // check os
    public static boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");
    public static boolean IS_OSX = System.getProperty("os.name").toLowerCase().startsWith("mac os");
    public static boolean IS_LINUX = System.getProperty("os.name").toLowerCase().contains("linux");
    public static boolean IS_WINDOWS7 = System.getProperty("os.name").toLowerCase().startsWith("windows 7");
    public static boolean IS_WINDOWS8 = System.getProperty("os.name").toLowerCase().startsWith("windows 8");
    
}
