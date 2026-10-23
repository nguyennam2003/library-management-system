<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.ptit.library.model.AuthSession" %>
<%
  AuthSession navAuth = (AuthSession) request.getAttribute("auth");
  String active = request.getParameter("active");
  if (active == null) active = "";
  boolean isAdmin = navAuth != null && "ADMIN".equals(navAuth.role());
  String ctx = request.getContextPath();
%>
<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
  <div class="container">
    <a class="navbar-brand" href="<%= ctx %>/staff/home">Quản lý thư viện</a>
    <div class="navbar-nav me-auto flex-wrap">
      <a class="nav-link <%= active.equals("dashboard") ? "active" : "" %>" href="<%= ctx %>/staff/dashboard">Thống kê</a>
      <a class="nav-link <%= active.equals("books") ? "active" : "" %>" href="<%= ctx %>/staff/books">Sách</a>
      <a class="nav-link <%= active.equals("catalog") ? "active" : "" %>" href="<%= ctx %>/staff/catalog">Thể loại & Tác giả</a>
      <a class="nav-link <%= active.equals("pending") ? "active" : "" %>" href="<%= ctx %>/staff/readers/pending">Duyệt thẻ</a>
      <a class="nav-link <%= active.equals("readers") ? "active" : "" %>" href="<%= ctx %>/staff/readers">Độc giả</a>
      <a class="nav-link <%= active.equals("loanNew") ? "active" : "" %>" href="<%= ctx %>/staff/loans/new">Lập phiếu mượn</a>
      <a class="nav-link <%= active.equals("loans") ? "active" : "" %>" href="<%= ctx %>/staff/loans">Phiếu mượn / Trả</a>
      <a class="nav-link <%= active.equals("overdue") ? "active" : "" %>" href="<%= ctx %>/staff/overdue">Quá hạn</a>
      <% if (isAdmin) { %>
      <a class="nav-link <%= active.equals("staffUsers") ? "active" : "" %>" href="<%= ctx %>/admin/staff-users">Nhân viên</a>
      <a class="nav-link <%= active.equals("settings") ? "active" : "" %>" href="<%= ctx %>/admin/settings">Cấu hình</a>
      <% } %>
      <a class="nav-link" href="<%= ctx %>/account/password">Đổi mật khẩu</a>
    </div>
    <div class="d-flex align-items-center gap-2 text-white">
      <span class="small"><%= navAuth != null ? com.ptit.library.web.Html.esc(navAuth.username()) : "" %></span>
      <form method="post" action="<%= ctx %>/logout" class="m-0">
        <button class="btn btn-outline-light btn-sm" type="submit">Đăng xuất</button>
      </form>
    </div>
  </div>
</nav>
<div class="container pt-3">
  <% if (request.getAttribute("info") != null) { %>
  <div class="alert alert-success"><%= com.ptit.library.web.Html.esc(request.getAttribute("info")) %></div>
  <% } %>
  <% if (request.getAttribute("error") != null) { %>
  <div class="alert alert-danger"><%= com.ptit.library.web.Html.esc(request.getAttribute("error")) %></div>
  <% } %>
</div>
