<%-- 
    Document   : InstationBrands
    Created on : 21-Nov-2012, 09:45:29
    Author     : svenkataramanappa
    SCJS       : 015 - Brand management
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Brands</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/brands.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_brands();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Administration">Administration</a> / Brands</h1>
            <h2>Configured Brands</h2>
            <p>* - These fields cannot be edited on this page</p>
            <table id="brand-table">
                <thead>
                    <tr>
                        <th>Brand Name </th>
                        <th>CSS URL </th>
                        <th>Website Address</th>
                        <th>Configuration *</th>
                        <th>Delete *</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div class="buttons">
                <button class="new-brand-button">Add new brand</button>
            </div>            
<!--            <div class="buttons">
                    <button class="csv-download-button">CSV download</button>
            </div>-->
            <div id="dialog-container">
                <div id="new-brand-dialog" title="Add new brand">
                    <p class="message"></p>
                    <form class="jeditable">
                        <fieldset>
                            <label for="brand_name">Brand Name<label for="brand_name" generated="true" class="error"></label></label>
                            <input id="brand_name" name="brand_name" type="text" />
                            <label for="css_url">CSS URL <label for="css_url" generated="true" class="error"></label></label>
                            <input id="css_url" name="css_url" type="text" />
                            <label for="website">Website Address</label>
                            <input id="website" name="website" type="text" />
                            <input id="action" name="action" value="insert" type="hidden"/>
                        </fieldset>
                    </form>
                </div>
                <div id="brand-filter-dialog" class="column-filter-dialog" title="Brand Filter">
                </div>
                <div id="brand-show-hide-column-dialog" class="column-selection-dialog" title="Brand Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
