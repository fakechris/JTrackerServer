/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.webinterface.json;

import java.io.PrintWriter;
import java.util.Map;
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

    public JSONTorrentListTest() {
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

    /**
     * Test of printTorrentList method, of class JSONTorrentList.
     */
    @Test
    public void testPrintTorrentList() {
        System.out.println("printTorrentList");
        Map<String, String[]> requestMap = null;
        PrintWriter out = null;
        JSONTorrentList instance = new JSONTorrentList();
        instance.printTorrentList(requestMap, out);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}