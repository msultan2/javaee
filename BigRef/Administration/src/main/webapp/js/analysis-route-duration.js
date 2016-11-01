var graph_url = "/AnalysisChart/RouteDurationChart";

function init_analysis_route(route_name){

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

    create_analysis_route_table(route_name);
    create_graph_controls_button();
    create_graph_csv_download_button();

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

function create_analysis_route_table(route_name){
    var table = $("#route-graph-data-table");
    var filter_div = $("#date-controls");
    var table_headers = $("th", table);
    var route_datatable_config = create_route_datatable_config(route_name, filter_div);
    datatables['route-graph-data-table'] = table.dataTable(route_datatable_config);
    var datatable = datatables['route-graph-data-table'];
        
    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_csv_download_button({
        filter_div:filter_div,
        datatable:datatable,
        parameters : [{"name":"route", "value":route_name}],
        ajax_url:route_datatable_config.sAjaxSource
    }))
    .append(create_show_hide_column_button({
        dialog:$("#route-show-hide-column-dialog"),
        headers:table_headers,
        datatable:datatable,
        aoColumnDefs:route_datatable_config.aoColumnDefs
    })); 
}

function create_route_datatable_config(route_name,filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [    
        {
            "sClass":"center",
            "aTargets":[0,1,2,3,4]
        }
    ];

    datatable_config.aaSorting = [[0,"asc"]];

    datatable_config.aoColumns = [
        null,
        null,
        null,
        null,
        null
    ];

    datatable_config.sAjaxSource = "AnalysisRouteDurationDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({
        route:route_name
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
        //modal:true,
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
                datatables['route-graph-data-table'].fnDraw();
            });
        });
    } else {
        show_graph(function(){
            datatables['route-graph-data-table'].fnDraw();
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

function create_graph_csv_download_button(){
    $('#graph-download-csv').button({
        icons: { primary: "ui-icon-circle-arrow-s" }
    }).bind({
        "click":function(){
            window.location.href= get_url() + '&csv=true';
        }
    });
}

function get_url() {
    return graph_url + "?" + $("#control-form").serialize();
}