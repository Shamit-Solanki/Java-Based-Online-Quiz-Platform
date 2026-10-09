<%@ include file="/WEB-INF/jspf/header.jspf" %>
<a class="icon-link" href="${ctx}/admin/quizzes">&larr; Back to quiz content</a>
<div class="card section" style="margin-top:16px">
  <div class="card-head">
    <div><h2 style="margin-bottom:4px"><c:out value="${quiz.title}"/></h2>
      <span class="badge ${quiz.status.badge}"><c:out value="${quiz.status.label}"/></span></div>
    <div class="spacer"></div>
    <span class="faint">${quiz.durationMinutes} min · ${fn:length(questions)} questions · by <c:out value="${quiz.creatorName}"/></span>
  </div>
  <p class="muted"><c:out value="${quiz.description}"/></p>
  <c:if test="${not empty quiz.reviewNote}">
    <div class="alert ${quiz.status == 'REJECTED' ? 'error' : 'info'}"><b>Review note:</b> <c:out value="${quiz.reviewNote}"/></div>
  </c:if>
  <c:if test="${quiz.status == 'PENDING'}">
    <div class="btn-row" style="margin-top:14px">
      <form method="post" action="${ctx}/admin/quizzes"><input type="hidden" name="action" value="approve"><input type="hidden" name="id" value="${quiz.id}"><input type="hidden" name="from" value="preview">
        <button class="btn success" type="submit">Approve &amp; publish</button></form>
      <button class="btn danger" type="button" data-open="rejectDlg">Reject</button>
    </div>
  </c:if>
</div>

<div class="section" style="display:flex;flex-direction:column;gap:14px">
<c:forEach var="q" items="${questions}" varStatus="st">
  <div class="card">
    <div class="q-head"><span class="q-no">${st.index+1}</span><span class="q-title"><c:out value="${q.questionText}"/></span></div>
    <ul class="opts" style="list-style:none;padding:0;margin:0;display:grid;gap:7px">
      <c:forEach var="L" items="${['A','B','C','D']}">
        <li style="display:flex;gap:10px;align-items:center;padding:8px 10px;border-radius:8px;border:1px solid var(--border);font-size:.86rem"
            class="${L == q.correctOption ? 'is-correct' : ''}">
          <b style="width:20px;height:20px;border-radius:50%;background:${L==q.correctOption?'var(--success)':'var(--surface-2)'};color:${L==q.correctOption?'#fff':'inherit'};display:flex;align-items:center;justify-content:center;font-size:.7rem;font-weight:700">${L}</b>
          <span><c:out value="${q.getOption(L)}"/></span>
          <c:if test="${L == q.correctOption}"><em style="margin-left:auto;font-style:normal;color:var(--success);font-weight:700;font-size:.72rem">✓ correct</em></c:if>
        </li>
      </c:forEach>
    </ul>
    <c:if test="${not empty q.explanation}"><p class="why" style="margin-top:12px;background:var(--info-soft);color:var(--info);padding:10px 12px;border-radius:8px;font-size:.83rem"><c:out value="${q.explanation}"/></p></c:if>
  </div>
</c:forEach>
</div>

<dialog id="rejectDlg">
  <form method="post" action="${ctx}/admin/quizzes" class="modal-body">
    <input type="hidden" name="action" value="reject"><input type="hidden" name="id" value="${quiz.id}"><input type="hidden" name="from" value="preview">
    <h2>Reject "${quiz.title}"</h2>
    <label for="note">Reason (shown to the creator)</label>
    <textarea id="note" name="note" maxlength="500" required placeholder="Explain what needs to change before resubmitting…"></textarea>
    <div class="modal-actions"><button type="button" class="btn ghost" data-close>Cancel</button><button type="submit" class="btn danger">Reject quiz</button></div>
  </form>
</dialog>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
