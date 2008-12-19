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

import java.io.UnsupportedEncodingException;

/**
 * A class containing some string-utilities used when dealing with info hashes
 * and peer id's.
 * @author bo
 */
public class StringUtils {
    // curtesy of google
    /**
     * lookup table for getHexString
     * @see StringUtils.getHexString(byte[] raw)
     */
    static final byte[] HEX_CHAR_TABLE = {
    (byte)'0', (byte)'1', (byte)'2', (byte)'3',
    (byte)'4', (byte)'5', (byte)'6', (byte)'7',
    (byte)'8', (byte)'9', (byte)'a', (byte)'b',
    (byte)'c', (byte)'d', (byte)'e', (byte)'f'
    };

    /**
     * Converts an array of raw bytes into a String containing the hex-
     * representation of these bytes.
     * @param raw the array of bytes to represent in a string
     * @return a String representing the bytes given in the input in hex.
     * @throws java.io.UnsupportedEncodingException if the target platform does
     * not have the ASCII-charset.
     */
    public static String getHexString(byte[] raw)
        throws UnsupportedEncodingException
    {
        byte[] hex = new byte[2 * raw.length];
        int index = 0;

        for (byte b : raw) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }
        return new String(hex, "ASCII");
    }

    /**
     * URLEncodes a string containing a hex-representation of a number of bytes.
     * @param hexString the string to URL-encode.
     * @return a URL-encoded version of the string given as output.
     */
    public static String URLEncodeFromHexString(String hexString) {
        StringBuffer result = new StringBuffer();

        for(int i = 0; i < hexString.length(); i = i + 2) {
            String substring = hexString.substring(i, i+2);
            // short because everything is signed in java and thus byte is too small
            char c = (char) Short.parseShort(substring, 0x10);

            if((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                // c is within a-z,A-Z,0-9, and therefore safe
                result.append(c);
            }
            else if(c == ' ') {
                result.append('+');
            }
            else {
                switch(c) {
                    case '.':
                    case '-':
                    case '*':
                    case '_':
                        result.append(c);
                    default:
                        result.append('%');
                        result.append(substring.toUpperCase());
                }
            }
        }

        return result.toString();
    }
}