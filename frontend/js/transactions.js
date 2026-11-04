import { api, query, downloadCsv } from './api.js';
import { $, setupPage, loadSection, action, toast, escape, transactionTable, formModal, confirmDelete, field, selectField, amountAttrs, today } from './ui.js';

let categories = [], rows = [], page = 0, filters = {}, totalPages = 0;
const payments = ['CASH', 'UPI', 'CARD', 'BANK_TRANSFER', 'OTHER'].map(v => [v, v.replaceAll('_', ' ')]);
function openTransaction(transaction = null) {
  const form = formModal({ title: transaction ? 'Edit transaction' : 'Add transaction',
    fields: `<div class="form-row">${selectField('type', 'Transaction type', [['EXPENSE', 'Expense'], ['INCOME', 'Income']], 'required')}${field('amount', 'Amount (₹)', 'number', amountAttrs)}</div>${selectField('categoryId', 'Category', [], 'required')}${field('description', 'Description', 'text', 'maxlength="500" placeholder="What was it for?"')}<div class="form-row">${selectField('paymentMethod', 'Payment method', payments, 'required')}${field('transactionDate', 'Date', 'date', `required min="1000-01-01" max="${today()}"`)}</div>`,
    values: transaction || { type: 'EXPENSE', transactionDate: today(), paymentMethod: 'UPI' },
    onSubmit: async data => {
      data.categoryId = Number(data.categoryId);
      if (transaction) await api.put(`/transactions/${transaction.id}`, data); else await api.post('/transactions', data);
      toast(transaction ? 'Transaction updated.' : 'Transaction added.'); await refresh();
    }
  });
  function updateCategories() {
    const type = form.elements.type.value;
    const allowed = categories.filter(c => c.type === type || c.type === 'BOTH');
    form.elements.categoryId.innerHTML = allowed.length ? allowed.map(c => `<option value="${c.id}">${escape(c.name)}</option>`).join('') : '<option value="">Create a matching category in Profile first</option>';
  }
  form.elements.type.onchange = updateCategories; updateCategories();
  if (transaction) form.elements.categoryId.value = transaction.categoryId;
}
async function refresh() {
  await loadSection('#transaction-results', async () => {
    let result = await api.get(`/transactions?${query({ ...filters, page, size: 15 })}`);
    if (page > 0 && !result.content.length) { page = Math.max(0, result.totalPages - 1); result = await api.get(`/transactions?${query({ ...filters, page, size: 15 })}`); }
    rows = result.content; totalPages = result.totalPages;
    $('#transaction-results').innerHTML = `${transactionTable(rows, true)}<div class="pagination-bar"><span>${result.totalElements} transaction${result.totalElements === 1 ? '' : 's'} · Page ${page + 1} of ${Math.max(1, totalPages)}</span><div class="pagination-buttons"><button class="btn btn-light" id="previous-page" ${page === 0 ? 'disabled' : ''}>Previous</button><button class="btn btn-light" id="next-page" ${page + 1 >= totalPages ? 'disabled' : ''}>Next</button></div></div>`;
    $('#previous-page').onclick = () => { page--; refresh(); }; $('#next-page').onclick = () => { page++; refresh(); };
  });
}
await loadSection('#transactions-content', async () => {
  await setupPage('transactions'); categories = await api.get('/categories');
  $('#transactions-content').innerHTML = `<section class="panel"><form id="transaction-filters" class="panel-body"><div class="filter-grid"><div class="search-field">${field('search', 'Search transactions', 'search', 'maxlength="100" placeholder="Description or category"')}</div>${selectField('type', 'Type', [['', 'All types'], ['INCOME', 'Income'], ['EXPENSE', 'Expense']])}${selectField('categoryId', 'Category', [['', 'All categories'], ...categories.map(c => [c.id, c.name])])}${selectField('paymentMethod', 'Payment method', [['', 'All methods'], ...payments])}${field('start', 'From date', 'date')}${field('end', 'To date', 'date')}${selectField('sort', 'Sort by', [['transactionDate', 'Date'], ['amount', 'Amount']])}${selectField('direction', 'Order', [['desc', 'Highest / newest first'], ['asc', 'Lowest / oldest first']])}</div><div class="filter-footer"><span class="section-note">Search and filters are applied on the server.</span><div class="d-flex gap-2"><button type="button" class="btn btn-light" id="reset-filters">Reset</button><button class="btn btn-primary" type="submit">Apply filters</button></div></div></form></section><section class="panel"><div class="panel-heading"><h2>Transaction history</h2><a class="subtle-link" href="profile.html">Manage categories</a></div><div id="transaction-results" aria-live="polite"></div></section>`;
  $('#transaction-filters').onsubmit = event => {
    event.preventDefault();
    const candidate = Object.fromEntries(new FormData(event.currentTarget));
    if (candidate.start && candidate.end && candidate.start > candidate.end) { toast('From date must not be after To date.', true); return; }
    filters = candidate; page = 0; action(refresh, event.submitter);
  };
  $('#reset-filters').onclick = () => { $('#transaction-filters').reset(); filters = {}; page = 0; refresh(); };
  $('#transaction-results').onclick = event => {
    const edit = event.target.closest('[data-edit]'); const remove = event.target.closest('[data-delete]');
    if (edit) openTransaction(rows.find(t => t.id === Number(edit.dataset.edit)));
    if (remove) action(async () => { if (await confirmDelete('transaction')) { await api.delete(`/transactions/${remove.dataset.delete}`); toast('Transaction deleted.'); await refresh(); } }, remove);
  };
  $('#add-transaction').disabled = false; $('#export-transactions').disabled = false;
  $('#add-transaction').onclick = () => openTransaction();
  $('#export-transactions').onclick = event => action(() => downloadCsv(`/transactions/export?${query(filters)}`, 'fintrack-transactions.csv'), event.currentTarget);
  await refresh();
  if (new URLSearchParams(location.search).has('add')) { history.replaceState(null, '', 'transactions.html'); openTransaction(); }
});
