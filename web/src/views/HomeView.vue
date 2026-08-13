<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getActivityCategories, getActivitiesPage } from '../api/activity'
import { cityHubHeroImage, imageFallback } from '../config/activityImages'
import PageContainer from '../components/common/PageContainer.vue'
import PageLoading from '../components/common/PageLoading.vue'
import ErrorState from '../components/common/ErrorState.vue'
import SectionHeader from '../components/common/SectionHeader.vue'
import ActivityCard from '../components/business/ActivityCard.vue'

const router = useRouter()
const categories = ref([])
const activities = ref([])
const loading = ref(true)
const error = ref(false)
const keyword = ref('')
const categoryMap = computed(() => Object.fromEntries(categories.value.map((item) => [item.id, item.name])))
const featured = computed(() => activities.value.slice(0, 4))
const selected = computed(() => activities.value.slice(4, 8))
const weekend = computed(() => activities.value.slice(8, 12))

async function loadHome() { loading.value = true; error.value = false; try { const [categoryData, firstPage, secondPage] = await Promise.all([getActivityCategories(), getActivitiesPage({ current: 1, size: 10 }), getActivitiesPage({ current: 2, size: 10 })]); categories.value = categoryData || []; activities.value = [...(firstPage.data || []), ...(secondPage.data || [])] } catch { error.value = true } finally { loading.value = false } }
function search() { const value = keyword.value.trim(); router.push({ path: '/activities', query: value ? { keyword: value } : {} }) }
function browseCategory(categoryId) { router.push({ path: '/activities', query: categoryId ? { categoryId } : {} }) }
onMounted(loadHome)
</script>

<template>
  <PageContainer>
    <section class="hero"><div class="hero-copy"><p class="eyebrow">City culture, close at hand</p><h1>发现城市，<em>遇见有趣。</em></h1><p>展览、音乐、市集、讲座与周末体验，都在 CityHub。</p><form class="hero-search" @submit.prevent="search"><el-input v-model="keyword" placeholder="搜索活动、展览、演出、市集..." clearable /><el-button type="primary" native-type="submit">搜索活动</el-button></form></div><div class="hero-art"><img :src="cityHubHeroImage" alt="CityHub 城市文化活动插画" @error="imageFallback" /></div></section>
    <section class="category-section"><p class="eyebrow">Explore by mood</p><div class="category-row"><button class="category-pill" @click="browseCategory()">全部</button><button v-for="category in categories" :key="category.id" class="category-pill" @click="browseCategory(category.id)">{{ category.name }}</button></div></section>
    <PageLoading v-if="loading" /><ErrorState v-else-if="error" message="首页活动暂时无法加载。" @retry="loadHome" />
    <template v-else><section class="activity-section"><SectionHeader eyebrow="This week" title="本周热门" description="这些活动正让城市的周末变得更有意思。"><template #action><RouterLink class="section-link" to="/activities">查看全部活动 →</RouterLink></template></SectionHeader><div class="activity-grid"><ActivityCard v-for="item in featured" :key="item.id" :activity="item" :category-name="categoryMap[item.categoryId]" featured /></div></section><section class="activity-section muted"><SectionHeader eyebrow="City picks" title="城市精选" description="从街区里挑出的几场特别相遇。"><template #action><RouterLink class="section-link" to="/activities">查看全部活动 →</RouterLink></template></SectionHeader><div class="activity-grid"><ActivityCard v-for="item in selected" :key="item.id" :activity="item" :category-name="categoryMap[item.categoryId]" /></div></section><section class="activity-section"><SectionHeader eyebrow="Weekend notes" title="周末灵感" description="留一点时间，去做一件不赶路的事。"><template #action><RouterLink class="section-link" to="/activities">查看全部活动 →</RouterLink></template></SectionHeader><div class="activity-grid"><ActivityCard v-for="item in weekend" :key="item.id" :activity="item" :category-name="categoryMap[item.categoryId]" /></div></section></template>
    <section class="community-cta"><div><p class="eyebrow">City voices</p><h2>和同好一起发现城市</h2><p>一场活动结束后，把值得记住的片刻分享给同路人。</p></div><el-button type="primary" @click="router.push('/community')">去社区</el-button></section>
  </PageContainer>
</template>

<style scoped>
.hero { display:grid; grid-template-columns:1.1fr .9fr; overflow:hidden; min-height:440px; border:1px solid var(--color-border); border-radius:var(--radius-xl); background:var(--color-surface); box-shadow:var(--shadow-md); }.hero-copy { display:flex; flex-direction:column; justify-content:center; padding:clamp(32px, 5.4vw, 76px); }.hero h1 { max-width:560px; margin:var(--space-3) 0 var(--space-4); font-family:Georgia,"Songti SC",serif; font-size:clamp(3rem,5.2vw,5.35rem); line-height:.98; letter-spacing:-.075em; }.hero h1 em { color:var(--color-primary); font-style:normal; }.hero-copy > p:not(.eyebrow) { max-width:420px; margin:0; color:var(--color-text-secondary); font-size:1.04rem; }.hero-search { display:flex; gap:var(--space-2); max-width:450px; margin-top:var(--space-6); }.hero-search .el-button { flex:0 0 auto; }.hero-art { min-height:320px; background:var(--color-primary-soft); }.hero-art img { width:100%; height:100%; object-fit:cover; }.category-section { padding:var(--space-6) 0 var(--space-3); }.category-row { display:flex; gap:var(--space-2); margin-top:var(--space-3); overflow:auto; padding-bottom:4px; }.category-pill { flex:0 0 auto; padding:8px 14px; border:1px solid var(--color-border); border-radius:999px; color:var(--color-text-primary); background:var(--color-surface); cursor:pointer; transition:.2s; }.category-pill:hover { border-color:var(--color-primary); color:var(--color-primary); }.activity-section { padding-top:var(--space-7); }.activity-section.muted { margin-top:var(--space-7); padding:var(--space-7) var(--space-6); border-radius:var(--radius-xl); background:var(--color-surface-muted); }.activity-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:var(--space-5); }.section-link { float:right; margin-top:-48px; color:var(--color-primary); font-size:.9rem; font-weight:700; }.community-cta { display:flex; align-items:center; justify-content:space-between; gap:var(--space-5); margin-top:var(--space-8); padding:var(--space-7); border-radius:var(--radius-xl); color:#fff; background:var(--color-primary); }.community-cta h2 { margin:var(--space-2) 0; font-family:Georgia,"Songti SC",serif; font-size:2rem; letter-spacing:-.04em; }.community-cta p:not(.eyebrow) { margin:0; color:rgba(255,255,255,.76); }.community-cta .eyebrow { color:#f2c9b1; }.community-cta .el-button { color:var(--color-primary); border:0; background:#fff; }
@media (max-width:1023px) { .hero { grid-template-columns:1fr; }.hero-art { min-height:250px; }.activity-grid { grid-template-columns:repeat(2,1fr); }.activity-section.muted { padding:var(--space-6) var(--space-4); } }
@media (max-width:767px) { .hero-copy { min-height:370px; padding:var(--space-6); }.hero-search { flex-direction:column; }.hero-search .el-button { width:100%; }.hero-art { min-height:205px; }.activity-grid { grid-template-columns:1fr; gap:var(--space-4); }.activity-section { padding-top:var(--space-6); }.activity-section.muted { margin-top:var(--space-6); }.section-link { float:none; display:inline-block; margin-top:var(--space-2); }.community-cta { align-items:flex-start; flex-direction:column; margin-top:var(--space-7); padding:var(--space-6); }.community-cta h2 { font-size:1.7rem; } }
</style>
