"""Destructive only to new UUID-named schemas created by this test; never the application schema."""
import argparse
import json
from mysql_backup import digest
import os
from pathlib import Path
import shutil
import subprocess
import sys
import uuid
from mysql_backup import mysql

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--container', required=True)
parser.add_argument('--application-database', required=True)
parser.add_argument('--directory', required=True, type=Path)
args = parser.parse_args()
os.umask(0o077)
args.directory.mkdir(parents=True, exist_ok=True)
suffix = uuid.uuid4().hex[:12]
source = 'library_restore_source_' + suffix
target = 'library_restore_target_' + suffix
bad = 'library_restore_corrupt_' + suffix
created = []
tables = ['categories', 'authors', 'books', 'book_authors', 'readers', 'book_copies', 'loans', 'flyway_schema_history']
original_counts = [mysql(args.container, f'SELECT COUNT(*) FROM `{t}`;', args.application_database) for t in tables]
script = Path(__file__).with_name('mysql_backup.py')
archive = args.directory / ('fixture-' + suffix + '.sql.gz')
def command(action, database, path, expected=0):
    result = subprocess.run([sys.executable, str(script), action, '--container', args.container, '--database', database, '--file', str(path)], capture_output=True)
    assert result.returncode == expected, 'Backup/restore runner failed; no data printed.'
try:
    mysql(args.container, f'CREATE DATABASE `{source}` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;'); created.append(source)
    for path in sorted(Path(__file__).resolve().parents[1].joinpath('backend/src/main/resources/db/migration').glob('V*.sql')):
        mysql(args.container, path.read_text(), source)
    # Migration metadata only, not the application's business records.
    from mysql_backup import identifier
    application = identifier(args.application_database)
    mysql(args.container, f'CREATE TABLE flyway_schema_history LIKE `{application}`.flyway_schema_history; INSERT INTO flyway_schema_history SELECT * FROM `{application}`.flyway_schema_history;', source)
    mysql(args.container, """
      INSERT INTO categories(id,name) VALUES(1,'Categoria de restore');
      INSERT INTO authors(id,name) VALUES(1,'Autor de restore');
      INSERT INTO books(id,isbn,title,publication_year,category_id) VALUES(1,'9780306406157','Livro de restore',2000,1);
      INSERT INTO book_authors(book_id,author_id) VALUES(1,1);
      INSERT INTO readers(id,registration_number,name,active) VALUES(1,'BACKUP1','Leitor de teste',1),(2,'BACKUP2','Leitor inativo',0);
      INSERT INTO book_copies(id,inventory_code,book_id,circulation_status) VALUES(1,'COPY1',1,'IN_CIRCULATION'),(2,'COPY2',1,'WITHDRAWN');
      INSERT INTO loans(id,reader_id,copy_id,loan_date,due_date,returned_date) VALUES(1,1,1,'2026-10-01','2026-10-15',NULL),(2,2,2,'2026-10-01','2026-10-15','2026-10-03');
    """, source)
    command('backup', source, archive)
    assert archive.stat().st_mode & 0o777 == 0o600
    assert Path(str(archive) + '.json').stat().st_mode & 0o777 == 0o600
    command('restore', target, archive); created.append(target)
    for table in tables:
        assert mysql(args.container, f'SELECT * FROM `{table}` ORDER BY 1;', source) == mysql(args.container, f'SELECT * FROM `{table}` ORDER BY 1;', target), table
        assert mysql(args.container, f'SHOW CREATE TABLE `{table}`;', source) == mysql(args.container, f'SHOW CREATE TABLE `{table}`;', target), table
    assert mysql(args.container, 'SELECT COUNT(*) FROM loans WHERE returned_date IS NULL;', target) == '1'
    assert mysql(args.container, 'SELECT COUNT(*) FROM loans WHERE returned_date IS NOT NULL;', target) == '1'
    try:
        mysql(args.container, "INSERT INTO loans(reader_id,copy_id,loan_date,due_date) VALUES(1,1,'2026-10-01','2026-10-15');", target)
    except RuntimeError as error:
        assert error.mysql_code == 1062
    else:
        raise AssertionError('Active-copy uniqueness missing after restore')
    for statement, code in [
        ("INSERT INTO loans(reader_id,copy_id,loan_date,due_date) VALUES(1,999,'2026-10-01','2026-10-15');", 1452),
        ("INSERT INTO loans(reader_id,copy_id,loan_date,due_date) VALUES(1,2,'2026-10-01','2026-09-30');", 3819),
    ]:
        try:
            mysql(args.container, statement, target)
        except RuntimeError as error:
            assert error.mysql_code == code
        else:
            raise AssertionError('Foreign key/check missing after restore')
    command('backup', source, archive, 1)
    command('restore', args.application_database, archive, 1)
    command('restore', target, archive, 1)
    corrupt = args.directory / ('corrupt-' + suffix + '.sql.gz')
    shutil.copyfile(archive, corrupt); shutil.copyfile(str(archive) + '.json', str(corrupt) + '.json')
    with corrupt.open('ab') as stream: stream.write(b'corrupted-test')
    command('restore', bad, corrupt, 1)
    assert mysql(args.container, f"SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='{bad}';") == '0'
    metadata = Path(str(corrupt) + '.json')
    record = json.loads(metadata.read_text()); record['sha256'] = digest(corrupt); metadata.write_text(json.dumps(record))
    command('restore', bad, corrupt, 1)
    assert mysql(args.container, f"SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='{bad}';") == '0'
    assert original_counts == [mysql(args.container, f'SELECT COUNT(*) FROM `{t}`;', args.application_database) for t in tables]
    print('Passed: private backup, all rows/DDL, active/returned history, UNIQUE/FK/CHECK, overwrite/target refusal, checksum/gzip refusal, application counts unchanged')
finally:
    for database in reversed(created):
        assert database.endswith(suffix) and database.startswith('library_restore_')
        mysql(args.container, f'DROP DATABASE `{database}`;')
