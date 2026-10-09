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

test('loads reference data beyond the first page', async () => {
  const { references } = await import('./api');
  const fetchMock = vi.fn().mockResolvedValueOnce(new Response(JSON.stringify({ content: [{ id: 1 }], page: { totalPages: 2 } })))
      .mockResolvedValueOnce(new Response(JSON.stringify({ content: [{ id: 101 }], page: { totalPages: 2 } })));
  vi.stubGlobal('fetch', fetchMock);
  expect(await references('authors', new AbortController().signal)).toEqual([{ id: 1 }, { id: 101 }]);
  expect(fetchMock.mock.calls[1][0]).toBe('/api/authors?page=1&size=100');
});
