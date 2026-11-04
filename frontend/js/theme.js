(() => {
  const saved = localStorage.getItem('fintrack-theme');
  document.documentElement.dataset.bsTheme = saved || (matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
})();
