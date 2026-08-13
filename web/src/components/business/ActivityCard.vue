<script setup>
defineProps({ activity: { type: Object, default: () => ({}) } })
</script>

<template>
  <article class="activity-card">
    <div class="cover" :style="activity.images ? { backgroundImage: `url(${activity.images})` } : {}"><span>{{ activity.categoryName || '城市活动' }}</span></div>
    <div class="card-body"><p class="card-meta">{{ activity.openHours || '时间信息即将公布' }}</p><h3>{{ activity.title || '一场值得期待的城市相遇' }}</h3><p class="location">{{ activity.area || activity.address || '探索城市里的新地点' }}</p><strong>{{ activity.avgPrice ? `¥${(activity.avgPrice / 100).toFixed(0)} 起` : '免费参与' }}</strong></div>
  </article>
</template>

<style scoped>
.activity-card { overflow:hidden; background:var(--color-surface); border:1px solid var(--color-border); border-radius:var(--radius-lg); box-shadow:var(--shadow-sm); }.cover { position:relative; min-height:164px; background:linear-gradient(140deg, #d6e3dc, #e9c7b5); background-size:cover; background-position:center; }.cover::after { content:''; position:absolute; inset:0; background:linear-gradient(180deg, transparent, rgba(32,33,31,.18)); }.cover span { position:absolute; z-index:1; top:var(--space-3); left:var(--space-3); padding:4px 8px; border-radius:999px; background:rgba(255,255,255,.9); color:var(--color-primary); font-size:.75rem; font-weight:700; }.card-body { padding:var(--space-4); }.card-meta { margin:0; color:var(--color-accent); font-size:.79rem; }.card-body h3 { margin:var(--space-2) 0; font-size:1.05rem; line-height:1.35; }.location { margin:0 0 var(--space-3); overflow:hidden; color:var(--color-text-secondary); font-size:.86rem; text-overflow:ellipsis; white-space:nowrap; }.card-body strong { color:var(--color-primary); font-size:.9rem; }
</style>
