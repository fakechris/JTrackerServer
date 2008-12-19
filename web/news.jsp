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

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>News</title>
    </head>
    <body>
        <jsp:include page="header.jsp">
            <jsp:param name="caller" value="News" />
        </jsp:include>
        <h1>News</h1>
        <div>
            <%
            // grab some news
            %>
        </div>
    </body>
</html>
