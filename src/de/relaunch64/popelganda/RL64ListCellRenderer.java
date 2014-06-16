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
        setText(value.getText());
        setIcon(value.getIcon());
        setFont(settings.getMainFont().deriveFont(value.isHeader() ? Font.BOLD : Font.PLAIN));
        setBorder(value.isHeader() ? null : new MatteBorder(0, 10, 0, 0, Color.white));
        
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else {
            setBackground(Color.WHITE);
            setForeground(value.isHeader() ? new Color(153,51,0) : Color.BLACK);
        }
        return this;
    }
}

