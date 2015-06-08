var appid = "dc0feacb-13c7-44c8-ad19-0acdd3c6a9dd";
var call = null;

// create a client object using the App ID value from Step 2
var client = new respoke.Client({
  appId: appid,
  developmentMode: true
});

// listen for the 'connect' event
client.listen('connect', function() {
    $("#status").html("Connected to Respoke!");
});

// listen for incoming messages
client.listen('message', function(evt) {
    $("#messages").append("<li>"+evt.message.message+"</li>");
});

// listen for and answer incoming calls
client.listen('call', function(evt) {
     call = evt.call;
     if (call.initiator !== true) {
         call.answer({constraints: {audio: true, video: false}});
         call.listen('hangup', function() {
            call = null;
         });
     }
});

// now connect when the user clicks the 'Connect' button
$("#doLogin").click(function() {
    var endpoint =  $("#endpoint").val();
    client.connect({
         endpointId: endpoint
    });
});

// send a message to the far-end party
$("#sendMessage").click(function(){

    // get the recipient name
    var remote = $("#remoteId").val();

    // make an endpoint for that recipient
    var endpoint = client.getEndpoint({"id" : remote});

    // grab the text to send
    var messageText = $("#textToSend").val();

    // send it
    endpoint.sendMessage({"message" : messageText});
});

// Create a call
$("#makeCall").click(function() {
    var endpoint = client.getEndpoint({"id" : $("#remoteId").val()});
    call = endpoint.startAudioCall();
});

// Hang up the call
$("#endCall").click(function() {
    if (call) {
        call.hangup();
        call = null;
    }
});