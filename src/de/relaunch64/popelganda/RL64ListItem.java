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

package de.relaunch64.popelganda;

import java.io.File;
import javax.swing.ImageIcon;

/**
 *
 * @author Luedeke
 */
public class RL64ListItem {

    private final String text;
    private final ImageIcon icon;
    private final int linenumber;
    private final File file;
    private final boolean isHeader;
    private final boolean isTitle;
    /**
     * User data for a list item of the Goto-sidebar.
     * 
     * @param text the item-text which will be displayed in the list
     * @param icon an icon which is displayed on the left side of the item. optional, may be {@code null}.
     * @param isHeader {@ode true} if item is a header (i.e. a new file name is indicating another editor pane in the list)
     * @param isTitle {@code true} if item is the sidebar's title. should only apply to the very first item, which is used as "title item".
     * @param linenumber the line number of the token (label, macro, section...). currently not used.
     * @param file the file path of the file where the item belongs to. needed to switch between tabs on selecting new list items.
     */
    public RL64ListItem(String text, ImageIcon icon, boolean isHeader, boolean isTitle, int linenumber, File file) {
        this.text = text;
        this.icon = icon;
        this.linenumber = linenumber;
        this.file = file;
        this.isHeader = isHeader;
        this.isTitle = isTitle;
    }

    public String getText() {
        return text;
    }
    public ImageIcon getIcon() {
        return icon;
    }
    public int getLineNumber() {
        return linenumber;
    }
    public File getFile() {
        return file;
    }
    public boolean isHeader() {
        return isHeader;
    }
    public boolean isTitle() {
        return isTitle;
    }
    @Override
    public String toString() {
        return text;
    }
}
