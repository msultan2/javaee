var graph_url = "/DetectorTwoGChart";

function init_2g(detector_id){

    $('#graph-image').css({
        width: "100%",
        height: "100%"
    }).hide();

    $(".timepicker").timepicker({
        timeFormat: 'h:mm:ss',
        showSecond: true
    }).attr("readonly", "readonly");
    $(".datepicker").datepicker({
        dateFormat: 'yy-mm-dd'
    }).attr("readonly", "readonly");
    $(".datetimepicker").datetimepicker({
                dateFormat: 'yy-mm-dd',
                timeFormat: 'hh:mm:ss',
                showSecond: true
    }).attr("readonly", "readonly");

    create_detector_table(detector_id);
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

function create_detector_table(detector_id){
    var table = $("#detector-graph-data-table");
    var filter_div = $("#date-controls");
    var table_headers = $("th", table);
    var detector_datatable_config = create_detector_datatable_config(detector_id, filter_div);
    datatables['detector-graph-data-table'] = table.dataTable(detector_datatable_config);
    var datatable = datatables['detector-graph-data-table'];

    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_csv_download_button({
        filter_div:filter_div,
        datatable:datatable,
        parameters : [{"name":"detector_id", "value":detector_id}],
        ajax_url:detector_datatable_config.sAjaxSource
    }))
    .append(create_show_hide_column_button({
        dialog:$("#detector-show-hide-column-dialog"),
        headers:table_headers,
        datatable:datatable,
        aoColumnDefs:detector_datatable_config.aoColumnDefs
    }));
}

function create_detector_datatable_config(detector_id,filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
    {
        "sClass":"center",
        "aTargets":[0,1,2,3]
    }
    ];

    datatable_config.aaSorting = [[0,"asc"]];

    datatable_config.aoColumns = [
    
    {
        "mDataProp":function(source, type, val){
            return source[0].split(".")[0];
        }
    },    
    null, null, null
    ];

    datatable_config.sAjaxSource = "/DetectorTwoGStatisticsDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({
        detector_id:detector_id
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
        width: "600px",
        buttons: {
            "Apply": function(){
                $("#graph_width").val($("#graph-image").width());
                $("#graph_height").val($("#graph-image").height());
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
                datatables['detector-graph-data-table'].fnDraw();
            });
        });
    } else {
        show_graph(function(){
            datatables['detector-graph-data-table'].fnDraw();
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