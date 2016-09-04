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

import de.relaunch64.popelganda.Editor.EditorPanes;
import de.relaunch64.popelganda.assemblers.Assembler;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author Daniel Lüdecke
 */
public class Tools {

    /**
     * Checks if a specifc character {@code character} is a syntax delimiter
     * char or not.
     *
     * @param character A string of length 1 (a character) which should be
     * compared to the syntax delimiter list.
     * @param additionalChars Additional chars that can be added to the default
     * delimiter list.
     * @return {@code true} if {@code character} is a syntax delimiter char,
     * {@code false} otherwise.
     */
    public static boolean isDelimiter(String character, String additionalChars) {
        String delimiterList = ",;{}()[]+-/<=>&|^~*#" + additionalChars;
        return Character.isWhitespace(character.charAt(0)) || delimiterList.contains(character);
    }

    /**
     * Checks if a specifc character {@code character} is a syntax delimiter
     * char or not.
     *
     * @param character A character which should be compared to the syntax
     * delimiter list.
     * @param additionalChars Additional chars that can be added to the default
     * delimiter list.
     * @return {@code true} if {@code character} is a syntax delimiter char,
     * {@code false} otherwise.
     */
    public static boolean isDelimiter(Character character, String additionalChars) {
        String delimiterList = ",;{}()[]+-/<=>&|^~*#" + additionalChars;
        return Character.isWhitespace(character) || delimiterList.indexOf(character) != -1;
    }

    /**
     * This method retrieves system information like the operating system and
     * version, the architecture, the used java runtime environment and the
     * official vendor of the jre, and the java home directory.
     *
     * @return a string with the above described system information
     */
    public static String getSystemInformation() {
        StringBuilder sysinfo = new StringBuilder("");
        sysinfo.append(ConstantsR64.APPLICATION_SHORT_TITLE).append(" ").append(ConstantsR64.BUILD_VERSION).append(System.lineSeparator());
        sysinfo.append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append(" (").append(System.getProperty("os.arch")).append(")").append(System.lineSeparator());
        sysinfo.append("Java-Version ").append(System.getProperty("java.version")).append(" (").append(System.getProperty("java.vendor")).append(")").append(System.lineSeparator());
        sysinfo.append(System.getProperty("java.home"));
        return sysinfo.toString();
    }

    /**
     * Reads a binary file and returns its content as byte table string.
     *
     * @param f FilePath to the binary file
     * @param assembler the currently active assembler. used for the
     * assembler-specific byte-token
     * @return a string value with the complete byte table, i.e. all byte values
     * from the file are converted to a byte-table. Or {@code null} if an error
     * occured.
     */
    public static String getByteTableFromFile(File f, Assembler assembler) {
        return getByteTableFromFile(f, 0, -1, assembler, 8);
    }

    /**
     * Reads a binary file and returns its content as byte table string.
     *
     * @param f FilePath to the binary file
     * @param assembler the currently active assembler. used for the
     * assembler-specific byte-token
     * @param bytesPerLine the amounts of bytes per line in the byte table
     * @return a string value with the complete byte table, i.e. all byte values
     * from the file are converted to a byte-table. Or {@code null} if an error
     * occured.
     */
    public static String getByteTableFromFile(File f, Assembler assembler, int bytesPerLine) {
        return getByteTableFromFile(f, 0, -1, assembler, bytesPerLine);
    }

    /**
     * Reads a binary file and returns its content as byte table string.
     *
     * @param f FilePath to the binary file
     * @param startOffset start offset for the first byte to read from file
     * @param endOffset end offset, the last byte that should be read from file
     * @param assembler the currently active assembler. used for the
     * assembler-specific byte-token
     * @param bytesPerLine the amounts of bytes per line in the byte table
     * @return a string value with the complete byte table, i.e. all byte values
     * from the file are converted to a byte-table. Or {@code null} if an error
     * occured.
     */
    public static String getByteTableFromFile(File f, int startOffset, int endOffset, Assembler assembler, int bytesPerLine) {
        // read File and retrieve content as byte arry
        byte[] content = FileTools.readBinaryFile(f, startOffset, endOffset);
        // check if we have content
        if (content != null && content.length > 0) {
            // get compiler byte-token
            String byteToken = assembler.getByteDirective();
            StringBuilder sb = new StringBuilder("");
            // some indicators for new lines and line-length of table
            boolean startNewLine = true;
            int bpl = 0;
            for (int i = 0; i < content.length; i++) {
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
                if (bpl >= bytesPerLine || i == (content.length - 1)) {
                    sb.append("\n");
                    // start new line
                    startNewLine = true;
                    // reset bytes per line counter
                    bpl = 0;
                } else {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
        return null;
    }

    /**
     * This methods checks if an ACME source code contains the {@code !to}
     * directive, and returns the file name if it was found. The file name can
     * then be used as parameter when calling ACME.
     *
     * <b>Note:</b> This method is currently not used!
     *
     * @param source The (current) source code which should be examined for
     * {@code !to} directive.
     * @return The file indicated by the {@code !to} directive, or {@code null}
     * if not found.
     */
    public static String getAcmeToFile(String source) {
        if (source != null && source.contains("!to")) {
            // retrieve !to macro
            int position = source.indexOf("!to");
            // find file name
            int fileNameStart = source.indexOf("\"", position);
            int fileNameEnd = source.indexOf("\"", fileNameStart + 1);
            // return file name
            try {
                return source.substring(fileNameStart + 1, fileNameEnd);
            } catch (IndexOutOfBoundsException ex) {
            }
        }
        return null;
    }

    /**
     * This method searches the source code {@code source} for a start label
     * that defines the start address of the compiled source. Can be used
     * together with placeholder {@code START} as parameter for cruncher and
     * packer.
     *
     * @param source The (current) source code which should be examined for
     * start label.
     * @param commentString
     * @return the start-address indicated by the start label, or {@code null}
     * if no start address was found.
     */
    public static String getCruncherStart(String source, String commentString) {
        String cruncherStart = getCommentedToken(source, commentString, "start=");
        // there may be text after address, so split at white char return only first part
        return (cruncherStart != null) ? cruncherStart.split("\\s")[0] : null;
    }

    /**
     * This method searches the source code {@code source} for a script label
     * that defines a custom compiler script that should be used when compiling
     * this source.
     *
     * @param source The (current) source code which should be examined for
     * script label.
     * @param commentString
     * @return the script name indicated by the script label, or {@code null} if
     * no script name was found.
     */
    public static String getCustomScriptName(String source, String commentString) {
        return getCommentedToken(source, commentString, "script=");
    }

    /**
     *
     * @param source
     * @param commentString
     * @param token
     * @return
     */
    protected static String getCommentedToken(String source, String commentString, String token) {
        // create line reader to read line by line
        StringReader sr = new StringReader(source);
        BufferedReader br = new BufferedReader(sr);
        LineNumberReader lineReader = new LineNumberReader(br);
        String line;
        // create regex pattern which extracts string behind commented "script="
        Pattern p = Pattern.compile("^\\s*" + commentString + "\\s*(" + Pattern.quote(token) + ")(.*)");
        // read line by line
        try {
            while ((line = lineReader.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    continue;
                }
                // if pattern found, return script name
                return m.group(2);
            }
        } catch (IOException ex) {
        }
        return null;
    }

    /**
     * Checks if a string starts with a specific opcode or compiler directive
     * char. This may indicate that the following word is a specific opcode or
     * compiler directive, and no asm keyword. Used for the tab/shift-tab
     * behaviour if text is selected.
     *
     * @param s The string that should be examined for the specific token or
     * char.
     * @return {@code true} if a specific opcode or compiler directive char was
     * found, false otherwise.
     */
    public static boolean startsWithOpcodeToken(String s) {
        return s.startsWith(".") || s.startsWith("+") || s.startsWith(":") || s.startsWith("-") || s.startsWith("!") || s.startsWith("#");
    }

    /**
     * This method inserts a Basic upstart code snippet into the currently
     * active source code, so the source will be executed after compiling and
     * running in an emulator. This is needed, if the source file will not be
     * crunched with a packer.
     *
     * @param editorPanes A reference to the EditorPanes class, so the current
     * source code and compiler / syntax settings can be examined.
     */
    public static void insertBasicStart(EditorPanes editorPanes) {
        int dezaddress = 0;
        // open input  dialog
        String startaddress = (String) JOptionPane.showInputDialog(null, "Enter start address:", "Insert Basic start", JOptionPane.PLAIN_MESSAGE, null, null, "2061");
        // check for return value / valid input
        if (startaddress != null && !startaddress.trim().isEmpty()) {
            // remove whitespace
            startaddress = startaddress.trim();
            // check if user entered decimal address
            if (!startaddress.startsWith("$")) {
                try {
                    // parse integer
                    dezaddress = Integer.parseInt(startaddress);
                } // if pasring fails, we assume that user 
                // entered hex-address without "$"
                catch (NumberFormatException ex) {
                    try {
                        dezaddress = Integer.parseInt(startaddress, 16);
                    } catch (NumberFormatException ex2) {
                    }
                }
            } else {
                try {
                    // we have a "$", so parse hex-string without leading "$"
                    dezaddress = Integer.parseInt(startaddress.substring(1), 16);
                } catch (NumberFormatException ex2) {
                }
            }
        }
        if (dezaddress != 0) {
            // if we have a valid decimal address, get assembler-specific basic-start 
            editorPanes.insertString(editorPanes.getActiveAssembler().getBasicStart(dezaddress));
        }
    }

    /**
     * Counts the amount of {@code sub} occurences in {@code str}.
     *
     * @param str the source string that should be examined.
     * @param sub the char which should be searched for and counted.
     * @return the count of how many times {@code sub} occurs in {@code str}, or
     * 0 if no match found or {@code str} is {@code null}.
     */
    public static int countMatches(final CharSequence str, final char sub) {
        if (null == str || str.length() == 0) {
            return 0;
        }
        int counter = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == sub) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * Handles the drop actions when user drags and drops files on the editor
     * component. Depending on the drop action, file names or file pathes are
     * included via compiler opcodes or directives. All files that should not be
     * included in the source, but opened in a new tab, will be returned as
     * {@code List<File>}.
     *
     * @param dtde the {@code DropTargetDropEvent}, fired by the
     * drop-event-handler.
     * @param editorPanes a reference to the EditorPanes class.
     * @return a {@code List} of type {@code File} with all files that should
     * not be included in the source, but opened in a new tab. Returns an empty
     * list if no files should be opened.
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
                if (c != null) {
                    // retrieve component's name
                    String name = c.getName();
                    // check for valid value
                    if (name != null && !name.isEmpty()) {
                        // check if files were dropped in entry field
                        // in this case, image files will we inserted into
                        // the entry, not attached as attachments
                        if (name.equalsIgnoreCase("jEditorPaneMain")) {
                            validDropLocation = true;
                        } else {
                            ConstantsR64.r64logger.log(Level.WARNING, "No valid drop location, drop rejected");
                            dtde.rejectDrop();
                        }
                    }
                }
                // retrieve list of dropped files
                java.util.List files = (java.util.List) tr.getTransferData(DataFlavor.javaFileListFlavor);
                // check for valid values
                if (files != null && files.size() > 0) {
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
                            if (dtde.getDropAction() == DnDConstants.ACTION_LINK && validDropLocation) {
                                linkedfiles.add(file);
                            } // if it's an asm, add it to asm file list
                            else if (FileTools.isSupportedFileType(file, FileTools.FILE_TYPE_ASM) && validDropLocation) {
                                // if so, add it to list
                                sourcefiles.add(file);
                            } // if it's an include file, add it to include file list
                            else if (FileTools.isSupportedFileType(file, FileTools.FILE_TYPE_INCLUDE) && validDropLocation) {
                                // if so, add it to list
                                includefiles.add(file);
                            }
                        }
                    }
                    // check if we have any valid values,
                    // i.e. any files have been dragged and dropped
                    // here we handle linked files, where only the file path should
                    // be added to the source
                    if (linkedfiles.size() > 0) {
                        for (File f : linkedfiles) {
                            String rf = FileTools.getRelativePath(editorPanes.getActiveFilePath(), f);
                            editorPanes.insertString("\"" + rf + "\"\n");
                        }
                    }
                    // check if we have any valid values,
                    // i.e. any files have been dragged and dropped
                    // if so, include files
                    if (includefiles.size() > 0) {
                        for (File f : includefiles) {
                            String insert;
                            // if user hold down ctrl-key, import bytes from file
                            if (dtde.getDropAction() == DnDConstants.ACTION_COPY) {
                                insert = Tools.getByteTableFromFile(f, editorPanes.getActiveAssembler());
                            } // else use include-directive
                            else {
                                // retrieve relative path of iimport file
                                String relpath = FileTools.getRelativePath(editorPanes.getActiveFilePath(), f);
                                switch (FileTools.getFileExtension(f).toLowerCase()) {
                                    case "txt":
                                        insert = editorPanes.getActiveAssembler().getIncludeTextDirective(relpath) + "\n";
                                        break;
                                    case "c64":
                                    case "prg":
                                        insert = editorPanes.getActiveAssembler().getIncludeC64Directive(relpath) + "\n";
                                        break;
                                    default:
                                        insert = editorPanes.getActiveAssembler().getIncludeBinaryDirective(relpath) + "\n";
                                }
                            }
                            editorPanes.insertString(insert);
                        }
                    }
                    // check if we have any valid values,
                    // i.e. any files have been dragged and dropped
                    // if so, include asm files via opcode / directive
                    if (sourcefiles.size() > 0) {
                        for (File f : sourcefiles) {
                            // if user hold down ctrl-key, use import-directive for asm-files
                            if (dtde.getDropAction() == DnDConstants.ACTION_COPY) {
                                String relpath = FileTools.getRelativePath(editorPanes.getActiveFilePath(), f);
                                String insert = editorPanes.getActiveAssembler().getIncludeSourceDirective(relpath) + "\n";
                                editorPanes.insertString(insert);
                            } else {
                                // else open files
                                openRemainingFiles.add(f);
                            }
                        }
                    }
                }
                dtde.getDropTargetContext().dropComplete(true);
            } else {
                ConstantsR64.r64logger.log(Level.WARNING, "DataFlavor.javaFileListFlavor is not supported, drop rejected");
                dtde.rejectDrop();
            }
        } catch (IOException | UnsupportedFlavorException ex) {
            ConstantsR64.r64logger.log(Level.WARNING, ex.getLocalizedMessage());
            dtde.rejectDrop();
        }
        // return remaining files that should be opened in a new tab
        return openRemainingFiles;
    }

    /**
     * Splits a command line string into separate tokens. This supports "
     * delimited tokens potentially containing a space. Within a quoted token, a
     * literal " or \ can be expressed by prefixing it with a backslash.
     * Escaping is not enabled outside of a quoted token, as this would break
     * existing user scripts for Windows users. Because of this, the only
     * possible way to express a literal " is to do so within a a quoted token.
     * Tokens not separated by whitespace are merged, i.e. f"oo b"ar will result
     * in a single token containing "foo bar".
     *
     * @param command A String containing a full command line.
     * @return An Array of String command tokens suitable for passing to a
     * ProcessBuilder.
     */
    public static String[] tokeniseCommandLine(String command) {
        final int NONE = 0;
        final int WITHIN_QUOTE = 1;

        int flags = NONE;
        StringBuilder token = null;
        ArrayList<String> tokens = new ArrayList<>();
        for (char character : command.toCharArray()) {
            // start of new token. "token" is null
            if (token == null) {
                // white space, assuming a new token, so skip here
                if (character == ' ' || character == '\t') {
                    continue;
                }
                // start of quoted token here
                if (character == '"') {
                    token = new StringBuilder();
                    flags = WITHIN_QUOTE;
                    continue;
                }
                // new token found, so init stringbuilder
                token = new StringBuilder();
                token.append(character);
                flags = NONE;
                continue;
            }
            // proceed token. check whether end of quoted
            // token was found
            if ((flags & WITHIN_QUOTE) != 0) {
                if (character == '"') {
                    // end of quoted section of token (possibly not end of token itself)
                    flags ^= WITHIN_QUOTE;
                    continue;
                }
            } else {
                if (character == '"') {
                    // beginning of quoted section of token
                    flags |= WITHIN_QUOTE;
                    continue;
                }
                // if we are not inside a quoted token, 
                // white space indicates end of currently
                // proceeded token
                if (character == ' ' || character == '\t') {
                    // end of token
                    tokens.add(token.toString());
                    token = null;
                    continue;
                }
            }

            token.append(character);
        }

        if (token != null) {
            if ((flags & WITHIN_QUOTE) != 0) {
                ConstantsR64.r64logger.log(Level.WARNING, "Unclosed quoted token: {0}", token.toString());
            }
            tokens.add(token.toString());
        }

        return tokens.toArray(new String[tokens.size()]);
    }

    /**
     * Splits a script string into separate lines. Lines ending in a backslash
     * character are joined with the following line.
     *
     * @param script A String containing command lines.
     * @return An Array of String command lines
     */
    public static String[] extractCommandLines(String script) {
        ArrayList<String> commandLines = new ArrayList<>();
        // match line ending with whitespace followed by backslash
        Pattern continuationPattern = Pattern.compile("\\s*(.*?)\\s+\\\\");
        String[] lines = script.split("\n");
        StringBuilder commandLine = new StringBuilder();

        for (String line : lines) {
            // check for empty line
            if (line.trim().isEmpty()) continue;
            Matcher joiningMatcher = continuationPattern.matcher(line);

            if (joiningMatcher.matches()) {
                commandLine.append(joiningMatcher.group(1));
                commandLine.append(' ');
            } else {
                commandLine.append(line.trim());

                if (commandLine.length() > 0) {
                    commandLines.add(commandLine.toString());
                    commandLine = new StringBuilder();
                }
            }
        }

        if (commandLine.length() > 0) {
            commandLines.add(commandLine.toString());
        }

        return commandLines.toArray(new String[commandLines.size()]);
    }
}
