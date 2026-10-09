<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="toolbar">
  <div class="search"><input type="search" placeholder="Search by participant or quiz…" data-filter-table="resultsTable"></div>
  <select data-filter-col="quiz" data-for="resultsTable" style="max-width:220px">
    <option value="">All quizzes</option>
    <c:forEach var="t" items="${quizTitles}"><option value="<c:out value='${t}'/>"><c:out value="${t}"/></option></c:forEach>
  </select>
</div>
<div class="table-wrap">
<table id="resultsTable" class="sortable">
  <thead><tr><th data-sort>Participant</th><th data-sort>Quiz</th><th data-sort>Score</th><th data-sort>Submitted</th><th data-sort>Status</th><th></th></tr></thead>
  <tbody>
  <c:forEach var="r" items="${results}">
    <tr data-quiz="<c:out value='${r.quizTitle}'/>">
      <td><c:out value="${r.participantName}"/></td>
      <td><c:out value="${r.quizTitle}"/></td>
      <td data-v="${r.percentage}"><b>${r.score}/${r.total}</b> <span class="faint">(${r.percentage}%)</span></td>
      <td data-v="<fmt:formatDate value='${r.submittedAt}' pattern='yyyyMMddHHmm'/>"><fmt:formatDate value="${r.submittedAt}" pattern="MMM d, h:mm a"/></td>
      <td>
        <span class="badge ${r.passed ? 'success' : 'danger'}">${r.passed ? 'Passed' : 'Failed'}</span>
        <c:if test="${r.reviewed}"><span class="badge brand">Reviewed</span></c:if>
      </td>
      <td class="row-actions"><a class="btn sm" href="${ctx}/creator/review?id=${r.attemptId}">${r.reviewed ? 'View review' : 'Review'}</a></td>
    </tr>
  </c:forEach>
  <tr class="empty-row" hidden><td colspan="6">No results match.</td></tr>
  </tbody>
</table>
</div>
<c:if test="${empty results}"><div class="card empty-state"><div class="big">📊</div><p>No submissions yet &mdash; results appear once participants take your quizzes.</p></div></c:if>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
