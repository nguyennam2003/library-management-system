<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,com.ptit.library.model.*,com.ptit.library.web.Html" %>
<% String ctx = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Cấu hình hệ thống</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="staff-nav.jsp"><jsp:param name="active" value="settings"/></jsp:include>
<div class="container pb-4">
  <h1 class="h4 mb-3">Cấu hình hệ thống</h1>
  <div class="card shadow-sm" style="max-width: 480px;">
    <div class="card-body">
      <form method="post" action="<%= ctx %>/admin/settings">
        <div class="mb-3"><label class="form-label">Số sách tối đa mỗi độc giả được mượn</label>
          <input type="number" min="1" class="form-control" name="maxBorrow" required value="<%= request.getAttribute("maxBorrow") %>"></div>
        <div class="mb-3"><label class="form-label">Số ngày mượn (hạn trả)</label>
          <input type="number" min="1" class="form-control" name="loanDays" required value="<%= request.getAttribute("loanDays") %>"></div>
        <button class="btn btn-primary">Lưu</button>
        <div class="form-text mt-2">Giá trị mới chỉ áp dụng cho phiếu mượn tạo sau khi lưu.</div>
      </form>
    </div>
  </div>
</div>
</body>
</html>
