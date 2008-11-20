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

/**
 *
 * @author bo
 */
public class JSONTorrentList implements TorrentList {

    Logger log = Logger.getLogger(JSONTorrentList.class.getName());

    public void printTorrentList(Map<String, String[]> requestMap, PrintWriter out) {
        try {
            // query result
            Vector<Torrent> result;
            Iterator itr;

            boolean searchDescriptions = false;
            boolean includeDead = false;

            StringBuilder jsonResponse = new StringBuilder();

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

                result = TorrentSearch.getList(searchString, searchDescriptions, includeDead);
            }
            // no search string given
            else {
                result = TorrentSearch.getList();
            }

            itr = result.iterator();

            {
                // TODO: replace with the JSON*-classes
                StringBuilder name = new StringBuilder();
                StringBuilder date = new StringBuilder();

                jsonResponse.append("{\n");
                name.append("\t\"name\": [\n");
                date.append("\t\"date\": [\n");

                char delim = ' ';
                while(itr.hasNext()) {
                    Torrent t = (Torrent) itr.next();
                    name.append(delim + "\n\t\t\"" + t.getName() + "\"");
                    date.append(delim + "\n\t\t\"" + t.getAdded().toString() + "\"");
                    delim = ',';
                }

                name.append("\n\t],\n");
                date.append("\n\t]\n");
                
                jsonResponse.append(name);
                jsonResponse.append(date);
                jsonResponse.append("}");
            }

            out.print(jsonResponse.toString());
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Exception caught when trying to form JSON", ex);
        }
    }

}
