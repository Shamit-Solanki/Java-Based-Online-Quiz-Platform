<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${quiz.title}"/> · QuizMania</title>
<meta name="csrf" content="<c:out value='${sessionScope.csrf}'/>">
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@500;700&display=swap">
<link rel="stylesheet" href="${ctx}/css/style.css">
<script>try{var t=localStorage.getItem('theme');if(t)document.documentElement.dataset.theme=t;}catch(e){}</script>
</head><body class="focus-mode">
<div class="focus-wrap">
  <div class="quiz-topbar">
    <h1><c:out value="${quiz.title}"/></h1>
    <span class="faint" id="qPos"></span>
    <div class="timer-ring" id="timeRing"><span id="timer">--:--</span></div>
  </div>
  <div class="quiz-body">
    <div class="quiz-main">
      <div class="progress-track"><div class="progress-fill" id="progressBar"></div></div>
      <form id="quizForm" method="post" action="${ctx}/participant/submit"
            data-attempt="${attempt.attemptId}" data-remaining="${attempt.remainingSeconds}" data-total="${quiz.durationMinutes * 60}">
        <input type="hidden" name="attemptId" value="${attempt.attemptId}">
        <input type="hidden" name="_csrf" value="<c:out value='${sessionScope.csrf}'/>">
        <c:forEach var="q" items="${questions}" varStatus="st">
          <div class="q-slide card" ${st.first ? '' : 'hidden'}>
            <div class="q-pos">Question ${st.index+1} of ${fn:length(questions)}</div>
            <div class="q-text"><c:out value="${q.questionText}"/></div>
            <c:forEach var="L" items="${['A','B','C','D']}">
              <label class="choice"><input type="radio" name="q_${q.id}" value="${L}"><span class="k">${L}</span><span><c:out value="${q.getOption(L)}"/></span></label>
            </c:forEach>
          </div>
        </c:forEach>
        <div class="nav-row">
          <button type="button" id="prev" class="btn ghost">&larr; Previous</button>
          <button type="button" id="next" class="btn">Next &rarr;</button>
          <button type="button" id="finish" class="btn success" hidden>Finish &amp; submit</button>
        </div>
      </form>
    </div>
    <div class="palette-card card">
      <h3 style="margin-bottom:4px">Question map</h3>
      <span class="faint" id="answeredCount"></span>
      <div class="palette" id="palette"></div>
      <div class="answered-count">Click a number to jump, or use ← → and A-D keys.</div>
    </div>
  </div>
  <div class="quiz-footer-bar">
    <button type="button" id="prevM" class="btn ghost sm" onclick="document.getElementById('prev').click()">&larr;</button>
    <span class="faint" style="flex:1;text-align:center" id="answeredCountM"></span>
    <button type="button" class="btn sm" onclick="document.getElementById('next').click()">&rarr;</button>
  </div>
</div>
<dialog id="confirmDlg">
  <div class="modal-body">
    <h2>Submit your answers?</h2>
    <p class="muted" id="unansweredMsg"></p>
    <div class="modal-actions"><button type="button" class="btn ghost" data-close>Keep working</button><button type="button" id="confirmYes" class="btn success">Submit now</button></div>
  </div>
</dialog>
<div class="toasts" id="toasts" aria-live="polite"></div>
<script src="${ctx}/js/app.js"></script>
</body></html>
