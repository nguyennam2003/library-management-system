<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,com.ptit.library.model.*,com.ptit.library.web.Html" %>
<% List<TopBook> top = (List<TopBook>) request.getAttribute("topBooks"); String ctx = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Thống kê</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="staff-nav.jsp"><jsp:param name="active" value="dashboard"/></jsp:include>
<div class="container pb-4">
  <h1 class="h4 mb-3">Thống kê thư viện</h1>
  <div class="row g-3 mb-4">
    <% String[][] cards = {
        {"Tổng đầu sách", String.valueOf(request.getAttribute("totalBooks"))},
        {"Tổng bản sách", String.valueOf(request.getAttribute("totalCopies"))},
        {"Độc giả ACTIVE", String.valueOf(request.getAttribute("activeReaders"))},
        {"Bản đang được mượn", String.valueOf(request.getAttribute("borrowedCopies"))},
        {"Quá hạn chưa trả", String.valueOf(request.getAttribute("overdueCount"))}}; %>
    <% for (String[] c : cards) { %>
    <div class="col-6 col-md-4 col-lg"><div class="card shadow-sm text-center"><div class="card-body">
      <div class="display-6"><%= c[1] %></div><div class="text-muted small"><%= c[0] %></div>
    </div></div></div>
    <% } %>
  </div>

  <form method="get" action="<%= ctx %>/staff/dashboard" class="row g-2 mb-3">
    <div class="col-auto"><label class="col-form-label">Ngày mượn từ</label></div>
    <div class="col-auto"><input type="date" class="form-control" name="from" value="<%= request.getAttribute("from") %>"></div>
    <div class="col-auto"><label class="col-form-label">đến</label></div>
    <div class="col-auto"><input type="date" class="form-control" name="to" value="<%= request.getAttribute("to") %>"></div>
    <div class="col-auto"><button class="btn btn-outline-primary">Lọc</button>
      <a class="btn btn-secondary" href="<%= ctx %>/staff/dashboard">Bỏ lọc</a></div>
  </form>
  <p>Số lượt mượn (cuốn) trong khoảng đã chọn: <strong><%= request.getAttribute("rangeCount") %></strong></p>

  <div class="card shadow-sm">
    <div class="card-header bg-white fw-bold">Đầu sách được mượn nhiều nhất</div>
    <table class="table table-striped mb-0">
      <thead class="table-light"><tr><th>#</th><th>Tên sách</th><th>Số lượt mượn</th></tr></thead>
      <tbody>
      <% if (top == null || top.isEmpty()) { %>
      <tr><td colspan="3" class="text-center text-muted py-3">Chưa có dữ liệu mượn sách.</td></tr>
      <% } else { int i = 1; for (TopBook t : top) { %>
      <tr><td><%= i++ %></td><td><%= Html.esc(t.title()) %></td><td><%= t.borrowCount() %></td></tr>
      <% } } %>
      </tbody>
    </table>
  </div>
</div>
</body>
</html>
