<template>
  <footer class="app-footer">
    <div class="content-container footer-inner">
      <div><strong>CityHub</strong><span>发现城市里的精彩活动</span></div>
      <small>© 2026 CityHub</small>
    </div>
  </footer>
</template>

<style scoped>
.app-footer { border-top: 1px solid var(--color-border); background: var(--color-surface); }
.footer-inner { display: flex; align-items: center; justify-content: space-between; gap: var(--space-4); padding: var(--space-5) 0; color: var(--color-text-secondary); font-size: 0.85rem; }
.footer-inner div { display: flex; align-items: center; gap: var(--space-3); }.footer-inner strong { color: var(--color-primary); }.footer-inner small { color: var(--color-text-muted); }
@media (max-width: 767px) { .footer-inner { align-items: flex-start; flex-direction: column; gap: var(--space-2); }.footer-inner div { flex-direction: column; align-items: flex-start; gap: 2px; } }
</style>
