export type Category = { id: number; name: string };
export type Page<T> = { content: T[]; page: { number: number; size: number; totalPages: number; totalElements: number } };
export type Dashboard = { totalBooks: number; totalCopies: number; totalReaders: number; availableCopies: number; activeLoans: number; overdueLoans: number; mostBorrowed: { bookId: number; title: string; loanCount: number }[]; recentLoans: { id: number; readerId: number; status: string; dueDate: string }[] };
export class ApiError extends Error {
  constructor(message: string, public status: number, public fields: { field: string; message: string }[] = []) { super(message); }
}
export async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  let response: Response;
  try { response = await fetch(`/api${path}`, { ...options, headers: { ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...options.headers } }); }
  catch (error) { if (error instanceof DOMException && error.name === 'AbortError') throw error; throw new ApiError('Não foi possível conectar à biblioteca. Tente novamente.', 0); }
  if (!response.ok) {
    const problem = await response.json().catch(() => ({}));
    throw new ApiError(problem.detail || 'Não foi possível concluir a operação.', response.status, problem.errors || []);
  }
  if (response.status === 204) return undefined as T;
  return response.json();
}

export type Author = { id: number; name: string };
export type Book = { id: number; title: string; isbn: string; publicationYear: number; description: string | null; categoryId: number; authors: Author[] };
// Reference selectors must include all pages, not only the first hundred records.
export async function references<T>(resource: string, signal: AbortSignal): Promise<T[]> {
  const items: T[] = [];
  for (let page = 0; ; page++) {
    const result = await request<Page<T>>(`/${resource}?page=${page}&size=100`, { signal });
    items.push(...result.content);
    if (page + 1 >= result.page.totalPages) return items;
  }
}
export type Reader = { id: number; registrationNumber: string; name: string; email: string | null; active: boolean };
export type Copy = { id: number; inventoryCode: string; bookId: number; circulationStatus: string; available: boolean };
export type Loan = { id: number; readerId: number; copyId: number; bookId: number; loanDate: string; dueDate: string; returnedDate: string | null; status: 'ACTIVE' | 'OVERDUE' | 'RETURNED' };
