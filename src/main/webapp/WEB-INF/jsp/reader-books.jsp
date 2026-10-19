<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.List,com.ptit.library.model.AuthSession,com.ptit.library.model.Category,com.ptit.library.model.BookItem,com.ptit.library.service.BookCatalogService" %>
<%
  AuthSession auth = (AuthSession) request.getAttribute("auth");
  List<Category> categories = (List<Category>) request.getAttribute("categories");
  BookCatalogService.PageResult pageResult = (BookCatalogService.PageResult) request.getAttribute("pageResult");
  String q = (String) request.getAttribute("q");
  if (q == null) q = "";
  Long categoryId = (Long) request.getAttribute("categoryId");
  boolean onlyAvailable = Boolean.TRUE.equals(request.getAttribute("onlyAvailable"));
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Tra cứu sách — Thư viện</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg navbar-dark bg-success">
  <div class="container">
    <a class="navbar-brand" href="${pageContext.request.contextPath}/reader/home">Thư viện</a>
    <div class="navbar-nav me-auto">
      <a class="nav-link" href="${pageContext.request.contextPath}/reader/home">Trang độc giả</a>
      <a class="nav-link active" href="${pageContext.request.contextPath}/reader/books">Tra cứu sách</a>
    </div>
    <div class="d-flex align-items-center gap-2 text-white">
      <span class="small">Thẻ: <strong><%= auth != null ? auth.readerCode() : "" %></strong></span>
      <form method="post" action="${pageContext.request.contextPath}/logout" class="m-0">
        <button class="btn btn-outline-light btn-sm" type="submit">Đăng xuất</button>
      </form>
    </div>
  </div>
</nav>

<div class="container py-4">
  <h1 class="h4 mb-3">Tra cứu danh mục sách</h1>

  <!-- Thanh tìm kiếm & lọc -->
  <div class="card shadow-sm mb-4">
    <div class="card-body">
      <form method="get" action="${pageContext.request.contextPath}/reader/books" class="row g-2 align-items-end">
        <div class="col-md-5">
          <label class="form-label small mb-1" for="q">Từ khóa (Tên sách, Tác giả, ISBN)</label>
          <input type="text" class="form-control" id="q" name="q" value="<%= q %>"
                 placeholder="Nhập tên sách, tên tác giả hoặc mã ISBN...">
        </div>
        <div class="col-md-3">
          <label class="form-label small mb-1" for="categoryId">Thể loại</label>
          <select class="form-select" id="categoryId" name="categoryId">
            <option value="">Tất cả thể loại</option>
            <% if (categories != null) {
                 for (Category c : categories) {
                   boolean sel = categoryId != null && categoryId == c.id();
            %>
            <option value="<%= c.id() %>" <%= sel ? "selected" : "" %>><%= c.name() %></option>
            <%   }
               } %>
          </select>
        </div>
        <div class="col-md-2">
          <div class="form-check mb-2">
            <input class="form-check-input" type="checkbox" id="onlyAvailable" name="onlyAvailable" value="1"
                   <%= onlyAvailable ? "checked" : "" %>>
            <label class="form-check-label small" for="onlyAvailable">Còn bản sẵn sàng</label>
          </div>
        </div>
        <div class="col-md-2 d-flex gap-1">
          <button type="submit" class="btn btn-success w-100">Tìm kiếm</button>
          <a href="${pageContext.request.contextPath}/reader/books" class="btn btn-outline-secondary">Xóa</a>
        </div>
      </form>
    </div>
  </div>

  <!-- Danh sách kết quả -->
  <div class="card shadow-sm">
    <div class="card-header bg-white d-flex justify-content-between align-items-center">
      <span class="fw-bold">Đầu sách đang lưu hành (<%= pageResult != null ? pageResult.totalItems() : 0 %>)</span>
      <span class="small text-muted">Trang <%= pageResult != null ? pageResult.page() : 1 %> / <%= pageResult != null ? pageResult.totalPages() : 1 %></span>
    </div>
    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light">
        <tr>
          <th>Tên đầu sách</th>
          <th>Tác giả</th>
          <th>Thể loại</th>
          <th>NXB / Năm XB</th>
          <th class="text-center">Số bản sẵn sàng</th>
          <th class="text-end">Chi tiết</th>
        </tr>
        </thead>
        <tbody>
        <% if (pageResult == null || pageResult.items().isEmpty()) { %>
        <tr>
          <td colspan="6" class="text-center text-muted py-4">Không tìm thấy đầu sách nào phù hợp.</td>
        </tr>
        <% } else {
             for (BookItem b : pageResult.items()) { %>
        <tr>
          <td>
            <div class="fw-semibold"><%= b.title() %></div>
            <div class="small text-muted">ISBN: <%= b.isbn() != null ? b.isbn() : "—" %></div>
          </td>
          <td><%= b.authorNames().isEmpty() ? "—" : b.authorNames() %></td>
          <td><span class="badge bg-light text-dark border"><%= b.categoryName() %></span></td>
          <td><%= b.publisher() %> (<%= b.publishYear() %>)</td>
          <td class="text-center">
            <% if (b.availableCopies() > 0) { %>
            <span class="badge bg-success">Còn <%= b.availableCopies() %> bản</span>
            <% } else { %>
            <span class="badge bg-secondary">Hết bản sẵn sàng (0)</span>
            <% } %>
          </td>
          <td class="text-end">
            <a href="${pageContext.request.contextPath}/reader/books?id=<%= b.id() %>"
               class="btn btn-outline-success btn-sm">Xem chi tiết</a>
          </td>
        </tr>
        <%   }
           } %>
        </tbody>
      </table>
    </div>
    <% if (pageResult != null && pageResult.totalPages() > 1) {
         String baseQuery = "q=" + java.net.URLEncoder.encode(q, "UTF-8")
                 + (categoryId != null ? "&categoryId=" + categoryId : "")
                 + (onlyAvailable ? "&onlyAvailable=1" : "");
    %>
    <div class="card-footer bg-white d-flex justify-content-center">
      <nav>
        <ul class="pagination pagination-sm mb-0">
          <% for (int p = 1; p <= pageResult.totalPages(); p++) { %>
          <li class="page-item <%= p == pageResult.page() ? "active" : "" %>">
            <a class="page-link" href="${pageContext.request.contextPath}/reader/books?<%= baseQuery %>&page=<%= p %>"><%= p %></a>
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
