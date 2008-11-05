/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

/**
 *
 * @author bo
 */
@Entity
public class Peer implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    // fixes some errors about multiple write-able IDs
    @Column(nullable=false,insertable=false,updatable=false)
    private Long id;

    @Id
    @Column(length=20)
    private String peerId;

    @ManyToOne(optional=false)
    private Torrent torrent;
    private InetAddress ip;
    //private Byte[] port = new Byte[2];
    private Long port;
    
    private Long downloaded;
    private Long uploaded;
    private Long bytesLeft;
    
    private Boolean seed;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastAction;

    public Peer() {
        downloaded = uploaded = bytesLeft = (long)0;
    }

    /**
     * gives a compact representation of this peer in a String format
     * @return a String containing 6-bytes; the first 4 being the ip-address,
     * and the last 2 be    ing the port number.
     */
    public String getCompactAddressPort()
    {
        byte[] buf = new byte[6];
        byte address[] = ip.getAddress();
        // ipv6?
        if(address.length > 4) {
            // no compact for you!
            return null;
        }

        for(int i = 0; i < address.length; i++) {
            buf[i] = address[i];
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(8);
        bb.putLong(port);
        buf[4] = bb.get(0);
        buf[5] = bb.get(1);

        String ret;
        try {
            ret = new String(buf, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, 
                    "no utf-8 for making compact peerlist?", ex);
            return(null);
        }

        return ret;
    }

    public Date getLastActionTime() {
        return lastAction;
    }

    public void setLastActionTime(Date lastAction) {
        this.lastAction = lastAction;
    }

    public Boolean isSeed() {
        return seed;
    }

    public void setSeed(Boolean seed) {
        this.seed = seed;
    }

    public Long getBytesLeft() {
        return bytesLeft;
    }

    public void setBytesLeft(Long bytesLeft) {
        this.bytesLeft = bytesLeft;
        
        if(this.bytesLeft == 0) {
            this.seed = true;
        }
    }

    public Long getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(Long downloaded) {
        this.downloaded = downloaded;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    public Torrent getTorrent() {
        return torrent;
    }

    public void setTorrent(Torrent torrentId) {
        this.torrent = torrentId;
    }

    public Long getUploaded() {
        return uploaded;
    }

    public void setUploaded(Long uploaded) {
        this.uploaded = uploaded;
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
            hash += peerId.getBytes()[i] ^ 0x8B;
        }
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Peer)) {
            return false;
        }
        Peer other = (Peer) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))
                || (this.peerId == null && other.peerId != null)
                || (this.peerId != null && !this.peerId.equals(other.peerId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.Peers[id=" + id + "]";
    }

}
