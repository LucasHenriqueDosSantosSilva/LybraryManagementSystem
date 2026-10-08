CREATE TABLE authors (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(150) NOT NULL,
 CONSTRAINT ck_authors_name CHECK (CHAR_LENGTH(TRIM(name)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE books (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 isbn CHAR(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 title VARCHAR(250) NOT NULL,
 description TEXT NULL,
 publication_year SMALLINT NOT NULL,
 category_id BIGINT NOT NULL,
 CONSTRAINT uk_books_isbn UNIQUE(isbn),
 CONSTRAINT ck_books_isbn CHECK (isbn REGEXP '^(978|979)[0-9]{10}$'),
 CONSTRAINT ck_books_title CHECK (CHAR_LENGTH(TRIM(title)) > 0),
 CONSTRAINT ck_books_year CHECK (publication_year BETWEEN 1 AND 9999),
 CONSTRAINT fk_books_category FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE book_authors (
 book_id BIGINT NOT NULL,
 author_id BIGINT NOT NULL,
 PRIMARY KEY(book_id,author_id),
 INDEX ix_book_authors_author(author_id,book_id),
 CONSTRAINT fk_book_authors_book FOREIGN KEY(book_id) REFERENCES books(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT fk_book_authors_author FOREIGN KEY(author_id) REFERENCES authors(id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
