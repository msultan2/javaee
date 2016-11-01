function init_diagnostic_detectors(){
    create_detector_table();
}

function create_detector_table(){
    var logical_group_index = 7;
    var recordType = "Detectors";
    var table = $("#detector-table");
    var table_headers = $("th", table);
    var filter_div = $("<div/>");
    var diagnostic_detector_datatable_config = create_diagnostic_detector_datatable_config(filter_div);
    datatables['detector-table'] = table.dataTable(diagnostic_detector_datatable_config);
    var datatable = datatables['detector-table'];

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
            ajax_url:diagnostic_detector_datatable_config.sAjaxSource
        }))
        .append(create_show_hide_column_button({
            dialog:$("#detector-show-hide-column-dialog"),
            headers:table_headers,
            datatable:datatable,
            aoColumnDefs:diagnostic_detector_datatable_config.aoColumnDefs
        }))
        .append(create_filter_button({
            dialog:$("#detector-filter-dialog"),
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
                "type":"numeric"
            },
            {
                "target":4,
                "type":"numeric"
            },
            {
                "target":5,
                "type":"string"
            },
            {
                "target":6,
                "type":"string"
            },
            {
                "target":logical_group_index,
                "type":"enum",
                "options": logical_group_options
            },
            // // SCJS 016 START
            {
                "target":8,
                "type":"string",
                "sClass":"status"
            }// SCJS 016 END
            ]
        }));
        
    },
    "json");
}

function create_diagnostic_detector_datatable_config(filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [{
        "sClass":"text",
        "aTargets":[0]
    },
    {
        "sClass":"text center",
        "aTargets":[1,2,3,4]
    },
    {
        "sClass":"mode center",
        "aTargets":[5]
    },
    {
        "sClass":"center carriageway",
        "aTargets":[6]
    },
    // SCJS 016 START
    {
        "aTargets": [8],
        "sClass":"status center"
    }// SCJS 016 END
    ,
    {
        "sClass":"center",
        "aTargets":[9]
    },
    {
        "bSortable":false,
        "aTargets":[9]
    },
    {
        "sWidth":"200px",
        "aTargets":[0,1]
    },
    {
        "aTargets":[3,4],
        "bVisible": false
    },
    {
        "sWidth":"200px",
        "aTargets":[5],
        "bVisible": false
    },
    {
        "sWidth":"100px",
        "aTargets":[6],
        "bVisible": false
    },
    {
        "aTargets":[7],
        "bVisible": false
    },
    // SCJS 016 START
    {
        "sWidth":"100px",
        "aTargets":[8,9]
    }// SCJS 016 END
];

    datatable_config.aoColumns = [
    null,
    null,
    null,
    null,
    null,    
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return source[7].replace(/\] \[/g, "] <br/> [");
        }
    },
    null,
    {
        "mDataProp":function(source, type, val){            
            return "<a class='view' title='View ["+source[0]+"]' href='DiagnosticDetector?id="+source[1]+"'>view data</a>";
        }
    }
    ];
    datatable_config.fnDrawCallback = function(){     
        $('a.view', datatables['detector-table'])
        .button({
            icons:{
                primary:"ui-icon-search"
            }
        })
        .addClass("ui-state-highlight");
    }

    // SCJS 016 START
    datatable_config.fnRowCallback = function(nRow, aData, iDisplayIndex){
        var td = $("td.status", nRow);
        if (td.text() === 'Silent') {
            td.addClass("status-silent");
        } else if (td.text() === 'Reporting') {
            td.addClass("status-reporting");
        } else if (td.text() === 'Degraded') {
            td.addClass("status-degrading");
        }
        return nRow;
    };
    // SCJS 016 END

    datatable_config.sAjaxSource = "DiagnosticDetectorDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);
    return datatable_config;
}