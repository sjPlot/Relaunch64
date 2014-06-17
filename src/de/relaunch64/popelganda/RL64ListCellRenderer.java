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

import de.relaunch64.popelganda.database.Settings;
import de.relaunch64.popelganda.util.ConstantsR64;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.MatteBorder;

/**
 *
 * @author Luedeke
 */
public class RL64ListCellRenderer extends JLabel implements ListCellRenderer<RL64ListItem> {
    private final Settings settings;
    RL64ListCellRenderer(Settings settings) {
        setOpaque(true);
        this.settings = settings;
    }
    @Override
    public Component getListCellRendererComponent(JList<? extends RL64ListItem> list, RL64ListItem value, int index, boolean isSelected, boolean cellHasFocus) {
        // define background highlightcolor for headers
        Color headerHighlightColor = new Color(225,237,242); // new Color(176,196,222);
        // text of list item
        setText(value.getText());
        // icon of list item, may be null if no item needed
        setIcon(value.isHeader() ? ConstantsR64.r64listicon : value.getIcon());
        // headings use bold font, normal entries plain
        setFont(settings.getMainFont().deriveFont(value.isTitle()? Font.BOLD : Font.PLAIN));
        // normal entries have small padding on left
        int leftMargin = (null==value.getIcon()) ? ConstantsR64.r64listicon.getIconWidth()+getIconTextGap()+2 : ConstantsR64.r64listicon.getIconWidth()-value.getIcon().getIconWidth()+2;
        if (value.isTitle()) {
            setBorder(new MatteBorder(1, 1, settings.getMainFont().getSize()/2, 1, Color.white));
        }
        else {
            setBorder(value.isHeader() ? new MatteBorder(1, 2, 1, 0, headerHighlightColor)
                                       : new MatteBorder(1, leftMargin, 1, 0, Color.white));
        }
        
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else {
            setBackground(value.isHeader() ? headerHighlightColor : Color.WHITE);
            setForeground(value.isHeader() ? new Color(85,95,102) : Color.BLACK);
        }
        return this;
    }
}

