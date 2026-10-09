<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="card" style="padding:0;overflow:hidden">
  <div class="card-head" style="padding:18px 20px 0">
    <h2>${role == 'ADMIN' ? 'System alerts' : 'Notifications'}</h2>
    <div class="spacer"></div>
    <c:if test="${not empty notifications}">
      <form method="post" action="${ctx}/notifications"><input type="hidden" name="action" value="clear">
        <button class="btn sm ghost" type="submit">Clear all</button></form>
    </c:if>
  </div>
  <div style="padding-top:12px">
  <c:forEach var="n" items="${notifications}">
    <div class="notif-item ${n.read ? '' : 'unread'}">
      <span class="dot-ic badge ${n.level == 'SUCCESS' ? 'success' : n.level == 'WARNING' ? 'warning' : n.level == 'CRITICAL' ? 'danger' : 'info'}" style="padding:0;width:34px;height:34px">
        ${n.level == 'SUCCESS' ? '✓' : n.level == 'WARNING' ? '!' : n.level == 'CRITICAL' ? '⛔' : 'i'}</span>
      <div class="body">
        <p><c:out value="${n.text}"/></p>
        <span><fmt:formatDate value="${n.createdAt}" pattern="MMM d, yyyy · h:mm a"/></span>
      </div>
      <div class="row-actions">
        <c:if test="${not empty n.link}"><a class="btn sm ghost" href="${ctx}${n.link}">Open</a></c:if>
        <form method="post" action="${ctx}/notifications"><input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${n.id}">
          <button class="icon-btn sm" type="submit" title="Dismiss" style="width:30px;height:30px">×</button></form>
      </div>
    </div>
  </c:forEach>
  <c:if test="${empty notifications}">
    <div class="empty-state"><div class="big">🔔</div><p>You are all caught up. No notifications yet.</p></div>
  </c:if>
  </div>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
