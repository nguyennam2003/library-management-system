<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,com.ptit.library.model.*,com.ptit.library.web.Html" %>
<% List<LoanLine> lines = (List<LoanLine>) request.getAttribute("lines");
   String q = (String) request.getAttribute("q");
   boolean onlyBorrowed = Boolean.TRUE.equals(request.getAttribute("onlyBorrowed"));
   String ctx = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Phiếu mượn / Trả sách</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="staff-nav.jsp"><jsp:param name="active" value="loans"/></jsp:include>
<div class="container pb-4">
  <h1 class="h4 mb-3">Phiếu mượn và trả sách</h1>
  <form method="get" action="<%= ctx %>/staff/loans" class="row g-2 mb-3">
    <div class="col-md-6"><input class="form-control" name="q" placeholder="Mã phiếu, mã độc giả hoặc họ tên..." value="<%= Html.esc(q) %>"></div>
    <div class="col-md-3 d-flex align-items-center">
      <div class="form-check"><input class="form-check-input" type="checkbox" name="onlyBorrowed" value="1" id="ob" <%= onlyBorrowed ? "checked" : "" %>>
        <label class="form-check-label" for="ob">Chỉ sách chưa trả</label></div>
    </div>
    <div class="col-md-3"><button class="btn btn-outline-primary w-100">Tìm</button></div>
  </form>
  <div class="card shadow-sm"><div class="table-responsive">
    <table class="table table-striped align-middle mb-0">
      <thead class="table-light"><tr><th>Phiếu</th><th>Độc giả</th><th>Sách</th><th>Mã bản</th><th>Ngày mượn</th><th>Hạn trả</th><th>Ngày trả</th><th>Trạng thái</th><th></th></tr></thead>
      <tbody>
      <% if (lines == null || lines.isEmpty()) { %>
      <tr><td colspan="9" class="text-center text-muted py-3">Không có dữ liệu.</td></tr>
      <% } else { for (LoanLine l : lines) { %>
      <tr>
        <td><%= Html.esc(l.loanCode()) %></td>
        <td><%= Html.esc(l.readerCode()) %> - <%= Html.esc(l.readerName()) %></td>
        <td><%= Html.esc(l.bookTitle()) %></td>
        <td><%= Html.esc(l.copyCode()) %></td>
        <td><%= l.borrowedOn() %></td>
        <td><%= l.dueDate() %></td>
        <td><%= l.returnedAt() == null ? "-" : l.returnedAt().toString() %></td>
        <td>
          <% if (l.isBorrowed()) { %>
            <% if (l.daysOverdue() > 0) { %><span class="badge bg-danger">Quá hạn <%= l.daysOverdue() %> ngày</span>
            <% } else { %><span class="badge bg-primary">Đang mượn</span><% } %>
          <% } else { %><span class="badge bg-success">Đã trả</span><% } %>
        </td>
        <td class="text-end">
          <% if (l.isBorrowed()) { %>
          <form method="post" action="<%= ctx %>/staff/loans" onsubmit="return confirm('Xác nhận độc giả đã trả cuốn này?');">
            <input type="hidden" name="detailId" value="<%= l.detailId() %>">
            <input type="hidden" name="q" value="<%= Html.esc(q) %>">
            <% if (onlyBorrowed) { %><input type="hidden" name="onlyBorrowed" value="1"><% } %>
            <button class="btn btn-success btn-sm">Trả sách</button>
          </form>
          <% } %>
        </td>
      </tr>
      <% } } %>
      </tbody>
    </table>
  </div></div>
</div>
</body>
</html>
