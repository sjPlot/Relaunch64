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
import de.relaunch64.popelganda.database.Settings;
import java.io.File;
import java.util.Map;

/**
 *
 * @author Luedeke
 */
public class Tools {
    public static boolean isDelimiter(String character, String additionalChars) {
        String delimiterList = ",;{}()[]+-/<=>&|^~*#" + additionalChars;
        return Character.isWhitespace(character.charAt(0)) || delimiterList.contains(character);
    }
    /**
     * This method retrieves system information like the operating system and version, the architecture,
     * the used java runtime environment and the official vendor of the jre, and the java home directory.
     * @return a string with the above described system information
     */
    public static String getSystemInformation() {
        StringBuilder sysinfo = new StringBuilder("");
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
                case ConstantsR64.COMPILER_KICKASSEMBLER:
                    byteToken = ".byte";
                    break;
                case ConstantsR64.COMPILER_64TASS:
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
    public static boolean isToolInEnvironment(String toolName) {
        // get all environment vars
        Map<String, String> env = System.getenv();
        // check if tool is in path
        String path = env.get("Path");
        if (path!=null) {
            if (path.toLowerCase().contains(toolName.toLowerCase())) return true;
        }
        // check if tool is in path
        path = env.get("PATH");
        if (path!=null) {
            if (path.toLowerCase().contains(toolName.toLowerCase())) return true;
        }
        // finally, check if tool itself is a key
        for (String envName : env.keySet()) {
            if (envName.toLowerCase().contains(toolName.toLowerCase()) || toolName.toLowerCase().contains(envName.toLowerCase())) return true;
        }
        return false;
    }
    public static void convertTabsToSpace(Settings settings, EditorPanes ep, int index) {
        String source = ep.getSourceCode(index);
        source = source.replace("\t", settings.getTabChar());
        ep.setSourceCode(index, source);
    }
    public static void convertSpaceToTabs(Settings settings, EditorPanes ep, int index) {
        String source = ep.getSourceCode(index);
        source = source.replace(settings.getTabChar(), "\t");
        ep.setSourceCode(index, source);
    }
}
