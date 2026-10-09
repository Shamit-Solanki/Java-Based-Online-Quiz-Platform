<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="code" value="${pageContext.errorData.statusCode}"/>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>${empty code ? 'Error' : code} · QuizMania</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800&display=swap">
<link rel="stylesheet" href="${ctx}/css/style.css"></head>
<body class="public"><div class="card pad-l error-page" style="max-width:460px">
<div class="code">${empty code ? '!' : code}</div>
<h2>
<c:choose>
<c:when test="${code == 403}">Access denied</c:when>
<c:when test="${code == 404}">Page not found</c:when>
<c:when test="${code == 405}">Method not allowed</c:when>
<c:otherwise>Something went wrong</c:otherwise>
</c:choose>
</h2>
<p class="muted">
<c:choose>
<c:when test="${code == 403}">You do not have permission to view this page.</c:when>
<c:when test="${code == 404}">The page you are looking for does not exist or was moved.</c:when>
<c:otherwise>An unexpected error occurred. Please make sure the database is running and try again.</c:otherwise>
</c:choose>
</p>
<a class="btn block" href="${ctx}/">Go to the home page</a>
</div></body></html>
