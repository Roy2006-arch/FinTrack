import { api, token, clearSession } from './api.js';
import { toggleTheme, updateThemeButtons } from './experience.js';

export const $ = selector => document.querySelector(selector);
export const money = value => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 }).format(Number(value || 0));
export const percent = value => `${Number(value || 0).toFixed(1)}%`;
export const escape = value => String(value ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
export function today() {
  const date = new Date(); return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}
export const monthNow = () => today().slice(0, 7);
export const dateLabel = value => new Date(`${value}T12:00:00`).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
export const empty = (title, detail = 'Add your first record to get started.') => `<div class="empty-state"><i class="bi bi-inbox" aria-hidden="true"></i><h3>${escape(title)}</h3><p>${escape(detail)}</p></div>`;
export const loading = () => '<div class="loading-state" role="status"><span class="spinner-border spinner-border-sm" aria-hidden="true"></span> Loading your finances…</div>';
export const metric = (title, value, icon, detail = '', kind = '') => `<article class="metric ${kind}"><div class="metric-top"><span>${escape(title)}</span><i class="bi bi-${icon}" aria-hidden="true"></i></div><strong><span class="visually-hidden">${escape(value)}</span><span data-count aria-hidden="true">${escape(value)}</span></strong><small>${escape(detail)}</small></article>`;

export function toast(message, error = false) {
  let container = $('#toasts');
  if (!container) { container = document.createElement('div'); container.id = 'toasts'; container.setAttribute('aria-live', 'polite'); document.body.append(container); }
  const item = document.createElement('div'); item.className = `app-toast ${error ? 'error' : ''}`; item.setAttribute('role', error ? 'alert' : 'status');
  const text = document.createElement('span'); text.textContent = message;
  const close = document.createElement('button'); close.type = 'button'; close.setAttribute('aria-label', 'Dismiss notification'); close.textContent = '×'; close.onclick = () => item.remove();
  item.append(text, close); container.append(item); setTimeout(() => item.remove(), error ? 15000 : 6000);
}
export async function action(fn, button) {
  if (button) { button.disabled = true; button.setAttribute('aria-busy', 'true'); }
  try { return await fn(); } catch (error) { toast(error.message, true); }
  finally { if (button) { button.disabled = false; button.removeAttribute('aria-busy'); } }
}
export async function loadSection(selector, fn) {
  $(selector).innerHTML = loading();
  try { await fn(); }
  catch (error) {
    $(selector).innerHTML = `<div class="error-state" role="alert"><h3>We couldn't load this section</h3><p>${escape(error.message)}</p><button class="btn btn-outline-primary" data-retry>Try again</button></div>`;
    $(selector).querySelector('[data-retry]').onclick = () => loadSection(selector, fn);
  }
}
export async function setupPage(active) {
  if (!token()) { location.replace('login.html'); throw new Error('Please sign in'); }
  const links = [['dashboard', 'grid-1x2', 'Overview'], ['transactions', 'arrow-left-right', 'Transactions'], ['budgets', 'pie-chart', 'Budgets'], ['goals', 'bullseye', 'Savings goals'], ['reports', 'bar-chart-line', 'Reports'], ['profile', 'person-circle', 'Profile & categories']];
  $('#app-nav').innerHTML = `<a class="brand" href="dashboard.html"><span class="brand-icon"><i class="bi bi-graph-up-arrow" aria-hidden="true"></i></span>FinTrack<span class="brand-dot">.</span></a>
    <p class="nav-caption">YOUR WORKSPACE</p><nav aria-label="Main navigation">${links.map(([page, icon, label]) => `<a href="${page}.html" ${page === active ? 'class="active" aria-current="page"' : ''}><i class="bi bi-${icon}" aria-hidden="true"></i>${label}</a>`).join('')}</nav>
    <div class="sidebar-note"><i class="bi bi-stars" aria-hidden="true"></i><strong>A little closer, every day.</strong><p>Big dreams start with<br>small, intentional steps.</p><a href="goals.html">Find your next goal <i class="bi bi-arrow-up-right" aria-hidden="true"></i></a></div><button id="logout" class="logout"><i class="bi bi-box-arrow-right" aria-hidden="true"></i> Sign out</button>`;
  $('#topbar').innerHTML = `<button class="icon-button menu-toggle" id="menu-toggle" aria-label="Toggle navigation" aria-expanded="false" aria-controls="app-nav"><i class="bi bi-list" aria-hidden="true"></i></button><span class="workspace-label"><span class="workspace-dot"></span>Personal workspace</span><div class="topbar-actions"><button class="quick-search" id="quick-search" aria-label="Quick navigation" title="Quick navigation (Ctrl+K or ⌘K)"><i class="bi bi-search" aria-hidden="true"></i><span>Jump to…</span><kbd>⌘ / Ctrl K</kbd></button><span class="today-label">${new Date().toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}</span><button class="icon-button" id="theme-toggle" aria-label="Switch color theme"><i class="bi bi-moon-stars" aria-hidden="true"></i></button><a class="user-avatar" href="profile.html" aria-label="Your profile">U</a></div>`;
  $('#quick-search').onclick = () => openQuickNavigation(links);
  document.addEventListener('keydown', event => {
    if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
      event.preventDefault();
      if (!document.querySelector('dialog[open]')) openQuickNavigation(links);
    }
  });
  const mobileMenu = matchMedia('(max-width: 991px)');
  const toggleMenu = open => {
    document.body.classList.toggle('nav-open', open);
    $('#menu-toggle').setAttribute('aria-expanded', String(open));
    $('#app-nav').inert = mobileMenu.matches && !open;
    if (open) $('#app-nav nav a').focus();
    else if (mobileMenu.matches && $('#app-nav').contains(document.activeElement)) $('#menu-toggle').focus();
  };
  $('#app-nav').inert = mobileMenu.matches;
  mobileMenu.addEventListener('change', () => toggleMenu(false));
  $('#menu-toggle').onclick = () => toggleMenu(!document.body.classList.contains('nav-open'));
  $('#nav-backdrop').onclick = () => toggleMenu(false);
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && !document.querySelector('dialog[open]')) toggleMenu(false);
    if (event.key === 'Tab' && mobileMenu.matches && document.body.classList.contains('nav-open') && !document.querySelector('dialog[open]')) {
      const focusable = [...$('#app-nav').querySelectorAll('a, button')];
      if (event.shiftKey && document.activeElement === focusable[0]) { event.preventDefault(); focusable.at(-1).focus(); }
      else if (!event.shiftKey && document.activeElement === focusable.at(-1)) { event.preventDefault(); focusable[0].focus(); }
    }
  });
  $('#theme-toggle').onclick = toggleTheme;
  updateThemeButtons();
  $('#logout').onclick = () => action(async () => { await api.post('/auth/logout'); clearSession(); location.replace('login.html'); }, $('#logout'));
  const user = await api.get('/users/me');
  $('.user-avatar').textContent = user.name.split(/\s+/).map(s => s[0]).slice(0, 2).join('').toUpperCase();
  return user;
}

function openQuickNavigation(links) {
  const dialog = document.createElement('dialog');
  dialog.className = 'app-dialog command-dialog';
  dialog.setAttribute('aria-label', 'Quick navigation');
  const descriptions = ['Your financial picture', 'Income, expenses and activity', 'Give every rupee a plan', 'Make room for your next big thing', 'Explore your financial story', 'Make this workspace yours'];
  dialog.innerHTML = `<div class="command-input-wrap"><i class="bi bi-search" aria-hidden="true"></i><input class="command-input" aria-label="Find a page" placeholder="Where would you like to go?" autocomplete="off"><button class="icon-button" data-close aria-label="Close quick navigation">×</button></div><div class="command-results"></div><p class="command-hint"><kbd>↑</kbd> <kbd>↓</kbd> to explore &nbsp; <kbd>Enter</kbd> to open &nbsp; <kbd>Esc</kbd> to close</p>`;
  const input = dialog.querySelector('input');
  const results = dialog.querySelector('.command-results');
  const render = () => {
    const filtered = links.filter(([, , label], index) => `${label} ${descriptions[index]}`.toLowerCase().includes(input.value.toLowerCase().trim()));
    results.innerHTML = filtered.length ? filtered.map(([page, icon, label]) => `<a href="${page}.html"><i class="bi bi-${icon}" aria-hidden="true"></i><span>${label}<small>${descriptions[links.findIndex(item => item[0] === page)]}</small></span><span aria-hidden="true" style="flex:0">↗</span></a>`).join('') : '<div class="empty-state" role="status"><h3>No pages found</h3><p>Try “goals”, “reports” or “transactions”.</p></div>';
  };
  input.oninput = render;
  dialog.querySelector('[data-close]').onclick = () => dialog.close();
  dialog.addEventListener('keydown', event => {
    const items = [...results.querySelectorAll('a')];
    if (event.key === 'Enter' && document.activeElement === input && items.length) { event.preventDefault(); items[0].click(); }
    if (['ArrowDown', 'ArrowUp'].includes(event.key) && items.length) {
      event.preventDefault();
      const current = items.indexOf(document.activeElement);
      const next = current < 0 ? (event.key === 'ArrowDown' ? 0 : items.length - 1) : (current + (event.key === 'ArrowDown' ? 1 : items.length - 1)) % items.length;
      items[next].focus();
    }
  });
  dialog.addEventListener('close', () => dialog.remove());
  document.body.append(dialog); render(); dialog.showModal(); input.focus();
}

export function formModal({ title, fields, values = {}, submitLabel = 'Save changes', onSubmit }) {
  const dialog = document.createElement('dialog'); dialog.className = 'app-dialog';
  dialog.setAttribute('aria-labelledby', 'dialog-title');
  dialog.innerHTML = `<form><div class="dialog-heading"><h2 id="dialog-title">${escape(title)}</h2><button type="button" class="icon-button" data-close aria-label="Close dialog">×</button></div><div class="dialog-body">${fields}<p class="form-error" role="alert"></p></div><div class="dialog-footer"><button type="button" class="btn btn-light" data-close>Cancel</button><button type="submit" class="btn btn-primary">${escape(submitLabel)}</button></div></form>`;
  document.body.append(dialog);
  const form = dialog.querySelector('form');
  Object.entries(values).forEach(([key, value]) => { if (form.elements.namedItem(key)) form.elements.namedItem(key).value = value ?? ''; });
  dialog.querySelectorAll('[data-close]').forEach(button => { button.onclick = () => dialog.close(); });
  dialog.addEventListener('close', () => dialog.remove());
  form.onsubmit = async event => {
    event.preventDefault(); const button = form.querySelector('[type=submit]'); button.disabled = true; button.setAttribute('aria-busy', 'true');
    const errorBox = form.querySelector('.form-error'); errorBox.textContent = '';
    try { await onSubmit(Object.fromEntries(new FormData(form)), form); dialog.close(); }
    catch (error) { errorBox.textContent = error.message; }
    finally { button.disabled = false; button.removeAttribute('aria-busy'); }
  };
  dialog.showModal(); return form;
}
export function confirmDelete(label) {
  return new Promise(resolve => {
    const dialog = document.createElement('dialog'); dialog.className = 'app-dialog confirm-dialog'; dialog.setAttribute('aria-labelledby', 'confirm-title');
    dialog.innerHTML = `<div class="dialog-body"><span class="delete-icon"><i class="bi bi-trash3" aria-hidden="true"></i></span><h2 id="confirm-title">Delete ${escape(label)}?</h2><p>This record will be permanently removed. This action cannot be undone.</p></div><div class="dialog-footer"><button class="btn btn-light" data-cancel autofocus>Keep record</button><button class="btn btn-danger" data-delete>Delete record</button></div>`;
    document.body.append(dialog); let confirmed = false;
    dialog.querySelector('[data-cancel]').onclick = () => dialog.close();
    dialog.querySelector('[data-delete]').onclick = () => { confirmed = true; dialog.close(); };
    dialog.addEventListener('close', () => { dialog.remove(); resolve(confirmed); }); dialog.showModal();
  });
}
let fieldSequence = 0;
export function field(name, label, type = 'text', attrs = '') {
  const id = `field-${name}-${++fieldSequence}`;
  return `<div class="form-field"><label class="form-label" for="${id}">${escape(label)}</label><input class="form-control" id="${id}" name="${name}" type="${type}" ${attrs}></div>`;
}
export function selectField(name, label, options, attrs = '') {
  const id = `field-${name}-${++fieldSequence}`;
  return `<div class="form-field"><label class="form-label" for="${id}">${escape(label)}</label><select class="form-select" id="${id}" name="${name}" ${attrs}>${options.map(([value, text]) => `<option value="${escape(value)}">${escape(text)}</option>`).join('')}</select></div>`;
}
export const amountAttrs = 'required min="0.01" max="9999999999999.99" step="0.01" inputmode="decimal"';
export function progress(value, tone = '') {
  const bounded = Math.max(0, Math.min(100, Number(value)));
  return `<div class="progress ${tone}" role="progressbar" aria-label="Progress" aria-valuenow="${bounded}" aria-valuemin="0" aria-valuemax="100"><div class="progress-bar" style="width:${bounded}%"></div></div>`;
}
export function transactionTable(rows, editable = false) {
  if (!rows.length) return empty('No transactions found', 'Add a transaction or adjust your filters.');
  return `<div class="table-responsive"><table class="table finance-table"><thead><tr><th scope="col">Transaction</th><th scope="col">Category</th><th scope="col">Date</th><th scope="col">Payment</th><th scope="col" class="text-end">Amount</th>${editable ? '<th scope="col" class="text-end">Actions</th>' : ''}</tr></thead><tbody>${rows.map(t => `<tr><td><div class="transaction-label"><span class="tx-icon ${t.type.toLowerCase()}"><i class="bi bi-arrow-${t.type === 'INCOME' ? 'down-left' : 'up-right'}" aria-hidden="true"></i></span><div><strong>${escape(t.description || t.categoryName)}</strong><small>${t.type === 'INCOME' ? 'Income' : 'Expense'}</small></div></div></td><td><span class="category-badge">${escape(t.categoryName)}</span></td><td class="nowrap">${dateLabel(t.transactionDate)}</td><td>${escape(t.paymentMethod.replaceAll('_', ' '))}</td><td class="text-end nowrap ${t.type === 'INCOME' ? 'text-income' : ''}"><strong>${t.type === 'INCOME' ? '+' : '−'}${money(t.amount)}</strong></td>${editable ? `<td><div class="row-actions"><button class="icon-button" data-edit="${t.id}" aria-label="Edit transaction"><i class="bi bi-pencil" aria-hidden="true"></i></button><button class="icon-button danger" data-delete="${t.id}" aria-label="Delete transaction"><i class="bi bi-trash3" aria-hidden="true"></i></button></div></td>` : ''}</tr>`).join('')}</tbody></table></div>`;
}
