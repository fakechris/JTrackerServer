/*
 * $Id$
 *
 * Copyright © 2008,2009 Bjørn Øivind Bjørnsen
 *
 * This file is part of Quash.
 *
 * Quash is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quash is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quash. If not, see <http://www.gnu.org/licenses/>.
 */

package com.tracker.backend.webinterface.json;

import com.tracker.backend.webinterface.TorrentList;
import com.tracker.backend.webinterface.TorrentSearch;
import com.tracker.backend.entity.Torrent;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONWriter;

/**
 * Implements a searchable torrentlist with the result being given in the JSON
 * format for use in different frontends or applications.
 *
 * <p>The format for the JSON stream is like this (liable to change):</p>
 * <code>
 * [
 *   { "torrent" : {
 *      "id" : id
 *      "name" : name
 *      "numSeeders" : numSeeders
 *      "numLeechers" : numLeechers
 *      "numCompleted" : numCompleted
 *      "dateAdded" : dateAdded
 *     }
 *   },
 *   { "torrent" : {
 *      "id" : id
 *      .....
 *     }
 *   }
 * ]
 * </code>
 * and so on.
 * @author bo
 */
public class JSONTorrentList implements TorrentList {

    Logger log = Logger.getLogger(JSONTorrentList.class.getName());

    /**
     * Implements printTorrentList from com.tracker.backend.webinterface.TorrentList
     * @see com.tracker.backend.webinterface.TorrentList
     */
    @Override
    public void printTorrentList(Map<String, String[]> requestMap, PrintWriter out) {
        try {
            // query result
            Vector<Torrent> result = (Vector<Torrent>) TorrentSearch.getList(requestMap);
            Iterator itr = result.iterator();

            // our json writer
            JSONWriter json = new JSONWriter(out);

            // torrents array
            json.array();

            while(itr.hasNext()) {
                Torrent t = (Torrent) itr.next();

                // torrent object
                json.object().key("torrent");

                json.object();
                json.key("id").value(t.getId());
                json.key("name").value(t.getTorrentData().getName());
                json.key("numSeeders").value(t.getNumSeeders());
                json.key("numLeechers").value(t.getNumLeechers());
                json.key("numCompleted").value(t.getNumCompleted());
                json.key("dateAdded").value(t.getTorrentData().getAdded().toString());
                json.endObject();

                // end torrent object
                json.endObject();
            }
            // end torrents array
            json.endArray();

        } catch (Exception ex) {
            log.log(Level.SEVERE, "Exception caught when trying to form JSON", ex);
        }
    }
}
