/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


// submit forminfo, get XML response
$(document).ready(function() {
    // bind to the form using ajaxforms
    $("#xmlUploadForm").ajaxForm({
        // expected content of server response
        dataType: 'xml',

        // error function
        error: displayError,
        // success function
        success: processUploadResult
    });
});

function processUploadResult(responseXML) {
    // populate some div's with the information received from the server
    var error = $('errorReason', responseXML).text();
    var warning = $('warningReason', responseXML).text();
    var redownload = $('redownload', responseXML).text();
    if(redownload == "true") {
        $("#redownloadDiv").append("<b>You must redownload the torrentfile.</b>");
        $("#redownloadDiv").show();
    }
    if(error != "") {
        $("#errorDiv").append(error);
        $("#errorDiv").show();
    }
    else {
        $("#errorDiv").append("Upload Successful.");
    }
    if(warning != "") {
        $("#warningDiv").append(warning);
        $("#warningDiv").show();
    }
}

/**
 * displays an error in case AJAX failed horribly.
 */
function displayError(XMLHttpRequest, textStatus, errorThrown) {
    var error = errorThrown.toString();
    $("#errorDiv").append("An error occured while getting AJAX response.<br />");
    $("#errorDiv").append(textStatus + " - " + error);
    $("div:hidden").show();
}