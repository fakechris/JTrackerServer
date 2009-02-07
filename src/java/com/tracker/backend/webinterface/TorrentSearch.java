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

package com.tracker.backend.webinterface;

import com.tracker.backend.entity.Torrent;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 * Provides a simple way to search the torrents currently in the database.
 * Implements this as a static method, so it does not particularly require
 * instantiation.
 * @author bo
 */
public class TorrentSearch {
    // keep one copy of the entity manager factory
    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("QuashPU");
    static Logger log = Logger.getLogger(TorrentSearch.class.getName());

    /**
     * A convenience method for getting a list of torrents given a simple map of
     * the request as given by the HTTP-servlet methods.
     * @param requestMap the request to produce a torrentlist for. For a
     * complete explanation of the keys and values this may consist of, see
     * the documentation of TorrentList.printTorrentList().
     * @return a List of Torrents acquired from the requests given.
     * @throws Exception if the query cannot be created or if the execution
     * failed.
     */
    public static List<Torrent> getList(Map<String, String[]> requestMap)
    throws Exception {
            // query result
            List<Torrent> result;

            // number of results and the index of the first result
            // set some sane defaults here
            int numResults = 25;
            int firstResult = 0;

            boolean searchDescriptions = false;
            boolean includeDead = false;

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

                result = getList(searchString,
                        searchDescriptions, includeDead, firstResult, numResults);
            }
            // no search string given
            else {
                result = getList(firstResult, numResults);
            }

            return result;
    }

    /**
     * Gets a list of torrents from the database with the given searchstring
     * and instructions on how to search (include dead, search descriptions).
     * @param searchString The string to search for in the list of torrents in
     * the database. If searchDescriptions is false, this only searches the names
     * of torrents. If searchDescriptions is true, this also searches through
     * descriptions.
     * @param searchDescriptions Whether to search the descriptions of torrents
     * instead of just the names.
     * @param includeDead Whether to include results that does not have any
     * activity - in other words, torrents with 0 seeds and 0 leechers.
     * @param firstResult the index of the first result to return from the query.
     * @param numResults the maximum number of results to return from the query.
     * The resulting list may be smaller than this.
     * @return a List of torrents containing the result of the database query,
     * where the first torrent has the index given in firstResult, and the list
     * being no bigger than numResults.
     * @throws Exception if the query cannot be created or if the execution failed.
     */
    public static List<Torrent> getList(String searchString, 
            boolean searchDescriptions, boolean includeDead, int firstResult,
            int numResults) throws Exception {
        List<Torrent> result;

        EntityManager em = emf.createEntityManager();

        // make sure the searchstring does not contain the character ' ('_'
        // matches any single character so it should work out)
        searchString = searchString.replaceAll("'", "_");

        // make sure that we can match missing words, for example;
        // user searches for "van graaf" and should find "van der graaf generator"
        searchString = searchString.replaceAll(" ", "%");

        // building the query
        Query q;
        StringBuilder query = new StringBuilder();

        query.append("SELECT t FROM Torrent t WHERE t.torrentData.name LIKE ");
        query.append("'%" + searchString + "%'");
        if(searchDescriptions) {
            query.append(" OR t.torrentData.description LIKE ");
            query.append("'%" + searchString + "%'");
        }
        if(!includeDead) {
            query.append(" AND (t.numSeeders > 0 OR t.numLeechers > 0)");
        }
        // order by date, descending
        query.append(" ORDER BY t.torrentData.added DESC");

        try {
            q = em.createQuery(query.toString());
            q.setFirstResult(firstResult);
            q.setMaxResults(numResults);
            
            result = q.getResultList();
        }
        catch(Exception ex) {
            throw new Exception("Exception caught when trying to get result. " +
                    "Query = " + query.toString(), ex);
        }

        return result;
    }

    /**
     * Gets a simple list of torrents in the database, with pagination.
     * @param firstResult the index of the first result to return from the query.
     * @param numResults the maximum number of results to return from the query.
     * The number of results may be smaller than this.
     * @return a list of torrents in the database starting with the torrent with
     * the index of "firstResult" and being no bigger than "numResults".
     * @throws java.lang.Exception if the query failed.
     */
    public static List<Torrent> getList(int firstResult, int numResults) throws Exception {
        List<Torrent> result;

        EntityManager em = emf.createEntityManager();

        Query q = em.createQuery("SELECT t FROM Torrent t ORDER BY " +
                "t.torrentData.added DESC");

        q.setFirstResult(firstResult);
        q.setMaxResults(numResults);

        result = q.getResultList();

        return result;
    }

    /**
     * Gets a simple list of all torrents in the database.
     * @return a List of torrents containing all torrents currently in the
     * database.
     * @throws java.lang.Exception if the query fails.
     */
    public static List<Torrent> getList() throws Exception {
        List<Torrent> result;

        EntityManager em = emf.createEntityManager();

        Query q = em.createQuery("SELECT t FROM Torrent t ORDER BY " +
                "t.torrentData.added DESC");

        result = q.getResultList();

        return result;
    }
}
