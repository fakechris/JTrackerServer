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

package com.tracker.backend;

import com.tracker.backend.entity.Peer;
import com.tracker.backend.entity.Torrent;

import com.tracker.backend.webinterface.entity.TorrentData;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
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

// TODO: test that the purging of inactive peers work.

/**
 *
 * @author bo
 */
public class TrackerRequestParserTest {

    static Peer p;
    static Torrent t;
    static TorrentData tData;

    static String infoHash;
    static String peerId;
    static String peerAddress; /** used for validating peers response. */
    static byte[] rawInfoHash = new byte[20];
    static byte[] rawPeerId = new byte[20];

    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("QuashPU");


    public TrackerRequestParserTest() {
    }

    public String getRawString(byte[] raw) {
        StringBuffer result = new StringBuffer();

        for(int i = 0; i < raw.length; i++) {
            result.append((char) raw[i]);
        }
        return result.toString();
    }

    public HashMap<String,String[]> peerRequest(String peerId, String infoHash, String event) throws Exception
    {
        HashMap<String,String[]> requestParams = new HashMap<String,String[]>();

        try {
            String[] s = new String[1];
            // info hash is not URL-encoded because it is automatically decoded
            // by tomcat
            s[0] = infoHash;
            requestParams.put((String) "info_hash", s.clone());
            s[0] = peerId;
            requestParams.put((String)"peer_id", s.clone());
            s[0] = String.valueOf(1234);
            requestParams.put((String)"port", s.clone());
            s[0] = String.valueOf(0);
            requestParams.put((String)"uploaded", s.clone());
            requestParams.put((String)"downloaded", s.clone());
            requestParams.put((String)"left", s.clone());
            s[0] = event;
            requestParams.put((String)"event", s.clone());
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
        tData = new TorrentData();
        p = new Peer();
        tData.setAdded(Calendar.getInstance().getTime());
        tData.setDescription((String)"testing testing");

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

        tData.setName((String)"testing torrent");
        tData.setTorrentSize((long)999999999);

        t.setTorrentData(tData);

        p.setBytesLeft(tData.getTorrentSize());
        p.setIp(InetAddress.getByName("192.168.1.3"));
        // 192.168.1.3 in bigendian bytes are 0xC0A80103

        Long port = (long) 63049;
        p.setPort(port);
        // 63049 in bigendian bytes are 0xF649

        // setting the peer address to compare to
        StringBuilder sb = new StringBuilder();
        byte[] addr = InetAddress.getByName("192.168.1.3").getAddress();
        for(int i = 0; i < addr.length; i++) {
            char charByte = (char) addr[i];
            sb.append(charByte);
        }
        // and for the port
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(port);
        sb.append((char)bb.get(6));
        sb.append((char)bb.get(7));

        peerAddress = sb.toString();

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
        em.persist(tData);
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
            HashMap params = peerRequest(peerId, getRawString(rawInfoHash), "");
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
            HashMap<String,HashMap> results = new HashMap<String,HashMap>();
            HashMap<String,HashMap> expResults = new HashMap<String,HashMap>();
            HashMap<String,Long> expResultContents = new HashMap<String,Long>();

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
            HashMap result;
            HashMap<String,Long> expResult = new HashMap<String,Long>();

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
            HashMap request;
            InetAddress address;
            HashMap expResult = new HashMap();
            String newPeerId = new String();
            String[] s = new String[1];
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
            s[0] = String.valueOf(999999999);
            request.put((String)"left", s.clone());

            instance.setRemoteAddress(address);
            instance.setRequestParams(request);

            System.out.println(" -- populating expected result");
            expResult.put((String)"complete", 0L);
            // the new peer and the default peer
            expResult.put((String)"incomplete", 2L);
            expResult.put((String)"interval", 1800L);
            expResult.put((String)"min interval", 180L);
            // grabbed from output
            /*expResult.put((String)"peers",
                    URLDecoder.decode("%28%01%03%00%00", "utf-8"));*/
            expResult.put((String)"peers", peerAddress);

            System.out.println(" -- parsing request");
            HashMap result = instance.parseRequest();

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
            HashMap request;
            InetAddress address;
            HashMap expResult = new HashMap();
            String[] s = new String[1];

            /**
             * For event = stopped
             */
            System.out.println(" -- populating address and request");
            address = p.getIp();

            request = peerRequest(getRawString(rawPeerId), getRawString(rawInfoHash), "stopped");
            s[0] = String.valueOf(999999999);
            request.put((String)"left", s.clone());

            instance.setRemoteAddress(address);
            instance.setRequestParams(request);

            System.out.println(" -- populating expected result");
            expResult.put((String)"complete", 0L);
            // no peers left after this one was removed
            expResult.put((String)"incomplete", 0L);
            expResult.put((String)"interval", 1800L);
            expResult.put((String)"min interval", 180L);
            expResult.put((String)"peers","");

            System.out.println(" -- parsing request");
            HashMap result = instance.parseRequest();

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
            HashMap request;
            InetAddress address;
            HashMap expResult = new HashMap();
            String[] s = new String[1];

            /**
             * For event = completed
             */
            System.out.println(" -- populating address and request");
            address = p.getIp();
            
            request = peerRequest(getRawString(rawPeerId), getRawString(rawInfoHash), "completed");
            s[0] = String.valueOf(999999999);
            request.put((String)"left", s.clone());

            instance.setRemoteAddress(address);
            instance.setRequestParams(request);

            System.out.println(" -- populating expected result");
            expResult.put((String)"complete", 1L);
            // no leechers left after this one turned into a seed
            expResult.put((String)"incomplete", 0L);
            expResult.put((String)"interval", 1800L);
            expResult.put((String)"min interval", 180L);
            expResult.put((String)"peers","");

            System.out.println(" -- parsing request");
            HashMap result = instance.parseRequest();

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
            HashMap request, result;
            InetAddress address;
            HashMap expResult = new HashMap();
            String[] s = new String[1];

            /**
             * For event = ""
             */

            System.out.println(" - Trying event=\"\"");
            System.out.println(" -- populating address and request");
            address = p.getIp();

            // first try with event=""
            request = peerRequest(getRawString(rawPeerId), getRawString(rawInfoHash), "");
            s[0] = String.valueOf(999990000);
            request.put((String)"left", s.clone());
            s[0] = String.valueOf(9999);
            request.put((String)"downloaded", s.clone());
            s[0] = String.valueOf(105068);
            request.put((String)"uploaded", s.clone());

            instance.setRemoteAddress(address);
            instance.setRequestParams(request);

            System.out.println(" -- populating expected result");
            expResult.put((String)"complete", 0L);
            expResult.put((String)"incomplete", 1L);
            expResult.put((String)"interval", 1800L);
            expResult.put((String)"min interval", 180L);
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
            s[0] = String.valueOf(999980000);
            request.put((String)"left", s.clone());
            s[0] = String.valueOf(19999);
            request.put((String)"downloaded", s.clone());
            s[0] = String.valueOf(185068);
            request.put((String)"uploaded", s.clone());

            instance.setRemoteAddress(address);
            instance.setRequestParams(request);

            System.out.println(" -- populating expected result");
            expResult.put((String)"complete", 0L);
            expResult.put((String)"incomplete", 1L);
            expResult.put((String)"interval", 1800L);
            expResult.put((String)"min interval", 180L);
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
            HashMap request, result;
            InetAddress address;
            HashMap expResult = new HashMap();
            String[] s = new String[1];

            /**
             * For compact=0
             */

            System.out.println(" -- populating address and request");
            address = p.getIp();
            
            request = peerRequest(getRawString(rawPeerId), getRawString(rawInfoHash), "");
            s[0] = String.valueOf(999990000);
            request.put((String)"left", s.clone());
            s[0] = String.valueOf(9999);
            request.put((String)"downloaded", s.clone());
            s[0] = String.valueOf(105068);
            request.put((String)"uploaded", s.clone());
            s[0] = String.valueOf(0);
            request.put((String)"compact", s.clone());

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
//            HashMap request = new HashMap();
//            InetAddress address;
//            HashMap expResult = new HashMap();
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
//            HashMap result = instance.parseRequest();
//
//            assertEquals(expResult, result);
//        } catch (Exception ex) {
//            System.out.println(ex.getMessage());
//            fail("Exception Caught");
//        }
//    }
}