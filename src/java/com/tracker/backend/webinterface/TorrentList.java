/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.webinterface;

import java.io.PrintWriter;
import java.util.Map;

/**
 * Provides an interface to the different torrentlist implementations (XML,
 * JSON, others?).
 * @author bo
 */
public interface TorrentList {
    /**
     * Prints the torrentlist to the specified output with the specified options
     * given in the request (search, include descriptions in search, etc). Used
     * for getting a standarised interface across different SoA-methods (XML and
     * JSON currently).
     * @param requestMap The requests to parse and extract searchstrings from.
     * The keys that are significant in this are <em>"searchField"</em>, which may
     * contain a string to search for, <em>"searchDescription"</em> which, if
     * the value equals "checked", searches through torrent descriptions instead
     * of just names, and finally <em>"includeDead"</em> which, if the value
     * equals "checked", includes "dead" torrents in the resultset (ie, torrents
     * which do not have any seeds or leechers.)
     * @param out The output writer to write the torrentlist to. If used in jsp-
     * files, the easiest way to get this i by doing a new PrintWriter(out),
     * since "out" in that context is a JspWriter.
     * @see XMLTorrentList
     * @see JSONTorrentList
     * @see JspWriter
     * @see PrintWriter
     */
    public abstract void printTorrentList(Map<String, String[]> requestMap, PrintWriter out);
}
