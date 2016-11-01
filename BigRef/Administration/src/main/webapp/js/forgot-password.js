function init_forgot_password(){
    $("a#forgotPassword").click(function(){
        display_forgot_password_dialog();
    });
}

function display_forgot_password_dialog(){
    var dialog = $("#forgot-password-dialog");
    var form = dialog.find("form");
    dialog.dialog({
        modal:true,
        width: "600px",
        buttons: {            
            "Submit": function(){
                form.validate({
                    messages: {
                        email:{
                            email: "Invalid email address"
                        }
                    }
                });
                if(form.valid()){

                    $("form :input").attr('readonly', 'readonly');
                    $(".ui-dialog-buttonpane").hide();
                    $.post("ActivationManager ",
                        form.serialize(),
                        function(data){
                            if(data.success){
                                dialog.dialog("close");
                                display_information_dialog("Email sent", "Password has been sent to your registered email address.");
                                $("form :input").removeAttr("readonly");
                                clearDialog();
                            } else {
                                $(".message").text(data.message).addClass("error");
                                $(".ui-dialog-buttonpane").show();
                                $("form :input").removeAttr("readonly");
                            }
                        },
                        "json");
                }
            },
            "Cancel": function(){
                $(".message").text("").removeClass("error");
                clearDialog();
                $("form :input").removeAttr("readonly");
                dialog.dialog("close");
            }
        }
    });
}

function clearDialog(){
    $("#username").val('');
    $("#email").val('');
}

function display_information_dialog(title, message, callback){
    var dialog = $("<div/>").append($("<p/>").text(message));
    dialog.dialog({
        title:title,
        modal:true,
        width:"600px",
        buttons: {
            "OK": function(){
                dialog.dialog("destroy");
            }
        }
    });
}