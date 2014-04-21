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
    
}
