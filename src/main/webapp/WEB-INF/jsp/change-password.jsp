<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="com.ptit.library.model.AuthSession" %>
<%
  AuthSession auth = (AuthSession) request.getAttribute("auth");
  boolean isReader = auth != null && auth.isReader();
  String homeUrl = isReader
          ? request.getContextPath() + "/reader/home"
          : request.getContextPath() + "/staff/home";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Đổi mật khẩu — Quản lý thư viện</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg navbar-dark <%= isReader ? "bg-success" : "bg-primary" %>">
  <div class="container">
    <a class="navbar-brand" href="<%= homeUrl %>"><%= isReader ? "Thư viện" : "Quản lý thư viện" %></a>
    <div class="navbar-nav me-auto">
      <% if (isReader) { %>
      <a class="nav-link" href="${pageContext.request.contextPath}/reader/home">Trang độc giả</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/reader/books">Tra cứu sách</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/reader/my-loans">Sách đang mượn & Lịch sử</a>
      <% } else { %>
      <a class="nav-link" href="${pageContext.request.contextPath}/staff/home">Trang chủ</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/staff/books">Đầu sách & Bản sách</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/staff/catalog">Thể loại & Tác giả</a>
      <% } %>
      <a class="nav-link active" href="${pageContext.request.contextPath}/account/password">Đổi mật khẩu</a>
    </div>
    <div class="d-flex align-items-center gap-2 text-white">
      <span class="small">Xin chào, <strong><%= auth != null ? auth.username() : "" %></strong></span>
      <form method="post" action="${pageContext.request.contextPath}/logout" class="m-0">
        <button class="btn btn-outline-light btn-sm" type="submit">Đăng xuất</button>
      </form>
    </div>
  </div>
</nav>

<div class="container py-5" style="max-width: 30rem;">
  <h1 class="h4 mb-3">Đổi mật khẩu cá nhân</h1>
  <% if (request.getAttribute("info") != null) { %>
  <div class="alert alert-success"><%= request.getAttribute("info") %></div>
  <% } %>
  <% if (request.getAttribute("error") != null) { %>
  <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
  <% } %>

  <form method="post" action="${pageContext.request.contextPath}/account/password" class="card card-body shadow-sm">
    <div class="mb-3">
      <label class="form-label" for="currentPassword">Mật khẩu hiện tại</label>
      <input class="form-control" id="currentPassword" name="currentPassword" type="password" required>
    </div>
    <div class="mb-3">
      <label class="form-label" for="newPassword">Mật khẩu mới (tối thiểu 8 ký tự)</label>
      <input class="form-control" id="newPassword" name="newPassword" type="password" required minlength="8">
    </div>
    <div class="mb-3">
      <label class="form-label" for="confirmPassword">Xác nhận mật khẩu mới</label>
      <input class="form-control" id="confirmPassword" name="confirmPassword" type="password" required minlength="8">
    </div>
    <button class="btn <%= isReader ? "btn-success" : "btn-primary" %> w-100" type="submit">Cập nhật mật khẩu</button>
  </form>
  <p class="mt-3">
    <a href="<%= homeUrl %>">&larr; Quay lại trang chủ</a>
  </p>
</div>
</body>
</html>
