/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.frontend;

import com.tracker.backend.Bencode;
import com.tracker.backend.TrackerRequestParser;
import java.io.IOException;
import java.io.PrintWriter;
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
public class Scrape extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        TrackerRequestParser trp = new TrackerRequestParser();
        Enumeration e = request.getAttributeNames();
        TreeMap<String,TreeMap> innerDictionary = new TreeMap<String,TreeMap>();
        String responseString = new String();

        try {
            while(e.hasMoreElements()) {
                String name = (String) e.nextElement();
                String value = request.getAttribute(name).toString();
                if(name.equalsIgnoreCase("info_hash")) {
                    innerDictionary.put(value, trp.scrape(value));
                }
            }

            // no info_hashes present?
            if(innerDictionary.isEmpty()) {
                // scrape all torrents
                innerDictionary = trp.scrape();
            }

            TreeMap<String,TreeMap> outerDictionary = new TreeMap();
            outerDictionary.put((String)"files", innerDictionary);

            responseString = Bencode.encode(outerDictionary);
        } catch(Exception ex) {
            Logger.getLogger(Scrape.class.getName()).log(Level.SEVERE,
                    "Exception caught", ex);
        }

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        try {
            out.print(responseString);
        } finally { 
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

}
