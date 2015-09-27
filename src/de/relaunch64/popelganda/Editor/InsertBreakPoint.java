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
package de.relaunch64.popelganda.Editor;

import de.relaunch64.popelganda.util.ConstantsR64;
import de.relaunch64.popelganda.assemblers.Assembler;
import de.relaunch64.popelganda.assemblers.Assemblers;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author Daniel Lüdecke
 */
public class InsertBreakPoint {

    public static final String breakPointMacro
            = "\n//----------------------------------------------------------\n"
            + "// Breakpoint Macro\n"
            + "//----------------------------------------------------------\n"
            + ".var _createDebugFiles = debug && cmdLineVars.get(\"afo\") == \"true\"\n"
            + ".print \"File creation \" + [_createDebugFiles\n"
            + "    ? \"enabled (creating breakpoint file)\"\n"
            + "    : \"disabled (no breakpoint file created)\"]\n"
            + ".var brkFile\n"
            + ".if(_createDebugFiles) {\n"
            + "    .eval brkFile = createFile(\"breakpoints.txt\")\n"
            + "    }\n"
            + ".macro break() {\n"
            + ".if(_createDebugFiles) {\n"
            + "    .eval brkFile.writeln(\"break \" + toHexString(*))\n"
            + "    }\n"
            + "}\n"
            + "//------------------------------------------------------\n";

    public static void insertBreakPoint(EditorPanes editorPane) {
        // check for valid value
        if (null == editorPane) {
            return;
        }
        // check for KickAss
        if (editorPane.getActiveAssembler() != Assemblers.ASM_KICKASSEMBLER) {
            JOptionPane.showMessageDialog(null, "Breakpoints are currently only supported under KickAssembler!");
            return;
        }
        boolean addMacro = false;
        // check if source already has a breakpoint macro
        if (!sourceHasBreakpointMacro(editorPane.getActiveSourceCode(), editorPane.getActiveAssembler())) {
            // ask if macro should be added
            int option = JOptionPane.showConfirmDialog(null, "The breakpoint function requires a macro to be added to the source.\nWithout this macro, breakpoints won't work.\n\nDo you want to add this macro now to the end of the source?", "Insert Breakpoint", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            // check if user cancelled
            if (option == JOptionPane.CANCEL_OPTION) {
                return;
            }
            // check if macro should be added
            if (option == JOptionPane.YES_OPTION) {
                addMacro = true;
            }
        }
        // insert breakpoint
        editorPane.insertBreakPoint();
        // add macro if necessary
        if (addMacro) {
            RL64TextArea ep = editorPane.getActiveEditorPane();
            int endpos = ep.getLineEndOffset(ep.getLastPhysicalLine());
            editorPane.insertString(breakPointMacro, endpos);
        }
    }

    public static boolean sourceHasBreakpointMacro(String source, Assembler assembler) {
        if (assembler == Assemblers.ASM_KICKASSEMBLER) {
            return (source != null && source.contains(".macro break()"));
        }
        return false;
    }

    public static void removeBreakPoints(EditorPanes editorPane) {
        // check for valid value
        if (null == editorPane) {
            return;
        }
        // get current source
        String source = editorPane.getActiveSourceCode();
        // create replace string
        String bpm1 = ConstantsR64.STRING_BREAKPOINT_KICKASSEMBLER;
        // remove macro
        source = source.replaceAll(Pattern.quote(breakPointMacro), "");
        // remove all breakpoints
        source = source.replace(bpm1, "");
        // update source
        editorPane.setSourceCode(source);
    }
}
