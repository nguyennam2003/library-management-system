# TÀI LIỆU TIẾN ĐỘ DỰ ÁN, KIẾN TRÚC HIỆN TẠI & PHÂN CHIA MODULE (NHÓM 5 THÀNH VIÊN)

> **Dành cho thành viên trong nhóm & AI Coding Agent:**  
> Vui lòng đọc kỹ tài liệu này trước khi bắt đầu code các module tiếp theo để **không tạo trùng class/DAO/Servlet**, **không ghi đè logic đã có** và **tránh xung đột (merge conflict)** trên các file dùng chung.

---

## 1. Tổng quan trạng thái dự án hiện tại

Dự án **Hệ thống Quản lý Thư viện** được xây dựng theo đặc tả `docs/SRS_Quan_Ly_Thu_Vien_v1.1.md` với kiến trúc đơn giản phù hợp đồ án sinh viên:
- **Stack:** Java 17 + Servlet 4.0 (`javax.servlet`) + JSP 2.3 (Scriptlet `<% ... %>`, không dùng JSTL) + JDBC thuần (`PreparedStatement`) + MySQL 8.0 + Bootstrap 5.3.3 CDN.
- **Không sử dụng:** Spring, Hibernate/JPA, Lombok hay thư viện frontend phức tạp.

### Bảng phân chia 5 thành viên & Trạng thái hiện tại trên nhánh `main`

| Thành viên | Phụ trách | Use Case / FR (SRS v1.1) | Màn hình (UI) | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| **Thành viên 1** | **Tài liệu SRS + Base Project + Đăng nhập / Đăng xuất / Tự đăng ký** | `UC-01`, `UC-02`, `UC-05`<br>(`FR-AUTH-01..04, 06, 07, 09, 10`, `FR-READER-07`) | `UI-01` (`/login`), `UI-02` (`/register`) | **ĐÃ XONG** |
| **Thành viên 2** | **Quản lý Kho Sách, Thể loại, Tác giả, Bản sách + Trọn bộ Cổng Độc giả & Đổi mật khẩu** | `UC-03`, `UC-09`, `UC-10`, `UC-14`, `UC-15`<br>(`FR-AUTH-08`, `FR-CAT-01..04`, `FR-BOOK-01..08`, `FR-SEARCH-01..06`, `FR-READER-06`) | **7 màn hình:**<br>`UI-03` (`/account/password`), `UI-07` (`/staff/catalog`), `UI-08` (`/staff/books`), `UI-09` (`/staff/books/copies`), `UI-15` (`/reader/books`), `UI-16` (`/reader/books?id=...`), `UI-17` (`/reader/my-loans`) | **ĐÃ XONG** |
| **Thành viên 3** | **Quản lý Độc giả, Duyệt thẻ & Tạo hộ độc giả tại quầy** | `UC-06`, `UC-07`, `UC-08`<br>(`FR-READER-01..05, 08..10`) | **2 màn hình:**<br>`UI-10` (`/staff/readers/pending`), `UI-11` (`/staff/readers`) | *Chờ triển khai* |
| **Thành viên 4** | **Giao dịch Mượn sách, Trả sách & Theo dõi Quá hạn** | `UC-11`, `UC-12`, `UC-13`<br>(`FR-LOAN-01..07`, `FR-RETURN-01..04, 06, 07`) | **3 màn hình:**<br>`UI-12` (`/staff/loans/new`), `UI-13` (`/staff/loans`), `UI-14` (`/staff/overdue`) | *Chờ triển khai* |
| **Thành viên 5** | **Quản trị Tài khoản Nhân viên, Cấu hình hệ thống & Dashboard Báo cáo** | `UC-04`, `UC-16`, `UC-17`<br>(`FR-AUTH-05`, `FR-REPORT-01..06`, `FR-CFG-01..03`) | **3 màn hình:**<br>`UI-04` (`/staff/dashboard`), `UI-05` (`/admin/staff-users`), `UI-06` (`/admin/settings`) | *Chờ triển khai* |

---

## 2. Chi tiết những gì Thành viên 2 (Kho Sách + Cổng Độc giả + Đổi mật khẩu) đã triển khai

### 2.1. Các chức năng nghiệp vụ đã hoàn thiện (7 màn hình: `UI-03, UI-07, UI-08, UI-09, UI-15, UI-16, UI-17`)
1. **Đổi mật khẩu cá nhân (`UC-03` — `FR-AUTH-08` — `UI-03` tại `/account/password`)**:
   - Dành cho mọi tài khoản đã đăng nhập (`ADMIN`, `LIBRARIAN`, `READER`).
   - Kiểm tra mật khẩu hiện tại bằng BCrypt (`PasswordHasher.check`), mật khẩu mới tối thiểu 8 ký tự và khớp xác nhận trước khi cập nhật hash mới.
2. **Quản lý Thể loại & Tác giả (`UC-09` — `FR-CAT-01..04` — `UI-07` tại `/staff/catalog`)**:
   - Thêm mới, cập nhật tên thể loại (`categories`, kiểm tra không rỗng, không trùng tên) và tác giả (`authors`).
   - Xóa thể loại / tác giả: Tự động đếm số đầu sách đang liên kết (`books.category_id` và `book_authors.author_id`), **chặn xóa nếu đang có đầu sách sử dụng** đúng quy tắc `FR-CAT-04`.
3. **Quản lý Đầu sách (`UC-10` — `FR-BOOK-01..04, 06..08` — `UI-08` tại `/staff/books`)**:
   - Thêm mới & cập nhật đầu sách (`books`): tên sách, ISBN (tùy chọn — cho phép bỏ trống hoặc nhập ISBN-10 / ISBN-13 có dấu gạch ngang `-`, hệ thống tự chuẩn hóa bỏ dấu gạch và kiểm tra unique), chọn 1 thể loại, chọn **một hoặc nhiều tác giả** (`book_authors` trong cùng JDBC transaction), nhà xuất bản, năm xuất bản.
   - Ngừng lưu hành / Mở lại lưu hành đầu sách (`ACTIVE` ↔ `WITHDRAWN`). Khi đầu sách `WITHDRAWN`, số bản sẵn sàng cho mượn trả về `0`.
   - Xóa vật lý đầu sách: Chỉ cho phép xóa khi đầu sách **chưa từng có bản sách nào phát sinh lịch sử trong `loan_details`** và không có bản đang `BORROWED`.
   - Tìm kiếm kết hợp (`FR-BOOK-06`): tìm theo tên sách, tên tác giả hoặc mã ISBN; lọc theo thể loại và chỉ hiển thị đầu sách còn bản `AVAILABLE` (`FR-BOOK-07`); phân trang (`FR-BOOK-08`).
4. **Quản lý Bản sách vật lý (`UC-10` — `FR-BOOK-05` — `UI-09` tại `/staff/books/copies?bookId=...`)**:
   - Xem thông tin đầu sách và danh sách tất cả các bản sách vật lý (`book_copies`) thuộc đầu sách đó.
   - Thêm bản sách mới bằng mã định danh riêng (`code` unique, trạng thái mặc định `AVAILABLE`).
   - Ngừng lưu hành bản sách (`AVAILABLE` → `WITHDRAWN`). **Chặn tuyệt đối việc chuyển bản đang `BORROWED` sang `WITHDRAWN`**.
   - Mở lại lưu hành bản sách (`WITHDRAWN` → `AVAILABLE`).
   - Xóa vật lý bản sách: Chỉ cho phép khi bản sách **chưa từng xuất hiện trong `loan_details`** và không ở trạng thái `BORROWED`.
5. **Tra cứu sách & Xem chi tiết dành cho Độc giả (`UC-14` — `FR-SEARCH-01..04` — `UI-15, UI-16` tại `/reader/books`)**:
   - Danh sách tra cứu (`UI-15` tại `/reader/books`): Chỉ hiển thị các đầu sách đang lưu hành (`books.status = 'ACTIVE'`), hỗ trợ tìm kiếm theo tên/tác giả/ISBN, lọc theo thể loại, lọc còn bản sẵn sàng và phân trang.
   - Xem chi tiết đầu sách (`UI-16` tại `/reader/books?id={bookId}`): Hiển thị đầy đủ thông tin ấn phẩm, số lượng bản `AVAILABLE` / tổng số bản, và bảng danh sách mã từng bản sách vật lý tại thư viện.
6. **Xem Hồ sơ, Sách đang mượn & Lịch sử mượn/trả của Độc giả (`UC-15` — `FR-SEARCH-05..06, FR-READER-06` — `UI-17` tại `/reader/my-loans`)**:
   - Hiển thị thông tin thẻ độc giả (`code`, họ tên, email, SĐT, trạng thái thẻ `PENDING/ACTIVE/LOCKED`).
   - Hiển thị danh sách các cuốn sách đang mượn (`loan_details.status = 'BORROWED'`) kèm hạn trả (`due_date`) và cảnh báo quá hạn nếu ngày hiện tại (theo `Asia/Ho_Chi_Minh`) sau `due_date`.
   - Hiển thị toàn bộ lịch sử mượn/trả của độc giả.
   - **Bảo mật chống IDOR:** Tự động lấy `readerId` từ `HttpSession`; nếu người dùng truyền `?readerId=...` hoặc `?loanId=...` của độc giả khác trên URL sẽ trả về **403 Forbidden** (`forbidden.jsp`).

### 2.2. Danh mục File đã có trong codebase (Không tạo trùng!)

| Phân lớp | Class / File | Trách nhiệm chính |
| :--- | :--- | :--- |
| **Model** | `com.ptit.library.model.Category` | Record `(id, name, bookCount)` |
| | `com.ptit.library.model.Author` | Record `(id, name, bookCount)` |
| | `com.ptit.library.model.BookItem` | Record đầu sách kèm `authorIds`, `authorNames`, `totalCopies`, `availableCopies`, `hasLoanHistory` |
| | `com.ptit.library.model.BookCopy` | Record bản vật lý `(id, bookId, code, status, hasLoanHistory)` |
| | `com.ptit.library.model.ReaderLoanItem` | Record chi tiết mượn/trả của độc giả kèm cờ `overdue` |
| | `com.ptit.library.model.UserAccount` | Record tài khoản `users` + `roleCode` |
| | `com.ptit.library.model.ReaderProfile` | Record hồ sơ độc giả `readers` |
| | `com.ptit.library.model.AuthSession` | Thông tin lưu trong `HttpSession` (`userId`, `username`, `role`, `readerId`, `readerStatus`, `readerCode`) |
| **DAO** | `com.ptit.library.dao.CategoryDao` | CRUD bảng `categories` |
| | `com.ptit.library.dao.AuthorDao` | CRUD bảng `authors` |
| | `com.ptit.library.dao.BookDao` | Tìm kiếm, lọc, phân trang, CRUD bảng `books` và `book_authors` |
| | `com.ptit.library.dao.BookCopyDao` | CRUD bảng `book_copies` |
| | `com.ptit.library.dao.ReaderLoanDao` | Truy vấn danh sách sách đang mượn & lịch sử mượn trả của độc giả |
| | `com.ptit.library.dao.UserDao` | Truy vấn/thêm mới bảng `users`, `findById`, `updatePasswordHash` |
| | `com.ptit.library.dao.ReaderDao` | Truy vấn/thêm mới bảng `readers`, sinh mã `DGxxxxxx` (`ReaderCodes`) |
| **Service** | `com.ptit.library.service.BookValidator` | Validate đầu sách, chuẩn hóa ISBN-10/ISBN-13, validate mã bản sách |
| | `com.ptit.library.service.BookCatalogService` | Nghiệp vụ quản lý Thể loại, Tác giả, Đầu sách, Bản sách & Tra cứu sách (`ServletContext` key `"bookCatalogService"`) |
| | `com.ptit.library.service.ReaderSelfService` | Nghiệp vụ hồ sơ & lịch sử mượn trả của độc giả (`ServletContext` key `"readerSelfService"`) |
| | `com.ptit.library.service.AuthService` | Đăng nhập & Đổi mật khẩu (`ServletContext` key `"authService"`) |
| | `com.ptit.library.service.RegistrationService` | Độc giả tự đăng ký (`ServletContext` key `"registrationService"`) |
| **Web / Servlet** | `com.ptit.library.web.ChangePasswordServlet` | `GET/POST /account/password` (`UI-03`) |
| | `com.ptit.library.web.StaffCatalogServlet` | `GET/POST /staff/catalog` (`UI-07`) |
| | `com.ptit.library.web.StaffBookServlet` | `GET/POST /staff/books` (`UI-08`) |
| | `com.ptit.library.web.StaffBookCopyServlet` | `GET/POST /staff/books/copies` (`UI-09`) |
| | `com.ptit.library.web.ReaderBookServlet` | `GET /reader/books` (`UI-15`) & `GET /reader/books?id=...` (`UI-16`) |
| | `com.ptit.library.web.ReaderLoanServlet` | `GET /reader/my-loans` (`UI-17`) |

---

## 3. Phân chia chi tiết 3 Module còn lại cho Thành viên 3, 4, 5 & AI Agent

### Thành viên 3 — Module Quản lý Độc giả, Duyệt thẻ & Tạo hộ tại quầy
- **Phạm vi SRS:** `UC-06`, `UC-07`, `UC-08` (`FR-READER-01..05, 08..10`).
- **Màn hình & URL cần tạo:**
  - `UI-10`: `/staff/readers/pending` (`StaffReaderApprovalServlet` + `WEB-INF/jsp/staff-reader-pending.jsp`) — Hàng đợi hồ sơ `PENDING`: Duyệt `PENDING → ACTIVE`, từ chối `PENDING → REJECTED`, duyệt lại `REJECTED → ACTIVE` hoặc mở lại `REJECTED → PENDING`.
  - `UI-11`: `/staff/readers` (`StaffReaderServlet` + `WEB-INF/jsp/staff-readers.jsp`) — Tìm kiếm độc giả theo mã `DGxxxxxx` hoặc họ tên; tạo hộ độc giả tại quầy (`users` + `readers` trạng thái `ACTIVE` trong 1 transaction, dùng lại `ReaderDao.nextCode`); sửa họ tên/email/SĐT; khóa/mở khóa thẻ (`ACTIVE ↔ LOCKED`).
- **Lưu ý tái sử dụng:** Mở rộng thêm hàm vào `ReaderDao` thay vì tạo DAO trùng tên.

### Thành viên 4 — Module Giao dịch Mượn sách, Trả sách & Theo dõi Quá hạn
- **Phạm vi SRS:** `UC-11`, `UC-12`, `UC-13` (`FR-LOAN-01..07`, `FR-RETURN-01..04, 06, 07`).
- **Màn hình & URL cần tạo:**
  - `UI-12`: `/staff/loans/new` (`StaffLoanCreateServlet` + `WEB-INF/jsp/staff-loan-create.jsp`) — Lập phiếu mượn nhiều bản sách (kiểm tra thẻ `ACTIVE`, đếm đang mượn + phiếu mới ≤ `max_borrow_count`, bản sách `AVAILABLE` thuộc đầu sách `ACTIVE`, sinh mã `PMxxxxxx`, chạy trong 1 transaction).
  - `UI-13`: `/staff/loans` (`StaffLoanReturnServlet` + `WEB-INF/jsp/staff-loans.jsp`) — Tra cứu phiếu mượn theo mã phiếu / độc giả, xác nhận trả từng bản (`RETURNED`, `returned_at = hôm nay`, bản sách về `AVAILABLE`, hết bản đang mượn thì phiếu `COMPLETED`).
  - `UI-14`: `/staff/overdue` (`StaffOverdueServlet` + `WEB-INF/jsp/staff-overdue.jsp`) — Danh sách các `loan_details` đang `BORROWED` có `due_date < CURDATE()`, hiển thị số ngày quá hạn (không tính tiền phạt).

### Thành viên 5 — Module Quản trị Nhân viên, Cấu hình hệ thống & Dashboard Báo cáo
- **Phạm vi SRS:** `UC-04`, `UC-16`, `UC-17` (`FR-AUTH-05`, `FR-REPORT-01..06`, `FR-CFG-01..03`).
- **Màn hình & URL cần tạo:**
  - `UI-05`: `/admin/staff-users` (`AdminStaffUserServlet` + `WEB-INF/jsp/admin-staff-users.jsp`) — Chỉ `ADMIN`: Tạo, sửa, khóa/mở khóa tài khoản `ADMIN` và `LIBRARIAN` (không tự khóa chính mình).
  - `UI-06`: `/admin/settings` (`AdminSettingsServlet` + `WEB-INF/jsp/admin-settings.jsp`) — Chỉ `ADMIN`: Xem và sửa `max_borrow_count` và `loan_period_days` (số nguyên ≥ 1) trong bảng `system_settings`.
  - `UI-04`: `/staff/dashboard` (hoặc hiển thị trực tiếp trên `StaffHomeServlet` `/staff/home`) — Thống kê tổng đầu sách, tổng bản sách, độc giả `ACTIVE`, số bản đang `BORROWED`, số chi tiết quá hạn, top sách mượn nhiều nhất và lọc theo khoảng ngày mượn.

---

## 4. Quy tắc bắt buộc cho AI Agent & Thành viên khi code tiếp

1. **Cách kiểm tra phân quyền trong Servlet:**
   - URL `/admin/*`: `if (!RoleGuard.require(req, resp, "ADMIN")) return;`
   - URL `/staff/*`: `if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) return;`
   - URL `/reader/*`: `if (!RoleGuard.require(req, resp, "READER")) return;`
2. **Các file dùng chung cần chú ý khi merge Git:**
   - `src/main/java/com/ptit/library/web/AppContextListener.java` (chỉ thêm dòng `setAttribute` mới ở cuối khối `try`).
   - `src/main/webapp/WEB-INF/web.xml` (chỉ thêm cặp `<servlet>` và `<servlet-mapping>` mới trước `<session-config>`).
   - `src/main/webapp/WEB-INF/jsp/staff-home.jsp` (thêm link điều hướng vào thanh `<nav>` và card chức năng).
   - `src/test/java/com/ptit/library/web/WebXmlTest.java` (giữ nguyên các `assertTrue` cũ khi thêm test cho servlet mới).
3. **Không commit file cấu hình máy cá nhân:**
   - Tuyệt đối không commit `.env` hoặc `src/main/resources/db.properties` (đã có trong `.gitignore`).
