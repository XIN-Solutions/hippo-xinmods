<%@ include file="/WEB-INF/jsp/include/imports.jsp" %>

<hst:setBundle basename="essentials.pagenotfound"/>
<div>
  <h1><fmt:message key="pagenotfound.title" var="title"/><c:out value="${title}"/></h1>
  <p><fmt:message key="pagenotfound.text"/><%--Skip XML escaping--%></p>
</div>
<div>
  <hst:include ref="container"/>
</div>
