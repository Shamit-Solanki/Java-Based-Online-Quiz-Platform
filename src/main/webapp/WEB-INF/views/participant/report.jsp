<%@ include file="/WEB-INF/jspf/header.jspf" %>
<c:if test="${justFinished}"><div class="alert ${attempt.passed ? 'success' : 'info'}">Quiz submitted! Here is how you did.</div></c:if>
<div class="card result-hero section">
  <div class="ring" data-value="${attempt.percentage}" style="--p:${attempt.percentage}"><span class="ring-val">${attempt.percentage}%</span></div>
  <h2 style="margin-bottom:2px"><c:out value="${quiz.title}"/></h2>
  <p class="muted">${attempt.score} out of ${attempt.total} correct</p>
  <div class="grade" style="color:${attempt.passed ? 'var(--success)' : 'var(--danger)'}">Grade ${attempt.grade}</div>
  <div class="btn-row" style="justify-content:center;margin-top:16px">
    <span class="badge ${attempt.passed ? 'success' : 'danger'}">${attempt.passed ? 'Passed' : 'Not passed'}</span>
    <span class="badge neutral">${correctCount} correct</span>
    <span class="badge danger">${wrongCount} incorrect</span>
    <span class="badge neutral">${skippedCount} skipped</span>
  </div>
</div>
<c:if test="${attempt.reviewed}">
  <div class="card section">
    <div class="card-head"><h2>Feedback from <c:out value="${attempt.gradedByName}"/></h2></div>
    <p><c:out value="${attempt.feedback}"/></p>
  </div>
</c:if>
<div class="card-head"><h2>Answer review</h2></div>
<%@ include file="/WEB-INF/jspf/answer-list.jspf" %>
<div class="btn-row" style="margin-top:20px">
  <a class="btn ghost" href="${ctx}/participant/quizzes">Back to quizzes</a>
  <a class="btn" href="${ctx}/participant/reports">My reports</a>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
