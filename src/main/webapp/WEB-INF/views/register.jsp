<%@ include file="/WEB-INF/jspf/public-header.jspf" %>
<div class="auth-wrap">
  <div class="auth-side">
    <a class="brand" href="${ctx}/"><span class="brand-mark">&lt;/&gt;</span><span>JavaQuiz Arena</span></a>
    <div>
      <h2>Join as a participant and start practising today.</h2>
      <p>Free, no credit card, cancel any time.</p>
      <ul>
        <li>📝 Unlimited approved quizzes to practise on</li>
        <li>⏰ Set reminders so you never miss a quiz</li>
        <li>💬 Message quiz creators directly for feedback</li>
      </ul>
    </div>
    <p class="faint" style="position:relative;opacity:.85">Want to create quizzes instead? Ask an administrator for a Quiz Creator account.</p>
  </div>
  <div class="auth-form">
    <h1>Create your account</h1>
    <p class="lead">Participant accounts can sign up instantly.</p>
    <c:choose>
    <c:when test="${not registrationOpen}">
      <div class="alert info">Public sign-up is currently closed by the administrator. Please check back later.</div>
    </c:when>
    <c:otherwise>
      <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>
      <form method="post" action="${ctx}/register">
        <label for="name">Full name</label>
        <input type="text" id="name" name="name" value="<c:out value='${name}'/>" maxlength="100" required autofocus>
        <label for="email">Email</label>
        <input type="email" id="email" name="email" value="<c:out value='${email}'/>" maxlength="150" required>
        <div class="field-row">
          <div><label for="password">Password</label><input type="password" id="password" name="password" minlength="6" maxlength="100" required></div>
          <div><label for="confirm">Confirm password</label><input type="password" id="confirm" name="confirm" minlength="6" maxlength="100" required></div>
        </div>
        <button class="btn block lg" type="submit" style="margin-top:18px">Create account</button>
      </form>
    </c:otherwise>
    </c:choose>
    <p class="auth-foot">Already have an account? <a class="icon-link" href="${ctx}/login">Sign in</a></p>
  </div>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
