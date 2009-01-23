<%--
  $Id$

  Copyright © 2008,2009 Bjørn Øivind Bjørnsen

  This file is part of Quash.

  Quash is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Quash is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Quash. If not, see <http://www.gnu.org/licenses/>.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<script type="text/javascript" src="resources/jquery_1.2.6/jquery-1.2.6.js"></script>
<script type="text/javascript" src="resources/jquery_1.2.6/jquery.form.js"></script>
<script type="text/javascript" src="resources/behaviour/upload.js"></script>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" >
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="keywords" content="quash, bittorrent, torrent" />
    <meta name="description" content="The upload interface to this Quash-based
    tracker." />

    <title>Upload Torrents</title>

    <style type="text/css" media="all">
        @import "resources/css/default.css";
    </style>
</head>
<body onload="window.defaultStatus='Upload Torrents';" id="upload-torrents">
    <div id="container">
        <jsp:include page="header.jsp">
            <jsp:param name="caller" value="Upload" />
        </jsp:include>
        <!-- do some checking on size of torrentfile in javascript -->
        <form id="xmlUploadForm" action="xml/TakeUpload" method="POST" enctype="multipart/form-data">
            <em>TorrentFile:</em> <input type="file" name="torrentFile" value="" /> <br />
            <em>Torrent Name:</em> <input type="text" name="torrentName" value="" /> <br />
            <em>Torrent Description:</em> <textarea name="torrentDescription" rows="4" cols="20"></textarea> <br />
            <input type="submit" value="Submit" name="torrentSubmit" />
        </form>

        <div id="content">
            <div id="successDiv" style="display: none"></div>
            <div id="errorDiv" style="display: none"></div>
            <div id="warningDiv" style="display: none"></div>
            <div id="redownloadDiv" style="display: none"></div>
        </div>
    </div>
</body>
</html>
