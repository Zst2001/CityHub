<script setup>
import PageContainer from '../components/common/PageContainer.vue'
import SectionHeader from '../components/common/SectionHeader.vue'
import ActivityCard from '../components/business/ActivityCard.vue'

const previewCards = [
  { categoryName: '展览', title: '城市里的展览、音乐与灵感', area: '你的周末，从一场相遇开始', openHours: '本周推荐', avgPrice: 0 },
  { categoryName: '市集', title: '把城市的好奇心带出门', area: '探索正在发生的文化现场', openHours: '即将上线', avgPrice: 0 },
  { categoryName: '体验', title: '留给生活一点亲手参与的时间', area: '手作、讲座与特别活动', openHours: '敬请期待', avgPrice: 0 },
]
</script>

<template>
  <PageContainer>
    <section class="hero"><div class="hero-copy"><p class="eyebrow">City culture, close at hand</p><h1>发现城市，<em>遇见有趣。</em></h1><p>展览、音乐、市集、讲座与周末体验，都在 CityHub。</p><div class="search-placeholder"><span>想去看什么？</span><el-button type="primary" @click="$router.push('/activities')">探索活动</el-button></div></div><div class="hero-map" aria-hidden="true"><span class="map-label one">河岸市集</span><span class="map-label two">夜间爵士</span><span class="map-label three">摄影展</span><i></i><i></i><i></i></div></section>
    <section class="intro-section"><SectionHeader eyebrow="Coming into focus" title="把周末交给城市" description="活动发现页正在准备真实数据接入；以下为 CityHub 的视觉与组件骨架。" /><div class="card-grid"><ActivityCard v-for="card in previewCards" :key="card.categoryName" :activity="card" /></div></section>
  </PageContainer>
</template>

<style scoped>
.hero { display:grid; grid-template-columns:1.15fr .85fr; overflow:hidden; min-height:438px; border:1px solid var(--color-border); border-radius:var(--radius-xl); background:var(--color-surface); box-shadow:var(--shadow-md); }.hero-copy { display:flex; flex-direction:column; justify-content:center; padding:clamp(32px, 6vw, 80px); }.hero h1 { max-width:560px; margin:var(--space-3) 0 var(--space-4); font-family:Georgia, "Songti SC", serif; font-size:clamp(2.9rem, 5.5vw, 5.5rem); line-height:.98; letter-spacing:-.075em; }.hero h1 em { color:var(--color-primary); font-style:normal; }.hero-copy > p:not(.eyebrow) { max-width:410px; margin:0; color:var(--color-text-secondary); font-size:1.04rem; }.search-placeholder { display:flex; align-items:center; justify-content:space-between; gap:var(--space-3); max-width:420px; margin-top:var(--space-6); padding:7px 8px 7px var(--space-4); border:1px solid var(--color-border); border-radius:var(--radius-md); color:var(--color-text-muted); background:var(--color-bg); }.hero-map { position:relative; overflow:hidden; min-height:320px; background:linear-gradient(140deg, #365b4b 0%, #426b5a 42%, #85a192 43%, #d9b18e 44%, #f1e3d7 100%); }.hero-map::before { content:''; position:absolute; width:150%; height:15px; top:45%; left:-20%; background:rgba(255,255,255,.76); transform:rotate(-25deg); box-shadow:0 130px 0 rgba(255,255,255,.52), 0 -115px 0 rgba(255,255,255,.42); }.hero-map::after { content:''; position:absolute; inset:18px; border:1px solid rgba(255,255,255,.48); border-radius:var(--radius-lg); }.map-label { position:absolute; z-index:1; padding:5px 9px; border-radius:999px; color:var(--color-text-primary); background:#fff; box-shadow:var(--shadow-sm); font-size:.76rem; font-weight:700; }.one { top:23%; left:20%; }.two { top:58%; right:18%; }.three { bottom:16%; left:35%; }.hero-map i { position:absolute; z-index:1; width:14px; height:14px; border:3px solid #fff; border-radius:50%; background:var(--color-accent); }.hero-map i:nth-of-type(1) { top:30%; left:52%; }.hero-map i:nth-of-type(2) { top:66%; left:26%; }.hero-map i:nth-of-type(3) { bottom:22%; right:34%; }.intro-section { padding-top:var(--space-8); }.card-grid { display:grid; grid-template-columns:repeat(3, 1fr); gap:var(--space-5); }
@media (max-width:1023px) { .hero { grid-template-columns:1fr; }.hero-map { min-height:220px; }.hero-copy { min-height:360px; }.card-grid { grid-template-columns:repeat(2, 1fr); } }
@media (max-width:767px) { .hero { min-height:auto; }.hero-copy { min-height:360px; padding:var(--space-6); }.search-placeholder { align-items:stretch; flex-direction:column; }.hero-map { min-height:185px; }.intro-section { padding-top:var(--space-7); }.card-grid { grid-template-columns:1fr; gap:var(--space-4); } }
</style>
