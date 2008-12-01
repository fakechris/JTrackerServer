/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.webinterface.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * This entity class stores the data (path and size) of a single file contained
 * in a torrent.
 * @author bo
 */
@Entity
public class TorrentContent implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * the path and filename of this file.
     */
    String fileName;

    /**
     * the size of this file in bytes.
     */
    Long fileSize;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the filename this object represents
     * @return
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the path and filename of this object.
     * @param s a String containing the full path and filename of the file this
     * object will represent.
     */
    public void setFileName(String s) {
        fileName = s;
    }

    /**
     * Gets the file size in bytes of the file this object represents.
     * @return a Long containing the file size in bytes.
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * Sets the filesize in bytes of the file that this object represents
     * @param l a Long containing the filesize in bytes.
     */
    public void setFileSize(Long l) {
        fileSize = l;
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
        if (!(object instanceof TorrentContent)) {
            return false;
        }
        TorrentContent other = (TorrentContent) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.tracker.backend.webinterface.entity.TorrentContent[id=" + id + "]";
    }

}
