<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,com.ptit.library.auth.RegistrationInput" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Đăng ký — Quản lý thư viện</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<%
  RegistrationInput input = (RegistrationInput) request.getAttribute("input");
  String username = input == null || input.username() == null ? "" : input.username();
  String fullName = input == null || input.fullName() == null ? "" : input.fullName();
  String email = input == null || input.email() == null ? "" : input.email();
  String phone = input == null || input.phone() == null ? "" : input.phone();
  List<?> errors = (List<?>) request.getAttribute("errors");
%>
<div class="container py-5" style="max-width: 32rem;">
  <h1 class="h3 mb-3">Đăng ký độc giả</h1>
  <p class="text-muted">Tài khoản sẽ chờ thủ thư duyệt thẻ trước khi được mượn sách.</p>
  <% if (errors != null) { %>
  <div class="alert alert-danger">
    <ul class="mb-0">
      <% for (Object error : errors) { %><li><%= error %></li><% } %>
    </ul>
  </div>
  <% } %>
  <form method="post" action="${pageContext.request.contextPath}/register" class="card card-body shadow-sm">
    <div class="mb-3">
      <label class="form-label" for="username">Tên đăng nhập</label>
      <input class="form-control" id="username" name="username" required value="<%= username %>">
    </div>
    <div class="mb-3">
      <label class="form-label" for="fullName">Họ tên</label>
      <input class="form-control" id="fullName" name="fullName" required value="<%= fullName %>">
    </div>
    <div class="mb-3">
      <label class="form-label" for="email">Email</label>
      <input class="form-control" id="email" name="email" type="email" required value="<%= email %>">
    </div>
    <div class="mb-3">
      <label class="form-label" for="phone">Số điện thoại</label>
      <input class="form-control" id="phone" name="phone" required value="<%= phone %>">
    </div>
    <div class="mb-3">
      <label class="form-label" for="password">Mật khẩu</label>
      <input class="form-control" id="password" name="password" type="password" required>
    </div>
    <div class="mb-3">
      <label class="form-label" for="confirmPassword">Xác nhận mật khẩu</label>
      <input class="form-control" id="confirmPassword" name="confirmPassword" type="password" required>
    </div>
    <button class="btn btn-primary w-100" type="submit">Đăng ký</button>
  </form>
  <p class="mt-3"><a href="${pageContext.request.contextPath}/login">Đã có tài khoản? Đăng nhập</a></p>
</div>
</body>
</html>
