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
        <form name="xmlUploadForm" action="xml/TakeUpload" method="POST" enctype="multipart/form-data">
            <em>TorrentFile:</em> <input type="file" name="torrentFile" value="" /> <br />
            <em>Torrent Name:</em> <input type="text" name="torrentName" value="" /> <br />
            <em>Torrent Description:</em> <textarea name="torrentDescription" rows="4" cols="20">
            </textarea> <br />
            <input type="submit" value="Submit" name="torrentSubmit" />
        </form>

        <div id="content">
            <div id="errorDiv" style="display: none"></div>
            <div id="warningDiv" style="display: none"></div>
            <div id="redownloadDiv" style="display: none"></div>
        </div>
    </body>
</html>
