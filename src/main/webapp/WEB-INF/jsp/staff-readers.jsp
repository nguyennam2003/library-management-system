<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,com.ptit.library.model.*,com.ptit.library.web.Html" %>
<%
  List<ReaderProfile> readers = (List<ReaderProfile>) request.getAttribute("readers");
  ReaderProfile edit = (ReaderProfile) request.getAttribute("editReader");
  String q = (String) request.getAttribute("q");
  String status = (String) request.getAttribute("status");
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Quản lý độc giả</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="staff-nav.jsp"><jsp:param name="active" value="readers"/></jsp:include>
<div class="container pb-4">
  <h1 class="h4 mb-3">Quản lý độc giả</h1>
  <div class="row g-3">
    <div class="col-lg-4">
      <div class="card shadow-sm">
        <div class="card-header bg-white fw-bold"><%= edit != null ? "Sửa độc giả " + Html.esc(edit.code()) : "Tạo độc giả tại quầy" %></div>
        <div class="card-body">
          <form method="post" action="<%= ctx %>/staff/readers">
            <input type="hidden" name="action" value="<%= edit != null ? "update" : "create" %>">
            <% if (edit != null) { %><input type="hidden" name="id" value="<%= edit.id() %>"><% } %>
            <% if (edit == null) { %>
            <div class="mb-2"><label class="form-label">Tên đăng nhập</label>
              <input class="form-control" name="username" required maxlength="32"></div>
            <div class="mb-2"><label class="form-label">Mật khẩu ban đầu</label>
              <input type="password" class="form-control" name="password" required minlength="8"></div>
            <% } %>
            <div class="mb-2"><label class="form-label">Họ tên</label>
              <input class="form-control" name="fullName" required maxlength="128" value="<%= edit != null ? Html.esc(edit.fullName()) : "" %>"></div>
            <div class="mb-2"><label class="form-label">Email</label>
              <input type="email" class="form-control" name="email" required value="<%= edit != null ? Html.esc(edit.email()) : "" %>"></div>
            <div class="mb-3"><label class="form-label">Số điện thoại</label>
              <input class="form-control" name="phone" required value="<%= edit != null ? Html.esc(edit.phone()) : "" %>"></div>
            <button class="btn btn-primary"><%= edit != null ? "Lưu" : "Tạo độc giả" %></button>
            <% if (edit != null) { %><a href="<%= ctx %>/staff/readers" class="btn btn-secondary">Hủy</a><% } %>
          </form>
        </div>
      </div>
    </div>
    <div class="col-lg-8">
      <form method="get" action="<%= ctx %>/staff/readers" class="row g-2 mb-3">
        <div class="col-sm-6"><input class="form-control" name="q" placeholder="Tìm theo mã hoặc họ tên..." value="<%= Html.esc(q) %>"></div>
        <div class="col-sm-3">
          <select name="status" class="form-select">
            <option value="">Mọi trạng thái</option>
            <% for (String s : new String[]{"PENDING", "ACTIVE", "LOCKED", "REJECTED"}) { %>
            <option value="<%= s %>" <%= s.equals(status) ? "selected" : "" %>><%= s %></option>
            <% } %>
          </select>
        </div>
        <div class="col-sm-3"><button class="btn btn-outline-primary w-100">Tìm</button></div>
      </form>
      <div class="card shadow-sm"><div class="table-responsive">
        <table class="table table-striped align-middle mb-0">
          <thead class="table-light"><tr><th>Mã</th><th>Họ tên</th><th>Email</th><th>SĐT</th><th>Trạng thái</th><th class="text-end">Thao tác</th></tr></thead>
          <tbody>
          <% if (readers == null || readers.isEmpty()) { %>
          <tr><td colspan="6" class="text-center text-muted py-3">Không tìm thấy độc giả.</td></tr>
          <% } else { for (ReaderProfile r : readers) { %>
          <tr>
            <td><%= Html.esc(r.code()) %></td>
            <td><%= Html.esc(r.fullName()) %></td>
            <td><%= Html.esc(r.email()) %></td>
            <td><%= Html.esc(r.phone()) %></td>
            <td><span class="badge bg-secondary"><%= r.status() %></span></td>
            <td class="text-end">
              <a class="btn btn-outline-primary btn-sm" href="<%= ctx %>/staff/readers?editId=<%= r.id() %>">Sửa</a>
              <% if ("ACTIVE".equals(r.status()) || "LOCKED".equals(r.status())) { %>
              <form method="post" action="<%= ctx %>/staff/readers" class="d-inline">
                <input type="hidden" name="id" value="<%= r.id() %>">
                <% if ("ACTIVE".equals(r.status())) { %>
                <button name="action" value="lock" class="btn btn-outline-danger btn-sm">Khóa</button>
                <% } else { %>
                <button name="action" value="unlock" class="btn btn-outline-success btn-sm">Mở khóa</button>
                <% } %>
              </form>
              <% } %>
            </td>
          </tr>
          <% } } %>
          </tbody>
        </table>
      </div></div>
    </div>
  </div>
</div>
</body>
</html>
