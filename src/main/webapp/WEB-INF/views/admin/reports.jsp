<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="section grid cols-3">
  <div class="stat accent-brand"><span class="label">Attempts submitted</span><span class="value" data-count="${totals['attempts']}">0</span></div>
  <div class="stat accent-success"><span class="label">Average score</span><span class="value" data-count="${totals['avgPercent']}">0</span><span class="sub">percent</span></div>
  <div class="stat accent-info"><span class="label">Quizzes graded</span><span class="value" data-count="${fn:length(quizPerformance)}">0</span><span class="sub">with at least one attempt</span></div>
</div>

<div class="section grid cols-2" style="align-items:start">
  <div class="card">
    <div class="card-head"><h2>Attempts, last 14 days</h2></div>
    <div class="bars">
      <c:forEach var="d" items="${perDay}">
        <div class="bar-col"><span class="bar-val">${d['count']}</span>
          <div class="bar" style="height:${d['count']/maxDay*140}px"></div>
          <span class="bar-label">${d['label']}</span></div>
      </c:forEach>
    </div>
  </div>
  <div class="card">
    <div class="card-head"><h2>Score distribution</h2></div>
    <div class="bars">
      <c:forEach var="e" items="${bands}">
        <div class="bar-col"><span class="bar-val">${e.value}</span>
          <div class="bar" style="height:${e.value/maxBand*140}px"></div>
          <span class="bar-label"><c:out value="${e.key}"/></span></div>
      </c:forEach>
    </div>
  </div>
</div>

<div class="card section">
  <div class="card-head"><h2>Average score by quiz</h2></div>
  <div class="hbars">
    <c:forEach var="q" items="${quizPerformance}">
      <div class="hbar-row"><span class="lbl" title="${q['title']}"><c:out value="${q['title']}"/></span>
        <div class="hbar-track"><div class="hbar-fill" style="width:${q['avg']}%"></div></div>
        <span class="pct">${q['avg']}%</span></div>
    </c:forEach>
    <c:if test="${empty quizPerformance}"><p class="faint">No attempts recorded yet.</p></c:if>
  </div>
</div>

<div class="card">
  <div class="card-head"><h2>Leaderboard (top 10)</h2></div>
  <div class="table-wrap" style="border:0">
    <table><thead><tr><th>Rank</th><th>Participant</th><th>Quizzes</th><th>Avg %</th></tr></thead>
    <tbody>
    <c:forEach var="e" items="${leaders}">
      <tr><td><b class="rank-${e.rank}">#${e.rank}</b></td><td><c:out value="${e.name}"/></td><td>${e.quizzesTaken}</td><td>${e.avgPercent}%</td></tr>
    </c:forEach>
    <c:if test="${empty leaders}"><tr class="empty-row"><td colspan="4">No completed attempts yet.</td></tr></c:if>
    </tbody></table>
  </div>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
