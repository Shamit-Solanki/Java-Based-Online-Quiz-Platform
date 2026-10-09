<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="section grid cols-4">
  <div class="stat accent-success"><span class="label">Approved quizzes</span><span class="value" data-count="${approvedCount}">0</span></div>
  <div class="stat accent-warning"><span class="label">Pending approval</span><span class="value" data-count="${pendingCount}">0</span></div>
  <div class="stat accent-brand"><span class="label">Total attempts</span><span class="value" data-count="${attemptCount}">0</span>
    <span class="sub">${awaitingReview} awaiting review</span></div>
  <div class="stat accent-info"><span class="label">Average score</span><span class="value" data-count="${avgPercent}">0</span><span class="sub">${passRate}% pass rate</span></div>
</div>

<div class="section grid cols-2" style="align-items:start">
  <div class="card">
    <div class="card-head"><h2>Performance by quiz</h2></div>
    <div class="hbars">
      <c:forEach var="q" items="${performance}">
        <div class="hbar-row"><span class="lbl" title="${q.title}"><c:out value="${q.title}"/></span>
          <div class="hbar-track"><div class="hbar-fill" style="width:${q.avgPercent}%"></div></div>
          <span class="pct">${q.avgPercent}%</span></div>
      </c:forEach>
      <c:if test="${empty performance}"><p class="faint">No attempts yet &mdash; once a quiz is approved and taken, its average appears here.</p></c:if>
    </div>
  </div>
  <div class="card">
    <div class="card-head"><h2>Recent results</h2><div class="spacer"></div><a class="icon-link" href="${ctx}/creator/results">View all</a></div>
    <c:forEach var="r" items="${recentResults}">
      <div class="list-item"><div class="meta"><b><c:out value="${r.participantName}"/></b><span><c:out value="${r.quizTitle}"/></span></div>
        <div class="trail"><span class="badge ${r.passed ? 'success' : 'danger'}">${r.score}/${r.total}</span></div></div>
    </c:forEach>
    <c:if test="${empty recentResults}"><div class="empty-state"><div class="big">📊</div><p>No submissions yet.</p></div></c:if>
  </div>
</div>

<div class="section grid cols-2" style="align-items:start">
  <div class="card">
    <div class="card-head"><h2>Rejected quizzes</h2></div>
    <c:forEach var="q" items="${rejected}">
      <div class="list-item"><div class="meta"><b><c:out value="${q.title}"/></b><span><c:out value="${q.reviewNote}"/></span></div>
        <div class="trail"><a class="btn sm ghost" href="${ctx}/creator/quiz-form?id=${q.id}">Fix &amp; resubmit</a></div></div>
    </c:forEach>
    <c:if test="${empty rejected}"><div class="empty-state"><div class="big">✓</div><p>Nothing rejected right now.</p></div></c:if>
  </div>
  <div class="card">
    <div class="card-head"><h2>Inbox</h2><div class="spacer"></div><a class="icon-link" href="${ctx}/messages">Open messages</a></div>
    <c:forEach var="m" items="${inbox}">
      <div class="list-item"><span class="initials"><c:out value="${m.senderName}"/></span>
        <div class="meta"><b><c:out value="${m.senderName}"/></b><span><c:out value="${m.text}"/></span></div></div>
    </c:forEach>
    <c:if test="${empty inbox}"><div class="empty-state"><div class="big">💬</div><p>No messages yet.</p></div></c:if>
  </div>
</div>
<a class="btn lg" href="${ctx}/creator/quiz-form" style="margin-top:8px">+ Create a new quiz</a>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
