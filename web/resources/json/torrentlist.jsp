<%-- 
    Document   : torrentlist
    Created on : 14-Nov-2008, 21:00:56
    Author     : bo
--%>

<%@page
contentType="application/json"
pageEncoding="UTF-8"
import="com.tracker.backend.webinterface.json.JSONTorrentList"
import="java.util.Map"
import="java.io.PrintWriter"
%>
<%
JSONTorrentList jsonList = new JSONTorrentList();
Map<String,String[]> requestMap = request.getParameterMap();
jsonList.printTorrentList(requestMap, new PrintWriter(out));
%>
