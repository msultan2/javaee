<%@ page language="java" contentType="text/css; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
            String css_url = (String) session.getAttribute("css_url");
            String css_image_url = "";
            if (css_url != null) {
                css_image_url = css_url.substring(0, css_url.lastIndexOf("/"));
                
%>
<c:import var="css"
          url="<%=css_url%>"
          scope="session"/>
<c:out value="${css}"/>
body {
line-height:1;
}
#branding-image {
background-image: url(<%=css_image_url%>/images/logo.png);
}
#banner h2 {
    font-size:100%;
    font-weight: normal;
    line-height:1;
}
<%            }
%>
