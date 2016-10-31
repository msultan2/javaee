// SCJS 016 START

function init_detector(detector_name, detectorId){
    setHeaderDetectorInfo(detector_name);
    create_notes_information_table(detectorId);
    create_new_note_button();
    display_detector_description(detectorId);
    
    var jeditable_config_text = common_jeditable_configuration();
    jeditable_config_text.type = 'textarea';
    jeditable_config_text.height = '80px';
    jeditable_config_text.width = '450px';
    jeditable_config_text.callback = function(sValue, y){
        var data = $.parseJSON(sValue);
        if(!data.success){
            display_information_dialog("Unable to update detector description", data.message);
        } else
        $("#description").text(data.data);
    };
    jeditable_config_text.onsubmit = function(settings, div){
        var form = $(div).find("form");
        var input = $(div).find("textarea");
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
            id: detectorId,
            column: "description",
            action: "update"
        };
    };
    $('#description').editable('DetectorManager', jeditable_config_text);
}

function setHeaderDetectorInfo(detector_name){
    $("#detector_name").text(detector_name);
}

function create_notes_information_table(detectorId){
    var table = $("#notes-information-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var notes_information_datatable_config = create_notes_information_datatable_config(filter_div, detectorId);
    datatables['notes-information-table'] = table.dataTable(notes_information_datatable_config);
    var datatable = datatables['notes-information-table'];

    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_csv_download_button({
        filter_div:filter_div,
        datatable:datatable,
        parameters : [{"name":"detector_id", "value":detectorId}],
        ajax_url:notes_information_datatable_config.sAjaxSource
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
                    $.post("DetectorEngineerNotesInfoManager",
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

function create_notes_information_datatable_config(filter_div, detectorId){
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
        "sClass":"center",
        "aTargets":[2,3,4]
    },
    {
        "bSortable":false,
        "aTargets":[4]
    },
    {
        "sWidth":"20px",
        "aTargets":[4]
    },
    {
        "sWidth":"150px",
        "aTargets":[2,3]
    }
    ];

    datatable_config.aoColumns = [
    null,
    {
        "mDataProp":function(source, type, val){
            return "<pre>"+source[1]+"</pre>";
        }
    },
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return "<a class='delete' title='Delete ["+source[1]+"]' message='Are you sure you wish to delete note ["+source[1]+"]?' detectorId='"+detectorId+"' note_id='"+source[0]+"'>delete</a>";
        }
    }
    ];

    datatable_config.fnDrawCallback = function(){
        create_datatable_links(datatables['notes-information-table'], {},
        {
            delete_callback:function(element, callbacks){
                $.post("DetectorEngineerNotesInfoManager",
                {
                    action:"delete",
                    detector_id:element.attr("detectorId"),
                    note_id:element.attr("note_id")
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

    datatable_config.sAjaxSource = "DetectorEngineerNotesDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function(
    {
        detector_id:detectorId
    }, filter_div);
    return datatable_config;
}

function display_detector_description(detectorId){
    $.post("DetectorManager",
    {
        "action":"get",
        "id":detectorId
    },
    function(data){
        if(data.success){
            if(data.data == null){
                $("#description").text("No detector information available");
            }
            else
            $("#description").text(data.data);       
        }
    },
    "json");
}

// SCJS 016 END