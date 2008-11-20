<%--
    Document   : torrentlist
    Created on : 14-Nov-2008, 21:00:56
    Author     : bo
--%>
<%@page
contentType="application/xml"
pageEncoding="UTF-8"
import="com.tracker.backend.webinterface.xml.XMLTorrentList"
import="java.util.Map"
import="java.io.PrintWriter"
%>
<%
// try to eat the extra whitespaces that gets into the output
out.clearBuffer();

XMLTorrentList xmlList = new XMLTorrentList();
Map<String,String[]> requestMap = request.getParameterMap();
xmlList.printTorrentList(requestMap, new PrintWriter(out));
%>
