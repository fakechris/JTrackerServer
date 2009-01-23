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

<%@page
contentType="text/html"
pageEncoding="UTF-8"
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%--
We need the context path for the browse.js, so create it here
--%>
<script type="text/javascript">
    var contextPath = "<%=application.getContextPath()%>";
</script>
<script type="text/javascript" src="resources/jquery_1.2.6/jquery-1.2.6.js"></script>
<script type="text/javascript" src="resources/jquery_1.2.6/jquery.form.js"></script>
<script type="text/javascript" src="resources/behaviour/browse.js"></script>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" >

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="keywords" content="quash, bittorrent, torrent" />
    <meta name="description" content="A list of torrents tracked by this
    tracker, including downloads for the torrentfiles and search capabilites" />

    <title>Browse Torrents</title>

    <style type="text/css" media="all">
        @import "resources/css/default.css";
    </style>
</head>

<body onload="window.defaultStatus='Browse Torrents';" id="browse-torrents">
    <div id="container">
        <%-- Load the header --%>
        <jsp:include page="header.jsp">
            <jsp:param name="caller" value="Browse" />
        </jsp:include>
        <div id="searchBar">
            <form id="searchForm" action="xml/torrentlist" method="post">
                Search terms: <input type="text" name="searchField" value="" />
                Search descriptions: <input type="checkbox"
                    name="searchDescriptions" value="checked" checked="checked" />
                Include dead torrents: <input type="checkbox" name="includeDead" value="checked" />
                Number of results to return:
                <select name="numResults">
                    <option>25</option>
                    <option>50</option>
                    <option>75</option>
                    <option>100</option>
                </select>
                <input type="submit" value="Search Torrents" name="submit" />
            </form>
            <div id="loading">
                <img src="resources/images/ajax-loader.gif" alt="loading..."/>
            </div>
        </div>

        <div id="torrentList">
            <table border="0">
                <tr>
                    <th>
                        <em><b>Torrent</b></em>
                    </th>
                    <th>
                        <em><b>Seeders</b></em>
                    </th>
                    <th>
                        <em><b>Peers</b></em>
                    </th>
                    <th>
                        <em><b>Completed</b></em>
                    </th>
                    <th>
                        <em><b>Date Added</b></em>
                    </th>
                </tr>
                <td>
                    <!-- content provided by JS -->
                    <div id="torrentName">
                    </div>
                </td>
                <td>
                    <!-- content provided by JS -->
                    <div id="torrentSeeds">
                    </div>
                </td>
                <td>
                    <!-- content provided by JS -->
                    <div id="torrentPeers">
                    </div>
                </td>
                <td>
                    <!-- content provided by JS -->
                    <div id="torrentCompleted">
                    </div>
                </td>
                <td>
                    <!-- content provided by JS -->
                    <div id="torrentDate">
                    </div>
                </td>
            </table>
        </div>
    </div>
</body>

</html>