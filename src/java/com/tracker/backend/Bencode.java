/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * <p>Handles encoding and decoding of Strings, Integers, Lists and Maps
 * (dictionaries) from/into their respective Bencode variants
 * through static methods. Does not particularly require instantiation.
 * @author bo
 */
public class Bencode {
    public Bencode() {
        
    }
    
    /**
     * Convenience method for bencoding different types of contents.
     * @param arg the general Object to bencode.
     * @return returns a String with the bencoded data if the object can be
     * encoded (ie, is one of String, Integer, Long, List or Map.) Throws an
     * exception if the object cannot be bencoded.
     * @throws java.lang.Exception if the argument is not an instance of
     * either String, Integer, Long, List or Map.
     */
    private static String encode(Object arg) throws Exception {      
        if(java.lang.String.class.isInstance(arg)) {
            return encode((String)arg);
        }
        else if(java.lang.Integer.class.isInstance(arg)) {
            return encode((Integer)arg);
        }
        else if(java.lang.Long.class.isInstance(arg)) {
            return encode((Long)arg);
        }
        else if(java.util.List.class.isInstance(arg)) {
            return encode((List)arg);
        }
        else if(java.util.Map.class.isInstance(arg)) {
            return encode((Map)arg);
        }
        else
            throw new Exception("Object class not supported. " + arg.getClass().getName());
    }
    /**
     * Bencodes a string.
     * @param arg the string to be bencoded
     * @return a string containing the length of the argument given, followed
     * by a colon and then the original argument arg. For example, the bencoded
     * version of "cat" is "3:cat".
     */
    public static String encode(String arg) {
        return(arg.length() + ":" + arg);
    }
    
    /**
     * Bencodes an Integer
     * @param arg the Integer to be bencoded
     * @return a string containing the bencoded integer given in arg,
     * represented by an 'i' followed by the number in base 10 and finished with
     * an 'e'. Does not return leading zeroes or i-0e.
     */
    public static String encode(Integer arg) {
        return("i" + Integer.toString(arg) + "e");
    }

    /**
     * Bencodes a Long (as integer, funny enough)
     * @param arg the Long to be bencoded
     * @return a string containing the bencoded Long given in arg
     */
    public static String encode(Long arg) {
        return("i" + Long.toString(arg) + "e");
    }
    
    /**
     * Bencodes a List
     * @param arg the List to bencode
     * @return a string containing the bencoded list if the list contains
     * only objects that can be bencoded
     * @throws java.lang.Exception when an object in the list cannot be bencoded
     */
    public static String encode(List arg) throws Exception {
        Iterator i = arg.iterator();
        String result = "l";
        
        while(i.hasNext()) {
            try {
                Object o = i.next();
                result += encode(o);
            }
            catch(Exception ex) {
                throw new Exception("Exception caught when encoding a List", ex);
            }
        }
        result += "e";
        
        return(result);
    }
    
    /**
     * Bencodes a map as a dictionary.
     * @param arg the map to bencode.
     * @return a string containing the bencoded dictionary if the map only
     * contains elements which can be bencoded.
     * @throws java.lang.Exception if an element of the map cannot be bencoded
     * or if the keys does not implement Comparable.
     */
    public static String encode(Map arg) throws Exception {
        String result = "d";
        
        try {
            // keys must be in sorted order
            // this can fail if the keys does not have Comparable, hence the
            // try-catch
            TreeMap sortedMap = new TreeMap(arg);
            Iterator i = sortedMap.entrySet().iterator();
            
            while(i.hasNext()) {
                Map.Entry pairs = (Map.Entry)i.next();
                // keys must be strings
                if(!(java.lang.String.class.isInstance(pairs.getKey()))) {
                    throw new Exception("Keys given in dictionary was not a" +
                            "string." + pairs.getKey().getClass().getName());
                }
                result += encode((String)pairs.getKey());
                result += encode(pairs.getValue());    
            }
        }
        catch(Exception ex) {
            throw new Exception("Exception caught when creating sorted Map", ex);
        }
        result += "e";
        
        return(result);
    }

    /**
     * Decodes a bencoded InputStream into its constituent parts
     * @param stream the InputStream to decode
     * @return a Map containing indexes of the different values in the list, and
     * the decoded values generated from the InputStream.
     * @throws java.lang.Exception if the InputStream contains malformed bencoded
     * data or throws an I/O error.
     */
    public static Map decode(InputStream stream) throws Exception {
        TreeMap result = new TreeMap();
        int readByte;
        char charByte;
        int index = 0;
        try {
            // read the whole stream until the end or an error occurs
            readByte = stream.read();
            charByte = (char) readByte;
            while(readByte != -1) {
                switch(charByte) {
                    case 'd':
                        result.put(index, decodeDictionary(stream));
                        break;
                    case 'l':
                        result.put(index, decodeList(stream));
                        break;
                    case 'i':
                        result.put(index, decodeInteger(stream));
                        break;
                    default:
                        if(Character.isDigit(charByte)) {
                            result.put(index, decodeString(charByte, stream));
                        }
                        else {
                            throw new Exception(Character.toString(charByte)
                                        + " found when expecting 'i', 'l', 'd'" +
                                        "or a string-length?");
                        }
                        break;
                }
                readByte = stream.read();
                charByte = (char) readByte;
            }
        }
        catch(Exception ex) {
            throw new Exception("Error when decoding bencoded input stream", ex);
        }

        return result;
    }

    /**
     * Decodes a bencoded dictionary into a java.util.Map object.
     * @param stream the input stream containing the bencoded dictionary.
     * @return a Map containing the decoded contents of the bencoded dictionary.
     * @throws java.lang.Exception if the dictionary ended unexpectedly, any key
     * of the dictionary was not a string or the contents of the dictionary are
     * malformed.
     */
    private static Map decodeDictionary(InputStream stream) throws Exception {
        TreeMap result = new TreeMap();
        int readByte;
        char charByte;

        try {
            readByte = stream.read();
            charByte = (char) readByte;

            // parse the dictionary
            while(charByte != 'e') {
                // check if the stream ended prematurely
                if(readByte == -1) {
                    throw new Exception("Dictionary ended prematurely?");
                }

                // parse the key
                // keys must be bencoded strings, strings starts with an
                // identifier specifying length
                String key;
                Object value;
                if(Character.isDigit(charByte)) {
                    key = decodeString(charByte, stream);
                }
                else {
                    // key not a string?
                    throw new Exception("Key in bencoded dictionary not a string?");
                }

                // read the value
                if((readByte = stream.read()) != -1) {
                    charByte = (char) readByte;
                    switch(charByte) {
                        case 'i':
                            value = decodeInteger(stream);
                            break;
                        case 'l':
                            value = decodeList(stream);
                            break;
                        case 'd':
                            value = decodeDictionary(stream);
                            break;
                        default:
                            if(Character.isDigit(charByte)) {
                                value = decodeString(charByte, stream);
                            }
                            else {
                                // unknown content/broken dictionary
                                throw new Exception(Character.toString(charByte)
                                        + " found when expecting 'i', 'l', 'd'" +
                                        "or a string-length?");
                            }
                            break;
                    }
                }
                else {
                    // missing a value from the dictionary? Something is amiss.
                    throw new Exception("Missing a value in bencoded dictionary?");
                }

                // add key/value pair to result
                result.put(key, value);
                // read next iterations key
                readByte = stream.read();
                charByte = (char) readByte;
            } // while
        }

        catch(Exception ex) {
            throw new Exception("Error when decoding bencoded dictionary", ex);
        }

        return result;
    }

    /**
     * decodes a bencoded list into a java.util.List object.
     * @param stream the InputStream to read the list from.
     * @return a List representing the bencoded list in the input stream
     * @throws java.lang.Exception if the list ends unexpectedly, or if the
     * content is malformed.
     */
    private static List decodeList(InputStream stream) throws Exception {
        List result = new Vector();
        int readByte;
        char charByte;

        try {
            readByte = stream.read();
            charByte = (char) readByte;
            while(charByte != 'e') {
                // check if the list ended prematurely
                if(readByte == -1) {
                    throw new Exception("List ended prematurely?");
                }

                switch(charByte) {
                    case 'i':
                        result.add(decodeInteger(stream));
                        break;
                    case 'l':
                        result.add(decodeList(stream));
                        break;
                    case 'd':
                        result.add(decodeDictionary(stream));
                        break;
                    default:
                        if(Character.isDigit(charByte)) {
                            result.add(decodeString(charByte, stream));
                        }
                        else {
                            // unknown content/borked list
                            throw new Exception(Character.toString(charByte)
                                        + " found when expecting 'i', 'l', 'd'" +
                                        "or a string-length?");
                        }
                        break;
                }
                readByte = stream.read();
                charByte = (char) readByte;
            }
        }
        catch(Exception ex) {
            throw new Exception("Error when decoding bencoded list", ex);
        }

        return result;
    }

    /**
     * Decodes a bencoded integer into a java.lang.Long object.
     * @param stream the input stream to decode the integer from.
     * @return a Long representing the bencoded integer in the input stream.
     * @throws java.lang.Exception if the Integer ended unexpectedly, contains
     * non-number data or due to problems converting to Long.
     */
    private static Long decodeInteger(InputStream stream) throws Exception {
        Long result = new Long(0);
        StringBuilder stringResult = new StringBuilder();
        int readByte;
        char charByte;

        try {
            readByte = stream.read();
            charByte = (char) readByte;
            while(charByte != 'e') {
                // check if the integer ended before 'e'
                if(readByte == -1) {
                    throw new Exception("Integer ended prematurely?");
                }
                // check for non-digits
                if(!Character.isDigit(charByte)) {
                    throw new Exception("Non-number data in a bencoded integer?");
                }
                stringResult.append(charByte);

                // read the next byte
                readByte = stream.read();
                charByte = (char) readByte;
            }

            // convert to Long, throws exception if the integer to too big or
            // malformed
            result = Long.parseLong(stringResult.toString());
        }
        catch(Exception ex) {
            throw new Exception("Error when decoding bencoded integer", ex);
        }

        return result;
    }

    /**
     * Decodes a bencoded string into a java.lang.String object.
     * @param firstLengthDigit the first digit of the length parameter, used
     * since we cannot easily rewind the input stream, and marking may not be
     * available (I haven't tested this)
     * @param stream the input stream to parse the string from
     * @return a String representing the bencoded string given in the input stream
     * @throws java.lang.Exception
     */
    private static String decodeString(char firstLengthDigit, InputStream stream) throws Exception {
        // since we cannot rewind the input stream, we need to get the first
        // digit of the length as a parameter.
        StringBuilder lengthString = new StringBuilder();
        lengthString.append(firstLengthDigit);

        StringBuilder result = new StringBuilder();

        try {
            // get the rest of the length
            int readByte = stream.read();
            char charByte = (char) readByte;
            while(charByte != ':') {
                // premature end of stream?
                if(readByte == -1) {
                    throw new Exception("String-length ended prematurely?");
                }
                lengthString.append(charByte);
                readByte = stream.read();
                charByte = (char) readByte;
            }
            // parse length
            int length = Integer.parseInt(lengthString.toString());

            // read the string
            for(int i = 0; i < length; i++) {
                readByte = stream.read();
                charByte = (char) readByte;
                if(readByte == -1) {
                    throw new Exception("String ended prematurely?");
                }
                result.append(charByte);
            }
        }
        catch(Exception ex) {
            throw new Exception("Error when decoding bencoded string", ex);
        }

        return result.toString();
    }
}
