<%@ include file="/WEB-INF/jspf/header.jspf" %>
<c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>
<form method="post" action="${ctx}/creator/quiz-form">
  <input type="hidden" name="id" value="${quiz.id}">
  <div class="card section">
    <div class="card-head"><h2>Quiz details</h2></div>
    <label for="title">Title</label>
    <input type="text" id="title" name="title" value="<c:out value='${quiz.title}'/>" maxlength="200" required>
    <label for="description">Description</label>
    <textarea id="description" name="description" maxlength="500"><c:out value="${quiz.description}"/></textarea>
    <label for="duration">Duration (minutes)</label>
    <input type="number" id="duration" name="duration" min="1" max="300" value="${quiz.durationMinutes == 0 ? 15 : quiz.durationMinutes}" required style="max-width:160px">
  </div>

  <div class="card-head"><h2>Questions</h2><div class="spacer"></div><span class="faint" id="qCount"></span></div>
  <div id="questionList" data-max="${maxQuestions}">
    <c:forEach var="q" items="${questions}" varStatus="st">
      <div class="q-card">
        <div class="q-head"><span class="q-no">${st.index+1}</span><span class="q-title">Question</span>
          <div class="q-tools">
            <button type="button" class="q-up" title="Move up">↑</button>
            <button type="button" class="q-down" title="Move down">↓</button>
            <button type="button" class="q-dup" title="Duplicate">⧉</button>
            <button type="button" class="q-remove" title="Remove">✕</button>
          </div>
        </div>
        <textarea name="question" maxlength="500" placeholder="Question text" required><c:out value="${q.questionText}"/></textarea>
        <div class="opt-grid">
          <label class="opt-pick ${q.correctOption=='A'?'on':''}" data-l="A"><b>A</b><input type="text" name="optionA" value="<c:out value='${q.optionA}'/>" maxlength="255" placeholder="Option A" required></label>
          <label class="opt-pick ${q.correctOption=='B'?'on':''}" data-l="B"><b>B</b><input type="text" name="optionB" value="<c:out value='${q.optionB}'/>" maxlength="255" placeholder="Option B" required></label>
          <label class="opt-pick ${q.correctOption=='C'?'on':''}" data-l="C"><b>C</b><input type="text" name="optionC" value="<c:out value='${q.optionC}'/>" maxlength="255" placeholder="Option C" required></label>
          <label class="opt-pick ${q.correctOption=='D'?'on':''}" data-l="D"><b>D</b><input type="text" name="optionD" value="<c:out value='${q.optionD}'/>" maxlength="255" placeholder="Option D" required></label>
        </div>
        <label style="margin-top:10px">Correct answer</label>
        <select name="correct" required>
          <option value="A" ${q.correctOption=='A'?'selected':''}>A</option>
          <option value="B" ${q.correctOption=='B'?'selected':''}>B</option>
          <option value="C" ${q.correctOption=='C'?'selected':''}>C</option>
          <option value="D" ${q.correctOption=='D'?'selected':''}>D</option>
        </select>
        <label>Explanation <span class="hint">(optional, shown in the participant's report)</span></label>
        <input type="text" name="explanation" value="<c:out value='${q.explanation}'/>" maxlength="500">
      </div>
    </c:forEach>
  </div>

  <template id="questionTpl">
    <div class="q-card">
      <div class="q-head"><span class="q-no">1</span><span class="q-title">Question</span>
        <div class="q-tools">
          <button type="button" class="q-up" title="Move up">↑</button>
          <button type="button" class="q-down" title="Move down">↓</button>
          <button type="button" class="q-dup" title="Duplicate">⧉</button>
          <button type="button" class="q-remove" title="Remove">✕</button>
        </div>
      </div>
      <textarea name="question" maxlength="500" placeholder="Question text" required></textarea>
      <div class="opt-grid">
        <label class="opt-pick on" data-l="A"><b>A</b><input type="text" name="optionA" maxlength="255" placeholder="Option A" required></label>
        <label class="opt-pick" data-l="B"><b>B</b><input type="text" name="optionB" maxlength="255" placeholder="Option B" required></label>
        <label class="opt-pick" data-l="C"><b>C</b><input type="text" name="optionC" maxlength="255" placeholder="Option C" required></label>
        <label class="opt-pick" data-l="D"><b>D</b><input type="text" name="optionD" maxlength="255" placeholder="Option D" required></label>
      </div>
      <label style="margin-top:10px">Correct answer</label>
      <select name="correct" required><option value="A">A</option><option value="B">B</option><option value="C">C</option><option value="D">D</option></select>
      <label>Explanation <span class="hint">(optional, shown in the participant's report)</span></label>
      <input type="text" name="explanation" maxlength="500">
    </div>
  </template>

  <div class="builder-foot">
    <button type="button" id="addQ" class="btn ghost">+ Add question</button>
  </div>

  <div class="btn-row" style="margin-top:24px">
    <button class="btn subtle" type="submit" name="action" value="draft">Save as draft</button>
    <button class="btn" type="submit" name="action" value="submit">Save &amp; submit for approval</button>
  </div>
</form>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
