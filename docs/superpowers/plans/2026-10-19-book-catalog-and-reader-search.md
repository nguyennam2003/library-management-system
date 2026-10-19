# Book Catalog, Reader Portal & Password Change Implementation Plan (Completed)

**Goal:** Triển khai trọn vẹn cụm Quản lý Thể loại, Tác giả, Đầu sách, Bản sách vật lý cho Thủ thư/Admin (`UC-09`, `UC-10`), trọn bộ Cổng Độc giả (`UC-14`, `UC-15`: Tra cứu sách, Chi tiết sách, Hồ sơ cá nhân, Sách đang mượn & Lịch sử mượn trả) và Đổi mật khẩu cá nhân (`UC-03`) theo đúng SRS v1.1.

**Spec Reference:** `docs/SRS_Quan_Ly_Thu_Vien_v1.1.md` mục 2.1 (`FR-AUTH-08`), 2.2 (`FR-BOOK-01..08`), 2.3 (`FR-CAT-01..04`), 2.4 (`FR-READER-06`), 2.7 (`FR-SEARCH-01..06`), 6.1–6.3 (`UC-03`, `UC-09`, `UC-10`, `UC-14`, `UC-15`), 7 (`UI-03`, `UI-07`, `UI-08`, `UI-09`, `UI-15`, `UI-16`, `UI-17`).

## File Structure Implemented

```text
src/main/java/com/ptit/library/model/Category.java
src/main/java/com/ptit/library/model/Author.java
src/main/java/com/ptit/library/model/BookItem.java
src/main/java/com/ptit/library/model/BookCopy.java
src/main/java/com/ptit/library/model/ReaderLoanItem.java
src/main/java/com/ptit/library/dao/CategoryDao.java
src/main/java/com/ptit/library/dao/AuthorDao.java
src/main/java/com/ptit/library/dao/BookDao.java
src/main/java/com/ptit/library/dao/BookCopyDao.java
src/main/java/com/ptit/library/dao/ReaderLoanDao.java
src/main/java/com/ptit/library/service/BookValidator.java
src/main/java/com/ptit/library/service/BookCatalogService.java
src/main/java/com/ptit/library/service/ReaderSelfService.java
src/main/java/com/ptit/library/web/ChangePasswordServlet.java
src/main/java/com/ptit/library/web/StaffCatalogServlet.java
src/main/java/com/ptit/library/web/StaffBookServlet.java
src/main/java/com/ptit/library/web/StaffBookCopyServlet.java
src/main/java/com/ptit/library/web/ReaderBookServlet.java
src/main/java/com/ptit/library/web/ReaderLoanServlet.java
src/main/webapp/WEB-INF/jsp/change-password.jsp
src/main/webapp/WEB-INF/jsp/staff-catalog.jsp
src/main/webapp/WEB-INF/jsp/staff-books.jsp
src/main/webapp/WEB-INF/jsp/staff-book-copies.jsp
src/main/webapp/WEB-INF/jsp/reader-books.jsp
src/main/webapp/WEB-INF/jsp/reader-book-detail.jsp
src/main/webapp/WEB-INF/jsp/reader-my-loans.jsp
src/test/java/com/ptit/library/service/BookValidatorTest.java
```

## Completed Checklist

- [x] **Task 1: Models & BookValidator** (`Category`, `Author`, `BookItem`, `BookCopy`, `ReaderLoanItem`, `BookValidator` + `BookValidatorTest`)
- [x] **Task 2: JDBC DAOs** (`CategoryDao`, `AuthorDao`, `BookDao`, `BookCopyDao`, `ReaderLoanDao`, cập nhật `UserDao`) sử dụng `PreparedStatement`
- [x] **Task 3: Services** (`BookCatalogService`, `ReaderSelfService`, bổ sung `AuthService.changePassword`)
- [x] **Task 4: Staff & Reader Servlets + JSPs** (`UI-03`, `UI-07`, `UI-08`, `UI-09`, `UI-15`, `UI-16`, `UI-17`), chống IDOR trên `/reader/my-loans`, đăng ký trong `web.xml`, `RoleRules`, `AppContextListener`
