<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.List,com.ptit.library.model.AuthSession,com.ptit.library.model.Category,com.ptit.library.model.Author" %>
<%
  AuthSession auth = (AuthSession) request.getAttribute("auth");
  List<Category> categories = (List<Category>) request.getAttribute("categories");
  List<Author> authors = (List<Author>) request.getAttribute("authors");
  Category editCategory = (Category) request.getAttribute("editCategory");
  Author editAuthor = (Author) request.getAttribute("editAuthor");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Quản lý Thể loại & Tác giả — Quản lý thư viện</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
  <div class="container">
    <a class="navbar-brand" href="${pageContext.request.contextPath}/staff/home">Quản lý thư viện</a>
    <div class="navbar-nav me-auto">
      <a class="nav-link" href="${pageContext.request.contextPath}/staff/home">Trang chủ</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/staff/books">Đầu sách & Bản sách</a>
      <a class="nav-link active" href="${pageContext.request.contextPath}/staff/catalog">Thể loại & Tác giả</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/account/password">Đổi mật khẩu</a>
    </div>
    <div class="d-flex align-items-center gap-2 text-white">
      <span class="small">Xin chào, <strong><%= auth != null ? auth.username() : "" %></strong></span>
      <form method="post" action="${pageContext.request.contextPath}/logout" class="m-0">
        <button class="btn btn-outline-light btn-sm" type="submit">Đăng xuất</button>
      </form>
    </div>
  </div>
</nav>

<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h4 mb-0">Quản lý Thể loại và Tác giả</h1>
    <a href="${pageContext.request.contextPath}/staff/books" class="btn btn-outline-primary btn-sm">Sang Quản lý Đầu sách</a>
  </div>

  <% if (request.getAttribute("info") != null) { %>
  <div class="alert alert-success"><%= request.getAttribute("info") %></div>
  <% } %>
  <% if (request.getAttribute("error") != null) { %>
  <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
  <% } %>

  <div class="row g-4">
    <!-- Cột Thể loại -->
    <div class="col-md-6">
      <div class="card shadow-sm mb-3">
        <div class="card-header bg-white fw-bold">
          <%= editCategory != null ? "Cập nhật thể loại #" + editCategory.id() : "Thêm thể loại mới" %>
        </div>
        <div class="card-body">
          <form method="post" action="${pageContext.request.contextPath}/staff/catalog" class="row g-2">
            <input type="hidden" name="action" value="<%= editCategory != null ? "updateCategory" : "createCategory" %>">
            <% if (editCategory != null) { %>
            <input type="hidden" name="id" value="<%= editCategory.id() %>">
            <% } %>
            <div class="col-sm-8">
              <input type="text" class="form-control" name="name" placeholder="Nhập tên thể loại..."
                     required maxlength="128"
                     value="<%= editCategory != null ? editCategory.name() : "" %>">
            </div>
            <div class="col-sm-4 d-flex gap-1">
              <button type="submit" class="btn btn-primary w-100">
                <%= editCategory != null ? "Lưu" : "Thêm" %>
              </button>
              <% if (editCategory != null) { %>
              <a href="${pageContext.request.contextPath}/staff/catalog" class="btn btn-secondary">Hủy</a>
              <% } %>
            </div>
          </form>
        </div>
      </div>

      <div class="card shadow-sm">
        <div class="card-header bg-white fw-bold">Danh sách thể loại (<%= categories != null ? categories.size() : 0 %>)</div>
        <div class="table-responsive">
          <table class="table table-striped table-hover align-middle mb-0">
            <thead class="table-light">
            <tr>
              <th>ID</th>
              <th>Tên thể loại</th>
              <th class="text-center">Số đầu sách</th>
              <th class="text-end">Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <% if (categories == null || categories.isEmpty()) { %>
            <tr>
              <td colspan="4" class="text-center text-muted py-3">Chưa có thể loại nào.</td>
            </tr>
            <% } else {
                 for (Category c : categories) { %>
            <tr>
              <td><%= c.id() %></td>
              <td><%= c.name() %></td>
              <td class="text-center"><span class="badge bg-secondary"><%= c.bookCount() %></span></td>
              <td class="text-end">
                <a href="${pageContext.request.contextPath}/staff/catalog?editCategoryId=<%= c.id() %>"
                   class="btn btn-outline-primary btn-sm">Sửa</a>
                <form method="post" action="${pageContext.request.contextPath}/staff/catalog"
                      class="d-inline" onsubmit="return confirm('Xóa thể loại này?');">
                  <input type="hidden" name="action" value="deleteCategory">
                  <input type="hidden" name="id" value="<%= c.id() %>">
                  <button type="submit" class="btn btn-outline-danger btn-sm"
                          <%= c.bookCount() > 0 ? "disabled title='Đang có sách sử dụng'" : "" %>>Xóa</button>
                </form>
              </td>
            </tr>
            <%   }
               } %>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Cột Tác giả -->
    <div class="col-md-6">
      <div class="card shadow-sm mb-3">
        <div class="card-header bg-white fw-bold">
          <%= editAuthor != null ? "Cập nhật tác giả #" + editAuthor.id() : "Thêm tác giả mới" %>
        </div>
        <div class="card-body">
          <form method="post" action="${pageContext.request.contextPath}/staff/catalog" class="row g-2">
            <input type="hidden" name="action" value="<%= editAuthor != null ? "updateAuthor" : "createAuthor" %>">
            <% if (editAuthor != null) { %>
            <input type="hidden" name="id" value="<%= editAuthor.id() %>">
            <% } %>
            <div class="col-sm-8">
              <input type="text" class="form-control" name="name" placeholder="Nhập họ tên tác giả..."
                     required maxlength="128"
                     value="<%= editAuthor != null ? editAuthor.name() : "" %>">
            </div>
            <div class="col-sm-4 d-flex gap-1">
              <button type="submit" class="btn btn-primary w-100">
                <%= editAuthor != null ? "Lưu" : "Thêm" %>
              </button>
              <% if (editAuthor != null) { %>
              <a href="${pageContext.request.contextPath}/staff/catalog" class="btn btn-secondary">Hủy</a>
              <% } %>
            </div>
          </form>
        </div>
      </div>

      <div class="card shadow-sm">
        <div class="card-header bg-white fw-bold">Danh sách tác giả (<%= authors != null ? authors.size() : 0 %>)</div>
        <div class="table-responsive">
          <table class="table table-striped table-hover align-middle mb-0">
            <thead class="table-light">
            <tr>
              <th>ID</th>
              <th>Họ tên tác giả</th>
              <th class="text-center">Số đầu sách</th>
              <th class="text-end">Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <% if (authors == null || authors.isEmpty()) { %>
            <tr>
              <td colspan="4" class="text-center text-muted py-3">Chưa có tác giả nào.</td>
            </tr>
            <% } else {
                 for (Author a : authors) { %>
            <tr>
              <td><%= a.id() %></td>
              <td><%= a.name() %></td>
              <td class="text-center"><span class="badge bg-secondary"><%= a.bookCount() %></span></td>
              <td class="text-end">
                <a href="${pageContext.request.contextPath}/staff/catalog?editAuthorId=<%= a.id() %>"
                   class="btn btn-outline-primary btn-sm">Sửa</a>
                <form method="post" action="${pageContext.request.contextPath}/staff/catalog"
                      class="d-inline" onsubmit="return confirm('Xóa tác giả này?');">
                  <input type="hidden" name="action" value="deleteAuthor">
                  <input type="hidden" name="id" value="<%= a.id() %>">
                  <button type="submit" class="btn btn-outline-danger btn-sm"
                          <%= a.bookCount() > 0 ? "disabled title='Đang có sách sử dụng'" : "" %>>Xóa</button>
                </form>
              </td>
            </tr>
            <%   }
               } %>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>
</body>
</html>
