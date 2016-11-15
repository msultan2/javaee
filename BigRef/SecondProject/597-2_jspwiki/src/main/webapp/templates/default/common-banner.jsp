<div id="min-width-div"></div>
<div id="banner">    
    <h1>Cloud Instation</h1>    
    <h2>Bluetooth Detector Cloud Instation Version ${project.version}</h2>
    <% if(request.getUserPrincipal() != null) { %>
    <div id="user-links">
        <span>signed in as <a href="../CurrentInstationUser?username=<%=request.getUserPrincipal().getName()%>"><%=request.getUserPrincipal().getName()%></a> (<a id="sign-out-link" href="../sign-out.jsp">sign out</a>)</span>
    </div>
    <div id="branding-image"></div>
    <% } %>
</div>