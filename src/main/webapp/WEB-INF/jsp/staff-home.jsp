<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.ptit.library.model.AuthSession" %>
<% AuthSession auth = (AuthSession) request.getAttribute("auth"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Trang nhân viên</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
  <div class="container">
    <a class="navbar-brand" href="${pageContext.request.contextPath}/staff/home">Quản lý thư viện</a>
    <div class="navbar-nav me-auto">
      <a class="nav-link active" href="${pageContext.request.contextPath}/staff/home">Trang chủ</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/staff/books">Đầu sách & Bản sách</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/staff/catalog">Thể loại & Tác giả</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/account/password">Đổi mật khẩu</a>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/logout" class="m-0">
      <button class="btn btn-outline-light btn-sm" type="submit">Đăng xuất</button>
    </form>
  </div>
</nav>
<div class="container py-4">
  <h1 class="h4">Xin chào, <%= auth.username() %></h1>
  <p>Vai trò: <%= "ADMIN".equals(auth.role()) ? "Quản trị viên" : "Thủ thư" %>.</p>
  <div class="row g-3 mt-2">
    <div class="col-md-6">
      <div class="card shadow-sm h-100">
        <div class="card-body">
          <h2 class="h5">Quản lý Đầu sách & Bản sách</h2>
          <p class="text-muted small">Thêm, sửa, ngừng lưu hành đầu sách; quản lý mã bản sách vật lý, tìm kiếm và lọc sách.</p>
          <a href="${pageContext.request.contextPath}/staff/books" class="btn btn-primary btn-sm">Mở Quản lý Sách</a>
        </div>
      </div>
    </div>
    <div class="col-md-6">
      <div class="card shadow-sm h-100">
        <div class="card-body">
          <h2 class="h5">Quản lý Thể loại & Tác giả</h2>
          <p class="text-muted small">Thêm, cập nhật và xóa danh mục thể loại sách, danh sách tác giả.</p>
          <a href="${pageContext.request.contextPath}/staff/catalog" class="btn btn-outline-primary btn-sm">Mở Thể loại & Tác giả</a>
        </div>
      </div>
    </div>
  </div>
</div>
</body>
</html>
