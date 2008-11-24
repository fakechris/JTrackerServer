/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend;

import com.tracker.entity.Peer;
import com.tracker.entity.Torrent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.security.MessageDigest;
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
    static byte[] rawInfoHash = new byte[20];
    static byte[] rawPeerId = new byte[20];

    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");


    public TrackerRequestParserTest() {
    }

    public String getRawString(byte[] raw) {
        StringBuffer result = new StringBuffer();

        for(int i = 0; i < raw.length; i++) {
            result.append((char) raw[i]);
        }
        return result.toString();
    }

    public TreeMap peerRequest(String peerId, String infoHash, String event) throws Exception
    {
        TreeMap requestParams = new TreeMap();

        try {
            // info hash is not URL-encoded because it is automatically decoded
            // by tomcat
            requestParams.put((String) "info_hash", infoHash);
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

        System.out.println("Setting up database");

        EntityManager em = emf.createEntityManager();
        Random r = new Random(Calendar.getInstance().getTimeInMillis());

        t = new Torrent();
        p = new Peer();
        t.setAdded(Calendar.getInstance().getTime());
        t.setDescription((String)"testing testing");

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

        t.setName((String)"testing torrent");
        t.setTorrentSize((long)999999999);

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
            TreeMap params = peerRequest(peerId, getRawString(rawInfoHash), "");
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
     * Test of scrape() method of class TrackerRequestParser.
     */
    @Test
    public void testScrapeVoid() {
        try {
            System.out.println("Scrape()");
            TrackerRequestParser instance = new TrackerRequestParser();
            TreeMap<String,TreeMap> results = new TreeMap<String,TreeMap>();
            TreeMap<String,TreeMap> expResults = new TreeMap<String,TreeMap>();
            TreeMap<String,Long> expResultContents = new TreeMap<String,Long>();

            // torrent from setUp()
            expResultContents.put((String)"downloaded", 0L);
            expResultContents.put((String)"incomplete", 1L);
            expResultContents.put((String)"complete", 0L);

            expResults.put(StringUtils.URLEncodeFromHexString(infoHash), expResultContents);

            results = instance.scrape();

            assertEquals(expResults, results);

        } catch(Exception ex) {
            StringWriter s = new StringWriter();
            s.append("Exception Caught: ");
            s.append(ex.toString());
            s.append(" ");
            s.append(ex.getMessage());
            PrintWriter w = new PrintWriter(s);
            ex.printStackTrace(w);
            fail(s.toString());
        }
    }
    
    /**
     * Test of scrape(String infoHash) method of class TrackerRequestParser.
     */
    @Test
    public void testScrapeInfoHash() {
        try {
            System.out.println("Scrape()");
            TrackerRequestParser instance = new TrackerRequestParser();
            TreeMap result;
            TreeMap<String,Long> expResult = new TreeMap<String,Long>();

            expResult.put((String)"downloaded", 0L);
            expResult.put((String)"incomplete", 1L);
            expResult.put((String)"complete", 0L);

            result = instance.scrape(getRawString(rawInfoHash));

            assertEquals(expResult, result);
            
        } catch(Exception ex) {
            StringWriter s = new StringWriter();
            s.append("Exception Caught: ");
            s.append(ex.toString());
            s.append(" ");
            s.append(ex.getMessage());
            PrintWriter w = new PrintWriter(s);
            ex.printStackTrace(w);
            fail(s.toString());
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
            byte[] newRawPeerId = new byte[20];

            /**
             * For event = started
             */
            System.out.println(" -- populating address and request");
            address = InetAddress.getByName("192.168.1.4");
            newPeerId = "lbdfgd1321-------002";
            for(int i = 0; i < newRawPeerId.length; i++) {
                newRawPeerId[i] = (byte) newPeerId.charAt(i);
            }

            request = peerRequest(getRawString(newRawPeerId), getRawString(rawInfoHash), "started");
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
            q.setParameter("peerId", StringUtils.getHexString(newRawPeerId));
            List<Peer> tmp = q.getResultList();
            assertEquals(tmp.size(), 1);
            
        } catch (Exception ex) {
            StringWriter s = new StringWriter();
            s.append("Exception Caught: ");
            s.append(ex.toString());
            s.append(" ");
            s.append(ex.getMessage());
            PrintWriter w = new PrintWriter(s);
            ex.printStackTrace(w);
            fail(s.toString());
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

            /**
             * For event = stopped
             */
            System.out.println(" -- populating address and request");
            address = p.getIp();

            request = peerRequest(getRawString(rawPeerId), getRawString(rawInfoHash), "stopped");
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
            q.setParameter("peerId", StringUtils.getHexString(rawPeerId));
            List<Peer> tmp = q.getResultList();
            assertEquals(tmp.size(), 0);

        } catch (Exception ex) {
            StringWriter s = new StringWriter();
            s.append("Exception Caught: ");
            s.append(ex.toString());
            s.append(" ");
            s.append(ex.getMessage());
            PrintWriter w = new PrintWriter(s);
            ex.printStackTrace(w);
            fail(s.toString());
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

            /**
             * For event = completed
             */
            System.out.println(" -- populating address and request");
            address = p.getIp();
            
            request = peerRequest(getRawString(rawPeerId), getRawString(rawInfoHash), "completed");
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
            q.setParameter("peerId", StringUtils.getHexString(rawPeerId));
            try {
                Peer tmp = (Peer) q.getSingleResult();
                // make sure that we don't have some stale cache of this
                em.refresh(tmp);
                assertTrue("is the peer a seed now?",tmp.isSeed());
            } catch(NoResultException ex) {
                fail("did not find any peer: " + ex.getMessage());
            }

        } catch (Exception ex) {
            StringWriter s = new StringWriter();
            s.append("Exception Caught: ");
            s.append(ex.toString());
            s.append(" ");
            s.append(ex.getMessage());
            PrintWriter w = new PrintWriter(s);
            ex.printStackTrace(w);
            fail(s.toString());
        }
    }

    /**
     * Test of parseRequest method with no event, of class TrackerRequestParser.
     */
    @Test
    public void testParseRequestEventNone() {
        try {
            System.out.println("parseRequestEventNone");
            TrackerRequestParser instance = new TrackerRequestParser();
            // populate request and address
            TreeMap request, result;
            InetAddress address;
            TreeMap expResult = new TreeMap();

            /**
             * For event = ""
             */

            System.out.println(" - Trying event=\"\"");
            System.out.println(" -- populating address and request");
            address = p.getIp();

            // first try with event=""
            request = peerRequest(getRawString(rawPeerId), getRawString(rawInfoHash), "");
            request.put((String)"left", (String)"999990000");
            request.put((String)"downloaded", (String)"9999");
            request.put((String)"uploaded", (String)"105068");

            instance.setRemoteAddress(address);
            instance.setRequestParams(request);

            System.out.println(" -- populating expected result");
            expResult.put((String)"complete",(String)"0");
            expResult.put((String)"incomplete",(String)"1");
            expResult.put((String)"interval",(String)"1800");
            expResult.put((String)"min interval", (String)"180");
            expResult.put((String)"peers","");

            System.out.println(" -- parsing request");
            result = instance.parseRequest();

            assertEquals(expResult, result);

            // check if the peer has been updated
            System.out.println(" -- checking if the peer has been updated");
            EntityManager em = emf.createEntityManager();
            Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
            q.setParameter("peerId", StringUtils.getHexString(rawPeerId));
            try {
                Peer tmp = (Peer) q.getSingleResult();
                // make sure that we don't have some stale cache of this
                em.refresh(tmp);
                Long l = new Long(105068L);
                assertEquals(l, tmp.getUploaded());
                l = 9999L;
                assertEquals(l, tmp.getDownloaded());
                l = 999990000L;
                assertEquals(l, tmp.getBytesLeft());
            } catch(NoResultException ex) {
                fail("did not find any peer: " + ex.getMessage());
            }


            /**
             * for announce without event-key
             */

            System.out.println(" - Trying no event key");
            System.out.println(" -- populating address and request");

            // now try with no event key
            request = peerRequest(getRawString(rawPeerId), getRawString(rawInfoHash), "");
            request.remove((String)"event");
            request.put((String)"left", (String)"999980000");
            request.put((String)"downloaded", (String)"19999");
            request.put((String)"uploaded", (String)"185068");

            instance.setRemoteAddress(address);
            instance.setRequestParams(request);

            System.out.println(" -- populating expected result");
            expResult.put((String)"complete",(String)"0");
            expResult.put((String)"incomplete",(String)"1");
            expResult.put((String)"interval",(String)"1800");
            expResult.put((String)"min interval", (String)"180");
            expResult.put((String)"peers","");

            System.out.println(" -- parsing request");
            result = instance.parseRequest();

            assertEquals(expResult, result);

            // check if the peer has been updated
            System.out.println(" -- checking if the peer has been updated");
            q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
            q.setParameter("peerId", StringUtils.getHexString(rawPeerId));
            try {
                Peer tmp = (Peer) q.getSingleResult();
                // make sure that we don't have some stale cache of this
                em.refresh(tmp);
                Long l = new Long(185068L);
                assertEquals(l, tmp.getUploaded());
                l = 19999L;
                assertEquals(l, tmp.getDownloaded());
                l = 999980000L;
                assertEquals(l, tmp.getBytesLeft());
            } catch(NoResultException ex) {
                fail("did not find any peer: " + ex.getMessage());
            }

        } catch (Exception ex) {
            StringWriter s = new StringWriter();
            s.append("Exception Caught: ");
            s.append(ex.toString());
            s.append(" ");
            s.append(ex.getMessage());
            PrintWriter w = new PrintWriter(s);
            ex.printStackTrace(w);
            fail(s.toString());
        }
    }

    /**
     * Test of parseRequest method with compact=0, of class TrackerRequestParser.
     */
    @Test
    public void testParseRequestNoCompact() {
        try {
            System.out.println("parseRequestNoCompact");
            TrackerRequestParser instance = new TrackerRequestParser();
            // populate request and address
            TreeMap request, result;
            InetAddress address;
            TreeMap expResult = new TreeMap();

            /**
             * For compact=0
             */

            System.out.println(" -- populating address and request");
            address = p.getIp();
            
            request = peerRequest(getRawString(rawPeerId), getRawString(rawInfoHash), "");
            request.put((String)"left", (String)"999990000");
            request.put((String)"downloaded", (String)"9999");
            request.put((String)"uploaded", (String)"105068");
            request.put((String)"compact", (String)"0");

            instance.setRemoteAddress(address);
            instance.setRequestParams(request);

            System.out.println(" -- populating expected result");
            expResult.put((String)"failure reason",(String)"this tracker only supports compact responses");

            System.out.println(" -- parsing request");
            result = instance.parseRequest();

            assertEquals(expResult, result);

        } catch (Exception ex) {
            StringWriter s = new StringWriter();
            s.append("Exception Caught: ");
            s.append(ex.toString());
            s.append(" ");
            s.append(ex.getMessage());
            PrintWriter w = new PrintWriter(s);
            ex.printStackTrace(w);
            fail(s.toString());
        }
    }

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