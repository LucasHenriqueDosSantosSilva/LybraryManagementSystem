import { useEffect, useState } from 'react';
import { request, type Dashboard } from './api';
export function DashboardPage() {
  const [data, setData] = useState<Dashboard>();
  const [error, setError] = useState('');
  const [attempt, setAttempt] = useState(0);
  useEffect(() => {
    const controller = new AbortController(); setError(''); setData(undefined);
    request<Dashboard>('/dashboard', { signal: controller.signal }).then(setData).catch(e => { if (!controller.signal.aborted) setError(e.message); });
    return () => controller.abort();
  }, [attempt]);
  return <section><p className="eyebrow">VISÃO GERAL</p><h1>O dia a dia da biblioteca</h1><p>Catálogo e circulação em um só lugar.</p>
    {error ? <div role="alert">{error} <button onClick={() => setAttempt(attempt + 1)}>Tentar novamente</button></div> : !data ? <p role="status">Carregando resumo…</p> : <>
      <div className="stats">{[['Títulos', data.totalBooks], ['Exemplares', data.totalCopies], ['Leitores', data.totalReaders], ['Disponíveis', data.availableCopies], ['Empréstimos ativos', data.activeLoans], ['Em atraso', data.overdueLoans]].map(([label, value]) => <article className="card" key={label}><span>{label}</span><strong>{value}</strong></article>)}</div>
      <div className="columns"><article className="card"><h2>Mais emprestados</h2>{data.mostBorrowed.length ? <ol>{data.mostBorrowed.map(book => <li key={book.bookId}>{book.title} <span>· {book.loanCount} empréstimos</span></li>)}</ol> : <p>Nenhum empréstimo registrado ainda.</p>}</article>
      <article className="card"><h2>Atividade recente</h2>{data.recentLoans.length ? <ul>{data.recentLoans.map(loan => <li key={loan.id}>Empréstimo #{loan.id} · {{ ACTIVE: 'Ativo', OVERDUE: 'Em atraso', RETURNED: 'Devolvido' }[loan.status] || loan.status}<small>Vencimento: {loan.dueDate.split('-').reverse().join('/')}</small></li>)}</ul> : <p>A circulação aparecerá aqui.</p>}</article></div>
    </>}
  </section>;
}
