<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getActivityImage, imageFallback } from '../../config/activityImages'
import { toggleBlogLike } from '../../api/blog'
import { setFollow } from '../../api/follow'
import { getFollowStatus } from '../../api/follow'
import { useUserStore } from '../../stores/user'

const props = defineProps({ blog: { type: Object, required: true }, activities: { type: Object, default: () => ({}) }, readOnly: Boolean })
const router = useRouter(); const route = useRoute(); const userStore = useUserStore(); const following = ref(false); const busy = ref(false)
const activity = computed(() => props.activities[props.blog.activityId])
const cover = computed(() => props.blog.images?.split(',')[0] || (activity.value ? getActivityImage(activity.value) : ''))
const isMine = computed(() => userStore.user?.id === props.blog.userId)
const date = computed(() => props.blog.createTime ? String(props.blog.createTime).replace('T', ' ').slice(0, 16) : '刚刚')
function login() { router.push({ name: 'login', query: { redirect: route.fullPath } }) }
async function like() { if (!userStore.isLoggedIn) return login(); if (busy.value) return; busy.value = true; try { await toggleBlogLike(props.blog.id); props.blog.isLike = !props.blog.isLike; props.blog.liked = Math.max(0, (props.blog.liked || 0) + (props.blog.isLike ? 1 : -1)) } finally { busy.value = false } }
async function follow() { if (!userStore.isLoggedIn) return login(); if (busy.value || isMine.value) return; busy.value = true; try { await setFollow(props.blog.userId, !following.value); following.value = !following.value; ElMessage.success(following.value ? '已关注这位作者' : '已取消关注') } finally { busy.value = false } }
onMounted(async () => { if (userStore.isLoggedIn && !isMine.value) { try { following.value = Boolean(await getFollowStatus(props.blog.userId)) } catch { following.value = false } } })
</script>

<template>
  <article class="blog-card">
    <div class="author-row"><el-avatar :size="42" :src="blog.icon">{{ blog.name?.slice(0, 1) || '城' }}</el-avatar><div class="author-copy"><strong>{{ blog.name || 'CityHub 参与者' }}</strong><small>{{ date }}</small></div><el-button v-if="!readOnly && !isMine" class="follow-button" plain size="small" :loading="busy" @click="follow">{{ following ? '已关注' : '+ 关注' }}</el-button></div>
    <RouterLink v-if="blog.activityId" class="activity-link" :to="`/activities/${blog.activityId}`">参加了 · {{ activity?.title || '相关活动' }} →</RouterLink>
    <h3>{{ blog.title }}</h3><p class="content">{{ blog.content }}</p>
    <RouterLink v-if="cover" class="blog-image" :to="blog.activityId ? `/activities/${blog.activityId}` : '/community'"><img :src="cover" :alt="blog.title" loading="lazy" @error="imageFallback" /></RouterLink>
    <div v-if="!readOnly" class="actions"><button :class="{ liked: blog.isLike }" @click="like">{{ blog.isLike ? '♥ 已点赞' : '♡ 点赞' }} <span>{{ blog.liked || 0 }}</span></button></div>
  </article>
</template>

<style scoped>
.blog-card{padding:var(--space-5);border:1px solid var(--color-border);border-radius:var(--radius-lg);background:var(--color-surface);box-shadow:var(--shadow-sm)}.author-row{display:flex;align-items:center;gap:var(--space-3)}.author-copy{display:grid;gap:2px}.author-copy small{color:var(--color-text-muted);font-size:.76rem}.follow-button{margin-left:auto;color:var(--color-primary);border-color:var(--color-primary)}.activity-link{display:inline-block;margin-top:var(--space-4);color:var(--color-primary);font-size:.84rem;font-weight:700}.blog-card h3{margin:var(--space-3) 0 var(--space-2);font-size:1.12rem}.content{display:-webkit-box;margin:0;color:var(--color-text-secondary);line-height:1.75;-webkit-box-orient:vertical;-webkit-line-clamp:3;overflow:hidden}.blog-image{display:block;margin-top:var(--space-4);overflow:hidden;border-radius:var(--radius-md);aspect-ratio:16/9;background:var(--color-surface-muted)}.blog-image img{width:100%;height:100%;object-fit:cover;transition:transform .22s}.blog-image:hover img{transform:scale(1.02)}.actions{display:flex;margin-top:var(--space-4);padding-top:var(--space-3);border-top:1px solid var(--color-border)}.actions button{border:0;background:none;color:var(--color-text-muted);cursor:pointer;font:inherit}.actions button.liked{color:var(--color-accent);font-weight:700}.actions span{margin-left:4px}@media(max-width:767px){.blog-card{padding:var(--space-4)}}
</style>
