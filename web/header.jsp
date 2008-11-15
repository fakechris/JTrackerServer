<%-- 
    Document   : header
    Created on : 14-Nov-2008, 13:46:20
    Author     : bo
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