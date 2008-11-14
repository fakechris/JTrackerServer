<%-- 
    Document   : index
    Created on : 13-Nov-2008, 19:01:27
    Author     : bo
--%>

<%@page
contentType="text/html"
pageEncoding="UTF-8"
import="javax.persistence.EntityManager"
import="javax.persistence.EntityManagerFactory"
import="javax.persistence.NoResultException"
import="javax.persistence.Persistence"
import="javax.persistence.Query"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Torent Tracker</title>
    </head>
    <body>
        <h1>Hello World!</h1>
        <div id="torrentlist">
            <%
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");
            EntityManager em = emf.createEntityManager();
            %>
        </div>
    </body>
</html>
