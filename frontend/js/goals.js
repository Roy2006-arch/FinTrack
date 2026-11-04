import { api } from './api.js';
import { celebrateGoal } from './experience.js';
import { $, setupPage, loadSection, action, toast, escape, money, percent, metric, progress, empty, formModal, confirmDelete, field, amountAttrs, dateLabel, today } from './ui.js';
let goals = [];
function openGoal(goal = null) {
  formModal({ title: goal ? 'Update your goal' : 'Start a new savings goal',
    fields: `${field('goalName', 'What are you saving for?', 'text', 'required maxlength="100" placeholder="A new laptop, an adventure…"')}<div class="form-row">${field('targetAmount', 'Target amount (₹)', 'number', amountAttrs)}${field('savedAmount', 'Saved so far (₹)', 'number', 'required min="0" max="9999999999999.99" step="0.01" inputmode="decimal"')}</div>${field('deadline', 'Target date', 'date', 'required min="2000-01-01" max="2100-12-31"')}<p class="form-text">You can update your saved amount anytime. Past deadlines are allowed so you can track overdue goals.</p>`,
    values: goal || { savedAmount: 0, deadline: today() },
    onSubmit: async data => {
      if (!data.goalName.trim()) throw new Error('Please enter a goal name.');
      if (Number(data.savedAmount) > Number(data.targetAmount)) throw new Error('Saved amount must not exceed the target.');
      if (goal) await api.put(`/goals/${goal.id}`, data); else await api.post('/goals', data);
       toast(goal ? 'Goal updated. Keep going!' : 'Your new goal is ready.'); await refresh();
       if (Number(data.savedAmount) >= Number(data.targetAmount) && (!goal || Number(goal.progressPercentage) < 100)) celebrateGoal();
    }
  });
}
async function refresh() {
  await loadSection('#goals-content', async () => {
    goals = await api.get('/goals');
    const saved = goals.reduce((sum, g) => sum + Number(g.savedAmount), 0), target = goals.reduce((sum, g) => sum + Number(g.targetAmount), 0);
    $('#goals-content').innerHTML = `<div class="metrics-grid">${metric('Saved towards goals', money(saved), 'piggy-bank', 'Manually tracked savings', 'featured')}${metric('Total target', money(target), 'bullseye', 'Across all your goals')}${metric('Still to save', money(target - saved), 'flag', 'One step at a time')}${metric('Goals completed', goals.filter(g => Number(g.progressPercentage) >= 100).length, 'check-circle', `Out of ${goals.length} goals`)}</div>${goals.length ? `<div class="cards-grid">${goals.map(goalCard).join('')}</div>` : `<section class="panel">${empty('Every goal starts somewhere', 'Create a savings goal and take your first step.')}</section>`}`;
  });
}
function goalCard(g) {
  const complete = Number(g.progressPercentage) >= 100;
  const status = complete ? 'COMPLETED' : g.daysRemaining < 0 ? 'OVERDUE' : 'IN PROGRESS';
  const deadline = complete ? 'You made it!' : g.daysRemaining < 0 ? `${Math.abs(g.daysRemaining)} days overdue` : `${g.daysRemaining} days remaining`;
  return `<article class="finance-card"><div class="card-top"><span class="card-symbol"><i class="bi bi-${complete ? 'check-circle' : 'bullseye'}" aria-hidden="true"></i></span><span class="status-badge ${status === 'OVERDUE' ? 'critical' : ''}">${status}</span></div>
    <h2>${escape(g.goalName)}</h2><p class="section-note">Target: ${dateLabel(g.deadline)}</p>
    <div class="card-amount">${money(g.savedAmount)} <span>/ ${money(g.targetAmount)}</span></div>
    <div class="progress-labels"><span>${percent(g.progressPercentage)} saved</span><span>${money(g.remainingAmount)} to go</span></div>${progress(g.progressPercentage)}
    <div class="card-footer-line"><span>${deadline}</span><div class="row-actions"><button class="icon-button" data-edit="${g.id}" aria-label="Update ${escape(g.goalName)}"><i class="bi bi-pencil" aria-hidden="true"></i></button><button class="icon-button danger" data-delete="${g.id}" aria-label="Delete ${escape(g.goalName)}"><i class="bi bi-trash3" aria-hidden="true"></i></button></div></div></article>`;
}
await loadSection('#goals-content', async () => {
  await setupPage('goals'); $('#add-goal').disabled = false; $('#add-goal').onclick = () => openGoal();
  $('#goals-content').onclick = event => {
    const edit = event.target.closest('[data-edit]'), remove = event.target.closest('[data-delete]');
    if (edit) openGoal(goals.find(g => g.id === Number(edit.dataset.edit)));
    if (remove) action(async () => { if (await confirmDelete('savings goal')) { await api.delete(`/goals/${remove.dataset.delete}`); toast('Goal deleted.'); await refresh(); } }, remove);
  };
  await refresh();
});
