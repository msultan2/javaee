function init_instation_user(){
    $("#instation-user-link").attr("href","#");
    create_instation_user_table();
    create_new_user_button();
}

function create_instation_user_table(){
    var logical_group_index = 4;
    var recordType = "Instation Users";
    var table = $("#instation-user-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var instation_user_datatable_config = create_instation_user_datatable_config(filter_div);
    datatables['instation-user-table'] = table.dataTable(instation_user_datatable_config);
    var datatable = datatables['instation-user-table'];

    var logical_group_options = [];
    var role_options = [];
    $.post("CurrentUserManager",
    {
        action:"user"
    },
    function(data){
        logical_group_options = [];
        for(var i = 0; i <data.data.logical_groups.length; i++){
            logical_group_options.push("["+data.data.logical_groups[i]+"]");
        }
        // Quick filter
        create_quick_filter(table, logical_group_options, logical_group_index, recordType, table_headers);
        role_options = [];
        for(var j = 0; j <data.data.roles.length; j++){
            role_options.push("["+data.data.roles[j]+"]");
        }
        $(".datatable-buttons", table.closest(".dataTables_wrapper"))
        .append(create_csv_download_button({
            filter_div:filter_div,
            datatable:datatable,
            ajax_url:instation_user_datatable_config.sAjaxSource
        }))
        .append(create_show_hide_column_button({
            dialog:$("#instation-users-show-hide-column-dialog"),
            headers:table_headers,
            datatable:datatable,
            aoColumnDefs:instation_user_datatable_config.aoColumnDefs
        }))
        .append(create_filter_button({
            dialog:$("#instation-users-filter-dialog"),
            filter_div:filter_div,
            datatable:datatable,
            datatable_headers:table_headers,
            logical_group_index:logical_group_index,
            recordType: recordType,
            column_filter_definitions:[{
                "target":0,
                "type":"string"
            },
            {
                "target":1,
                "type":"string"
            },
            {
                "target":2,
                "type":"string"
            },
            {
                "target":3,
                "type":"enum",
                "options": role_options
            },
            {
                "target":logical_group_index,
                "type":"enum",
                "options": logical_group_options
            }
            ]
        }));
    },
    "json");
}

function create_new_user_button(){    
    $(".new-user-button").button().bind({
        "click":display_new_user_dialog
    });
}

function display_new_user_dialog(){

    $.post("CurrentUserManager",
    {
        action:"visible_roles"
    },
    function(data){
        $("#role-select").empty();
        for(var i = 0; i <data.data.length; i++){
            $("#role-select").append($("<div/>")
                .append($("<label/>").text(data.data[i]))
                .append($("<input type='checkbox' checked='checked'>")
                    .val(data.data[i])
                    .attr("name","role_names")
                    .addClass("required"))
                );
        }
        $("#role-select").append($("<hr/>").addClass("hr-clear-float"));
    },
    "json");

    $.post("CurrentUserManager",
    {
        action:"visible_logical_groups"
    },
    function(data){
        $("#logical-group-select").empty();
        for(var i = 0; i <data.data.length; i++){
            $("#logical-group-select").append($("<div/>")
                .append($("<label/>").text(data.data[i]))
                .append($("<input type='checkbox' checked='checked'>")
                    .val(data.data[i])
                    .attr("name","logical_group_names")
                    .addClass("required"))
                );
        }
        $("#logical-group-select").append($("<hr/>").addClass("hr-clear-float"));
    },
    "json");

    $(".message").text("").removeClass("error");

    var dialog = $("#new-user-dialog");
    dialog.dialog({
        modal:true,
        width: "600px",
        buttons: {
            "Add new user": function(){
                var form = dialog.find("form");

                form.validate({
                    messages: {
                        logical_group_names: {
                            required: "At least one logical group must be selected."
                        },
                        role_names: {
                            required: "At least one role must be selected."
                        },
                        email:{
                           email: "Invalid email address",
                           required: "You can't leave this empty"
                        }
                    }
                });

                if(form.valid()){
                    $.post("InstationUserManager",
                        form.serialize(),
                        function(data){
                            if(data.success){
                                datatable_refresh();
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

function create_instation_user_datatable_config(filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.sAjaxSource = "InstationUserDatatable";
    datatable_config.aoColumnDefs = [{
        "sClass":"text",
        "aTargets":[0,1]
    },
    {
        "sClass":"email",
        "aTargets":[2]
    },
    {
        "bSortable":false,
        "aTargets":[3,4,5,6]
    },
    {
        "sClass":"center",
        "aTargets":[5,6]
    },
    {
        "sWidth":"300px",
        "aTargets":[3,4]
    },
    {
        "sWidth":"20px",
        "aTargets":[5]
    },
    {
        "sWidth":"100px",
        "aTargets":[6]
    },
    {
        "aTargets": [3,4],
        "bVisible": false
    }];

    datatable_config.fnRowCallback = function(nRow, aData, iDisplayIndex){
        if (aData[5] == "false"){
            $(nRow).addClass("removed");
        } else {
            $(nRow).addClass("added");
        }
    }

    datatable_config.aoColumns = [
    null,
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return source[3].replace(/\] \[/g, "] <br/> [");
        }
    },
    {
        "mDataProp":function(source, type, val){            
            return source[4].replace(/\] \[/g, "] <br/> [");
        }
    },
    {
        "mDataProp":function(source, type, val){
            return "<a class='view-user' title='View ["+source[1]+"]' message='View ["+source[1]+"]' username='"+source[1]+"'>view</a>";
        }
    },
    {
        "mDataProp":function(source, type, val){
            if(source[5] == "false") {
                return "<a class='activate-user add' title='Activate user ["+source[1]+"]?' message='Are you sure you wish to activate user ["+source[1]+"]?' username='"+source[1]+"'>Activate</a>";
            } else {
                return "<a class='deactivate-user remove' title='Deactivate user ["+source[1]+"]?' message='Are you sure you wish to deactivate user ["+source[1]+"]?' username='"+source[1]+"'>Deactivate</a>";
            }
        }
    }
    ];

    var jeditable_config_email = common_jeditable_configuration();
    jeditable_config_email.callback = function(sValue, y){
        var aPos = datatables['instation-user-table'].fnGetPosition(this);
        datatables['instation-user-table'].fnUpdate('Saving...', aPos[0], aPos[1]);
        var data = $.parseJSON(sValue);
        if(!data.success){
            display_information_dialog("Unable to update user email", data.message);
        } 
    };
   
    jeditable_config_email.onsubmit = function(settings, td){
        var form = $(td).find("form");
        var input = $(td).find("input");
        input.attr("name", "current-edit");
        $(form).validate({
            rules:{
                "current-edit":{
                    required: true,
                    email: true
                }
            },
            highlight: function(element, errorClass){
                $(element).addClass("error");
            }
        });
        return ($(form).valid());
    };
    jeditable_config_email.submitdata = function(){
        return {
            username: datatables['instation-user-table'].fnGetData(this.parentNode)[1],
            column: datatables['instation-user-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };

    var jeditable_config = common_jeditable_configuration();
    jeditable_config.callback = function(sValue, y){
        var aPos = datatables['instation-user-table'].fnGetPosition(this);
        datatables['instation-user-table'].fnUpdate("Saving...", aPos[0], aPos[1]);
        var data = $.parseJSON(sValue);
        if(!data.success){
            display_information_dialog("Unable to update user", data.message);
        }
    };
    jeditable_config.onsubmit = function(settings, td){
        var form = $(td).find("form");
        var input = $(td).find("input");
        input.attr("name", "current-edit");
        $(form).validate({
            rules:{
                "current-edit":{
                    required: true
                }
            },
            highlight: function(element, errorClass){
                $(element).addClass("error");
            }
        });
        return ($(form).valid());
    };
    jeditable_config.submitdata = function(){
        return {
            username: datatables['instation-user-table'].fnGetData(this.parentNode)[1],
            column: datatables['instation-user-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };

    datatable_config.fnDrawCallback = function(){
        $('td.text', datatables['instation-user-table'].fnGetNodes()).editable('InstationUserManager', jeditable_config);
        $('td.email', datatables['instation-user-table'].fnGetNodes()).editable('InstationUserManager', jeditable_config_email);
        create_datatable_links(datatables['instation-user-table'], {}, {});
    };

    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";

    datatable_config.sAjaxSource = "InstationUserDatatable";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}



