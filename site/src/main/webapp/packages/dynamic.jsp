<%@ page trimDirectiveWhitespaces="true" %>
<%@ page import="nz.xinsolutions.beans.ContextVariablesBean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctxVariables" value="<%= new ContextVariablesBean(request) %>" />

<script>
    var Config = {
        ApiAuth : "${ctxVariables.apiAuthenticationHeader}",
        ApiUrl : "${ctxVariables.apiUrl}",
        XinApiUrl : "${ctxVariables.xinApiUrl}"
    };
</script>