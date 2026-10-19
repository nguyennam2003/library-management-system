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
  <title>Bản sách — <%= book != null ? book.title() : "" %></title>
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
    <h1 class="h4 mb-0">Quản lý Bản sách vật lý</h1>
    <a href="${pageContext.request.contextPath}/staff/books" class="btn btn-outline-secondary btn-sm">&larr; Quay lại danh sách đầu sách</a>
  </div>

  <% if (request.getAttribute("info") != null) { %>
  <div class="alert alert-success"><%= request.getAttribute("info") %></div>
  <% } %>
  <% if (request.getAttribute("error") != null) { %>
  <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
  <% } %>

  <!-- Thông tin đầu sách -->
  <div class="card shadow-sm mb-4">
    <div class="card-body">
      <div class="row">
        <div class="col-md-8">
          <h2 class="h5 mb-1"><%= book.title() %></h2>
          <p class="mb-1 text-muted">
            Tác giả: <strong><%= book.authorNames().isEmpty() ? "—" : book.authorNames() %></strong> |
            Thể loại: <strong><%= book.categoryName() %></strong>
          </p>
          <p class="mb-0 small text-muted">
            ISBN: <%= book.isbn() != null ? book.isbn() : "—" %> |
            NXB: <%= book.publisher() %> (<%= book.publishYear() %>) |
            Trạng thái đầu sách:
            <% if (book.isActive()) { %>
            <span class="badge bg-success">ACTIVE</span>
            <% } else { %>
            <span class="badge bg-warning text-dark">WITHDRAWN</span>
            <% } %>
          </p>
        </div>
        <div class="col-md-4 text-md-end mt-3 mt-md-0">
          <div class="small text-muted">Số bản sẵn sàng / Tổng số bản</div>
          <div class="fs-4 fw-bold text-success"><%= book.availableCopies() %> / <%= book.totalCopies() %></div>
        </div>
      </div>
    </div>
  </div>

  <div class="row g-4">
    <!-- Form thêm bản sách -->
    <div class="col-md-4">
      <div class="card shadow-sm">
        <div class="card-header bg-white fw-bold">Thêm bản sách vật lý</div>
        <div class="card-body">
          <form method="post" action="${pageContext.request.contextPath}/staff/books/copies">
            <input type="hidden" name="action" value="addCopy">
            <input type="hidden" name="bookId" value="<%= book.id() %>">
            <div class="mb-3">
              <label class="form-label" for="code">Mã bản sách <span class="text-danger">*</span></label>
              <input type="text" class="form-control" id="code" name="code" required maxlength="32"
                     placeholder="VD: CC-003">
              <div class="form-text">Mã định danh riêng trên từng cuốn sách vật lý (unique).</div>
            </div>
            <button type="submit" class="btn btn-primary w-100">Thêm bản sách</button>
          </form>
        </div>
      </div>
    </div>

    <!-- Danh sách bản sách -->
    <div class="col-md-8">
      <div class="card shadow-sm">
        <div class="card-header bg-white fw-bold">
          Danh sách bản sách của đầu sách (<%= copies != null ? copies.size() : 0 %>)
        </div>
        <div class="table-responsive">
          <table class="table table-striped table-hover align-middle mb-0">
            <thead class="table-light">
            <tr>
              <th>ID</th>
              <th>Mã bản sách</th>
              <th class="text-center">Trạng thái</th>
              <th class="text-center">Lịch sử mượn</th>
              <th class="text-end">Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <% if (copies == null || copies.isEmpty()) { %>
            <tr>
              <td colspan="5" class="text-center text-muted py-4">Chưa có bản sách vật lý nào cho đầu sách này.</td>
            </tr>
            <% } else {
                 for (BookCopy copy : copies) { %>
            <tr>
              <td><%= copy.id() %></td>
              <td class="fw-semibold"><code><%= copy.code() %></code></td>
              <td class="text-center">
                <% if (copy.isAvailable()) { %>
                <span class="badge bg-success">AVAILABLE (Sẵn sàng)</span>
                <% } else if (copy.isBorrowed()) { %>
                <span class="badge bg-primary">BORROWED (Đang mượn)</span>
                <% } else { %>
                <span class="badge bg-secondary">WITHDRAWN (Ngừng lưu hành)</span>
                <% } %>
              </td>
              <td class="text-center">
                <% if (copy.hasLoanHistory()) { %>
                <span class="badge bg-info text-dark">Đã từng mượn</span>
                <% } else { %>
                <span class="text-muted small">Chưa từng mượn</span>
                <% } %>
              </td>
              <td class="text-end">
                <% if (copy.isAvailable()) { %>
                <form method="post" action="${pageContext.request.contextPath}/staff/books/copies" class="d-inline">
                  <input type="hidden" name="action" value="withdrawCopy">
                  <input type="hidden" name="bookId" value="<%= book.id() %>">
                  <input type="hidden" name="copyId" value="<%= copy.id() %>">
                  <button type="submit" class="btn btn-outline-warning btn-sm">Ngừng lưu hành</button>
                </form>
                <% } else if (copy.isWithdrawn()) { %>
                <form method="post" action="${pageContext.request.contextPath}/staff/books/copies" class="d-inline">
                  <input type="hidden" name="action" value="restoreCopy">
                  <input type="hidden" name="bookId" value="<%= book.id() %>">
                  <input type="hidden" name="copyId" value="<%= copy.id() %>">
                  <button type="submit" class="btn btn-outline-success btn-sm">Mở lại</button>
                </form>
                <% } else { %>
                <button type="button" class="btn btn-outline-secondary btn-sm" disabled
                        title="Bản sách đang được mượn, không thể ngừng lưu hành">Đang mượn</button>
                <% } %>

                <form method="post" action="${pageContext.request.contextPath}/staff/books/copies" class="d-inline"
                      onsubmit="return confirm('Xóa vật lý bản sách này?');">
                  <input type="hidden" name="action" value="deleteCopy">
                  <input type="hidden" name="bookId" value="<%= book.id() %>">
                  <input type="hidden" name="copyId" value="<%= copy.id() %>">
                  <button type="submit" class="btn btn-outline-danger btn-sm"
                          <%= (copy.hasLoanHistory() || copy.isBorrowed()) ? "disabled title='Không thể xóa bản sách đã mượn'" : "" %>>
                    Xóa
                  </button>
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
