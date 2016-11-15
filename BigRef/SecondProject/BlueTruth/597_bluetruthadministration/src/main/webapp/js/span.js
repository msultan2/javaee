// SCJS 008 START

function init_span(span_name){
    setHeaderSpanInfo(span_name);
    create_notes_information_table(span_name);
    create_new_note_button();
    create_events_information_table(span_name);
    create_new_event_button();
    create_incidents_information_table(span_name);
    create_new_incident_button();    
}

function setHeaderSpanInfo(span_name){
    $("#span_name").text(span_name);
}

function create_notes_information_table(span_name){
    var table = $("#notes-information-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var notes_information_datatable_config = create_notes_information_datatable_config(filter_div, span_name);
    datatables['notes-information-table'] = table.dataTable(notes_information_datatable_config);
    var datatable = datatables['notes-information-table'];

    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_csv_download_button({
        filter_div:filter_div,
        datatable:datatable,
        parameters : [{"name":"span_name", "value":span_name}],
        ajax_url: notes_information_datatable_config.sAjaxSource
    }))
    .append(create_show_hide_column_button({
        dialog:$("#notes-information-hide-column-dialog"),
        headers:table_headers,
        datatable:datatable,
        aoColumnDefs:notes_information_datatable_config.aoColumnDefs
    }))
    .append(create_filter_button({
        dialog:$("#notes-information-filter-dialog"),
        filter_div:filter_div,
        datatable:datatable,
        datatable_headers:table_headers,
        column_filter_definitions:[
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
                "type":"date"
        }
        ]
    }));
}

function create_new_note_button(){
    $(".new-note-button").button().bind({
        "click":display_new_note_dialog
    });
}

function display_new_note_dialog(){

    var dialog = $("#new-note-dialog");
    dialog.dialog({
        modal:true, 
        width: "600px",
        buttons: {
            "Add new note": function(){
                var form = dialog.find("form");

                form.validate({
                    rules:{
                        description:{
                            required: true
                        }
                    }
                });

                if(form.valid()){
                    $.post("SpanNotesInformationManager",
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

function create_notes_information_datatable_config(filter_div, span_name){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
    {
        "bVisible": false,
        "aTargets":[0]
    },
    {
        "sClass":"text",
        "aTargets":[1]
    },
    {
        "sClass":"center ",
        "aTargets":[2,3,4]
    },
    {
        "bSortable":false,
        "aTargets":[4]
    },
    {
        "sWidth":"80px",
        "aTargets":[4]
    }
    ];

    datatable_config.aoColumns = [
    null,
    null,
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return "<a class='delete' title='Delete ["+source[1]+"]' message='Are you sure you wish to delete note ["+source[1]+"]?' spanname='"+span_name+"' note_id='"+source[0]+"'>delete</a>";
        }
    }
    ];

    datatable_config.fnDrawCallback = function(){
        create_datatable_links(datatables['notes-information-table'], {},
        {
            delete_callback:function(element, callbacks){
                $.post("SpanNotesInformationManager",
                {
                    action:"delete",
                    span_name:element.attr("spanname"),
                    span_note_id:element.attr("note_id")
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
    }

    datatable_config.sAjaxSource = "SpanNotesInformationDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function(
    {
        span_name:span_name
    }, filter_div);
    return datatable_config;
}

function create_events_information_table(span_name){
    var table = $("#events-information-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var events_information_datatable_config = create_events_information_datatable_config(filter_div, span_name);
    datatables['events-information-table'] = table.dataTable(events_information_datatable_config);
    var datatable = datatables['events-information-table'];

    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_csv_download_button({
        filter_div:filter_div,
        datatable:datatable,
        parameters : [{"name":"span_name", "value":span_name}],
        ajax_url: events_information_datatable_config.sAjaxSource
    }))
    .append(create_show_hide_column_button({
        dialog:$("#events-information-hide-column-dialog"),
        headers:table_headers,
        datatable:datatable,
        aoColumnDefs:events_information_datatable_config.aoColumnDefs
    }))
    .append(create_filter_button({
        dialog:$("#events-information-filter-dialog"),
        filter_div:filter_div,
        datatable:datatable,
        datatable_headers:table_headers,
        column_filter_definitions:[
        {
                "target":1,
                "type":"string"
        },
        {
                "target":2,
                "type":"date"
        },
        {
                "target":3,
                "type":"date"
        }
        ]
    }, "test1"));
}

function create_new_event_button(){

    jQuery.validator.addMethod("greaterThanStartTimestamp", function(end_timestamp,element,start_timestamp){
        var startDate = new Date(start_timestamp);
        var endDate = new Date(end_timestamp);
       return startDate < endDate;
    }, "error");
    
    $(".new-event-button").button().bind({
        "click":display_new_event_dialog
    });
}

function init_date_time_picker(){

    $(".datetimepicker").datetimepicker({
        dateFormat: 'yy-mm-dd',
        timeFormat: 'h:mm:ss',
        showSecond: true
    }).attr("readonly","readonly");
}

function display_new_event_dialog(){

    init_date_time_picker();
    var dialog = $("#new-event-dialog");
    dialog.dialog({
        modal:true,
        width: "600px",
        buttons: {
            "Add new event": function(){
                var form = dialog.find("form");

                form.validate({
                    rules:{
                        description:{
                            required: true
                        },
                        startTimestamp:{
                            required: true
                        },
                        endTimestamp:{
                            required: true,
                            greaterThanStartTimestamp: function(){
                                return $("#start_timestamp").val();
                            }
                        }
                    },
                    messages:{
                        endTimestamp:{
                            greaterThanStartTimestamp:"End Timestamp should be greater than Start Timestamp"
                        }
                    }
                });

                if(form.valid()){
                    $.post("SpanEventsInformationManager",
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

function create_events_information_datatable_config(filter_div, span_name){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
    {
        "bVisible": false,
        "aTargets":[0]
    },
    {
        "sClass":"text",
        "aTargets":[1]
    },
    {
        "sClass":"center ",
        "aTargets":[2,3,4]
    },
    {
        "bSortable":false,
        "aTargets":[4]
    },
    {
        "sWidth":"80px",
        "aTargets":[4]
    }
    ];

    datatable_config.aoColumns = [
    null,
    null,
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return "<a class='delete' title='Delete ["+source[1]+"]' message='Are you sure you wish to delete event ["+source[1]+"]?' spanname='"+span_name+"' event_id='"+source[0]+"'>delete</a>";
        }
    }
    ];

    datatable_config.fnDrawCallback = function(){
        create_datatable_links(datatables['events-information-table'], {},
        {
            delete_callback:function(element, callbacks){
                $.post("SpanEventsInformationManager",
                {
                    action:"delete",
                    span_name:element.attr("spanname"),
                    span_event_id:element.attr("event_id")
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
    }

    datatable_config.sAjaxSource = "SpanEventsInformationDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function(
    {
        span_name:span_name
    }, filter_div);
    return datatable_config;
}

function create_incidents_information_table(span_name){
    var table = $("#incidents-information-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var incidents_information_datatable_config = create_incidents_information_datatable_config(filter_div, span_name);
    datatables['incidents-information-table'] = table.dataTable(incidents_information_datatable_config);
    var datatable = datatables['incidents-information-table'];

    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_csv_download_button({
        filter_div:filter_div,
        datatable:datatable,
        parameters : [{"name":"span_name", "value":span_name}],
        ajax_url: incidents_information_datatable_config.sAjaxSource
    }))
    .append(create_show_hide_column_button({
        dialog:$("#incidents-information-hide-column-dialog"),
        headers:table_headers,
        datatable:datatable,
        aoColumnDefs:incidents_information_datatable_config.aoColumnDefs
    }))
    .append(create_filter_button({
        dialog:$("#incidents-information-filter-dialog"),
        filter_div:filter_div,
        datatable:datatable,
        datatable_headers:table_headers,
        column_filter_definitions:[
        {
                "target":1,
                "type":"string"
        },
        {
                "target":2,
                "type":"date"
        },
        {
                "target":3,
                "type":"date"
        }
        ]
    }));
}

function create_new_incident_button(){

    jQuery.validator.addMethod("greaterThanStartTimestamp", function(end_timestamp,element,start_timestamp){
        var startDate = new Date(start_timestamp);
        var endDate = new Date(end_timestamp);
       return startDate < endDate;
    }, "error");

    $(".new-incident-button").button().bind({
        "click":display_new_incident_dialog
    });
}

function display_new_incident_dialog(){

    init_date_time_picker();
    var dialog = $("#new-incident-dialog");
    dialog.dialog({
        modal:true,
        width: "600px",
        buttons: {
            "Add new incident": function(){
                var form = dialog.find("form");

                form.validate({
                    rules:{
                        description:{
                            required: true
                        },
                        startTimestamp1:{
                            required: true
                        },
                        endTimestamp1:{
                            required: true,
                            greaterThanStartTimestamp: function(){
                                return $("#start_timestamp1").val();
                            }
                        }
                    },
                    messages:{
                        endTimestamp1:{
                            greaterThanStartTimestamp:"End Timestamp should be greater than Start Timestamp"
                        }
                    }
                });

                if(form.valid()){
                    $.post("SpanIncidentsInformationManager",
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

function create_incidents_information_datatable_config(filter_div, span_name){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
    {
        "bVisible": false,
        "aTargets":[0]
    },
    {
        "sClass":"text",
        "aTargets":[1]
    },
    {
        "sClass":"center ",
        "aTargets":[2,3,4]
    },
    {
        "bSortable":false,
        "aTargets":[4]
    },
    {
        "sWidth":"80px",
        "aTargets":[4]
    }
    ];

    datatable_config.aoColumns = [
    null,
    null,
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return "<a class='delete' title='Delete ["+source[1]+"]' message='Are you sure you wish to delete incident ["+source[1]+"]?' spanname='"+span_name+"' incident_id='"+source[0]+"'>delete</a>";
        }
    }
    ];

    datatable_config.fnDrawCallback = function(){
        create_datatable_links(datatables['incidents-information-table'], {},
        {
            delete_callback:function(element, callbacks){
                $.post("SpanIncidentsInformationManager",
                {
                    action:"delete",
                    span_name:element.attr("spanname"),
                    span_incident_id:element.attr("incident_id")
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
    }

    datatable_config.sAjaxSource = "SpanIncidentsInformationDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function(
    {
        span_name:span_name
    }, filter_div);
    return datatable_config;
}

// SCJS 008 END












