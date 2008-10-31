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
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

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

    public TreeMap parseRequest()
    {
        if(requestParams == null || remoteAddress == null) {
            return(null);
        }

        EntityManager em = emf.createEntityManager();
        TreeMap responseParams = new TreeMap();
        String infoHash, peerId;
        Long uploaded, downloaded, left;
        Integer numPeersToReturn = new Integer(50);
        Integer port;
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
            t = em.find(Torrent.class, infoHash);

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
            // client just started the download
            if(requestParams.get((String)"event") == "started") {
                // enable this if needed
                // is the peer already registered?
                /*Query q = em.createNativeQuery("select p from Peer p " +
                        "where p.ip = :ip or p.peerId = :peerId", Peer.class);
                q.setParameter("ip", remoteAddress);
                q.setParameter("peerId", peerId.getBytes());

                List<Peer> res = q.getResultList();
                if(!res.isEmpty()) {
                    while(res.iterator().hasNext()) {
                        Peer temp = res.iterator().next();

                        // calculate amount of time inactive
                        long inactivity = Calendar.getInstance().getTimeInMillis() -
                                temp.getLastActionTime().getTime();

                        // is the peer dead, or at least an approximation of dead?
                        if(inactivity > ((defaultInterval + 120) * 1000)) {
                            em.remove(temp);
                        }
                    }
                }*/

                // add new peer
                p = new Peer();

                p.setBytesLeft(left);
                p.setDownloaded(downloaded);
                p.setUploaded(uploaded);

                Byte[] b = new Byte[2];
                // lovely binary logic
                b[0] = (byte)((port >> 8) & 0xFF);
                b[1] = (byte)(port & 0xFF);

                p.setPort(b);

                // set address
                p.setIp(remoteAddress);

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
            else if(requestParams.get((String)"event") == "stopped") {
                // remove peer from list
                p = em.find(Peer.class, peerId.getBytes());
                if(p != null) {
                    em.remove(p);
                }
                // remove peer from torrent
                if(p.isSeed()) {
                    p.getTorrent().removeSeed(p);
                }
                else
                    p.getTorrent().removeLeecher(p);
            }
            // client is now a seed
            else if(requestParams.get((String)"event") == "completed") {
                // peer is now seeding
                p = em.find(Peer.class, peerId.getBytes());
                if(p != null) {
                    p.setSeed(true);
                    p.getTorrent().removeLeecher(p);
                    p.getTorrent().addSeeder(p);

                    // increment completed counter
                    p.getTorrent().setNumCompleted(p.getTorrent().getNumCompleted() + 1);
                }
            }
        } // if(event)

        // update the data for the peer
        p = em.find(Peer.class, peerId.getBytes());
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

        // populate the response
        responseParams.put((String)"complete", t.getNumSeeders().toString());
        responseParams.put((String)"incomplete", t.getNumLeechers().toString());
        responseParams.put((String)"interval", defaultInterval.toString());
        responseParams.put((String)"min interval", minInterval.toString());

        // find some peers!
        String peerList;
        if(p.isSeed()) {
            // only return leechers
            peerList = getCompactPeerList((Peer[])t.getLeechersData().toArray(), numPeersToReturn);
        }
        else {
            // return both seeds and leechers
            peerList = getCompactPeerList((Peer[])t.getPeersData().toArray(), numPeersToReturn);
        }

        responseParams.put((String)"peers", peerList);

        em.persist(p);
        em.getTransaction().commit();
        em.close();

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
    private String getCompactPeerList(Peer[] population, Integer numWanted)
    {
        int numReturn = numWanted;
        String ret = new String();
        // is the population big enough to get all the peers we want?
        if(population.length < numWanted) {
            // apparently not
            // return everything
            for(int i = 0; i < population.length; i++) {
                Peer p = population[i];
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
                ret += population[next].getCompactAddressPort();
                picked.set(next);
            }
            // check chances for collisions?
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
