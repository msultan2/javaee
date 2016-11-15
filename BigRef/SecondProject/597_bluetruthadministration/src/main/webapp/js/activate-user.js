function init_activate_user(action, activation_key){
    $.post("ActivationManager",
    {
        action: action,
        act_key: activation_key
    },
    function(data){
        if(data.success){
            $("#activation-status").text(data.message);
        } else {
            $("#activation-status").text("ERROR activating user");
    }
    },
    "json");
}

