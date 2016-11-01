function init_instation_user(username){
    create_instation_user_role_table(username);
    create_change_password_button();
    create_change_timezone_button();
    create_change_passord_expiry_days_button();
    display_current_user_details(username);
}

function display_current_user_details(username){
    $.post("CurrentUserManager",
    {
        action:"user"
    },
    function(data){
        if(data.success){
            $("#current-user-name").text(data.data.user_details.full_name);
            $("#current-user-username").text(data.data.user_details.username);
            $("#current-user-timezone").text(data.data.user_details.timezone_name);
            $("#current-user-email").text(data.data.user_details.email_address);
            $("#current-user-expiry-days").text(data.data.user_details.expiry_days);

            var remaining_days = data.data.user_details.remaining_days;
            if (remaining_days == null) {
                $("#current-user-remaining-days").text("---");
            } else if (remaining_days >= 1) {
                $("#current-user-remaining-days").text(Math.floor(remaining_days));
            } else if (remaining_days >= 0) {
                $("#current-user-remaining-days").text("Today");
            } else if (remaining_days > -1) {
                $("#current-user-remaining-days").text("Password expired: Today");
            } else {
                 $("#current-user-remaining-days").text("Password expired: " + (Math.abs(Math.floor(remaining_days))));
            }

            var roles = data.data.roles;
            $("#current-user-roles").empty();
            for(var i = 0; i < roles.length; i++){
                $("#current-user-roles").append($("<li/>").text(roles[i]));
            }
            var logical_groups = data.data.logical_groups;
            $("#current-user-logical-groups").empty();
            for(var i = 0; i < logical_groups.length; i++){
                $("#current-user-logical-groups").append($("<li/>").text(logical_groups[i]));
            }
            var time_zones = data.data.time_zones;
            $("#timezone_name").empty();
            for(var i = 0; i < time_zones.length; i++){
                $("#timezone_name").append($("<option/>").text(time_zones[i]));
            }
        }
    },
    "json");
    // SCJS 015 END
}

function create_change_passord_expiry_days_button(){
    $("#change-password-expiry-days-dialog")
    .find("form")
    .validate({
        rules:{
            expiry_days: {
                required:true,
                digits: true,
                range:[1, 100]
            }
        }
    });
    $("#change-password-expiry-days").button().bind({
        "click":display_change_password_expiry_days_dialog
    });
}

function display_change_password_expiry_days_dialog(){
    $(".message").text("").removeClass("error");
    var dialog = $("#change-password-expiry-days-dialog");
    dialog.dialog({
        modal:true,
        width: "600px",
        buttons: {
            "Change days": function(){
                var form = dialog.find("form");
                if(form.valid()){
                    $.post("CurrentUserManager",
                        form.serialize(),
                        function(data){
                            if(data.success){
                                display_information_dialog("Password expiry days change", "Password expiry days is changed successfully.");
                                display_current_user_details();
                                dialog.dialog("close");
                            } else {
                                $(".message").text(data.message).addClass("error");
                            }
                        },
                        "json");
                }
            },
            "Cancel": function(){
                dialog.dialog("close");
            }
        }
    });
}

function create_change_timezone_button(){
    $("#change-timezone-dialog")
    .find("form")
    .validate({
        highlight: function(element, errorClass){
            $(element).addClass("error");
        }
    });
    $("#change-timezone-button").button().bind({
        "click":display_change_timezone_dialog
    });
}

function display_change_timezone_dialog(){
    $(".message").text("").removeClass("error");
    var dialog = $("#change-timezone-dialog");
    dialog.dialog({
        modal:true,
        width: "600px",
        buttons: {
            "Change timezone": function(){
                var form = dialog.find("form");
                if(form.valid()){
                    $.post("CurrentUserManager",
                        form.serialize(),
                        function(data){
                            if(data.success){
                                display_information_dialog("Timezone changed successfully", "Timezone changed successfully.");                                
                                display_current_user_details();
                                dialog.dialog("close");
                            } else {
                                $(".message").text(data.message).addClass("error");
                            }
                        },
                        "json");
                }
            },
            "Cancel": function(){
                dialog.dialog("close");
            }
        }
    });
}

function create_change_password_button(){
    $("#change-password-dialog")
    .find("form")
    .validate({
        highlight: function(element, errorClass){
            $(element).addClass("error");
        }
    });
    $("#change-password-button").button().bind({
        "click":display_new_user_dialog
    });
}

function display_new_user_dialog(){
    $(".message").text("").removeClass("error");
    var dialog = $("#change-password-dialog");
    dialog.dialog({
        modal:true,
        width: "600px",
        buttons: {
            "Change password": function(){
                var form = dialog.find("form");
                if(form.valid()){
                    $.post("CurrentUserManager",
                        form.serialize(),
                        function(data){
                            if(data.success){
                                display_information_dialog("Password changed successfully", "Password changed successfully.");
                                dialog.dialog("close");
                            } else {
                                $(".message").text(data.message).addClass("error");
                            }
                        },
                        "json");
                }
            },
            "Cancel": function(){
                dialog.dialog("close");
            }
        }
    });
}

function create_instation_user_role_table(username){
    datatables['instation-user-role-table'] = $("#instation-user-role-table").dataTable(create_instation_user_role_datatable_config(username));
}

function create_instation_user_role_datatable_config(username){
    var datatable_config = common_datatable_configuration();
    datatable_config.sAjaxSource = "InstationUserRoleDatatable";
    datatable_config.aoColumnDefs = [{
        "sClass":"text",
        "aTargets":[0,1]
    },{
        "sClass":"center",
        "aTargets":[2]
    }];

    datatable_config.aoColumns = [
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            if(source[2] == undefined){
                return "<a class='add-role' title='Add role ["+source[0]+"]?' rolename='"+source[0]+"' >Add</a>";
            } else {
                return "<a class='remove-role' title='Remove role ["+source[0]+"]?' rolename='"+source[0]+"' >Remove</a>";
            }
        }
    }
    ];

    datatable_config.fnServerParams = function(aoData){
        aoData.push({
            "name":"username",
            "value":username
        });
    };

    datatable_config.fnDrawCallback = function(){
        create_datatable_links(datatables['instation-user-role-table'], {}, {});
    };

    return datatable_config;
}



