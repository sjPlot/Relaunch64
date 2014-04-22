/*
 * Relaunch64 - A Java Crossassembler for C64 machine language coding.
 * Copyright (C) 2001-2013 by Daniel Lüdecke (http://www.danielluedecke.de)
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
    public static final String BUILD_VERSION = "3.0.0.0 (Build 20140421)";
    /**
     * This variable stores the application title that appears in the main window
     */
    public static final String APPLICATION_TITLE = "Relaunch64 (beta 10)";
    /**
     * This constants stores the website-address where the app can be downloaded
     */
    public static final String UPDATE_URI = "http://www.popelganda.de/download/";
    /**
     * This constants stores the address of the file that contains update information for the current app version
     */
    public static final String UPDATE_INFO_URI = "http://www.popelganda.de/download/update.txt";
    /**
     * 
     */
    public final static Logger r64logger = Logger.getLogger(Relaunch64View.class.getName());
    /**
     * This is the constant for the application's icon
     */
    public static final ImageIcon r64icon = new ImageIcon(ConstantsR64.class.getResource("/de/relaunch64/popelganda/resources/icons/r64_16x16.png"));
    
    public static final String STRING_NORMAL = "normal";
    public static final String STRING_COMMENT = "comment";
    public static final String STRING_STRING = "string";
    public static final String STRING_NUMBER = "number";
    public static final String STRING_KEYWORD = "keyword";
    public static final String STRING_COMPILER_KEYWORD = "compilerkeyword";
    public static final String STRING_HEXA = "hexa";
    public static final String STRING_LOHI = "lohi";
    public static final String STRING_BIN = "binary";
    public static final String STRING_MACRO = "macro";
    public static final String STRING_JUMP = "jump";

    public static final String STRING_FUNCTION_KICKASSEMBLER = ".function";
    public static final String STRING_MACRO_KICKASSEMBLER = ".macro";
    
    public static final String DEFAULT_FONT = java.awt.Font.MONOSPACED;
    public static final int DEFAULT_FONT_SIZE = 13;
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.white;
    
    public static final String ASSEMBLER_INPUT_FILE = "SOURCEFILE";
    public static final String ASSEMBLER_OUPUT_FILE = "OUTFILE";
    
    public static final String CB_GOTO_DEFAULT_STRING = "Goto ...";
    public static final String CB_GOTO_SECTION_STRING = "Goto section ...";
    public static final String CB_GOTO_LABEL_STRING = "Goto label ...";
    public static final String CB_GOTO_FUNCTION_STRING = "Goto function or macro ...";
    
    public static final int EMU_VICE = 0;
    public static final int EMU_CCS64 = 1;
    public static final int EMU_FRODO = 2;
    public static final int EMU_EMU64 = 3;
    // CAUTION! MUST HAVE SAME SIZE AS DEFAULT EMULATORS AVAILABLE
    public static final String[] EMU_NAMES = new String[] { "Vice", "CCS64", "Frodo", "Emu64" };
    
    public static final int COMPILER_KICKASSEMBLER = 0;
    public static final int COMPILER_ACME = 1;
    // CAUTION! MUST HAVE SAME SIZE AS DEFAULT COMPILER AVAILABLE
    public static final String[] COMPILER_NAMES = new String[] { "Kick Assembler", "ACME" };
    
    public static final String[] FILE_EXTENSIONS = new String[] {".a", ".asm", ".txt"};
}
