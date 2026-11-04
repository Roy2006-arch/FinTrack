import { api, query } from './api.js';
import { $, setupPage, loadSection, action, toast, escape, money, percent, metric, progress, empty, formModal, confirmDelete, field, selectField, amountAttrs, monthNow } from './ui.js';
import { chart } from './charts.js';
let categories = [], budgets = [];
$('#budget-month').value = monthNow();
function openBudget(budget = null) {
  const period = $('#budget-month').value;
  formModal({ title: budget ? 'Edit monthly budget' : 'Create a monthly budget',
    fields: `${selectField('categoryId', 'Expense category', categories.filter(c => c.type !== 'INCOME').map(c => [c.id, c.name]), 'required')}<div class="form-row">${field('amount', 'Monthly limit (₹)', 'number', amountAttrs)}${field('period', 'Month', 'month', 'required min="2000-01" max="2100-12"')}</div><p class="form-text">One budget per category per month. Spending is calculated from your expense transactions.</p>`,
    values: budget ? { categoryId: budget.categoryId, amount: budget.amount, period: `${budget.year}-${String(budget.month).padStart(2, '0')}` } : { period },
    onSubmit: async data => {
      const [year, month] = data.period.split('-').map(Number); const body = { categoryId: Number(data.categoryId), amount: data.amount, year, month };
      if (budget) await api.put(`/budgets/${budget.id}`, body); else await api.post('/budgets', body);
      $('#budget-month').value = data.period; toast(budget ? 'Budget updated.' : 'Budget created.'); await refresh();
    }
  });
}
async function refresh() {
  if (!$('#budget-month').reportValidity()) return;
  await loadSection('#budgets-content', async () => {
    const [year, month] = $('#budget-month').value.split('-').map(Number);
    budgets = await api.get(`/budgets?${query({ month, year })}`);
    const allocated = budgets.reduce((sum, b) => sum + Number(b.amount), 0), spent = budgets.reduce((sum, b) => sum + Number(b.spent), 0);
    $('#budgets-content').innerHTML = `<div class="metrics-grid">${metric('Total allocated', money(allocated), 'pie-chart', 'Planned category limits', 'featured')}${metric('Amount spent', money(spent), 'credit-card', 'Within budgeted categories')}${metric('Remaining', money(allocated - spent), 'wallet2', 'Allocated minus spent')}${metric('Needs attention', budgets.filter(b => b.warningLevel !== 'NORMAL').length, 'exclamation-circle', 'Budgets at 75% or above')}</div>${budgets.length ? `<div class="cards-grid">${budgets.map(b => `<article class="finance-card"><div class="card-top"><span class="card-symbol"><i class="bi bi-pie-chart" aria-hidden="true"></i></span><span class="status-badge ${b.warningLevel.toLowerCase()}">${b.warningLevel === 'NORMAL' ? 'ON TRACK' : b.warningLevel}</span></div><h2>${escape(b.categoryName)}</h2><p class="section-note">Monthly spending plan</p><div class="card-amount">${money(b.spent)} <span>/ ${money(b.amount)}</span></div><div class="progress-labels"><span>${percent(b.usagePercentage)} used</span><span>${money(Math.abs(b.remaining))} ${Number(b.remaining) < 0 ? 'over' : 'left'}</span></div>${progress(b.usagePercentage, b.warningLevel.toLowerCase())}<div class="card-footer-line"><span>${b.month}/${b.year}</span><div class="row-actions"><button class="icon-button" data-edit="${b.id}" aria-label="Edit ${escape(b.categoryName)} budget"><i class="bi bi-pencil" aria-hidden="true"></i></button><button class="icon-button danger" data-delete="${b.id}" aria-label="Delete ${escape(b.categoryName)} budget"><i class="bi bi-trash3" aria-hidden="true"></i></button></div></div></article>`).join('')}</div><section class="panel mt-4"><div class="panel-heading"><h2>Plan versus reality</h2></div><div class="chart-wrap"><canvas id="budget-chart" role="img" aria-label="Budget limits compared with actual spending"></canvas></div></section>` : `<section class="panel">${empty('A new month. A new plan.', 'Create category budgets to keep your spending intentional.')}</section>`}<p class="section-note mt-4">Warning at 75% · Critical at 90% · Exceeded above 100%. Expenses outside budgeted categories are excluded from these totals.</p>`;
    chart('budget-chart', 'bar', budgets.map(b => b.categoryName), [{ label: 'Budget', data: budgets.map(b => b.amount) }, { label: 'Spent', data: budgets.map(b => b.spent) }]);
  });
}
await loadSection('#budgets-content', async () => {
  await setupPage('budgets'); categories = await api.get('/categories');
  $('#add-budget').disabled = false; $('#add-budget').onclick = () => openBudget();
  $('#budget-month').onchange = refresh;
  $('#budgets-content').onclick = event => {
    const edit = event.target.closest('[data-edit]'), remove = event.target.closest('[data-delete]');
    if (edit) openBudget(budgets.find(b => b.id === Number(edit.dataset.edit)));
    if (remove) action(async () => { if (await confirmDelete('budget')) { await api.delete(`/budgets/${remove.dataset.delete}`); toast('Budget deleted.'); await refresh(); } }, remove);
  };
  await refresh();
});
