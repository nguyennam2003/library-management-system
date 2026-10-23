<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,com.ptit.library.model.*,com.ptit.library.web.Html" %>
<% List<LoanLine> lines = (List<LoanLine>) request.getAttribute("lines"); String ctx = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Sách quá hạn</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="staff-nav.jsp"><jsp:param name="active" value="overdue"/></jsp:include>
<div class="container pb-4">
  <h1 class="h4 mb-3">Danh sách sách quá hạn (<%= lines == null ? 0 : lines.size() %>)</h1>
  <div class="card shadow-sm"><div class="table-responsive">
    <table class="table table-striped align-middle mb-0">
      <thead class="table-light"><tr><th>Phiếu</th><th>Độc giả</th><th>SĐT/Mã thẻ</th><th>Sách</th><th>Mã bản</th><th>Hạn trả</th><th>Số ngày quá hạn</th></tr></thead>
      <tbody>
      <% if (lines == null || lines.isEmpty()) { %>
      <tr><td colspan="7" class="text-center text-muted py-3">Không có sách nào quá hạn.</td></tr>
      <% } else { for (LoanLine l : lines) { %>
      <tr>
        <td><a href="<%= ctx %>/staff/loans?q=<%= Html.esc(l.loanCode()) %>"><%= Html.esc(l.loanCode()) %></a></td>
        <td><%= Html.esc(l.readerName()) %></td>
        <td><%= Html.esc(l.readerCode()) %></td>
        <td><%= Html.esc(l.bookTitle()) %></td>
        <td><%= Html.esc(l.copyCode()) %></td>
        <td><%= l.dueDate() %></td>
        <td><span class="badge bg-danger"><%= l.daysOverdue() %> ngày</span></td>
      </tr>
      <% } } %>
      </tbody>
    </table>
  </div></div>
</div>
</body>
</html>
