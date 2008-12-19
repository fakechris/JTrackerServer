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
public class StringUtilsTest {

    public StringUtilsTest() {
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
     * Test of getHexString method, of class StringUtils.
     */
    @Test
    public void testGetHexString() throws Exception {
        System.out.println("getHexString");
        byte[] raw = new byte[20];
        short[] contents = { 31, -127, 118, -65, -8, -62, -63, -73, 107, -8, -71, 109, -56, -90, -4, -21, -44, -46, -39, -43, };
        for(int i = 0; i < contents.length; i++) {
            raw[i] = (byte) contents[i];
        }
        String expResult = "1f8176bff8c2c1b76bf8b96dc8a6fcebd4d2d9d5";
        String result = StringUtils.getHexString(raw);
        assertEquals(expResult, result);
    }

    /**
     * Test of URLEncodeFromHexString method, of class StringUtils.
     */
    @Test
    public void testURLEncodeFromHexString() {
        System.out.println("URLEncodeFromHexString");
        String hexString = "1f8176bff8c2c1b76bf8b96dc8a6fcebd4d2d9d5";
        String expResult = "%1F%81v%BF%F8%C2%C1%B7k%F8%B9m%C8%A6%FC%EB%D4%D2%D9%D5";
        String result = StringUtils.URLEncodeFromHexString(hexString);
        assertEquals(expResult, result);
    }

}