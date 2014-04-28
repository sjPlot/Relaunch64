/*
 * Relaunch64 - A Java Crossassembler for C64 machine language coding.
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
package de.relaunch64.popelganda.Editor;

import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * Sets up an own undo manager. The default undo-manager would consider each 
 * syntax-highlighting step as own undo-event. To prevent this, the custom 
 * undo-manager only receives text-input/changes as undoable events.
 *
 * @author Daniel Luedecke
 */
public class MyUndoManager extends UndoManager implements UndoableEditListener {
    /**
     * An undo manager to undo/redo input from the main text field
     */
    private final UndoManager undomanager = new UndoManager();
    private boolean registerUndoEvents = true;

    public MyUndoManager() {
        // keep the last 1000 actions for undoing
        undomanager.setLimit(1000);
    }
    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        if (registerUndoEvents) {
            if (e.getEdit() instanceof AbstractDocument.DefaultDocumentEvent) {
                AbstractDocument.DefaultDocumentEvent de = (AbstractDocument.DefaultDocumentEvent) e.getEdit();
                // this selects the style events, the others are INSERT or REMOVE
                if (de.getType() == DocumentEvent.EventType.CHANGE) {
                    // style events are caused by syntax highlighting
                    // these events should not be treated as undoable
                    // action (else each undo-command would "re-color" all
                    // tokens before undoing text changes).
                    e.getEdit().die();
                    return;
                }
            }
            undomanager.addEdit(e.getEdit());
        }
    }
    public void enableRegisterUndoEvents(boolean enable) {
        registerUndoEvents = enable;
    }
    @Override
    public void undo() {
        try {
            if (undomanager.canUndo()) {
                undomanager.undo();
            }
        } catch (CannotUndoException ex) {
        }
    }
    @Override
    public void redo() {
        try {
            if (undomanager.canRedo()) {
                undomanager.redo();
            }
        } catch (CannotRedoException ex) {
        }
    }
    @Override
    public boolean canUndo() {
        return undomanager.canUndo();
    }
    @Override
    public boolean canRedo() {
        return undomanager.canRedo();
    }
    @Override
    public void discardAllEdits() {
        // reset the undomanager, in case it stored any changes
        // from the text field initiation when editing new entries
        undomanager.discardAllEdits();
    }
}
