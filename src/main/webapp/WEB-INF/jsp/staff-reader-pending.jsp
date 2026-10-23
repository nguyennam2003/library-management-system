<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,com.ptit.library.model.*,com.ptit.library.web.Html" %>
<% List<ReaderProfile> queue = (List<ReaderProfile>) request.getAttribute("queue"); String ctx = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Duyệt thẻ độc giả</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="staff-nav.jsp"><jsp:param name="active" value="pending"/></jsp:include>
<div class="container pb-4">
  <h1 class="h4 mb-3">Hàng đợi duyệt thẻ độc giả</h1>
  <div class="card shadow-sm"><div class="table-responsive">
    <table class="table table-striped align-middle mb-0">
      <thead class="table-light"><tr><th>Mã</th><th>Họ tên</th><th>Email</th><th>SĐT</th><th>Trạng thái</th><th class="text-end">Thao tác</th></tr></thead>
      <tbody>
      <% if (queue == null || queue.isEmpty()) { %>
      <tr><td colspan="6" class="text-center text-muted py-3">Không có hồ sơ nào chờ duyệt.</td></tr>
      <% } else { for (ReaderProfile r : queue) { boolean pending = "PENDING".equals(r.status()); %>
      <tr>
        <td><%= Html.esc(r.code()) %></td>
        <td><%= Html.esc(r.fullName()) %></td>
        <td><%= Html.esc(r.email()) %></td>
        <td><%= Html.esc(r.phone()) %></td>
        <td><span class="badge <%= pending ? "bg-warning text-dark" : "bg-danger" %>"><%= r.status() %></span></td>
        <td class="text-end">
          <form method="post" action="<%= ctx %>/staff/readers/pending" class="d-inline">
            <input type="hidden" name="id" value="<%= r.id() %>">
            <button name="action" value="approve" class="btn btn-success btn-sm">Duyệt</button>
            <% if (pending) { %>
            <button name="action" value="reject" class="btn btn-outline-danger btn-sm">Từ chối</button>
            <% } else { %>
            <button name="action" value="reopen" class="btn btn-outline-secondary btn-sm">Chuyển về chờ duyệt</button>
            <% } %>
          </form>
        </td>
      </tr>
      <% } } %>
      </tbody>
    </table>
  </div></div>
</div>
</body>
</html>
