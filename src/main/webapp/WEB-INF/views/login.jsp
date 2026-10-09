<%@ include file="/WEB-INF/jspf/public-header.jspf" %>
<div class="auth-wrap">
  <div class="auth-side">
    <a class="brand" href="${ctx}/"><span class="brand-mark">QM</span><span>QuizMania</span></a>
    <div>
      <h2>Interactive timed quizzes with instant, detailed feedback.</h2>
      <p>Sign in to pick up where you left off.</p>
      <ul>
        <li>⏱ Server-enforced timers, no cheating the clock</li>
        <li>📊 Automatic scoring and performance reports</li>
        <li>🏆 Leaderboards that update the moment you submit</li>
      </ul>
    </div>
    <p class="faint" style="position:relative;opacity:.85">Classroom-ready &middot; built with Servlets &amp; JSP</p>
  </div>
  <div class="auth-form">
    <h1>Welcome back</h1>
    <p class="lead">Sign in to your account to continue.</p>
    <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>
    <form method="post" action="${ctx}/login" autocomplete="on">
      <label for="email">Email</label>
      <input type="email" id="email" name="email" value="<c:out value='${email}'/>" required autofocus>
      <label for="password">Password</label>
      <div class="pw-wrap">
        <input type="password" id="password" name="password" required>
        <button type="button" class="pw-toggle" data-target="password">SHOW</button>
      </div>
      <button class="btn block lg" type="submit" style="margin-top:18px">Sign in</button>
    </form>
    <div class="divider"></div>
    <p class="faint" style="margin-bottom:8px">Quick demo logins</p>
    <div class="demo-row">
      <button type="button" class="quick-login" data-email="admin@quiz.com" data-pw="admin123">Admin</button>
      <button type="button" class="quick-login" data-email="creator@quiz.com" data-pw="creator123">Creator</button>
      <button type="button" class="quick-login" data-email="participant@quiz.com" data-pw="participant123">Participant</button>
    </div>
    <p class="auth-foot">New here? <a class="icon-link" href="${ctx}/register">Create an account</a></p>
  </div>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
