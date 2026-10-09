import { test, expect, type Page } from '@playwright/test';
import { execFileSync } from 'node:child_process';
import { randomUUID } from 'node:crypto';

test('complete circulation through real UI and API', async ({ page, request }) => {
  if (process.env.E2E_DB_SCHEMA !== 'library_test') throw new Error('Use the exclusive library_test schema and set E2E_DB_SCHEMA=library_test.');
  const suffix = randomUUID().slice(0, 12);
  const created: { resource: string; id: number }[] = [];
  const registration = `B-${suffix}`.toUpperCase();
  let loanId: number | undefined, readerId: number | undefined, copyId: number | undefined;
  const errors: string[] = []; page.on('pageerror', error => errors.push(error.message));
  async function create(resource: string, submit: string) {
    const response = page.waitForResponse(r => r.url().endsWith(`/api/${resource}`) && r.request().method() === 'POST');
    await page.getByRole('button', { name: submit, exact: true }).click();
    const result = await response; expect(result.status()).toBe(201);
    const body = await result.json(); created.push({ resource, id: body.id }); return body.id as number;
  }
  async function visit(path: string, heading: string) { await page.goto(path); await expect(page.getByRole('heading', { name: heading, exact: true })).toBeVisible(); }
  try {
    await visit('/categories', 'Categorias'); await page.getByLabel('Nome', { exact: true }).fill(`Categoria ${suffix}`); const category = await create('categories', 'Salvar categoria');
    await visit('/authors', 'Autores'); await page.getByLabel('Nome', { exact: true }).fill(`Autor ${suffix}`); const author = await create('authors', 'Salvar autor');
    await visit('/books', 'Livros'); await expect(page.getByRole('button', { name: 'Salvar livro' })).toBeEnabled();
    await page.getByLabel('Título', { exact: true }).fill(`Livro ${suffix}`); await page.getByLabel('ISBN', { exact: true }).fill('9780804429573');
    await page.getByLabel('Ano de publicação').fill('2000'); await page.getByRole('combobox', { name: 'Categoria', exact: true }).selectOption(String(category)); await page.getByRole('listbox', { name: 'Autores', exact: true }).selectOption(String(author));
    const book = await create('books', 'Salvar livro');
    await visit('/readers', 'Leitores'); await page.getByLabel('Matrícula').fill(registration); await page.getByLabel('Nome', { exact: true }).fill(`Leitor ${suffix}`); readerId = await create('readers', 'Salvar leitor');
    await visit('/copies', 'Exemplares'); await expect(page.getByRole('button', { name: 'Salvar exemplar' })).toBeEnabled(); await page.getByLabel('Código patrimonial').fill(`C-${suffix}`); await page.getByRole('combobox', { name: 'Livro', exact: true }).selectOption(String(book)); copyId = await create('copies', 'Salvar exemplar');
    await visit('/loans', 'Empréstimos e devoluções'); await expect(page.getByRole('button', { name: 'Registrar empréstimo' })).toBeEnabled();
    await page.getByRole('combobox', { name: 'Leitor ativo', exact: true }).selectOption(String(readerId)); await page.getByRole('combobox', { name: 'Exemplar disponível', exact: true }).selectOption(String(copyId));
    const response = page.waitForResponse(r => r.url().endsWith('/api/loans') && r.request().method() === 'POST');
    await page.getByRole('button', { name: 'Registrar empréstimo' }).click(); const result = await response; expect(result.status()).toBe(201); loanId = (await result.json()).id;
    await expect(page.getByText(`Empréstimo #${loanId} · Ativo`, { exact: true })).toBeVisible();
    await expect(page.getByRole('combobox', { name: 'Exemplar disponível', exact: true }).locator(`option[value="${copyId}"]`)).toHaveCount(0);
    const duplicate = await request.post('/api/loans', { data: { readerId, copyId } }); expect(duplicate.status()).toBe(409);
    await visit('/readers', 'Leitores'); await page.getByRole('button', { name: `Desativar Leitor ${suffix}`, exact: true }).click(); await page.getByRole('button', { name: 'Confirmar', exact: true }).click();
    await expect(page.getByRole('button', { name: `Ativar Leitor ${suffix}`, exact: true })).toBeVisible();
    await visit('/loans', 'Empréstimos e devoluções'); await expect(page.getByRole('button', { name: `Devolver empréstimo #${loanId}`, exact: true })).toBeVisible();
    await page.getByRole('button', { name: `Devolver empréstimo #${loanId}`, exact: true }).click(); await page.getByRole('button', { name: 'Confirmar devolução', exact: true }).click();
    await expect(page.getByText(`Empréstimo #${loanId} · Devolvido`, { exact: true })).toBeVisible();
    await expect(page.getByRole('button', { name: `Devolver empréstimo #${loanId}`, exact: true })).toHaveCount(0);
    await page.getByRole('combobox', { name: 'Leitor do histórico', exact: true }).selectOption(String(readerId)); await page.getByRole('combobox', { name: 'Situação', exact: true }).selectOption('RETURNED'); await page.getByRole('button', { name: 'Filtrar histórico' }).click();
    await expect(page.getByRole('heading', { name: 'Histórico (1)', exact: true })).toBeVisible();
    expect((await (await request.get(`/api/copies/${copyId}`)).json()).available).toBe(true);
    await page.goto('/'); await expect(page.getByRole('heading', { name: 'O dia a dia da biblioteca' })).toBeVisible(); await page.waitForLoadState('networkidle');
    await page.screenshot({ path: 'test-results/dashboard-desktop.png', fullPage: true }); expect(errors).toEqual([]);
  } finally {
    // Only this fixture's history; never delete existing readers' circulation.
    if (loanId && readerId && copyId) {
      const sql = `DELETE l FROM loans l JOIN readers r ON r.id=l.reader_id WHERE l.id=${Number(loanId)} AND l.reader_id=${Number(readerId)} AND l.copy_id=${Number(copyId)} AND r.registration_number='${registration}';`;
      execFileSync('docker', ['exec', '-i', 'library-mysql', 'sh', '-c', 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot library_test'], { input: sql, stdio: ['pipe', 'ignore', 'pipe'] });
    }
    for (const record of created.reverse()) expect((await fetch(`http://127.0.0.1:5174/api/${record.resource}/${record.id}`, { method: 'DELETE' })).status).toBe(204);
  }
});

test('all routes fit 320px and support keyboard navigation', async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 800 });
  for (const path of ['/', '/categories', '/authors', '/books', '/readers', '/copies', '/loans']) {
    await page.goto(path); await page.waitForLoadState('networkidle'); await expect(page.locator('h1')).toBeVisible();
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true);
  }
  await page.goto('/'); await page.keyboard.press('Tab'); await expect(page.getByRole('link', { name: 'Pular para o conteúdo' })).toBeFocused();
  await page.keyboard.press('Enter'); expect(await page.evaluate(() => location.hash)).toBe('#main'); await expect(page.locator('#main')).toBeFocused();
  await page.screenshot({ path: 'test-results/dashboard-mobile.png', fullPage: true });
});
