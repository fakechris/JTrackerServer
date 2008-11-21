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
     * prints the torrentlist to the specified output with the specified options
     * given in the request (search, include descriptions in search, etc). Used
     * for getting a standarised interface across different SoA-methods (XML and
     * JSON currently).
     * @param requestMap the requests to parse and extract searchstrings from
     * @param out the output writer to write the torrentlist to
     */
    public abstract void printTorrentList(Map<String, String[]> requestMap, PrintWriter out);
}
