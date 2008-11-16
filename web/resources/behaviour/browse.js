/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

// behaviour for browse.jsp
function getTorrentList(searchParameters) {
    $.getJSON("torrentlist.json", searchParameters, function(json) {
        // run on successful completion
        $.each(json.name, function(i, name) {
            $("#torrentName").append("#" + i + " " +name + "<br />");
        });
        $.each(json.date, function(i, date) {
            $("#torrentDate").append("#" + i + " " +date + "<br />");
        });
        $("div:hidden").fadeIn("fast");
    });
}

$(document).ready(function() {
    getTorrentList({});

    $("#submit").click(function() {
        getTorrentList({
            searchField: $("#searchField").val(),
            searchDescriptions: $("#searchDescriptions").val(),
            includeDead: $("#includeDead").val()
        });
    });
});