<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="false" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Không có quyền</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-5" style="max-width: 32rem;">
  <h1 class="h4">403 — Không có quyền truy cập</h1>
  <p>Tài khoản của bạn không được phép mở trang này.</p>
  <p><a href="${pageContext.request.contextPath}/home">Về trang chính</a></p>
</div>
</body>
</html>
