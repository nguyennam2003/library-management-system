<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.List,com.ptit.library.model.AuthSession,com.ptit.library.model.Category,com.ptit.library.model.Author,com.ptit.library.model.BookItem,com.ptit.library.service.BookCatalogService" %>
<%
  AuthSession auth = (AuthSession) request.getAttribute("auth");
  List<Category> categories = (List<Category>) request.getAttribute("categories");
  List<Author> authors = (List<Author>) request.getAttribute("authors");
  BookCatalogService.PageResult pageResult = (BookCatalogService.PageResult) request.getAttribute("pageResult");
  BookItem editBook = (BookItem) request.getAttribute("editBook");
  List<?> errors = (List<?>) request.getAttribute("errors");

  Long formId = (Long) request.getAttribute("formId");
  if (formId == null && editBook != null) {
    formId = editBook.id();
  }
  String formTitle = (String) request.getAttribute("formTitle");
  if (formTitle == null) {
    formTitle = editBook != null ? editBook.title() : "";
  }
  String formIsbn = (String) request.getAttribute("formIsbn");
  if (formIsbn == null) {
    formIsbn = editBook != null && editBook.isbn() != null ? editBook.isbn() : "";
  }
  Long formCategoryId = (Long) request.getAttribute("formCategoryId");
  if (formCategoryId == null && editBook != null) {
    formCategoryId = editBook.categoryId();
  }
  String formPublisher = (String) request.getAttribute("formPublisher");
  if (formPublisher == null) {
    formPublisher = editBook != null ? editBook.publisher() : "";
  }
  String formPublishYear = (String) request.getAttribute("formPublishYear");
  if (formPublishYear == null) {
    formPublishYear = editBook != null ? String.valueOf(editBook.publishYear()) : "2024";
  }
  List<Long> formAuthorIds = (List<Long>) request.getAttribute("formAuthorIds");
  if (formAuthorIds == null && editBook != null) {
    formAuthorIds = editBook.authorIds();
  }

  String q = (String) request.getAttribute("q");
  if (q == null) q = "";
  Long categoryIdFilter = (Long) request.getAttribute("categoryIdFilter");
  boolean onlyAvailable = Boolean.TRUE.equals(request.getAttribute("onlyAvailable"));
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Quản lý Đầu sách — Quản lý thư viện</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
  <div class="container">
    <a class="navbar-brand" href="${pageContext.request.contextPath}/staff/home">Quản lý thư viện</a>
    <div class="navbar-nav me-auto">
      <a class="nav-link" href="${pageContext.request.contextPath}/staff/home">Trang chủ</a>
      <a class="nav-link active" href="${pageContext.request.contextPath}/staff/books">Đầu sách & Bản sách</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/staff/catalog">Thể loại & Tác giả</a>
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
    <h1 class="h4 mb-0">Quản lý Đầu sách</h1>
    <a href="${pageContext.request.contextPath}/staff/catalog" class="btn btn-outline-primary btn-sm">Quản lý Thể loại & Tác giả</a>
  </div>

  <% if (request.getAttribute("info") != null) { %>
  <div class="alert alert-success"><%= request.getAttribute("info") %></div>
  <% } %>
  <% if (request.getAttribute("error") != null) { %>
  <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
  <% } %>
  <% if (errors != null && !errors.isEmpty()) { %>
  <div class="alert alert-danger">
    <ul class="mb-0">
      <% for (Object err : errors) { %><li><%= err %></li><% } %>
    </ul>
  </div>
  <% } %>

  <!-- Form Thêm / Cập nhật đầu sách -->
  <div class="card shadow-sm mb-4">
    <div class="card-header bg-white fw-bold">
      <%= formId != null ? "Cập nhật đầu sách #" + formId : "Thêm đầu sách mới" %>
    </div>
    <div class="card-body">
      <form method="post" action="${pageContext.request.contextPath}/staff/books">
        <input type="hidden" name="action" value="<%= formId != null ? "update" : "create" %>">
        <% if (formId != null) { %>
        <input type="hidden" name="id" value="<%= formId %>">
        <% } %>
        <div class="row g-3">
          <div class="col-md-5">
            <label class="form-label">Tên đầu sách <span class="text-danger">*</span></label>
            <input type="text" class="form-control" name="title" required maxlength="255"
                   value="<%= formTitle %>">
          </div>
          <div class="col-md-3">
            <label class="form-label">ISBN (tùy chọn)</label>
            <input type="text" class="form-control" name="isbn" maxlength="17"
                   placeholder="VD: 978-0132350884" value="<%= formIsbn %>">
          </div>
          <div class="col-md-4">
            <label class="form-label">Thể loại <span class="text-danger">*</span></label>
            <select class="form-select" name="categoryId" required>
              <option value="">-- Chọn thể loại --</option>
              <% if (categories != null) {
                   for (Category c : categories) {
                     boolean selected = formCategoryId != null && formCategoryId == c.id();
              %>
              <option value="<%= c.id() %>" <%= selected ? "selected" : "" %>><%= c.name() %></option>
              <%   }
                 } %>
            </select>
          </div>
          <div class="col-md-5">
            <label class="form-label">Nhà xuất bản <span class="text-danger">*</span></label>
            <input type="text" class="form-control" name="publisher" required maxlength="128"
                   value="<%= formPublisher %>">
          </div>
          <div class="col-md-3">
            <label class="form-label">Năm xuất bản <span class="text-danger">*</span></label>
            <input type="number" class="form-control" name="publishYear" required min="1500" max="2100"
                   value="<%= formPublishYear %>">
          </div>
          <div class="col-md-12">
            <label class="form-label">Tác giả (chọn một hoặc nhiều) <span class="text-danger">*</span></label>
            <div class="border rounded p-2 bg-white" style="max-height: 9rem; overflow-y: auto;">
              <div class="row g-2">
                <% if (authors == null || authors.isEmpty()) { %>
                <div class="col-12 text-muted small">Chưa có tác giả. Vui lòng thêm tác giả ở mục Thể loại & Tác giả.</div>
                <% } else {
                     for (Author a : authors) {
                       boolean checked = formAuthorIds != null && formAuthorIds.contains(a.id());
                %>
                <div class="col-md-4 col-sm-6">
                  <div class="form-check">
                    <input class="form-check-input" type="checkbox" name="authorIds"
                           id="author_<%= a.id() %>" value="<%= a.id() %>" <%= checked ? "checked" : "" %>>
                    <label class="form-check-label" for="author_<%= a.id() %>"><%= a.name() %></label>
                  </div>
                </div>
                <%   }
                   } %>
              </div>
            </div>
          </div>
          <div class="col-12 d-flex gap-2">
            <button type="submit" class="btn btn-primary">
              <%= formId != null ? "Lưu cập nhật" : "Thêm đầu sách" %>
            </button>
            <% if (formId != null) { %>
            <a href="${pageContext.request.contextPath}/staff/books" class="btn btn-secondary">Hủy sửa</a>
            <% } %>
          </div>
        </div>
      </form>
    </div>
  </div>

  <!-- Bộ lọc & Tìm kiếm -->
  <div class="card shadow-sm mb-3">
    <div class="card-body">
      <form method="get" action="${pageContext.request.contextPath}/staff/books" class="row g-2 align-items-end">
        <div class="col-md-4">
          <label class="form-label small mb-1">Tìm theo tên sách, tác giả hoặc ISBN</label>
          <input type="text" class="form-control" name="q" value="<%= q %>"
                 placeholder="Nhập tên sách, tác giả, ISBN...">
        </div>
        <div class="col-md-3">
          <label class="form-label small mb-1">Thể loại</label>
          <select class="form-select" name="categoryIdFilter">
            <option value="">Tất cả thể loại</option>
            <% if (categories != null) {
                 for (Category c : categories) {
                   boolean sel = categoryIdFilter != null && categoryIdFilter == c.id();
            %>
            <option value="<%= c.id() %>" <%= sel ? "selected" : "" %>><%= c.name() %></option>
            <%   }
               } %>
          </select>
        </div>
        <div class="col-md-3">
          <div class="form-check mb-2">
            <input class="form-check-input" type="checkbox" id="onlyAvailable" name="onlyAvailable" value="1"
                   <%= onlyAvailable ? "checked" : "" %>>
            <label class="form-check-label" for="onlyAvailable">Chỉ sách còn bản sẵn sàng</label>
          </div>
        </div>
        <div class="col-md-2 d-flex gap-1">
          <button type="submit" class="btn btn-outline-primary w-100">Lọc</button>
          <a href="${pageContext.request.contextPath}/staff/books" class="btn btn-outline-secondary">Xóa</a>
        </div>
      </form>
    </div>
  </div>

  <!-- Bảng danh sách đầu sách -->
  <div class="card shadow-sm">
    <div class="card-header bg-white d-flex justify-content-between align-items-center">
      <span class="fw-bold">Danh sách đầu sách (<%= pageResult != null ? pageResult.totalItems() : 0 %>)</span>
      <span class="small text-muted">Trang <%= pageResult != null ? pageResult.page() : 1 %> / <%= pageResult != null ? pageResult.totalPages() : 1 %></span>
    </div>
    <div class="table-responsive">
      <table class="table table-striped table-hover align-middle mb-0">
        <thead class="table-light">
        <tr>
          <th>ID</th>
          <th>Tên sách / ISBN</th>
          <th>Tác giả</th>
          <th>Thể loại</th>
          <th>NXB / Năm</th>
          <th class="text-center">Bản sẵn sàng / Tổng</th>
          <th class="text-center">Trạng thái</th>
          <th class="text-end">Thao tác</th>
        </tr>
        </thead>
        <tbody>
        <% if (pageResult == null || pageResult.items().isEmpty()) { %>
        <tr>
          <td colspan="8" class="text-center text-muted py-4">Không tìm thấy đầu sách nào phù hợp.</td>
        </tr>
        <% } else {
             for (BookItem b : pageResult.items()) { %>
        <tr>
          <td><%= b.id() %></td>
          <td>
            <div class="fw-semibold"><%= b.title() %></div>
            <div class="small text-muted">ISBN: <%= b.isbn() != null ? b.isbn() : "—" %></div>
          </td>
          <td><%= b.authorNames().isEmpty() ? "—" : b.authorNames() %></td>
          <td><%= b.categoryName() %></td>
          <td><%= b.publisher() %> (<%= b.publishYear() %>)</td>
          <td class="text-center">
            <span class="badge <%= b.availableCopies() > 0 ? "bg-success" : "bg-secondary" %>">
              <%= b.availableCopies() %> / <%= b.totalCopies() %>
            </span>
          </td>
          <td class="text-center">
            <% if (b.isActive()) { %>
            <span class="badge bg-success">Đang lưu hành</span>
            <% } else { %>
            <span class="badge bg-warning text-dark">Ngừng lưu hành</span>
            <% } %>
          </td>
          <td class="text-end">
            <div class="d-inline-flex gap-1 flex-wrap justify-content-end">
              <a href="${pageContext.request.contextPath}/staff/books/copies?bookId=<%= b.id() %>"
                 class="btn btn-info btn-sm text-white">Bản sách (<%= b.totalCopies() %>)</a>
              <a href="${pageContext.request.contextPath}/staff/books?editId=<%= b.id() %>"
                 class="btn btn-outline-primary btn-sm">Sửa</a>
              <form method="post" action="${pageContext.request.contextPath}/staff/books" class="d-inline">
                <input type="hidden" name="action" value="toggleStatus">
                <input type="hidden" name="id" value="<%= b.id() %>">
                <button type="submit" class="btn btn-outline-warning btn-sm">
                  <%= b.isActive() ? "Ngừng lưu hành" : "Mở lại" %>
                </button>
              </form>
              <form method="post" action="${pageContext.request.contextPath}/staff/books" class="d-inline"
                    onsubmit="return confirm('Xóa vật lý đầu sách này?');">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="<%= b.id() %>">
                <button type="submit" class="btn btn-outline-danger btn-sm"
                        <%= b.hasLoanHistory() ? "disabled title='Đã từng phát sinh mượn trả, không thể xóa'" : "" %>>
                  Xóa
                </button>
              </form>
            </div>
          </td>
        </tr>
        <%   }
           } %>
        </tbody>
      </table>
    </div>
    <% if (pageResult != null && pageResult.totalPages() > 1) {
         String baseQuery = "q=" + java.net.URLEncoder.encode(q, "UTF-8")
                 + (categoryIdFilter != null ? "&categoryIdFilter=" + categoryIdFilter : "")
                 + (onlyAvailable ? "&onlyAvailable=1" : "");
    %>
    <div class="card-footer bg-white d-flex justify-content-center">
      <nav>
        <ul class="pagination pagination-sm mb-0">
          <% for (int p = 1; p <= pageResult.totalPages(); p++) { %>
          <li class="page-item <%= p == pageResult.page() ? "active" : "" %>">
            <a class="page-link" href="${pageContext.request.contextPath}/staff/books?<%= baseQuery %>&page=<%= p %>"><%= p %></a>
          </li>
          <% } %>
        </ul>
      </nav>
    </div>
    <% } %>
  </div>
</div>
</body>
</html>
