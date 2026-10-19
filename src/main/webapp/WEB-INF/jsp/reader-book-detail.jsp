<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.List,com.ptit.library.model.AuthSession,com.ptit.library.model.BookItem,com.ptit.library.model.BookCopy" %>
<%
  AuthSession auth = (AuthSession) request.getAttribute("auth");
  BookItem book = (BookItem) request.getAttribute("book");
  List<BookCopy> copies = (List<BookCopy>) request.getAttribute("copies");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Chi tiết đầu sách — Thư viện</title>
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
  <div class="mb-3">
    <a href="${pageContext.request.contextPath}/reader/books" class="btn btn-outline-secondary btn-sm">&larr; Quay lại tra cứu sách</a>
  </div>

  <% if (request.getAttribute("error") != null || book == null) { %>
  <div class="alert alert-warning">
    <%= request.getAttribute("error") != null ? request.getAttribute("error") : "Không tìm thấy thông tin đầu sách." %>
  </div>
  <% } else { %>
  <div class="card shadow-sm mb-4">
    <div class="card-header bg-white fw-bold">Thông tin chi tiết đầu sách</div>
    <div class="card-body">
      <h1 class="h4 mb-3"><%= book.title() %></h1>
      <div class="row g-3">
        <div class="col-md-6">
          <p class="mb-2"><strong>Tác giả:</strong> <%= book.authorNames().isEmpty() ? "—" : book.authorNames() %></p>
          <p class="mb-2"><strong>Thể loại:</strong> <%= book.categoryName() %></p>
          <p class="mb-2"><strong>ISBN:</strong> <%= book.isbn() != null ? book.isbn() : "Không có" %></p>
        </div>
        <div class="col-md-6">
          <p class="mb-2"><strong>Nhà xuất bản:</strong> <%= book.publisher() %></p>
          <p class="mb-2"><strong>Năm xuất bản:</strong> <%= book.publishYear() %></p>
          <p class="mb-2">
            <strong>Số bản sẵn sàng cho mượn:</strong>
            <span class="badge <%= book.availableCopies() > 0 ? "bg-success" : "bg-secondary" %> fs-6">
              <%= book.availableCopies() %> / <%= book.totalCopies() %> bản
            </span>
          </p>
        </div>
      </div>
      <div class="alert alert-info mt-3 mb-0 small">
        Để mượn sách, bạn vui lòng ghi nhớ <strong>Mã bản sách</strong> đang ở trạng thái <strong>AVAILABLE</strong> bên dưới và mang thẻ độc giả (trạng thái <strong>ACTIVE</strong>) tới quầy thủ thư để lập phiếu mượn.
      </div>
    </div>
  </div>

  <div class="card shadow-sm">
    <div class="card-header bg-white fw-bold">Danh sách các bản sách vật lý tại thư viện</div>
    <div class="table-responsive">
      <table class="table table-striped align-middle mb-0">
        <thead class="table-light">
        <tr>
          <th>Mã bản sách</th>
          <th>Tình trạng</th>
        </tr>
        </thead>
        <tbody>
        <% if (copies == null || copies.isEmpty()) { %>
        <tr>
          <td colspan="2" class="text-center text-muted py-3">Đầu sách này hiện chưa có bản sách vật lý nào.</td>
        </tr>
        <% } else {
             for (BookCopy copy : copies) { %>
        <tr>
          <td><code><%= copy.code() %></code></td>
          <td>
            <% if (copy.isAvailable()) { %>
            <span class="badge bg-success">Sẵn sàng cho mượn (AVAILABLE)</span>
            <% } else if (copy.isBorrowed()) { %>
            <span class="badge bg-warning text-dark">Đang được mượn (BORROWED)</span>
            <% } else { %>
            <span class="badge bg-secondary">Ngừng lưu hành (WITHDRAWN)</span>
            <% } %>
          </td>
        </tr>
        <%   }
           } %>
        </tbody>
      </table>
    </div>
  </div>
  <% } %>
</div>
</body>
</html>
