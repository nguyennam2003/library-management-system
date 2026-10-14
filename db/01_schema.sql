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

-- system_settings keys: max_borrow_count, loan_period_days (seed in 02_seed.sql)
CREATE TABLE system_settings (
  setting_key VARCHAR(64) NOT NULL,
  setting_value INT NOT NULL,
  PRIMARY KEY (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
