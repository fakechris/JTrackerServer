/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend;

import com.tracker.entity.Peer;
import com.tracker.entity.Torrent;

import java.io.UnsupportedEncodingException;
import java.lang.Exception;
import java.net.InetAddress;
import java.util.TreeMap;
import java.net.URLDecoder;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author bo
 */
public class TrackerRequestParser {
    private TreeMap<String,String> requestParams;
    private InetAddress remoteAddress;
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");

    // TODO: get from config or some such
    /// intervals between announces
    private Long minInterval = (long)180;
    private Long defaultInterval = (long)1800;

    public TrackerRequestParser()
    {
    }

    /**
     * Performs a scrape on all tracked torrents
     * @return a TreeMap consisting of {infoHash,{complete,downloaded,incomplete}}
     */
    public TreeMap<String, TreeMap> scrape()
    {
        TreeMap<String, TreeMap> result = new TreeMap<String, TreeMap>();
        TreeMap<String, Long> contents;

        EntityManager em = emf.createEntityManager();

        Query q = em.createQuery("SELECT t FROM Torrent t");

        Vector<Torrent> torrents = (Vector<Torrent>) q.getResultList();
        Iterator itr = torrents.iterator();
        while(itr.hasNext()) {
            Torrent t = (Torrent) itr.next();
            contents = new TreeMap<String, Long>();
            contents.put((String)"complete", t.getNumSeeders());
            contents.put((String)"downloaded", t.getNumCompleted());
            contents.put((String)"incomplete", t.getNumLeechers());

            result.put(t.getInfoHash(), contents);
        }

        return result;
    }

    /**
     * Performs a scrape on a specified torrent.
     * @param infoHash the info hash to scrape
     * @return a TreeMap consisting of {infoHash,{complete,downloaded,incomplete}}
     * @throws java.lang.Exception if the torrent cannot be found.
     */
    public TreeMap<String, TreeMap> scrape(String infoHash) throws Exception
    {
        TreeMap<String, TreeMap> result = new TreeMap<String, TreeMap>();
        TreeMap<String, Long> contents = new TreeMap<String, Long>();

        EntityManager em = emf.createEntityManager();

        Query q = em.createQuery("SELECT t FROM Torrent t WHERE t.infoHash = :infoHash");
        q.setParameter("infoHash", infoHash);

        try {
            Torrent t = (Torrent) q.getSingleResult();

            contents.put((String)"complete", t.getNumSeeders());
            contents.put((String)"downloaded", t.getNumCompleted());
            contents.put((String)"incomplete", t.getNumLeechers());

            result.put(t.getInfoHash(), contents);
        }
        // no results found?
        catch(NoResultException ex) {
            Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.FINE,
                    "cannot find torrent scraped by " + remoteAddress.toString()
                    + ": " + ex.getMessage());
            Exception res = new Exception("Cannot find torrent",ex);
            throw res;
        }
        // some other error?
        catch(Exception ex) {
            Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.WARNING,
                    "error when scraping info hash " + infoHash + ", request by "
                    + remoteAddress.toString() + ": " + ex.getMessage());
            throw ex;
        }

        return result;
    }

    public void setRequestParams(TreeMap<String,String> params)
    {
        requestParams = params;
    }

    public TreeMap getRequestParams()
    {
        return(requestParams);
    }

    public void setRemoteAddress(InetAddress address)
    {
        remoteAddress = address;
    }

    public InetAddress getRemoteAddress()
    {
        return(remoteAddress);
    }

    public TreeMap<String,String> parseRequest() throws Exception
    {
        if(requestParams == null || remoteAddress == null) {
            return(null);
        }

        EntityManager em = emf.createEntityManager();
        TreeMap<String,String> responseParams = new TreeMap<String,String>();
        String infoHash, peerId;
        String event = new String();
        Long uploaded, downloaded, left, port;
        boolean returnSeeds = true;
        Integer numPeersToReturn = new Integer(50);
        Torrent t = null;
        Peer p = null;

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
            Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.SEVERE,
                    "could not decode info hash", ex);
            em.close();
            return(parseFailed("Tracker error"));
        }

        // check for peer id
        if(!requestParams.containsKey((String)"peer_id")) {
            return(parseFailed("missing peer_id!"));
        }

        peerId = (String)requestParams.get((String)"peer_id");

        // check for port-number
        if(!requestParams.containsKey((String)"port")) {
            return(parseFailed("missing port number!"));
        }

        port = Long.parseLong((String)requestParams.get((String)"port"));

        // check for uploaded
        if(!requestParams.containsKey((String)"uploaded")) {
            return(parseFailed("missing 'uploaded' field!"));
        }

        uploaded = Long.parseLong((String)requestParams.get((String)"uploaded"));

        // check for downloaded
        if(!requestParams.containsKey((String)"downloaded")) {
            return(parseFailed("missing 'downloaded' field!"));
        }

        downloaded = Long.parseLong((String)requestParams.get((String)"downloaded"));

        // check for left
        if(!requestParams.containsKey((String)"left")) {
            return(parseFailed("missing 'left' field!"));
        }

        left = Long.parseLong((String)requestParams.get((String)"left"));

        /*
         * Check for optional fields
         */
        // check for 'compact'
        if(requestParams.containsKey((String)"compact")) {
            if(requestParams.get((String)"compact").equalsIgnoreCase("0")) {
                return(parseFailed("this tracker only supports compact responses"));
            }
        }

        // check for numwant
        if(requestParams.containsKey((String)"numwant")) {
            Integer numWant = Integer.parseInt((String)requestParams.get((String)"numwant"));
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
            Query q = em.createQuery("SELECT t FROM Torrent t WHERE t.infoHash = :infoHash");
            q.setParameter("infoHash", infoHash);
            t = (Torrent) q.getSingleResult();
        }
        // cannot find torrent?
        catch(NoResultException ex) {
            Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.FINE,
                    "cannot find torrent requested by " + remoteAddress.toString()
                    + ": " + ex.getMessage());
            em.close();
            return(parseFailed("Torrent not tracked."));
        } 
        // some other error occurred
        catch(Exception ex) {
            Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.SEVERE,
                    "error when looking for torrent in database", ex);
            em.close();
            return(parseFailed("Tracker error."));
        }

        // begin the transaction, commit at the end
        em.getTransaction().begin();

        /*
         * check for event-key
         * the first announce to the tracker from a new peer must have the
         * event = started key/value pair.
         */
        if(requestParams.containsKey((String)"event")) {
            event = (String) requestParams.get((String)"event");
        }

        if(!event.isEmpty()) {
            // client just started the download
            if(event.equalsIgnoreCase("started")) {
                // is the peer already on this torrent?
                try {
                    Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
                    q.setParameter("peerId", peerId);

                    p = (Peer) q.getSingleResult();
                    // check for inactivity
                    if(!peerIsInactive(p)) {
                        // not inactive yet, and attempts at a new started
                        Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.FINE,
                            "event=started more than once. IP: " + remoteAddress.toString());
                        em.getTransaction().rollback();
                        em.close();
                        return parseFailed("You are already on this torrent! Wait a while if this is not true.");
                    }
                }
                // no peer found (it's a good thing).
                catch(NoResultException ex) {}
                // oh dear! some other error.
                catch(Exception ex) {
                    Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.SEVERE,
                            "error when looking for peer in database", ex);
                    em.getTransaction().rollback();
                    em.close();
                    return(parseFailed("Tracker error."));
                }

                // add new peer
                p = new Peer();

                p.setPeerId(peerId);

                p.setBytesLeft(left);
                p.setDownloaded(downloaded);
                p.setUploaded(uploaded);

                p.setPort(port);

                // set address
                p.setIp(remoteAddress);

                p.setLastActionTime(Calendar.getInstance().getTime());

                // if left = 0, this peer has a complete copy, so add as seed,
                // if not, add as leech
                if(left == 0) {
                    t.addSeeder(p);
                }
                else {
                    t.addLeecher(p);
                }

                // persist this object
                em.persist(p);
            }
            // client stopped download
            else if(event.equalsIgnoreCase("stopped")) {
                // remove peer from database
                try {
                    Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
                    q.setParameter("peerId", peerId);
                    
                    p = (Peer) q.getSingleResult();
                }
                // no peer found?
                catch(NoResultException ex) {
                    Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.FINE,
                            "event=stopped but no matching peer found. IP: " + 
                            remoteAddress.toString() + ex.getMessage());
                    em.getTransaction().rollback();
                    em.close();
                    return parseFailed("Cannot stop a peer that has not started");
                }
                // oh dear! some other error.
                catch(Exception ex) {
                    Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.SEVERE,
                            "error when looking for peer in database", ex);
                    em.getTransaction().rollback();
                    em.close();
                    return(parseFailed("Tracker error."));
                }

                // remove peer from torrent
                t.removePeer(p);

                em.remove(p);
                // no reason to give out more peers
                numPeersToReturn = 0;
            }
            // client is now a seed
            else if(event.equalsIgnoreCase("completed")) {
                // peer is now seeding
                // try to find the peer
                try {
                    Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
                    q.setParameter("peerId", peerId);
                    p = (Peer) q.getSingleResult();
                }
                // no peer found matching this peerid?
                catch(NoResultException ex) {
                    em.getTransaction().rollback();
                    em.close();
                    Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.FINE,
                            "event=completed but no matching peer found. IP: " + 
                            remoteAddress.toString() + ex.getMessage());
                    return parseFailed("Cannot mark a peer as completed if it has not started.");
                }
                // some other error occurred
                catch(Exception ex) {
                    Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.SEVERE,
                            "error when looking for peer in database", ex);
                    em.getTransaction().rollback();
                    em.close();
                    return parseFailed("Tracker error.");
                }

                if(!p.getTorrent().leecherCompleted(p)) {
                    Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.SEVERE,
                            "cannot turn leech into seed?");
                    em.getTransaction().rollback();
                    em.close();
                    return parseFailed("Tracker error.");
                }
                returnSeeds = false;
            }
        } // if(event)

        // no event specified
        else {
            // update data for the peer
            try {
                Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
                q.setParameter("peerId", peerId);

                p = (Peer) q.getSingleResult();
            }
            // no peer found?
            catch(NoResultException ex) {
                em.getTransaction().rollback();
                em.close();
                Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.FINE,
                        "no event but no matching peer found. IP: " +
                        remoteAddress.toString() + ex.getMessage());
                return parseFailed("Start-event was not received, so you are not tracked.");
            }
            // some other error occurred
            catch(Exception ex) {
                Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.SEVERE,
                        "error when looking for peer in database", ex);
                em.getTransaction().rollback();
                em.close();
                return parseFailed("Tracker error.");
            }

            // update attributes
            p.setBytesLeft(left);
            p.setDownloaded(downloaded);
            p.setUploaded(uploaded);
            p.setLastActionTime(Calendar.getInstance().getTime());

            returnSeeds = !p.isSeed();

            em.merge(p);

        }
        // commit the changes made so far and close the EntityManager
        try {
            synchronized(this) {
                em.getTransaction().commit();
            }
        } catch(Exception ex) {
            if(em.getTransaction().isActive())
                em.getTransaction().rollback();
            em.close();
            throw ex;
        }

        // make sure that the response is not based on some stale cache
        em.refresh(t);

        // populate the response
        responseParams.put((String)"complete", t.getNumSeeders().toString());
        responseParams.put((String)"incomplete", t.getNumLeechers().toString());
        responseParams.put((String)"interval", defaultInterval.toString());
        responseParams.put((String)"min interval", minInterval.toString());

        // find some peers!
        String peerList;
        if(returnSeeds) {
            // only return leechers
            Vector<Peer> v = (Vector<Peer>) t.getLeechersData();
            peerList = getCompactPeerList(v, numPeersToReturn, p);
        }
        else {
            // return both seeds and leechers
            Vector<Peer> v = (Vector<Peer>) t.getPeersData();
            peerList = getCompactPeerList(v, numPeersToReturn, p);
        }

        // close the EntityManager
        em.close();

        responseParams.put((String)"peers", peerList);

        return(responseParams);
    }

    /**
     * checks to see if the peer has been inactive (no announce) for an extended
     * period of time. Removes the peer from the persisted database if this is
     * the case and returns true
     * @param p the peer to check
     * @return true if the peer was inactive and has been removed, false if it
     * is active
     */
    private boolean peerIsInactive(Peer p)
    {
        long lastAction = p.getLastActionTime().getTime();
        long currentTime = Calendar.getInstance().getTimeInMillis();
        // interval + 120 secs
        long drag = (defaultInterval * 1000) + 120000;
        // inactive for a long period?
        if(currentTime - (lastAction + drag) > 0) {
            Torrent t = p.getTorrent();

            t.removePeer(p);

            // remove from persistence
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            em.remove(p);
            em.getTransaction().commit();
            em.close();

            return(true);
        }
        return(false);
    }

    /**
     * Method to return a peer list from the given population with maximum
     * numWanted entries.
     * @param population the peer list to pick peers from
     * @param numWanted the maximum amount of peers to return
     * @return a peer list in the compact format ((4 bytes address + 2 bytes port) * amount)
     * as a String.
     */
    private String getCompactPeerList(Vector<Peer> population, Integer numWanted, Peer askingPeer)
    {
        int numReturn = numWanted;
        String ret = new String();
        // is the population big enough to get all the peers we want?
        if(population.size() < numWanted) {
            // apparently not
            // return everything
            for(int i = 0; i < population.size(); i++) {
                Peer p = population.get(i);
                // is this the asking peer?
                if(askingPeer != null && p.equals(askingPeer)) {
                    continue;
                }
                // is the peer inactive and thus open for removal?
                if(peerIsInactive(p)) {
                    continue;
                }

                ret += p.getCompactAddressPort();
            }
        }

        else {
            // pick some random peers
            Random r = new Random(Calendar.getInstance().getTimeInMillis());
            // index of already picked peers
            BitSet picked = new BitSet(numReturn);
            // pick some peers
            for(int i = 0; i < numReturn; i++) {
                int next = r.nextInt(numReturn);
                while(picked.get(next)) {
                    next = r.nextInt(numReturn);
                }
                Peer p = population.get(next);
                if(askingPeer != null && !p.equals(askingPeer)) {
                    ret += p.getCompactAddressPort();
                }
                picked.set(next);
            }
            // check chances for collisions?
            // TODO: improve random pick, check for inactivity
            // if numpeers is half the size or more of the swarm, drop the
            // usual random pick-method, and simply pick either a sequential
            // stream of peers either right-to-left or left-to-right?
            // would greatly improve the performance of the algorithm on
            // small swarms where peers to return is less than the total
            // amount of peers
        }
        return ret;
    }

    // not implemented
    //private String getNormalPeerList()

    /**
     * Convenience method for errors enountered during parsing of the request
     * @param error the error message to send with the "failure reason" key.
     * @return returns a TreeMap containing one element. The key being
     * "failure reason" and the value being the human-readable explanation.
     */
    private TreeMap<String,String> parseFailed(String error) {
        TreeMap<String,String> t = new TreeMap<String,String>();
        t.put((String)"failure reason", error);
        return(t);
    }

    /**
     * Convenience method for warnings enountered during parsing of the request
     * @param warning the warning message to send with the "warning message" key.
     * @return returns a TreeMap containing one element. The key being
     * "warning message" and the value being the human-readable explanation.
     */
    private TreeMap<String,String> parseWarning(String warning) {
        TreeMap<String,String> t = new TreeMap<String,String>();
        t.put((String)"warning message", warning);
        return(t);
    }
}
