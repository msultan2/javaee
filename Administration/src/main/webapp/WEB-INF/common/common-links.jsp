<div id="links">
    <div id="common-links" class="ui-state-default">
        <a id="home-link" href="/">Home</a>
        <% if (request.isUserInRole("journey_report") || request.isUserInRole("occupancy_report")) {%>
        <a id="report-link" href="Report">Report</a>
        <% }
                    if (request.isUserInRole("map_interface")) {%>
        <a id="map-link" href="Map">Map</a>
        <% }
                    if (request.isUserInRole("analysis")) {%>
        <a id="analysis-link" href="Analysis">Analysis</a>
         <% }
                    if (request.isUserInRole("diagnostic")) {%>
        <a id="diagnostic-link" href="Diagnostic">Diagnostic</a>
        <% }
                    if (request.isUserInRole("route_configuration")
                            || request.isUserInRole("span_configuration")
                            || request.isUserInRole("detector_configuration")) {%>
        <a id="configuration-link" href="Configuration">Configuration</a>
        <% }
                    
                    //BUG - This needs to be updated in the next version:
                    //The roles specified below do not describe all the roles 
                    //that come under the administration section.
                    //For instance - Branding and support roles.                    
                    if (request.isUserInRole("user_administration")
                            || request.isUserInRole("role_administration")
                            || request.isUserInRole("logical_group_administration")) {%>
        <a id="administration-link" href="Administration">Administration</a>
        <% }
                    if (request.isUserInRole("wiki_view")
            || request.isUserInRole("wiki_administration")) {%>
        <a id="wiki-link" href="Wiki">Wiki</a>
        <% }%>
        <a id="support-link" href="Support">Support</a>         
    </div>    
</div>