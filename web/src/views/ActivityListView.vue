<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getActivityCategories, getActivitiesByCategory, getActivitiesPage, searchActivitiesByName } from '../api/activity'
import PageContainer from '../components/common/PageContainer.vue'
import PageLoading from '../components/common/PageLoading.vue'
import EmptyState from '../components/common/EmptyState.vue'
import ErrorState from '../components/common/ErrorState.vue'
import SectionHeader from '../components/common/SectionHeader.vue'
import ActivityCard from '../components/business/ActivityCard.vue'

const route = useRoute(); const router = useRouter(); const categories = ref([]); const activities = ref([]); const total = ref(0); const loading = ref(false); const error = ref(false); const current = ref(1); const pageSize = 5; const draftKeyword = ref('')
const categoryMap = computed(() => Object.fromEntries(categories.value.map((item) => [item.id, item.name])))
const keyword = computed(() => route.query.keyword?.trim() || ''); const categoryId = computed(() => route.query.categoryId ? Number(route.query.categoryId) : null)
async function loadCategories() { categories.value = await getActivityCategories() || [] }
async function loadActivities() { loading.value=true; error.value=false; try { let result; if(keyword.value) { const data = await searchActivitiesByName({ name: keyword.value, current: current.value }); activities.value=data || []; total.value=data?.length || 0 } else if(categoryId.value) { const data = await getActivitiesByCategory({ categoryId: categoryId.value, current: current.value }); activities.value=data || []; total.value=data?.length || 0 } else { result=await getActivitiesPage({ current: current.value }); activities.value=result.data || []; total.value=result.total || 0 } } catch { error.value=true } finally { loading.value=false } }
function setQuery(next={}) { current.value=1; router.push({ path:'/activities', query:next }) }
function handleSearch() { setQuery(draftKeyword.value.trim() ? { keyword:draftKeyword.value.trim() } : {}) }
function clearFilters() { draftKeyword.value=''; setQuery() }
function handlePage(page) { current.value=page; loadActivities() }
watch(() => route.query, () => { draftKeyword.value=keyword.value; current.value=1; loadActivities() }, { deep:true })
onMounted(async()=>{ await loadCategories(); draftKeyword.value=keyword.value; await loadActivities() })
</script>

<template><PageContainer><section class="list-head"><SectionHeader eyebrow="Discover" title="发现活动" description="在城市里找到你的下一站。" /><form class="search-bar" @submit.prevent="handleSearch"><el-input v-model="draftKeyword" placeholder="搜索活动、展览、演出、市集..." clearable /><el-button type="primary" native-type="submit">搜索</el-button></form><div class="filters"><button :class="{active:!categoryId&&!keyword}" @click="clearFilters">全部</button><button v-for="item in categories" :key="item.id" :class="{active:categoryId===item.id}" @click="setQuery({categoryId:item.id})">{{ item.name }}</button></div></section><p v-if="!loading&&!error" class="result-count">{{ keyword ? `“${keyword}” 的搜索结果` : categoryId ? `${categoryMap[categoryId] || ''}活动` : '全部活动' }} · {{ total }} 个</p><PageLoading v-if="loading" /><ErrorState v-else-if="error" message="活动加载失败，请检查网络后重试。" @retry="loadActivities" /><EmptyState v-else-if="!activities.length" title="没找到相关活动" description="换个关键词，或者看看其他分类。"><el-button type="primary" @click="clearFilters">查看全部活动</el-button></EmptyState><template v-else><div class="activity-grid"><ActivityCard v-for="item in activities" :key="item.id" :activity="item" :category-name="categoryMap[item.categoryId]" /></div><div class="pagination"><el-pagination v-if="!keyword&&!categoryId" v-model:current-page="current" :page-size="pageSize" layout="prev, pager, next" :total="total" @current-change="handlePage" /></div></template></PageContainer></template>

<style scoped>.list-head { padding-bottom:var(--space-5); border-bottom:1px solid var(--color-border); }.search-bar { display:flex; max-width:560px; gap:var(--space-2); }.filters { display:flex; gap:var(--space-2); overflow:auto; margin-top:var(--space-5); padding-bottom:3px; }.filters button { flex:0 0 auto; padding:8px 14px; border:1px solid var(--color-border); border-radius:999px; color:var(--color-text-primary); background:var(--color-surface); cursor:pointer; }.filters button.active { border-color:var(--color-primary); color:#fff; background:var(--color-primary); }.result-count { margin:var(--space-5) 0; color:var(--color-text-secondary); font-size:.9rem; }.activity-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:var(--space-5); }.pagination { display:flex; justify-content:center; margin-top:var(--space-7); }@media(max-width:1023px){.activity-grid{grid-template-columns:repeat(2,1fr)}}@media(max-width:767px){.search-bar{flex-direction:column}.search-bar .el-button{width:100%}.activity-grid{grid-template-columns:1fr;gap:var(--space-4)}} </style>
