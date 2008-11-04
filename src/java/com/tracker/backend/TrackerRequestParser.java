/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend;

import com.tracker.entity.Peer;
import com.tracker.entity.Torrent;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.TreeMap;
import java.net.URLDecoder;
import java.util.BitSet;
import java.util.Calendar;
import java.util.List;
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
    private TreeMap requestParams;
    private InetAddress remoteAddress;
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");

    // TODO: get from config or some such
    /// intervals between announces
    private Long minInterval = (long)180;
    private Long defaultInterval = (long)1800;

    public TrackerRequestParser()
    {
    }

    public void setRequestParams(TreeMap params)
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

    public TreeMap parseRequest() throws Exception
    {
        if(requestParams == null || remoteAddress == null) {
            return(null);
        }

        EntityManager em = emf.createEntityManager();
        TreeMap responseParams = new TreeMap();
        String infoHash, peerId;
        String event = new String();
        Long uploaded, downloaded, left, port;
        boolean returnSeeds = true;
        Integer numPeersToReturn = new Integer(50);
        Torrent t;
        Peer p;

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
            if(requestParams.get((String)"compact") == "0") {
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
            Query q = em.createQuery("select t from Torrent t where t.infoHash = :infoHash");
            q.setParameter("infoHash", infoHash);
            t = (Torrent) q.getSingleResult();

            if(t == null) {
                return(parseFailed("Torrent not tracked."));
            }
        } catch(Exception ex) {
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
                Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
                q.setParameter("peerId", peerId);

                // there will only be zero or one results from this query. PeerID
                // must be unique
                List<Peer> res = q.getResultList();
                if(!res.isEmpty()) {
                    // check for inactivity
                    if(!peerIsInactive(res.get(0))) {
                        // not inactive yet, and attempts at a new started
                        Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.WARNING,
                            "event=started more than once. IP: " + remoteAddress.toString());
                        em.getTransaction().rollback();
                        em.close();
                        return parseFailed("You are already on this torrent! Wait a while if this is not true.");
                    }
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
                // remove peer from list
                Query q = em.createQuery("SELECT p FROM Peer p WHERE p.peerId = :peerId");
                q.setParameter("peerId", peerId);

                // zero or one results returned
                List<Peer> res = q.getResultList();
                if(!res.isEmpty()) {
                    p = res.get(0);
                    em.remove(p);
                }
                else {
                    Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.WARNING,
                            "event=stopped but no matching peer found. IP: " + remoteAddress.toString());
                    em.getTransaction().rollback();
                    em.close();
                    return(parseFailed("Cannot stop a peer that has not started"));
                }

                // remove peer from torrent
                if(p.isSeed()) {
                    t.removeSeed(p);
                }
                else
                    t.removeLeecher(p);
                // no reason to give out more peers
                numPeersToReturn = 0;
                /*p = (Peer)q.getSingleResult();
                if(p != null) {
                    em.remove(p);
                }
                else {
                    Logger.getLogger(TrackerRequestParser.class.getName()).log(Level.WARNING,
                            "event=stopped but no matching peer found");
                    em.getTransaction().rollback();
                    em.close();
                    return(parseFailed("Cannot stop a peer that has not started"));
                }
                // remove peer from torrent
                if(p.isSeed()) {
                    p.getTorrent().removeSeed(p);
                }
                else
                    p.getTorrent().removeLeecher(p);
                // no reason to give out any more peers
                numPeersToReturn = 0;*/
            }
            // client is now a seed
            else if(event.equalsIgnoreCase("completed")) {
                // peer is now seeding
                // TODO: no more em.find
                p = em.find(Peer.class, peerId.getBytes());
                if(p != null) {
                    p.setSeed(true);
                    returnSeeds = false;
                    p.getTorrent().removeLeecher(p);
                    p.getTorrent().addSeeder(p);

                    // increment completed counter
                    p.getTorrent().setNumCompleted(p.getTorrent().getNumCompleted() + 1);
                }
            }
        } // if(event)

        // no event specified
        else {
            // update the data for the peer
            Query q = em.createQuery("SELECT p FROM Peers p WHERE p.peerId = :peerId");
            q.setParameter("peerId", peerId);
            // TODO: no more getSingleResult()
            p = (Peer) q.getSingleResult();
            // peer not found?
            if(p == null) {
                em.getTransaction().rollback();
                em.close();
                return(parseFailed("peer not in database and no event given?"));
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
            em.getTransaction().commit();
        } catch(Exception ex) {
            if(em.getTransaction().isActive())
                em.getTransaction().rollback();
            em.close();
            throw ex;
        }
        em.close();
        
        // populate the response
        responseParams.put((String)"complete", t.getNumSeeders().toString());
        responseParams.put((String)"incomplete", t.getNumLeechers().toString());
        responseParams.put((String)"interval", defaultInterval.toString());
        responseParams.put((String)"min interval", minInterval.toString());

        // find some peers!
        String peerList;
        if(returnSeeds) {
            // only return leechers
            Vector v = (Vector) t.getLeechersData();
            peerList = getCompactPeerList(v, numPeersToReturn);
        }
        else {
            // return both seeds and leechers
            Vector v = (Vector) t.getPeersData();
            peerList = getCompactPeerList(v, numPeersToReturn);
        }

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
            if(p.isSeed()) {
                t.removeSeed(p);
            }
            else {
                t.removeLeecher(p);
            }

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
     * numWanted entries. TODO: don't return the asking peer
     * @param population the peer list to pick peers from
     * @param numWanted the maximum amount of peers to return
     * @return a peer list in the compact format ((4 bytes address + 2 bytes port) * amount)
     * as a String.
     */
    private String getCompactPeerList(Vector<Peer> population, Integer numWanted)
    {
        int numReturn = numWanted;
        String ret = new String();
        // is the population big enough to get all the peers we want?
        if(population.size() < numWanted) {
            // apparently not
            // return everything
            for(int i = 0; i < population.size(); i++) {
                Peer p = population.get(i);
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
                ret += population.get(next).getCompactAddressPort();
                picked.set(next);
            }
            // check chances for collisions?
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
}
