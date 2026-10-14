<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Đăng nhập — Quản lý thư viện</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-5" style="max-width: 28rem;">
  <h1 class="h3 mb-3">Đăng nhập</h1>
  <p class="text-muted">Hệ thống Quản lý Thư viện</p>
  <% if (request.getAttribute("info") != null) { %>
  <div class="alert alert-success"><%= request.getAttribute("info") %></div>
  <% } %>
  <% if (request.getAttribute("error") != null) { %>
  <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
  <% } %>
  <form method="post" action="${pageContext.request.contextPath}/login" class="card card-body shadow-sm">
    <div class="mb-3">
      <label class="form-label" for="username">Tên đăng nhập</label>
      <input class="form-control" id="username" name="username" required
             value="<%= request.getAttribute("username") == null ? "" : request.getAttribute("username") %>">
    </div>
    <div class="mb-3">
      <label class="form-label" for="password">Mật khẩu</label>
      <input class="form-control" id="password" name="password" type="password" required>
    </div>
    <button class="btn btn-primary w-100" type="submit">Đăng nhập</button>
  </form>
  <p class="mt-3"><a href="${pageContext.request.contextPath}/register">Đăng ký tài khoản độc giả</a></p>
</div>
</body>
</html>
