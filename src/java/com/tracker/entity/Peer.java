/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.entity;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
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
    @ManyToOne
    private Torrent torrent;

    @Id
    @Column(length=20)
    private String peerId;
    private InetAddress ip;
    private Byte[] port = new Byte[2];
    
    private Long downloaded;
    private Long uploaded;
    private Long bytesLeft;
    
    private Boolean seed;

    @Temporal(javax.persistence.TemporalType.DATE)
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
        String ret = new String();
        byte address[] = ip.getAddress();
        // ipv6?
        if(address.length > 4) {
            // no compact for you!
            return null;
        }

        for(int i = 0; i < address.length; i++) {
            ret += address[i];
        }

        for(int i = 0; i < port.length; i++) {
            ret += port[i];
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

    public Byte[] getPort() {
        return port;
    }

    public void setPort(Byte[] port) {
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
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Peer)) {
            return false;
        }
        Peer other = (Peer) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.Peers[id=" + id + "]";
    }

}
