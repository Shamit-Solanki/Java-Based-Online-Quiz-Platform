<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="grid auto">
<c:forEach var="q" items="${quizzes}">
  <c:set var="used" value="${q.attemptsUsed}"/>
  <c:set var="maxed" value="${used >= maxAttempts}"/>
  <div class="quiz-card">
    <div class="top"><h3><c:out value="${q.title}"/></h3>
      <c:if test="${q.bestScore >= 0}"><span class="badge success">${q.bestScore}/${q.questionCount}</span></c:if></div>
    <p class="desc"><c:out value="${q.description}"/></p>
    <div class="facts"><span>⏱ ${q.durationMinutes} min</span><span>❓ ${q.questionCount} questions</span>
      <span>↻ ${used}/${maxAttempts} attempts used</span></div>
    <div class="foot">
      <c:choose>
        <c:when test="${q.questionCount == 0}"><span class="faint">No questions yet</span></c:when>
        <c:when test="${maxed}"><span class="badge neutral">Attempts used up</span></c:when>
        <c:otherwise><a class="btn" href="${ctx}/participant/take?id=${q.id}">${used > 0 ? 'Try again' : 'Start quiz'}</a></c:otherwise>
      </c:choose>
      <a class="btn sm ghost" href="${ctx}/participant/reminders?quiz=${q.id}">⏰ Remind me</a>
    </div>
  </div>
</c:forEach>
</div>
<c:if test="${empty quizzes}"><div class="card empty-state"><div class="big">📭</div><p>No quizzes are available right now. Check back soon!</p></div></c:if>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
