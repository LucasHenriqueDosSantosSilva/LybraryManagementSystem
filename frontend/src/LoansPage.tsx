import { useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { ApiError, references, request, type Book, type Copy, type Loan, type Page, type Reader } from './api';
const labels = { ACTIVE: 'Ativo', OVERDUE: 'Em atraso', RETURNED: 'Devolvido' };
const date = (value: string) => value.split('-').reverse().join('/');
export function LoansPage() {
  const [readers, setReaders] = useState<Reader[]>([]);
  const [copies, setCopies] = useState<Copy[]>([]);
  const [books, setBooks] = useState<Book[]>([]);
  const [ready, setReady] = useState(false);
  const [referenceError, setReferenceError] = useState('');
  const [referenceRevision, setReferenceRevision] = useState(0);
  const [data, setData] = useState<Page<Loan>>();
  const [loadError, setLoadError] = useState('');
  const [page, setPage] = useState(0);
  const [revision, setRevision] = useState(0);
  const [draft, setDraft] = useState({ readerId: '', status: '' });
  const [filters, setFilters] = useState(draft);
  const [form, setForm] = useState({ readerId: '', copyId: '' });
  const [returning, setReturning] = useState<Loan>();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  useEffect(() => {
    const controller = new AbortController(); setReady(false); setReferenceError('');
    Promise.all([references<Reader>('readers', controller.signal), references<Copy>('copies', controller.signal), references<Book>('books', controller.signal)])
      .then(([r, c, b]) => { setReaders(r); setCopies(c); setBooks(b); setReady(true); })
      .catch(e => { if (!controller.signal.aborted) setReferenceError(e.message); });
    return () => controller.abort();
  }, [referenceRevision]);
  useEffect(() => {
    const controller = new AbortController(); setData(undefined); setLoadError('');
    const params = new URLSearchParams({ page: String(page), size: '10' });
    Object.entries(filters).forEach(([key, value]) => { if (value) params.set(key, value); });
    request<Page<Loan>>(`/loans?${params}`, { signal: controller.signal }).then(result => {
      if (!result.content.length && page > 0) setPage(page - 1); else setData(result);
    }).catch(e => { if (!controller.signal.aborted) setLoadError(e.message); });
    return () => controller.abort();
  }, [filters, page, revision]);
  function refresh() { setRevision(r => r + 1); setReferenceRevision(r => r + 1); }
  function failed(e: unknown) {
    setError(e instanceof ApiError && e.status === 0 ? 'A resposta não foi recebida. Atualize e consulte o histórico antes de tentar novamente: a operação pode ter sido registrada.' : (e as Error).message);
  }
  async function borrow(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError(''); setNotice('');
    try {
      const result = await request<Loan>('/loans', { method: 'POST', body: JSON.stringify({ readerId: Number(form.readerId), copyId: Number(form.copyId) }) });
      setNotice(`Empréstimo #${result.id} registrado. Vencimento: ${date(result.dueDate)}.`); setForm({ readerId: '', copyId: '' }); refresh();
    } catch (e) { failed(e); } finally { setBusy(false); }
  }
  async function returnLoan() {
    if (!returning) return; setBusy(true); setError(''); setNotice('');
    try {
      const result = await request<Loan>(`/loans/${returning.id}/return`, { method: 'POST' });
      setReturning(undefined); setNotice(`Empréstimo #${result.id} devolvido em ${date(result.returnedDate!)}.`); refresh();
    } catch (e) { failed(e); } finally { setBusy(false); }
  }
  const activeReaders = readers.filter(r => r.active);
  const availableCopies = copies.filter(c => c.available && c.circulationStatus === 'IN_CIRCULATION');
  const readerName = (id: number) => readers.find(r => r.id === id)?.name || `Leitor #${id}`;
  const copyName = (id: number) => copies.find(c => c.id === id)?.inventoryCode || `Exemplar #${id}`;
  const bookName = (id: number) => books.find(b => b.id === id)?.title || `Livro #${id}`;
  return <section><p className="eyebrow">CIRCULAÇÃO</p><h1>Empréstimos e devoluções</h1><p>Acompanhe o histórico e a circulação do acervo.</p><button disabled={busy} onClick={refresh}>Atualizar dados</button><p role="status">{notice}</p>{error && <p role="alert" className="error">{error}</p>}
    {referenceError ? <p role="alert">{referenceError} <button disabled={busy} onClick={() => setReferenceRevision(r => r + 1)}>Recarregar cadastros</button></p> : !ready ? <p role="status">Carregando cadastros…</p> : null}
    {returning ? <div className="card"><h2>Confirmar devolução do empréstimo #{returning.id}?</h2><p>{readerName(returning.readerId)} · {copyName(returning.copyId)} · {bookName(returning.bookId)}</p><p>A data de devolução será registrada pela biblioteca.</p><button disabled={busy} onClick={returnLoan}>Confirmar devolução</button><button disabled={busy} onClick={() => { setReturning(undefined); setError(''); }}>Cancelar</button></div> : <form className="card" onSubmit={borrow}><h2>Novo empréstimo</h2><fieldset disabled={busy || !ready || !activeReaders.length || !availableCopies.length}><legend className="sr-only">Dados do empréstimo</legend><label>Leitor ativo<select required value={form.readerId} onChange={e => setForm({ ...form, readerId: e.target.value })}><option value="">Selecione</option>{activeReaders.map(r => <option key={r.id} value={r.id}>{r.name} · {r.registrationNumber}</option>)}</select></label><label>Exemplar disponível<select required value={form.copyId} onChange={e => setForm({ ...form, copyId: e.target.value })}><option value="">Selecione</option>{availableCopies.map(c => <option key={c.id} value={c.id}>{c.inventoryCode} · {bookName(c.bookId)}</option>)}</select></label><p>O prazo e as datas são definidos pela biblioteca.</p><button className="primary">{busy ? 'Registrando…' : 'Registrar empréstimo'}</button></fieldset>{ready && !activeReaders.length && <p>Nenhum leitor ativo disponível. Consulte <Link to="/readers">leitores</Link>.</p>}{ready && !availableCopies.length && <p>Nenhum exemplar disponível. Consulte <Link to="/copies">exemplares</Link>.</p>}</form>}
    <form className="card" onSubmit={e => { e.preventDefault(); setPage(0); setFilters({ ...draft }); }}><h2>Consultar histórico</h2><label>Leitor do histórico<select disabled={!ready || busy} value={draft.readerId} onChange={e => setDraft({ ...draft, readerId: e.target.value })}><option value="">Todos</option>{readers.map(r => <option key={r.id} value={r.id}>{r.name} · {r.registrationNumber}{!r.active ? ' (inativo)' : ''}</option>)}</select></label><label>Situação<select value={draft.status} disabled={busy} onChange={e => setDraft({ ...draft, status: e.target.value })}><option value="">Todas</option><option value="ACTIVE">Não devolvidos (inclui atrasados)</option><option value="OVERDUE">Em atraso</option><option value="RETURNED">Devolvidos</option></select></label><button disabled={busy}>Filtrar histórico</button><button type="button" disabled={busy} onClick={() => { const empty = { readerId: '', status: '' }; setDraft(empty); setFilters(empty); setPage(0); }}>Limpar filtros</button></form>
    {loadError ? <p role="alert">{loadError} <button disabled={busy} onClick={() => setRevision(r => r + 1)}>Tentar novamente</button></p> : !data ? <p role="status">Carregando histórico…</p> : <div className="card"><h2>Histórico ({data.page.totalElements})</h2>{!data.content.length ? <p>Nenhum empréstimo encontrado.</p> : <ul className="resource-list">{data.content.map(loan => <li key={loan.id}><div><strong>Empréstimo #{loan.id} · {labels[loan.status]}</strong><small>{readerName(loan.readerId)} · {copyName(loan.copyId)} · {bookName(loan.bookId)}</small><small>Empréstimo: {date(loan.loanDate)} · Vencimento: {date(loan.dueDate)}</small>{loan.returnedDate && <small>Devolução: {date(loan.returnedDate)}</small>}</div>{!loan.returnedDate && <button disabled={busy || !!returning} onClick={() => { setError(''); setReturning(loan); }}>Devolver <span className="sr-only">empréstimo #{loan.id}</span></button>}</li>)}</ul>}<nav aria-label="Paginação do histórico"><button disabled={!page || busy} onClick={() => setPage(page - 1)}>Anterior</button><span>Página {page + 1} de {Math.max(1, data.page.totalPages)}</span><button disabled={page + 1 >= data.page.totalPages || busy} onClick={() => setPage(page + 1)}>Próxima</button></nav></div>}
  </section>;
}
