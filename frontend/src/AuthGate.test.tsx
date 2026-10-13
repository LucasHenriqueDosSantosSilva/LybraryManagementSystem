import { afterEach, expect, test, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AuthGate } from './AuthGate';
import * as api from './api';
afterEach(() => { cleanup(); api.configureAuthentication(false); });
test('waits for session before exposing application and allows retry', async () => {
 vi.spyOn(api, 'request').mockRejectedValueOnce(new Error('Sem conexão.')).mockResolvedValue({ authenticationRequired: true, authenticated: false });
 render(<AuthGate><p>Catálogo privado</p></AuthGate>);
 await screen.findByRole('alert'); expect(screen.queryByText('Catálogo privado')).toBeNull();
 await userEvent.click(screen.getByRole('button', { name: 'Tentar novamente' })); await screen.findByLabelText('Senha');
});
test('logs in and logs out without storing password', async () => {
 const call = vi.spyOn(api, 'request').mockResolvedValueOnce({ authenticationRequired: true, authenticated: false }).mockResolvedValueOnce(undefined)
 .mockResolvedValueOnce({ authenticationRequired: true, authenticated: true, username: 'bibliotecario' }).mockResolvedValueOnce(undefined)
 .mockResolvedValue({ authenticationRequired: true, authenticated: false });
 render(<AuthGate><p>Catálogo privado</p></AuthGate>); await screen.findByLabelText('Senha');
 await userEvent.type(screen.getByLabelText('Usuário'), 'bibliotecario'); await userEvent.type(screen.getByLabelText('Senha'), 'Test-password-only');
 await userEvent.click(screen.getByRole('button', { name: 'Entrar' })); await screen.findByText('Catálogo privado');
 expect(call.mock.calls[1][0]).toBe('/auth/login'); expect(localStorage.length).toBe(0);
 await userEvent.click(screen.getByRole('button', { name: 'Sair' })); await screen.findByLabelText('Senha');
 expect(screen.queryByText('Catálogo privado')).toBeNull();
});
