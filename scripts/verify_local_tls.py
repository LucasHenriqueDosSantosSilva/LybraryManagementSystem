"""Verify the disposable local HTTPS stack. Requires a fresh, idle login window."""
import http.cookiejar
import json
import os
import ssl
import urllib.error
import urllib.parse
import urllib.request

base = os.environ.get('TLS_TEST_URL', 'https://127.0.0.1:8443')
assert urllib.parse.urlparse(base).hostname in {'127.0.0.1', 'localhost'}
context = ssl.create_default_context(cafile=os.environ['TLS_CA_CERT'])
jar = http.cookiejar.CookieJar()
opener = urllib.request.build_opener(urllib.request.HTTPSHandler(context=context), urllib.request.HTTPCookieProcessor(jar))
def call(path, method='GET', data=None, headers=None, expected=200):
    request = urllib.request.Request(base + path, data=data, method=method, headers=headers or {})
    try:
        response = opener.open(request, timeout=10)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        assert response.status == expected, (path, response.status, expected)
        body = response.read()
        return (json.loads(body) if body else None), response.headers

def token():
    value, _ = call('/api/auth/csrf')
    return {value['headerName']: value['token'], 'Content-Type': 'application/x-www-form-urlencoded'}

def payload(username, password):
    return urllib.parse.urlencode({'username': username, 'password': password}).encode()

try:
    urllib.request.urlopen(base + '/api/health', timeout=10)
except urllib.error.URLError as error:
    assert isinstance(error.reason, ssl.SSLCertVerificationError)
else:
    raise AssertionError('Disposable certificate unexpectedly trusted by the default context')
assert call('/api/health')[0] == {'status': 'UP'}
call('/api/books', expected=401)
username = os.environ.get('LIBRARY_ADMIN_USERNAME', 'bibliotecario')
password = os.environ['LIBRARY_ADMIN_PASSWORD']
call('/api/auth/login', 'POST', payload(username, password), expected=403)
call('/api/auth/login', 'POST', payload(username, password), token(), 204)
assert any(cookie.name == 'JSESSIONID' and cookie.secure for cookie in jar)
assert call('/api/auth/session')[0]['authenticated'] is True
call('/api/books')
call('/api/auth/logout', 'POST', headers=token(), expected=204)
call('/api/books', expected=401)
limit = int(os.environ.get('LIBRARY_LOGIN_LIMIT', '10'))
for i in range(limit - 1):
    call('/api/auth/login', 'POST', payload('invalid-test-' + str(i), 'wrong-test-password'), token(), 401)
headers = token(); headers['X-Forwarded-For'] = '198.51.100.1'
_, response_headers = call('/api/auth/login', 'POST', payload(username, password), headers, 429)
assert int(response_headers['Retry-After']) > 0
print('Verified TLS certificate/hostname, Secure cookie, login/logout, CSRF and 429 Retry-After')
