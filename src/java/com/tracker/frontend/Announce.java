/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.frontend;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.TreeMap;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author bo
 */
public class Announce extends HttpServlet {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");
    
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
        Enumeration e = request.getAttributeNames();
        TreeMap requestParams = new TreeMap();
        TreeMap responseParams = new TreeMap();
        
        while(e.hasMoreElements()) {
            String name = (String)e.nextElement();
            String value = request.getAttribute(name).toString();
            requestParams.put(name, value);
        }
        
        
        //responseParams = createResponse(requestParams);
        
        // bencode the response params to a dictionary, send it
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        try {
            /* TODO output your page here
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Announce</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Announce at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>"); 
            */
            
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Annouce test</title");
            out.println("</head>");
            out.println("<body>");
            
            out.println("Remote address: " + remoteAddress.toString() + "<br>");
  
            out.print("Value from POST: ");
            out.print(request.getParameter("post_dataname"));
            out.print(":");
            out.print(request.getParameter("post_datavalue"));
            out.println();

            out.print("Value from GET: ");
            out.print(request.getParameter("get_dataname"));
            out.print(":");
            out.print(request.getParameter("get_datavalue"));
            out.println();
            
            out.println("<P>POST based form:<br>");
            out.print("<form action=\"");
            out.print(response.encodeURL("Announce"));
            out.print("\" ");
            out.println("method=POST>");
            out.println("Test 1 (POST) Name");
            out.println("<input type=text size=20 name=post_dataname>");
            out.println("<br>");
            out.println("Test 1 (POST) Value");
            out.println("<input type=text size=20 name=post_datavalue>");
            out.println("<br>");
            out.println("<input type=submit>");
            out.println("</form>");

            out.println("<P>GET based form:<br>");
            out.print("<form action=\"");
            out.print(response.encodeURL("Announce"));
            out.print("\" ");
            out.println("method=GET>");
            out.println("Test 2 (GET) Name");
            out.println("<input type=text size=20 name=get_dataname>");
            out.println("<br>");
            out.println("Test 2 (POST) Value");
            out.println("<input type=text size=20 name=get_datavalue>");
            out.println("<br>");
            out.println("<input type=submit>");
            out.println("</form>");
            
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

    private TreeMap createResponse(TreeMap requestParams) {
        EntityManager em = emf.createEntityManager();
        TreeMap responseParams = new TreeMap();
        String infoHash, peerId, event;
        Long uploaded, downloaded, left;
        Integer numPeersToReturn = new Integer(50);
        Integer port;
        com.tracker.entity.Torrent t;
        com.tracker.entity.Peer p;
        
        /*
         * Check for mandatory fields
         */
        // check for info hash
        if(!requestParams.containsKey((String)"info_hash")) {
            return(parseFailed("missing info hash!"));
        }
        
        // decode and store info hash for later
        try {
            infoHash = URLDecoder.decode(
                    (String) requestParams.get((String)"info_hash"), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Announce.class.getName()).log(Level.SEVERE, 
                    "could not decode info hash", ex);
            return(parseFailed("Tracker error"));
        }

        // check for peer id
        if(!requestParams.containsKey((String)"peer id")) {
            return(parseFailed("missing peer id!"));
        }
        
        peerId = (String)requestParams.get((String)"peer id");
        
        // check for port-number
        if(!requestParams.containsKey((String)"port")) {
            return(parseFailed("missing port number!"));
        }
        
        port = Integer.getInteger((String)requestParams.get((String)"port"));
        
        // check for uploaded
        if(!requestParams.containsKey((String)"uploaded")) {
            return(parseFailed("missing 'uploaded' field!"));
        }
        
        uploaded = Long.getLong((String)requestParams.get((String)"uploaded"));
        
        // check for downloaded
        if(!requestParams.containsKey((String)"downloaded")) {
            return(parseFailed("missing 'downloaded' field!"));
        }
        
        downloaded = Long.getLong((String)requestParams.get((String)"downloaded"));
        
        // check for left
        if(!requestParams.containsKey((String)"left")) {
            return(parseFailed("missing 'left' field!"));
        }
        
        left = Long.getLong((String)requestParams.get((String)"left"));
        
        /*
         * Check for optional fields
         */
        // check for 'compact'
        if(requestParams.containsKey((String)"compact")) {
            if(requestParams.get((String)"compact") == "0") {
                return(parseFailed("this tracker only supports compact responses"));
            }
        }
        
        // check for numwant
        if(requestParams.containsKey((String)"numwant")) {
            Integer numWant = (Integer)requestParams.get((String)"numwant");
            // never give more than 50, less than 0 is not valid
            if(numWant < numPeersToReturn && numWant >= 0) {
                // honour numwant from the client
                numPeersToReturn = numWant;
            }
        }
        
        /* 
         * ignored optional keys:
         * - no_peer_id (only compact responses)
         * - ip (no checks implemented)
         * - key (no need for reliable identification)
         * - trackerid (samesame)
         */
        
        // find torrent in database of tracked torrents
        try {
            t = em.find(com.tracker.entity.Torrent.class, infoHash.getBytes());

            if(t == null) {
                return(parseFailed("Torrent not tracked."));
            }
        } catch(Exception ex) {
            Logger.getLogger(Announce.class.getName()).log(Level.SEVERE, 
                    "error when looking for torrent in database", ex);
            return(parseFailed("Tracker error."));
        }
        
        /*
         * check for event-key
         * the first announce to the tracker from a new peer must have the
         * event = started key/value pair.
         */
        if(requestParams.containsKey((String)"event")) {
            // client just started the download
            if(requestParams.get((String)"event") == "started") {
                // add new peer
                p = new com.tracker.entity.Peer();
                
                p.setBytesLeft(left);
                p.setDownloaded(downloaded);
                p.setUploaded(uploaded);

                Byte[] b = new Byte[2];
                // lovely binary logic
                b[0] = (byte)((port >> 8) & 0xFF);
                b[1] = (byte)(port & 0xFF);

                p.setPort(b);

                // set address
                // convert to Byte[]
                byte[] primitiveArray = remoteAddress.getAddress();
                Byte[] objectArray = new Byte[primitiveArray.length];
                for(int i = 0; i < primitiveArray.length; i++) {
                    objectArray[i] = Byte.valueOf(primitiveArray[i]);
                }

                p.setIp(objectArray);
                
                // if left = 0, this peer has a complete copy, so add as seed,
                // if not, add as leech
                if(left == 0) {
                    t.addSeeder(p);
                }
                else {
                    t.addLeecher(p);
                }
            }
            // client stopped download
            else if(requestParams.get((String)"event") == "stopped") {
                // remove peer from list
                p = em.find(com.tracker.entity.Peer.class, peerId.getBytes());
                if(p != null) {
                    em.remove(p);
                }
            }
            // client is now a seed
            else if(requestParams.get((String)"event") == "completed") {
                // peer is now seeding
            }
        }
        
        return(responseParams);
    }

    /**
     * Convenience method for errors enountered during parsing of the request
     * @param error the error message to send with the "failure reason" key.
     * @return returns a TreeMap containing one element. The key being
     * "failure reason" and the value being the human-readable explanation.
     */
    private TreeMap parseFailed(String error) {
        TreeMap t = new TreeMap();
        t.put((String)"failure reason", error);
        return(t);
    }
    
    /**
     * Convenience method for warnings enountered during parsing of the request
     * @param warning the warning message to send with the "warning message" key.
     * @return returns a TreeMap containing one element. The key being
     * "warning message" and the value being the human-readable explanation.
     */
    private TreeMap parseWarning(String warning) {
        TreeMap t = new TreeMap();
        t.put((String)"warning message", warning);
        return(t);
    }

    // TODO peers should time out after some time if no contact has been made
    public void persist(Object object) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(object);
            em.getTransaction().commit();
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(
                    java.util.logging.Level.SEVERE, "exception caught", e);
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
