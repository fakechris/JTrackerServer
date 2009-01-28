/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.webinterface.json;

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
import org.json.JSONWriter;

/**
 *
 * @author bo
 */
public class JSONTakeUpload extends HttpServlet implements TakeUpload {
    Logger log = Logger.getLogger(JSONTakeUpload.class.getName());
 
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
            out.println("{ \"errorReason\": \"" + s.toString() + "\" }");
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

    @Override
    public void parseUpload(HttpServletRequest request, PrintWriter out) throws Exception {
        Map<String, String> response;
        try {
            // split the request.
            TorrentUpload upload = new TorrentUpload();
            TorrentUpload.UnparsedTorrentData data = upload.getDataFromRequest(request);

            // get the URL of the webapp
            String URL = TorrentUpload.getURL(request);

            // parse the request
            response = TorrentUpload.addTorrent(data.decodedTorrent, data.name,
                    data.description, URL);

            // set up JSON
            JSONWriter json = new JSONWriter(out);

            // add results to JSON
            /*
             * JSON looks like this:
             * {
             *  "errorReason": errors go here,
             *  "warningReason": warnings go here,
             *  "redownload": true/false
             * }
             */
            String errorReason = response.get("error reason");
            String warningReason = response.get("warning reason");
            String redownload = response.get("redownload");

            json.object();

            json.key("errorReason").value(errorReason);
            json.key("warningReason").value(warningReason);
            json.key("redownload").value(redownload);

            json.endObject();
        }
        catch(Exception ex) {
            log.log(Level.SEVERE,
                    "Exception caught when trying to parse upload request", ex);
            throw new Exception("Cannot parse upload request.", ex);
        }
    }
}
