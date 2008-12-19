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

package com.tracker.backend.webinterface.xml;

import com.tracker.backend.StringUtils;
import com.tracker.backend.entity.Peer;
import com.tracker.backend.entity.Torrent;
import com.tracker.backend.webinterface.entity.TorrentData;
import java.io.PrintWriter;
import java.io.StringReader;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import static org.junit.Assert.*;

/**
 *
 * @author bo
 */
public class XMLTorrentListTest {
    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");

    public XMLTorrentListTest() {
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
        TorrentData tData;
        Peer p;

        String infoHash;
        String peerId;
        byte[] rawInfoHash = new byte[20];
        byte[] rawPeerId = new byte[20];

        System.out.println("Setting up database");

        EntityManager em = emf.createEntityManager();
        Random r = new Random(Calendar.getInstance().getTimeInMillis());

        t = new Torrent();
        tData = new TorrentData();
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

        tData.setName((String)"This name contains the word 'Brilliant'");
        tData.setTorrentSize((long)999999999);

        tData.setAdded(Calendar.getInstance().getTime());
        tData.setDescription((String)"This description contains the word 'fabulous'.");

        t.setTorrentData(tData);

        p.setBytesLeft(tData.getTorrentSize());
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
        em.persist(tData);
        em.persist(p);
        em.getTransaction().commit();
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
     * Test of printTorrentList method, of class XMLTorrentList.
     */
    @Test
    public void testPrintTorrentList() {
        System.out.println("printTorrentList");
        Map<String, String[]> requestMap = new TreeMap<String,String[]>();
        StringWriter stringResult = new StringWriter();
        PrintWriter out = new PrintWriter(stringResult);
        StringReader resultReader;
        InputSource is;

        try {
            // no need to populate the request with search-keys and so on,
            // this is covered by the TorrentSearch test. This only needs to
            // test the XML result.
            // create the XML document
            XMLTorrentList instance = new XMLTorrentList();
            instance.printTorrentList(requestMap, out);

            // store the result in a form DocumentBuilder can parse.
            resultReader = new StringReader(stringResult.toString());
            is = new InputSource(resultReader);

            // set-up DOM
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // parse the document
            Document doc = db.parse(is);

            NodeList list = doc.getElementsByTagName("torrent");

            assertTrue("Is the list of torrents equal to 1?", list.getLength() == 1);

            // test an empty set of torrents by searching for a string which
            // does not exist
            String s[] = new String[1];
            s[0] = "abidubada";
            requestMap.put("searchField", s);

            stringResult = new StringWriter();
            out = new PrintWriter(stringResult);

            instance.printTorrentList(requestMap, out);
            resultReader = new StringReader(stringResult.toString());
            is = new InputSource(resultReader);

            doc = db.parse(is);

            list = doc.getElementsByTagName("torrent");

            assertTrue("Is the list of torrents empty?", list.getLength() == 0);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}