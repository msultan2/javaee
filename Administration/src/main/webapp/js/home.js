function init_home(){
    $("#broadcast-messages").hide();
    $.post("/HomeBroadcastMessagesDatatable", function(data){
        if(data.aaData.length > 0){
//            $("#broadcast-messages").append($("<h1/>").text("System Messages"));
            $("#links").css({"margin-bottom":"0px"});        
        for(row in data.aaData){
            //alert(data.aaData[row][0] + data.aaData[row][1]);
            $("#broadcast-messages").append($("<div/>").addClass("message")
            .append($("<h1/>").text(data.aaData[row][1]))
            .append($("<pre/>").text(data.aaData[row][2]))
            );
        }
        $("#broadcast-messages").show();
        } else {
           $("#broadcast-messages").remove();
        }
    }, "json");
    
}