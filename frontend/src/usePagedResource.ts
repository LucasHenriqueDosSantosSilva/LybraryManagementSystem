import { useEffect, useState } from 'react';
import { request, type Page } from './api';
export function usePagedResource<T>(resource: string) {
  const [page, setPage] = useState(0);
  const [data, setData] = useState<Page<T>>();
  const [error, setError] = useState('');
  const [revision, setRevision] = useState(0);
  useEffect(() => {
    const controller = new AbortController(); setData(undefined); setError('');
    request<Page<T>>(`/${resource}?page=${page}&size=10`, { signal: controller.signal })
      .then(result => { if (!result.content.length && page > 0) setPage(page - 1); else setData(result); })
      .catch(e => { if (!controller.signal.aborted) setError(e.message); });
    return () => controller.abort();
  }, [resource, page, revision]);
  return { data, error, page, setPage, reload: () => setRevision(r => r + 1) };
}
