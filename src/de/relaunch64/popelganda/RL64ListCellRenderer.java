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
    /**
     * Creates a specific list cell renderer for the Goto-sidebar.
     * 
     * @param settings a reference to the {@link de.relaunch64.popelganda.database.Settings Settings}-class.
     */
    RL64ListCellRenderer(Settings settings) {
        setOpaque(true);
        this.settings = settings;
    }
    /**
     * Overriden method of ListCellRendererComponent. Creates a special rendered label that 
     * will be used for list items.
     * 
     * @param list the new rendered JList
     * @param value a reference to the {@link RL64ListItem RL64ListItem} class with more information
     * about the list item that should be rendered (e.g. is it a header item, which icon etc.)
     * @param index not used (internally used only)
     * @param isSelected (internally used only, if item is selected)
     * @param cellHasFocus (internally used only, if item has focus)
     * @return a rendered JLabel which will be used for the JList.
     */
    @Override
    public Component getListCellRendererComponent(JList<? extends RL64ListItem> list, RL64ListItem value, int index, boolean isSelected, boolean cellHasFocus) {
        // get font height. we determin border (margins from header items to normal items)
        // proportional to font size
        int fontHeight = settings.getMainFont().getSize();
        // text of list item
        setText(value.getText());
        // icon of list item, may be null if no item needed
        setIcon(value.isHeader() ? ConstantsR64.r64listicon : value.getIcon());
        // headings use bold font, normal entries plain
        setFont(settings.getMainFont().deriveFont(value.isTitle()? Font.BOLD : Font.PLAIN));
        // normal entries have small padding on left, depending on whether they have an icon or not
        int leftMargin = (null==value.getIcon()) ? ConstantsR64.r64listicon.getIconWidth()+getIconTextGap()+2 : ConstantsR64.r64listicon.getIconWidth()-value.getIcon().getIconWidth()+2;
        // set border to item. we use a matte border with background color, so border remains unvisible and
        // is only used for item-margins.
        setBorder(value.isHeader() ? new MatteBorder(fontHeight, 2, 1, 0, ConstantsR64.OSX_BG_STYLE)
                                   : value.isTitle() ? new MatteBorder(1, 1, 1, 1, ConstantsR64.OSX_BG_STYLE)
                                                     : new MatteBorder(1, leftMargin, 1, 0, ConstantsR64.OSX_BG_STYLE));
        
        if (isSelected) {
            setBackground(ConstantsR64.OSX_HIGHLIGHT_STYLE);
            setForeground(Color.WHITE);
        }
        else {
            setBackground(ConstantsR64.OSX_BG_STYLE);
            setForeground(value.isHeader() ? new Color(113,126,140) : Color.BLACK);
        }
        return this;
    }
}

