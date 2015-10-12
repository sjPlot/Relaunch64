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
package de.relaunch64.popelganda;

import de.relaunch64.popelganda.util.ConstantsR64;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;

/**
 *
 * @author Daniel Lüdecke
 */
public class CheckForUpdates extends org.jdesktop.application.Task<Object, Void> {

    // indicates whether the zettelkasten has updates or not.

    boolean updateavailable = false;
    String updateBuildNr = null;

    CheckForUpdates(org.jdesktop.application.Application app) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to ImportFileTask fields, here.
        super(app);
    }

    /**
     * Opens and reads the file {@code updatetext} to get information about the
     * latest releases.
     *
     * @param updatetext an URL to the (text-)file containg the update
     * information. See ConstantsR64.UPDATE_INFO_URI.
     *
     * @return the content of the (text-)file
     */
    protected String accessUpdateFile(URL updatetext) {
        // input stream that will read the update text
        InputStream is = null;
        // stringbuilder that will contain the content of the update-file
        StringBuilder updateinfo = new StringBuilder("");
        try {
            // open update-file on server
            is = updatetext.openStream();
            // buffer for stream
            int buff = 0;
            // read update-file and copy content to string builder
            while (buff != -1) {
                buff = is.read();
                if (buff != -1) {
                    updateinfo.append((char) buff);
                }
            }
        } catch (IOException e) {
            // tell about fail
            ConstantsR64.r64logger.log(Level.INFO, "No access to Relaunch64-Website. Automatic update-check failed.");
            updateavailable = false;
            return null;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            }
        }
        return updateinfo.toString()
                .replace("\n", "")
                .replace("\r", "")
                .trim();
    }

    /**
     * This thread accesses the update information file and checks whether new
     * releases are available. f yes, an information will be shown in the log
     * window.
     *
     * @return always {@code null}.
     * @throws IOException
     */
    @Override
    protected Object doInBackground() throws IOException {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.
        updateBuildNr = accessUpdateFile(new URL(ConstantsR64.UPDATE_INFO_URI));
        // check for valid access
        if (null == updateBuildNr || updateBuildNr.isEmpty()) {
            return null;
        }
        // check whether there's a newer version online
        updateavailable = (ConstantsR64.BUILD_NUMBER.compareTo(updateBuildNr) < 0);
        return null;  // return your result
    }

    @Override
    protected void succeeded(Object result) {
        // Runs on the EDT.  Update the GUI based on
        // the result computed by doInBackground().
    }

    @Override
    protected void finished() {
        if (updateavailable) {
            //log info
            ConstantsR64.r64logger.log(Level.INFO, ("A new version of Relaunch64 is available!" + System.lineSeparator() + "Download from " + ConstantsR64.UPDATE_URI));
        } else {
            // log latest available build
            if (updateBuildNr != null) {
                ConstantsR64.r64logger.log(Level.INFO, ("Latest stable Relaunch64-build: " + updateBuildNr));
            }
        }
    }
}
