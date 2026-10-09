<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="toolbar">
  <div class="search"><input type="search" placeholder="Search quizzes…" data-filter-table="quizTable"></div>
  <div class="tabbar" data-tabs="quizTable">
    <button class="on" data-value="">All <span class="faint">(${counts['DRAFT']+counts['PENDING']+counts['APPROVED']+counts['REJECTED']})</span></button>
    <button data-value="PENDING">Pending (${counts['PENDING']})</button>
    <button data-value="APPROVED">Approved (${counts['APPROVED']})</button>
    <button data-value="REJECTED">Rejected (${counts['REJECTED']})</button>
    <button data-value="DRAFT">Draft (${counts['DRAFT']})</button>
  </div>
</div>
<div class="table-wrap">
<table id="quizTable" class="sortable">
  <thead><tr><th data-sort>Title</th><th data-sort>Creator</th><th data-sort>Status</th><th data-sort>Questions</th><th data-sort>Attempts</th><th data-sort>Avg score</th><th></th></tr></thead>
  <tbody>
  <c:forEach var="q" items="${quizzes}">
    <tr data-status="${q.status}">
      <td><span class="cell-main"><c:out value="${q.title}"/></span><div class="cell-sub">${q.durationMinutes} min</div></td>
      <td><c:out value="${q.creatorName}"/></td>
      <td><span class="badge ${q.status.badge}"><c:out value="${q.status.label}"/></span></td>
      <td>${q.questionCount}</td>
      <td>${q.attemptCount}</td>
      <td><c:choose><c:when test="${q.attemptCount > 0}">${q.avgPercent}%</c:when><c:otherwise>—</c:otherwise></c:choose></td>
      <td class="row-actions">
        <a class="btn sm ghost" href="${ctx}/admin/quiz?id=${q.id}">Preview</a>
        <form method="post" action="${ctx}/admin/quizzes" data-confirm="Delete '${q.title}'? This removes all its questions and results."><input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${q.id}">
          <button class="btn sm danger" type="submit">Delete</button></form>
      </td>
    </tr>
  </c:forEach>
  <tr class="empty-row" hidden><td colspan="7">No quizzes match.</td></tr>
  </tbody>
</table>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
