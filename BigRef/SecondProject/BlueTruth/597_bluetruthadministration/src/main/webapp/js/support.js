function init_support_page(){
    $.post("/SupportDatatable", function(data){
        var email_section = $("#contact-email-section");
        var telephone_section = $("#contact-telephone-section");
        
        for(row in data.aaData){
            if(data.aaData[row][3] == "email"){
                email_section.append($("<div class='contact-email'/>")
                .append($("<h2/>")
                    .append($("<span class='title'>").text(data.aaData[row][0]))
                    .append($("<span>").text(" - "))
                    .append($("<span class='email-address'>").text(data.aaData[row][1])))
                    .append($("<p/>").text(data.aaData[row][2]))
                    
                );
            }
            if(data.aaData[row][3] == "phone"){
                telephone_section.append($("<div class='contact-telephone'/>")
                .append($("<h2/>")
                    .append($("<span class='title'>").text(data.aaData[row][0]))
                    .append($("<span>").text(" - "))
                    .append($("<span class='telephone-number'>").text(data.aaData[row][1])))
                    .append($("<p/>").text(data.aaData[row][2]))
                    
                    );
            }
        }
        if(!email_section.is(":empty")){
            email_section.prepend($("<h1>Email</h1>"));
        }
        if(!telephone_section.is(":empty")){
            telephone_section.prepend($("<h1>Telephone</h1>"));
        }
    }, "json");


}


