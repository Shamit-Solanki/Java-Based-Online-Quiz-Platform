<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="section grid cols-4">
  <div class="stat accent-brand"><span class="label">Quizzes completed</span><span class="value" data-count="${summary.completed}">0</span></div>
  <div class="stat accent-success"><span class="label">Average score</span><span class="value" data-count="${summary.avgPercent}">0</span><span class="sub">percent</span></div>
  <div class="stat accent-info"><span class="label">Best score</span><span class="value" data-count="${summary.bestPercent}">0</span><span class="sub">percent</span></div>
  <div class="stat accent-warning"><span class="label">Available quizzes</span><span class="value" data-count="${availableQuizzes}">0</span>
    <span class="sub"><a class="icon-link" href="${ctx}/participant/quizzes">Browse &rarr;</a></span></div>
</div>

<div class="section grid cols-2" style="align-items:start">
  <div class="card">
    <div class="card-head"><h2>Recent attempts</h2><div class="spacer"></div><a class="icon-link" href="${ctx}/participant/reports">View all</a></div>
    <c:forEach var="r" items="${history}">
      <div class="list-item"><div class="meta"><b><c:out value="${r.quizTitle}"/></b><span><fmt:formatDate value="${r.submittedAt}" pattern="MMM d, h:mm a"/></span></div>
        <div class="trail"><span class="badge ${r.passed ? 'success' : 'danger'}">${r.score}/${r.total}</span></div></div>
    </c:forEach>
    <c:if test="${empty history}"><div class="empty-state"><div class="big">📝</div><p>No attempts yet.</p><a class="btn sm" href="${ctx}/participant/quizzes">Take your first quiz</a></div></c:if>
  </div>
  <div class="card">
    <div class="card-head"><h2>Leaderboard</h2><div class="spacer"></div><a class="icon-link" href="${ctx}/participant/leaderboard">View all</a></div>
    <c:forEach var="e" items="${top}">
      <div class="list-item"><b class="rank-${e.rank}" style="width:26px">#${e.rank}</b>
        <div class="meta"><b><c:out value="${e.name}"/></b><span>${e.quizzesTaken} quizzes</span></div>
        <div class="trail"><b>${e.avgPercent}%</b></div></div>
    </c:forEach>
    <c:if test="${not empty mine && mine.rank > 5}">
      <div class="divider"></div>
      <div class="list-item"><b style="width:26px">#${mine.rank}</b><div class="meta"><b>You</b><span>${mine.quizzesTaken} quizzes</span></div><div class="trail"><b>${mine.avgPercent}%</b></div></div>
    </c:if>
    <c:if test="${empty top}"><div class="empty-state"><div class="big">🏆</div><p>Be the first on the leaderboard.</p></div></c:if>
  </div>
</div>

<div class="section grid cols-2" style="align-items:start">
  <div class="card">
    <div class="card-head"><h2>Upcoming reminders</h2><div class="spacer"></div><a class="icon-link" href="${ctx}/participant/reminders">Manage</a></div>
    <c:forEach var="r" items="${reminders}">
      <div class="list-item"><div class="meta"><b><c:out value="${r.quizTitle}"/></b><span><fmt:formatDate value="${r.remindAt}" pattern="MMM d, h:mm a"/><c:if test="${not empty r.note}"> &middot; <c:out value="${r.note}"/></c:if></span></div>
        <div class="trail"><c:if test="${r.due}"><span class="badge warning">Due</span></c:if></div></div>
    </c:forEach>
    <c:if test="${empty reminders}"><div class="empty-state"><div class="big">⏰</div><p>No reminders set.</p></div></c:if>
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
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
