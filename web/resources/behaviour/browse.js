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
        type: 'GET',
        dataType: 'xml',
        error: displayError,
        success: processBrowseResult
    });
}

/**
 * processes the XML received from the server and populates the torrentlist.
 */
function processBrowseResult(reponseXML) {
    $(responseXML).label('torrent').each(function(index) {
        $("#torrentName").append("<a href=/Download?id=" + $(this).attr("id") +
            ">" + "#" + i + " " + $(this).find("name").text() + "</a><br />");
        $("#torrentDate").append($(this).find("dateAdded").text() + "<br />");
    });
    $("div:hidden").fadeIn("fast");
}

/**
 * displays an error in case AJAX failed horribly.
 */
function displayError() {
    $("#torrentName").append("An error occured while getting AJAX response.");
    $("div:hidden").show();
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

    // bind the submit button to the getTorrentlist function
    $("#submit").click(function() {
        getTorrentList({
            searchField: $("#searchField").val(),
            searchDescriptions: $("#searchDescriptions").val(),
            includeDead: $("#includeDead").val()
        });
    });
});