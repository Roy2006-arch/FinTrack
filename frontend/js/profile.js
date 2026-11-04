import { api } from './api.js';
import { $, setupPage, loadSection, action, toast, escape, formModal, confirmDelete, field, selectField, empty } from './ui.js';
let categories = [];
function openCategory(category = null) {
  formModal({ title: category ? 'Edit category' : 'Create a category',
    fields: `${field('name', 'Category name', 'text', 'required maxlength="60"')}${selectField('type', 'Use for', [['EXPENSE', 'Expenses'], ['INCOME', 'Income'], ['BOTH', 'Income and expenses']], 'required')}<p class="form-text">A category's type cannot change while transactions or budgets use it.</p>`,
    values: category || {},
    onSubmit: async data => {
      if (!data.name.trim()) throw new Error('Please enter a category name.');
      if (category) await api.put(`/categories/${category.id}`, data); else await api.post('/categories', data);
      toast(category ? 'Category updated.' : 'Category created.'); await refreshCategories();
    }
  });
}
async function refreshCategories() {
  await loadSection('#category-list', async () => {
    categories = await api.get('/categories');
    $('#category-list').innerHTML = categories.length ? `<div class="table-responsive"><table class="table finance-table"><thead><tr><th scope="col">Category</th><th scope="col">Type</th><th scope="col" class="text-end">Actions</th></tr></thead><tbody>${categories.map(c => `<tr><td>${escape(c.name)}</td><td><span class="category-badge">${c.type}</span></td><td><div class="row-actions"><button class="icon-button" data-edit="${c.id}" aria-label="Edit ${escape(c.name)}"><i class="bi bi-pencil" aria-hidden="true"></i></button><button class="icon-button danger" data-delete="${c.id}" aria-label="Delete ${escape(c.name)}"><i class="bi bi-trash3" aria-hidden="true"></i></button></div></td></tr>`).join('')}</tbody></table></div>` : empty('No categories yet', 'Create categories to organize your transactions.');
  });
}
await loadSection('#profile-content', async () => {
  const user = await setupPage('profile');
  $('#profile-content').innerHTML = `<div class="profile-grid"><section class="panel"><div class="panel-heading"><h2>Personal details</h2></div><form class="panel-body" id="profile-form">${field('name', 'Full name', 'text', 'required maxlength="100" autocomplete="name"')}${field('email', 'Email address', 'email', 'required maxlength="254" autocomplete="email"')}${field('phone', 'Phone number (optional)', 'tel', 'maxlength="20" autocomplete="tel"')}<p class="section-note">Member since ${new Date(user.createdAt).toLocaleDateString('en-IN', { month: 'long', year: 'numeric' })}. Changing your email also changes your sign-in email.</p><p class="form-error" id="profile-error" role="alert"></p><button class="btn btn-primary" type="submit">Save profile</button></form></section><section class="panel"><div class="panel-heading"><div><h2>Your categories</h2><p>Organize your money in a way that works for you</p></div><button class="btn btn-light" id="add-category"><i class="bi bi-plus-lg" aria-hidden="true"></i>Add</button></div><div id="category-list"></div><p class="section-note p-4 mb-0">Categories in use cannot be deleted. Reassign or delete their transactions and budgets first.</p></section></div>`;
  const form = $('#profile-form'); ['name', 'email', 'phone'].forEach(key => { form.elements[key].value = user[key] || ''; });
  form.onsubmit = async event => {
    event.preventDefault(); const button = event.submitter; button.disabled = true; $('#profile-error').textContent = '';
    try {
      const data = Object.fromEntries(new FormData(form));
      if (!data.name.trim()) throw new Error('Please enter your name.');
      if (!/^[+0-9 ()-]{0,20}$/.test(data.phone)) throw new Error('Enter a valid phone number using digits, spaces, +, parentheses or hyphens.');
      const saved = await api.put('/users/me', data); ['name', 'email', 'phone'].forEach(key => { form.elements[key].value = saved[key] || ''; });
      $('.user-avatar').textContent = saved.name.split(/\s+/).map(s => s[0]).slice(0, 2).join('').toUpperCase(); toast('Your profile is up to date.');
    } catch (error) { $('#profile-error').textContent = error.message; } finally { button.disabled = false; }
  };
  $('#add-category').onclick = () => openCategory();
  $('#category-list').onclick = event => {
    const edit = event.target.closest('[data-edit]'), remove = event.target.closest('[data-delete]');
    if (edit) openCategory(categories.find(c => c.id === Number(edit.dataset.edit)));
    if (remove) action(async () => { if (await confirmDelete('category')) { await api.delete(`/categories/${remove.dataset.delete}`); toast('Category deleted.'); await refreshCategories(); } }, remove);
  };
  await refreshCategories();
});
