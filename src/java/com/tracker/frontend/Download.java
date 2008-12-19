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

import com.tracker.backend.entity.Torrent;
import com.tracker.backend.webinterface.entity.TorrentData;
import com.tracker.backend.webinterface.entity.TorrentFile;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Makes the torrentfiles available for download. Uses the id-attribute to
 * find the given torrentfile.
 * @author bo
 */
public class Download extends HttpServlet {
    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        EntityManager em = emf.createEntityManager();
        Query q;

        Torrent t;
        TorrentFile file;
        TorrentData data;

        String fileName;
        String id = null;

        try {
            // ServletOutputStream for binary data, see javadoc for getOutputStream().
            ServletOutputStream out = response.getOutputStream();
            // grab the id Parameter.
            id = request.getParameter("id");
            
            // sanitise the input. By making sure the input does not contain the
            // '-character we can avoid SQL-injection.
            id = id.replaceAll("'", "");
            
            // try to look up this id in the database.
            q = em.createQuery("SELECT t FROM Torrent t WHERE t.id = '" + id + "'");
            t = (Torrent) q.getSingleResult();
            file = t.getTorrentFile();
            data = t.getTorrentData();

            response.setContentType("application/x-bittorrent");
            response.setContentLength(file.getFileLength());
            // construct the filename from the torrentname, with some regexp
            // tossed in to make sure it is valid.
            // TODO: does all browsers handle UTF-8 in the Content-Disposition
            // header?
            fileName = data.getName();
            // remove all tags
            fileName = fileName.replaceAll("<.*>", "");
            // remove everything that is not suitable for filenames and replace
            // with '_'
            fileName = fileName.replaceAll("[^\\w\\(\\)\\_\\[\\]\\.\\-\\:\\;] ", "_");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".torrent\"");

            // finally write the torrentfile.
            file.writeTorrentFile(out);

            out.flush();
            out.close();
        }
        catch(Exception ex) {
            // print an error message
            PrintWriter out = response.getWriter();
            response.setContentType("text/html;charset=UTF-8");
            response.setHeader("Content-Disposition", "");
            out.println("<html>");
            out.println("<head><title>Error</title><head>");
            out.println("<body><h1>Torrent (ID: " + id + ") not found.</h1></body>");
            out.println("</html>");
            out.close();

            Logger.getLogger(Download.class.getName()).log(Level.WARNING,
                    "Exception caught in Download, id provided is " + id + ".", ex);
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
