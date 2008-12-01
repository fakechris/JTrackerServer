<%-- 
    Document   : upload
    Created on : 17-Nov-2008, 17:42:52
    Author     : bo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<script type="text/javascript" src="resources/jquery_1.2.6/jquery-1.2.6.js"></script>
<script type="text/javascript" src="resources/jquery_1.2.6/jquery.form.js"></script>
<script type="text/javascript" src="resources/behaviour/upload.js"></script>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Upload</title>
    </head>
    <body>
        <jsp:include page="header.jsp">
            <jsp:param name="caller" value="News" />
        </jsp:include>
        <!-- do some checking on size of torrentfile in javascript -->
        <form name="uploadForm" action="" method="POST" enctype="multipart/form-data">
            <input type="file" name="torrentFile" value="" />
            <input type="text" name="torrentName" value="" />
            <textarea name="torrentDescription" rows="4" cols="20">
            </textarea>
            <input type="submit" value="Submit" name="torrentSubmit" />
        </form>
    </body>
</html>
