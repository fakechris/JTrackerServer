/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.entity;

import com.tracker.backend.StringUtils;
import com.tracker.backend.webinterface.entity.TorrentData;
import com.tracker.backend.webinterface.entity.TorrentFile;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;



/**
 *
 * @author bo
 */
@Entity
public class Torrent implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * Primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /**
     * Primary key, 40-byte hex-representation of the info hash
     * encoded as hex to make it easier with all the binary data
     */
    @Id
    @Column(length=40)
    private String infoHash;
    
    /**
     * Number of seeders seeding this torrent
     */
    private Long numSeeders;
    /**
     * Number of leechers downloading this torrent
     */
    private Long numLeechers;
    /**
     * Total number of peers on this torrent (seeders + leechers)
     */
    private Long numPeers;
    /**
     * How many have completed this torrent over its lifetime
     */
    private Long numCompleted;

    /**
     * All peers connected with this torrent,
     * bi-directional relationship with the Peer Entity class
     */
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "torrent")
    private List<Peer> peersData;
    /**
     * The peers that are seeding this torrent,
     * one-directional relationship with the Peer Entity class.
     * We have to specify the table used to avoid sharing the default name with
     * the leech-data.
     */
    @JoinTable(name="TORRENT_SEED")
    @OneToMany
    private List<Peer> seedersData;
    /**
     * The peers that are leeching this torrent,
     * one-directional relationship with the Peer Entity class.
     * We have to specify the table used to avoid sharing the default name with
     * the seed-data.
     */
    @JoinTable(name="TORRENT_LEECH")
    @OneToMany
    private List<Peer> leechersData;
    /**
     * A link to the metadata about this torrent. Name, description and such
     * are contained in this table.
     */
    @OneToOne(cascade=CascadeType.ALL)
    private TorrentData torrentData;
    /**
     * The torrentile itself which maps to this torrent (ie, the SHA-1 hash of
     * the info dictionary equals the info hash contained here).
     */
    @OneToOne(cascade=CascadeType.ALL)
    private TorrentFile torrentFile;

    /**
     * default constructor
     */
    public Torrent() {
        numCompleted = numLeechers = numPeers = numSeeders = (long)0;
        peersData = new Vector<Peer>();
        seedersData = new Vector<Peer>();
        leechersData = new Vector<Peer>();
    }

    /**
     * Convenience method for converting a leecher to a seed.
     * @param p the leecher that has completed this torrent
     * @return true if the leecher is on this torrent and has been changed to a
     * seed, false if the peer is not a leecher of this torrent.
     */
    public boolean leecherCompleted(Peer p) {
        if(leechersData.contains(p)) {
            leechersData.remove(p);
            numLeechers--;

            seedersData.add(p);
            numSeeders++;

            p.setSeed(true);

            return true;
        }

        return false;
    }
    
    /**
     * Gets the leechers data
     * @return a Collection of Peers that are leeching this torrent
     */
    public List<Peer> getLeechersData() {
        return leechersData;
    }

    /**
     * Adds a leecher to this torrent
     * @param p a Peer that is leeching this torrent
     * @return true if the peers is added as a leecher, false if the peer is
     * already in the list of peers.
     */
    public boolean addLeecher(Peer p) {
        if(this.peersData.contains(p))
            return(false);

        p.setSeed(false);
        this.leechersData.add(p);
        numLeechers++;
        this.addPeer(p);
        
        return(true);
    }
    
    /**
     * Removes a leecher from this torrent
     * @param p the Peer to remove from the list of leechers
     * @return true if the Peer was leeching this torrent and was successfully
     * removed, false if the Peer was not leeching this torrent.
     */
    private boolean removeLeecher(Peer p) {
        if(this.leechersData.contains(p)) {
            this.leechersData.remove(p);
            numLeechers--;
            return(true);
        }
        else
            return(false);
    }

    /**
     * Gives the number of leechers
     * @return the number of leechers this torrent has
     */
    public Long getNumLeechers() {
        return numLeechers;
    }

    /**
     * sets the number of leechers
     * @param numLeechers the number of leechers this torrent supposedly has
     */
    public void setNumLeechers(Long numLeechers) {
        this.numLeechers = numLeechers;
    }

    /**
     * gets the data of the seeding peers
     * @return a Collection of Peers that is currently seeding this torrent
     */
    public List<Peer> getSeedersData() {
        return seedersData;
    }

    /**
     * adds a seed to this torrent
     * @param p the Peer to add as a seed to this torrent
     * @return true if the Peer is successfully added as a seed, false if the
     * Peer is already seeding this torrent
     */
    public boolean addSeeder(Peer p) {
        if(this.seedersData.contains(p))
            return(false);

        p.setSeed(true);
        this.seedersData.add(p);
        numSeeders++;
        this.addPeer(p);
        
        return(true);
    }
    
    /**
     * removes a seed from this torrent
     * @param p the Peer to remove as a seed from this torrent
     * @return true if the Peer is successfully removed, false if the Peer is
     * not seeding this torrent or is not a peer of this torrent
     */
    private boolean removeSeed(Peer p) {
        if(this.seedersData.contains(p)) {
            this.seedersData.remove(p);
            numSeeders--;
            return(true);
        }
        else
            return(false);
    }
    
    /**
     * gets the peer data for the given torrent
     * @return a Collection of Peers that is all the peers connected with the
     * torrent
     */
    public List<Peer> getPeersData() {
        return peersData;
    }
    
    /**
     * adds a peer to the list of peers.
     * private method only, use addSeeder() or addLeecher() for everyone else
     * @param p the peer to add the the list
     */
    private void addPeer(Peer p) {
        this.peersData.add(p);
        p.setTorrent(this);
        numPeers++;
    }
    
    /**
     * Removes a peer from the list of peers. Handles both seeds and leechers.
     * @param p the peer to remove from the list
     * @return true if the peer has been removed, false if the peer is not
     * on this torrent
     */
    public boolean removePeer(Peer p) {
        if(this.peersData.contains(p)) {
            this.peersData.remove(p);
            numPeers--;

            if(p.isSeed())
                return this.removeSeed(p);
            else
                return this.removeLeecher(p);
        }
        else
            return(false);
    }

    /**
     * Gets the torrent meta-data connected with this torrent.
     * @return a TorrentData object containing the torrent metadata.
     */
    public TorrentData getTorrentData() {
        return torrentData;
    }

    /**
     * Sets the torrent metadata connected with this torrent.
     * @param td
     */
    public void setTorrentData(TorrentData td) {
        torrentData = td;
        torrentData.setTorrent(this);
    }

    /**
     * Gets the torrentfile of this torrent.
     * @return the TorrentFile this torrent is connected to.
     */
    public TorrentFile getTorrentFile() {
        return torrentFile;
    }

    /**
     * Sets the TorrentFile this torrent is connected to.
     * @param tf the TorrentFile to set.
     */
    public void setTorrentFile(TorrentFile tf) {
        torrentFile = tf;
        torrentFile.setTorrent(this);
    }

    public Long getNumPeers() {
        return numPeers;
    }

    public void setNumPeers(Long numPeers) {
        this.numPeers = numPeers;
    }

    public Long getNumSeeders() {
        return numSeeders;
    }

    public void setNumSeeders(Long numSeeders) {
        this.numSeeders = numSeeders;
    }
        
    public Long getNumCompleted() {
        return numCompleted;
    }

    public void setNumCompleted(Long completed) {
        this.numCompleted = completed;
    }

    /**
     * returns the hex representation of the info hash
     * @return the hex string representing the info hash
     */
    public String getInfoHash() {
        return infoHash;
    }

    /**
     * Takes the raw info hash supplied and turns it into a hex-string and sets
     * the hex string.
     * @param infoHash the raw info hash recevied.
     * @return true if the info hash has been set, false if there is some error
     */
    public boolean setInfoHash(String infoHash) {
        // is the info hash raw?
        if(infoHash.length() == 20) {
            try {
                byte[] b = new byte[20];
                // charAt is used instead of getBytes, because getBytes insists on
                // going through the whole encoding thing
                for(int i = 0; i < b.length; i++) {
                    b[i] = (byte) infoHash.charAt(i);
                }
                this.infoHash = StringUtils.getHexString(b);
            } catch (Exception ex) {
                // something went wrong
                return false;
            }
            // all good
            return true;
        } // if(length != 20)
        // is the info hash already encoded?
        if(infoHash.length() == 40) {
            this.infoHash = infoHash;
            return true;
        }
        return false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for(int i = 0; i < 20; i++) {
            hash += infoHash.getBytes()[i] ^ 0x4F;
        }
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Torrent)) {
            return false;
        }
        Torrent other = (Torrent) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))
                || (this.infoHash == null && other.infoHash != null)
                || (this.infoHash != null && !this.infoHash.equals(other.infoHash))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.Torrent[id=" + id + ",info_hash=" + infoHash + "]";
    }

}
