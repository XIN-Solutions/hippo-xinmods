<%@ page isErrorPage="true" %>
<% if (request.getHeader("Content-Type") != null && request.getHeader("Content-Type").contains("html")) { %>
<!doctype html>
  <%@ include file="/WEB-INF/jspf/htmlTags.jspf" %>
  <% response.setStatus(400); %>
  <html lang="en">
  <head>
    <meta charset="utf-8"/>
    <title>400 error</title>
  </head>
  <body>
  <h1>Bad request!!!</h1>
  <p>The request cannot be fulfilled due to bad syntax.</p>
  </body>
  </html>
<% } %>