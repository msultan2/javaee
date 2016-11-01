// SCJS 023 START

function init_password_update(){
    create_change_password_button();
}

function create_change_password_button(){
    $("#change-password-dialog")
    .find("form")
    .validate({
        highlight: function(element, errorClass){
            $(element).addClass("error");
        }
    });
    $(".update-password").button().bind({
        "click":function(){
                var dialog = $("#change-password-dialog");
                var form = dialog.find("form");
                if(form.valid()){
                    $.post("CurrentUserManager",
                        form.serialize(),
                        function(data){
                            if(data.success){
                                display_information_dialog_and_redirect_home("Password changed successfully", "Password changed successfully.");
                                form[0].reset();
                                $(".message").text("").removeClass("error");
                            } else {
                                $(".message").text(data.message).addClass("error");
                            }
                        },
                        "json");
                }
            }
    });
}

function display_information_dialog_and_redirect_home(title, message, callback){
    var dialog = $("<div/>").append($("<p/>").text(message));
    dialog.dialog({
        title:title,
        modal:true,
        width:"600px",
        buttons: {
            "OK": function(){
                if(callback != undefined){
                    callback();
                }
                dialog.dialog("destroy");
                window.location.href="Home";
            }
        }
    });
}
// SCJS 023 END