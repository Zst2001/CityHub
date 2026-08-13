<script setup>
import { computed } from 'vue'
import { getActivityImage, imageFallback } from '../../config/activityImages'

const props = defineProps({ activity: { type: Object, required: true }, categoryName: { type: String, default: '' }, featured: Boolean })
const image = computed(() => getActivityImage(props.activity))
const price = computed(() => props.activity.avgPrice ? `¥${(props.activity.avgPrice / 100).toFixed(0)} 起` : '免费参与')
</script>

<template>
  <RouterLink class="activity-card" :to="`/activities/${activity.id}`">
    <div class="cover"><img :src="image" :alt="activity.title" loading="lazy" @error="imageFallback" /><span>{{ categoryName || '城市活动' }}</span><b v-if="featured">热门</b></div>
    <div class="card-body"><p class="card-meta">{{ activity.openHours || '时间信息待公布' }}</p><h3>{{ activity.title }}</h3><p class="location">{{ activity.area || activity.address || '探索城市里的新地点' }}</p><strong>{{ price }}</strong></div>
  </RouterLink>
</template>

<style scoped>
.activity-card { display:block; overflow:hidden; background:var(--color-surface); border:1px solid var(--color-border); border-radius:var(--radius-lg); box-shadow:var(--shadow-sm); transition:transform .22s ease, box-shadow .22s ease; }.activity-card:hover { transform:translateY(-3px); box-shadow:var(--shadow-md); }.cover { position:relative; overflow:hidden; aspect-ratio:16 / 10; background:var(--color-surface-muted); }.cover img { width:100%; height:100%; object-fit:cover; transition:transform .25s ease; }.activity-card:hover .cover img { transform:scale(1.03); }.cover span, .cover b { position:absolute; z-index:1; top:var(--space-3); padding:4px 8px; border-radius:999px; font-size:.75rem; }.cover span { left:var(--space-3); background:rgba(255,255,255,.93); color:var(--color-primary); font-weight:700; }.cover b { right:var(--space-3); background:var(--color-accent); color:white; }.card-body { padding:var(--space-4); }.card-meta { margin:0; color:var(--color-accent); font-size:.79rem; }.card-body h3 { margin:var(--space-2) 0; font-size:1.05rem; line-height:1.35; }.location { margin:0 0 var(--space-3); overflow:hidden; color:var(--color-text-secondary); font-size:.86rem; text-overflow:ellipsis; white-space:nowrap; }.card-body strong { color:var(--color-primary); font-size:.9rem; }
</style>
