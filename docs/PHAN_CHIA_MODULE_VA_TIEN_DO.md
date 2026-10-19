# TÀI LIỆU TIẾN ĐỘ DỰ ÁN, KIẾN TRÚC HIỆN TẠI & PHÂN CHIA MODULE (NHÓM 5 THÀNH VIÊN)

> **Dành cho thành viên trong nhóm & AI Coding Agent:**  
> Vui lòng đọc kỹ tài liệu này trước khi bắt đầu code các module tiếp theo để **không tạo trùng class/DAO/Servlet**, **không ghi đè logic đã có** và **tránh xung đột (merge conflict)** trên các file dùng chung.

---

## 1. Tổng quan trạng thái dự án hiện tại

Dự án **Hệ thống Quản lý Thư viện** được xây dựng theo đặc tả `docs/SRS_Quan_Ly_Thu_Vien_v1.1.md` với kiến trúc đơn giản phù hợp đồ án sinh viên:
- **Stack:** Java 17 + Servlet 4.0 (`javax.servlet`) + JSP 2.3 (Scriptlet `<% ... %>`, không dùng JSTL) + JDBC thuần (`PreparedStatement`) + MySQL 8.0 + Bootstrap 5.3.3 CDN.
- **Không sử dụng:** Spring, Hibernate/JPA, Lombok hay thư viện frontend phức tạp.

### Các hạng mục ĐÃ HOÀN THÀNH trên nhánh `main`

| Cụm chức năng | Use Case / FR (SRS v1.1) | Màn hình (UI) | Trạng thái |
| :--- | :--- | :--- | :--- |
| **Base & Hạ tầng** | Schema 10 bảng (`db/01_schema.sql`), Seed (`db/02_seed.sql`), Docker Compose, `ConnectionFactory`, `CharacterEncodingFilter`, `AuthFilter`, `RoleGuard`, `/health` | — | **ĐÃ XONG** |
| **Xác thực & Tự đăng ký** | `UC-01` (Đăng nhập), `UC-02` (Đăng xuất), `UC-05` (Độc giả tự đăng ký tài khoản `PENDING`) — `FR-AUTH-01..04, 06, 07, 09, 10`, `FR-READER-07` | `UI-01` (`/login`), `UI-02` (`/register`) | **ĐÃ XONG** |
| **Module 1 (Thành viên 1): Quản lý Kho Sách, Thể loại, Tác giả, Bản sách & Tra cứu Sách Độc giả** | `UC-09` (`FR-CAT-01..04`), `UC-10` (`FR-BOOK-01..08` cả Must & Should), `UC-14` (`FR-SEARCH-01..04`) | `UI-07` (`/staff/catalog`), `UI-08` (`/staff/books`), `UI-09` (`/staff/books/copies`), `UI-15` (`/reader/books`), `UI-16` (`/reader/books?id=...`) | **ĐÃ XONG** |

---

## 2. Chi tiết những gì Thành viên 1 (Module Sách & Danh mục) đã triển khai

### 2.1. Các chức năng nghiệp vụ đã hoàn thiện
1. **Quản lý Thể loại & Tác giả (`UC-09` — `FR-CAT-01..04` — `UI-07` tại `/staff/catalog`)**:
   - Thêm mới, cập nhật tên thể loại (`categories`, kiểm tra không rỗng, không trùng tên).
   - Thêm mới, cập nhật tên tác giả (`authors`, kiểm tra không rỗng).
   - Xóa thể loại / tác giả: Tự động đếm số đầu sách đang liên kết (`books.category_id` và `book_authors.author_id`), **chặn xóa nếu đang có đầu sách sử dụng** đúng quy tắc `FR-CAT-04`.
2. **Quản lý Đầu sách (`UC-10` — `FR-BOOK-01..04, 06..08` — `UI-08` tại `/staff/books`)**:
   - Thêm mới & cập nhật đầu sách (`books`): tên sách, ISBN (tùy chọn — cho phép bỏ trống hoặc nhập ISBN-10 / ISBN-13 có dấu gạch ngang `-`, hệ thống tự chuẩn hóa bỏ dấu gạch và kiểm tra unique), chọn 1 thể loại, chọn **một hoặc nhiều tác giả** (`book_authors` trong cùng JDBC transaction), nhà xuất bản, năm xuất bản.
   - Ngừng lưu hành / Mở lại lưu hành đầu sách (`ACTIVE` ↔ `WITHDRAWN`). Khi đầu sách `WITHDRAWN`, số bản sẵn sàng cho mượn trả về `0`.
   - Xóa vật lý đầu sách: Chỉ cho phép xóa khi đầu sách **chưa từng có bản sách nào phát sinh lịch sử trong `loan_details`** và không có bản đang `BORROWED`.
   - Tìm kiếm kết hợp (`FR-BOOK-06`): tìm theo tên sách, tên tác giả hoặc mã ISBN.
   - Lọc nâng cao (`FR-BOOK-07` - Should): lọc theo thể loại và chỉ hiển thị đầu sách còn bản `AVAILABLE`.
   - Phân trang (`FR-BOOK-08` - Should): phân trang danh sách sách (`page`, `PAGE_SIZE = 8`).
3. **Quản lý Bản sách vật lý (`UC-10` — `FR-BOOK-05` — `UI-09` tại `/staff/books/copies?bookId=...`)**:
   - Xem thông tin đầu sách và danh sách tất cả các bản sách vật lý (`book_copies`) thuộc đầu sách đó.
   - Thêm bản sách mới bằng mã định danh riêng (`code` unique, trạng thái mặc định `AVAILABLE`).
   - Ngừng lưu hành bản sách (`AVAILABLE` → `WITHDRAWN`). **Chặn tuyệt đối việc chuyển bản đang `BORROWED` sang `WITHDRAWN`**.
   - Mở lại lưu hành bản sách (`WITHDRAWN` → `AVAILABLE`).
   - Xóa vật lý bản sách: Chỉ cho phép khi bản sách **chưa từng xuất hiện trong `loan_details`** và không ở trạng thái `BORROWED`.
4. **Tra cứu sách & Xem chi tiết dành cho Độc giả (`UC-14` — `FR-SEARCH-01..04` — `UI-15, UI-16` tại `/reader/books`)**:
   - Danh sách tra cứu (`UI-15` tại `/reader/books`): Chỉ hiển thị các đầu sách đang lưu hành (`books.status = 'ACTIVE'`), hỗ trợ tìm kiếm theo tên/tác giả/ISBN, lọc theo thể loại, lọc còn bản sẵn sàng và phân trang.
   - Xem chi tiết đầu sách (`UI-16` tại `/reader/books?id={bookId}`): Hiển thị đầy đủ thông tin ấn phẩm, số lượng bản `AVAILABLE` / tổng số bản, và bảng danh sách mã từng bản sách vật lý tại thư viện để độc giả ghi nhớ mã khi ra quầy mượn.

### 2.2. Danh mục File đã có trong codebase (Không tạo trùng!)

| Phân lớp | Class / File | Trách nhiệm chính |
| :--- | :--- | :--- |
| **Model** | `com.ptit.library.model.Category` | Record `(id, name, bookCount)` |
| | `com.ptit.library.model.Author` | Record `(id, name, bookCount)` |
| | `com.ptit.library.model.BookItem` | Record đầu sách kèm `authorIds`, `authorNames`, `totalCopies`, `availableCopies`, `hasLoanHistory` |
| | `com.ptit.library.model.BookCopy` | Record bản vật lý `(id, bookId, code, status, hasLoanHistory)` |
| | `com.ptit.library.model.UserAccount` | Record tài khoản `users` + `roleCode` |
| | `com.ptit.library.model.ReaderProfile` | Record hồ sơ độc giả `readers` |
| | `com.ptit.library.model.AuthSession` | Thông tin lưu trong `HttpSession` (`userId`, `username`, `role`, `readerId`, `readerStatus`, `readerCode`) |
| **DAO** | `com.ptit.library.dao.CategoryDao` | CRUD bảng `categories` |
| | `com.ptit.library.dao.AuthorDao` | CRUD bảng `authors` |
| | `com.ptit.library.dao.BookDao` | Tìm kiếm, lọc, phân trang, CRUD bảng `books` và `book_authors` |
| | `com.ptit.library.dao.BookCopyDao` | CRUD bảng `book_copies` |
| | `com.ptit.library.dao.UserDao` | Truy vấn/thêm mới bảng `users`, tra `roles` |
| | `com.ptit.library.dao.ReaderDao` | Truy vấn/thêm mới bảng `readers`, sinh mã `DGxxxxxx` (`ReaderCodes`) |
| **Service** | `com.ptit.library.service.BookValidator` | Validate đầu sách, chuẩn hóa ISBN-10/ISBN-13, validate mã bản sách |
| | `com.ptit.library.service.BookCatalogService` | Nghiệp vụ quản lý Thể loại, Tác giả, Đầu sách, Bản sách & Tra cứu sách (lưu trong `ServletContext` với key `BookCatalogService.ATTR_NAME = "bookCatalogService"`) |
| | `com.ptit.library.service.AuthService` | Đăng nhập (`ServletContext` key `"authService"`) |
| | `com.ptit.library.service.RegistrationService` | Độc giả tự đăng ký (`ServletContext` key `"registrationService"`) |
| **Web / Servlet** | `com.ptit.library.web.StaffCatalogServlet` | `GET/POST /staff/catalog` (`UI-07`) |
| | `com.ptit.library.web.StaffBookServlet` | `GET/POST /staff/books` (`UI-08`) |
| | `com.ptit.library.web.StaffBookCopyServlet` | `GET/POST /staff/books/copies` (`UI-09`) |
| | `com.ptit.library.web.ReaderBookServlet` | `GET /reader/books` (`UI-15`) & `GET /reader/books?id=...` (`UI-16`) |
| **JSP** | `WEB-INF/jsp/staff-catalog.jsp` | Giao diện Quản lý Thể loại & Tác giả (`UI-07`) |
| | `WEB-INF/jsp/staff-books.jsp` | Giao diện Quản lý Đầu sách (`UI-08`) |
| | `WEB-INF/jsp/staff-book-copies.jsp` | Giao diện Quản lý Bản sách (`UI-09`) |
| | `WEB-INF/jsp/reader-books.jsp` | Giao diện Tra cứu sách Độc giả (`UI-15`) |
| | `WEB-INF/jsp/reader-book-detail.jsp` | Giao diện Chi tiết đầu sách Độc giả (`UI-16`) |

---

## 3. Phân chia chi tiết 4 Module còn lại & Hướng dẫn cho người code tiếp / AI Agent

Để không xảy ra xung đột hay trùng lặp code, 4 thành viên còn lại (hoặc AI Agent hỗ trợ từng thành viên) thực hiện đúng phạm vi dưới đây:

### Thành viên 2 — Module Quản lý Độc giả, Duyệt thẻ & Quản lý Nhân viên
- **Phạm vi SRS:** `UC-04`, `UC-06`, `UC-07`, `UC-08` (`FR-AUTH-05`, `FR-READER-01..05, 08..10`).
- **Màn hình & URL cần tạo:**
  - `UI-05`: `/admin/staff-users` (`AdminStaffUserServlet` + `WEB-INF/jsp/admin-staff-users.jsp`) — Chỉ `ADMIN` (`RoleGuard.require(req, resp, "ADMIN")`): Tạo, sửa, khóa/mở khóa tài khoản `ADMIN` và `LIBRARIAN`. Không tự khóa chính mình (`auth.userId()`).
  - `UI-10`: `/staff/readers/pending` (`StaffReaderApprovalServlet` + `WEB-INF/jsp/staff-reader-pending.jsp`) — Duyệt `PENDING → ACTIVE`, từ chối `PENDING → REJECTED`, duyệt lại `REJECTED → ACTIVE` hoặc `REJECTED → PENDING`.
  - `UI-11`: `/staff/readers` (`StaffReaderServlet` + `WEB-INF/jsp/staff-readers.jsp`) — Tìm kiếm độc giả theo mã `DGxxxxxx` hoặc họ tên; tạo hộ độc giả tại quầy (`users` + `readers` trạng thái `ACTIVE` trong 1 transaction, dùng lại `ReaderDao.nextCode`); sửa họ tên/email/SĐT; khóa/mở khóa thẻ (`ACTIVE ↔ LOCKED`).
- **Lưu ý tái sử dụng:** Mở rộng thêm hàm vào `UserDao` và `ReaderDao` thay vì tạo DAO trùng tên.

### Thành viên 3 — Module Giao dịch Mượn sách & Trả sách tại quầy
- **Phạm vi SRS:** `UC-11`, `UC-12` (`FR-LOAN-01..07`, `FR-RETURN-01..03, 07`).
- **Màn hình & URL cần tạo:**
  - `UI-12`: `/staff/loans/new` (`StaffLoanCreateServlet` + `WEB-INF/jsp/staff-loan-create.jsp`) — Lập phiếu mượn:
    - Kiểm tra thẻ độc giả phải `ACTIVE`.
    - Đếm số bản đang mượn (`loan_details.status = 'BORROWED'`) + số bản mới ≤ `max_borrow_count` (đọc từ bảng `system_settings`).
    - Mỗi mã bản sách phải tồn tại, `book_copies.status = 'AVAILABLE'` và đầu sách `books.status = 'ACTIVE'`.
    - Sinh mã phiếu `PM` + 6 chữ số (`PM000001`), hạn trả `due_date = borrowed_on + loan_period_days` (theo múi giờ `Asia/Ho_Chi_Minh`).
    - Chạy trong **1 transaction**: insert `loans` (`OPEN`), insert `loan_details` (`BORROWED`), update `book_copies` → `BORROWED`.
  - `UI-13`: `/staff/loans` (`StaffLoanReturnServlet` + `WEB-INF/jsp/staff-loans.jsp`) — Tra cứu phiếu mượn theo mã phiếu / độc giả, xem chi tiết phiếu và xác nhận trả từng bản (`loan_details.status = 'RETURNED'`, `returned_at = hôm nay`, `book_copies.status = 'AVAILABLE'`; khi trả hết mọi bản trên phiếu thì đổi `loans.status = 'COMPLETED'`).
- **Class mới nên tạo:** `Loan.java`, `LoanDetail.java`, `LoanDao.java`, `SettingDao.java`, `LoanService.java`.

### Thành viên 4 — Module Theo dõi Quá hạn, Hồ sơ/Lịch sử Độc giả & Dashboard
- **Phạm vi SRS:** `UC-13`, `UC-15`, `UC-16` (`FR-RETURN-04, 06`, `FR-READER-06`, `FR-SEARCH-05..06`, `FR-REPORT-01..06`).
- **Màn hình & URL cần tạo:**
  - `UI-14`: `/staff/overdue` (`StaffOverdueServlet` + `WEB-INF/jsp/staff-overdue.jsp`) — Danh sách các `loan_details` đang `BORROWED` có `due_date < CURDATE()`, hiển thị số ngày quá hạn (không tính tiền phạt).
  - `UI-17`: `/reader/my-loans` (`ReaderLoanHistoryServlet` + `WEB-INF/jsp/reader-my-loans.jsp`) — Độc giả xem thông tin hồ sơ, danh sách sách đang mượn (`BORROWED`) và lịch sử mượn/trả của **chính mình** (lấy `auth.readerId()` từ `SessionAuth.get(req.getSession(false))`, tuyệt đối không lấy `readerId` từ query param để chống lỗi bảo mật IDOR).
  - `UI-04`: `/staff/dashboard` (hoặc tích hợp trực tiếp số liệu lên `StaffHomeServlet` `/staff/home` và `staff-home.jsp`) — Thống kê tổng đầu sách, tổng bản sách, độc giả `ACTIVE`, số bản đang `BORROWED`, số chi tiết quá hạn, top sách mượn nhiều nhất và lọc theo khoảng ngày mượn.

### Thành viên 5 (Làm tài liệu) — Đổi mật khẩu & Cấu hình hệ thống
- **Phạm vi SRS:** `UC-03`, `UC-17` (`FR-AUTH-08`, `FR-CFG-01..03`).
- **Màn hình & URL cần tạo:**
  - `UI-03`: `/account/password` (`ChangePasswordServlet` + `WEB-INF/jsp/change-password.jsp`) — Cho phép mọi user đã đăng nhập (`ADMIN`, `LIBRARIAN`, `READER`) đổi mật khẩu của chính mình (cần thêm `/account` vào `RoleRules.allowedRoles` cho cả 3 role).
  - `UI-06`: `/admin/settings` (`AdminSettingsServlet` + `WEB-INF/jsp/admin-settings.jsp`) — Chỉ `ADMIN`: Xem và cập nhật 2 khóa `max_borrow_count` và `loan_period_days` (số nguyên ≥ 1) trong bảng `system_settings`.

---

## 4. Quy tắc bắt buộc cho AI Agent & Thành viên khi code tiếp

1. **Cách kiểm tra phân quyền trong Servlet:**
   - Mọi URL `/admin/*` đã được `AuthFilter` + `RoleRules` chặn chỉ cho `ADMIN`. Trong Servlet vẫn gọi:
     ```java
     if (!RoleGuard.require(req, resp, "ADMIN")) return;
     ```
   - Mọi URL `/staff/*` cho phép `"ADMIN", "LIBRARIAN"`:
     ```java
     if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) return;
     ```
   - Mọi URL `/reader/*` cho phép `"READER"`:
     ```java
     if (!RoleGuard.require(req, resp, "READER")) return;
     ```
   - Nếu mở prefix URL mới ngoài `/admin`, `/staff`, `/reader`, `/home` (ví dụ `/account/password`), **bắt buộc** khai báo thêm trong `com.ptit.library.web.RoleRules.allowedRoles(String path)`.
2. **Cách lấy Connection & khởi tạo Service:**
   - Khởi tạo Service trong `AppContextListener.contextInitialized` sử dụng `ConnectionFactory` có sẵn, đặt vào `ServletContext`.
   - Mọi câu lệnh SQL bắt buộc dùng `PreparedStatement` (`NFR-05`).
3. **Các file dùng chung cần chú ý khi merge Git:**
   - `src/main/java/com/ptit/library/web/AppContextListener.java` (chỉ thêm dòng `setAttribute` mới ở cuối khối `try`).
   - `src/main/webapp/WEB-INF/web.xml` (chỉ thêm cặp `<servlet>` và `<servlet-mapping>` mới trước `<session-config>`).
   - `src/main/webapp/WEB-INF/jsp/staff-home.jsp` và `reader-home.jsp` (thêm link điều hướng vào thanh `<nav>` và card chức năng).
   - `src/test/java/com/ptit/library/web/WebXmlTest.java` (vẫn giữ nguyên các `assertTrue` cũ khi thêm test cho servlet mới).
4. **Không commit file cấu hình máy cá nhân:**
   - Tuyệt đối không commit `.env` hoặc `src/main/resources/db.properties` (đã có trong `.gitignore`).
