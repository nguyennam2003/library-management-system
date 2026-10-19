# Hệ thống Quản lý Thư viện

Website đồ án (Java Servlet + JSP + MySQL).
- Đặc tả SRS v1.1: `docs/SRS_Quan_Ly_Thu_Vien_v1.1.md`
- Tiến độ & Phân chia module cho nhóm 5 người / AI Agent: `docs/PHAN_CHIA_MODULE_VA_TIEN_DO.md`

## Yêu cầu máy local

- Docker + Docker Compose
- (Tuỳ chọn) JDK 17 + Maven 3.9 nếu chạy test ngoài container

## Chạy bằng Docker

```bash
cp .env.example .env
docker compose up --build
```

Mở:

- Trang chủ (chuyển tới đăng nhập): http://localhost:8080/
- Đăng nhập: http://localhost:8080/login
- Đăng ký độc giả: http://localhost:8080/register
- Quản lý Đầu sách & Bản sách (Admin/Thủ thư): http://localhost:8080/staff/books
- Quản lý Thể loại & Tác giả (Admin/Thủ thư): http://localhost:8080/staff/catalog
- Tra cứu sách (Độc giả): http://localhost:8080/reader/books
- Health (phải in `OK`): http://localhost:8080/health

Phân quyền URL (filter + `RoleGuard.require` trên servlet): `/admin/*` chỉ Admin; `/staff/*` Admin và Thủ thư; `/reader/*` Độc giả. Sai vai trò trả **403**.

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

Trong Docker, app đọc `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` từ `docker-compose.yml` / `.env`. Không commit file `.env` hay `src/main/resources/db.properties`. Healthcheck MySQL dùng cùng mật khẩu root: Compose thay `${MYSQL_ROOT_PASSWORD:-rootsecret}` (mặc định `rootsecret` trong `.env.example`) vào `healthcheck.test`.

Chạy WAR ngoài Docker:

1. Copy `src/main/resources/db.properties.example` thành `src/main/resources/db.properties` (file này nằm trong `.gitignore`).
2. Export `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, hoặc điền `db.host`, `db.port`, `db.name`, `db.user`, `db.password` trong file đó. Từng biến môi trường ghi đè khóa tương ứng trong file; biến trống hoặc không có thì app dùng file. File vắng mặt vẫn được (Docker chỉ cần biến môi trường).
3. Import schema và seed bằng client MySQL, ví dụ:

```bash
mysql -ulibrary -plibrary library_db < db/01_schema.sql
mysql -ulibrary -plibrary library_db < db/02_seed.sql
```

4. Đóng gói rồi deploy `target/library-management.war` lên Tomcat 9 với context ROOT (`webapps/ROOT.war`):

```bash
mvn package
```

Múi giờ: `Asia/Ho_Chi_Minh`. Session: 30 phút.
