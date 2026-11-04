/* Small, dependency-free enhancements shared by public and signed-in pages. */
const reducedMotion = matchMedia('(prefers-reduced-motion: reduce)');
const enhanced = new WeakSet();
const animatedNumbers = new WeakSet();
const revealSelector = '.metric, .panel, .finance-card, .overview-hero, .feature-tile, .horizon-feature, .horizon-final, .horizon-hero-copy, .horizon-stage, .landing-final, .landing-hero-copy, .product-scene';

function countUp(element) {
  if (animatedNumbers.has(element) || reducedMotion.matches) return;
  animatedNumbers.add(element);
  const final = element.textContent;
  const number = Number(final.replace(/[^\d.\-]/g, ''));
  if (!Number.isFinite(number) || number === 0) return;
  const currency = final.includes('₹');
  const percentage = final.includes('%');
  const formatter = new Intl.NumberFormat('en-IN', currency
    ? { style: 'currency', currency: 'INR', minimumFractionDigits: 2, maximumFractionDigits: 2 }
    : { minimumFractionDigits: percentage ? 1 : 0, maximumFractionDigits: percentage ? 1 : 0 });
  const started = performance.now();
  function frame(now) {
    if (!element.isConnected) return;
    const progress = reducedMotion.matches ? 1 : Math.min(1, (now - started) / 950);
    element.textContent = progress === 1 ? final : `${formatter.format(number * (1 - (1 - progress) ** 4))}${percentage ? '%' : ''}`;
    if (progress < 1) requestAnimationFrame(frame);
  }
  requestAnimationFrame(frame);
}

const observer = 'IntersectionObserver' in window ? new IntersectionObserver(entries => {
  for (const entry of entries) {
    if (!entry.isIntersecting) continue;
    entry.target.classList.add('is-visible');
    entry.target.querySelectorAll('[data-count]').forEach(countUp);
    observer.unobserve(entry.target);
    // Release the entrance transform so each component's hover styles can take over.
    setTimeout(() => entry.target.classList.remove('reveal-ready', 'is-visible'), 1000);
  }
}, { threshold: .06 }) : null;

function enhance(root) {
  const elements = [...(root.matches?.(revealSelector) ? [root] : []), ...root.querySelectorAll(revealSelector)];
  elements.forEach((element, index) => {
    if (enhanced.has(element)) return;
    enhanced.add(element);
    if (element.matches('.finance-card, .feature-tile')) element.classList.add('spotlight');
    if (!observer || reducedMotion.matches) return;
    element.style.setProperty('--reveal-delay', `${Math.min(index % 4, 3) * 65}ms`);
    element.classList.add('reveal-ready');
    observer.observe(element);
  });
}

export function toggleTheme() {
  const theme = document.documentElement.dataset.bsTheme === 'dark' ? 'light' : 'dark';
  document.documentElement.dataset.bsTheme = theme;
  localStorage.setItem('fintrack-theme', theme);
  document.dispatchEvent(new Event('themechange'));
}

export function updateThemeButtons() {
  const dark = document.documentElement.dataset.bsTheme === 'dark';
  document.querySelectorAll('[data-theme-toggle], #theme-toggle').forEach(button => {
    button.innerHTML = `<i class="bi bi-${dark ? 'sun' : 'moon-stars'}" aria-hidden="true"></i>`;
    button.setAttribute('aria-label', `Switch to ${dark ? 'light' : 'dark'} theme`);
    button.title = `Switch to ${dark ? 'light' : 'dark'} theme`;
  });
}

export function celebrateGoal() {
  if (reducedMotion.matches) return;
  const burst = document.createElement('div');
  burst.className = 'goal-celebration';
  burst.setAttribute('aria-hidden', 'true');
  for (let index = 0; index < 18; index++) {
    const piece = document.createElement('span');
    piece.style.setProperty('--x', `${12 + index * 4.5}vw`);
    piece.style.setProperty('--delay', `${(index % 5) * 45}ms`);
    piece.style.setProperty('--spin', `${index % 2 ? -280 : 280}deg`);
    burst.append(piece);
  }
  document.body.append(burst);
  setTimeout(() => burst.remove(), 1600);
}

function boot() {
  const story = document.querySelector('[data-auth] .auth-story>div');
  if (story) {
    const art = document.createElement('div');
    art.className = 'auth-art';
    art.setAttribute('aria-hidden', 'true');
    art.innerHTML = '<div class="auth-art-card"><small>A LITTLE EVERY DAY</small><strong>Big things ahead.</strong><div><span>YOUR NEXT CHAPTER</span><i class="bi bi-arrow-up-right"></i></div></div><div class="auth-art-badge"><i class="bi bi-stars"></i>Good habits. Real progress.</div>';
    story.append(art);
    const theme = document.createElement('button');
    theme.className = 'icon-button auth-theme-toggle';
    theme.type = 'button';
    theme.dataset.themeToggle = '';
    document.querySelector('.auth-form-wrap').prepend(theme);
  }
  updateThemeButtons();
  enhance(document);
  new MutationObserver(records => {
    for (const record of records) {
      for (const node of record.addedNodes) if (node.nodeType === Node.ELEMENT_NODE) enhance(node);
    }
  }).observe(document.body, { childList: true, subtree: true });
  document.addEventListener('click', event => {
    if (event.target.closest('[data-theme-toggle]')) toggleTheme();
    const button = event.target.closest('.btn');
    if (!button || button.disabled || reducedMotion.matches || event.detail === 0) return;
    const bounds = button.getBoundingClientRect();
    const ripple = document.createElement('span');
    ripple.className = 'button-ripple';
    ripple.setAttribute('aria-hidden', 'true');
    ripple.style.left = `${event.clientX - bounds.left}px`;
    ripple.style.top = `${event.clientY - bounds.top}px`;
    button.append(ripple);
    setTimeout(() => ripple.remove(), 700);
  });
  document.addEventListener('pointermove', event => {
    if (event.pointerType !== 'mouse' || reducedMotion.matches) return;
    const card = event.target.closest('.spotlight');
    if (!card) return;
    const bounds = card.getBoundingClientRect();
    card.style.setProperty('--pointer-x', `${event.clientX - bounds.left}px`);
    card.style.setProperty('--pointer-y', `${event.clientY - bounds.top}px`);
  }, { passive: true });
  document.querySelectorAll('[data-preview-period]').forEach(button => {
    button.addEventListener('click', () => {
      const weekly = button.dataset.previewPeriod === 'week';
      document.querySelectorAll('[data-preview-period]').forEach(tab => tab.setAttribute('aria-pressed', String(tab === button)));
      const heights = weekly ? [40, 25, 73, 36, 58, 30, 89, 52, 66, 38, 96, 44] : [42, 24, 58, 39, 47, 28, 75, 42, 62, 32, 92, 48];
      document.querySelectorAll('.preview-chart span').forEach((bar, index) => bar.style.setProperty('--bar', `${heights[index]}%`));
      document.querySelector('.preview-months').innerHTML = (weekly ? ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'] : ['May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct']).map(label => `<span>${label}</span>`).join('');
      document.querySelector('.preview-chart').setAttribute('aria-label', `Illustrative ${weekly ? 'daily' : 'monthly'} income and expenses`);
    });
  });
}

document.addEventListener('themechange', updateThemeButtons);
reducedMotion.addEventListener('change', () => {
  if (reducedMotion.matches) document.querySelectorAll('.reveal-ready').forEach(element => element.classList.add('is-visible'));
});
if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', boot, { once: true });
else boot();
