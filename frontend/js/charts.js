const charts = new Map();
const colors = ['#2f8b70', '#b6d88f', '#d8a175', '#8fb8bd', '#d4a9c1', '#88c6a6', '#b7c593'];
export function chart(id, type, labels, datasets) {
  charts.get(id)?.destroy();
  const canvas = document.getElementById(id);
  if (!canvas) return;
  if (!window.Chart) {
    canvas.parentElement.innerHTML = '<p class="error-state">Charts could not load. Check your internet connection and reload.</p>'; return;
  }
  const dark = document.documentElement.dataset.bsTheme === 'dark';
  const text = dark ? '#9db2a9' : '#71817a';
  const grid = dark ? '#21453b' : '#e8eee8';
  const circular = type === 'doughnut';
  const instance = new Chart(canvas, {
    type,
    data: { labels, datasets: datasets.map((d, i) => ({ borderRadius: circular ? 5 : 6, borderWidth: circular ? 3 : type === 'line' ? 2.5 : 0, backgroundColor: circular ? colors : colors[i], borderColor: circular ? (dark ? '#1b1e2e' : '#ffffff') : colors[i], tension: 0.4, pointRadius: 0, pointHoverRadius: 5, hoverOffset: circular ? 7 : 0, maxBarThickness: 24, ...d })) },
    options: { responsive: true, maintainAspectRatio: false, animation: { duration: matchMedia('(prefers-reduced-motion: reduce)').matches ? 0 : 950, easing: 'easeOutQuart' }, cutout: circular ? '78%' : undefined,
      layout: { padding: { top: 10, left: 2, right: 2 } },
      interaction: { mode: circular ? 'nearest' : 'index', intersect: circular },
      plugins: { legend: { position: 'bottom', labels: { color: text, usePointStyle: true, pointStyle: 'circle', padding: 22, boxWidth: 6, boxHeight: 6, font: { family: 'DM Sans', size: 10 } } }, tooltip: { backgroundColor: dark ? '#08241f' : '#12382f', titleColor: '#f3faf3', bodyColor: '#d7eadc', padding: 13, cornerRadius: 11, displayColors: true, boxPadding: 5, callbacks: { label: c => ` ${c.dataset.label || c.label}: ₹${Number(c.raw).toLocaleString('en-IN')}` } } },
      scales: circular ? {} : { x: { border: { display: false }, grid: { display: false }, ticks: { color: text, font: { size: 10, family: 'DM Sans' } } }, y: { beginAtZero: true, border: { display: false }, grid: { color: grid }, ticks: { color: text, padding: 10, maxTicksLimit: 5, font: { size: 10, family: 'DM Sans' }, callback: v => v >= 1000 ? `₹${v / 1000}k` : `₹${v}` } } }
    },
    plugins: circular ? [{ id: 'centerLabel', afterDraw(chart) {
      const { ctx, chartArea } = chart;
      if (!chartArea || !chart.getDatasetMeta(0).data.length) return;
      const total = datasets[0].data.reduce((sum, value, index) => sum + (chart.getDataVisibility(index) ? Number(value) : 0), 0);
      const arc = chart.getDatasetMeta(0).data[0];
      ctx.save(); ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
      ctx.fillStyle = text; ctx.font = '10px "DM Sans", sans-serif'; ctx.fillText('TOTAL SPENDING', arc.x, arc.y - 12);
      ctx.fillStyle = dark ? '#edf6ef' : '#14372f'; ctx.font = '800 19px "Manrope", sans-serif';
      ctx.fillText(`₹${new Intl.NumberFormat('en-IN', { notation: 'compact', maximumFractionDigits: 1 }).format(total)}`, arc.x, arc.y + 12);
      ctx.restore();
    } }] : []
  });
  charts.set(id, instance);
}
document.addEventListener('themechange', () => {
  for (const [id, instance] of charts) {
    if (document.getElementById(id)) {
      const datasets = instance.data.datasets.map(dataset => {
        const next = { ...dataset };
        if (instance.config.type === 'doughnut') delete next.borderColor;
        return next;
      });
      chart(id, instance.config.type, instance.data.labels, datasets);
    }
  }
});
