function init_logical_groups(){
    $("#logical-groups-link").attr("href","#");
    create_logical_groups_table();
    create_new_logical_group_button();
}

function create_new_logical_group_button(){
    $(".new-logical-group-button").button().bind({
        "click":display_new_logical_group_dialog
    });
}

function display_new_logical_group_dialog(){

    $(".message").text("").removeClass("error");

    var dialog = $("#new-logical-group-dialog");
    dialog.dialog({
        modal:true,
        width: "600px",
        buttons: {
            "Add new logical group": function(){
                var form = dialog.find("form");
                if(form.valid()){
                    $.post("LogicalGroupManager",
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

function create_logical_groups_table(){

    var table = $("#logical-group-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var logical_groups_datatable_config = create_logical_groups_datatable_config(filter_div);
    datatables['logical-group-table'] = table.dataTable(logical_groups_datatable_config);
    var datatable = datatables['logical-group-table'];

    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_csv_download_button({
        filter_div:filter_div,
        datatable:datatable,
        ajax_url:logical_groups_datatable_config.sAjaxSource
    }))
    .append(create_show_hide_column_button({
        dialog:$("#logical-groups-show-hide-column-dialog"),
        headers:table_headers,
        datatable:datatable,
        aoColumnDefs:logical_groups_datatable_config.aoColumnDefs
    }))
    .append(create_filter_button({
        dialog:$("#logical-groups-filter-dialog"),
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


function create_logical_groups_datatable_config(filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [{
        "sClass":"text",
        "aTargets":[0,1]
    },
    {
        "sClass":"center",
        "aTargets":[2,3]
    },
    {
        "sWidth":"20px",
        "aTargets":[2,3]
    }];

    datatable_config.aoColumns = [
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return "<a class='view-group' title='View ["+source[0]+"]' message='View ["+source[0]+"]' logicalgroupid='"+source[0]+"' href='LogicalGroup?group="+encodeURIComponent(escape(source[0]))+"'>view</a>";
        }
    },
    {
        "mDataProp":function(source, type, val){
            return "<a class='delete-group' title='Delete ["+source[0]+"]' message='Delete ["+source[0]+"]' logicalgroupid='"+source[0]+"'>delete</a>";
        }
    }
    ];

    var jeditable_config = common_jeditable_configuration();
    jeditable_config.callback = function(sValue, y){
        var aPos = datatables['logical-group-table'].fnGetPosition(this);
        datatables['logical-group-table'].fnUpdate(sValue, aPos[0], aPos[1]);
        var data = $.parseJSON(sValue);
        if(!data.success){
            display_information_dialog("Unable to update logical group", data.message);
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
            logical_group_name: datatables['logical-group-table'].fnGetData(this.parentNode)[0],
            column: datatables['logical-group-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };

    datatable_config.fnDrawCallback = function(){
        $('td.text', datatables['logical-group-table'].fnGetNodes()).editable('LogicalGroupManager',jeditable_config);
        create_datatable_links(datatables['logical-group-table'], {}, {});
    };

    datatable_config.sAjaxSource = "LogicalGroupDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}



