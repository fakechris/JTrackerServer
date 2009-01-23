/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

// behaviour for browse.jsp

/**
 * Grabs a XML torrentlist from the server
 */
function getTorrentList(searchParameters) {
    $.ajax({
        url: 'xml/torrentlist',
        type: 'GET',
        dataType: 'xml',
        error: displayError,
        success: processBrowseResult
    });
}

/**
 * processes the XML received from the server and populates the torrentlist.
 */
function processBrowseResult(xml) {
    //$("#torrentList").fadeOut("fast");
    $("#torrentName").empty();
    $("#torrentDate").empty();
    $(xml).find('torrent').each(function(index) {
        // list of all torrents including links to the relevant download.
        $("#torrentName").append("<a href=" + contextPath + "/Download?id=" 
            + $(this).attr("id") + ">" + "#" + index + " "
            + $(this).find("name").text() + "</a><br />");
        $("#torrentDate").append($(this).find("dateAdded").text() + "<br />");
    });
    //$("#torrentList").fadeIn("fast");
}

/**
 * displays an error in case AJAX failed horribly.
 */
function displayError(XMLHttpRequest, textStatus, errorThrown) {
    $("#torrentName").empty();
    $("#torrentDate").empty();
    $("#torrentName").append("An error occured while getting AJAX response.");
    $("#torrentDate").append(textStatus + " - " + errorThrown);
    //$("div:hidden").show();
}

$(document).ready(function() {
    // bind the loading gif to the global ajax events
    $("#loading").bind("ajaxSend", function() {
        $(this).show();
    }).bind("ajaxComplete", function() {
       $(this).hide();
    });

    // fill the torrentlist
    getTorrentList({});

    // bind the form to the processing
    $("#searchForm").ajaxForm({
        dataType: 'xml',
        success: processBrowseResult
    });
});