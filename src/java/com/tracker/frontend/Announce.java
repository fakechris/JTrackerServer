/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.frontend;

import com.tracker.backend.Bencode;
import com.tracker.backend.TrackerRequestParser;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author bo
 */
public class Announce extends HttpServlet {    
    /** 
     * the address the request originated from, used 
     */
    private InetAddress remoteAddress;
   
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException  {
        // store remote address in a useful form
        remoteAddress = InetAddress.getByName(request.getRemoteAddr());
        
        // parse request
        TrackerRequestParser trp = new TrackerRequestParser();
        Enumeration e = request.getParameterNames();
        TreeMap<String, String> requestParams = new TreeMap<String,String>();
        TreeMap<String, ?> responseParams;
        
        while(e.hasMoreElements()) {
            String name = (String)e.nextElement();
            String value = request.getParameter(name).toString();
            requestParams.put(name, value);
        }
        
        trp.setRemoteAddress(remoteAddress);
        trp.setRequestParams(requestParams);

        String responseString = new String();

        try {
            responseParams = trp.parseRequest();

            // bencode the response params to a dictionary, send it

            responseString = Bencode.encode(responseParams);
        } catch (Exception ex) {
            Logger.getLogger(Announce.class.getName()).log(Level.SEVERE,
                    "Exception caught", ex);
        }
        
        response.setContentType("text/plain");
        // the peers key will contain binary data, so we want to avoid the
        // inevitable encoding the getWriter() PrintWriter performs at the
        // Servlet-Container level.
        OutputStream out = response.getOutputStream();
        try {
            for(int i = 0; i < responseString.length(); i++) {
                out.write(responseString.charAt(i));
            }
        } finally { 
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
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
    */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
    * Returns a short description of the servlet.
    */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
