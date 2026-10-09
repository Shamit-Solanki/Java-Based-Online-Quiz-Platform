<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="toolbar"><div class="spacer"></div><a class="btn" href="${ctx}/creator/quiz-form">+ Create quiz</a></div>
<div class="grid auto">
<c:forEach var="q" items="${quizzes}">
  <div class="quiz-card">
    <div class="top"><h3><c:out value="${q.title}"/></h3><span class="badge ${q.status.badge}"><c:out value="${q.status.label}"/></span></div>
    <p class="desc"><c:out value="${q.description}"/></p>
    <c:if test="${not empty q.reviewNote}"><div class="alert error" style="margin:0"><c:out value="${q.reviewNote}"/></div></c:if>
    <div class="facts"><span>⏱ ${q.durationMinutes} min</span><span>❓ ${q.questionCount} questions</span><span>▶ ${q.attemptCount} attempts</span></div>
    <div class="foot">
      <c:if test="${q.editable}">
        <a class="btn sm ghost" href="${ctx}/creator/quiz-form?id=${q.id}">Edit</a>
        <form method="post" action="${ctx}/creator/quizzes"><input type="hidden" name="action" value="submit"><input type="hidden" name="id" value="${q.id}">
          <button class="btn sm" type="submit" ${q.questionCount == 0 ? 'disabled title="Add questions first"' : ''}>Submit for approval</button></form>
        <form method="post" action="${ctx}/creator/quizzes" data-confirm="Delete '${q.title}'?"><input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${q.id}">
          <button class="btn sm danger" type="submit">Delete</button></form>
      </c:if>
      <c:if test="${q.status == 'PENDING'}"><span class="faint">Waiting for admin review…</span></c:if>
      <c:if test="${q.status == 'APPROVED'}"><span class="faint">Live for participants</span></c:if>
    </div>
  </div>
</c:forEach>
</div>
<c:if test="${empty quizzes}"><div class="card empty-state"><div class="big">✎</div><p>You haven't created any quizzes yet.</p>
  <a class="btn" href="${ctx}/creator/quiz-form">Create your first quiz</a></div></c:if>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
