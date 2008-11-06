/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend;

import com.tracker.entity.Peer;
import com.tracker.entity.Torrent;

import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
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
public class TrackerRequestParserTest {

    static Peer p;
    static Torrent t;

    static String infoHash;
    static String peerId;

    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");


    public TrackerRequestParserTest() {
    }

    public TreeMap peerRequest(String peerId, String infoHash, String event) throws Exception
    {
        TreeMap requestParams = new TreeMap();

        try {
            // info hash must be url-encoded
            requestParams.put((String) "info_hash", (String) URLEncoder.encode(infoHash, "utf-8"));
            requestParams.put((String)"peer_id", peerId);
            requestParams.put((String)"port", (String)"1234");
            requestParams.put((String)"uploaded", (String)"0");
            requestParams.put((String)"downloaded", (String)"0");
            requestParams.put((String)"left", (String)"0");
            requestParams.put((String)"event", event);
        } catch (Exception ex) {
            throw ex;
        }

        return requestParams;
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws Exception {
        // we need to add some torrents and peers to test this
        // in setUp to have the same environment before each test

        EntityManager em = emf.createEntityManager();
        Random r = new Random(Calendar.getInstance().getTimeInMillis());

        t = new Torrent();
        p = new Peer();
        t.setAdded(Calendar.getInstance().getTime());
        t.setDescription((String)"testing testing");

        byte byteHash[] = new byte[20];
        r.nextBytes(byteHash);
        infoHash = new String(byteHash, "utf-8");

        t.setInfoHash(infoHash);

        t.setName((String)"testing torrent");
        t.setTorrentSize((long)999999999);

        p.setBytesLeft(t.getTorrentSize());
        p.setIp(InetAddress.getByName("192.168.1.3"));

        Long port = (long) 63049;
        p.setPort(port);

        p.setLastActionTime(Calendar.getInstance().getTime());

        peerId = new String();
        peerId = "lbdfgd1321-------001";
        p.setPeerId(peerId);

        p.setSeed(false);
        p.setTorrent(t);

        t.addLeecher(p);

        em.getTransaction().begin();
        em.persist(t);
        em.persist(p);
        em.getTransaction().commit();
        em.close();
    }

    @After
    public void tearDown() {
        // removes all the peers and torrent after each test
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.clear();

        Query q = em.createQuery("SELECT p FROM Peer p");
        List l = q.getResultList();
        Iterator itr = l.iterator();
        while(itr.hasNext()) {
            Peer tmp = (Peer) itr.next();
            Torrent tTemp = tmp.getTorrent();
            /*if(tmp.isSeed())
                tTemp.removeSeed(tmp);
            else
                tTemp.removeLeecher(tmp);*/
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
     * Test of {set,get}RequestParams method, of class TrackerRequestParser.
     */
    @Test
    public void testRequestParams() {
        System.out.println("setRequestParams");
        TrackerRequestParser instance = new TrackerRequestParser();
        try {
            TreeMap params = peerRequest(peerId, infoHash, "");
            instance.setRequestParams(params);
            assertEquals(params, instance.getRequestParams());
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            fail("Exception caught");
        }
    }

    /**
     * Test of {set,get}RemoteAddress method, of class TrackerRequestParser.
     */
    @Test
    public void testRemoteAddress() {
        try {
            System.out.println("setRemoteAddress");
            InetAddress address = InetAddress.getByName("192.168.1.1");
            TrackerRequestParser instance = new TrackerRequestParser();
            instance.setRemoteAddress(address);
            assertEquals(address, instance.getRemoteAddress());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            fail("Exception Caught");
        }
    }

    /**
     * Test of parseRequest method with event=started, of class TrackerRequestParser.
     */
    @Test
    public void testParseRequestEventStarted() {
        try {
            System.out.println("parseRequestEventStarted");
            TrackerRequestParser instance = new TrackerRequestParser();
            // populate request and address
            TreeMap request;
            InetAddress address;
            TreeMap expResult = new TreeMap();
            String newPeerId = new String();

            /**
             * For event = started
             */
            System.out.println(" -- populating address and request");
            address = InetAddress.getByName("192.168.1.4");
            newPeerId = "lbdfgd1321-------002";

            request = peerRequest(newPeerId, infoHash, "started");
            request.put((String)"left", (String)"999999999");

            instance.setRemoteAddress(address);
            instance.setRequestParams(request);

            System.out.println(" -- populating expected result");
            expResult.put((String)"complete",(String)"0");
            // the new peer and the default peer
            expResult.put((String)"incomplete",(String)"2");
            expResult.put((String)"interval",(String)"1800");
            expResult.put((String)"min interval", (String)"180");
            // grabbed from output
            expResult.put((String)"peers", 
                    URLDecoder.decode("%28%01%03%00%00", "utf-8"));

            System.out.println(" -- parsing request");
            TreeMap result = instance.parseRequest();

            assertEquals(expResult, result);
            
            // also look for the new peer in the DB
            System.out.println(" -- checking for new peer in DB");
            EntityManager em = emf.createEntityManager();
            Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
            q.setParameter("peerId", newPeerId);
            List<Peer> tmp = q.getResultList();
            assertEquals(tmp.size(), 1);
            
        } catch (Exception ex) {
            String failMessage = "Exception Caught: ";
            failMessage += ex.toString();
            failMessage += " ";
            failMessage += ex.getMessage();
            fail(failMessage);
        }
    }

    /**
     * Test of parseRequest method with event=stopped, of class TrackerRequestParser.
     */
    @Test
    public void testParseRequestEventStopped() {
        try {
            System.out.println("parseRequestEventStopped");
            TrackerRequestParser instance = new TrackerRequestParser();
            // populate request and address
            TreeMap request;
            InetAddress address;
            TreeMap expResult = new TreeMap();
            String newPeerId = new String();

            /**
             * For event = stopped
             */
            System.out.println(" -- populating address and request");
            address = p.getIp();
            newPeerId = p.getPeerId();

            request = peerRequest(newPeerId, infoHash, "stopped");
            request.put((String)"left", (String)"999999999");

            instance.setRemoteAddress(address);
            instance.setRequestParams(request);

            System.out.println(" -- populating expected result");
            expResult.put((String)"complete",(String)"0");
            // no peers left after this one was removed
            expResult.put((String)"incomplete",(String)"0");
            expResult.put((String)"interval",(String)"1800");
            expResult.put((String)"min interval", (String)"180");
            expResult.put((String)"peers","");

            System.out.println(" -- parsing request");
            TreeMap result = instance.parseRequest();

            assertEquals(expResult, result);

            // check if the peer is gone from the DB
            System.out.println(" -- checking if the peer is gone from the DB");
            EntityManager em = emf.createEntityManager();
            Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
            q.setParameter("peerId", newPeerId);
            List<Peer> tmp = q.getResultList();
            assertEquals(tmp.size(), 0);

        } catch (Exception ex) {
            String failMessage = "Exception Caught: ";
            failMessage += ex.toString();
            failMessage += " ";
            failMessage += ex.getMessage();
            ex.printStackTrace();
            fail(failMessage);
        }
    }

    /**
     * Test of parseRequest method with event=completed, of class TrackerRequestParser.
     */
    @Test
    public void testParseRequestEventCompleted() {
        try {
            System.out.println("parseRequestEventCompleted");
            TrackerRequestParser instance = new TrackerRequestParser();
            // populate request and address
            TreeMap request;
            InetAddress address;
            TreeMap expResult = new TreeMap();
            String newPeerId = new String();

            /**
             * For event = completed
             */
            System.out.println(" -- populating address and request");
            address = p.getIp();
            newPeerId = p.getPeerId();

            request = peerRequest(newPeerId, infoHash, "completed");
            request.put((String)"left", (String)"999999999");

            instance.setRemoteAddress(address);
            instance.setRequestParams(request);

            System.out.println(" -- populating expected result");
            expResult.put((String)"complete",(String)"1");
            // no leechers left after this one turned into a seed
            expResult.put((String)"incomplete",(String)"0");
            expResult.put((String)"interval",(String)"1800");
            expResult.put((String)"min interval", (String)"180");
            expResult.put((String)"peers","");

            System.out.println(" -- parsing request");
            TreeMap result = instance.parseRequest();

            assertEquals(expResult, result);

            // check if the peer has turned into a seed properly
            System.out.println(" -- checking if the peer is a seed");
            EntityManager em = emf.createEntityManager();
            Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
            q.setParameter("peerId", newPeerId);
            try {
                Peer tmp = (Peer) q.getSingleResult();
                assertTrue("is the peer a seed now?",tmp.isSeed());
            } catch(NoResultException ex) {
                fail("did not find any peer: " + ex.getMessage());
            }

        } catch (Exception ex) {
            String failMessage = "Exception Caught: ";
            failMessage += ex.toString();
            failMessage += " ";
            failMessage += ex.getMessage();
            ex.printStackTrace();
            fail(failMessage);
        }
    }

//
//    /**
//     * Test of parseRequest method with no event given, of class TrackerRequestParser.
//     * Regular announce.
//     */
//    @Test
//    public void testParseRequestNoEvent() {
//        try {
//            System.out.println("parseRequest");
//            TrackerRequestParser instance = new TrackerRequestParser();
//            // populate request and address
//            TreeMap request = new TreeMap();
//            InetAddress address;
//            TreeMap expResult = new TreeMap();
//            String newPeerId = new String();
//
//            /**
//             * For event = started
//             */
//            address = InetAddress.getByName("192.168.1.4");
//            request.put((String)"info_hash", infoHash);
//            newPeerId = "lbdfgd1321-------002";
//            request.put((String)"peerId",newPeerId);
//            request.put((String)"",)
//
//            TreeMap result = instance.parseRequest();
//
//            assertEquals(expResult, result);
//        } catch (Exception ex) {
//            System.out.println(ex.getMessage());
//            fail("Exception Caught");
//        }
//    }
//
//    /**
//     * Test of parseRequest method with compact = 0, of class TrackerRequestParser.
//     */
//    @Test
//    public void testParseRequestNoCompact() {
//        try {
//            System.out.println("parseRequest");
//            TrackerRequestParser instance = new TrackerRequestParser();
//            // populate request and address
//            TreeMap request = new TreeMap();
//            InetAddress address;
//            TreeMap expResult = new TreeMap();
//            String newPeerId = new String();
//
//            /**
//             * For event = started
//             */
//            address = InetAddress.getByName("192.168.1.4");
//            request.put((String)"info_hash", infoHash);
//            newPeerId = "lbdfgd1321-------002";
//            request.put((String)"peerId",newPeerId);
//            request.put((String)"",)
//
//            TreeMap result = instance.parseRequest();
//
//            assertEquals(expResult, result);
//        } catch (Exception ex) {
//            System.out.println(ex.getMessage());
//            fail("Exception Caught");
//        }
//    }
//
//    /**
//     * Test of parseRequest method with errors, of class TrackerRequestParser.
//     */
//    @Test
//    public void testParseRequestAssortedErrors() {
//        try {
//            System.out.println("parseRequest");
//            TrackerRequestParser instance = new TrackerRequestParser();
//            // populate request and address
//            TreeMap request = new TreeMap();
//            InetAddress address;
//            TreeMap expResult = new TreeMap();
//            String newPeerId = new String();
//
//            /**
//             * For event = started
//             */
//            address = InetAddress.getByName("192.168.1.4");
//            request.put((String)"info_hash", infoHash);
//            newPeerId = "lbdfgd1321-------002";
//            request.put((String)"peerId",newPeerId);
//            request.put((String)"",)
//
//            TreeMap result = instance.parseRequest();
//
//            assertEquals(expResult, result);
//        } catch (Exception ex) {
//            System.out.println(ex.getMessage());
//            fail("Exception Caught");
//        }
//    }
}