function init_routes(){
    $("#routes-link").attr("href","#");
    create_routes_table();
    create_new_route_button();
}

function create_routes_table(){
    var logical_group_index = 2;
    var recordType = "Routes";
    var table = $("#routes-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var routes_datatable_config = create_routes_datatable_config(filter_div);
    datatables['routes-table'] = table.dataTable(routes_datatable_config);
    var datatable = datatables['routes-table'];


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
            ajax_url:routes_datatable_config.sAjaxSource
        }))
        .append(create_show_hide_column_button({
            dialog:$("#route-show-hide-column-dialog"),
            headers:table_headers,
            datatable:datatable,
            aoColumnDefs:routes_datatable_config.aoColumnDefs
        }))
        .append(create_filter_button({
            dialog:$("#route-filter-dialog"),
            filter_div:filter_div,
            datatable:datatable,
            logical_group_index:logical_group_index,
            recordType: recordType,
            datatable_headers:table_headers,
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
                "type":"enum",
                "options": logical_group_options
            }
            ]
        }));

    },
    "json");
}

function create_new_route_button() {
    $(".new-route-button").button().bind({
        "click":display_new_route_dialog
    });
}

function display_new_route_dialog(){

    $.post("CurrentUserManager",
    {
        action:"visible_logical_groups"
    },
            function (data) {
                add_logical_groups('#logical_group_names', data.data);
            },
            "json");

    $(".message").text("").removeClass("error");

    var dialog = $("#new-route-dialog");
    dialog.dialog({
        modal:true,
        width: "600px",
        buttons: {
            "Add new route": function(){
                var form = dialog.find("form");

                form.validate({
                    messages: {
                        logical_group_names: {
                            required: "At least one logical group must be selected."
                        }
                    }
                });

                if (form.valid()) {
                    $.post("RouteManager",
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

function create_routes_datatable_config(filter_div){
    var datatable_config = common_datatable_configuration();    
    datatable_config.aoColumnDefs = [{
        "sClass":"text",
        "aTargets":[0,1,2]
    },
    {
        "sClass":"center",
        "aTargets":[3,4]
    },
    {
        "sWidth":"20px",
        "aTargets":[3,4]
    },
    {
        "aTargets": [2],
        "bVisible": false
    }];

    datatable_config.aoColumns = [
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return source[2].replace(/\] \[/g, "] <br/> [");
        }
    },
    {
        "mDataProp":function(source, type, val){
            return "<a class='view-route' title='View ["+source[0]+"]' routename='"+source[0]+"' href='Route?route="+encodeURIComponent(escape(source[0]))+"'>view</a>";
        }
    },
    {
        "mDataProp":function(source, type, val){
            return "<a class='delete' title='Delete ["+source[0]+"]' message='Are you sure you wish to delete route ["+source[1]+"]?' routename='"+source[0]+"'>delete</a>";
        }
    }
    ];

    var jeditable_config = common_jeditable_configuration();
    jeditable_config.callback = function(sValue, y){
        var aPos = datatables['routes-table'].fnGetPosition(this);
        datatables['routes-table'].fnUpdate('Saving...', aPos[0], aPos[1]);
        var data = $.parseJSON(sValue);
        if(!data.success){
            display_information_dialog("Unable to update route", data.message);
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
            route_name: datatables['routes-table'].fnGetData(this.parentNode)[0],
            column: datatables['routes-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };

    datatable_config.fnDrawCallback = function(){
        $('td.text', datatables['routes-table'].fnGetNodes()).editable('RouteManager',jeditable_config);
        create_datatable_links(datatables['routes-table'], {}, {
            delete_callback:function(element, callbacks){
                $.post("RouteManager",
                {
                    action:"delete",
                    route_name:element.attr("routename")
                },
                function(data){
                    datatable_refresh();
                    dialog.dialog("close");
                })
                .error(function(){
                    alert("error");
                });

                callbacks.complete();
            }

        });
    };

    datatable_config.sAjaxSource = "RouteDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}



