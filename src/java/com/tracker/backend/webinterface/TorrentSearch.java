/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.webinterface;

import com.tracker.entity.Torrent;
import java.util.Date; // TODO: remove test stuff
import java.util.Vector;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author bo
 */
public class TorrentSearch {
    // Persistence stuff
    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");

    public static Vector<Torrent> getList(String searchString, boolean searchDescriptions, boolean includeDead) {
        Vector<Torrent> result;

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
        result = (Vector<Torrent>) q.getResultList();

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

    public static Vector<Torrent> getList() {
        Vector<Torrent> result;

        EntityManager em = emf.createEntityManager();

        Query q = em.createQuery("SELECT t FROM Torrent t");

        result = (Vector<Torrent>) q.getResultList();


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
