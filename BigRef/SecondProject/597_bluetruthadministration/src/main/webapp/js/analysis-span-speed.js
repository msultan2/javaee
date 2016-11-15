var graph_url = "/AnalysisChart/SpanSpeedChart";

function init_analysis_span(span_name){

    $('#graph-image').css({
        width:"100%",
        height:"100%"
    }).hide();

    $(".timepicker").timepicker({
        timeFormat: 'h:mm:ss',
        showSecond: true
    }).attr("readonly","readonly");
    $(".datepicker").datepicker({
        dateFormat: 'yy-mm-dd'
    }).attr("readonly","readonly");
    $(".datetimepicker").datetimepicker({
        dateFormat: 'yy-mm-dd',
        timeFormat: 'hh:mm:ss',
        showSecond: true
    }).attr("readonly","readonly");

    create_analysis_span_table(span_name);
    create_graph_controls_button();

    set_graph_dimensions_and_position();
    window_resize_watchdog();

    update_graph_and_table();
}

function set_graph_dimensions_and_position(){
    //the map must be displayed directly below the common links bar
    var common_links = $("#common-links");
    var distance_from_top = common_links.offset().top + common_links.height();

    //Set the height to work with IE
    var graph_height = $(window).height() - distance_from_top - 120;//1 = border width;
    $('#graph').css({
        top:distance_from_top,
        height:graph_height
    });

    $("#graph_width").val($("#graph").width());
    $("#graph_height").val($("#graph").height());
}

var current_window_height;
var current_window_width;
function window_resize_watchdog(){
    var this_window = $(window);
    setInterval(function(){
        if(this_window.height() != current_window_height || this_window.width() != current_window_width){
            current_window_height = this_window.height();
            current_window_width = this_window.width();
            set_graph_dimensions_and_position();
        }
    }, 500);

}

function create_analysis_span_table(span_name){
    var table = $("#span-graph-data-table");
    var filter_div = $("#date-controls");
    var table_headers = $("th", table);
    var span_datatable_config = create_span_datatable_config(span_name, filter_div);
    datatables['span-graph-data-table'] = table.dataTable(span_datatable_config);
    var datatable = datatables['span-graph-data-table'];
        
    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_csv_download_button({
        filter_div:filter_div,
        datatable:datatable,
        parameters : [{"name":"span", "value":span_name}],
        ajax_url:span_datatable_config.sAjaxSource
    }))
    .append(create_show_hide_column_button({
        dialog:$("#span-show-hide-column-dialog"),
        headers:table_headers,
        datatable:datatable,
        aoColumnDefs:span_datatable_config.aoColumnDefs
    }));
/*
    .append(create_filter_button({
        dialog:$("#span-filter-dialog"),
        filter_div:filter_div,
        datatable:datatable,
        datatable_headers:table_headers,
        column_filter_definitions:[
        {
            "target":4,
            "type":"string"
        },
        {
            "target":5,
            "type":"date"
        }
        ]
    }));
    */
}

function create_span_datatable_config(span_name,filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [    
    {
        "sClass":"center",
        "aTargets":[0,1,2,3,4,5]
    }
    ];

    datatable_config.aaSorting = [[0,"asc"]];

    datatable_config.aoColumns = [
    null,
    {
        "mDataProp":function(source, type, val){
            return source[1].split(".")[0];
        }
    },
    {
        "mDataProp":function(source, type, val){
            return source[2].split(".")[0];
        }
    },
    {
        "mDataProp":function(source, type, val){
            return source[3].split(".")[0];
        }
    },
    {
        "mDataProp":function(source, type, val){
            return source[4].split(".")[0];
        }
    },
    null
    ];

    datatable_config.sAjaxSource = "AnalysisSpanSpeedDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({
        span:span_name
    }, filter_div);

    return datatable_config;
}

function create_graph_controls_button(){    
    $("#graph-control-button").button().bind({
        "click":display_graph_controls_dialog
    });
}

function display_graph_controls_dialog(){

    var dialog = $("#graph-controls-dialog");
    dialog.dialog({
       // modal:true,
        width: "600px",
        buttons: {
            "Apply": function(){
                $("#graph_width").val($("#graph").width());
                $("#graph_height").val($("#graph").height());
                update_graph_and_table();
            },
            "Close": function(){
                dialog.dialog("close");
            }
        },
        open: function(){
            $("#ui-dialog-auto-focus-hack-fix").hide();
        },
        close: function(){
            $("#ui-dialog-auto-focus-hack-fix").show();
        }
    });
}

function update_graph_and_table(){
    if($('#graph-image').is(":visible")){
        hide_graph(function(){
            show_graph(function(){
                datatables['span-graph-data-table'].fnDraw();
            });
        });
    } else {
        show_graph(function(){
            datatables['span-graph-data-table'].fnDraw();
        });
    }
}

function hide_graph(callback){
    $('#graph-image').hide("slide",{
        'direction':'left'
    },500, callback);
}

function show_graph(callback){
    //loader gif
    $("#loader").show();

    //receive chart id
    $.post(graph_url + "?" + $("#control-form").serialize(), function (data) {
        chart(data.data);
    }, "json");
    
    //redraw datatable
    callback();
}