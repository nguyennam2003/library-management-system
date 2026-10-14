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
