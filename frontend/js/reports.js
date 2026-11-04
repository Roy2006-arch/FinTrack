import { api, query, downloadCsv } from './api.js';
import { $, setupPage, loadSection, action, toast, escape, money, metric, empty, dateLabel } from './ui.js';
import { chart } from './charts.js';
let selected = null;
function setPeriod() {
  const custom = $('#period').value === 'custom';
  $('#report-start').readOnly = custom ? false : true; $('#report-end').readOnly = custom ? false : true;
  if (custom) return;
  const date = new Date(); if ($('#period').value === 'previous') date.setMonth(date.getMonth() - 1, 1);
  const year = date.getFullYear(), month = date.getMonth() + 1;
  $('#report-start').value = `${year}-${String(month).padStart(2, '0')}-01`;
  $('#report-end').value = `${year}-${String(month).padStart(2, '0')}-${new Date(year, month, 0).getDate()}`;
}
async function generate() {
  const candidate = { start: $('#report-start').value, end: $('#report-end').value };
  if (!candidate.start || !candidate.end || candidate.start > candidate.end) { toast('Choose a valid date range with From on or before To.', true); return; }
  $('#export-report').disabled = true; $('#export-report-transactions').disabled = true;
  await loadSection('#reports-content', async () => {
    const [r, monthly] = await Promise.all([api.get(`/reports/date-range?${query(candidate)}`), api.get(`/dashboard/monthly?year=${candidate.end.slice(0, 4)}`)]);
    selected = candidate;
    $('#reports-content').innerHTML = `<p class="section-note">Report for ${dateLabel(r.start)} — ${dateLabel(r.end)} · All amounts in INR</p><div class="metrics-grid report-stats">${metric('Total income', money(r.totalIncome), 'arrow-down-left', 'For the selected period', 'featured')}${metric('Total expenses', money(r.totalExpense), 'arrow-up-right', 'For the selected period')}${metric('Net balance', money(r.netBalance), 'wallet2', 'Income minus expenses')}${metric('Transactions', r.transactionCount, 'arrow-left-right', 'Income and expense entries')}${metric('Highest expense', money(r.highestExpense), 'graph-up', 'Largest single expense')}${metric('Average transaction', money(r.averageTransaction), 'calculator', 'All transaction amounts / count')}</div><div class="report-grid"><section class="panel"><div class="panel-heading"><h2>Expense breakdown</h2></div>${r.categories.length ? `<div class="table-responsive"><table class="table finance-table"><thead><tr><th scope="col">Category</th><th scope="col" class="text-end">Total</th></tr></thead><tbody>${r.categories.map(c => `<tr><td>${escape(c.categoryName)}</td><td class="text-end">${money(c.total)}</td></tr>`).join('')}</tbody></table></div>` : empty('No expenses in this period', 'Try a different date range.')}</section><section class="panel"><div class="panel-heading"><h2>Spending distribution</h2></div>${r.categories.length ? '<div class="chart-wrap"><canvas id="report-category-chart" role="img" aria-label="Expense distribution for selected date range"></canvas></div>' : empty('Nothing to chart yet', 'Your expense categories will appear here.')}</section></div><section class="panel"><div class="panel-heading"><div><h2>Your spending over time</h2><p>Full calendar year ${candidate.end.slice(0, 4)} · shown for context, independently of the selected report dates</p></div></div><div class="chart-wrap"><canvas id="spending-chart" role="img" aria-label="Monthly spending for the report end year"></canvas></div></section>`;
    chart('report-category-chart', 'doughnut', r.categories.map(c => c.categoryName), [{ data: r.categories.map(c => c.total) }]);
    chart('spending-chart', 'line', monthly.map(m => new Date(`${m.month}-01T12:00:00`).toLocaleDateString('en', { month: 'short' })), [{ label: 'Expenses', data: monthly.map(m => m.expense), fill: true, backgroundColor: '#2f8b7020', borderColor: '#2f8b70' }]);
    $('#export-report').disabled = false; $('#export-report-transactions').disabled = false;
  });
}
await loadSection('#reports-content', async () => {
  await setupPage('reports'); setPeriod();
  $('#period').onchange = setPeriod;
  $('#report-filters').onsubmit = event => { event.preventDefault(); action(generate, event.submitter); };
  $('#export-report').onclick = event => action(() => downloadCsv(`/reports/export?${query(selected)}`, `fintrack-report-${selected.start}-${selected.end}.csv`), event.currentTarget);
  $('#export-report-transactions').onclick = event => action(() => downloadCsv(`/transactions/export?${query(selected)}`, `fintrack-transactions-${selected.start}-${selected.end}.csv`), event.currentTarget);
  await generate();
});
