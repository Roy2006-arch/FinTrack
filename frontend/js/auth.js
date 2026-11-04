import { api, saveSession, token } from './api.js';
import { $, toast } from './ui.js';
if (token()) location.replace('dashboard.html');
const mode = document.body.dataset.auth;
if (new URLSearchParams(location.search).has('expired')) toast('Your session expired. Please sign in again.');
$('#show-password').onclick = () => {
  const visible = $('#password').type === 'password';
  $('#password').type = visible ? 'text' : 'password';
  $('#show-password').setAttribute('aria-label', visible ? 'Hide password' : 'Show password');
  $('#show-password').setAttribute('aria-pressed', String(visible));
  $('#show-password').innerHTML = `<i class="bi bi-eye${visible ? '-slash' : ''}" aria-hidden="true"></i>`;
};
if (mode === 'register') {
  const strength = document.createElement('div'); strength.className = 'password-strength';
  strength.innerHTML = '<div class="password-strength-bars" aria-hidden="true"><span></span><span></span><span></span><span></span></div><span data-strength-label>Password strength</span>';
  $('#password').closest('.password-wrap').after(strength);
  $('#password').addEventListener('input', event => {
    const value = event.target.value;
    const score = value ? [value.length >= 10, /[a-z]/.test(value) && /[A-Z]/.test(value), /[0-9]/.test(value), /[^a-zA-Z0-9]/.test(value)].filter(Boolean).length : 0;
    strength.querySelectorAll('.password-strength-bars span').forEach((bar, index) => bar.classList.toggle('filled', index < score));
    strength.querySelector('[data-strength-label]').textContent = value && /[^\x21-\x7E]/.test(value) ? 'Use ASCII; no spaces' : value ? ['Keep going', 'Getting started', 'Getting stronger', 'Looking good', 'Strong password'][score] : 'Password strength';
  });
}
$('#auth-form').onsubmit = async event => {
  event.preventDefault(); const button = event.currentTarget.querySelector('[type=submit]');
  const data = Object.fromEntries(new FormData(event.currentTarget)); data.email = data.email.trim();
  $('#auth-error').textContent = '';
  if (mode === 'register') {
    data.name = data.name.trim();
    if (!data.name) { $('#auth-error').textContent = 'Please enter your name.'; return; }
    if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])[\x21-\x7E]{10,72}$/.test(data.password)) {
      $('#auth-error').textContent = 'Use 10–72 ASCII characters with uppercase, lowercase and a number, without spaces.'; return;
    }
  }
  button.disabled = true; button.setAttribute('aria-busy', 'true');
  try { saveSession(await api.post(`/auth/${mode}`, data, { anonymous: true })); location.replace('dashboard.html'); }
  catch (error) { $('#auth-error').textContent = error.message; }
  finally { button.disabled = false; button.removeAttribute('aria-busy'); }
};
