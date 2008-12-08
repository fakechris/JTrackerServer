<%-- 
    Document   : browse
    Created on : 14-Nov-2008, 15:01:19
    Author     : bo
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
        <input type="text" name="searchField" value="" />
        <input type="submit" value="Submit" />
        <input type="checkbox" name="searchDescriptions" value="checked" checked />
        <input type="checkbox" name="includeDead" value="checked" />
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
