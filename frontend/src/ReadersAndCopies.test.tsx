import { afterEach, expect, test, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { ReadersPage } from './ReadersPage';
import { CopiesPage } from './CopiesPage';
import * as api from './api';
afterEach(cleanup);
const empty = { content: [], page: { number: 0, size: 10, totalPages: 0, totalElements: 0 } };
const reader = { id: 1, name: 'Ana', registrationNumber: 'R-1', email: null, active: true };
const readers = { ...empty, content: [reader], page: { ...empty.page, totalPages: 1, totalElements: 1 } };
const copy = { id: 2, inventoryCode: 'C-1', bookId: 3, circulationStatus: 'IN_CIRCULATION', available: true };
const copies = { ...empty, content: [copy], page: { ...empty.page, totalPages: 1, totalElements: 1 } };
function books() { vi.spyOn(api, 'references').mockResolvedValue([{ id: 3, title: 'Livro', isbn: '9780306406157' }]); }
test('creates reader with optional email as null', async () => {
 const call = vi.spyOn(api, 'request').mockResolvedValue(empty);
 render(<ReadersPage />); await screen.findByText('Nenhum leitor cadastrado.');
 await userEvent.type(screen.getByLabelText('Matrícula'), 'R-1');
 await userEvent.type(screen.getByLabelText('Nome'), 'Ana');
 await userEvent.click(screen.getByRole('button', { name: 'Salvar leitor' }));
 await screen.findByText('Leitor salvo.');
 expect(call).toHaveBeenCalledWith('/readers', { method: 'POST', body: '{"registrationNumber":"R-1","name":"Ana","email":null}' });
});
test('changes reader status only after confirmation and refreshes state', async () => {
 const call = vi.spyOn(api, 'request').mockResolvedValueOnce(readers).mockResolvedValueOnce({ ...reader, active: false }).mockResolvedValue({ ...readers, content: [{ ...reader, active: false }] });
 render(<ReadersPage />); await screen.findByRole('button', { name: 'Desativar Ana' });
 await userEvent.click(screen.getByRole('button', { name: 'Desativar Ana' }));
 expect(call).toHaveBeenCalledTimes(1);
 await userEvent.click(screen.getByRole('button', { name: 'Confirmar' }));
 await screen.findByRole('button', { name: 'Ativar Ana' });
 expect(call).toHaveBeenCalledWith('/readers/1/status', { method: 'PATCH', body: '{"active":false}' });
});
test('keeps deletion confirmation when reader history causes conflict', async () => {
 const call = vi.spyOn(api, 'request').mockResolvedValueOnce(readers).mockRejectedValueOnce(new api.ApiError('Leitor possui histórico.', 409));
 render(<ReadersPage />); await screen.findByRole('button', { name: 'Excluir Ana' });
 await userEvent.click(screen.getByRole('button', { name: 'Excluir Ana' }));
 await userEvent.click(screen.getByRole('button', { name: 'Confirmar' }));
 expect(await screen.findByRole('alert')).toHaveProperty('textContent', 'Leitor possui histórico.');
 expect(screen.queryByText('Leitor excluído.')).toBeNull();
 expect(call).toHaveBeenCalledWith('/readers/1', { method: 'DELETE' });
});
test('creates copy using numeric book identifier', async () => {
 books(); const call = vi.spyOn(api, 'request').mockResolvedValue(empty);
 render(<MemoryRouter><CopiesPage /></MemoryRouter>); await screen.findByRole('option', { name: 'Livro · 9780306406157' });
 await userEvent.type(screen.getByLabelText('Código patrimonial'), 'C-1');
 await userEvent.selectOptions(screen.getByLabelText('Livro'), '3');
 await userEvent.click(screen.getByRole('button', { name: 'Salvar exemplar' }));
 await screen.findByText('Exemplar salvo.');
 expect(call).toHaveBeenCalledWith('/copies', { method: 'POST', body: '{"inventoryCode":"C-1","bookId":3}' });
});
test('confirms withdrawal and refreshes availability', async () => {
 books(); const call = vi.spyOn(api, 'request').mockResolvedValueOnce(copies).mockResolvedValueOnce({ ...copy, circulationStatus: 'WITHDRAWN', available: false }).mockResolvedValue({ ...copies, content: [{ ...copy, circulationStatus: 'WITHDRAWN', available: false }] });
 render(<MemoryRouter><CopiesPage /></MemoryRouter>); await screen.findByRole('button', { name: 'Retirar C-1' });
 await userEvent.click(screen.getByRole('button', { name: 'Retirar C-1' }));
 expect(call).toHaveBeenCalledTimes(1);
 await screen.findByText(/não pode ser desfeita/);
 await userEvent.click(screen.getByRole('button', { name: 'Confirmar' }));
 await screen.findByText('Retirado de circulação');
 expect(call).toHaveBeenCalledWith('/copies/2/withdrawal', { method: 'POST' });
 expect(screen.getByRole('button', { name: 'Retirar C-1' })).toHaveProperty('disabled', true);
});
test('prevents withdrawal of a copy displayed as borrowed', async () => {
 books(); vi.spyOn(api, 'request').mockResolvedValue({ ...copies, content: [{ ...copy, available: false }] });
 render(<MemoryRouter><CopiesPage /></MemoryRouter>);
 await screen.findByText('Emprestado');
 expect(screen.getByRole('button', { name: 'Retirar C-1' })).toHaveProperty('disabled', true);
});
test('edits reader fields without changing status through PUT', async () => {
 const call = vi.spyOn(api, 'request').mockResolvedValueOnce(readers).mockResolvedValue(readers);
 render(<ReadersPage />); await screen.findByRole('button', { name: 'Editar Ana' });
 await userEvent.click(screen.getByRole('button', { name: 'Editar Ana' }));
 await userEvent.clear(screen.getByLabelText('Nome')); await userEvent.type(screen.getByLabelText('Nome'), 'Ana Maria');
 await userEvent.click(screen.getByRole('button', { name: 'Salvar leitor' }));
 await screen.findByText('Leitor salvo.');
 expect(call).toHaveBeenCalledWith('/readers/1', { method: 'PUT', body: '{"registrationNumber":"R-1","name":"Ana Maria","email":null}' });
});
test('preserves copy form values when update conflicts with history', async () => {
 books(); const call = vi.spyOn(api, 'request').mockResolvedValueOnce(copies).mockRejectedValueOnce(new api.ApiError('Exemplar possui histórico.', 409));
 render(<MemoryRouter><CopiesPage /></MemoryRouter>); await screen.findByRole('option', { name: 'Livro · 9780306406157' });
 await userEvent.click(await screen.findByRole('button', { name: 'Editar C-1' }));
 await userEvent.clear(screen.getByLabelText('Código patrimonial')); await userEvent.type(screen.getByLabelText('Código patrimonial'), 'C-2');
 await userEvent.click(screen.getByRole('button', { name: 'Salvar exemplar' }));
 await screen.findByRole('alert');
 expect(call).toHaveBeenCalledWith('/copies/2', { method: 'PUT', body: '{"inventoryCode":"C-2","bookId":3}' });
 expect(screen.getByLabelText('Código patrimonial')).toHaveProperty('value', 'C-2');
 expect(screen.queryByText('Exemplar salvo.')).toBeNull();
});
