/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend;

import com.tracker.entity.Peer;
import com.tracker.entity.Torrent;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Random;
import java.util.TreeMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
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
public class TrackerRequestParserTest {

    static Peer p;
    static Torrent t;

    public TrackerRequestParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        // we need to add some torrents and peers to test this

        System.out.println("Setting up database for test");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");
        EntityManager em = emf.createEntityManager();
        Random r = new Random(Calendar.getInstance().getTimeInMillis());

        t = new Torrent();
        p = new Peer();
        t.setAdded(Calendar.getInstance().getTime());
        t.setDescription((String)"testing testing");

        byte byteHash[] = new byte[20];
        r.nextBytes(byteHash);
        String infoHash = new String(byteHash, "utf-8");

        t.setInfoHash(infoHash);

        t.setName((String)"testing torrent");
        t.setTorrentSize((long)32321311);

        p.setBytesLeft(t.getTorrentSize());
        p.setIp(InetAddress.getByName("192.168.1.3"));

        Byte port[] = new Byte[2];
        port[0] = (byte)0x03;
        port[1] = (byte)0xff;
        p.setPort(port);

        p.setLastActionTime(Calendar.getInstance().getTime());

        String peerId = new String();
        peerId = "lbdfgd1321-------987";
        p.setPeerId(peerId);

        p.setSeed(false);
        p.setTorrent(t);

        t.addLeecher(p);

        System.out.println("Attempting to persist objects");

        em.getTransaction().begin();
        em.persist(t);
        em.persist(p);
        em.getTransaction().commit();
        em.close();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("Removing test objects from database");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.clear();
        p = em.find(Peer.class, p.getId());
        t = em.find(Torrent.class, t.getId());
        if(p != null) em.remove(p);
        if(t != null) em.remove(t);
        em.getTransaction().commit();
        em.close();
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of setRequestParams method, of class TrackerRequestParser.
     */
    @Test
    public void testSetRequestParams() {
        System.out.println("setRequestParams");
        TreeMap params = null;
        TrackerRequestParser instance = new TrackerRequestParser();
        instance.setRequestParams(params);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of getRequestParams method, of class TrackerRequestParser.
     */
    @Test
    public void testGetRequestParams() {
        System.out.println("getRequestParams");
        TrackerRequestParser instance = new TrackerRequestParser();
        TreeMap expResult = null;
        TreeMap result = instance.getRequestParams();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of setRemoteAddress method, of class TrackerRequestParser.
     */
    @Test
    public void testSetRemoteAddress() {
        System.out.println("setRemoteAddress");
        InetAddress address = null;
        TrackerRequestParser instance = new TrackerRequestParser();
        instance.setRemoteAddress(address);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of getRemoteAddress method, of class TrackerRequestParser.
     */
    @Test
    public void testGetRemoteAddress() {
        System.out.println("getRemoteAddress");
        TrackerRequestParser instance = new TrackerRequestParser();
        InetAddress expResult = null;
        InetAddress result = instance.getRemoteAddress();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of parseRequest method, of class TrackerRequestParser.
     */
    @Test
    public void testParseRequest() {
        System.out.println("parseRequest");
        TrackerRequestParser instance = new TrackerRequestParser();
        TreeMap expResult = null;
        TreeMap result = instance.parseRequest();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

}