function init_span(){
    
    create_span_table();
    create_new_span_button();
}

function create_span_table(){
    var logical_group_index = 8;
    var recordType = "Spans";
    var table = $("#span-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var span_datatable_config = create_span_datatable_config(filter_div);
    datatables['span-table'] = table.dataTable(span_datatable_config);
    var datatable = datatables['span-table'];

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
        // Quick filter
        create_quick_filter(table, logical_group_options, logical_group_index, recordType, table_headers);
        $(".datatable-buttons", table.closest(".dataTables_wrapper"))
        .append(create_csv_download_button({
            filter_div:filter_div,
            datatable:datatable,
            ajax_url:span_datatable_config.sAjaxSource
        }))
        .append(create_show_hide_column_button({
            dialog:$("#span-show-hide-column-dialog"),
            headers:table_headers,
            datatable:datatable,
            aoColumnDefs:span_datatable_config.aoColumnDefs
        }))
        .append(create_filter_button({
            dialog:$("#span-filter-dialog"),
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
                "type":"string"
            },
            {
                "target":3,
                "type":"integer"
            },
            {
                "target":4,
                "type":"integer"
            },
            {
                "target":5,
                "type":"integer"
            },
            {
                "target":6,
                "type":"integer"
            },
            {
                "target":7,
                "type":"integer"
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

function create_new_span_button(){
    jQuery.validator.addMethod("notEqualTo", function(elementValue,element,param){
        return elementValue != param;
    },"Value cannot be {0}");

    jQuery.validator.addMethod("lessThanVerySlow", function(stationary_val, element, very_slow_val){  
       return parseInt(stationary_val) < parseInt(very_slow_val);
    }, "error");

    jQuery.validator.addMethod("greaterThanStationaryAndLowerThanSlow", function(very_slow_val, element, param){
       return ((parseInt(very_slow_val) < parseInt(param.slow)) && (parseInt(very_slow_val) > parseInt(param.stationary)));
    },"error");

    jQuery.validator.addMethod("greaterThanVerySlowAndLowerThanModerate", function(slow_val, element, param){
       return ((parseInt(slow_val) < parseInt(param.moderate)) && (parseInt(slow_val) > parseInt(param.very_slow)));
    },"error");

    jQuery.validator.addMethod("greaterThanSlow", function(moderate_val, element, slow_val){
       return (parseInt(slow_val) < parseInt(moderate_val));
    },"error");

    $(".new-span-button").button().bind({
        "click":display_new_span_dialog
    });
}

function display_new_span_dialog(){

    $.post("CurrentUserManager",
    {
        action:"visible_logical_groups"
    },
    function(data){
                $("#logical-group-select").empty();
                add_logical_groups('#logical_group_names', data.data);
    },
    "json");

    $.post("DetectorManager",
    {
        "action":"list"
    },
    function(data){
        if(data.success){
            var options = data.data;
            options = sortObject(options);
            for(var id in options){
                $("#start_detector_id").append($("<option/>").val(id).text(id+" ("+options[id]+")"));
                $("#end_detector_id").append($("<option/>").val(id).text(id+" ("+options[id]+")"));
            }
        }
    },
    "json");

    $(".message").text("").removeClass("error");

    var dialog = $("#new-span-dialog");
    dialog.dialog({
        modal:true,
        width: "600px",
        buttons: {
            "Add new span": function(){
                var form = dialog.find("form");

                form.validate({
                    rules:{
                        span_name: {
                            required: true
                        },
                        end_detector_id: {
                            notEqualTo: function(){
                                return $("#start_detector_id").val()
                            }
                        },
                        stationary:{
                            required: true,
                            digits: true,
                            lessThanVerySlow: function(){
                                return $("#very_slow").val();
                            }
                        },
                        very_slow:{
                            required: true,
                            digits: true,
                            greaterThanStationaryAndLowerThanSlow: function(){
                                var values = new Object();
                                values.stationary = $("#stationary").val();
                                values.slow = $("#slow").val();
                                return values;
                            }
                        },
                        slow:{
                            required: true,
                            digits: true,
                            greaterThanVerySlowAndLowerThanModerate: function(){
                                var values = new Object();
                                values.very_slow = $("#very_slow").val();
                                values.moderate = $("#moderate").val();
                                return values;
                            }
                        },
                        moderate:{
                            required: true,
                            digits: true,
                            greaterThanSlow: function(){
                                return $("#slow").val();
                            }
                        }
                    },
                    messages:{
                        end_detector_id: {
                            notEqualTo: "Start and end detector must be different."
                        },
                        logical_group_names: {
                            required: "At least one logical group must be selected."
                        },
                        stationary: {
                            lessThanVerySlow: "'Stationary' speed should be less than 'Very Slow' speed."
                        },
                        very_slow: {
                            greaterThanStationaryAndLowerThanSlow: "'Very Slow' speed should lie betweeen 'Stationary' and 'Slow Speed'."
                        },
                        slow: {
                            greaterThanVerySlowAndLowerThanModerate: "'Slow' speed should lie betweeen 'Very Slow' and 'Moderate' speeds."
                        },
                        moderate: {
                            greaterThanSlow: "'Moderate' speed should be greater than 'Slow' speed."
                        }
                    }
                });

                if(form.valid()){
                    $.post("SpanManager",
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

function create_span_datatable_config(filter_div){
    var datatable_config = common_datatable_configuration();    
    datatable_config.aoColumnDefs = [    
    {
        "sClass":"text",
        "aTargets":[0]
    },
    {
        "sClass":"detector",
        "aTargets":[1,2]
    },
    {
        "sClass":"center",
        "aTargets":[8,9,10]
    },
    {
        "bSortable":false,
        "aTargets":[9,10]
    },
    {
        "sWidth":"20px",
        "aTargets":[8]
    },
    {
        "aTargets": [8],
        "bVisible": false
    },
    {
        "sClass": "numeric center",
        "aTargets": [3,4,5,6,7]
    },
    {
        "sWidth":"110px",
        "aTargets":[9]
    }
    ];

    datatable_config.aoColumns = [
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return source[8].replace(/\] \[/g, "] <br/> [");
        }
    },
    {
        "mDataProp":function(source, type, val){
            return "<a class='view' title='View ["+source[0]+"]' spanname='"+source[0]+"' href='Span?spanName="+encodeURIComponent(escape(source[0]))+"'>view data</a>";
        }
    },
    {
        "mDataProp":function(source, type, val){
            return "<a class='delete' title='Delete ["+source[0]+"]' message='Are you sure you wish to delete span ["+source[0]+"]?' spanname='"+source[0]+"'>delete</a>";
        }
    }
    ];

    var jeditable_config_text = common_jeditable_configuration();
    jeditable_config_text.callback = function(sValue, y){
        var aPos = datatables['span-table'].fnGetPosition(this);
        datatables['span-table'].fnUpdate('Saving...', aPos[0], aPos[1]);
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
            span_name: datatables['span-table'].fnGetData(this.parentNode)[0],
            column: datatables['span-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };

    var jeditable_config_select = common_jeditable_configuration();
    jeditable_config_select.callback = function(sValue, y){
        var aPos = datatables['span-table'].fnGetPosition(this);
        datatables['span-table'].fnUpdate('Saving...', aPos[0], aPos[1]);
        var data = $.parseJSON(sValue);
        if(!data.success){
            display_information_dialog("Unable to update span", data.message);
        }
    };
    jeditable_config_select.onsubmit = function(settings, td){
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
    jeditable_config_select.submitdata = function(){
        return {
            span_name: datatables['span-table'].fnGetData(this.parentNode)[0],
            column: datatables['span-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };
    jeditable_config_select.type = "select";

    $.post("DetectorManager",
    {
        action:"list"
    },
    function(data){
        if(data.success){
            jeditable_config_select.data = data.data;
        }
    },
    "json");

    var jeditable_config_numeric = common_jeditable_configuration();
    jeditable_config_numeric.callback = function(sValue, y){
        var aPos = datatables['span-table'].fnGetPosition(this);
        datatables['span-table'].fnUpdate('Saving...', aPos[0], aPos[1]);
        var data = $.parseJSON(sValue);
        if(!data.success){
            display_information_dialog("Unable to update span", data.message);
        }
    };
    jeditable_config_numeric.onsubmit = function(settings, td){
        var form = $(td).find("form");
        var input = $(td).find("input");
        input.attr("name", "current-edit");
        $(form).validate({
            rules:{
                "current-edit":{
                    required: true,
                    digits:true
                }

            },
            highlight: function(element, errorClass){
                $(element).addClass("error");
            }
        });
        return ($(form).valid());
    };

        jeditable_config_numeric.submitdata = function(){
        return {
            span_name: datatables['span-table'].fnGetData(this.parentNode)[0],
            column: datatables['span-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };

    datatable_config.fnDrawCallback = function(){
        $('td.text', datatables['span-table'].fnGetNodes()).editable('SpanManager', jeditable_config_text);
        $('td.detector', datatables['span-table'].fnGetNodes()).editable('SpanManager', jeditable_config_select);
        $('td.numeric', datatables['span-table'].fnGetNodes()).editable('SpanManager', jeditable_config_numeric);
        $('a.view', datatables['span-table'])
        .button({
            icons:{
                primary:"ui-icon-search"
            }
        })
        .addClass("ui-state-highlight")
        .bind({
            click:function(){
                var location = "Span";
                var spanName = $(this).attr("spanname");
                if(spanName != undefined){
                    location+="?spanName="+encodeURIComponent(escape(spanName));
                    window.location.href=location;
                }

            }
        });

        create_datatable_links(datatables['span-table'], {},
        {
            delete_callback:function(element, callbacks){
                $.post("SpanManager",
                {
                    action:"delete",
                    span_name:element.attr("spanname")
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
    datatable_config.sAjaxSource = "SpanDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}
