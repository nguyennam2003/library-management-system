# Project Bootstrap + Docker Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Dựng source base Maven WAR, cấu hình JDBC ngoài mã nguồn, schema + seed MySQL, và chạy được toàn bộ stack bằng Docker Compose (trang chủ tiếng Việt + `/health` ping được DB).

**Architecture:** Ứng dụng Java Servlet đóng gói WAR, deploy `ROOT` trên Tomcat 9. Docker Compose chạy hai service: `mysql` (init `db/01_schema.sql` + `db/02_seed.sql`) và `app` (multi-stage: Maven build → Tomcat). JDBC đọc `DB_*` từ environment; không commit mật khẩu. Plan này **không** làm đăng nhập, CRUD sách, hay mượn/trả — những phần đó là plan tiếp theo, dùng đúng schema và `ConnectionFactory` tạo ở đây.

**Tech Stack:** JDK 17, Maven, Java Servlet 4.0, JSP 2.3, Apache Tomcat 9.0, MySQL 8.0 (`utf8mb4`), JDBC (`mysql-connector-j`), jBCrypt 0.4, JUnit 5, Docker Compose.

## Global Constraints

- JDK 17; Servlet 4.0 / JSP 2.3 trên Apache Tomcat 9.0; MySQL 8.0 charset `utf8mb4`.
- Java Servlet + JSP + JDBC + MySQL; không dùng Spring.
- Thông tin kết nối DB nằm ngoài mã nguồn (NFR-09); không commit mật khẩu thật.
- Giao diện tiếng Việt, UTF-8; ngày nghiệp vụ `Asia/Ho_Chi_Minh`.
- Session idle timeout 30 phút (khai báo `web.xml` ngay từ base).
- Hash mật khẩu BCrypt cost factor ≥ 10; seed dùng hash jBCrypt `$2a$10$...`, không plaintext.
- Không có bảng `fines`. Mọi bảng/enum đúng SRS v1.1 mục 5.
- Package gốc: `com.ptit.library`. Artifact WAR `library-management`, context path `/` (ROOT).

**Spec:** `docs/superpowers/specs/2026-10-14-srs-quan-ly-thu-vien-v1.1-design.md`

---

## File structure (khóa trước khi làm task)

```text
.gitignore
.env.example
.dockerignore
Dockerfile
docker-compose.yml
pom.xml
README.md
db/01_schema.sql
db/02_seed.sql
src/main/java/com/ptit/library/config/DbSettings.java
src/main/java/com/ptit/library/config/DbSettingsLoader.java
src/main/java/com/ptit/library/dao/ConnectionFactory.java
src/main/java/com/ptit/library/web/AppContextListener.java
src/main/java/com/ptit/library/web/CharacterEncodingFilter.java
src/main/java/com/ptit/library/web/HealthServlet.java
src/main/resources/db.properties.example
src/main/webapp/WEB-INF/web.xml
src/main/webapp/index.jsp
src/test/java/com/ptit/library/config/DbSettingsLoaderTest.java
src/test/java/com/ptit/library/config/DbSettingsTest.java
src/test/java/com/ptit/library/db/SchemaScriptTest.java
src/test/java/com/ptit/library/db/SeedScriptTest.java
```

**Trách nhiệm từng nhóm:**

| Nhóm | Việc |
| --- | --- |
| `config/*` | Đọc host/port/name/user/password; ghép JDBC URL. Không mở connection. |
| `dao/ConnectionFactory` | `DriverManager.getConnection`. DAO sau này chỉ gọi `getConnection()`. |
| `web/*` | Filter UTF-8, listener gắn factory vào `ServletContext`, servlet `/health`. |
| `db/*.sql` | Schema đúng SRS + seed demo. MySQL container chạy đúng thứ tự `01` rồi `02`. |
| Docker | Build WAR, Tomcat ROOT, đợi MySQL healthy, truyền `DB_*`. |

---

### Task 1: Maven WAR + DbSettings

**Files:**
- Create: `pom.xml`
- Create: `.gitignore`
- Create: `src/test/java/com/ptit/library/config/DbSettingsTest.java`
- Create: `src/test/java/com/ptit/library/config/DbSettingsLoaderTest.java`
- Create: `src/main/java/com/ptit/library/config/DbSettings.java`
- Create: `src/main/java/com/ptit/library/config/DbSettingsLoader.java`
- Create: `src/main/resources/db.properties.example`

**Interfaces:**
- Consumes: nothing
- Produces:
  - `DbSettings(String host, int port, String name, String user, String password)` with getters `host()`, `port()`, `name()`, `user()`, `password()`, `jdbcUrl()`
  - `DbSettingsLoader.load(Map<String, String> env, Properties fileProps)` → `DbSettings`
  - Env keys: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
  - File keys: `db.host`, `db.port`, `db.name`, `db.user`, `db.password`
  - Ưu tiên env; thiếu một key env thì lấy file; vẫn thiếu → `IllegalStateException`

- [ ] **Step 1: Write `pom.xml` and `.gitignore`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.ptit</groupId>
  <artifactId>library-management</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <packaging>war</packaging>
  <name>Library Management System</name>
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.release>17</maven.compiler.release>
  </properties>
  <dependencies>
    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>javax.servlet-api</artifactId>
      <version>4.0.1</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>javax.servlet.jsp</groupId>
      <artifactId>javax.servlet.jsp-api</artifactId>
      <version>2.3.3</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <version>8.4.0</version>
    </dependency>
    <dependency>
      <groupId>org.mindrot</groupId>
      <artifactId>jbcrypt</artifactId>
      <version>0.4</version>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.10.2</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
  <build>
    <finalName>library-management</finalName>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.13.0</version>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-war-plugin</artifactId>
        <version>3.4.0</version>
        <configuration>
          <failOnMissingWebXml>false</failOnMissingWebXml>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.2.5</version>
      </plugin>
    </plugins>
  </build>
</project>
```

`.gitignore`:

```gitignore
target/
.idea/
*.iml
.classpath
.project
.settings/
.vscode/
.env
src/main/resources/db.properties
*.log
```

- [ ] **Step 2: Write failing tests**

`src/test/java/com/ptit/library/config/DbSettingsTest.java`:

```java
package com.ptit.library.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DbSettingsTest {

    @Test
    void jdbcUrl_usesUtf8AndVietnamTimezone() {
        DbSettings settings = new DbSettings("mysql", 3306, "library_db", "library", "secret");
        String url = settings.jdbcUrl();
        assertTrue(url.startsWith("jdbc:mysql://mysql:3306/library_db?"));
        assertTrue(url.contains("characterEncoding=UTF-8"));
        assertTrue(url.contains("connectionCollation=utf8mb4_unicode_ci"));
        assertTrue(url.contains("serverTimezone=Asia/Ho_Chi_Minh"));
        assertTrue(url.contains("allowPublicKeyRetrieval=true"));
        assertTrue(url.contains("useSSL=false"));
    }
}
```

`src/test/java/com/ptit/library/config/DbSettingsLoaderTest.java`:

```java
package com.ptit.library.config;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DbSettingsLoaderTest {

    @Test
    void load_prefersEnvironmentOverFile() {
        Map<String, String> env = new HashMap<>();
        env.put("DB_HOST", "mysql");
        env.put("DB_PORT", "3306");
        env.put("DB_NAME", "library_db");
        env.put("DB_USER", "library");
        env.put("DB_PASSWORD", "from-env");
        Properties file = new Properties();
        file.setProperty("db.host", "localhost");
        file.setProperty("db.port", "3307");
        file.setProperty("db.name", "other");
        file.setProperty("db.user", "fileuser");
        file.setProperty("db.password", "from-file");

        DbSettings settings = DbSettingsLoader.load(env, file);

        assertEquals("mysql", settings.host());
        assertEquals(3306, settings.port());
        assertEquals("library_db", settings.name());
        assertEquals("library", settings.user());
        assertEquals("from-env", settings.password());
    }

    @Test
    void load_fallsBackToFileWhenEnvMissing() {
        Properties file = new Properties();
        file.setProperty("db.host", "localhost");
        file.setProperty("db.port", "3306");
        file.setProperty("db.name", "library_db");
        file.setProperty("db.user", "library");
        file.setProperty("db.password", "from-file");

        DbSettings settings = DbSettingsLoader.load(Map.of(), file);

        assertEquals("localhost", settings.host());
        assertEquals("from-file", settings.password());
    }

    @Test
    void load_throwsWhenIncomplete() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> DbSettingsLoader.load(Map.of(), new Properties()));
        assertTrue(ex.getMessage().contains("DB_HOST"));
    }
}
```

- [ ] **Step 3: Run tests to verify they fail**

Run: `mvn -q test -Dtest=DbSettingsTest,DbSettingsLoaderTest`

Expected: FAIL compiling (`package com.ptit.library.config does not exist` or cannot find `DbSettings`).

- [ ] **Step 4: Implement `DbSettings` and `DbSettingsLoader`**

`src/main/java/com/ptit/library/config/DbSettings.java`:

```java
package com.ptit.library.config;

import java.util.Objects;

public final class DbSettings {

    private final String host;
    private final int port;
    private final String name;
    private final String user;
    private final String password;

    public DbSettings(String host, int port, String name, String user, String password) {
        this.host = Objects.requireNonNull(host, "host");
        this.port = port;
        this.name = Objects.requireNonNull(name, "name");
        this.user = Objects.requireNonNull(user, "user");
        this.password = Objects.requireNonNull(password, "password");
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("invalid db port: " + port);
        }
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String name() {
        return name;
    }

    public String user() {
        return user;
    }

    public String password() {
        return password;
    }

    public String jdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + name
                + "?useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&characterEncoding=UTF-8"
                + "&connectionCollation=utf8mb4_unicode_ci"
                + "&serverTimezone=Asia/Ho_Chi_Minh";
    }
}
```

`src/main/java/com/ptit/library/config/DbSettingsLoader.java`:

```java
package com.ptit.library.config;

import java.util.Map;
import java.util.Properties;

public final class DbSettingsLoader {

    private DbSettingsLoader() {
    }

    public static DbSettings load(Map<String, String> env, Properties fileProps) {
        String host = first(env.get("DB_HOST"), fileProps.getProperty("db.host"), "DB_HOST / db.host");
        String portRaw = first(env.get("DB_PORT"), fileProps.getProperty("db.port"), "DB_PORT / db.port");
        String name = first(env.get("DB_NAME"), fileProps.getProperty("db.name"), "DB_NAME / db.name");
        String user = first(env.get("DB_USER"), fileProps.getProperty("db.user"), "DB_USER / db.user");
        String password = first(env.get("DB_PASSWORD"), fileProps.getProperty("db.password"), "DB_PASSWORD / db.password");
        int port;
        try {
            port = Integer.parseInt(portRaw);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("DB_PORT / db.port must be an integer", e);
        }
        return new DbSettings(host, port, name, user, password);
    }

    public static DbSettings loadFromEnvironment() {
        return load(System.getenv(), new Properties());
    }

    private static String first(String envValue, String fileValue, String label) {
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue;
        }
        throw new IllegalStateException("Missing database setting: " + label);
    }
}
```

`src/main/resources/db.properties.example`:

```properties
db.host=localhost
db.port=3306
db.name=library_db
db.user=library
db.password=change-me
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `mvn -q test -Dtest=DbSettingsTest,DbSettingsLoaderTest`

Expected: `BUILD SUCCESS`, tests pass.

- [ ] **Step 6: Commit**

```bash
git add pom.xml .gitignore \
  src/main/java/com/ptit/library/config/DbSettings.java \
  src/main/java/com/ptit/library/config/DbSettingsLoader.java \
  src/main/resources/db.properties.example \
  src/test/java/com/ptit/library/config/DbSettingsTest.java \
  src/test/java/com/ptit/library/config/DbSettingsLoaderTest.java
git commit -m "$(cat <<'EOF'
chore: add Maven WAR skeleton and external JDBC settings loader

EOF
)"
```

---

### Task 2: ConnectionFactory

**Files:**
- Create: `src/main/java/com/ptit/library/dao/ConnectionFactory.java`
- Modify: `src/test/java/com/ptit/library/config/DbSettingsTest.java` (không bắt buộc nếu Task 1 đã cover URL)

**Interfaces:**
- Consumes: `DbSettings.jdbcUrl()`, `user()`, `password()`
- Produces: `ConnectionFactory(DbSettings settings)` and `Connection getConnection() throws SQLException`; constructor loads `com.mysql.cj.jdbc.Driver` or throws `IllegalStateException`

Không viết integration test MySQL ở task này (chưa có container). `/health` + Docker ở Task 6 là vòng kiểm tra connection thật.

- [ ] **Step 1: Implement `ConnectionFactory`**

`src/main/java/com/ptit/library/dao/ConnectionFactory.java`:

```java
package com.ptit.library.dao;

import com.ptit.library.config.DbSettings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public final class ConnectionFactory {

    private final DbSettings settings;

    public ConnectionFactory(DbSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC driver not found", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(settings.jdbcUrl(), settings.user(), settings.password());
    }
}
```

- [ ] **Step 2: Compile to verify the class is on the main classpath**

Run: `mvn -q -DskipTests compile`

Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/ptit/library/dao/ConnectionFactory.java
git commit -m "$(cat <<'EOF'
feat: add JDBC ConnectionFactory for DAO layer

EOF
)"
```

---

### Task 3: Schema SQL (SRS v1.1)

**Files:**
- Create: `db/01_schema.sql`
- Create: `src/test/java/com/ptit/library/db/SchemaScriptTest.java`

**Interfaces:**
- Consumes: SRS mục 5 (bảng, FK, enum, không `fines`)
- Produces: MySQL script tạo toàn bộ bảng; unique một bản `BORROWED` tại một thời điểm qua generated column `borrowed_copy_key`

- [ ] **Step 1: Write failing `SchemaScriptTest`**

```java
package com.ptit.library.db;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaScriptTest {

    private static String sql;

    @BeforeAll
    static void load() throws IOException {
        sql = Files.readString(Path.of("db/01_schema.sql"), StandardCharsets.UTF_8);
    }

    @Test
    void createsEverySpecifiedTableAndOmitsFines() {
        assertTrue(sql.contains("CREATE TABLE roles"));
        assertTrue(sql.contains("CREATE TABLE users"));
        assertTrue(sql.contains("CREATE TABLE readers"));
        assertTrue(sql.contains("CREATE TABLE categories"));
        assertTrue(sql.contains("CREATE TABLE authors"));
        assertTrue(sql.contains("CREATE TABLE books"));
        assertTrue(sql.contains("CREATE TABLE book_authors"));
        assertTrue(sql.contains("CREATE TABLE book_copies"));
        assertTrue(sql.contains("CREATE TABLE loans"));
        assertTrue(sql.contains("CREATE TABLE loan_details"));
        assertTrue(sql.contains("CREATE TABLE system_settings"));
        assertFalse(sql.contains("CREATE TABLE fines"));
    }

    @Test
    void usesUtf8mb4AndRequiredStatuses() {
        assertTrue(sql.contains("utf8mb4"));
        assertTrue(sql.contains("PENDING"));
        assertTrue(sql.contains("AVAILABLE"));
        assertTrue(sql.contains("borrowed_copy_key"));
        assertTrue(sql.contains("max_borrow_count"));
        assertTrue(sql.contains("loan_period_days"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q test -Dtest=SchemaScriptTest`

Expected: FAIL (`db/01_schema.sql` missing or NoSuchFileException).

- [ ] **Step 3: Write `db/01_schema.sql`**

```sql
SET NAMES utf8mb4;
SET time_zone = '+07:00';

CREATE TABLE roles (
  id TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(32) NOT NULL,
  name_vi VARCHAR(64) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_roles_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(32) NOT NULL,
  password_hash VARCHAR(60) NOT NULL,
  role_id TINYINT UNSIGNED NOT NULL,
  locked TINYINT(1) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE readers (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  code VARCHAR(16) NOT NULL,
  full_name VARCHAR(128) NOT NULL,
  email VARCHAR(128) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  status VARCHAR(16) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_readers_user_id (user_id),
  UNIQUE KEY uk_readers_code (code),
  UNIQUE KEY uk_readers_email (email),
  CONSTRAINT fk_readers_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT chk_readers_status CHECK (status IN ('PENDING', 'ACTIVE', 'LOCKED', 'REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE categories (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE authors (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE books (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  isbn VARCHAR(17) NULL,
  category_id BIGINT UNSIGNED NOT NULL,
  publisher VARCHAR(128) NOT NULL,
  publish_year SMALLINT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_books_isbn (isbn),
  CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories (id),
  CONSTRAINT chk_books_status CHECK (status IN ('ACTIVE', 'WITHDRAWN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE book_authors (
  book_id BIGINT UNSIGNED NOT NULL,
  author_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (book_id, author_id),
  CONSTRAINT fk_book_authors_book FOREIGN KEY (book_id) REFERENCES books (id),
  CONSTRAINT fk_book_authors_author FOREIGN KEY (author_id) REFERENCES authors (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE book_copies (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  book_id BIGINT UNSIGNED NOT NULL,
  code VARCHAR(32) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_book_copies_code (code),
  CONSTRAINT fk_book_copies_book FOREIGN KEY (book_id) REFERENCES books (id),
  CONSTRAINT chk_book_copies_status CHECK (status IN ('AVAILABLE', 'BORROWED', 'WITHDRAWN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE loans (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(16) NOT NULL,
  reader_id BIGINT UNSIGNED NOT NULL,
  created_by_user_id BIGINT UNSIGNED NOT NULL,
  borrowed_on DATE NOT NULL,
  status VARCHAR(16) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_loans_code (code),
  CONSTRAINT fk_loans_reader FOREIGN KEY (reader_id) REFERENCES readers (id),
  CONSTRAINT fk_loans_created_by FOREIGN KEY (created_by_user_id) REFERENCES users (id),
  CONSTRAINT chk_loans_status CHECK (status IN ('OPEN', 'COMPLETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE loan_details (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  loan_id BIGINT UNSIGNED NOT NULL,
  copy_id BIGINT UNSIGNED NOT NULL,
  due_date DATE NOT NULL,
  returned_at DATE NULL,
  status VARCHAR(16) NOT NULL,
  borrowed_copy_key BIGINT UNSIGNED GENERATED ALWAYS AS (
    CASE WHEN status = 'BORROWED' THEN copy_id ELSE NULL END
  ) STORED,
  PRIMARY KEY (id),
  UNIQUE KEY uk_one_borrowed_copy (borrowed_copy_key),
  CONSTRAINT fk_loan_details_loan FOREIGN KEY (loan_id) REFERENCES loans (id),
  CONSTRAINT fk_loan_details_copy FOREIGN KEY (copy_id) REFERENCES book_copies (id),
  CONSTRAINT chk_loan_details_status CHECK (status IN ('BORROWED', 'RETURNED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE system_settings (
  setting_key VARCHAR(64) NOT NULL,
  setting_value INT NOT NULL,
  PRIMARY KEY (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -q test -Dtest=SchemaScriptTest`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add db/01_schema.sql src/test/java/com/ptit/library/db/SchemaScriptTest.java
git commit -m "$(cat <<'EOF'
feat: add MySQL schema matching SRS v1.1 entities

EOF
)"
```

---

### Task 4: Seed data

**Files:**
- Create: `db/02_seed.sql`
- Create: `src/test/java/com/ptit/library/db/SeedScriptTest.java`

**Interfaces:**
- Consumes: schema Task 3; jBCrypt hashes đã kiểm tra với jBCrypt 0.4
- Produces: roles, admin, librarian, 1 độc giả ACTIVE + 1 PENDING, settings 5/14, vài đầu sách + bản mẫu

Mật khẩu demo (ghi README, không phải secret production):

| Username | Password | Role / thẻ |
| --- | --- | --- |
| `admin` | `Admin@123` | Admin |
| `librarian` | `Lib@12345` | Librarian |
| `reader1` | `Reader@123` | Reader / ACTIVE `DG000001` |
| `reader2` | `Reader@123` | Reader / PENDING `DG000002` |

Hashes (jBCrypt `$2a$10`, cost 10):

- `Admin@123` → `$2a$10$KJ4LtyAjVb4577GmN1bqYuP8of.0uqdqe/bmvAfC7QTo/aEJbd1CS`
- `Lib@12345` → `$2a$10$Avlj.S9AJbJomOgj6J/i.uxipKBrZ3B9Rj1SV1nonc8CzjEuDVXn6`
- `Reader@123` → `$2a$10$rW78Cr7Re4XQodARPuKzaud9T3z8uyL4yJeaiY0Ug1ehEN/Lv2Faa`

- [ ] **Step 1: Write failing `SeedScriptTest`**

```java
package com.ptit.library.db;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SeedScriptTest {

    private static String sql;

    @BeforeAll
    static void load() throws IOException {
        sql = Files.readString(Path.of("db/02_seed.sql"), StandardCharsets.UTF_8);
    }

    @Test
    void seedsRequiredDemoRows() {
        assertTrue(sql.contains("'ADMIN'"));
        assertTrue(sql.contains("'LIBRARIAN'"));
        assertTrue(sql.contains("'READER'"));
        assertTrue(sql.contains("'admin'"));
        assertTrue(sql.contains("'librarian'"));
        assertTrue(sql.contains("'reader1'"));
        assertTrue(sql.contains("'reader2'"));
        assertTrue(sql.contains("'DG000001'"));
        assertTrue(sql.contains("'DG000002'"));
        assertTrue(sql.contains("'PENDING'"));
        assertTrue(sql.contains("'ACTIVE'"));
        assertTrue(sql.contains("'max_borrow_count'"));
        assertTrue(sql.contains("'loan_period_days'"));
        assertFalseContainsFines();
    }

    @Test
    void adminHashMatchesKnownPassword() {
        Matcher matcher = Pattern.compile("\\$2a\\$10\\$[A-Za-z0-9./]{53}").matcher(sql);
        assertTrue(matcher.find(), "expected bcrypt hashes in seed");
        boolean adminOk = false;
        matcher.reset();
        while (matcher.find()) {
            if (BCrypt.checkpw("Admin@123", matcher.group())) {
                adminOk = true;
            }
        }
        assertTrue(adminOk, "seed must contain BCrypt hash for Admin@123");
    }

    private static void assertFalseContainsFines() {
        org.junit.jupiter.api.Assertions.assertFalse(sql.toLowerCase().contains("insert into fines"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q test -Dtest=SeedScriptTest`

Expected: FAIL (file missing).

- [ ] **Step 3: Write `db/02_seed.sql`**

```sql
SET NAMES utf8mb4;
SET time_zone = '+07:00';

INSERT INTO roles (id, code, name_vi) VALUES
  (1, 'ADMIN', 'Quản trị viên'),
  (2, 'LIBRARIAN', 'Thủ thư'),
  (3, 'READER', 'Độc giả');

INSERT INTO users (id, username, password_hash, role_id, locked) VALUES
  (1, 'admin', '$2a$10$KJ4LtyAjVb4577GmN1bqYuP8of.0uqdqe/bmvAfC7QTo/aEJbd1CS', 1, 0),
  (2, 'librarian', '$2a$10$Avlj.S9AJbJomOgj6J/i.uxipKBrZ3B9Rj1SV1nonc8CzjEuDVXn6', 2, 0),
  (3, 'reader1', '$2a$10$rW78Cr7Re4XQodARPuKzaud9T3z8uyL4yJeaiY0Ug1ehEN/Lv2Faa', 3, 0),
  (4, 'reader2', '$2a$10$rW78Cr7Re4XQodARPuKzaud9T3z8uyL4yJeaiY0Ug1ehEN/Lv2Faa', 3, 0);

INSERT INTO readers (id, user_id, code, full_name, email, phone, status) VALUES
  (1, 3, 'DG000001', 'Nguyễn Văn An', 'an.nguyen@example.com', '0901000001', 'ACTIVE'),
  (2, 4, 'DG000002', 'Trần Thị Bình', 'binh.tran@example.com', '0901000002', 'PENDING');

INSERT INTO system_settings (setting_key, setting_value) VALUES
  ('max_borrow_count', 5),
  ('loan_period_days', 14);

INSERT INTO categories (id, name) VALUES
  (1, 'Công nghệ thông tin'),
  (2, 'Văn học'),
  (3, 'Khoa học');

INSERT INTO authors (id, name) VALUES
  (1, 'Nguyễn Nhật Ánh'),
  (2, 'Robert C. Martin'),
  (3, 'Erich Gamma');

INSERT INTO books (id, title, isbn, category_id, publisher, publish_year, status) VALUES
  (1, 'Clean Code', '9780132350884', 1, 'Prentice Hall', 2008, 'ACTIVE'),
  (2, 'Design Patterns', '9780201633610', 1, 'Addison-Wesley', 1994, 'ACTIVE'),
  (3, 'Cho tôi xin một vé đi tuổi thơ', NULL, 2, 'Nxb Trẻ', 2008, 'ACTIVE');

INSERT INTO book_authors (book_id, author_id) VALUES
  (1, 2),
  (2, 3),
  (3, 1);

INSERT INTO book_copies (id, book_id, code, status) VALUES
  (1, 1, 'CC-001', 'AVAILABLE'),
  (2, 1, 'CC-002', 'AVAILABLE'),
  (3, 2, 'DP-001', 'AVAILABLE'),
  (4, 3, 'VT-001', 'AVAILABLE');
```

- [ ] **Step 4: Run tests**

Run: `mvn -q test -Dtest=SchemaScriptTest,SeedScriptTest`

Expected: PASS. `adminHashMatchesKnownPassword` true.

- [ ] **Step 5: Commit**

```bash
git add db/02_seed.sql src/test/java/com/ptit/library/db/SeedScriptTest.java
git commit -m "$(cat <<'EOF'
feat: add demo seed roles, users, books, and loan settings

EOF
)"
```

---

### Task 5: web.xml, UTF-8 filter, health endpoint

**Files:**
- Create: `src/main/webapp/WEB-INF/web.xml`
- Create: `src/main/java/com/ptit/library/web/CharacterEncodingFilter.java`
- Create: `src/main/java/com/ptit/library/web/AppContextListener.java`
- Create: `src/main/java/com/ptit/library/web/HealthServlet.java`
- Create: `src/main/webapp/index.jsp`
- Create: `src/test/java/com/ptit/library/web/WebXmlTest.java`

**Interfaces:**
- Consumes: `DbSettingsLoader.loadFromEnvironment()`, `ConnectionFactory(DbSettings)`, `ConnectionFactory.getConnection()`
- Produces:
  - `ServletContext` attribute name `"connectionFactory"` type `ConnectionFactory`
  - `GET /health` → `200` body `OK` (UTF-8 text/plain) nếu `SELECT 1` thành công; `503` body `DB_ERROR` nếu SQLException
  - `index.jsp` tiếng Việt, link `/health`
  - `session-timeout` = 30

`AppContextListener` nếu thiếu env: log và không set attribute; `/health` lúc đó trả 503.

- [ ] **Step 1: Write failing `WebXmlTest`**

```java
package com.ptit.library.web;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WebXmlTest {

    private static String xml;

    @BeforeAll
    static void load() throws IOException {
        xml = Files.readString(Path.of("src/main/webapp/WEB-INF/web.xml"), StandardCharsets.UTF_8);
    }

    @Test
    void declaresServlet40SessionHealthAndEncoding() {
        assertTrue(xml.contains("version=\"4.0\""));
        assertTrue(xml.contains("<session-timeout>30</session-timeout>"));
        assertTrue(xml.contains("com.ptit.library.web.HealthServlet"));
        assertTrue(xml.contains("<url-pattern>/health</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.CharacterEncodingFilter"));
        assertTrue(xml.contains("com.ptit.library.web.AppContextListener"));
        assertTrue(xml.contains("<url-pattern>/*</url-pattern>"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q test -Dtest=WebXmlTest`

Expected: FAIL (web.xml missing).

- [ ] **Step 3: Implement web layer**

`src/main/java/com/ptit/library/web/CharacterEncodingFilter.java`:

```java
package com.ptit.library.web;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class CharacterEncodingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
```

`src/main/java/com/ptit/library/web/AppContextListener.java`:

```java
package com.ptit.library.web;

import com.ptit.library.config.DbSettings;
import com.ptit.library.config.DbSettingsLoader;
import com.ptit.library.dao.ConnectionFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppContextListener implements ServletContextListener {

    public static final String CONNECTION_FACTORY = "connectionFactory";
    private static final Logger LOGGER = Logger.getLogger(AppContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            DbSettings settings = DbSettingsLoader.loadFromEnvironment();
            event.getServletContext().setAttribute(CONNECTION_FACTORY, new ConnectionFactory(settings));
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Database settings missing; /health will return 503", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        event.getServletContext().removeAttribute(CONNECTION_FACTORY);
    }
}
```

`src/main/java/com/ptit/library/web/HealthServlet.java`:

```java
package com.ptit.library.web;

import com.ptit.library.dao.ConnectionFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain; charset=UTF-8");
        ConnectionFactory factory =
                (ConnectionFactory) getServletContext().getAttribute(AppContextListener.CONNECTION_FACTORY);
        if (factory == null) {
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            resp.getWriter().write("DB_ERROR");
            return;
        }
        try (Connection connection = factory.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT 1")) {
            if (rs.next()) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("OK");
                return;
            }
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            resp.getWriter().write("DB_ERROR");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            resp.getWriter().write("DB_ERROR");
        }
    }
}
```

`src/main/webapp/WEB-INF/web.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
  <display-name>Library Management System</display-name>
  <listener>
    <listener-class>com.ptit.library.web.AppContextListener</listener-class>
  </listener>
  <filter>
    <filter-name>CharacterEncodingFilter</filter-name>
    <filter-class>com.ptit.library.web.CharacterEncodingFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>CharacterEncodingFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
  <servlet>
    <servlet-name>HealthServlet</servlet-name>
    <servlet-class>com.ptit.library.web.HealthServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>HealthServlet</servlet-name>
    <url-pattern>/health</url-pattern>
  </servlet-mapping>
  <session-config>
    <session-timeout>30</session-timeout>
  </session-config>
  <welcome-file-list>
    <welcome-file>index.jsp</welcome-file>
  </welcome-file-list>
</web-app>
```

`src/main/webapp/index.jsp`:

```jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Quản lý thư viện</title>
</head>
<body>
  <h1>Hệ thống Quản lý Thư viện</h1>
  <p>Phiên bản nền (MVP bootstrap). Đăng nhập và nghiệp vụ sẽ được bổ sung sau.</p>
  <p><a href="${pageContext.request.contextPath}/health">Kiểm tra kết nối cơ sở dữ liệu</a></p>
</body>
</html>
```

- [ ] **Step 4: Run unit tests and package WAR**

Run: `mvn -q test package`

Expected: `BUILD SUCCESS`; file `target/library-management.war` exists.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/ptit/library/web \
  src/main/webapp/WEB-INF/web.xml \
  src/main/webapp/index.jsp \
  src/test/java/com/ptit/library/web/WebXmlTest.java
git commit -m "$(cat <<'EOF'
feat: add UTF-8 filter, health servlet, and Vietnamese home page

EOF
)"
```

---

### Task 6: Docker Compose + README

**Files:**
- Create: `Dockerfile`
- Create: `.dockerignore`
- Create: `docker-compose.yml`
- Create: `.env.example`
- Create: `README.md`

**Interfaces:**
- Consumes: WAR `library-management.war`; SQL `db/01_schema.sql`, `db/02_seed.sql`; env `DB_HOST=mysql`, `DB_PORT=3306`, `DB_NAME=library_db`, `DB_USER=library`, `DB_PASSWORD` khớp `MYSQL_USER`/`MYSQL_PASSWORD`
- Produces: `docker compose up --build` → http://localhost:8080/ (HTML tiếng Việt) và http://localhost:8080/health body `OK`

- [ ] **Step 1: Write Docker files**

`.dockerignore`:

```dockerignore
target/
.git/
.idea/
*.iml
.env
docs/
```

`Dockerfile`:

```dockerfile
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM tomcat:9.0.96-jdk17-temurin
ENV TZ=Asia/Ho_Chi_Minh
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /src/target/library-management.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
```

`.env.example`:

```dotenv
MYSQL_DATABASE=library_db
MYSQL_USER=library
MYSQL_PASSWORD=library
MYSQL_ROOT_PASSWORD=rootsecret
DB_NAME=library_db
DB_USER=library
DB_PASSWORD=library
DB_HOST=mysql
DB_PORT=3306
```

`docker-compose.yml`:

```yaml
services:
  mysql:
    image: mysql:8.0.40
    environment:
      MYSQL_DATABASE: ${MYSQL_DATABASE:-library_db}
      MYSQL_USER: ${MYSQL_USER:-library}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-library}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-rootsecret}
      TZ: Asia/Ho_Chi_Minh
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
      - --default-time-zone=+07:00
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./db/01_schema.sql:/docker-entrypoint-initdb.d/01_schema.sql:ro
      - ./db/02_seed.sql:/docker-entrypoint-initdb.d/02_seed.sql:ro
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "127.0.0.1", "-uroot", "-prootsecret"]
      interval: 5s
      timeout: 5s
      retries: 20
      start_period: 20s

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_HOST: mysql
      DB_PORT: "3306"
      DB_NAME: ${DB_NAME:-library_db}
      DB_USER: ${DB_USER:-library}
      DB_PASSWORD: ${DB_PASSWORD:-library}
      TZ: Asia/Ho_Chi_Minh
    depends_on:
      mysql:
        condition: service_healthy

volumes:
  mysql_data:
```

Healthcheck MySQL dùng `-prootsecret` khớp default trong `.env.example`. Nếu đổi `MYSQL_ROOT_PASSWORD`, sửa cùng giá trị trong `healthcheck.test`.

- [ ] **Step 2: Write README.md**

```markdown
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
```

- [ ] **Step 3: Run unit tests then bring the stack up**

Run:

```bash
cp -n .env.example .env
mvn -q test
docker compose up --build -d
```

Đợi app healthy (Tomcat boot ~20–40s). Poll:

```bash
for i in 1 2 3 4 5 6 7 8 9 10 11 12; do
  if curl -fsS http://localhost:8080/health | grep -qx OK; then
    echo HEALTH_OK
    break
  fi
  sleep 5
done
curl -fsS http://localhost:8080/ | grep -q "Quản lý Thư viện"
echo HOME_OK
docker compose exec -T mysql mysql -ulibrary -plibrary library_db -e "SELECT username FROM users ORDER BY id;"
```

Expected:

- `curl /health` body exactly `OK`
- HTML trang chủ chứa `Quản lý Thư viện`
- MySQL in `admin`, `librarian`, `reader1`, `reader2`

Nếu `/health` là `DB_ERROR`: `docker compose logs app` — thường là app lên trước khi MySQL accept; `depends_on: service_healthy` phải chặn trường hợp này. Không dùng `restart: unless-stopped` để che lỗi.

- [ ] **Step 4: Tear down is optional; commit Docker files**

```bash
git add Dockerfile .dockerignore docker-compose.yml .env.example README.md
git commit -m "$(cat <<'EOF'
chore: run Tomcat and MySQL with Docker Compose

EOF
)"
```

---

## Ngoài phạm vi plan này

Không làm trong các task trên (plan sau, vẫn dùng `ConnectionFactory` + schema này):

- UC-01..UC-17 (đăng nhập, độc giả, sách, mượn/trả, dashboard, cấu hình UI)
- JSTL/Bootstrap layout đầy đủ
- Maven Wrapper, Testcontainers, connection pool

## Spec coverage (self-review)

| Spec | Task |
| --- | --- |
| JDK 17, Servlet 4, JSP 2.3, Tomcat 9, MySQL 8 utf8mb4 | Task 1 `pom.xml`, Task 6 Dockerfile/compose |
| Không Spring; JDBC DAO | Task 2 `ConnectionFactory` |
| NFR-09 config DB ngoài source | Task 1 loader env/file; `.gitignore`; `.env` |
| NFR-10 README + schema + seed + Admin mặc định | Task 3, 4, 6 |
| NFR-11 timezone VN | JDBC URL, MySQL `TZ`, Tomcat `TZ` |
| NFR-12 session 30 phút | Task 5 `web.xml` |
| NFR-02 UTF-8 tiếng Việt | Filter + `index.jsp` |
| NFR-04 BCrypt cost ≥ 10 | Task 4 seed `$2a$10$` + test `checkpw` |
| Bảng mục 5, không `fines` | Task 3 |
| Settings 5 cuốn / 14 ngày | Task 4 `system_settings` |
| Unique một bản đang BORROWED | Task 3 `borrowed_copy_key` |
| Đăng nhập / mượn / CRUD | **không** — plan sau |

Placeholder scan: không còn TBD/TODO trong bước triển khai. Healthcheck MySQL dùng `-prootsecret` khớp default `.env.example`; đổi root password thì sửa cùng một chỗ trong `docker-compose.yml`.
