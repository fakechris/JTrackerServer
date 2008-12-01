/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tracker.backend.webinterface.entity;

import com.tracker.backend.entity.Torrent;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

/**
 * This entity class contains the torrentfile to be downloaded.
 * @author bo
 */
@Entity
public class TorrentFile implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * The torrentfile itself stored in the database.
     */
    @Lob
    byte[] torrentFile;

    /**
     * The length of the torrentfile in bytes.
     */
    int fileLength;

    /**
     * the torrent this torrentfile belongs to
     */
    @OneToOne(mappedBy = "torrentFile")
    private Torrent torrent;

    /**
     * Gets the OutputStream connected with this torrentfile.
     * @return an OutputStream containing this torrentfile.
     * @throws java.lang.Exception if an I/O error occurs when writing to the
     * output stream.
     */
    public OutputStream getTorrentFileOutputStream() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream(torrentFile.length);
        output.write(torrentFile);

        return output;
    }

    /**
     * Stores the torrentfile given in the bencoded string.
     * @param bencodedTorrent the Bencoded string containing the torrentfile.
     */
    public void setTorrentFile(String bencodedTorrent) {
        torrentFile = new byte[bencodedTorrent.length()];
        for (int i = 0; i < torrentFile.length; i++) {
            torrentFile[i] = (byte) bencodedTorrent.charAt(i);
        }
        fileLength = torrentFile.length;
    }

    /**
     * Gets the length of the torrentfile in bytes
     * @return an int containing the length of the torrentfile in bytes.
     */
    public int getFileLength() {
        return fileLength;
    }

    /**
     * Sets the Torrent object connected with this torrentfile.
     * @param t the Torrent connected with this torrentfile.
     */
    public void setTorrent(Torrent t) {
        torrent = t;
    }

    /**
     * Gets the Torrent object connected with this torrentfile.
     * @return a Torrent object this torrentfile is connected to.
     */
    public Torrent getTorrent() {
        return torrent;
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
        if (!(object instanceof TorrentFile)) {
            return false;
        }
        TorrentFile other = (TorrentFile) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.tracker.backend.webinterface.entity.TorrentFile[id=" + id + "]";
    }

}
