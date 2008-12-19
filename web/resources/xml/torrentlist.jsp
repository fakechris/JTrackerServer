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
contentType="application/xml"
pageEncoding="UTF-8"
import="com.tracker.backend.webinterface.TorrentList"
import="com.tracker.backend.webinterface.xml.XMLTorrentList"
import="java.util.Map"
import="java.io.PrintWriter"
%>
<%
// try to eat the extra whitespaces that gets into the output
out.clearBuffer();

TorrentList tList = new XMLTorrentList();
Map<String,String[]> requestMap = request.getParameterMap();
tList.printTorrentList(requestMap, new PrintWriter(out));
%>
