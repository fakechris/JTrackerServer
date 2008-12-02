/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.webinterface;

import java.io.InputStream;
import java.util.TreeMap;
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
public class TorrentUploadTest {

    public TorrentUploadTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /*
     * We don't test the getDataFromRequest method simply because it's too much
     * of a hassle to fix up a multipart HttpServletRequest mock-object compared
     * to the work the method does. We instead hope that the FileUpload guys
     * have tested their stuff thoroughly (famous last words).
     */

    /**
     * Test of addTorrent method, of class TorrentUpload.
     */
    @Test
    public void testAddTorrent() throws Exception {
        System.out.println("addTorrent");
        InputStream torrent = null;
        String torrentName = "";
        String torrentDescription = "";
        String contextPath = "";
        /*TreeMap<String, String> expResult = null;
        TreeMap<String, String> result = TorrentUpload.addTorrent(torrent, torrentName, torrentDescription, contextPath);
        assertEquals(expResult, result);*/
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}