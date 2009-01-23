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
 * Clears the torrentlist - used for reading the space for new data or errors
 */
function emptyList() {
    $("#torrentName").empty();
    $("#torrentSeeds").empty();
    $("#torrentPeers").empty();
    $("#torrentCompleted").empty();
    $("#torrentDate").empty();
}

/**
 * processes the XML received from the server and populates the torrentlist.
 */
function processBrowseResult(xml) {
    emptyList();
    $(xml).find('torrent').each(function() {
        // list of all torrents including links to the relevant download.
        $("#torrentName").append("<a href=" + contextPath + "/Download?id=" 
            + $(this).attr("id") + "> "
            + $(this).find("name").text()
            + "</a><br />");
        $("#torrentSeeds").append($(this).find("numSeeders").text()
            + "<br />");
        $("#torrentPeers").append($(this).find("numLeechers").text()
            + "<br />");
        $("#torrentCompleted").append($(this).find("numCompleted").text()
            + "<br />");
        $("#torrentDate").append($(this).find("dateAdded").text()
            + "<br />");
    });
}

/**
 * displays an error in case AJAX failed horribly.
 */
function displayError(XMLHttpRequest, textStatus, errorThrown) {
    emptyList();
    $("#torrentName").append("An error occured while getting AJAX response.");
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