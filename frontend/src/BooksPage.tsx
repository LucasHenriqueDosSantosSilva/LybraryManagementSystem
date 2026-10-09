import { useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { ApiError, request, references, type Author, type Book, type Category, type Page } from './api';
const initial = { title: '', isbn: '', description: '', publicationYear: '', categoryId: '', authorIds: [] as number[] };
export function BooksPage() {
  const [data, setData] = useState<Page<Book>>();
  const [categories, setCategories] = useState<Category[]>([]);
  const [authors, setAuthors] = useState<Author[]>([]);
  const [ready, setReady] = useState(false);
  const [referenceError, setReferenceError] = useState('');
  const [referenceAttempt, setReferenceAttempt] = useState(0);
  const [loadError, setLoadError] = useState('');
  const [draft, setDraft] = useState({ title: '', author: '', isbn: '', categoryId: '' });
  const [filters, setFilters] = useState(draft);
  const [page, setPage] = useState(0);
  const [revision, setRevision] = useState(0);
  const [form, setForm] = useState(initial);
  const [editing, setEditing] = useState<Book>();
  const [removing, setRemoving] = useState<Book>();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [fields, setFields] = useState<{ field: string; message: string }[]>([]);
  const [notice, setNotice] = useState('');
  useEffect(() => {
    const controller = new AbortController(); setReady(false); setReferenceError('');
    Promise.all([references<Category>('categories', controller.signal), references<Author>('authors', controller.signal)])
      .then(([c, a]) => { setCategories(c); setAuthors(a); setReady(true); })
      .catch(e => { if (!controller.signal.aborted) setReferenceError(e.message); });
    return () => controller.abort();
  }, [referenceAttempt]);
  useEffect(() => {
    const controller = new AbortController(); setData(undefined); setLoadError('');
    const params = new URLSearchParams({ page: String(page), size: '10' });
    Object.entries(filters).forEach(([key, value]) => { if (value.trim()) params.set(key, value.trim()); });
    request<Page<Book>>(`/books?${params}`, { signal: controller.signal }).then(result => {
      if (!result.content.length && page > 0) setPage(page - 1); else setData(result);
    }).catch(e => { if (!controller.signal.aborted) setLoadError(e.message); });
    return () => controller.abort();
  }, [filters, page, revision]);
  function reset() { setEditing(undefined); setForm(initial); setFields([]); setError(''); }
  async function save(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError(''); setFields([]); setNotice('');
    try {
      await request<Book>(editing ? `/books/${editing.id}` : '/books', { method: editing ? 'PUT' : 'POST', body: JSON.stringify({ ...form, publicationYear: Number(form.publicationYear), categoryId: Number(form.categoryId) }) });
      reset(); setNotice('Livro salvo.'); setRevision(r => r + 1);
    } catch (e) { setError((e as Error).message); if (e instanceof ApiError) setFields(e.fields); }
    finally { setBusy(false); }
  }
  async function remove() {
    if (!removing) return; setBusy(true); setError(''); setNotice('');
    try { await request(`/books/${removing.id}`, { method: 'DELETE' }); setRemoving(undefined); setNotice('Livro excluído.'); setRevision(r => r + 1); }
    catch (e) { setError((e as Error).message); } finally { setBusy(false); }
  }
  function edit(book: Book) {
    reset(); setEditing(book); setForm({ title: book.title, isbn: book.isbn, description: book.description || '', publicationYear: String(book.publicationYear), categoryId: String(book.categoryId), authorIds: book.authors.map(a => a.id) });
  }
  function invalid(field: string) { return fields.some(e => e.field === field); }
  return <section><p className="eyebrow">CATÁLOGO</p><h1>Livros</h1><p>Encontre e organize as edições da biblioteca.</p>
    <form className="card filters" onSubmit={e => { e.preventDefault(); setPage(0); setFilters({ ...draft }); }}><h2>Buscar livros</h2>{(['title', 'author', 'isbn'] as const).map(key => <label key={key}>{ { title: 'Título da busca', author: 'Autor da busca', isbn: 'ISBN da busca' }[key]}<input value={draft[key]} onChange={e => setDraft({ ...draft, [key]: e.target.value })} /></label>)}<label>Categoria da busca<select value={draft.categoryId} onChange={e => setDraft({ ...draft, categoryId: e.target.value })}><option value="">Todas</option>{categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}</select></label><button>Buscar</button><button type="button" onClick={() => { const empty = { title: '', author: '', isbn: '', categoryId: '' }; setDraft(empty); setFilters(empty); setPage(0); }}>Limpar busca</button></form>
    <p role="status">{notice}</p>{error && <p role="alert" className="error">{error}</p>}
    {referenceError ? <p role="alert">{referenceError} <button onClick={() => setReferenceAttempt(n => n + 1)}>Recarregar autores e categorias</button></p> : !ready ? <p role="status">Carregando autores e categorias…</p> : !authors.length || !categories.length ? <p>Para cadastrar livros, cadastre uma <Link to="/categories">categoria</Link> e um <Link to="/authors">autor</Link>.</p> : null}
    {removing ? <div className="card"><h2>Excluir “{removing.title}”?</h2><p>Livros com exemplares ou histórico não podem ser excluídos.</p><button disabled={busy} onClick={remove}>Confirmar exclusão</button><button disabled={busy} onClick={() => { setRemoving(undefined); setError(''); }}>Cancelar</button></div> : <form className="card" onSubmit={save}><h2>{editing ? 'Editar livro' : 'Novo livro'}</h2><fieldset disabled={busy || !ready || !categories.length || !authors.length}><legend className="sr-only">Dados do livro</legend>
      {(['title', 'isbn', 'publicationYear'] as const).map(key => <label key={key}>{{ title: 'Título', isbn: 'ISBN', publicationYear: 'Ano de publicação' }[key]}<input required type={key === 'publicationYear' ? 'number' : 'text'} min={key === 'publicationYear' ? 1 : undefined} max={key === 'publicationYear' ? 9999 : undefined} maxLength={key === 'title' ? 250 : key === 'isbn' ? 30 : undefined} value={form[key]} aria-invalid={invalid(key)} aria-describedby={invalid(key) ? 'book-errors' : undefined} onChange={e => setForm({ ...form, [key]: e.target.value })} /></label>)}
      <label>Descrição<textarea maxLength={5000} value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} /></label>
      <label>Categoria<select required value={form.categoryId} aria-invalid={invalid('categoryId')} onChange={e => setForm({ ...form, categoryId: e.target.value })}><option value="">Selecione</option>{categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}</select></label>
      <label>Autores<select multiple required value={form.authorIds.map(String)} aria-invalid={invalid('authorIds')} aria-describedby="authors-help" onChange={e => setForm({ ...form, authorIds: Array.from(e.target.selectedOptions, o => Number(o.value)) })}>{authors.map(a => <option key={a.id} value={a.id}>{a.name} · #{a.id}</option>)}</select></label><small id="authors-help">Selecione um ou mais. No computador, use Ctrl ou Command para selecionar vários autores.</small>
      <ul id="book-errors" className="error">{fields.map((e, i) => <li key={i}>{e.field}: {e.message}</li>)}</ul><button className="primary">{busy ? 'Salvando…' : 'Salvar livro'}</button>{editing && <button type="button" onClick={reset}>Cancelar edição</button>}</fieldset></form>}
    {loadError ? <p role="alert">{loadError} <button onClick={() => setRevision(r => r + 1)}>Tentar novamente</button></p> : !data ? <p role="status">Carregando livros…</p> : <div className="card"><h2>Livros encontrados ({data.page.totalElements})</h2>{!data.content.length ? <p>Nenhum livro encontrado.</p> : <ul className="resource-list">{data.content.map(book => <li key={book.id}><div><strong>{book.title}</strong><small>{book.authors.map(a => a.name).join(', ')} · {book.publicationYear}</small><small>ISBN {book.isbn}</small></div><div><button disabled={busy || !!removing || !ready} onClick={() => edit(book)}>Editar <span className="sr-only">{book.title}</span></button><button disabled={busy || !!removing} onClick={() => { reset(); setRemoving(book); }}>Excluir <span className="sr-only">{book.title}</span></button></div></li>)}</ul>}<nav aria-label="Paginação de livros"><button disabled={!page || busy} onClick={() => setPage(page - 1)}>Anterior</button><span>Página {page + 1} de {Math.max(1, data.page.totalPages)}</span><button disabled={page + 1 >= data.page.totalPages || busy} onClick={() => setPage(page + 1)}>Próxima</button></nav></div>}
  </section>;
}
