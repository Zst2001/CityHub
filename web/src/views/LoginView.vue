<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, sendCode } from '../api/user'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const form = ref({ phone: '', code: '' })
const sending = ref(false)
const submitting = ref(false)
const seconds = ref(0)
let timer = null
const canSendCode = computed(() => /^1\d{10}$/.test(form.value.phone) && !sending.value && seconds.value === 0)

function startCountdown() {
  seconds.value = 60
  timer = window.setInterval(() => {
    seconds.value -= 1
    if (seconds.value <= 0) { window.clearInterval(timer); timer = null }
  }, 1000)
}

async function handleSendCode() {
  if (!/^1\d{10}$/.test(form.value.phone)) return ElMessage.warning('请输入正确的手机号')
  sending.value = true
  try { await sendCode(form.value.phone); ElMessage.success('验证码已发送'); startCountdown() } finally { sending.value = false }
}

async function handleLogin() {
  if (!/^1\d{10}$/.test(form.value.phone)) return ElMessage.warning('请输入正确的手机号')
  if (!form.value.code.trim()) return ElMessage.warning('请输入验证码')
  submitting.value = true
  try {
    const token = await login(form.value)
    userStore.setToken(token)
    await userStore.fetchCurrentUser()
    ElMessage.success('欢迎来到 CityHub')
    router.replace(route.query.redirect || '/')
  } catch {
    userStore.clearAuth()
  } finally { submitting.value = false }
}

onBeforeUnmount(() => { if (timer) window.clearInterval(timer) })

const adminMode = ref(false)
const adminForm = ref({ username: 'root', password: '' })
async function handleAdminLogin() {
  if (!adminForm.value.password) return ElMessage.warning('请输入密码')
  submitting.value = true
  try {
    const { adminLogin } = await import('../api/admin')
    const token = await adminLogin(adminForm.value)
    userStore.setToken(token); await userStore.fetchCurrentUser()
    router.replace(route.query.redirect || '/admin/activities')
  } finally { submitting.value = false }
}
</script>

<template>
<main class="login-page"><div class="login-scene" aria-hidden="true"><div class="scene-copy"><span>CityHub</span><p>把一座城市的<br /><em>好奇与相遇</em><br />留给今天。</p></div><div class="scene-route"><i></i><i></i><i></i></div></div><section class="login-panel"><RouterLink class="mobile-brand" to="/">CityHub</RouterLink><div class="form-wrap"><p class="eyebrow">Welcome back</p><template v-if="!adminMode"><h1>登录 CityHub</h1><p class="intro">用手机号进入你的活动地图。</p><el-form :model="form" label-position="top" @submit.prevent="handleLogin"><el-form-item label="手机号"><el-input v-model="form.phone" inputmode="numeric" maxlength="11" placeholder="请输入手机号" /></el-form-item><el-form-item label="验证码"><div class="code-row"><el-input v-model="form.code" inputmode="numeric" maxlength="6" placeholder="6 位验证码" /><el-button :disabled="!canSendCode" :loading="sending" @click="handleSendCode">{{ seconds ? `${seconds}s 后重试` : '获取验证码' }}</el-button></div></el-form-item><el-button class="login-submit" type="primary" native-type="submit" :loading="submitting">登录 / 注册</el-button></el-form><button class="admin-entry" type="button" @click="adminMode = true">管理员登录</button><p class="helper">首次登录将自动创建 CityHub 账户。</p></template><template v-else><h1>管理员登录</h1><el-form :model="adminForm" label-position="top" @submit.prevent="handleAdminLogin"><el-form-item label="用户名"><el-input v-model="adminForm.username" /></el-form-item><el-form-item label="密码"><el-input v-model="adminForm.password" type="password" show-password /></el-form-item><el-button class="login-submit" type="primary" native-type="submit" :loading="submitting">登录管理端</el-button></el-form><button class="admin-entry" type="button" @click="adminMode = false">返回用户登录</button></template><RouterLink class="back-home" to="/">← 返回首页</RouterLink></div></section></main>
</template>

<style scoped>
.admin-entry { display:block; width:100%; margin-top:12px; border:0; color:var(--color-primary); background:none; cursor:pointer; font:inherit; }
.login-page { display:grid; grid-template-columns:minmax(0, 1fr) minmax(440px, .86fr); min-height:100vh; background:var(--color-surface); }.login-scene { position:relative; overflow:hidden; padding:clamp(40px, 7vw, 100px); background:linear-gradient(150deg, #29493c, #426b5a 50%, #b16f4c); color:#fff; }.scene-copy { position:relative; z-index:2; }.scene-copy > span { display:inline-block; padding:5px 9px; border:1px solid rgba(255,255,255,.5); border-radius:var(--radius-sm); font-size:.82rem; font-weight:800; letter-spacing:.04em; }.scene-copy p { margin:var(--space-6) 0 0; font-family:Georgia, "Songti SC", serif; font-size:clamp(2.7rem, 5vw, 5rem); line-height:1.04; letter-spacing:-.07em; }.scene-copy em { color:#f5d5c2; font-style:normal; }.scene-route { position:absolute; inset:0; background:radial-gradient(circle at 68% 30%, rgba(255,255,255,.22) 0 3px, transparent 4px), radial-gradient(circle at 24% 70%, rgba(255,255,255,.26) 0 4px, transparent 5px); }.scene-route::before, .scene-route::after { content:''; position:absolute; width:130%; height:28px; left:-20%; background:rgba(255,255,255,.15); transform:rotate(-30deg); }.scene-route::before { top:42%; }.scene-route::after { top:69%; transform:rotate(18deg); }.scene-route i { position:absolute; width:13px; height:13px; border:3px solid #fff; border-radius:50%; background:var(--color-accent); }.scene-route i:nth-child(1) { top:33%; left:62%; }.scene-route i:nth-child(2) { top:64%; left:30%; }.scene-route i:nth-child(3) { bottom:15%; right:21%; }.login-panel { display:grid; place-items:center; padding:var(--space-6); }.form-wrap { width:min(380px, 100%); }.mobile-brand { display:none; color:var(--color-primary); font-weight:800; }.form-wrap h1 { margin:var(--space-2) 0; font-family:Georgia, "Songti SC", serif; font-size:2.45rem; letter-spacing:-.06em; }.intro, .helper { color:var(--color-text-secondary); }.intro { margin:0 0 var(--space-6); }.code-row { display:flex; gap:var(--space-2); width:100%; }.code-row .el-input { min-width:0; }.code-row .el-button { flex:0 0 116px; }.login-submit { width:100%; height:44px; margin-top:var(--space-2); }.helper { margin:var(--space-4) 0; font-size:.82rem; }.back-home { color:var(--color-primary); font-size:.88rem; font-weight:600; }
@media (max-width:767px) { .login-page { display:block; }.login-scene { display:none; }.login-panel { min-height:100vh; align-items:start; padding:var(--space-6) var(--space-4); }.mobile-brand { display:inline-block; margin:var(--space-2) 0 var(--space-8); }.form-wrap { width:100%; }.form-wrap h1 { font-size:2.2rem; }.code-row .el-button { flex-basis:108px; } }
</style>
