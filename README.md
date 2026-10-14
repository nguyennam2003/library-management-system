# Hệ thống Quản lý Thư viện

Website đồ án (Java Servlet + JSP + MySQL). Đặc tả: `docs/superpowers/specs/2026-10-14-srs-quan-ly-thu-vien-v1.1-design.md`.

## Yêu cầu máy local

- Docker + Docker Compose
- (Tuỳ chọn) JDK 17 + Maven 3.9 nếu chạy test ngoài container

## Chạy bằng Docker

```bash
cp .env.example .env
docker compose up --build
```

Mở:

- Trang chủ: http://localhost:8080/
- Health (phải in `OK`): http://localhost:8080/health

Tài khoản seed (chỉ dùng demo):

| Vai trò | Username | Mật khẩu |
| --- | --- | --- |
| Admin | admin | Admin@123 |
| Thủ thư | librarian | Lib@12345 |
| Độc giả ACTIVE | reader1 | Reader@123 |
| Độc giả PENDING | reader2 | Reader@123 |

Schema + seed chỉ chạy khi volume MySQL **lần đầu trống**. Reset:

```bash
docker compose down -v
docker compose up --build
```

## Test (không cần Docker)

```bash
mvn test
```

## Cấu hình DB

Trong Docker, app đọc `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` từ `docker-compose.yml` / `.env`. Không commit file `.env` hay `src/main/resources/db.properties`.

Chạy WAR ngoài Docker: copy `src/main/resources/db.properties.example` → `db.properties` (file này nằm trong `.gitignore`) rồi export cùng các biến `DB_*` hoặc đặt properties; deploy `target/library-management.war` lên Tomcat 9.

Múi giờ: `Asia/Ho_Chi_Minh`. Session: 30 phút.
