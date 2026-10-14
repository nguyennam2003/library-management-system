<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.ptit.library.web.SessionAuth" %>
<%
  if (SessionAuth.get(session) != null) {
    response.sendRedirect(request.getContextPath() + "/home");
  } else {
    response.sendRedirect(request.getContextPath() + "/login");
  }
%>
