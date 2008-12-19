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
<!-- Header -->
<div id="header" style="text-align:center">
    <table border="0" width="100%">
<%
String caller = request.getParameter("caller");
String[] pages = {"News", "Browse", "Upload", "FAQ", "Help", "Stats" };

for(String s : pages) {
    out.println("<td>");
    if(!s.equalsIgnoreCase(caller)) {
        out.println("\t<a href=\"" + application.getContextPath() + "/" + s + "\">" + s + "</a>");
    }
    else {
        out.println("\t" + s);
    }
    out.println("</td>");
}
%>
    </table>
</div>