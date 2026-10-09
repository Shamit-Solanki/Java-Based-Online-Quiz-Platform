<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="grid cols-2" style="align-items:start">
  <div class="card">
    <div class="card-head"><h2>Set a reminder</h2></div>
    <form method="post" action="${ctx}/participant/reminders">
      <input type="hidden" name="action" value="add">
      <label for="quizId">Quiz</label>
      <select id="quizId" name="quizId" required>
        <option value="">Choose a quiz…</option>
        <c:forEach var="q" items="${quizzes}"><option value="${q.id}" ${q.id == preselect ? 'selected' : ''}><c:out value="${q.title}"/></option></c:forEach>
      </select>
      <label for="remindAt">Date &amp; time</label>
      <input type="datetime-local" id="remindAt" name="remindAt" required>
      <label for="note">Note <span class="hint">(optional)</span></label>
      <input type="text" id="note" name="note" maxlength="200" placeholder="e.g. Revise chapter 4 first">
      <button class="btn" type="submit" style="margin-top:14px">Set reminder</button>
    </form>
  </div>
  <div class="card">
    <div class="card-head"><h2>Your reminders</h2></div>
    <c:forEach var="r" items="${reminders}">
      <div class="list-item">
        <div class="meta"><b><c:out value="${r.quizTitle}"/></b>
          <span><fmt:formatDate value="${r.remindAt}" pattern="MMM d, yyyy h:mm a"/><c:if test="${not empty r.note}"> &middot; <c:out value="${r.note}"/></c:if></span></div>
        <div class="trail">
          <c:if test="${r.due}"><span class="badge warning">Due</span></c:if>
          <form method="post" action="${ctx}/participant/reminders"><input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${r.id}">
            <button class="icon-btn sm" type="submit" style="width:30px;height:30px" title="Remove">×</button></form>
        </div>
      </div>
    </c:forEach>
    <c:if test="${empty reminders}"><div class="empty-state"><div class="big">⏰</div><p>No reminders set yet.</p></div></c:if>
  </div>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
