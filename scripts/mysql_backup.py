"""Private logical MySQL backup and restoration into a NEW isolated schema only."""
import argparse
import datetime
import gzip
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import tempfile


def identifier(value):
    if not re.fullmatch(r'[a-z][a-z0-9_]{0,63}', value) or value in {'mysql', 'sys', 'information_schema', 'performance_schema'}:
        raise ValueError('Use a non-system lowercase database identifier, at most 64 characters.')
    return value


def mysql(container, sql, database=None):
    command = ['docker', 'exec', '-i', container, 'sh', '-c',
               'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot --batch --skip-column-names "$@"', 'sh']
    if database:
        command.append(identifier(database))
    result = subprocess.run(command, input=sql.encode(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if result.returncode:
        error = RuntimeError('MySQL administrative check failed; no SQL or credentials printed.')
        code = re.search(rb'ERROR (\d+)', result.stderr)
        error.mysql_code = int(code.group(1)) if code else None
        raise error
    return result.stdout.decode().strip()


def digest(path):
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def backup(container, database, destination):
    database = identifier(database)
    destination = destination.resolve()
    metadata = Path(str(destination) + '.json')
    if destination.exists() or metadata.exists():
        raise ValueError('Destination or metadata already exists; nothing overwritten.')
    if not destination.parent.is_dir():
        raise ValueError('Create a private backup directory before running.')
    if mysql(container, f"SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='{database}';") != '1':
        raise ValueError('Source database does not exist.')
    fd, temporary = tempfile.mkstemp(prefix='.mysql-backup-', dir=destination.parent)
    temporary = Path(temporary)
    error_log = Path(str(destination) + '.dump.log')
    try:
        with os.fdopen(fd, 'wb') as raw, gzip.GzipFile(fileobj=raw, mode='wb') as archive, error_log.open('xb') as errors:
            process = subprocess.Popen(['docker', 'exec', container, 'sh', '-c',
                'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysqldump -uroot --single-transaction --quick --triggers --routines --events --no-tablespaces --set-gtid-purged=OFF --skip-add-drop-table "$1"',
                'sh', database], stdout=subprocess.PIPE, stderr=errors)
            try:
                for block in iter(lambda: process.stdout.read(1024 * 1024), b''):
                    archive.write(block)
                if process.wait() != 0:
                    raise RuntimeError('Dump failed; inspect the private .dump.log file.')
            finally:
                if process.poll() is None:
                    process.kill(); process.wait()
        with gzip.open(temporary, 'rb') as archive:
            for _ in iter(lambda: archive.read(1024 * 1024), b''):
                pass
        record = {'format': 1, 'database': database, 'created_utc': datetime.datetime.now(datetime.timezone.utc).isoformat(),
                  'sha256': digest(temporary), 'compressed_bytes': temporary.stat().st_size}
        os.link(temporary, destination)  # Atomic publication, refuses an existing destination.
        with metadata.open('x') as stream:
            json.dump(record, stream, indent=2)
        if error_log.stat().st_size == 0:
            error_log.unlink()
        print('Backup created:', destination)
    finally:
        temporary.unlink(missing_ok=True)


def restore(container, source, database):
    database = identifier(database)
    if not database.startswith('library_restore_'):
        raise ValueError('Restore target must start with library_restore_.')
    record = json.loads(Path(str(source) + '.json').read_text())
    if record.get('format') != 1 or digest(source) != record.get('sha256'):
        raise ValueError('Checksum/format mismatch; target not created.')
    if mysql(container, f"SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='{database}';") != '0':
        raise ValueError('Restore target already exists; nothing imported.')
    # Validate complete gzip before creating a schema, including its CRC/trailer.
    with gzip.open(source, 'rb') as archive:
        for _ in iter(lambda: archive.read(1024 * 1024), b''):
            pass
    mysql(container, f'CREATE DATABASE `{database}` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;')
    log = Path(str(source) + '.restore-' + database + '.log')
    with log.open('x') as errors, gzip.open(source, 'rb') as archive:
        process = subprocess.Popen(['docker', 'exec', '-i', container, 'sh', '-c',
            'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot "$1"', 'sh', database], stdin=subprocess.PIPE, stdout=subprocess.DEVNULL, stderr=errors)
        try:
            for block in iter(lambda: archive.read(1024 * 1024), b''):
                process.stdin.write(block)
            process.stdin.close()
            if process.wait() != 0:
                raise RuntimeError('Import failed; new partial schema retained for inspection. See private restore log.')
        finally:
            if process.poll() is None:
                process.kill(); process.wait()
    if log.stat().st_size == 0:
        log.unlink()
    print('Restored into new schema:', database)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action', choices=['backup', 'restore'])
    parser.add_argument('--container', required=True)
    parser.add_argument('--database', required=True)
    parser.add_argument('--file', required=True, type=Path)
    args = parser.parse_args()
    os.umask(0o077)
    if args.action == 'backup':
        backup(args.container, args.database, args.file)
    else:
        restore(args.container, args.file, args.database)


if __name__ == '__main__':
    try:
        main()
    except (ValueError, RuntimeError, OSError) as error:
        print(type(error).__name__ + ': ' + str(error), file=sys.stderr)
        raise SystemExit(1)
