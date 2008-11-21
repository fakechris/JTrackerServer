/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.webinterface;

import com.tracker.backend.StringUtils;
import com.tracker.entity.Peer;
import com.tracker.entity.Torrent;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bo
 */
public class TorrentSearchTest {

    static Peer p;
    static Vector<Torrent> torrents;


    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");

    public TorrentSearchTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        // we need to add some torrents and peers to test this
        // in setUp to have the same environment before each test
        Torrent t;

        String infoHash;
        String peerId;
        byte[] rawInfoHash = new byte[20];
        byte[] rawPeerId = new byte[20];

        System.out.println("Setting up database");

        EntityManager em = emf.createEntityManager();
        Random r = new Random(Calendar.getInstance().getTimeInMillis());

        t = new Torrent();
        p = new Peer();

        // fix up a convincing info hash
        byte byteHash[] = new byte[1024];
        r.nextBytes(byteHash);
        // get SHA-1 sum of byteHash
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        md.update(byteHash);
        rawInfoHash = md.digest();

        infoHash = StringUtils.getHexString(rawInfoHash);

        t.setInfoHash(infoHash);

        t.setName((String)"This name contains the word 'Brilliant'");
        t.setTorrentSize((long)999999999);

        t.setAdded(Calendar.getInstance().getTime());
        t.setDescription((String)"This description contains the word 'fabulous'.");

        p.setBytesLeft(t.getTorrentSize());
        p.setIp(InetAddress.getByName("192.168.1.3"));

        Long port = (long) 63049;
        p.setPort(port);

        p.setLastActionTime(Calendar.getInstance().getTime());

        // set peer id and raw peer id
        peerId = new String();
        peerId = "lbdfgd1321-------001";
        for(int i = 0; i < peerId.length(); i++) {
            rawPeerId[i] = (byte) peerId.charAt(i);
        }
        p.setPeerId(peerId);

        p.setSeed(false);
        p.setTorrent(t);

        t.addLeecher(p);

        torrents.add(t);

        em.getTransaction().begin();
        em.persist(t);
        em.persist(p);
        em.getTransaction().commit();

        // I need at least two torrents to test this class
        t = new Torrent();
        r.nextBytes(byteHash);
        md.update(byteHash);
        rawInfoHash = md.digest();

        infoHash = StringUtils.getHexString(rawInfoHash);
        t.setInfoHash(infoHash);

        t.setName((String)"This name contains the word 'Awful");
        t.setDescription((String)"This description contains the word 'Horrible'");
        t.setTorrentSize(965485654L);
        t.setAdded(Calendar.getInstance().getTime());

        torrents.add(t);

        em.getTransaction().begin();
        em.persist(t);
        em.getTransaction().commit();

        em.close();
    }

    @After
    public void tearDown() {
        // removes all the peers and torrent after each test
        EntityManager em = emf.createEntityManager();

        System.out.println("Tearing down database");

        em.getTransaction().begin();
        em.clear();

        Query q = em.createQuery("SELECT p FROM Peer p");
        List l = q.getResultList();
        Iterator itr = l.iterator();
        while(itr.hasNext()) {
            Peer tmp = (Peer) itr.next();
            Torrent tTemp = tmp.getTorrent();

            if(tTemp != null)
                tTemp.removePeer(tmp);
            else
                System.out.println("woot?");
            em.remove(tmp);
        }

        q = em.createQuery("SELECT t FROM Torrent t");
        l = q.getResultList();
        itr = l.iterator();
        while(itr.hasNext()) {
            Torrent tmp = (Torrent) itr.next();
            em.remove(tmp);
        }

        em.getTransaction().commit();
        em.close();
    }

    /**
     * Test of getList method, of class TorrentSearch.
     */
    @Test
    public void testGetList_3args() {
        System.out.println("getList");
        String searchString = "";
        boolean searchDescriptions = false;
        boolean includeDead = false;
        List<Torrent> expResult = new Vector<Torrent>();
        List<Torrent> result = TorrentSearch.getList(searchString, searchDescriptions, includeDead);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getList method, of class TorrentSearch.
     */
    @Test
    public void testGetList() {
        System.out.println("getList");
        // this should give back a list of all torrents.
        List<Torrent> expResult = torrents;
        List<Torrent> result = TorrentSearch.getList();
        assertEquals(expResult, result);
    }

}