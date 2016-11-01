var current_username = "";
var current_edit_user_roles = false;

function init_instation_user(username, edit_user_roles){

    current_username = username;
    current_edit_user_roles = edit_user_roles;

    if(edit_user_roles == undefined){
        edit_user_roles = false;
    }
    if(edit_user_roles){
        create_instation_user_role_table(username);
    }
    // SCJS 015 START - NEW METHOD
    create_change_brand_button();
    // SCJS 015 END
    create_change_timezone_button();
    create_change_passord_expiry_days_button();
    display_user_details(username, edit_user_roles);
}

function display_user_details(username, edit_user_roles){
    $.post("InstationUserManager",
    {
        action:"user",
        username:username
    },
    function(data){
        if(data.success){
            $("#current-user-name").text(data.data.user_details.full_name);
            $("#current-user-username").text(data.data.user_details.username);
            // SCJS 015 START
            $("#current-user-brand").text(data.data.user_details.brand);
            // SCJS 015 END
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

            if(!edit_user_roles){
                var roles = data.data.roles;
                $("#user-roles").empty();
                for(var i = 0; i < roles.length; i++){
                    $("#user-roles").append($("<li/>").text(roles[i]));
                }
                $("#instation-user-role-section").remove();
            } else {
               $("#user-roles-section").remove();
            }
            var logical_groups = data.data.logical_groups;
            $("#user-logical-groups").empty();
            for(var i = 0; i < logical_groups.length; i++){
                $("#user-logical-groups").append($("<li/>").text(logical_groups[i]));
            }
            var time_zones = data.data.time_zones;
            $("#timezone_name").empty();
            for(var i = 0; i < time_zones.length; i++){
                $("#timezone_name").append($("<option/>").text(time_zones[i]));
            }
            // SCJS 015 START
            var brands = data.data.brands;
            $("#brands").empty();
            for(var i = 0; i < brands.length; i++){
                $("#brands").append($("<option/>").text(brands[i]));
            }
            // SCJS 015 END
        }
    },
    "json");
}

// SCJS 015 START
function create_change_brand_button(){
    $("#change-brand-dialog")
    .find("form")
    .validate({
        highlight: function(element, errorClass){
            $(element).addClass("error");
        }
    });
    $("#change-brand-button").button().bind({
        "click":display_change_brand_dialog
    });
}

function display_change_brand_dialog(){
    $(".message").text("").removeClass("error");
    var dialog = $("#change-brand-dialog");
    dialog.dialog({
        modal:true,
        width: "600px",
        buttons: {
            "Change brand": function(){
                var form = dialog.find("form");
                if(form.valid()){
                    $.post("InstationUserManager",
                        form.serialize(),
                        function(data){
                            if(data.success){
                                display_information_dialog("Brand changed successfully", "Brand changed successfully.");
                                display_user_details(current_username, current_edit_user_roles);
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
// SCJS 015 END

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
                    $.post("InstationUserManager",
                        form.serialize(),
                        function(data){
                            if(data.success){
                                display_information_dialog("Timezone changed successfully", "Timezone changed successfully.");
                                display_user_details(current_username, current_edit_user_roles);
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
    var table = $("#instation-user-role-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var instation_user_role_datatable_config = create_instation_user_role_datatable_config(username,filter_div);
    datatables['instation-user-role-table'] = table.dataTable(instation_user_role_datatable_config);
    var datatable = datatables['instation-user-role-table'];

    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
        .append(create_csv_download_button({
            filter_div:filter_div,
            datatable:datatable,
            parameters : [{"name":"username", "value":username}],
            ajax_url:instation_user_role_datatable_config.sAjaxSource
        }))
        .append(create_show_hide_column_button({
            dialog:$("#instation-user-roles-show-hide-column-dialog"),
            headers:table_headers,
            datatable:datatable,
            aoColumnDefs:instation_user_role_datatable_config.aoColumnDefs
        }))
        .append(create_filter_button({
            dialog:$("#instation-user-roles-filter-dialog"),
            filter_div:filter_div,
            datatable:datatable,
            datatable_headers:table_headers,
            column_filter_definitions:[{
                "target":0,
                "type":"string"
            },
            {
                "target":1,
                "type":"string"
            }
            ]
        }));
}

function create_instation_user_role_datatable_config(username, filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.sAjaxSource = "InstationUserRoleDatatable";
    datatable_config.aoColumnDefs = [{
        "sClass":"text",
        "aTargets":[0,1]
    },{
        "sClass":"center",
        "aTargets":[2]
    },
    {
        "sWidth":"20px",
        "aTargets":[2]
    }];

    datatable_config.fnRowCallback = function(nRow, aData, iDisplayIndex){
        if (aData[2] == null){
            $(nRow).addClass("removed");
        } else {
            $(nRow).addClass("added");
        }
    }
    datatable_config.aaSorting = [[2, "asc"]];

    datatable_config.aoColumns = [
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            if(source[2] == undefined){
                return "<a class='add-role add' title='Add role ["+source[0]+"]?' username='"+username+"' rolename='"+source[0]+"' >Add</a>";
            } else {
                return "<a class='remove-role remove' title='Remove role ["+source[0]+"]?' username='"+username+"' rolename='"+source[0]+"' >Remove</a>";
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

    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
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
                    $.post("InstationUserManager",
                        form.serialize(),
                        function(data){
                            if(data.success){
                                display_information_dialog("Password expiry days change", "Password expiry days is changed successfully.");
                                display_user_details(current_username, current_edit_user_roles);
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