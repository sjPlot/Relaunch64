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

import de.relaunch64.popelganda.util.ConstantsR64;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;

/**
 *
 * @author Luedeke
 */
public class CheckForUpdates extends org.jdesktop.application.Task<Object, Void> {
    // indicates whether the zettelkasten has updates or not.
    boolean updateavailable = false;
    String updateBuildNr = "0";

    CheckForUpdates(org.jdesktop.application.Application app) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to ImportFileTask fields, here.
        super(app);
    }

    protected String accessUpdateFile(URL updatetext) {
        // input stream that will read the update text
        InputStream is;
        // stringbuilder that will contain the content of the update-file
        StringBuilder updateinfo = new StringBuilder("");
        try {
            // open update-file on server
            is = updatetext.openStream();
            // buffer for stream
            int buff = 0;
            // read update-file and copy content to string builder
            while (buff!=-1) {
                buff = is.read();
                if (buff!=-1) updateinfo.append((char)buff);
            }
        }
        catch (IOException e) {
            // tell about fail
            ConstantsR64.r64logger.log(Level.INFO,"No access to Relaunch64-Website. Automatic update-check failed.");
            updateavailable = false;
            return null;
        }
        return updateinfo.toString();
    }

    @Override
    protected Object doInBackground() throws IOException {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.
        updateBuildNr = accessUpdateFile(new URL(ConstantsR64.UPDATE_INFO_URI));
        // check for valid access
        if (null==updateBuildNr || updateBuildNr.isEmpty()) return null;
        // retrieve start-index of the build-number within the version-string.
        int substringindex = ConstantsR64.BUILD_VERSION.indexOf("(Build")+7;
        // only copy buildinfo into string, other information of version-info are not needed
        String curversion = ConstantsR64.BUILD_VERSION.substring(substringindex,substringindex+8);
        // check whether there's a newer version online
        updateavailable = (curversion.compareTo(updateBuildNr)<0);
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
            ConstantsR64.r64logger.log(Level.INFO,("A new version of Relaunch64 is available!"+System.getProperty("line.separator")+"Download from "+ConstantsR64.UPDATE_URI));
        }
        else {
            // log latest available build
            ConstantsR64.r64logger.log(Level.INFO, ("Latest available Relaunch64-build: "+updateBuildNr));
        }
    }
}
