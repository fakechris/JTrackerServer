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

package com.tracker.frontend;

import com.tracker.backend.Bencode;
import com.tracker.backend.TrackerRequestParser;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.HashMap;
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
        HashMap<String, ?> responseParams;
        
        trp.setRemoteAddress(remoteAddress);
        trp.setRequestParams(request.getParameterMap());
        try {
            // these can potentially return null, set some sensible defaults if they do
            if(getInitParameter("minInterval") == null)
                trp.setMinInterval(180L);
            else
                trp.setMinInterval(Long.parseLong(getInitParameter("minInterval")));

            if(getInitParameter("defaultInterval") == null)
                trp.setDefaultInterval(300L);
            else
                trp.setDefaultInterval(Long.parseLong(getInitParameter("defaultInterval")));
        }
        catch(Exception ex) {
            trp.setMinInterval(180L);
            trp.setDefaultInterval(300L);
        }

        String responseString = "";

        try {
            responseParams = trp.parseRequest();

            // bencode the response params to a dictionary, send it

            responseString = Bencode.encode(responseParams);
        } catch (Exception ex) {
            Logger.getLogger(Announce.class.getName()).log(Level.SEVERE,
                    "Exception caught", ex);
            // set a simple failure reason response
            HashMap<String,String> failureResponse = new HashMap<String,String>();
            failureResponse.put("failure reason", "Internal tracker error.");

            try {
                responseString = Bencode.encode(failureResponse);
            } catch (Exception ex1) {
                Logger.getLogger(Announce.class.getName()).log(Level.SEVERE,
                        "Error when constructing failure response?", ex1);
            }
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
