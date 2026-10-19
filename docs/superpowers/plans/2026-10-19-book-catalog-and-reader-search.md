# Book Catalog & Reader Search Implementation Plan (Completed)

**Goal:** Triển khai trọn vẹn cụm Quản lý Thể loại, Tác giả, Đầu sách, Bản sách vật lý cho Thủ thư/Admin (`UC-09`, `UC-10`) và Tra cứu / Xem chi tiết đầu sách cho Độc giả (`UC-14`) theo đúng SRS v1.1.

**Spec Reference:** `docs/SRS_Quan_Ly_Thu_Vien_v1.1.md` mục 2.2 (`FR-BOOK-01..08`), 2.3 (`FR-CAT-01..04`), 2.7 (`FR-SEARCH-01..04`), 6.1/6.3 (`UC-09`, `UC-10`, `UC-14`), 7 (`UI-07`, `UI-08`, `UI-09`, `UI-15`, `UI-16`).

## File Structure Implemented

```text
src/main/java/com/ptit/library/model/Category.java
src/main/java/com/ptit/library/model/Author.java
src/main/java/com/ptit/library/model/BookItem.java
src/main/java/com/ptit/library/model/BookCopy.java
src/main/java/com/ptit/library/dao/CategoryDao.java
src/main/java/com/ptit/library/dao/AuthorDao.java
src/main/java/com/ptit/library/dao/BookDao.java
src/main/java/com/ptit/library/dao/BookCopyDao.java
src/main/java/com/ptit/library/service/BookValidator.java
src/main/java/com/ptit/library/service/BookCatalogService.java
src/main/java/com/ptit/library/web/StaffCatalogServlet.java
src/main/java/com/ptit/library/web/StaffBookServlet.java
src/main/java/com/ptit/library/web/StaffBookCopyServlet.java
src/main/java/com/ptit/library/web/ReaderBookServlet.java
src/main/webapp/WEB-INF/jsp/staff-catalog.jsp
src/main/webapp/WEB-INF/jsp/staff-books.jsp
src/main/webapp/WEB-INF/jsp/staff-book-copies.jsp
src/main/webapp/WEB-INF/jsp/reader-books.jsp
src/main/webapp/WEB-INF/jsp/reader-book-detail.jsp
src/test/java/com/ptit/library/service/BookValidatorTest.java
```

## Completed Checklist

- [x] **Task 1: Models & BookValidator** (`Category`, `Author`, `BookItem`, `BookCopy`, `BookValidator` + `BookValidatorTest`)
- [x] **Task 2: JDBC DAOs** (`CategoryDao`, `AuthorDao`, `BookDao`, `BookCopyDao`) sử dụng `PreparedStatement`
- [x] **Task 3: BookCatalogService** quản lý transaction N-N `book_authors`, kiểm tra lịch sử `loan_details` trước khi xóa vật lý, kiểm tra trạng thái `AVAILABLE`/`BORROWED`/`WITHDRAWN`
- [x] **Task 4: Staff & Reader Servlets + JSPs** (`UI-07`, `UI-08`, `UI-09`, `UI-15`, `UI-16`) và đăng ký trong `web.xml` + `AppContextListener`
