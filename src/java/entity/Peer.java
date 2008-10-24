/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author bo
 */
@Entity
public class Peer implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ManyToOne
    private Torrent torrent;
        
    private Byte[] peerId;
    private Byte[] ip;
    private Byte[] port;
    
    private Long downloaded;
    private Long uploaded;
    private Long bytesLeft;
    
    private Boolean seed;

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

    public Byte[] getIp() {
        return ip;
    }

    public void setIp(Byte[] ip) {
        this.ip = ip;
    }

    public Byte[] getPeerId() {
        return peerId;
    }

    public void setPeerId(Byte[] peerId) {
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
