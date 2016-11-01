// SCJS 018 START

function init_broadcast_messages(){
    $("#broadcast-message-link").attr("href","#");
    create_broadcast_messages_table();
    create_new_broadcast_message_button();
}

function create_broadcast_messages_table(){
    var logical_group_index = 3;
    var recordType = "broadcast messages";
    var table = $("#broadcast-message-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var broadcast_message_datatable_config = create_broadcast_message_datatable_config(filter_div);
    datatables['broadcast-message-table'] = table.dataTable(broadcast_message_datatable_config);
    var datatable = datatables['broadcast-message-table'];

    var logical_group_options = [];
    $.post("CurrentUserManager",
    {
        action:"user"
    },
    function(data){
        logical_group_options = [];
        for(var i = 0; i <data.data.logical_groups.length; i++){
            logical_group_options.push("["+data.data.logical_groups[i]+"]");
        }
        create_quick_filter(table, logical_group_options, logical_group_index, recordType, table_headers);
        $(".datatable-buttons", table.closest(".dataTables_wrapper"))
        .append(create_csv_download_button({
            filter_div:filter_div,
            datatable:datatable,
            ajax_url:broadcast_message_datatable_config.sAjaxSource
        }))
        .append(create_show_hide_column_button({
            dialog:$("#broadcast-message-show-hide-column-dialog"),
            headers:table_headers,
            datatable:datatable,
            aoColumnDefs:broadcast_message_datatable_config.aoColumnDefs
        }))
        .append(create_filter_button({
            dialog:$("#broadcast-message-filter-dialog"),
            filter_div:filter_div,
            datatable:datatable,
            logical_group_index:logical_group_index,
            recordType: recordType,
            datatable_headers:table_headers,
            column_filter_definitions:[{
                "bVisible": false,
                "aTargets":[0]
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
                "target":logical_group_index,
                "type":"enum",
                "options": logical_group_options
            }
            ]
        }));

    },
    "json");
}

function create_broadcast_message_datatable_config(filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [{
        "sClass":"text",
        "aTargets":[1]
    },
    {
        "sClass":"textarea",
        "aTargets":[2]
    },
     {
        "aTargets": [0,3],
        "bVisible": false
    },
    {
        "sClass":"center",
        "aTargets":[4]
    },   
    {
        "sWidth":"20px",
        "aTargets":[4]
    },
    {
        "aTargets": [4],
        "bSortable": false
    },];

    datatable_config.aoColumns = [
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return "<pre>"+source[2]+"</pre>";
        }
    },
    {
        "mDataProp":function(source, type, val){
            return source[3].replace(/\] \[/g, "] <br/> [");
        }
    },
    {
        "mDataProp":function(source, type, val){
            return "<a class='delete' title='Delete ["+source[1]+"]' message='Are you sure you wish to delete message ["+source[1]+"]?' message_id ='"+source[0]+"'>delete</a>";
        }
    }
    ];

    var jeditable_config_text = common_jeditable_configuration();
    jeditable_config_text.callback = function(sValue, y){
        var aPos = datatables['broadcast-message-table'].fnGetPosition(this);
        datatables['broadcast-message-table'].fnUpdate('Saving...', aPos[0], aPos[1]);
        var data = $.parseJSON(sValue);
        if(!data.success){
            display_information_dialog("Unable to update span", data.message);
        }
    };
    jeditable_config_text.onsubmit = function(settings, td){
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
    jeditable_config_text.submitdata = function(){
        return {
            message_id: datatables['broadcast-message-table'].fnGetData(this.parentNode)[0],
            column: datatables['broadcast-message-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };

    var jeditable_config_textarea = common_jeditable_configuration();
    jeditable_config_textarea.callback = function(sValue, y){
        var td = $(this).closest("td")[0];
        var aPos = datatables['broadcast-message-table'].fnGetPosition(td);
        datatables['broadcast-message-table'].fnUpdate('Saving...', aPos[0], aPos[1]);        
        var data = $.parseJSON(sValue);
        if(!data.success){
            display_information_dialog("Unable to update route", data.message);
        }
    };
    jeditable_config_textarea.onsubmit = function(settings, td){
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
    jeditable_config_textarea.submitdata = function(){
        var td = $(this).closest("td")[0];
        return {
            message_id: datatables['broadcast-message-table'].fnGetData(td.parentNode)[0],
            column: datatables['broadcast-message-table'].fnGetPosition(td)[2],
            action: "update"
        };
    };
    jeditable_config_textarea.type = "textarea";
    jeditable_config_textarea.height = "100px",
    jeditable_config_textarea.width = "500px",

    datatable_config.fnDrawCallback = function(){
        $("pre", 'td.textarea', datatables['broadcast-message-table'].fnGetNodes()).editable('BroadcastMessageManager',jeditable_config_textarea);
        $('td.text', datatables['broadcast-message-table'].fnGetNodes()).editable('BroadcastMessageManager', jeditable_config_text);

        create_datatable_links(datatables['broadcast-message-table'], {}, {
            delete_callback:function(element, callbacks){
                $.post("BroadcastMessageManager",
                {
                    action:"delete",
                    message_id:element.attr("message_id")
                },
                function(data){
                    datatable_refresh();
                })
                .error(function(){
                    alert("error");
                });

                callbacks.complete();
            }

        });
    };

    datatable_config.sAjaxSource = "BroadcastMessagesDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}

function create_new_broadcast_message_button(){
    $('.new-broadcast-message-button').button().bind({
        'click': display_new_broadcast_message_dialog
    });
}

function display_new_broadcast_message_dialog(){
    
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

    var dialog = $("#new-broadcast-message-dialog");
    dialog.dialog({
        modal:true,
        width: "600px",
        buttons: {
            "Add new broadcast message": function(){
                var form = dialog.find("form");

                form.validate({
                    messages: {
                        logical_group_names: {
                            required: "At least one logical group must be selected."
                        }
                    }
                });

                if(form.valid()){
                    $.post("BroadcastMessageManager",
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

// SCJS 018 END