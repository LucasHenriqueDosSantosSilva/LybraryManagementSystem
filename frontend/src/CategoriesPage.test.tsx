import { afterEach, expect, test, vi } from 'vitest';
import { cleanup, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CategoriesPage } from './CategoriesPage';
import * as api from './api';
afterEach(cleanup);
const empty = { content: [], page: { number: 0, size: 10, totalPages: 0, totalElements: 0 } };
test('shows connection failure instead of an empty list and allows retry', async () => {
  const call = vi.spyOn(api, 'request').mockRejectedValueOnce(new api.ApiError('Sem conexão.', 0)).mockResolvedValue(empty);
  render(<CategoriesPage />);
  expect(await screen.findByRole('alert')).toHaveProperty('textContent', expect.stringContaining('Sem conexão.'));
  await userEvent.click(screen.getByRole('button', { name: 'Tentar novamente' }));
  await screen.findByText(/Nenhuma categoria cadastrada/);
  expect(call).toHaveBeenCalledTimes(2);
});
test('submits the name and confirms success only after server response', async () => {
  const call = vi.spyOn(api, 'request').mockResolvedValueOnce(empty).mockResolvedValueOnce({ id: 1, name: 'História' }).mockResolvedValue(empty);
  render(<CategoriesPage />); await screen.findByText(/Nenhuma categoria cadastrada/);
  await userEvent.type(screen.getByLabelText('Nome'), 'História');
  await userEvent.click(screen.getByRole('button', { name: 'Salvar categoria' }));
  await screen.findByText('Categoria salva.');
  expect(call).toHaveBeenCalledWith('/categories', { method: 'POST', body: JSON.stringify({ name: 'História' }) });
  expect(screen.getByLabelText('Nome')).toHaveProperty('value', '');
});
test('preserves form contents when uniqueness conflict occurs', async () => {
  vi.spyOn(api, 'request').mockResolvedValueOnce(empty).mockRejectedValueOnce(new api.ApiError('Categoria já existe.', 409));
  render(<CategoriesPage />); await screen.findByText(/Nenhuma categoria cadastrada/);
  await userEvent.type(screen.getByLabelText('Nome'), 'História');
  await userEvent.click(screen.getByRole('button', { name: 'Salvar categoria' }));
  await screen.findByRole('alert');
  expect(screen.getByLabelText('Nome')).toHaveProperty('value', 'História');
  expect(screen.queryByText('Categoria salva.')).toBeNull();
});
test('requires confirmation and handles deletion without a JSON body', async () => {
  const call = vi.spyOn(api, 'request').mockResolvedValueOnce({ ...empty, content: [{ id: 7, name: 'Arte' }], page: { ...empty.page, totalElements: 1, totalPages: 1 } }).mockResolvedValueOnce(undefined).mockResolvedValue(empty);
  render(<CategoriesPage />); await screen.findByRole('button', { name: 'Excluir Arte' });
  await userEvent.click(screen.getByRole('button', { name: 'Excluir Arte' }));
  expect(call).toHaveBeenCalledTimes(1);
  await userEvent.click(screen.getByRole('button', { name: 'Confirmar exclusão' }));
  await waitFor(() => expect(call).toHaveBeenCalledWith('/categories/7', { method: 'DELETE' }));
  await screen.findByText('Categoria excluída.');
});
