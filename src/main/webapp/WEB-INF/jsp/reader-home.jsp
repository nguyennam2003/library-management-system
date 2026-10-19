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
<nav class="navbar navbar-expand-lg navbar-dark bg-success">
  <div class="container">
    <a class="navbar-brand" href="${pageContext.request.contextPath}/reader/home">Thư viện</a>
    <div class="navbar-nav me-auto">
      <a class="nav-link active" href="${pageContext.request.contextPath}/reader/home">Trang độc giả</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/reader/books">Tra cứu sách</a>
    </div>
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
  <div class="alert alert-warning">Thẻ đang chờ duyệt. Bạn đã có thể tra cứu sách ngay bây giờ; mượn sách tại quầy chỉ khi thẻ ACTIVE.</div>
  <% } else if ("LOCKED".equals(auth.readerStatus())) { %>
  <div class="alert alert-danger">Thẻ đang bị khóa. Bạn không thể mượn sách mới.</div>
  <% } %>
  <div class="card shadow-sm mt-3" style="max-width: 32rem;">
    <div class="card-body">
      <h2 class="h5">Tra cứu danh mục sách</h2>
      <p class="text-muted small">Tìm kiếm đầu sách theo tên, tác giả, ISBN và xem số lượng bản sách sẵn sàng cho mượn tại thư viện.</p>
      <a href="${pageContext.request.contextPath}/reader/books" class="btn btn-success btn-sm">Tra cứu sách ngay</a>
    </div>
  </div>
</div>
</body>
</html>
