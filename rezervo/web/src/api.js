// Toate apelurile catre backend trec prin aceasta functie.
const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '');

export async function api(path, { method = 'GET', body, token } = {}) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (token) headers['Authorization'] = `Bearer ${token}`;

  let res;
  try {
    res = await fetch(`${API_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new Error('Serverul nu răspunde. Verifică conexiunea sau încearcă peste un minut.');
  }

  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    const err = new Error(data?.error || `Eroare ${res.status}`);
    err.status = res.status;
    throw err;
  }
  return data;
}

export function formatDateTime(value) {
  return new Date(value).toLocaleString('ro-RO', {
    weekday: 'long', day: 'numeric', month: 'long', hour: '2-digit', minute: '2-digit',
  });
}

export function formatPrice(value) {
  return `${Number(value).toLocaleString('ro-RO')} lei`;
}
