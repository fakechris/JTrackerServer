/*
 * $Id$
 *
 * Copyright © 2009 Bjørn Øivind Bjørnsen
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

package com.tracker.frontend;

import com.tracker.backend.StringUtils;
import com.tracker.backend.entity.Peer;
import com.tracker.backend.entity.Torrent;
import com.tracker.backend.webinterface.entity.TorrentData;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.runner.RunWith;

/**
 * Integration Test of the Announce class.
 * Currently implements the following tests:
 *  - empty request
 *  - event=started
 *  - event=stopped
 *  - event=completed
 *  - no event
 *  - no compact
 *  - peer timeout
 * TODO:
 *  - more tests of counters (seeds, peers, completed)?
 *
 * @see com.tracker.frontend.Announce
 * @author bo
 */
@RunWith(JMock.class)
public class AnnounceTest {

    Mockery context = new JUnit4Mockery();

    final HttpServletRequest request = context.mock(HttpServletRequest.class);
    final HttpServletResponse response = context.mock(HttpServletResponse.class);

    static Peer leech;
    static Peer seed;
    static Torrent t;
    static TorrentData tData;

    static String infoHash;
    static String leechId;
    static String seedId;
    static String leechAddress; /** used for validating peers response. */
    static String seedAddress; /** used for validating peers response. */

    static byte[] rawInfoHash = new byte[20];

    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("QuashPU");


    /**
     * Mocks out the ServletOutputStream used by Announce to write the tracker
     * response.
     */
    private class MockOutput extends ServletOutputStream {

        private StringBuffer buf;

        public MockOutput() {
            buf = new StringBuffer();
        }

        @Override
        public void write(int b) throws IOException {
            buf.append((char)b);
        }

        public String getContent() {
            return buf.toString();
        }

        public void clear() {
            buf.delete(0, buf.length());
        }

    }

    public AnnounceTest() {
    }

    /**
     * Constructs a simple set of expectations of what the Announce class will
     * demand of the HttpServletRequest and HttpServletResponse classes.
     * @param out the ServletOutputStream the response will be written to.
     * @param params the parameters sent as part of the request.
     * @throws java.io.IOException if there is a problem with the
     * ServletOutputStream class (not likely, this only constructs the mocks).
     */
    void constructExpectations(final MockOutput out, final Map params) throws IOException {
        context.checking(new Expectations() {{
            oneOf(request).getRemoteAddr(); will(returnValue("192.168.1.1"));
            oneOf(request).getParameterMap(); will(returnValue(params));
            oneOf(response).setContentType(with(aNonNull(String.class)));
            oneOf(response).getOutputStream(); will(returnValue(out));
        }});
    }
    
    /**
     * Simple method for getting a raw string from a stream of bytes.
     * @param raw the array of bytes to convert to String.
     * @return a String with the raw representation of the bytes given as parameter.
     */
    private String getRawString(byte[] raw) {
        StringBuffer result = new StringBuffer();

        for(int i = 0; i < raw.length; i++) {
            result.append((char) raw[i]);
        }
        return result.toString();
    }

    /**
     * Construct a simple peer request in the same form as the servlet receives,
     * based on the parameters given.
     * The other parameters are as follows:
     * port = 1234
     * uploaded = 0
     * downloaded = 0
     * left = 0
     * @param peerId the (raw) peer id of the peer
     * @param infoHash the (raw) info hash of the torrent
     * @param event the (optional) event given.
     * @return a Map<String,String[]> containing the parameters given.
     */
    private HashMap<String,String[]> peerRequest(String peerId, String infoHash, String event) {
        HashMap<String,String[]> requestParams = new HashMap<String,String[]>();

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

        return requestParams;
    }

    /**
     * Gets the compact form of the ip and port specified in the parameters as a
     * String.
     * @param ip the address of the peer.
     * @param port the port the peer uses.
     * @return a String containing the compact ip and port of the peer.
     */
    private String getCompactAddress(byte[] ip, Long port) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < ip.length; i++) {
            char charByte = (char) ip[i];
            sb.append(charByte);
        }
        // and for the port
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(port);
        sb.append((char)bb.get(6));
        sb.append((char)bb.get(7));

        return sb.toString();
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
        System.out.println("Setting up database");

        EntityManager em = emf.createEntityManager();
        Random r = new Random(Calendar.getInstance().getTimeInMillis());

        // our test torrent
        t = new Torrent();
        tData = new TorrentData();
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

        // our test leech
        leech = new Peer();
        leech.setBytesLeft(tData.getTorrentSize());
        leech.setIp(InetAddress.getByName("192.168.1.3"));
        leech.setPort(63049L);

        // set peer id and raw peer id
        leechId = "lbdfgd1321-------001";
        leech.setPeerId(leechId);
        leech.setSeed(false);
        leech.setLastActionTime(Calendar.getInstance().getTime());

        // grab the peer address for use in the tests
        leechAddress = getCompactAddress(leech.getIp().getAddress(), leech.getPort());

        t.addLeecher(leech);

        // our test seed
        seed = new Peer();
        seed.setBytesLeft(0L);
        seed.setIp(InetAddress.getByName("192.168.1.2"));
        seed.setPort(38123L);

        // set peer id and raw peer id
        seedId = "lbdfgd1321-------002";
        seed.setPeerId(seedId);
        seed.setSeed(true);
        seed.setLastActionTime(Calendar.getInstance().getTime());

        // grab the peer address for use in the tests
        seedAddress = getCompactAddress(seed.getIp().getAddress(), seed.getPort());

        t.addSeeder(seed);

        // persist the changes
        em.getTransaction().begin();
        em.persist(t);
        em.persist(tData);
        em.persist(leech);
        em.persist(seed);
        em.getTransaction().commit();
        em.close();
    }

    @After
    public void tearDown() {
        // removes all the peers and torrent after each test
        EntityManager em = emf.createEntityManager();

        System.out.println("Tearing down database");

        em.getTransaction().begin();

        // removing the torrent should tear down the peers.
        Query q = em.createQuery("SELECT t FROM Torrent t");
        List l = q.getResultList();
        Iterator itr = l.iterator();
        while(itr.hasNext()) {
            Torrent tmp = (Torrent) itr.next();
            em.remove(tmp);
        }

        em.getTransaction().commit();
        em.close();
    }

    /**
     * Test of an empty request given to Announce
     */
    @Test
    public void testEmptyRequest() throws Exception {
        System.out.println("empty request");

        final MockOutput out = new MockOutput();

        final Map params = new HashMap<String,String[]>();

        constructExpectations(out, params);

        Announce instance = new Announce();
        instance.processRequest(request, response);

        assertEquals("d14:failure reason18:missing info hash!e", out.getContent());
    }

    /**
     * Test of an event=started event from a seed.
     */
    @Test
    public void testEventStartedSeed() throws Exception {
        System.out.println("event=started seed");

        String expectedResult = new String();
        expectedResult += "d" +         // dictionary
                "8:completei2e" +       // 2 completed (the added peer)
                "10:incompletei1e" +    // 1 incomplete (the peer from setup)
                "8:intervali300e" +     // 300 second regular interval
                "12:min intervali180e" + // 120 second minimum interval
                "5:peers12:"            // string of peers
                ;
        // this is followed by the leech address and the seed address in no
        // particularly defined order.
        String expectedSeed = seedAddress;
        String expectedLeech = leechAddress;

        final MockOutput out = new MockOutput();

        final Map params = peerRequest("lbdfgd1321-------003", getRawString(rawInfoHash), "started");

        constructExpectations(out, params);

        Announce instance = new Announce();
        instance.processRequest(request, response);

        assertTrue("Result contains the expected dictionary values",
                out.getContent().contains(expectedResult));
        assertTrue("Result contains the expected seed address",
                out.getContent().contains(expectedSeed));
        assertTrue("Result contains the expected leech address",
                out.getContent().contains(expectedLeech));

        // also look for the new peer in the DB
        EntityManager em = emf.createEntityManager();

        Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
        byte newRawPeerId[] = new byte[20];
        for(int i = 0; i < newRawPeerId.length; i++) {
            newRawPeerId[i] = (byte) "lbdfgd1321-------003".charAt(i);
        }
        q.setParameter("peerId", StringUtils.getHexString(newRawPeerId));

        List tmp = q.getResultList();
        assertEquals(1, tmp.size());
    }

    /**
     * Test of an event=started event from a peer.
     */
    @Test
    public void testEventStartedPeer() throws Exception {
        System.out.println("event=started peer");

        String expectedResult = new String();
        expectedResult += "d" +         // dictionary
                "8:completei1e" +       // 1 completed
                "10:incompletei2e" +    // 2 incomplete (the added peer + the peer from setup)
                "8:intervali300e" +     // 300 second regular interval
                "12:min intervali180e" + // 120 second minimum interval
                "5:peers12:"            // string of peers
                ;
        // this is followed by the leech address and the seed address in no
        // particularly defined order.
        String expectedSeed = seedAddress;
        String expectedLeech = leechAddress;

        final MockOutput out = new MockOutput();

        final Map<String,String[]> params = peerRequest("lbdfgd1321-------003", getRawString(rawInfoHash), "started");
        String s[] = new String[1];
        s[0] = "999999";
        params.put("left", s);

        constructExpectations(out, params);

        Announce instance = new Announce();
        instance.processRequest(request, response);

        assertTrue("Result contains the expected dictionary values",
                out.getContent().contains(expectedResult));
        assertTrue("Result contains the expected seed address",
                out.getContent().contains(expectedSeed));
        assertTrue("Result contains the expected leech address",
                out.getContent().contains(expectedLeech));

        // also look for the new peer in the DB
        EntityManager em = emf.createEntityManager();

        Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
        byte newRawPeerId[] = new byte[20];
        for(int i = 0; i < newRawPeerId.length; i++) {
            newRawPeerId[i] = (byte) "lbdfgd1321-------003".charAt(i);
        }
        q.setParameter("peerId", StringUtils.getHexString(newRawPeerId));

        List tmp = q.getResultList();
        assertEquals(1, tmp.size());
    }

    /**
     * Test of an event=stopped event from a seed.
     */
    @Test
    public void testEventStoppedSeed() throws Exception {
        System.out.println("event=stopped seed");

        String expectedResult = new String();
        expectedResult += "d" +         // dictionary
                "8:completei0e" +       // 0 completed (we just stopped the single seed)
                "10:incompletei1e" +    // 1 incomplete (the peer from setUp())
                "8:intervali300e" +     // 300 second regular interval
                "12:min intervali180e" + // 120 second minimum interval
                "5:peers0:" +           // string of peers (none should be returned)
                "e"                     // end of dictionary
                ;

        final MockOutput out = new MockOutput();

        final Map<String,String[]> params = peerRequest(seedId, getRawString(rawInfoHash), "stopped");

        constructExpectations(out, params);
        
        Announce instance = new Announce();
        instance.processRequest(request, response);

        assertEquals(expectedResult, out.getContent());

        // check that the peer has been removed from the DB
        EntityManager em = emf.createEntityManager();

        Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
        q.setParameter("peerId", seed.getPeerId());

        List tmp = q.getResultList();
        assertEquals(0, tmp.size());

        // check that the numPeers and numSeeders are updated
        q = em.createQuery("SELECT t FROM Torrent t WHERE t.infoHash = :infoHash");
        q.setParameter("infoHash", infoHash);
        Torrent dbObject = (Torrent) q.getSingleResult();
        Long num = 1L;
        assertEquals("Checking the number of peers the torrent has", num, dbObject.getNumPeers());
        assertEquals("Checking the number of leechers the torrent has", num, dbObject.getNumLeechers());
        num = 0L;
        assertEquals("Checking the number of seeders the torrent has", num, dbObject.getNumSeeders());
    }

    /**
     * Test of an event=stopped event from a peer.
     */
    @Test
    public void testEventStoppedPeer() throws Exception {
        System.out.println("event=stopped peer");

        String expectedResult = new String();
        expectedResult += "d" +         // dictionary
                "8:completei1e" +       // 1 completed (the seed from setUp())
                "10:incompletei0e" +    // 0 incomplete (we just stopped the peer)
                "8:intervali300e" +     // 300 second regular interval
                "12:min intervali180e" + // 120 second minimum interval
                "5:peers0:" +           // string of peers (none should be returned)
                "e"                     // end of dictionary
                ;

        final MockOutput out = new MockOutput();

        final Map<String,String[]> params = peerRequest(leechId, getRawString(rawInfoHash), "stopped");

        constructExpectations(out, params);

        Announce instance = new Announce();
        instance.processRequest(request, response);

        assertEquals(expectedResult, out.getContent());

        // check that the peer has been removed from the DB
        EntityManager em = emf.createEntityManager();

        Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
        q.setParameter("peerId", leech.getPeerId());

        List tmp = q.getResultList();
        assertEquals(0, tmp.size());

        // check that the numPeers and numSeeders are updated
        q = em.createQuery("SELECT t FROM Torrent t WHERE t.infoHash = :infoHash");
        q.setParameter("infoHash", infoHash);
        Torrent dbObject = (Torrent) q.getSingleResult();
        Long num = 1L;
        assertEquals("Checking the number of peers the torrent has", num, dbObject.getNumPeers());
        assertEquals("Checking the number of seeds the torrent has", num, dbObject.getNumSeeders());
        num = 0L;
        assertEquals("Checking the number of leechers the torrent has", num, dbObject.getNumLeechers());
    }

    /**
     * Test of an event=completed event from a peer.
     */
    @Test
    public void testEventCompletedPeer() throws Exception {
        System.out.println("event=completed peer");

        String expectedResult = new String();
        expectedResult += "d" +         // dictionary
                "8:completei2e" +       // 1 completed (the seed from setUp() and this peer)
                "10:incompletei0e" +    // 0 incomplete (peer completed)
                "8:intervali300e" +     // 300 second regular interval
                "12:min intervali180e" + // 120 second minimum interval
                "5:peers6:"             // string of peers (the other seed)
                ;
        String expectedPeerList = seedAddress;

        final MockOutput out = new MockOutput();

        final Map<String,String[]> params = peerRequest(leechId, getRawString(rawInfoHash), "completed");

        constructExpectations(out, params);

        Announce instance = new Announce();
        instance.processRequest(request, response);

        assertTrue("Checking that the response is proper",
                out.getContent().contains(expectedResult));
        assertTrue("Checking that the response contains correct peerlist",
                out.getContent().contains(expectedPeerList));

        // check that the peer has been promoted to seed
        EntityManager em = emf.createEntityManager();

        Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
        q.setParameter("peerId", leech.getPeerId());

        List tmp = q.getResultList();
        assertEquals(1, tmp.size());
        Peer p = (Peer)tmp.get(0);
        assertTrue("Checking that the leech turned into a seed", p.isSeed());

        // check that the numPeers and numSeeders are updated
        q = em.createQuery("SELECT t FROM Torrent t WHERE t.infoHash = :infoHash");
        q.setParameter("infoHash", infoHash);
        Torrent dbObject = (Torrent) q.getSingleResult();
        Long num = 2L;
        assertEquals("Checking the number of peers the torrent has", num, dbObject.getNumPeers());
        assertEquals("Checking the number of seeds the torrent has", num, dbObject.getNumSeeders());
        num = 0L;
        assertEquals("Checking the number of leechers the torrent has", num, dbObject.getNumLeechers());
    }

    /**
     * Test of an event=completed event from a seed.
     * (should not do anything in particular, a seed is already completed by
     * definition.)
     */
    @Test
    public void testEventCompletedSeed() throws Exception {
        System.out.println("event=completed seed");

        String expectedResult = new String();
        expectedResult += "d" +         // dictionary
                "8:completei1e" +       // 1 completed (the seed from setUp())
                "10:incompletei1e" +    // 0 incomplete (the leech from setUp())
                "8:intervali300e" +     // 300 second regular interval
                "12:min intervali180e" + // 120 second minimum interval
                "5:peers6:"             // string of peers (the leech)
                ;
        String expectedPeerList = leechAddress;

        final MockOutput out = new MockOutput();

        final Map<String,String[]> params = peerRequest(seedId, getRawString(rawInfoHash), "completed");

        constructExpectations(out, params);

        Announce instance = new Announce();
        instance.processRequest(request, response);

        assertTrue("Checking that the response is proper",
                out.getContent().contains(expectedResult));
        assertTrue("Checking that the response contains correct peerlist",
                out.getContent().contains(expectedPeerList));

        // check that the peer has been promoted to seed
        EntityManager em = emf.createEntityManager();

        Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
        q.setParameter("peerId", seed.getPeerId());

        List tmp = q.getResultList();
        assertEquals(1, tmp.size());
        Peer p = (Peer)tmp.get(0);
        assertTrue("Checking that the seed is still a seed", p.isSeed());

        // check that the numPeers and numSeeders are not updated, but stay static
        q = em.createQuery("SELECT t FROM Torrent t WHERE t.infoHash = :infoHash");
        q.setParameter("infoHash", infoHash);
        Torrent dbObject = (Torrent) q.getSingleResult();
        Long num = 2L;
        assertEquals("Checking the number of peers the torrent has", num, dbObject.getNumPeers());
        num = 1L;
        assertEquals("Checking the number of seeds the torrent has", num, dbObject.getNumSeeders());
        assertEquals("Checking the number of leechers the torrent has", num, dbObject.getNumLeechers());
    }

    /**
     * Test of an event="" event from a Peer and a Seed.
     * Should only update the peer stats.
     */
    @Test
    public void testEventNone() throws Exception {
        System.out.println("event=\"\"");

        // test the leech
        String expectedResult = new String();
        expectedResult += "d" +         // dictionary
                "8:completei1e" +       // 1 completed (the seed from setUp())
                "10:incompletei1e" +    // 0 incomplete (the leech from setUp())
                "8:intervali300e" +     // 300 second regular interval
                "12:min intervali180e" + // 120 second minimum interval
                "5:peers6:"             // string of peers (the leech)
                ;
        String expectedPeerList = seedAddress;

        final MockOutput out = new MockOutput();

        final Map<String,String[]> params = peerRequest(leechId, getRawString(rawInfoHash), "");
        String s[] = new String[1];
        s[0] = String.valueOf(819600L);
        params.put("downloaded", s.clone());
        s[0] = String.valueOf(192000L);
        params.put("uploaded", s.clone());
        s[0] = String.valueOf(800000L);
        params.put("left", s.clone());

        constructExpectations(out, params);

        Announce instance = new Announce();
        instance.processRequest(request, response);

        assertTrue("Checking that the response is proper",
                out.getContent().contains(expectedResult));
        assertTrue("Checking that the response contains correct peerlist",
                out.getContent().contains(expectedPeerList));

        // check that the peer status has been updated
        EntityManager em = emf.createEntityManager();

        Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
        q.setParameter("peerId", leech.getPeerId());

        List tmp = q.getResultList();
        assertEquals(1, tmp.size());
        Peer p = (Peer)tmp.get(0);
        Long l = 819600L;
        assertEquals("Checking that the downloaded stats is updated", l, p.getDownloaded());
        l = 192000L;
        assertEquals("Checking that the uploaded stats is updated", l, p.getUploaded());
        l = 800000L;
        assertEquals("Checking that the left stats is updated", l, p.getBytesLeft());

        // test the seed
        out.clear();
        expectedPeerList = leechAddress;
        s[0] = seedId;
        params.put("peer_id", s.clone());

        constructExpectations(out, params);

        instance.processRequest(request, response);

        assertTrue("Checking that the response is proper",
                out.getContent().contains(expectedResult));
        assertTrue("Checking that the response contains correct peerlist",
                out.getContent().contains(expectedPeerList));

        q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
        q.setParameter("peerId", seed.getPeerId());

        tmp = q.getResultList();
        assertEquals(1, tmp.size());
        p = (Peer)tmp.get(0);
        l = 819600L;
        assertEquals("Checking that the downloaded stats is updated", l, p.getDownloaded());
        l = 192000L;
        assertEquals("Checking that the uploaded stats is updated", l, p.getUploaded());
        l = 800000L;
        assertEquals("Checking that the left stats is updated", l, p.getBytesLeft());

        // check that the numPeers and numSeeders are not updated, but stay static
        q = em.createQuery("SELECT t FROM Torrent t WHERE t.infoHash = :infoHash");
        q.setParameter("infoHash", infoHash);
        Torrent dbObject = (Torrent) q.getSingleResult();
        Long num = 2L;
        assertEquals("Checking the number of peers the torrent has", num, dbObject.getNumPeers());
        num = 1L;
        assertEquals("Checking the number of seeds the torrent has", num, dbObject.getNumSeeders());
        assertEquals("Checking the number of leechers the torrent has", num, dbObject.getNumLeechers());
    }

    /**
     * Test of a standard announce with no event key from a Peer and a Seed.
     * Should only update the peer stats.
     */
    @Test
    public void testNoEvent() throws Exception {
        System.out.println("no event key");

        // test the leech
        String expectedResult = new String();
        expectedResult += "d" +         // dictionary
                "8:completei1e" +       // 1 completed (the seed from setUp())
                "10:incompletei1e" +    // 0 incomplete (the leech from setUp())
                "8:intervali300e" +     // 300 second regular interval
                "12:min intervali180e" + // 120 second minimum interval
                "5:peers6:"             // string of peers (the leech)
                ;
        String expectedPeerList = seedAddress;

        final MockOutput out = new MockOutput();

        final Map<String,String[]> params = peerRequest(leechId, getRawString(rawInfoHash), "");

        params.remove("event");

        String s[] = new String[1];
        s[0] = String.valueOf(819600L);
        params.put("downloaded", s.clone());
        s[0] = String.valueOf(192000L);
        params.put("uploaded", s.clone());
        s[0] = String.valueOf(800000L);
        params.put("left", s.clone());

        constructExpectations(out, params);

        Announce instance = new Announce();
        instance.processRequest(request, response);

        assertTrue("Checking that the response is proper",
                out.getContent().contains(expectedResult));
        assertTrue("Checking that the response contains correct peerlist",
                out.getContent().contains(expectedPeerList));

        // check that the peer status has been updated
        EntityManager em = emf.createEntityManager();

        Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
        q.setParameter("peerId", leech.getPeerId());

        List tmp = q.getResultList();
        assertEquals(1, tmp.size());
        Peer p = (Peer)tmp.get(0);
        Long l = 819600L;
        assertEquals("Checking that the downloaded stats is updated", l, p.getDownloaded());
        l = 192000L;
        assertEquals("Checking that the uploaded stats is updated", l, p.getUploaded());
        l = 800000L;
        assertEquals("Checking that the left stats is updated", l, p.getBytesLeft());

        // test the seed
        out.clear();
        expectedPeerList = leechAddress;
        s[0] = seedId;
        params.put("peer_id", s.clone());

        constructExpectations(out, params);

        instance.processRequest(request, response);

        assertTrue("Checking that the response is proper",
                out.getContent().contains(expectedResult));
        assertTrue("Checking that the response contains correct peerlist",
                out.getContent().contains(expectedPeerList));

        q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
        q.setParameter("peerId", seed.getPeerId());

        tmp = q.getResultList();
        assertEquals(1, tmp.size());
        p = (Peer)tmp.get(0);
        l = 819600L;
        assertEquals("Checking that the downloaded stats is updated", l, p.getDownloaded());
        l = 192000L;
        assertEquals("Checking that the uploaded stats is updated", l, p.getUploaded());
        l = 800000L;
        assertEquals("Checking that the left stats is updated", l, p.getBytesLeft());

        // check that the numPeers and numSeeders are not updated, but stay static
        q = em.createQuery("SELECT t FROM Torrent t WHERE t.infoHash = :infoHash");
        q.setParameter("infoHash", infoHash);
        Torrent dbObject = (Torrent) q.getSingleResult();
        Long num = 2L;
        assertEquals("Checking the number of peers the torrent has", num, dbObject.getNumPeers());
        num = 1L;
        assertEquals("Checking the number of seeds the torrent has", num, dbObject.getNumSeeders());
        assertEquals("Checking the number of leechers the torrent has", num, dbObject.getNumLeechers());
    }

    /**
     * Test of a request specifying compact=0.
     */
    @Test
    public void testNoCompact() throws Exception {
        System.out.println("no compact");

        final MockOutput out = new MockOutput();

        final Map<String,String[]> params = peerRequest(leechId, getRawString(rawInfoHash), "");
        String s[] = new String[1];
        s[0] = String.valueOf(0);
        params.put("compact", s.clone());

        constructExpectations(out, params);

        Announce instance = new Announce();
        instance.processRequest(request, response);

        assertEquals("d14:failure reason44:this tracker only supports compact responsese", out.getContent());
    }

    /**
     * Test of a request causing a peer to be removed by the inactive peer check.
     */
    @Test
    public void testPeerInactive() throws Exception {
        System.out.println("peer inactive");

        /* add a leech with a last action time well in the past (beyond the
         default announce interval and the grace period.
         */
        EntityManager em = emf.createEntityManager();

        // our test leech
        Peer leech2 = new Peer();
        leech2.setBytesLeft(tData.getTorrentSize());
        leech2.setIp(InetAddress.getByName("192.168.1.4"));
        leech2.setPort(1024L);

        // set peer id and raw peer id
        String leech2Id = "lbdfgd1321-------004";
        leech2.setPeerId(leech2Id);
        leech2.setSeed(false);
        Date d = new Date();
        // 300 seconds is the default announce time, the grace time is 120 seconds
        d.setTime(Calendar.getInstance().getTimeInMillis() - (300 * 1000) - (200 * 1000));
        leech2.setLastActionTime(d);

        // grab the peer address for use in the tests
        String leech2Address = getCompactAddress(leech.getIp().getAddress(), leech.getPort());

        assertTrue(t.addLeecher(leech2));

        em.getTransaction().begin();
        em.merge(t);
        em.persist(leech2);
        em.getTransaction().commit();
        
        String expectedResult = new String();
        expectedResult += "d" +         // dictionary
                "8:completei1e" +       // 1 completed (the seed from setUp())
                "10:incompletei1e" +    // 1 incomplete (the leech from setUp())
                "8:intervali300e" +     // 300 second regular interval
                "12:min intervali180e" + // 120 second minimum interval
                "5:peers6:"             // string of peers (the leech)
                ;
        String expectedPeerList = seedAddress;

        final MockOutput out = new MockOutput();

        final Map<String,String[]> params = peerRequest(leechId, getRawString(rawInfoHash), "");
        String s[] = new String[1];
        s[0] = String.valueOf(819600L);
        params.put("downloaded", s.clone());
        s[0] = String.valueOf(192000L);
        params.put("uploaded", s.clone());
        s[0] = String.valueOf(800000L);
        params.put("left", s.clone());

        constructExpectations(out, params);

        Announce instance = new Announce();
        instance.processRequest(request, response);

        assertTrue("Checking that the response is proper",
                out.getContent().contains(expectedResult));
        assertTrue("Checking that the response contains correct peerlist",
                out.getContent().contains(expectedPeerList));
        assertFalse("Checking that the peerlist does not contain the expired peer",
                out.getContent().contains(leech2Address));

        // check that the numPeers and numSeeders reflect the purged peer
        em.clear(); // make sure the context is clean
        Query q = em.createQuery("SELECT t FROM Torrent t WHERE t.infoHash = :infoHash");
        q.setParameter("infoHash", infoHash);
        Torrent dbObject = (Torrent) q.getSingleResult();
        Long num = 2L;
        assertEquals("Checking the number of peers the torrent has", num, dbObject.getNumPeers());
        num = 1L;
        assertEquals("Checking the number of seeds the torrent has", num, dbObject.getNumSeeders());
        assertEquals("Checking the number of leechers the torrent has", num, dbObject.getNumLeechers());

        assertFalse("Checking that the peer is no longer in the seed table", dbObject.getLeechersData().contains(leech2));
        assertFalse("Checking that the peer is no longer in the peer table", dbObject.getPeersData().contains(leech2));
    }

    /**
     * Test of a request causing a seed to be removed by the inactive peer check.
     */
    @Test
    public void testSeedInactive() throws Exception {
        System.out.println("seed inactive");

        /* add a leech with a last action time well in the past (beyond the
         default announce interval and the grace period.
         */
        EntityManager em = emf.createEntityManager();

        // our test seed
        Peer seed2 = new Peer();
        seed2.setBytesLeft(0L);
        seed2.setIp(InetAddress.getByName("192.168.1.4"));
        seed2.setPort(1024L);

        // set peer id and raw peer id
        String seed2Id = "lbdfgd1321-------004";
        seed2.setPeerId(seed2Id);
        seed2.setSeed(true);
        Date d = new Date();
        // 300 seconds is the default announce time, the grace time is 120 seconds
        d.setTime(Calendar.getInstance().getTimeInMillis() - (300 * 1000) - (200 * 1000));
        seed2.setLastActionTime(d);

        // grab the peer address for use in the tests
        String seed2Address = getCompactAddress(seed2.getIp().getAddress(), seed2.getPort());

        assertTrue(t.addSeeder(seed2));

        em.getTransaction().begin();
        em.merge(t);
        em.persist(seed2);
        em.getTransaction().commit();

        String expectedResult = new String();
        expectedResult += "d" +         // dictionary
                "8:completei1e" +       // 1 completed (the seed from setUp())
                "10:incompletei1e" +    // 1 incomplete (the leech from setUp())
                "8:intervali300e" +     // 300 second regular interval
                "12:min intervali180e" + // 120 second minimum interval
                "5:peers6:"             // string of peers (the leech)
                ;
        String expectedPeerList = seedAddress;

        final MockOutput out = new MockOutput();

        final Map<String,String[]> params = peerRequest(leechId, getRawString(rawInfoHash), "");
        String s[] = new String[1];
        s[0] = String.valueOf(819600L);
        params.put("downloaded", s.clone());
        s[0] = String.valueOf(192000L);
        params.put("uploaded", s.clone());
        s[0] = String.valueOf(800000L);
        params.put("left", s.clone());

        constructExpectations(out, params);

        Announce instance = new Announce();
        instance.processRequest(request, response);

        assertTrue("Checking that the response is proper",
                out.getContent().contains(expectedResult));
        assertTrue("Checking that the response contains correct peerlist",
                out.getContent().contains(expectedPeerList));
        assertFalse("Checking that the peerlist does not contain the expired peer",
                out.getContent().contains(seed2Address));

        // check that the seed is no longer in the database
        Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
        q.setParameter("peerId", seed2.getPeerId());
        List tmp = q.getResultList();
        assertTrue("Checking that the seed is no longer in the database", tmp.isEmpty());

        // check that the numPeers and numSeeders reflect the purged peer
        em.clear(); // make sure the context is clean
        q = em.createQuery("SELECT t FROM Torrent t WHERE t.infoHash = :infoHash");
        q.setParameter("infoHash", infoHash);
        Torrent dbObject = (Torrent) q.getSingleResult();
        Long num = 2L;
        assertEquals("Checking the number of peers the torrent has", num, dbObject.getNumPeers());
        num = 1L;
        assertEquals("Checking the number of seeds the torrent has", num, dbObject.getNumSeeders());
        assertEquals("Checking the number of leechers the torrent has", num, dbObject.getNumLeechers());
        
        assertFalse("Checking that the seed is no longer in the seed table", dbObject.getSeedersData().contains(seed2));
        assertFalse("Checking that the seed is no longer in the peer table", dbObject.getPeersData().contains(seed2));
    }
}