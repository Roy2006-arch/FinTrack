import { API_BASE_URL } from './config.js';

export function token() { return sessionStorage.getItem('fintrack-token'); }
export function saveSession(auth) { sessionStorage.setItem('fintrack-token', auth.token); }
export function clearSession() { sessionStorage.removeItem('fintrack-token'); }
export function query(values) {
  const params = new URLSearchParams();
  Object.entries(values).forEach(([key, value]) => {
    if (value !== '' && value !== null && value !== undefined) params.set(key, value);
  });
  return params.toString();
}
export async function request(path, { method = 'GET', body, download = false, anonymous = false, signal } = {}) {
  const headers = { Accept: download ? 'text/csv' : 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (!anonymous && token()) headers.Authorization = `Bearer ${token()}`;
  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, { method, headers, body: body === undefined ? undefined : JSON.stringify(body), signal });
  } catch (error) {
    if (error.name === 'AbortError') throw error;
    throw new Error('Cannot reach FinTrack. Check that the backend is running and the API URL is correct.');
  }
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: `Request failed (${response.status})` }));
    if (response.status === 401 && !anonymous) {
      clearSession(); location.replace('login.html?expired=1');
    }
    const message = [error.message, ...Object.entries(error.errors || {}).map(([field, value]) => `${field}: ${value}`)].join(' · ');
    throw new Error(message);
  }
  if (response.status === 204) return null;
  return download ? response.blob() : response.json();
}
export const api = {
  get: (path, options) => request(path, options),
  post: (path, body, options) => request(path, { ...options, method: 'POST', body }),
  put: (path, body) => request(path, { method: 'PUT', body }),
  delete: path => request(path, { method: 'DELETE' })
};
export async function downloadCsv(path, filename) {
  const blob = await request(path, { download: true });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a'); link.href = url; link.download = filename;
  document.body.append(link); link.click(); link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
