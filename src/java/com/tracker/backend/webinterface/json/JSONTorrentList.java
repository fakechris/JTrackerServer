/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.webinterface.json;

import com.tracker.backend.webinterface.TorrentList;
import com.tracker.backend.webinterface.TorrentSearch;
import com.tracker.entity.Torrent;
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
    public void printTorrentList(Map<String, String[]> requestMap, PrintWriter out) {
        try {
            // query result
            Vector<Torrent> result;
            Iterator itr;

            boolean searchDescriptions = false;
            boolean includeDead = false;

            // our json writer
            JSONWriter json = new JSONWriter(out);

            // do we search for anything?
            if(requestMap.containsKey((String)"searchField")) {
                // search for the search string given
                // (there is only one object of this anyway)
                String searchString = requestMap.get((String)"searchField")[0];

                // do we search descriptions?
                if(requestMap.containsKey((String)"searchDescriptions")) {
                    String value = requestMap.get((String)"searchDescriptions")[0];
                    if(value.equalsIgnoreCase("checked")) {
                        searchDescriptions = true;
                    }
                }

                // do we search dead torrents?
                if(requestMap.containsKey((String)"includeDead")) {
                    String value = requestMap.get((String)"includeDead")[0];
                    if(value.equalsIgnoreCase("checked")) {
                        includeDead = true;
                    }
                }

                result = (Vector<Torrent>) TorrentSearch.getList(searchString, searchDescriptions, includeDead);
            }
            // no search string given
            else {
                result = (Vector<Torrent>) TorrentSearch.getList();
            }

            itr = result.iterator();

            // torrents array
            json.array();

            while(itr.hasNext()) {
                Torrent t = (Torrent) itr.next();

                // torrent object
                json.object().key("torrent");

                json.object();
                json.key("id").value(t.getId());
                json.key("name").value(t.getName());
                json.key("numSeeders").value(t.getNumSeeders());
                json.key("numLeechers").value(t.getNumLeechers());
                json.key("numCompleted").value(t.getNumCompleted());
                json.key("dateAdded").value(t.getAdded().toString());
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
