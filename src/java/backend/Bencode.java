/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package backend;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>Handles bencoding of Strings, Integers, Lists and Maps (dictionaries)
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
     * encoded (ie, is one of String, Integer, List or Map.) Throws an exception
     * if the object cannot be bencoded.
     * @throws java.lang.Exception if the argument is not an instance of
     * either String, Integer, List or Map.
     */
    private static String encode(Object arg) throws Exception {      
        if(java.lang.String.class.isInstance(arg)) {
            return encode((String)arg);
        }
        else if(java.lang.Integer.class.isInstance(arg)) {
            return encode((Integer)arg);
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
     * @throws java.lang.Exception if an element of the map cannot be bencoded.
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
}
