/*
 * $Id$
 *
 * Copyright © 2008,2009 Bjørn Øivind Bjørnsen
 *
 * This file is part of Quash.
 *
 * Quash is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quash is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quash. If not, see <http://www.gnu.org/licenses/>.
 */

package com.tracker.backend.webinterface;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;

/**
 * Provides an interface to different implementations of the upload frontend.
 * @author bo
 */
public interface TakeUpload {
    /**
     * Parses the upload request and prints the output of the process to the
     * given PrintWriter. Used for getting a standarised interface across
     * multiple implementations (XML, JSON, etc?).
     * @param request The request to parse the upload from. The method looks for
     * <em>"torrentName"</em>, <em>"torrentDescription"</em> keys as well as
     * multipart file-content. An error is issued if there is some problems
     * reading the filepart.
     * @param out the OutputStream to print to. The output basically consists
     * of a error reason and warning reason which is only populated in the case
     * of an error, and a redownload key which defines whether the .torrent-file
     * will have to be redownloaded or not - for example through having to change
     * the announce URL or deactivate the private key.
     * @throws java.lang.Exception
     */
    public abstract void parseUpload(HttpServletRequest request, PrintWriter out) throws Exception;
}
