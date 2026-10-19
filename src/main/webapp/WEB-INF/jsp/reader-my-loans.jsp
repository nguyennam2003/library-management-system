<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.List,com.ptit.library.model.AuthSession,com.ptit.library.model.ReaderProfile,com.ptit.library.model.ReaderLoanItem" %>
<%
  AuthSession auth = (AuthSession) request.getAttribute("auth");
  ReaderProfile profile = (ReaderProfile) request.getAttribute("profile");
  List<ReaderLoanItem> borrowedItems = (List<ReaderLoanItem>) request.getAttribute("borrowedItems");
  List<ReaderLoanItem> historyItems = (List<ReaderLoanItem>) request.getAttribute("historyItems");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Hồ sơ & Sách đang mượn — Thư viện</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg navbar-dark bg-success">
  <div class="container">
    <a class="navbar-brand" href="${pageContext.request.contextPath}/reader/home">Thư viện</a>
    <div class="navbar-nav me-auto">
      <a class="nav-link" href="${pageContext.request.contextPath}/reader/home">Trang độc giả</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/reader/books">Tra cứu sách</a>
      <a class="nav-link active" href="${pageContext.request.contextPath}/reader/my-loans">Sách đang mượn & Lịch sử</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/account/password">Đổi mật khẩu</a>
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
  <h1 class="h4 mb-3">Hồ sơ độc giả & Thông tin mượn trả</h1>

  <!-- Thông tin hồ sơ thẻ độc giả -->
  <div class="card shadow-sm mb-4">
    <div class="card-header bg-white d-flex justify-content-between align-items-center">
      <span class="fw-bold">Thông tin thẻ độc giả</span>
      <a href="${pageContext.request.contextPath}/account/password" class="btn btn-outline-secondary btn-sm">Đổi mật khẩu</a>
    </div>
    <div class="card-body">
      <% if (profile == null) { %>
      <p class="text-muted mb-0">Không tìm thấy thông tin hồ sơ độc giả.</p>
      <% } else { %>
      <div class="row g-3">
        <div class="col-md-4">
          <div class="small text-muted">Mã độc giả</div>
          <div class="fw-bold fs-5"><%= profile.code() %></div>
        </div>
        <div class="col-md-4">
          <div class="small text-muted">Họ và tên</div>
          <div class="fw-semibold"><%= profile.fullName() %></div>
        </div>
        <div class="col-md-4">
          <div class="small text-muted">Trạng thái thẻ</div>
          <div>
            <% if ("ACTIVE".equals(profile.status())) { %>
            <span class="badge bg-success">ACTIVE (Đang hoạt động)</span>
            <% } else if ("PENDING".equals(profile.status())) { %>
            <span class="badge bg-warning text-dark">PENDING (Chờ duyệt)</span>
            <% } else if ("LOCKED".equals(profile.status())) { %>
            <span class="badge bg-danger">LOCKED (Đang khóa)</span>
            <% } else { %>
            <span class="badge bg-secondary"><%= profile.status() %></span>
            <% } %>
          </div>
        </div>
        <div class="col-md-4">
          <div class="small text-muted">Tên đăng nhập</div>
          <div><%= auth != null ? auth.username() : "" %></div>
        </div>
        <div class="col-md-4">
          <div class="small text-muted">Email</div>
          <div><%= profile.email() %></div>
        </div>
        <div class="col-md-4">
          <div class="small text-muted">Số điện thoại</div>
          <div><%= profile.phone() %></div>
        </div>
      </div>
      <% } %>
    </div>
  </div>

  <!-- Danh sách sách đang mượn (BORROWED) -->
  <div class="card shadow-sm mb-4">
    <div class="card-header bg-white fw-bold">
      Sách đang mượn hiện tại (<%= borrowedItems != null ? borrowedItems.size() : 0 %>)
    </div>
    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light">
        <tr>
          <th>Mã phiếu</th>
          <th>Tên đầu sách</th>
          <th>Mã bản sách</th>
          <th>Ngày mượn</th>
          <th>Hạn trả</th>
          <th class="text-center">Tình trạng</th>
        </tr>
        </thead>
        <tbody>
        <% if (borrowedItems == null || borrowedItems.isEmpty()) { %>
        <tr>
          <td colspan="6" class="text-center text-muted py-4">Bạn hiện không có cuốn sách nào đang mượn.</td>
        </tr>
        <% } else {
             for (ReaderLoanItem item : borrowedItems) { %>
        <tr>
          <td><code><%= item.loanCode() %></code></td>
          <td>
            <a href="${pageContext.request.contextPath}/reader/books?id=<%= item.bookId() %>" class="text-decoration-none fw-semibold">
              <%= item.bookTitle() %>
            </a>
          </td>
          <td><code><%= item.copyCode() %></code></td>
          <td><%= item.borrowedOn() %></td>
          <td class="fw-semibold <%= item.overdue() ? "text-danger" : "" %>"><%= item.dueDate() %></td>
          <td class="text-center">
            <% if (item.overdue()) { %>
            <span class="badge bg-danger">Quá hạn trả</span>
            <% } else { %>
            <span class="badge bg-primary">Đang mượn (BORROWED)</span>
            <% } %>
          </td>
        </tr>
        <%   }
           } %>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Lịch sử mượn / trả của độc giả -->
  <div class="card shadow-sm">
    <div class="card-header bg-white fw-bold">
      Lịch sử mượn / trả của tôi (<%= historyItems != null ? historyItems.size() : 0 %>)
    </div>
    <div class="table-responsive">
      <table class="table table-striped align-middle mb-0">
        <thead class="table-light">
        <tr>
          <th>Mã phiếu</th>
          <th>Tên đầu sách</th>
          <th>Mã bản sách</th>
          <th>Ngày mượn</th>
          <th>Hạn trả</th>
          <th>Ngày trả thực tế</th>
          <th class="text-center">Trạng thái</th>
        </tr>
        </thead>
        <tbody>
        <% if (historyItems == null || historyItems.isEmpty()) { %>
        <tr>
          <td colspan="7" class="text-center text-muted py-4">Chưa có lịch sử giao dịch mượn/trả nào.</td>
        </tr>
        <% } else {
             for (ReaderLoanItem item : historyItems) { %>
        <tr>
          <td><code><%= item.loanCode() %></code></td>
          <td><%= item.bookTitle() %></td>
          <td><code><%= item.copyCode() %></code></td>
          <td><%= item.borrowedOn() %></td>
          <td><%= item.dueDate() %></td>
          <td><%= item.returnedAt() != null ? item.returnedAt() : "—" %></td>
          <td class="text-center">
            <% if ("RETURNED".equals(item.detailStatus())) { %>
            <span class="badge bg-success">Đã trả (RETURNED)</span>
            <% } else if (item.overdue()) { %>
            <span class="badge bg-danger">Đang mượn (Quá hạn)</span>
            <% } else { %>
            <span class="badge bg-primary">Đang mượn (BORROWED)</span>
            <% } %>
          </td>
        </tr>
        <%   }
           } %>
        </tbody>
      </table>
    </div>
  </div>
</div>
</body>
</html>
