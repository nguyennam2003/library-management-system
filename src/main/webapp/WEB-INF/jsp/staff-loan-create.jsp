<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,com.ptit.library.model.*,com.ptit.library.web.Html" %>
<% String ctx = request.getContextPath();
   String readerCode = (String) request.getAttribute("readerCode");
   String copyCodes = (String) request.getAttribute("copyCodes"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Lập phiếu mượn</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="staff-nav.jsp"><jsp:param name="active" value="loanNew"/></jsp:include>
<div class="container pb-4">
  <h1 class="h4 mb-3">Lập phiếu mượn</h1>
  <div class="card shadow-sm" style="max-width: 640px;">
    <div class="card-body">
      <form method="post" action="<%= ctx %>/staff/loans/new">
        <div class="mb-3">
          <label class="form-label">Mã độc giả (VD: DG000001)</label>
          <input class="form-control" name="readerCode" required value="<%= Html.esc(readerCode) %>">
        </div>
        <div class="mb-3">
          <label class="form-label">Mã các bản sách (cách nhau bằng dấu phẩy hoặc xuống dòng)</label>
          <textarea class="form-control" name="copyCodes" rows="4" required placeholder="CC-001, CC-002"><%= Html.esc(copyCodes) %></textarea>
          <div class="form-text">Hạn trả = ngày mượn + số ngày cấu hình. Số sách tối đa theo cấu hình hệ thống.</div>
        </div>
        <button class="btn btn-primary">Lập phiếu</button>
      </form>
    </div>
  </div>
</div>
</body>
</html>
