# Auth Login Register Logout Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Cho phép khách đăng ký độc giả, mọi vai trò đăng nhập/đăng xuất theo SRS UC-01, UC-02, UC-05; session 30 phút chứa `userId` và `role`.

**Architecture:** Tách logic thuần (`CredentialVerifier`, `RegistrationValidator`) để TDD không cần MySQL. Servlet gọi `AuthService` / `RegistrationService` qua JDBC `ConnectionFactory` đã có. `AuthFilter` chặn URL trừ login/register/health. Sau đăng nhập: Admin/Librarian vào trang nhân viên, Reader vào trang độc giả (chưa làm duyệt thẻ, CRUD sách, mượn/trả).

**Tech Stack:** Servlet 4.0, JSP, JDBC, jBCrypt, JUnit 5, Bootstrap 5 CDN, MySQL 8.

## Global Constraints

- JDK 17; Servlet 4.0 / JSP 2.3; Tomcat 9; MySQL 8 utf8mb4; không Spring.
- Session idle timeout 30 phút (đã có trong `web.xml`).
- Mật khẩu BCrypt cost ≥ 10; không plaintext.
- Username 4–32 `[a-zA-Z0-9._]`; email unique đúng định dạng; mật khẩu ≥ 8 ký tự.
- Đăng nhập sai / user nhân viên bị khóa: thông báo chung, không lộ username hay mật khẩu (NFR-08).
- Reader REJECTED: không đăng nhập; thông báo tài khoản chưa được duyệt hoặc đã bị từ chối.
- Reader PENDING/ACTIVE/LOCKED: đăng nhập được.
- Đăng ký: `users` role Reader + `readers` PENDING, mã `DGxxxxxx`, cùng transaction; rollback nếu lỗi.
- Giao diện tiếng Việt, UTF-8.
- Không quên mật khẩu, không đổi mật khẩu, không duyệt thẻ, không tạo hộ tại quầy (plan sau).

**Spec:** `docs/superpowers/specs/2026-10-14-srs-quan-ly-thu-vien-v1.1-design.md` mục 2.1, 2.4 (FR-AUTH-01..04, 06, 07, 09, 10; FR-READER-07), UC-01, UC-02, UC-05.

---

## File structure

```text
src/main/java/com/ptit/library/model/RoleCode.java
src/main/java/com/ptit/library/model/ReaderStatus.java
src/main/java/com/ptit/library/model/UserAccount.java
src/main/java/com/ptit/library/model/ReaderProfile.java
src/main/java/com/ptit/library/model/AuthSession.java
src/main/java/com/ptit/library/auth/PasswordHasher.java
src/main/java/com/ptit/library/auth/CredentialVerifier.java
src/main/java/com/ptit/library/auth/AuthFailure.java
src/main/java/com/ptit/library/auth/RegistrationValidator.java
src/main/java/com/ptit/library/auth/RegistrationInput.java
src/main/java/com/ptit/library/dao/UserDao.java
src/main/java/com/ptit/library/dao/ReaderDao.java
src/main/java/com/ptit/library/service/AuthService.java
src/main/java/com/ptit/library/service/RegistrationService.java
src/main/java/com/ptit/library/web/AuthFilter.java
src/main/java/com/ptit/library/web/LoginServlet.java
src/main/java/com/ptit/library/web/LogoutServlet.java
src/main/java/com/ptit/library/web/RegisterServlet.java
src/main/java/com/ptit/library/web/HomeServlet.java
src/main/webapp/WEB-INF/jsp/login.jsp
src/main/webapp/WEB-INF/jsp/register.jsp
src/main/webapp/WEB-INF/jsp/staff-home.jsp
src/main/webapp/WEB-INF/jsp/reader-home.jsp
src/test/java/com/ptit/library/auth/CredentialVerifierTest.java
src/test/java/com/ptit/library/auth/RegistrationValidatorTest.java
src/test/java/com/ptit/library/auth/PasswordHasherTest.java
```

Session keys (đặt trên `HttpSession`): `userId` (Long), `username` (String), `role` (String: ADMIN|LIBRARIAN|READER), `readerId` (Long, chỉ Reader), `readerStatus` (String, chỉ Reader), `readerCode` (String, chỉ Reader).

Thông báo login thất bại mặc định: `Tên đăng nhập hoặc mật khẩu không đúng.`  
REJECTED: `Tài khoản chưa được duyệt hoặc đã bị từ chối.`

---

### Task 1: PasswordHasher + CredentialVerifier

**Files:** create model enums/records, `PasswordHasher`, `CredentialVerifier`, `AuthFailure`, tests.

**Interfaces:**
- `PasswordHasher.hash(String raw)` → BCrypt gensalt(10); `check(String raw, String hash)` → boolean
- `CredentialVerifier.verify(UserAccount userOrNull, ReaderProfile readerOrNull, String rawPassword)` → `CredentialVerifier.Result` with `ok()`, `session()`, `failure()`
- Rules: null user or bad password → `INVALID_CREDENTIALS`; role ADMIN/LIBRARIAN and `user.locked()` → `INVALID_CREDENTIALS`; role READER and reader null → `INVALID_CREDENTIALS`; READER REJECTED → `REJECTED`; else success session

- [ ] **Step 1: Write failing tests** (`CredentialVerifierTest`, `PasswordHasherTest`) covering success admin, success pending reader, wrong password, locked librarian, rejected reader, missing reader profile.

- [ ] **Step 2: Run tests — expect compile fail**

- [ ] **Step 3: Implement minimal classes**

- [ ] **Step 4: Tests pass**

- [ ] **Step 5: Commit** `feat: add credential verification for login rules`

---

### Task 2: RegistrationValidator

**Files:** `RegistrationInput`, `RegistrationValidator` + test.

**Interfaces:**
- `RegistrationValidator.validate(RegistrationInput)` → `List<String>` errors (Vietnamese)
- username blank / not 4–32 `[a-zA-Z0-9._]`
- password blank / < 8
- confirm not equal
- fullName blank
- email blank / not `^[^@\s]+@[^@\s]+\.[^@\s]+$`
- phone blank / not 9–15 digits (allow leading 0)

- [ ] **Step 1–5:** TDD then commit `feat: validate reader self-registration fields`

---

### Task 3: JDBC DAOs + AuthService + RegistrationService

**Files:** `UserDao`, `ReaderDao`, `AuthService`, `RegistrationService`. Wire factory in `AppContextListener` as `authService`, `registrationService`.

**Interfaces:**
- `UserDao.findByUsername(Connection, String)` → `Optional<UserAccount>` join roles for `code`
- `UserDao.insert(Connection, username, hash, roleId)` → generated id
- `ReaderDao.findByUserId(Connection, long)` → `Optional<ReaderProfile>`
- `ReaderDao.existsEmail(Connection, email)` / `UserDao.existsUsername`
- `ReaderDao.nextCode(Connection)` → `DG` + 6 digits from `MAX(CAST(SUBSTRING(code,3) AS UNSIGNED))+1`
- `ReaderDao.insert(Connection, userId, code, fullName, email, phone, status)`
- `AuthService.login(username, password)` → `Optional<AuthSession>` or result with failure; uses one connection
- `RegistrationService.register(input)` → `RegistrationResult` (ok + readerCode | field errors | duplicate username/email). Transaction: insert user role_id=3, insert reader PENDING. Catch duplicate key → Vietnamese error, rollback.

Role Reader `id` is 3 in seed; lookup by `SELECT id FROM roles WHERE code='READER'` instead of hardcoding 3.

- [ ] **Step 1–5:** TDD validator/hasher already cover rules; add a small test for `ReaderDao` code format with a package-visible `formatReaderCode(long n)` static method. Commit `feat: add JDBC auth and registration services`

---

### Task 4: Servlets, JSP, AuthFilter, web.xml

**Files:** servlets, JSPs under `WEB-INF/jsp` (not publicly addressable), `AuthFilter`, update `web.xml`, `index.jsp` redirect, `WebXmlTest`.

**Behavior:**
- `GET /login` form; `POST /login` bind session, redirect `/home`
- `GET/POST /logout` `session.invalidate()`, redirect `/login`
- `GET /register` form; `POST /register` success → redirect `/login?registered=1`
- `GET /home` → staff-home.jsp or reader-home.jsp by role
- Filter: allow `/login`, `/register`, `/health`, `/` (index redirect). Require session for `/home`. Logged-in user hitting `/login` or `/register` → `/home`
- Generic login error; REJECTED specific message
- Bootstrap 5 CDN, tiếng Việt, link qua lại login/register
- Reader home shows mã thẻ + trạng thái (PENDING được tra cứu sau này; hiện chỉ chào)

- [ ] **Step 1–5:** Update `WebXmlTest` for new mappings. `mvn test package`. Commit `feat: add login, register, and logout pages`

---

### Task 5: README + Docker smoke

Update README with login URLs and seed accounts. Rebuild compose app service and verify:

```bash
curl -fsS -c cookies -b cookies -o /dev/null -w '%{http_code}' http://localhost:8080/login
# POST login admin/Admin@123 → 302 to /home
# GET /home with cookie → 200 and contains admin
# POST logout → login
# Register a new user, login as that user, page shows PENDING
```

Commit `docs: document login and registration`

---

## Ngoài phạm vi

UC-03 đổi mật khẩu, UC-04 tài khoản nhân viên, UC-06 duyệt thẻ, UC-07 tạo hộ, mượn/trả, dashboard.
