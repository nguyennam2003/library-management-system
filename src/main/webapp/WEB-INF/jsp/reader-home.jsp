<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.ptit.library.model.AuthSession" %>
<% AuthSession auth = (AuthSession) request.getAttribute("auth"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Trang độc giả</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-dark bg-success">
  <div class="container">
    <span class="navbar-brand">Thư viện</span>
    <form method="post" action="${pageContext.request.contextPath}/logout" class="m-0">
      <button class="btn btn-outline-light btn-sm" type="submit">Đăng xuất</button>
    </form>
  </div>
</nav>
<div class="container py-4">
  <h1 class="h4">Xin chào, <%= auth.username() %></h1>
  <p>Mã thẻ: <strong><%= auth.readerCode() %></strong></p>
  <p>Trạng thái thẻ: <strong><%= auth.readerStatus() %></strong></p>
  <% if ("PENDING".equals(auth.readerStatus())) { %>
  <div class="alert alert-warning">Thẻ đang chờ duyệt. Bạn có thể tra cứu sách sau khi chức năng tra cứu được mở; mượn sách chỉ khi thẻ ACTIVE.</div>
  <% } else if ("LOCKED".equals(auth.readerStatus())) { %>
  <div class="alert alert-danger">Thẻ đang bị khóa. Bạn không thể mượn sách mới.</div>
  <% } %>
</div>
</body>
</html>
