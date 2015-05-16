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
package de.relaunch64.popelganda.assemblers;

import de.relaunch64.popelganda.assemblers.ErrorHandler.ErrorInfo;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import java.io.LineNumberReader;
import java.util.ArrayList;

/**
 *
 * @author Soci/Singular
 */
class Assembler_dummy implements Assembler
{
    @Override
    public String name() {
        return "Dummy";
    }

    @Override
    public String fileName() {
        return "";
    }

    @Override
    public int getID() {
        return 99;
    }

    @Override
    public String syntaxFile() {
        return "";
    }

    @Override
    public String getLineComment() {
        return "";
    }

    @Override
    public String getMacroPrefix() {
        return "";
    }

    @Override
    public String getByteDirective() {
        return "";
    }

    @Override
    public String getBasicStart(int start) {
        return "";
    }

    @Override
    public String getIncludeSourceDirective(String path) {
        return "";
    }

    @Override
    public String getIncludeTextDirective(String path) {
        return "";
    }

    @Override
    public String getIncludeC64Directive(String path) {
        return "";
    }

    @Override
    public String getIncludeBinaryDirective(String path) {
        return "";
    }

    @Override
    public String[] getScriptKeywords() {
        return new String[] {};
    }

    @Override
    public String getDefaultCommandLine(String fp) {
        return "";
    }

    @Override
    public String getHelpCLI() {
        return "";
    }

    @Override
    public labelList getLabels(LineNumberReader lineReader, int lineNumber) {
        return new labelList(null, null, null);
    }

    @Override
    public String labelGetStart(String line, int pos) {
        return "";
    }

    @Override
    public ArrayList<ErrorInfo> readErrorLines(LineNumberReader lineReader, boolean ignore_warnings) {
        final ArrayList<ErrorInfo> errors = new ArrayList<>();
        return errors;
    }

    @Override
    public int getFoldLevel(JEditBuffer buffer, int lineIndex, int foldtokens) {
        return buffer.getFoldLevel(lineIndex);
    }
}
