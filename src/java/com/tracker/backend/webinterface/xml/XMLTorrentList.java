/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.webinterface.xml;

import com.tracker.backend.webinterface.TorrentList;
import com.tracker.backend.webinterface.TorrentSearch;
import com.tracker.entity.Torrent;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author bo
 */
public class XMLTorrentList implements TorrentList {
    
    Logger log = Logger.getLogger(XMLTorrentList.class.getName());

    public void printTorrentList(Map<String, String[]> requestMap, PrintWriter out) {
        try {
            // query result
            Vector<Torrent> result;
            Iterator itr;

            boolean searchDescriptions = false;
            boolean includeDead = false;

            // do we search for anything?
            if(requestMap.containsKey((String)"searchField")) {
                // search for the search string given
                // (there is only one object of this anyway)
                String searchString = requestMap.get((String)"searchField")[0];

                // do we search descriptions?
                if(requestMap.containsKey((String)"searchDescriptions")) {
                    String value = requestMap.get((String)"searchDescriptions")[0];
                    if(value.equalsIgnoreCase("checked")) {
                        searchDescriptions = true;
                    }
                }

                // do we search dead torrents?
                if(requestMap.containsKey((String)"includeDead")) {
                    String value = requestMap.get((String)"includeDead")[0];
                    if(value.equalsIgnoreCase("checked")) {
                        includeDead = true;
                    }
                }

                result = TorrentSearch.getList(searchString, searchDescriptions, includeDead);
            }
            // no search string given
            else {
                result = TorrentSearch.getList();
            }

            // Set-up JAXP + SAX
            StreamResult streamResult = new StreamResult(out);
            SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();

            // SAX2.0 ContentHandler.
            TransformerHandler tHandler = tf.newTransformerHandler();
            Transformer serializer = tHandler.getTransformer();

            // set encoding and indenting
            serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
            serializer.setOutputProperty(OutputKeys.INDENT,"yes");

            tHandler.setResult(streamResult);

            // document start
            tHandler.startDocument();
            AttributesImpl attr = new AttributesImpl();

            // add results to XML
            /*
             * markup looks like this:
             * - torrents
             *   - torrent [id]
             *     - name
             *     - num seeders
             *     - num leechers
             *     - num completed
             *     - (data moved?)
             *     - date added
             *   - end torrent
             * - end torrents
             */
            
            tHandler.startElement("", "", "torrents", attr);

            itr = result.iterator();
            while(itr.hasNext()) {
                Torrent t = (Torrent) itr.next();

                attr.clear();
                attr.addAttribute("", "", "id", "", t.getId().toString());
                tHandler.startElement("", "", "torrent", attr);

                attr.clear();

                tHandler.startElement("", "", "name", attr);
                tHandler.characters(t.getName().toCharArray(), 0, t.getName().length());
                tHandler.endElement("", "", "name");

                // don't include the description for the browse list, only
                // for details
                /*tHandler.startElement("", "", "description", attr);
                tHandler.characters(t.getDescription().toCharArray(), 0, t.getDescription().length());
                tHandler.endElement("", "", "description");*/

                tHandler.startElement("", "", "numSeeders", attr);
                tHandler.characters(t.getNumSeeders().toString().toCharArray(),
                        0, t.getNumSeeders().toString().length());
                tHandler.endElement("", "", "numSeeders");

                tHandler.startElement("", "", "numLeechers", attr);
                tHandler.characters(t.getNumLeechers().toString().toCharArray(),
                        0, t.getNumLeechers().toString().length());
                tHandler.endElement("", "", "numLeechers");

                tHandler.startElement("", "", "numCompleted", attr);
                tHandler.characters(t.getNumCompleted().toString().toCharArray(),
                        0, t.getNumCompleted().toString().length());
                tHandler.endElement("", "", "numCompleted");

                // data moved?

                tHandler.startElement("", "", "dateAdded", attr);
                tHandler.characters(t.getAdded().toString().toCharArray(),
                        0, t.getAdded().toString().length());
                tHandler.endElement("", "", "dateAdded");

                tHandler.endElement("", "", "torrent");
            }

            tHandler.endElement("", "", "torrents");
        } catch (SAXException ex) {
            log.log(Level.SEVERE, "SAX Exception caught", ex);
        } catch (TransformerConfigurationException ex) {
            log.log(Level.SEVERE, "Cannot configure XML transformer", ex);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Exception caught when trying to form XML", ex);
        }
    }
}
