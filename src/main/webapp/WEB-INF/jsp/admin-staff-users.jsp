<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,com.ptit.library.model.*,com.ptit.library.web.Html" %>
<% List<UserAccount> staff = (List<UserAccount>) request.getAttribute("staff");
   UserAccount edit = (UserAccount) request.getAttribute("editUser");
   AuthSession me = (AuthSession) request.getAttribute("auth");
   String ctx = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Quản lý nhân viên</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="staff-nav.jsp"><jsp:param name="active" value="staffUsers"/></jsp:include>
<div class="container pb-4">
  <h1 class="h4 mb-3">Quản lý tài khoản nhân viên</h1>
  <div class="row g-3">
    <div class="col-lg-4">
      <div class="card shadow-sm">
        <div class="card-header bg-white fw-bold"><%= edit != null ? "Sửa tài khoản " + Html.esc(edit.username()) : "Tạo tài khoản mới" %></div>
        <div class="card-body">
          <form method="post" action="<%= ctx %>/admin/staff-users">
            <input type="hidden" name="action" value="<%= edit != null ? "update" : "create" %>">
            <% if (edit != null) { %><input type="hidden" name="id" value="<%= edit.id() %>"><% } else { %>
            <div class="mb-2"><label class="form-label">Tên đăng nhập</label>
              <input class="form-control" name="username" required maxlength="32"></div>
            <% } %>
            <div class="mb-2"><label class="form-label"><%= edit != null ? "Mật khẩu mới (bỏ trống nếu không đổi)" : "Mật khẩu" %></label>
              <input type="password" class="form-control" name="password" <%= edit == null ? "required" : "" %> minlength="8"></div>
            <div class="mb-3"><label class="form-label">Vai trò</label>
              <select name="role" class="form-select">
                <option value="LIBRARIAN" <%= edit != null && "LIBRARIAN".equals(edit.roleCode()) ? "selected" : "" %>>Thủ thư (LIBRARIAN)</option>
                <option value="ADMIN" <%= edit != null && "ADMIN".equals(edit.roleCode()) ? "selected" : "" %>>Quản trị viên (ADMIN)</option>
              </select></div>
            <button class="btn btn-primary"><%= edit != null ? "Lưu" : "Tạo" %></button>
            <% if (edit != null) { %><a href="<%= ctx %>/admin/staff-users" class="btn btn-secondary">Hủy</a><% } %>
          </form>
        </div>
      </div>
    </div>
    <div class="col-lg-8">
      <div class="card shadow-sm"><div class="table-responsive">
        <table class="table table-striped align-middle mb-0">
          <thead class="table-light"><tr><th>ID</th><th>Tên đăng nhập</th><th>Vai trò</th><th>Trạng thái</th><th class="text-end">Thao tác</th></tr></thead>
          <tbody>
          <% for (UserAccount u : staff) { boolean self = u.id() == me.userId(); %>
          <tr>
            <td><%= u.id() %></td>
            <td><%= Html.esc(u.username()) %><%= self ? " (bạn)" : "" %></td>
            <td><%= u.roleCode() %></td>
            <td><span class="badge <%= u.locked() ? "bg-danger" : "bg-success" %>"><%= u.locked() ? "Đã khóa" : "Hoạt động" %></span></td>
            <td class="text-end">
              <a class="btn btn-outline-primary btn-sm" href="<%= ctx %>/admin/staff-users?editId=<%= u.id() %>">Sửa</a>
              <% if (!self) { %>
              <form method="post" action="<%= ctx %>/admin/staff-users" class="d-inline">
                <input type="hidden" name="id" value="<%= u.id() %>">
                <% if (u.locked()) { %><button name="action" value="unlock" class="btn btn-outline-success btn-sm">Mở khóa</button>
                <% } else { %><button name="action" value="lock" class="btn btn-outline-danger btn-sm">Khóa</button><% } %>
              </form>
              <% } %>
            </td>
          </tr>
          <% } %>
          </tbody>
        </table>
      </div></div>
    </div>
  </div>
</div>
</body>
</html>
