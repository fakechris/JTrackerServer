/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.webinterface.entity;

import com.tracker.backend.entity.Torrent;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;

/**
 * This entity class contains meta-information about the torrent, such as
 * name, description, the files it contains, etc.
 * @author bo
 */
@Entity
public class TorrentData implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * link to the torrent this data is about.
     */
    @OneToOne(mappedBy = "torrentData")
    private Torrent t;

    /**
     * The files this torrent contains.
     */
    @OneToMany
    private List<TorrentContent> torrentFiles;

    /**
     * Name of the torrent
     */
    private String name;
    /**
     * Description of the torrent contents
     */
    private String description;

    /**
     * Date and time the torrent was added
     */
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date added;

    /**
     * Size of the torrent in bytes
     */
    private Long torrentSize;

    public TorrentData() {
        torrentFiles = new Vector<TorrentContent>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the torrent this data maps to.
     * @return a Torrent that this data maps to.
     */
    public Torrent getTorrent() {
        return t;
    }

    /**
     * Sets the torrent this data maps to.
     * @param t the Torrent this data is mapping to.
     */
    public void setTorrent(Torrent t) {
        this.t = t;
    }

    /**
     * Gets the list of files this torrent contains
     * @return a List containing TorrentContent objects describing the files
     * this torrent contains
     * @see TorrentContent
     */
    public List<TorrentContent> getTorrentContent() {
        return torrentFiles;
    }

    /**
     * Sets which files this torrent contains.
     * @param files a List of TorrentContent describing the files.
     */
    public void setTorrentContent(List<TorrentContent> files) {
        torrentFiles = files;
    }

    /**
     * Convenience method for setting the torrent content.
     * @param files a Map containing the paths and sizes of the files this
     * torrent contains.
     */
    public void addFiles(Map<String,Long> files) {
        for(Map.Entry<String, Long> entry : files.entrySet()) {
            TorrentContent oneFile = new TorrentContent();
            oneFile.setFileName(entry.getKey());
            oneFile.setFileSize(entry.getValue());
            
            torrentFiles.add(oneFile);
        }
    }

    /**
     * Gets the Date the torrent was added.
     * @return Date when the torrent was added.
     */
    public Date getAdded() {
        return added;
    }

    /**
     * Sets the Date the torrent was added.
     * @param added Date the torrent was added.
     */
    public void setAdded(Date added) {
        this.added = added;
    }

    /**
     * Gets the name of the torrent.
     * @return a String containing the name of the torrent.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this torrent.
     * @param name the name to set to this torrent.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of this torrent.
     * @return a String containing the description of this torrent.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description for this torrent.
     * @param description the description to set to this torrent.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the size of the torrent in bytes
     * @return the size of the torrent in bytes
     */
    public Long getTorrentSize() {
        return torrentSize;
    }

    /**
     * sets the size of the torrent in bytes
     * @param torrentSize the new size of the torrent
     */
    public void setTorrentSize(Long torrentSize) {
        this.torrentSize = torrentSize;
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
        if (!(object instanceof TorrentData)) {
            return false;
        }
        TorrentData other = (TorrentData) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.tracker.backend.webinterface.entity.TorrentData[id=" + id + "]";
    }

}
