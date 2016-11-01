<%-- 
    Document   : DiagnosticSpans
    Created on : 07-Nov-2012, 10:37:42
    Author     : svenkataramanappa
    SCJS       : 009
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <title>Cloud Instation - Span Diagnostic</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/diagnostic-spans.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_diagnostic_spans();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Diagnostic">Diagnostic</a> / Spans</h1>
            <h2>Registered Spans</h2>
            <table id="span-table">
                <thead>
                    <tr>
                        <th>Span Name *</th>
                        <th>Start *</th>
                        <th>End *</th>
                        <th>Total Distance (Metres) *</th>
                        <th>Stationary (MPH) *</th>
                        <th>Very Slow (MPH) *</th>
                        <th>Slow (MPH) *</th>
                        <th>Moderate (MPH) *</th>
                        <th>Logical Groups *</th>
                        <th>Status *</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div id="dialog-container">
                <div id="span-filter-dialog" class="column-filter-dialog" title="Span Filter">
                </div>
                <div id="span-show-hide-column-dialog" class="column-selection-dialog" title="Span Show/Hide Columns">
                </div>
            </div>
            
        </div>
    </body>
</html>
