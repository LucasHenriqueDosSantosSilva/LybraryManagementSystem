CREATE TABLE loans (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 reader_id BIGINT NOT NULL,
 copy_id BIGINT NOT NULL,
 loan_date DATE NOT NULL,
 due_date DATE NOT NULL,
 returned_date DATE NULL,
 active_copy_id BIGINT GENERATED ALWAYS AS (CASE WHEN returned_date IS NULL THEN copy_id ELSE NULL END) VIRTUAL,
 CONSTRAINT uk_loans_active_copy UNIQUE(active_copy_id),
 CONSTRAINT ck_loans_due CHECK (due_date > loan_date),
 CONSTRAINT ck_loans_return CHECK (returned_date IS NULL OR returned_date >= loan_date),
 CONSTRAINT fk_loans_reader FOREIGN KEY(reader_id) REFERENCES readers(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT fk_loans_copy FOREIGN KEY(copy_id) REFERENCES book_copies(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 INDEX ix_loans_reader_date(reader_id,loan_date,id),
 INDEX ix_loans_due(returned_date,due_date),
 INDEX ix_loans_recent(loan_date,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
