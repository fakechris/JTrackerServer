/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.webinterface.xml;

import com.tracker.backend.webinterface.TakeUpload;
import com.tracker.backend.webinterface.TorrentUpload;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public class XMLTakeUpload extends HttpServlet implements TakeUpload {

    Logger log = Logger.getLogger(XMLTakeUpload.class.getName());
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("application/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            parseUpload(request, out);
        }
        catch(Exception ex) {
            StringWriter s = new StringWriter();
            s.append("Exception Caught: ");
            s.append(ex.toString());
            s.append(" ");
            s.append(ex.getMessage());
            PrintWriter w = new PrintWriter(s);
            ex.printStackTrace(w);
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<errorReason>Internal error:" + s.toString() + "</errorReason>");
        }
        finally {
            out.close();
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    public void parseUpload(HttpServletRequest request, PrintWriter out) throws Exception {
        Map<String, String> response;

        try {
            // split the request.
            TorrentUpload upload = new TorrentUpload();
            TorrentUpload.UnparsedTorrentData data = upload.getDataFromRequest(request);
            
            // reconstruct the URL of the application, used to check against
            // announce entries in the addTorrent method. We use this method
            // instead of request.getRequestURL because it is simpler than
            // having to remove all the /TakeUpload bits.
            
            String scheme = request.getScheme();             // http
            String serverName = request.getServerName();     // hostname.com
            int serverPort = request.getServerPort();        // 8080, if 80, disregard
            String contextPath = request.getContextPath();   // /mywebapp
            String URL = scheme + "://" + serverName;
            // disregard the port if it's 80, no need to add it
            // TODO: accept :80 in announce also?
            if(serverPort != 80) {
                URL += ":" + serverPort;
            }
            URL += contextPath;
            // parse the request
            response = TorrentUpload.addTorrent(data.decodedTorrent, data.name,
                    data.description, URL);

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
             * <errorReason>
             * errors go here
             * </errorReason>
             * <warningReason>
             * warnings go here
             * </warningReason>
             * <redownload>
             * true/false
             * </redownload>
             */
            String errorReason = response.get("error reason");
            String warningReason = response.get("warning reason");
            String redownload = response.get("redownload");

            tHandler.startElement("", "", "errorReason", attr);
            tHandler.characters(errorReason.toCharArray(), 0, errorReason.length());
            tHandler.endElement("", "", "errorReason");

            tHandler.startElement("", "", "warningReason", attr);
            tHandler.characters(warningReason.toCharArray(), 0, warningReason.length());
            tHandler.endElement("", "", "warningReason");

            tHandler.startElement("", "", "redownload", attr);
            tHandler.characters(redownload.toCharArray(), 0, redownload.length());
            tHandler.endElement("", "", "redownload");

        }
        catch (SAXException ex) {
            log.log(Level.SEVERE, "SAX Exception caught", ex);
        } catch (TransformerConfigurationException ex) {
            log.log(Level.SEVERE, "Cannot configure XML transformer", ex);
        }
        catch(Exception ex) {
            log.log(Level.SEVERE,
                    "Exception caught when trying to parse upload request", ex);
            throw new Exception("Cannot parse upload request.", ex);
        }
    }
}
