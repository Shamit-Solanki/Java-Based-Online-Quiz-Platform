<%@ include file="/WEB-INF/jspf/public-header.jspf" %>
<div class="landing">
  <div class="landing-hero">
    <span class="kicker">🎯 The Ultimate Quiz Experience</span>
    <h1>Challenge yourself with timed quizzes on any topic.</h1>
    <p>Create custom quizzes, test your knowledge under the clock, and see exactly where you stand &mdash; with instant scoring, detailed reports, and a live leaderboard.</p>
    <div class="btn-row" style="justify-content:center">
      <a class="btn lg" href="${ctx}/login">Sign in</a>
      <a class="btn lg ghost" href="${ctx}/register">Create a free account</a>
    </div>
  </div>
  <div class="role-grid">
    <div class="role-card"><div class="ic">⚙</div><h3>Admins</h3><p>Manage every account, approve quiz content before it goes live, and keep an eye on platform-wide performance.</p></div>
    <div class="role-card"><div class="ic">✎</div><h3>Quiz creators</h3><p>Build timed multiple-choice quizzes, submit them for approval, then review results and coach participants directly.</p></div>
    <div class="role-card"><div class="ic">▶</div><h3>Participants</h3><p>Take approved quizzes against the clock, track your progress over time, and climb the leaderboard.</p></div>
  </div>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
