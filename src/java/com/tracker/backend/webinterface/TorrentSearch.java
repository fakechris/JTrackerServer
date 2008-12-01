/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.webinterface;

import com.tracker.backend.entity.Torrent;
import java.util.List;
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
    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");

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
     * @return a List of torrents containing the result of the database query.
     * @throws Exception if the query cannot be created or if the execution failed.
     */
    public static List<Torrent> getList(String searchString, boolean searchDescriptions, boolean includeDead) throws Exception {
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

        query.append("SELECT t FROM Torrent t WHERE t.name LIKE ");
        query.append("'%" + searchString + "%'");
        if(searchDescriptions) {
            query.append(" OR t.description LIKE ");
            query.append("'%" + searchString + "%'");
        }
        if(!includeDead) {
            query.append(" AND (t.numSeeders > 0 OR t.numLeechers > 0)");
        }

        try {
            q = em.createQuery(query.toString());
            result = q.getResultList();
        }
        catch(Exception ex) {
            throw new Exception("Exception caught when trying to get result. " +
                    "Query = " + query.toString(), ex);
        }

        return result;
    }

    /**
     * Gets a simple list of all torrents in the database.
     * @return a List of torrents containing all torrents currently in the
     * database.
     */
    public static List<Torrent> getList() {
        List<Torrent> result;

        EntityManager em = emf.createEntityManager();

        Query q = em.createQuery("SELECT t FROM Torrent t");

        result = q.getResultList();

        return result;
    }
}
