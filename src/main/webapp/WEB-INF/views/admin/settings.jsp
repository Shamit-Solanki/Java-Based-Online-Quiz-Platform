<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="card form-card" style="max-width:640px">
  <div class="card-head"><h2>Platform settings</h2></div>
  <form method="post" action="${ctx}/admin/settings">
    <c:forEach var="def" items="${definitions}">
      <c:set var="v" value="${values[def.key]}"/>
      <c:choose>
        <c:when test="${def.type == 'boolean'}">
          <label class="checkline" for="s_${def.key}" style="margin-top:20px">
            <span class="switch"><input type="checkbox" id="s_${def.key}" name="${def.key}" value="true" ${v == 'true' ? 'checked' : ''}><span class="track"></span></span>
            <span><b><c:out value="${def.label}"/></b><br><span class="hint"><c:out value="${def.help}"/></span></span>
          </label>
        </c:when>
        <c:when test="${def.type == 'number'}">
          <label for="s_${def.key}"><c:out value="${def.label}"/></label>
          <input type="number" id="s_${def.key}" name="${def.key}" value="<c:out value='${v}'/>" required>
          <p class="hint" style="margin:-6px 0 10px"><c:out value="${def.help}"/></p>
        </c:when>
        <c:otherwise>
          <label for="s_${def.key}"><c:out value="${def.label}"/></label>
          <c:choose>
            <c:when test="${def.key == 'announcement'}"><textarea id="s_${def.key}" name="${def.key}" maxlength="300"><c:out value="${v}"/></textarea></c:when>
            <c:otherwise><input type="text" id="s_${def.key}" name="${def.key}" value="<c:out value='${v}'/>" maxlength="60" required></c:otherwise>
          </c:choose>
          <p class="hint" style="margin:-6px 0 10px"><c:out value="${def.help}"/></p>
        </c:otherwise>
      </c:choose>
    </c:forEach>
    <button class="btn" type="submit" style="margin-top:16px">Save settings</button>
  </form>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
