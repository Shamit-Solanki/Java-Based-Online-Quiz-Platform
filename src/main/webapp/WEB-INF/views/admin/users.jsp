<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="toolbar">
  <div class="search"><input type="search" placeholder="Search users…" data-filter-table="usersTable"></div>
  <select data-filter-col="role" data-for="usersTable" style="max-width:160px">
    <option value="">All roles</option>
    <c:forEach var="r" items="${roles}"><option value="${r}"><c:out value="${r.label}"/></option></c:forEach>
  </select>
  <div class="spacer"></div>
  <button class="btn" data-open="userDlg" data-title="Add user">+ Add user</button>
</div>

<div class="table-wrap">
<table id="usersTable" class="sortable">
  <thead><tr><th data-sort>Name</th><th data-sort>Email</th><th data-sort>Role</th><th data-sort>Status</th><th data-sort>Joined</th><th></th></tr></thead>
  <tbody>
  <c:forEach var="u" items="${users}">
    <tr data-role="${u.role}">
      <td><div class="name-cell"><span class="initials"><c:out value="${u.initials}"/></span><span class="cell-main"><c:out value="${u.name}"/></span></div></td>
      <td><c:out value="${u.email}"/></td>
      <td><span class="badge brand"><c:out value="${u.role.label}"/></span></td>
      <td><span class="badge ${u.active ? 'success' : 'neutral'}">${u.active ? 'Active' : 'Disabled'}</span></td>
      <td data-v="<fmt:formatDate value='${u.createdAt}' pattern='yyyyMMdd'/>"><fmt:formatDate value="${u.createdAt}" pattern="MMM d, yyyy"/></td>
      <td class="row-actions">
        <button class="btn sm ghost" data-open="userDlg" data-title="Edit user"
          data-id="${u.id}" data-name="<c:out value='${u.name}'/>" data-email="<c:out value='${u.email}'/>"
          data-role="${u.role}" data-active="${u.active}">Edit</button>
        <form method="post" action="${ctx}/admin/users" data-confirm="Delete ${u.name}? This also removes their quizzes/attempts."><input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${u.id}">
          <button class="btn sm danger" type="submit">Delete</button></form>
      </td>
    </tr>
  </c:forEach>
  <tr class="empty-row" hidden><td colspan="6">No users match your search.</td></tr>
  </tbody>
</table>
</div>

<dialog id="userDlg">
  <form method="post" action="${ctx}/admin/users" class="modal-body">
    <input type="hidden" name="id" data-fill="id">
    <input type="hidden" name="action" value="create" class="action-field">
    <h2 data-title>Add user</h2>
    <label for="uName">Full name</label><input type="text" id="uName" name="name" data-fill="name" maxlength="100" required>
    <label for="uEmail">Email</label><input type="email" id="uEmail" name="email" data-fill="email" maxlength="150" required>
    <label for="uRole">Role</label>
    <select id="uRole" name="role" data-fill="role" required>
      <c:forEach var="r" items="${roles}"><option value="${r}"><c:out value="${r.label}"/></option></c:forEach>
    </select>
    <label for="uPass">Password <span class="hint" id="pwHint">(min 6 characters)</span></label>
    <input type="password" id="uPass" name="password" minlength="6" maxlength="100">
    <label class="checkline" for="uActive" style="margin-top:16px">
      <span class="switch"><input type="checkbox" id="uActive" name="active" data-fill="active" checked><span class="track"></span></span>
      Account active
    </label>
    <div class="modal-actions">
      <button type="button" class="btn ghost" data-close>Cancel</button>
      <button type="submit" class="btn">Save</button>
    </div>
  </form>
</dialog>
<script>
(function(){
  var dlg=document.getElementById('userDlg'), form=dlg.querySelector('form'), actionField=form.querySelector('.action-field'), pwHint=document.getElementById('pwHint'), pw=document.getElementById('uPass');
  document.querySelectorAll('[data-open=userDlg]').forEach(function(b){
    b.addEventListener('click',function(){
      var editing=!!b.dataset.id;
      actionField.value=editing?'update':'create';
      pw.required=!editing; pwHint.textContent=editing?'(leave blank to keep current password)':'(min 6 characters)';
      if(!editing){form.reset(); document.getElementById('uActive').checked=true;}
    });
  });
})();
</script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
