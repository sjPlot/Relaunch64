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

import de.relaunch64.popelganda.Editor.EditorPanes;
import de.relaunch64.popelganda.Editor.RL64TextArea;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;

/**
 *
 * @author Daniel Lüdecke
 */
public class Tools {
    /**
     * Checks if a specifc character {@code character} is a syntax delimiter char or not.
     * 
     * @param character A string of length 1 (a character) which should be compared to the syntax
     * delimiter list.
     * @param additionalChars Additional chars that can be added to the default delimiter list.
     * @return {@code true} if {@code character} is a syntax delimiter char, {@code false} otherwise.
     */
    public static boolean isDelimiter(String character, String additionalChars) {
        String delimiterList = ",;{}()[]+-/<=>&|^~*#" + additionalChars;
        return Character.isWhitespace(character.charAt(0)) || delimiterList.contains(character);
    }
    /**
     * Checks if a specifc character {@code character} is a syntax delimiter char or not.
     * 
     * @param character A character which should be compared to the syntax delimiter list.
     * @param additionalChars Additional chars that can be added to the default delimiter list.
     * @return {@code true} if {@code character} is a syntax delimiter char, {@code false} otherwise.
     */
    public static boolean isDelimiter(Character character, String additionalChars) {
        String delimiterList = ",;{}()[]+-/<=>&|^~*#" + additionalChars;
        return Character.isWhitespace(character) || delimiterList.indexOf(character)!=-1;
    }
    /**
     * This method retrieves system information like the operating system and version, the architecture,
     * the used java runtime environment and the official vendor of the jre, and the java home directory.
     * @return a string with the above described system information
     */
    public static String getSystemInformation() {
        StringBuilder sysinfo = new StringBuilder("");
        sysinfo.append(ConstantsR64.APPLICATION_SHORT_TITLE).append(" ").append(ConstantsR64.BUILD_VERSION).append(System.getProperty("line.separator"));
        sysinfo.append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append(" (").append(System.getProperty("os.arch")).append(")").append(System.getProperty("line.separator"));
        sysinfo.append("Java-Version ").append(System.getProperty("java.version")).append(" (").append(System.getProperty("java.vendor")).append(")").append(System.getProperty("line.separator"));
        sysinfo.append(System.getProperty("java.home"));
        return sysinfo.toString();
    }
    /**
     * Reads a binary file and returns its content as byte table string.
     * 
     * @param f FilePath to the binary file
     * @param compiler the currently active compiler. used for the compiler-specific byte-token
     * @return a string value with the complete byte table, i.e. all byte values from the
     * file are converted to a byte-table. Or {@code null} if an error occured.
     */
    public static String getByteTableFromFile(File f, int compiler) {
        return getByteTableFromFile(f, 0, -1, compiler, 8);
    }
    /**
     * Reads a binary file and returns its content as byte table string.
     * 
     * @param f FilePath to the binary file
     * @param compiler the currently active compiler. used for the compiler-specific byte-token
     * @param bytesPerLine the amounts of bytes per line in the byte table
     * @return a string value with the complete byte table, i.e. all byte values from the
     * file are converted to a byte-table. Or {@code null} if an error occured.
     */
    public static String getByteTableFromFile(File f, int compiler, int bytesPerLine) {
        return getByteTableFromFile(f, 0, -1, compiler, bytesPerLine);
    }
    /**
     * Reads a binary file and returns its content as byte table string.
     * 
     * @param f FilePath to the binary file
     * @param startOffset start offset for the first byte to read from file
     * @param endOffset end offset, the last byte that should be read from file
     * @param compiler the currently active compiler. used for the compiler-specific byte-token
     * @param bytesPerLine the amounts of bytes per line in the byte table
     * @return a string value with the complete byte table, i.e. all byte values from the
     * file are converted to a byte-table. Or {@code null} if an error occured.
     */
    public static String getByteTableFromFile(File f, int startOffset, int endOffset, int compiler, int bytesPerLine) {
        // read File and retrieve content as byte arry
        byte[] content = FileTools.readBinaryFile(f, startOffset, endOffset);
        // check if we have content
        if (content!=null && content.length>0) {
            // get compiler byte-token
            String byteToken;
            switch (compiler) {
                case ConstantsR64.ASM_ACME:
                    byteToken = "!byte";
                    break;
                case ConstantsR64.ASM_DASM:
                    byteToken = "dc.b";
                    break;
                case ConstantsR64.ASM_KICKASSEMBLER:
                case ConstantsR64.ASM_64TASS:
                case ConstantsR64.ASM_TMPX:
                case ConstantsR64.ASM_DREAMASS:
                case ConstantsR64.ASM_CA65:
                    byteToken = ".byte";
                    break;
                default:
                    byteToken = ".byte";
                    break;
            }
            StringBuilder sb = new StringBuilder("");
            // some indicators for new lines and line-length of table
            boolean startNewLine = true;
            int bpl = 0;
            for (int i=0; i<content.length; i++) {
                // check if we have a new line
                if (startNewLine) {
                    sb.append(byteToken).append(" ");
                    startNewLine = false;
                }
                // append byte
                sb.append("$").append(String.format("%02x", content[i]));
                // increase counter for bytes per line
                bpl++;
                // check if we reached end of line
                if (bpl>=bytesPerLine || i==(content.length-1)) {
                    sb.append("\n");
                    // start new line
                    startNewLine = true;
                    // reset bytes per line counter
                    bpl = 0;
                }
                else {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
        return null;
    }
    /**
     * This methods checks if an ACME source code contains the {@code !to} directive,
     * and returns the file name if it was found. The file name can then be used as
     * parameter when calling ACME.
     * 
     * <b>Note:</b> This method is currently not used!
     * 
     * @param source The (current) source code which should be examined for {@code !to} directive.
     * @return The file indicated by the {@code !to} directive, or {@code null} if not found. 
     */
    public static String getAcmeToFile(String source) {
        if (source!=null && source.contains("!to")) {
            // retrieve !to macro
            int position = source.indexOf("!to");
            // find file name
            int fileNameStart = source.indexOf("\"", position);
            int fileNameEnd = source.indexOf("\"", fileNameStart+1);
            // return file name
            try {
                return source.substring(fileNameStart+1, fileNameEnd);
            }
            catch (IndexOutOfBoundsException ex) {
            }
        }
        return null;
    }
    /**
     * This method searches the source code {@code source} for a start label
     * that defines the start address of the compiled source. Can be used together
     * with placeholder {@code START} as parameter for cruncher and packer.
     * 
     * @param source The (current) source code which should be examined for start label.
     * @param compiler The compiler, which indicates the syntax used by {@code source}.
     * @return the start-address indicated by the start label, or {@code null} if no
     * start address was found.
     */
    public static String getCruncherStart(String source, int compiler) {
        String cs = RL64TextArea.getCommentString(compiler) + " start=";
        // find start token
        int position = source.toLowerCase().indexOf(cs);
        // found?
        if (position!=-1) {
            // end of line?
            int eol = source.indexOf("\n", position+cs.length());
            // retrieve line
            String start = source.substring(position+cs.length(), eol);
            // remove white spaces
            start = start.replace("\r", "").trim();
            // return result
            return start;
        }
        return null;
    }
    /**
     * This method searches the source code {@code source} for a script label
     * that defines a custom compiler script that should be used when compiling this sourc.
     * 
     * @param source The (current) source code which should be examined for script label.
     * @param compiler The compiler, which indicates the syntax used by {@code source}.
     * @return the script name indicated by the script label, or {@code null} if no
     * script name was found.
     */
    public static String getCustomScriptName(String source, int compiler) {
        String cs = RL64TextArea.getCommentString(compiler) + " script=";
        // find start token
        int position = source.toLowerCase().indexOf(cs);
        // found?
        if (position!=-1) {
            // end of line?
            int eol = source.indexOf("\n", position+cs.length());
            // retrieve line
            String start = source.substring(position+cs.length(), eol);
            // remove white spaces
            start = start.replace("\r", "").trim();
            // return result
            return start;
        }
        return null;
    }
    /**
     * Checks if a string starts with a specific opcode or compiler directive char. This may
     * indicate that the following word is a specific opcode or compiler directive, and no
     * asm keyword. Used for the tab/shift-tab behaviour if text is selected.
     * 
     * @param s The string that should be examined for the specific token or char.
     * @return {@code true} if a specific opcode or compiler directive char was found,
     * false otherwise.
     */
    public static boolean startsWithOpcodeToken(String s) {
        return s.startsWith(".") || s.startsWith("+") || s.startsWith(":") || s.startsWith("-") || s.startsWith("!") || s.startsWith("#");
    }
    /**
     * This method inserts a Basic upstart code snippet into the currently active source code,
     * so the source will be executed after compiling and running in an emulator. This is needed,
     * if the source file will not be crunched with a packer.
     * 
     * @param editorPanes A reference to the EditorPanes class, so the current source code
     * and compiler / syntax settings can be examined.
     */
    public static void insertBasicStart(EditorPanes editorPanes) {
        int dezaddress = 0;
        // open input  dialog
        String startaddress = (String)JOptionPane.showInputDialog(null, "Enter start address:");
        // check for return value / valid input
        if (startaddress!=null && !startaddress.trim().isEmpty()) {
            // remove whitespace
            startaddress = startaddress.trim();
            // check if user entered decimal address
            if (!startaddress.startsWith("$")) {
                try {
                    // parse integer
                    dezaddress = Integer.parseInt(startaddress);
                }
                // if pasring fails, we assume that user 
                // entered hex-address without "$"
                catch (NumberFormatException ex) {
                    try {
                        dezaddress = Integer.parseInt(startaddress, 16);
                    }
                    catch (NumberFormatException ex2) {
                    }
                }
            }
            else {
                try {
                    // we have a "$", so parse hex-string without leading "$"
                    dezaddress = Integer.parseInt(startaddress.substring(1), 16);
                }
                catch (NumberFormatException ex2) {
                }
            }
        }
        if (dezaddress!=0) {
            // convert to string, so we can access each single digit
            startaddress = String.valueOf(dezaddress);
            StringBuilder output = new StringBuilder("$0c,$08,$0a,$00,$9e");
            // copy all digits
            for (int i=0; i<startaddress.length(); i++) {
                output.append(",$3").append(startaddress.charAt(i));
            }
            output.append(",$00,$00,$00").append("\n");
            switch (editorPanes.getActiveCompiler()) {
                case ConstantsR64.ASM_ACME:
                    output.insert(0, "!byte ");
                    break;
                case ConstantsR64.ASM_DASM:
                    output.insert(0, "dc.b ");
                    break;
                case ConstantsR64.ASM_64TASS:
                case ConstantsR64.ASM_CA65:
                case ConstantsR64.ASM_KICKASSEMBLER:
                case ConstantsR64.ASM_TMPX:
                case ConstantsR64.ASM_DREAMASS:
                    output.insert(0, ".byte ");
                    break;
            }
            switch (editorPanes.getActiveCompiler()) {
                case ConstantsR64.ASM_ACME:
                case ConstantsR64.ASM_TMPX:
                case ConstantsR64.ASM_64TASS:
                case ConstantsR64.ASM_CA65:
                case ConstantsR64.ASM_DREAMASS:
                    output.insert(0, "*=$0801\n");
                    break;
                case ConstantsR64.ASM_KICKASSEMBLER:
                    output.insert(0, ".pc = $0801\n");
                    break;
                case ConstantsR64.ASM_DASM:
                    output.insert(0, "org $0801\n");
                    break;
            }
            editorPanes.insertString(output.toString());
        }
    }
    /**
     * Counts the amount of {@code sub} occurences in {@code str}.
     * 
     * @param str the source string that should be examined.
     * @param sub the char which should be searched for and counted.
     * @return the count of how many times {@code sub} occurs in {@code str},
     * or 0 if no match found or {@code str} is {@code null}.
     */
    public static int countMatches(final CharSequence str, final char sub) {
        if (null==str || str.length()==0) return 0;
        int counter = 0;
        for (int i=0; i<str.length(); i++ ) {
            if (str.charAt(i) == sub ) {
                counter++;
            } 
        }
        return counter;
    }
    /**
     * Handles the drop actions when user drags and drops files on the editor component.
     * Depending on the drop action, file names or file pathes are included via
     * compiler opcodes or directives. All files that should not be included in the
     * source, but opened in a new tab, will be returned as {@code List<File>}.
     * 
     * @param dtde the {@code DropTargetDropEvent}, fired by the drop-event-handler.
     * @param editorPanes a reference to the EditorPanes class.
     * @return a {@code List} of type {@code File} with all files that should not be included in the
     * source, but opened in a new tab. Returns an empty list if no files should be opened.
     */
    public static List<File> drop(DropTargetDropEvent dtde, EditorPanes editorPanes) {
        List<File> openRemainingFiles = new ArrayList<>();
        boolean validDropLocation = false;
        // get transferable
        Transferable tr = dtde.getTransferable();
        try {
            // check whether we have files dropped into textarea
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                // drag&drop was link action
                dtde.acceptDrop(DnDConstants.ACTION_LINK | DnDConstants.ACTION_COPY_OR_MOVE);
                // retrieve drop component
                Component c = dtde.getDropTargetContext().getDropTarget().getComponent();
                // check for valid value
                if (c!=null) {
                    // retrieve component's name
                    String name = c.getName();
                    // check for valid value
                    if (name!=null && !name.isEmpty()) {
                        // check if files were dropped in entry field
                        // in this case, image files will we inserted into
                        // the entry, not attached as attachments
                        if (name.equalsIgnoreCase("jEditorPaneMain")) {
                            validDropLocation = true;
                        }
                        else {
                            ConstantsR64.r64logger.log(Level.WARNING,"No valid drop location, drop rejected");
                            dtde.rejectDrop();
                        }
                    }
                }
                // retrieve list of dropped files
                java.util.List files = (java.util.List)tr.getTransferData(DataFlavor.javaFileListFlavor);
                // check for valid values
                if (files!=null && files.size()>0) {
                    // create list with final image files
                    List<File> sourcefiles = new ArrayList<>();
                    List<File> includefiles = new ArrayList<>();
                    List<File> linkedfiles = new ArrayList<>();
                    // dummy
                    File file;
                    for (Object file1 : files) {
                        // get each single object from droplist
                        file = (File) file1;
                        // check whether it is a file
                        if (file.isFile()) {
                            // if we have link action, only insert paths
                            if (dtde.getDropAction()==DnDConstants.ACTION_LINK && validDropLocation) {
                                linkedfiles.add(file);
                            }
                            // if it's an asm, add it to asm file list
                            else if (FileTools.hasValidFileExtension(file) && validDropLocation) {
                                // if so, add it to list
                                sourcefiles.add(file);
                            }
                            // if it's an include file, add it to include file list
                            else if (FileTools.hasValidIncludeFileExtension(file) && validDropLocation) {
                                // if so, add it to list
                                includefiles.add(file);
                            }
                        }
                    }
                    // check if we have any valid values,
                    // i.e. any files have been dragged and dropped
                    // here we handle linked files, where only the file path should
                    // be added to the source
                    if (linkedfiles.size()>0) {
                        for (File f : linkedfiles) {
                            String rf = FileTools.getRelativePath(editorPanes.getActiveFilePath(), f);
                            editorPanes.insertString("\""+rf+"\"\n");
                        }
                    }
                    // check if we have any valid values,
                    // i.e. any files have been dragged and dropped
                    // if so, include files
                    if (includefiles.size()>0) {
                        for (File f : includefiles) {
                            String insert = "";
                            // if user hold down ctrl-key, import bytes from file
                            if (dtde.getDropAction()==DnDConstants.ACTION_COPY) {
                                insert = Tools.getByteTableFromFile(f, editorPanes.getActiveCompiler());
                            }
                            // else use include-directive
                            else {
                                // retrieve relative path of iimport file
                                String relpath = FileTools.getRelativePath(editorPanes.getActiveFilePath(), f);
                                // *************************
                                // here we handle text files
                                // *************************
                                if (FileTools.getFileExtension(f).equalsIgnoreCase("txt")) {
                                    switch (editorPanes.getActiveCompiler()) {
                                        case ConstantsR64.ASM_ACME:
                                            insert = "!bin \""+relpath+"\"\n";
                                            break;
                                        case ConstantsR64.ASM_TMPX:
                                            insert = ".binary \""+relpath+"\"\n";
                                            break;
                                        case ConstantsR64.ASM_KICKASSEMBLER:
                                            insert = ".import text \""+relpath+"\"\n";
                                            break;
                                        case ConstantsR64.ASM_CA65:
                                            insert = ".incbin \""+relpath+"\"\n";
                                            break;
                                        case ConstantsR64.ASM_DREAMASS:
                                            insert = ".binclude \""+relpath+"\"\n";
                                            break;
                                        case ConstantsR64.ASM_DASM:
                                            insert = "incbin \""+relpath+"\"\n";
                                            break;
                                        case ConstantsR64.ASM_64TASS:
                                            insert = ".binary \""+relpath+"\"\n";
                                            break;
                                    }
                                }
                                // *************************
                                // here we handle c64 or prg files
                                // *************************
                                else if (FileTools.getFileExtension(f).equalsIgnoreCase("c64") || FileTools.getFileExtension(f).equalsIgnoreCase("prg")) {
                                    switch (editorPanes.getActiveCompiler()) {
                                        case ConstantsR64.ASM_ACME:
                                            insert = "!bin \""+relpath+"\",,2\n";
                                            break;
                                        case ConstantsR64.ASM_TMPX:
                                            insert = ".binary \""+relpath+"\",2\n";
                                            break;
                                        case ConstantsR64.ASM_KICKASSEMBLER:
                                            insert = ".import c64 \""+relpath+"\"\n";
                                            break;
                                        case ConstantsR64.ASM_CA65:
                                            insert = ".incbin \""+relpath+"\",2\n";
                                            break;
                                        case ConstantsR64.ASM_DREAMASS:
                                            insert = ".binclude \""+relpath+"\",2\n";
                                            break;
                                        case ConstantsR64.ASM_DASM:
                                            insert = "incbin \""+relpath+"\"\n";
                                            break;
                                        case ConstantsR64.ASM_64TASS:
                                            insert = ".binary \""+relpath+"\",2\n";
                                            break;
                                    }
                                }
                                // *************************
                                // here we handle any other valid binary files
                                // like koa, fli, bin etc. See ConstantsR64.FILE_EXTENSIONS_INCLUDES
                                // and FileTools.hasValidIncludeFileExtension for further info
                                // *************************
                                else {
                                    switch (editorPanes.getActiveCompiler()) {
                                        case ConstantsR64.ASM_ACME:
                                            insert = "!bin \""+relpath+"\"\n";
                                            break;
                                        case ConstantsR64.ASM_TMPX:
                                            insert = ".binary \""+relpath+"\"\n";
                                            break;
                                        case ConstantsR64.ASM_KICKASSEMBLER:
                                            insert = ".import binary \""+relpath+"\"\n";
                                            break;
                                        case ConstantsR64.ASM_CA65:
                                            insert = ".incbin \""+relpath+"\"\n";
                                            break;
                                        case ConstantsR64.ASM_DASM:
                                            insert = "incbin \""+relpath+"\"\n";
                                            break;
                                        case ConstantsR64.ASM_DREAMASS:
                                            insert = ".binclude \""+relpath+"\"\n";
                                            break;
                                        case ConstantsR64.ASM_64TASS:
                                            insert = ".binary \""+relpath+"\"\n";
                                            break;
                                    }
                                }
                                
                            }
                            editorPanes.insertString(insert);
                        }
                    }
                    // check if we have any valid values,
                    // i.e. any files have been dragged and dropped
                    // if so, include asm files via opcode / directive
                    if (sourcefiles.size()>0) {
                        for (File f : sourcefiles) {
                            // if user hold down ctrl-key, use import-directive for asm-files
                            if (dtde.getDropAction()==DnDConstants.ACTION_COPY) {
                                String insert = "";
                                String relpath = FileTools.getRelativePath(editorPanes.getActiveFilePath(), f);
                                switch (editorPanes.getActiveCompiler()) {
                                    case ConstantsR64.ASM_ACME:
                                        insert = "!src \""+relpath+"\"\n";
                                        break;
                                    case ConstantsR64.ASM_TMPX:
                                        insert = ".include \""+relpath+"\"\n";
                                        break;
                                    case ConstantsR64.ASM_DASM:
                                        insert = "include \""+relpath+"\"\n";
                                        break;
                                    case ConstantsR64.ASM_KICKASSEMBLER:
                                        insert = ".import source \""+relpath+"\"\n";
                                        break;
                                    case ConstantsR64.ASM_CA65:
                                        insert = ".include \""+relpath+"\"\n";
                                        break;
                                    case ConstantsR64.ASM_64TASS:
                                        insert = ".binclude \""+relpath+"\"\n";
                                        break;
                                    case ConstantsR64.ASM_DREAMASS:
                                        insert = "#include \""+relpath+"\"\n";
                                        break;
                                }
                                editorPanes.insertString(insert);
                            }
                            else {
                                // else open files
                                openRemainingFiles.add(f);
                            }
                        }
                        /**
                         * JDK 8 Lamda
                         */
//                        anyfiles.stream().forEach((f) -> {
//                            openFile(f);
//                        });
                    }
                }
                dtde.getDropTargetContext().dropComplete(true);
            } else {
                ConstantsR64.r64logger.log(Level.WARNING,"DataFlavor.javaFileListFlavor is not supported, drop rejected");
                dtde.rejectDrop();
            }
        }
        catch (IOException | UnsupportedFlavorException ex) {
            ConstantsR64.r64logger.log(Level.WARNING,ex.getLocalizedMessage());
            dtde.rejectDrop();
        }
        // return remaining files that should be opened in a new tab
        return openRemainingFiles;
    }
}
