/*
 * Copyright (C) 2014 Soci/Singular
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

import org.gjt.sp.jedit.buffer.FoldHandler;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import javax.swing.text.Segment;

public class RL64FoldHandler extends FoldHandler {

    private final RL64TextArea textArea;

    public RL64FoldHandler(RL64TextArea textArea) {
        super("RL64assembler");
        this.textArea = textArea;
    }

    @Override
    public int getFoldLevel(JEditBuffer buffer, int lineIndex, Segment seg) {
        if (lineIndex == 0) {
            return 1;
        }
        return Math.max(0, textArea.getAssembler().getFoldLevel(buffer, lineIndex - 1, textArea.getSettings().getCodeFoldingTokens()));
    }
}
