/*
 * Copyright (C) 2014 Luedeke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.relaunch64.popelganda.util;

import de.relaunch64.popelganda.Editor.EditorPanes;
import de.relaunch64.popelganda.Editor.SyntaxScheme;
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
 * @author Luedeke
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
                case ConstantsR64.COMPILER_ACME:
                    byteToken = "!byte";
                    break;
                case ConstantsR64.COMPILER_DASM:
                    byteToken = "dc.b";
                    break;
                case ConstantsR64.COMPILER_KICKASSEMBLER:
                case ConstantsR64.COMPILER_64TASS:
                case ConstantsR64.COMPILER_DREAMASS:
                case ConstantsR64.COMPILER_CA65:
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
                    sb.append(System.getProperty("line.separator"));
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
    public static String getCruncherStart(String source, int compiler) {
        String cs = SyntaxScheme.getCommentString(compiler) + " start=";
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
    public static String getCustomScriptName(String source, int compiler) {
        String cs = SyntaxScheme.getCommentString(compiler) + " script=";
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
    public static boolean startsWithOpcodeToken(String s) {
        return s.startsWith(".") || s.startsWith("+") || s.startsWith(":") || s.startsWith("-") || s.startsWith("!") || s.startsWith("#");
    }
    public static void insertBasicStart(EditorPanes editorPanes) {
        int dezaddress = 0;
        // open inpu  dialog
        String startaddress = (String)JOptionPane.showInputDialog(null, "Enter start address:");
        // check for return
        if (startaddress!=null && !startaddress.trim().isEmpty()) {
            startaddress = startaddress.trim();
            if (!startaddress.startsWith("$")) {
                try {
                    dezaddress = Integer.parseInt(startaddress);
                }
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
                    dezaddress = Integer.parseInt(startaddress.substring(1), 16);
                }
                catch (NumberFormatException ex2) {
                }
            }
        }
        if (dezaddress!=0) {
            // convcert to string, so we can access each single digit
            startaddress = String.valueOf(dezaddress);
            StringBuilder output = new StringBuilder("$0c,$08,$0a,$00,$9e");
            // copy all digits
            for (int i=0; i<startaddress.length(); i++) {
                output.append(",$3").append(startaddress.charAt(i));
            }
            output.append(",$00,$00,$00").append(System.getProperty("line.separator"));
            switch (editorPanes.getActiveCompiler()) {
                case ConstantsR64.COMPILER_ACME:
                    output.insert(0, "!byte ");
                    break;
                case ConstantsR64.COMPILER_DASM:
                    output.insert(0, "dc.b ");
                    break;
                case ConstantsR64.COMPILER_64TASS:
                case ConstantsR64.COMPILER_CA65:
                case ConstantsR64.COMPILER_KICKASSEMBLER:
                case ConstantsR64.COMPILER_DREAMASS:
                    output.insert(0, ".byte ");
                    break;
            }
            switch (editorPanes.getActiveCompiler()) {
                case ConstantsR64.COMPILER_ACME:
                case ConstantsR64.COMPILER_64TASS:
                case ConstantsR64.COMPILER_CA65:
                case ConstantsR64.COMPILER_DREAMASS:
                    output.insert(0, "*=$0801"+System.getProperty("line.separator"));
                    break;
                case ConstantsR64.COMPILER_KICKASSEMBLER:
                    output.insert(0, ".pc = $0801"+System.getProperty("line.separator"));
                    break;
                case ConstantsR64.COMPILER_DASM:
                    output.insert(0, "org $0801"+System.getProperty("line.separator"));
                    break;
            }
            editorPanes.insertString(output.toString());
        }
    }
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
    public static List<File> drop(DropTargetDropEvent dtde, EditorPanes editorPanes) {
        List<File> openRemainingFiles = new ArrayList<>();
        // 
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
                    List<File> anyfiles = new ArrayList<>();
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
                                anyfiles.add(file);
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
                    // if so, include files
                    if (linkedfiles.size()>0) {
                        for (File f : linkedfiles) {
                            String rf = FileTools.getRelativePath(editorPanes.getActiveFilePath(), f);
                            editorPanes.insertString("\""+rf+"\""+System.getProperty("line.separator"));
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
                                if (FileTools.getFileExtension(f).equalsIgnoreCase("txt")) {
                                    switch (editorPanes.getActiveCompiler()) {
                                        case ConstantsR64.COMPILER_ACME:
                                            insert = "!bin \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_KICKASSEMBLER:
                                            insert = ".import text \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_CA65:
                                            insert = ".incbin \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_DREAMASS:
                                            insert = ".binclude \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_DASM:
                                            insert = "incbin \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_64TASS:
                                            insert = ".binary \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                    }
                                }
                                else if (FileTools.getFileExtension(f).equalsIgnoreCase("c64") || FileTools.getFileExtension(f).equalsIgnoreCase("prg")) {
                                    switch (editorPanes.getActiveCompiler()) {
                                        case ConstantsR64.COMPILER_ACME:
                                            insert = "!bin \""+relpath+"\",,2"+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_KICKASSEMBLER:
                                            insert = ".import c64 \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_CA65:
                                            insert = ".incbin \""+relpath+"\",2"+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_DREAMASS:
                                            insert = ".binclude \""+relpath+"\",2"+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_DASM:
                                            insert = "incbin \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_64TASS:
                                            insert = ".binary \""+relpath+"\",2"+System.getProperty("line.separator");
                                            break;
                                    }
                                }
                                else {
                                    switch (editorPanes.getActiveCompiler()) {
                                        case ConstantsR64.COMPILER_ACME:
                                            insert = "!bin \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_KICKASSEMBLER:
                                            insert = ".import binary \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_CA65:
                                            insert = ".incbin \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_DASM:
                                            insert = "incbin \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_DREAMASS:
                                            insert = ".binclude \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                        case ConstantsR64.COMPILER_64TASS:
                                            insert = ".binary \""+relpath+"\""+System.getProperty("line.separator");
                                            break;
                                    }
                                }
                                
                            }
                            editorPanes.insertString(insert);
                        }
                    }
                    // check if we have any valid values,
                    // i.e. any files have been dragged and dropped
                    // if so, open asm files
                    if (anyfiles.size()>0) {
                        for (File f : anyfiles) {
                            // if user hold down ctrl-key, use import-directive for asm-files
                            if (dtde.getDropAction()==DnDConstants.ACTION_COPY) {
                                String insert = "";
                                String relpath = FileTools.getRelativePath(editorPanes.getActiveFilePath(), f);
                                switch (editorPanes.getActiveCompiler()) {
                                    case ConstantsR64.COMPILER_ACME:
                                        insert = "!src \""+relpath+"\""+System.getProperty("line.separator");
                                        break;
                                    case ConstantsR64.COMPILER_DASM:
                                        insert = "include \""+relpath+"\""+System.getProperty("line.separator");
                                        break;
                                    case ConstantsR64.COMPILER_KICKASSEMBLER:
                                        insert = ".import source \""+relpath+"\""+System.getProperty("line.separator");
                                        break;
                                    case ConstantsR64.COMPILER_CA65:
                                        insert = ".include \""+relpath+"\""+System.getProperty("line.separator");
                                        break;
                                    case ConstantsR64.COMPILER_64TASS:
                                        insert = ".binclude \""+relpath+"\""+System.getProperty("line.separator");
                                        break;
                                    case ConstantsR64.COMPILER_DREAMASS:
                                        insert = "#include \""+relpath+"\""+System.getProperty("line.separator");
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
        return openRemainingFiles;
    }
}
