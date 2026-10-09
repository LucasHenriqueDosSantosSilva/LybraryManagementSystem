import { afterEach, expect, test, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { BooksPage } from './BooksPage';
import { AuthorsPage } from './CategoriesPage';
import * as api from './api';
afterEach(cleanup);
const empty = { content: [], page: { number: 0, size: 10, totalPages: 0, totalElements: 0 } };
function setup() {
 vi.spyOn(api, 'references').mockImplementation(async resource => resource === 'authors' ? [{ id: 2, name: 'Ana' }, { id: 3, name: 'Bia' }] : [{ id: 1, name: 'Ciência' }]);
 const call = vi.spyOn(api, 'request').mockResolvedValue(empty);
 render(<MemoryRouter><BooksPage /></MemoryRouter>);
 return call;
}
test('encodes combined search filters and resets page', async () => {
 const call = setup(); await screen.findByText('Nenhum livro encontrado.');
 await userEvent.type(screen.getByLabelText('Título da busca'), 'A & B');
 await userEvent.type(screen.getByLabelText('Autor da busca'), 'Ana');
 await userEvent.click(screen.getByRole('button', { name: 'Buscar' }));
 await screen.findByText('Nenhum livro encontrado.');
 const path = call.mock.calls.at(-1)![0]; const params = new URLSearchParams(path.split('?')[1]);
 expect(params.get('title')).toBe('A & B'); expect(params.get('author')).toBe('Ana'); expect(params.get('page')).toBe('0');
});
test('submits numeric references and multiple authors, preserving fields on conflict', async () => {
 const call = setup(); await screen.findByRole('option', { name: 'Ana · #2' });
 await userEvent.type(screen.getByLabelText('Título', { exact: true }), 'Livro');
 await userEvent.type(screen.getByLabelText('ISBN', { exact: true }), '0306406152');
 await userEvent.type(screen.getByLabelText('Ano de publicação'), '2000');
 await userEvent.selectOptions(screen.getByLabelText('Categoria', { exact: true }), '1');
 await userEvent.selectOptions(screen.getByLabelText('Autores', { exact: true }), ['2', '3']);
 call.mockRejectedValueOnce(new api.ApiError('ISBN já cadastrado.', 409));
 await userEvent.click(screen.getByRole('button', { name: 'Salvar livro' }));
 await screen.findByRole('alert');
 const payload = JSON.parse(call.mock.calls.at(-1)![1]!.body as string);
 expect(payload).toMatchObject({ publicationYear: 2000, categoryId: 1, authorIds: [2, 3] });
 expect(screen.getByLabelText('Título', { exact: true })).toHaveProperty('value', 'Livro');
 expect(screen.queryByText('Livro salvo.')).toBeNull();
});
test('makes missing prerequisites actionable rather than enabling an invalid form', async () => {
 vi.spyOn(api, 'references').mockResolvedValue([]); vi.spyOn(api, 'request').mockResolvedValue(empty);
 render(<MemoryRouter><BooksPage /></MemoryRouter>);
 await screen.findByRole('link', { name: 'categoria' });
 expect(screen.getByRole('button', { name: 'Salvar livro' }).closest('fieldset')).toHaveProperty('disabled', true);
});
test('uses author endpoint and allows 150 character names', async () => {
 const call = vi.spyOn(api, 'request').mockResolvedValue(empty);
 render(<AuthorsPage />); await screen.findByText(/Nenhum autor cadastrado/);
 expect(screen.getByLabelText('Nome')).toHaveProperty('maxLength', 150);
 await userEvent.type(screen.getByLabelText('Nome'), 'Ana');
 await userEvent.click(screen.getByRole('button', { name: 'Salvar autor' }));
 await screen.findByText('Autor salvo.');
 expect(call).toHaveBeenCalledWith('/authors', { method: 'POST', body: '{"name":"Ana"}' });
});
