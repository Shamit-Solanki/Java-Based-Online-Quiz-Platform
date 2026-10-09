<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="chat-shell">
  <div class="contacts">
    <c:forEach var="ct" items="${contacts}">
      <a class="contact ${ct.userId == withId ? 'active' : ''}" href="${ctx}/messages?with=${ct.userId}">
        <span class="initials"><c:out value="${ct.initials}"/></span>
        <span class="meta"><b><c:out value="${ct.name}"/></b>
          <span><c:out value="${empty ct.lastMessage ? 'No messages yet' : ct.lastMessage}"/></span></span>
        <c:if test="${ct.unread > 0}"><span class="unread">${ct.unread}</span></c:if>
      </a>
    </c:forEach>
    <c:if test="${empty contacts}"><p class="faint" style="padding:16px">No one to message yet.</p></c:if>
  </div>
  <div class="chat-pane">
    <c:choose>
      <c:when test="${empty partner}">
        <div class="chat-empty"><span style="font-size:2rem">💬</span><p>Pick a conversation on the left to get started.</p></div>
      </c:when>
      <c:otherwise>
        <div class="chat-head"><span class="initials"><c:out value="${partner.initials}"/></span>
          <div><b><c:out value="${partner.name}"/></b><div class="faint"><c:out value="${partner.role.label}"/></div></div></div>
        <div class="thread">
          <c:forEach var="m" items="${thread}">
            <div class="bubble ${m.senderId == me.id ? 'mine' : 'theirs'}">
              <c:out value="${m.text}"/>
              <span class="time"><fmt:formatDate value="${m.createdAt}" pattern="MMM d, h:mm a"/></span>
            </div>
          </c:forEach>
          <c:if test="${empty thread}"><p class="faint" style="text-align:center;margin-top:30px">Say hello to start the conversation.</p></c:if>
        </div>
        <form class="chat-form" method="post" action="${ctx}/messages?with=${partner.id}">
          <textarea name="text" maxlength="1000" placeholder="Write a message…" required></textarea>
          <button class="btn" type="submit">Send</button>
        </form>
      </c:otherwise>
    </c:choose>
  </div>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
