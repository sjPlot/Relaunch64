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

package de.relaunch64.popelganda.Editor;

import de.relaunch64.popelganda.util.ConstantsR64;
import javax.swing.JOptionPane;

/**
 *
 * @author Luedeke
 */
public class InsertBreakPoint {
    public static final String breakPointMacro = 
            "\n//----------------------------------------------------------\n" + 
            "// Breakpoint Macro\n" + "//----------------------------------------------------------\n" + 
            ".var _createDebugFiles = debug && cmdLineVars.get(\"afo\") == \"true\"\n" + 
            ".print \"File creation \" + [_createDebugFiles\n" + 
            "    ? \"enabled (creating breakpoint file)\"\n" + 
            "    : \"disabled (no breakpoint file created)\"]\n" + 
            ".var brkFile\n" + ".if(_createDebugFiles) {\n" + 
            "    .eval brkFile = createFile(\"breakpoints.txt\")\n" + 
            "    }\n" + ".macro break() {\n" + 
            ".if(_createDebugFiles) {\n" + 
            "    .eval brkFile.writeln(\"break \" + toHexString(*))\n" + 
            "    }\n" + 
            "}\n" + 
            "//------------------------------------------------------\n";
    
    public static void insertBreakPoint(EditorPanes editorPane) {
        // check for valid value
        if (null==editorPane) return;
        // check for KickAss
        if (editorPane.getActiveCompiler()!=ConstantsR64.COMPILER_KICKASSEMBLER) {
            JOptionPane.showMessageDialog(null, "Breakpoints are currently only supported under KickAssembler!");
            return;
        }
        boolean addMacro = false;
        // check if source already has a breakpoint macro
        if (!sourceHasBreakpointMacro(editorPane.getActiveSourceCode(), editorPane.getActiveCompiler())) {
            // ask if macro should be added
            int option = JOptionPane.showConfirmDialog(null, "The breakpoint function requires a macro to be added to the source.\nWithout this macro, breakpoints won't work.\n\nDo you want to add this macro now to the end of the source?", "Insert Breakpoint", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            // check if user cancelled
            if (option==JOptionPane.CANCEL_OPTION) return;
            // check if macro should be added
            if (option==JOptionPane.YES_OPTION) addMacro = true;
        }
        // insert breakpoint
        editorPane.insertBreakPoint();
        // add macro if necessary
        if (addMacro) {
            int endpos = editorPane.getActiveEditorPane().getDocument().getLength();
            editorPane.insertString(breakPointMacro, endpos);
        }
    }
    public static boolean sourceHasBreakpointMacro(String source, int compiler) {
        switch (compiler) {
            case ConstantsR64.COMPILER_KICKASSEMBLER:
                return (source!=null && source.contains(".macro break()"));
        }
        return false;
    }
    public static void removeBreakPoints(EditorPanes editorPane) {
        // check for valid value
        if (null==editorPane) return;
        // get current source
        String source = editorPane.getActiveSourceCode();
        // create replace string
        String bpm1 = ConstantsR64.STRING_BREAKPOINT_KICKASSEMBLER+"\n";
        String bpm2 = ConstantsR64.STRING_BREAKPOINT_KICKASSEMBLER+"\r\n";
        // remove all breakpoints
        source = source.replace(bpm1, "").replace(bpm2, "");
        // remove macro
        source = source.replace(breakPointMacro, "");
        // update source
        editorPane.setSourceCode(source);
    }
}
