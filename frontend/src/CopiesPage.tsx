import { useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { ApiError, references, request, type Book, type Copy } from './api';
import { usePagedResource } from './usePagedResource';
const initial = { inventoryCode: '', bookId: '' };
export function CopiesPage() {
  const list = usePagedResource<Copy>('copies');
  const [books, setBooks] = useState<Book[]>([]);
  const [ready, setReady] = useState(false);
  const [referenceError, setReferenceError] = useState('');
  const [attempt, setAttempt] = useState(0);
  const [form, setForm] = useState(initial);
  const [editing, setEditing] = useState<Copy>();
  const [action, setAction] = useState<{ copy: Copy; kind: 'delete' | 'withdrawal' }>();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [fields, setFields] = useState<{ field: string; message: string }[]>([]);
  const [notice, setNotice] = useState('');
  useEffect(() => {
    const controller = new AbortController(); setReady(false); setReferenceError('');
    references<Book>('books', controller.signal).then(result => { setBooks(result); setReady(true); })
      .catch(e => { if (!controller.signal.aborted) setReferenceError(e.message); });
    return () => controller.abort();
  }, [attempt]);
  function reset() { setEditing(undefined); setForm(initial); setError(''); setFields([]); }
  async function save(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError(''); setFields([]); setNotice('');
    try {
      await request<Copy>(editing ? `/copies/${editing.id}` : '/copies', { method: editing ? 'PUT' : 'POST', body: JSON.stringify({ inventoryCode: form.inventoryCode, bookId: Number(form.bookId) }) });
      reset(); setNotice('Exemplar salvo.'); list.reload();
    } catch (e) { setError((e as Error).message); if (e instanceof ApiError) setFields(e.fields); } finally { setBusy(false); }
  }
  async function confirm() {
    if (!action) return; setBusy(true); setError(''); setNotice('');
    try {
      await request(`/copies/${action.copy.id}${action.kind === 'withdrawal' ? '/withdrawal' : ''}`, { method: action.kind === 'withdrawal' ? 'POST' : 'DELETE' });
      setNotice(action.kind === 'withdrawal' ? 'Exemplar retirado de circulação.' : 'Exemplar excluído.'); setAction(undefined); reset(); list.reload();
    } catch (e) { setError((e as Error).message); } finally { setBusy(false); }
  }
  const title = (id: number) => books.find(b => b.id === id)?.title || `Livro #${id}`;
  return <section><p className="eyebrow">ACERVO FÍSICO</p><h1>Exemplares</h1><p>Cada exemplar tem um código patrimonial e disponibilidade própria.</p><p role="status">{notice}</p>{error && <p role="alert" className="error">{error}</p>}
    {referenceError ? <p role="alert">{referenceError} <button onClick={() => setAttempt(n => n + 1)}>Recarregar livros</button></p> : !ready ? <p role="status">Carregando livros…</p> : !books.length ? <p>Cadastre um <Link to="/books">livro</Link> antes de adicionar exemplares.</p> : null}
    {action ? <div className="card"><h2>{action.kind === 'withdrawal' ? 'Retirar de circulação' : 'Excluir'} “{action.copy.inventoryCode}”?</h2><p>{action.kind === 'withdrawal' ? 'A retirada não pode ser desfeita nesta versão e exige exemplar sem empréstimo ativo.' : 'Exemplares com histórico não podem ser excluídos.'}</p><button disabled={busy} onClick={confirm}>Confirmar</button><button disabled={busy} onClick={() => { setAction(undefined); setError(''); }}>Cancelar</button></div> : <form className="card" onSubmit={save}><h2>{editing ? 'Editar exemplar' : 'Novo exemplar'}</h2><fieldset disabled={busy || !ready || !books.length}><legend className="sr-only">Dados do exemplar</legend><label>Código patrimonial<input required maxLength={40} pattern=" *[A-Za-z0-9._-]+ *" value={form.inventoryCode} aria-invalid={fields.some(f => f.field === 'inventoryCode')} aria-describedby="copy-errors" onChange={e => setForm({ ...form, inventoryCode: e.target.value })} /></label><small>Use letras ASCII, números, ponto, hífen ou sublinhado.</small><label>Livro<select required value={form.bookId} aria-invalid={fields.some(f => f.field === 'bookId')} aria-describedby="copy-errors" onChange={e => setForm({ ...form, bookId: e.target.value })}><option value="">Selecione</option>{books.map(book => <option key={book.id} value={book.id}>{book.title} · {book.isbn}</option>)}</select></label><ul id="copy-errors" className="error">{fields.map((f, i) => <li key={i}>{f.field}: {f.message}</li>)}</ul><button className="primary">{busy ? 'Salvando…' : 'Salvar exemplar'}</button>{editing && <button type="button" onClick={reset}>Cancelar edição</button>}</fieldset></form>}
    {list.error ? <p role="alert">{list.error} <button onClick={list.reload}>Tentar novamente</button></p> : !list.data ? <p role="status">Carregando exemplares…</p> : <div className="card"><h2>Exemplares cadastrados ({list.data.page.totalElements})</h2>{!list.data.content.length ? <p>Nenhum exemplar cadastrado.</p> : <ul className="resource-list">{list.data.content.map(copy => <li key={copy.id}><div><strong>{copy.inventoryCode}</strong><small>{title(copy.bookId)}</small><small>{copy.circulationStatus === 'WITHDRAWN' ? 'Retirado de circulação' : copy.available ? 'Disponível' : 'Emprestado'}</small></div><div><button disabled={busy || !!action || !ready} onClick={() => { reset(); setEditing(copy); setForm({ inventoryCode: copy.inventoryCode, bookId: String(copy.bookId) }); }}>Editar <span className="sr-only">{copy.inventoryCode}</span></button><button disabled={busy || !!action || !copy.available || copy.circulationStatus === 'WITHDRAWN'} onClick={() => { reset(); setAction({ copy, kind: 'withdrawal' }); }}>Retirar <span className="sr-only">{copy.inventoryCode}</span></button><button disabled={busy || !!action} onClick={() => { reset(); setAction({ copy, kind: 'delete' }); }}>Excluir <span className="sr-only">{copy.inventoryCode}</span></button></div></li>)}</ul>}<nav aria-label="Paginação de exemplares"><button disabled={!list.page || busy} onClick={() => list.setPage(list.page - 1)}>Anterior</button><span>Página {list.page + 1} de {Math.max(1, list.data.page.totalPages)}</span><button disabled={list.page + 1 >= list.data.page.totalPages || busy} onClick={() => list.setPage(list.page + 1)}>Próxima</button></nav></div>}
  </section>;
}
