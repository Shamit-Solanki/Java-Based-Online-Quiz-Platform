<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="grid cols-2" style="align-items:start">
  <div class="card">
    <div class="card-head"><h2>Profile details</h2></div>
    <form method="post" action="${ctx}/profile">
      <input type="hidden" name="action" value="profile">
      <label for="name">Full name</label>
      <input type="text" id="name" name="name" value="<c:out value='${me.name}'/>" maxlength="100" required>
      <label>Email</label>
      <input type="text" value="<c:out value='${me.email}'/>" disabled>
      <p class="hint">Email cannot be changed here. Ask an administrator if it needs updating.</p>
      <label>Role</label>
      <input type="text" value="<c:out value='${me.role.label}'/>" disabled>
      <button class="btn" type="submit" style="margin-top:16px">Save changes</button>
    </form>
  </div>
  <div class="card">
    <div class="card-head"><h2>Change password</h2></div>
    <form method="post" action="${ctx}/profile">
      <input type="hidden" name="action" value="password">
      <label for="current">Current password</label>
      <input type="password" id="current" name="current" required>
      <div class="field-row">
        <div><label for="next">New password</label><input type="password" id="next" name="next" minlength="6" required></div>
        <div><label for="confirm">Confirm new password</label><input type="password" id="confirm" name="confirm" minlength="6" required></div>
      </div>
      <button class="btn subtle" type="submit" style="margin-top:16px">Update password</button>
    </form>
  </div>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
