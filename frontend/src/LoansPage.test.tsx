import { afterEach, expect, test, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { LoansPage } from './LoansPage';
import * as api from './api';
afterEach(cleanup);
const empty = { content: [], page: { number: 0, size: 10, totalPages: 0, totalElements: 0 } };
const loan: api.Loan = { id: 7, readerId: 1, copyId: 3, bookId: 5, loanDate: '2026-10-09', dueDate: '2026-10-23', returnedDate: null, status: 'ACTIVE' };
const history = { ...empty, content: [loan], page: { ...empty.page, totalPages: 1, totalElements: 1 } };
function setup(data: unknown = empty) {
 const refs = vi.spyOn(api, 'references').mockImplementation(async resource => ({
  readers: [{ id: 1, name: 'Ana', registrationNumber: 'R1', active: true }, { id: 2, name: 'Bia', registrationNumber: 'R2', active: false }],
  copies: [{ id: 3, inventoryCode: 'C1', bookId: 5, circulationStatus: 'IN_CIRCULATION', available: true }, { id: 4, inventoryCode: 'C2', bookId: 5, circulationStatus: 'IN_CIRCULATION', available: false }],
  books: [{ id: 5, title: 'Livro' }]
 }[resource] || []));
 const call = vi.spyOn(api, 'request').mockResolvedValue(data);
 render(<MemoryRouter><LoansPage /></MemoryRouter>);
 return { call, refs };
}
async function chooseLoan() {
 await screen.findByRole('option', { name: 'C1 · Livro' });
 await userEvent.selectOptions(screen.getByLabelText('Leitor ativo'), '1');
 await userEvent.selectOptions(screen.getByLabelText('Exemplar disponível'), '3');
}
test('offers only active readers and available copies but keeps inactive readers in history', async () => {
 setup(); await screen.findByRole('option', { name: 'C1 · Livro' });
 expect(screen.getByLabelText('Leitor ativo').textContent).not.toContain('Bia');
 expect(screen.getByLabelText('Exemplar disponível').textContent).not.toContain('C2');
 expect(screen.getByLabelText('Leitor do histórico').textContent).toContain('Bia');
});
test('posts numeric ids and refreshes references after successful loan', async () => {
 const { call, refs } = setup(); await chooseLoan();
 call.mockResolvedValueOnce(loan);
 await userEvent.click(screen.getByRole('button', { name: 'Registrar empréstimo' }));
 await screen.findByText('Empréstimo #7 registrado. Vencimento: 23/10/2026.');
 expect(call).toHaveBeenCalledWith('/loans', { method: 'POST', body: '{"readerId":1,"copyId":3}' });
 expect(refs.mock.calls.length).toBeGreaterThanOrEqual(6);
});
test('preserves selection and reports conflict without success', async () => {
 const { call } = setup(); await chooseLoan(); call.mockRejectedValueOnce(new api.ApiError('Exemplar indisponível.', 409));
 await userEvent.click(screen.getByRole('button', { name: 'Registrar empréstimo' }));
 expect(await screen.findByRole('alert')).toHaveProperty('textContent', 'Exemplar indisponível.');
 expect(screen.getByLabelText('Exemplar disponível')).toHaveProperty('value', '3');
 expect(screen.queryByText(/registrado\. Vencimento/)).toBeNull();
});
test('requires confirmation and updates returned history', async () => {
 const { call } = setup(history); await screen.findByRole('button', { name: 'Devolver empréstimo #7' });
 await userEvent.click(screen.getByRole('button', { name: 'Devolver empréstimo #7' }));
 expect(call).toHaveBeenCalledTimes(1);
 const returned = { ...loan, returnedDate: '2026-10-09', status: 'RETURNED' };
 call.mockResolvedValueOnce(returned).mockResolvedValue({ ...history, content: [returned] });
 await userEvent.click(screen.getByRole('button', { name: 'Confirmar devolução' }));
 await screen.findByText('Empréstimo #7 devolvido em 09/10/2026.');
 await screen.findByText('Empréstimo #7 · Devolvido');
 expect(screen.queryByRole('button', { name: 'Devolver empréstimo #7' })).toBeNull();
 expect(call).toHaveBeenCalledWith('/loans/7/return', { method: 'POST' });
});
test('filters history by reader including inactive and ACTIVE semantics', async () => {
 const { call } = setup(); await screen.findByRole('option', { name: 'Bia · R2 (inativo)' });
 await userEvent.selectOptions(screen.getByLabelText('Leitor do histórico'), '2');
 await userEvent.selectOptions(screen.getByLabelText('Situação'), 'ACTIVE');
 await userEvent.click(screen.getByRole('button', { name: 'Filtrar histórico' }));
 await screen.findByText('Nenhum empréstimo encontrado.');
 expect(call.mock.calls.at(-1)![0]).toBe('/loans?page=0&size=10&readerId=2&status=ACTIVE');
});
test('does not retry ambiguous mutation and tells user to inspect history', async () => {
 const { call } = setup(); await chooseLoan(); call.mockRejectedValueOnce(new api.ApiError('Sem conexão.', 0));
 await userEvent.click(screen.getByRole('button', { name: 'Registrar empréstimo' }));
 expect(await screen.findByRole('alert')).toHaveProperty('textContent', expect.stringContaining('consulte o histórico'));
 expect(call.mock.calls.filter(([path]) => path === '/loans')).toHaveLength(1);
});
