#!/usr/bin/env bash
set -euo pipefail
umask 077
tls_dir="${TLS_TEST_DIR:-/tmp/library-local-tls-test}"
tls_volume="${TLS_VOLUME:-library-local-tls-test}"
case "$tls_volume" in library-local-tls-*) ;; *) echo 'Use a library-local-tls-* disposable volume.' >&2; exit 1 ;; esac
mkdir -p "$tls_dir"
chmod 700 "$tls_dir"
if [ ! -s "$tls_dir/key.pem" ] || ! openssl x509 -checkend 3600 -noout -in "$tls_dir/cert.pem" >/dev/null 2>&1; then
  openssl req -x509 -newkey rsa:2048 -sha256 -days 1 -nodes \
    -keyout "$tls_dir/key.pem" -out "$tls_dir/cert.pem" -subj '/CN=localhost' \
    -addext 'subjectAltName=DNS:localhost,IP:127.0.0.1' > "$tls_dir/generation.log" 2>&1
fi
chmod 600 "$tls_dir/key.pem"
docker volume create "$tls_volume" >/dev/null
docker run --rm --user 0 -v "$tls_dir:/source:ro" -v "$tls_volume:/tls" \
  nginx@sha256:30f1c0d78e0ad60901648be663a710bdadf19e4c10ac6782c235200619158284 \
  sh -c 'cp /source/cert.pem /source/key.pem /tls/ && chown 101:101 /tls/cert.pem /tls/key.pem && chmod 600 /tls/key.pem && chmod 644 /tls/cert.pem'
echo 'Local disposable certificate prepared. Recreate/reload frontend before verifying TLS.'
