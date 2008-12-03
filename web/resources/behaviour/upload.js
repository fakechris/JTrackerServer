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

        // success function
        success: processUploadResult
    });
});

function ProcessUploadResult(responseXML) {
    // populate some div's with the information received from the server
    var error = $('error reason', responseXML).text();
    var warning = $('warning reason', responseXML).text();
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