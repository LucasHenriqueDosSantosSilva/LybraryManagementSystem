CREATE TABLE readers (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 registration_number VARCHAR(30) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 name VARCHAR(150) NOT NULL,
 email VARCHAR(254) NULL,
 active BOOLEAN NOT NULL DEFAULT TRUE,
 CONSTRAINT uk_readers_registration UNIQUE(registration_number),
 CONSTRAINT ck_readers_registration CHECK (registration_number REGEXP '^[A-Z0-9._-]+$'),
 CONSTRAINT ck_readers_name CHECK (CHAR_LENGTH(TRIM(name))>0),
 CONSTRAINT ck_readers_active CHECK (active IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE book_copies (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 inventory_code VARCHAR(40) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 book_id BIGINT NOT NULL,
 circulation_status VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'IN_CIRCULATION',
 CONSTRAINT uk_copies_inventory UNIQUE(inventory_code),
 CONSTRAINT ck_copies_inventory CHECK (inventory_code REGEXP '^[A-Z0-9._-]+$'),
 CONSTRAINT ck_copies_status CHECK (circulation_status IN ('IN_CIRCULATION','WITHDRAWN')),
 CONSTRAINT fk_copies_book FOREIGN KEY(book_id) REFERENCES books(id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
