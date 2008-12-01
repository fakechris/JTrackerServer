/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.webinterface.json;

import com.tracker.backend.StringUtils;
import com.tracker.backend.entity.Peer;
import com.tracker.backend.entity.Torrent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
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
public class JSONTorrentListTest {

    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");

    public JSONTorrentListTest() {
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
        Peer p;

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

        t.setName((String)"This name contains the word 'Awful'");
        t.setDescription((String)"This description contains the word 'Horrible'");
        t.setTorrentSize(965485654L);
        t.setAdded(Calendar.getInstance().getTime());

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
     * Test of printTorrentList method, of class JSONTorrentList.
     */
    @Test
    public void testPrintTorrentList() {
        System.out.println("printTorrentList");
        Map<String, String[]> requestMap = new TreeMap<String, String[]>();
        StringWriter stringResult = new StringWriter();
        PrintWriter out = new PrintWriter(stringResult);

        // the output is ordered by id's
        Torrent first, second;

        EntityManager em = emf.createEntityManager();
        Query q = em.createQuery("SELECT t FROM Torrent t");
        List<Torrent> l = (List<Torrent>) q.getResultList();

        first = l.get(0);
        second = l.get(1);

        String expResult = "[{\"torrent\":" +
                "{\"id\":" + first.getId().toString() +
                ",\"name\":\"" + first.getName() + "\"," +
                "\"numSeeders\":" + first.getNumSeeders().toString() + "," +
                "\"numLeechers\":" + first.getNumLeechers().toString() + "," +
                "\"numCompleted\":" + first.getNumCompleted().toString() + "," +
                "\"dateAdded\":\"" + first.getAdded().toString() + "\"}}," +
                "{\"torrent\":" +
                "{\"id\":" + second.getId().toString() + "," +
                "\"name\":\"" + second.getName() + "\"," +
                "\"numSeeders\":" + second.getNumSeeders().toString() + "," +
                "\"numLeechers\":" + second.getNumLeechers().toString() + "," +
                "\"numCompleted\":" + second.getNumCompleted().toString() + "," +
                "\"dateAdded\":\"" + second.getAdded().toString() + "\"}}]";

        try {
            JSONTorrentList instance = new JSONTorrentList();
            instance.printTorrentList(requestMap, out);

            assertEquals(expResult, stringResult.toString());
        }
        catch(Exception ex) {
            fail(ex.getMessage());
        }
    }

}