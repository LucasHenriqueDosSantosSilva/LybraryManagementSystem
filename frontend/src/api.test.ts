import { afterEach, expect, test, vi } from 'vitest';
import { ApiError, request } from './api';
afterEach(() => vi.unstubAllGlobals());
test('does not parse JSON after 204', async () => {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(null, { status: 204 })));
  expect(await request('/categories/1', { method: 'DELETE' })).toBeUndefined();
});
test('retains status and field errors from ProblemDetail', async () => {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({ detail: 'Revise os campos.', errors: [{ field: 'name', message: 'Informe o nome.' }] }), { status: 400 })));
  await expect(request('/categories')).rejects.toMatchObject({ status: 400, fields: [{ field: 'name', message: 'Informe o nome.' }] });
});
test('reports non-JSON server failure with a usable fallback', async () => {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('unavailable', { status: 502 })));
  await expect(request('/categories')).rejects.toBeInstanceOf(ApiError);
});
