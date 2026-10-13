import { useEffect, useState, type FormEvent, type ReactNode } from 'react';
import { configureAuthentication, request } from './api';
type Session = { authenticationRequired: boolean; authenticated: boolean; username: string };
export function AuthGate({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session>();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [loadError, setLoadError] = useState('');
  async function load() {
    setLoadError('');
    try { const result = await request<Session>('/auth/session'); configureAuthentication(result.authenticationRequired); setSession(result); }
    catch (e) { setLoadError((e as Error).message); setSession(undefined); }
  }
  useEffect(() => {
    void load(); const expired = () => { setSession(undefined); setPassword(''); void load(); };
    window.addEventListener('library:unauthorized', expired);
    return () => window.removeEventListener('library:unauthorized', expired);
  }, []);
  async function login(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError('');
    try { await request('/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: new URLSearchParams({ username, password }).toString() }); setPassword(''); await load(); }
    catch (e) { setError((e as Error).message); setPassword(''); } finally { setBusy(false); }
  }
  async function logout() {
    setBusy(true); setError('');
    try { await request('/auth/logout', { method: 'POST' }); setSession(undefined); await load(); }
    catch (e) { setError((e as Error).message); } finally { setBusy(false); }
  }
  if (loadError) return <main id="main" tabIndex={-1}><h1>Biblioteca</h1><p role="alert">{loadError}</p><button onClick={load}>Tentar novamente</button></main>;
  if (!session) return <main id="main" tabIndex={-1}><p role="status">Verificando sessão…</p></main>;
  if (session.authenticationRequired && !session.authenticated) return <main id="main" tabIndex={-1}><p className="eyebrow">GESTÃO DA BIBLIOTECA</p><h1>Entrar</h1><p>Acesso do bibliotecário.</p>{error && <p role="alert" className="error">{error}</p>}<form className="card" onSubmit={login}><label>Usuário<input required autoComplete="username" value={username} disabled={busy} onChange={e => setUsername(e.target.value)} /></label><label>Senha<input required type="password" autoComplete="current-password" value={password} disabled={busy} onChange={e => setPassword(e.target.value)} /></label><button className="primary" disabled={busy}>{busy ? 'Entrando…' : 'Entrar'}</button></form></main>;
  return <>{session.authenticationRequired && <aside className="session-bar"><span>Conectado como {session.username}</span> <button disabled={busy} onClick={logout}>Sair</button>{error && <p role="alert">{error}</p>}</aside>}{children}</>;
}
