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
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%--
We need the context path for the browse.js, so create it here
--%>
<script type="text/javascript">
    var contextPath = "<%=application.getContextPath()%>";
</script>
<script type="text/javascript" src="resources/jquery_1.2.6/jquery-1.2.6.js"></script>
<script type="text/javascript" src="resources/behaviour/browse.js"></script>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Browse Torrents</title>
    </head>
    <body>
        <jsp:include page="header.jsp">
            <jsp:param name="caller" value="Browse" />
        </jsp:include>
        <h1>Hello World!</h1>
        Search field:'
        <input type="text" name="searchField" value="" />
        <input type="submit" value="Submit" />
        Search descriptions
        <input type="checkbox" name="searchDescriptions" value="checked" checked />
        Include dead
        <input type="checkbox" name="includeDead" value="checked" />
        Num results
        <select name="numResults">
            <option>25</option>
            <option>50</option>
            <option>75</option>
            <option>100</option>
        </select>
        <div id="loading"><img src="resources/images/ajax-loader.gif" alt="loading..."/></div>

        <div id="torrentList" style="display:none">
            <table border="0">
                <tr>
                    <th>
                        <em><b>Torrent</b></em>
                    </th>
                    <th>
                        <em><b>Date Added</b></em>
                    </th>
                </tr>
                <td>
                    <!-- content provided by JS -->
                    <div id="torrentName" style="display:none">
                    </div>
                </td>
                <td>
                    <!-- content provided by JS -->
                    <div id="torrentDate" style="display:none">
                    </div>
                </td>
            </table>
        </div>
    </body>
</html>
