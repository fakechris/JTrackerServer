/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.webinterface;

import java.io.PrintWriter;
import java.util.Map;

/**
 *
 * @author bo
 */
public interface TorrentList {
    public abstract void printTorrentList(Map<String, String[]> requestMap, PrintWriter out);
}
