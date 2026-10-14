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
<nav class="navbar navbar-dark bg-primary">
  <div class="container">
    <span class="navbar-brand">Quản lý thư viện</span>
    <form method="post" action="${pageContext.request.contextPath}/logout" class="m-0">
      <button class="btn btn-outline-light btn-sm" type="submit">Đăng xuất</button>
    </form>
  </div>
</nav>
<div class="container py-4">
  <h1 class="h4">Xin chào, <%= auth.username() %></h1>
  <p>Vai trò: <%= "ADMIN".equals(auth.role()) ? "Quản trị viên" : "Thủ thư" %>.</p>
  <p class="text-muted">Nghiệp vụ sách, độc giả và mượn/trả sẽ được bổ sung ở bước tiếp theo.</p>
</div>
</body>
</html>
