<%-- 
    Document   : BrandConfiguration
    Created on : 30-Nov-2012, 11:30:47
    Author     : wingc
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
            String selectedBrandName = request.getParameter("brandname");
            if (selectedBrandName == null) {
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="REFRESH" content="0; url=Brands">
        <title>Redirecting.</title>
    </head>
</html>
<%          } else {%>
<html>
    <head>
        <title>Cloud Instation - Brand Configuration</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/brand-configuration.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                var brand_name = decodeURIComponent("<%=selectedBrandName%>");
                init_brand_configuration(brand_name);
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Administration">Administration</a> / <a href="Brands">Brands</a> / <span id="brand-name-crumb" /></h1>
            <h2>Email</h2>
            <table id="brand-email-table">
                <thead>
                    <tr>
                        <th>Title</th>
                        <th>Email Address</th>
                        <th>Description</th>
                        <th>Delete</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div class="buttons">
                <button class="new-brand-email-button">Add new email</button>
            </div>
            <h2>Telephone</h2>
            <table id="brand-phone-table">
                <thead>
                    <tr>
                        <th>Title</th>
                        <th>Telephone Number</th>
                        <th>Description</th>
                        <th>Delete</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div class="buttons">
                <button class="new-brand-phone-button">Add new telephone</button>
            </div>
            <div id="dialog-container">
                <div id="add-brand-email-dialog" title="Add new email">
                    <p class="message"></p>
                    <form class="jeditable">
                        <fieldset>
                            <label for="title">Title:<label for="title" generated="true" class="error"></label></label>
                            <input id="title" name="title" class="required" type="text"/>
                            <label for="email_address">Email Address:<label for="email_address" generated="true" class="error"></label></label>
                            <input id="contact" name="contact" class="required" type="text"/>
                            <label for="description">Description:<label for="description" generated="true" class="error"></label></label>
                            <input id="description" name="description" class="required" type="text"/>
                            <input id="action" name="action" value="insert" type="hidden"/>
                            <input id="brand_name" name="brand_name" value="<%=selectedBrandName%>" type="hidden"/>
                            <input id="contact_method" name="contact_method" value="email" type="hidden"/>
                        </fieldset>
                    </form>
                </div>
                <div id="brand-email-filter-dialog" class="column-filter-dialog" title="Email Filter">
                </div>
                <div id="brand-email-show-hide-column-dialog" class="column-selection-dialog" title="Email Show/Hide Columns">
                </div>
                <div id="add-brand-phone-dialog" title="Add new phone">
                    <p class="message"></p>
                    <form class="jeditable">
                        <fieldset>
                            <label for="title">Title:<label for="title" generated="true" class="error"></label></label>
                            <input id="title" name="title" class="required" type="text"/>
                            <label for="phone">Telephone:<label for="phone" generated="true" class="error"></label></label>
                            <input id="contact" name="contact" class="required" type="text"/>
                            <label for="description">Description:<label for="description" generated="true" class="error"></label></label>
                            <input id="description" name="description" class="required" type="text"/>
                            <input id="action" name="action" value="insert" type="hidden"/>
                            <input id="brand_name" name="brand_name" value="<%=selectedBrandName%>" type="hidden"/>
                            <input id="contact_method" name="contact_method" value="phone" type="hidden"/>
                        </fieldset>
                    </form>
                </div>
                <div id="brand-phone-filter-dialog" class="column-filter-dialog" title="Telephone Filter">
                </div>
                <div id="brand-phone-show-hide-column-dialog" class="column-selection-dialog" title="Phone Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
<%          }%>