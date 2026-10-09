import { useState, type FormEvent } from 'react';
import { ApiError, request, type Reader } from './api';
import { usePagedResource } from './usePagedResource';
const initial = { registrationNumber: '', name: '', email: '' };
export function ReadersPage() {
  const list = usePagedResource<Reader>('readers');
  const [form, setForm] = useState(initial);
  const [editing, setEditing] = useState<Reader>();
  const [action, setAction] = useState<{ reader: Reader; kind: 'delete' | 'status' }>();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [fields, setFields] = useState<{ field: string; message: string }[]>([]);
  const [notice, setNotice] = useState('');
  function reset() { setForm(initial); setEditing(undefined); setError(''); setFields([]); }
  async function save(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError(''); setFields([]); setNotice('');
    try {
      await request<Reader>(editing ? `/readers/${editing.id}` : '/readers', { method: editing ? 'PUT' : 'POST', body: JSON.stringify({ ...form, email: form.email.trim() || null }) });
      reset(); setNotice('Leitor salvo.'); list.reload();
    } catch (e) { setError((e as Error).message); if (e instanceof ApiError) setFields(e.fields); }
    finally { setBusy(false); }
  }
  async function confirm() {
    if (!action) return; setBusy(true); setError(''); setNotice('');
    try {
      const { reader, kind } = action;
      await request(`/readers/${reader.id}${kind === 'status' ? '/status' : ''}`, kind === 'status' ? { method: 'PATCH', body: JSON.stringify({ active: !reader.active }) } : { method: 'DELETE' });
      setAction(undefined); reset(); setNotice(kind === 'delete' ? 'Leitor excluído.' : 'Situação do leitor atualizada.'); list.reload();
    } catch (e) { setError((e as Error).message); } finally { setBusy(false); }
  }
  return <section><p className="eyebrow">ATENDIMENTO</p><h1>Leitores</h1><p>Cadastros e situação de acesso aos empréstimos.</p><p role="status">{notice}</p>{error && <p role="alert" className="error">{error}</p>}
    {action ? <div className="card"><h2>{action.kind === 'delete' ? 'Excluir' : action.reader.active ? 'Desativar' : 'Ativar'} “{action.reader.name}”?</h2><p>{action.kind === 'delete' ? 'Leitores com histórico não podem ser excluídos.' : 'Desativar impede novos empréstimos, preserva o histórico e permite devolver os pendentes.'}</p><button disabled={busy} onClick={confirm}>Confirmar</button><button disabled={busy} onClick={() => { setAction(undefined); setError(''); }}>Cancelar</button></div> : <form className="card" onSubmit={save}><h2>{editing ? 'Editar leitor' : 'Novo leitor'}</h2><fieldset disabled={busy}><legend className="sr-only">Dados do leitor</legend>{(['registrationNumber', 'name', 'email'] as const).map(key => {
      const issue = fields.find(f => f.field === key);
      return <label key={key}>{{ registrationNumber: 'Matrícula', name: 'Nome', email: 'E-mail (opcional)' }[key]}<input required={key !== 'email'} type={key === 'email' ? 'email' : 'text'} maxLength={{ registrationNumber: 30, name: 150, email: 254 }[key]} pattern={key === 'registrationNumber' ? ' *[A-Za-z0-9._-]+ *' : undefined} value={form[key]} aria-invalid={!!issue} aria-describedby={issue ? `reader-${key}-error` : undefined} onChange={e => setForm({ ...form, [key]: e.target.value })} /><span className="error" id={`reader-${key}-error`}>{issue?.message}</span></label>;
    })}<small>Matrícula: letras ASCII, números, ponto, hífen ou sublinhado.</small><button className="primary">{busy ? 'Salvando…' : 'Salvar leitor'}</button>{editing && <button type="button" onClick={reset}>Cancelar edição</button>}</fieldset></form>}
    {list.error ? <p role="alert">{list.error} <button onClick={list.reload}>Tentar novamente</button></p> : !list.data ? <p role="status">Carregando leitores…</p> : <div className="card"><h2>Leitores cadastrados ({list.data.page.totalElements})</h2>{!list.data.content.length ? <p>Nenhum leitor cadastrado.</p> : <ul className="resource-list">{list.data.content.map(reader => <li key={reader.id}><div><strong>{reader.name}</strong><small>{reader.registrationNumber} · {reader.active ? 'Ativo' : 'Inativo'}</small>{reader.email && <small>{reader.email}</small>}</div><div><button disabled={busy || !!action} onClick={() => { reset(); setEditing(reader); setForm({ registrationNumber: reader.registrationNumber, name: reader.name, email: reader.email || '' }); }}>Editar <span className="sr-only">{reader.name}</span></button><button disabled={busy || !!action} onClick={() => { reset(); setAction({ reader, kind: 'status' }); }}>{reader.active ? 'Desativar' : 'Ativar'} <span className="sr-only">{reader.name}</span></button><button disabled={busy || !!action} onClick={() => { reset(); setAction({ reader, kind: 'delete' }); }}>Excluir <span className="sr-only">{reader.name}</span></button></div></li>)}</ul>}<nav aria-label="Paginação de leitores"><button disabled={!list.page || busy} onClick={() => list.setPage(list.page - 1)}>Anterior</button><span>Página {list.page + 1} de {Math.max(1, list.data.page.totalPages)}</span><button disabled={list.page + 1 >= list.data.page.totalPages || busy} onClick={() => list.setPage(list.page + 1)}>Próxima</button></nav></div>}
  </section>;
}
