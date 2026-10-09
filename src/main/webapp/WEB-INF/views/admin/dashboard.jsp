<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="section grid cols-4">
  <div class="stat accent-brand"><span class="icon">☺</span><span class="label">Total users</span><span class="value" data-count="${totalUsers}">0</span>
    <span class="sub">${roleCounts['PARTICIPANT']} participants · ${roleCounts['CREATOR']} creators</span></div>
  <div class="stat accent-info"><span class="icon">✎</span><span class="label">Total quizzes</span><span class="value" data-count="${totalQuizzes}">0</span>
    <span class="sub">${statusCounts['APPROVED']} approved · ${statusCounts['PENDING']} pending</span></div>
  <div class="stat accent-success"><span class="icon">▶</span><span class="label">Attempts submitted</span><span class="value" data-count="${totals['attempts']}">0</span>
    <span class="sub">across the whole platform</span></div>
  <div class="stat accent-warning"><span class="icon">⌁</span><span class="label">Average score</span><span class="value" data-count="${totals['avgPercent']}">0</span>
    <span class="sub">percent, all attempts</span></div>
</div>

<div class="section grid cols-2" style="align-items:start">
  <div class="card">
    <div class="card-head"><h2>Attempts, last 7 days</h2></div>
    <div class="bars">
      <c:forEach var="d" items="${perDay}">
        <div class="bar-col"><span class="bar-val">${d['count']}</span>
          <div class="bar" style="height:${d['count']/maxDay*140}px"></div>
          <span class="bar-label">${d['label']}</span></div>
      </c:forEach>
    </div>
  </div>
  <div class="card">
    <div class="card-head"><h2>Top quizzes by activity</h2></div>
    <div class="hbars">
      <c:forEach var="q" items="${quizPerformance}">
        <div class="hbar-row"><span class="lbl" title="${q['title']}"><c:out value="${q['title']}"/></span>
          <div class="hbar-track"><div class="hbar-fill" style="width:${q['avg']}%"></div></div>
          <span class="pct">${q['avg']}%</span></div>
      </c:forEach>
      <c:if test="${empty quizPerformance}"><p class="faint">No attempts recorded yet.</p></c:if>
    </div>
  </div>
</div>

<div class="section grid cols-2" style="align-items:start">
  <div class="card">
    <div class="card-head"><h2>Pending quiz approvals</h2><div class="spacer"></div>
      <a class="icon-link" href="${ctx}/admin/quizzes">View all</a></div>
    <c:forEach var="q" items="${pending}">
      <div class="list-item">
        <div class="meta"><b><c:out value="${q.title}"/></b><span>by <c:out value="${q.creatorName}"/> · ${q.questionCount} questions</span></div>
        <div class="trail"><a class="btn sm" href="${ctx}/admin/quiz?id=${q.id}">Review</a></div>
      </div>
    </c:forEach>
    <c:if test="${empty pending}"><div class="empty-state"><div class="big">✓</div><p>Nothing waiting for approval.</p></div></c:if>
  </div>
  <div class="card">
    <div class="card-head"><h2>System alerts</h2><div class="spacer"></div>
      <a class="icon-link" href="${ctx}/notifications">View all</a></div>
    <c:forEach var="a" items="${alerts}">
      <div class="list-item"><div class="meta"><b><c:out value="${a.text}"/></b>
        <span><fmt:formatDate value="${a.createdAt}" pattern="MMM d, h:mm a"/></span></div></div>
    </c:forEach>
    <c:if test="${empty alerts}"><div class="empty-state"><div class="big">🔔</div><p>No alerts yet.</p></div></c:if>
  </div>
</div>

<div class="card">
  <div class="card-head"><h2>Recently joined</h2><div class="spacer"></div><a class="icon-link" href="${ctx}/admin/users">Manage users</a></div>
  <c:forEach var="u" items="${recentUsers}">
    <div class="list-item">
      <span class="initials"><c:out value="${u.initials}"/></span>
      <div class="meta"><b><c:out value="${u.name}"/></b><span><c:out value="${u.email}"/></span></div>
      <div class="trail"><span class="badge brand"><c:out value="${u.role.label}"/></span></div>
    </div>
  </c:forEach>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
