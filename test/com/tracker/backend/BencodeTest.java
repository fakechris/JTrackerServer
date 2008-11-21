/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
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
public class BencodeTest {

    public BencodeTest() {
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
     * Test of encode method, of class Bencode.
     */
    @Test
    public void testEncode_String() {
        System.out.println("encode(String arg)");
        String arg = "spam";
        String expResult = "4:spam";
        String result = Bencode.encode(arg);
        assertEquals(expResult, result);
    }

    /**
     * Test of encode method, of class Bencode.
     */
    @Test
    public void testEncode_Integer() {
        System.out.println("encode(Integer arg)");
        Integer intArg = -1;
        Long longArg = -1L;
        String expResult = "i-1e";
        String result = Bencode.encode(intArg);
        assertEquals(expResult, result);
        result = Bencode.encode(longArg);
        assertEquals(expResult, result);
    }

    /**
     * Test of encode method, of class Bencode.
     */
    @Test
    public void testEncode_List() throws Exception {
        System.out.println("encode(List arg)");
        List arg = new Vector();
        arg.add((String)"spam");
        arg.add((String)"egg");
        arg.add((Integer)(-1));
        
        Vector v = new Vector();
        v.add((String)"nested list");
        v.add((Integer)2);
        
        arg.add((List)v);
        
        String expResult = "l4:spam3:eggi-1el11:nested listi2eee";
        String result = Bencode.encode(arg);
        assertEquals(expResult, result);
    }

    /**
     * Test of encode method, of class Bencode.
     */
    @Test
    public void testEncode_Map() throws Exception {
        System.out.println("encode(Map arg)");
        Map arg = new HashMap();
        
        arg.put((String)"eggs", (String)"spam");
        arg.put((String)"one", (Integer)1);
        Vector v = new Vector();
        v.add((String)"vector");
        v.add((Integer)2);
        arg.put((String)"list", (Vector)v);
        
        String expResult = "d4:eggs4:spam4:listl6:vectori2ee3:onei1ee";
        String result = Bencode.encode(arg);
        assertEquals(expResult, result);
    }

    /**
     * Test of decode method, of class Bencode.
     */
    @Test
    public void testDecode() throws Exception {
        System.out.println("decode");
        InputStream stream = null;
        Map expResult = null;
        //Map result = Bencode.decode(stream);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}