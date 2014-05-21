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

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import org.gjt.sp.jedit.textarea.StandaloneTextArea;
import org.gjt.sp.jedit.IPropertyManager;
import org.gjt.sp.util.IOUtilities;

public class RL64TextArea extends StandaloneTextArea {
    final static Properties props;
    static IPropertyManager propertyManager;

    static {
        props = new Properties();
        props.putAll(loadProperties("/de/relaunch64/popelganda/resources/jedit_keys.props"));
        props.putAll(loadProperties("/de/relaunch64/popelganda/resources/jedit.props"));
        propertyManager = new IPropertyManager() {
            public String getProperty(String name) {
                return props.getProperty(name);
            }
        };
    }

    public void setProperty(String name, String val) {
        props.setProperty(name, val);
    }

    public void propertiesFromFile(String fileName) {
        props.putAll(loadProperties("/de/relaunch64/popelganda/resources/" + fileName));
    }

    private static Properties loadProperties(String fileName) {
        Properties loadedProps = new Properties();
        InputStream in = StandaloneTextArea.class.getResourceAsStream(fileName);
        try {
            loadedProps.load(in);
        }
        catch (IOException e) {
            //TODO: log error message
            System.out.println("property load error");
        }
        finally {
            IOUtilities.closeQuietly(in);
        }
        return loadedProps;
    }

    public RL64TextArea() {
        super(propertyManager);
    }
}
