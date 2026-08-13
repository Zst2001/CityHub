<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const userLabel = computed(() => userStore.user?.nickName || '我的 CityHub')

function goToProfile() { router.push('/profile') }
</script>

<template>
  <header class="app-header">
    <div class="content-container header-inner">
      <RouterLink class="brand" to="/" aria-label="CityHub 首页"><span class="brand-mark">C</span><span>CityHub</span></RouterLink>
      <nav class="header-nav" aria-label="主导航">
        <RouterLink to="/activities">发现活动</RouterLink>
        <RouterLink to="/community">活动社区</RouterLink>
      </nav>
      <div class="header-user">
        <el-button v-if="!userStore.isLoggedIn" type="primary" @click="router.push('/login')">登录</el-button>
        <el-button v-else class="profile-button" text @click="goToProfile">
          <el-avatar :size="30" :src="userStore.user?.icon"><el-icon><UserFilled /></el-icon></el-avatar>
          <span>{{ userLabel }}</span>
        </el-button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.app-header { position: sticky; top: 0; z-index: 20; background: color-mix(in srgb, var(--color-bg) 92%, transparent); border-bottom: 1px solid var(--color-border); backdrop-filter: blur(14px); }
.header-inner { min-height: 72px; display: flex; align-items: center; gap: var(--space-6); }
.brand { display: inline-flex; align-items: center; gap: 9px; color: var(--color-text-primary); font-size: 1.16rem; font-weight: 800; letter-spacing: -0.04em; }
.brand-mark { display: grid; width: 29px; height: 29px; place-items: center; border-radius: 9px 9px 9px 2px; color: #fff; background: var(--color-primary); font-family: Georgia, serif; font-size: 1.05rem; }
.header-nav { display: flex; align-items: center; gap: var(--space-5); color: var(--color-text-secondary); font-size: 0.93rem; }
.header-nav a { padding: 25px 0 21px; border-bottom: 3px solid transparent; }
.header-nav a.router-link-active { color: var(--color-primary); border-color: var(--color-accent); }
.header-user { margin-left: auto; }
.profile-button { color: var(--color-text-primary); }
.profile-button span { max-width: 130px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
@media (max-width: 767px) { .header-inner { min-height: 64px; gap: var(--space-4); } .header-nav { gap: var(--space-3); font-size: 0.85rem; } .header-nav a { padding: 21px 0 17px; } .profile-button span { display: none; } }
</style>
