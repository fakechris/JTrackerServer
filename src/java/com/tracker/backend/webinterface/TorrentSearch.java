/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.webinterface;

import com.tracker.entity.Torrent;
import java.util.Date; // TODO: remove test stuff
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
     */
    public static List<Torrent> getList(String searchString, boolean searchDescriptions, boolean includeDead) {
        List<Torrent> result;

        EntityManager em = emf.createEntityManager();

        // building the query
        Query q;
        StringBuilder query = new StringBuilder();

        query.append("SELECT t FROM Torrent t WHERE t.name LIKE ");
        // horrible SQL-injection waiting to happen? :D
        query.append("%" + searchString + "%");
        if(searchDescriptions) {
            query.append(" OR t.description LIKE ");
            query.append("%" + searchString + "%");
        }
        if(!includeDead) {
            query.append(" AND t.numSeeders > 0 OR t.numLeechers > 0");
        }

        q = em.createQuery(query.toString());
        result = q.getResultList();

        // TODO: remove test stuff
        {
            // add some torrents to test
            Date d = new Date();
            d.setTime(1226703180000L);
            Torrent t = new Torrent();
            // fill in an ID, since this is not from the DB
            t.setId(1L);
            t.setName("test-torrent1");
            t.setDescription("dafdasfdasfas");
            t.setAdded(d);
            result.add(t);

            d = new Date();
            t = new Torrent();
            t.setId(2L);
            d.setTime(1226603180000L);
            t.setName("test-torrent2");
            t.setDescription("fdsfgsfdsfs");
            t.setAdded(d);
            result.add(t);
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


        // TODO: remove test stuff
        {
            // add some torrents to test
            Date d = new Date();
            d.setTime(1226703180000L);
            Torrent t = new Torrent();
            // fill in an ID, since this is not from the DB
            t.setId(1L);
            t.setName("test-torrent1");
            t.setDescription("dafdasfdasfas");
            t.setAdded(d);
            result.add(t);

            d = new Date();
            t = new Torrent();
            t.setId(2L);
            d.setTime(1226603180000L);
            t.setName("test-torrent2");
            t.setDescription("fdsfgsfdsfs");
            t.setAdded(d);
            result.add(t);
        }

        return result;
    }
}
