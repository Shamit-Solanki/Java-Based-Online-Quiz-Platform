<%@ include file="/WEB-INF/jspf/header.jspf" %>
<c:if test="${not empty mine}">
<div class="card section" style="display:flex;align-items:center;gap:16px">
  <div class="ring" style="--size:70px;--p:${mine.avgPercent}"><span class="ring-val" style="font-size:.95rem">${mine.avgPercent}%</span></div>
  <div><p class="faint" style="margin:0">Your standing</p><h2 style="margin:0">Rank #${mine.rank} of ${fn:length(board)}</h2>
    <p class="muted" style="margin:4px 0 0">${mine.quizzesTaken} quizzes taken &middot; ${mine.totalScore} total points</p></div>
</div>
</c:if>
<div class="table-wrap">
<table>
  <thead><tr><th>Rank</th><th>Participant</th><th>Quizzes taken</th><th>Total score</th><th>Average</th></tr></thead>
  <tbody>
  <c:forEach var="e" items="${board}">
    <tr style="${e.userId == me.id ? 'background:var(--brand-soft)' : ''}">
      <td><b class="rank-${e.rank}">
        <c:choose><c:when test="${e.rank==1}"><span class="medal">🥇</span></c:when>
        <c:when test="${e.rank==2}"><span class="medal">🥈</span></c:when>
        <c:when test="${e.rank==3}"><span class="medal">🥉</span></c:when>
        <c:otherwise>#${e.rank}</c:otherwise></c:choose></b></td>
      <td><c:out value="${e.name}"/> <c:if test="${e.userId == me.id}"><span class="badge brand">You</span></c:if></td>
      <td>${e.quizzesTaken}</td><td>${e.totalScore}</td><td><b>${e.avgPercent}%</b></td>
    </tr>
  </c:forEach>
  <tr class="empty-row" ${not empty board ? 'hidden' : ''}><td colspan="5">No completed attempts yet.</td></tr>
  </tbody>
</table>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
