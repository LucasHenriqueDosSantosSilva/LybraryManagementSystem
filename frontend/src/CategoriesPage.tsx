import { useEffect, useRef, useState, type FormEvent } from 'react';
import { ApiError, request, type Category, type Page } from './api';
export function NamedRecordsPage({ resource = "categories" }: { resource?: "categories" | "authors" }) {
  const author = resource === "authors";
  const singular = author ? "Autor" : "Categoria";
  const title = author ? "Autores" : "Categorias";
  const [data, setData] = useState<Page<Category>>();
  const [page, setPage] = useState(0);
  const [revision, setRevision] = useState(0);
  const [loadError, setLoadError] = useState('');
  const [name, setName] = useState('');
  const [editing, setEditing] = useState<Category>();
  const [removing, setRemoving] = useState<Category>();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [fieldError, setFieldError] = useState('');
  const [notice, setNotice] = useState('');
  const input = useRef<HTMLInputElement>(null);
  useEffect(() => {
    const controller = new AbortController(); setData(undefined); setLoadError('');
    request<Page<Category>>(`/${resource}?page=${page}&size=10`, { signal: controller.signal }).then(result => {
      if (result.content.length === 0 && page > 0) setPage(page - 1); else setData(result);
    }).catch(e => { if (!controller.signal.aborted) setLoadError(e.message); });
    return () => controller.abort();
  }, [page, revision, resource]);
  function reset() { setEditing(undefined); setName(''); setError(''); setFieldError(''); }
  async function save(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError(''); setFieldError(''); setNotice('');
    try {
      await request<Category>(editing ? `/${resource}/${editing.id}` : `/${resource}`, { method: editing ? 'PUT' : 'POST', body: JSON.stringify({ name }) });
      reset(); setNotice(author ? 'Autor salvo.' : 'Categoria salva.'); setRevision(revision + 1); input.current?.focus();
    } catch (e) { setError((e as Error).message); if (e instanceof ApiError) setFieldError(e.fields.find(f => f.field === 'name')?.message || ''); }
    finally { setBusy(false); }
  }
  async function remove() {
    if (!removing) return; setBusy(true); setError(''); setNotice('');
    try { await request<void>(`/${resource}/${removing.id}`, { method: 'DELETE' }); setRemoving(undefined); setNotice(author ? 'Autor excluído.' : 'Categoria excluída.'); setRevision(revision + 1); input.current?.focus(); }
    catch (e) { setError((e as Error).message); } finally { setBusy(false); }
  }
  return <section><p className="eyebrow">ORGANIZAÇÃO DO CATÁLOGO</p><h1>{title}</h1><p>{author ? "Cadastre quem escreve os títulos do catálogo." : "Organize os títulos por assunto."}</p>
    <p role="status">{notice}</p>{error && <p role="alert" className="error">{error}</p>}
    {removing ? <div className="card"><h2>Excluir “{removing.name}”?</h2><p>{title} vinculados a livros não podem ser excluídos.</p><button disabled={busy} onClick={remove}>Confirmar exclusão</button> <button disabled={busy} onClick={() => { setRemoving(undefined); setError(''); input.current?.focus(); }}>Cancelar</button></div> : <form className="card" onSubmit={save}><h2>{editing ? `Editar ${singular.toLowerCase()}` : author ? 'Novo autor' : 'Nova categoria'}</h2><label htmlFor="name">Nome</label><input ref={input} id="name" required maxLength={author ? 150 : 100} value={name} disabled={busy} aria-invalid={!!fieldError} aria-describedby={fieldError ? 'name-error' : undefined} onChange={e => setName(e.target.value)} /><span id="name-error" className="error">{fieldError}</span><div><button className="primary" disabled={busy}>{busy ? 'Salvando…' : `Salvar ${singular.toLowerCase()}`} </button>{editing && <button type="button" disabled={busy} onClick={reset}>Cancelar edição</button>}</div></form>}
    {loadError ? <p role="alert">{loadError} <button onClick={() => setRevision(revision + 1)}>Tentar novamente</button></p> : !data ? <p role="status">Carregando categorias…</p> : <div className="card"><h2>Lista de {title.toLowerCase()} <small>({data.page.totalElements})</small></h2>{data.content.length ? <ul className="resource-list">{data.content.map(category => <li key={category.id}><span>{category.name}</span><div><button disabled={busy || !!removing} onClick={() => { reset(); setEditing(category); setName(category.name); input.current?.focus(); }}>Editar <span className="sr-only">{category.name}</span></button><button disabled={busy || !!removing} onClick={() => { reset(); setRemoving(category); }}>Excluir <span className="sr-only">{category.name}</span></button></div></li>)}</ul> : <p>{author ? "Nenhum autor cadastrado." : "Nenhuma categoria cadastrada."} Comece pelo formulário acima.</p>}<nav aria-label={`Paginação de ${title.toLowerCase()}`}><button disabled={page === 0 || busy} onClick={() => setPage(page - 1)}>Anterior</button><span> Página {page + 1} de {Math.max(1, data.page.totalPages)} </span><button disabled={page + 1 >= data.page.totalPages || busy} onClick={() => setPage(page + 1)}>Próxima</button></nav></div>}
  </section>;
}

export function CategoriesPage() { return <NamedRecordsPage />; }
export function AuthorsPage() { return <NamedRecordsPage resource="authors" />; }
