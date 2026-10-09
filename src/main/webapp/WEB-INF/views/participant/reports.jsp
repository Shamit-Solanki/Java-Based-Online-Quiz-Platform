<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="section grid cols-4">
  <div class="stat accent-brand"><span class="label">Completed</span><span class="value" data-count="${summary.completed}">0</span></div>
  <div class="stat accent-success"><span class="label">Average</span><span class="value" data-count="${summary.avgPercent}">0</span><span class="sub">percent</span></div>
  <div class="stat accent-info"><span class="label">Best score</span><span class="value" data-count="${summary.bestPercent}">0</span><span class="sub">percent</span></div>
  <div class="stat accent-warning"><span class="label">Pass rate</span><span class="value" data-count="${summary.passRate}">0</span><span class="sub">percent</span></div>
</div>
<div class="card section">
  <div class="card-head"><h2>Score trend</h2></div>
  <div class="bars">
    <c:forEach var="r" items="${summary.trend}">
      <div class="bar-col"><span class="bar-val">${r.percentage}%</span>
        <div class="bar" style="height:${r.percentage/100*140}px"></div>
        <span class="bar-label"><c:out value="${r.quizTitle}"/></span></div>
    </c:forEach>
    <c:if test="${empty summary.trend}"><p class="faint">Take a quiz to see your trend here.</p></c:if>
  </div>
</div>
<div class="table-wrap">
<table id="histTable" class="sortable">
  <thead><tr><th data-sort>Quiz</th><th data-sort>Score</th><th data-sort>Grade</th><th data-sort>Submitted</th><th data-sort>Status</th><th></th></tr></thead>
  <tbody>
  <c:forEach var="r" items="${results}">
    <tr>
      <td><c:out value="${r.quizTitle}"/></td>
      <td data-v="${r.percentage}">${r.score}/${r.total} <span class="faint">(${r.percentage}%)</span></td>
      <td><b class="mono">${r.grade}</b></td>
      <td data-v="<fmt:formatDate value='${r.submittedAt}' pattern='yyyyMMddHHmm'/>"><fmt:formatDate value="${r.submittedAt}" pattern="MMM d, h:mm a"/></td>
      <td><span class="badge ${r.passed ? 'success' : 'danger'}">${r.passed ? 'Passed' : 'Failed'}</span></td>
      <td><a class="btn sm ghost" href="${ctx}/participant/report?id=${r.attemptId}">View report</a></td>
    </tr>
  </c:forEach>
  <tr class="empty-row" ${not empty results ? 'hidden' : ''}><td colspan="6">No attempts yet.</td></tr>
  </tbody>
</table>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
