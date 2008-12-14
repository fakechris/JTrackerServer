/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    public void printTorrentList(Map<String, String[]> requestMap, PrintWriter out) {
        try {
            // query result
            Vector<Torrent> result;
            Iterator itr;

            boolean searchDescriptions = false;
            boolean includeDead = false;

            // number of results and the index of the first result
            // set some sane defaults here
            int numResults = 25;
            int firstResult = 0;

            // our json writer
            JSONWriter json = new JSONWriter(out);

                        // do we have a request for a specific number of results?
            if(requestMap.containsKey((String)"numResults")) {
                try {
                    int requestedNumResults = Integer.parseInt(requestMap.get(
                            (String)"numResults")[0]);
                    // do we have a valid number?
                    if(requestedNumResults > 100 || requestedNumResults < 0) {
                        // ignore the setting and log
                        log.log(Level.INFO, "Requested number of results was" +
                                "invalid (requested number: " +
                                Integer.toString(requestedNumResults) + ")");
                    }
                    else {
                        // valid number in a valid range, honour it
                        numResults = requestedNumResults;
                    }
                }
                catch(NumberFormatException ex) {
                    // error parsing the number, log and ignore the requested
                    // number
                    log.log(Level.INFO, "Requested number of results is not a" +
                            "long?", ex);
                }
            }

            // do we have a request for the index of the first result for
            // pagination?
            if(requestMap.containsKey((String)"firstResult")) {
                try {
                    int requestedFirstResult = Integer.parseInt(requestMap.get(
                            (String)"firstResult")[0]);
                    // do we have a valid number?
                    if(requestedFirstResult < 0) {
                        // ignore the setting and log
                        log.log(Level.INFO, "Requested index of first result" +
                                "was invalid (requested number: " +
                                Integer.toString(requestedFirstResult) + ")");
                    }
                    else {
                        // valid number in a valid range, honour it
                        firstResult = requestedFirstResult;
                    }
                }
                catch(NumberFormatException ex) {
                    // error parsing the number, log and ignore the requested
                    // number
                    log.log(Level.INFO, "Requested index of first result is not a" +
                            "long?", ex);
                }
            }

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

                result = (Vector<Torrent>) TorrentSearch.getList(searchString,
                        searchDescriptions, includeDead, firstResult, numResults);
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
