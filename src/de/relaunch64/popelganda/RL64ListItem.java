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

    private String text;
    private final ImageIcon icon;
    private final int linenumber;
    private final File file;
    private final boolean isHeader;

    public RL64ListItem(String text, ImageIcon icon, boolean isHeader, int linenumber, File file) {
        this.text = text;
        this.icon = icon;
        this.linenumber = linenumber;
        this.file = file;
        this.isHeader = isHeader;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
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
    @Override
    public String toString() {
        return text;
    }
}
