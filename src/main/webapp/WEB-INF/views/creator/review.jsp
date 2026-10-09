<%@ include file="/WEB-INF/jspf/header.jspf" %>
<a class="icon-link" href="${ctx}/creator/results">&larr; Back to results</a>
<div class="grid cols-2 section" style="align-items:start;margin-top:16px">
  <div class="card">
    <div class="card-head"><h2><c:out value="${attempt.quizTitle}"/></h2></div>
    <div class="kv"><b>Participant</b><span><c:out value="${attempt.participantName}"/></span></div>
    <div class="kv"><b>Auto score</b><span>${attempt.autoScore}/${attempt.total}</span></div>
    <div class="kv"><b>Current score</b><span><b>${attempt.score}/${attempt.total} (${attempt.percentage}%)</b></span></div>
    <div class="kv"><b>Result</b><span class="badge ${attempt.passed ? 'success' : 'danger'}">${attempt.passed ? 'Passed' : 'Failed'}</span></div>
    <div class="kv"><b>Submitted</b><span><fmt:formatDate value="${attempt.submittedAt}" pattern="MMM d, yyyy h:mm a"/></span></div>
    <c:if test="${not empty attempt.gradedByName}"><div class="kv"><b>Last reviewed by</b><span><c:out value="${attempt.gradedByName}"/></span></div></c:if>
  </div>
  <div class="card">
    <div class="card-head"><h2>Grade &amp; feedback</h2></div>
    <form method="post" action="${ctx}/creator/review?id=${attempt.attemptId}">
      <label for="score">Score (0&ndash;${attempt.total})</label>
      <input type="number" id="score" name="score" min="0" max="${attempt.total}" value="${attempt.score}" required>
      <label for="feedback">Feedback for the participant</label>
      <textarea id="feedback" name="feedback" maxlength="1000" data-counter="fbCount" placeholder="What did they do well? What should they review?"><c:out value="${attempt.feedback}"/></textarea>
      <div class="char-count" id="fbCount"></div>
      <button class="btn" type="submit" style="margin-top:10px">Save review</button>
    </form>
  </div>
</div>
<div class="card-head"><h2>Answers</h2></div>
<%@ include file="/WEB-INF/jspf/answer-list.jspf" %>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
